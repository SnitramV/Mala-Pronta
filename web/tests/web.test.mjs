import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import {pathToFileURL} from 'node:url';
import {Window} from 'happy-dom';
import {indexedDB} from 'fake-indexeddb';
const root=fs.realpathSync(new URL('../dist',import.meta.url));
const {defaultState,restoreState,generateItems}=await import(pathToFileURL(root+'/model.js'));
let state=defaultState();assert.equal(generateItems(state).length,78);assert.equal(restoreState({trip:{days:-5}}).trip.days,3);
for(let stay=0;stay<4;stay++)for(let transport=0;transport<3;transport++)for(let activity=0;activity<4;activity++){
 state={...defaultState(),trip:{personalized:true,days:5,stay,transport,activity}};const all=generateItems(state);assert.equal(new Set(all.map(i=>i.id)).size,all.length);assert.equal(all.find(i=>i.id==='1-3').label,'Roupas íntimas • 7 unidades');if(stay===1)assert(!all.some(i=>i.id==='5-0'));
}
const win=new Window({url:'http://127.0.0.1:4173/'});win.addEventListener('error',e=>console.error('DOMERROR',e.error?.stack||e.message));win.console=console;win.document.write(fs.readFileSync(root+'/index.html','utf8').replace(/<script[\s\S]*?<\/script>/g,''));
win.document.getElementById('dialog').showModal=function(){this.open=true;};win.document.getElementById('dialog').close=function(value=''){this.open=false;this.returnValue=value;this.dispatchEvent(new win.Event('close'));};
const photos=await import(pathToFileURL(root+'/photos.js'));
Object.assign(globalThis,{indexedDB,document:win.document,Image:class{naturalWidth=180;naturalHeight=180;async decode(){}}});
win.HTMLCanvasElement.prototype.getContext=()=>({drawImage(){}});win.HTMLCanvasElement.prototype.toBlob=function(callback){callback(new Blob(['thumbnail'],{type:'image/jpeg'}));};
Object.assign(win,{categories:(await import(pathToFileURL(root+'/model.js'))).categories,stays:['Alojamento universitário','Hotel','Casa alugada','Camping'],transports:['Ônibus / excursão','Carro','Avião'],activities:['Festas + jogos + passeios','Principalmente festas','Festas + praia','Jogos + competições'],defaultState,restoreState,generateItems,...photos});
const app=fs.readFileSync(root+'/app.js','utf8').replace(/^import .*;\r?\n/gm,'');win.eval(app);
const doc=win.document;assert.match(doc.querySelector('#progress-count').textContent,/0 \/ 78/);assert.equal(doc.querySelectorAll('[data-item]').length,78);
const first=doc.querySelector('[data-item="0-0"]');first.checked=true;first.dispatchEvent(new win.Event('change'));assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);assert(JSON.parse(win.localStorage.getItem('mala-pronta-web-v1')).checked.includes('0-0'));
doc.querySelector('[data-filter="pending"]').click();assert.equal(doc.querySelector('[data-item="0-0"]'),null);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);
doc.querySelector('#add-item').click();const form=doc.querySelector('#custom-form');form.elements.label.value='<img src=x onerror=alert(1)>';form.dispatchEvent(new win.Event('submit',{cancelable:true}));assert.equal(doc.querySelectorAll('.check-label img').length,0);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 79/);
win.location.hash='viagem';win.dispatchEvent(new win.HashChangeEvent('hashchange'));const tripForm=doc.querySelector('#trip-form');tripForm.elements.name.value='Inter com a turma';tripForm.elements.days.value='5';tripForm.elements.stay.value='1';tripForm.elements.transport.value='2';tripForm.elements.activity.value='2';tripForm.dispatchEvent(new win.Event('submit',{cancelable:true}));win.dispatchEvent(new win.HashChangeEvent('hashchange'));const stored=JSON.parse(win.localStorage.getItem('mala-pronta-web-v1'));assert.equal(stored.name,'Inter com a turma');assert.equal(stored.trip.days,5);assert.equal(stored.trip.stay,1);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);
const file=new File([fs.readFileSync(root+'/assets/icon-180.png')],'teste.png',{type:'image/png'});const photo=await photos.importPhoto(file,'Ingresso teste');let saved=await photos.listPhotos();assert.equal(saved.length,1);assert.deepEqual(Buffer.from(await saved[0].blob.arrayBuffer()),Buffer.from(await file.arrayBuffer()));assert(saved[0].thumb.size>0);
await assert.rejects(()=>photos.importPhoto(new File(['x'],'x.txt',{type:'text/plain'}),'Inválido'));await assert.rejects(()=>photos.importPhoto(new File([new Uint8Array(25*1024*1024+1)],'grande.png',{type:'image/png'}),'Grande'));
await photos.removePhoto(photo.id);assert.equal((await photos.listPhotos()).length,0);
const handlers={},cacheData=new Map();let offline=false;const scope=new URL('http://127.0.0.1:4173/sw.js');const resolve=p=>new URL(typeof p==='string'?p:p.url,scope).href;
const caches={open:async()=>({addAll:async paths=>{for(const p of paths){const url=new URL(p,scope);const local=url.pathname==='/'?'index.html':url.pathname.slice(1);assert(fs.existsSync(root+'/'+local),`Falta ${local}`);cacheData.set(resolve(p),local);}}}),keys:async()=>['mala-pronta-web-v1'],delete:async()=>true,match:async p=>cacheData.get(resolve(p))};
vm.runInNewContext(fs.readFileSync(root+'/sw.js','utf8'),{URL,caches,fetch:async()=>{if(offline)throw new Error('Offline');return 'network';},self:{location:scope,skipWaiting:async()=>{},clients:{claim:async()=>{}},addEventListener:(name,fn)=>handlers[name]=fn}});
let task;handlers.install({waitUntil:p=>task=p});await task;assert(cacheData.size>=20);offline=true;handlers.fetch({request:{method:'GET',url:'http://127.0.0.1:4173/',mode:'navigate'},respondWith:p=>task=p});assert.equal(await task,'index.html');handlers.fetch({request:{method:'GET',url:'http://127.0.0.1:4173/app.js',mode:'cors'},respondWith:p=>task=p});assert.equal(await task,'app.js');
const manifest=JSON.parse(fs.readFileSync(root+'/manifest.webmanifest','utf8').replace(/^\ufeff/,''));assert.equal(manifest.display,'standalone');for(const icon of manifest.icons){const bytes=fs.readFileSync(root+'/'+icon.src.slice(2));assert.equal(bytes.readUInt32BE(16),Number(icon.sizes.split('x')[0]));}
console.log('OK: 78 itens; 48 combinações; marcação e persistência; filtro; item extra sem XSS; formulário da viagem; fotos em IndexedDB, bytes originais, exclusão e limites; manifesto e ícones; cache completo e fallback offline.');
console.log('Nota: DOM e IndexedDB simulados. Não equivale a teste visual, gesto de pinça ou instalação em iPhone real.');
await win.happyDOM.close();
