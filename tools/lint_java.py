#!/usr/bin/env python3
"""Lint barato para este repo, sin JDK.

Comprueba tres cosas que ya nos costaron tres builds rojos:
  1. `@Override` seguido de algo que no es un metodo.
  2. Variables locales con el mismo nombre en ambitos anidados (javac: "already defined").
  3. Lineas que empiezan por un operador suelto (`+ (`, `&& ...`) fuera de una expresion,
     sintoma de una sentencia cortada por una edicion a mano.

Uso: python3 tools/lint_java.py [rutas...]
Sale con codigo 1 si encuentra algo.
"""
import glob
import re
import sys

DECL = re.compile(r'^\s*(?:final\s+)?(?:[A-Za-z_][\w.]*(?:<[^;=]*>)?(?:\[\])*)\s+([a-z_]\w*)\s*=[^=]')
SKIP_WORDS = {
    'if', 'for', 'while', 'switch', 'catch', 'return', 'new', 'else', 'do', 'try',
    'synchronized', 'case', 'assert', 'throw', 'yield',
}


def strip_noise(src):
    """Quita comentarios y cadenas conservando las lineas."""
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n':
                i += 1
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'):
                if src[i] == '\n':
                    out.append('\n')
                i += 1
            i += 2
        elif c in '"\'':
            q = c
            i += 1
            while i < n and src[i] != q:
                if src[i] == '\\':
                    i += 1
                if i < n and src[i] == '\n':
                    out.append('\n')
                i += 1
            i += 1
            out.append('""')
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def check_override(path, lines):
    bad = []
    for i, line in enumerate(lines):
        if line.strip() == '@Override':
            j = i + 1
            while j < len(lines) and not lines[j].strip():
                j += 1
            nxt = lines[j].strip() if j < len(lines) else ''
            if not (nxt.startswith(('public', 'protected', 'private')) or nxt.startswith('@')):
                bad.append((path, i + 1, '@Override sin metodo: ' + nxt[:60]))
    return bad


def check_shadow(path, clean):
    bad = []
    # ambito de cada bloque por indice de caracter
    depth = 0
    scope_at = []
    stack = []
    scopes = []  # (start, end)
    for idx, c in enumerate(clean):
        if c == '{':
            stack.append(idx)
            depth += 1
        elif c == '}':
            if stack:
                start = stack.pop()
                scopes.append((start, idx))
            depth = max(0, depth - 1)
        scope_at.append(depth)
    decls = []
    pos = 0
    for line in clean.split('\n'):
        m = DECL.match(line)
        if m and '(' not in line[:m.start(1)] and not line[:m.start(1)].endswith('else '):
            name = m.group(1)
            if name not in SKIP_WORDS:
                decls.append((name, pos + (m.end(1) - len(name)), pos))
        pos += len(line) + 1
    # agrupa por nombre y compara ambitos anidados
    by_name = {}
    for name, idx, linepos in decls:
        by_name.setdefault(name, []).append((idx, linepos))
    for name, items in by_name.items():
        if len(items) < 2:
            continue
        for a in range(len(items)):
            for b in range(a + 1, len(items)):
                ia, la = items[a]
                ib, lb = items[b]
                sa = [s for s in scopes if s[0] < ia < s[1]]
                sb = [s for s in scopes if s[0] < ib < s[1]]
                # si comparten bloque contenedor (anidado o igual), javac protesta
                inner_a = max(sa) if sa else None
                inner_b = max(sb) if sb else None
                if inner_a is None or inner_b is None:
                    continue
                # solo choca si la primera sigue viva cuando se declara la segunda
                if inner_a == inner_b or (inner_a[0] < ib < inner_a[1]):
                    line_a = clean.count('\n', 0, la) + 1
                    line_b = clean.count('\n', 0, lb) + 1
                    bad.append((path, line_b, "variable '%s' ya declarada en la linea %d" % (name, line_a)))
    return bad


def check_dangling(path, lines):
    """Solo canta si la linea anterior cerro una sentencia: entonces el operador
    suelto es un resto de edicion, no una continuacion."""
    bad = []
    for i, line in enumerate(lines):
        s = line.strip()
        if not (s.startswith('+ ') or s.startswith('+ (') or s.startswith('&&') or s.startswith('||')):
            continue
        j = i - 1
        while j >= 0 and not lines[j].strip():
            j -= 1
        if j < 0:
            continue
        prev = lines[j].strip()
        if prev.endswith(';') or prev.endswith('}') or prev.endswith('{'):
            bad.append((path, i + 1, 'sentencia suelta: ' + s[:60]))
    return bad


def notas_de_puntos(f):
    """
    Aviso blando (no falla el build): la casa no remata lo que escribe con puntos
    suspensivos. Se avisa cuando un `drawString(` lleva el literal "..." en la
    misma linea, que es como vuelven a entrar los recortes con puntos.
    """
    notas = []
    if '/client/' not in f.replace('\\', '/'):
        return notas
    try:
        lines = open(f, encoding='utf-8').read().split('\n')
    except OSError:
        return notas
    for i, line in enumerate(lines):
        st = line.strip()
        if st.startswith('*') or st.startswith('//'):
            continue
        if 'drawString(' in line and ('"..."' in line or '"\u2026"' in line):
            notas.append((f, i + 1, 'aviso: puntos suspensivos en un texto dibujado'))
    return notas


def notas_de_orden(f):
    """
    Aviso blando (no falla el build): pantallas que llaman a `super.render(` y no
    vuelven a escribir nada despues. Esa es la forma en la que se cuelan los
    textos blancos del juego encima del papel: la pantalla pinta su rotulo, la
    super lo pisa y nadie lo reescribe. Solo se avisa de las pantallas de la
    casa que llevan lista o campos del juego.
    """
    notas = []
    if '/client/screens/' not in f.replace('\\', '/'):
        return notas
    try:
        lines = open(f, encoding='utf-8').read().split('\n')
    except OSError:
        return notas
    last_super = -1
    last_write = -1
    for i, line in enumerate(lines):
        if 'super.render(' in line and not line.strip().startswith('*'):
            last_super = i
        if ('drawString(' in line or 'paint' in line or 'inkPackRows(' in line
                or 'washVanilla' in line or 'renderWidgets(' in line):
            if not line.strip().startswith('*'):
                last_write = i
    if last_super >= 0 and last_super > last_write:
        notas.append((f, last_super + 1,
                      'aviso: super.render() es lo ultimo; el juego puede dejar texto encima'))
    return notas


def main(argv):
    files = []
    for arg in argv or ['src/main/java']:
        if arg.endswith('.java'):
            files.append(arg)
        else:
            files.extend(glob.glob(arg.rstrip('/') + '/**/*.java', recursive=True))
    problems = []
    notas = []
    for f in sorted(files):
        src = open(f, encoding='utf-8').read()
        lines = src.split('\n')
        clean = strip_noise(src)
        problems += check_override(f, lines)
        problems += check_dangling(f, lines)
        problems += check_shadow(f, clean)
        notas += notas_de_orden(f)
        notas += notas_de_puntos(f)
    for path, line, msg in notas:
        print('%s:%d: %s' % (path, line, msg))
    if not problems:
        print('lint_java: %d ficheros, sin problemas (%d avisos de orden)'
              % (len(files), len(notas)))
        return 0
    for path, line, msg in problems:
        print('%s:%d: %s' % (path, line, msg))
    print('lint_java: %d problemas' % len(problems))
    return 1


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
