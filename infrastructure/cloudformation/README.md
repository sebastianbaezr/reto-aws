# CloudFormation - Reto AWS
## IaC Independiente para HU7, HU8, HU9

⚠️ **Nota**: Este stack es **completamente independiente** de los stacks de Serverless Framework (Java y Node). Se despliega por separado y no comparte recursos.

---

## 📋 Contenido

| Archivo | Descripción |
|---------|-------------|
| **reto-aws-main-stack.yaml** | Template CloudFormation completo (~600 líneas, ~35 recursos) |
| **validate-iam-access.sh** | Script para validar permisos IAM |
| **README.md** | Este archivo |

---

## 🎯 Qué Incluye el Template

### **HU7 - API Gateway + 8 Lambdas (CRUD)**
- ✅ 1 API Gateway (HTTP API v2) - **Independiente**
- ✅ 4 Endpoints: POST, GET, PUT, DELETE (/api/serverless/users)
- ✅ 4 Lambdas Java (java17)
- ✅ 4 Lambdas Node.js (nodejs20.x)

### **HU8 - DynamoDB (Privada)**
- ✅ Tabla: `cf-reto-aws-serverless-users-dev`
- ✅ Acceso SOLO desde Lambda (vía IAM)
- ✅ GSI en email
- ✅ PITR + Streams habilitados

### **HU9 - SQS + SNS (Email Notifications)**
- ✅ SQS Queue: `cf-reto-aws-serverless-users-user-created-dev`
- ✅ SNS Topic: `cf-reto-aws-serverless-users-email-notifications-dev`
- ✅ Event Source Mapping (SQS → Lambda)
- ✅ Políticas IAM restrictivas

---

## 📌 Independencia de Stacks

Este stack es **totalmente independiente**:

| Aspecto | CloudFormation | Serverless Java | Serverless Node |
|--------|---|---|---|
| **API Gateway** | Propia | Propia (crea) | Usa la de Java |
| **DynamoDB** | `cf-*` | `java-*` | `node-*` |
| **SQS/SNS** | `cf-*` | `java-*` | `node-*` |
| **Lambdas** | 8 (Java + Node) | 4 Java | 4 Node |
| **Despliegue** | Separado | Separado | Separado |

---

## 🚀 Pasos para Desplegar

### **Paso 1: Construir Artefactos**

```bash
# Java (4 Lambdas CRUD)
cd infrastructure/lambda/serverless-java
gradle clean build -x test
aws s3 cp build/libs/serverless-java-all.jar s3://tu-bucket/

# Node.js (4 Lambdas CRUD)
cd serverless-nodejs
npm install
npm run build  # Crea dist/ con dependencias de producción y ZIP para S3
aws s3 cp nodejs-lambda.zip s3://tu-bucket/
```

**Detalles del build de Java:**
- `gradle clean build -x test`: Compila y empaqueta todas las 4 Lambdas CRUD
- Genera: `build/libs/serverless-java-all.jar` (fat JAR con todas las dependencias)
- El JAR contiene: handlers, services, modelos, y todas las librerías necesarias

**Detalles del build de Node.js:**
- `npm install`: Instala todas las dependencias (dev + production)
- `npm run build`: Crea el paquete de despliegue:
  - Copia `src/` a `dist/`
  - Instala SOLO dependencias de producción en `dist/node_modules`
  - Crea `nodejs-lambda.zip` (3.6 MB aproximadamente)
- El ZIP contiene: `src/handlers`, `src/services`, `node_modules`, `package.json`

### **Paso 2: Validar Template**

```bash
cd infrastructure/cloudformation
aws cloudformation validate-template \
  --template-body file://reto-aws-main-stack.yaml \
  --region us-east-1
```

### **Paso 3: Crear Stack**

