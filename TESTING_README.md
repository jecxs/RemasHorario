# Guía de Pruebas del Sistema de Asistencia y Contabilidad Docente

## Introducción

Este documento te guiará en el proceso de pruebas de todos los endpoints de las nuevas funcionalidades del sistema:

- **AttendanceActivityType**: Tipos de actividades (Clase regular, Taller, Examen sustitutorio, etc.)
- **TeacherAttendance**: Asistencia de docentes con registro de entrada/salida y cálculo de penalizaciones
- **AcademicCalendarException**: Excepciones de calendario (feriados, días no laborables)
- **ExtraAssignment**: Asignaciones extra fuera del horario regular
- **TeacherRate**: Tarifas específicas por docente
- **ModalityRate**: Tarifas por modalidad educativa (Instituto/Escuela)
- **DefaultRate**: Tarifas por defecto del sistema
- **PayrollPeriod**: Períodos de nómina (semanal, quincenal, mensual)
- **PayrollLine**: Cálculo de nóminas con asistencias, extras y penalizaciones

## Archivos Disponibles

### 1. API_TESTING_GUIDE.md
Documento completo con **todos los endpoints** documentados y ejemplos de cURL. Incluye:
- Descripción de cada endpoint
- Ejemplos de peticiones cURL
- Payloads de ejemplo
- Respuestas esperadas
- Flujo de pruebas recomendado

### 2. test_api_endpoints.py
Script de Python para **pruebas automatizadas**. Características:
- Prueba múltiples endpoints automáticamente
- Colorea la salida para fácil lectura
- Muestra estadísticas de pruebas exitosas/fallidas
- Guarda UUIDs para pruebas relacionadas

## Requisitos Previos

### 1. Iniciar el Servidor

#### Opción A: Con Gradle (Recomendado)
```bash
./gradlew bootRun
```

#### Opción B: Con Maven
```bash
mvn spring-boot:run
```

#### Opción C: Con JAR compilado
```bash
# Compilar
./gradlew build

# Ejecutar
java -jar build/libs/remashorario-0.0.1-SNAPSHOT.jar
```

### 2. Verificar que el Servidor esté Corriendo

```bash
# Verificar salud del servidor
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{"status":"UP"}
```

### 3. Base de Datos

Asegúrate de que SQL Server esté corriendo:
- **Host**: localhost:1433
- **Database**: HoraRemAlt
- **User**: sa
- **Password**: root

## Método 1: Pruebas con cURL (Manual)

### Paso 1: Obtener Token de Autenticación

```bash
# Guardar el token en una variable
export TOKEN=$(curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' \
  | jq -r '.data.token')

# Verificar que el token se guardó
echo $TOKEN
```

### Paso 2: Probar Endpoints

Ahora puedes usar los ejemplos del archivo `API_TESTING_GUIDE.md`. Por ejemplo:

```bash
# Inicializar tipos de actividad por defecto
curl -X POST "http://localhost:8080/api/protected/attendance-activity-types/initialize-defaults" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
```

```bash
# Obtener todos los tipos de actividad
curl -X GET "http://localhost:8080/api/protected/attendance-activity-types" \
  -H "Authorization: Bearer ${TOKEN}"
```

### Flujo de Pruebas Recomendado con cURL

#### 1. Configuración Inicial
```bash
# a) Inicializar tipos de actividad
curl -X POST "http://localhost:8080/api/protected/attendance-activity-types/initialize-defaults" \
  -H "Authorization: Bearer ${TOKEN}"

# b) Obtener tipos creados
curl -X GET "http://localhost:8080/api/protected/attendance-activity-types" \
  -H "Authorization: Bearer ${TOKEN}"
```

#### 2. Crear Tarifas por Defecto
```bash
# Necesitarás el UUID del tipo de actividad obtenido en el paso anterior
export ACTIVITY_TYPE_UUID="uuid-aqui"

curl -X POST "http://localhost:8080/api/protected/default-rates" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "activityTypeUuid": "'${ACTIVITY_TYPE_UUID}'",
    "ratePerHour": 50.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": null
  }'
```

