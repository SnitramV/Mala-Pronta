const CACHE='mala-pronta-web-v1';
const ASSETS=["./", "./index.html", "./styles.css", "./app.js", "./data.js", "./model.js", "./photos.js", "./manifest.webmanifest", "./assets/icon-world-180.png", "./assets/icon-world-192.png", "./assets/icon-world-512.png", "./assets/inter_2026.png", "./assets/lieu_square.png", "./assets/lieu_transparent.png", "./assets/mascot_phone.png", "./assets/mascot_thumb.png", "./assets/mascot_travel.png", "./assets/sport_atletismo.png", "./assets/sport_basquete.png", "./assets/sport_futsal_futebol.png", "./assets/sport_handebol.png", "./assets/sport_lutas.png", "./assets/sport_tenisdecampo.png", "./assets/sport_tenisdemesa.png", "./assets/sport_volei.png"];
const paths=new Set(ASSETS.map(path=>new URL(path,self.location).pathname));
self.addEventListener('install',event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(ASSETS)).then(()=>self.skipWaiting())));
self.addEventListener('activate',event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(key=>key.startsWith('mala-pronta-web-')&&key!==CACHE).map(key=>caches.delete(key)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',event=>{
 const request=event.request,url=new URL(request.url);if(request.method!=='GET'||url.origin!==self.location.origin)return;
 if(request.mode==='navigate'){event.respondWith(fetch(request).catch(()=>caches.match('./index.html')));return;}
 if(paths.has(url.pathname))event.respondWith(caches.match(request).then(cached=>cached||fetch(request)));
});