```bash
aws cloudformation create-stack \
  --stack-name reto-aws-serverless-users-cloudformation \
  --template-body file://infrastructure/cloudformation/reto-aws-main-stack.yaml \
  --parameters \
    ParameterKey=Environment,ParameterValue=dev \
    ParameterKey=ServiceName,ParameterValue=reto-aws-serverless-users \
    ParameterKey=JavaLambdaBucket,ParameterValue=tu-bucket \
    ParameterKey=JavaLambdaKey,ParameterValue=serverless-java-all.jar \
    ParameterKey=NodeLambdaBucket,ParameterValue=tu-bucket \
    ParameterKey=NodeLambdaKey,ParameterValue=nodejs-lambda.zip \
  --capabilities CAPABILITY_NAMED_IAM \
  --region us-east-1
```

### **Paso 4: Esperar Creación**

```bash
aws cloudformation wait stack-create-complete \
  --stack-name reto-aws-serverless-users-cloudformation \
  --region us-east-1

echo "✓ Stack creado exitosamente"
```

### **Paso 5: Obtener Outputs**

```bash
aws cloudformation describe-stacks \
  --stack-name reto-aws-serverless-users-cloudformation \
  --region us-east-1 \
  --query 'Stacks[0].Outputs'
```

Outputs que obtendrás:
- ApiGatewayEndpoint (URL del API)
- DynamoDBTableName
- SQSQueueUrl
- SNSTopicArn

---

## 🧪 Probar Endpoints

```bash
# Obtener endpoint
API_ENDPOINT="https://[api-id].execute-api.us-east-1.amazonaws.com/dev"

# Crear usuario
curl -X POST "$API_ENDPOINT/api/serverless/users" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"John","email":"john@example.com"}'

# Obtener usuario
curl "$API_ENDPOINT/api/serverless/users/{id}"

# Actualizar usuario
curl -X PUT "$API_ENDPOINT/api/serverless/users/{id}" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Jane","email":"jane@example.com"}'

# Eliminar usuario
curl -X DELETE "$API_ENDPOINT/api/serverless/users/{id}"
```

---

## ✅ Validar Acceso IAM

```bash
bash infrastructure/cloudformation/validate-iam-access.sh
```

Esto verifica:
- ✓ Rol IAM existe
- ✓ Permisos DynamoDB
- ✓ Permisos SQS
- ✓ Permisos SNS
- ✓ Lambdas creadas
- ✓ DynamoDB tabla
- ✓ SQS queue
- ✓ SNS topic
- ✓ API Gateway

---

## 📊 Arquitectura

Este stack tiene su **propia arquitectura independiente**:

```
CloudFormation Stack (INDEPENDIENTE)
│
├── API GATEWAY (Propia - cf-)
│   ├─ POST   → CreateUser (Java/Node)   → DynamoDB (cf-) + SQS (cf-)
│   ├─ GET    → GetUser    (Java/Node)   → DynamoDB (cf-)
│   ├─ PUT    → UpdateUser (Java/Node)   → DynamoDB (cf-)
│   └─ DELETE → DeleteUser (Java/Node)   → DynamoDB (cf-)
│
├── SQS Queue (cf-)
│   └─ EnviarCorreos Lambda → SNS Topic (cf-)
│
└── DynamoDB Table (cf-)
```

**Notas:**
- Los recursos de Serverless Java y Node tienen sus **propios** API Gateway, DynamoDB, SQS y SNS
- No hay compartición de recursos entre CloudFormation y Serverless
- Cada stack puede existir de forma independiente

---

## 🔐 Seguridad

| Componente | Acceso |
|-----------|--------|
| **DynamoDB** | Solo desde Lambda (IAM) ✅ |
| **SQS** | Solo CreateUser puede enviar ✅ |
| **SNS** | Solo EnviarCorreos puede publicar ✅ |
| **API** | Público HTTPS (pero DB es privada) ✅ |

---

## 📝 Ver Logs

