#!/usr/bin/env python3
"""
Script de pruebas automatizadas para los endpoints del sistema de asistencia y contabilidad docente
Autor: Sistema
Fecha: 2025-11-22
"""

import requests
import json
import sys
from datetime import datetime, timedelta
from typing import Dict, Any, Optional, List

class Colors:
    """Códigos de colores para la terminal"""
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

class APITester:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.api_base = f"{base_url}/api/protected"
        self.token = None
        self.test_results = {
            "passed": 0,
            "failed": 0,
            "total": 0
        }
        # UUIDs de prueba (se llenarán durante las pruebas)
        self.test_uuids = {
            "activity_type": None,
            "teacher": None,
            "class_session": None,
            "attendance": None,
            "calendar_exception": None,
            "extra_assignment": None,
            "teacher_rate": None,
            "modality_rate": None,
            "default_rate": None,
            "payroll_period": None,
            "payroll_line": None,
            "modality": None
        }

    def print_header(self, text: str):
        """Imprime un header colorido"""
        print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
        print(f"{Colors.HEADER}{Colors.BOLD}{text.center(80)}{Colors.ENDC}")
        print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")

    def print_subheader(self, text: str):
        """Imprime un subheader"""
        print(f"\n{Colors.OKBLUE}{Colors.BOLD}{text}{Colors.ENDC}")
        print(f"{Colors.OKBLUE}{'-'*len(text)}{Colors.ENDC}")

    def print_success(self, text: str):
        """Imprime mensaje de éxito"""
        print(f"{Colors.OKGREEN}✓ {text}{Colors.ENDC}")

    def print_error(self, text: str):
        """Imprime mensaje de error"""
        print(f"{Colors.FAIL}✗ {text}{Colors.ENDC}")

    def print_warning(self, text: str):
        """Imprime mensaje de advertencia"""
        print(f"{Colors.WARNING}⚠ {text}{Colors.ENDC}")

    def print_info(self, text: str):
        """Imprime mensaje informativo"""
        print(f"{Colors.OKCYAN}ℹ {text}{Colors.ENDC}")

    def login(self, username: str = "admin", password: str = "password") -> bool:
        """Realiza login y obtiene el token JWT"""
        self.print_subheader("Autenticación")
        try:
            response = requests.post(
                f"{self.base_url}/api/auth/login",
                json={"username": username, "password": password},
                timeout=10
            )

            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    self.token = data.get("data", {}).get("token")
                    if self.token:
                        self.print_success(f"Login exitoso. Token obtenido.")
                        return True

            self.print_error(f"Login fallido: {response.status_code}")
            return False

        except Exception as e:
            self.print_error(f"Error al hacer login: {str(e)}")
            return False

    def get_headers(self) -> Dict[str, str]:
        """Retorna los headers con autenticación"""
        return {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

    def test_endpoint(self, method: str, endpoint: str, data: Optional[Dict] = None,
                     expected_status: int = 200, description: str = "") -> Optional[Dict]:
        """
        Prueba un endpoint y retorna la respuesta
        """
        self.test_results["total"] += 1
        url = f"{self.api_base}{endpoint}"

        try:
            if method.upper() == "GET":
                response = requests.get(url, headers=self.get_headers(), timeout=10)
            elif method.upper() == "POST":
                response = requests.post(url, headers=self.get_headers(), json=data, timeout=10)
            elif method.upper() == "PATCH":
                response = requests.patch(url, headers=self.get_headers(), json=data, timeout=10)
            elif method.upper() == "DELETE":
                response = requests.delete(url, headers=self.get_headers(), timeout=10)
            else:
                raise ValueError(f"Método HTTP no soportado: {method}")

            # Verificar status code
            if response.status_code == expected_status:
                self.test_results["passed"] += 1
                self.print_success(f"{description or endpoint} - Status {response.status_code}")

                # Intentar parsear JSON
                try:
                    return response.json()
                except:
                    return {"status_code": response.status_code}
            else:
                self.test_results["failed"] += 1
                self.print_error(f"{description or endpoint} - Esperado {expected_status}, recibido {response.status_code}")
                try:
                    error_data = response.json()
                    self.print_warning(f"  Error: {error_data.get('message', 'Sin mensaje')}")
                except:
                    pass
                return None

        except Exception as e:
            self.test_results["failed"] += 1
            self.print_error(f"{description or endpoint} - Error: {str(e)}")
            return None

    # ==================== ATTENDANCE ACTIVITY TYPE ====================

    def test_attendance_activity_types(self):
        """Prueba todos los endpoints de AttendanceActivityType"""
        self.print_header("1. ATTENDANCE ACTIVITY TYPE - Tipos de Actividad")

        # 1.1 Inicializar tipos por defecto
        self.print_subheader("1.1 Inicializar Tipos por Defecto")
        result = self.test_endpoint(
            "POST",
            "/attendance-activity-types/initialize-defaults",
            description="Inicializar tipos de actividad por defecto"
        )

        # 1.2 Obtener todos los tipos
        self.print_subheader("1.2 Obtener Todos los Tipos")
        result = self.test_endpoint(
            "GET",
            "/attendance-activity-types",
            description="Obtener todos los tipos de actividad"
        )

        if result and result.get("data"):
            types = result["data"]
            if types:
                self.test_uuids["activity_type"] = types[0]["uuid"]
                self.print_info(f"  UUID de prueba guardado: {self.test_uuids['activity_type']}")

        # 1.3 Crear nuevo tipo
        self.print_subheader("1.3 Crear Nuevo Tipo")
        new_type_data = {
            "code": "TEST_WORKSHOP",
            "name": "Taller de Prueba",
            "description": "Taller de prueba para testing"
        }
        result = self.test_endpoint(
            "POST",
            "/attendance-activity-types",
            data=new_type_data,
            expected_status=201,
            description="Crear nuevo tipo de actividad"
        )

        if result and result.get("data"):
            created_uuid = result["data"]["uuid"]

            # 1.4 Obtener por UUID
            self.print_subheader("1.4 Obtener por UUID")
            self.test_endpoint(
                "GET",
                f"/attendance-activity-types/{created_uuid}",
                description="Obtener tipo de actividad por UUID"
            )

            # 1.5 Obtener por código
            self.print_subheader("1.5 Obtener por Código")
            self.test_endpoint(
                "GET",
                "/attendance-activity-types/code/TEST_WORKSHOP",
                description="Obtener tipo de actividad por código"
            )

            # 1.6 Actualizar tipo
            self.print_subheader("1.6 Actualizar Tipo")
            update_data = {
                "code": "TEST_WORKSHOP",
                "name": "Taller de Prueba Actualizado",
                "description": "Descripción actualizada"
            }
            self.test_endpoint(
                "PATCH",
                f"/attendance-activity-types/{created_uuid}",
                data=update_data,
                description="Actualizar tipo de actividad"
            )

            # 1.7 Eliminar tipo
            self.print_subheader("1.7 Eliminar Tipo")
            self.test_endpoint(
                "DELETE",
                f"/attendance-activity-types/{created_uuid}",
                description="Eliminar tipo de actividad"
            )

    # ==================== ACADEMIC CALENDAR EXCEPTION ====================

    def test_calendar_exceptions(self):
        """Prueba todos los endpoints de AcademicCalendarException"""
        self.print_header("3. ACADEMIC CALENDAR EXCEPTION - Excepciones de Calendario")

        # 3.1 Obtener todas las excepciones
        self.print_subheader("3.1 Obtener Todas las Excepciones")
        self.test_endpoint(
            "GET",
            "/calendar-exceptions",
            description="Obtener todas las excepciones de calendario"
        )

        # 3.2 Crear excepción
        self.print_subheader("3.2 Crear Excepción")
        exception_data = {
            "date": "2025-12-25",
            "code": "CHRISTMAS_TEST",
            "description": "Navidad - Prueba"
        }
        result = self.test_endpoint(
            "POST",
            "/calendar-exceptions",
            data=exception_data,
            expected_status=201,
            description="Crear excepción de calendario"
        )

        if result and result.get("data"):
            exception_uuid = result["data"]["uuid"]
            self.test_uuids["calendar_exception"] = exception_uuid

            # 3.3 Obtener por UUID
            self.print_subheader("3.3 Obtener por UUID")
            self.test_endpoint(
                "GET",
                f"/calendar-exceptions/{exception_uuid}",
                description="Obtener excepción por UUID"
            )

            # 3.4 Obtener por fecha
            self.print_subheader("3.4 Obtener por Fecha")
            self.test_endpoint(
                "GET",
                "/calendar-exceptions/date/2025-12-25",
                description="Obtener excepción por fecha"
            )

            # 3.5 Verificar si es feriado
            self.print_subheader("3.5 Verificar si es Feriado")
            self.test_endpoint(
                "GET",
                "/calendar-exceptions/is-holiday/2025-12-25",
                description="Verificar si la fecha es feriado"
            )

            # 3.6 Obtener excepciones en rango
            self.print_subheader("3.6 Obtener Excepciones en Rango")
            self.test_endpoint(
                "GET",
                "/calendar-exceptions/range?startDate=2025-12-01&endDate=2025-12-31",
                description="Obtener excepciones en rango de fechas"
            )

            # 3.7 Actualizar excepción
            self.print_subheader("3.7 Actualizar Excepción")
            update_data = {
                "date": "2025-12-25",
                "code": "CHRISTMAS_TEST",
                "description": "Navidad - Actualizado"
            }
            self.test_endpoint(
                "PATCH",
                f"/calendar-exceptions/{exception_uuid}",
                data=update_data,
                description="Actualizar excepción de calendario"
            )

            # 3.8 Eliminar excepción
            self.print_subheader("3.8 Eliminar Excepción")
            self.test_endpoint(
                "DELETE",
                f"/calendar-exceptions/{exception_uuid}",
                description="Eliminar excepción de calendario"
            )

    # ==================== PAYROLL PERIOD ====================

    def test_payroll_periods(self):
        """Prueba todos los endpoints de PayrollPeriod"""
        self.print_header("8. PAYROLL PERIOD - Períodos de Nómina")

        # 8.1 Obtener todos los períodos
        self.print_subheader("8.1 Obtener Todos los Períodos")
        self.test_endpoint(
            "GET",
            "/payroll-periods",
            description="Obtener todos los períodos de nómina"
        )

        # 8.2 Crear período
        self.print_subheader("8.2 Crear Período")
        period_data = {
            "name": "Noviembre 2025 - Prueba",
            "startDate": "2025-11-01",
            "endDate": "2025-11-30"
        }
        result = self.test_endpoint(
            "POST",
            "/payroll-periods",
            data=period_data,
            expected_status=201,
            description="Crear período de nómina"
        )

        if result and result.get("data"):
            period_uuid = result["data"]["uuid"]
            self.test_uuids["payroll_period"] = period_uuid

            # 8.3 Obtener por UUID
            self.print_subheader("8.3 Obtener por UUID")
            self.test_endpoint(
                "GET",
                f"/payroll-periods/{period_uuid}",
                description="Obtener período por UUID"
            )

            # 8.4 Obtener períodos pendientes
            self.print_subheader("8.4 Obtener Períodos Pendientes")
            self.test_endpoint(
                "GET",
                "/payroll-periods/pending",
                description="Obtener períodos pendientes"
            )

            # 8.5 Verificar si se puede modificar
            self.print_subheader("8.5 Verificar si se Puede Modificar")
            self.test_endpoint(
                "GET",
                f"/payroll-periods/{period_uuid}/can-modify",
                description="Verificar si el período se puede modificar"
            )

            # 8.6 Marcar como calculado
            self.print_subheader("8.6 Marcar como Calculado")
            self.test_endpoint(
                "PATCH",
                f"/payroll-periods/{period_uuid}/mark-calculated",
                description="Marcar período como calculado"
            )

            # 8.7 Revertir a borrador
            self.print_subheader("8.7 Revertir a Borrador")
            self.test_endpoint(
                "PATCH",
                f"/payroll-periods/{period_uuid}/revert-to-draft",
                description="Revertir período a borrador"
            )

            # 8.8 Actualizar período
            self.print_subheader("8.8 Actualizar Período")
            update_data = {
                "name": "Noviembre 2025 - Prueba Actualizado",
                "startDate": "2025-11-01",
                "endDate": "2025-11-30"
            }
            self.test_endpoint(
                "PATCH",
                f"/payroll-periods/{period_uuid}",
                data=update_data,
                description="Actualizar período de nómina"
            )

            # 8.9 Eliminar período
            self.print_subheader("8.9 Eliminar Período")
            self.test_endpoint(
                "DELETE",
                f"/payroll-periods/{period_uuid}",
                description="Eliminar período de nómina"
            )

        # 8.10 Generar período mensual
        self.print_subheader("8.10 Generar Período Mensual")
        self.test_endpoint(
            "POST",
            "/payroll-periods/generate/monthly?year=2025&month=12",
            expected_status=201,
            description="Generar período mensual"
        )

    # ==================== RESUMEN ====================

    def print_summary(self):
        """Imprime el resumen de las pruebas"""
        self.print_header("RESUMEN DE PRUEBAS")

        total = self.test_results["total"]
        passed = self.test_results["passed"]
        failed = self.test_results["failed"]

        success_rate = (passed / total * 100) if total > 0 else 0

        print(f"Total de pruebas:  {total}")
        print(f"{Colors.OKGREEN}Exitosas:          {passed}{Colors.ENDC}")
        print(f"{Colors.FAIL}Fallidas:          {failed}{Colors.ENDC}")
        print(f"Tasa de éxito:     {success_rate:.2f}%")

        if failed == 0:
            print(f"\n{Colors.OKGREEN}{Colors.BOLD}¡Todas las pruebas pasaron exitosamente!{Colors.ENDC}")
        else:
            print(f"\n{Colors.WARNING}{Colors.BOLD}Algunas pruebas fallaron. Revisa los errores arriba.{Colors.ENDC}")

        print(f"\n{Colors.HEADER}{'='*80}{Colors.ENDC}\n")

    def run_all_tests(self):
        """Ejecuta todas las pruebas"""
        self.print_header("PRUEBAS DE API - SISTEMA DE ASISTENCIA Y CONTABILIDAD")

        # Verificar conexión
        self.print_info(f"URL Base: {self.base_url}")

        try:
            response = requests.get(f"{self.base_url}/actuator/health", timeout=5)
            if response.status_code == 200:
                self.print_success("Servidor está disponible")
            else:
                self.print_warning("Servidor respondió pero puede tener problemas")
        except:
            self.print_error("No se puede conectar al servidor. Asegúrate de que esté ejecutándose.")
            return

        # Login
        if not self.login():
            self.print_error("No se pudo autenticar. Verifica las credenciales.")
            return

        # Ejecutar grupos de pruebas
        try:
            self.test_attendance_activity_types()
            self.test_calendar_exceptions()
            self.test_payroll_periods()

            # Aquí se pueden agregar más grupos de pruebas:
            # self.test_teacher_attendance()
            # self.test_extra_assignments()
            # self.test_default_rates()
            # self.test_modality_rates()
            # self.test_teacher_rates()
            # self.test_payroll_lines()

        except KeyboardInterrupt:
            self.print_warning("\nPruebas interrumpidas por el usuario")
        except Exception as e:
            self.print_error(f"Error inesperado: {str(e)}")
        finally:
            self.print_summary()

def main():
    """Función principal"""
    import argparse

    parser = argparse.ArgumentParser(description='Script de pruebas de API')
    parser.add_argument('--url', default='http://localhost:8080', help='URL base del servidor')
    parser.add_argument('--username', default='admin', help='Usuario para login')
    parser.add_argument('--password', default='password', help='Contraseña para login')

    args = parser.parse_args()

    tester = APITester(base_url=args.url)
    tester.run_all_tests()

if __name__ == "__main__":
    main()
