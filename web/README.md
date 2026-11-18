# Mala Pronta — versão web instalável

Aplicativo web estático (PWA) para Android e iPhone, com identidade visual da Lieu e Inter 2026. Não usa servidor para dados pessoais, conta de usuário, analytics ou envio de fotos. O link público serve apenas os arquivos do aplicativo.

## Instalar

No iPhone: abra o link publicado no Safari, toque em Compartilhar e em Adicionar à Tela de Início. Se aparecer, ative Abrir como App da Web. No Android: abra no Chrome e use Instalar aplicativo ou Adicionar à tela inicial. O botão Instalar explica os passos ou abre o instalador compatível.

Abra online uma primeira vez e espere a indicação Offline pronto. O service worker guarda os arquivos estáticos para as próximas aberturas sem rede. Fotos importadas são armazenadas separadamente em IndexedDB e nunca entram no cache de rede.

## Dados locais

- Checklist, viagem e itens extras: localStorage.
- Fotos originais e miniaturas: IndexedDB, limite de 25 MB por foto. A cópia original não é recomprimida.
- Cada aparelho/navegador tem seus dados. Os dados do APK não são transferidos automaticamente.
- Limpar os dados do site/navegador apaga o conteúdo. O sistema também pode liberar armazenamento de sites. Solicitação de persistência é feita quando o navegador oferece suporte, sem garantia de concessão. Mantenha os originais dos documentos e ingressos.
- A versão web não consegue impedir capturas de tela como a versão Android nativa.

## Desenvolvimento

Sem compilação ou dependências de produção. Os arquivos-fonte estáticos estão em web/dist/. O comando npm run build gera public/ para a Vercel. É necessário HTTPS (ou localhost para desenvolvimento) para instalar o service worker.

```text
npm run build
python -m http.server 4173 --directory public
```

Para testes automatizados:

```text
npm ci
npm test
```

Os testes usam DOM e IndexedDB simulados. Cobrem 78 itens, 48 combinações de viagem, marcações e persistência, filtro, escape de item extra, formulário da viagem, cópia original das fotos, exclusão, limites, manifesto e ícones e cache/fallback offline. O decodificador de imagens é simulado; não equivale a validação visual, gesto de pinça ou instalação em iPhone real.

A prévia integrada do navegador estava indisponível. A automação nativa de Chrome foi interrompida pela ferramenta por não conseguir confirmar a URL da janela, por isso não houve teste visual no navegador ou aparelho físico nesta entrega.

## Publicação

A configuração da Vercel fica em vercel.json, na raiz do repositório. Use Root Directory na raiz e o Framework Preset Other. O build gera public/ e atualiza a versão do cache offline conforme o conteúdo dos arquivos. Nenhum documento pessoal deve ser incluído no repositório. As imagens em web/dist/assets são apenas as marcas, mascotes e ícones do projeto.
