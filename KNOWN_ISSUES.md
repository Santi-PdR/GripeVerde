# KNOWN_ISSUES - Eternal Craft: Gripe Verde

## v0.11.14 - Banderas pixel-art (KI-08 CERRADO)

Se agrega bandera 16x11 px a la izquierda de cada entrada en DiaryLanguageScreen.
Idiomas sin bandera mapeada muestran rombo tinta como fallback.

### Cambios
- **Nuevo**: client/render/FlagRenderer.java (~700 lineas). Registro Map<String, int[][]>
  con ~90 codigos de idioma. Helpers hBands, vBands, h2, nordicCross. API publica draw() + has().
- **Modificado**: client/screens/DiaryLanguageScreen.java. FLAG_SLOT=22 reservados a la
  izquierda. Bandera en left+3, y centrada. Texto centrado en zona derecha.

### Cobertura
- Ingles: 7 variantes (US, UK, CA, AU, NZ, Pirate, upside-down).
- Espanol: 22 variantes (todas las hispanas americanas + ES + AN + 419).
- Portugues, Frances, Aleman (3), Italiano, Neerlandes, Belga, Irlandes.
- Nordicos completos, Este europeo, Baltico, Griego, Turco.
- Asia oriental (JA/KO/ZH x3), Sudeste (VI/TH/ID/FIL/MS).
- Israel, Arabia, Iran, Sudafrica, Georgia, Armenia, Azerbaiyan, Kazajistan, Mongolia.
- Regionales: Cataluna, Vasco, Galicia, Wales, Scotland.

### Errores catalogados
- **E108**: PowerShell interactivo pego "pri" suelto + basura "───────" en here-string
  largo. Similar a E39. Fix: pos-limpieza puntual con .Replace().
- **E109**: Ancla asumida "## Version 0.11.13" no existia; el CHANGELOG usa "## v0.11.13"
  (sin la palabra "Version"). El throw dentro de if en PS interactivo no aborta.
  Refuerza R-PS-THROW y R-VERIFY-DISK.
- **E110**: client.jar de 1.20.1 solo trae en_us.json. Los demas idiomas se descargan
  como assets con hash. No se pueden auditar los codigos por bytecode.

### Reglas nuevas
- **R-CHANGELOG-ANCHOR-CONVENTION**: verificar la convencion real del archivo antes de
  asumir el ancla. CHANGELOG usa "## v0.X.Y" para v0.10+ y "## Version 0.X.Y" para v0.9-.
  Preferir IndexOf() con validacion explicita.
- **R-VANILLA-LANGS-1.20**: en Minecraft 1.20+ los idiomas NO estan en el client.jar.
  Solo en_us. Para auditar codigos usar reflection en runtime.

### KI-08 (CERRADO en v0.11.14)
- Banderas pixel-art hardcoded implementadas. Fallback rombo tinta para no mapeados.
- Deuda visual menor resuelta en el mismo release (Uruguay, Mexico, Ecuador, Vasco).
- Cerrada tras 3 iteraciones, respetando R-ITER-VISUAL.

---
## v0.11.10 a v0.11.13 - Cierre del ciclo del gabinete

Cinco versiones cortas encadenadas cierran los ultimos hitos visuales del gabinete pergamino. Se agrupa aqui porque forman un mismo ciclo narrativo.

### v0.11.10 - Polish: fades, scrollbar y multiplayer
- KI-15 (fades inferiores) CERRADO. Nuevo helper DiaryChrome.drawListFades()
  aplicado en Accessibility, Chat y Sound. Fade pergamino->transparente en top y
  bottom de la lista suaviza el corte duro.
- KI-16 (scrollbar respirada) CERRADO. drawStyledScrollThumbInPanel ahora
  usa panelRightEdge-18 (antes 14). Ademas la tapa sobre la barra vanilla es
  condicional: solo se dibuja si la barra cae dentro del panel visible, para no
  generar columnas fantasma en paneles anchos.
- Multiplayer: banda sepia detras de los servers atenuada de 0x70 a 0x58 alpha.
  El rojo neon "Offline" se lee mejor sin agredir al pergamino.

### v0.11.11 - DiaryTextField (KI nuevo M-widget)
- Nuevo widget: DiaryTextField extiende EditBox vanilla. Preserva TODA la
  logica de edicion (cursor, seleccion, Ctrl+C/V, filter, responder, formatter,
  hint) y solo overridea renderWidget para dibujar sobre pergamino.
- Fondo PAPER_STAINED con borde tinta que se aviva a rojo victoriano al focus.
  Cursor tinta parpadeante. Seleccion en banda sepia semi. Hint sepia claro
  cuando el campo esta vacio.
- Aplicado en DiaryEditServerScreen y DiaryDirectJoinScreen.
- Reflection defensiva con scan por tipo dentro de EditBox.class (sin herencia)
  para descubrir highlightPos, frame, displayPos, isEditable, hint, formatter.

### v0.11.12 - DiaryToggle
- Nuevo widget: DiaryToggle envuelve un OptionInstance<Boolean>. Persiste
  inmediatamente al cambiar via Options.save().
- Layout: label izquierda + separador + valor derecha + circulillo indicador
  (relleno si ON, hueco si OFF).
- Layout adaptativo: si el texto excede el ancho, primero omite el indicador,
  luego trunca el label con puntos suspensivos.
- Aplicado en DiaryControlsScreen para los 4 toggles booleanos: Sneak (Hold/
  Toggle), Sprint (Hold/Toggle), Auto-Jump (On/Off), Operator Items Tab (On/Off).
- Formatter custom via Function<Boolean, Component>: Sneak/Sprint muestran
  Hold/Toggle en vez de On/Off.

### v0.11.13 - Tooltips estilizados (KI-10 CERRADO)
- KI-10 CERRADO. Fondo pergamino con borde tinta en TODOS los tooltips
  del juego: Chat, Options, Inventario, botones Diary, item tooltips, etc.
- Estrategia final: TooltipEventHandler suscrito a RenderTooltipEvent.Color
  de Forge, con EventPriority.LOWEST para ejecutar despues de otros mods.
- NO usa mixin. Forge expone hook nativo que reemplaza los 4 colores del
  tooltip (backgroundStart, backgroundEnd, borderStart, borderEnd).
- Camino previo (mixin sobre TooltipRenderUtil.renderTooltipBackground)
  fue descartado tras confirmar que ese metodo NO es el que dibuja los
  tooltips reales de Forge en 1.20.1 - lo hace un lambda interno en
  GuiGraphics.renderTooltipInternal.
