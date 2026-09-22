# GameHub API

Base URL local:

```text
http://localhost:8080
```

## Variables de entorno

La aplicacion necesita estas variables cuando corre contra MySQL, Steam, RAWG y/o IGDB:

```powershell
$env:DB_PASSWORD="..."
$env:STEAM_API_KEY="..."
$env:RAWG_API_KEY="..."
$env:IGDB_CLIENT_ID="..."
$env:IGDB_CLIENT_SECRET="..."
```

No guardar estos valores en Git.

## Modelo mental

GameHub separa dos conceptos importantes:

- `Game`: juego global del catalogo. Representa el juego como entidad compartida.
- `UserGame`: juego dentro de la biblioteca personal de un usuario. Aca viven datos como `rating`, `status`, `favorite` y `playtimeMinutes`.

Esta separacion es clave para el frontend: la pantalla de busqueda/catalogo trabaja principalmente con `Game`; la biblioteca, estadisticas y progreso personal trabajan con `UserGame`.

## Errores

Los errores devuelven una estructura consistente:

```json
{
  "timestamp": "2026-09-15T19:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El rating debe estar entre 1 y 10",
  "path": "/api/library/1/rating"
}
```

Codigos esperados:

- `400`: entrada invalida del cliente.
- `401`: credenciales invalidas.
- `404`: recurso inexistente.
- `409`: conflicto, por ejemplo juego duplicado o cuenta externa ya vinculada.
- `502`: error al hablar con un proveedor externo como Steam.

## Usuarios

### Registrar usuario

```http
POST /api/users
Content-Type: application/json
```

```json
{
  "username": "Bautista",
  "email": "bauti@example.com",
  "password": "Password123"
}
```

Respuesta:

```json
{
  "id": 1,
  "username": "Bautista",
  "email": "bauti@example.com"
}
```

