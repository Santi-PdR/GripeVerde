# Contributing - Eternal Craft: Gripe Verde

Guia corta para tocar el mod sin romper las reglas del proyecto.

## Estado vigente de la entrega

- Última publicación verificada: **v0.401.0**, tag dirigido al commit construido
  `e0f845007681e92e747e04127169f483e159456b`; JAR de 10.855.632 bytes, SHA-256
  `dd37d443b7307c1b3d0d09fc5befdaee6d1dd887bfca002d6e7bc3670ef0ee88`.
  El CI de publicación `44997077455` pasó en `build` y `publish`.
- La próxima versión requiere sincronizar versión, marcador, CHANGELOG y las 25
  notas dentro del juego; el workflow publica automáticamente solo tras pasar
  todas las comprobaciones.
- Rama de trabajo vigente: `arena/48be547c-gripeverde`, un solo push por entrega.
- Baseline medida por las herramientas: **73 archivos Java**, **25 locales con
  192 claves** cada uno, 35 eventos de sonido, 42 referencias OGG y 42 archivos
  OGG, 4 piezas de música y **8/8 sellos espejo**.
- Cada publicación es: `python3 tools/verify_mirrors.py` -> `python3 tools/lint_java.py`
  -> `python3 tools/test_list_render_guards.py` ->
  `python3 tools/test_smooth_scroll.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_language_screen.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_language_reload.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_dispatch_save.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_dispatch_hotkeys.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_server_count.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_memory_shutdown.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_marginalia_layout.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_discoveries.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_menu_music.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_menu_ambience.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_ui_sounds.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_title_open_count.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_slider_input.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_button_input.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_house_audio.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_option_screen_safety.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_world_navigation.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_server_form_layout.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_server_forms.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_text_fields.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_confirm_screen.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/ci/test_release_gate.py` ->
  `python3 tools/ci/test_installer.py` -> commit + push -> esperar el CI ->
  comprobar el release y el SHA-256. El job `publish` compara `release-tag.txt`
  con `build.gradle`, crea el tag sobre `$GITHUB_SHA` (el commit que produjo el
  JAR) y no sobrescribe un release existente.
- Pantallas de opciones: `test_option_screen_safety.py` compila Accesibilidad
  completa con dobles GUI/config, y métodos de Sonido con reloj/audio/lore dobles.
  22 escenarios (hitbox actual antes de render/tras resize, cooldown/reentrada,
  aislamiento en partida, temporizador de silencio cero/negativo/reinicio y
  toggles persistentes) + 4.494 casos de geometría init/render/resize desde
  160x120. No certifica rendering, audio ni E/S reales; mantener los secretos
  fuera de partidas activas y no usar relojes de pared en sus plazos.
- Navegación de Mundos: `test_world_navigation.py` ejecuta métodos reales
  extraídos con dobles de listas/GUI: foco de botones/buscador, scroll hacia la
  selección y estabilidad en viewport pequeño, búsqueda sin corte de 128 y
  primer índice visible sin corte de 64. 45 escenarios, 72.000 puntos de hit
  test y 360 rangos de filas contra oráculos lineales. Los recorridos de render
  tienen guardas de fuente; no se ejecuta el render ni carga/borrado de mundos.
- Layout de formularios: `test_server_form_layout.py` compila las dos clases
  completas con dobles GUI/servicios y registra controles y texto emitidos por
  init/render. Barre 7 anchos, todas las alturas de 120 a 320, ES/EN, tres entradas
  y el aviso antes/después de resize y al expirar: 50.652 casos geométricos.
  La fuente doble es monoespaciada 6x9: no sustituye prueba de glifos/render real.
- Formularios de servidor: `test_server_forms.py` ejecuta 57 escenarios sobre
  métodos reales extraídos: validación final, aceptación/cancelación únicas,
  reentrada desde callback/historial/options, foco de pie tras resize, chips
  recientes obsoletos y cooldown monotónico de la dirección especial.
  Usa dobles de widgets, Screen, parser, reloj, historial, options y lore; no
  certifica conexión, E/S real ni entrada nativa. `test_text_fields.py` conserva
  sus dobles official/SRG del widget completo y ahora ejecuta 70 escenarios.
