const C='drops-pos-v109';
self.addEventListener('install',e=>{self.skipWaiting();e.waitUntil(caches.open(C).then(c=>c.addAll(['./manifest.webmanifest'])))});
self.addEventListener('activate',e=>e.waitUntil(Promise.all([caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==C).map(k=>caches.delete(k)))),self.clients.claim()])));
self.addEventListener('fetch',e=>{
 if(e.request.mode==='navigate'||e.request.destination==='document'){
  e.respondWith(fetch(e.request,{cache:'no-store'}).catch(()=>caches.match('./')||caches.match('index.html')));return;
 }
 e.respondWith(fetch(e.request).then(r=>{if(r&&r.ok){const x=r.clone();caches.open(C).then(c=>c.put(e.request,x))}return r}).catch(()=>caches.match(e.request)));
});