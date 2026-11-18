let dbPromise;
function database(){
 if(!dbPromise)dbPromise=new Promise((resolve,reject)=>{if(!('indexedDB'in globalThis)){reject(new Error('Armazenamento indisponível neste navegador.'));return;}
 const request=indexedDB.open('mala-pronta-documents',1);request.onupgradeneeded=()=>request.result.createObjectStore('photos',{keyPath:'id'});request.onsuccess=()=>{const db=request.result;db.onversionchange=()=>{db.close();dbPromise=null;};resolve(db);};request.onerror=()=>{dbPromise=null;reject(request.error);};request.onblocked=()=>{dbPromise=null;reject(new Error('Feche outras abas do aplicativo e tente novamente.'));};});return dbPromise;
}
async function transaction(mode,action){const db=await database();return new Promise((resolve,reject)=>{const tx=db.transaction('photos',mode);const request=action(tx.objectStore('photos'));let result;request.onsuccess=()=>{result=request.result;};tx.oncomplete=()=>resolve(result);tx.onerror=()=>reject(tx.error||request.error||new Error('Não foi possível salvar.'));tx.onabort=()=>reject(tx.error||new Error('A operação não foi concluída.'));});}
export async function listPhotos(){return (await transaction('readonly',store=>store.getAll())).sort((a,b)=>b.date-a.date);}
export async function removePhoto(id){return transaction('readwrite',store=>store.delete(id));}
export async function importPhoto(file,title){
 if(file.size>25*1024*1024)throw new Error('Escolha uma imagem de até 25 MB.');
 if(!file.type.startsWith('image/'))throw new Error('Escolha uma imagem JPEG, PNG ou WebP.');
 const url=URL.createObjectURL(file);const image=new Image();
 let thumb;
 try{image.src=url;await image.decode();if(!image.naturalWidth||!image.naturalHeight)throw new Error('Imagem inválida.');
 const ratio=Math.min(1,320/Math.max(image.naturalWidth,image.naturalHeight));const canvas=document.createElement('canvas');canvas.width=Math.max(1,Math.round(image.naturalWidth*ratio));canvas.height=Math.max(1,Math.round(image.naturalHeight*ratio));canvas.getContext('2d').drawImage(image,0,0,canvas.width,canvas.height);thumb=await new Promise(resolve=>canvas.toBlob(resolve,'image/jpeg',.8));
 }catch(error){throw new Error('Não foi possível ler a imagem. Tente uma foto JPEG, PNG ou WebP.');}finally{image.src='';URL.revokeObjectURL(url);}
 const photo={id:crypto.randomUUID(),title,date:Date.now(),blob:file,thumb:thumb||file,size:file.size};await transaction('readwrite',store=>store.put(photo));return photo;
}
