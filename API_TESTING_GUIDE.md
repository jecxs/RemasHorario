# Guía de Pruebas de API - Sistema de Asistencia y Contabilidad Docente

## Tabla de Contenidos
1. [Configuración Inicial](#configuración-inicial)
2. [AttendanceActivityType - Tipos de Actividad](#1-attendanceactivitytype---tipos-de-actividad)
3. [TeacherAttendance - Asistencia de Docentes](#2-teacherattendance---asistencia-de-docentes)
4. [AcademicCalendarException - Excepciones de Calendario](#3-academiccalendarexception---excepciones-de-calendario)
5. [ExtraAssignment - Asignaciones Extra](#4-extraassignment---asignaciones-extra)
6. [TeacherRate - Tarifas de Docentes](#5-teacherrate---tarifas-de-docentes)
7. [ModalityRate - Tarifas por Modalidad](#6-modalityrate---tarifas-por-modalidad)
8. [DefaultRate - Tarifas por Defecto](#7-defaultrate---tarifas-por-defecto)
9. [PayrollPeriod - Períodos de Nómina](#8-payrollperiod---períodos-de-nómina)
10. [PayrollLine - Líneas de Nómina](#9-payrollline---líneas-de-nómina)

---

## Configuración Inicial

### Variables de Entorno
```bash
export BASE_URL="http://localhost:8080"
export API_BASE="${BASE_URL}/api/protected"
export TOKEN="your_jwt_token_here"
```

### Obtener Token de Autenticación
```bash
# Login
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

### Header de Autenticación para todas las peticiones
```bash
-H "Authorization: Bearer ${TOKEN}"
```

---

## 1. AttendanceActivityType - Tipos de Actividad

Base URL: `/api/protected/attendance-activity-types`

### 1.1 Inicializar Tipos de Actividad por Defecto
```bash
curl -X POST "${API_BASE}/attendance-activity-types/initialize-defaults" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Tipos de actividad por defecto creados con éxito",
  "data": null
}
```

### 1.2 Obtener Todos los Tipos de Actividad
```bash
curl -X GET "${API_BASE}/attendance-activity-types" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Tipos de actividad recuperados con éxito",
  "data": [
    {
      "uuid": "uuid-here",
      "code": "REGULAR_CLASS",
      "name": "Clase Regular",
      "description": "Clase regular según horario",
      "createdAt": "2025-11-22T10:00:00",
      "updatedAt": "2025-11-22T10:00:00"
    }
  ]
}
```

### 1.3 Obtener Tipo de Actividad por UUID
```bash
curl -X GET "${API_BASE}/attendance-activity-types/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 1.4 Obtener Tipo de Actividad por Código
```bash
curl -X GET "${API_BASE}/attendance-activity-types/code/REGULAR_CLASS" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 1.5 Crear Nuevo Tipo de Actividad
```bash
curl -X POST "${API_BASE}/attendance-activity-types" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WORKSHOP",
    "name": "Taller",
    "description": "Taller extracurricular"
  }'
```

### 1.6 Actualizar Tipo de Actividad
```bash
curl -X PATCH "${API_BASE}/attendance-activity-types/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WORKSHOP",
    "name": "Taller Actualizado",
    "description": "Taller extracurricular actualizado"
  }'
```

### 1.7 Eliminar Tipo de Actividad
```bash
curl -X DELETE "${API_BASE}/attendance-activity-types/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 2. TeacherAttendance - Asistencia de Docentes

Base URL: `/api/protected/teacher-attendances`

### 2.1 Obtener Todas las Asistencias
```bash
curl -X GET "${API_BASE}/teacher-attendances" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.2 Obtener Asistencia por UUID
```bash
curl -X GET "${API_BASE}/teacher-attendances/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.3 Obtener Asistencia con Detalles
```bash
curl -X GET "${API_BASE}/teacher-attendances/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.4 Obtener Asistencias de un Docente
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.5 Obtener Asistencias de un Docente en una Fecha
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/date/2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.6 Obtener Asistencias en un Rango de Fechas (Docente)
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/range?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.7 Obtener Asistencias en un Rango de Fechas (General)
```bash
curl -X GET "${API_BASE}/teacher-attendances/range?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.8 Obtener Asistencias Pendientes de un Docente
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/pending" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.9 Registrar Entrada (Check-In Básico)
```bash
curl -X POST "${API_BASE}/teacher-attendances/check-in" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "teacherUuid": "teacher-uuid-here",
    "classSessionUuid": "class-session-uuid-here",
    "attendanceDate": "2025-11-22"
  }'
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Entrada registrada con éxito",
  "data": {
    "uuid": "attendance-uuid",
    "teacherUuid": "teacher-uuid",
    "classSessionUuid": "class-session-uuid",
    "attendanceDate": "2025-11-22",
    "checkinAt": "2025-11-22T08:00:00",
    "checkoutAt": null,
    "scheduledStartTime": null,
    "scheduledEndTime": null,
    "status": "PENDING"
  }
}
```

### 2.10 Registrar Entrada con Horario (Check-In con Cálculo de Penalizaciones)
```bash
curl -X POST "${API_BASE}/teacher-attendances/check-in-with-schedule" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "teacherUuid": "teacher-uuid-here",
    "classSessionUuid": "class-session-uuid-here",
    "attendanceDate": "2025-11-22",
    "scheduledStartTime": "08:00:00",
    "scheduledEndTime": "09:45:00",
    "scheduledDurationMinutes": 105
  }'
```

### 2.11 Registrar Salida (Check-Out)
```bash
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/check-out" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.12 Aprobar Asistencia (Admin)
```bash
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/approve?adminNote=Aprobado" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.13 Modificar Asistencia (Admin Override)
```bash
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/override" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "checkinAt": "2025-11-22T08:00:00",
    "checkoutAt": "2025-11-22T09:45:00",
    "resetPenalties": true,
    "adminNote": "Corregido por feriado"
  }'
```

### 2.14 Marcar como Feriado
```bash
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/mark-holiday?adminNote=Feriado nacional" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.15 Rechazar Asistencia
```bash
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/reject?adminNote=Rechazado por inconsistencia" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.16 Calcular Total de Minutos Trabajados
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/total-minutes-worked?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.17 Calcular Total de Minutos de Penalización
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/total-penalty-minutes?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 2.18 Obtener Estadísticas de Asistencia
```bash
curl -X GET "${API_BASE}/teacher-attendances/teacher/{teacherUuid}/statistics?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Estadísticas de asistencia calculadas con éxito",
  "data": {
    "totalAttendances": 20,
    "totalMinutesWorked": 1890,
    "totalPenaltyMinutes": 60,
    "totalLateArrivals": 3,
    "totalEarlyDepartures": 1,
    "averageLatenessMinutes": 20
  }
}
```

---

## 3. AcademicCalendarException - Excepciones de Calendario

Base URL: `/api/protected/calendar-exceptions`

### 3.1 Obtener Todas las Excepciones
```bash
curl -X GET "${API_BASE}/calendar-exceptions" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.2 Obtener Excepción por UUID
```bash
curl -X GET "${API_BASE}/calendar-exceptions/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.3 Obtener Excepción por Fecha
```bash
curl -X GET "${API_BASE}/calendar-exceptions/date/2025-12-25" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.4 Obtener Excepciones en Rango de Fechas
```bash
curl -X GET "${API_BASE}/calendar-exceptions/range?startDate=2025-12-01&endDate=2025-12-31" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.5 Obtener Próximas Excepciones
```bash
curl -X GET "${API_BASE}/calendar-exceptions/upcoming?fromDate=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.6 Verificar si es Feriado
```bash
curl -X GET "${API_BASE}/calendar-exceptions/is-holiday/2025-12-25" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Verificación de feriado realizada con éxito",
  "data": true
}
```

### 3.7 Obtener Feriados de un Mes
```bash
curl -X GET "${API_BASE}/calendar-exceptions/month/2025/12" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.8 Crear Excepción de Calendario
```bash
curl -X POST "${API_BASE}/calendar-exceptions" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-25",
    "code": "CHRISTMAS",
    "description": "Navidad"
  }'
```

### 3.9 Actualizar Excepción
```bash
curl -X PATCH "${API_BASE}/calendar-exceptions/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-25",
    "code": "CHRISTMAS",
    "description": "Navidad - Día Festivo"
  }'
```

### 3.10 Eliminar Excepción
```bash
curl -X DELETE "${API_BASE}/calendar-exceptions/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 3.11 Crear Excepciones en Masa
```bash
curl -X POST "${API_BASE}/calendar-exceptions/bulk" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "exceptions": [
      {
        "date": "2025-12-25",
        "code": "CHRISTMAS",
        "description": "Navidad"
      },
      {
        "date": "2025-12-31",
        "code": "NEW_YEAR_EVE",
        "description": "Víspera de Año Nuevo"
      }
    ]
  }'
```

---

## 4. ExtraAssignment - Asignaciones Extra

Base URL: `/api/protected/extra-assignments`

### 4.1 Obtener Todas las Asignaciones
```bash
curl -X GET "${API_BASE}/extra-assignments" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.2 Obtener Asignación por UUID
```bash
curl -X GET "${API_BASE}/extra-assignments/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.3 Obtener Asignación con Detalles
```bash
curl -X GET "${API_BASE}/extra-assignments/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.4 Obtener Asignaciones de un Docente
```bash
curl -X GET "${API_BASE}/extra-assignments/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.5 Obtener Asignaciones por Fecha (Docente)
```bash
curl -X GET "${API_BASE}/extra-assignments/teacher/{teacherUuid}/date/2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.6 Obtener Asignaciones en Rango (Docente)
```bash
curl -X GET "${API_BASE}/extra-assignments/teacher/{teacherUuid}/range?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.7 Obtener Asignaciones en Rango (General)
```bash
curl -X GET "${API_BASE}/extra-assignments/range?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.8 Obtener Asignaciones por Tipo de Actividad
```bash
curl -X GET "${API_BASE}/extra-assignments/activity-type/{activityTypeUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.9 Calcular Pago de una Asignación
```bash
curl -X GET "${API_BASE}/extra-assignments/{uuid}/calculate-payment" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Pago de asignación extra calculado con éxito",
  "data": 150.00
}
```

### 4.10 Obtener Total de Horas (Docente)
```bash
curl -X GET "${API_BASE}/extra-assignments/teacher/{teacherUuid}/total-hours?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.11 Obtener Total de Pago (Docente)
```bash
curl -X GET "${API_BASE}/extra-assignments/teacher/{teacherUuid}/total-payment?startDate=2025-11-01&endDate=2025-11-30" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.12 Crear Asignación Extra
```bash
curl -X POST "${API_BASE}/extra-assignments" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "teacherUuid": "teacher-uuid-here",
    "activityTypeUuid": "activity-type-uuid-here",
    "title": "Taller de Matemáticas",
    "assignmentDate": "2025-11-25",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "ratePerHour": 50.00,
    "notes": "Taller para estudiantes de secundaria"
  }'
```

### 4.13 Actualizar Asignación
```bash
curl -X PATCH "${API_BASE}/extra-assignments/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Taller de Matemáticas Avanzadas",
    "assignmentDate": "2025-11-25",
    "startTime": "14:00:00",
    "endTime": "17:00:00",
    "ratePerHour": 60.00,
    "notes": "Taller extendido"
  }'
```

### 4.14 Eliminar Asignación
```bash
curl -X DELETE "${API_BASE}/extra-assignments/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.15 Crear Asignaciones en Masa
```bash
curl -X POST "${API_BASE}/extra-assignments/bulk" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "teacherUuid": "teacher-uuid-1",
      "activityTypeUuid": "activity-type-uuid",
      "title": "Taller 1",
      "assignmentDate": "2025-11-25",
      "startTime": "14:00:00",
      "endTime": "16:00:00",
      "ratePerHour": 50.00,
      "notes": ""
    },
    {
      "teacherUuid": "teacher-uuid-2",
      "activityTypeUuid": "activity-type-uuid",
      "title": "Taller 2",
      "assignmentDate": "2025-11-26",
      "startTime": "15:00:00",
      "endTime": "17:00:00",
      "ratePerHour": 50.00,
      "notes": ""
    }
  ]'
```

---

## 5. TeacherRate - Tarifas de Docentes

Base URL: `/api/protected/teacher-rates`

### 5.1 Obtener Todas las Tarifas
```bash
curl -X GET "${API_BASE}/teacher-rates" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.2 Obtener Tarifa por UUID
```bash
curl -X GET "${API_BASE}/teacher-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.3 Obtener Tarifa con Detalles
```bash
curl -X GET "${API_BASE}/teacher-rates/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.4 Obtener Tarifas de un Docente
```bash
curl -X GET "${API_BASE}/teacher-rates/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.5 Obtener Tarifas por Tipo de Actividad
```bash
curl -X GET "${API_BASE}/teacher-rates/activity-type/{activityTypeUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.6 Obtener Tarifa Activa (Docente + Tipo de Actividad)
```bash
curl -X GET "${API_BASE}/teacher-rates/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.7 Obtener Tarifas Activas de un Docente
```bash
curl -X GET "${API_BASE}/teacher-rates/teacher/{teacherUuid}/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.8 Verificar si Tiene Tarifa Específica
```bash
curl -X GET "${API_BASE}/teacher-rates/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/has-specific-rate?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.9 Obtener Tarifa por Minuto
```bash
curl -X GET "${API_BASE}/teacher-rates/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/rate-per-minute?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Tarifa por minuto calculada con éxito",
  "data": 0.833333
}
```

### 5.10 Crear Tarifa de Docente
```bash
curl -X POST "${API_BASE}/teacher-rates" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "teacherUuid": "teacher-uuid-here",
    "activityTypeUuid": "activity-type-uuid-here",
    "ratePerHour": 50.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": null
  }'
```

### 5.11 Actualizar Tarifa
```bash
curl -X PATCH "${API_BASE}/teacher-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "ratePerHour": 55.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": "2025-12-31"
  }'
```

### 5.12 Cerrar Tarifa
```bash
curl -X PATCH "${API_BASE}/teacher-rates/{uuid}/close" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.13 Crear Nueva Versión de Tarifa
```bash
curl -X POST "${API_BASE}/teacher-rates/teacher/{teacherUuid}/activity-type/{activityTypeUuid}/new-version?newRatePerHour=60.00&effectiveFrom=2025-12-01" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.14 Eliminar Tarifa
```bash
curl -X DELETE "${API_BASE}/teacher-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 5.15 Crear Tarifas en Masa (Docente)
```bash
curl -X POST "${API_BASE}/teacher-rates/teacher/{teacherUuid}/bulk?effectiveFrom=2025-11-01" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "activityTypeUuid": "activity-type-uuid-1",
      "ratePerHour": 50.00
    },
    {
      "activityTypeUuid": "activity-type-uuid-2",
      "ratePerHour": 60.00
    }
  ]'
