# Guía de Autenticación con Cognito

Esta guía explica cómo generar tokens JWT desde AWS Cognito para autenticarte en los endpoints de la API.

## Prerequisitos

- AWS CLI configurado
- OpenSSL instalado (para generar SECRET_HASH)
- Usuario creado en Cognito User Pool
- Credenciales de Cognito (User Pool ID, Client ID, Client Secret)

## Parámetros Necesarios

Reemplaza los siguientes valores con los tuyos:

```bash
# Tu User Pool ID (formato: region_random)
USER_POOL_ID="us-east-1_ZDzS3SzqV"

# Tu App Client ID
CLIENT_ID="65a673ik1gtr784lr746tdjt1k"

# Tu App Client Secret (obtenlo de AWS Console)
CLIENT_SECRET="721e2s56optgph49hg9pf2qjj4fe44thti055md466dn7eqdvuo"

# Usuario de prueba en Cognito
USERNAME="testuser"

# Contraseña del usuario
PASSWORD="MySecurePass123!"
```

## Obtener tus Credenciales de Cognito

### 1. User Pool ID y Client ID

```bash
# Listar User Pools
aws cognito-idp list-user-pools --max-results 10 --region us-east-1

# Obtener detalles del User Pool
aws cognito-idp describe-user-pool \
  --user-pool-id us-east-1_ZDzS3SzqV \
  --region us-east-1

# Listar App Clients del User Pool
aws cognito-idp list-user-pool-clients \
  --user-pool-id us-east-1_ZDzS3SzqV \
  --region us-east-1

# Ver detalles del App Client (obtener Client Secret)
aws cognito-idp describe-user-pool-client \
  --user-pool-id us-east-1_ZDzS3SzqV \
  --client-id 65a673ik1gtr784lr746tdjt1k \
  --region us-east-1
```

## Generar Token JWT

### Paso 1: Crear variables con tus valores

```bash
# Reemplaza con tus valores reales
USER_POOL_ID="us-east-1_ZDzS3SzqV"
CLIENT_ID="65a673ik1gtr784lr746tdjt1k"
CLIENT_SECRET="721e2s56optgph49hg9pf2qjj4fe44thti055md466dn7eqdvuo"
USERNAME="testuser"
PASSWORD="MySecurePass123!"
REGION="us-east-1"
```

### Paso 2: Generar SECRET_HASH

El SECRET_HASH es un hash HMAC-SHA256 de la concatenación `USERNAME + CLIENT_ID` usando el `CLIENT_SECRET` como clave.

```bash
SECRET_HASH=$(echo -n "${USERNAME}${CLIENT_ID}" | \
  openssl dgst -sha256 -hmac "${CLIENT_SECRET}" -binary | \
  base64)

echo "SECRET_HASH: $SECRET_HASH"
```

### Paso 3: Obtener el Token de Autenticación

```bash
aws cognito-idp admin-initiate-auth \
  --user-pool-id ${USER_POOL_ID} \
  --client-id ${CLIENT_ID} \
  --auth-flow ADMIN_USER_PASSWORD_AUTH \
  --auth-parameters USERNAME=${USERNAME},PASSWORD="${PASSWORD}",SECRET_HASH="${SECRET_HASH}" \
  --region ${REGION}
```

**Respuesta esperada:**

```json
{
    "AuthenticationResult": {
        "AccessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "ExpiresIn": 3600,
        "TokenType": "Bearer",
        "RefreshToken": "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ...",
        "IdToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
}
```

## Usar el Token para Acceder a los Endpoints

### Guardar el token en una variable

```bash
TOKEN=$(aws cognito-idp admin-initiate-auth \
  --user-pool-id ${USER_POOL_ID} \
  --client-id ${CLIENT_ID} \
  --auth-flow ADMIN_USER_PASSWORD_AUTH \
  --auth-parameters USERNAME=${USERNAME},PASSWORD="${PASSWORD}",SECRET_HASH="${SECRET_HASH}" \
  --region ${REGION} \
  --query 'AuthenticationResult.AccessToken' \
  --output text)

echo "Token: $TOKEN"
```

### Llamar a un endpoint con el token

```bash
# Obtener lista de personas
curl -X GET http://localhost:8080/persons \
  -H "Authorization: Bearer ${TOKEN}"

# Crear una persona
curl -X POST http://localhost:8080/persons \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "identification": "12345678"
  }'

# Obtener una persona por ID
curl -X GET http://localhost:8080/persons/1 \
  -H "Authorization: Bearer ${TOKEN}"
```

## Script Automatizado

Para simplificar, crea un script `deployment/get-token.sh`:

```bash
#!/bin/bash

# Configurar variables
USER_POOL_ID="us-east-1_ZDzS3SzqV"
CLIENT_ID="65a673ik1gtr784lr746tdjt1k"
CLIENT_SECRET="721e2s56optgph49hg9pf2qjj4fe44thti055md466dn7eqdvuo"
USERNAME="${1:-testuser}"
PASSWORD="${2:-MySecurePass123!}"
REGION="us-east-1"

# Generar SECRET_HASH
SECRET_HASH=$(echo -n "${USERNAME}${CLIENT_ID}" | \
  openssl dgst -sha256 -hmac "${CLIENT_SECRET}" -binary | \
  base64)

# Obtener token
TOKEN=$(aws cognito-idp admin-initiate-auth \
  --user-pool-id ${USER_POOL_ID} \
  --client-id ${CLIENT_ID} \
  --auth-flow ADMIN_USER_PASSWORD_AUTH \
  --auth-parameters USERNAME=${USERNAME},PASSWORD="${PASSWORD}",SECRET_HASH="${SECRET_HASH}" \
  --region ${REGION} \
  --query 'AuthenticationResult.AccessToken' \
  --output text)

echo "Token obtenido exitosamente:"
echo "$TOKEN"
```

Usar el script:

```bash
chmod +x deployment/get-token.sh

# Obtener token para usuario 'testuser'
./deployment/get-token.sh testuser MySecurePass123!

# O con valores por defecto
./deployment/get-token.sh
```

## Verificar Token

Para verificar que el token es válido:

```bash
# Decodificar el token (sin validar firma, solo para ver el contenido)
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq .
```

**Salida esperada:**

```json
{
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email_verified": true,
  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ZDzS3SzqV",
  "cognito:username": "testuser",
  "aud": "65a673ik1gtr784lr746tdjt1k",
  "token_use": "access",
  "auth_time": 1674000000,
  "exp": 1674003600,
  "iat": 1674000000
}
```

## Troubleshooting

### Error: "Invalid SECRET_HASH"
- Verifica que `USERNAME + CLIENT_ID` sea exacto
- Verifica que `CLIENT_SECRET` sea correcto
- Asegúrate de estar usando la codificación correcta (base64)

### Error: "User does not exist"
- Verifica que el usuario existe en Cognito
- Crea un usuario de prueba con:
  ```bash
  aws cognito-idp admin-create-user \
    --user-pool-id ${USER_POOL_ID} \
    --username testuser \
    --temporary-password TempPass123! \
    --region ${REGION}
  ```

### Error: "Password did not conform with policy"
- La contraseña debe tener:
  - Mínimo 8 caracteres
  - Al menos una mayúscula
  - Al menos una minúscula
  - Al menos un número

## Referencias

- [AWS Cognito AdminInitiateAuth API](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html)
- [JWT Token Structure](https://jwt.io/)
- [HMAC-SHA256 Documentation](https://en.wikipedia.org/wiki/HMAC)
