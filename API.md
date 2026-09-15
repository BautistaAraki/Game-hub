# GameHub API

Base URL local:

```text
http://localhost:8080
```

## Variables de entorno

La aplicacion necesita estas variables cuando corre contra MySQL y/o Steam:

```powershell
$env:DB_PASSWORD="..."
$env:STEAM_API_KEY="..."
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
  "id": 1,
  "username": "Bautista",
  "email": "bauti@example.com"
}
```

Nota: este login todavia no devuelve JWT. Eso queda para el bloque de autenticacion con Spring Security.

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
GET /api/library/users/{userId}
```

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
```

```json
{
  "userId": 1,
  "gameId": 1
}
```

Si el usuario ya tiene ese juego, devuelve `409`.

### Actualizar rating

```http
PATCH /api/library/{userGameId}/rating?rating=8
```

El rating valido va de `1` a `10`.

### Actualizar estado

```http
PATCH /api/library/{userGameId}/status?status=PLAYING
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
```

### Quitar de favoritos

```http
PATCH /api/library/{userGameId}/unfavorite
```

### Eliminar juego de biblioteca

```http
DELETE /api/library/{userGameId}
```

## Estadisticas

### Obtener estadisticas de biblioteca

```http
GET /api/library/users/{userId}/stats
```

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
POST /api/users/{userId}/external-accounts/steam
Content-Type: application/json
```

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
POST /api/steam/users/{userId}/import-library
```

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
