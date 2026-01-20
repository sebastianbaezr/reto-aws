# Guía: Provisionar ECS desde AWS Console

Esta guía provisiona ECS Fargate de forma manual y económica (~$10-15/mes sin ALB).

## Prerequisitos

- Imagen Docker publicada en ECR: `041065662579.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:latest`
- Acceso a AWS Console
- Región: `us-east-1`

---

## Paso 1: Crear ECS Cluster

1. Ve a **ECS** → **Clusters**
2. Haz clic en **Create Cluster**
3. **Cluster name**: `reto-aws-cluster`
4. **Infrastructure**: selecciona **AWS Fargate**
5. Haz clic en **Create**

Espera 1-2 minutos a que se cree.

---

## Paso 2: Crear Task Definition

1. Ve a **ECS** → **Task definitions**
2. Haz clic en **Create new task definition**
3. Haz clic en **Create new task definition** (botón azul)

### Configuración Básica

- **Task definition family**: `reto-aws-api`
- **Launch type**: **AWS Fargate**
- **Operating system/Architecture**: Linux / x86_64
- **Network mode**: **awsvpc** (debe estar seleccionado)
- **CPU**: **0.25 vCPU** (256)
- **Memory**: **0.5 GB** (512)

### Container Definitions

1. Haz clic en **Add container**
2. **Container name**: `reto-aws-api`
3. **Image URI**:
   ```
   041065662579.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:latest
   ```
4. **Port mappings**:
   - **Container port**: `8080`
   - **Protocol**: `tcp`

5. Desplázate hacia abajo en **Log configuration**:
   - **Log driver**: `awslogs`
   - **Log group**: `/ecs/reto-aws-api`
   - **Log stream prefix**: `ecs`
   - **Region**: `us-east-1`

6. Haz clic en **Create**

Espera a que se cree la task definition.

---

## Paso 3: Crear Security Groups

### Security Group para ALB

1. Ve a **EC2** → **Security Groups**
2. Haz clic en **Create security group**
3. **Security group name**: `reto-aws-alb-sg`
4. **Description**: `Security group for ALB`
5. **VPC**: Selecciona `default` (o tu VPC)

**Inbound Rules**:
- **Type**: `HTTP`
- **Port range**: `80`
- **Source**: `0.0.0.0/0`

6. Haz clic en **Create security group**

### Security Group para ECS

1. Ve a **EC2** → **Security Groups**
2. Haz clic en **Create security group**
3. **Security group name**: `reto-aws-ecs-sg`
4. **Description**: `Security group for reto-aws ECS tasks`
5. **VPC**: Selecciona `default` (o tu VPC)

**Inbound Rules**:
- **Type**: `Custom TCP`
- **Port range**: `8080`
- **Source**: Selecciona el security group `reto-aws-alb-sg` (no 0.0.0.0/0)

6. Haz clic en **Create security group**

---

## Paso 4a: Crear Application Load Balancer

1. Ve a **EC2** → **Load Balancers**
2. Haz clic en **Create load balancer**
3. Selecciona **Application Load Balancer** → **Create**

### Configuración Básica

- **Name**: `reto-aws-alb`
- **Scheme**: `Internet-facing`
- **IP address type**: `IPv4`

### Network mapping

- **VPC**: `default` (o tu VPC)
- **Subnets**: Selecciona al menos 2 subnets disponibles

### Security groups

- Selecciona `reto-aws-alb-sg`

### Listeners and routing

- **Protocol**: `HTTP`
- **Port**: `80`
- **Default action**: Haz clic en **Create target group** (nueva pestaña)

### Crear Target Group

1. En la nueva pestaña:
   - **Target type**: `IP`
   - **Target group name**: `reto-aws-tg`
   - **Protocol**: `HTTP`
   - **Port**: `8080`
   - **VPC**: `default`

2. Haz clic en **Next**
3. Skips el paso de registrar targets (ECS lo hará automáticamente)
4. Haz clic en **Create target group**

### Volver al ALB

5. En la primera pestaña, actualiza el **Default action** con el target group `reto-aws-tg`
6. Haz clic en **Create load balancer**

Espera 2-3 minutos a que se active (estado: **Active**)

---

## Paso 4b: Crear ECS Service con AWS CLI

### Opción 1: Script Automático (Recomendado)

Ejecuta este script bash que configura todo automáticamente:

