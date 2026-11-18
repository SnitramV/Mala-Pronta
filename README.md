# Mala Pronta • Inter 2026

Checklist de viagem universitária para Android e uma versão web instalável (PWA) para iPhone e Android. Inclui os 78 itens originais, personalização por viagem, documentos offline, identidade Lieu/Inter 2026 e mascotes.

## Publicar na Vercel

1. Na Vercel, escolha **Add New → Project** e importe **SnitramV/Mala-Pronta**.
2. Mantenha **Root Directory** na raiz do repositório (`./`) e **Framework Preset: Other**.
3. Clique em **Deploy**. O arquivo `vercel.json` configura o restante:
   - Build Command: `npm run build`
   - Output Directory: `public`
   - Install Command: vazio (o build não precisa de dependências)

Não são necessárias variáveis de ambiente, banco de dados ou chaves de API. A hospedagem publica apenas a pasta gerada `public/`. O código Android fica no repositório, mas não é publicado como parte do site.

O build valida os arquivos e gera automaticamente uma nova versão do cache offline quando o conteúdo muda. `sw.js` é servido sem cache HTTP para permitir atualizações.

Documentação da configuração: https://vercel.com/docs/project-configuration/vercel-json

## Instalar no celular

- **iPhone:** abra o endereço da Vercel no Safari → Compartilhar → Adicionar à Tela de Início. Se aparecer, ative Abrir como App da Web.
- **Android:** abra no Chrome → Instalar aplicativo / Adicionar à tela inicial. Também existe um botão Instalar no app.
- Abra online uma primeira vez e espere **Offline pronto** antes de viajar.

## Desenvolvimento e testes

Requer Node.js 22 ou superior. O build estático não depende de `npm install`:

```sh
npm run build
python -m http.server 4173 --directory public
```

Abra http://localhost:4173. Para os testes automatizados, instale as dependências de desenvolvimento:

```sh
npm ci
npm test
```

Os testes cobrem checklist, 48 combinações de viagem, persistência, filtros, itens extras, proteção contra HTML em textos, armazenamento de fotos, manifesto, ícones e fallback offline. Usam DOM, IndexedDB e decodificação de imagens simulados; não substituem a validação visual e a instalação em um iPhone real.

## Estrutura

- `web/dist/`: código e imagens da versão web, prontos para servir.
- `web/tests/`: testes automatizados da versão web.
- `scripts/build-web.mjs`: validação e geração da pasta `public/`.
- `vercel.json`: configuração da hospedagem.
- `app/`: código Android nativo em Java.
- `tests/`, `build.ps1`, `test.ps1`, `test-device-build.ps1`: compilação e testes Android.
- `docs/ANDROID.md`: instruções do APK.
- `VALIDACAO.md`: histórico de validação das versões Android e web.

## Dados e privacidade

Os checklists ficam em localStorage e as fotos em IndexedDB, apenas no aparelho/navegador da pessoa. As fotos não são enviadas ao GitHub, à Vercel ou a um servidor do aplicativo. Cada navegador, aparelho e domínio tem armazenamento próprio. Os dados do APK ou do endereço anterior não são transferidos automaticamente para o novo endereço da Vercel.

Limpar os dados do site ou do navegador pode apagar as marcações e fotos; o sistema também pode liberar espaço de sites. Mantenha os arquivos originais dos documentos e ingressos.

Arquivos de assinatura Android, credenciais, fotos pessoais e saídas de compilação não são versionados. Para gerar atualizações Android com a mesma assinatura dos APKs já distribuídos, preserve sua chave de assinatura local fora do repositório.
