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
 const cleanName=p=>{const name=String(p.name||'').replace(/\s+(4|5)L\s*$/i,'').trim();return /^pine\s*oil$/i.test(name)?'Disinfectant':name;};
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
 window.dropsPriceListShare=async function(){
  try{
   if(!window.jspdf?.jsPDF)throw new Error('PDF generator unavailable');
   const doc=new window.jspdf.jsPDF({unit:'mm',format:'a4'});
   let y=18;const margin=14;
   const line=(value,size=10,bold=false)=>{doc.setFont('helvetica',bold?'bold':'normal');doc.setFontSize(size);doc.text(String(value),margin,y);y+=size*.48+3;};
   const nextPage=()=>{if(y>272){doc.addPage();y=18;}};
   const centered=(value,size=10,bold=false)=>{doc.setFont('helvetica',bold?'bold':'normal');doc.setFontSize(size);doc.text(String(value),105,y,{align:'center'});y+=size*.48+3;};
   doc.setFillColor(17,125,186);doc.circle(33,27,17,'F');doc.addImage('data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABkRExYTEBkWFBYcGxkeJT4pJSIiJUw3Oi0+WlBfXllQV1ZkcJB6ZGqIbFZXfap+iJSZoaKhYXiwva+cu5CeoZr/2wBDARscHCUhJUkpKUmaZ1dnmpqampqampqampqampqampqampqampqampqampqampqampr/wAARCABAAEADASIAAhEBAxEB/8QAGgAAAgMBAQAAAAAAAAAAAAAABAUCAwYAAf/EADUQAAIBAwIEAwUGBwEAAAAAAAECAwAEEQUSEyExUUFhcRQiQpHwJEOBobHRFSMyM1PB8VL/xAAYAQADAQEAAAAAAAAAAAAAAAACAwQBAP/EAB4RAAICAgMBAQAAAAAAAAAAAAECABEDIRIxQSJR/9oADAMBAAIRAxEAPwDUVTdXUNpEZJ3CL59T6V5e3cdlbtNKeQ6Due1KrSybUrgXt8CV+6iPQDuf2o1XVnqCT4JMajfX5+wW/Di/yy+P1+NTaxvNhe51IoBzO0YApm7xwqoYgZ5AAcz6CkmvXW+K2lgbiQrL/MA7+AP50a/RoChAYCt9yLyPbMB/EpVz0MkR2n8aNjvLqDb7UgeM9JU5ilMMx1Sfa53Fv0p5pMRj09EfmMnGe2a3PiAWwdxWNizV1C0dZFDKcg1KgbkvZLxYhmIf1L28xRcEyTxLIhyDUqsTpu5SCLqJb0HUNaWAjMNuMsPAt9Y+RpwHWGENMyr3PhSq1gEk0juSGllZhg4PI0v1uWeFt8xjeY/CfeVB2A6VUUs8YpX9hGr6rp9xtj3T74zlZIgOXzNdp6JKmY5ePbSnhuSuHQ+GR6451ZqtlCujRNMiCdQuXChSTjn0pfoN7HYQy8SOV+O4CBBnoD+9EpIX5nMAW33J2eyxSW4k5B2KqOm7vj96Mhuri6BCx4BGAzDAHoKpubtFHtEenrIIgqtxGyEz8Kj/AHXk+qTuJpLDgwxRIrH3BnJ8P1+VIz4nzH8EFVr2PbeJorFUnbJVTuLUDpjey38tnnMbjfF6Uqub25e5tpL8zRwSqpUwtgZ5ZyPH0prfjhalZyjAw+zkMcvChZOIENtUfyeWsh9pePPOKYrjxIP0aQTXcd5rIeZwsHFySf8AyP8AlONVX2PU+OTthukMZfGdj45H6866w0bT0IB3SyEZDuMj8B0+eaoLasTAoXUE1C5fWJl2uILJD/dk5A98dz5CrLHgQagZRvaCNCkIRCSPMnHXr86d21vaq25UUyYzlubY+u1F5FJLN0NQ69mZtrDdG0M11utjJxCgQhn5dCfCp6fpkREk+ojYWfcIlf3cDpkDrWiIVuRAPqKDu1s4I98sSnngLjqaUz5h6JtAQC1tLF70yW8LsFbcWdiVU+Q71fcobieBgORmB6c+VBJfT3M4hgxHChyQo/LNF6RmWZ3Dboo/dVu58qwMWXuzFE8jUY3lrFeWzwTDKsPke9Z8JLZSG2uH2P8Adyke7IPPsfr101V3EEVzEY5kDqfA09H4xjpyEUQX7w+5LHgDkCvw+WO1T9tl4YbhvIn+RMH5gdKrm0aeE/ZJVkjHSOXw9DVJgukfc1lKrj4o2yP0p94zuTkONGc8t3cFkFwoHXamVI/3S+We43xxpI8kgLBcncQD602IuZ9oaxkLDozHbj8qIj06aRt0zLEvisY5n1P/AGosuMXam4S8j5A4LCRYVtYhh3GZX8FHanlvAltCsUYwqj51KONYkCoMAVKsxpxG49Vrc//Z','JPEG',17,11,32,32);doc.setFont('helvetica','bold');doc.setFontSize(22);doc.text('DROPS DETERGENTS',121,24,{align:'center'});doc.setFont('helvetica','normal');doc.setFontSize(11);doc.text('Tel: '+phone,121,32,{align:'center'});y=47;y+=2;centered(label+' Price List',13,true);
   const issued=new Date();const expires=new Date(issued.getTime());expires.setDate(expires.getDate()+7);
   const dateText=d=>String(d.getDate()).padStart(2,'0')+'/'+String(d.getMonth()+1).padStart(2,'0')+'/'+d.getFullYear();
   centered('Date: '+dateText(issued)+'    Valid until: '+dateText(expires),10);
   const customerName=customer();
   if(customerName){y+=3;line('Prepared for: '+customerName.replace(/[^\x20-\x7E]/g,' '),11,true);}
   centered('All prices in USD',9);centered('Prices are subject to change without prior notice.',9);y+=4;
   doc.setFillColor(235,241,249);doc.rect(margin,y-5,182,9,'F');doc.setFont('helvetica','bold');doc.setFontSize(10);
   doc.text('Product',margin+3,y);doc.text('4 L',140,y,{align:'right'});doc.text('5 L',190,y,{align:'right'});y+=8;
   doc.setFont('helvetica','normal');
   for(const e of entries){nextPage();doc.setFontSize(9);doc.text(String(e.name).replace(/[^\x20-\x7E]/g,' ').slice(0,37),margin+3,y);doc.text(e.p4?fmt(e.p4.usd):'-',140,y,{align:'right'});doc.text(e.p5?fmt(e.p5.usd):'-',190,y,{align:'right'});y+=8;doc.setDrawColor(225);doc.line(margin,y-4,196,y-4);}
   if(extras.length){y+=7;nextPage();line('Special orders - 10 L / 20 L',12,true);for(const e of extras){nextPage();line(String(e.name).replace(/[^\x20-\x7E]/g,' ')+' - '+e.size+' L - '+fmt(Number(e.usd)||0),9);}}
   const base64=doc.output('datauristring').split(',')[1];
   const fs=window.Capacitor?.Plugins?.Filesystem,share=window.Capacitor?.Plugins?.Share;
   if(!fs||!share)throw new Error('Android PDF sharing unavailable');
   const filename='Drops-'+label+'-Price-List.pdf';
   const saved=await fs.writeFile({path:filename,data:base64,directory:'CACHE',recursive:true});
   await share.share({title:heading,text:'Drops Detergents '+label+' Price List',files:[saved.uri],dialogTitle:'Share price list PDF'});
  }catch(e){if(String(e?.message||'').includes('cancel'))return;alert('Could not share PDF: '+(e?.message||e));}
 };
 window.dropsPriceListAdd=function(){const name=document.getElementById('dropsExtraName').value.trim(),size=Number(document.getElementById('dropsExtraSize').value),usd=Number(document.getElementById('dropsExtraUsd').value);if(!name||![10,20].includes(size)||!Number.isFinite(usd)||usd<0)return alert('Enter product, 10/20 L, and valid USD price');const d=data();(d[mode]||(d[mode]=[])).push({name,size,usd});saveData(d);dropsCustomerPriceList(mode);};
 window.dropsPriceListDelete=function(i){if(!confirm('Remove this special price-list entry?'))return;const d=data();(d[mode]||[]).splice(i,1);saveData(d);dropsCustomerPriceList(mode);};
 const extraRows=extras.map((e,i)=>'<div class="row"><span>'+esc(e.name)+' — '+Number(e.size)+' L — $'+Number(e.usd).toFixed(2)+'</span><button onclick="dropsPriceListDelete('+i+')">Remove</button></div>').join('');
 document.getElementById('app').innerHTML='<div class="title">'+heading+'</div><div class="panel"><b>DROPS DETERGENTS</b><div class="muted">Tel: '+esc(phone)+'</div><label for="dropsCustomerName">Customer Name (optional)</label><input id="dropsCustomerName" placeholder="Customer or company name" autocomplete="off"><div class="muted">Automatically reads '+label.toLowerCase()+' prices from Selling Prices. No gallon pictures. USD/LBP '+rate.toLocaleString('en-US')+'.</div><div class="row"><button onclick="dropsPriceListPrint()">🖨 Print</button><button onclick="dropsPriceListShare()">📄 Share PDF</button></div></div><div class="panel" style="overflow-x:auto">'+table+'</div><div class="panel"><b>Special orders — 10 L / 20 L</b><div class="muted">Manual list entries only; do not change product stock or master prices.</div><input id="dropsExtraName" placeholder="Product name"><select id="dropsExtraSize"><option value="10">10 L</option><option value="20">20 L</option></select><input id="dropsExtraUsd" type="number" min="0" step="0.01" placeholder="USD price"><button onclick="dropsPriceListAdd()">Add to '+label+' list</button>'+extraRows+'</div>';
};
window.dropsOpenPriceList=function(mode){if(typeof go==='function')go('settings');dropsCustomerPriceList(mode);};
document.addEventListener('DOMContentLoaded',function(){const btns=document.querySelectorAll('.bo-card');const card=[...btns].find(b=>b.textContent.includes('Selling Prices'));if(card&&!document.getElementById('dropsWholesalePriceListLink')){const w=document.createElement('button'),r=document.createElement('button');w.className=r.className='bo-card';w.id='dropsWholesalePriceListLink';r.id='dropsRetailPriceListLink';w.innerHTML='<span>📦</span><b>Wholesale Price List</b>';r.innerHTML='<span>🏷️</span><b>Retail Price List</b>';w.onclick=()=>dropsOpenPriceList('wholesale');r.onclick=()=>dropsOpenPriceList('retail');card.after(w,r);}});
})();