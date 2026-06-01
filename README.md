# DMH Backend - Portfolio Microservicios

Solución backend desarrollada con Spring Cloud que implementa una arquitectura de microservicios para gestión de usuarios y cuentas financieras.

## Arquitectura

El sistema está compuesto por los siguientes microservicios:

- **Eureka Server** (Puerto 8761): Service Discovery - Registro y descubrimiento de servicios
- **API Gateway** (Puerto 8080): Enrutador centralizado y gestión de filtros
- **Auth Service** (Puerto 8088): Autenticación, emisión y validación de JWT
- **Users Service** (Puerto 8081): Gestión de usuarios, perfiles y roles
- **Accounts Service** (Puerto 8082): Gestión de cuentas bancarias, tarjetas y saldo

## Requisitos

- Java 11 o superior
- Maven 3.6+
- Docker y Docker Compose
- MySQL 8.0+

## Instalación Local

### Con Docker Compose

```bash
docker-compose up -d
```

### Manual

1. Iniciar Eureka Server:
```bash
cd eureka-server
mvn spring-boot:run
```

2. Iniciar API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
```

3. Iniciar los servicios adicionales de forma similar

## Documentación

Consulte la carpeta `docs/` para:
- Diagrama de arquitectura
- Colección de Postman para testing

## Testing

Importe la colección `docs/postman/DMH_Sprint1.postman_collection.json` en Postman para realizar pruebas manuales de los endpoints.

