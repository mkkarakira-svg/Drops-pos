const C='drops-pos-v149';
self.addEventListener('install',e=>{self.skipWaiting()});
self.addEventListener('activate',e=>e.waitUntil(Promise.all([
 caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==C).map(k=>caches.delete(k)))),
 self.clients.claim()
])));
self.addEventListener('fetch',e=>{
 const u=new URL(e.request.url);
 if(e.request.mode==='navigate'||e.request.destination==='document'){
  e.respondWith(fetch(e.request,{cache:'no-store'}).catch(()=>caches.match(e.request)));
  return;
 }
 e.respondWith(fetch(e.request,{cache:'no-store'}).then(r=>{
  if(r&&r.ok){const x=r.clone();caches.open(C).then(c=>c.put(e.request,x))}
  return r;
 }).catch(()=>caches.match(e.request)));
});
self.addEventListener('message',e=>{if(e.data&&e.data.type==='SKIP_WAITING')self.skipWaiting()});
