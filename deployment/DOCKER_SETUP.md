# Guía de Configuración y Despliegue con Docker

## Credenciales de Base de Datos

**IMPORTANTE:** Las credenciales están hardcodeadas en `applications/app-service/src/main/resources/application.yaml` (ignorado en git). No necesitas pasar variables de entorno para desarrollo local o testing.

Credenciales usadas:
```
DB_HOST: database-1.cyjc2g6ec3fc.us-east-1.rds.amazonaws.com
DB_PORT: 5432
DB_NAME: reto_aws
DB_USERNAME: postgres
DB_PASSWORD: zRunL4ngcNaR65E
```

## Pasos para Construir la Imagen Docker

### 1. Construcción de la imagen

Desde la raíz del proyecto, ejecuta:

```bash
docker build -f deployment/Dockerfile -t reto-aws-api:latest .
```

Esto creará una imagen Docker con:
- **Nombre**: `reto-aws-api`
- **Tag**: `latest`

### 2. Ejecutar el contenedor

**Forma simple** (usa credenciales hardcodeadas en application.yaml):

```bash
docker run -d --name reto-aws-api -p 8080:8080 reto-aws-api:latest
```

**Forma con variables de entorno** (para sobrescribir credenciales si es necesario):

```bash
docker run -d --name reto-aws-api -p 8080:8080 -e DB_PASSWORD=otra_contraseña reto-aws-api:latest
```

#### Parámetros:
- `-d`: Ejecuta el contenedor en background
- `--name`: Nombre del contenedor
- `-p 8080:8080`: Mapea el puerto 8080 del contenedor al host
- `-e`: Opcional - Define variables de entorno para sobrescribir los defaults
- `reto-aws-api:latest`: Imagen y tag a ejecutar

### 3. Verificar que el contenedor está corriendo

```bash
docker ps
```

Deberías ver tu contenedor `reto-aws-api` en la lista.

### 4. Ver logs de la aplicación

```bash
docker logs -f reto-aws-api
```

La opción `-f` mantiene los logs en tiempo real.

### 5. Detener el contenedor

```bash
docker stop reto-aws-api
```

### 6. Eliminar el contenedor

```bash
docker rm reto-aws-api
```

## Verificar que la API está funcionando

Una vez el contenedor esté corriendo, puedes verificar que la API responde:

```bash
curl http://localhost:8080/actuator/health
```

Deberías recibir una respuesta similar a:
```json
{
  "status": "UP"
}
```

## Configuración de Producción

Para entornos de producción:

1. **No incluyas credenciales en este archivo** - usa un gestor de secretos (AWS Secrets Manager, Docker Secrets, Kubernetes Secrets)
2. **Cambia las credenciales por defecto** de RDS
3. **Usa HTTPS** en lugar de HTTP
4. **Configura límites de recursos** en Docker (CPU, memoria)

Ejemplo con límites:
```bash
docker run -d \
  --name reto-aws-api \
  -p 8080:8080 \
  --memory=512m \
  --cpus=1.0 \
  -e DB_PASSWORD=zRunL4ngcNaR65E \
  reto-aws-api:latest
```

## Troubleshooting

### Error de conexión a la base de datos
- Verifica que las credenciales sean correctas
- Asegúrate que RDS está accesible desde tu red
- Revisa los security groups de RDS en AWS

### Puerto 8080 ya está en uso
Cambia el mapeo de puerto:
```bash
docker run -d -p 8081:8080 reto-aws-api:latest
```

### El contenedor se detiene inmediatamente
Revisa los logs:
```bash
docker logs reto-aws-api
```
