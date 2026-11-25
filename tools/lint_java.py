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
     con volatile; al reabrir, un escaneo nuevo cancela y reemplaza el anterior.
  6. Las texturas dinámicas de mundos borrados se liberan al reabrir la hoja;
     los iconos ausentes se vuelven a comprobar.
  7. Las miniaturas de Mundos dibujan la imagen completa, ajustada sin recorte.
  8. Multiplayer libera favicons obsoletos, reintenta los pings pendientes y limita
     los PNG remotos antes de decodificar.
  9. Mundos valida y limita icon.png antes de decodificarlo.
 10. Multiplayer deja al ServerSelectionList nativo gestionar una sola vez los
     clics de servidor y LAN, sin falsos dobles clics en los márgenes.
 11. Mundos devuelve el foco a la lista al consumir un doble clic de fila, para
     que Enter no siga escribiendo en el buscador.
 12. La inspeccion del reloj en la portada es modal: clics y teclas la cierran
     sin activar controles ocultos.
 13. Al elegir una direccion reciente, Unirse vuelve a dar el foco de teclado
     al campo de direccion.
 14. Las direcciones recientes largas caben en la ficha y conservan su valor real
     al seleccionarlas.
 15. Mundos no traduce clics de fila al icono de acceso rápido ni reserva una
     franja geométrica sin controles que eluda la guarda del doble clic.
 16. Packs dibuja una sola pasada sobre el papel, repone los rótulos después
     de la banda opaca y cubre la columna de texto vanilla sin tocar su entrada.
 17. El velo compartido viste las filas visibles aunque la lista tenga más de
     64 elementos y sin recorrer cada elemento fuera del viewport.
 18. Mundos solo selecciona dentro de la fila real, cubre el texto vanilla antes
     de volver a pintarlo y conserva visible el aviso experimental de Forge.

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


def java_method_body(clean, signature):
    start = clean.find(signature)
    if start < 0:
        return ''
    opening = clean.find('{', start + len(signature))
    depth = 0
    for index in range(opening, len(clean)):
        if clean[index] == '{':
            depth += 1
        elif clean[index] == '}':
            depth -= 1
            if depth == 0:
                return clean[start:index + 1]
    return ''


def check_world_click_suppression_focus(path, clean):
    """Direct selection still transfers focus away from search."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    handler = java_method_body(clean, 'public boolean mouseClicked(')
    focus = java_method_body(clean, 'private void focusWorldList(')
    if ('this.vanillaList.setSelected(entry);' not in handler
            or 'focusWorldList();' not in handler
            or any(token not in focus for token in (
                'box.setFocused(false)', 'this.setFocused(this.vanillaList)',
                'this.vanillaList.setFocused(true)'))):
        return [(path, 1, 'Mundos: seleccionar y enfocar la lista sin activar una fila')]
    return []


def check_world_row_click_safety(path, clean):
    """Pointer selection never calls native entry handlers or invents coordinates."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    handler = java_method_body(clean, 'public boolean mouseClicked(')
    hit = java_method_body(clean, 'private Object worldRowAt(')
    required = ('widgetAt(mouseX, mouseY)', 'Object row = worldRowAt(mouseX, mouseY);',
                'button == 0 && !summaryOf(row).isDisabled()', 'this.vanillaList.setSelected(entry);')
    if (any(t not in handler for t in required)
            or handler.count('super.mouseClicked(') != 2
            or 'rowSelectionClickX' in handler or 'joinWorld(' in handler
            or '.mouseClicked(' in handler.replace('super.mouseClicked(', '')):
        return [(path, 1, 'Mundos: clic solo selecciona, sin entrada inmediata ni doble clic vanilla')]
    if any(t not in hit for t in (
            'mouseX < ledgerLeft() + 6', 'mouseX >= ledgerRight() - 12',
            'mouseY < this.listTop', 'mouseY >= this.listBottom',
            'summaryOf(row) == null', 'mouseY < top + rowStep() - 4')):
        return [(path, 1, 'Mundos: hover y clic comparten limites de fila sin margenes ni huecos')]
    return []


