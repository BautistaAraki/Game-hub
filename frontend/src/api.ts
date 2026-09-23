const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export type UserResponse = {
  id: number;
  username: string;
  email: string;
};

export type AuthResponse = {
  token: string;
  user: UserResponse;
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

export type UserExternalAccountResponse = {
  id: number;
  userId: number;
  platform: "STEAM" | "IGDB";
  externalUserId: string;
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

let authToken: string | null = null;

export function setAuthToken(token: string | null) {
  authToken = token;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  if (authToken && !headers.has("Authorization")) headers.set("Authorization", `Bearer ${authToken}`);
  const timeout = AbortSignal.timeout(60000);
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      signal: options.signal ? AbortSignal.any([options.signal, timeout]) : timeout
    });
  } catch (error) {
    if (options.signal?.aborted) throw error;
    throw new Error(timeout.aborted
      ? "La solicitud tardó demasiado. Intentá nuevamente."
      : "No se pudo conectar con el servidor. Comprobá que Spring esté iniciado.");
  }

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ApiErrorResponse;
    throw new Error(body?.message ?? "No se pudo completar la solicitud");
  }

  const body = await response.text();
  return body ? JSON.parse(body) as T : undefined as T;
}

export function login(email: string, password: string) {
  return request<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email: email.trim().toLowerCase(), password })
  });
}

export function register(username: string, email: string, password: string) {
  return request<UserResponse>("/api/users", {
    method: "POST",
    body: JSON.stringify({
      username: username.trim(),
      email: email.trim().toLowerCase(),
      password
    })
  });
}

export function getCatalog() {
  return request<GameResponse[]>("/api/games");
}

export function searchCatalog(query: string, signal?: AbortSignal) {
  return request<GameResponse[]>(`/api/games/search?query=${encodeURIComponent(query)}`, { signal });
}

export function getUserLibrary() {
  return request<UserGameResponse[]>("/api/library/me");
}

export function getLibraryStats() {
  return request<LibraryStatsResponse>("/api/library/me/stats");
}

export function linkSteamAccount(steamId: string) {
  return request<UserExternalAccountResponse>("/api/me/external-accounts/steam", {
    method: "POST",
    body: JSON.stringify({ steamId: steamId.trim() })
  });
}

export function searchExternalGames(query: string, signal?: AbortSignal) {
  return request<ExternalGameSearchResponse[]>(
    `/api/external-games/search?query=${encodeURIComponent(query)}`,
    { signal }
  );
}

export function addGameToLibrary(gameId: number) {
  return request<UserGameResponse>("/api/library", {
    method: "POST",
    body: JSON.stringify({ gameId })
  });
}

export function addExternalGameToLibrary(
  game: ExternalGameSearchResponse
) {
  return request<UserGameResponse>("/api/external-games/library", {
    method: "POST",
    body: JSON.stringify({
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
