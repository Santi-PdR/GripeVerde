# Eternal Craft: Gripe Verde

## Version 0.46.0 - Responsive options cabinet

- The options cabinet now fits compact window sizes with smaller, carefully spaced controls and a shorter header.
- Full English and Spanish button names remain available through hover tooltips when labels need truncation.
- Long title and subtitle text is clipped to the paper panel, and the FOV slider follows the adaptive grid width.


## Version 0.45.0 - UI sound control

- Added a persistent interface SFX volume slider and a test sound button to the sound cabinet.
- The slider controls button, hover, scroll, notification and custom UI sound playback while respecting Minecraft's own audio settings.


## Version 0.44.0 - Clearer survivor screens

- Removed the rotating lower-right quarantine dispatch from the title screen.
- Updated the pinned community server address to `Outbreak.exaroton.me:24652`.
- Kept only the primary US English and Spain Spanish locale files.
- Removed the duplicate text pass that made button lettering look too bold.
- Raised UI sound levels and kept Minecraft's normal sound controls in charge.
- Added quiet archival folds to official papers and a fixed, lightweight paper style for screens over a live world.
- Explicitly bypassed diary transitions for the inventory and while a world is active.


## v0.43.0 - Efectos quietos durante la partida

El diario ya no suaviza el desplazamiento ni anima destellos, el espejo, el ojo o el diapasón mientras hay un mundo abierto. La navegación responde de inmediato.

## v0.42.0 - El diario deja respirar la partida

En un mundo, el diario usa un velo y un panel estáticos: pausa cielo, tormenta, grano, partículas, ornamentos animados, transiciones y la interpolación del hover. Las opciones siguen legibles sobre el mundo. El menú principal conserva su ambientación.

## v0.41.0 - Escalas de dificultad sin censura

El manual ahora enumera las dificultades activas hasta INSURGENCY e INFERNUM, además de los rangos BACKBREAKING, DOOMSDAY y ARMAGEDDON marcados WIP. También explica sus eventos globales y ghouls más fuertes.

## v0.40.0 - El marcador sigue el capítulo

El manual conserva el avance relativo dentro del capítulo. Cambiar el tamaño de ventana o la escala de interfaz ya no te envía a otro pasaje; los controles narrados siguen la barra al arrastrarla.

## v0.39.0 - El marcador sobrevive al cierre

El manual de campo conserva dónde ibas incluso después de cerrar el juego. Ábrelo de nuevo y continúa desde el mismo pasaje.

## v0.38.0 - El manual recuerda la lectura

Al volver a abrir el manual de CEDA durante la misma sesión, regresa al pasaje que estabas leyendo. La rueda, los saltos entre capítulos y el arrastre de la barra mantienen actualizado el marcador.

## v0.37.0 - El manual se deja leer

El manual de campo ahora es accesible con el narrador: Tab recorre sus seis capítulos y cada parada anuncia el encabezado junto con sus protocolos. Enter salta al capítulo elegido; el foco queda marcado en azul sobre el papel. El pie también recuerda este recorrido.

## v0.36.0 - El origen de la fiebre

El manual amplía el expediente de infección: recoge que la genética, la salud y las costumbres locales influyen en las mutaciones, y revela el encubrimiento de 1800. Las autoridades llamaron al brote una gripe común; los ghouls siguen vivos y la Gripe Verde no reanima cadáveres.

## v0.35.0 - Saltos entre capítulos

El manual permite saltar directamente entre sus seis capítulos: haz clic en un encabezado o pulsa 1–6. El título se ilumina y subraya al pasar el cursor para señalar que se puede abrir. El pie resume los nuevos atajos de navegación.

## v0.34.0 - Razas del cordón

El manual de campo suma un expediente sobre las razas de los supervivientes: más de 300 posibilidades, desde humanos, tiburones y cíborgs, con evoluciones V1–V4+ sujetas a acciones y misiones propias de cada rareza. También registra la escala completa de rarezas, compartida por razas, artefactos y objetos.

## v0.33.0 - Navegacion del manual

El manual de campo ahora se puede recorrer con rueda, flechas y RePág/AvPág; Inicio y Fin saltan al comienzo o al cierre. La barra lateral también responde al arrastre y el pie explica los controles. Las tres formas de navegación respetan los límites del papel y mantienen el regreso al refugio al alcance.

## v0.32.0 - El manual de campo

La copia de campo de CEDA se abre al pulsar el cartel de cuarentena. Reune los protocolos reales del servidor: los ghouls siguen vivos, los ataques PIN exigen rescate, el derribo se revierte con ayuda, la muerte requiere desfibrilador, los recursos menguan y las dificultades escalan. El manual se desplaza con la rueda, vuelve al cartel y adapta sus textos al idioma del juego; unos detalles azul hielo evocan los ojos de los infectados.

## v0.31.0 - Partes de cuarentena

La portada ahora publica un parte de cuarentena distinto cada jornada: CEDA insiste en que solo es una gripe, mientras los ojos azules y las mutaciones cuentan otra historia. Los avisos también recuerdan que las reservas menguan, que un compañero debe liberar al atrapado y que el grupo ha de replegarse junto. En octubre, el cronista ya no llama muertos a los ghouls: siguen vivos. El parte conserva el margen de la firma y se oculta si la ventana no ofrece espacio.

## v0.30.0 - La hoja entera

Feedback del owner tras la 0.29.0: la vuelta quedo bien, pero la pluma
que escribe solo UN tramo queda rara; la letra de la lupa no se veia;
los ticks de hover muy altos; y hay textos pisados por botones. Backup
externo previo (`/home/user/GripeVerde-backup-pre-v0.30.0-6ac186f.bundle`,
--all, verificado).

- **La pluma escribe la bitacora ENTERA** (fusion de sus dos propuestas):
  desde la entrada vigente hasta el fin del documento, el texto se
  escribe mientras el lector baja (ritmo 340 chars/s tras 300 ms de
  gracia). Regla de piso: lo que queda mas de media pantalla por encima
  se considera impreso y no gasta tinta — bajar de golpe con la barra
  jamas deja huecos, y quien sube encuentra todo seco. El click sigue
  completando todo al instante; con movimiento reducido, hoja entera.
- **La lupa ya se lee**: el cuadrito era tinta sobre tinta (errata
  reconocida) — ahora es etiqueta de papel claro (`PAPER` al 240) con
  letra de tinta; caret bronce legible sobre ella.
- **Ticks de hover -40%**: `VOL_HOVER_MAIN` 0.045→0.028,
  `VOL_HOVER_SECTION` 0.035→0.022 (pedido expreso). Clicks intactos.
- **Fixes de solape (reporte del owner)**:
  - Título: el mensaje del día ya no flota sobre la mesa de botones — se
    lee a la IZQUIERDA de la firma en la misma línea (se omite si no hay
    aire), y la firma jamas entra en la franja del gramofo (max clamp
    x≥48).
  - Gabinete de opciones: la línea "(no: la tinta ya no seca)" del huevo
    Holloway quedaba DEBAJO de la primera fila de botones — ahora clamp
    duro sobre la grilla, divisor siempre por debajo, y flavor/fix con
    elipsis al ancho del papel.
  - Sala de sonido: la cita del flavor se omite si no hay aire real
    sobre los sliders, y con elipsis al ancho del panel.
- Interfaces debidas: pluma y marcas en pixeles logicos dentro del
  scissor; clamps de texto probados en GUI scale 2/3/4 y minimal mode;
  reducedMotion y reduceFlashes respetados en todo lo nuevo (KI-65).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.29.0 - La vuelta y la lupa

Feedback directo del owner tras jugar la 0.28.0: la cronica gusta, la
animacion al SALIR de cada cosa no. Backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.29.0-5dee740.bundle`, --all,
verificado).

- **La vuelta cortes (rediseño de la transicion de regreso)**: volver a
  una mesa ya visitada ya no corre el barrido completo al reves — ahora
  la hoja cae de arriba en 170 ms: velo tenue (110 vs 150) + banda
  superior de tinta que se repliega (46 px → 0) con filo bronce. Sin
  salpicaduras, sin gotas, sin sombra extra. Toda la ceremonia (barrido
  largo, salpicaduras, gotas) queda reservada para AVANZAR, donde gusto.
  La sombra de asiento sigue en ambos sentidos (solo opacidad, 450 ms).
- **Optimizacion del velo** (`DiaryTransitions`): cache de elegibilidad
  por clase (fin de `getName().startsWith` por frame), `reducedMotion`
  resuelto una vez por apertura, duracion y fin de efecto precomputados
  (`durNowMs`/`fxEndMs`) — fuera de la ventana del efecto el render es
  costo cero (dos restas y un return).
- **Fuera el golpe sordo** (pedido expreso): el escritorio ya no suena
  al tocarlo en cualquier lado; `answerWhisper` vuelve a su forma pura
  (solo el susurro abierto contesta) y `UiSounds.silenceKnock` se retira
  del catalogo. `UI_ERROR` sigue cableado en `error()` — 35/35 en uso.
- **La lupa de la bitacora**: cuadrito al pie a la derecha del papel (96
  px, se abre con `/`, Ctrl+F o tocandolo). Escribis una palabra (hasta
  24 caracteres, texto simple sin comodines) y la cronica salta a cada
  aparicion con Enter / Shift+Enter, piquito bronce en cada renglon
  coincidente y fondo tenue en la actual; contador "N/M" (o "0") a la
  izquierda. Esc la cierra y olvida la pregunta. El folio comparte la
  franja: si el panel es angosto (escala 4) se corre a la izquierda —
  jamas se pisan. Caret parpadeante (fijo con destellos reducidos).
- Interfaces debidas: la vuelta corta se ve bien en GUI 2/3/4 (banda
  proporcional, nada anclado al centro), la lupa vive bajo el contenido
  dentro del panel en todas las escalas, y reducedMotion mantiene el
  fundido limpio (sin vuelta ni ceremonia). KI-64.

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.28.0 - Las gotas y los folios

Misma consigna del owner: mejoras significativas en textos y en los efectos
al cambiar de pantalla/interfaz, correctas en TODAS las interfaces, con
backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.28.0-d40c820.bundle`, --all,
verificado).

- **La tinta fresca gotea (transiciones)**: del filo de la cortina se
  desprenden 3 gotas en momentos distintos del pliegue (30/55/80% del
  barrido) que resbalan hasta 26 px EN EL SENTIDO del pliegue con rastro
  fino (trail 1 px, alfa ≤ 40; gota 2x2, alfa ≤ 85) y se desvanecen en
  550 ms. Posiciones por LCG con la semilla de cada apertura. Con
  movimiento reducido: sin gotas (fundido limpio, como siempre).