#### 3. Crear Excepciones de Calendario
```bash
curl -X POST "http://localhost:8080/api/protected/calendar-exceptions" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-25",
    "code": "CHRISTMAS",
    "description": "Navidad"
  }'
```

#### 4. Crear Período de Nómina
```bash
curl -X POST "http://localhost:8080/api/protected/payroll-periods/generate/monthly?year=2025&month=11" \
  -H "Authorization: Bearer ${TOKEN}"
```

## Método 2: Pruebas Automatizadas con Python

### Requisitos
```bash
# Instalar requests si no está instalado
pip install requests
```

### Ejecución Básica

```bash
# Ejecutar con valores por defecto (localhost:8080)
python3 test_api_endpoints.py
```

### Ejecución con Parámetros Personalizados

```bash
# Especificar URL y credenciales
python3 test_api_endpoints.py \
  --url http://localhost:8080 \
  --username admin \
  --password password
```

### Salida del Script

El script mostrará:
- ✓ Pruebas exitosas en **verde**
- ✗ Pruebas fallidas en **rojo**
- ⚠ Advertencias en **amarillo**
- ℹ Información en **cyan**

**Ejemplo de salida:**
```
================================================================================
         1. ATTENDANCE ACTIVITY TYPE - Tipos de Actividad
================================================================================

1.1 Inicializar Tipos por Defecto
----------------------------------
✓ Inicializar tipos de actividad por defecto - Status 200

1.2 Obtener Todos los Tipos
----------------------------
✓ Obtener todos los tipos de actividad - Status 200
ℹ   UUID de prueba guardado: 550e8400-e29b-41d4-a716-446655440000

...

================================================================================
                           RESUMEN DE PRUEBAS
================================================================================
Total de pruebas:  25
Exitosas:          24
Fallidas:          1
Tasa de éxito:     96.00%
```

### Extensión del Script de Pruebas

El script `test_api_endpoints.py` incluye ejemplos de tres módulos. Puedes extenderlo agregando más métodos de prueba:

```python
def test_teacher_attendance(self):
    """Prueba todos los endpoints de TeacherAttendance"""
    self.print_header("2. TEACHER ATTENDANCE - Asistencia de Docentes")

    # Agregar tus pruebas aquí
    self.test_endpoint(
        "GET",
        "/teacher-attendances",
        description="Obtener todas las asistencias"
    )
    # ... más pruebas

# Y luego agregar al método run_all_tests():
def run_all_tests(self):
    # ... código existente
    self.test_teacher_attendance()  # Agregar esta línea
```

## Método 3: Pruebas con Postman

### Importar Colección

1. Abre Postman
2. Crea una nueva colección llamada "RemasHorario API"
3. Configura variables de entorno:
   - `base_url`: `http://localhost:8080`
   - `api_base`: `{{base_url}}/api/protected`
   - `token`: (se llenará después del login)

### Configurar Pre-request Script Global

En la configuración de la colección, agrega este script:

```javascript
// Pre-request Script para todas las peticiones
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get('token')
});
```

### Crear Request de Login

1. Crea un nuevo request POST:
   - URL: `{{base_url}}/api/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "admin",
       "password": "password"
     }
     ```

2. Agrega este script en "Tests":
   ```javascript
   var jsonData = pm.response.json();
   if (jsonData.success && jsonData.data.token) {
       pm.environment.set("token", jsonData.data.token);
       console.log("Token guardado: " + jsonData.data.token);
   }
   ```

### Crear Requests desde la Guía

Usa los ejemplos del archivo `API_TESTING_GUIDE.md` para crear los requests en Postman.

**Ejemplo - Obtener Tipos de Actividad:**
- Método: GET
- URL: `{{api_base}}/attendance-activity-types`
- Headers: (el script pre-request agregará el token automáticamente)

## Escenarios de Prueba Completos

### Escenario 1: Configuración Inicial del Sistema

Este escenario configura el sistema desde cero:

