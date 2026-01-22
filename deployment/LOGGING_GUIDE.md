# Guía de Logs y Trazas

Esta guía explica cómo funcionan los logs, trazas y correlación de requests en la aplicación.

## Arquitectura de Logging

```
1. LoggingInterceptor (preHandle)
   ├─ Generar UUID (correlationId)
   ├─ Poner en MDC: correlationId, method, path, startTime
   ├─ Log: Incoming request
   │
2. Endpoint (PersonController)
   ├─ Request llega al controller
   │
3. UseCase (CreatePersonUseCase.execute() / GetPersonUseCase.execute())
   ├─ Lógica de negocio
   │
4. Repository
   ├─ Interactúa con base de datos
   │
5. Response
   ├─ Retorna resultado
   │
6. LoggingInterceptor (afterCompletion)
   ├─ Calcula duración
   ├─ Log: Request completed
   ├─ MDC.clear() limpia los datos
```

## Componentes

### 1. LoggingInterceptor
**Ubicación:** `infrastructure/entry-points/api-rest/src/main/java/co/com/bancolombia/api/interceptor/LoggingInterceptor.java`

**Responsabilidad:**
- Generar UUID único por request (`correlationId`)
- Poner información en MDC (Mapped Diagnostic Context)
- Agregar `correlationId` al header de respuesta
- Calcular duración total del request
- Limpiar MDC al finalizar

**Datos que pone en MDC:**
- `correlationId`: UUID único del request
- `method`: HTTP method (GET, POST, etc)
- `path`: URI del request
- `startTime`: Timestamp de inicio

**Ejemplo de log:**
```
[INFO] 2026-01-21 15:30:45.123 [abc-123-uuid] [GET /persons] [http-thread-1] CorrelationIdFilter - Incoming request - Method: GET, Path: /persons
[INFO] 2026-01-21 15:30:46.456 [abc-123-uuid] [GET /persons] [http-thread-1] CorrelationIdFilter - Request completed - Status: 200, Duration: 1333ms
```

### 2. WebMvcConfig
**Ubicación:** `infrastructure/entry-points/api-rest/src/main/java/co/com/bancolombia/api/config/WebMvcConfig.java`

**Responsabilidad:**
- Registrar el LoggingInterceptor en Spring
- Aplicar interceptor a todos los requests HTTP

**Ejemplo de logs:**
```
[INFO] 2026-01-21 15:30:45.234 [abc-123-uuid] [POST /persons] [http-thread-1] LoggingInterceptor - Incoming request - Method: POST, Path: /persons
[INFO] 2026-01-21 15:30:46.456 [abc-123-uuid] [POST /persons] [http-thread-1] LoggingInterceptor - Request completed - Status: 201, Duration: 1222ms
```

## Patrón de Log

El patrón configurado en `log4j2.properties` es:

```properties
[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%X{correlationId}] [%X{method} %X{path}] [%t] %c{1} - %msg%n
```

**Explicación:**
- `[%-5level]`: Nivel de log alineado a la izquierda (5 caracteres)
- `%d{yyyy-MM-dd HH:mm:ss.SSS}`: Timestamp
- `[%X{correlationId}]`: **Correlation ID del MDC**
- `[%X{method} %X{path}]`: **HTTP method y path del MDC**
- `[%t]`: Nombre del thread
- `%c{1}`: Nombre corto de la clase
- `%msg`: Mensaje de log
- `%n`: Nueva línea

## Ejemplo de Flujo Completo

### Request: `POST /persons`

```bash
curl -X POST http://localhost:8080/persons \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "identification": "12345678"
  }'
```

### Logs generados:

```log
[INFO ] 2026-01-21 15:30:45.123 [a1b2c3d4-e5f6] [POST /persons] [http-thread-1] CorrelationIdFilter - Incoming request - Method: POST, Path: /persons

[INFO ] 2026-01-21 15:30:45.234 [a1b2c3d4-e5f6] [POST /persons] [http-thread-1] UseCaseLoggingAspect - Starting CreatePersonUseCase.execute - Params: [Person{name='Juan Pérez', email='juan@example.com'}]

[INFO ] 2026-01-21 15:30:45.567 [a1b2c3d4-e5f6] [POST /persons] [http-thread-1] UseCaseLoggingAspect - Completed CreatePersonUseCase.execute - Duration: 333ms

[INFO ] 2026-01-21 15:30:46.123 [a1b2c3d4-e5f6] [POST /persons] [http-thread-1] CorrelationIdFilter - Request completed - Status: 201, Duration: 1000ms
```

**Nota:** Todos los logs tienen el mismo `[a1b2c3d4-e5f6]` (correlationId), lo que permite rastrear el flujo completo.

## Buscar Logs en CloudWatch

### Por correlationId
```
fields @message
| filter correlationId = "a1b2c3d4-e5f6"
```

### Errores en UseCases
```
fields @message
| filter @message like /Error in.*UseCase/
```

### Requests lentos (> 2 segundos)
```
fields @message, duration
| filter duration > 2000
```

### Por endpoint específico
```
fields @message
| filter path = "/persons"
```

## Beneficios de esta Implementación

✅ **Trazabilidad:** Seguir un request desde entrada a salida
✅ **Debugging:** Buscar por correlationId para ver todo lo relacionado
✅ **Performance:** Medir duración de cada operación
✅ **Código limpio:** UseCases sin lógica de logs
✅ **Automático:** @Aspect maneja los logs sin código adicional
✅ **Consistente:** Mismo patrón para todos los UseCases
✅ **MDC:** Propagar contexto sin parámetros adicionales

## Niveles de Log

```
DEBUG: Información detallada (solo en desarrollo)
INFO:  Eventos importantes (inicio/fin de operaciones)
WARN:  Situaciones anómalas (validaciones fallidas)
ERROR: Errores de la aplicación (excepciones)
```

## Próximos Pasos

1. **Configurar CloudWatch Log Group** para capturar estos logs
2. **Crear CloudWatch Alarms** basadas en patrones de logs
3. **Usar correlationId** en respuestas de error para facilitar debugging
4. **Analizar logs** en CloudWatch Insights para problemas de performance
