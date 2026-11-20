#!/usr/bin/env python3
"""Gera uiclasses.txt a partir do relatório de strings do APK.

Uso:
    python3 make_uiclasses.py dexstrings.tsv mapping.tsv > uiclasses.txt

Uma classe entra na lista quando referencia pelo menos uma "frase longa" do
mapa (>= 10 caracteres, com espaço). Essas classes são UI — inclusive as que o
R8 renomeou — e nelas os rótulos curtos (ex.: "Settings", "Tables") também
podem ser traduzidos com segurança.

Formato de dexstrings.tsv: string<TAB>nº de referências<TAB>UI/-<TAB>classes
(gerado por: DexTool report <apk> dexstrings.tsv)
"""
import sys


def is_wide(s):
    if len(s) < 10 or " " not in s:
        return False
    if "$" in s or "_" in s:
        return False
    letters = sum(1 for c in s if c.isalpha())
    return letters * 2 > len(s) and len(s) > letters


def main():
    if len(sys.argv) < 3:
        print(__doc__, file=sys.stderr)
        return 1
    keys = set()
    with open(sys.argv[2], encoding="utf-8") as f:
        for line in f:
            if "\t" in line:
                keys.add(line.split("\t", 1)[0])
    wide = {k for k in keys if is_wide(k)}

    out = set()
    with open(sys.argv[1], encoding="utf-8", errors="replace") as f:
        for line in f:
            parts = line.rstrip("\n").split("\t")
            if len(parts) < 4 or parts[0] not in wide:
                continue
            for ref in parts[3].split(" | "):
                cls = ref.split(" :: ")[0].strip()
                if cls:
                    out.add(cls)

    for cls in sorted(out):
        print(cls)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
