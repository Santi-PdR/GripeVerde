#!/usr/bin/env python3
"""Lint barato para este repo, sin JDK.

Comprueba errores estructurales que ya nos costaron builds rojos y una regresion
interactiva de la hoja de mundos:
  1. `@Override` seguido de algo que no es un metodo.
  2. Variables locales con el mismo nombre en ambitos anidados (javac: "already defined").
  3. Lineas que empiezan por un operador suelto (`+ (`, `&& ...`) fuera de una expresion,
     sintoma de una sentencia cortada por una edicion a mano.
  4. La hoja de mundos detecta el campo visible y enfocado y delega Enter/Supr
     antes de ejecutar atajos sobre el mundo seleccionado.
  5. El total del archivo, escrito en un worker y leído por la GUI, se publica
     con volatile para que el hilo de render vea el resultado.

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


def check_world_search_keys(path, clean):
    """El buscador de Mundos debe recibir Enter/Supr antes que los atajos de fila."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    signature = 'public boolean keyPressed(int keyCode, int scanCode, int modifiers)'
    start = clean.find(signature)
    if start < 0:
        return [(path, 1, 'Mundos: falta el manejador de teclado esperado')]

    focus_signature = 'private boolean isSearchBoxFocused()'
    focus_start = clean.find(focus_signature)
    if focus_start < 0 or focus_start >= start:
        return [(path, 1, 'Mundos: falta el control del foco del buscador')]
    focus_helper = clean[focus_start:start]
    iterates_search_boxes = re.search(
        r'for\s*\(\s*EditBox\s+box\s*:\s*this\.searchBoxes\s*\)', focus_helper)
    checks_visible_focus = re.search(
        r'if\s*\(\s*box\s*!=\s*null\s*&&\s*box\.visible\s*&&\s*box\.isFocused\(\)\s*\)\s*return\s+true\s*;',
        focus_helper)
    if not iterates_search_boxes or not checks_visible_focus:
        line = clean[:focus_start].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: el guard debe detectar un buscador visible y enfocado')]

    opening = clean.find('{', start + len(signature))
    if opening < 0:
        return [(path, clean[:start].count(chr(10)) + 1,
                 'Mundos: bloque keyPressed incompleto')]
    depth = 0
    closing = -1
    for index in range(opening, len(clean)):
        if clean[index] == '{':
            depth += 1
        elif clean[index] == '}':
            depth -= 1
            if depth == 0:
                closing = index + 1
                break
    method = clean[start:closing if closing >= 0 else len(clean)]
    guard = method.find('if (isSearchBoxFocused()) return super.keyPressed(')
    enter = method.find('GLFW_KEY_ENTER')
    delete = method.find('GLFW_KEY_DELETE')
    if guard < 0 or enter < 0 or delete < 0 or guard >= min(enter, delete):
        line = clean[:start].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: delegar al buscador enfocado antes de atajos Enter/Supr')]
    return []


def check_world_archive_visibility(path, clean):
    """El total del archivo se escribe en un worker y se lee desde el hilo GUI."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    match = re.search(r'\bprivate\s+static\s+([^;=]+?)\s+archiveBytes\s*=', clean)
    if not match:
        return [(path, 1, 'Mundos: falta la declaracion del total del archivo')]
    if not re.search(r'\bvolatile\b', match.group(1)):
        line = clean[:match.start()].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: archiveBytes debe ser volatile para publicar el resultado del worker')]
    return []


def notas_de_chapa(files):
    """
    Aviso blando: dos pantallas de la casa con la misma chapa de registro
    ("REG. VIII"). Las chapas van de I a XIX y cada hoja tiene la suya; cuando
    dos coinciden es que una se copio sin cambiar el numero.
    """
    import re
    notas = []
    seen = {}
    for f in files:
        if '/client/screens/' not in f.replace('\\', '/'):
            continue
        try:
            src = open(f, encoding='utf-8').read()
        except OSError:
            continue
        for m in re.finditer(r'"(REG\.\s+[IVXLC]+)"', src):
            tag = m.group(1)
            line = src[:m.start()].count('\n') + 1
            seen.setdefault(tag, []).append((f, line))
    for tag, hits in sorted(seen.items()):
        if len(hits) > 1:
            for f, line in hits:
                notas.append((f, line, 'aviso: la chapa %s esta en %d pantallas' % (tag, len(hits))))
    return notas


def notas_de_widgets(f):
    """
    Aviso blando: pantallas que NO llaman a super.render y ademas no recorren
    sus widgets (ni `renderables`, ni `children()`, ni `dressed`, ni botones
    propios). En esas hojas un control del juego puede quedar visible para el
    raton y sin dibujar nunca: un boton invisible que igual responde. Se marca
    con `// widgets-ok` cuando se dibujan a mano uno por uno.
    """
    notas = []
    if '/client/screens/' not in f.replace('\\', '/'):
        return notas
    try:
        src = open(f, encoding='utf-8').read()
    except OSError:
        return notas
    if 'widgets-ok' in src:
        return notas
    if 'super.render(' in src:
        return notas
    for marca in ('this.renderables', 'this.children()', 'this.dressed',
                  'this.widgetsWithHelp', 'renderWidgets('):
        if marca in src:
            return notas
    notas.append((f, 1, 'aviso: pantalla sin super.render y sin recorrer sus widgets'))
    return notas


def notas_de_reflexion(f):
    """
    Aviso blando: reflexion sobre clases del juego buscando un nombre fijo
    (`getDeclaredMethod("setSelected")`, `getDeclaredField("ip")`) sin ningun
    barrido por forma cerca. En el juego real esos nombres vienen cambiados, el
    find devuelve null y la pantalla se queda muda sin avisar: es lo que dejo a
    Mundos sin clics. Si el bloque no hace un barrido (`getDeclaredMethods()` /
    `getDeclaredFields()`), se avisa.
    """
    notas = []
    try:
        lines = open(f, encoding='utf-8').read().split('\n')
    except OSError:
        return notas
    for i, line in enumerate(lines):
        st = line.strip()
        if st.startswith('*') or st.startswith('//'):
            continue
        if any('reflexion-ok' in lines[k] for k in range(max(0, i - 3), i + 1)):
            continue
        if 'getDeclaredMethod("' in line or 'getDeclaredField("' in line:
            vecino = '\n'.join(lines[max(0, i - 40):i + 40])
            if ('getDeclaredMethods()' in vecino or 'getDeclaredFields()' in vecino
                    or 'getDeclaredConstructors()' in vecino):
                continue
            notas.append((f, i + 1,
                          'aviso: reflexion por nombre sin barrido por forma cerca'))
    return notas


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
        problems += check_world_search_keys(f, clean)
        problems += check_world_archive_visibility(f, clean)
        notas += notas_de_orden(f)
        notas += notas_de_puntos(f)
        notas += notas_de_reflexion(f)
        notas += notas_de_widgets(f)
    notas += notas_de_chapa(files)
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
