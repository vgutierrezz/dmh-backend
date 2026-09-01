# Testing Exploratorio - Sprint 1
## Digital Money House (DMH) - Backend

**Versión:** 1.0

---

## 1. Objetivo

Realizar testing exploratorio sobre las funcionalidades implementadas en el Sprint 1 del backend de DMH, con foco en validaciones funcionales, manejo de errores y consistencia de respuestas en los endpoints:

- `POST /users/register`
- `POST /auth/login`
- `POST /user/logout`

---

## 2. Alcance

Se evaluaron los siguientes criterios:

- Alta de usuario con creación de cuenta digital.
- Validaciones de campos obligatorios y unicidad de email.
- Inicio de sesión con validación de credenciales.
- Cierre de sesión según estrategia JWT stateless.
- Comportamiento ante errores esperados (`400`, `500`) y respuestas de éxito.

---

## 3. Ambiente de prueba

- **Sistema operativo:** Windows
- **Servicios:** Eureka, Gateway, `users-service`, `auth-service`, `accounts-service`
- **Base de datos:** MySQL
- **Herramientas:** Postman, logs de Spring Boot, consola PowerShell
- **Versión API probada:** [v1.0]

---

## 4. Estrategia exploratoria (charters)

### Sesión 1 - Registro de usuario
**Duración:** 45 min  
**Charter:** Explorar el endpoint `POST /users/register` verificando validaciones, unicidad y generación de datos de cuenta.

**Casos explorados:**
- Registro válido.
- Email duplicado.
- Campos obligatorios faltantes.
- Formato de email inválido.
- Validación de formato de `CVU` y `alias`.

---

### Sesión 2 - Login
**Duración:** 30 min  
**Charter:** Explorar `POST /auth/login` para validar autenticación y respuesta de token.

**Casos explorados:**
- Credenciales válidas.
- Usuario inexistente.
- Contraseña incorrecta.
- Body incompleto.

---

### Sesión 3 - Logout
**Duración:** 20 min  
**Charter:** Explorar `POST /user/logout` para validar cierre de sesión y comportamiento de seguridad.

**Casos explorados:**
- Logout con token válido.
- Logout sin token.
- Token inválido o malformado.
- Repetición de logout (idempotencia).

---

## 5. Resultados por endpoint

### 5.1 `POST /users/register`
- **Estado:** [OK / Parcial / NOK]
- **Observaciones:** [Completar]
- **Respuesta esperada validada:** [Sí/No]
- **Códigos observados:** [201/200, 400, 500]

### 5.2 `POST /auth/login`
- **Estado:** [OK / Parcial / NOK]
- **Observaciones:** [Completar]
- **Respuesta esperada validada:** [Sí/No]
- **Códigos observados:** [200, 400, 500]

### 5.3 `POST /user/logout`
- **Estado:** [OK / Parcial / NOK]
- **Observaciones:** [Completar]
- **Respuesta esperada validada:** [Sí/No]
- **Códigos observados:** [200, 500]

---

## 6. Hallazgos

| ID | Severidad | Endpoint | Descripción | Evidencia | Estado |
|----|-----------|----------|-------------|-----------|--------|
| EX-01 | [Alta/Media/Baja] | `/users/register` | [Completar] | `evidencias/captura_01.png` | [Abierto/Cerrado] |
| EX-02 | [Alta/Media/Baja] | `/auth/login` | [Completar] | `evidencias/captura_02.png` | [Abierto/Cerrado] |
| EX-03 | [Alta/Media/Baja] | `/user/logout` | [Completar] | `evidencias/log_01.txt` | [Abierto/Cerrado] |

---

## 7. Riesgos detectados

- [R1] [Completar]
- [R2] [Completar]
- [R3] [Completar]

---

## 8. Conclusiones

El testing exploratorio permitió cubrir los flujos críticos del Sprint 1 e identificar [cantidad] hallazgos relevantes.  
Se recomienda, para el Sprint 2:

1. Fortalecer validaciones de entrada y mensajes de error.
2. Completar pruebas automatizadas (RestAssured/JUnit) para los casos críticos.
3. Revisar consistencia de códigos HTTP y contratos de respuesta.

---

## 9. Anexos

- Colección Postman: `docs/postman/DMH_Sprint1.postman_collection.json`
- Evidencias:
    - `evidencias/captura_01.png`
    - `evidencias/captura_02.png`
    - `evidencias/log_01.txt`