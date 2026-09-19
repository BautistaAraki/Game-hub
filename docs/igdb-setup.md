# Configurar IGDB en desarrollo

La pantalla Buscar consulta el catálogo local y `GET /api/igdb/games/search?query=minecraft` de forma independiente. Si IGDB no está configurado o falla, el catálogo local sigue disponible. Buscar no crea Game ni UserGame; agregar resultados externos queda pendiente, como en la integración anterior.

## Obtener credenciales

1. Iniciar sesión con una cuenta de Twitch y activar la autenticación en dos pasos.
2. Registrar GameHub en https://dev.twitch.tv/console/apps .
3. Elegir tipo de cliente **Confidential**. Si pide una URL de redirección, usar `http://localhost`; este flujo no la utiliza.
4. Copiar el **Client ID** y generar un **Client Secret**.

Documentación oficial: https://api-docs.igdb.com/#account-creation . IGDB indica que el uso no comercial es gratuito; para uso comercial se deben revisar sus condiciones.

## Iniciar Spring desde PowerShell

Detener Spring con Ctrl+C. En la misma terminal que ya tiene `DB_PASSWORD`:

```powershell
cd D:\gamehub\gamehub
$env:IGDB_CLIENT_ID = Read-Host 'Client ID de Twitch'
$igdbSecret = Read-Host 'Client Secret de Twitch' -AsSecureString
$env:IGDB_CLIENT_SECRET = [System.Net.NetworkCredential]::new('', $igdbSecret).Password
Remove-Variable igdbSecret
.\mvnw.cmd spring-boot:run
```

Los comandos funcionan en Windows PowerShell y PowerShell 7. Las variables duran en esa terminal. No colocar estas credenciales en React, variables `VITE_*`, archivos versionados ni mensajes de chat. No se necesita `RAWG_API_KEY` para esta búsqueda.

## Comprobar

Con Spring iniciado, buscar Minecraft desde la pantalla Buscar, o ejecutar en otra terminal:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/igdb/games/search?query=minecraft'
```

El backend obtiene el token mediante `client_credentials`, lo guarda en memoria y lo renueva antes de vencer. Ante un 401 lo invalida y reintenta una sola vez. Las llamadas tienen límites de tiempo y los errores externos se convierten en respuestas 502 sin incluir secretos. El rating externo de IGDB se muestra sobre 100 y no modifica el rating personal.

El endpoint RAWG anterior se conserva por compatibilidad, pero la pantalla ahora usa IGDB. No se modifican tablas ni se guardan identificadores IGDB en esta etapa.

Las pruebas automatizadas simulan Twitch e IGDB; la comprobación real requiere configurar tus credenciales.
