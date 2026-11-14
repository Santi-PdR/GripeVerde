# Contexto de continuidad: GripeVerde ModMenu

Actualizado: 2026-11-13

## Proyecto y reglas de trabajo

- Repositorio: [Santi-PdR/GripeVerde](https://github.com/Santi-PdR/GripeVerde) — **privado**
  (`visibility: private`, confirmado con la API). Consecuencia: sin credenciales, la API de
  releases y los assets responden **404**, aunque todo exista. El repo hermano
  `Santi-PdR/Photon-2` es público, y por eso el script de instalación de Photon funciona sin
  token; el de GripeVerde no puede.
- Rama de trabajo: `codex/v0.104-ghoul-warning-poster`
- Pull request: [#90](https://github.com/Santi-PdR/GripeVerde/pull/90)
- Versión de esta rama: `0.182.0` (anterior entregada: `0.181.0`)
- El usuario quiere seguir mejorando el mod de forma continua hasta que diga que pare.
- El trabajo se hace sobre el código fuente alojado en GitHub; no se clona ni se edita el
  proyecto local del usuario.
- No se controla Minecraft ni la PC del usuario para pruebas visuales. El CI acredita
  compilación, recursos y localización; no sustituye una revisión dentro del juego.
- Las interfaces del juego se mantienen instantáneas: no se agregan animaciones al
  inventario, la pausa ni el mundo. Las únicas piezas animadas siguen siendo las del menú
  y respetan Movimiento reducido y Destellos reducidos.
- **Entrega de cada versión:** el usuario no descarga archivos del workspace. Al terminar cada
  tanda hay que darle **un script de PowerShell para pegar**, que baja el `.jar` del último
  release directo a su carpeta de mods, respalda el anterior y verifica tamaño y SHA-256 contra
  lo que publica la API. No se guarda ninguna copia del JAR en el repositorio ni en el workspace.
  Tampoco se dispara la descarga desde el sandbox: la red de este entorno solo alcanza
  `github.com`, `api.github.com` y pocos hosts más, y el binario del release viene de
  `release-assets.githubusercontent.com` (da `EOF` acá).
- El script vive en `tools/install-latest.ps1` y usa, en este orden: `$env:GITHUB_TOKEN`,
  `gh auth token`, `git credential fill` (host github.com). Si no hay ninguna, corta con un
  mensaje que dice qué hacer. Verifica `size` y `digest` (sha256) que publica la API del
  asset, comprueba que el ZIP tenga `META-INF/MANIFEST.MF` y `mods.toml`, respalda los
  `*gripeverde*.jar` viejos en `<instancia>\gripeverde-jar-backups` y revierte si algo falla.
- El usuario **autorizó** pasar el repo a público (2026-11-13), pero **la integración de esta
  sesión no puede**: `gh repo edit` de esta versión no tiene el flag de consecuencias y
  `PATCH /repos` con `private=false` devuelve `403 Resource not accessible by integration`
  (falta permiso de administración). Queda en manos del usuario:
  `Settings > General > Danger Zone > Change repository visibility > Make public`.
  Repos hermanos de referencia, todos **públicos**: `Fisk-Superheroes`, `Photon-2`, `Lclient`.
  Los comandos cortos que el usuario probó con éxito salen de ahí, sin token.
- Mientras siga privado, `tools/install-latest.ps1` usa token si lo encuentra y, cuando el repo
  pase a público, funciona igual sin token (el fallback sin credenciales ya está escrito y, si
  recibe un 404 sin token, avisa exactamente eso).
- **Cambio de rama de la sesión (2026-11-13):** el checkout pasó a
  `arena/48be547c-gripeverde`, que arranca en `02e85bc` (0.103.0). Todo el trabajo de
  0.104→0.181 vive en `codex/v0.104-ghoul-warning-poster` @ `027f5f7`, que es la cabeza del
  PR #90. La rama de la sesión estaba en 0.173.0; se integró con `-s ours` para conservar su
  historial y dejar el árbol moderno.

## Estado confirmado al retomar (2026-11-13)

- `0.182.0` **publicada y en verde** desde la rama de la sesión: commit `a5d2bfd`,
  run `43558073201` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.182.0/gripeverde-0.182.0.jar`
  (9.447.525 bytes, `sha256:9ea5102c09f527c1acd404f3cf9c467e122d6bc188d7766e4be4e540b9db7e14`).
- El repo **ya es público** (el usuario lo cambió en Settings): el comando corto de
  instalación vuelve a funcionar sin token, como el de Photon/Fisk.
- `.github/workflows/build.yml` también compila `arena/**`, así que la rama de la sesión tiene
  sus propios runs y releases.
- `0.181.0` **publicada y en verde**: commit de código `421be29`, run `43550635643`
  (un intento previo `8d995dc` cayó por reimportar `net.minecraft.util.Util`; el
  comentario automático del PR lo señaló en tres líneas).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.181.0/gripeverde-0.181.0.jar`
  (9.440.233 bytes).
- `0.180.0` **publicada y en verde**: commit de código `2fc259c`, run `43549058805`.
  El JAR ya no viaja como ZIP de artefacto: el job `publish` lo adjunta a un
  release, así que la descarga es directa a
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.180.0/gripeverde-0.180.0.jar`
  (9.416.179 bytes).
- `0.179.0`: commit `8ee47fc`, run `43545600000`, artefacto `14094161283`,
  8.753.677 bytes (ZIP).
- Referencias anteriores: `0.178.0` run `43543676142` (`7cd9d9f`; el primer intento
  `29e88f3` no compiló por leer el campo `this.children`), `0.177.0` run `43539899164`
  (`d9e7a52`), `0.176.0` run `43538221683` (`531a91d`), `0.175.0` run `43453123285`
  (`754b224`), `0.174.0` run `43449824926` (`eedfaa9`), `0.173.0` run `43447945730`
  (`9d81d3d`), `0.172.0` run `40763347401` con SHA-256 del JAR
  `9520432c3027a3695b4e1246c2f8a3298cdc8ce11ab0cbff517d6fc914508c60`.
- `0.172.0` **no** se instaló en la instancia del usuario; la carpeta de mods de destino
  sigue siendo `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`.
- **Instalación bloqueada en esta sesión por dos motivos ajenos al mod:** el entorno es
  Linux (la ruta `D:\...` no existe como montaje) y el host de descarga de artefactos
  (`productionresultssa*.blob.core.windows.net`) no es alcanzable desde el sandbox, así que
  tampoco se pudo verificar aquí el SHA-256 del JAR. Queda listo
  `tools/install-artifact.ps1`, que valida `mods.toml`, admite `-ExpectedSha256` (el hash del
  JAR ya extraído), reemplaza solo `gripeverde-*.jar` y avisa o aparta cualquier archivo que
  no sea `.jar`. Con el ZIP sin abrir, el hash a contrastar es el del ZIP:
  `5d6ddc32d264efc6b9159f419d305ec6472034ba282d16fefe2a2493820e076e`.
- La sesión actual corre en un entorno Linux remoto: `D:\...` no existe como ruta
  montada, así que la instalación queda pendiente de que la carpeta sea accesible.
- El validador local `python3 tools/verify_mirrors.py` reproduce los sellos de Gradle
  (idiomas, sonidos, código muerto, versión, duraciones, subtítulos, uso de sonidos,
  literales y balance) y pasa en verde con `0.175.0`.

## Cambios de 0.182.0 — la crónica respira

`ChangelogScreen` (1066 líneas) era la hoja más apretada del diario y la única que nunca se
había auditado: 22 `return true` entre lupa, barra, marcas del encuadernador y folio escondido.

- **Ritmo**: `LINE_H` 11→12, `H1_LINE_H` 16→18, `H2_LINE_H` y `BOLD_LINE_H` 13→14, `GAP_H`
  10→13, `SEP_H` 12→16, `PAD_Y` 26→30.
- **Margen lateral**: `PANEL_MX = 56` fijo pasó a `panelMx() = clamp(12, width/8, 56)`. A 320 px
  el panel crece de 208 a 240 de ancho; `panelW()` tiene piso de 120.
- **Botón Cerrar**: `closeW = clamp(84, min(130, panelW-24))` y centrado sobre el panel; en
  ventanas mínimas ya no sobresale de la hoja.
- **Clics**: las marcas del encuadernador y el folio escondido se consultan **después** de los
  widgets (`overAnyWidget()` con `this.children()`, igual que la portada desde 0.178.0).
- **Rueda**: si `maxScroll <= 0` devuelve `super.mouseScrolled` y no suena; si el gesto no movió
  nada, tampoco.
- **CI**: `.github/workflows/build.yml` compila también `arena/**`.

## Cambios de 0.181.0 — los mensajes de CopyL se vienen con nosotros

CopyL guardaba los diez mensajes rápidos en `config/copyl-messages.json`
(`names[10]`, `messages[10]`, `keyCodes[10]`, con `-1` libre, teclas GLFW crudas
y botones de mouse como `1000 + n`), y la tecla de apertura en `config/copyl.json`
(`openKey`, Alt derecho por defecto; `lclient.json` como legado). Desinstalarlo se
llevaba todo eso puesto.

- **`client/integration/DispatchLibrary`**: lee esos archivos **una sola vez** y
  guarda la copia propia en `config/gripeverde-dispatch.json`
  (`importedFrom`, `openKey`, `slots[]` con `name`, `message`, `key`). Nunca
  escribe ni borra archivos de CopyL. Normaliza igual que CopyL: una sola línea,
  texto ≤ 256, nombre ≤ 24, tecla válida o `-1`.
- **`client/DispatchHotkeys`**: sondea el estado físico del teclado (sin
  `KeyMapping`, para no tocar `options.txt`) y envía la ranura al flanco de
  bajada; `/algo` va por `sendCommand`, el resto por `sendChat`. Se **apaga
  entero mientras CopyL esté instalado** (`CopyLCompatibility.isAvailable()`),
  así ningún mensaje sale dos veces. La tecla de apertura levanta la ficha.
- **La ficha**: con `panelW >= 420` la ficha mide hasta 520 de ancho y abre una
  **columna de diez ranuras** a la derecha (nombre a la izquierda, tecla a la
  derecha, 22 px por fila); en ventanas angostas las ranuras son una **tira de
  chips numerados** arriba del formulario. Clic envía, `Shift`+clic carga el
  texto en el primer renglón libre, `Ctrl`+clic guarda el renglón en esa ranura y
  clic derecho fija tecla (Esc cancela, Supr borra).
- La línea de estado avisa: cuántas ranuras tienen texto, que CopyL está
  instalado y sus teclas dormidas, y la ayuda de la ranura bajo el puntero.
- **Idiomas**: 9 claves nuevas × 25 locales; el espejo pasa de 191 a **200**.
- **Para el usuario**: se puede sacar `copyl-*.jar` de `mods` sin perder los
  mensajes; al primer arranque la ficha los importa y los atajos pasan a
  responder. Si CopyL sigue instalado, siguen mandando los suyos.

## Cambios de 0.180.0 — el parte tiene su propia mesa

El acceso de CEDA de la portada era un puente por reflexión: solo aparecía si
`ModList` encontraba `copyl` y abría `CopyLSettingsScreen` de otro mod. Ahora el
formulario es nuestro (`DiaryDispatchScreen`) y el puente queda solo como puerta
opcional dentro de la ficha.

- **Siempre hay entrada**: el icono de la portada (x = `width - 76`, 20x20) ya no
  depende de CopyL; con CopyL instalado, su panel se abre desde la esquina
  superior derecha de la ficha (20x20, icono `DISPATCH_NOTE`).
- **Aire de sobra**: ocho renglones (`MAX_LINES`) de 16 px con **12 px de hueco**
  (`ROW_STEP = 28`), carril de **34 px** para el número (`01`..`08`) y un filete
  de tinta antes del pie. CopyL apilaba los renglones sin carril ni hueco.
- **Pie en dos filas** (`FOOTER_H = 84`): línea de estado y cuatro botones
  (ENVIAR, ENVIAR TODO, LIMPIAR, VOLVER) con 10 px entre ellos y `btnInset` de
  12 a 22 px según el ancho.
- **Comportamiento**: `Enter` envía el renglón enfocado (o el primero con texto) y
  pasa al siguiente; `Tab`/`Shift+Tab` recorren los renglones; el envío en tanda
  espera **10 ticks** entre mensajes; el borrador vive en un `static String[]`
  para que cerrar la ficha no borre lo escrito. Sin conexión, el estado avisa y
  no se envía nada (`player.connection == null`).
- **Tamaños**: la ficha mide `min(400, max(240, width - 24))` por
  `min(380, max(190, height - 24))`; los renglones visibles salen de
  `(rowsBottom - rowsTop) / 28` acotado a 3..8. Con el tope viejo de 300 px de
  alto solo cabían 5, así que la ficha creció a 380: ahora 320x240 y 427x240 dan
  3 renglones, 480x270 da 4, 640x360 da 7 y desde 700x400 hasta 1920x1080 dan
  los 8 completos.
- **Idiomas**: 14 claves nuevas (`gripeverde.dispatch.*`) en los 25 locales; el
  espejo pasa de 177 a **191** claves y se actualizó en `verify_mirrors.py` y en
  `build.gradle`.
- **Entrega**: `release-tag.txt` marca la versión a publicar; el job `publish`
  solo crea el release si el tag coincide con `build.gradle` y el release no
  existe, así que un push normal no publica nada y nunca se sobrescribe un
  release viejo con un build nuevo.

## Cambios de 0.179.0 — el cronista habla en toda ventana

`renderChroniclerNote()` salía en silencio en cuanto `isMinimalMode()` era cierto
(toda ventana de menos de 620 px de ancho, o de menos de 290 px de alto), y
también cuando la ficha no tenía carril (`cardW < 180`) o altura. Resultado: en
427x240 —la ventana por defecto— el saludo del cronista **nunca** se veía.

- `drawChroniclerCard()` es ahora la ficha completa y devuelve `false` en vez de
  salir por su cuenta: carril menor de 180 px, sin altura entre `logoBottom()` y
  `buttonTableTop()`, o lista de líneas vacía.
- `drawGreetingHeader()` dibuja la misma frase en **una línea centrada en y=10**,
  recortada a `width - 160` con `fitString`. La cabecera estaba libre en todos los
  tamaños: el control de música ocupa x 8..44 y los iconos de acceso empiezan en
  `width - 76`, así que quedan 36 px a la izquierda y 4 px a la derecha.
- Se cachea por ancho, fuente y texto, como el resto de notas de la portada.
- Cuando la ficha cabe, no cambia nada: 620x360, 700x400, 854x480, 1024x600,
  1280x720 y 1920x1080 siguen con ficha (390x44, o 204/224/284 donde el carril
  manda). Pasan a cabecera 320..619 px de ancho y las anchas por debajo de
  ~330 px de alto (620x290, 700x300, 720x340, 854x240).
- Sin colisión con el susurro de la vigilia: la cabecera está en y=10..19 y
  `vigilLineY()` nunca devuelve menos de `logoBottom() + GAP_MEDIUM` ni más abajo
  del pie.

## Cambios de 0.178.0 — ningún adorno roba un clic

`DiaryTitleScreen.mouseClicked` comprueba primero los puntos calientes
decorativos y después llama a `super.mouseClicked`, que es quien reparte el clic
entre los widgets. Varios de esos adornos devolvían `true`, así que un clic
destinado a un botón se quedaba en el adorno:

| Adorno | Tamaño | Solape con botones |
| --- | --- | --- |
| Ventana del ático (`signalWindowRect`) | 427x240 | 12 px dentro de la pareja (x 254..264, y 138..150 sobre Multijugador 116..204) |
| Rata (`ratHit`, y = 0,90 h) | 854x480 | 8 px dentro de la mesa y de Ajustes/Salir |
| Rata | 1280x720 y 1920x1080 | 2 a 8 px dentro de la pareja |
| Gato (`catRect`) | 427x240 | 7 px, pero ya estaba excluido por `isMinimalMode()` |

- `overAnyButton(mouseX, mouseY)` recorre `this.children` y pregunta a los
  widgets reales (`AbstractWidget.visible && isMouseOver`), así que no duplica la
  geometría de `init()` ni puede desincronizarse como las fórmulas repetidas.
- La bandera se calcula una vez por clic y protege reloj, bando de cuarentena,
  cartel del buscado, gato, rata, ventana del ático y clic del emblema.
- `DiaryPauseScreen`: la vela (`y = panelH - 46..panelH - 4`) se metía hasta 12 px
  dentro del último botón de la izquierda en la rejilla de dos columnas de las
  ventanas de 235 a 260 px de alto; ahora vela y cuervo también preguntan por los
  botones.
- El zumbido del clic sigue igual: si no hay botón debajo, el adorno responde como
  siempre.

## Cambios de 0.177.0 — el susurro de la vigilia encuentra su banda

`drawVigilLine` dibujaba el susurro de la vigilia en `height / 3 + 24`, una
posición fija que en **todas** las ventanas de menos de unos 410 px de alto caía
dentro del emblema:

| Tamaño | Emblema | Susurro antes | Ahora |
| --- | --- | --- | --- |
| 427x240 | 30..108 | 104..113, sobre el filete de la fecha | 216..225, al pie sobre la firma |
| 560x315 | 30..161 | 129..138, sobre «GRIPE VERDE» | 169..178, banda libre |
| 700x400 | 30..161 | 157..166, sobre el renglón del día | 169..178, banda libre |
| 854x480 | 30..161 | 184..193 | 184..193 (sin cambios) |
| 1024x600 | 30..161 | 224..233 | 224..233 (sin cambios) |

- `vigilLineY()` conserva la posición original cuando ya cae en banda limpia y,
  si no, busca la banda entre `logoBottom() + GAP_MEDIUM` (8 px bajo el filete
  de la fecha) y `buttonTableTop() - GAP_SMALL`; cuando no hay ni una línea de
  alto, baja al pie en `height - 24`, que deja 3 px sobre la firma
  (`height - 12`).
- El susurro nunca coincide con la ficha del cronista: la ficha se muestra 8 s
  al abrir la portada y el susurro exige 60 s sin interacción.
- `estimateFirstButtonTop()` pasa a devolver `buttonTableTop()`: era otra copia
  de la fórmula de la mesa derivada de `leftColumn()`, que en ventanas estrechas
  no coincide porque el grupo se centra. Ahora emblema, mesa y caja de telegrama
  comparten una sola fuente de verdad.
- Auditoría: 17 anchos (320 a 1920) x 15 altos (180 a 1080) = 255 combinaciones,
  sin fallos de susurro sobre el emblema, sobre la mesa, sobre la firma ni fuera
  de pantalla.

## Cambios de 0.176.0 — Ajustes y Salir se apartan del emblema

Defecto que la auditoría de 0.175.0 no vio porque solo comparaba el emblema con
la pareja principal: en **todas** las ventanas de menos de 620 px de ancho, la
fila de Ajustes y Salir se colocaba en `y = max(8, min(36, btnY - 36))`, o sea
en `y = 36..68`, justo sobre el emblema, que empieza en `LOGO_TOP = 30`. A
427x240 el escudo ocupa `y = 30..108` y los dos botones tapaban 32 px de su
franja media; a 560x315 tapaban 32 px de un emblema que ahí llega hasta 161.

- La pareja principal y el carril comparten banda vertical: el carril vive en
  `[btnY + (88 - 68) / 2, ...]`, o sea dentro de la mesa, que el emblema ya
  respeta desde 0.175.0. No hay solape posible por construcción, no por cálculo
  de distancias.
- `compactSlimRailWidth() = clamp(28 % del ancho interior, 40, 110)`: el carril
  nunca se lleva más de lo que la pareja necesita para su rótulo y subtítulo.
- El grupo (pareja + 4 px + carril) se centra con
  `btnX = max(compactInset, (width - groupW) / 2)`; a 320 px ocupa justo el
  ancho interior (198 + 4 + 78 = 280) y en ventanas más anchas queda sobre el
  eje del emblema, como los carteles.
- `btnY` en compacto pasa a ser `buttonTableTop()` directamente: antes se
  derivaba de `LayoutRegion.screen(...).inset(btnX)`, y con el grupo centrado
  `btnX` ya no es el margen, así que ambas fórmulas podían discrepar.
- `compactInset()` centraliza el margen compacto que estaba repetido en
  `init()` y en `buttonTableTop()`.

Auditoría: 13 anchos (320 a 619) x 10 altos (180 a 480) = 130 combinaciones, sin
fallos de carril fuera de pantalla, carril sobre la pareja, carril fuera de la
banda, emblema sobre la mesa, aire menor que `GAP_MEDIUM`, pareja menor de
120 px ni carril menor de 64 px. Las ventanas de 620 px o más no cambian
(conservan Ajustes y Salir en la columna derecha).

## Cambios de 0.175.0 — la portada deja de pisar sus propios botones

Auditoría de layout de `DiaryTitleScreen` con las fórmulas exactas del código. Cuatro
defectos reproducibles, corregidos solo tras medirlos:

- **Emblema sobre la mesa de botones.** El bloque del emblema (`LogoRenderer`) ocupa
  `y = 30..161`. La mesa de botones arranca en
  `height - inset - 88 - 16` (`inset` = 20, o `clamp(width/16, 8, 20)` en modo compacto).
  A **427x240** —el tamaño por defecto de una ventana 854x480 con escala GUI 2— los botones
  empiezan en 116, así que el emblema los invadía 45 px; igual en 480x270 (invadía 15 px).
  Ahora `logoScale()` mide la banda libre y encoge el bloque completo (`BLOCK_H`, `BLOCK_W`)
  con una transformación de pose alrededor de su ancla; por debajo de `LOGO_MIN_SCALE = 0.5`
  se omite antes que dibujarlo encima de los botones.
- **Ficha del cronista.** Estaba clavada en `y = 168`. En 620x290 y 700x300 la mesa de
  botones empieza en 166 y 176, así que la ficha caía encima de «Multijugador». Ahora se
  coloca bajo `logoBottom()` y se sube al límite de la mesa, o no se dibuja si no cabe.
- **Cadena del reloj de bolsillo.** Se dibujaba desde `topY = 0` y cruzaba la fila de
  iconos superiores (crónica, guía, CEDA), que ocupa `y 8..28` en el mismo carril de la
  derecha. Arranca en `ICON_ROW_BOTTOM + GAP_SMALL`.
- **Zona de clic del emblema.** Seguía siendo `y 30..130` con caja fija, de modo que en
  ventanas de 240 px el borde de «Multijugador» (y=116) disparaba el sello del emblema. Usa
  el alto real escalado, y el clic del ojo de la calavera se recalcula a la misma escala.

Verificado con la geometría real en 320x240, 427x240, 480x270, 560x315, 620x290, 700x300,
700x400, 854x480, 1280x720 y 1920x1080: emblema y ficha siempre terminan al menos 8 px por
encima de la mesa de botones, o se ocultan.

## Cambios de 0.173.0

- **Aviso CEDA de la portada (nota de campo Ghoul).** Pasa a leerse como ficha de campo:
  cabecera CEDA en tinta azul, filete, una placa de imprenta con el retrato del infectado y
  un rótulo de dos renglones. El retrato se rediseñó por completo (cráneo ovalado,
  mandíbula angosta, ojos azules con brillo, levita con cuello abierto) y se dibuja con
  formas fijas, adaptándose al tamaño del cartel. En carteles bajos el retrato se omite en
  lugar de comprimir el texto.
- **Pie sin solapes.** El aviso final («NUNCA VIAJES SOLO») y el sello CEDA ocupan zonas
  distintas: el sello circular de ojo queda en el margen izquierdo y el texto se limita a
  su derecha. Sustituye al ojo suelto anterior y ya no puede montarse sobre el parte.
- **Bando de cuarentena.** El retrato del pie se agrandó y ahora convive con la pena en dos
  columnas; el espaciado se compacta en carteles bajos y el sello de cera solo se dibuja
  si queda aire real bajo el último renglón.
- **Nueva clave de idioma** `gripeverde.poster.ghoul.caption` en los 25 locales
  (inglés/español regional, incluido `es_uy`). El total de claves pasa de 176 a 177 y la
  validación de Gradle y el espejo en Python se actualizaron.
- **Integración CEDA Dispatch.** `CopyLCompatibility` ahora registra en `latest.log` la
  versión instalada de CopyL y si dibuja sus paneles con texto plano o con la fuente
  personalizada. No cambia la pantalla, que sigue siendo de CopyL.
- Versión del mod, `build.gradle`, crónicas de los 25 locales, `CHANGELOG.md` y `README.md`
  actualizados a `0.173.0`.

## Investigación de CEDA Dispatch / CopyL (evidencia)

Repositorio fuente localizado: [Santi-PdR/Lclient](https://github.com/Santi-PdR/Lclient).
El JAR que usa el usuario, `copyl-3.9.4.jar`, corresponde a la rama
`codex/ceda-dispatch-visuals` (mods.toml `version="3.9.4"`, PR #18 de ese repo), no a
`main`, que quedó en 3.3.0.

Hallazgos concretos sobre los dos síntomas reportados:

1. **Caracteres cuadrados (tofu).** El commit `bdf77b8` («Use a clearer typeface across
   CEDA Dispatch», base 3.9.0) añadió `assets/copyl/font/AtkinsonHyperlegible-Regular.ttf`
   y `assets/copyl/font/dispatch.json` (proveedor `ttf`, `size: 9`, `oversample: 2`) y pasó
   todos los textos de los paneles por `DispatchText.styled()`, que aplicaba
   `style.withFont(copyl:dispatch)`. El commit `22f8c0f` («v3.9.1: use reliable default
   text in CEDA panels») retiró ese `withFont` y dejó `DispatchText.plain()`, con un
   comentario explícito: «so an unavailable custom font cannot become tofu boxes». Es
   decir, el propio CopyL documentó que los cuadros venían de su fuente personalizada.
   El `.ttf` y su JSON siguen empaquetados aunque ya nadie los usa.
2. **Textos superpuestos o duplicados.** El editor pasó a un dibujo híbrido:
   `MessageEditorScreen.render()` llama a `super.render()` (que ya pinta cada `EditBox` con
   su texto y su sombra mediante `AbstractWidget.render`), después `graphics.flush()` y
   luego `DispatchEditBox.drawFlatText()` vuelve a pintar el mismo texto sin sombra sobre un
   `fill` de papel. Son dos pasadas sobre el mismo píxel; además `getScreenX()` se mide con
   el ancho real de la fuente pero el repintado se desplaza usando `DispatchText.width`,
   con lo que cualquier diferencia de métrica desplaza el segundo texto. Las etiquetas
   centradas de `DispatchCard`/`drawCards` también se redibujan tras el flush.
3. **Integración de GripeVerde.** Es un puente por reflexión
   (`CopyLSettingsScreen(Screen)`); no envuelve ni sustituye la pantalla, no aplica estilos
   y no cambia la fuente global, así que no puede producir ni el tofu ni el doble dibujo.
   Su única vía de efecto visual sería abrir la pantalla equivocada, y CopyL 3.9.4 mantiene
   la clase y el constructor. El fallo está en CopyL; GripeVerde solo lo abre.
4. **Pendiente de verificación en cliente.** Confirmar en `latest.log` la línea
   `CEDA Dispatch x.y.z draws its panels with plain Minecraft text` (o el aviso si está en
   una versión con fuente) y comprobar si el JAR instalado es 3.9.4. Si el usuario tiene
   3.9.0–3.9.3, los cuadros se explican por la fuente; si ya tiene 3.9.4, lo que queda es
   el doble dibujo del editor.
5. **`build-output` de Lclient está desactualizado**: publica `version.txt` = 3.3.0 con
   SHA-256 propio, así que no debe usarse como fuente del JAR 3.9.4.

Corrección preparada para CopyL 3.9.5, en `tools/copyl/0001-copyl-3.9.5-one-text-pass.patch`
(SHA-256 `856be3c7…bef298`, 1.106 líneas, aplicado con `git am` limpio sobre `27854f6`):
quita la segunda pasada de texto del editor y la subclase `DispatchEditBox`, deja 6 px
entre los campos y la regla del margen para que las etiquetas no queden bajo las cajas,
borra el `.ttf`, `dispatch.json` y la licencia sin uso, y hace que el CI de Lclient falle si
vuelve a empaquetarse algo bajo `assets/copyl/font/`. Sube la versión a 3.9.5.

**Bloqueo del entorno:** la integración de GitHub de esta sesión puede escribir en
`Santi-PdR/GripeVerde`, pero responde `403 Resource not accessible by integration` al
crear refs o empujar en `Santi-PdR/Lclient`. Por eso la corrección queda como parche
reproducible; aplicarlo requiere una sesión con permiso sobre ese repositorio (o hacerlo a
mano con los pasos de `tools/copyl/README.md`).

## Pendientes

1. **Publicar CopyL 3.9.5:** aplicar `tools/copyl/0001-copyl-3.9.5-one-text-pass.patch` en
   `Santi-PdR/Lclient` (rama `codex/ceda-dispatch-visuals` o PR #18), dejar que el CI
   genere `CopyL-3.9.5` y sustituir `copyl-3.9.4.jar` en la carpeta de mods. El usuario ya
   autorizó la corrección; falta un entorno con permiso de escritura sobre Lclient.
2. **Instalar `0.175.0`** en la carpeta de mods cuando el equipo del usuario sea accesible:
   descargar el artefacto del CI, validar contenido y SHA-256, retirar solo los JAR
   antiguos de GripeVerde (y la versión previa de CopyL si se publica una nueva) y dejar la
   carpeta con archivos `.jar` únicamente. En una máquina Windows con la instancia montada:
   `powershell -ExecutionPolicy Bypass -File tools\install-artifact.ps1 -ArtifactZip <zip del artefacto>`.
3. **Revisión visual del usuario** del nuevo aviso Ghoul y del bando: el retrato se validó
   con una simulación de píxeles 1:1 del código, no dentro de Minecraft.
4. Actualizar la descripción del PR #90 con `0.173.0`, el resultado del CI y estos
   pendientes.

## Contexto de estilo y contenido

El mod se inspira en Ghouls/Left 4 Dead 2 y presenta una epidemia ficticia en el año 1800,
ocultada como «Gripe Verde». Los infectados son personas vivas transformadas, con piel
azulada y ojos azules brillantes: siguen siendo personas, no cadáveres. La interfaz debe
sentirse como papeles, avisos y anotaciones de época con señales de cuarentena, CEDA, tinta
roja y azul Ghoul. El tono de diarios y crónicas debe sonar natural, como testimonios
humanos, no como instrucciones técnicas.

Los idiomas principales son inglés y español, con sus variantes regionales; toda variante
`en_*` debe usar inglés y toda variante `es_*` español. No usar otros idiomas.

## Consejo de diseño vigente

Interfaces distinguidas y legibles, tipografía vanilla clara cuando una fuente personalizada
pueda producir tofu, jerarquía visual, sin sombras de texto excesivas, sin solapamientos y
con animación limitada a la portada. El juego en curso y las pantallas de
inventario/pausa deben seguir libres de animaciones añadidas.

## Próximo paso técnico recomendado

Que el usuario pruebe `0.181.0` sacando CopyL de `mods`: la ficha importa los diez mensajes al primer
arranque y los atajos pasan a responder. Revisar a ojo la columna de ranuras. Queda pendiente llevar el
mismo aire a las pantallas de CopyL: el push a `Santi-PdR/Lclient` sigue en 403, así que ese cambio
viaja como parche en `tools/copyl/`.
