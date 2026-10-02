# AGENTS.md

Guía para agentes de IA (y personas) que trabajan en **ecatlim-backend**, el backend del aula virtual ECATLIM de Scouts de Canarias.

## Stack

- Java 21, Spring Boot 3.4.3, Maven (usar el wrapper `./mvnw`).
- Spring Web, Security, Data JPA, Validation, WebSocket (STOMP), Mail + Thymeleaf, Cache (Caffeine).
- MySQL 8 + Flyway (`flyway-mysql`).
- JWT con `jjwt` 0.11.5, contraseñas con BCrypt(12) y reglas de `passay`.
- Azure Blob Storage (`azure-storage-blob`, Azurite en local) y `thumbnailator` para imágenes.
- Lombok (`@Slf4j`, `@Getter`/`@Setter`, `@RequiredArgsConstructor`). No se usa MapStruct.

## Comandos

```bash
docker-compose up -d          # MySQL (3306) + Azurite (10000). No levanta la app.
./mvnw spring-boot:run        # Arranca la app
./mvnw clean package          # Compila y empaqueta
./mvnw test                   # Tests (requiere MySQL levantado y variables de entorno)
```

Variables de entorno (ver `src/main/resources/application.properties`): `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `ECATLIM_LINK`, `BLOB_CONNECTION`, `NO_REPLY_EMAIL_USERNAME`, `NO_REPLY_EMAIL_PASSWORD`. No hay perfiles de Spring. **Nunca** escribas secretos reales en ficheros versionados.

## Estructura

Paquete raíz: `org.scoutsdecanarias.ecatlim_backend` (`src/main/java/...`).

- `core/`: piezas transversales.
  - `auth/`: login (`POST /auth/login`), `JwtUtil`, `JWTAuthFilter`, `JwtHandshakeInterceptor`, `SecurityUtils`. Subpaquete `password/` para recuperar y cambiar contraseña (`@ValidPassword`).
  - `configuration/`: `WebSecurityConfig`, `WebSocketConfig`, `AsyncConfig`, `CacheConfig`, `AzureBlobConfig`.
  - `exception/`: `EcatlimException`, `ResourceNotFoundException`, `EcatlimErrorResponse`, `ExceptionControllerAdvice`.
- `shared/`: servicios reutilizables: `blob/` (Azure Blob), `email/` (envío asíncrono + plantillas), `utils/`.
- `features/<feature>/`: un paquete por dominio (`user`, `chat`, `event`, `activity`, `learning_resource`, `module`, `lesson_block`, `education_stage`, `education_session`, `enrollment`, `scout_group`, `timeline`, `user_file`).
- `src/main/resources/templates/`: plantillas Thymeleaf de email.
- `src/main/resources/db/migration/`: migraciones Flyway.

Las features más grandes usan subpaquetes `controller/`, `dto/`, `entity/`, `enums/`, `repository/`, `service/`. Otras más pequeñas lo tienen todo en la raíz del paquete. **En features nuevas usa los subpaquetes.** En las existentes, sigue lo que ya haya (por ejemplo, en `chat` las entidades `Chat` y `ChatMessage` están en la raíz de la feature). No reorganices paquetes si no te lo piden.

## Convenciones de código

- **Inyección por constructor**, con campos `private final`. Se usan tanto constructores escritos a mano como `@RequiredArgsConstructor`; mantén el estilo del fichero que edites.
- **Logging** con `@Slf4j`. Mensajes en inglés con este formato:
  ```java
  log.info("METHOD methodName() - Description {}", arg);
  ```
- **DTOs**: `record`s dentro de `dto/`. Se mapean a mano con factorías estáticas `fromEntity(entity)` y `fromCollection(list)`. Los DTOs de entrada se llaman `*FormDto` o `*CreationDto`. No expongas entidades JPA en los controladores.
- **Entidades**: Lombok `@Getter`/`@Setter`, no `@Data`.
- **Respuestas**: la mayoría de controladores devuelven `ResponseEntity<T>`; algunos (como `ChatRestController`) devuelven el DTO directamente. Sigue el estilo del controlador que edites.
- **Errores**: lanza `EcatlimException(mensaje, HttpStatus)` o `ResourceNotFoundException`. `ExceptionControllerAdvice` los convierte en `EcatlimErrorResponse`. **Los mensajes para el usuario van en español.**
- **Validación**: `@Valid` en el `@RequestBody` y anotaciones de Jakarta Validation en los DTOs.
- **Usuario actual**: `SecurityContextHolder.getContext().getAuthentication().getName()` o `SecurityUtils.getLoggedUsername()`; devuelven el **email**. También puedes recibir `Principal` en el controlador. En WebSocket se lee de `simpSessionAttributes.get("username")`.
- **Autorización** a nivel de método: `@PreAuthorize("hasAuthority('ADMIN')")` / `hasAnyAuthority(...)`. Los roles están en `features/user/enums/Role.java`, y un usuario puede tener varios.
- La lógica de negocio va en los servicios. Los controladores solo delegan, registran el log y, si hace falta, publican eventos WebSocket.

## Seguridad

- API sin estado con JWT (`Authorization: Bearer <token>`), sin CSRF, y `@EnableMethodSecurity` activado.
- Endpoints públicos (`WebSecurityConfig`): `/auth/login`, `/password/**`, `/scout-group/all`, `/pending-user/request`, `/ws/**`. Todo lo demás requiere autenticación. Si añades un endpoint público, actualiza esta lista.
- Valida siempre en el servicio que el usuario tiene permiso sobre el recurso, por ejemplo que pertenece al chat o es el autor del mensaje. Que el usuario esté autenticado no basta.

## WebSocket / chat

- Endpoint STOMP: `/ws?token=<jwt>`. `JwtHandshakeInterceptor` valida el token. No se usa SockJS.
- El prefijo de aplicación es `/app` y los prefijos del broker son `/topic` y `/queue`.
- Destinos del chat:
  - Enviar: `/app/chat/{chatId}/send` con payload `{ message, clientId }`.
  - Recibir mensajes: `/topic/chat/{chatId}`.
  - Eventos: `/topic/chat/{chatId}/message-deleted`, publicado desde `ChatRestController` con `SimpMessagingTemplate`.
  - Errores al enviar: el servicio lanza `ChatException` (`features/chat/exception`, con `ChatErrorCode`) y `ChatWebsocketController` la envía a `/user/queue/errors` con `@MessageExceptionHandler` (`{chatId, code, message}`). En REST la misma excepción llega como `EcatlimException`. El `Principal` se fija en `WebSocketConfig` a partir del atributo `username`.
  - Notificación por usuario: `/user/queue/chat-notifications` (`ChatNotificationDto(chatId)`), enviada a los demás miembros al guardar un mensaje; el header la usa para el badge en cualquier página.
  - Chat nuevo: `/user/queue/new-chats` (`ChatDto`), enviado desde `ChatRestController.addChat` a los miembros distintos del creador para que les aparezca en la lista sin recargar.
  - Al salir de un chat se guarda un mensaje de sistema (`ChatMessageType.USER_LEFT`, ya leído) y se emite en `/topic/chat/{chatId}`.
- REST del chat (`/chat`): `GET /{id}/messages?page&size`, `GET /allMyChats`, `GET /unread-chats`, `POST /add` (multipart: parte JSON `chat` y `picture` opcional), `POST /{id}/picture` y `DELETE /{id}/picture` (foto del grupo; devuelven el `ChatDto` actualizado y lo emiten en `/topic/chat/{chatId}/updated`), `POST /{id}/mark-read`, `DELETE /{id}` (salir del chat; solo si era el último miembro se elimina el chat con sus mensajes), `DELETE /{id}/messages/{messageId}`. Todos comprueban que el usuario es miembro.

## Base de datos y migraciones

- Hibernate trabaja con `ddl-auto=validate`, así que **cualquier cambio en las entidades necesita una migración Flyway**.
- Nombre de los ficheros: `V<n>__descripcion_en_snake_case.sql` en `src/main/resources/db/migration/`. Los números son enteros consecutivos, sin ceros a la izquierda. Antes de crear una, mira cuál es la última versión.
- **No modifiques migraciones que ya se hayan aplicado**: provoca errores de checksum. Crea siempre una nueva.
- Si al fusionar ramas hay versiones duplicadas, renumera las de tu rama por encima de la última de `develop`.

## Tests

- Solo hay un `@SpringBootTest` (`contextLoads`), que necesita MySQL y las variables de entorno.
- Si añades tests, usa `spring-boot-starter-test` y `spring-security-test`, que ya están en el `pom.xml`.
- La CI no ejecuta los tests, así que ejecútalos en local con `./mvnw test` antes de dar algo por terminado.

## Git y CI

- GitFlow: `master`, `develop`, `test`, y ramas `feature/ECL-<n>`. Las PR van a `develop`.
- Commits en inglés, con el prefijo de la tarea Jira: `ECL-12 Added chat unread counts`.
- `.github/workflows/test_ecatlim-test-backend.yml` despliega en Azure Web App (`ecatlim-test-backend`) en cada push a `test`.

## Notas conocidas

- `core/configuration/WebSocketConfig.java` declara el paquete `org.scoutsdecanarias.ecatlim_backend.configuration`, que no coincide con su carpeta.
