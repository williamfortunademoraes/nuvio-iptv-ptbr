# Nuvio + IPTV 1.2.0-beta.2.1-ptBR.3 — Português do Brasil (não oficial)

Atualização da tradução pt-BR da camada de TV ao vivo: Guia, Esportes, Gravações,
Multitela, Calendário e painel do player.

## Baixar

**NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.3.apk** — 188 MB (universal: arm64-v8a, armeabi-v7a, x86_64)

SHA-256:
```
08c6c809cfe62aeb1c79be016db1e93b0dc71e2e2bae4ff69153b0b4a72d63a9
```

## O que mudou

Nada de lógica, layout ou permissões: apenas 831 textos (1.168 ocorrências) que
estavam fixos no código e nunca chegavam ao português. Esta versão acrescenta 77 traduções para mensagens de erro, gravações, guia, esportes e reprodução.

- **Guia / TV ao vivo**: abas, menus, dicas de controle, busca, favoritos, grupos, reordenar canais, atribuir EPG, logos
- **Esportes**: Meus Esportes, Tabelas, Todos os eventos, ligas, cartões, escalações, estados vazios
- **Gravações**: lista, nova gravação, séries, mover/excluir
- **Multitela**: layouts, painéis, avisos
- **Calendário**: abas, estados vazios, menu de canal, sinos e lembretes
- **Player**: Configurações, barra de reprodução, canais rápidos, tamanho de tela, timer de sono, info da transmissão

Continua no idioma de origem: nomes de programas, sinopses, times, ligas e placares (vêm do provedor/EPG e dos feeds esportivos).

## Instalar / atualizar

Assinado com a mesma chave do app oficial (`CN=Nuvio Live TV fork`), então
atualiza por cima, sem desinstalar e sem perder dados.

```bash
adb install -r NuvioPlusIPTV-1.2.0-beta.2.1-ptBR.3.apk
```

Na TV: baixe pelo *Downloader* (link do README) ou copie por pendrive e abra.

## Avisos

- Build **não oficial**, sem vínculo com os autores do Nuvio ou do fork.
- A atualização automática do app baixa a versão oficial em inglês por cima desta.
- Se aparecer texto novo em inglês, abra uma issue com print e o caminho da tela.

Base: [homelessbrian/NuvioPlusIPTV `1.2.0-beta.2.1`](https://github.com/homelessbrian/NuvioPlusIPTV/releases/tag/1.2.0-beta.2.1) · GPLv3 · código da tradução em `patch/`.