```bash
#!/bin/bash

# Variables
CLUSTER="reto-aws-cluster"
SERVICE="reto-aws-api-service"
TASK_DEFINITION="reto-aws-api"
REGION="us-east-1"
DESIRED_COUNT=1

# Obtener información del target group
ALB_TG_ARN=$(aws elbv2 describe-target-groups \
  --region $REGION \
  --query 'TargetGroups[?TargetGroupName==`reto-aws-tg`].TargetGroupArn' \
  --output text)

# Obtener subnets de la VPC default
SUBNETS=$(aws ec2 describe-subnets \
  --region $REGION \
  --filters "Name=default-for-az,Values=true" \
  --query 'Subnets[*].SubnetId' \
  --output text)

# Obtener security group
SECURITY_GROUP=$(aws ec2 describe-security-groups \
  --region $REGION \
  --filters "Name=group-name,Values=reto-aws-ecs-sg" \
  --query 'SecurityGroups[0].GroupId' \
  --output text)

# Crear el servicio
aws ecs create-service \
  --cluster $CLUSTER \
  --service-name $SERVICE \
  --task-definition $TASK_DEFINITION:1 \
  --desired-count $DESIRED_COUNT \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[$SUBNETS],
    securityGroups=[$SECURITY_GROUP],
    assignPublicIp=ENABLED
  }" \
  --load-balancers targetGroupArn=$ALB_TG_ARN,containerName=reto-aws-api,containerPort=8080 \
  --health-check-grace-period-seconds 60 \
  --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100,deploymentCircuitBreaker={enable=true,rollback=true}" \
  --region $REGION

echo "Service creado: $SERVICE"
echo "ALB Target Group: $ALB_TG_ARN"
echo "Subnets: $SUBNETS"
echo "Security Group: $SECURITY_GROUP"
```

Guarda el script como `create-service.sh` y ejecuta:

```bash
chmod +x create-service.sh
./create-service.sh
```

---

### Opción 2: Comando Manual

Si prefieres hacerlo manualmente, necesitas obtener primero los valores:

```bash
# 1. Obtener ARN del Target Group
aws elbv2 describe-target-groups \
  --region us-east-1 \
  --filters "Name=target-group-name,Values=reto-aws-tg" \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text

# Salida: arn:aws:elasticloadbalancing:us-east-1:041065662579:targetgroup/reto-aws-tg/f09142a99118cdb7
```

```bash
# 2. Obtener Subnets
aws ec2 describe-subnets \
  --region us-east-1 \
  --filters "Name=default-for-az,Values=true" \
  --query 'Subnets[*].SubnetId' \
  --output text

# Salida: subnet-xxxx subnet-yyyy subnet-zzzz
```

```bash
# 3. Obtener Security Group
aws ec2 describe-security-groups \
  --region us-east-1 \
  --filters "Name=group-name,Values=reto-aws-ecs-sg" \
  --query 'SecurityGroups[0].GroupId' \
  --output text

# Salida: sg-0b0aec43ce706c6c7
```

Luego ejecuta:

```bash
aws ecs create-service \
  --cluster reto-aws-cluster \
  --service-name reto-aws-api-service \
  --task-definition reto-aws-api:1 \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[subnet-xxxx,subnet-yyyy,subnet-zzzz],
    securityGroups=[sg-0b0aec43ce706c6c7],
    assignPublicIp=ENABLED
  }" \
  --load-balancers \
    targetGroupArn=arn:aws:elasticloadbalancing:us-east-1:041065662579:targetgroup/reto-aws-tg/f09142a99118cdb7,\
    containerName=reto-aws-api,\
    containerPort=8080 \
  --health-check-grace-period-seconds 60 \
  --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100,deploymentCircuitBreaker={enable=true,rollback=true}" \
  --region us-east-1
```

**IMPORTANTE**: Reemplaza los valores:
- `subnet-xxxx,subnet-yyyy,subnet-zzzz` - con tus subnets (mínimo 2)
- `sg-0b0aec43ce706c6c7` - con tu security group ID
- `arn:aws:elasticloadbalancing:...` - con tu target group ARN

Espera 2-3 minutos a que se cree y lance la tarea.

---

## Paso 5: Obtener DNS del ALB y Acceder

1. Ve a **EC2** → **Load Balancers**
2. Selecciona `reto-aws-alb`
3. Copia el **DNS name** (ejemplo: `reto-aws-alb-123456.us-east-1.elb.amazonaws.com`)

### Prueba la aplicación

