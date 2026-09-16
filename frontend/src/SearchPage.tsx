import { FormEvent, useState } from "react";
import {
  addGameToLibrary,
  GameResponse,
  RawgGameResponse,
  searchCatalog,
  searchRawgGames,
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
  const [rawgResults, setRawgResults] = useState<RawgGameResponse[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [message, setMessage] = useState("");
  const [rawgWarning, setRawgWarning] = useState("");

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!query.trim()) {
      setMessage("Escribi el nombre de un juego para buscar.");
      setLocalResults([]);
      setRawgResults([]);
      setRawgWarning("");
      return;
    }

    setIsSearching(true);
    setMessage("");
    setRawgWarning("");

    const [localResponse, rawgResponse] = await Promise.allSettled([
      searchCatalog(query),
      searchRawgGames(query)
    ]);

    if (localResponse.status === "fulfilled") {
      setLocalResults(localResponse.value);
    } else {
      setLocalResults([]);
      setMessage("No se pudo consultar el catalogo local.");
    }

    if (rawgResponse.status === "fulfilled") {
      setRawgResults(rawgResponse.value);
    } else {
      setRawgResults([]);
      setRawgWarning(
        rawgResponse.reason instanceof Error
          ? rawgResponse.reason.message
          : "No pudimos consultar RAWG ahora."
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
        {rawgWarning && (
          <p className="form-message warning">
            Buscador externo no disponible: {rawgWarning}
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
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Sin resultados locales"
                text="Si RAWG esta disponible, abajo vas a ver coincidencias externas."
              />
            )}
          </div>

          <div className="home-section">
            <div className="section-heading">
              <h2>Resultados externos</h2>
            </div>

            {rawgResults.length > 0 ? (
              <div className="external-game-grid">
                {rawgResults.map((game) => (
                  <article className="external-game-card" key={game.rawgId}>
                    <RawgArtwork game={game} />
                    <div>
                      <h3>{game.name}</h3>
                      <p>
                        {game.released ?? "Sin fecha"} · {game.rating ?? "Sin rating"}
                      </p>
                    </div>
                    <button disabled type="button">Pendiente</button>
                  </article>
                ))}
              </div>
            ) : (
              <EmptyState
                title="Sin resultados externos"
                text="Si RAWG falla, tu busqueda local sigue funcionando."
              />
            )}
          </div>
        </section>
      </section>
    </main>
  );
}

function GameArtwork({ game }: { game: GameResponse }) {
  return (
    <div className="game-art game-art-small fallback-art" aria-hidden="true">
      <span>{game.name.slice(0, 2).toUpperCase()}</span>
    </div>
  );
}

function RawgArtwork({ game }: { game: RawgGameResponse }) {
  const initials = game.name.slice(0, 2).toUpperCase();

  if (game.imageUrl) {
    return <img alt={game.name} className="external-game-art" src={game.imageUrl} />;
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
