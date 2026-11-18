# Validação — Mala Pronta 1.0.0

Verificação concluída em 18/11/2026.

- Compilação nativa com JDK 17, Android Platform 35 e Build Tools 35.0.0: aprovada.
- APK com assinatura v2 e v3: verificação aprovada pelo apksigner.
- Alinhamento do APK: aprovado pelo zipalign.
- 78 itens originais, quantidades e 48 combinações de personalização: testes Java aprovados.
- Instalação do APK e abertura no emulador Android API 37: aprovadas.
- Inspeção visual da tela principal em 360 × 800 dp: aprovada.
- Personalização pela tela Minha viagem: aplicada; viagem de ônibus em alojamento resulta em 79 itens.
- Marcação de documento: contador atualizado para 1/79.
- Encerramento forçado e reabertura: marcação e personalização preservadas.
- Filtro Pendentes: item marcado ocultado e progresso preservado.
- Nenhum erro AndroidRuntime registrado durante os testes.

A captura `tela-checklist.png` foi atualizada para a versão 1.2 e mostra o checklist personalizado usado no emulador. O APK inicia sem os dados usados nos testes; eles ficaram apenas no emulador.

Não houve teste em um aparelho físico. Compartilhamento e todas as combinações visuais dos seletores não foram testados em dispositivo; as regras de geração das 48 combinações foram cobertas pelos testes Java.


## Atualização 1.1.0 — Documentos

- Novo APK com versionCode 2 e mesma assinatura: instalou como atualização no emulador API 37.
- Teste Android de instrumentação aprovado: importação pelo resultado da Activity, título e listagem na interface, cópia byte a byte, disponibilidade offline após excluir a origem, diretório privado sem backup, exclusão da cópia, cancelamento do seletor, rejeição de arquivo inválido e de imagem maior que 25 MB, orientação EXIF e preservação do checklist.
- Visualizador aberto durante o teste e imagem decodificada nas dimensões esperadas.
- FLAG_SECURE habilitado nas telas de documentos e desabilitado ao voltar ao checklist.
- Os testes anteriores de regras de personalização continuam aprovados.

Não foram usados documentos pessoais nos testes. A interação manual com seletores de galeria de diferentes fabricantes e o gesto físico de pinça em aparelho real não foram testados.


## Atualização 1.2.0 — Logos Lieu e Inter

- Três PNGs incorporados; hashes SHA-256 idênticos aos arquivos fornecidos.
- Compilação e assinatura v2/v3 verificadas; APK instalou como atualização sobre a versão 1.1.
- Checklist existente preservado (1/79 no emulador).
- Telas Checklist e Viagem inspecionadas visualmente: logos legíveis, sem distorções ou cortes do conteúdo.
- Capturas atualizadas em `dist/tela-checklist.png` e `dist/tela-viagem.png`.
- Nenhum erro AndroidRuntime registrado na verificação desta atualização visual.


## Atualização 1.3.0 — Mascotes e ícone

- Onze mascotes fornecidos incorporados: três Chaminhas nas telas e oito mascotes esportivos em uma faixa horizontal na tela Viagem.
- Inspeção visual de Checklist, Dicas e faixa de modalidades no emulador; imagens proporcionais e sem sobreposição dos controles.
- Ícone adaptativo da mala verde conferido na página de informações do aplicativo no Android.
- APK com versionCode 4, mesma assinatura e mesmo pacote; atualização instalou preservando o progresso de 1/79 itens.
- Nenhum erro AndroidRuntime observado durante a validação.
- Arquivos originais mantidos. As imagens grandes são decodificadas em resolução limitada durante a exibição para reduzir uso de memória.
- A recomendação de análise do Google Play Protect é externa ao app e não foi removida. A assinatura do APK não equivale à análise de segurança do Google.

Esta atualização altera a apresentação; as regras de checklist e o armazenamento de fotos não foram alterados. Não houve teste em aparelho físico.

## Preparação do repositório e Vercel

- Código Android e PWA organizados no mesmo repositório.
- vercel.json na raiz: preset Other, build npm run build, saída public/, sem instalação de dependências para o build.
- Build estático aprovado: 25 entradas offline, manifesto e tamanhos de ícones validados; nome do cache derivado do conteúdo.
- Testes da versão web aprovados no novo caminho web/tests/.
- Chaves de assinatura, credenciais, dados locais, node_modules e saídas de build excluídos do versionamento.
- A publicação na Vercel não foi executada nesta etapa; o repositório foi preparado para importação.
