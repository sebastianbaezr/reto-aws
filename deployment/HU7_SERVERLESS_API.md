# HU7: API Serverless CRUD de Usuarios

## Descripción General

Este documento describe la API Serverless CRUD para usuarios implementada en HU7. La API está construida con AWS Lambda en Java 17 y Node.js 20.x, desplegada usando Serverless Framework.

## Arquitectura

### Componentes

- **API Gateway (HTTP API)**: Punto de entrada único para todas las operaciones REST
- **Lambda Functions**: 8 funciones totales (4 Java + 4 Node.js)
- **Repositorio en Memoria**: Usuarios predefinidos almacenados en memoria
- **Implementaciones Duales**: Java 17 y Node.js 20.x

### Estructura de Directorios

```
infrastructure/
├── lambda/
│   └── serverless-java/           # Implementación Lambda en Java
│       ├── build.gradle
│       └── src/
│           ├── main/java/...      # Handlers, DTOs, Repository, UseCases
│           └── test/java/...      # Tests unitarios

serverless-nodejs/                 # Implementación en Node.js
├── package.json
├── serverless.yml                 # Configuración para desplegar solo Node.js
└── src/
    ├── handlers/                  # Funciones Lambda
    ├── services/                  # Lógica de negocio y repositorio
    ├── utils/                     # Validadores, respuestas, manejo de errores
    └── models/                    # Modelos de datos

deployment/
├── serverless.yml                 # Configuración principal (Java)
└── HU7_SERVERLESS_API.md         # Esta documentación
```

## Endpoints de la API

### URL Base

```
https://{API_ID}.execute-api.{REGION}.amazonaws.com/{STAGE}/api/serverless/users
```

### 1. Crear Usuario

**Endpoint**: `POST /api/serverless/users`

**Request**:
```json
{
  "nombre": "Juan González",
  "email": "juan.gonzalez@example.com"
}
```

**Response** (201 Created):
```json
{
  "id": 4,
  "nombre": "Juan González",
  "email": "juan.gonzalez@example.com"
}
```

**Errores**:
- `400 Bad Request`: Campos faltantes o inválidos
- `400 Bad Request`: El email ya existe
- `400 Bad Request`: Formato de email inválido

**Ejemplo cURL**:
```bash
curl -X POST https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Nuevo Usuario",
    "email": "nuevouser@example.com"
  }'
```

### 2. Obtener Usuario

**Endpoint**: `GET /api/serverless/users/{id}`

**Parámetros**:
- `id` (path): ID del usuario (número)

**Response** (200 OK):
```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan.perez@bancolombia.com"
}
```

**Errores**:
- `404 Not Found`: Usuario no encontrado
- `400 Bad Request`: ID inválido

**Ejemplo cURL**:
```bash
curl -X GET https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1
```

### 3. Actualizar Usuario

**Endpoint**: `PUT /api/serverless/users/{id}`

**Parámetros**:
- `id` (path): ID del usuario (número)

**Request**:
```json
{
  "nombre": "Nombre Actualizado",
  "email": "actualizado@example.com"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "nombre": "Nombre Actualizado",
  "email": "actualizado@example.com"
}
```

**Errores**:
- `404 Not Found`: Usuario no encontrado
- `400 Bad Request`: Campos faltantes o inválidos
- `400 Bad Request`: El email ya existe (excepto el del usuario actual)
- `400 Bad Request`: Formato de email inválido

**Ejemplo cURL**:
```bash
curl -X PUT https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Usuario Actualizado",
    "email": "actualizado@example.com"
  }'
```

### 4. Eliminar Usuario

**Endpoint**: `DELETE /api/serverless/users/{id}`

**Parámetros**:
- `id` (path): ID del usuario (número)

**Response** (200 OK):
```json
{
  "message": "Usuario eliminado exitosamente"
}
```

**Errores**:
- `404 Not Found`: Usuario no encontrado
- `400 Bad Request`: ID inválido

**Ejemplo cURL**:
```bash
curl -X DELETE https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1
```

## Usuarios Predefinidos

