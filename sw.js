const C='drops-pos-v151';
const BACKUP_CACHE='drops-pos-export-temp-v1';

self.addEventListener('install',e=>{self.skipWaiting()});
self.addEventListener('activate',e=>e.waitUntil(Promise.all([
 caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==C&&k!==BACKUP_CACHE).map(k=>caches.delete(k)))),
 self.clients.claim()
])));
self.addEventListener('fetch',e=>{
 const u=new URL(e.request.url);
 if(u.pathname.endsWith('/__drops_backup_download__')){
  e.respondWith((async()=>{
   const c=await caches.open(BACKUP_CACHE);
   const stored=await c.match(new Request(new URL('./__drops_backup_staged__',self.registration.scope).href));
   if(!stored)return new Response('Backup not staged. Return to Drops POS and retry.',{status:404,headers:{'Content-Type':'text/plain'}});
   const body=await stored.blob();
   const filename=(stored.headers.get('X-Drops-Filename')||'DropsPOS-backup.json').replace(/[^a-zA-Z0-9._-]/g,'');
   return new Response(body,{status:200,headers:{'Content-Type':'application/octet-stream','Content-Disposition':'attachment; filename="'+filename+'"','Cache-Control':'no-store','Content-Length':String(body.size)}});
  })());
  return;
 }
 if(e.request.mode==='navigate'||e.request.destination==='document'){
  e.respondWith(fetch(e.request,{cache:'no-store'}).catch(()=>caches.match(e.request)));
  return;
 }
 e.respondWith(fetch(e.request,{cache:'no-store'}).then(r=>{
  if(r&&r.ok){const x=r.clone();caches.open(C).then(c=>c.put(e.request,x))}
  return r;
 }).catch(()=>caches.match(e.request)));
});
self.addEventListener('message',e=>{
 if(e.data&&e.data.type==='SKIP_WAITING')self.skipWaiting();
 if(e.data&&e.data.type==='STAGE_BACKUP'){
  const port=e.ports&&e.ports[0];
  e.waitUntil((async()=>{
   try{
    const json=e.data.json;
    if(typeof json!=='string'||json.length<100||!json.includes('"products"'))throw Error('Invalid backup payload');
    const filename=String(e.data.filename||'DropsPOS-backup.json');
    const c=await caches.open(BACKUP_CACHE);
    await c.put(new Request(new URL('./__drops_backup_staged__',self.registration.scope).href),new Response(json,{headers:{'Content-Type':'application/json','X-Drops-Filename':filename}}));
    if(port)port.postMessage({ok:true});
   }catch(err){if(port)port.postMessage({ok:false,error:String(err)})}
  })());
 }
});
