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
- **Comunicación:** WebSockets (STOMP)
- **Almacenamiento de ficheros:** Azure Blob Storage (Azurite en local)
- **Contenerización:** Docker & Docker Compose
- **Despliegue:** Azure App Service

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
| `DATABASE_URL`            | URL de conexión a MySQL                    | Perfil `dev`: `jdbc:mysql://localhost:3306/ecatlim` |
| `DATABASE_USERNAME`       | Usuario de la base de datos                | Perfil `dev`: `admin`                               |
| `DATABASE_PASSWORD`       | Contraseña de la base de datos             | Perfil `dev`: `password`                            |
| `JWT_SECRET`              | Secreto JWT (Base64, ≥ 256 bits: `openssl rand -base64 48`) | Perfil `dev`: secreto solo para local |
| `ECATLIM_LINK`            | URL del frontend (para resets de password) | Perfil `dev`: `http://localhost:4200`               |
| `BLOB_CONNECTION`         | Cadena de conexión de Azure Blob Storage   | Perfil `dev`: Azurite local (`127.0.0.1:10000`)     |
| `NO_REPLY_EMAIL_USERNAME` | Usuario SMTP (Gmail)                       | **REQUERIDO**                         |
| `NO_REPLY_EMAIL_PASSWORD` | Contraseña/Token SMTP                      | **REQUERIDO**                         |

## 🚀 Guía de Inicio Rápido

1. **Levantar los servicios de infraestructura** (MySQL en el puerto `3306` y Azurite en el `10000`):
   ```bash
   docker-compose up -d
   ```
   *`docker-compose.yml` no incluye la aplicación, solo sus dependencias. También puedes usar una instancia local de
   MySQL con una base de datos llamada `ecatlim`.*

2. **Ejecutar la aplicación** (Flyway aplica las migraciones automáticamente al arrancar):
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

INSERT INTO user (password, email, enabled)
VALUES ('$2a$12$d.phqIe.7XzADQr7hzahQe8Ox2SnekB50PjePxoA9F2YjjQND8kjO',
        '<TU_CORREO_ELECTRONICO p.ej: tu_correo+admin_local_ecatlim@gmail.com>',
        true);

SET @admin_id = LAST_INSERT_ID();

INSERT INTO user_profile (user_id, name, surname, scout_group_id)
VALUES (@admin_id,
        '<NOMBRE, p.ej: ADMIN>',
        '<APELLIDO, p.ej: ADMIN>',
        1);

INSERT INTO user_roles (user_id, role)
VALUES (@admin_id, 'ADMIN');
```

- El campo `password` ya contiene la contraseña encriptada **1234** (puedes cambiarla después desde la app).
- Los datos del usuario se reparten en tres tablas: `user` (credenciales), `user_profile` (datos personales) y
  `user_roles` (un usuario puede tener varios roles).
- Sustituye los valores entre `<>` por los datos reales que desees utilizar.

## 🛠️ Scripts y Comandos Maven

- `./mvnw clean install`: Limpia y construye el proyecto generando el archivo JAR.
- `./mvnw spring-boot:run`: Arranca la aplicación en modo desarrollo.
- `./mvnw test`: Ejecuta los tests.

## 🧪 Tests

Para ejecutar los tests del proyecto:

```bash
./mvnw test
```

Los tests se encuentran en `src/test/java`. Actualmente solo hay un test de carga de contexto (`@SpringBootTest`), que
necesita MySQL levantado y las variables de entorno configuradas.

## 📁 Estructura del Proyecto

```text
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org.scoutsdecanarias.ecatlim_backend
│   │   │       ├── core
│   │   │       │   ├── auth          # Login, JWT y recuperación de contraseña
│   │   │       │   ├── configuration # Security, CORS, WebSocket, caché, async, Blob
│   │   │       │   └── exception     # Excepciones y manejador global de errores
│   │   │       ├── shared            # Servicios reutilizables (blob, email, utils)
│   │   │       └── features          # Un paquete por dominio (user, chat, event, ...)
│   │   │           └── <feature>     # controller / dto / entity / repository / service
│   │   └── resources
│   │       ├── db/migration      # Scripts de Flyway
│   │       ├── templates         # Plantillas (Thymeleaf/Email)
│   │       └── application.properties
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🚧 CI/CD y Despliegue

Este proyecto implementa GitFlow para el control de versiones (`master`, `develop`, `test` y ramas `feature/ECL-<n>`).

- Cada push a la rama `test` ejecuta el workflow `.github/workflows/test_ecatlim-test-backend.yml`, que compila el JAR
  y lo despliega en **Azure App Service** (`ecatlim-test-backend`).
- La CI no ejecuta los tests (`-DskipTests`), así que ejecútalos en local antes de subir cambios.

Las convenciones de código y la guía para agentes de IA están en [`AGENTS.md`](AGENTS.md).

---

### 📚 Documentación de referencia

Para más detalles sobre las herramientas utilizadas:

- [Spring Boot 3.4.3](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [Flyway Documentation](https://flywaydb.org/documentation/)