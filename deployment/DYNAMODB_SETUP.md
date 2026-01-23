# Configuración Manual de DynamoDB

## Descripción

Este documento proporciona instrucciones para crear manualmente la tabla DynamoDB necesaria para la aplicación HU8. La tabla es compartida por las 4 funciones Lambda de Java y las 4 funciones Lambda de Node.js.

## Tabla DynamoDB

- **Nombre:** `reto-aws-serverless-users-dev`
- **Partition Key:** `id` (String)
- **Global Secondary Index (GSI):** `email-index` (para validación de email único)
- **Billing Mode:** PAY_PER_REQUEST (bajo demanda)
- **Región:** us-east-1

## Crear la Tabla

### Opción 1: AWS CLI (Recomendada)

Ejecuta el siguiente comando en tu terminal (asegúrate de tener AWS CLI configurado):

```bash
aws dynamodb create-table \
  --table-name reto-aws-serverless-users-dev \
  --attribute-definitions \
    AttributeName=id,AttributeType=S \
    AttributeName=email,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes \
    IndexName=email-index,KeySchema=[{AttributeName=email,KeyType=HASH}],Projection={ProjectionType=ALL} \
  --region us-east-1
```

### Opción 2: AWS Console (Manual)

1. **Abre AWS Console:**
   - Ve a https://console.aws.amazon.com/
   - Busca "DynamoDB" o ve a Services > DynamoDB

2. **Crear tabla:**
   - Click en "Create table"
   - **Table name:** `reto-aws-serverless-users-dev`
   - **Partition key:** `id` (String)
   - Click "Create"

3. **Agregar índice GSI:**
   - Una vez creada la tabla, ve a la pestaña "Indexes"
   - Click "Create global secondary index"
   - **Index name:** `email-index`
   - **Partition key:** `email` (String)
   - **Projection:** All
   - Click "Create index"

4. **Configurar Billing:**
   - Ve a la pestaña "General"
   - Click "Edit" en Capacity/Billing
   - Cambia a **"On-demand"** (PAY_PER_REQUEST)
   - Click "Save"

## Verificar la Tabla

Una vez creada, verifica que:

1. La tabla existe y se llama `reto-aws-serverless-users-dev`
2. Tiene las siguientes columnas:
   - `id` (String, Partition Key)
   - `nombre` (String)
   - `email` (String)
3. El índice `email-index` existe y está en estado **ACTIVE** (puede tomar 1-2 minutos)

### Comando para verificar:

```bash
aws dynamodb describe-table \
  --table-name reto-aws-serverless-users-dev \
  --region us-east-1
```

## Estructuras de Datos

### Atributos de la Tabla

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",  // UUID (String)
  "nombre": "Juan Pérez",                         // Nombre del usuario (String)
  "email": "juan.perez@example.com"               // Email del usuario (String)
}
```

### Índice Global Secundario (email-index)

El índice `email-index` permite consultas rápidas por email sin hacer un scan completo de la tabla:

- **Partition Key:** `email`
- **Projection:** ALL (todos los atributos)

## Permisos IAM

Las funciones Lambda de Java y Node.js tienen los siguientes permisos en DynamoDB:

- `dynamodb:GetItem` - Obtener un usuario por ID
- `dynamodb:PutItem` - Crear/actualizar un usuario
- `dynamodb:UpdateItem` - Actualizar atributos
- `dynamodb:DeleteItem` - Eliminar un usuario
- `dynamodb:Query` - Consultar usando índices
- `dynamodb:Scan` - Escanear la tabla (fallback)

Estos permisos están definidos en:
- `infrastructure/lambda/serverless-java/serverless.yml`
- `serverless-nodejs/serverless.yml`

## Operaciones Disponibles

Una vez que la tabla existe y las Lambdas están desplegadas, puedes:

### Crear Usuario

```bash
curl -X POST https://{api-id}.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan.perez@example.com"
  }'
```

**Respuesta (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com"
}
```

### Obtener Usuario

```bash
curl https://{api-id}.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/{id}
```

### Actualizar Usuario

```bash
curl -X PUT https://{api-id}.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez Actualizado",
    "email": "nuevo.email@example.com"
  }'
```

### Eliminar Usuario

```bash
curl -X DELETE https://{api-id}.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/{id}
```

## Ver Datos en AWS Console

1. Ve a **DynamoDB > Tables > reto-aws-serverless-users-dev**
2. Click en la pestaña **"Explore items"** o **"Items"**
3. Ahí verás todos los usuarios creados

## Troubleshooting

### Error: "Requested resource not found"

**Causa:** El índice `email-index` aún no está en estado ACTIVE.

**Solución:** Espera 1-2 minutos y vuelve a intentar.

### Error: "User access not found"

**Causa:** La tabla no existe o está en otra región.

**Solución:**
- Verifica que la tabla existe: `aws dynamodb list-tables --region us-east-1`
- Si no existe, créala siguiendo las instrucciones arriba

### Error: "AccessDeniedException"

**Causa:** Las Lambdas no tienen permisos para acceder DynamoDB.

**Solución:**
- Redeploya las Lambdas para que se asignen los permisos IAM:
  ```bash
  cd infrastructure/lambda/serverless-java
  serverless deploy --stage dev

  cd ../../serverless-nodejs
  serverless deploy --stage dev
  ```

## Información de Seguridad

- La tabla DynamoDB es **privada** y no está expuesta al público
- Solo se puede acceder a través de las Lambdas que tienen permisos IAM específicos
- Los usuarios finales acceden a los datos **únicamente** a través del API Gateway (público)
- La comunicación entre API Gateway y Lambda es interna de AWS
- No hay acceso directo desde internet a DynamoDB

## Variables de Entorno

Las Lambdas usan la siguiente variable de entorno para conectar a la tabla:

```
DYNAMODB_TABLE_NAME=reto-aws-serverless-users-dev
```

Esta está definida en los `serverless.yml` de ambas implementaciones.

## Próximos Pasos

1. ✅ Crear la tabla DynamoDB manualmente
2. ✅ Desplegar Lambdas Java: `cd infrastructure/lambda/serverless-java && serverless deploy --stage dev`
3. ✅ Desplegar Lambdas Node.js: `cd serverless-nodejs && serverless deploy --stage dev`
4. ✅ Probar endpoints con curl o Postman
5. ✅ Verificar datos en AWS DynamoDB Console

## Referencias

- [AWS DynamoDB Documentation](https://docs.aws.amazon.com/dynamodb/)
- [AWS CLI DynamoDB Commands](https://docs.aws.amazon.com/cli/latest/reference/dynamodb/)
- [DynamoDB Best Practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)
