# Guía: Crear Repositorio ECR y Publicar Imagen Docker

## Requisitos Previos

- AWS CLI instalado y configurado
- Docker instalado
- Imagen Docker construida localmente: `reto-aws-api:latest`
- Permisos en AWS para crear repositorios ECR

## Verificar Configuración de AWS CLI

Asegúrate de que AWS CLI está correctamente configurado:

```bash
aws sts get-caller-identity
```

Esto debería devolver tu Account ID, User/Role ARN, etc.

## Paso 1: Crear el Repositorio en ECR

```bash
aws ecr create-repository \
  --repository-name reto-aws-api \
  --region us-east-1
```

**Salida esperada:**
```json
{
    "repository": {
        "repositoryArn": "arn:aws:ecr:us-east-1:123456789012:repository/reto-aws-api",
        "registryId": "123456789012",
        "repositoryName": "reto-aws-api",
        "repositoryUri": "123456789012.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api",
        ...
    }
}
```

**Guarda el `repositoryUri`** - lo necesitarás más adelante.

## Paso 2: Obtener tu Account ID

Si no lo anotaste del paso anterior:

```bash
aws sts get-caller-identity --query Account --output text
```

Guarda este valor, lo usaremos como `ACCOUNT_ID`.

## Paso 3: Autenticarse en ECR

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com
```

Reemplaza `ACCOUNT_ID` con tu Account ID.

**Salida esperada:**
```
Login Succeeded
```

## Paso 4: Tagear la Imagen Local

```bash
docker tag reto-aws-api:latest ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:latest
```

Reemplaza `ACCOUNT_ID` con tu Account ID.

## Paso 5: Pushear la Imagen a ECR

```bash
docker push ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api:latest
```

**Salida esperada:**
```
The push refers to repository [ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/reto-aws-api]
...
latest: digest: sha256:... size: ...
```

## Verificar en AWS Console

Ve a AWS Console → ECR → Repositorios → `reto-aws-api`

Deberías ver tu imagen con el tag `latest`.

## Script Automatizado

Para hacer esto en un solo comando:

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=us-east-1

# Crear repositorio
aws ecr create-repository --repository-name reto-aws-api --region $REGION 2>/dev/null || true

# Autenticarse
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

# Tagear
docker tag reto-aws-api:latest $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/reto-aws-api:latest

# Pushear
docker push $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/reto-aws-api:latest

echo "Imagen publicada en: $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/reto-aws-api:latest"
```

## Cambiar Región

Si usas otra región (ej: `eu-west-1`), reemplaza `us-east-1` en todos los comandos.

## Troubleshooting

**Error: "RepositoryAlreadyExistsException"**
- El repositorio ya existe, puedes ignorar y seguir al paso 3.

**Error: "InvalidAction"**
- Verifica que tengas permisos ECR en tu política de IAM.

**Error: "denied: User is not authorized"**
- Vuelve a autenticarte en el paso 3.

## Variables de Entorno (Opcional)

Para no repetir el Account ID, puedes setear variables:

```bash
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION=us-east-1
export ECR_REGISTRY=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
export ECR_REPOSITORY=reto-aws-api

# Luego usarlas así:
docker tag reto-aws-api:latest $ECR_REGISTRY/$ECR_REPOSITORY:latest
docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
```
