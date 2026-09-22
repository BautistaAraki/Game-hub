import { FormEvent, useState } from "react";
import { login, register, UserResponse } from "./api";
import GameDetailPage from "./GameDetailPage";
import HomePage from "./HomePage";
import LibraryPage from "./LibraryPage";
import { AppScreen } from "./navigation";
import ProfilePage from "./ProfilePage";
import SearchPage from "./SearchPage";
import SettingsPage from "./SettingsPage";
import StatsPage from "./StatsPage";

type AuthMode = "login" | "register";

type FormState = {
  username: string;
  email: string;
  password: string;
};

const initialForm: FormState = {
  username: "",
  email: "",
  password: ""
};

const SESSION_STORAGE_KEY = "gamehub.user";

function App() {
  const [mode, setMode] = useState<AuthMode>("login");
  const [form, setForm] = useState<FormState>(initialForm);
  const [user, setUser] = useState<UserResponse | null>(() => readStoredUser());
  const [screen, setScreen] = useState<AppScreen>("home");
  const [selectedGameId, setSelectedGameId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isLogin = mode === "login";

  function updateField(field: keyof FormState, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value
    }));
  }

  function switchMode(nextMode: AuthMode) {
    setMode(nextMode);
    setError("");
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const response = isLogin
        ? await login(form.email, form.password)
        : await register(form.username, form.email, form.password);

      setUser(response);
      storeUser(response);
      setScreen("home");
      setSelectedGameId(null);
      setForm(initialForm);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Error inesperado");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (user) {
    function openGameDetail(gameId: number) {
      setSelectedGameId(gameId);
      setScreen("detail");
    }

    function logout() {
      clearStoredUser();
      setUser(null);
      setSelectedGameId(null);
      setScreen("home");
    }

    if (screen === "search") {
      return (
        <SearchPage
          activeScreen={screen}
          onLogout={logout}
          onOpenGame={openGameDetail}
          onNavigate={setScreen}
          user={user}
        />
      );
    }

    if (screen === "library") {
      return (
        <LibraryPage
          activeScreen={screen}
          onLogout={logout}
          onOpenGame={openGameDetail}
          onNavigate={setScreen}
          user={user}
        />
      );
    }

    if (screen === "stats") {
      return (
        <StatsPage
          activeScreen={screen}
          onLogout={logout}
          onNavigate={setScreen}
          user={user}
        />
      );
    }

    if (screen === "profile") {
      return (
        <ProfilePage
          activeScreen={screen}
          onLogout={logout}
          onNavigate={setScreen}
          onOpenGame={openGameDetail}
          user={user}
        />
      );
    }

    if (screen === "settings") {
      return (
        <SettingsPage
          activeScreen={screen}
          onLogout={logout}
          onNavigate={setScreen}
          user={user}
        />
      );
    }

    if (screen === "detail") {
      return (
        <GameDetailPage
          activeScreen={screen}
          gameId={selectedGameId}
          onLogout={logout}
          onNavigate={setScreen}
          user={user}
        />
      );
    }

    return (
      <HomePage
        activeScreen={screen}
        onLogout={logout}
        onOpenGame={openGameDetail}
        onNavigate={setScreen}
        user={user}
      />
    );
  }

  return (
    <main className="auth-shell">
      <section className="brand-panel" aria-label="GameHub">
        <div className="brand-mark">GAMEHUB</div>
        <div className="brand-copy">
          <p className="eyebrow">Tu biblioteca personal</p>
          <h1>Tu biblioteca de juegos, organizada en un solo lugar.</h1>
          <p>
            Guarda tu progreso, separa tu catalogo global de tu biblioteca personal
            y prepara el camino para importar juegos desde Steam.
          </p>
        </div>
        <div className="cover-strip" aria-hidden="true">
          <span className="cover cover-one" />
          <span className="cover cover-two" />
          <span className="cover cover-three" />
          <span className="cover cover-four" />
        </div>
      </section>

      <section className="auth-panel" aria-label={isLogin ? "Iniciar sesion" : "Registrarse"}>
        <div className="auth-card">
          <div className="mode-switch" aria-label="Cambiar formulario">
            <button
              className={isLogin ? "active" : ""}
              type="button"
              onClick={() => switchMode("login")}
            >
              Iniciar sesion
            </button>
            <button
              className={!isLogin ? "active" : ""}
              type="button"
              onClick={() => switchMode("register")}
            >
              Registrarse
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <header>
              <h2>{isLogin ? "Iniciar sesion" : "Crear cuenta"}</h2>
              <p>
                {isLogin
                  ? "Entra para continuar con tu biblioteca."
                  : "Crea tu usuario para empezar a guardar juegos."}
              </p>
            </header>

            {!isLogin && (
              <label>
                Usuario
                <input
                  autoComplete="username"
                  minLength={2}
                  onChange={(event) => updateField("username", event.target.value)}
                  placeholder="Bautista"
                  required
                  type="text"
                  value={form.username}
                />
              </label>
            )}

            <label>
              Email
              <input
                autoComplete="email"
                onChange={(event) => updateField("email", event.target.value)}
                placeholder="tu@email.com"
                required
                type="email"
                value={form.email}
              />
            </label>

            <label>
              Contrasena
              <input
                autoComplete={isLogin ? "current-password" : "new-password"}
                minLength={8}
                onChange={(event) => updateField("password", event.target.value)}
                placeholder="Minimo 8 caracteres"
                required
                type="password"
                value={form.password}
              />
            </label>

            {error && <p className="form-message error">{error}</p>}

            <button className="submit-button" disabled={isSubmitting} type="submit">
              {isSubmitting ? "Procesando..." : isLogin ? "Entrar" : "Crear cuenta"}
            </button>
          </form>
        </div>
      </section>
    </main>
  );
}

function readStoredUser() {
  const storedValue = window.localStorage.getItem(SESSION_STORAGE_KEY);

  if (!storedValue) {
    return null;
  }

  try {
    const parsedValue = JSON.parse(storedValue) as UserResponse;

    if (
      typeof parsedValue.id === "number"
      && typeof parsedValue.username === "string"
      && typeof parsedValue.email === "string"
    ) {
      return parsedValue;
    }
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
  }

  return null;
}

function storeUser(user: UserResponse) {
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
}

function clearStoredUser() {
  window.localStorage.removeItem(SESSION_STORAGE_KEY);
}

export default App;
