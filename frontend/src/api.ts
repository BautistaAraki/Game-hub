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

export type IgdbGameResponse = {
  igdbId: number;
  name: string;
  imageUrl: string | null;
  released: string | null;
  rating: number | null;
};

type ApiErrorResponse = {
  message?: string;
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
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

export function searchCatalog(query: string, signal?: AbortSignal) {
  return request<GameResponse[]>(`/api/games/search?query=${encodeURIComponent(query)}`, { signal });
}

export function getUserLibrary(userId: number) {
  return request<UserGameResponse[]>(`/api/library/users/${userId}`);
}

export function getLibraryStats(userId: number) {
  return request<LibraryStatsResponse>(`/api/library/users/${userId}/stats`);
}

export function searchIgdbGames(query: string, signal?: AbortSignal) {
  return request<IgdbGameResponse[]>(
    `/api/igdb/games/search?query=${encodeURIComponent(query)}`, { signal }
  );
}

export function addGameToLibrary(userId: number, gameId: number) {
  return request<UserGameResponse>("/api/library", {
    method: "POST",
    body: JSON.stringify({ userId, gameId })
  });
}
