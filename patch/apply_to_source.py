#!/usr/bin/env python3
"""Aplica o mapa de tradução diretamente nos fontes Kotlin do Nuvio + IPTV.

Uso:
    python3 apply_to_source.py <raiz-do-repo> [mapping.tsv]

Percorre app/src/main/java/com/nuvio/tv/livetv/**/*.kt e troca os literais
"..." exatos pelas traduções do mapa. É o "código correspondente" (GPLv3) da
modificação publicada como APK: o binário é o APK oficial com essas mesmas
substituições aplicadas nos dex.

Não é um tradutor de recursos: só mexe em literais de string. Linhas de
comentário, imports e pacotes são ignoradas.
"""
import pathlib
import re
import sys

LITERAL = re.compile(r'"((?:[^"\\]|\\.)*)"')
SKIP_PREFIX = ("//", "*", "/*", "import ", "package ", "Log.")


def load_mapping(path):
    mapping = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line or line.startswith("#") or "\t" not in line:
                continue
            src, dst = line.split("\t", 1)
            mapping[src] = dst
    return mapping


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 1
    root = pathlib.Path(sys.argv[1])
    map_path = sys.argv[2] if len(sys.argv) > 2 else "mapping.tsv"
    mapping = load_mapping(map_path)

    base = root / "app/src/main/java/com/nuvio/tv/livetv"
    if not base.is_dir():
        print(f"não encontrei {base}", file=sys.stderr)
        return 2

    changed_files = 0
    changed_strings = 0
    for path in sorted(base.rglob("*.kt")):
        lines = path.read_text(encoding="utf-8").splitlines(keepends=True)
        touched = False
        for i, line in enumerate(lines):
            stripped = line.lstrip()
            if stripped.startswith(SKIP_PREFIX) or "TAG" in line:
                continue

            def repl(match):
                nonlocal touched, changed_strings
                value = match.group(1)
                pt = mapping.get(value)
                if pt is None or pt == value:
                    return match.group(0)
                touched = True
                changed_strings += 1
                return '"' + pt.replace("\\", "\\\\").replace('"', '\\"') + '"'

            new_line = LITERAL.sub(repl, line)
            if new_line != line:
                lines[i] = new_line
        if touched:
            path.write_text("".join(lines), encoding="utf-8", newline="")
            changed_files += 1
            print(f"ok  {path.relative_to(root)}")

    print(f"\n{changed_strings} literais em {changed_files} arquivos")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
