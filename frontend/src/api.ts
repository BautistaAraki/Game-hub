const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export type UserResponse = {
  id: number;
  username: string;
  email: string;
};

export type GameResponse = {
  id: number;
  name: string;
  imageUrl: string | null;
  description: string | null;
  releaseDate: string | null;
  platforms: string | null;
  externalSource: string | null;
};

export type UserGameResponse = {
  id: number;
  userId: number;
  gameId: number;
  rating: number | null;
  playtimeMinutes: number;
  favorite: boolean;
  status: "BACKLOG" | "PLAYING" | "COMPLETED" | "ON_HOLD" | "DROPPED";
};

export type GameStatus = UserGameResponse["status"];

export type LibraryStatsResponse = {
  userId: number;
  totalGames: number;
  completedGames: number;
  backlogGames: number;
  playingGames: number;
  onHoldGames: number;
  droppedGames: number;
  favoriteGames: number;
  totalPlaytimeMinutes: number;
  totalPlaytimeHours: number;
};

export type RawgGameResponse = {
  rawgId: number;
  name: string;
  imageUrl: string | null;
  released: string | null;
  rating: number | null;
};

export type ExternalGameSearchResponse = {
  source: string;
  externalId: string;
  title: string;
  description: string | null;
  imageUrl: string | null;
  releaseDate: string | null;
  platforms: string[];
};

type ApiErrorResponse = {
  message?: string;
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...options.headers
    },
    ...options
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ApiErrorResponse;
    throw new Error(body.message ?? "No se pudo completar la solicitud");
  }

  return response.json() as Promise<T>;
}

export function login(email: string, password: string) {
  return request<UserResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
}

export function register(username: string, email: string, password: string) {
  return request<UserResponse>("/api/users", {
    method: "POST",
    body: JSON.stringify({ username, email, password })
  });
}

export function getCatalog() {
  return request<GameResponse[]>("/api/games");
}

export function searchCatalog(query: string) {
  return request<GameResponse[]>(`/api/games/search?query=${encodeURIComponent(query)}`);
}

export function getUserLibrary(userId: number) {
  return request<UserGameResponse[]>(`/api/library/users/${userId}`);
}

export function getLibraryStats(userId: number) {
  return request<LibraryStatsResponse>(`/api/library/users/${userId}/stats`);
}

export function searchRawgGames(query: string) {
  return request<RawgGameResponse[]>(
    `/api/rawg/games/search?query=${encodeURIComponent(query)}`
  );
}

export function searchExternalGames(query: string) {
  return request<ExternalGameSearchResponse[]>(
    `/api/external-games/search?query=${encodeURIComponent(query)}`
  );
}

export function addGameToLibrary(userId: number, gameId: number) {
  return request<UserGameResponse>("/api/library", {
    method: "POST",
    body: JSON.stringify({ userId, gameId })
  });
}

export function addExternalGameToLibrary(
  userId: number,
  game: ExternalGameSearchResponse
) {
  return request<UserGameResponse>("/api/external-games/library", {
    method: "POST",
    body: JSON.stringify({
      userId,
      source: game.source,
      externalId: game.externalId,
      title: game.title,
      imageUrl: game.imageUrl,
      description: game.description,
      releaseDate: game.releaseDate,
      platforms: game.platforms
    })
  });
}

export function updateLibraryGameStatus(userGameId: number, status: GameStatus) {
  return request<UserGameResponse>(
    `/api/library/${userGameId}/status?status=${encodeURIComponent(status)}`,
    { method: "PATCH" }
  );
}

export function updateLibraryGameRating(userGameId: number, rating: number) {
  return request<UserGameResponse>(
    `/api/library/${userGameId}/rating?rating=${encodeURIComponent(rating)}`,
    { method: "PATCH" }
  );
}

export function markLibraryGameAsFavorite(userGameId: number) {
  return request<UserGameResponse>(`/api/library/${userGameId}/favorite`, {
    method: "PATCH"
  });
}

export function unmarkLibraryGameAsFavorite(userGameId: number) {
  return request<UserGameResponse>(`/api/library/${userGameId}/unfavorite`, {
    method: "PATCH"
  });
}
