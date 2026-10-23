/* Drops customer price lists: separate read-only wholesale and retail views. */
(function(){
function esc(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]));}
function data(){return db.specialPriceLists||{}}
function saveData(d){db.specialPriceLists=d;save();}
window.dropsCustomerPriceList=function(mode){
 const wholesale=mode==='wholesale',label=wholesale?'Wholesale':'Retail';
 const rate=Number(db.settings?.usdLbp)||90000;
 const products=(typeof posSellProducts==='function'?posSellProducts():db.products||[]).filter(p=>!p.formulaMaster&&[4,5].includes(Number(p.size))).sort((a,b)=>String(a.name).localeCompare(String(b.name)));
 const extras=(data()[mode]||[]);
 const price=p=>Number(wholesale?p.wholesalePrice:p.price)||0;
 const row=(name,size,usd)=>'<tr><td>'+esc(name)+'</td><td>'+size+' L</td><td>$'+usd.toFixed(2)+'</td></tr>';
 const cleanName=p=>String(p.name||'').replace(/\s+(4|5)L\s*$/i,'').trim();
 const grouped=new Map();
 products.forEach(p=>{const name=cleanName(p),key=name.toLocaleLowerCase('en');if(!grouped.has(key))grouped.set(key,{name,items:[]});grouped.get(key).items.push({name,size:Number(p.size),usd:price(p)});});
 const groups=[...grouped.values()].sort((a,b)=>{const both=x=>x.items.some(i=>i.size===4)&&x.items.some(i=>i.size===5);return Number(both(b))-Number(both(a))||a.name.localeCompare(b.name);});
 const entries=groups.map(g=>({name:g.name,p4:g.items.find(i=>i.size===4),p5:g.items.find(i=>i.size===5)}));
 const fmt=usd=>'$'+usd.toFixed(2);
 const rows=entries.map(e=>'<tr><td>'+esc(e.name)+'</td><td>'+(e.p4?fmt(e.p4.usd):'—')+'</td><td>'+(e.p5?fmt(e.p5.usd):'—')+'</td></tr>').join('');
 const table='<table style="width:100%;border-collapse:collapse;font-size:13px;text-align:left;table-layout:fixed"><thead><tr><th style="width:34%">Product</th><th>4 L · USD</th><th>5 L · USD</th></tr></thead><tbody>'+rows+'</tbody></table>';
 const specialTable=extras.length?'<h3>Special orders — 10 L / 20 L</h3><table style="width:100%;border-collapse:collapse"><thead><tr><th>Product</th><th>Size</th><th>USD</th></tr></thead><tbody>'+extras.map(e=>row(e.name,Number(e.size),Number(e.usd)||0)).join('')+'</tbody></table>':'';
 const heading='DROPS DETERGENTS — '+label+' Price List';
 const phone='00961 3 880 271';
 const customer=()=>String(document.getElementById('dropsCustomerName')?.value||'').trim();
 const customerHtml=()=>customer()?'<p><b>Prepared for: '+esc(customer())+'</b></p>':'';
 const contact=phone?' • Tel: '+esc(phone):'';
 window.dropsPriceListPrint=function(){const w=window.open('','_blank');if(!w)return alert('Allow print popup');w.document.write('<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:Arial;padding:20px}th,td{padding:8px;border-bottom:1px solid #ddd}</style></head><body><h2>'+heading+'</h2><p>USD/LBP '+rate+'</p>'+table+specialTable+'</body></html>');w.document.close();w.focus();w.print();};
 window.dropsPriceListShare=async function(){const txt=heading+'\nTel: '+phone+(customer()?'\nPrepared for: '+customer():'')+'\nExchange rate: USD/LBP '+rate+'\n'+entries.map(e=>e.name+' | 4 L: '+(e.p4?fmt(e.p4.usd):'—')+' | 5 L: '+(e.p5?fmt(e.p5.usd):'—')).join('\n')+(extras.length?'\nSpecial orders — 10 L / 20 L\n'+extras.map(e=>e.name+' — '+e.size+' L — '+fmt(Number(e.usd)||0)).join('\n'):'');try{if(navigator.share)await navigator.share({title:heading,text:txt});else if(navigator.clipboard){await navigator.clipboard.writeText(txt);alert('Copied')}else alert(txt)}catch(e){if(e.name!=='AbortError')alert('Sharing unavailable')}};
 window.dropsPriceListAdd=function(){const name=document.getElementById('dropsExtraName').value.trim(),size=Number(document.getElementById('dropsExtraSize').value),usd=Number(document.getElementById('dropsExtraUsd').value);if(!name||![10,20].includes(size)||!Number.isFinite(usd)||usd<0)return alert('Enter product, 10/20 L, and valid USD price');const d=data();(d[mode]||(d[mode]=[])).push({name,size,usd});saveData(d);dropsCustomerPriceList(mode);};
 window.dropsPriceListDelete=function(i){if(!confirm('Remove this special price-list entry?'))return;const d=data();(d[mode]||[]).splice(i,1);saveData(d);dropsCustomerPriceList(mode);};
 const extraRows=extras.map((e,i)=>'<div class="row"><span>'+esc(e.name)+' — '+Number(e.size)+' L — $'+Number(e.usd).toFixed(2)+'</span><button onclick="dropsPriceListDelete('+i+')">Remove</button></div>').join('');
 document.getElementById('app').innerHTML='<div class="title">'+heading+'</div><div class="panel"><b>DROPS DETERGENTS</b><div class="muted">Tel: '+esc(phone)+'</div><label for="dropsCustomerName">Customer Name (optional)</label><input id="dropsCustomerName" placeholder="Customer or company name" autocomplete="off"><div class="muted">Automatically reads '+label.toLowerCase()+' prices from Selling Prices. No gallon pictures. USD/LBP '+rate.toLocaleString('en-US')+'.</div><div class="row"><button onclick="dropsPriceListPrint()">🖨 Print</button><button onclick="dropsPriceListShare()">📤 Share</button></div></div><div class="panel" style="overflow-x:auto">'+table+'</div><div class="panel"><b>Special orders — 10 L / 20 L</b><div class="muted">Manual list entries only; do not change product stock or master prices.</div><input id="dropsExtraName" placeholder="Product name"><select id="dropsExtraSize"><option value="10">10 L</option><option value="20">20 L</option></select><input id="dropsExtraUsd" type="number" min="0" step="0.01" placeholder="USD price"><button onclick="dropsPriceListAdd()">Add to '+label+' list</button>'+extraRows+'</div>';
};
window.dropsOpenPriceList=function(mode){if(typeof go==='function')go('settings');dropsCustomerPriceList(mode);};
document.addEventListener('DOMContentLoaded',function(){const btns=document.querySelectorAll('.bo-card');const card=[...btns].find(b=>b.textContent.includes('Selling Prices'));if(card&&!document.getElementById('dropsWholesalePriceListLink')){const w=document.createElement('button'),r=document.createElement('button');w.className=r.className='bo-card';w.id='dropsWholesalePriceListLink';r.id='dropsRetailPriceListLink';w.innerHTML='<span>📦</span><b>Wholesale Price List</b>';r.innerHTML='<span>🏷️</span><b>Retail Price List</b>';w.onclick=()=>dropsOpenPriceList('wholesale');r.onclick=()=>dropsOpenPriceList('retail');card.after(w,r);}});
})()
