// Offline support: serve the game from the cache, refresh it in the background.
var CACHE = "timemaze-v1";
var SHELL = [
  "./",
  "index.html",
  "host.js",
  "js/timemaze.js",
  "manifest.webmanifest",
  "icons/icon-192.png",
  "icons/icon-512.png",
  "icons/apple-touch-icon.png"
];

self.addEventListener("install", function (event) {
  event.waitUntil(caches.open(CACHE).then(function (cache) { return cache.addAll(SHELL); }));
  self.skipWaiting();
});

self.addEventListener("activate", function (event) {
  event.waitUntil(caches.keys().then(function (keys) {
    return Promise.all(keys.filter(function (k) { return k !== CACHE; }).map(function (k) { return caches.delete(k); }));
  }));
  self.clients.claim();
});

self.addEventListener("fetch", function (event) {
  if (event.request.method !== "GET") return;
  event.respondWith(caches.open(CACHE).then(function (cache) {
    return cache.match(event.request).then(function (cached) {
      var fresh = fetch(event.request).then(function (response) {
        if (response && response.ok) cache.put(event.request, response.clone());
        return response;
      }).catch(function () { return cached; });
      return cached || fresh;
    });
  }));
});
