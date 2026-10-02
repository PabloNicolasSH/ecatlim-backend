# 📚 ECATLIM - Backend

Este repositorio contiene el backend de **ECATLIM**, un aula virtual desarrollada para la **Escuela Canaria de Animación
y Tiempo Libre Insignia de Madera**
(Federación Scouts Exploradores de Canarias). La plataforma permite gestionar formaciones destinadas a scouters y
personas interesadas en la educación
con infancia y juventud.

## 🚀 Stack Tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.4.3
- **Gestor de Dependencias:** Maven
- **Base de Datos:** MySQL 8.0
- **Migraciones:** Flyway
- **Seguridad:** Spring Security + JWT (JSON Web Token)
- **Caché:** Caffeine
- **Comunicación:** WebSockets
- **Contenerización:** Docker & Docker Compose
- **Despliegue:** Azure (Planificado)

## 📋 Requisitos Previos

- **Java 21** o superior.
- **Maven** (o usar el wrapper `./mvnw` incluido).
- **Docker & Docker Compose** (recomendado para la base de datos).
- **MySQL 8.0** (si se opta por ejecución manual sin Docker).

## ⚙️ Configuración y Variables de Entorno

Asegúrate de configurar las siguientes variables de entorno. Puedes definirlas en tu sistema, en un archivo `.env` (si
usas Docker) o directamente en el servicio de despliegue.

| Variable                  | Descripción                                | Valor por Defecto                     |
|:--------------------------|:-------------------------------------------|:--------------------------------------|
| `DATABASE_URL`            | URL de conexión a MySQL                    | `jdbc:mysql://localhost:3306/ecatlim` |
| `DATABASE_USERNAME`       | Usuario de la base de datos                | `admin`                               |
| `DATABASE_PASSWORD`       | Contraseña de la base de datos             | `password`                            |
| `JWT_SECRET`              | Secreto para firmar los tokens JWT         | (Generado por defecto)                |
| `ECATLIM_LINK`            | URL del frontend (para resets de password) | `http://localhost:4200`               |
| `NO_REPLY_EMAIL_USERNAME` | Usuario SMTP (Gmail)                       | **REQUERIDO**                         |
| `NO_REPLY_EMAIL_PASSWORD` | Contraseña/Token SMTP                      | **REQUERIDO**                         |

## 🚀 Guía de Inicio Rápido

### Opción A: Con Docker (Recomendado)

Docker Compose levantará automáticamente la base de datos y la aplicación.

```bash
docker-compose up --build
```

### Opción B: Ejecución Manual

1. **Levantar solo la base de datos:**
   ```bash
   docker-compose up -d db
   ```
   *O asegúrate de tener una instancia de MySQL corriendo localmente con una base de datos llamada `ecatlim`.*

2. **Ejecutar la aplicación:**
   ```bash
   ./mvnw spring-boot:run
   ```

### 🏁 Datos Iniciales para la Base de Datos

Tras arrancar la aplicación por primera vez, es necesario insertar algunos datos mínimos para poder acceder y utilizar
la plataforma. Puedes ejecutar las siguientes queries en la base de datos (ajusta los valores de los placeholders según
corresponda):

```sql
INSERT INTO scout_group (name, province_id, group_number, email)
VALUES ('<NOMBRE, p.ej: ACAICATE>',
        35,
           <NUMERO DE GRUPO, p.ej: 999>,
        '<TU_CORREO_ELECTRONICO p.ej.: tu_correo+acaicate_ecatlim@gmail.com>');

INSERT INTO user (name, surname, role, password, email, scout_group_id, enabled)
VALUES ('<NOMBRE, p.ej: ADMIN>',
        '<APELLIDO, p.ej: ADMIN>',
        'ADMIN',
        '$2a$12$d.phqIe.7XzADQr7hzahQe8Ox2SnekB50PjePxoA9F2YjjQND8kjO',
        '<TU_CORREO_ELECTRONICO p.ej: tu_correo+admin_local_ecatlim@gmail.com>',
        1,
        true);
```

- El campo `password` ya contiene la contraseña encriptada **1234** (puedes cambiarla después desde la app).
- Sustituye los valores entre `<>` por los datos reales que desees utilizar.

## 🛠️ Scripts y Comandos Maven

- `mvn clean install`: Limpia y construye el proyecto generando el archivo JAR.
- `mvn spring-boot:run`: Arranca la aplicación en modo desarrollo.
- `mvn test`: Ejecuta la suite de pruebas unitarias e integración.
- `mvn flyway:migrate`: Ejecuta manualmente las migraciones de base de datos (habitualmente automático al arrancar).

## 🧪 Tests

Para ejecutar los tests del proyecto:

```bash
./mvnw test
```

Los tests se encuentran en `src/test/java`. Actualmente incluye tests de carga de contexto
y [TODO: añadir descripción de cobertura de tests adicionales].

## 📁 Estructura del Proyecto

```text
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org.scoutsdecanarias.ecatlim_backend
│   │   │       ├── auth          # Lógica de autenticación y JWT
│   │   │       ├── configuration # Configuración de Spring (CORS, WS, Security)
│   │   │       ├── controller    # Endpoints REST
│   │   │       ├── dto           # Objetos de transferencia de datos
│   │   │       ├── entity        # Entidades JPA (Modelo de base de datos)
│   │   │       ├── repository    # Interfaces de acceso a datos
│   │   │       └── service       # Lógica de negocio
│   │   └── resources
│   │       ├── db/migration      # Scripts de Flyway
│   │       ├── templates         # Plantillas (Thymeleaf/Email)
│   │       └── application.properties
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🚧 CI/CD y Despliegue

Este proyecto implementa GitFlow para el control de versiones. El despliegue está configurado/planificado para:

- **Azure App Service**
- **Contenedores Docker** en infraestructura cloud.

---

### 📚 Documentación de referencia

Para más detalles sobre las herramientas utilizadas:

- [Spring Boot 3.4.3](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [Flyway Documentation](https://flywaydb.org/documentation/)