```

---

## 6. ModalityRate - Tarifas por Modalidad

Base URL: `/api/protected/modality-rates`

### 6.1 Obtener Todas las Tarifas
```bash
curl -X GET "${API_BASE}/modality-rates" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.2 Obtener Tarifa por UUID
```bash
curl -X GET "${API_BASE}/modality-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.3 Obtener Tarifa con Detalles
```bash
curl -X GET "${API_BASE}/modality-rates/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.4 Obtener Tarifas de una Modalidad
```bash
curl -X GET "${API_BASE}/modality-rates/modality/{modalityUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.5 Obtener Tarifas por Tipo de Actividad
```bash
curl -X GET "${API_BASE}/modality-rates/activity-type/{activityTypeUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.6 Obtener Tarifa Activa (Modalidad + Tipo de Actividad)
```bash
curl -X GET "${API_BASE}/modality-rates/modality/{modalityUuid}/activity-type/{activityTypeUuid}/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.7 Obtener Tarifas Activas de una Modalidad
```bash
curl -X GET "${API_BASE}/modality-rates/modality/{modalityUuid}/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.8 Obtener Tarifa por Minuto
```bash
curl -X GET "${API_BASE}/modality-rates/modality/{modalityUuid}/activity-type/{activityTypeUuid}/rate-per-minute?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.9 Crear Tarifa por Modalidad
```bash
curl -X POST "${API_BASE}/modality-rates" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "modalityUuid": "modality-uuid-here",
    "activityTypeUuid": "activity-type-uuid-here",
    "ratePerHour": 45.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": null
  }'