- Infraestructura mixin queda armada aunque sin mixins activos, lista para
  futuros hitos (ej: KI-12).

### Errores nuevos catalogados

- **E101**: Mixin 0.8.5 rechaza silenciosamente compatibilityLevel JAVA_17.
  Log dice "Compatibility level JAVA_17 specified by X.mixins.json is higher
  than the maximum level supported by this version of mixin (JAVA_13)". El
  config se ignora pero no crashea. Fix: usar JAVA_8 en gripeverde.mixins.json.

- **E102**: Aunque el compatLevel sea JAVA_8, si el .class del mixin se compila
  como Java 17 target (class version 61), Mixin lo rechaza con "Class version
  61 required is higher than the class version supported by the current version
  of Mixin (JAVA_8 supports class version 52)". Requiere compilar el mixin
  aparte con --release 8. Se intento con task mixinCompatibility en build.gradle
  pero se abandono al descubrir la solucion via event bus.

- **E103**: TooltipRenderUtil.renderTooltipBackground NO es el metodo real que
  dibuja tooltips en Forge 1.20.1. GuiGraphics.renderTooltipInternal tiene un
  lambda propio que llama fillGradient con colores tomados del
  RenderTooltipEvent.Color de Forge. Un mixin sobre TooltipRenderUtil se aplica
  sin errores pero no tiene efecto visual.

- **E104**: mods.toml requiere seccion [[mixins]] explicita en Forge 1.20.1
  para que el config sea reconocido por Mixin loader. El atributo MixinConfigs
  en MANIFEST.MF NO alcanza. Sin [[mixins]] en toml, Mixin ni siquiera menciona
  el config en logs.

- **E105**: readField<T> generico con cast implicito puede tirar
  ClassCastException si el field name pega en un field del tipo equivocado
  (ej: FIELD_VALUE "value" mapeo a un Boolean vez de String). Fix: usar
  reflection tipada (findIntField, findBoolField, findObjField con
  expected.isAssignableFrom check).

- **E106**: DiaryToggle iteracion 1 no chequeaba si el texto entraba en la
  caja. Textos largos ("Pestana de administrador: NO") se salian del boton.
  Fix: layout adaptativo de 3 niveles (con indicador -> sin indicador ->
  label truncado con ellipsis).

- **E107**: build.gradle task JavaCompile custom no incluye el output de
  compileJava en su classpath por defecto. Si el mixin importa clases del
  propio mod (DiaryTheme), la task de recompile aislada falla con "package
  does not exist". Fix: eliminar dependencias del mixin al codigo propio
  (hardcodear colores).

### Reglas nuevas

- **R-MIXIN-COMPAT**: Con Mixin 0.8.5 usar SIEMPRE compatibilityLevel JAVA_8.
  Los levels JAVA_16/17 son rechazados. Si el mod usa Java 17 features en el
  resto del codigo, el mixin especificamente debe estar limitado a sintaxis
  Java 8 O compilarse con --release 8.

- **R-MODS-TOML-MIXIN**: En Forge 1.20.1 es OBLIGATORIO declarar el bloque
  [[mixins]] config="X.mixins.json" en META-INF/mods.toml. El atributo
  MixinConfigs del manifest NO alcanza para que Forge se lo pase al Mixin
  subsystem. Sin ese bloque, el config es silenciosamente ignorado.

- **R-FORGE-EVENTS-FIRST**: Antes de escribir un mixin sobre codigo vanilla,
  verificar si Forge expone un evento propio para lo mismo. Los eventos son
  hooks oficiales, no rompen con updates, y no requieren refmap/compat level.
  RenderTooltipEvent.Color es un ejemplo perfecto: retematiza tooltips sin
  tocar bytecode.

- **R-WIDGET-OVERFLOW-3TIER**: Widgets con contenido dinamico (labels, valores,
  iconos) deben tener layout adaptativo de 3 niveles: (1) todo el contenido
  ideal, (2) omitir elementos opcionales si no cabe, (3) truncar texto con
  ellipsis si sigue sin caber. Usar font.plainSubstrByWidth() para truncar.

- **R-REFLECTION-TYPED**: Reflection para leer fields privados debe SIEMPRE
  filtrar por tipo esperado, no solo por nombre. findIntField, findBoolField,
  findObjField con expected.isAssignableFrom check evita colisiones cuando
  distintos fields comparten nombre en la jerarquia.

- **R-MIXIN-SELF-CONTAINED**: Las clases mixin no deben importar codigo del
  propio mod. Hardcodear constantes/colores directamente en la clase. Esto
  permite recompilar el mixin aislado con --release 8 sin arrastrar
  dependencias.

- **R-MIXIN-VERIFY-TARGET**: Antes de escribir un mixin, VERIFICAR con javap
  y grep del bytecode del caller que el metodo target sea realmente el que
  se ejecuta en el path de interes. TooltipRenderUtil existe pero no es
  usado por Forge; GuiGraphics.renderTooltipInternal usa su propio lambda.

### KIs cerrados en este ciclo
- KI-10 (tooltips morados) CERRADO en v0.11.13.
- KI-15 (fade inferior listas) CERRADO en v0.11.10.
- KI-16 (scrollbar respirada) CERRADO en v0.11.10.

### KIs abiertos que siguen
- KI-07 (parcial): sub-hits vanilla restantes (MouseSettings, KeyBinds,
  PackSelection, Online). Cubiertos parcialmente por KI-12.
- KI-08 (banderas pixel-art en Language). No abordado en este ciclo.
- KI-12 (dirt bypass en 4 pantallas de trabajo). Requiere mixin, ahora
  posible con infraestructura preparada.
- KI-13 (server pinneado sin proteccion). Prioridad baja.
- KI-14 (banda multiplayer vs faviconones brillantes). Prioridad baja.

Ultima actualizacion: v0.11.14
Estado: EN DESARROLLO
Version estable actual: v0.11.14
Ultimo tag publicado: v0.11.13

---

# KNOWN_ISSUES - Eternal Craft: Gripe Verde

## v0.11.10 a v0.11.13 - Cierre del ciclo del gabinete

Cinco versiones cortas encadenadas cierran los ultimos hitos visuales del gabinete pergamino. Se agrupa aqui porque forman un mismo ciclo narrativo.

### v0.11.10 - Polish: fades, scrollbar y multiplayer
- KI-15 (fades inferiores) CERRADO. Nuevo helper DiaryChrome.drawListFades()
  aplicado en Accessibility, Chat y Sound. Fade pergamino->transparente en top y
  bottom de la lista suaviza el corte duro.
