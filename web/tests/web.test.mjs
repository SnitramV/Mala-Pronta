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
Object.assign(win,{structuredClone});
Object.assign(win,await import(pathToFileURL(root+'/lists.js')));
const app=fs.readFileSync(root+'/app.js','utf8').replace(/^import .*;\r?\n/gm,'');win.eval(app);
const doc=win.document;assert.match(doc.querySelector('#progress-count').textContent,/0 \/ 78/);assert.equal(doc.querySelectorAll('[data-item]').length,78);
const first=doc.querySelector('[data-item="0-0"]');first.checked=true;first.dispatchEvent(new win.Event('change'));assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);assert(JSON.parse(win.localStorage.getItem('mala-pronta-web-v1')).checked.includes('0-0'));
doc.querySelector('[data-filter="pending"]').click();assert.equal(doc.querySelector('[data-item="0-0"]'),null);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);
doc.querySelector('#add-item').click();const form=doc.querySelector('#custom-form');form.elements.label.value='<img src=x onerror=alert(1)>';form.dispatchEvent(new win.Event('submit',{cancelable:true}));assert.equal(doc.querySelectorAll('.check-label img').length,0);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 79/);
win.location.hash='viagem';win.dispatchEvent(new win.HashChangeEvent('hashchange'));const tripForm=doc.querySelector('#trip-form');tripForm.elements.name.value='Inter com a turma';tripForm.elements.days.value='5';tripForm.elements.stay.value='1';tripForm.elements.transport.value='2';tripForm.elements.activity.value='2';tripForm.dispatchEvent(new win.Event('submit',{cancelable:true}));win.dispatchEvent(new win.HashChangeEvent('hashchange'));const stored=JSON.parse(win.localStorage.getItem('mala-pronta-web-v1'));assert.equal(stored.name,'Inter com a turma');assert.equal(stored.trip.days,5);assert.equal(stored.trip.stay,1);assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);
// Multiple independent lists, category selection, progress and sharing a frozen template.
const {encodeTemplate,decodeTemplate,restoreLibrary}=await import(pathToFileURL(root+'/lists.js'));
const beforeLibrary=JSON.parse(win.localStorage.getItem('mala-pronta-lists-v1'));
assert.equal(beforeLibrary.lists.length,1);assert(beforeLibrary.lists[0].state.checked.includes('0-0'));
doc.querySelector('#new-list').click();let newForm=doc.querySelector('#new-list-form');newForm.elements.name.value='Praia 🏖️ & férias';newForm.dispatchEvent(new win.Event('submit',{cancelable:true}));
assert.equal(doc.querySelectorAll('[data-item]').length,0);assert.equal(doc.querySelector('.progress-track').getAttribute('aria-valuenow'),'0');
doc.querySelector('#add-item').click();let customForm=doc.querySelector('#custom-form');customForm.elements.label.value='Protetor & óculos 😎';customForm.elements.category.value='2';customForm.dispatchEvent(new win.Event('submit',{cancelable:true}));
assert(doc.querySelector('[data-category="2"]'));assert.equal(doc.querySelectorAll('[data-item]').length,1);
let checkbox=doc.querySelector('[data-item]');checkbox.checked=true;checkbox.dispatchEvent(new win.Event('change'));
let currentLibrary=JSON.parse(win.localStorage.getItem('mala-pronta-lists-v1'));assert.equal(currentLibrary.lists.length,2);assert.equal(currentLibrary.lists[1].state.checked.length,1);
const restored=restoreLibrary(currentLibrary);assert.equal(generateItems(restored.lists[1].state)[0].category,2);assert.equal(generateItems(restored.lists[1].state).length,1);
doc.querySelector('#share').click();doc.querySelector('#share-link').click();const shareURL=doc.querySelector('#list-url').value;assert(shareURL.startsWith('http://127.0.0.1:4173/#lista='));
const incoming=decodeTemplate(new URL(shareURL).hash.slice(7));assert.equal(incoming.name,'Praia 🏖️ & férias');assert.equal(incoming.custom[0].label,'Protetor & óculos 😎');assert.equal(incoming.custom[0].category,2);assert.deepEqual(incoming.checked,[]);assert(incoming.ownList);
assert.equal(decodeTemplate(encodeTemplate(stored)).custom.length,78);
for(const bad of ['', '!', 'a'.repeat(100001),Buffer.from(JSON.stringify({v:2})).toString('base64url'),Buffer.from(JSON.stringify({v:1,name:'Inválida',items:[{label:'Item',category:99}]})).toString('base64url')])assert.throws(()=>decodeTemplate(bad));
assert.throws(()=>encodeTemplate({...defaultState(),ownList:true,custom:Array.from({length:201},()=>({label:'Item',category:0}))}));
// Viewing a link preserves the library; importing adds a clean, editable independent copy.
win.location.href=shareURL;win.dispatchEvent(new win.HashChangeEvent('hashchange'));assert.equal(doc.querySelector('#save-shared').textContent,'Salvar nos meus checklists');assert.equal(win.localStorage.getItem('mala-pronta-lists-v1'),JSON.stringify(currentLibrary));
doc.querySelector('#save-shared').click();win.dispatchEvent(new win.HashChangeEvent('hashchange'));currentLibrary=JSON.parse(win.localStorage.getItem('mala-pronta-lists-v1'));assert.equal(currentLibrary.lists.length,3);assert.equal(currentLibrary.lists[2].state.checked.length,0);assert.equal(currentLibrary.lists[1].state.checked.length,1);
// Changing lists restores each list's own progress, and copying resets it.
doc.querySelector('#my-lists').click();doc.querySelector(`[data-load-list="${currentLibrary.lists[0].id}"]`).click();assert.match(doc.querySelector('#progress-count').textContent,/1 \/ 78/);
doc.querySelector('#new-list').click();newForm=doc.querySelector('#new-list-form');newForm.elements.name.value='Cópia da viagem';newForm.elements.source.value='copy';newForm.dispatchEvent(new win.Event('submit',{cancelable:true}));assert.match(doc.querySelector('#progress-count').textContent,/0 \/ 78/);
// Shared labels are text, including markup; invalid links never overwrite saved lists.
const hostile={...defaultState(),name:'<img src=x>',ownList:true,custom:[{id:'custom-x',label:'<img src=x onerror=alert(1)>',category:0}]};win.location.hash='lista='+encodeTemplate(hostile);win.dispatchEvent(new win.HashChangeEvent('hashchange'));assert.equal(doc.querySelectorAll('.shared-preview img').length,0);
const stable=win.localStorage.getItem('mala-pronta-lists-v1');win.location.hash='lista=invalid';win.dispatchEvent(new win.HashChangeEvent('hashchange'));assert.match(doc.querySelector('h1').textContent,/Não foi possível abrir/);assert.equal(win.localStorage.getItem('mala-pronta-lists-v1'),stable);
win.location.hash='checklist';win.dispatchEvent(new win.HashChangeEvent('hashchange'));
// A reload uses the library as the source of truth, preserving custom categories and progress.
const reload=new Window({url:'http://127.0.0.1:4173/'});reload.document.write(fs.readFileSync(root+'/index.html','utf8').replace(/<script[\s\S]*?<\/script>/g,''));reload.localStorage.setItem('mala-pronta-lists-v1',stable);Object.assign(reload,{structuredClone,categories:win.categories,stays:win.stays,transports:win.transports,activities:win.activities,defaultState,restoreState,generateItems,encodeTemplate,decodeTemplate,restoreLibrary,...photos});reload.eval(app);assert.match(reload.document.querySelector('#progress-count').textContent,/0 \/ 78/);await reload.happyDOM.close();
console.log('OK: listas do zero e cópia; categorias pr?prias; biblioteca e recarga; progresso independente; link Unicode; importação sem sobrescrever; limites e links inválidos; proteção contra HTML compartilhado.');
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
