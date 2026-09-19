import Sidebar from "./Sidebar";
import { FormEvent, useEffect, useRef, useState } from "react";
import {
  addGameToLibrary,
  GameResponse,
  IgdbGameResponse,
  searchCatalog,
  searchIgdbGames,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";

type SearchPageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

function SearchPage({ activeScreen, user, onLogout, onNavigate }: SearchPageProps) {
  const [query, setQuery] = useState("");
  const [localResults, setLocalResults] = useState<GameResponse[]>([]);
  const [igdbResults, setIgdbResults] = useState<IgdbGameResponse[]>([]);
  const [localLoading, setLocalLoading] = useState(false);
  const [externalLoading, setExternalLoading] = useState(false);
  const isSearching = localLoading || externalLoading;
  const [hasSearched, setHasSearched] = useState(false);
  const [messageIsError, setMessageIsError] = useState(false);
  const [addingIds, setAddingIds] = useState<Set<number>>(new Set());
  const [addedIds, setAddedIds] = useState<Set<number>>(new Set());
  const pendingAdds = useRef(new Set<number>());
  const searchController = useRef<AbortController | null>(null);
  useEffect(() => () => searchController.current?.abort(), []);
  const [message, setMessage] = useState("");
  const [igdbWarning, setIgdbWarning] = useState("");

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSearching) return;

    if (!query.trim()) {
      setMessageIsError(true);
      setMessage("Escribi el nombre de un juego para buscar.");
      setLocalResults([]);
      setIgdbResults([]);
      setIgdbWarning("");
      return;
    }

    const controller = new AbortController();
    searchController.current?.abort();
    searchController.current = controller;
    setHasSearched(true);
    setLocalLoading(true);
    setExternalLoading(true);
    setMessageIsError(false);
    setMessage("");
    setIgdbWarning("");

    setLocalResults([]);
    setIgdbResults([]);
    // Render each source as soon as it responds; a slow provider must not hide local results.
    await Promise.all([
      searchCatalog(query.trim(), controller.signal).then((games) => {
        if (!controller.signal.aborted) setLocalResults(games);
      }).catch((error: unknown) => {
        if (!controller.signal.aborted) {
          setMessageIsError(true);
          setMessage(error instanceof Error ? error.message : "No se pudo consultar el catalogo local.");
        }
      }).finally(() => { if (!controller.signal.aborted) setLocalLoading(false); }),
      searchIgdbGames(query.trim(), controller.signal).then((games) => {
        if (!controller.signal.aborted) setIgdbResults(games);
      }).catch((error: unknown) => {
        if (!controller.signal.aborted) setIgdbWarning(error instanceof Error ? error.message : "No pudimos consultar IGDB ahora.");
      }).finally(() => { if (!controller.signal.aborted) setExternalLoading(false); })
    ]);


  }

  async function handleAddLocalGame(game: GameResponse) {
    if (pendingAdds.current.has(game.id) || addedIds.has(game.id)) return;
    pendingAdds.current.add(game.id);
    setAddingIds(new Set(pendingAdds.current));
    setMessage("");
    setMessageIsError(false);

    try {
      await addGameToLibrary(user.id, game.id);
      setAddedIds((current) => new Set(current).add(game.id));
      setMessage(`${game.name} se agrego a tu biblioteca.`);
    } catch (requestError) {
      setMessageIsError(true);
      setMessage(
        requestError instanceof Error
          ? requestError.message
          : "No se pudo agregar el juego."
      );
    } finally {
      pendingAdds.current.delete(game.id);
      setAddingIds(new Set(pendingAdds.current));
    }
  }

  return (
    <main className="app-shell">
      <Sidebar activeScreen={activeScreen} user={user} onLogout={onLogout} onNavigate={onNavigate} />

      <section className="home-page">
        <header className="topbar">
          <div>
            <p className="section-kicker">Agregar juegos</p>
            <h1>Buscar</h1>
          </div>
        </header>

        <form className="search-panel" onSubmit={handleSearch}>
          <label>
            Nombre del juego
            <input
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Minecraft, Elden Ring, Hollow Knight..."
              maxLength={200}
              disabled={isSearching}
              type="search"
              value={query}
            />
          </label>
          <button className="submit-button" disabled={isSearching} type="submit">
            {isSearching ? "Buscando..." : "Buscar juego"}
          </button>
        </form>

        {message && <p role={messageIsError ? "alert" : "status"} className={`form-message ${messageIsError ? "error" : "success"}`}>{message}</p>}
        {igdbWarning && (
          <p className="form-message warning">
            Buscador externo no disponible: {igdbWarning}
          </p>
        )}

        <section className="search-results-grid">
          <div className="home-section">
            <div className="section-heading">
              <h2>Catalogo local</h2>
            </div>

            {localLoading ? <p role="status" className="muted">Buscando en el catálogo local...</p> : localResults.length > 0 ? (
              <div className="catalog-list">
                {localResults.map((game) => (
                  <article className="catalog-row catalog-row-action" key={game.id}>
                    <GameArtwork game={game} />
                    <div>
                      <h3>{game.name}</h3>
                      <p>Disponible en GameHub</p>
                    </div>
                    <button type="button" disabled={addingIds.has(game.id) || addedIds.has(game.id)} onClick={() => handleAddLocalGame(game)}>
                      {addingIds.has(game.id) ? "Agregando..." : addedIds.has(game.id) ? "Agregado" : "Agregar"}
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title={hasSearched ? "Sin resultados locales" : "Buscá tu próximo juego"}
                text="Escribí un nombre para consultar el catálogo local y los resultados externos."
              />
            )}
          </div>

          <div className="home-section">
            <div className="section-heading">
              <h2>Resultados de IGDB</h2>
              <a href="https://www.igdb.com/" target="_blank" rel="noreferrer">Datos de IGDB</a>
            </div>

            {externalLoading ? <p role="status" className="muted">Buscando en IGDB...</p> : igdbResults.length > 0 ? (
              <div className="external-game-grid">
                {igdbResults.map((game) => (
                  <article className="external-game-card" key={game.igdbId}>
                    <IgdbArtwork game={game} />
                    <div>
                      <h3>{game.name}</h3>
                      <p>
                        {game.released ?? "Sin fecha"} · {game.rating == null ? "Sin rating" : `${game.rating.toFixed(1)}/100`}
                      </p>
                    </div>
                    <button disabled type="button">Pendiente</button>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title={hasSearched ? "Sin resultados externos" : "Búsqueda externa"}
                text="Si IGDB falla, tu busqueda local sigue funcionando."
              />
            )}
          </div>
        </section>
      </section>
    </main>
  );
}

function GameArtwork({ game }: { game: GameResponse }) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  if (game.imageUrl && game.imageUrl !== failedUrl) {
    return <img alt={game.name} className="game-art game-art-small" src={game.imageUrl} onError={() => setFailedUrl(game.imageUrl)} />;
  }
  return (
    <div className="game-art game-art-small fallback-art" aria-hidden="true">
      <span>{game.name.slice(0, 2).toUpperCase()}</span>
    </div>
  );
}

function IgdbArtwork({ game }: { game: IgdbGameResponse }) {
  const [imageFailed, setImageFailed] = useState(false);
  const initials = game.name.slice(0, 2).toUpperCase();

  if (game.imageUrl && !imageFailed) {
    return <img alt={game.name} className="external-game-art" src={game.imageUrl} onError={() => setImageFailed(true)} />;
  }

  return (
    <div className="external-game-art fallback-art" aria-hidden="true">
      <span>{initials}</span>
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

export default SearchPage;
