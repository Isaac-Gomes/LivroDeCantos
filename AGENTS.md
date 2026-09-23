# Ressuscitou — contexto do projeto

## Visão geral

Este é um aplicativo Android chamado Ressuscitou, um cancioneiro litúrgico do Caminho Neocatecumenal.

O projeto usa:

- Kotlin
- Jetpack Compose
- Room
- Media3 / ExoPlayer
- WorkManager
- minSdk 26
- package: `com.example.livrodecantos`

Não usa XML para montar as telas principais; a interface é feita em Jetpack Compose.

## Regra de trabalho

Antes de alterar qualquer arquivo:

1. Leia os arquivos envolvidos e entenda a implementação atual.
2. Preserve funcionalidades existentes.
3. Faça a menor alteração necessária.
4. Não reescreva arquivos inteiros sem necessidade.
5. Depois da alteração, execute um build do projeto.
6. Se houver erro de compilação causado pela mudança, corrija antes de encerrar.
7. Não altere versões de dependências sem necessidade.
8. Não apague dados do Room nem use destructive migration.
9. Não altere assets, banco ou estrutura de dados sem explicar a necessidade.
10. Preserve compatibilidade com instalações existentes do aplicativo.

## Estrutura principal

Principais pacotes:

- `data/database`
- `data/dao`
- `data/entity`
- `data/repository`
- `model`
- `ui/components`
- `ui/screens`
- `ui/viewmodel`
- `worker`

Principais telas:

- `TelaPrincipal.kt`
- `TelaFicha.kt`
- `TelaIndiceLiturgico.kt`
- `TelaIndiceBiblico.kt`
- `TelaAudiosOffline.kt`
- `TelaMinhasListas.kt`
- `TelaEditarLista.kt`
- `TelaDetalheLista.kt`
- `TelaFilaReproducao.kt`

Principais ViewModels:

- `CantoViewModel.kt`
- `AudioViewModel.kt`
- `ListaViewModel.kt`
- `DownloadTodosViewModel.kt`

## Navegação atual

A navegação principal é controlada em `TelaPrincipal.kt` por um enum semelhante a:

- PRINCIPAL
- INDICE_LITURGICO
- INDICE_BIBLICO
- MINHAS_LISTAS
- DETALHE_LISTA
- EDITAR_LISTA
- FILA_REPRODUCAO
- AUDIOS_OFFLINE
- FICHA

Ao abrir uma ficha, a tela anterior deve ser preservada para que o botão voltar retorne ao contexto correto.

## Cantos

As etapas são:

- PRE_CATECUMENATO
- LITURGICOS
- CATECUMENATO
- ELEICAO

Cores da numeração:

- Pré-Catecumenato: branco
- Litúrgicos: amarelo
- Catecumenato: azul
- Eleição: verde

A numeração começa em:

- 21 = A cabana dos pastores

e termina em:

- 268 = Suba O Esposo Ao Lenho Do Seu Tálamo

Cantos com mais de uma página podem ter mais de um número.

Exemplos:

- Dayenu: 73,74
- Oração Eucarística II: 218,219,220
- Oração Eucarística IV: 221,222,223,224

Itens especiais:

- Prontuário De Acordes
- Arpejos
- Tabela Para Transportar

devem aparecer no final da listagem.

Arpejos e Tabela Para Transportar não possuem número.

## Fichas

As fichas são imagens.

Características atuais:

- zoom por gesto
- rolagem horizontal quando ampliadas
- páginas múltiplas em sequência vertical
- abertura direta da ficha

Não substituir essa visualização por WebView ou PDF.

## Áudio

O aplicativo usa Media3 / ExoPlayer.

O áudio pode:

- ser reproduzido online
- ser baixado para uso offline
- ser excluído individualmente
- ser excluído em lote
- continuar através de mini-player
- ser usado em fila de reprodução
- ser reproduzido em ordem ou aleatoriamente

Existe um `AudioRepository` responsável por descobrir e baixar MP3s.

Não duplicar a lógica de download se já existir no repository.

Existe download global usando WorkManager.

## Listas

O usuário pode:

- criar listas
- escolher cantos
- manter a ordem de seleção
- editar listas
- reproduzir em ordem
- reproduzir aleatoriamente
- baixar os áudios da lista

Não substituir listas por sistema de favoritos.

## Melhorias planejadas

Implementar nesta ordem:

### 1. Preservar a rolagem

Quando o usuário estiver, por exemplo, na letra O da ordem alfabética, abrir uma ficha e voltar, a LazyColumn deve permanecer exatamente na mesma posição.

A posição deve ser preservada também quando possível entre diferentes contextos de listagem.

### 2. Todos os Cantos por número

No menu lateral existem:

- Todos os Cantos
- Ordem Alfabética

Eles devem ser modos diferentes.

`Ordem Alfabética`:
- ordenar alfabeticamente em português.

`Todos os Cantos`:
- ordenar pela numeração das fichas, começando em 21 e terminando em 268.
- cantos multipágina devem ser posicionados pelo primeiro número.
- itens especiais devem continuar no final.

### 3. Menu de ferramentas na ficha

Criar na ficha um único botão flutuante no canto inferior direito.

Ao tocar, abrir um `ModalBottomSheet`.

Esse painel será a central de ferramentas do canto e deverá reunir progressivamente:

- áudio
- download offline
- transposição
- capotraste
- monição

Evitar espalhar vários botões flutuantes sobre a ficha.

O mini-player global deve continuar funcionando.

### 4. Monições

Adicionar monições aos cantos, exceto aos cantos da etapa LITURGICOS.

As monições serão baseadas em material fornecido pelo usuário, incluindo um livro em espanhol.

Não inventar intenções do autor sem suporte no material fornecido.

A monição deve poder ser acessada a partir do painel de ferramentas da ficha.

### 5. Transposição e capotraste

As fichas atuais são imagens, portanto os acordes da imagem não podem simplesmente ser editados como texto.

Para implementar transposição corretamente, criar uma representação estruturada dos acordes.

Requisitos:

- alterar somente acordes
- nunca alterar a letra
- preservar acordes com sustenidos, bemóis, menores, sétimas etc.
- permitir subir e descer o tom
- mostrar tom original
- mostrar tom atual
- implementar cálculo de capotraste

Não tentar editar pixels da ficha como solução definitiva.

## Próxima tarefa imediata

A primeira melhoria a ser implementada é:

Preservar a posição da rolagem da lista principal ao abrir uma ficha e voltar.

Antes de modificar, examine `TelaPrincipal.kt` e como `LazyColumn` e navegação estão implementadas atualmente.

Depois:

1. implemente a solução;
2. preserve todo o comportamento existente;
3. rode o build;
4. corrija eventuais erros relacionados à alteração;
5. informe resumidamente quais arquivos foram modificados.
## Validação

Por padrão, NÃO realizar testes manuais/visuais no emulador ou simulador,
pois eles consomem muito tempo.

Ao finalizar alterações, priorizar:

1. testes unitários;
2. testes instrumentados automatizados somente quando forem realmente necessários;
3. :app:assembleDebug.

Não abrir nem interagir manualmente com o emulador, não navegar pela interface
e não executar validação visual, salvo quando o usuário pedir explicitamente.

Se uma mudança tiver algum aspecto visual que não possa ser comprovado pelos
testes automatizados, apenas informar isso no resumo final, sem abrir o emulador.