- Feedback UI: `test_ui_sounds.py` ejecuta 22 escenarios con UiSounds completo
  y motor/registro/config dobles. No introducir relojes de pared en cooldowns
  ni consumir la ventana de reintento cuando la reproducción falla; un sonido
  opcional no debe impedir que se ejecute la acción de interfaz.
- Audio de menú: probar mute/reanudación, cero/subida, reloj y susurros sin
  sonido, entrada a mundo y recargas repetidas. `test_menu_ambience.py` compila
  la clase completa con motor/registro/config dobles (31 escenarios); música
  tiene 16 y La casa 18 (callbacks extraídos y ciclo de vida doble). Esas
  pruebas no certifican audio audible ni recargas de recursos de Minecraft.
- Scroll debe consultar MotionPolicy también en el menú, no solo comprobar
  si hay mundo: 21 escenarios de SmoothScroll/MotionPolicy reales con dobles.
  Las animaciones de clic deben respetar isValidClickButton; los 20 escenarios
  de `test_button_input.py` prueban métodos extraídos y una política remapeada.
  La tarea de sliders añade 16 checks de rechazo en botones reales sin cliente.
- Recarga de idiomas: `test_language_reload.py` cubre 23 escenarios con
  métodos extraídos y futuros/executor/pantalla/audio dobles. Manejar ambas
  finalizaciones en el hilo cliente, desbloquear error con reintento y no
  navegar ni recuperar audio si otra pantalla tomó el control. Esto no prueba
  packs reales ni rollback; se conserva la preferencia que ya fue guardada.
- Confirmaciones: usar el hook addButtons (sin duplicados ocultos), respetar
  setDelay en controles propios y dirigir Enter por identidad/foco, nunca por
  texto traducido. Fijar el latch de decisión antes de efectos/callbacks.
  `test_confirm_screen.py` tiene 29 escenarios con dobles. Check/build requieren
  además `verifyConfirmationRegression`: 14 checks con padre y botones reales,
  sin init/render/cliente ni efectos de aceptación/E/S de memoria.
- `./gradlew check` y `build` incluyen `verifyServerAddressRegression`:
  38 comprobaciones ejecutan los helpers de direcciones contra el parser real
  de Minecraft sin red ni cliente gráfico. Puede correrse esa tarea sola.
- También incluyen `verifyTextFieldGeometryRegression`: 33 comprobaciones
  del margen lógico, bindings oficiales, estado de selección y formateadores
  con widgets reales, sin rendering ni eventos de mouse. `test_text_fields.py`
  añade 62 escenarios con nombres oficiales/SRG dobles y métodos de formularios
  extraídos: no ejecuta el JAR reobfuscado ni la navegación real de Screen.
  Al tocar reflexión de EditBox, verificar ambos namespaces: desarrollo por sí
  solo oculta errores de nombres SRG. No cachear resultados de formatters que
  pueden depender de estado externo. Resize debe mantener cursor y selección.
- `verifyDispatchPersistenceRegression` también es obligatoria en check/build:
  usa DispatchLibrary real y un config temporal de Forge para comprobar JSON,
  edición sin E/S, fallo de reemplazo, limpieza y recuperación. La suite Python
  de CEDA ejecuta los métodos de pantalla con reloj/almacén dobles, sin GUI.
- `verifyDispatchLoadingRegression` verifica archivos propios/CopyL con campos
  nulos o mal tipados, preservación de otras ranuras y teclas sin truncamiento
  ni desbordamiento; también se exige en check/build, sin cliente gráfico.
- La recuperación de JSON CEDA ilegible conserva el original en config como
  `gripeverde-dispatch.json.corrupt-*.bak` antes de reemplazarlo. Si el backup
  falla no se guarda encima. No borrar esas copias automáticamente ni incluir
  datos de usuario en Git; no es un sistema de restauración automática.
