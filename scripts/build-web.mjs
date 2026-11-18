import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
const root=fileURLToPath(new URL('../',import.meta.url));
const source=path.join(root,'web','dist');
const output=path.join(root,'public');
const sw=fs.readFileSync(path.join(source,'sw.js'),'utf8');
const match=sw.match(/const ASSETS=(\[[\s\S]*?\]);/);
assert(match,'Lista de arquivos offline não encontrada.');
const assets=JSON.parse(match[1]);
const digest=createHash('sha256');
for(const asset of assets){
 assert(asset.startsWith('./')&&!asset.includes('..'),'Caminho de asset inválido.');
 const relative=asset==='./'?'index.html':asset.slice(2);
 const filename=path.resolve(source,relative);
 assert(filename.startsWith(source+path.sep),'Asset fora da pasta web.');
 assert(fs.statSync(filename).isFile(),`Arquivo obrigatório ausente: ${relative}`);
 digest.update(relative).update(fs.readFileSync(filename));
}
const manifest=JSON.parse(fs.readFileSync(path.join(source,'manifest.webmanifest'),'utf8').replace(/^\uFEFF/,''));
assert.equal(manifest.display,'standalone');
assert.equal(manifest.start_url,'./');
for(const icon of manifest.icons){
 const bytes=fs.readFileSync(path.resolve(source,icon.src));
 assert.equal(bytes.readUInt32BE(16),Number(icon.sizes.split('x')[0]),'Tamanho incorreto do ícone.');
}
const version=digest.digest('hex').slice(0,16);
const generated=sw.replace(/const CACHE='[^']+';/,`const CACHE='mala-pronta-web-${version}';`);
assert.notEqual(generated,sw,'Versão automática do cache não foi aplicada.');
fs.mkdirSync(output,{recursive:true});
fs.cpSync(source,output,{recursive:true});
fs.writeFileSync(path.join(output,'sw.js'),generated);
console.log(`PWA pronta em public/: ${assets.length} entradas offline, cache ${version}.`);
