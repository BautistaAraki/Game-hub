import { FormEvent, useState } from "react";
import {
  addExternalGameToLibrary,
  addGameToLibrary,
  ExternalGameSearchResponse,
  GameResponse,
  searchCatalog,
  searchExternalGames,
  UserResponse
} from "./api";
import { AppScreen } from "./navigation";

type SearchPageProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onOpenGame: (gameId: number) => void;
  onNavigate: (screen: AppScreen) => void;
};

function SearchPage({ activeScreen, user, onLogout, onOpenGame, onNavigate }: SearchPageProps) {
  const [query, setQuery] = useState("");
  const [localResults, setLocalResults] = useState<GameResponse[]>([]);
  const [externalResults, setExternalResults] = useState<ExternalGameSearchResponse[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [message, setMessage] = useState("");
  const [externalWarning, setExternalWarning] = useState("");

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!query.trim()) {
      setMessage("Escribi el nombre de un juego para buscar.");
      setLocalResults([]);
      setExternalResults([]);
      setExternalWarning("");
      return;
    }

    setIsSearching(true);
    setMessage("");
    setExternalWarning("");

    const [localResponse, externalResponse] = await Promise.allSettled([
      searchCatalog(query),
      searchExternalGames(query)
    ]);

    if (localResponse.status === "fulfilled") {
      setLocalResults(localResponse.value);
    } else {
      setLocalResults([]);
      setMessage("No se pudo consultar el catalogo local.");
    }

    if (externalResponse.status === "fulfilled") {
      setExternalResults(externalResponse.value);
    } else {
      setExternalResults([]);
      setExternalWarning(
        externalResponse.reason instanceof Error
          ? externalResponse.reason.message
          : "No pudimos consultar la API externa ahora."
      );
    }

    setIsSearching(false);
  }

  async function handleAddLocalGame(game: GameResponse) {
    setMessage("");

    try {
      await addGameToLibrary(user.id, game.id);
      setMessage(`${game.name} se agrego a tu biblioteca.`);
    } catch (requestError) {
      setMessage(
        requestError instanceof Error
          ? requestError.message
          : "No se pudo agregar el juego."
      );
    }
  }

  async function handleAddExternalGame(game: ExternalGameSearchResponse) {
    setMessage("");

    try {
      const response = await addExternalGameToLibrary(user.id, game);
      setMessage(`${game.title} se agrego a tu biblioteca.`);
      onOpenGame(response.gameId);
    } catch (requestError) {
      setMessage(
        requestError instanceof Error
          ? requestError.message
          : "No se pudo agregar el juego externo."
      );
    }
  }

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
              type="search"
              value={query}
            />
          </label>
          <button className="submit-button" disabled={isSearching} type="submit">
            {isSearching ? "Buscando..." : "Buscar juego"}
          </button>
        </form>

        {message && <p className="form-message success">{message}</p>}
        {externalWarning && (
          <p className="form-message warning">
            Buscador externo no disponible: {externalWarning}
          </p>
        )}

        <section className="search-results-grid">
          <div className="home-section">
            <div className="section-heading">
              <h2>Catalogo local</h2>
            </div>

            {localResults.length > 0 ? (
              <div className="catalog-list">
                {localResults.map((game) => (
                  <article className="catalog-row catalog-row-action" key={game.id}>
                    <GameArtwork game={game} />
                    <div>
                      <h3>{game.name}</h3>
                      <p>Disponible en GameHub</p>
                    </div>
                    <button type="button" onClick={() => handleAddLocalGame(game)}>
                      Agregar
                    </button>
                    <button type="button" onClick={() => onOpenGame(game.id)}>
                      Ver
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Sin resultados locales"
                text="Si la API externa esta disponible, abajo vas a ver coincidencias."
              />
            )}
          </div>

          <div className="home-section">
            <div className="section-heading">
              <h2>Resultados externos</h2>
            </div>

            {externalResults.length > 0 ? (
              <div className="external-game-grid">
                {externalResults.map((game) => (
                  <article className="external-game-card" key={`${game.source}-${game.externalId}`}>
                    <ExternalArtwork game={game} />
                    <div>
                      <h3>{game.title}</h3>
                      <p>
                        {game.releaseDate ?? "Sin fecha"} · {game.source}
                      </p>
                      {game.platforms.length > 0 && (
                        <p>{game.platforms.slice(0, 3).join(", ")}</p>
                      )}
                    </div>
                    <button type="button" onClick={() => handleAddExternalGame(game)}>
                      Agregar
                    </button>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Sin resultados externos"
                text="Si la API externa falla, tu busqueda local sigue funcionando."
              />
            )}
          </div>
        </section>
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
        className="game-art game-art-small"
        onError={() => setImageFailed(true)}
        src={game.imageUrl}
      />
    );
  }

  return (
    <div className="game-art game-art-small fallback-art" aria-hidden="true">
      <span>{game.name.slice(0, 2).toUpperCase()}</span>
    </div>
  );
}

function ExternalArtwork({ game }: { game: ExternalGameSearchResponse }) {
  const initials = game.title.slice(0, 2).toUpperCase();

  if (game.imageUrl) {
    return <img alt={game.title} className="external-game-art" src={game.imageUrl} />;
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