- KI-16 (scrollbar respirada) CERRADO. drawStyledScrollThumbInPanel ahora
  usa panelRightEdge-18 (antes 14). Ademas la tapa sobre la barra vanilla es
  condicional: solo se dibuja si la barra cae dentro del panel visible, para no
  generar columnas fantasma en paneles anchos.
- Multiplayer: banda sepia detras de los servers atenuada de 0x70 a 0x58 alpha.
  El rojo neon "Offline" se lee mejor sin agredir al pergamino.

### v0.11.11 - DiaryTextField (KI nuevo M-widget)
- Nuevo widget: DiaryTextField extiende EditBox vanilla. Preserva TODA la
  logica de edicion (cursor, seleccion, Ctrl+C/V, filter, responder, formatter,
  hint) y solo overridea renderWidget para dibujar sobre pergamino.
- Fondo PAPER_STAINED con borde tinta que se aviva a rojo victoriano al focus.
  Cursor tinta parpadeante. Seleccion en banda sepia semi. Hint sepia claro
  cuando el campo esta vacio.
- Aplicado en DiaryEditServerScreen y DiaryDirectJoinScreen.
- Reflection defensiva con scan por tipo dentro de EditBox.class (sin herencia)
  para descubrir highlightPos, frame, displayPos, isEditable, hint, formatter.

### v0.11.12 - DiaryToggle
- Nuevo widget: DiaryToggle envuelve un OptionInstance<Boolean>. Persiste
  inmediatamente al cambiar via Options.save().
- Layout: label izquierda + separador + valor derecha + circulillo indicador
  (relleno si ON, hueco si OFF).
- Layout adaptativo: si el texto excede el ancho, primero omite el indicador,
  luego trunca el label con puntos suspensivos.
- Aplicado en DiaryControlsScreen para los 4 toggles booleanos: Sneak (Hold/
  Toggle), Sprint (Hold/Toggle), Auto-Jump (On/Off), Operator Items Tab (On/Off).
- Formatter custom via Function<Boolean, Component>: Sneak/Sprint muestran
  Hold/Toggle en vez de On/Off.

### v0.11.13 - Tooltips estilizados (KI-10 CERRADO)
- KI-10 CERRADO. Fondo pergamino con borde tinta en TODOS los tooltips
  del juego: Chat, Options, Inventario, botones Diary, item tooltips, etc.
- Estrategia final: TooltipEventHandler suscrito a RenderTooltipEvent.Color
  de Forge, con EventPriority.LOWEST para ejecutar despues de otros mods.
- NO usa mixin. Forge expone hook nativo que reemplaza los 4 colores del
  tooltip (backgroundStart, backgroundEnd, borderStart, borderEnd).
- Camino previo (mixin sobre TooltipRenderUtil.renderTooltipBackground)
  fue descartado tras confirmar que ese metodo NO es el que dibuja los
  tooltips reales de Forge en 1.20.1 - lo hace un lambda interno en
  GuiGraphics.renderTooltipInternal.
- Infraestructura mixin queda armada aunque sin mixins activos, lista para
  futuros hitos (ej: KI-12).

### Errores nuevos catalogados

- **E101**: Mixin 0.8.5 rechaza silenciosamente compatibilityLevel JAVA_17.
  Log dice "Compatibility level JAVA_17 specified by X.mixins.json is higher
  than the maximum level supported by this version of mixin (JAVA_13)". El
  config se ignora pero no crashea. Fix: usar JAVA_8 en gripeverde.mixins.json.

- **E102**: Aunque el compatLevel sea JAVA_8, si el .class del mixin se compila
  como Java 17 target (class version 61), Mixin lo rechaza con "Class version
  61 required is higher than the class version supported by the current version
  of Mixin (JAVA_8 supports class version 52)". Requiere compilar el mixin
  aparte con --release 8. Se intento con task mixinCompatibility en build.gradle
  pero se abandono al descubrir la solucion via event bus.

- **E103**: TooltipRenderUtil.renderTooltipBackground NO es el metodo real que
  dibuja tooltips en Forge 1.20.1. GuiGraphics.renderTooltipInternal tiene un
  lambda propio que llama fillGradient con colores tomados del
  RenderTooltipEvent.Color de Forge. Un mixin sobre TooltipRenderUtil se aplica
  sin errores pero no tiene efecto visual.

- **E104**: mods.toml requiere seccion [[mixins]] explicita en Forge 1.20.1
  para que el config sea reconocido por Mixin loader. El atributo MixinConfigs
  en MANIFEST.MF NO alcanza. Sin [[mixins]] en toml, Mixin ni siquiera menciona
  el config en logs.

- **E105**: readField<T> generico con cast implicito puede tirar
  ClassCastException si el field name pega en un field del tipo equivocado
  (ej: FIELD_VALUE "value" mapeo a un Boolean vez de String). Fix: usar
  reflection tipada (findIntField, findBoolField, findObjField con
  expected.isAssignableFrom check).

- **E106**: DiaryToggle iteracion 1 no chequeaba si el texto entraba en la
  caja. Textos largos ("Pestana de administrador: NO") se salian del boton.
  Fix: layout adaptativo de 3 niveles (con indicador -> sin indicador ->
  label truncado con ellipsis).

- **E107**: build.gradle task JavaCompile custom no incluye el output de
  compileJava en su classpath por defecto. Si el mixin importa clases del
  propio mod (DiaryTheme), la task de recompile aislada falla con "package
  does not exist". Fix: eliminar dependencias del mixin al codigo propio
  (hardcodear colores).

### Reglas nuevas

- **R-MIXIN-COMPAT**: Con Mixin 0.8.5 usar SIEMPRE compatibilityLevel JAVA_8.
  Los levels JAVA_16/17 son rechazados. Si el mod usa Java 17 features en el
  resto del codigo, el mixin especificamente debe estar limitado a sintaxis
  Java 8 O compilarse con --release 8.

- **R-MODS-TOML-MIXIN**: En Forge 1.20.1 es OBLIGATORIO declarar el bloque
  [[mixins]] config="X.mixins.json" en META-INF/mods.toml. El atributo
  MixinConfigs del manifest NO alcanza para que Forge se lo pase al Mixin
  subsystem. Sin ese bloque, el config es silenciosamente ignorado.

- **R-FORGE-EVENTS-FIRST**: Antes de escribir un mixin sobre codigo vanilla,
  verificar si Forge expone un evento propio para lo mismo. Los eventos son
  hooks oficiales, no rompen con updates, y no requieren refmap/compat level.
  RenderTooltipEvent.Color es un ejemplo perfecto: retematiza tooltips sin
  tocar bytecode.

