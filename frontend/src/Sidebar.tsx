import { UserResponse } from "./api";
import { AppScreen } from "./navigation";

type SidebarProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

export default function Sidebar({ activeScreen, user, onLogout, onNavigate }: SidebarProps) {
  return (
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
            aria-current={activeScreen === "library" ? "page" : undefined}
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
  );
}
