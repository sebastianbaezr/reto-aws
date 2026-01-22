# Configuración Local

Guía para ejecutar la aplicación localmente con configuración específica.

## Pasos

### 1. El archivo application-local.yaml ya existe

Ya tienes `applications/app-service/src/main/resources/application-local.yaml` con las credenciales de desarrollo local.

### 2. Editar credenciales (si es necesario)

Abre `application-local.yaml` y actualiza si es necesario:

```yaml
datasource:
    url: "jdbc:postgresql://tu-host:5432/tu-db"
    username: "tu-usuario"
    password: "tu-contraseña"
```

### 3. Ejecutar la aplicación

```bash
./gradlew bootRun --args="--spring.profiles.active=local"
```

## Estructura

- `application.yaml` → Configuración base (sin credenciales)
- `application-local.yaml` → Perfil local con credenciales (NO se commitea)

## Seguridad

⚠️ **Importante:**
- `application-local.yaml` está en `.gitignore` para evitar subir credenciales
- Nunca commiteacredenciales en archivos versionados
- Solo usamos perfiles locales para desarrollo

## Cómo funciona

Spring Boot carga archivos en este orden:

1. `application.yaml` (base)
2. `application-{profile}.yaml` (sobrescribe)

Cuando ejecutas con `--spring.profiles.active=local`, Spring carga:
- `application.yaml` + `application-local.yaml`

Las propiedades de `application-local.yaml` sobrescriben las de `application.yaml`.

## Verificar que funcionó

```bash
curl http://localhost:8080/actuator/health
```

Deberías recibir:
```json
{"status":"UP"}
```

## Troubleshooting

**Error: Database connection refused**
- Verifica que las credenciales en `application-local.yaml` son correctas
- Comprueba que RDS está accesible

**Error: Profile not found**
- Asegúrate de usar `--spring.profiles.active=local` (con guión)

**Error: Port already in use**
- Cambia el puerto en `application-local.yaml`:
  ```yaml
  server:
    port: 8081
  ```