- **R-WIDGET-OVERFLOW-3TIER**: Widgets con contenido dinamico (labels, valores,
  iconos) deben tener layout adaptativo de 3 niveles: (1) todo el contenido
  ideal, (2) omitir elementos opcionales si no cabe, (3) truncar texto con
  ellipsis si sigue sin caber. Usar font.plainSubstrByWidth() para truncar.

- **R-REFLECTION-TYPED**: Reflection para leer fields privados debe SIEMPRE
  filtrar por tipo esperado, no solo por nombre. findIntField, findBoolField,
  findObjField con expected.isAssignableFrom check evita colisiones cuando
  distintos fields comparten nombre en la jerarquia.

- **R-MIXIN-SELF-CONTAINED**: Las clases mixin no deben importar codigo del
  propio mod. Hardcodear constantes/colores directamente en la clase. Esto
  permite recompilar el mixin aislado con --release 8 sin arrastrar
  dependencias.

- **R-MIXIN-VERIFY-TARGET**: Antes de escribir un mixin, VERIFICAR con javap
  y grep del bytecode del caller que el metodo target sea realmente el que
  se ejecuta en el path de interes. TooltipRenderUtil existe pero no es
  usado por Forge; GuiGraphics.renderTooltipInternal usa su propio lambda.

### KIs cerrados en este ciclo
- KI-10 (tooltips morados) CERRADO en v0.11.13.
- KI-15 (fade inferior listas) CERRADO en v0.11.10.
- KI-16 (scrollbar respirada) CERRADO en v0.11.10.

### KIs abiertos que siguen
- KI-07 (parcial): sub-hits vanilla restantes (MouseSettings, KeyBinds,
  PackSelection, Online). Cubiertos parcialmente por KI-12.
- KI-08 (banderas pixel-art en Language). No abordado en este ciclo.
- KI-12 (dirt bypass en 4 pantallas de trabajo). Requiere mixin, ahora
  posible con infraestructura preparada.
- KI-13 (server pinneado sin proteccion). Prioridad baja.
- KI-14 (banda multiplayer vs faviconones brillantes). Prioridad baja.

Ultima actualizacion: v0.11.14
Estado: EN DESARROLLO
Version estable actual: v0.11.14
Ultimo tag publicado: v0.11.13

---

# KNOWN_ISSUES - Eternal Craft: Gripe Verde

## v0.11.9 - Coherencia del gabinete (KI-11 cerrado)

### Cambios
- MenuMusic.tick() agregado en 6 pantallas Diary que no lo tenian
  (Accessibility, Chat, Language, Skin, Sound, Options). La musica
  ya no se congela al navegar por las subpantallas.
- Clamp adaptativo en 4 paneles hardcoded (Confirm, DirectJoin,
  EditServer, Skin): Math.min(PANEL_MAX_W, this.width - N) evita
  que el pergamino se salga en GuiScale 3/4.
- DiaryLanguageScreen.onClose() ahora CANCELA en vez de aplicar.
  ESC descarta el idioma pendiente, coherente con la distincion
  entre appliedLanguageCode y pendingLanguageCode.
- drawCenteredString reemplazado por drawString(..., false) con
  centrado manual en 5 pantallas (Access, Chat, Controls, Credits,
  Sound). Elimina el doble contorno negro vanilla sobre pergamino.
- 25 lang files normalizados: 9 claves *.subtitle sin guiones
  decorativos ("- valor -" -> "valor"). La decoracion vive en el
  codigo, no en los strings.
- 7 pantallas migradas para envolver el subtitle con "- ... -" a
  la hora de renderizar. Todas las Diary respiran hoy el mismo
  patron de cabecera (title / "- subtitle -" / flavor).
- DiaryLanguageScreen: el fondo interno de la lista de idiomas
  ahora usa PaperTexture.drawClean(). Antes era un fill oscuro
  0x24100804 que se veia como cartulina pegada.
- DiaryMultiplayerScreen: banda tras el server suavizada de
  0xB0281608 (69% marron opaco) a 0x703A2416 (44% sepia claro).
  Contrasta con el texto blanco vanilla sin agredir al pergamino.
- DiaryChrome.drawPanelShadow(): nuevo helper. Sombra proyectada
  del panel abajo-derecha con offset 6px, color sepia oscuro y
  4 capas concentricas. Aplicado en 12 pantallas Diary.
- DiaryButton: fondo diferenciado del panel. Base PAPER_STAINED
  (mas oscuro que el pergamino PAPER del panel), hover ocre sucio,
  sombra externa proyectada, highlight superior. Los botones se
  leen ahora como tiras de papel apoyadas, no como parte del panel.
- DiaryChrome.drawStyledScrollThumbInPanel(): nuevo helper que
  ancla la scrollbar al borde derecho del panel y TAPA la barra
  vanilla (width/2+156) con pergamino antes de dibujar la nuestra.
  Migradas Chat, Accessibility, Sound.

### Errores nuevos catalogados
- E100: return dentro de if en PowerShell interactivo NO aborta
  el script; solo aborta la subexpresion. Combinado con IndexOf
  que devuelve -1, terminamos calculando bodyStart = -1 + longitud
  del string y reemplazamos un rango arbitrario, corrompiendo
  DiaryChrome.java (queda a 1562 bytes de 11627). Fix: restaurar
  con git checkout HEAD -- <archivo> y reinsertar. Refuerza
  R-PS-THROW: usar throw en vez de return, y validar IndexOf con
  guard antes de calcular offsets.

### Reglas reforzadas
- R-PS-THROW: reforzada. Cuando IndexOf/LastIndexOf puede devolver
  -1, chequear con if ($idx -lt 0) { throw "..." } explicito. No
  confiar en return dentro de bloques interactivos.
- R-INSERT-BEFORE-CLOSE: para agregar metodos al final de una
  clase Java, usar $c.LastIndexOf('}') y reemplazar solo el
  ultimo caracter. Robusto contra cualquier EOL/whitespace,
  a diferencia de bloques multilinea que fallan por diferencias
  sutiles de indentacion.

### KI-11 (CERRADO en v0.11.9)
- Slider "Musica del Cronista" ya aplica volumen en tiempo real
  durante el arrastre.
- Causa raiz: no era del slider ni del SoundManager. Era que
  DiarySoundOptionsScreen.tick() NO llamaba MenuMusic.tick(), asi
  que MenuMusic.updateVolumes() nunca corria mientras el slider
  se movia. Un fix de una linea (mas 5 pantallas hermanas que
  tenian el mismo problema silencioso: la musica se congelaba
  al navegar por las subpantallas).

