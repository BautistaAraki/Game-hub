import { UserResponse } from "./api";
import { AppScreen } from "./navigation";

type SidebarProps = {
  activeScreen: AppScreen;
  user: UserResponse;
  onLogout: () => void;
  onNavigate: (screen: AppScreen) => void;
};

const navItems: Array<{ label: string; screen: AppScreen }> = [
  { label: "Home", screen: "home" },
  { label: "Biblioteca", screen: "library" },
  { label: "Buscar", screen: "search" },
  { label: "Estadisticas", screen: "stats" },
  { label: "Perfil", screen: "profile" }
];

function Sidebar({ activeScreen, user, onLogout, onNavigate }: SidebarProps) {
  return (
    <aside className="sidebar" aria-label="Navegacion principal">
      <div className="sidebar-brand">GAMEHUB</div>
      <nav>
        {navItems.map((item) => (
          <button
            className={activeScreen === item.screen ? "nav-item active" : "nav-item"}
            key={item.screen}
            onClick={() => onNavigate(item.screen)}
            type="button"
          >
            {item.label}
          </button>
        ))}
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

export default Sidebar;