La aplicación se inicializa con tres usuarios predefinidos:

| ID | Nombre | Email |
|----|--------|-------|
| 1 | Juan Pérez | juan.perez@bancolombia.com |
| 2 | María López | maria.lopez@bancolombia.com |
| 3 | Carlos Rodríguez | carlos.rodriguez@bancolombia.com |

Los nuevos usuarios creados vía POST recibirán IDs comenzando desde 4.

## Validación de Datos

### Modelo de Usuario

- **id** (número): Identificador único generado automáticamente
- **nombre** (string): Nombre del usuario, requerido, no vacío
- **email** (string): Email del usuario, requerido, formato válido, único

### Reglas de Validación

1. **nombre**: No puede ser nulo o vacío
2. **email**:
   - No puede ser nulo o vacío
   - Debe cumplir el patrón: `^[A-Za-z0-9+_.-]+@(.+)$`
   - Debe ser único en toda la aplicación (case-insensitive)

### Formato de Respuesta de Error

```json
{
  "error": "Mensaje describiendo el problema"
}
```

## Códigos de Estado HTTP

| Código | Significado | Usado en |
|--------|-------------|----------|
| 200 | OK | GET, PUT, DELETE |
| 201 | Creado | POST |
| 400 | Solicitud Inválida | Errores de validación |
| 404 | No Encontrado | Usuario no existe |
| 500 | Error Interno del Servidor | Errores inesperados |

## Despliegue

### Requisitos Previos

- Cuenta de AWS con permisos adecuados
- Serverless Framework instalado
- AWS CLI configurado

### Pasos de Despliegue

#### Opción 1: Desplegar Java (Recomendado)

```bash
# 1. Compilar el proyecto Java
cd infrastructure/lambda/serverless-java
gradle build

# 2. Desplegar desde deployment
cd ../../deployment
export AWS_REGION=us-east-1
export ENVIRONMENT=dev
serverless deploy
```

#### Opción 2: Desplegar Node.js

```bash
# 1. Desplegar desde serverless-nodejs
cd serverless-nodejs
export AWS_REGION=us-east-1
export ENVIRONMENT=dev
npm install
serverless deploy
```

#### Opción 3: Desplegar Ambas Implementaciones

```bash
# Primero Java
cd infrastructure/lambda/serverless-java
gradle build
cd ../../deployment
export AWS_REGION=us-east-1
export ENVIRONMENT=dev
serverless deploy

# Luego Node.js (reemplaza las funciones Java)
cd ../../serverless-nodejs
npm install
serverless deploy
```

### Ver Salida del Despliegue

```bash
# Ver endpoints desplegados
serverless info --stage dev

# Ver stack de CloudFormation
aws cloudformation describe-stacks --stack-name reto-aws-serverless-users-dev --region us-east-1
```

## Pruebas

### Tests Java

```bash
cd infrastructure/lambda/serverless-java
gradle test
```

### Tests Node.js

```bash
cd serverless-nodejs
npm install
npm test
```

### Probar Manualmente

Después de desplegar, usar cURL para probar:

```bash
# Crear usuario
curl -X POST https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Test", "email": "test@example.com"}'

# Obtener usuario
curl -X GET https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1

# Actualizar usuario
curl -X PUT https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Actualizado", "email": "actualizado@example.com"}'

# Eliminar usuario
curl -X DELETE https://YOUR_API_ID.execute-api.us-east-1.amazonaws.com/dev/api/serverless/users/1
```

## Variables de Entorno

| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `AWS_REGION` | us-east-1 | Región de AWS para desplegar |
| `ENVIRONMENT` | dev | Entorno (dev, staging, prod) |

## Implementación Java

### Estructura del Proyecto