```bash
# API Gateway
aws logs tail /aws/apigateway/reto-aws-serverless-users-dev --follow

# Lambda CreateUser (Java)
aws logs tail /aws/lambda/reto-aws-serverless-users-dev-createUserJava --follow

# Lambda CreateUser (Node)
aws logs tail /aws/lambda/reto-aws-serverless-users-dev-createUserNode --follow

# Lambda EnviarCorreos
aws logs tail /aws/lambda/reto-aws-serverless-users-dev-enviarCorreosJava --follow

# Ver todos los logs del stack
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/reto-aws-serverless-users
```

---

## 🗑️ Limpiar (Eliminar Stack)

⚠️ **Nota**: Si también desplegó Serverless Java y Node, debe eliminarlos PRIMERO (en orden inverso: Node → Java → CloudFormation).

```bash
# Eliminar Serverless Node (si existe)
cd serverless-nodejs
serverless remove --stage dev

# Eliminar Serverless Java (si existe)
cd infrastructure/lambda/serverless-java
serverless remove --stage dev

# Eliminar CloudFormation
aws cloudformation delete-stack \
  --stack-name reto-aws-serverless-users-cloudformation \
  --region us-east-1

aws cloudformation wait stack-delete-complete \
  --stack-name reto-aws-serverless-users-cloudformation \
  --region us-east-1

echo "✓ Stack eliminado"
```

---

## 📋 Parámetros CloudFormation

| Parámetro | Requerido | Por Defecto | Descripción |
|-----------|-----------|------------|-------------|
| JavaLambdaBucket | ✅ Sí | - | Bucket S3 del JAR Java |
| JavaLambdaKey | ✅ Sí | serverless-java-all.jar | Clave S3 del JAR Java |
| NodeLambdaBucket | ✅ Sí | - | Bucket S3 del ZIP Node.js |
| NodeLambdaKey | ✅ Sí | nodejs-lambda.zip | Clave S3 del ZIP Node.js |
| ServiceName | No | reto-aws-serverless-users | Nombre del servicio |
| Environment | No | dev | dev/staging/prod |

---

## 🔧 Troubleshooting

### Lambda no se invoca
```bash
aws lambda get-policy --function-name reto-aws-serverless-users-dev-createUserJava
aws apigatewayv2 get-integrations --api-id [api-id]
```

### Error de acceso a DynamoDB
```bash
aws iam get-role-policy \
  --role-name reto-aws-serverless-users-dev-lambda-execution-role \
  --policy-name DynamoDBAccess
```

### SQS no procesa mensajes
```bash
aws lambda list-event-source-mappings \
  --function-name reto-aws-serverless-users-dev-enviarCorreosJava

aws sqs get-queue-attributes \
  --queue-url [queue-url] \
  --attribute-names All
```

---

## ⚠️ Notas Importantes

- **Independencia**: Este stack NO comparte recursos con Serverless Java/Node
- **Recursos con prefijo `cf-`**: Todos los recursos tienen este prefijo para diferenciarse
- Las rutas S3 deben estar en la misma región AWS
- Se requiere `CAPABILITY_NAMED_IAM` para crear el rol
- DynamoDB es **privada** (sin acceso directo a internet)
- SQS tiene DLQ para mensajes fallidos (reintentos automáticos)
- Retention de logs CloudWatch: 7 días
- **Orden de eliminación**: Si desplegó también Serverless, elimine en orden inverso (Node → Java → CloudFormation)

---

## 📊 Estadísticas

- **Líneas del template**: ~600
- **Recursos creados**: ~35
- **Lambdas**: 10 (4 CRUD Java + 4 CRUD Node.js + 2 Email)
- **Políticas IAM**: 3 (DynamoDB, SQS, SNS)
- **Costo estimado**: Muy bajo (PAY_PER_REQUEST en DynamoDB)

---

## ✅ Checklist Antes de Desplegar

- [ ] Artefactos construidos (Java + Node.js)
- [ ] Subidos a S3
- [ ] AWS CLI configurado
- [ ] Template validado
- [ ] Parámetros correctos
- [ ] Bucket S3 existe

---

**¡Listo para desplegar!** 🚀