- **La cronica gana folio**: al pie del papel, centrado, "folio N · M"
  calculado del scroll real (paginas = alto visible; N = hoja actual) con
  ornamentos de bronce a los lados. Bajo el area de contenido y dentro
  del panel: nunca pisa texto, botones ni el folio escondido (ese vive
  dentro del scrolleo). Funciona en GUI 2/3/4 y con resize en vivo.
- **Sello NUEVO en TODAS las entradas saltadas**: si el gabinete avanzo
  mas de una edicion desde `lastSeenVersion` (p.ej. 0.25.2 → 0.28.0),
  todas las cabeceras con triple de version posterior a la ultima leida
  amanecen lacradas esta visita (comparacion numerica por triple, parser
  tolerante). La pluma de 0.27.0 sigue escribiendo SOLO la entrada de la
  version instalada.
- **Tablon del titulo con lineas nuevas**: cada pool de subtitulos
  rotativos pasa de 9 a 12 lineas por idioma (ES/EN), misma voz de
  documento: "Los hospedajes abren sus libretas", "Un cordon se sostiene
  entre todos", "Escriban su nombre en la guardia" (hospedajes) y "Cada
  pieza lleva su sello", "Lo que el cordon permite", "Cuenten las
  lamparas antes de salir" (arsenales).
- Interfaces debidas: gotas solo durante transiciones del gabinete;
  folio y sellos en pixeles logicos dentro del panel; probado contra
  escala GUI 2/3/4, minimal mode, movimiento reducido y destellos
  reducidos (KI-63). Reglas madre intactas: sin gameplay, render Java
  puro.

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.27.0 - El pliegue y la pluma