def check_visible_list_row_iteration(path, clean):
    """Cosmetic list passes visit the viewport, not just the first 64 entries."""
    if not path.replace(chr(92), '/').endswith('/client/render/DiaryChrome.java'):
        return []

    def method_body(signature):
        start = clean.find(signature)
        if start < 0:
            return '', -1
        opening = clean.find('{', start + len(signature))
        if opening < 0:
            return '', start
        depth = 0
        for index in range(opening, len(clean)):
            if clean[index] == '{':
                depth += 1
            elif clean[index] == '}':
                depth -= 1
                if depth == 0:
                    return clean[start:index + 1], start
        return '', start

    first, first_start = method_body(
        'public static int firstVisibleRow(AbstractSelectionList<?> list, int count,')
    wash, wash_start = method_body(
        'public static void washVanillaRows(GuiGraphics gfx, AbstractSelectionList<?> list)')
    if (not first or 'rowTopOf(list, 0)' not in first
            or 'readInt(list, "", "")' not in first
            or 'Math.floorDiv(top - firstTop, rowStep) - 1' not in first
            or '(int) Math.floor(scrollAmount / rowStep) - 2' not in first
            or 'Math.max(0, Math.min(count, first))' not in first
            or not wash or 'firstVisibleRow(list, count, top, step, scroll)' not in wash
            or 'top + 4 - (int) scroll + i * step + headerHeight' not in wash
            or 'i < 64' in wash or 'if (rowTop > bottom) break;' not in wash):
        line_start = first_start if first_start >= 0 else wash_start
        line = clean[:line_start].count(chr(10)) + 1 if line_start >= 0 else 1
        return [(path, line,
                 'Listas: vestir filas visibles también después de las primeras 64 entradas')]
    return []


