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

type ProfilePageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
  onOpenGame: (gameId: number) => void;
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

function ProfilePage({
  activeScreen,
  user,
  onLogout,
  onNavigate,
  onOpenGame
}: ProfilePageProps) {
  const [catalog, setCatalog] = useState<GameResponse[]>([]);
  const [library, setLibrary] = useState<UserGameResponse[]>([]);
  const [stats, setStats] = useState<LibraryStatsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadProfileData() {
      setIsLoading(true);
      setError("");

      try {
        const [catalogResponse, libraryResponse, statsResponse] = await Promise.all([
          getCatalog(),
          getUserLibrary(user.id),
          getLibraryStats(user.id)
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
              : "No se pudo cargar el perfil"
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadProfileData();

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

  const favoriteGames = libraryGames.filter((entry) => entry.favorite).slice(0, 4);
  const currentGame = libraryGames.find((entry) => entry.status === "PLAYING") ?? libraryGames[0];
  const completedPercentage = percentage(stats?.completedGames ?? 0, stats?.totalGames ?? 0);
  const initials = user.username.slice(0, 2).toUpperCase();

  return (
    <main className="app-shell">
      <Sidebar
        activeScreen={activeScreen}
        onLogout={onLogout}
        onNavigate={onNavigate}
        user={user}
      />

      <section className="profile-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Cuenta</p>
            <h1>Perfil</h1>
          </div>
          <button className="secondary-action" onClick={() => onNavigate("library")} type="button">
            Ver biblioteca
          </button>
        </header>

        {error && <p className="form-message error">{error}</p>}

        <section className="profile-hero">
          <div className="profile-identity">
            <span className="profile-avatar">{initials}</span>
            <div>
              <p className="section-kicker">GameHub player</p>
              <h2>{user.username}</h2>
              <p>{user.email}</p>
            </div>
          </div>
          <dl className="profile-meta">
            <div>
              <dt>Juegos</dt>
              <dd>{stats?.totalGames ?? 0}</dd>
            </div>
            <div>
              <dt>Horas</dt>
              <dd>{stats?.totalPlaytimeHours ?? 0}</dd>
            </div>
            <div>
              <dt>Completado</dt>
              <dd>{completedPercentage}%</dd>
            </div>
          </dl>
        </section>

        {isLoading ? (
          <p className="muted">Cargando perfil...</p>
        ) : (
          <section className="profile-layout">
            <article className="profile-panel">
              <div className="section-heading">
                <h2>Resumen</h2>
              </div>
              <dl className="detail-list">
                <div>
                  <dt>Favoritos</dt>
                  <dd>{stats?.favoriteGames ?? 0}</dd>
                </div>
                <div>
                  <dt>Jugando</dt>
                  <dd>{stats?.playingGames ?? 0}</dd>
                </div>
                <div>
                  <dt>Backlog</dt>
                  <dd>{stats?.backlogGames ?? 0}</dd>
                </div>
                <div>
                  <dt>Cuenta Steam</dt>
                  <dd>Pendiente</dd>
                </div>
              </dl>
            </article>

            <article className="profile-panel">
              <div className="section-heading">
                <h2>Actividad</h2>
              </div>
              {currentGame?.game ? (
                <button
                  className="profile-current-game"
                  onClick={() => onOpenGame(currentGame.gameId)}
                  type="button"
                >
                  <GameArtwork game={currentGame.game} />
                  <span>
                    <strong>{currentGame.game.name}</strong>
                    <small>{statusLabels[currentGame.status]}</small>
                  </span>
                </button>
              ) : (
                <EmptyState
                  title="Sin actividad"
                  text="Cuando agregues juegos, vamos a destacar uno aca."
                />
              )}
            </article>

            <article className="profile-panel profile-wide-panel">
              <div className="section-heading">
                <h2>Favoritos</h2>
                <button onClick={() => onNavigate("library")} type="button">Gestionar</button>
              </div>
              {favoriteGames.length > 0 ? (
                <div className="profile-favorites">
                  {favoriteGames.map((entry) => (
                    <button
                      className="profile-favorite-card"
                      key={entry.id}
                      onClick={() => entry.game && onOpenGame(entry.game.id)}
                      type="button"
                    >
                      <GameArtwork game={entry.game} />
                      <span>{entry.game?.name ?? `Juego #${entry.gameId}`}</span>
                    </button>
                  ))}
                </div>
              ) : (
                <EmptyState
                  title="Sin favoritos"
                  text="Marca juegos como favoritos desde tu biblioteca."
                />
              )}
            </article>
          </section>
        )}
      </section>
    </main>
  );
}

function percentage(value: number, total: number) {
  if (total === 0) {
    return 0;
  }

  return Math.round((value / total) * 100);
}

function GameArtwork({ game }: { game?: GameResponse }) {
  const [imageFailed, setImageFailed] = useState(false);

  if (game?.imageUrl && !imageFailed) {
    return (
      <img
        alt={game.name}
        className="game-art game-art-small"
        onError={() => setImageFailed(true)}
        src={game.imageUrl}
      />
    );
  }

  return (
    <div className="game-art game-art-small fallback-art" aria-hidden="true">
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

export default ProfilePage;