```

### 6.10 Actualizar Tarifa
```bash
curl -X PATCH "${API_BASE}/modality-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "ratePerHour": 50.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": "2025-12-31"
  }'
```

### 6.11 Cerrar Tarifa
```bash
curl -X PATCH "${API_BASE}/modality-rates/{uuid}/close" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.12 Crear Nueva Versión de Tarifa
```bash
curl -X POST "${API_BASE}/modality-rates/modality/{modalityUuid}/activity-type/{activityTypeUuid}/new-version?newRatePerHour=55.00&effectiveFrom=2025-12-01" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 6.13 Eliminar Tarifa
```bash
curl -X DELETE "${API_BASE}/modality-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 7. DefaultRate - Tarifas por Defecto

Base URL: `/api/protected/default-rates`

### 7.1 Obtener Todas las Tarifas
```bash
curl -X GET "${API_BASE}/default-rates" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.2 Obtener Tarifa por UUID
```bash
curl -X GET "${API_BASE}/default-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.3 Obtener Tarifa con Detalles
```bash
curl -X GET "${API_BASE}/default-rates/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.4 Obtener Tarifas por Tipo de Actividad
```bash
curl -X GET "${API_BASE}/default-rates/activity-type/{activityTypeUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.5 Obtener Tarifa Activa por Tipo de Actividad
```bash
curl -X GET "${API_BASE}/default-rates/activity-type/{activityTypeUuid}/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.6 Obtener Todas las Tarifas Activas
```bash
curl -X GET "${API_BASE}/default-rates/active?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.7 Obtener Tarifa por Minuto
```bash
curl -X GET "${API_BASE}/default-rates/activity-type/{activityTypeUuid}/rate-per-minute?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.8 Crear Tarifa por Defecto
```bash
curl -X POST "${API_BASE}/default-rates" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "activityTypeUuid": "activity-type-uuid-here",
    "ratePerHour": 40.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": null
  }'
