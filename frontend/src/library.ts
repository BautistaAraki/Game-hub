import { GameResponse, UserGameResponse } from "./api";

export type StatusFilter = "ALL" | UserGameResponse["status"];
export type LibrarySort = "name" | "rating" | "hours" | "favorites";
export type LibraryEntry = UserGameResponse & { game?: GameResponse; name: string };

export const statusLabels: Record<UserGameResponse["status"], string> = {
  BACKLOG: "Backlog",
  PLAYING: "Jugando",
  COMPLETED: "Completado",
  ON_HOLD: "Pausado",
  DROPPED: "Abandonado"
};

export function selectLibrary(
  library: UserGameResponse[],
  catalog: GameResponse[],
  status: StatusFilter,
  sort: LibrarySort
): LibraryEntry[] {
  const games = new Map(catalog.map((game) => [game.id, game]));
  return library
    .filter((entry) => status === "ALL" || entry.status === status)
    .map((entry) => {
      const game = games.get(entry.gameId);
      return { ...entry, game, name: game?.name || `Juego #${entry.gameId}` };
    })
    .sort((a, b) => {
      let difference = 0;
      if (sort === "rating") difference = (b.rating ?? -1) - (a.rating ?? -1);
      if (sort === "hours") difference = b.playtimeMinutes - a.playtimeMinutes;
      if (sort === "favorites") difference = Number(b.favorite) - Number(a.favorite);
      return difference || a.name.localeCompare(b.name, "es", { sensitivity: "base", numeric: true }) || a.id - b.id;
    });
}
