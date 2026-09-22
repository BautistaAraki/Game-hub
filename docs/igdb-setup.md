# Configurar IGDB en desarrollo

La pantalla Buscar consulta el catalogo local y `GET /api/external-games/search?query=minecraft` de forma independiente. Si IGDB no esta configurado o falla, el catalogo local sigue disponible. Cuando el usuario agrega un resultado externo, el backend crea o reutiliza el `Game` global y despues crea el `UserGame` de su biblioteca personal.

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
Invoke-RestMethod 'http://localhost:8080/api/external-games/search?query=minecraft'
```

El backend obtiene el token mediante `client_credentials`, lo guarda en memoria y lo renueva antes de vencer. Los errores externos se convierten en respuestas 502 sin incluir secretos.

El endpoint RAWG anterior se conserva por compatibilidad, pero la pantalla ahora usa el flujo externo generico respaldado por IGDB. Los identificadores externos se guardan como `ExternalGameId` con plataforma `IGDB` para evitar duplicados.

Las pruebas automatizadas simulan la integracion externa; la comprobacion real requiere configurar tus credenciales.