```

### 7.9 Actualizar Tarifa
```bash
curl -X PATCH "${API_BASE}/default-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "ratePerHour": 45.00,
    "effectiveFrom": "2025-11-01",
    "effectiveTo": "2025-12-31"
  }'
```

### 7.10 Cerrar Tarifa
```bash
curl -X PATCH "${API_BASE}/default-rates/{uuid}/close" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.11 Crear Nueva Versión de Tarifa
```bash
curl -X POST "${API_BASE}/default-rates/activity-type/{activityTypeUuid}/new-version?newRatePerHour=50.00&effectiveFrom=2025-12-01" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 7.12 Eliminar Tarifa
```bash
curl -X DELETE "${API_BASE}/default-rates/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 8. PayrollPeriod - Períodos de Nómina

Base URL: `/api/protected/payroll-periods`

### 8.1 Obtener Todos los Períodos
```bash
curl -X GET "${API_BASE}/payroll-periods" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.2 Obtener Período por UUID
```bash
curl -X GET "${API_BASE}/payroll-periods/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.3 Obtener Períodos por Estado
```bash
# Estados: DRAFT, CALCULATED, APPROVED, PAID
curl -X GET "${API_BASE}/payroll-periods/status/DRAFT" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.4 Obtener Período por Fecha
```bash
curl -X GET "${API_BASE}/payroll-periods/date/2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.5 Obtener Períodos Pendientes
```bash
curl -X GET "${API_BASE}/payroll-periods/pending" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.6 Obtener Períodos Pasados
```bash
curl -X GET "${API_BASE}/payroll-periods/past?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.7 Obtener Períodos Futuros
```bash
curl -X GET "${API_BASE}/payroll-periods/future?date=2025-11-22" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.8 Crear Período de Nómina
```bash
curl -X POST "${API_BASE}/payroll-periods" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Noviembre 2025 - Semana 1",
    "startDate": "2025-11-01",
    "endDate": "2025-11-07"
  }'
