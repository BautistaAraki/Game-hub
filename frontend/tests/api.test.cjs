const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const ts = require('typescript');

// Execute the actual API module with an isolated HTTP transport, without a running server.
function apiWith(fetch) {
  const source = fs.readFileSync('src/api.ts', 'utf8')
    .replace('import.meta.env.VITE_API_BASE_URL', 'undefined');
  const compiled = ts.transpileModule(source, {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 }
  }).outputText;
  const exports = {};
  new Function('exports', 'fetch', compiled)(exports, fetch);
  return exports;
}

test('search encodes input and GET does not force a JSON content type', async () => {
  const api = apiWith(async (url, options) => {
    assert.ok(url.endsWith('query=a%26b'));
    assert.equal(options.headers.has('Content-Type'), false);
    return new Response('[]');
  });
  assert.deepEqual(await api.searchCatalog('a&b'), []);
});

test('POST retains JSON headers and user/game identifiers', async () => {
  const api = apiWith(async (url, options) => {
    assert.equal(options.method, 'POST');
    assert.equal(options.headers.get('Content-Type'), 'application/json');
    assert.deepEqual(JSON.parse(options.body), { userId: 1, gameId: 2 });
    return new Response('{"id":3}');
  });
  assert.deepEqual(await api.addGameToLibrary(1, 2), { id: 3 });
});

test('authenticated requests send bearer token', async () => {
  const api = apiWith(async (url, options) => {
    assert.equal(options.headers.get('Authorization'), 'Bearer jwt-token');
    return new Response('[]');
  });
  api.setAuthToken('jwt-token');
  assert.deepEqual(await api.getCatalog(), []);
});

test('accepts empty successful responses', async () => {
  const api = apiWith(async () => new Response(null, { status: 204 }));
  assert.equal(await api.getCatalog(), undefined);
});

test('reports structured and empty provider errors', async () => {
  const structured = apiWith(async () => new Response('{"message":"IGDB no disponible"}', { status: 502 }));
  await assert.rejects(structured.searchExternalGames('game'), /IGDB no disponible/);
  const empty = apiWith(async () => new Response('null', { status: 502 }));
  await assert.rejects(empty.searchExternalGames('game'), /No se pudo completar/);
});

test('explains network failure and preserves cancellation', async () => {
  const offline = apiWith(async () => { throw new TypeError('Failed to fetch'); });
  await assert.rejects(offline.getCatalog(), /Comprobá que Spring esté iniciado/);
  const controller = new AbortController();
  controller.abort();
  const cancelled = apiWith(async (url, options) => {
    assert.equal(options.signal.aborted, true);
    throw new DOMException('Aborted', 'AbortError');
  });
  await assert.rejects(cancelled.searchCatalog('game', controller.signal), { name: 'AbortError' });
});
