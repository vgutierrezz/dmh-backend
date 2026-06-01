# Digital Money House - Backend Ecosystem 🚀

Este repositorio contiene el ecosistema de microservicios para la plataforma fintech **Digital Money House**, diseñado bajo un enfoque de alta escalabilidad, resiliencia y automatización de flujos financieros.

La arquitectura implementa el patrón de **Base de Datos por Servicio (Database per Service)** para garantizar el desacoplamiento completo de los dominios de negocio, utilizando **Spring Cloud** para la orquestación distribuida.

---

## 🏗️ Arquitectura del Sistema

El ecosistema está compuesto por los siguientes módulos interconectados:

* **`eureka-server`**: Servidor de descubrimiento (Service Discovery) que centraliza el registro dinámico de todas las instancias de microservicios.
* **`api-gateway`**: Punto de entrada único del sistema. Se encarga del enrutamiento inteligente y la seguridad unificada.
* **`auth-service`**: Microservicio dedicado a la autenticación, validación de credenciales (BCrypt) y emisión de tokens **JWT (JJWT 0.12.x)** sin estado.
* **`users-service`**: Gestiona el ciclo de vida de los usuarios y coordina los flujos de registro público mediante validaciones de integridad de datos.
* **`accounts-service`**: Administra las billeteras virtuales, saldos y core financiero. Implementa los algoritmos asincrónicos/sincrónicos de negocio para la generación única de datos bancarios.

### 🔄 Flujo de Registro (Sprint 1)

Cuando un cliente solicita un nuevo registro a través del `users-service`, el sistema ejecuta un flujo distribuido coordinado:



1. El usuario envía sus datos al endpoint público del `users-service`.
2. `users-service` valida la unicidad de campos críticos (DNI, Email) y encripta la contraseña usando **BCrypt**.
3. Mediante **OpenFeign**, se realiza una invocación sincrónica hacia el `accounts-service` delegando la creación de la billetera digital.
4. El `accounts-service` inicializa la cuenta en `$0.0` y ejecuta de forma interna los algoritmos automatizados para asignar:
    * **CVU:** Cadena numérica única de exactamente 22 dígitos.
    * **Alias:** Combinación aleatoria de 3 palabras en español separadas por un punto extraídas de un diccionario local (`aliases.txt`).
5. Se unifican las respuestas y se retorna al cliente el perfil completo con sus credenciales financieras resueltas.

---

## 🛠️ Tecnologías y Herramientas Utilizadas

* **Java 17** (LTS)
* **Spring Boot 3.2.0**
* **Spring Cloud 2023.0.0** (OpenFeign, Netflix Eureka Client & Server, Spring Cloud Gateway)
* **Spring Security 6** & **io.jsonwebtoken (JJWT 0.12.3)**
* **Spring Data JPA** & **Hibernate**
* **MySQL 8** (Bases de datos independientes: `users_db` y `accounts_db`)
* **Lombok** (Optimización de código Boilerplate)
* **SpringDoc OpenAPI 3** (Documentación interactiva con Swagger)
* **RestAssured 5.4.0** & **Spring Security Test** (Frameworks de pruebas automatizadas)

---

## 🗺️ Mapa de Puertos y Endpoints del Ecosistema

| Microservicio | Puerto Base | Endpoint Core | Tipo | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `eureka-server` | `8761` | `/` | UI | Panel de control de instancias registradas. |
| `api-gateway` | `8080` | `/**` | Proxy | Enrutador perimetral del ecosistema. |
| `auth-service` | `8088` | `/auth/login` | `POST` | Autenticación de usuarios y entrega de JWT. |
| `users-service` | `8081` | `/users/register` | `POST` | Registro de clientes e inicialización distribuida. |
| `accounts-service` | `8082` | `/accounts/internal/create` | `POST` | Endpoint interno (Feign) para setup de billetera. |

---

## 🚀 Instrucciones de Configuración y Despliegue Local

### 1. Requisitos Previos
* Contar con el **Java Development Kit (JDK) 17** instalado.
* Tener configurado un gestor de bases de datos **MySQL** corriendo localmente en los puertos correspondientes (`3306`/`3307` o el configurado en tus perfiles `yml`).

### 2. Preparación de las Bases de Datos
Asegurate de que tu servidor MySQL local tenga disponibles los siguientes esquemas independientes. Las tablas físicas serán autogeneradas por Hibernate al iniciar los servicios (`ddl-auto: update`):
```sql
CREATE DATABASE users_db;
CREATE DATABASE accounts_db;