```

### 8.9 Actualizar Período
```bash
curl -X PATCH "${API_BASE}/payroll-periods/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Noviembre 2025 - Semana 1 Actualizado",
    "startDate": "2025-11-01",
    "endDate": "2025-11-08"
  }'
```

### 8.10 Eliminar Período
```bash
curl -X DELETE "${API_BASE}/payroll-periods/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.11 Marcar como Calculado
```bash
curl -X PATCH "${API_BASE}/payroll-periods/{uuid}/mark-calculated" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.12 Marcar como Aprobado
```bash
curl -X PATCH "${API_BASE}/payroll-periods/{uuid}/mark-approved" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.13 Marcar como Pagado
```bash
curl -X PATCH "${API_BASE}/payroll-periods/{uuid}/mark-paid" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.14 Revertir a Borrador
```bash
curl -X PATCH "${API_BASE}/payroll-periods/{uuid}/revert-to-draft" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.15 Generar Períodos Semanales
```bash
curl -X POST "${API_BASE}/payroll-periods/generate/weekly?year=2025&month=11" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.16 Generar Períodos Quincenales
```bash
curl -X POST "${API_BASE}/payroll-periods/generate/biweekly?year=2025&month=11" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.17 Generar Período Mensual
```bash
curl -X POST "${API_BASE}/payroll-periods/generate/monthly?year=2025&month=11" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.18 Verificar si se Puede Modificar
```bash
curl -X GET "${API_BASE}/payroll-periods/{uuid}/can-modify" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 8.19 Verificar si se Puede Eliminar
```bash
curl -X GET "${API_BASE}/payroll-periods/{uuid}/can-delete" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 9. PayrollLine - Líneas de Nómina

Base URL: `/api/protected/payroll-lines`

### 9.1 Obtener Todas las Líneas de Nómina
```bash
curl -X GET "${API_BASE}/payroll-lines" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.2 Obtener Línea por UUID
```bash
curl -X GET "${API_BASE}/payroll-lines/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.3 Obtener Línea con Detalles
```bash
curl -X GET "${API_BASE}/payroll-lines/{uuid}/details" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.4 Obtener Líneas de un Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Líneas de nómina del período recuperadas con éxito",
  "data": [
    {
      "uuid": "payroll-line-uuid",
      "payrollPeriodUuid": "period-uuid",
      "teacherUuid": "teacher-uuid",
      "regularHoursWorked": 40.00,
      "extraHoursWorked": 5.00,
      "totalHoursWorked": 45.00,
      "regularPayment": 2000.00,
      "extraPayment": 300.00,
      "totalGrossPayment": 2300.00,
      "totalPenalties": 100.00,
      "totalNetPayment": 2200.00,
      "attendanceCount": 10,
      "extraAssignmentCount": 2,
      "lateArrivalCount": 2,
      "earlyDepartureCount": 1
    }
  ]
}
```

### 9.5 Obtener Líneas de un Docente
```bash
curl -X GET "${API_BASE}/payroll-lines/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.6 Obtener Línea de un Docente en un Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.7 Calcular Nómina de un Docente
```bash
curl -X POST "${API_BASE}/payroll-lines/calculate/period/{payrollPeriodUuid}/teacher/{teacherUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Nómina del docente calculada con éxito",
  "data": {
    "uuid": "new-payroll-line-uuid",
    "payrollPeriodUuid": "period-uuid",
    "teacherUuid": "teacher-uuid",
    "regularHoursWorked": 40.00,
    "extraHoursWorked": 5.00,
    "totalHoursWorked": 45.00,
    "regularPayment": 2000.00,
    "extraPayment": 300.00,
    "totalGrossPayment": 2300.00,
    "totalPenalties": 100.00,
    "totalNetPayment": 2200.00,
    "calculationDetails": "Cálculo basado en asistencias y asignaciones extra"
  }
}
```

