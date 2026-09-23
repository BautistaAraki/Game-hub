import { FormEvent, useState } from "react";
import { linkSteamAccount, UserExternalAccountResponse, UserResponse } from "./api";
import { AppScreen } from "./navigation";
import Sidebar from "./Sidebar";

type SettingsPageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

function SettingsPage({ activeScreen, user, onLogout, onNavigate }: SettingsPageProps) {
  const [displayName, setDisplayName] = useState(user.username);
  const [publicUsername, setPublicUsername] = useState(toPublicUsername(user.username));
  const [bio, setBio] = useState("Estudiante de sistemas y fan de los videojuegos.");
  const [friendActivity, setFriendActivity] = useState(true);
  const [reminders, setReminders] = useState(true);
  const [recommendations, setRecommendations] = useState(false);
  const [message, setMessage] = useState("");
  const [steamId, setSteamId] = useState("");
  const [steamAccount, setSteamAccount] = useState<UserExternalAccountResponse | null>(null);
  const [steamMessage, setSteamMessage] = useState("");
  const [steamError, setSteamError] = useState("");
  const [isLinkingSteam, setIsLinkingSteam] = useState(false);

  function saveChanges() {
    setMessage("Cambios guardados localmente. La API de perfil queda para un bloque futuro.");
  }

  async function handleLinkSteamAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSteamMessage("");
    setSteamError("");

    if (!steamId.trim()) {
      setSteamError("Pegá tu SteamID64 para conectar la cuenta.");
      return;
    }

    setIsLinkingSteam(true);

    try {
      const response = await linkSteamAccount(steamId);
      setSteamAccount(response);
      setSteamId(response.externalUserId);
      setSteamMessage("Cuenta Steam conectada. En el próximo bloque vamos a importar la biblioteca.");
    } catch (requestError) {
      setSteamError(
        requestError instanceof Error
          ? requestError.message
          : "No se pudo conectar la cuenta Steam."
      );
    } finally {
      setIsLinkingSteam(false);
    }
  }

  return (
    <main className="app-shell">
      <Sidebar
        activeScreen={activeScreen}
        onLogout={onLogout}
        onNavigate={onNavigate}
        user={user}
      />

      <section className="settings-page">
        <header className="settings-header">
          <p className="section-kicker">Cuenta</p>
          <h1>Ajustes</h1>
          <p>Personaliza tu cuenta, privacidad y preferencias.</p>
        </header>

        <div className="settings-content">
          {message && <p className="form-message success">{message}</p>}

          <section className="settings-card settings-profile-card">
            <div className="settings-card-header">
              <div>
                <h2>Perfil publico</h2>
                <p>Esta informacion aparece en tu perfil y actividad.</p>
              </div>
            </div>

            <div className="settings-avatar-row">
              <span className="settings-avatar">{user.username.slice(0, 2).toUpperCase()}</span>
              <div>
                <button className="secondary-action" type="button">Cambiar foto</button>
                <p>PNG o JPG. Maximo 5 MB.</p>
              </div>
            </div>

            <div className="settings-form-grid">
              <label>
                Nombre visible
                <input
                  onChange={(event) => setDisplayName(event.target.value)}
                  type="text"
                  value={displayName}
                />
              </label>
              <label>
                Usuario
                <input
                  onChange={(event) => setPublicUsername(event.target.value)}
                  type="text"
                  value={publicUsername}
                />
              </label>
              <label className="settings-full-field">
                Biografia
                <input
                  onChange={(event) => setBio(event.target.value)}
                  type="text"
                  value={bio}
                />
              </label>
            </div>

            <div className="settings-actions">
              <button className="primary-action compact-action" onClick={saveChanges} type="button">
                Guardar cambios
              </button>
            </div>
          </section>

          <section className="settings-card">
            <div className="settings-card-header">
              <div>
                <h2>Preferencias</h2>
                <p>Configuracion general de la app.</p>
              </div>
            </div>

            <div className="settings-stack">
              <label>
                Idioma
                <select defaultValue="es">
                  <option value="es">Espanol</option>
                  <option value="en">English</option>
                </select>
              </label>
              <label>
                Tema
                <select defaultValue="dark">
                  <option value="dark">Oscuro</option>
                  <option value="system">Sistema</option>
                </select>
              </label>
              <label>
                Formato de hora
                <select defaultValue="24h">
                  <option value="24h">24 horas</option>
                  <option value="12h">12 horas</option>
                </select>
              </label>
            </div>
          </section>

          <section className="settings-card settings-integrations-card">
            <div className="settings-card-header">
              <div>
                <h2>Integraciones</h2>
                <p>Conecta servicios externos para traer tu biblioteca.</p>
              </div>
            </div>

            <form className="settings-steam-form" onSubmit={handleLinkSteamAccount}>
              <label>
                SteamID64
                <input
                  inputMode="numeric"
                  onChange={(event) => setSteamId(event.target.value)}
                  placeholder="7656119..."
                  type="text"
                  value={steamId}
                />
              </label>
              <button className="primary-action compact-action" disabled={isLinkingSteam} type="submit">
                {isLinkingSteam ? "Conectando..." : "Conectar Steam"}
              </button>
            </form>

            {steamMessage && <p className="form-message success">{steamMessage}</p>}
            {steamError && <p className="form-message error">{steamError}</p>}

            {steamAccount && (
              <div className="integration-status">
                <span>Conectado</span>
                <strong>{steamAccount.externalUserId}</strong>
              </div>
            )}

            <div className="settings-help-box">
              <strong>Como encontrar tu SteamID64</strong>
              <p>
                Abrí tu perfil de Steam en el navegador, copiá el número largo de la URL
                y pegalo acá. Ejemplo: /profiles/76561198738369808/.
              </p>
              <p>
                Si tu perfil está privado, GameHub puede conectar la cuenta, pero Steam puede
                no devolver juegos al importar.
              </p>
            </div>
          </section>

          <section className="settings-card">
            <div className="settings-card-header">
              <div>
                <h2>Notificaciones</h2>
                <p>Elegi que novedades queres recibir.</p>
              </div>
            </div>

            <div className="settings-toggle-list">
              <ToggleRow
                checked={friendActivity}
                description="Cuando alguien te sigue o comenta."
                label="Actividad de amigos"
                onChange={setFriendActivity}
              />
              <ToggleRow
                checked={reminders}
                description="Juegos pendientes y objetivos."
                label="Recordatorios"
                onChange={setReminders}
              />
              <ToggleRow
                checked={recommendations}
                description="Sugerencias segun tus gustos."
                label="Recomendaciones"
                onChange={setRecommendations}
              />
            </div>
          </section>

          <section className="settings-card">
            <div className="settings-card-header">
              <div>
                <h2>Seguridad</h2>
                <p>Acciones sensibles de la cuenta.</p>
              </div>
            </div>

            <div className="settings-security-actions">
              <button className="secondary-action" type="button">Cambiar contrasena</button>
              <button className="danger-button" type="button">Eliminar cuenta</button>
            </div>
          </section>
        </div>
      </section>
    </main>
  );
}

function ToggleRow({
  checked,
  description,
  label,
  onChange
}: {
  checked: boolean;
  description: string;
  label: string;
  onChange: (checked: boolean) => void;
}) {
  return (
    <div className="settings-toggle-row">
      <div>
        <strong>{label}</strong>
        <span>{description}</span>
      </div>
      <button
        aria-pressed={checked}
        className={checked ? "toggle-switch active" : "toggle-switch"}
        onClick={() => onChange(!checked)}
        type="button"
      >
        <span />
      </button>
    </div>
  );
}

function toPublicUsername(username: string) {
  return username.trim().toLowerCase().replace(/\s+/g, "_");
}

export default SettingsPage;
