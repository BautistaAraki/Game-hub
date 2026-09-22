import { useEffect, useMemo, useState } from "react";
import {
  GameResponse,
  getCatalog,
  getLibraryStats,
  getUserLibrary,
  LibraryStatsResponse,
  UserGameResponse,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";
import Sidebar from "./Sidebar";

type HomePageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onOpenGame: (gameId: number) => void;
  onNavigate: (screen: AppScreen) => void;
};

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

function HomePage({ activeScreen, user, onLogout, onOpenGame, onNavigate }: HomePageProps) {
  const [catalog, setCatalog] = useState<GameResponse[]>([]);
  const [library, setLibrary] = useState<UserGameResponse[]>([]);
  const [stats, setStats] = useState<LibraryStatsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadHomeData() {
      setIsLoading(true);
      setError("");

      try {
        const [catalogResponse, libraryResponse, statsResponse] = await Promise.all([
          getCatalog(),
          getUserLibrary(),
          getLibraryStats()
        ]);

        if (!isMounted) {
          return;
        }

        setCatalog(catalogResponse);
        setLibrary(libraryResponse);
        setStats(statsResponse);
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "No se pudo cargar el Home"
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadHomeData();

    return () => {
      isMounted = false;
    };
  }, [user.id]);

  const gameById = useMemo(() => {
    return new Map(catalog.map((game) => [game.id, game]));
  }, [catalog]);

  const libraryGames: LibraryGame[] = library.map((entry) => ({
    ...entry,
    game: gameById.get(entry.gameId)
  }));

  const featuredGame = libraryGames[0]?.game ?? catalog[0];
  const recentGames = libraryGames.slice(0, 8);
  const catalogPreview = catalog.slice(0, 6);

  return (
    <main className="app-shell">
      <Sidebar
        activeScreen={activeScreen}
        onLogout={onLogout}
        onNavigate={onNavigate}
        user={user}
      />

      <section className="home-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Bienvenido, {user.username}</p>
            <h1>Home</h1>
          </div>
          <label className="search-box">
            <span>Buscar</span>
            <input
              onFocus={() => onNavigate("search")}
              placeholder="Buscar juegos"
              type="search"
            />
          </label>
        </header>

        {error && <p className="form-message error">{error}</p>}

        <section className="hero-band" aria-label="Juego destacado">
          <div className="hero-content">
            <p className="section-kicker">Destacado</p>
            <h2>{featuredGame?.name ?? "Tu proximo juego favorito"}</h2>
            <p>
              Organiza tu biblioteca personal, marca favoritos y segui el estado
              de cada juego desde un solo lugar.
            </p>
            <button
              className="primary-action"
              disabled={!featuredGame}
              onClick={() => featuredGame && onOpenGame(featuredGame.id)}
              type="button"
            >
              Ver detalle
            </button>
          </div>
          <GameArtwork game={featuredGame} size="large" />
        </section>

        <section className="metric-row" aria-label="Resumen de biblioteca">
          <Metric label="Juegos" value={stats?.totalGames ?? 0} />
          <Metric label="Completados" value={stats?.completedGames ?? 0} />
          <Metric label="Favoritos" value={stats?.favoriteGames ?? 0} />
          <Metric label="Horas" value={stats?.totalPlaytimeHours ?? 0} />
        </section>

        <section className="content-grid">
          <div className="home-section">
            <div className="section-heading">
              <h2>Tu biblioteca</h2>
              <button onClick={() => onNavigate("library")} type="button">Ver todo</button>
            </div>

            {isLoading ? (
              <p className="muted">Cargando juegos...</p>
            ) : recentGames.length > 0 ? (
              <div className="game-grid">
                {recentGames.map((entry) => (
                  <article
                    className="game-card clickable-card"
                    key={entry.id}
                    onClick={() => entry.game && onOpenGame(entry.game.id)}
                  >
                    <GameArtwork game={entry.game} />
                    <div>
                      <h3>{entry.game?.name ?? `Juego #${entry.gameId}`}</h3>
                      <p>{statusLabels[entry.status]}</p>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Todavia no hay juegos"
                text="Cuando agregues juegos, van a aparecer aca con su estado y progreso."
              />
            )}
          </div>

          <div className="home-section">
            <div className="section-heading">
              <h2>Catalogo</h2>
              <button onClick={() => onNavigate("search")} type="button">Buscar</button>
            </div>

            {catalogPreview.length > 0 ? (
              <div className="catalog-list">
                {catalogPreview.map((game) => (
                  <article
                    className="catalog-row clickable-card"
                    key={game.id}
                    onClick={() => onOpenGame(game.id)}
                  >
                    <GameArtwork game={game} size="small" />
                    <div>
                      <h3>{game.name}</h3>
                      <p>Disponible para agregar</p>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Catalogo vacio"
                text="Cuando cargues juegos globales, van a aparecer aca."
              />
            )}
          </div>
        </section>
      </section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function GameArtwork({
  game,
  size = "default"
}: {
  game?: GameResponse;
  size?: "small" | "default" | "large";
}) {
  const [imageFailed, setImageFailed] = useState(false);

  if (game?.imageUrl && !imageFailed) {
    return (
      <img
        alt={game.name}
        className={`game-art game-art-${size}`}
        onError={() => setImageFailed(true)}
        src={game.imageUrl}
      />
    );
  }

  return (
    <div className={`game-art game-art-${size} fallback-art`} aria-hidden="true">
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

export default HomePage;