### 9.8 Calcular Nómina de Todos los Docentes
```bash
curl -X POST "${API_BASE}/payroll-lines/calculate/period/{payrollPeriodUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.9 Recalcular Nómina de un Período
```bash
curl -X POST "${API_BASE}/payroll-lines/recalculate/period/{payrollPeriodUuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.10 Obtener Total Neto del Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}/total-net-amount" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Total neto del período calculado con éxito",
  "data": 45000.00
}
```

### 9.11 Obtener Total de Penalizaciones del Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}/total-penalties" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.12 Obtener Cantidad de Docentes del Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}/teacher-count" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.13 Eliminar Línea de Nómina
```bash
curl -X DELETE "${API_BASE}/payroll-lines/{uuid}" \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.14 Obtener Resumen del Período
```bash
curl -X GET "${API_BASE}/payroll-lines/period/{payrollPeriodUuid}/summary" \
  -H "Authorization: Bearer ${TOKEN}"
```

**Respuesta Esperada:**
```json
{
  "success": true,
  "message": "Resumen del período calculado con éxito",
  "data": {
    "totalNetAmount": 45000.00,
    "totalPenalties": 1500.00,
    "totalGrossAmount": 46500.00,
    "teacherCount": 25
  }
}
```

---

## Flujo de Pruebas Recomendado

### 1. Configuración Inicial (Una sola vez)
```bash
# 1. Inicializar tipos de actividad por defecto
curl -X POST "${API_BASE}/attendance-activity-types/initialize-defaults" ...

# 2. Crear tarifas por defecto para cada tipo de actividad
curl -X POST "${API_BASE}/default-rates" ...

# 3. Crear tarifas por modalidad (si aplica)
curl -X POST "${API_BASE}/modality-rates" ...

# 4. Crear excepciones de calendario (feriados)
curl -X POST "${API_BASE}/calendar-exceptions/bulk" ...
```

