import {defaultState,restoreState,generateItems} from './model.js';

// A shared link contains a frozen template, never progress or documents.
export function sharePayload(state){
 const items=generateItems(state).map(({label,category})=>({label,category}));
 return {v:1,name:state.name,items};
}
export function readTemplate(raw){
 if(!raw||raw.v!==1||typeof raw.name!=='string'||!raw.name.trim()||raw.name.length>60||!Array.isArray(raw.items)||raw.items.length>200)throw new Error('Link de checklist inválido ou incompatível.');
 if(raw.items.some(i=>!i||typeof i.label!=='string'||!i.label.trim()||i.label.length>120||!Number.isInteger(i.category)||i.category<0||i.category>7))throw new Error('O link contém itens inválidos.');
 return {...defaultState(),name:raw.name.trim(),ownList:true,custom:raw.items.map((i,n)=>({id:`custom-shared-${n}`,label:i.label,category:i.category}))};
}
export function encodeTemplate(state){
 const data=sharePayload(state);readTemplate(data);
 const bytes=new TextEncoder().encode(JSON.stringify(data));
 return btoa(Array.from(bytes,b=>String.fromCharCode(b)).join('')).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');
}
export function decodeTemplate(encoded){
 if(!encoded||encoded.length>100000||!/^[\w-]+$/.test(encoded))throw new Error('Link de checklist inválido ou incompleto.');
 try{return readTemplate(JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(Uint8Array.from(atob(encoded.replace(/-/g,'+').replace(/_/g,'/')),c=>c.charCodeAt(0)))));}catch{throw new Error('Link de checklist inválido ou incompatível.');}
}
export function restoreLibrary(raw){
 if(!raw||!Array.isArray(raw.lists)||!raw.lists.length)throw new Error('Biblioteca inválida.');
 const ids=new Set();
 const lists=raw.lists.map(entry=>{
  if(!entry||typeof entry.id!=='string'||ids.has(entry.id)||!entry.state||typeof entry.state!=='object')throw new Error('Checklist salvo inválido.');
  ids.add(entry.id);return {id:entry.id,state:restoreState(entry.state)};
 });
 return {activeId:ids.has(raw.activeId)?raw.activeId:lists[0].id,lists};
}