- **handlers/**: Clases manejadoras de Lambda
- **dto/**: Objetos de Transferencia de Datos
- **repository/**: Repositorio en memoria
- **usecase/**: Lógica de negocio para cada operación
- **mapper/**: Mapeo entre DTOs y JSON
- **exception/**: Excepciones personalizadas

### Clases Principales

- `CreateUserHandler`: Maneja solicitudes POST
- `GetUserHandler`: Maneja solicitudes GET
- `UpdateUserHandler`: Maneja solicitudes PUT
- `DeleteUserHandler`: Maneja solicitudes DELETE
- `InMemoryUserRepository`: Gestiona datos de usuarios

### Dependencias

- AWS Lambda Java Core: 1.2.3
- AWS Lambda Java Events: 3.14.0
- Gson: 2.10.1
- Jakarta Validation: 3.0.2
- Hibernate Validator: 8.0.1.Final

## Implementación Node.js

### Estructura del Proyecto

- **handlers/**: Funciones manejadoras
- **services/**: UserService y UserRepository
- **models/**: Clase de modelo User
- **utils/**: Validadores, respuestas, manejo de errores

### Módulos Principales

- `createUser.js`: Crea nuevos usuarios
- `getUser.js`: Obtiene usuario por ID
- `updateUser.js`: Actualiza usuario por ID
- `deleteUser.js`: Elimina usuario por ID
- `userRepository.js`: Almacenamiento en memoria
- `userService.js`: Lógica de negocio
- `validator.js`: Validación de email
- `errorHandler.js`: Utilidades de manejo de errores

## Monitoreo

### Ver Logs con CloudWatch

```bash
# Logs de una función específica
serverless logs -f createUserJava --stage dev --tail

# Logs en tiempo real
serverless logs -f getUserNode --stage dev --tail
```

### Ver Métricas en CloudWatch

Métricas disponibles:
- Invocaciones
- Duración
- Errores
- Throttles (limitaciones)

## Consideraciones de Seguridad

1. **CORS**: Actualmente abierto a todos (`*`). Para producción, restringir orígenes.
2. **Autenticación**: No implementada. Considerar AWS Cognito.
3. **Validación de Email**: Case-insensitive para prevenir duplicados.
4. **Validación de Entrada**: Todos los campos validados antes de procesar.

Para producción, considerar:
- Agregar AWS Cognito para autenticación
- Restringir orígenes CORS
- Usar API Keys u OAuth2
- Habilitar VPC para acceso a bases de datos
- Encripción en tránsito y en reposo

## Solución de Problemas

### Problema: Error "MalformedHandlerName"

**Causa**: Rutas relativas inválidas en serverless.yml
**Solución**:
- Para Java: Usar rutas relativas desde `deployment/`
- Para Node.js: Usar rutas relativas desde `serverless-nodejs/`

### Problema: Usuario con Email Duplicado

**Causa**: Validación de unicidad case-insensitive
**Solución**: Asegurar que el nuevo email sea diferente (case-insensitive)

### Problema: "No existing streams for the function"

**Causa**: Normal para funciones recientemente desplegadas
**Solución**: Los logs aparecerán después de la primera invocación

### Problema: Función Tarda Demasiado

**Solución**: Aumentar timeout en serverless.yml
```yaml
timeout: 60
```

### Problema: Errores de CORS

**Solución**: Verificar configuración de httpApi en serverless.yml

## Mejoras Futuras

1. Agregar DynamoDB para almacenamiento persistente
2. Implementar paginación para listados
3. Agregar ordenamiento y filtrado
4. Autenticación con AWS Cognito
5. Registros y auditoría de solicitudes
6. Implementar rate limiting
7. Trazabilidad con AWS X-Ray
8. Operaciones en lote

## Referencias

- [Documentación de AWS Lambda](https://docs.aws.amazon.com/lambda/)
- [Documentación de Serverless Framework](https://www.serverless.com/framework/docs)
- [Documentación de API Gateway](https://docs.aws.amazon.com/apigateway/)
- [Jest Testing Framework](https://jestjs.io/)

## Soporte

Para problemas o preguntas sobre esta API:

1. Revisar la sección de pruebas para cómo ejecutar tests
2. Verificar logs en CloudWatch para detalles de errores
3. Confirmar que todas las variables de entorno están configuradas
4. Verificar credenciales de AWS y permisos
5. Revisar configuración en serverless.yml