### 2. Configuración de Docentes
```bash
# 1. Crear tarifas específicas para docentes
curl -X POST "${API_BASE}/teacher-rates" ...

# 2. Crear asignaciones extra
curl -X POST "${API_BASE}/extra-assignments" ...
```

### 3. Registro de Asistencias
```bash
# 1. Docente marca entrada
curl -X POST "${API_BASE}/teacher-attendances/check-in-with-schedule" ...

# 2. Docente marca salida
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/check-out" ...

# 3. Admin revisa y aprueba
curl -X PATCH "${API_BASE}/teacher-attendances/{uuid}/approve" ...
```

### 4. Proceso de Nómina
```bash
# 1. Crear período de nómina
curl -X POST "${API_BASE}/payroll-periods/generate/monthly?year=2025&month=11" ...

# 2. Calcular nómina de todos los docentes
curl -X POST "${API_BASE}/payroll-lines/calculate/period/{periodUuid}" ...

# 3. Revisar resumen
curl -X GET "${API_BASE}/payroll-lines/period/{periodUuid}/summary" ...

# 4. Marcar período como calculado
curl -X PATCH "${API_BASE}/payroll-periods/{periodUuid}/mark-calculated" ...

# 5. Aprobar período
curl -X PATCH "${API_BASE}/payroll-periods/{periodUuid}/mark-approved" ...

# 6. Marcar como pagado
curl -X PATCH "${API_BASE}/payroll-periods/{periodUuid}/mark-paid" ...
```

---

## Notas Importantes

### Jerarquía de Tarifas
El sistema aplica las tarifas en el siguiente orden de prioridad:
1. **TeacherRate** - Tarifa específica del docente (máxima prioridad)
2. **ModalityRate** - Tarifa por modalidad educativa
3. **DefaultRate** - Tarifa por defecto (mínima prioridad)

### Estados de Asistencia
- **PENDING**: Entrada registrada, esperando salida
- **COMPLETED**: Entrada y salida registradas
- **APPROVED**: Aprobada por administrador
- **REJECTED**: Rechazada por administrador
- **ADMIN_MODIFIED**: Modificada por administrador

### Estados de Período de Nómina
- **DRAFT**: Borrador, puede ser modificado
- **CALCULATED**: Calculado, nóminas generadas
- **APPROVED**: Aprobado por administrador
- **PAID**: Pagado a los docentes

### Cálculo de Penalizaciones
- Llegada tarde: Se descuentan minutos proporcionales
- Salida temprana: Se descuentan minutos proporcionales
- Tolerancia: 5 minutos configurables
- Cálculo: `(tarifa_por_hora / 60) * minutos_de_retraso`

### Formatos de Fecha y Hora
- Fecha: `YYYY-MM-DD` (ejemplo: `2025-11-22`)
- Hora: `HH:mm:ss` (ejemplo: `08:00:00`)
- Fecha y Hora: `YYYY-MM-DDTHH:mm:ss` (ejemplo: `2025-11-22T08:00:00`)

---

## Testing con Postman

### Importar Colección
Puedes crear una colección de Postman con estos endpoints. Variables de entorno sugeridas:
- `base_url`: `http://localhost:8080`
- `api_base`: `{{base_url}}/api/protected`
- `token`: Tu JWT token

### Scripts de Pre-request
```javascript
// Agregar token automáticamente
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get('token')
});
```

---

## Troubleshooting

### Error 401 Unauthorized
- Verifica que el token JWT sea válido
- Regenera el token si ha expirado (1 hora de duración)

### Error 404 Not Found
- Verifica que la URL esté correcta
- Asegúrate de usar el puerto correcto (default: 8080)

### Error 500 Internal Server Error
- Revisa los logs del servidor
- Verifica que la base de datos esté corriendo
- Confirma que las relaciones entre entidades sean válidas

### Base de Datos
- SQL Server debe estar corriendo en `localhost:1433`
- Base de datos: `HoraRemAlt`
- Usuario: `sa`
- Password: `root`

---

## Conclusión

Este documento cubre todos los endpoints disponibles en el sistema de asistencia y contabilidad de docentes. Asegúrate de seguir el flujo recomendado para obtener los mejores resultados en las pruebas.
