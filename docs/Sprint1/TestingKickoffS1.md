# Testing Kickoff - Sprint 1
## Digital Money House (DMH) - Backend


**Versión:** 1.0

---

## 1. Objetivo del Kickoff

Definir el enfoque de pruebas para Sprint 1, estableciendo alcance, riesgos, prioridades, tipos de prueba, ambientes, criterios de entrada/salida y entregables.

---

## 2. Alcance funcional del Sprint 1

### Historias incluidas
1. Registro de usuario y creación de cuenta digital.
2. Inicio de sesión con credenciales.
3. Cierre de sesión.

### Endpoints bajo prueba
- `POST /users/register`
- `POST /auth/login`
- `POST /user/logout`

### Fuera de alcance (Sprint 1)
- Rendimiento/carga formal.
- Pentesting avanzado.
- Integración completa con IAM externo (Keycloak productivo), salvo pruebas de factibilidad.

---

## 3. Riesgos iniciales identificados

| ID | Riesgo | Impacto | Probabilidad | Mitigación |
|----|--------|---------|--------------|------------|
| R1 | Validaciones incompletas en registro | Alto | Media | Casos negativos obligatorios en Postman |
| R2 | Inconsistencia de códigos HTTP | Medio | Alta | Definir contrato esperado por endpoint |
| R3 | Conflicto de puertos/ambiente | Medio | Media | Checklist de puertos antes de ejecutar |
| R4 | Errores de conexión DB | Alto | Media | Verificar datasource y DB existentes |
| R5 | Manejo inseguro de token | Alto | Baja | Revisar expiración y formato JWT |

---

## 4. Estrategia de testing

### 4.1 Tipos de prueba
- **Exploratorio:** detectar riesgos tempranos y comportamiento no previsto.
- **Manual funcional:** validar casos positivos/negativos por endpoint.
- **Smoke técnico:** disponibilidad de servicios, DB y rutas principales.
- **Base para automatización:** dejar casos listos para RestAssured en Sprint 2.

### 4.2 Priorización
1. Flujo de `register` (crítico).
2. Flujo de `login` (crítico).
3. Flujo de `logout` (medio, según estrategia JWT stateless).

---

## 5. Ambiente de pruebas

- **OS:** Windows
- **Servicios:** Eureka, Gateway, `users-service`, `auth-service`, `accounts-service`
- **DB:** MySQL
- **Herramientas:** Postman, logs de Spring Boot, PowerShell
- **Control de versiones:** GitHub (`main`, `dev`, `test`)

---

## 6. Datos de prueba

### Usuarios
- Usuario válido nuevo.
- Usuario con email ya existente.
- Usuario con email inválido.
- Usuario con campos faltantes.

### Credenciales
- Password válida.
- Password incorrecta.
- Password vacía/corta.

### Tokens
- JWT válido.
- JWT malformado.
- Token ausente.

---

## 7. Criterios de entrada y salida

### Entrada
- Servicios levantados y accesibles.
- Base de datos conectada.
- Endpoints disponibles.
- Colección Postman actualizada.

### Salida
- Casos críticos ejecutados.
- Evidencia adjunta (capturas/logs).
- Hallazgos registrados con severidad.
- Estado final por historia: OK / Parcial / NOK.

---

## 8. Entregables del Sprint 1 (Testing)

1. `TestingExploratorioS1.md`
2. `TestingKickoffS1.md`
3. Planilla de casos manuales ejecutados
4. Evidencias en carpeta `evidencias/`
5. Colección Postman actualizada

---

## 9. Roles y responsabilidades

- **Tester QA:** diseño/ejecución de casos, reporte de hallazgos.
- **Dev Backend:** corrección de defectos, soporte técnico.
- **Equipo:** validación de criterios de aceptación y cierre.

---

## 10. Cronograma breve sugerido

- Día 1: Kickoff + definición de alcance y riesgos.
- Día 2: Ejecución exploratoria.
- Día 3: Casos manuales y consolidación de evidencia.
- Día 4: Cierre de hallazgos y estad2o del sprint.

---

## 11. Aprobación

**Responsable QA:** [Completar]  
**Responsable Técnico:** [Valentina Gutiérrez]  
**Estado del plan:** [Aprobado]