def check_pack_selection_single_render(path, clean):
    """Packs paints the paper first; the vanilla pass must not leak through it."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryPackSelectionScreen.java'):
        return []

    def method_body(signature):
        start = clean.find(signature)
        if start < 0:
            return '', -1
        opening = clean.find('{', start + len(signature))
        if opening < 0:
            return '', start
        depth = 0
        for index in range(opening, len(clean)):
            if clean[index] == '{':
                depth += 1
            elif clean[index] == '}':
                depth -= 1
                if depth == 0:
                    return clean[start:index + 1], start
        return '', start

    render, render_start = method_body(
        'public void render(GuiGraphics g, int mouseX, int mouseY, float partial)')
    if not render:
        return [(path, 1, 'Packs: falta el render de la hoja')]
    draw_page = render.find('drawPage(g);')
    first_flush = render.find('g.flush();')
    contents = render.find('renderContentsOnPage(g, mouseX, mouseY, partial);')
    second_flush = render.find('g.flush();', first_flush + 1)
    rows = render.find('DiaryChrome.washVanillaRows(')
    if ('super.render(' in render or draw_page < 0 or first_flush < draw_page
            or contents < first_flush or second_flush < contents or rows < second_flush
            or render.count('renderContentsOnPage(g, mouseX, mouseY, partial);') != 1
            or 'washVanillaListBox(' in render):
        line = clean[:render_start].count(chr(10)) + 1
        return [(path, line,
                 'Packs: dibujar fondo, una sola pasada de filas y sin marco/texto vanilla duplicado')]

    page, page_start = method_body('private void drawPage(GuiGraphics g)')
    if 'gripeverde.options.packs.subtitle' in page:
        line = clean[:page_start].count(chr(10)) + 1
        return [(path, line, 'Packs: dejar una sola linea secundaria en la cabecera')]

    column, column_start = method_body(
        'private void drawPackColumn(GuiGraphics g, AbstractSelectionList<?> list)')
    headings, headings_start = method_body('private void drawPackColumnHeadings(GuiGraphics g)')
    heading, heading_start = method_body(
        'private void drawPackColumnHeading(GuiGraphics g, AbstractSelectionList<?> list,')
    header_fill = render.find('g.fill(bandLeft, bandTop, bandRight, bandBottom')
    subtitle_draw = render.find('g.drawString(this.font, subCut')
    headings_draw = render.find('drawPackColumnHeadings(g);')
    if (not column or 'list.getBottom()' not in column
            or 'int h = bottom - top;' not in column or 'bottom + 6' not in column
            or 'list.getHeight()' in column
            or not headings or 'this.availableList' not in headings
            or 'this.selectedList' not in headings
            or not heading or 'list.getTop()' not in heading
            or 'PACK_LIST_HEADER_HEIGHT' not in heading
            or 'PACK_LIST_HEADER_HEIGHT = 13' not in clean
            or 'g.fill(x1 + 1, top, x2 - 1, bottom' not in heading
            or 'g.drawString(this.font, shown' not in heading
            or header_fill < 0 or subtitle_draw < header_fill
            or headings_draw < subtitle_draw):
        line_start = next((start for start in (column_start, headings_start, heading_start, render_start)
                           if start >= 0), 0)
        line = clean[:line_start].count(chr(10)) + 1
        return [(path, line,
                 'Packs: limitar cajones al viewport y pintar sus rótulos después de la cabecera opaca')]

    rows_paint, rows_start = method_body('private void inkPackRows(')
    if (not rows_paint or 'int x1 = left + 33;' not in rows_paint
            or 'int textX = left + 34;' not in rows_paint
            or 'int x2 = right - 8;' not in rows_paint
            or 'int textW = x2 - textX - 1;' not in rows_paint
            or 'entry.getExtendedDescription()' not in rows_paint
            or 'DiaryChrome.firstVisibleRow(list, count, top, step, scroll)' not in rows_paint
            or 'PACK_LIST_HEADER_HEIGHT' not in rows_paint
            or 'i < 64' in rows_paint or 'right - 26' in rows_paint):
        line_start = rows_start if rows_start >= 0 else render_start
        line = clean[:line_start].count(chr(10)) + 1
        return [(path, line,
                 'Packs: respetar la zona de texto vanilla de 157 px y su descripción extendida')]
    clip_scope = re.search(
        r'g\.enableScissor\(x1, top, x2, bottom\);\s*try\s*\{', rows_paint)
    clip_release = re.search(r'\}\s*finally\s*\{\s*g\.disableScissor\(\);\s*\}', rows_paint)
    if (not clip_scope or not clip_release
            or rows_paint.count('g.enableScissor(') != 1
            or rows_paint.count('g.disableScissor();') != 1
            or rows_paint.find('g.fill(') < clip_scope.end()
            or rows_paint.find('int first =') > clip_scope.start()
            or 'scissored' in rows_paint):
        line = clean[:rows_start].count(chr(10)) + 1
        return [(path, line,
                 'Packs: adquirir recorte antes de pintar y liberarlo siempre en finally')]
    if ('textX, rowTop + 1,' not in rows_paint
            or '.renderLeftAligned(g, textX, rowTop + 12, 10,' not in rows_paint
            or 'if (drawBottom <= drawTop) continue;' not in rows_paint
            or 'g.fill(x1 + 3, rowBottom - 1, x2 - 3, rowBottom,' not in rows_paint
            or re.search(r'textX\s*,\s*drawTop\s*\+', rows_paint)):
        line = clean[:rows_start].count(chr(10)) + 1
        return [(path, line,
                 'Packs: anclar texto y separador a la fila real, no al borde del viewport')]
    warning, warning_start = method_body('private boolean showPackCompatibilityWarning(')
    warning_tokens = (
        'entry.getCompatibility().isCompatible()) return false;',
        'entry.isFixedPosition() && entry.isRequired()) return false;',
        'list.isMouseOver(mouseX, mouseY)',
        'child instanceof GuiEventListener row && row.isMouseOver(mouseX, mouseY)',
        'this.minecraft.options.touchscreen().get() || hovered',
        'list.getSelected() == child && list.isFocused()',
    )
    if (not warning or any(token not in warning for token in warning_tokens)
            or 'showPackCompatibilityWarning(list, child, entry, mouseX, mouseY)' not in rows_paint
            or 'compatibilityDescription = entry.getCompatibility().getDescription();' not in rows_paint
            or 'packDescription(child, shownDescription, textW)' not in rows_paint
            or '? compatibilityDescription : description;' not in rows_paint
            or '.renderLeftAligned(g, textX, rowTop + 12, 10,' not in rows_paint):
        line = clean[:max(0, warning_start)].count(chr(10)) + 1
        return [(path, line,
                 'Packs: reponer el aviso de incompatibilidad con hover, foco y pantalla táctil vanilla')]
    description_cache, cache_start = method_body('private MultiLineLabel packDescription(')
    cache_tokens = (
        'cached.width() == width && cached.text().equals(text)',
        'return cached.label();',
        'this.packDescriptions.size() >= 256',
        'MultiLineLabel.create(this.font, text, width, 2)',
        'new CachedPackDescription(text.copy(), width, label)',
    )
    removed, _ = method_body('public void removed()')
    init_cache, _ = method_body('protected void init()')
    if (not description_cache or any(token not in description_cache for token in cache_tokens)
            or 'this.packDescriptions.clear();' not in description_cache
            or 'this.packDescriptions.clear();' not in init_cache
            or 'this.packDescriptions.clear();' not in removed
            or 'super.removed();' not in removed
            or 'description = d == null ? Component.empty() : d;' not in rows_paint):
        line = clean[:max(0, cache_start)].count(chr(10)) + 1
        return [(path, line,
                 'Packs: conservar dos líneas de descripción con caché acotada e invalidada')]
    mouse_handler = re.search(
        r'\b(?:public|protected)\s+boolean\s+mouse(?:Clicked|Released|Dragged|Scrolled)\s*\(', clean)
    if mouse_handler:
        line = clean.count('\n', 0, mouse_handler.start()) + 1
        return [(path, line,
                 'Packs: mantener el despacho de ratón en las listas vanilla')]

    init, init_start = method_body('protected void init()')
    resolver, resolver_start = method_body('private void resolvePackLists()')
    reset = init.find('this.availableList = null;')
    clear_dressed = init.find('this.dressed.clear();')
    super_init = init.find('super.init();')
    resolve = init.find('resolvePackLists();')
    dress = init.find('PaperButtons.dressAll(this)')
    if (not init or not resolver or reset < 0 or clear_dressed < 0
            or super_init < max(reset, clear_dressed) or resolve < super_init or dress < resolve
            or 'this.selectedList = null;' not in init
            or 'this.children()' not in resolver
            or 'instanceof AbstractSelectionList<?> list' not in resolver
            or 'lists.sort(' not in resolver or 'left.getLeft()' not in resolver
            or 'right.getLeft()' not in resolver
            or 'this.availableList = lists.get(0)' not in resolver
            or 'this.selectedList = lists.get(1)' not in resolver
            or 'getDeclaredFields()' in resolver):
        line = clean[:(init_start if init_start >= 0 else resolver_start)].count(chr(10)) + 1
        return [(path, line,
                 'Packs: reasignar y ordenar las listas vanilla en cada init sin depender de reflexion')]
    return []


def check_world_list_overlay_and_hitbox(path, clean):
    """Clip the native drawing completely; no transparent Forge-warning column."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    paper = java_method_body(clean, 'private void paintListPaper(')
    hidden = java_method_body(clean, 'private void renderNativeListHidden(')
    rows = java_method_body(clean, 'private void paintWorldRowsInk(')
    tooltip = java_method_body(clean, 'private void drawWorldWarningTooltip(')
    if ('g.fill(x1, top, x2, bottom, opaquePaper);' not in paper
            or 'withAlpha(paper, 255)' not in paper or 'preserveForgeWarning' in paper
            or 'g.enableScissor(0, 0, 0, 0);' not in hidden
            or 'this.vanillaList.render(g, -1, -1, partial);' not in hidden
            or 'finally { g.disableScissor(); }' not in hidden
            or 'summary.isLifecycleExperimental()' not in rows
            or 'summary.isLifecycleExperimental()' not in tooltip
            or 'g.renderTooltip(' not in tooltip):
        return [(path, 1, 'Mundos: una sola capa visible y aviso experimental propio con tooltip')]
    return []


