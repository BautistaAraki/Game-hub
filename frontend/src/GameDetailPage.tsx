import { useEffect, useMemo, useState } from "react";
import {
  GameStatus,
  GameResponse,
  getCatalog,
  getUserLibrary,
  updateLibraryGameRating,
  updateLibraryGameStatus,
  UserGameResponse,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";
import Sidebar from "./Sidebar";

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
  const [message, setMessage] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadDetailData() {
      setIsLoading(true);
      setError("");
      setMessage("");

      try {
        const [catalogResponse, libraryResponse] = await Promise.all([
          getCatalog(),
          getUserLibrary()
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

  async function handleStatusChange(entry: UserGameResponse, status: GameStatus) {
    await updateLibraryEntry(
      () => updateLibraryGameStatus(entry.id, status),
      "Estado actualizado"
    );
  }

  async function handleRatingChange(entry: UserGameResponse, value: string) {
    const rating = Number(value);

    if (!Number.isInteger(rating)) {
      return;
    }

    await updateLibraryEntry(
      () => updateLibraryGameRating(entry.id, rating),
      "Rating actualizado"
    );
  }

  async function updateLibraryEntry(
    requestUpdate: () => Promise<UserGameResponse>,
    successMessage: string
  ) {
    setIsSaving(true);
    setError("");
    setMessage("");

    try {
      const updatedEntry = await requestUpdate();
      setLibrary((current) =>
        current.map((entry) => entry.id === updatedEntry.id ? updatedEntry : entry)
      );
      setMessage(successMessage);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "No se pudo actualizar el juego"
      );
    } finally {
      setIsSaving(false);
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
        {message && <p className="form-message success">{message}</p>}

        {isLoading ? (
          <p className="muted">Cargando detalle...</p>
        ) : selectedGame ? (
          <>
            <section className="detail-hero">
              <GameArtwork game={selectedGame} />
              <div className="detail-summary">
                {libraryEntry && (
                  <div className="detail-quick-controls" aria-label="Controles de biblioteca">
                    <label>
                      Estado
                      <select
                        disabled={isSaving}
                        onChange={(event) =>
                          handleStatusChange(libraryEntry, event.target.value as GameStatus)
                        }
                        value={libraryEntry.status}
                      >
                        <option value="BACKLOG">Backlog</option>
                        <option value="PLAYING">Jugando</option>
                        <option value="COMPLETED">Completado</option>
                        <option value="ON_HOLD">Pausado</option>
                        <option value="DROPPED">Abandonado</option>
                      </select>
                    </label>

                    <label>
                      Rating
                      <select
                        disabled={isSaving}
                        onChange={(event) => handleRatingChange(libraryEntry, event.target.value)}
                        value={libraryEntry.rating ?? ""}
                      >
                        <option value="" disabled>Sin rating</option>
                        {Array.from({ length: 10 }, (_, index) => index + 1).map((rating) => (
                          <option key={rating} value={rating}>{rating}/10</option>
                        ))}
                      </select>
                    </label>
                  </div>
                )}
                <p className="section-kicker">GameHub</p>
                <h2>{selectedGame.name}</h2>
                <p>
                  {selectedGame.description
                    ?? "Este juego esta guardado en el catalogo global de GameHub. Si fue agregado manualmente y todavia no tenemos informacion externa, el detalle se muestra igual con datos basicos y sin romper la experiencia."}
                </p>
                {selectedGame.platforms && (
                  <div className="platform-chip-row">
                    {selectedGame.platforms.split(",").map((platform) => (
                      <span className="platform-chip" key={platform.trim()}>
                        {platform.trim()}
                      </span>
                    ))}
                  </div>
                )}
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
                    <dt>Lanzamiento</dt>
                    <dd>{selectedGame.releaseDate ?? "Sin fecha"}</dd>
                  </div>
                  <div>
                    <dt>Imagen</dt>
                    <dd>{selectedGame.imageUrl ? "Disponible" : "Pendiente"}</dd>
                  </div>
                  <div>
                    <dt>Origen</dt>
                    <dd>{selectedGame.externalSource ?? "Catalogo GameHub"}</dd>
                  </div>
                  <div>
                    <dt>ID local</dt>
                    <dd>{selectedGame.id}</dd>
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
