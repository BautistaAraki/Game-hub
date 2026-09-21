import { useEffect, useMemo, useState } from "react";
import {
  GameResponse,
  getCatalog,
  getUserLibrary,
  UserGameResponse,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";

type LibraryPageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onOpenGame: (gameId: number) => void;
  onNavigate: (screen: AppScreen) => void;
};

type SortMode = "recent" | "name" | "status" | "playtime";

type LibraryGame = UserGameResponse & {
  game?: GameResponse;
};

const statusLabels: Record<UserGameResponse["status"], string> = {
  BACKLOG: "Backlog",
  PLAYING: "Jugando",
  COMPLETED: "Completado",
  ON_HOLD: "Pausado",
  DROPPED: "Abandonado"
};

function LibraryPage({
  activeScreen,
  user,
  onLogout,
  onOpenGame,
  onNavigate
}: LibraryPageProps) {
  const [catalog, setCatalog] = useState<GameResponse[]>([]);
  const [library, setLibrary] = useState<UserGameResponse[]>([]);
  const [query, setQuery] = useState("");
  const [sortMode, setSortMode] = useState<SortMode>("recent");
  const [statusFilter, setStatusFilter] = useState<UserGameResponse["status"] | "ALL">("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadLibrary() {
      setIsLoading(true);
      setError("");

      try {
        const [catalogResponse, libraryResponse] = await Promise.all([
          getCatalog(),
          getUserLibrary(user.id)
        ]);

        if (!isMounted) {
          return;
        }

        setCatalog(catalogResponse);
        setLibrary(libraryResponse);
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "No se pudo cargar la biblioteca"
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadLibrary();

    return () => {
      isMounted = false;
    };
  }, [user.id]);

  const gameById = useMemo(() => {
    return new Map(catalog.map((game) => [game.id, game]));
  }, [catalog]);

  const libraryGames = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return library
      .map<LibraryGame>((entry) => ({
        ...entry,
        game: gameById.get(entry.gameId)
      }))
      .filter((entry) => {
        const name = entry.game?.name ?? `Juego #${entry.gameId}`;
        const matchesQuery = !normalizedQuery
          || name.toLowerCase().includes(normalizedQuery);
        const matchesStatus = statusFilter === "ALL" || entry.status === statusFilter;

        return matchesQuery && matchesStatus;
      })
      .sort((left, right) => compareLibraryGames(left, right, sortMode));
  }, [gameById, library, query, sortMode, statusFilter]);

  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Navegacion principal">
        <div className="sidebar-brand">GAMEHUB</div>
        <nav>
          <button
            className={activeScreen === "home" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("home")}
            type="button"
          >
            Home
          </button>
          <button
            className={activeScreen === "library" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("library")}
            type="button"
          >
            Biblioteca
          </button>
          <button
            className={activeScreen === "search" ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate("search")}
            type="button"
          >
            Buscar
          </button>
          <button className="nav-item" type="button">Estadisticas</button>
          <button className="nav-item" type="button">Ajustes</button>
        </nav>
        <div className="sidebar-user">
          <span className="avatar">{user.username.slice(0, 1).toUpperCase()}</span>
          <div>
            <strong>{user.username}</strong>
            <button type="button" onClick={onLogout}>Cerrar sesion</button>
          </div>
        </div>
      </aside>

      <section className="library-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Tu coleccion</p>
            <h1>Biblioteca</h1>
          </div>
          <button className="primary-action compact-action" onClick={() => onNavigate("search")} type="button">
            Agregar juego
          </button>
        </header>

        <section className="library-toolbar" aria-label="Filtros de biblioteca">
          <label>
            Buscar en biblioteca
            <input
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Buscar por nombre"
              type="search"
              value={query}
            />
          </label>

          <label>
            Estado
            <select
              onChange={(event) =>
                setStatusFilter(event.target.value as UserGameResponse["status"] | "ALL")
              }
              value={statusFilter}
            >
              <option value="ALL">Todos</option>
              <option value="BACKLOG">Backlog</option>
              <option value="PLAYING">Jugando</option>
              <option value="COMPLETED">Completado</option>
              <option value="ON_HOLD">Pausado</option>
              <option value="DROPPED">Abandonado</option>
            </select>
          </label>

          <label>
            Ordenar
            <select
              onChange={(event) => setSortMode(event.target.value as SortMode)}
              value={sortMode}
            >
              <option value="recent">Mas recientes</option>
              <option value="name">Nombre</option>
              <option value="status">Estado</option>
              <option value="playtime">Horas jugadas</option>
            </select>
          </label>
        </section>

        {error && <p className="form-message error">{error}</p>}

        <section className="library-summary-row" aria-label="Resumen">
          <Metric label="Mostrando" value={libraryGames.length} />
          <Metric label="Total" value={library.length} />
          <Metric label="Favoritos" value={library.filter((entry) => entry.favorite).length} />
          <Metric
            label="Horas"
            value={Math.floor(
              library.reduce((total, entry) => total + entry.playtimeMinutes, 0) / 60
            )}
          />
        </section>

        {isLoading ? (
          <p className="muted">Cargando biblioteca...</p>
        ) : libraryGames.length > 0 ? (
          <section className="library-grid" aria-label="Juegos de biblioteca">
            {libraryGames.map((entry) => (
              <article
                className="library-card clickable-card"
                key={entry.id}
                onClick={() => entry.game && onOpenGame(entry.game.id)}
              >
                <GameArtwork game={entry.game} />
                <div className="library-card-body">
                  <div>
                    <h2>{entry.game?.name ?? `Juego #${entry.gameId}`}</h2>
                    <p>{statusLabels[entry.status]}</p>
                  </div>
                  <div className="library-card-meta">
                    <span>{Math.floor(entry.playtimeMinutes / 60)} h</span>
                    <span>{entry.rating ? `${entry.rating}/10` : "Sin rating"}</span>
                    {entry.favorite && <span>Favorito</span>}
                  </div>
                </div>
              </article>
            ))}
          </section>
        ) : (
          <EmptyState
            title="No hay juegos para mostrar"
            text="Agrega juegos o cambia los filtros para ver tu biblioteca."
          />
        )}
      </section>
    </main>
  );
}

function compareLibraryGames(left: LibraryGame, right: LibraryGame, sortMode: SortMode) {
  const leftName = left.game?.name ?? `Juego #${left.gameId}`;
  const rightName = right.game?.name ?? `Juego #${right.gameId}`;

  if (sortMode === "name") {
    return leftName.localeCompare(rightName);
  }

  if (sortMode === "status") {
    return statusLabels[left.status].localeCompare(statusLabels[right.status]);
  }

  if (sortMode === "playtime") {
    return right.playtimeMinutes - left.playtimeMinutes;
  }

  return right.id - left.id;
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function GameArtwork({ game }: { game?: GameResponse }) {
  const [imageFailed, setImageFailed] = useState(false);

  if (game?.imageUrl && !imageFailed) {
    return (
      <img
        alt={game.name}
        className="library-cover"
        onError={() => setImageFailed(true)}
        src={game.imageUrl}
      />
    );
  }

  return (
    <div className="library-cover fallback-art" aria-hidden="true">
      <span>{game?.name.slice(0, 2).toUpperCase() ?? "GH"}</span>
    </div>
  );
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}

export default LibraryPage;