def check_title_watch_inspection_modal(path, clean):
    """La inspeccion del reloj no debe filtrar eventos a botones invisibles."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryTitleScreen.java'):
        return []

    def method_body(signature):
        start = clean.find(signature)
        if start < 0:
            return '', -1
        opening = clean.find('{', start + len(signature))
        if opening < 0:
            return '', start
        depth = 0
        for index in range(opening, len(clean)):
            if clean[index] == '{':
                depth += 1
            elif clean[index] == '}':
                depth -= 1
                if depth == 0:
                    return clean[start:index + 1], start
        return '', start

    checks = (
        ('public boolean keyPressed(int keyCode, int scanCode, int modifiers)',
         'GLFW_KEY_S', 'teclado'),
        ('public boolean charTyped(char codePoint, int modifiers)',
         'super.charTyped(', 'texto'),
        ('public boolean mouseClicked(double mouseX, double mouseY, int button)',
         'if (button == 0)', 'raton'),
    )
    for signature, later_action, label in checks:
        method, start = method_body(signature)
        guard = method.find('PocketWatchRenderer.isInspecting()')
        close = method.find('PocketWatchRenderer.closeInspection()', guard)
        consumed = method.find('return true;', close)
        action = method.find(later_action, consumed if consumed >= 0 else guard)
        if (not method or start < 0 or guard < 0 or close < 0 or consumed < close
                or action < 0 or guard > action or consumed > action):
            line = clean[:start].count(chr(10)) + 1 if start >= 0 else 1
            return [(path, line,
                     'Portada: la inspeccion debe cerrar y consumir la entrada de ' + label)]
    click_method, click_start = method_body(
        'public boolean mouseClicked(double mouseX, double mouseY, int button)')
    if 'if (!overButton && mouseX >= logoCx + 1' not in click_method:
        line = clean[:click_start].count(chr(10)) + 1 if click_start >= 0 else 1
        return [(path, line,
                 'Portada: el punto del emblema no debe activarse encima de un boton real')]
    return []


def check_direct_join_recent_address_focus(path, clean):
    """Tocar una direccion recordada debe enfocar el campo de texto completo."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryDirectJoinScreen.java'):
        return []
    signature = 'public boolean mouseClicked(double mouseX, double mouseY, int button)'
    start = clean.find(signature)
    if start < 0:
        return [(path, 1, 'Unirse: falta el manejador de direcciones recientes')]
    opening = clean.find('{', start + len(signature))
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
    handler = clean[start:closing] if closing >= 0 else ''
    value = handler.find('this.ipEdit.setValue(this.recentShown.get(i));')
    screen_focus = handler.find('this.setFocused(this.ipEdit);', value)
    widget_focus = handler.find('this.ipEdit.setFocused(true);', value)
    update_state = handler.find('updateSelectButton();', value)
    consumed = handler.find('return true;', value)
    if (value < 0 or screen_focus < value or widget_focus < screen_focus
            or update_state < widget_focus or consumed < update_state):
        line = clean[:start].count(chr(10)) + 1
        return [(path, line,
                 'Unirse: seleccionar una direccion reciente debe sincronizar foco y validacion')]
    return []