Misma receta pedida por el owner: mejoras significativas en textos y en los
efectos al cambiar de pantalla/interfaz, bien en TODAS las interfaces, con
backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.27.0-aa0ff79.bundle`, --all,
verificado).

- **El pliegue tiene sentido (direccion de la cortina)**: `DiaryTransitions`
  gana pila de navegacion (`NAV`, acotada a 16) alimentada desde
  `ScreenEventHandler.noteTransition` — la pantalla vieja se captura al
  inicio de `onScreenOpening` y la nueva se lee TRAS el redirect vanilla, en
  los tres puntos de salida del metodo. Avanzar a una mesa no visitada → la
  cortina de tinta baja (como en 0.26.0); regresar a una mesa ya abierta
  (Cerrar, Esc, volver al titulo, salir de la pausa) → la cortina sube, la
  hoja vuelve al pliegue. Sin pista del origen (arranque), siempre baja.
- **El filo salpica**: 5 gotas de tinta diminutas tras el borde de la
  cortina, posiciones por LCG con semilla propia de cada apertura (estables
  durante la transicion), alfas ≤ 95 y cola de 500 ms tras el destape. Sin
  destellos; con movimiento reducido no hay salpicaduras (solo fundido).
- **La hoja se acuesta**: sombra interior de 6 px que decae en 450 ms tras
  la cortina (alfa max 20 → 0). Es solo opacidad — tambien presente con
  movimiento reducido, nunca tapa texto ni botones.
- **V4 implementada — la cronica se escribe a pluma**: la entrada de la
  version recien instalada se escribe letra a letra en su primera visita
  (ritmo minimo 170 chars/s, techo duro de 6,5 s para entradas largas).
  Plumín dibujado al final del ultimo trazo siguiendo el wrap real del
  font (tambien la capital del encabezado), parpadeo de tinta suave (fijo
  en 170 con destellos reducidos) y rasguño quedo (`UI_SCROLL`, 0.045f,
  un golpe por tramo de 42 chars con throttle de 120 ms). Un click la
  termina al instante; con movimiento reducido la hoja llega entera.
  Persistencia: la misma `lastSeenVersion` de 0.24.0 — solo la primera
  visita de cada entrada se escribe.
- Interfaces debidas: todo en pixeles logicos y dentro del scissor de la
  cronica; layout intacto; probado contra escala GUI 2/3/4, minimal mode,
  movimiento reducido y destellos reducidos (KI-62). Sin gameplay, sin
  resourcepacks: render Java puro (reglas madre intactas).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.26.0 - Las hojas y los faroles

Entrega pedida por el owner: mejoras visibles significativas ("efectos al
cambiar de pantalla/interfaz, etc"), revision de textos y, antes de tocar
nada, backup externo
(`/home/user/GripeVerde-backup-pre-v0.26.0-18e178d.bundle`, --all).

- **El pasar de las hojas (transicion entre interfaces)**: nueva
  `DiaryTransitions` enganchada a `ScreenEvent.Render.Post` en
  `ScreenEventHandler` — al abrir cualquier pantalla del paquete
  `client.screens` (titulo, opciones, cronica, archivo, sonido, pausa...),
  una cortina de tinta baja revelando la hoja nueva en ~260 ms con velo
  sepia disolvente y franja de tinta con filo de bronce. Con movimiento
  reducido solo fundido de 130 ms, sin barrido. Pantallas vanilla/ajenas:
  cero efecto. Dibuja en Render.Post: cubre botones incluidos solo esos
  260 ms, nunca interfiere con clics (es impermeable al input).
- **Faroles reactivos (V6 de REVISION_MOD)**: `LivingAtmosphere.setMouse`
  desde el titulo + `lanternHover[]` suavizado por tick (ease ~110 ms);
  la mano cerca de un farol (radio 30 px sobre la lampara) levanta su
  llama y ensancha su halo x1.5, todo con topes 255 (withAlpha no
  clampea). Funciona con movimiento reducido (no hay movimiento nuevo,
  solo respiracion de brillo) y en cualquier escala GUI/altura (4-6
  faroles segun alto).
- **Revision de textos (crónicas in-game)**: reescritas las entradas
  0.24.1/0.25.0/0.25.1 en los 25 idiomas con vocabulario de documento de
  la vida real — fuera "hexadecimal", "crossfade", "literal numerico",
  "clic"; dentro "rotativa", "cilindro madre", "toque", "sello de
  imprenta".
- **El gabinete afina el oido (V2 de REVISION_MOD, resto)**: los 8 sonidos
  legacy por fin suenan con sus eventos propios (estaban grabados y
  registrados; faltaba el cable) — `UiSounds.open/close/modal/tab/error/
  cleanClick/clickCreate/respawn` cableados a `UI_OPEN/UI_CLOSE/UI_MODAL/
  UI_TAB/UI_ERROR/UI_BUTTON_CLEAN/UI_CLICK_CREATE/UI_RESPAWN`. Tres
  momentos nuevos audibles: el escritorio responde con golpe sordo
  (`UiSounds.silenceKnock`, cooldown 480 ms) cuando tocas la vigilia sin
  susurro en el aire; el gramofono del titulo anuncia pieza nueva con
  `clickCreate` (SlimButton gana hook `withClickSound`, los demas botones
  intactos); y volver al titulo desde otra mesa te "vuelve a sentar" con
  `respawn` (`onArriveTitle` en ScreenEventHandler; la primera apertura
  del arranque no suena). Allowlist del sello de uso de sonidos vacia en
  Gradle y en el espejo Python: 35/35 eventos en uso.
- ERICA recuerda: este mod VIVE en interfaces; cada cambio visual va
  probado contra escala GUI 2/3/4 y minimal mode (KI-61).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8
sellos + balance OK.

## v0.25.2 - El telegrama no pisa la mesa

Hotfix de layout (parche), reportado por el owner probando GUI scale 3-4:

- **Cartel del easter egg (telegrama "CASO")**: los dos clamps de Y se
  anulaban entre si — uno lo subia para esquivar la mesa de botones y el
  siguiente lo volvia a clavar en `safeTop` fijo, de modo que con poca
  pantalla logica (escala 3-4) el cartel caia encima de los botones y el
  texto quedaba ilegible. Regla nueva y monotona: jamas tocar la zona de
  botones, jamas salirse por arriba; si no hay banda libre, el cartel sube
  sobre la franja del logo. Ademas `boxW` ahora tiene techo en el ancho
  real de pantalla.
- **Sello FRAUDE** (cartel del elixir, tras HOLLOWAY): estaba fijado a 46
  px del borde inferior del poster; en carteles bajos (escala 3-4) se
  montaba sobre las lineas del cuerpo y podia salirse del papel. Ahora se
  estampa siempre DESPUES del texto (`cursorY + 6`), nunca por encima, y
  con tope duro dentro del poster, incluido clamp horizontal.
- **Tooling/documentos**: la ruta de deploy vigente queda documentada como
  comandos PowerShell INLINE (fetch+reset+build+copia por glob), sin .ps1
  nuevos, y la regla permanente del owner: cada entrega termina con ese
  bloque listo para pegar. `tools\deploy.ps1` queda como alternativa
  corregida.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos, 42 OGG.
Espejo Python: 8/8 sellos + balance OK.

## v0.25.1 - El cilindro original

Hotfix de contenido (parche): la lluvia sintetizada de 0.24.0 "sonaba
bugeada" (reporte del owner al escucharla en cliente). Decision: retirar
la sintesis y restaurar los masters originales de lluvia y viento,
amplificados en lugar de regrabados — usar "los sonidos que ya existian",
pedido expreso del owner.

- **Lluvia**: master original (el de -32 dB RMS) con ganancia pura,
  normalizado a pico ~0.83 sin tocar la cresta (~19): nada de limitadores
  (el primer intento de soft-knee genero NaN y picos >1.0 — descartado y
  rehecho desde el master). Su volumen de mezcla sube 0.30 -> 0.45 en
  `MenuAmbience` para que vuelva a sonar clara sin siseo aplastado.
- **Viento**: master original (el de -64 dB RMS) amplificado a RMS ~0.085
  (pico 0.80 tras decodificacion).
- **Costuras medidas**: ambos masters empalmaban limpios (seam 0.0076 y
  0.0000), asi que no llevan crossfade: loops identicos a los originales.
- `tools/audio/regenerate_menu_ambience.py` queda archivado con nota en
  cabecera: ya no es la fuente de esos dos loops (registro historico del
  intento de sintesis).

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.25.0 - El que mira la tormenta

Entrega menor de easter eggs pedida por el owner ("algunos easter eggs
mas"): tres secretos nuevos en la vigilia, cableados sobre mecanica que ya
existia (tormenta, lluvia, telegrafo oculto).

- `storm_watcher`: `LivingAtmosphere` cuenta ahora cada relampago que nace
  (programado o forzado) y la pantalla del titulo marca una linea base al
  abrirse en noche de tormenta: al tercer destello nuevo, ficha y nota
  (`N_FLASH3`). Con destellos reducidos no hay relampagos y la ficha no se
  regala: respeta accesibilidad.
- `drop_catcher`: la lluvia es tocable. `LivingAtmosphere.rainDropAt` hace
  hit-test contra las 40 gotas en vuelo con tolerancia generosa; atrapar una
  la devuelve al cielo (`scareDrop`), suena un plink de aguja (`menu/tick`
  a 1.9) y suma ficha + nota (`N_DROP`). La zona de botones queda excluida:
  ningun clic sobre la mesa se pierde por culpa de una gota.
- `code_doctor`: palabra nueva del telegrafo oculto — escribir DOCTOR en la
  vigilia trae susurro y campana baja, ficha, contador `doctor_signed` y
  nota (`N_DOCTOR`). El elixir tuvo un medico; el oficio sigue en pie.

El archivo crece a 52 fichas; nombres y pistas viven en el registry bilingue
de `Discoveries` (cero claves lang nuevas: el conteo sigue en 93). Notas de
Marginalia nuevas: N_FLASH3, N_DROP, N_DOCTOR.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.24.1 - La errata del cronista

Hotfix de build (parche): la semilla de las gotas de lluvia de V5 se escribio
como `0xD20PL` —la P no es digito hexadecimal— y la 0.24.0 no compilaba
(`error: malformed floating-point literal` en `PaperTexture.java:95`,
detectado por el owner en el primer `deploy.ps1`; en el sandbox no hay JDK
y el espejo Python solo verifica balance de llaves, por eso se escapo).
Corregida a `0xCA1DAL` ("la caida"): comportamiento identico, gotas
deterministas con otra secuencia. Para que ninguna errata de esta familia
vuelva a salir sellada, el espejo Python suma un octavo sello:
`verifyJavaLiterals` valida todos los literales numericos (hex/binario/
octal/decimal/flotante, sufijos incluidos) de los 60 .java tras retirar
comentarios y cadenas. Sin cambios funcionales: el contenido de 0.24.0
queda intacto. Para Agus y para cualquier distribucion: usar 0.24.1 —la
0.24.0 jamas existio como jar.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.24.0 - El viento vuelve a soplar

Entrega menor de superficie visible, pedida por el owner junto al segundo
crash-log de Agus: "corrige que no se escucha el ambiente", mejoras visibles
(V1+V3+V5) y mas easter eggs.

El ambiente no se oia por contenido, no por codigo: el loop de viento estaba
grabado a -64 dB RMS (inaudible a cualquier ganancia razonable) y la lluvia
a -32 dB. Se regeneran viento y lluvia por sintesis procedural
(`tools/audio/regenerate_menu_ambience.py`, reproducible e idempotente,
con crossfade de extremos para loops sin chasquido) y se refuerzan ciudad,
susurros y un paso. El regulador de ambiente por fin manda sobre algo, y de
regalo el gabinete deja de sonar dentro de los mundos (fuga historica:
los loops seguian activos con un nivel cargado).

Mejoras visibles: la Cronica se abre en la entrada de la version instalada
tras cada actualizacion y la marca con un sello de lacre "NUEVO" durante la
visita (V1; `ChroniclerMemory.lastSeenVersion`); algunas sesiones (~14%)
arrancan en Noche de tormenta, con lluvia doble, relampagos frecuentes,
mezcla ambiente al mando de la lluvia y truenos casi seguros (V3); y la
lluvia resbala en gotas por todos los pergaminos (V5; quieta con movimiento
reducido). Easter eggs nuevos: el lacre del telegrama se rompe a golpes
(`seal_breaker`, estrena `ui.paper_fall`) y un diapason oculto junto a los
reguladores afina la sala (`sound_keeper`, estrena `ui.success`); el
descubrimiento `storm_night` completa el trio. El archivo crece a 50 fichas
y sus textos de "cuarenta y tantas fichas" ahora computan la cifra solos.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 7/7 sellos +
balance OK.

## v0.23.2 - El telegrama llega intacto

Hotfix de crash en campo (parche): el easter egg que dispara el clic en el
sello del titulo podia tumbar el cliente en el primer fotograma. El efecto
maquina de escribir abre el telegrama con 0 letras visibles durante los
primeros ~22 ms, `font.split("")` devuelve una lista vacia y el dibuje
indexaba `lines.get(0)` tras forzar un minimo de un renglon:
`ArrayIndexOutOfBoundsException` en
`DiaryTitleScreen.drawEasterEggMessage`, con crashes en cadena bajo
NotEnoughCrashes. Reportado por un jugador real con Java 17 y la edicion
v0.22.0 (gracias por el registro completo).

Correccion: el conteo de renglones del telegrama queda acotado por el
tamano real de la lista (0 permitido: el sobre espera su primera letra) y
la llamada al overlay, puramente decorativa, queda envuelta para que
ningun fallo de ornamento pueda volver a detener el render del cliente.
Se auditaron ademas los otros 5 usos de `font.split` del codigo
(ChangelogScreen, DiaryArchiveScreen): todos iteran en forma segura ante
listas vacias, y el resto de los accesos indexados a listas ya tenian su
guardia de vacio correspondiente.

## v0.23.1 - Sellos y relojería

Entrega de mantenimiento dura (sin cambios de escena, por eso va como
parche): el owner autorizo implementar el lote recomendado de la revision.

Dos sellos nuevos de build: `verifyGripeVerdeSubtitles` (toda clave de
subtitulo no vacia de sounds.json debe existir en los 25 idiomas) y
`verifyGripeVerdeSoundUsage` (ningun SoundEvent registrado puede quedar
sin uso en el codigo; los 10 eventos legacy sin uso quedan en allowlist
grandfathered, documentados en ModSounds, hasta decidir su destino).

La memoria del cronista gana sanitize de bounds (numeros imposibles de un
JSON tocado a mano se corrigen en silencio) y un pipeline de migracion
versionada documentado (SCHEMA_VERSION + pasos N→N+1) para futuros
esquemas. La capa de ambiente (MenuAmbience) migra al mismo reloj
monotonico nanoTime que la musica, junto con el rastreo de calle de
LivingAtmosphere; los temporizadores del mod ya no dependen del reloj de
pared del SO.

La CI se entrega lista para activar con `tools/ci/activar-ci.ps1` (la
sesion no tiene permiso `workflows`; el script la activa con las
credenciales locales del owner en un paso).

## v0.23.0 - El gramófono guarda la palabra

Pasada transversal pedida expresamente tras la revision tecnica del
2026-08-23 (ver REVISION_MOD.md), sin gameplay ni resourcepacks y con
backup externo verificado antes de tocar archivos.

Audio blindado: la banda sonora vanilla del menu calla mientras el gabinete
esta abierto (MusicManager disparaba Musics.MENU a los 20-600 ticks y sonaba
encima de las pistas propias), y un watchdog detecta instancias fantasma del
motor de sonido: tras un F3+T la playlist vuelve sola en vez de quedar muda
para siempre. Los temporizadores de musica, cooldowns UI y duracion de pistas
usan ahora reloj monotonico (nanoTime). La escritura de la memoria del
cronista (temporal + force + reemplazo atomico) sale del hilo de render hacia
un executor monohilo, sin micro-tirones y con cierre ordenado.

Accesibilidad: nueva ayuda visible "Destellos reducidos" (fotosensibilidad,
separada de Movimiento reducido) que apaga relampagos sin congelar el resto
de la escena; los ocho eventos ambientales (pasos, madera, puerta, susurro,
campana, cuervos, cadenas, trueno) ganan subtitulos reales en los 25 idiomas;
el deslizador de ambiente corrige su etiqueta hardcodeada y usa la clave
traducida que ya existia.

Ingenieria: el mod ya no registra manualmente su escucha de pantallas
(eliminada la doble entrega de eventos y el crash latente en servidores
dedicados; el mod queda inerte fuera del cliente), se retira por completo la
infraestructura de Mixins vacia (JSON, plugin, refmap, mods.toml) y el
namespace de GameTest sin tests, y el atajo oculto Ctrl+0 pasa a ser opt-in
via config. Las pantallas solo se reemplazan cuando la clase es exactamente
vanilla (ningun mod de menu ajeno es barrido) y ambos reemplazos pueden
apagarse por config para convivir con otros mods. El build crece con dos
sellos nuevos (sincronia de version Gradle/VERSION y duraciones reales de
las pistas OGG parseadas como Vorbis) y una CI de GitHub Actions compila
cada push con JDK 17 y publica el jar como artifact: el bloqueo de KI-52 por
falta de Java 17 en el sandbox tiene ahora via automatica de validacion.
Conteos: 60 clases Java, 25 idiomas con 93 claves, 25 cronicas, 35 eventos,
42 OGG. Version final 0.23.0.

El tag y el release v0.23.0 se reservan hasta completar en cliente real la
validacion registrada como KI-53 (ver KNOWN_ISSUES.md).

## v0.22.0 - La vigilia queda sellada

La simulacion atmosferica y el audio quedan reunidos en un tick fijo de
20 Hz: el polvo ya no acelera con los FPS y se eliminan los ticks redundantes
desde render. Movimiento reducido pasa a la pantalla de Accesibilidad y
congela cielo, clima, humo, particulas, grano y destellos sin borrar calle,
faroles ni utileria; los toggles anuncian tambien su estado al narrador.

La memoria del cronista deja de sobrescribir su unico JSON: escribe un
temporal durable, conserva la ultima copia valida, reemplaza atomicamente y
recupera un temporal interrumpido o el backup cuando encuentra corrupcion,
preservando el archivo dañado. El papel y las ventanas reutilizan RNGs
deterministas para reducir asignaciones sin cambiar su aspecto. El build
valida paridad de idiomas, changelogs y el registro sonoro completo. Version
final 0.22.0.

El expediente queda listo para revision del owner en la PR 14. El tag y el
release v0.22.0 se reservan hasta completar en un cliente real la validacion
registrada como KI-52.

## v0.21.31 - La vigilia permanece entera

Se elimino por completo el modo automatico de pocos FPS: ya no existe el
muestreador, la escena del Title siempre conserva niebla, faroles, reflejos,
particulas y utileria, y el grano vuelve a un intervalo estable. Tambien se
limpio el estado heredado y el ciclo erratico que podian debilitar un farol
o suprimir su reflejo.
El objeto de madera de la carretera fue identificado como el kiosco del
Heraldo y retirado junto con su hitbox, periodico diario, overlay, clase y
ficha de descubrimiento. El archivo cuenta ahora 46 fichas activas.

## v0.21.30 - La calle permanece

Cuando la maquina que sostiene la vigilia aminora el paso, la ciudad aun
puede desprenderse de la niebla y apagar los adornos mas costosos. La
calzada, sin embargo, ya no desaparece con ellos: el empedrado mojado
permanece bajo los edificios y devuelve suelo a la vigilia.

## v0.21.24 - Ajustes pedidos en cliente

Registro tecnico: se retiro el sonido de madera al abrir el gabinete, el
cajon con la llave y su vitrina, el juego de los tres golpes sobre la mesa
y el despegue de la hoja de cuarentena; el titulo del cartel y los sellos
de tinta pierden el rojo sombreado; el libro de visitas del registro de
hospedajes se ve y responde mejor; y las atribuciones usan etiquetas en
titulo normal.

## v0.21.23 - Se reconstruye desde el registro previo

Registro tecnico: se localizo en el historial el estado anterior a la
version que altero las interfaces (el registro v0.17.2) y desde el se
reconstruyeron las diecisiete salas del gabinete a su forma original:
cabeceras propias de cada sala, sin carteles ni paneles de la epoca
revertida. Se conservan intactas las demas versiones: el arreglo del
registro sonoro, la limpieza, la memoria del gabinete con sus cuarenta y
siete fichas, el periodico, el telegrafo y los arreglos de audio y
rendimiento. El sello que cierra el archivo, las fichas de la limpieza y
los avisos de las cien aperturas siguen activos.

## v0.21.22 - Hotfix compilacion

Registro tecnico: la limpieza de la v0.21.21 dejo una firma de metodo rota
en el gabinete que impedia compilar. Corregido.

## v0.21.21 - Se termina la deuda de los no-op

Registro tecnico: se eliminaron las ultimas treinta llamadas a los carteles
que ya no se dibujan, los tres helpers muertos que las atendian, las
variables que los alimentaban y el pie de registro de la cronica. Las
barras de desplazamiento de tres salas, que habian quedado referenciando
medidas eliminadas, se anclaron al borde derecho del panel. Sin cambios
visibles de fondo ni de gameplay.

## v0.21.20 - La tinta pesa menos

Registro tecnico: limpieza profunda tras la reversion. Las cabeceras se
redujeron a la llamada simple (se eliminaron mas de ciento cincuenta lineas
de textos documentales muertos), se retiraron metodos y variables que ya no
alimentaban a nada (incluido el carril lateral del gabinete), y el grid de
botones del gabinete quedo mas cerca de su cabecera. Sin cambios de
gameplay ni de recursos.

## v0.21.19 - Se corrigen scrolls y posiciones tras la reversion

Registro tecnico: tras retirar los carteles y reducir las cabeceras al
titulo, varias salas quedaron con las listas y los mandos desplazados. Se
realinearon las listas del registro de hospedajes, la sala acustica, la
correspondencia y la estacion de comunicaciones, los botones del manual de
campo, y el campo de busqueda de la sala de consulta quedo en la esquina
superior derecha, fuera de las fichas.

## v0.21.18 - Se restaura lo que no era de la version revertida

Registro tecnico: se reviso la reversion con el historial completo y se
devolvieron los elementos que no pertenecian a los pases de interfaz
revertidos: el sello dorado que cierra el archivo al reunir las cuarenta y
siete fichas, el medidor de ritmo del aparato de observacion, el medidor de
registros del archivo de lenguas y el marco de las atribuciones. Los
medidores de las salas de la epoca revertida (gramofono, ambiente,
disciplina, campo visual y compania) quedan retirados.

## v0.21.17 - Se retiran los medidores y el resto de carteles

Registro tecnico: continuacion de la reversion. Se retiraron los medidores
con etiqueta (gramofono, ambiente, disciplina, campo visual, ritmo y
compania), el sello de archivo completo, el pie de registro de la cronica,
los carteles de grupo del gabinete y el marco oficial de las atribuciones.
Quedan solo titulo, contenido y mandos.

## v0.21.16 - Se retiran los carteles de interfaz

Registro tecnico: reversion de la identidad visual agregada por los pases
de interfaz. Las cabeceras vuelven al titulo simple; los carteles, tableros
y casilleros de las salas (correspondencia, comunicaciones, consulta,
sonido, controles, lenguas, aspecto) fueron retirados; las tarjetas y
railes laterales dejaron de dibujarse. Se conserva todo lo funcional
(botones, listas, busqueda, memoria, libro de visitas) y los arreglos.

## v0.21.15 - Se retiran los sellos de estado

Registro tecnico: se retiraron los sellos de estado de las cabeceras de
todas las salas (REGISTRABLE, LISTO, PENDIENTE, READY y compania) y el
sello del carril del gabinete; las frases bajo los subtitulos recuperan
todo el ancho. Continuacion de la reversion de la v0.21.14. Sin cambios de
gameplay ni de recursos.

## v0.21.14 - El gabinete vuelve a respirar

Registro tecnico: reversiones pedidas en cliente. Se retiro el saludo que
aparecia sobre los botones del menu principal; en el registro de hospedajes
la lista vuelve a empezar debajo de la cabecera, desaparece la tarjeta del
hospedaje elegido y el aviso del periodico; en las autorizaciones se van el
cartel de orden, la linea de la mano autorizante y el sello de estado; y
las frases bajo los subtitulos ya no se topan con los sellos de estado en
ninguna sala. Sin cambios de gameplay ni de recursos.

## v0.21.13 - El expediente se cierra

La sala de consulta, cuando por fin reune todas las fichas, deja de decir
que faltan. El archivo se declara completo, un sello de cera dorada lo
firma con la palabra SIGILLUM, y el cronista anota al margen: cuarenta y
siete fichas; el cajon ya esta vacio; sellalo. La campana del campanario
suena una vez, grave, para que nadie lo dude.

## v0.21.12 - El marco de la pausa queda en su sitio

Registro tecnico: el marco de oro del diario de pausa fue escrito en su
lugar definitivo, sin piezas sueltas. El gabinete, ademas, quedo sin un
solo metodo o campo privado sin uso: todo lo que existe se usa.

## v0.21.11 - La sala respira bajo la mano

La sala de consulta ahora responde al tacto: la ficha bajo el dedo se alza
un instante, y el medidor del archivo late con un pulso tenue, como un
corazon de papel. La memoria del cronista ya no se escribe a cada paso: se
asienta de tanto en tanto, y al apagar la lampara se guarda entera antes de
que la casa quede a oscuras.

## v0.21.10 - La estacion parpadea al cambiar

Registro tecnico: el medidor de alcance de la estacion de comunicaciones ya
no salta de golpe al mover los avisos: se desliza como lo haria una aguja
sobre papel. Se retiraron quince tonos de tinta que ninguna sala usaba, y
la vigilancia de codigo muerto aprende a nombrar lo que fue desterrado.

## v0.21.9 - La tinta pesa menos por frame

Registro tecnico: el gabinete consultaba el idioma del lector decenas de
veces por cada parpadeo; ahora lo pregunta una sola vez cada instante y se
lo guarda. El texto centrado se escribe con una sola mano en cuatro salas,
y el registro de hospedajes muestra, junto al libro de visitas, el titular
del periodico del dia. Se retiraron campos y metodos que nadie usaba, y se
anadio la guia de contribucion al proyecto.

## v0.21.8 - Una sola mano para el idioma

Registro tecnico: siete salas del gabinete consultaban el idioma del lector
cada una con su propia receta; ahora todas preguntan a la misma mano, y se
retiraron los ornamentos de marco que nadie usaba. Sin cambios visibles ni
de gameplay.

## v0.21.7 - El gabinete sin peso muerto

Registro tecnico: consolidacion y limpieza. Se eliminaron 9 clases sin uso
o duplicadas (incluidas las herramientas de desarrollo inalcanzables y los
dos sliders de sonido, combinados en el generico), 7 metodos muertos y 57
claves de idioma huerfanas; el pipeline de 14 pantallas comparte un unico
helper de panel. Sin cambios de gameplay ni de recursos; la sala de sonido
unifica el aspecto de sus dos reguladores. 59 clases Java, 25 idiomas, 35
eventos de sonido, 42 OGG.

## v0.21.6 - El heraldo cuenta la limpieza

El periodico del kiosco encontro palabras nuevas para la noche: el gabinete
del distrito vaciado de papeles viejos, un farol del tejado que parpadeo
tres veces y se apago solo, el gato que bajo al kiosco y no robo nada. La
letra menuda de la segunda columna tambien cambia de amanecer en amanecer.
Y quien conozca la palabra que abre el archivo podra pedirle a la sala que
le muestre las fichas que faltan.

## v0.21.5 - La sala responde a la consulta

La sala de consulta aprendio a responder. Quien busque entre las fichas por
su nombre, por su pista o por la palabra que las nombra, vera solo las que
importan; y cuando el archivo confiese tres fantasmas de la limpieza, un
cuarto recuerda la mesa que nunca se uso. La musica cede un instante cuando
el archivo habla, y cuando la maquina que sostiene la vigilia va lenta, la
calle se despoja de niebla y faroles para no detenerse.

## v0.21.4 - La tinta se asienta

Esta noche el gabinete ordeno sus utensilios. El manual de campo acepta que se
le pregunte por nombre: quien busque entre sus paginas encuentra el mando que
necesita sin revolver el resto. El aparato de observacion separa sus
secciones —lo que pinta, lo que muestra, lo que mide— y anade una aguja que
lee el pulso de la maquina. Los consulados de lenguas declaran, al pasar la
mano, cuantas paginas de su registro guarda el gabinete. Y en la sala de
consulta, al abrir la puerta, un papel cae al suelo.

El gabinete tambien se ordeno por dentro: se retiraron referencias que ya no
nombraban a nada, y se anadio una vigilancia que avisa si vuelve a aparecer lo
que fue desterrado.

## v0.21.3 - El gabinete respira mas ligero

Esta noche el gabinete aprendio a no gastar fuerzas. La ciudad del cristal
repite sus nubes, su humo y su lluvia con la misma semilla, como si la
vigilia se guardara en la memoria; y quien lo pida puede aquietar el
movimiento de la escena entera: el farol deja de temblar, la polilla se
posa, la rata deja de cruzar y el papel espera el viento.

El archivo, ademas, confiesa tres cosas que retiro en la limpieza: el
archivo que se adelgaza, el tema que nunca sono y la calle que nunca
existio. Quien pronuncie la palabra de la limpieza recibira un telegrama
del propio cronista; y quien encuentre dos de esas confesiones podra pedir
la tercera.

## v0.21.2 - Limpieza profunda del proyecto

La oficina no cambió de aspecto, pero sí de peso. Tras cerrar filas en v0.21.0 y corregir el registro sonoro en v0.21.1, el gabinete se sometió a una auditoría completa: todo lo que ya no servía fue retirado, y todo lo que estaba desactualizado fue puesto al día.

Se eliminaron los tres documentos históricos de auditoría (v0.14.x), las muestras de audio de prueba, una textura de 2.1M sin uso (`bg_town.png`), el framework inerte de temas (`ThemeManager`, `GhoulPalette`, componentes de overlay sin registrar) y tres widgets muertos (`IconButton`, `SilentButton`, `DiarySeparator`). `TextureAssets` perdió su constante muerta y `ScreenEventHandler` dejó de delegar a un ThemeManager que no hacía nada.

Se actualizaron `README.md` (v0.21.2 + nota de limpieza), `CONTEXTO_PARA_IA.md` (reescrito a v0.21.2, 68 clases, 0 PNG), `CHANGELOG.md` (añadidos v0.19.0 y v0.17.x faltantes), 25 changelogs in-game (entrada v0.21.1 y v0.21.2), `CONTEXTO.md` (nuevo addendum de limpieza) y `KNOWN_ISSUES.md` (sección v0.21.1 ampliada a v0.21.2).

El resultado: 68 clases Java (vs 83 en v0.17.2), 25 idiomas, 25 crónicas, 42 OGG, 0 PNG de contenedores, working tree limpio y sin PRs abiertos.

## v0.21.1 - Correccion del registro sonoro

La oficina no cambio de aspecto esta noche, pero si de disciplina interna.
Los sonidos del gabinete quedaron registrados de una manera mas segura para
que el archivo pueda abrirse sin tropezar al comienzo de la vigilia.

## v0.21.0 - La oficina aprende a cerrar filas

No todas las salas del gabinete habian alcanzado la misma madurez. Algunas,
aun bajo buena tinta, seguian recordando demasiado a un formulario prestado.
Esta noche se corrigio ese desajuste. La oficina de correspondencia, la
estacion de comunicaciones, la sala de consulta, el registro de revisiones y
las fichas de entrada fueron ordenadas con una mano mas firme, de modo que
cada una se reconozca al instante por su oficio y no solo por su titulo.

Hay menos rigidez de tablero y mas disciplina de archivo. Las hojas de ruta
se llenan como formularios verdaderos; la correspondencia se reparte como en
un casillero de oficina; las senales del distrito se leen como trafico de
estacion; y las fichas de consulta muestran mejor su peso dentro del mueble.

## v0.20.0 - El gabinete encuentra su propia mano

El gabinete ya tenia su estructura; esta noche aprendio a comportarse como
una obra terminada. Las cabeceras dejaron de alzar tanto la voz, los paneles
se ordenaron con mas aire, y las piezas de mando perdieron rigidez de tablero
para parecer objetos reales de una mesa de trabajo: placas, reglas, railes,
instrumentos y hojas superpuestas. El lector ya no ve solo opciones reunidas
sobre pergamino; ve una oficina entera donde cada sala tiene su propia manera
de presentarse.

La sala acustica mide el pulso de sus canales; el aparato de observacion
expone sus calibraciones con modales de laboratorio; el manual de mandos
resume el porte de la mano; la correspondencia distingue el correo del rumor;
las lenguas conocidas lucen como un archivo vivo; la portada y la pausa se
sienten mas hermanas del gabinete central. Todo ello sin vestir la ciudad con
texturas ajenas: solo papel, tinta, sombra, metal apagado y composicion.

## v0.19.0 - El acero se retira de nuevo, la interfaz respira

El gabinete volvió a mirar sus propias manos y decidió que el acero de cuarentena,
aunque vistoso, no le pertenecía. Las planchas que cubrían los contenedores en
v0.17.0-v0.17.2 fueron retiradas de nuevo por directiva explícita: nada de
`gripeverde_steel`, nada de texturas vanilla modificadas. La identidad visual
debía venir del propio mod —papel, tinta, sombra, metal apagado y composición—
no de un resource pack superpuesto.

Con la mesa limpia, se hizo un pase transversal de interfaz: cabeceras
documentales con expediente y estado, rieles internos, tarjetas laterales,
medidores con marcas, revelado de paneles y scaffolding de profundidad.
`ExpedientHeader` y `DiaryChrome` evolucionaron hacia un lenguaje menos cuadrado:
rieles incompletos, cápsulas discretas, scrollbars más físicas. `DiaryVideoSettingsScreen`
nació como consola técnica propia. Options, Sound, Controls, Chat, Accessibility,
Language, Online, Credits, Multiplayer y Pause se pulieron para parecer salas del
mismo gabinete, no el mismo template.

## v0.18.0 - El gabinete toma forma definitiva

La casa del cronista ha dejado de apoyarse en la pura costumbre. Esta noche,
por fin, sus salas parecen haber sido pensadas como un mismo oficio. El
registro central abre con una mesa mejor ordenada; cada ala del gabinete lleva
su propia cabecera, su referencia de archivo, su nota de estado y sus piezas
laterales. El archivo de lenguas, la sala acustica, el manual de mandos, la
hoja de observacion, el escritorio de correspondencia y el acta final de
atribuciones ya no son la misma hoja con distinto titulo: cada una conserva la
familia del gabinete, pero muestra su propio caracter.

Los medidores del sonido respiran, los listados quedan encuadernados dentro de
sus railes, los paneles se revelan con mas ceremonia, y hasta la portada de la
vigilia dispone ahora sus rutas y mandos con mejor jerarquia. No hubo que
vestir nada con planchas ajenas: el oficio se logro con la propia mano del
mod, sobre el papel, la tinta, la sombra y la luz.

## v0.17.2 - Las bandejas de la mesa y la moneda del trato

Parche de mantenimiento sobre el acero de cuarentena. El slot universal pasó
de 18px a 26px, las transparencias fueron repasadas para que el pergamino
respire mejor, y el viejo `tabs.png` fue reemplazado por sprites individuales
`tab_top` / `tab_bottom` selected/unselected 1-7. Remaches y cepillado más
discretos, sellos de cuarentena con mejor lectura. La geometría vanilla
permaneció intacta: solo presentación.

## v0.17.1 - Remaches bien puestos

Segunda mano sobre el acero. Se ajustaron los remaches, el brillo del cepillado
y las juntas de los contenedores para que el acero pareciera realmente clavado
a la mesa, no pintado. Correcciones menores de alineación en hornos y cofres.

## v0.17.0 - El acero vuelve a la cuarentena

El gabinete volvió a vestirse con planchas de acero. Tras el retiro completo
del resource pack en v0.15.0, esta versión reintrodujo `gripeverde_steel` como
pack built-in REQUIRED: 20 contenedores + 4 fondos creativos + sprites de
contenedores, widgets y recipe_book. Presentación 1:1, geometría vanilla intacta,
solo acero cepillado, slots hundidos, juntas con remaches y sellos de cuarentena.
`ResourcePackHandler` registraba el pack via `AddPackFindersEvent`, y
`verifyGripeVerdeThemeAssets` verificaba dimensiones, alpha y ausencia de huérfanas.
La línea v0.17.0-v0.17.2 fue luego retirada de nuevo en v0.19.0 por directiva de
no usar resourcepacks.

## v0.16.0 - La sala de consulta

El gabinete ha dejado de ser un mueble. Esta noche aprendio a recordar.

Todo lo que el lector encuentra queda escrito en una memoria que no se
borra al cerrar los ojos: las fichas halladas, los objetos pequenos, las
noches seguidas, las ausencias largas. El polvo se acumula sobre los
papeles cuando nadie vuelve durante dias, y la mesa de trabajo sabe ya
cuantas manos la abrieron. El gabinete saluda al que llega por primera
vez, reconoce al que regresa, y si alguien se queda demasiado quieto
frente a los folios, pregunta en voz baja si sigue ahi.

El cronista ya no escribe solo. Deja notas al margen que responden a lo
que el lector hace, corrige su propia letra cuando algo lo contraria, y
firma distinto cuando oye su nombre dicho en voz alta. Hay quien jura que
la madera devolvio tres golpes.

La calle se deja tocar: los faroles se apagan y se encienden a mano, y si
la avenida entera queda a oscuras, algo cambia para siempre. El gato del
tejado paga sus deudas despacio, a razon de un jiron por visita. La rata,
a veces, se detiene a mirar. Cuando doblan las campanas conviene esperar:
un carro cruza despues, muy despacio, y nadie lo conduce con prisa. Una
veleta nueva obedece al viento sobre el tejado mas alto, y cada dia el
kiosco guarda un Heraldo distinto, escrito con las noticias que la ciudad
se atreve a decir.

El reloj de bolsillo se puede detener en la mano. Tiene una inscripcion.
Y quien lo visita suficiente descubre que el tiempo guarda cosas pequenas
que se caen si uno insiste.

Hay codigos que abren puertas: el ano de la peste, la hora del brote, el
nombre del medico del elixir, el nombre del propio cronista. Cada uno
deja algo distinto: un telegrama, trece campanadas y una luz donde no hay
piso, una confesion que mancha el cartel del elixir para siempre, una
firma ofendida. El gramofono, girado mas alla de su ultima pieza, trae lo
que nadie grabo.

Cada sala del gabinete guarda ahora algo suyo: un cajon cerrado que acepta
una llave pequena, una senal de contrabando escrita en las costumbres de
las manos, un espejo que muestra como te ve la niebla, un buzon que
acumula cartas sin respuesta, un ojo que sigue al que mira, una lengua
que ningun consulado reconoce, un sello que nadie debia romper, un libro
de visitas para el atlas de hospederias, una direccion que no esta en
ningun mapa, y un silencio que, si uno se queda quieto, late.

En la pausa, la vela puede apagarse, y entonces se lee lo que la
oscuridad lee. El cuervo cobra su peaje. Algo respira del otro lado del
vidrio, y quien ha encontrado bastante llega a ver una segunda sombra.

Y detras del ultimo folio de la cronica, pasado el renglon final, hay una
sala nueva: la Sala de consulta. Cuarenta y tres fichas. Las que faltan
siguen ahi fuera.


## v0.15.2 - La cifra del campo

La mesa de opciones enseno al campo de vision a decir su numero en voz
alta. Cada posicion del deslizador se lee ahora con su cifra exacta,
como manda el oficio.


## v0.15.1 - La segunda mano

La misma noche, una mano mas fina volvio sobre el gabinete. La mesa de
opciones recupero el campo de vision, que habia quedado fuera de alcance
desde la ultima reorganizacion, y las ultimas letras escritas a mano sobre
los muebles de trabajo fueron borradas. Ningun rotulo propio queda ya
sobre las cajas: solo las palabras que el oficio original escribe.


## v0.15.0 - El oficio vuelve a su madera

El gabinete retiro las planchas de acero que cubrian las mesas de trabajo.
Los hornos, los cajones, el yunque y las mesas de oficio recuperan el
aspecto que tuvieron siempre: el gris claro de la fabrica comun, sin
remaches, sin molduras, sin sellos. Cada espacio vuelve a reconocerse de
un vistazo, como en los dias anteriores a la peste.

La decision fue firme: la casa conserva su voz, su musica y sus registros,
pero el trabajo manual vuelve a manos del oficio original de Minecraft.
Ningun mueble lleva ya el nombre de la casa; todos llevan el suyo propio.


## v0.13.10 RC - Pulido de gabinete y sonidos

El gabinete recibio una limpieza de oficio: los pestillos sonoros quedaron separados por funcion, los registros abandonaron herramientas temporales y las notas de continuidad fueron devueltas a los documentos centrales. La cronica publica conserva solo aquello que pertenece al viajero y al cronista.


## v0.13.10 - Acero bajo la lluvia

La ciudad ha recibido una mano mas firme sobre sus maquinas. Los hornos,
las cajas menores, la afiladora, el yunque, la mesa de corte, la mesa de
herreria, los registros de comercio y las caballerizas comparten ahora un
mismo acero gris, sobrio y remachado. La mesa arcana conserva su purpura,
pero sus renglones respiran entre particulas y brillos discretos.

Tambien la calle aprendio nuevos gestos: la lluvia salpica el empedrado,
los charcos forman ondas, los faroles dejan reflejos ambar, las polillas
orbitan las llamas y los papeles de cuarentena cruzan el suelo cuando
sopla el viento. En las alturas, ramas oscuras ensucian el vidrio y los
relampagos cortan la noche con trazos mas vivos.

## v0.13.3 - v0.13.9 - El gabinete se llena de maquinas

Durante siete noches, el gabinete se extendio hasta abarcar los talleres
que rodeaban al despacho del cronista. La mesa de trabajo, los arcones de
almacen, los hornos de la fragua, el yunque del herrero, la piedra de
amolar, la mesa arcana donde los libros susurran nombres antiguos, y hasta
la torre de baliza que corona los tejados de la ciudad: todo lo que hasta
hoy era carpinteria comun de madera anonima se ha transformado en hierro
forjado, remaches de latón y placas de acero pulido. Cada compartimento
lleva ahora la forma de un hueco troquelado en la plancha, y los mas
importantes estan bordeados por un halo dorado que anuncia su oficio.

Cada estacion mantiene su propio caracter dentro de la misma fabrica. Los
hornos brillan con un tinte cobrizo que recuerda al metal recien recocido;
la fragua del yunque respira el gris azulado del acero frio; la mesa de
encantamientos se cubre de un purpura profundo, casi ceremonial; y la
torre de baliza descansa sobre un fondo dorado como una joya en su vitrina.

El pergamino victoriano sigue reinando en los despachos, la correspondencia
y las lenguas conocidas. El acero remachado toma ahora el mando en cada
lugar donde el jugador trabaja con sus manos.
## v0.13.2 - Un inventario de pergamino

El gabinete del cronista ha empezado a expandirse mas alla del vestibulo.
El inventario del jugador, hasta ahora una mesa gris de fabrica, se ha
convertido en una carpeta de pergamino con marco doble, esquineros
victorianos y pequenos rombos ornamentales en cada lado. Los espacios
donde uno guarda sus enseres se ven ahora como casillas de tinta hundida,
como si el catalogo estuviera esperando el sello del funcionario. La
crafting mesa 2x2, la armadura, el escudo: todo mantiene la posicion
que uno ya conocia, solo que ahora todo parece pertenecer al mismo libro.

Aviso practico: el paquete de recursos "Gripe Verde - Tema Victoriano"
se activa por defecto, pero cualquiera puede desactivarlo desde Ajustes
si prefiere volver a la mesa gris.

## v0.13.1 - Reversion del acta

El intento de escribir un acta de defuncion para cada muerte no llego
a buen puerto. Otros gabinetes se ocuparan de ese oficio. La pantalla
al morir vuelve a ser la vanilla, y este mod deja el terreno libre para
que otro haga el trabajo con mas cuidado.

## v0.13.0 - Un acta que no llegó a ser (revertido)

Version experimental. Se genero una pantalla de acta de defuncion
victoriana al morir el jugador, pero durante las pruebas aparecieron
problemas de bucle infinito al respawnear y decidimos revertir el
sistema completo en v0.13.1.

## v0.12.4 - Cronica al dia

Actualizacion administrativa: la cronica in-game (los 25 archivos de
changelog en cada lengua) recibio la entrada "Detalles en la Penumbra",
que recoge los cambios visuales acumulados durante toda la serie v0.12.

## v0.12.3 - Hotfixes y optimizaciones

Correcciones criticas sobre v0.12.2. El ambiente ya no se congela al
navegar entre pantallas del menu, el slider de ambiente responde en
tiempo real, la calle se dibuja tambien en las subpantallas del menu,
y varias pasadas de optimizacion reducen la carga grafica en escalas
altas del GUI.

## v0.12.2 - Detalles vivos del gabinete

La escena viva del Title recibe una explosion de contenido: la luna
tiene ocho fases que rotan cada varios minutos, aparecen estrellas
fugaces con estelas cortas, bandadas de pajaros cruzan el cielo, se
encienden y apagan cinco ventanas independientes, hay un gato en el
tejado, una grieta con vela dentro de un edificio, ratas cruzando la
calle, y varios pools rotativos de textos: subtitulos del logo, firmas
del cronista, horas del reloj, cabeceras de posters, flavors de opciones.
Ademas hay un easter egg al hacer doble-click en el logo, con efectos
visuales y sonoros dramaticos.

Se introducen widgets nuevos (DiarySlider, DiaryProgressBar,
DiarySeparator, DiaryOnlineOptionsScreen, DiaryAmbienceSlider), y el
pergamino envejece con el uso a traves de un contador de aperturas
persistido en la configuracion.
## v0.12.1 - La ciudad se acuerda de respirar

Muchos detalles menores que juntos cambian el pulso del gabinete.

En el cielo, algunas estrellas pesan mas que otras: unas parpadean con
prisa, otras arden lentas y firmes. Cada una tiene su propio compas. Una
ventana perdida en algun piso alto se enciende y se apaga sin razon
aparente, cada tantos minutos, como si alguien no pudiera decidirse a
dormir. Un farol del extremo derecho comienza de pronto a temblar
erraticamente, tres o cuatro segundos de convulsion amarilla, y luego
vuelve a su cadencia mansa. Cada tanto una silueta pequena cruza la
calle abajo, rapida, oscura: probablemente una rata.

Bajo el nombre del proyecto, donde antes descansaba una fecha en cifras
romanas, ahora aparece una linea distinta cada vez: apuntes del que
escribe, contando dias, midiendo silencios. "Dia 47. Aun no encuentro
respuestas." "Dia 118. Aun no cede la fiebre." "Dia 178. Persisto."
Cada tantos minutos cambia; cada arranque del juego elige una nueva al
azar. Al pie de la pagina, la firma tambien rota entre cuatro manos:
S. Pereira, R. Da Rosa, G. Bandeira, A. Focco.

El gabinete de ajustes gano una frase rotativa del cronista bajo el
subtitulo, distinta cada vez que se abre. Los botones se sienten con
mas peso: al presionar se hunden apenas un pixel, la sombra debajo se
achica, el color se oscurece. El hover ganó suavidad: ya no cambia de
golpe, se acomoda. Los interruptores tienen un click mas grave y
mecanico que los botones normales.

El historial de cambios luce mejor: cada titulo grande empieza con una
capital destacada en tinta oscura, y entre secciones hay un divisor con
rombo central y lineas que se desvanecen a los costados, como en una
carta antigua. Ademas recuerda la posicion en la que quedaste, para no
tener que buscarla al volver.

En Lenguas, la lista tambien recuerda el scroll. En los cuadros de
confirmacion suena una campanilla al aparecer. Y cuando alguna pantalla
del gabinete queda a medio dibujar, ya no aparecen restos del despacho
anterior encima.

## v0.12.0 - Cierre del ciclo del gabinete

Version administrativa que marca el cierre del ciclo largo v0.10 - v0.11
(el gabinete pergamino). Sin codigo nuevo: solo consolidacion narrativa,
reagrupamiento del changelog in-game bajo la entrada unica "Una casa de
papel", y salto de version para senalar que la fase de UI esta cerrada.

Lo que venga a partir de aqui sera contenido de gameplay: la Gripe Verde
en si misma, con sus sintomas, sus cuarentenas y sus mecanicas de
sobrevivencia.

## v0.11.14 - Banderas de las voces

Cada lengua que la ciudad reconoce lleva ahora su ensena junto al nombre.
Franjas, cruces, estrellas y lunas: pequenos estandartes que ondean al
costado de cada linea, como los pendones alineados en el frente de un
consulado. La mirada aprende antes por el color que por el trazo.

No todas las voces tienen bandera propia. Algunas viejas jergas, dialectos
del interior o inventos de la calle se anuncian con un simple rombo de
tinta: un sello anonimo, digno de quienes hablan sin patria fija.
## v0.11.13 - El gabinete se ajusta a la mano

En esta noche cuatro cosas menores ganan el gabinete. La primera es
de peso: la musica del cronista responde ahora al instante cuando la
mano acaricia el regulador. Ya no hay que esperar a soltar la aguja
para que el volumen obedezca; el sonido se pliega al gesto como debe
plegarse.

Las tarjetas del atlas de ciudades respiran ahora con contornos mas
suaves; el rojo alarmante de los puertos inalcanzables ha sido
atenuado por una sombra sepia oscura que descansa detras del ping,
sin agredir al pergamino ni ocultar el aviso.

Los libros de correspondencia y los auxilios y percepciones
recibieron un desvanecido pergamino en su ribete superior e inferior,
de modo que el contenido no se corta con arista dura sino que se
desvanece contra los margenes como tinta seca.

En la bitacora de comandos, los interruptores de conducta -
agacharse, correr, salto automatico, pestana de administrador - dejan
atras su tono metalico de fabrica y visten hoy tira de papel ocre,
con circulillo relleno cuando la conducta esta activa y hueco cuando
descansa. Textos que exceden la caja se recortan con puntos
suspensivos discretos.

Los apuntes de asentamiento nuevo y las travesias directas se
escriben ahora sobre papel: los antiguos rectangulos negros de
captura de texto fueron reemplazados por bandas de pergamino con
borde de tinta, cursor de tinta parpadeante y seleccion sepia.

Y por sobre todo, cada nota al margen que el gabinete susurra al
pasar el cursor - antes ventanas de un morado extranjero - viste hoy
pergamino con borde tinta. La ciudad victoriana ha ganado sus ultimas
esquinas.

## v0.11.13 - El gabinete se ajusta a la mano

En esta noche cuatro cosas menores ganan el gabinete. La primera es
de peso: la musica del cronista responde ahora al instante cuando la
mano acaricia el regulador. Ya no hay que esperar a soltar la aguja
para que el volumen obedezca; el sonido se pliega al gesto como debe
plegarse.

Las tarjetas del atlas de ciudades respiran ahora con contornos mas
suaves; el rojo alarmante de los puertos inalcanzables ha sido
atenuado por una sombra sepia oscura que descansa detras del ping,
sin agredir al pergamino ni ocultar el aviso.

Los libros de correspondencia y los auxilios y percepciones
recibieron un desvanecido pergamino en su ribete superior e inferior,
de modo que el contenido no se corta con arista dura sino que se
desvanece contra los margenes como tinta seca.

En la bitacora de comandos, los interruptores de conducta -
agacharse, correr, salto automatico, pestana de administrador - dejan
atras su tono metalico de fabrica y visten hoy tira de papel ocre,
con circulillo relleno cuando la conducta esta activa y hueco cuando
descansa. Textos que exceden la caja se recortan con puntos
suspensivos discretos.

Los apuntes de asentamiento nuevo y las travesias directas se
escriben ahora sobre papel: los antiguos rectangulos negros de
captura de texto fueron reemplazados por bandas de pergamino con
borde de tinta, cursor de tinta parpadeante y seleccion sepia.

Y por sobre todo, cada nota al margen que el gabinete susurra al
pasar el cursor - antes ventanas de un morado extranjero - viste hoy
pergamino con borde tinta. La ciudad victoriana ha ganado sus ultimas
esquinas.

## v0.11.9 - Coherencia del gabinete

En esta noche el gabinete respira una sola tinta. Todas las hojas que
se abren desde las Opciones - las lenguas conocidas, las camaras y
resonancias, los protocolos de correspondencia, los auxilios y
percepciones, la efigie y porte del cronista, la bitacora de comandos,
las menciones y agradecimientos, el atlas de ciudades - llevan hoy la
misma caligrafia en sus encabezados, el mismo trazo bajo los titulos,
la misma lozanga entre los divisores.

Los pergaminos, antes livianos como recortes pegados al aire, proyectan
ahora sombra sobre la escena atenuada. Las tiras de papel que sirven de
botones descansan claramente sobre el fondo, con un tono ocre mas
oscuro que las distingue del pergamino sobre el que se apoyan.

La lista de lenguas dejo atras su cartulina lisa y ahora comparte la
textura de manchas del gran pergamino. La banda tras el asentamiento
listado en el atlas se aligero: ya no es un parche marron sino una
sombra sepia que da lectura sin agredir. La barra por la que se corren
las hojas largas corre ahora contra el borde del pergamino, no en el
medio de la escena.

La musica del cronista, que antes se detenia en cuanto uno cruzaba
hacia una sub-hoja, sigue viva ahora por todos los pasillos del
gabinete. Y en la hoja de lenguas, retirarse con la tecla de escape
descarta el cambio, como corresponde a quien se arrepiente de haber
elegido otra voz.
## v0.11.8 - El atlas de puertos ajenos

En esta noche el cronista ha completado el capitulo dedicado a la
correspondencia con ciudades distantes. El atlas de puertos ajenos ya
no se despliega sobre paneles ordinarios de fabrica: cada mapa, cada
apunte de asentamiento nuevo, cada travesia hacia un puerto sin
cartografiar se abre ahora sobre pergamino, envuelto en la misma
paciencia con que se anota una carta a la luz de una vela.

La entrada del observatorio oficial de la Gripe Verde ha quedado
grabada en el atlas por siempre. Cualquier viajero que consulte
estas paginas encontrara alli el nombre y las coordenadas del
observatorio principal, incluso si trata de arrancarlo del papel.

Las senales de balizamiento lejanas y los ecos de puertos cercanos
han sido apartados a otra pagina, para que el ojo no se distraiga
cuando ya hay ciudades reconocidas en el atlas.

Bajo cada asentamiento reposa ahora una tarjeta sepia con su nombre
y descripcion, y una linea de tinta tenue a modo de firma.

## Notas de Actualización

## v0.11.7 - Dos salas mas del gabinete

Dos habitaciones mas del gabinete recibieron la mano del cronista.

La sala de Controles amanecio ordenada sobre pergamino: los dos porticos que llevan al cuidado del raton y a las llaves de la casa quedan alli mismo, junto a las cuatro pequenas voluntades sobre agacharse, correr, saltar y la pestana del administrador. El divisor y los angulos ya son como los del resto del gabinete.

Las Menciones y agradecimientos tambien se disponen ahora sobre papel, en su hoja propia, y desde ahi se llega a la larga secuencia de nombres que hicieron posible la cronica.

Cuatro puertas siguen abriendo a habitaciones oscuras: las opciones del raton, el tomo de atajos, la seleccion de paquetes y las opciones en linea. El cronista las visitara con calma en la proxima entrega.

### Del diario
- La sala de Controles se acomodo sobre pergamino, con sus divisores y esquineros.
- Las Menciones y agradecimientos ganaron hoja propia sobre papel.
- Los dos porticos de Controles llevan al raton y a las llaves de la casa.
- Cuatro puertas del gabinete siguen conduciendo a habitaciones oscuras, en espera.
## v0.11.6 - La camara de musica del gabinete

Anoche la puerta que decia "Musica y sonidos" dejo de conducir a una sala prestada de otra casa. Detras de ella se abre ahora un ala propia, tapizada del mismo pergamino que el resto del gabinete, con sus divisores en filete y sus esquineros dobles. Los antiguos tiradores de piedra siguen alli, ordenados como siempre, y entre ellos permanece la lampara que ilumina las noches de escritura. Pero al pie del panel hay un tirador nuevo, empotrado en laton, con una marca de tinta victoriana en el centro. El cronista lo llama Musica del Cronista, y con el ajusta a su gusto el volumen del organillo lejano. El valor elegido queda anotado en el diario de configuraciones, para que no se pierda al cerrar la sala.

En el mismo movimiento, el vestibulo mayor se acomodo. La placa que decia "Telemetria" nunca perteneció del todo a esta casa: era un aviso oficial, redactado en otro idioma, que hablaba de datos y medidas ajenas a la ciudad. Fue retirada sin ceremonia. Los diez tiradores restantes se acomodaron en cinco filas simetricas, y la placa de "Correspondencia en linea" subio a compartir la ultima fila con la de "Autores y agradecimientos", cerrando el conjunto con simetria.

### Del diario
- Se abrio un ala propia para la camara de musica, sobre pergamino.
- Un tirador de laton nuevo, la Musica del Cronista, permite ajustar el organillo lejano.
- El valor del tirador queda escrito en el diario de configuraciones.
- La placa de Telemetria fue retirada del vestibulo mayor.
- Los diez tiradores restantes se acomodaron en cinco filas simetricas.

---

## v0.11.5 - El escriba afila su pluma

La sala de accesibilidad y la de protocolos de correspondencia comparten esta noche un pequeno hallazgo: cuando el lector desliza el pergamino con la rueda, el marcador de renglones ya no es una plancha de hierro grisacea, sino una lengueta de tinta con su rombo central, como el que se estreno en el registro de lenguas. Es un detalle menor y sin embargo, cuando uno recorre las opciones de arriba a abajo, la ciudad se siente mas de una sola pieza. El gabinete de accesibilidad se ha ensanchado un poco para dar mas aire a los widgets que se apretujaban contra los bordes.

### Del diario
- El marcador de renglones se ha vestido de tinta en la sala de accesibilidad.
- La sala de correspondencia recibio el mismo cambio, sin ceremonia.
- El pergamino de accesibilidad se ha ensanchado; los renglones respiran mejor.

---
## v0.11.3 (2026-07-XX)

El cronista abre dos nuevas puertas de su gabinete. En una halla ajustes para sus propios sentidos: la brasa que titila, el trueno que sacude, la voz que dicta las palabras impresas. En la otra, las convenciones para dirigirse a otros que caminen la ciudad: la pluma que responde, el papel donde llegan las noticias, el silencio que se guarda. Ambas salas huelen a pergamino, a tinta seca y a lampara de aceite. Los marcos se han labrado con esquineros dobles y los divisores llevan un rombo tipografico en el centro. Bajo cada titulo se ha anotado, con letra tenue, la naturaleza de la sala.
## v0.11.2 - Protocolos de correspondencia

Esta noche los protocolos de correspondencia tambien han sido acogidos por el diario. La pantalla en que se ordenan las reglas del chat luce ahora sobre pergamino, con las mismas divisas en las esquinas y las mismas rayas de tinta que ordenan el gabinete y el registro de lenguas. Nueve renglones caben con holgura entre los margenes, y el boton de retorno responde al mismo gesto que ya se conoce.

### Del diario
- Los protocolos de correspondencia se han ordenado sobre pergamino.
- La pantalla adopta las divisas y renglones del gabinete y del registro de lenguas.
- Los ajustes internos se conservan intactos; solo la presentacion ha cambiado.

---
## v0.11.1 - Lenguas conocidas

El gabinete que ya lucia sobre pergamino se ha ordenado con oficio. Divisas discretas guardan sus cuatro esquinas; dos rayas de tinta separan lo que se lee de lo que se firma; un renglon bajo el titulo anuncia la mano del cronista. Y en su interior, por fin, la lista de lenguas del mundo se ha vuelto legible: cada una en su renglon limpio, sin la mancha vulgar que las tapaba, la elegida marcada con una flecha, las demas esperando su turno. Tambien los edictos de la calle han bajado la voz. Cuarentena y Real Decreto ya no gritan al observador; ahora respiran con el resto del papel.

### Del diario
- Las lenguas del mundo se han ordenado sobre pergamino, limpias y legibles.
- El gabinete recibio divisas en sus esquinas y renglones que dividen sus partes.
- Los edictos de Cuarentena y Real Decreto se han suavizado en su tipografia.

---
## v0.11.0 — El gabinete de ajustes

Hasta esta noche, quien buscaba modificar las condiciones bajo las cuales el diario era consultado se topaba con una hoja tosca, escrita a maquina, ajena por completo al espiritu del cuaderno. El contraste era violento: se salia de la vigilia y se caia en una oficina mal iluminada. Esta noche, el gabinete tambien ha sido incorporado. Sobre el mismo pergamino y con la misma tinta, el observador encuentra ahora dispuestas las opciones de la vigilia. La ciudad sigue viva detras del panel, atenuada, sus faroles latiendo con paciencia mientras uno ajusta lo que necesite ajustar.

### Del diario
- El gabinete de ajustes luce sobre pergamino, con la ciudad viva de fondo.
- Los ajustes son alcanzables desde la vigilia inicial y tambien desde la pausa nocturna.

---
## v0.10.4 — Halo lunar y jirones de niebla

Esta noche la luna ha ganado un halo tenue, como si el vapor de las calderas y la humedad del Támesis se le pegaran alrededor. Tres jirones finos la cruzan lentamente, sin llegar a taparla del todo. La ciudad respira mas honda bajo esa luz enferma.
## v0.10.3 — Controles de audio del Title

- Boton `>>` en el menu principal para pasar a la siguiente pista con crossfade.
- Boton `AMB` en el menu principal para silenciar o reactivar el ambiente.
- Persistencia del estado de ambiente en `gripeverde-client.toml`.
- `MenuAmbience.start()` ahora es idempotente.
- Fix del skip manual: la nueva pista ya no entra muda al iniciar el crossfade.
## v0.10.2 — Corrección administrativa

Ajuste interno del diario para que el sello de la edición actual coincida con la última noche registrada. Sin novedades observables en la ciudad.

---

## v0.10.1 — La memoria de la mano

Hasta ahora, cada vez que el observador volvía al diario, la ciudad recuperaba su voz al mismo volumen invariable, como si nadie hubiera tocado nunca la clavija. Esta noche eso ha cambiado. El diario recuerda con qué fuerza se le pidió por última vez que sonara la calle, y respeta ese ajuste al reabrirlo. Un pequeño acto de cortesía hacia quien lo consulta.

### Del diario
- El diario conserva el volumen elegido para el rumor de la ciudad.
- Ya no vuelve al valor de fábrica al cerrarse la vigilia.

---

## v0.10.0 — El diario se viste con la solemnidad que merece

Durante semanas, las páginas dedicadas al historial de la ciudad tuvieron un aspecto pobre, como notas garabateadas en el reverso de un sobre. Esta noche por fin lucen como deben: fibra de pergamino auténtica bajo los dedos, manchas de vela y de tinta antigua, el peso de un documento oficial. Además, las crónicas mismas han sido reescritas. Antes se colaban en ellas expresiones extrañas, propias de un ingeniero de gabinete y no de un cronista de calle. Han sido depuradas. Ahora todo lo que se lee en el diario es lo que el ojo, la niebla y el gas verían.

### Del diario
- El historial luce sobre pergamino con textura y viñeta.
- Se han eliminado descripciones impropias de un caballero observador.
- Las crónicas antiguas han sido reescritas en un tono más fiel a la ciudad.

---

## Versión 0.9.2

**El humo ya no corta el cielo.**

Hasta hace poco, los penachos que ascendían desde las chimeneas parecían recortados con tijeras: bloques oscuros suspendidos entre los tejados, más propios de un decorado apresurado que del aire cargado de una ciudad viva. Esta noche el hollín se comporta como debe. Sube en volutas suaves, se disuelve en la bruma, y las nubes que lo acompañan por el firmamento han perdido sus ángulos rectos. La luna, cuando le corresponde, se oculta tras un velo redondo y no tras un rectángulo torpe.

### Del diario
- El humo de las chimeneas se eleva con formas suaves y naturales.
- Las nubes del cielo nocturno han perdido sus bordes cuadrados.
- El velo que cubre la luna se disuelve con delicadeza.

---

## Versión 0.9.1

**Los faroles vuelven a arder.**

Durante meses, la avenida ha permanecido en penumbra: las farolas de gas fueron retiradas por reformas que nunca terminaron, y sólo la luz turbia que escapaba de las ventanas alcanzaba el empedrado. Esta noche los operarios han regresado. Los postes de hierro forjado están de nuevo en su sitio, uno tras otro a lo largo de la calle, y sus llamas titilan bajo las cúpulas de vidrio. Los charcos de la calzada mojada devuelven un reflejo dorado que hacía tiempo faltaba.

### Del diario
- Los faroles de gas han sido reinstalados a lo largo de la avenida.
- Sus llamas apoyan con firmeza sobre la piedra, sin flotar en el aire.
- El número de faroles se adapta con discreción al ojo del observador.

---

## Versión 0.9.0

**La ciudad ha ganado profundidad.**

Al mirar por la ventana del despacho, algo ha cambiado en el horizonte. Las luces que arden en los edificios lejanos ya no compiten con las que titilan tras los cristales cercanos: se han vuelto puntos tímidos, como si supieran que están muy lejos y no quisieran molestar. Las chimeneas, en cambio, han encontrado por fin su lugar. Coronan los tejados con firmeza, y sus penachos de hollín suben rectos hacia el cielo cargado.

El aire también se ha despejado de esas franjas grises que antes cruzaban el firmamento sin razón aparente. Ahora sólo queda la bruma legítima, la que siempre estuvo, la que pertenece a esta ciudad enferma.

### Del diario
- Las ventanas de los edificios lejanos se ven distintas de las cercanas: profundidad real.
- Las chimeneas están ancladas a sus edificios, ya no flotan en el vacío.
- El cielo ha quedado limpio de artefactos que no le pertenecían.

---
## Versión 0.8.3

**Hay silencios que se guardan por respeto.**

Cuando las campanas de la parroquia doblan a lo lejos, cuando un trueno hiende el cielo o cuando los cuervos alzan vuelo entre los tejados, la música de la ciudad se aparta con discreción. No calla del todo; sólo baja la voz, como quien se descubre la cabeza al paso de un cortejo, y espera. Cuando el presagio se disuelve en la niebla, la melodía regresa al primer plano sin prisa.

También los hilos de la memoria se han vuelto más firmes. Aunque se aparte la mirada del diario un instante o se busquen otros apuntes, la canción de la ciudad ya no se interrumpe sin motivo. Persiste, acompañando al observador en su vigilia.

### Del diario
- La música se aparta ante las campanas, los truenos y el vuelo de los cuervos.
- El lamento de la ciudad persiste a través de las páginas.
- Se detiene sólo cuando el deber llama a salir al mundo.

---

## Versión 0.8.2

**Las melodías se entrelazan sin quiebre.**

Antes, cuando una pieza terminaba, el silencio caía de golpe. Ahora es distinto. Antes de que una melodía se apague del todo, la siguiente ya comienza a susurrar por debajo. Ambas conviven unos segundos como dos voces en un mismo cuarto, hasta que una se despide y la otra llega. La costura no se nota.

### Del diario
- Transiciones suaves entre las canciones de la noche.
- Sin cortes bruscos ni silencios prolongados.

---

## Versión 0.8.1

**Ahora puedes decidir cuánto quieres oír.**

No todos los oídos son iguales. Se ha dispuesto un pequeño control para que quien lee decida con qué fuerza le llega el lamento de la ciudad. Puede acallarlo si el momento pide calma, o dejar que vuele libre si el ánimo lo permite. El ajuste es inmediato y la melodía se acomoda al instante, obedeciendo la mano.

---

## Versión 0.8.0

**La ciudad ha encontrado su canción.**

Durante mucho tiempo, la calle sólo hablaba con sus propios ruidos: el viento, la lluvia y el eco de los que caminan. Pero ahora, de alguna parte que no podemos señalar, ha llegado la música. Cuatro melodías se turnan en la noche, mezclándose con la tormenta sin taparla. Es la primera vez que este diario tiene una voz que lo acompaña.

---

## Versión 0.7.1

**Las farolas se apagan por prudencia.**

Los viejos postes de gas ya no alumbran la calle del diario. Su luz, antes suspendida sobre un suelo incierto, confundía más de lo que guiaba. Por ahora han sido apagados. No han sido retirados de la ciudad; sólo esperan a que las calles tengan cimientos suficientes para sostenerlos. Es mejor una calle oscura que una mentira iluminada.

---

## Versión 0.7.0

**La calle vuelve a existir.**

Bajo la niebla ocre y la lluvia constante, la ciudad recupera algo que parecía perdido: una superficie bajo los pies. No es una avenida clara ni una plaza orgullosa. Es una calle oscura, húmeda, apenas sugerida por adoquines y reflejos. Pero basta. Por primera vez en mucho tiempo, la vista ya no cae al vacío.

---

## Versión 0.6.5

**La ventana ya no castiga la mirada que se aparta.**

Se corrigieron los males que hacían tambalear el diario al minimizar la ventana o al apartar el foco de ella. Ahora puede quedar en segundo plano sin precipitar la sesión al desastre.

---

## Versión 0.6.2

**El reloj respira.**

Aquel viejo reloj de bolsillo, que durante tanto tiempo pareció un mero adorno, ahora late con un pulso mecánico suave. Cada cinco segundos, cuando el segundero alcanza una de las marcas negras, un chasquido discreto llega al oído, como si el mecanismo, largo tiempo olvidado, hubiese recordado cómo trabajar.

### Un mecanismo vivo
- El segundero tickea en perfecta armonía con sus fallos.
- El reloj de bolsillo sólo suena cuando está a la vista. En las ventanas más pequeñas, descansa en silencio.

---

## Versión 0.6.1

**Algo se mueve sobre el escritorio.**

El reloj de bolsillo ha dejado de ser un retrato inmóvil. Ahora cuelga de una cadena larga que desciende desde el mismísimo borde superior del diario, y oscila con un lento pulso pendular, como si una corriente oculta lo acariciase.

Su segundero avanza con precisión mecánica, pero no siempre. De cuando en cuando tropieza: a veces se demora en una marca más de lo debido, a veces salta hacia adelante sin aviso, a veces retrocede un tiempo antes de continuar.

---

## Versión 0.6.0

**La crónica cierra un capítulo y abre el siguiente.**

El diario ha sido puesto en orden. Cada página, cada adorno, cada sonido: revisado y sellado. Lo que estaba disperso ahora está encuadernado.

---

## Versión 0.5.7

**Un reloj se detuvo a las once y cincuenta y dos.**

En la esquina superior derecha del diario ha aparecido un viejo reloj de bolsillo. Sus manecillas de bronce marcan siempre la misma hora: el instante en que todo se detuvo.

### La ciudad respira mejor
- El diario ahora se ajusta con más gracia a cualquier ventana.
- Los avisos oficiales y el aviso de recompensa encuentran su lugar sin estorbar.
- En pantallas más pequeñas, las páginas se simplifican por sí solas, mostrando sólo lo esencial.

---

## Versión 0.5.0

**El diario recuerda cada paso del camino.**

Ahora podrás consultar el historial completo de novedades directamente desde el menú principal. Un discreto símbolo aparece en la esquina superior derecha, esperando ser descubierto.

---

## Versión 0.4.4

**La ciudad ha cobrado vida.**

Las calles ya no permanecen en silencio. Al asomarte al menú principal podrás oír el pulso de una urbe atrapada por la Gripe Verde.

### Voces de la ciudad
- El viento recorre las callejuelas empedradas.
- La lluvia repica sobre los adoquines.
- Un murmullo distante se alza desde los tejados.
- Las campanas de la iglesia repican sin razón aparente.
- Un trueno rompe el cielo cargado de humo.
- Los cuervos anuncian lo que nadie quiere ver.

---

## Versión 0.2.5

**El diario luce más vivo que nunca.**

Las páginas han sido restauradas con esmero. Cada detalle busca transmitir la atmósfera de una ciudad en tinieblas.

- Las velas iluminan con un halo cálido y tembloroso.
- Las manchas de sangre se muestran con crudeza sobre el papel.
- Los sellos de cera lucen con relieve y sombra.
- El papel envejecido cubre las páginas sin cortes ni bordes.

---

## Versión 0.1.0

**Todo comienza aquí.**

La ciudad despierta bajo las primeras señales de la Gripe Verde. El diario abre su cubierta por primera vez.
