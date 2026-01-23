# Serverless Java - Guía de Despliegue

## 📋 Prerrequisitos

- Java 17+
- Gradle 9.2+
- AWS CLI configurado
- Serverless Framework 4.x

## 🔨 Compilar y Desplegar

### 1. Compilar y Construir JAR
```bash
# Desde la raíz del proyecto
gradlew :serverless-java:clean :serverless-java:fatJar --daemon
```

### 2. Desplegar en AWS
```bash
# Navegar al directorio serverless-java
cd infrastructure/lambda/serverless-java

# Desplegar
serverless deploy
```

## 🧪 Pruebas Locales

```bash
# Ejecutar pruebas
gradlew :serverless-java:test

# Reporte de cobertura
gradlew :serverless-java:jacocoTestReport
```

## 📁 Archivos Clave

- `build/libs/serverless-java-all.jar` - Fat JAR para Lambda
- `serverless.yml` - Configuración de despliegue AWS
- `src/main/java/co/com/bancolombia/lambda/handler/` - Manejadores Lambda

## 🚀 Endpoints de la API

- `POST /api/serverless/users` - Crear usuario
- `GET /api/serverless/users/{id}` - Obtener usuario
- `PUT /api/serverless/users/{id}` - Actualizar usuario  
- `DELETE /api/serverless/users/{id}` - Eliminar usuario