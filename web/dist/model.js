import {categories,labels} from './data.js';
export {categories};
export const stays=['Alojamento universitário','Hotel','Casa alugada','Camping'];
export const transports=['Ônibus / excursão','Carro','Avião'];
export const activities=['Festas + jogos + passeios','Principalmente festas','Festas + praia','Jogos + competições'];
export const defaultState=()=>({name:'Minha viagem universitária',trip:{days:3,stay:0,transport:0,activity:0,personalized:false},checked:[],custom:[]});
export function restoreState(raw){
 const fallback=defaultState();if(!raw||typeof raw!=='object')return fallback;
 const t=raw.trip||{};const number=(value,min,max,def)=>Number.isInteger(value)&&value>=min&&value<=max?value:def;
 return {ownList:raw.ownList===true,name:typeof raw.name==='string'&&raw.name.trim()?raw.name.slice(0,60):fallback.name,trip:{days:number(t.days,1,60,3),stay:number(t.stay,0,3,0),transport:number(t.transport,0,2,0),activity:number(t.activity,0,3,0),personalized:t.personalized===true},checked:Array.isArray(raw.checked)?[...new Set(raw.checked.filter(x=>typeof x==='string'))]:[],custom:Array.isArray(raw.custom)?raw.custom.filter(x=>x&&typeof x.id==='string'&&x.id.startsWith('custom-')&&typeof x.label==='string'&&x.label.trim()).map(x=>({id:x.id,label:x.label.slice(0,120),category:number(x.category,0,7,7)})).filter((x,i,all)=>all.findIndex(y=>y.id===x.id)===i):[]};
}
export function generateItems(state){
 if(state.ownList)return state.custom;
 const t=state.trip,all=[];
 labels.forEach((group,c)=>group.forEach((original,i)=>{if(t.personalized&&t.stay===1&&c===5&&i<=4)return;
 let label=original;if(t.personalized&&c===1){if(i===0)label=`Camisetas • ${t.days} unidades`;if(i===3)label=`Roupas íntimas • ${t.days+2} unidades`;if(i===4)label=`Meias • ${t.days+2} pares`;if(i===1)label=`Shorts e bermudas • ${Math.max(2,Math.ceil(t.days/2))} unidades`;}
 all.push({id:`${c}-${i}`,label,category:c});}));
 const add=(id,label,category)=>all.push({id,label,category});
 if(t.personalized){if(t.stay===3){add('camp-tent','Barraca e lona de proteção',5);add('camp-light','Lanterna e pilhas',5);add('camp-sleep','Saco de dormir',5);}
 if(t.transport===0)add('bus-change','Troca de roupa na mochila de mão',7);if(t.transport===1)add('car-doc','Documentos do veículo e CNH do motorista',0);if(t.transport===2){add('air-ticket','Passagem e cartão de embarque',0);add('air-bag','Conferir limites de bagagem da companhia',7);}
 if(t.activity===2){add('beach-towel','Canga ou toalha de praia',6);add('beach-bag','Sacola para roupa de banho molhada',7);}if(t.activity===3){add('sport-uniform','Uniforme e equipamento da modalidade',1);add('sport-shoe','Tênis específico para competição',1);}}
 return all.concat(state.custom);
}