def check_direct_join_recent_address_fit(path, clean):
    """La etiqueta reciente se recorta, pero el clic conserva la direccion real."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryDirectJoinScreen.java'):
        return []
    render_signature = 'public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial)'
    render_start = clean.find(render_signature)
    block_start = clean.find('int maxW = Math.max(60, panelW - 48);', render_start)
    block_end = clean.find('this.recentY = noteY;', block_start)
    if min(render_start, block_start, block_end) < 0:
        line = clean[:render_start].count(chr(10)) + 1 if render_start >= 0 else 1
        return [(path, line, 'Unirse: no se puede comprobar el ancho de direcciones recientes')]
    block = clean[block_start:block_end]
    required = (
        'java.util.List<String> fitAddresses',
        'java.util.List<String> fitLabels',
        'int available = maxW - total - gapW;',
        'String label = ellipsizeTo(this.font, r, available);',
        'fitAddresses.add(r);',
        'fitLabels.add(label);',
        'for (int i = 0; i < fitLabels.size(); i++)',
        'String label = fitLabels.get(i);',
        'int rw = DiaryChrome.widthCached(this.font, label);',
        'gfx.drawString(this.font, label, cx2, noteY, DiaryChrome.INK_SUBTITLE, false);',
        'this.recentWs.add(rw);',
        'this.recentShown.add(fitAddresses.get(i));',
    )
    cursor = 0
    for token in required:
        position = block.find(token, cursor)
        if position < 0:
            line = clean[:block_start].count(chr(10)) + 1
            return [(path, line,
                     'Unirse: limitar el ancho visible de recientes sin truncar su direccion')]
        cursor = position + len(token)
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
    generation_guards = (
        'AtomicLong ARCHIVE_SCAN_GENERATION',
        'scanGeneration = ARCHIVE_SCAN_GENERATION.incrementAndGet()',
        'scanGeneration == ARCHIVE_SCAN_GENERATION.get()',
        'scanGeneration != ARCHIVE_SCAN_GENERATION.get()',
    )
    if any(token not in clean for token in generation_guards):
        line = clean[:match.start()].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: cancelar y descartar escaneos de archivo reemplazados')]
    return []


def check_world_icon_cache_lifecycle(path, clean):
    """Mundos libera texturas de fichas borradas y reintenta iconos ausentes."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    init_start = clean.find('protected void init()')
    init_end = clean.find('private void measureArchiveAsync()', init_start)
    if init_start < 0 or init_end < 0:
        return [(path, 1, 'Mundos: no se puede comprobar la limpieza de iconos')]
    init_block = clean[init_start:init_end]
    if 'this.worldIconCachePruned = pruneWorldIconCache();' not in init_block:
        line = clean[:init_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: limpiar el cache de iconos al abrir la hoja')]

    release_start = clean.find('private void releaseWorldIcon(')
    prune_start = clean.find('private boolean pruneWorldIconCache()')
    icon_start = clean.find('private net.minecraft.resources.ResourceLocation worldIcon(', prune_start)
    if min(release_start, prune_start, icon_start) < 0 or release_start >= prune_start:
        return [(path, 1, 'Mundos: falta el liberador del cache de iconos')]
    release = clean[release_start:prune_start]
    prune = clean[prune_start:icon_start]
    release_tokens = (
        'WORLD_ICONS.remove(folder)',
        'WORLD_ICON_SIZES.remove(folder)',
        'WORLD_ICON_FINGERPRINTS.remove(folder)',
        'getTextureManager().release(icon)',
    )
    prune_tokens = (
        'liveWorlds.contains(folder)',
        'worldIconFingerprint(saves.resolve(folder).resolve(""))',
        'cached.equals(current)',
        'releaseWorldIcon(folder)',
        'WORLD_ICONS_ABSENT.clear()',
    )
    if (any(token not in release for token in release_tokens)
            or any(token not in prune for token in prune_tokens)):
        line = clean[:prune_start].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: liberar iconos borrados o modificados y reintentar ausentes')]
    if 'WORLD_ICON_FINGERPRINTS.put(levelId, source.fingerprint())' not in clean:
        line = clean[:prune_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: guardar la huella de cada icono cargado')]
    fingerprint_start = clean.find('private static WorldIconFingerprint worldIconFingerprint(')
    fingerprint_end = clean.find('private static WorldIconData readWorldIconData(', fingerprint_start)
    if min(fingerprint_start, fingerprint_end) < 0:
        return [(path, 1, 'Mundos: falta la huella de modificación del icono')]
    fingerprint = clean[fingerprint_start:fingerprint_end]
    if any(token not in fingerprint for token in (
            'Files.readAttributes(path,',
            'attributes.isRegularFile()',
            'attributes.size()',
            'attributes.lastModifiedTime()')):
        line = clean[:fingerprint_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: guardar tamaño y fecha de icon.png')]
    return []


def check_world_icon_fits_box(path, clean):
    """Mundos escala el icono entero al recuadro, sin dibujar solo una esquina."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    draw_start = clean.find('private static boolean drawWorldIcon(')
    icon_start = clean.find('private net.minecraft.resources.ResourceLocation worldIcon(', draw_start)
    if draw_start < 0 or icon_start < 0:
        return [(path, 1, 'Mundos: falta el dibujo completo de miniaturas')]
    draw = clean[draw_start:icon_start]
    required = (
        'WORLD_ICON_SIZES.get(levelId)',
        'g.pose().scale(drawWidth / (float) size.width(),',
        'drawHeight / (float) size.height(), 1.0F',
        'g.blit(icon, 0, 0, 0, 0, size.width(), size.height(),',
    )
    if any(token not in draw for token in required):
        line = clean[:draw_start].count(chr(10)) + 1
        return [(path, line,
                 'Mundos: escalar el icono completo en vez de recortar la esquina superior')]
    if 'drawWorldIcon(g, folder, icon,' not in clean:
        line = clean[:draw_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: usar el escalado de icono al dibujar cada ficha')]
    if 'WORLD_ICON_SIZES.put(levelId, new WorldIconSize(imageWidth, imageHeight))' not in clean:
        line = clean[:draw_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: conservar las dimensiones reales de cada icono')]
    if 'WORLD_ICON_TEXTURE_IDS.incrementAndGet()' not in clean:
        line = clean[:draw_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: asignar un ID de textura único después de liberar caché')]
    return []


def check_world_icon_input_guard(path, clean):
    """Mundos limita bytes y dimensiones del icon.png antes de decodificarlo."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiarySelectWorldScreen.java'):
        return []
    read_start = clean.find('private static WorldIconData readWorldIconData(')
    read_end = clean.find('private static int readWorldPngInt(', read_start)
    icon_start = clean.find('private net.minecraft.resources.ResourceLocation worldIcon(', read_end)
    if min(read_start, read_end, icon_start) < 0:
        return [(path, 1, 'Mundos: falta la validación del icon.png')]
    reader = clean[read_start:read_end]
    required = (
        'before.size() > MAX_WORLD_ICON_BYTES',
        'in.readNBytes(MAX_WORLD_ICON_BYTES + 1)',
        'bytes.length != before.size()',
        '(bytes[0] & 0xFF) != 0x89',
        'readWorldPngInt(bytes, 8) != 13',
        'readWorldPngInt(bytes, 16)',
        'readWorldPngInt(bytes, 20)',
        'MAX_WORLD_ICON_DIMENSION',
        '!before.equals(after)',
    )
    if any(token not in reader for token in required):
        line = clean[:read_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: limitar tamaño y dimensiones antes de decodificar')]
    icon = clean[icon_start:]
    read_at = icon.find('readWorldIconData(path)')
    decode_at = icon.find('NativeImage.read(in)')
    if read_at < 0 or decode_at < 0 or read_at >= decode_at:
        line = clean[:icon_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: validar icon.png antes de decodificarlo')]
    if 'imageWidth != source.width() || imageHeight != source.height()' not in icon:
        line = clean[:icon_start].count(chr(10)) + 1
        return [(path, line, 'Mundos: verificar dimensiones reales tras decodificar')]
    return []


def check_server_icon_safety_and_lifecycle(path, clean):
    """Multiplayer poda texturas obsoletas y limita PNG de favicons remotos."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryMultiplayerScreen.java'):
        return []
    init_start = clean.find('protected void init()')
    prune_start = clean.find('private boolean pruneServerIconCache()', init_start)
    if init_start < 0 or prune_start < 0:
        return [(path, 1, 'Multiplayer: falta la limpieza del cache de favicons')]
    init = clean[init_start:prune_start]
    if 'this.serverIconCachePruned = pruneServerIconCache();' not in init:
        line = clean[:init_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: limpiar favicons al abrir la hoja')]

    ensure_start = clean.find('private void ensurePinnedServer()', prune_start)
    if ensure_start < 0:
        return [(path, 1, 'Multiplayer: no se puede comprobar la poda de favicons')]
    prune = clean[prune_start:ensure_start]
    prune_tokens = (
        'this.vanillaServers.size()',
        'this.vanillaServers.get(i)',
        'liveServers.contains(address)',
        'SERVER_ICONS.remove(address)',
        'SERVER_ICON_SIZES.remove(address)',
        'getTextureManager().release(icon)',
        'SERVER_ICON_RETRY_AT.clear()',
    )
    if any(token not in prune for token in prune_tokens):
        line = clean[:prune_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: liberar caches de servidores borrados')]

    refresh_start = clean.find('private void onRefresh()')
    invalidate_start = clean.find('private void invalidateServerIconCache()', refresh_start)
    cancel_start = clean.find('private void onCancel()', invalidate_start)
    if min(refresh_start, invalidate_start, cancel_start) < 0:
        return [(path, 1, 'Multiplayer: refrescar favicon al actualizar la lista')]
    refresh = clean[refresh_start:invalidate_start]
    invalidate = clean[invalidate_start:cancel_start]
    if (refresh.find('invalidateServerIconCache();') < 0
            or refresh.find('invalidateServerIconCache();') > refresh.find('refreshList();')
            or 'SERVER_ICONS.clear()' not in invalidate
            or 'SERVER_ICON_SIZES.clear()' not in invalidate
            or 'SERVER_ICON_RETRY_AT.clear()' not in invalidate
            or 'getTextureManager().release(icon)' not in invalidate):
        line = clean[:refresh_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: invalidar texturas antes de volver a consultar servidores')]

    header_start = clean.find('private static boolean hasSafeServerIconHeader(byte[] bytes)')
    header_end = clean.find('private static int readPngInt(', header_start)
    icon_start = clean.find('private net.minecraft.resources.ResourceLocation serverIcon(', header_end)
    if min(header_start, header_end, icon_start) < 0:
        return [(path, 1, 'Multiplayer: falta el validador de cabecera PNG')]
    header = clean[header_start:header_end]
    header_tokens = (
        'MAX_SERVER_ICON_BYTES',
        'MAX_SERVER_ICON_DIMENSION',
        '(bytes[0] & 0xFF) != 0x89',
        'readPngInt(bytes, 8) != 13',
        'readPngInt(bytes, 16)',
        'readPngInt(bytes, 20)',
    )
    if any(token not in header for token in header_tokens):
        line = clean[:header_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: limitar tamaño y dimensiones del PNG remoto')]

    icon = clean[icon_start:]
    validate_at = icon.find('hasSafeServerIconHeader(bytes)')
    decode_at = icon.find('NativeImage.read(in)')
    if validate_at < 0 or decode_at < 0 or validate_at >= decode_at:
        line = clean[:icon_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: validar el favicon antes de decodificarlo')]
    retry_guard = icon.find('System.currentTimeMillis() < retryAt')
    field_scan = icon.find('ServerData.class.getDeclaredFields()')
    if (retry_guard < 0 or field_scan < 0 or retry_guard >= field_scan
            or 'SERVER_ICON_RETRY_AT.get(key)' not in icon
            or 'SERVER_ICON_RETRY_AT.put(key,' not in icon
            or 'SERVER_ICON_RETRY_AT.remove(key)' not in icon):
        line = clean[:icon_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: volver a comprobar los iconos tras completar el ping')]
    if 'SERVER_ICON_TEXTURE_IDS.incrementAndGet()' not in icon:
        line = clean[:icon_start].count(chr(10)) + 1
        return [(path, line, 'Multiplayer: asignar un ID de textura único al favicon')]
    return []


def check_multiplayer_native_click_handling(path, clean):
    """La lista vanilla debe gestionar el doble clic y LAN una sola vez."""
    if not path.replace(chr(92), '/').endswith('/client/screens/DiaryMultiplayerScreen.java'):
        return []
    signature = 'public boolean mouseClicked(double mouseX, double mouseY, int button)'
    start = clean.find(signature)
    if start >= 0:
        opening = clean.find('{', start + len(signature))
        if opening < 0:
            return [(path, clean[:start].count(chr(10)) + 1,
                     'Multiplayer: manejador de clic incompleto')]
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
        handler = clean[start:closing] if closing >= 0 else ''
        if not handler or 'onJoin();' in handler:
            line = clean[:start].count(chr(10)) + 1
            return [(path, line,
                     'Multiplayer: no duplicar el doble clic que ya maneja ServerSelectionList')]
    stale_tokens = ('lastListClickEntry', 'lastListClickMs', 'serverEntryAt(')
    if any(token in clean for token in stale_tokens):
        return [(path, 1,
                 'Multiplayer: quitar el hit-test duplicado y conservar los clics nativos de LAN')]
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
        problems += check_world_click_suppression_focus(f, clean)
        problems += check_world_row_click_safety(f, clean)
        problems += check_pack_selection_single_render(f, clean)
        problems += check_visible_list_row_iteration(f, clean)
        problems += check_world_list_overlay_and_hitbox(f, clean)
        problems += check_title_watch_inspection_modal(f, clean)
        problems += check_direct_join_recent_address_focus(f, clean)
        problems += check_direct_join_recent_address_fit(f, clean)
        problems += check_world_archive_visibility(f, clean)
        problems += check_world_icon_cache_lifecycle(f, clean)
        problems += check_world_icon_fits_box(f, clean)
        problems += check_world_icon_input_guard(f, clean)
        problems += check_server_icon_safety_and_lifecycle(f, clean)
        problems += check_multiplayer_native_click_handling(f, clean)
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