- `verifyRecentAddressesRegression` también se exige en check/build: prueba
  la biblioteca real de historial reciente en un CONFIGDIR temporal de Forge,
  sin cliente/red, con inmutabilidad, orden y recuperación tras fallo de disco.
- `verifyMemoryShutdownRegression` comprueba memoria/JSON/executor reales en
  un CONFIGDIR temporal; check/build la requieren. La suite Python de cierre
  vigila el monitor durante la espera con executor real y E/S simulada.
- `verifyMemorySchemaRegression` se exige en check/build: prueba la protección
  de esquemas futuros en principal/temporal/respaldo. Al detectarlos la memoria
  es de solo lectura para toda la sesión; no eliminar ni reescribir esas copias
  desde una versión vieja del mod. Los cambios de esa sesión no se persisten.
- `verifyMemoryDataRegression` se exige en check/build: nulls/IDs inválidos,
  limpieza antes de límites, contadores sin desbordamiento y versiones antiguas
  inválidas, con la biblioteca y archivos reales en CONFIGDIR temporal. Incluye
  `Marginalia.queue`/`queueOnce`: límite de 256 también en ejecución, rechazo
  sin consumir el marcador once, duplicados y persistencia del reintento.
  Las sesiones se prueban con días explícitos: visitas/racha saturadas, fechas
  ausentes/futuras, saludos y recordatorios persistentes, sin cliente gráfico.
- `verifySliderPointerRegression` también es obligatoria en check/build: usa
  DiarySlider y padre vanilla reales, sin cliente, para callback diferido desde
  la pulsación inicial, liberaciones ajenas/repetidas, reentrada y cancelación.
  La suite Python ejecuta la base compartida y métodos reales de los tres
  deslizadores con padres/audio dobles, incluyendo el handoff de foco vanilla.
  Ambas verifican también que el teclado no modifique mandos ocultos/inactivos/
  desenfocados aunque el padre retenga su modo de edición.
  La captura de liberación fuera del widget se integra en Sonido/Opciones/La
  casa: probar también con ContainerEventHandler real y no solo mouseReleased
  del widget. La suite Python ejecuta los métodos reales de esas pantallas.
  Se cubren también anchos 1/4/8/9/12 para evitar dividir por width-8
  en mandos de 8px o invertir el arrastre en mandos más estrechos.
  Las teclas en los extremos deben consumirse sin callbacks/guardados de más,
  salvo una edición de ratón pendiente que deba confirmarse una sola vez.
- **Nunca dos pushes seguidos**: el CI cancela el run anterior por concurrencia.
- Sin JDK local: la compilación semántica la hace el CI. No instalar Java 17
  en la máquina para publicar.
- Deploy del owner: la carpeta
  `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`, con JAR
  solamente y una sola versión del mod. El instalador verificado está en
  `tools/install-latest.ps1` y enlazado desde el README; no se ejecuta desde el
  sandbox en la PC del owner.
- Las rutas antiguas que sobreviven en documentación histórica no son
  instrucciones de trabajo.

## Reglas base

1. **Rama fija:** el trabajo de cada sesion vive en su rama `arena/*-gripeverde`.
   No cambiar de branch ni mergear ramas viejas sin verificar `git log` y
   `build.gradle version` (ver E-STEEL-REGRESSION).
2. **PROHIBIDO gameplay:** no agregar sistemas de enfermedad, cuarentena
   jugable, NPCs, items, bloques, recetas ni efectos que alteren la
   supervivencia. El mod es solo interfaz + audio + memoria narrativa.
3. **Sin resourcepacks:** nada de `gripeverde_steel`, `tools/steel_pack` ni
   texturas vanilla modificadas (R-NO-RESOURCEPACK). La identidad visual se
   hace con Java/render/widgets.
4. **No tocar logica vanilla:** solo presentacion (R-VANILLA-LOGIC). Los
   contenedores son 100% vanilla (0 PNG).
