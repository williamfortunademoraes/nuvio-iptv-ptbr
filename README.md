# Nuvio + IPTV — Português do Brasil (não oficial)

[![Última versão](https://img.shields.io/github/v/release/williamfortunademoraes/nuvio-ptbr?label=vers%C3%A3o&color=2ea043)](https://github.com/williamfortunademoraes/nuvio-ptbr/releases/latest)
[![Rastreador do upstream](https://github.com/williamfortunademoraes/nuvio-ptbr/actions/workflows/upstream-check.yml/badge.svg)](https://github.com/williamfortunademoraes/nuvio-ptbr/actions/workflows/upstream-check.yml)

## ⬇️ Baixar

### **[NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.apk](https://github.com/williamfortunademoraes/nuvio-ptbr/releases/latest/download/NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.apk)** — 188 MB

SHA-256: `9d7dc1340fd1ad0a0049679090960c9de232c8cb127e87d5a2b24ebc774dbade`

> Link direto, sempre aponta para o APK da versão mais recente deste repositório.
> Outras versões: [Releases](https://github.com/williamfortunademoraes/nuvio-ptbr/releases).

| Base (upstream) | Este APK | Arquivo | SHA-256 |
|---|---|---|---|
| `1.2.0-beta.2.1` | `1.2.0-beta.2.1-ptBR` | `NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.apk` | `9d7dc134…4dbade` |

---

## O que é isto

O **Nuvio + IPTV** é um app Android TV de código aberto (GPLv3), fork do Nuvio. Na versão oficial, mesmo com o idioma do sistema em **Português (Brasil)**, boa parte da camada de TV ao vivo continua em inglês: as telas de *Configurações* foram traduzidas (PR #112), mas o resto do texto está fixo no código Kotlin e nunca chega aos recursos `values-pt`.

Este repositório publica uma **compilação não oficial** com esses textos traduzidos para pt-BR. É o **mesmo APK oficial**, com as strings alteradas — nenhuma mudança de lógica, layout ou comportamento.

## O que foi traduzido

- **Guia / TV ao vivo** — cabeçalho, abas, menus de contexto, dicas de controle remoto, busca, favoritos, grupos, reordenar canais, atribuir EPG, seletor de logos
- **Esportes** — Meus Esportes, Tabelas, Todos os eventos, cartões de jogo, tabelas de classificação, escalações, estados vazios e erros
- **Gravações** — lista, nova gravação, séries, mover/excluir, diálogos de confirmação
- **Multitela** — layouts, painéis, avisos
- **Calendário** — abas, estados vazios, menu de canal, sinos e lembretes
- **Painel do player** — Configurações, barra de reprodução, canais rápidos, tamanho de tela, timer de sono, informações da transmissão

São 736 strings traduzidas (1.016 ocorrências). O mapa completo está em [`patch/mapping.tsv`](patch/mapping.tsv).

## O que **não** é traduzido (e não pode ser)

| Conteúdo | Origem |
|---|---|
| Nomes de programas, sinopses, horários | XMLTV/EPG do seu provedor |
| Times, ligas, competições, placares | ESPN / TheSportsDB |
| Nomes de canais, playlists e grupos | o que você cadastrou |
| Nomes de temas, siglas (`PIN`, `EPG`, `HD`) | o próprio app |

Traduzir isso exigiria tradução automática do conteúdo do provedor, o que este projeto não faz.

## Instalação

O APK é assinado com **a mesma chave do app oficial**, então ele **atualiza por cima**: não precisa desinstalar e você não perde contas, playlists, EPG nem gravações.

**Android TV / Google TV (mais fácil)**
1. Baixe o APK na TV (app *Downloader*: use o link direto acima) ou copie por pendrive / *Send files to TV*.
2. Abra o arquivo no gerenciador de arquivos e aceite **Atualizar**.

**Por ADB**
```bash
adb connect IP_DA_TV:5555
adb install -r NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.apk
```

**Conferir o arquivo (opcional)**
```bash
sha256sum NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.apk
# 9d7dc1340fd1ad0a0049679090960c9de232c8cb127e87d5a2b24ebc774dbade
```

## Atualizações

O app tem atualização automática apontada para o serviço do mantenedor do fork. Se ela rodar, **baixa a versão oficial em inglês por cima** desta. Desative a atualização automática (ou apenas ignore o aviso) para manter o português.

Este repositório tem um **rastreador** que roda todo dia ([`upstream-check.yml`](.github/workflows/upstream-check.yml)): ele consulta a última release do fork oficial e, quando aparece versão nova, **abre uma issue** aqui avisando. Aí é só gerar o APK novo (processo em [`patch/BUILD.md`](patch/BUILD.md)) e publicar.

> Status atual: baseado em `1.2.0-beta.2.1` (a release mais recente do upstream até agora).

## Como é feito (reprodutível)

O binário é gerado a partir do **APK oficial universal**, trocando as constantes de texto nos `classes*.dex` e re-assinando com a chave do fork:

```
patch/DexTool.java    → localiza e substitui as strings nos dex
patch/mapping.tsv     → mapa English → pt-BR (736 entradas)
patch/uiclasses.txt   → classes de UI onde rótulos curtos podem ser trocados
patch/repack.py       → remonta o APK com os dex novos
patch/BUILD.md        → passo a passo completo
patch/apply_to_source.py → aplica o mesmo mapa direto no código-fonte Kotlin
```

A forma em código-fonte da modificação está em `apply_to_source.py` (edita os literais em `livetv/`), que é o "código correspondente" da GPLv3 junto com o código oficial do upstream.

## Limitações

- Build **não oficial**, sem vínculo com os autores do Nuvio ou do fork. Não abra issue no repositório oficial por causa desta versão.
- Pode haver texto novo em inglês em versões futuras — o mapa cobre a build `1.2.0-beta.2.1`.
- Não mexe em assinatura, permissões, anúncios, telemetria nem em qualquer lógica: só texto.
- Se você encontrar um texto ainda em inglês, abra uma issue com o print e o caminho da tela.

## Créditos e licença

- App original: [Nuvio](https://github.com/tapframe/Nuvio) e o fork [homelessbrian/NuvioPlusIPTV](https://github.com/homelessbrian/NuvioPlusIPTV).
- Licença: **GPLv3** — veja [`LICENSE`](LICENSE). O código-fonte oficial está nos repositórios acima; a modificação (mapa de strings + ferramentas) está neste repositório.
- Tradução pt-BR: contribuição da comunidade. Se o mantenedor aceitar o PR de tradução ([issue #136](https://github.com/homelessbrian/NuvioPlusIPTV/issues/136)), este APK deixa de ser necessário.
