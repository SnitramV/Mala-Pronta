# Mala Pronta 1.3 — aplicativo Android

Checklist offline para viagens universitárias, feito em Java com componentes nativos do Android. Não exige conta, servidor ou conexão à internet.

## Instalar no celular

1. Transfira `dist/Mala-Pronta.apk` para o celular Android.
2. Abra o arquivo e, se o Android solicitar, permita a instalação por essa fonte.
3. Toque em Instalar e abra **Mala Pronta**.

Compatível com Android 8.0 ou superior. Não é um aplicativo para iPhone.

## Recursos

- Os 78 itens originais, divididos em 8 categorias.
- Marcação por item, progresso geral e contadores por categoria.
- Filtros Todos, Pendentes e Na mala; categorias recolhíveis.
- Armazenamento local automático, inclusive ao fechar ou reiniciar o app.
- Nome da viagem, duração em dias, hospedagem, transporte e atividades.
- Quantidades sugeridas de camisetas, roupas íntimas, meias e bermudas.
- Hotel oculta 5 itens de cama; camping, transporte, praia e competições acrescentam itens específicos. As marcações dos itens ocultos são mantidas.
- Itens próprios em Extras que salvam. Toque e segure um item próprio para excluí-lo.
- Compartilhamento de uma cópia em texto pelo seletor do Android.
- Limpeza de marcações com confirmação e opção de voltar à lista original.
- Dicas de viagem fornecidas no pedido.
- Aba Documentos: importação de fotos da galeria ou dos arquivos, com nome próprio.
- Cópias privadas persistentes para consultar RG, CNH e ingressos offline; até 25 MB por imagem.
- Visualização com pinça, arraste, duplo toque e ajuste à tela; orientação EXIF respeitada.
- Imagens originais copiadas sem recompressão; exclusão com confirmação remove somente a cópia no app.
- Telas de documentos e visualização usam a proteção de captura do Android (FLAG_SECURE).
- Sem permissão de acesso amplo à galeria, sem envio das fotos à internet e sem backup remoto.

As fotos ficam no diretório privado `noBackupFilesDir/documents`, e não no cache temporário. Limpar apenas o cache preserva as fotos. Desinstalar o app ou limpar seus dados apaga as cópias. Não há recuperação nem sincronização. Use imagens JPEG, PNG ou WebP; a importação de PDF não está incluída.

Para atualizar da versão 1.0, instale o novo APK por cima do anterior. A assinatura e o identificador do aplicativo foram mantidos para preservar o checklist. Não desinstale a versão anterior para atualizar.

A personalização mantém os itens de precaução da lista original. As quantidades são sugestões, e a hospedagem deve ser consultada sobre roupas de cama. Desinstalar o aplicativo ou limpar os dados do Android remove os dados locais. Não há sincronização nem backup remoto.

## Compilar

Requer Windows PowerShell, JDK 17, Android SDK Platform 35 e Android Build Tools 35.0.0. Não usa Gradle, bibliotecas externas ou downloads durante a compilação.

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Para caminhos diferentes:

```powershell
.\build.ps1 -Sdk 'C:\caminho\Android\Sdk' -Jdk 'C:\caminho\jdk-17'
```

Saída: `dist/Mala-Pronta.apk`. O script valida a assinatura e imprime o SHA-256.

O APK é assinado com um certificado local de desenvolvimento gerado em `build/local-signing.p12`. Guarde esse arquivo para assinar atualizações compatíveis. A senha de desenvolvimento está no script; para distribuição pública, configure uma chave de produção e proteção adequada da senha. O aplicativo não foi publicado na Play Store.

## Testes

```powershell
& 'C:\Program Files\Java\jdk-17\bin\javac.exe' -encoding UTF-8 -d build\tests app\src\main\java\br\com\malapronta\Checklist.java tests\ChecklistTest.java
& 'C:\Program Files\Java\jdk-17\bin\java.exe' -cp build\tests br.com.malapronta.ChecklistTest
```

Os testes verificam a contagem original, quantidades, itens de hotel/camping/transporte/atividades, estabilidade de IDs e ausência de duplicações nas 48 combinações.


## Testes Android das fotos

`test-device-build.ps1` compila um APK de instrumentação separado, fora do APK distribuído. Instale `build/instrumentation/tests.apk` no mesmo emulador do aplicativo e execute:

```powershell
adb shell am instrument -w br.com.malapronta.tests/br.com.malapronta.PhotoInstrumentation
```

O teste cria apenas imagens sintéticas e verifica importação pelo resultado da Activity, interface, cópia exata dos bytes, consulta após remoção da origem, armazenamento privado, exclusão, cancelamento, arquivo inválido, limite de 25 MB, orientação EXIF, proteção de captura e preservação do checklist. Não testa a navegação manual em todos os provedores de galeria de fabricantes.


## Identidade visual — versão 1.2

As três imagens fornecidas foram incluídas sem alterar os arquivos originais: Lieu sem fundo no cabeçalho, Inter Prudente 2026 no checklist e a versão quadrada da Lieu ao lado do Inter na tela Viagem. O layout preserva as proporções e usa descrições acessíveis para as imagens. Os arquivos estão em `app/src/main/res/drawable-nodpi`.

A versão 1.2 usa versionCode 3 e a mesma chave das versões anteriores. Instale por cima do aplicativo existente para preservar checklist e fotos.


## Mascotes e ícone — versão 1.3

Chaminha com mochila no checklist, Chaminha com celular na pasta de documentos e Chaminha positivo nas dicas. A tela Viagem inclui uma faixa horizontal com os oito mascotes esportivos enviados. Os PNGs originais foram mantidos; o app limita a resolução de decodificação e enquadra a área visível sem distorcer as proporções.

O ícone instalado usa a mala verde fornecida, em um ícone adaptativo com margem para as máscaras dos lançadores Android. A assinatura e o pacote continuam os mesmos; a versão 1.3 usa versionCode 4.

### Aviso do Google Play Protect

A recomendação de verificar um aplicativo ainda não analisado é exibida pelo Google Play Protect durante a instalação de APKs. Ela não é uma tela do Mala Pronta e não pode ser desativada pelo aplicativo. O novo APK não promete eliminar esse aviso. Na tela enviada, use **Verificar app** e aguarde a análise do Google.

Referência: https://support.google.com/googleplay/answer/2812853?hl=pt-BR