```bash
# 1. Login
export TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' \
  | jq -r '.data.token')

# 2. Inicializar tipos de actividad
curl -X POST "http://localhost:8080/api/protected/attendance-activity-types/initialize-defaults" \
  -H "Authorization: Bearer ${TOKEN}"

# 3. Obtener tipos creados y guardar UUID
export REGULAR_CLASS_UUID=$(curl -s -X GET \
  "http://localhost:8080/api/protected/attendance-activity-types/code/REGULAR_CLASS" \
  -H "Authorization: Bearer ${TOKEN}" \
  | jq -r '.data.uuid')

# 4. Crear tarifa por defecto para clase regular
curl -X POST "http://localhost:8080/api/protected/default-rates" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "activityTypeUuid": "'${REGULAR_CLASS_UUID}'",
    "ratePerHour": 50.00,
    "effectiveFrom": "2025-11-01"
  }'

# 5. Crear excepciones de calendario (feriados)
curl -X POST "http://localhost:8080/api/protected/calendar-exceptions/bulk" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "exceptions": [
      {"date": "2025-12-25", "code": "CHRISTMAS", "description": "Navidad"},
      {"date": "2025-12-31", "code": "NEW_YEAR", "description": "Fin de Año"},
      {"date": "2026-01-01", "code": "NEW_YEAR_DAY", "description": "Año Nuevo"}
    ]
  }'

echo "✓ Configuración inicial completada"
```

### Escenario 2: Registro de Asistencia de Docente

```bash
# Prerequisito: Tener un docente y una clase en el sistema
export TEACHER_UUID="tu-teacher-uuid"
export CLASS_SESSION_UUID="tu-class-session-uuid"

# 1. Docente marca entrada con horario
ATTENDANCE_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/protected/teacher-attendances/check-in-with-schedule" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "teacherUuid": "'${TEACHER_UUID}'",
    "classSessionUuid": "'${CLASS_SESSION_UUID}'",
    "attendanceDate": "2025-11-22",
    "scheduledStartTime": "08:00:00",
    "scheduledEndTime": "09:45:00",
    "scheduledDurationMinutes": 105
  }')

# Guardar UUID de asistencia
export ATTENDANCE_UUID=$(echo $ATTENDANCE_RESPONSE | jq -r '.data.uuid')

# 2. Ver estadísticas de penalización (si llegó tarde)
echo "Asistencia creada:"
echo $ATTENDANCE_RESPONSE | jq '.data'

# 3. Docente marca salida
curl -X PATCH \
  "http://localhost:8080/api/protected/teacher-attendances/${ATTENDANCE_UUID}/check-out" \
  -H "Authorization: Bearer ${TOKEN}"

# 4. Obtener estadísticas del docente
curl -X GET \
  "http://localhost:8080/api/protected/teacher-attendances/teacher/${TEACHER_UUID}/statistics?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### Escenario 3: Proceso Completo de Nómina

```bash
# 1. Crear período de nómina mensual
PERIOD_RESPONSE=$(curl -s -X POST \
  "http://localhost:8080/api/protected/payroll-periods/generate/monthly?year=2025&month=11" \
  -H "Authorization: Bearer ${TOKEN}")

export PERIOD_UUID=$(echo $PERIOD_RESPONSE | jq -r '.data.uuid')

# 2. Calcular nómina de todos los docentes
curl -X POST \
  "http://localhost:8080/api/protected/payroll-lines/calculate/period/${PERIOD_UUID}" \
  -H "Authorization: Bearer ${TOKEN}"

# 3. Ver resumen del período
curl -X GET \
  "http://localhost:8080/api/protected/payroll-lines/period/${PERIOD_UUID}/summary" \
  -H "Authorization: Bearer ${TOKEN}" \
  | jq '.'

# 4. Ver líneas de nómina del período
curl -X GET \
  "http://localhost:8080/api/protected/payroll-lines/period/${PERIOD_UUID}" \
  -H "Authorization: Bearer ${TOKEN}" \
  | jq '.data'

