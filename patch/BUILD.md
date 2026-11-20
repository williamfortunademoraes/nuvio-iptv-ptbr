# Como gerar o APK pt-BR

Tudo roda com **JDK 17+** e Python 3 — não precisa de Android SDK, Gradle nem Android Studio.

## Pré-requisitos (uma vez)

```bash
# ferramentas
curl -LO https://repo1.maven.org/maven2/org/smali/dexlib2/2.5.2/dexlib2-2.5.2.jar
curl -LO https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar
curl -LO https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar
curl -LO https://github.com/patrickfav/uber-apk-signer/releases/download/v1.3.0/uber-apk-signer-1.3.0.jar

# código-fonte oficial (para a chave de assinatura e para o "código correspondente")
git clone --depth 1 https://github.com/homelessbrian/NuvioPlusIPTV.git
```

## 1. Baixar o APK oficial universal da versão desejada

```bash
TAG=1.2.0-beta.2.1
curl -L -o base.apk \
  "https://github.com/homelessbrian/NuvioPlusIPTV/releases/download/$TAG/NuvioPlusIPTV-$TAG-universal-release.apk"
```

Confira que ele está assinado com a chave do fork (é o que permite atualizar por cima):

```bash
java -jar uber-apk-signer.jar -y -a base.apk
# Subject: CN=Nuvio Live TV fork
# SHA256: e2c9a67239c6297fb28a28b72a088ad32cff8aeb94e0157b02d22ee7aeb5cbd8
```

## 2. Compilar a ferramenta e aplicar as traduções

```bash
javac -encoding UTF-8 -cp "dexlib2-2.5.2.jar;guava-31.1-jre.jar;jsr305-3.0.2.jar" DexTool.java

rm -rf out_dex
java -Xmx4g -cp ".;dexlib2-2.5.2.jar;guava-31.1-jre.jar;jsr305-3.0.2.jar" \
  DexTool patch base.apk mapping.tsv out_dex uiclasses.txt
# → dex files: 6, replacements: 1016, used keys: 736/755
```

O relatório final lista as chaves que **não** foram encontradas. Três motivos possíveis:

1. o texto virou parte de uma string maior (concatenação em tempo de compilação) — normal;
2. a string não existe mais na nova versão — remover do mapa;
3. a classe saiu da lista `uiclasses.txt` (a ofuscação muda entre versões) — ver abaixo.

### Atualizar `uiclasses.txt` quando o upstream mudar

`uiclasses.txt` é derivada do próprio APK: são as classes que referenciam pelo menos
uma frase longa do mapa. Gere de novo com:

```bash
# 1. relatório de strings (string → classes que a usam)
java -Xmx4g -cp ".;dexlib2-2.5.2.jar;guava-31.1-jre.jar;jsr305-3.0.2.jar" \
  DexTool report base.apk dexstrings.tsv

# 2. classes com pelo menos uma chave "frase longa"
python3 make_uiclasses.py dexstrings.tsv mapping.tsv > uiclasses.txt
```

(`make_uiclasses.py` está neste diretório.)

## 3. Remontar e assinar

```bash
python3 repack.py base.apk out_dex ptbr-unsigned.apk

java -jar uber-apk-signer.jar \
  -a ptbr-unsigned.apk \
  --ks NuvioPlusIPTV/keystore/livetv-fork.jks \
  --ksAlias livetv --ksPass livetvfork --ksKeyPass livetvfork \
  -o signed
```

A chave `keystore/livetv-fork.jks` faz parte do repositório oficial do fork (é a chave de release pública dele). Sem ela o APK não atualiza por cima do app instalado.

## 4. Verificar e publicar

```bash
java -jar uber-apk-signer.jar -y -a signed/*-signed.apk
sha256sum signed/*-signed.apk
```

Depois:

```bash
gh release create v<TAG>-ptBR signed/*-signed.apk \
  --title "Nuvio + IPTV <TAG> — pt-BR" \
  --notes-file NOTES.md
```

E atualize `upstream.json` (`based_on`, `ptbr_release`) para o rastreador parar de avisar.

---

## Alternativa: aplicar direto no código-fonte

Se preferir compilar com Gradle a partir do fonte oficial (ex.: para mandar um PR):

```bash
python3 apply_to_source.py /caminho/NuvioPlusIPTV patch/mapping.tsv
```

O script substitui os literais `"..."` em `app/src/main/java/com/nuvio/tv/livetv/**/*.kt`.
É uma ajuda de migração: o APK publicado foi gerado pelo caminho 2–3 acima (dex),
que é o que reproduz exatamente este binário.
