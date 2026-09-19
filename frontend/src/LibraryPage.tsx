import { useEffect, useMemo, useState } from "react";
import { GameResponse, getCatalog, getUserLibrary, UserGameResponse, UserResponse } from "./api";
import { AppScreen } from "./navigation";
import { LibrarySort, selectLibrary, StatusFilter, statusLabels } from "./library";
import Sidebar from "./Sidebar";

type LibraryPageProps = {
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

export default function LibraryPage({ user, onLogout, onNavigate }: LibraryPageProps) {
  const [catalog, setCatalog] = useState<GameResponse[]>([]);
  const [library, setLibrary] = useState<UserGameResponse[]>([]);
  const [status, setStatus] = useState<StatusFilter>("ALL");
  const [sort, setSort] = useState<LibrarySort>("name");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let active = true;
    setIsLoading(true);
    setError("");
    async function loadLibrary() {
      try {
        const [games, entries] = await Promise.all([getCatalog(), getUserLibrary(user.id)]);
        if (active) {
          setCatalog(games);
          setLibrary(entries);
        }
      } catch (cause) {
        if (active) setError(cause instanceof Error ? cause.message : "No se pudo cargar la biblioteca.");
      } finally {
        if (active) setIsLoading(false);
      }
    }
    void loadLibrary();
    return () => { active = false; };
  }, [user.id, attempt]);

  const entries = useMemo(() => selectLibrary(library, catalog, status, sort), [library, catalog, status, sort]);

  return (
    <main className="app-shell">
      <Sidebar activeScreen="library" user={user} onLogout={onLogout} onNavigate={onNavigate} />
      <section className="home-page library-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Tu colección personal</p>
            <h1>Biblioteca</h1>
          </div>
          <button className="primary-action" type="button" onClick={() => onNavigate("search")}>Agregar juego</button>
        </header>

        <div className="library-controls">
          <label>Estado
            <select value={status} onChange={(event) => setStatus(event.target.value as StatusFilter)}>
              <option value="ALL">Todos los estados</option>
              {Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>
          <label>Ordenar por
            <select value={sort} onChange={(event) => setSort(event.target.value as LibrarySort)}>
              <option value="name">Nombre (A–Z)</option>
              <option value="rating">Rating (mayor primero)</option>
              <option value="hours">Horas (mayor primero)</option>
              <option value="favorites">Favoritos primero</option>
            </select>
          </label>
        </div>

        {isLoading ? <p className="muted" role="status">Cargando biblioteca...</p> : error ? (
          <div className="library-error">
            <p className="form-message error" role="alert">{error}</p>
            <button className="primary-action" type="button" onClick={() => setAttempt((value) => value + 1)}>Reintentar</button>
          </div>
        ) : library.length === 0 ? (
          <div className="empty-state">
            <h2>Tu biblioteca está vacía</h2>
            <p>Buscá un juego y agregalo para empezar tu colección.</p>
            <button className="primary-action" type="button" onClick={() => onNavigate("search")}>Buscar juegos</button>
          </div>
        ) : (
          <section className="home-section" aria-label="Juegos de tu biblioteca">
            <p className="muted" role="status">Mostrando {entries.length} de {library.length} juegos</p>
            {entries.length === 0 ? (
              <div className="empty-state">
                <h2>No hay juegos con este estado</h2>
                <p>Elegí otro estado para ver el resto de tu biblioteca.</p>
                <button className="primary-action" type="button" onClick={() => setStatus("ALL")}>Mostrar todos</button>
              </div>
            ) : (
              <div className="library-grid">
                {entries.map((entry) => (
                  <article className="game-card library-card" key={entry.id}>
                    <LibraryArtwork key={entry.game?.imageUrl ?? "missing"} name={entry.name} imageUrl={entry.game?.imageUrl} />
                    <div className="library-card-content">
                      <h2>{entry.name}</h2>
                      <p className="library-status">{statusLabels[entry.status]}</p>
                      <p>{entry.rating === null ? "Sin rating" : `Rating: ${entry.rating}`}</p>
                      <p>{new Intl.NumberFormat("es-AR", { maximumFractionDigits: 1 }).format(entry.playtimeMinutes / 60)} h jugadas</p>
                      {entry.favorite && <span className="library-favorite">★ Favorito</span>}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        )}
      </section>
    </main>
  );
}

function LibraryArtwork({ name, imageUrl }: { name: string; imageUrl?: string | null }) {
  const [failed, setFailed] = useState(false);
  return imageUrl && !failed ? (
    <img className="game-art" alt={name} src={imageUrl} loading="lazy" onError={() => setFailed(true)} />
  ) : (
    <div className="game-art fallback-art" aria-hidden="true"><span>{name.slice(0, 2).toUpperCase()}</span></div>
  );
}
