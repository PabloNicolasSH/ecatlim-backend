# 📚 ECATLIM - Backend

Este repositorio contiene el backend de **ECATLIM**, un aula virtual desarrollada para la **Escuela Canaria de Animación y Tiempo Libre Insignia de Madera** 
(Federación Scouts Exploradores de Canarias). La plataforma permite gestionar formaciones destinadas a scouters y personas interesadas en la educación 
con infancia y juventud.

## 🚀 Tecnologías utilizadas

- Java 17
- Spring Boot
- MySQL
- JWT (Autenticación)
- Docker y Docker Compose
- Azure (para despliegue)

## 📁 Estructura del proyecto
├── src <br>
│ ├── main <br>
│ │ ├── java/... # Código fuente <br>
│ │ └── resources # Configuración y properties <br>
├── docker-compose.yml # Contenedor con MySQL + backend <br>
├── pom.xml #Control de librerías de Maven <br>
└── README.md<br> 


## ⚙️ Variables de entorno

Asegúrate de configurar las siguientes variables de entorno en tu entorno local o servicio de despliegue (ej: `.env`, Azure App Configuration, etc.):

- DATABASE_URL=jdbc:mysql://localhost:3306/ecatlim
- DATABASE_USERNAME=your_mysql_user
- DATABASE_PASSWORD=your_mysql_password
- JWT_SECRET=your_jwt_secret
- ECATLIM_LINK=https://domain-name
- NO_REPLY_EMAIL_USERNAME=no-reply@your_email.org
- NO_REPLY_EMAIL_PASSWORD=your_email_password

## 🐳 Uso con Docker
Con Docker Compose puedes levantar la base de datos y la API en segundos.

`docker-compose up --build`

Esto inicia:

- 📦 Un contenedor MySQL con la base de datos ecatlim
- 🔧 Un contenedor con la aplicación Spring Boot

| La base de datos se inicializa automáticamente si está configurado correctamente.

## ▶️ Ejecución manual (sin Docker)
1. Asegúrate de tener MySQL corriendo y una base de datos llamada ecatlim
2. Configura las variables de entorno o application.properties
3. Ejecuta:
`./mvnw spring-boot:run`

## 🚧 CI/CD
Este proyecto implementa GitFlow para control de versiones e integración continua. El despliegue 
está planificado en Azure App Service o contenedor Docker en máquina virtual.

# 🚀 ¿Comenzando a desarrollar ECATLIM - Backend?

### 📚 Documentación de referencia

Para una mejor comprensión y personalización del proyecto, puedes consultar la documentación oficial de las herramientas utilizadas:

- [Documentación oficial de Apache Maven](https://maven.apache.org/guides/index.html)
- [Guía del plugin de Spring Boot para Maven](https://docs.spring.io/spring-boot/3.4.3/maven-plugin)
- [Cómo crear una imagen OCI](https://docs.spring.io/spring-boot/3.4.3/maven-plugin/build-image.html)
- [Spring Web (MVC y REST)](https://docs.spring.io/spring-boot/3.4.3/reference/web/servlet.html)
- [Thymeleaf (motor de plantillas)](https://docs.spring.io/spring-boot/3.4.3/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
- [Spring Security (seguridad y autenticación)](https://docs.spring.io/spring-boot/3.4.3/reference/web/spring-security.html)
- [Spring Data JPA (acceso a datos)](https://docs.spring.io/spring-boot/3.4.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Flyway para migración de base de datos](https://docs.spring.io/spring-boot/3.4.3/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)

### 🛠️ Guías útiles

Estas guías te serán de ayuda para implementar funcionalidades concretas en el backend:

- [Crear un servicio web RESTful](https://spring.io/guides/gs/rest-service/)
- [Servir contenido web con Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Construcción de servicios REST con Spring](https://spring.io/guides/tutorials/rest/)
- [Manejo de formularios con Spring](https://spring.io/guides/gs/handling-form-submission/)
- [Asegurar una aplicación web](https://spring.io/guides/gs/securing-web/)
- [Spring Boot y OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Autenticación de usuarios con LDAP](https://spring.io/guides/gs/authenticating-ldap/)
- [Acceso a datos con Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### 🔧 Recomendación sobre herencia en Maven

Debido al diseño de Maven, los elementos del POM padre se heredan automáticamente al POM del proyecto. Aunque esto es útil en la mayoría de los casos, también puede implicar la herencia de elementos no deseados como `<license>` o `<developers>`.

En este proyecto, se han añadido sobrescrituras vacías para evitar dicha herencia. Si decides usar un POM padre diferente y **sí deseas heredar esos elementos**, simplemente elimina las sobrescrituras vacías en el `pom.xml`.