import { useEffect, useMemo, useState } from "react";
import {
  getLibraryStats,
  LibraryStatsResponse,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";
import Sidebar from "./Sidebar";

type StatsPageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

function StatsPage({ activeScreen, user, onLogout, onNavigate }: StatsPageProps) {
  const [stats, setStats] = useState<LibraryStatsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadStats() {
      setIsLoading(true);
      setError("");

      try {
        const response = await getLibraryStats(user.id);

        if (isMounted) {
          setStats(response);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : "No se pudieron cargar las estadisticas"
          );
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadStats();

    return () => {
      isMounted = false;
    };
  }, [user.id]);

  const statusRows = useMemo(() => {
    if (!stats) {
      return [];
    }

    return [
      { label: "Completados", value: stats.completedGames },
      { label: "Backlog", value: stats.backlogGames },
      { label: "Jugando", value: stats.playingGames },
      { label: "Pausados", value: stats.onHoldGames },
      { label: "Abandonados", value: stats.droppedGames }
    ];
  }, [stats]);

  const maxStatusValue = Math.max(...statusRows.map((row) => row.value), 1);

  return (
    <main className="app-shell">
      <Sidebar
        activeScreen={activeScreen}
        onLogout={onLogout}
        onNavigate={onNavigate}
        user={user}
      />

      <section className="stats-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Resumen personal</p>
            <h1>Estadisticas</h1>
          </div>
          <button className="secondary-action" onClick={() => onNavigate("library")} type="button">
            Ver biblioteca
          </button>
        </header>

        {error && <p className="form-message error">{error}</p>}

        {isLoading ? (
          <p className="muted">Cargando estadisticas...</p>
        ) : stats ? (
          <>
            <section className="stats-metric-grid" aria-label="Metricas principales">
              <Metric label="Juegos" value={stats.totalGames} />
              <Metric label="Completados" value={stats.completedGames} />
              <Metric label="Favoritos" value={stats.favoriteGames} />
              <Metric label="Horas" value={stats.totalPlaytimeHours} />
            </section>

            <section className="stats-layout">
              <article className="stats-panel">
                <div className="section-heading">
                  <h2>Estado de biblioteca</h2>
                </div>
                <div className="status-bars">
                  {statusRows.map((row) => (
                    <div className="status-bar-row" key={row.label}>
                      <div>
                        <span>{row.label}</span>
                        <strong>{row.value}</strong>
                      </div>
                      <div className="status-track" aria-hidden="true">
                        <span style={{ width: `${(row.value / maxStatusValue) * 100}%` }} />
                      </div>
                    </div>
                  ))}
                </div>
              </article>

              <article className="stats-panel">
                <div className="section-heading">
                  <h2>Distribucion</h2>
                </div>
                <dl className="detail-list">
                  <div>
                    <dt>Backlog</dt>
                    <dd>{percentage(stats.backlogGames, stats.totalGames)}%</dd>
                  </div>
                  <div>
                    <dt>Completados</dt>
                    <dd>{percentage(stats.completedGames, stats.totalGames)}%</dd>
                  </div>
                  <div>
                    <dt>Favoritos</dt>
                    <dd>{percentage(stats.favoriteGames, stats.totalGames)}%</dd>
                  </div>
                  <div>
                    <dt>Tiempo total</dt>
                    <dd>{stats.totalPlaytimeMinutes} min</dd>
                  </div>
                </dl>
              </article>
            </section>
          </>
        ) : (
          <EmptyState
            title="Sin estadisticas"
            text="Cuando agregues juegos, vamos a mostrar tu progreso aca."
          />
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

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <article className="metric-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
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

export default StatsPage;