La contrasena no debe aparecer en la respuesta.

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "bauti@example.com",
  "password": "Password123"
}
```

Respuesta:

```json
{
  "token": "jwt...",
  "user": {
    "id": 1,
    "username": "Bautista",
    "email": "bauti@example.com"
  }
}
```

El frontend debe enviar el token en los endpoints protegidos:

```http
Authorization: Bearer jwt...
```

`POST /api/users` y `POST /api/auth/login` son publicos. El resto de endpoints bajo `/api/**` requiere token.

## Catalogo

### Listar catalogo global

```http
GET /api/games
```

Respuesta:

```json
[
  {
    "id": 1,
    "name": "God of War",
    "imageUrl": "https://..."
  }
]
```

### Buscar juegos

```http
GET /api/games/search?query=god
```

Si `query` esta vacio o no se envia, devuelve el catalogo completo.

## Biblioteca

### Listar biblioteca de un usuario

```http
GET /api/library/me
Authorization: Bearer jwt...
```

Este es el endpoint recomendado para el frontend. El usuario se toma del token.

Endpoint compatible:

```http
GET /api/library/users/{userId}
```

Si `{userId}` no coincide con el usuario del token, devuelve `403`.

Respuesta:

```json
[
  {
    "id": 10,
    "userId": 1,
    "gameId": 1,
    "rating": 8,
    "playtimeMinutes": 120,
    "favorite": true,
    "status": "PLAYING"
  }
]
```

### Agregar juego a biblioteca

```http
POST /api/library
Content-Type: application/json
Authorization: Bearer jwt...
```

```json
{
  "gameId": 1
}
```

El usuario se toma del token. Si un cliente viejo envia `userId`, el backend lo ignora.

Si el usuario ya tiene ese juego, devuelve `409`.

### Actualizar rating

```http
PATCH /api/library/{userGameId}/rating?rating=8
Authorization: Bearer jwt...
```

El rating valido va de `1` a `10`.
Si el `UserGame` pertenece a otro usuario, devuelve `403`.

### Actualizar estado

```http
PATCH /api/library/{userGameId}/status?status=PLAYING
Authorization: Bearer jwt...
```

Estados validos:

- `BACKLOG`
- `PLAYING`
- `COMPLETED`
- `ON_HOLD`
- `DROPPED`

### Marcar como favorito

```http
PATCH /api/library/{userGameId}/favorite
Authorization: Bearer jwt...
```

### Quitar de favoritos

```http
PATCH /api/library/{userGameId}/unfavorite
Authorization: Bearer jwt...
```

### Eliminar juego de biblioteca

```http
DELETE /api/library/{userGameId}
Authorization: Bearer jwt...
```

## Estadisticas

### Obtener estadisticas de biblioteca

```http
GET /api/library/me/stats
Authorization: Bearer jwt...
```

Endpoint compatible:

```http
GET /api/library/users/{userId}/stats
```

Si `{userId}` no coincide con el usuario del token, devuelve `403`.

Respuesta:

```json
{
  "userId": 1,
  "totalGames": 10,
  "completedGames": 3,
  "backlogGames": 4,
  "playingGames": 2,
  "onHoldGames": 1,
  "droppedGames": 0,
  "favoriteGames": 5,
  "totalPlaytimeMinutes": 1200,
  "totalPlaytimeHours": 20
}
```

## Cuentas externas

### Vincular cuenta de Steam

```http
POST /api/me/external-accounts/steam
Content-Type: application/json
Authorization: Bearer jwt...
```

Endpoint compatible:

```http
POST /api/users/{userId}/external-accounts/steam
Content-Type: application/json
Authorization: Bearer jwt...
```

Si `{userId}` no coincide con el usuario del token, devuelve `403`.

```json
{
  "steamId": "76561198738369808"
}
```

Respuesta:

```json
{
  "id": 1,
  "userId": 1,
  "platform": "STEAM",
  "externalUserId": "76561198738369808"
}
```

Un usuario no puede tener dos cuentas Steam vinculadas al mismo tiempo. Si intenta repetirlo, devuelve `409`.

## Steam

### Consultar juegos de una cuenta Steam

```http
GET /api/steam/users/{steamId}/games
```

Requiere `STEAM_API_KEY`.

### Importar biblioteca de Steam

```http
POST /api/steam/me/import-library
Authorization: Bearer jwt...
```

Endpoint compatible:

```http
POST /api/steam/users/{userId}/import-library
Authorization: Bearer jwt...
```

Si `{userId}` no coincide con el usuario del token, devuelve `403`.

Requiere:

- usuario existente;
- cuenta Steam vinculada;
- `STEAM_API_KEY` configurada;
- perfil/biblioteca Steam accesible por la Steam Web API.

Respuesta:

```json
{
  "userId": 1,
  "steamGamesFound": 20,
  "gamesCreated": 5,
  "gamesMatched": 15,
  "libraryEntriesCreated": 18,
  "libraryEntriesSkipped": 2
}
```

Steam se integra por una capa externa aislada. La biblioteca personal no debe llamar directamente a la Steam Web API.

## RAWG

RAWG se usa para buscar juegos cuando el usuario no conecta Steam o quiere agregar un juego manualmente desde una base externa.

### Buscar juegos en RAWG

```http
GET /api/rawg/games/search?query=minecraft
```

Requiere `RAWG_API_KEY`.

Respuesta:

```json
[
  {
    "rawgId": 58751,
    "name": "Minecraft",
    "imageUrl": "https://...",
    "released": "2011-11-18",
    "rating": 4.4
  }
]
```

Este endpoint solo busca candidatos. No crea juegos en `Game` y no modifica la biblioteca del usuario.

Flujo recomendado para el frontend:

```text
Usuario busca un juego
GameHub consulta RAWG
Usuario elige el resultado correcto
GameHub agrega ese juego a la biblioteca
```

## Checklist manual antes del frontend

Con la app corriendo localmente, validar:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/games"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/games/search?query=god"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/library/users/1"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/library/users/1/stats"
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/library/1/rating?rating=8"
```

Validar tambien errores:

```powershell
Invoke-WebRequest -Method Patch -Uri "http://localhost:8080/api/library/1/rating?rating=15"
Invoke-WebRequest -Method Patch -Uri "http://localhost:8080/api/library/1/status?status=INVALIDO"
Invoke-WebRequest -Method Post -Uri "http://localhost:8080/api/library" -ContentType "application/json" -Body '{"userId":1,"gameId":1}'
```

El caso `rating=15` debe devolver `400` y no debe modificar el rating anterior.

## Busqueda externa de videojuegos

`GET /api/external-games/search?query=minecraft`

Es el flujo usado por la pantalla Buscar para consultar un proveedor externo. Actualmente el proveedor configurado es IGDB. Requiere `IGDB_CLIENT_ID` y `IGDB_CLIENT_SECRET` en el backend. Configuracion: [guia IGDB](docs/igdb-setup.md).

Respuesta 200:

```json
[
  {
    "source": "IGDB",
    "externalId": "121",
    "title": "Minecraft",
    "description": "Juego de construccion y supervivencia.",
    "imageUrl": null,
    "releaseDate": "2011-11-18",
    "platforms": ["PC", "Xbox One"]
  }
]
```

`imageUrl`, `description`, `releaseDate` y `platforms` pueden venir vacios o null segun el proveedor. Una busqueda sin coincidencias devuelve `[]`. Query obligatorio: entrada invalida devuelve 400. Credenciales ausentes, errores de Twitch/IGDB o limites del proveedor devuelven 502 con `ApiErrorResponse`.

### Agregar resultado externo a biblioteca

```http
POST /api/external-games/library
Content-Type: application/json
Authorization: Bearer jwt...
```

```json
{
  "source": "IGDB",
  "externalId": "121",
  "title": "Minecraft",
  "imageUrl": "https://...",
  "description": "Juego de construccion y supervivencia.",
  "releaseDate": "2011-11-18",
  "platforms": ["PC", "Xbox One"]
}
```

Este endpoint crea o reutiliza el `Game` global, vincula `source + externalId` mediante `ExternalGameId` para evitar duplicados y crea el `UserGame` en la biblioteca del usuario autenticado. Si un cliente viejo envia `userId`, el backend lo ignora. El endpoint RAWG anterior sigue disponible por compatibilidad.