5. **Alcance coherente por version:** evitar mezclar cambios no relacionados.
   Una entrega transversal solo procede cuando fue pedida expresamente y
   cuenta con backup previo verificable, como v0.22.0.

## Antes de proponer un cambio

- Leer primero el addendum operativo al comienzo de `CONTEXTO.md`; consultar
  el archivo historico solo para reglas R-*, errores E-* y decisiones pasadas.
- Auditar el estado real: `git status`, `find src -name "*.java" | wc -l`,
  `cat build.gradle | grep version`.
- Si el entorno no dispone de JDK 17 (E125), limitarse a validaciones
  estaticas y dejar explicitamente pendiente la compilacion semantica.

## Validacion

- Usar **Java 17** para Forge 1.20.1.
- **4 escalas GUI** para cualquier cambio de UI (scale 1-4; reiniciar cliente
  al cambiar escala si hay duda).
- Validar JSON tras editar lang/sounds (`python3 -c "import json; json.load(...)"`).
- Build local limpio: `.\gradlew.bat clean build` ->
  `build\libs\gripeverde-<VERSION>.jar`.
- Deploy: limpiar primero los `gripeverde-*.jar` antiguos tanto en
  `build\libs` como en el directorio de SKLauncher, copiar el jar de la
  version exacta y confirmarla en el reloj del Title.
- Un parser externo puede comprobar sintaxis, pero no sustituye el build
  contra Forge/Minecraft ni la prueba en cliente.

## Verificaciones de build activas (processResources)

- `verifyGripeVerdeLocalization` — conjunto exacto de 25 locales, JSON
  valido, exactamente 191 claves no vacias con paridad frente a `en_us` y
  presencia de la version actual en las 25 cronicas in-game.
- `verifyGripeVerdeSoundAssets` — estructura e identificadores de eventos,
  listas no vacias, referencias existentes y cobertura OGG exacta, sin
  archivos huerfanos.
- `verifyGripeVerdeNoDeadCode` — falla si reaparecen simbolos de codigo
  desterrado fuera de comentarios (ThemeManager, steel pack, bg_town,
  IconButton, dev screens, `drawHeraldTicker` y otros retirados).
- `verifyGripeVerdeVersionSync` — `GripeVerdeMod.VERSION` debe coincidir con
  la version de `build.gradle`.
- `verifyGripeVerdeMusicDurations` — la duracion real Vorbis de cada OGG de
  musica debe estar dentro de 1.5 s de `TRACK_DURATIONS_MS`.
- `verifyGripeVerdeSubtitles` — toda clave de subtitulo no vacia (8 eventos
  ambientales) debe existir en los 25 idiomas.
- `verifyGripeVerdeSoundUsage` — ningún SoundEvent registrado sin uso; el
  allowlist legacy está vacío desde v0.26.0.

La CI activa está en `.github/workflows/build.yml`: verifica espejos,
lint Java, la compuerta de publicación y compila con JDK 17. El job que ejecuta
Gradle solo tiene `contents: read`; los comentarios de fallo se publican desde
un job separado con permisos de escritura. Los PR nunca publican;
`workflow_dispatch` exige activar explícitamente `publish_release`.
`tools/ci/build.yml` es solo una plantilla de build + artifact, alineada con
los checks del workflow activo, sin la etapa de publicación.

## Codigo

- UTF-8 sin BOM, LF, sin trailing whitespace (ver `.editorconfig`).
- Commits descriptivos: `feat:` / `fix:` / `docs:` / `chore:` + version.
- No dejar clases ni metodos sin uso; si algo ya no sirve, borrarlo y
  documentarlo en CONTEXTO.md y KNOWN_ISSUES.md.

## Cierre de hito (R-CIERRE)

Al bumpear version, sincronizar en la misma sesion: CONTEXTO.md,
CHANGELOG.md, KNOWN_ISSUES.md, README.md y los 25 changelog in-game (solo
si el hito tiene manifestacion observable, ver R-CHANGELOG-DISCRECIONAL).
Luego commit + push (los tags se publican cuando el usuario valida en
cliente real).
