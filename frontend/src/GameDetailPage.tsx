import { useEffect, useMemo, useState } from "react";
import {
  GameResponse,
  getCatalog,
  getUserLibrary,
  UserGameResponse,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";

type GameDetailPageProps = {
  activeScreen: AppScreen;
  gameId: number | null;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

const statusLabels: Record<UserGameResponse["status"], string> = {
  BACKLOG: "Backlog",
  PLAYING: "Jugando",
  COMPLETED: "Completado",
  ON_HOLD: "Pausado",
  DROPPED: "Abandonado"
};

function GameDetailPage({
  activeScreen,
  gameId,
  user,
  onLogout,
  onNavigate
}: GameDetailPageProps) {
  const [catalog, setCatalog] = useState<GameResponse[]>([]);
  const [library, setLibrary] = useState<UserGameResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadDetailData() {
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
              : "No se pudo cargar el detalle del juego"
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadDetailData();

    return () => {
      isMounted = false;
    };
  }, [user.id]);

  const selectedGame = useMemo(() => {
    return catalog.find((game) => game.id === gameId) ?? null;
  }, [catalog, gameId]);

  const libraryEntry = useMemo(() => {
    return library.find((entry) => entry.gameId === gameId) ?? null;
  }, [library, gameId]);

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
          <button className="nav-item" type="button">Biblioteca</button>
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

      <section className="detail-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Detalle del juego</p>
            <h1>{selectedGame?.name ?? "Juego"}</h1>
          </div>
          <button className="secondary-action" onClick={() => onNavigate("home")} type="button">
            Volver
          </button>
        </header>

        {error && <p className="form-message error">{error}</p>}

        {isLoading ? (
          <p className="muted">Cargando detalle...</p>
        ) : selectedGame ? (
          <>
            <section className="detail-hero">
              <GameArtwork game={selectedGame} />
              <div className="detail-summary">
                <p className="section-kicker">GameHub</p>
                <h2>{selectedGame.name}</h2>
                <p>
                  Este juego esta guardado en el catalogo global de GameHub. Si fue
                  agregado manualmente y todavia no tenemos informacion externa, el detalle
                  se muestra igual con datos basicos y sin romper la experiencia.
                </p>
              </div>
            </section>

            <section className="detail-grid">
              <article className="detail-panel">
                <h2>En tu biblioteca</h2>
                {libraryEntry ? (
                  <dl className="detail-list">
                    <div>
                      <dt>Estado</dt>
                      <dd>{statusLabels[libraryEntry.status]}</dd>
                    </div>
                    <div>
                      <dt>Rating personal</dt>
                      <dd>{libraryEntry.rating ?? "Sin rating"}</dd>
                    </div>
                    <div>
                      <dt>Horas jugadas</dt>
                      <dd>{Math.floor(libraryEntry.playtimeMinutes / 60)}</dd>
                    </div>
                    <div>
                      <dt>Favorito</dt>
                      <dd>{libraryEntry.favorite ? "Si" : "No"}</dd>
                    </div>
                  </dl>
                ) : (
                  <EmptyState
                    title="Todavia no esta en tu biblioteca"
                    text="Podes buscarlo y agregarlo desde la pantalla Buscar."
                  />
                )}
              </article>

              <article className="detail-panel">
                <h2>Informacion disponible</h2>
                <dl className="detail-list">
                  <div>
                    <dt>ID local</dt>
                    <dd>{selectedGame.id}</dd>
                  </div>
                  <div>
                    <dt>Imagen</dt>
                    <dd>{selectedGame.imageUrl ? "Disponible" : "Pendiente"}</dd>
                  </div>
                  <div>
                    <dt>Origen</dt>
                    <dd>Catalogo GameHub</dd>
                  </div>
                </dl>
              </article>
            </section>
          </>
        ) : (
          <EmptyState
            title="No encontramos ese juego"
            text="Volvé al Home o a Buscar para abrir un juego del catalogo."
          />
        )}
      </section>
    </main>
  );
}

function GameArtwork({ game }: { game: GameResponse }) {
  const [imageFailed, setImageFailed] = useState(false);

  if (game.imageUrl && !imageFailed) {
    return (
      <img
        alt={game.name}
        className="detail-artwork"
        onError={() => setImageFailed(true)}
        src={game.imageUrl}
      />
    );
  }

  return (
    <div className="detail-artwork fallback-art" aria-hidden="true">
      <span>{game.name.slice(0, 2).toUpperCase()}</span>
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

export default GameDetailPage;