```bash
# Crear persona (sin puerto, el ALB maneja el puerto 80)
curl -X POST http://reto-aws-alb-123456.us-east-1.elb.amazonaws.com/api/people \
  -H "Content-Type: application/json" \
  -d '{
    "identification": "12345657129",
    "name": "Kevin Alza",
    "email": "kevin.alza@example.com"
  }'

# Respuesta esperada:
# {"id": 1, "identification": "12345657129", "name": "Kevin Alza", "email": "kevin.alza@example.com"}

# Obtener persona
curl http://reto-aws-alb-123456.us-east-1.elb.amazonaws.com/api/people/1
```

**Nota**: Si el ALB retorna error 502 Bad Gateway, espera 1-2 minutos más a que el health check del target group sea "Healthy".

---

## Ver Logs

1. Ve a **CloudWatch** → **Log groups**
2. Selecciona `/ecs/reto-aws-api`
3. Busca el log stream más reciente

O con AWS CLI:

```bash
aws logs tail /ecs/reto-aws-api --follow
```

---

## Actualizar Aplicación

Cuando hagas cambios a la aplicación:

1. **Rebuild y push** la imagen Docker:
   ```bash
   docker build -f deployment/Dockerfile -t 041065662579.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:v1.0.0 .
   docker push 041065662579.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:v1.0.0
   ```

2. Ve a **ECS Task Definitions** → `reto-aws-api`

3. Haz clic en **Create new revision**

4. Actualiza la **Image URI** con la nueva versión:
   ```
   041065662579.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:v1.0.0
   ```

5. Haz clic en **Create**

6. Ve al Service → **Update service**

7. En **Revision**, selecciona la nueva revisión

8. Haz clic en **Update**

---

## Detener el Service (para ahorrar dinero)

Si no necesitas la aplicación corriendo:

1. Ve a **ECS** → **Clusters** → `reto-aws-cluster`
2. Selecciona el service `reto-aws-api-service`
3. Haz clic en **Update service**
4. Cambia **Desired count** a `0`
5. Haz clic en **Update**

Esto mata las tareas pero mantiene la configuración.

---

## Eliminar Todo (Clean Up)

Si quieres eliminar completamente:

1. **Service**: Ve a ECS → Service → Delete
2. **Task Definition**: Ve a ECS → Task Definition → Mark as inactive + Delete
3. **Cluster**: Ve a ECS → Cluster → Delete
4. **Security Group**: Ve a EC2 → Security Groups → Delete (si no lo usas)
5. **CloudWatch Logs**: Ve a CloudWatch → Log Groups → Delete

---

## Costos Aproximados

| Recurso | Costo/mes |
|---------|-----------|
| Fargate (256 CPU, 512 MB) | ~$10-15 |
| Application Load Balancer | ~$5 |
| CloudWatch Logs | ~$1-2 |
| **Total** | **~$16-22/mes** |

Con ALB el costo aumenta ~$5/mes, pero es necesario para distribuir tráfico y alta disponibilidad.

---

## Troubleshooting

### La tarea no arranca

1. Verifica **Task details** → **Logs**
2. Revisa CloudWatch Logs
3. Revisa el Security Group (puerto 8080 abierto)
4. Verifica que la imagen de ECR exista

### "CannotPullContainerImage"

- La URL de ECR es incorrecta
- Los permisos de IAM no están configurados

Solución:
```bash
# Verifica que la imagen existe
aws ecr describe-images --repository-name reto-aws-api --region us-east-1
```

### "Insufficient memory"

- La memoria reservada en la tarea es muy baja
- Aumenta a 1GB

### No hay IP Pública

- Verifica que "Auto-assign public IP" está **ENABLED** en el service

---

## Monitoreo

### En AWS Console

1. **ECS** → **Clusters** → `reto-aws-cluster` → **Services**
2. Ver estado de tareas: `RUNNING`, `PROVISIONING`, `STOPPED`
3. **CloudWatch** → **Log groups** → `/ecs/reto-aws-api`

### Con AWS CLI

```bash
# Ver tareas corriendo
aws ecs list-tasks --cluster reto-aws-cluster --region us-east-1

# Describir tarea específica
aws ecs describe-tasks --cluster reto-aws-cluster --tasks <TASK_ARN> --region us-east-1

# Ver logs
aws logs tail /ecs/reto-aws-api --follow --region us-east-1
```

---

## Próximos Pasos

- Configurar dominio personalizado (Route 53)
- Agregar HTTPS con ACM
- Auto Scaling (escala automáticamente por CPU/memoria)
- CI/CD con GitHub Actions para actualizar automáticamente