# 5. Marcar período como calculado
curl -X PATCH \
  "http://localhost:8080/api/protected/payroll-periods/${PERIOD_UUID}/mark-calculated" \
  -H "Authorization: Bearer ${TOKEN}"

# 6. Aprobar período
curl -X PATCH \
  "http://localhost:8080/api/protected/payroll-periods/${PERIOD_UUID}/mark-approved" \
  -H "Authorization: Bearer ${TOKEN}"

# 7. Marcar como pagado
curl -X PATCH \
  "http://localhost:8080/api/protected/payroll-periods/${PERIOD_UUID}/mark-paid" \
  -H "Authorization: Bearer ${TOKEN}"

echo "✓ Proceso de nómina completado"
```

## Verificación de Resultados

### Ver Datos en la Base de Datos

Puedes verificar los datos directamente en SQL Server:

```sql
-- Ver tipos de actividad
SELECT * FROM attendance_activity_types;

-- Ver asistencias
SELECT * FROM teacher_attendances ORDER BY created_at DESC;

-- Ver períodos de nómina
SELECT * FROM payroll_periods ORDER BY start_date DESC;

-- Ver líneas de nómina con totales
SELECT
    pl.*,
    t.first_name + ' ' + t.last_name AS teacher_name
FROM payroll_lines pl
JOIN teachers t ON pl.teacher_uuid = t.uuid
ORDER BY pl.created_at DESC;

-- Ver resumen de nómina por período
SELECT
    pp.name AS period_name,
    COUNT(pl.uuid) AS teacher_count,
    SUM(pl.total_gross_payment) AS total_gross,
    SUM(pl.total_penalties) AS total_penalties,
    SUM(pl.total_net_payment) AS total_net
FROM payroll_periods pp
LEFT JOIN payroll_lines pl ON pp.uuid = pl.payroll_period_uuid
GROUP BY pp.uuid, pp.name
ORDER BY pp.start_date DESC;
```

## Troubleshooting

### Problema: Error 401 Unauthorized

**Causa**: Token expirado o inválido

**Solución**:
```bash
# Regenerar token
export TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' \
  | jq -r '.data.token')
```

### Problema: Error de conexión

**Causa**: Servidor no está corriendo

**Solución**:
```bash
# Verificar si el servidor está corriendo
ps aux | grep java | grep remashorario

# Si no está corriendo, iniciarlo
./gradlew bootRun
```

### Problema: Error 404 en endpoints

**Causa**: URL incorrecta o endpoint no existe

**Solución**:
- Verifica la URL base (debe ser `http://localhost:8080`)
- Verifica que el endpoint existe en la guía `API_TESTING_GUIDE.md`
- Asegúrate de incluir `/api/protected` en la ruta

### Problema: Script Python falla con error de importación

**Causa**: Módulo `requests` no instalado

**Solución**:
```bash
pip install requests
# o
pip3 install requests
```

### Problema: Datos no se están guardando

**Causa**: SQL Server no está corriendo o credenciales incorrectas

**Solución**:
```bash
# Verificar SQL Server
docker ps | grep sqlserver
# o si está instalado localmente
systemctl status mssql-server

# Verificar credenciales en application.properties
cat src/main/resources/application.properties | grep datasource
```

## Próximos Pasos

Una vez que hayas probado todos los endpoints:

1. ✅ Verifica que los datos se guarden correctamente en la base de datos
2. ✅ Prueba casos extremos (fechas inválidas, UUIDs inexistentes, etc.)
3. ✅ Documenta cualquier bug encontrado
4. ✅ Procede con el desarrollo del frontend

## Recursos Adicionales

- **API_TESTING_GUIDE.md**: Documentación completa de todos los endpoints
- **test_api_endpoints.py**: Script de pruebas automatizadas
- **Postman Collections**: Crea una colección con los endpoints para futuras pruebas

## Contacto

Si encuentras problemas o necesitas ayuda, documenta:
1. El endpoint que estás probando
2. El payload enviado
3. La respuesta recibida
4. Los logs del servidor