### KI-15 (ABIERTO en v0.11.9)
- Bandas inferiores de las listas vanilla (Accesibilidad, Chat,
  Sonido) recortan el contenido de la ultima fila visible sin
  fade suave, dando efecto de "corte duro". La ausencia de fade
  se notaba menos antes cuando habia doble scrollbar; ahora al
  quedar sola una barra estilizada, el corte es mas visible.
- Solucion futura: fade blur inferior en el area de la lista
  simulando que el contenido se sale de la vista, similar al
  gradient de fade que ya usa DiaryLanguageScreen.
- Prioridad baja. Cosmetico.

### KI-16 (ABIERTO en v0.11.9)
- Barra sepia estilizada esta pegada al MARCO exterior del panel
  en Accesibilidad/Chat/Sonido (0px de margen interno). Podria
  respirar 4-6px hacia adentro.
- Prioridad muy baja. Cosmetico.
## v0.11.8 - Scroll estilizado en OptionsList (KI-09 cerrado)

### Cambios
- DiaryChrome: nuevos helpers publicos getOptionsListScrollbarX() y drawStyledScrollThumbForOptionsList().
- DiaryChrome: thumb minimo aumentado de 20 a 32px.
- DiaryChrome: comentario obsoleto sobre "OptionsList final" corregido.
- DiaryAccessibilityOptionsScreen: panel ampliado a 380px, scroll estilizado activo.
- DiaryChatOptionsScreen: scroll estilizado activo.
- Intento de Hito H (DiaryControlsScreen via router + Controlling compileOnly) revertido por decision de scope. Codigo aprendido queda en backup.

### Errores nuevos catalogados
- E81: Comentario en DiaryChrome afirmaba "OptionsList es final" pero era falso. Verificar con javap antes de aceptar comentarios como verdad.
- E82: renderTooltip NO existe en Screen 1.20.1. Sistema deferred con renderWithTooltip final. Interceptar requiere mixin.
- E83: .Replace multilinea con \r\n explicitos puede fallar por diferencias de EOL sutiles. Fallback: ReadAllLines + Where-Object filter por linea.
- E84: Nombres de artifacts en maven BlameJared son case-sensitive (Searchables con S mayuscula, no searchables).

### Reglas nuevas
- R-DOC-VERIFY: comentarios que documentan limitaciones deben verificarse contra bytecode antes de aceptarse.
- R-VERIFY-JAR-CASE: nombres de artifacts maven case-sensitive; verificar via HTTP GET al maven repo.

### KI-10 (ABIERTO en v0.11.8)
- Tooltips vanilla morados sobre pergamino rompen la estetica.
- En 1.20.1 el sistema deferred (Screen.renderWithTooltip es final) impide override directo desde subclase.
- Solucion futura: mixin sobre Screen.renderWithTooltip o TooltipRenderUtil.
- Aplicaria globalmente a todas las Diary screens con tooltips.
- No bloqueante.

### Hito H (DiaryControlsScreen) - postpuesto
- Estrategia E (router + Controlling compileOnly + wrapper reflection) funciono tecnicamente.
- Revertido antes de validar visual por decision de scope: no depender de Controlling ni siquiera opcionalmente.
- Codigo en `_backups\pre_v0.11.8_20260722_155547\`.
- Retomar en hito futuro con decision de scope tomada de entrada.

### Deuda tecnica actualizada
- Widget "Duracion de las notificaciones" en Accesibilidad sigue con texto cortado. Widget vanilla 150px hardcoded. Panel a 380px alivia pero no resuelve. Limitacion vanilla ya catalogada, no se corrige.
- Tooltips morados vanilla (KI-10).

---

## v0.11.3 - DiaryAccessibilityOptionsScreen + pulido de Chat y Lenguas (KI-07 sigue avanzando)

### Cambios
- Nueva clase DiaryAccessibilityOptionsScreen que hereda de AccessibilityOptionsScreen.
- Chat y Lenguas ganan: subtitulo, flavor text, esquineros dobles, divisores con rombo tipografico central.
- Accesibilidad ampliada a PANEL_H=395 para mostrar las 20 opciones sin scroll.
- Emblemas figurativos (ojo, sobre, fleur, estrella, cruz de Malta) probados y descartados: no leen a escala pixel-art vanilla; se removieron.
- Sombras vanilla de drawCenteredString eliminadas via drawString con dropShadow=false y centrado manual.

### Errores nuevos catalogados
- E66: SimpleOptionsSubScreen.render() pinta fondo dirt + titulo vanilla. Al ambientar hay que hacer bypass: NO llamar super.render(), dibujar this.list.render() manualmente y ocultar footer widgets.
- E67: DiaryButton con Icon+Variant espera Runnable, no Consumer<Button>. Usar this::onClose o () -> ..., no b -> ....
- E68: list.render() tapa cosmeticos previos dibujados en el area de la lista. Los divisores/esquineros deben ir DESPUES de list.render().
- E69: [System.IO.File]::WriteAllText con [System.Text.Encoding]::UTF8 escribe BOM en PowerShell 5. Compilar .java con BOM da error illegal character '\ufeff'. Usar new UTF8Encoding(False).
- E70: drawCenteredString fuerza dropShadow=true de la fuente vanilla. Genera doble contorno feo en subtitulos sobre pergamino claro. Alternativa: drawString(..., dropShadow=false) con centrado manual x - font.width(text)/2.
- E71: PowerShell 5 queda esperando cierre (>>) cuando hay subexpresiones anidadas $(( ... )) con parentesis + comillas dentro de Write-Host multilinea. Calcular en variables antes de interpolar.

### Reglas nuevas
- R-NO-DROPSHADOW-CENTERED: para textos en pergamino, usar drawString(..., dropShadow=false) con centrado manual en vez de drawCenteredString.
- R-EMBLEM-SKIP: no intentar emblemas figurativos (ojo, sobre, flor, estrella) con gfx.fill() a menos de 20px de ancho. A esa escala solo funcionan simbolos geometricos abstractos simples (rombo, cruz, punto).
- R-PS-NOSUBEXPR-MULTILINE: evitar $((...)) anidados en Write-Host cuando el bloque tiene multiples lineas seguidas. Calcular en variables primero.
- R-UTF8-NOBOM: siempre usar New-Object System.Text.UTF8Encoding $false para escribir .java. [System.Text.Encoding]::UTF8 incluye BOM y rompe compilacion.
- R-SUB-SCREEN-STRATEGY: al ambientar una subpantalla vanilla que extiende SimpleOptionsSubScreen, NO llamar super.render(). Dibujar fondo propio, luego this.list.render(), luego cosmeticos superpuestos, luego boton Volver propio. Widgets vanilla del footer (narratorButton, done) se ocultan via visible=false iterando children() en init().
- R-COSMETIC-ORDER: elementos cosmeticos sobre areas donde luego se dibuja una lista vanilla deben renderizarse DESPUES de la lista.

### KI-07 estado
- Sub-pantallas Diary implementadas: Idioma (v0.11.1), Chat (v0.11.2), Accesibilidad (v0.11.3).
- Restantes vanilla: Controles, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.

### Deuda tecnica
- Barra de scroll vanilla gris en DiaryLanguageScreen rompe estetica pergamino. Reemplazo por thumb tinta victoriana queda para Fase 6 en hito futuro.
- Slider vanilla "Duracion de las notificaciones" en Accesibilidad tiene texto que se corta a la derecha por ancho fijo 150px del widget. Limitacion vanilla. Igual que en Chat (Duracion notificaciones), no se corrige.


Ultima actualizacion: v0.11.8
Estado: EN DESARROLLO
Version estable actual: v0.11.8
Ultimo tag publicado: v0.11.8
Hitos recientes: v0.11.0 (DiaryOptionsScreen sobre pergamino con Title vivo detras, KI-06 cerrado extendido, sub-pantallas vanilla integradas); v0.10.4 (bump administrativo, changelog in-game intencionalmente sin entrada); v0.10.1 (persistencia del volumen de musica); v0.10.0 (ChangelogScreen sobre pergamino + reescritura diegetica de las 25 cronicas, KI-06 cerrado); v0.9.2 (fix humo/nubes, KI-05 cerrado); v0.9.1 (faroles reactivados, KI-03 cerrado completo); v0.9.0 hito visual del skyline; sistema de musica (v0.8.x) estable.

---

## 1. Version actual

- Proyecto: Eternal Craft: Gripe Verde
- Base: Minecraft Forge 1.20.1
- Version desplegada: 0.11.1
- Tipo: mod de interfaz + audio
- Ambientacion: ciudad victoriana bajo cuarentena, Green Flu
- Estado general: compilando limpio. Sistema de musica del Title con persistencia (v0.10.1). ChangelogScreen sobre pergamino y cronicas diegeticas (v0.10.0). Mejoras visuales previas de v0.9.x estables.

---

## 2. Sistemas activos

### Title / DiaryTitleScreen
Pipeline de fondo (estructura estable desde v0.7.1; ajustes visuales en v0.9.0):
1. AtmosphereRenderer.renderSky
2. VictorianSkyline.renderBackground
3. LivingAtmosphere.renderFogLow
4. VictorianSkyline.renderForeground
5. LivingAtmosphere.renderStreetLevel
6. LivingAtmosphere.renderAir
7. LivingAtmosphere.renderFlash
8. LivingAtmosphere.renderVignette

### MenuMusic (v0.8.3)
- 4 pistas registradas: music_title_1..4 (OGG stream)
- Playback aleatorio (shuffle), sin repeticion hasta completar ciclo
- Crossfade de 4s entre pistas
- Ducking automatico (35% durante 2.5s) al reproducir SFX RARE
- Volumen ajustable en tiempo real via MusicVolumeSlider
- Idempotente: start() no reinicia si ya suena
- Auto-stop al detectar mc.level != null (jugador en mundo)
- Persistencia: sobrevive a apertura de Options y subpantallas
- FadingMusicInstance: soporta volumen dinamico sin recrear el sample

### MenuAmbience
- Sin cambios funcionales; integracion nueva: llama a MenuMusic.duckFor(2500L) al disparar sonidos RARE

### MusicVolumeSlider
- Widget nuevo en client/widgets
- Se inyecta en OptionsScreen via ScreenEventHandler.onScreenInitPost
- Posicion: centrado horizontal, y = screen.height - 25 (pie de pagina, seguro para todas las escalas)

### UiSounds
- Version defensiva con safePlay() que tolera RegistryObject vacio
- Workaround para incompatibilidad con ModernFix RegistryObjectMixin

### LivingAtmosphere
- Sin cambios desde v0.7.0 (calle adaptativa)

### VictorianSkyline
- Sin cambios desde v0.7.1 (postes ocultos)

---

## 3. Problemas conocidos activos

### KI-01 - Freeze al presionar Leave (RESUELTO)
Sintoma original: podia quedar en "Saving world".
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Sospecha historica: entorno, RAM, GPU, Embeddium.

### KI-02 - Crash al hacer screenshots F2 (RESUELTO)
Sintoma original: NVAPI + Embeddium + NVIDIA.
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Buena practica sugerida: usar Windows + Shift + S como alternativa segura.

### KI-03 - Fondo del Title con defectos visuales (RESUELTO COMPLETO en v0.9.1)
- Calle definida: RESUELTO en v0.7.0.
- Postes/faroles flotantes: RESUELTO en v0.9.1 (reactivados con anclaje a la calle, densidad por escala GUI).
- Vacio entre skyline FG y calle: ATENUADO por jerarquia visual de v0.9.0.
- Skyline demasiado poblado / sin jerarquia: RESUELTO en v0.9.0 (BG/FG diferenciados).
- Luna solitaria: contexto atmosferico mas rico alrededor de la luna queda como mejora futura opcional (no es bug).

### KI-04 - Incompatibilidad con ModernFix RegistryObjectMixin
Sintoma: NullPointerException en RegistryObject.get() al hoverear botones.
Causa: mixin de ModernFix sobre net.minecraftforge.registries.RegistryObject.
Workaround: UiSounds.safePlay() tolera excepcion y omite el sonido sin crashear.
Estado: MITIGADO.

### Deuda tecnica activa
- ICON_BTN_MP e ICON_BTN_MODS sin PNG real.
- Framework UI subutilizado (solo Title lo consume).
- ThemeManager sin themes registrados.
- Slider de musica no persiste en disco (se pierde al cerrar juego).

---

## 4. Historial relevante reciente

### v0.7.0 - Sub-hito 1 KI-03
- renderStreet() adaptativo agregado.

### v0.7.1 - Sub-hito 2 KI-03
- getLanternPositions() devuelve int[0][0].

### v0.8.0 - Sistema de musica base
- 4 pistas OGG registradas.
- MenuMusic con playback secuencial shuffle.
- Fix critico: sounds.json corrupto por append incorrecto.
- Fix critico: mod_version hardcodeado en build.gradle (E32).
- Fix critico: UiSounds defensivo contra ModernFix (KI-04).

### v0.8.1 - Slider de volumen
- MusicVolumeSlider widget.
- Inyeccion en OptionsScreen via ScreenEventHandler.
- Reubicado 3 veces por problemas de escala; posicion final: pie de pagina.

### v0.8.2 - Crossfade
- FadingMusicInstance creado.
- MenuMusic reescrito para soportar dos instancias simultaneas.

### v0.8.3 - Ducking + persistencia
- MenuAmbience llama duckFor() en RARE.
- MenuMusic.start() idempotente.
- Auto-stop cuando jugador entra a un mundo.
- Se detecta perdida de instancia al salir de servidor y se reinicia.

---

## 5. Reglas vigentes

### Desarrollo
- UN sistema por version.
- UN cambio visual sensible por bump.
- Validacion visual/auditiva en 4 escalas cuando se toque UI/audio.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real antes de proponer cambios.
- Java UTF-8 sin BOM (UTF8Encoding false).
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre acompanado de deploy.
- R-JAR: Limpiar jars gripeverde-*.jar viejos del deploy path antes de validar.
- R-VER: Validar la version visible en el reloj antes de aprobar o rechazar.
- R-GRADLE-VER: mod_version en build.gradle debe usar project.version.toString(), nunca hardcodear.

### PowerShell
- R-CD: Todo bloque de comandos debe iniciar con Set-Location al repositorio para evitar errores de ruta (E35).
- R-PS: Minimizar cantidad de bloques PowerShell.
- R-PATH: Usar rutas absolutas si no se esta parado en la raiz del repo.
- R-PATH2: gradlew y git tambien requieren estar en la raiz del repo. Si el bloque falla y estamos en system32, Set-Location primero.

### Validacion
- R-CACHE: LivingAtmosphere cachea lanternPositions; reiniciar cliente al cambiar GuiScale si hay duda.
- R-CAP: Capturas pre/post en 4 escalas antes de validar cambios visuales.
- R-JSON: Validar JSON con ConvertFrom-Json tras cualquier append/edit a sounds.json u otros JSONs criticos.

### Java
- R-DUP: Cuando se agregan bloques nuevos que declaran variables locales, verificar que no existan ya en scope superior del metodo.

---

## 6. Errores historicos importantes

### E20 - Escritura con BOM rompio compilacion
Solucion: usar UTF8Encoding(false) al escribir Java desde PowerShell.

### E21/E23 - Parseo ingenuo de .deploy
Solucion: filtrar por DEPLOY_MODS_PATH= y no asumir primera linea.

### E27 - Replaces multi-cambio simultaneos
Regla: un replace por vez, verificacion inmediata.

### E28 - Formulas cross-file
Regla: no crear dependencias silenciosas entre renderers.

### E29 - Sin capturas de referencia
Regla: capturas pre/post en 4 escalas.

### E30 - Extrapolar entre escalas
Regla: validar escala por escala.

### E31 - Sistemas interdependientes en mismo bump
Regla absoluta: un sistema por version.

### E32 - mod_version hardcodeado en build.gradle
Sintoma: jar dice v0.7.1 en el nombre pero mods.toml interno decia v0.6.1.
Causa: build.gradle tenia mod_version: '0.6.1' hardcodeado en vez de project.version.toString().
Solucion: usar project.version.toString() para que Gradle expanda dinamicamente.

### E33 - JSON append rompio sounds.json
Sintoma: WARN Invalid sounds.json, todos los sonidos del mod silenciados.
Causa: append manual con string dejo coma huerfana antes del cierre del JSON.
Solucion: regenerar el JSON completo desde plantilla + validar con ConvertFrom-Json.

### E34 - Redefinicion de variable local en bloque agregado
Sintoma: BUILD FAILED, variable mc is already defined in method tick().
Causa: se agrego "Minecraft mc = Minecraft.getInstance()" al inicio de tick() sin verificar que otro bloque interior ya la definia.
Solucion: R-DUP.

### E35 - PowerShell corriendo desde system32
Sintoma: .\gradlew no se encuentra, git fatal not a git repository.
Causa: bloque no hizo Set-Location al repo antes de comandos de build/git.
Solucion: R-PATH2. Siempre Set-Location $repo al inicio de bloques con gradlew/git.

---

## 7. Plan futuro inmediato

- v0.9.1: COMPLETADO (faroles reactivados).
- v0.9.2: COMPLETADO (humo/nubes con blobs suaves, KI-05 cerrado).
- (Opcional) v0.10.0: Persistencia del volumen en disco / mejoras de calidad de vida del sistema de musica.
- Deuda visual pendiente: contexto atmosferico mas rico alrededor de la luna (nubes, halo mayor).

## 8. Estado tecnico

- Repo local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde
- Deploy: D:\Juegos\Minecraft\Instances\Test\mods
- Ultimo tag: v0.11.4
- Build limpio.
- Working tree esperado: limpio antes de proximas sesiones.

### KI-06 - Menu del Historial de Cambios sin ambientacion victoriana (RESUELTO en v0.10.0)
- ChangelogScreen ya no usa UI vanilla: panel de pergamino, boton al estilo DiaryButton y cronicas reescritas en tono diegetico.

### KI-07 - Subpantallas de Opciones siguen usando UI vanilla (PARCIAL)
- v0.11.3: Idioma cerrado (DiaryLanguageScreen sobre pergamino, con lista limpia, sin fondo dirt ni barra de seleccion vanilla).
- Siguen vanilla: Controles, Chat, Accesibilidad, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.
- Estado: funcional (se abren y vuelven sin crash), pero rompe la estetica en las que aun no fueron ambientadas.
- Plan: continuar de a una en v0.11.x. Musica y sonidos como proximo candidato natural.

### KI-05 (RESUELTO en v0.9.2)
- Humo de chimeneas, nubes del skyline y nube frente a la luna se dibujaban como rectangulos por g.fill.
- Solucion aplicada: AtmosphereRenderer.drawSoftBlob con 6 anillos concentricos y alpha cuadratico decreciente.
- Nubes reducidas a 2 blobs con radio capado a 20px para evitar gasto excesivo en radios grandes.

---

## E43 - System.IO.File con ruta relativa

Sintoma: [System.IO.File]::ReadAllText / WriteAllText con ruta relativa fallan si el CWD del proceso .NET no coincide con Set-Location de PowerShell (ocurre al abrir nueva instancia PS que arranca en C:\\Users\\<user>).
Solucion: siempre pasar ruta absoluta con $PWD\\... explicito. Cubierto por R-DOTNET-PATH y R-CD.


### KI-08 (ABIERTO)
- Deuda visual: banderas / emojis por idioma en DiaryLanguageScreen.
- La font vanilla de Minecraft no renderiza regional indicators (secuencias tipo U+1F1EA U+1F1F8 = ES) ni emojis a color.
- Force Unicode Font tampoco resuelve: renderiza como cuadros o glifos genericos.
- Solucion futura: font provider propio del mod (assets/gripeverde/font/*.json) mapeando code points custom a PNGs con banderas dibujadas manualmente en pixel-art victoriano (no fotorealistas).
- Alcance estimado: sub-hito propio, no bloqueante.

### KI-09 (CERRADO en v0.11.8)
- Scroll estilizado en pantallas que heredan de SimpleOptionsSubScreen (principalmente Accesibilidad).
- OptionsList vanilla NO puede recibir tratamiento visual completo:
  - OptionsList.Entry es protected y no se puede calificar desde otro paquete
  - En runtime PROD los campos y metodos tienen nombres SRG (f_XXXXX_, m_XXXXX_) que dificultan reflection
- Impacto: Accesibilidad muestra scroll vanilla gris. Funcional, feo.
- Solucion futura: reimplementar OptionsList como widget propio (DiaryOptionsList) que NO herede de vanilla, componga entries manualmente desde OptionInstance<?>.
- Sub-hito propio cuando se planifique (Candidato M).


### Errores post-v0.11.4

- **E72**: HumanoidArm.getName() no existe en 1.20.1. Correcto: getCaption() via OptionEnum.
- **E73**: DiarySkinCustomizationScreen iteracion 1 con super.init() vanilla mostro CycleButtons feos tapando titulo. Fix: reimplementar desde cero.
- **E74**: PowerShell interpreto lineas de Select-String como cmdlets. No es bug de codigo, es ruido de PS. Solucion: capturar output en variable, inspeccionar con .Contains().
- **E75**: renderDecorations() no tapa scroll vanilla (vanilla lo dibuja despues). Fix: override render() completo.
- **E76**: Thumb 3px sobre barra vanilla de 6px dejo lineas grises visibles. Fix: tapar 6px enteros con pergamino primero.
- **E77**: Reflection getDeclaredField("width") falla en runtime porque Forge sirve SRG (f_93388_), no official. Debug via log lo confirmo.
- **E78**: Subclasear OptionsList con extends fallo por OptionsList.Entry protected. Cast (Entry) desde otro paquete no compila.
- **E79**: Refuerzo R-UTF8-NOBOM. UTF8Encoding(False) siempre.
- **E80**: Copy-Item con $jar null tira excepcion. Fix: if ($jar) { Copy-Item }.

### Reglas nuevas post-v0.11.4

- **R-SRG-RUNTIME**: Reflection contra vanilla debe probar nombres official (dev) y SRG (prod) en cascada + fallback siempre + log de resolucion.
- **R-VANILLA-INNER-PROTECTED**: Subclasear vanilla con inner types protected desde otro paquete es problematico. Alternativas: dibujar encima, reimplementar completo, marcar como deuda.
- **R-DEBUG-LOG-FIRST**: Antes de reflection compleja, hacer pasada de debug con LogUtils para descubrir nombres reales en runtime. Ahorra iteraciones.
- **R-ITER-VISUAL-STRICT**: Refuerzo. Si iteracion 3 tampoco resuelve un bloqueo tecnico, revertir sin excepciones. En esta sesion se violo (5+ iteraciones en Accesibilidad scroll) antes de aceptar reversion.


---

## KI-11 (CERRADO en v0.11.9) - Slider del Cronista no aplica en vivo

**Estado**: activo. Impacto menor.

**Sintoma**: al arrastrar el slider "Musica del Cronista" dentro de DiarySoundOptionsScreen, el volumen no cambia audiblemente durante el drag. El cambio se percibe al soltar el mouse (o al proximo tick de MenuMusic, tipicamente <=50ms despues).

**Comportamiento correcto**: el valor SI se persiste inmediatamente en ClientConfig.MUSIC_VOLUME (el override de onDrag llama applyValue en cada frame de arrastre). El bug es solamente que el audible no se sincroniza con el visual.

**Causa probable**:
- AbstractSliderButton.onDrag() llama applyValue() correctamente.
- applyValue() persiste via MenuMusic.setUserVolume().
- MenuMusic.updateVolumes() lee getUserVolume() y aplica al currentInstance via setDynamicVolume().
- Pero updateVolumes() solo se llama desde MenuMusic.tick(), y aunque la screen tiquea MenuMusic (v0.11.8 fix), SoundManager de MC puede estar cacheando el volumen del SoundInstance hasta el proximo audio buffer swap.

**Plan**: sub-hito K3 en v0.11.x. Investigar si hay que forzar refresh del SoundManager o si MenuMusic necesita un pulso adicional en cada frame (no solo en tick).

**No bloqueante**.

## KI-12 (ABIERTO en v0.11.8)

Fondos dirt vanilla persisten en pantallas de trabajo:
- MouseSettingsScreen (Controles -> Mouse)
- KeyBindsScreen (Controles -> Atajos)
- PackSelectionScreen (Options -> Paquetes de recursos)
- OnlineOptionsScreen (Options -> En linea)

Causa: en 1.20.1, estas pantallas pintan renderDirtBackground
DIRECTAMENTE desde su render() sin pasar por el hook renderBackground.
Un override simple en subclase no funciona (ver E89).

Solucion: mixin @Redirect o @Inject sobre esas pantallas.

Plan: v0.11.8 hito mixin dedicado. Investigar previamente por que
fallo el refmap en el intento de v0.11.8 (E90).

No bloqueante. Las pantallas funcionan, solo lucen vanilla.


---
## v0.11.8 (Multiplayer completo)

### KIs abiertos nuevos
- **KI-13**: Server pinneado editable/borrable sin bloqueo visual explicito.
  ensurePinnedServer() lo restaura en el proximo init, pero seria mas
  claro mostrar ConfirmScreen antes de borrarlo o desactivar el boton.
  Prioridad: baja.
- **KI-14**: Banda sepia bajo el server puede verse muy oscura contra
  faviconones brillantes. Adaptacion dinamica de opacidad seria mejor.
  Prioridad: baja.

### KIs que siguen abiertos
- KI-07 (parcial): faltan MouseSettings, KeyBinds, PackSelection, Online
- KI-08: banderas pixel-art
- KI-10: tooltips morados vanilla
- KI-12: 4 pantallas con dirt bypass (Mouse, KeyBinds, PackSelection, Online)
