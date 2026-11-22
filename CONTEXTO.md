# Eternal Craft: Gripe Verde - Contexto maestro

## Addendum operativo actual (2026-11-13)

- **Foco y overlays en atajos CEDA, posterior a `01bea15`:** el sondeo de
  atajos se suspende si la ventana no está activa o hay un overlay de carga.
  Se reinicia el estado de flancos, también sin cliente/ventana, para no
  interpretar una tecla/botón ya mantenido al regresar como pulsación nueva.
  Debe soltarse y pulsarse otra vez. No cambia teclas, options.txt ni el envío
  explícito desde la ficha. Nueva suite `tools/test_dispatch_hotkeys.py` con
  17 escenarios que ejecutan métodos Java reales extraídos usando dobles de
  cliente/GLFW, más 3 guardas estructurales. Se exige JDK 17 en ambos workflows.
  Validación local pasa; compilación/ejecución Java pendientes de CI. No es
  prueba del foco del SO, input nativo, eventos Minecraft ni red. Sin bump/release.

- **Atajos duplicados al cargar CEDA, posterior a `042bcf6`:** se aplican
  también al cargar/importar las invariantes del editor: una sola ranura por
  tecla o botón, y la tecla de apertura reservada. En archivos con duplicados
  conserva el atajo de la primera ranura y desasigna los posteriores, sin
  borrar mensajes ni nombres. El archivo propio no se reescribe solo por
  cargar; el siguiente guardado explícito usa los atajos normalizados. CopyL
  sigue siendo de solo lectura. La suite de carga añade archivos propios y
  CopyL con duplicados de teclado/ratón y apertura no predeterminada.
  El candidato `5187caf` compiló y pasó todas las suites en
  [CI 45048410532](https://github.com/Santi-PdR/GripeVerde/actions/runs/45048410532),
  incluido `verifyDispatchLoadingRegression` y las pruebas anteriores de
  persistencia, autoguardado, direcciones y geometría. Artefacto actual
  `GripeVerde-jar` ID `14764642088`. Sin JDK local ni prueba en Minecraft.
  Sin bump/release: asset v0.401.0 reconsultado tras publish con el mismo
  ID/tamaño/digest. Pendiente probar carga/importación y atajos en cliente.

- **Carga parcial de CEDA/CopyL, posterior a `c578e0e`:** valores nulos,
  objetos o arrays donde se esperaba un campo escalar, y ranuras que no son
  objetos, ya no abortan la lectura completa. Solo ese campo/ranura vuelve a
  su valor predeterminado; se conservan los mensajes válidos posteriores y
  un archivo propio legible no dispara reimportación de CopyL. Las teclas se
  convierten de forma exacta: fracciones y enteros fuera de rango no pueden
  convertirse por truncamiento/desbordamiento en un atajo válido. Se admiten
  enteros numéricos o en cadena y se mantiene la compatibilidad wheelKey.
  Nueva tarea `verifyDispatchLoadingRegression`, requerida por check/build,
  usa Gson/biblioteca/archivos reales y carpetas config aisladas, sin cliente.
  El candidato `042bcf6` compiló y pasó todas las suites en
  [CI 45048141199](https://github.com/Santi-PdR/GripeVerde/actions/runs/45048141199),
  artefacto `GripeVerde-jar` ID `14764696211`. Sin JDK local. El primer run
  `45047902634` detectó una constante equivocada solo en el test (Right Alt
  no es Right Shift); se corrigió usando la constante GLFW, no un número.
  Alcance: JSON sintácticamente legible. Un documento totalmente ilegible
  todavía sigue la recuperación histórica desde CopyL; no se afirma recuperar
  archivos truncados ni se añade un sistema de backups. Sin bump/release.

- **Cierre externo de CEDA, posterior a `5e6a5e5`:** `removed()` también
  vacía la cola de guardado al sustituir la pantalla sin pasar por onClose,
  por ejemplo al desconectarse. Respeta el backoff de un intento fallido
  reciente para no duplicarlo. La suite de métodos de pantalla se amplía
  a 14 escenarios (salida externa, cierre fallido seguido de removed y
  salida limpia incluidos). El candidato final `1510bd8` compiló y pasó
  [CI 45045888583](https://github.com/Santi-PdR/GripeVerde/actions/runs/45045888583):
  14 escenarios de pantalla, 3 guardas estructurales y 89 comprobaciones de
  persistencia/Unicode, más las suites anteriores. Artefacto `GripeVerde-jar`
  ID `14763760675`. Sin pruebas Minecraft. Asset v0.401.0 reconsultado tras
  publish, mismo ID/tamaño/digest; no hubo bump ni release nuevo.
  Los cambios que no logren escribirse solo permanecen en memoria: no
  sobreviven al cierre del proceso.

- **Recorte Unicode de CEDA, posterior a `b8004f2`:** los límites de
  256 unidades UTF-16 del mensaje y 24 del nombre podían separar un par
  sustituto después de limpiarlo, dejando una mitad inválida. El recorte
  respeta el límite existente pero retrocede una unidad si cortaría el par.
  Se amplía `verifyDispatchPersistenceRegression` con límites exactos,
  caracteres suplementarios, sustitutos aislados, valores nulos y round trip
  JSON. No cambia los límites ni el EditBox vanilla; no garantiza soporte
  visual de emojis por la fuente. El candidato `5e6a5e5` compiló y pasó
  [CI 45045605899](https://github.com/Santi-PdR/GripeVerde/actions/runs/45045605899),
  incluidas 89 comprobaciones de persistencia/normalización; artefacto
  `GripeVerde-jar` ID `14763387599`. Release v0.401.0 reconsultado intacto.

- **Guardado del Parte/CEDA, posterior a `551329f`:** la edición usa
  `updateMessage` sin E/S y agrupa las pulsaciones durante 700 ms. Se conserva
  la API inmediata `setMessage` y el guardado de atajos. Un fallo mantiene
  los cambios pendientes en la biblioteca (también tras cerrar la pantalla),
  muestra un error localizado y reintenta como máximo cada 3 s en tick;
  cerrar fuerza un intento. Los atajos ya no afirman éxito tras un fallo.
  JSON se escribe en un temporal vecino y luego se reemplaza, atómicamente
  si el sistema lo admite, sin truncar previamente el destino. No equivale
  a garantía de durabilidad ante corte eléctrico; el fallback no es atómico.
  Pruebas añadidas: métodos reales de pantalla con reloj/almacén dobles
  (11 escenarios, más 3 guardas estructurales) y persistencia JSON/archivos
  reales en un directorio aislado de Forge mediante tarea Gradle requerida
  por check/build. El candidato `b8004f2` compiló y pasó las suites en
  [CI 45045285372](https://github.com/Santi-PdR/GripeVerde/actions/runs/45045285372),
  artefacto `GripeVerde-jar` ID `14763590784`. Sin JDK local.
  Son 192 claves en los 25 locales. Sin bump/release ni prueba en Minecraft.
  Asset v0.401.0 reconsultado: ID/tamaño/digest sin cambios.

- **Geometría de DiaryTextField, posterior a `f173ab8`:** el renderer usaba
  un margen de 4 px pero EditBox tenía bordered=false, haciendo que el ancho
  editable y la traducción clic/cursor no coincidieran con el dibujo. Se
  conserva bordered=true como estado lógico (incluidos helpers cosméticos);
  el renderer propio sigue evitando el fondo/borde vanilla. No cambia los
  EditBox vanilla. Nueva tarea Gradle `verifyTextFieldGeometryRegression`,
  requerida por check/build, comprueba 12 casos de ancho lógico reales sin
  render ni eventos de mouse. Lint/espejos/guardas locales pasan. El candidato
  `ae796c7` compiló y pasó las suites, incluidos los 12 casos geométricos y
  los 38 de direcciones, en
  [CI 45042852283](https://github.com/Santi-PdR/GripeVerde/actions/runs/45042852283).
  Artefacto actual `GripeVerde-jar`, ID `14762691152`, incluye Idiomas,
  direcciones, scroll compartido y Paquetes. Prueba visual/en cliente pendiente.
  Asset v0.401.0 reconsultado tras publish: mismo ID/tamaño/digest; sin bump
  ni release nuevo. No atribuir el desfase a los clics fantasma generales.

- **Direcciones de servidor, posterior a `a1d58c7`:** validación y readout
  delegan en ServerAddress de Minecraft, admitiendo IPv6 entre corchetes y
  evitando confundir su último bloque numérico con un puerto. Se rechazan
  espacios/controles internos; la validación vanilla comprueba puertos.
  Editar servidor guarda la IP con trim, igual que Conexión directa y que la
  validación. Nueva tarea Gradle `verifyServerAddressRegression`, requerida
  por check/build, ejecuta 38 comprobaciones con el parser real sin red.
  Chequeos locales pasan; `f173ab8` compiló y ejecutó las comprobaciones
  Gradle en CI `45042533041` (sin JDK local). Sin bump/release ni prueba de
  conexión en cliente.

- **Idiomas, posterior a `d4109ba`:** al reconstruir la pantalla se conserva
  el idioma pendiente y el scroll actual (incluido cero), en lugar de volver
  al idioma aplicado/scroll de otra apertura. Primera apertura usa -1 como
  sentinel y conserva el centrado inicial. Una selección eliminada por packs
  vuelve al idioma aplicado. Se invalida la cabecera en init para recalcular
  su centro aunque el panel conserve su ancho máximo. LanguageList.setSelected
  sincroniza la edición pendiente para la navegación vanilla de teclado,
  excluyendo la fila de Niebla, null y cambios durante aplicación. No aplica
  al navegar: Listo confirma y Esc cancela como antes.
  Test nuevo: 3 verificaciones estructurales locales pasan; 11 escenarios de
  métodos Java reales extraídos pasaron en CI `45042270304` junto con la
  compilación del candidato `a1d58c7` (sin JDK local). También pasan guardas
  18/18, lint 72 Java, espejos 8/8, gate 7/7, instalador 7/7, py_compile y
  diff. Sin bump/release ni prueba en cliente.

- **Scroll compartido, posterior a `9f3f9a1`:** SmoothScroll ahora detecta
  desplazamientos externos (barra, teclado/ensureVisible) y cambio de lista,
  cancelando el destino inercial viejo. Conserva la acumulación de rueda y
  la rama sin easing mientras hay mundo abierto; no añade animaciones.
  `tools/test_smooth_scroll.py` compila el Java real con dobles mínimos de
  Minecraft/lista y ejecuta 15 escenarios. Localmente se omitieron por falta
  de JDK; CI los exige con `--require-java`. Pasan guardas estáticas 18/18,
  lint 72 Java, espejos 8/8, gate 7/7, instalador 7/7, py_compile y diff.
  El candidato `30d268f` compiló y ejecutó los 15 escenarios Java en
  [CI 45041710607](https://github.com/Santi-PdR/GripeVerde/actions/runs/45041710607).
  Artefacto `GripeVerde-jar`, ID `14762220269`. Prueba en Minecraft pendiente.
  Sin bump ni release; asset v0.401.0 reconsultado tras publish, mismo
  ID/tamaño/digest. No confirma resueltos los clics fantasma generales.

- **Recorte de Paquetes, posterior a `9dbd0ef`:** se resuelven geometría y
  entradas antes de adquirir el scissor. La banda opaca y las filas se dibujan
  solo después de activarlo, dentro de un try/finally que siempre solicita
  liberarlo; no se continúa pintando si la activación lanza una excepción.
  Corrige una ruta defensiva, no demuestra un fallo observado en Minecraft.
  Guardas estáticas 18/18, lint 72 Java, espejos 8/8, gate 7/7, instalador
  7/7, py_compile y diff pasan. El candidato `f152571` compiló y pasó las
  suites en [CI 45041210093](https://github.com/Santi-PdR/GripeVerde/actions/runs/45041210093).
  Artefacto `GripeVerde-jar`, ID `14761956917`. Prueba en cliente pendiente.
  No modifica entrada ni versión; asset v0.401.0 conserva ID/tamaño/digest
  tras publish. No hay release nuevo.

- **Continuación posterior a `64261bb`:** las descripciones normales de packs
  recuperan las dos líneas de vanilla, igual que los avisos de incompatibilidad.
  Se conserva el Component (incluidos saltos y estilos), sin convertirlo en
  una cadena truncada. Título en `rowTop + 1`, descripción en `rowTop + 12`
  con interlínea de 10 px: caben en los 32 px útiles de la fila vanilla.
  Caché por identidad de fila, máximo 256 entradas; invalida por texto/estilo
  o ancho y se limpia en init/removed. No cambia handlers ni confirmaciones.
  Guardas estáticas 16/16, lint 72 Java, espejos 8/8, gate 7/7 e instalador
  7/7 pasan. El candidato `e9e4896` compiló y pasó las suites en
  [CI 45040439067](https://github.com/Santi-PdR/GripeVerde/actions/runs/45040439067).
  Artefacto `GripeVerde-jar`, ID `14760938281`. Prueba en Minecraft pendiente.
  Sin bump ni release; asset v0.401.0 reconsultado tras publish, mismo ID,
  tamaño y digest. No confirma resueltos los clics fantasma.

- **Continuación posterior a `d1d4e83`:** se repone el aviso de packs
  incompatibles que la banda opaca borraba. Reproduce las condiciones de
  `PackEntry.render` 1.20.1: pack incompatible y no simultáneamente fijo y obligatorio,
  más hover real de fila, selección con foco o modo táctil. Título traducido y
  descripción de compatibilidad en hasta dos líneas, ancladas a la fila.
  No toca activación, reordenación ni confirmación vanilla. Guardas de listas
  14/14 (once mutaciones nuevas), lint 72 Java, espejos 8/8, gate 7/7 e
  instalador 7/7 pasan. CI `45039654130` detectó acceso a la clase protegida
  `AbstractSelectionList.Entry`; se sustituyó por su interfaz pública
  `GuiEventListener` y se añadió una guarda de regresión. El candidato
  `8708784` compiló y pasó las suites en
  [CI 45039859475](https://github.com/Santi-PdR/GripeVerde/actions/runs/45039859475).
  Falta prueba en cliente. Sin bump ni release: el asset `778229607` conserva
  ID, tamaño y digest tras el job publish. No acredita resueltos los clics
  fantasma. Artefacto del candidato: `GripeVerde-jar`, ID `14760749545`.

- **Candidato `4d37582`, posterior a `0aa6a25`:** el repintado de Paquetes
  ancla texto y separador a `rowTop/rowBottom`, no al borde recortado del
  viewport. Evita desplazar el texto y ocultar la descripción antes de tiempo
  en filas parcialmente visibles; no cambia entrada ni geometría de clics.
  Guardas de listas ampliadas: 12/12, con cinco mutaciones de recorte y el
  modelo aritmético reproducible de 4.536 casos. Suite conectada al workflow
  y su plantilla. Pasan también espejos 8/8, lint completo de 72 Java,
  gate 7/7, instalador 7/7 y py_compile. Compilación y suite de listas verdes
  en [CI 45034644163](https://github.com/Santi-PdR/GripeVerde/actions/runs/45034644163).
  Falta prueba en Minecraft. Sin bump ni release: tras el job publish se
  reconsultaron tag y asset de v0.401.0, que conservan commit, ID, tamaño y hash.
  Se reconcilió HEAD local con `0aa6a25` en la misma rama: antes se verificó
  que solo diferían los ocho archivos de esta continuación, sin cambios en
  staging ni archivos faltantes/adicionales. Un reset mixed actualizó solo
  rama/índice; se comprobó SHA-256 de los 222 archivos antes/después para
  acreditar que el workspace se conservó byte por byte.

- **Última publicación verificada: GripeVerde v0.401.0**, tag dirigido a
  `e0f845007681e92e747e04127169f483e159456b`; JAR de 10.855.632 bytes, SHA-256
  `dd37d443b7307c1b3d0d09fc5befdaee6d1dd887bfca002d6e7bc3670ef0ee88`. El CI
  `44997077455` pasó en `build` y `publish`; [release v0.401.0](https://github.com/Santi-PdR/GripeVerde/releases/tag/v0.401.0).
  Reconsultados el tag y el asset `778229607`: el tamaño y el digest SHA-256
  informado por GitHub coinciden con lo anterior. No es una nueva descarga ni
  inspección binaria: el host de descarga queda fuera de la red permitida.
- v0.401.0 hace que el fallback de Mundos calcule el índice de fila con el
  mismo orden de redondeo/truncado que vanilla cuando el scroll es fraccionario.
  Es una corrección de respaldo acotada, no confirma resueltos los clics
  fantasma generales ni cambia el aspecto de la pantalla.
- **Pendiente de revisión en Minecraft:** Paquetes de recursos, los clics
  fantasma generales y la interfaz de Un Jugador siguen abiertos. La revisión
  estática de Paquetes encontró que la repintada opaca escondía los rótulos y
  dejaba corta la zona de texto. La rama contiene un candidato que los restaura,
  cubre la franja vanilla completa, conserva icono/flechas fuera de ella y
  repinta filas visibles aunque haya más de 64 packs. No toca la entrada. Aún
  requiere prueba en cliente y no está publicado. Esta ronda no modifica Un Jugador.
  Se contrastaron las coordenadas con `TransferableSelectionList` y
  `AbstractSelectionList` 1.20.1; los accesores de límites los agrega Forge.
  Validación local: espejos 8/8, lint de 72 Java sin avisos, gate 7/7,
  instalador 7/7, guardas de listas 9/9, py_compile y diff sin errores.
  Las guardas son pruebas estáticas; no acreditan ejecución en cliente.
  El commit `1532c19` pasó la compilación en CI
  [44999893206](https://github.com/Santi-PdR/GripeVerde/actions/runs/44999893206).
  Se reconsultó el asset después del job `publish`: digest, tamaño e ID sin
  cambios. No hubo release nuevo. El modelo aritmético del viewport pasó
  4.536 casos; no ejecuta Java ni reemplaza la prueba en Minecraft.
- Las reglas de siempre siguen en pie: mod **solo de cliente**, sin bloques,
  sin ítems, sin gameplay ni resourcepacks; la identidad visual se hace con
  Java y los render/widgets propios.
- Validar y publicar: `verify_mirrors.py` -> `lint_java.py` ->
  `tools/ci/test_release_gate.py` -> `tools/ci/test_installer.py` -> commit +
  push -> esperar el CI. Los PR nunca publican; en un push, el marcador debe
  coincidir con `build.gradle`. **Nunca dos pushes seguidos** (el CI cancela
  por concurrencia). No hay JDK en este entorno; la compilación semántica queda
  a cargo del CI.
- Baseline validada: 72 archivos Java, 25 locales x 191 claves, 35 eventos de
  sonido, 42 refs/42 OGG, 4 piezas de música y 8/8 sellos espejo.
- Destino de instalación del owner:
  `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`, con JAR
  solamente y una sola versión del mod. El instalador verificado es
  `tools/install-latest.ps1`; no se ejecuta en la PC del owner desde el sandbox.
- La tabla de hojas, medidas, chapas `REG.` y paleta vive en
  `docs/CONTEXTO_MODMENU.md`; el estado operativo, en `KNOWN_ISSUES.md`; la
  guía para tocar el mod, en `CONTRIBUTING.md`.
- **Lo que el CI no acredita**: cómo se ve la pantalla o cómo responden sus
  clics. Compilación, recursos, localización y espejos sí; revisión visual e
  interactiva en Minecraft, no.

## Addendum operativo anterior (2026-10-27) - historico

- Versiones en curso: GripeVerde v0.149.0 y CEDA Dispatch (repo Lclient) v3.4.0; los cambios remotos se trabajan por GitHub.
- El botón opcional de CEDA Dispatch solo aparece cuando el mod ID `copyl` está cargado; no agrega dependencia obligatoria.
- El código fuente de este trabajo se modifica remotamente por GitHub; no usar ni actualizar un checkout local del repositorio para esta tarea. El ZIP/JAR de CI se maneja temporalmente solo para instalar el artefacto y luego se limpia.
- Destino de instalación indicado por el owner: `D:\\Minecraft\\SKLauncher\\sklauncher-data-v4\\instances\\test-1\\mods`. La carpeta queda con JAR solamente y una sola versión de GripeVerde y CEDA Dispatch; conservar los demás mods.
- Idiomas principales: inglés y español, con sus variantes regionales disponibles en Minecraft 1.20.1 (incluido `es_uy`). Cada variante debe tener sus archivos de idioma y crónica para no caer al inglés.
- Restricción funcional vigente: no añadir animaciones ni efectos de render al mundo activo ni al inventario. Las animaciones de menú respetan las opciones de movimiento reducido y destellos reducidos.
- Ambos modmenus comparten papel y tinta sepia; el ID `copyl`, el formato JSON y las rutas de configuración se conservan. La CI no sustituye una revisión visual en Minecraft cuando el owner pueda hacerla.
- Las decisiones y rutas bajo este addendum son históricas; si contradicen las instrucciones actuales del owner, prevalecen las instrucciones actuales.



### DECISION 2026-08-23: v0.23.0 - El gramófono guarda la palabra

Pasada transversal autorizada por el owner a partir de la revision tecnica
completa del mod (hallazgos documentados en `REVISION_MOD.md` junto con
evidencia de verificaciones: duraciones OGG reales parseadas, ausencia de
MusicManager en el codigo, doble registro de eventos). Antes de tocar
archivos se creo el bundle externo
`/home/user/GripeVerde-backup-pre-v0.23.0-4ca038d.bundle` (contiene --all).

- `GripeVerdeMod`: eliminado el `EVENT_BUS.register(new ScreenEventHandler())`
  manual. La doble entrega quedaba enmascarada por cooldowns y el guard
  `redirecting`, pero el registro corria en ambos lados y crashearia un
  servidor dedicado al cargar `net.minecraftforge.client.event.*`. El mod
  queda inerte fuera del cliente; `mods.toml` suma
  `displayTest="IGNORE_ALL_VERSION"` e `issueTrackerURL`.
- `MenuMusic`: guardia por tick sobre `MusicManager` (`isPlayingMusic` +
  `stopPlaying` de la musica situacional) que silencia la banda sonora
  vanilla de menu bajo el gramofono; watchdog `instanceGoneDead` recupera la
  playlist si el motor mata la instancia (F3+T/recargas), caso que antes
  dejaba la musica muda para siempre; temporizadores migrados a `nanoTime`.
- `LivingAtmosphere` + `DiaryAccessibilityOptionsScreen` + `ClientConfig`:
  nueva ayuda "Destellos reducidos" (fotosensibilidad, WCAG 2.3.1) separada
  de Movimiento reducido; congela el temporizador del relampago sin tocar
  niebla, lluvia ni faroles.
- `sounds.json` + 25 lang: subtitulos reales para los ocho eventos
  ambientales (pasos, madera, puerta, susurro, campana, cuervos, cadenas,
  trueno). `DiaryAmbienceSlider` corrige su etiqueta hardcodeada usando la
  clave traducida existente. Paridad lang: 93 claves.
- `ChroniclerMemory`: escritura en executor monohilo daemon ("ultima copia
  gana" conservado, `writeNow` synchronized); `flushForShutdown` hace
  shutdown + awaitTermination y solo entonces escribe en linea lo pendiente.
- `ScreenEventHandler`: reemplazo solo si la clase es exactamente
  TitleScreen/PauseScreen vanilla; config `integration.replaceTitleScreen` /
  `replacePauseScreen`; Ctrl+0 pasa a `debug.debugShortcut` (por defecto
  off).
- Retirados: mixins.json vacio (+ plugin, annotationProcessor, refmap y
  entrada en mods.toml), namespace GameTest sin tests, metodos no-op de
  UiSounds, propiedad sponge de settings.gradle.
- Build: sellos nuevos `verifyGripeVerdeVersionSync` y
  `verifyGripeVerdeMusicDurations` (parser Vorbis granule/rate, tolerancia
  1.5 s); conteo lang 83 -> 93; CI lista para activar en
  `tools/ci/build.yml` (JDK 17 Temurin, Gradle cacheado, jar como artifact;
  mover a `.github/workflows/`: la sesion carece del permiso `workflows`),
  con lo que la compilacion semantica pendiente en el sandbox (KI-52) tiene
  via automatica; `tools/deploy.ps1` lee la version de build.gradle.
- Conteos: 60 clases Java, 25 lang (93 claves), 25 cronicas, 35 eventos,
  42 OGG, 42 referencias. Bump sincronizado: build.gradle +
  GripeVerdeMod.VERSION -> 0.23.0.
- Compilacion semantica Gradle/Forge no ejecutada en el sandbox (sin Java
  17): queda delegada a la CI y al PC del owner (KI-53).
- Changelog in-game actualizado en los 25 idiomas (manifestacion observable:
### DECISION 2026-08-23 (segunda vuelta): v0.23.1 - Sellos y relojería

El owner autorizo implementar el lote recomendado del informe y pidio la
activacion de la CI resuelta de forma automatica (quedo como script, ver
mas abajo); las mejoras CON superficie visible/efectos quedan reservadas
hasta nueva autorizacion (ver REVISION_MOD.md, seccion de propuestas).

- Build: sellos nuevos `verifyGripeVerdeSubtitles` (subtitle != "" debe
  existir en los 25 lang) y `verifyGripeVerdeSoundUsage` (SoundEvent sin
  uso -> fallo; 10 legacy en allowlist grandfathered documentada en
  ModSounds).
- ChroniclerMemory: SCHEMA_VERSION=1 + migrate() (pasos N→N+1, esquema
  futuro se conserva con warn) + sanitize de bounds (clamps, nulls, caps
  256 notas / 4096 guestbook).
- MenuAmbience -> nanoTime con LivingAtmosphere como unico consumidor de
  lastBellMs migrado al mismo reloj (hearse/cuervos/campana estables).
- CI: ambas vias automaticas bloqueadas (git push: workflows permission
  denied; gh api: 403 Resource not accessible by integration). Solucion:
  `tools/ci/activar-ci.ps1` ejecutable por el owner en su PC; activa la CI
  con sus credenciales en un paso.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.23.1; docs
  y 25 cronicas in-game al dia (parche: sellos, memoria y relojeria).

### DECISION 2026-08-23 (tercera vuelta): v0.23.2 - El telegrama llega intacto

Hotfix parche por crash en campo, reportado el mismo dia con crash-log
completo de una instancia real (Java 17, Forge 47.4.10, mod v0.22.0):
`ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0` en
`DiaryTitleScreen.drawEasterEggMessage` durante el render del Title.

- Causa raiz: el efecto maquina de escribir del telegrama (easter egg del
  sello del titulo) produce una cadena vacia durante los primeros ~22 ms;
  `font.split("")` devuelve lista vacia y el codigo indexaba `lines.get(0)`
  tras forzar `lineCount >= 1`. El jugador clickeaba el logo y el cliente
  moria en cada fotograma posterior (4 crashes consecutivos en el reporte).
- Correccion: `lineCount` queda acotado por `lines.size()` (0 permitido; el
  sobre conserva su altura minima con `Math.max(1, lineCount)` solo para el
  boxH) y la llamada al overlay decorativo en `render()` queda envuelta en
  try/catch (misma filosofia que el resto de capas ornamentales).
- Auditoria lateral: los otros 5 usos de `font.split` (ChangelogScreen,
  DiaryArchiveScreen) iteran seguros; el unico otro acceso indexado
  (`MenuAmbience.weightedRandom`) ya tenia guardia `isEmpty()`.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.23.2; docs y
  25 cronicas in-game al dia.
- KI-55 registra la validacion pendiente en cliente; el jar fijado debe
  llegar al jugador que reporto (sigue en v0.22.0).

### DECISION 2026-08-23 (cuarta vuelta): v0.24.0 - El viento vuelve a soplar

Pedido del owner (mismo dia, junto al segundo crash-log de Agus): "haz las
implementaciones que dijiste, piensa en corregir este bug, implementa mejoras
visibles, algunos easter eggs mas, corrige que no se escucha el ambiente."
Bug ya corregido en v0.23.2 (tercera vuelta); aqui va el resto, autorizado.

- Causa raiz del ambiente mudo (diagnostico con soundfile en sandbox): los
  loops base estaban grabados casi en silencio. `wind_urban_loop.ogg` media
  RMS 0.0006 (-64 dB), lluvia RMS 0.026 (-32 dB), ciudad RMS 0.034 (-29 dB),
  susurro 0.0056 y primer paso 0.0077; con las ganancias del codigo
  (0.35/0.30/0.20) el viento salia a ~-73 dB: fisicamente inaudible. El motor,
  las instancias, el slider y los eventos estaban sanos (la musica, audios UI
  y raros siempre se oyeron). NO era un bug de codigo: era contenido mudo.
- Correccion de activos: `tools/audio/regenerate_menu_ambience.py` (nuevo,
  idempotente) sintetiza viento (ruido 1/f con rafagas, remolinos y silbido)
  y lluvia (siseo + masa grave + ~1400 gotas) como loops perfectos con
  crossfade de extremos, y refuerza ciudad x3, susurro x3 y paso 1 x3.
  Objetivos: RMS 0.10-0.13, picos <= 0.90 (margen por sobresalto Vorbis).
- Mejoras visibles autorizadas (V1+V3+V5 de REVISION_MOD):
  * V1 Cronica: al abrir el Changelog tras una actualizacion salta a la
    entrada de la version instalada y la sello de lacre "NUEVO/NEW" la marca
    durante esa visita; `ChroniclerMemory.lastSeenVersion` (campo nuevo,
    tolerante con memorias viejas: GSON lo deja null y se sanea).
  * V3 Noche de tormenta: ~14% de las sesiones arrancan bajo tormenta
    cerrada (sorteo una vez por JVM en `MenuAmbience.isStormNight()`): lluvia visual x2 con mas inclinacion y
    brillo, relampagos cada 3.5-9.5 s, mezcla ambiente con lluvia x1.8 /
    viento x1.25 / ciudad x0.8, capa rara cada 14-26 s con trueno casi
    garantizado, velo oscuro en titulo y pergaminos; descubrimiento
    `storm_night` + marginalia. Respeta reduceFlashes (sin relampagos) y
    reducedMotion (sin cortina extra de movimiento).
  * V5 Gotas sobre el pergamino: 7 gotas (14 en tormenta) resbalan por
    todos los pergaminos (`PaperTexture.draw/drawClean`); con movimiento
    reducido quedan quietas como condensacion.
- Easter eggs nuevos (descubrimientos 48-50 de 50):
  * `seal_breaker`: 9 toques rapidos sobre el sello del titulo -> el lacre
    salta en esquirlas con gravedad; estrena `ui.paper_fall` (sale del
    allowlist del sello de uso de sonidos).
  * `sound_keeper`: diapason diminuto al margen del regulador de ambiente en
    Opciones de sonido; al pulsarlo suena `ui.success` (segundo evento que
    sale del allowlist) y el gabinete queda "afinado".
  * Textos de fichas al 100% (`N_MANY_CARDS`, `N_ARCHIVE_COMPLETE`) ahora
    computan la cifra desde `Discoveries.ALL.length` (quedaron dinamicos).
- Higiene lateral: el ambiente del gabinete se detiene al entrar a un mundo
  (antes los loops seguian sonando bajo el gameplay; `start()` ademas se
  niega a arrancar con mundo cargado).
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.24.0; docs y
  25 cronicas in-game al dia. Espejo Python: 7/7 sellos + balance OK.
- KI-56 registra la validacion pendiente en cliente real.

### DECISION 2026-08-23 (quinta vuelta): v0.24.1 - La errata del cronista

El owner ejecuto `tools\deploy.ps1` y `compileJava` fallo:
`PaperTexture.java:95` usaba `0xD20PL` como semilla de las gotas y la P no
es digito hexadecimal (`malformed floating-point literal`). La 0.24.0 jamas
existio como jar: el sandbox no tiene JDK y el espejo Python solo verifica
balance de llaves, asi que la errata salio sellada (fallo de proceso,
registrado para que no se repita).

- Correccion: semilla `0xCA1DAL` ("la caida"); comportamiento identico,
  sin ningun otro cambio funcional respecto a 0.24.0.
- Refuerzo de proceso: el espejo Python suma el sello 8
  (`verifyJavaLiterals`), lint de todos los literales numericos de los 60
  .java (hex/binario/octal/decimal/flotante, sufijos, octales con 8/9)
  ignorando comentarios y cadenas. Detectado el caso real: `0xD20PL`
  queda rechazado; con el sello activo, esta familia de erratas no vuelve
  a salir.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.24.1; docs y
  25 cronicas in-game al dia. Espejo Python: 8/8 sellos + balance OK.
- KI-57 registra la validacion pendiente; KI-56 queda pendiente INTEGRO (la
  0.24.0 nunca se pudo probar). Para Agus: enviar 0.24.1.

### DECISION 2026-08-23 (sexta vuelta): v0.25.0 - El que mira la tormenta

Pedido del owner: "algunos easter eggs mas". Tres secretos nuevos en la
vigilia, todos cableados sobre mecanica existente, sin resourcepacks y sin
gameplay (reglas madre intactas):

- `storm_watcher`: `LivingAtmosphere.flashCount()` (nuevo; se incrementa en
  el destello programado y en `triggerLightningFlash`) + linea base por
  apertura de la vigilia en `DiaryTitleScreen.tick`; al tercer relampago
  nuevo, ficha y nota `N_FLASH3`. Con destellos reducidos los relampagos no
  nacen y la ficha no se otorga sola (accesibilidad).
- `drop_catcher`: `LivingAtmosphere.rainDropAt` (hit-test sobre las 40
  gotas, tolerancia generosa) + `scareDrop` (la gota vuelve al cielo) +
  plink de aguja (`menu/tick` 1.9) + guarda `estimateFirstButtonTop` para
  no tragar clics de botones; nota `N_DROP`.
- `code_doctor`: rama DOCTOR en el telegrafo oculto (`HiddenCipher`):
  susurro + campana baja, contador `doctor_signed`, nota `N_DOCTOR`.
- Registry: 3 ids nuevos en `Discoveries.ALL` (archivo: 52 fichas) con
  nombre y pista bilingues inline (cero claves lang nuevas; 93 intactas) y
  3 notas nuevas en `Marginalia` (N_FLASH3, N_DROP, N_DOCTOR).
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.25.0; docs y
  25 cronicas in-game al dia. Espejo Python: 8/8 sellos + balance OK.
- KI-58 registra la validacion pendiente en cliente real.

### DECISION 2026-08-23 (septima vuelta): v0.25.1 - El cilindro original

Feedback del owner tras escuchar la 0.25.0 en cliente: "El sonido de la
lluvia que creaste esta bugeado creo, deberias usar los sonidos que ya
existian." Leccion registrada: no re-sintetizar contenido que el owner
reconoce; restaurar el master y amplificar.

- Lluvia: se retira la sintesis de 0.24.0 y vuelve el master original
  (commit 447c290) con ganancia pura, normalizado a pico ~0.83 conservando
  la cresta (~19); sin limitadores (descartado un soft-knee con NaN y
  picos >1.0, rehecho desde el master). Mezcla `BaseLoop` lluvia 0.30 ->
  0.45.
- Viento: master original amplificado a RMS ~0.085 (pico 0.80). Costuras
  de loop medidas: limpias (0.0076 / 0.0000), sin crossfade; 30 s ambos.
- `tools/audio/regenerate_menu_ambience.py` archivado con nota en cabecera
  (ya no es la fuente de esos loops).
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.25.1; docs y
  25 cronicas in-game al dia. Espejo Python: 8/8 sellos + balance OK.
- KI-59 registra la validacion pendiente en cliente real.

### DECISION 2026-08-23 (octava vuelta): v0.25.2 - El telegrama no pisa la mesa

Reporte del owner probando GUI scale 3-4: el cartel del easter egg (el
telegrama "CASO" que muestra textos como "Como conoces ese nombre?...")
caia SOBRE la mesa de botones — los dos clamps de Y se anulaban (el segundo
re-clavaba en `safeTop`) — y el sello FRAUDE del cartel del elixir se
montaba sobre el texto en carteles bajos.

- `DiaryTitleScreen.drawEasterEggMessage`: clamps reordenados (jamas pisar
  botones, jamas salir por arriba; sin banda libre, sube sobre el logo) +
  `boxW` con techo en el ancho real de pantalla.
- `PosterRenderer` (FRAUDE): estampado siempre DESPUES del texto
  (`cursorY + 6`), tope duro dentro del papel y clamp horizontal.
- De paso: los textos del telegrama salen de un pool bilingue fijo en
  `DiaryTitleScreen` (eggPool) y las fichas/pistas viven en el registry
  `Discoveries` + notas en `Marginalia`; el owner pregunto "de donde los
  saca" — respondido en chat y registrado aqui para la memoria.
- Docs/tooling: deploy vigente = comandos PowerShell INLINE (sin .ps1
  nuevos) + regla permanente: cada entrega termina con el bloque PW inline.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.25.2; docs y
  25 cronicas in-game al dia. Espejo Python: 8/8 sellos + balance OK.
- KI-60 registra la validacion pendiente en cliente real.

### DECISION 2026-08-24 (decimocuarta vuelta): REPOSO — plan de la proxima iteracion

El owner puso el proyecto EN REPOSO: "dejare en reposo ahora mismo...
documenta todo para seguir mas tarde". NO hubo cambios de codigo: la
v0.30.0 (3c598a6) queda como version vigente y candidata para Agus. Esto
solo registra el plan de la proxima iteracion, con su contexto tecnico.

**P1 — Pluma de la cronica: coherencia visual y sonora (correccion).**
Feedback textual: "se ve extraña cuando ya animo una parte y sigue la
otra; el sonido es constante y no para — deberia parar cuando NO estas
viendo la parte que se anima". Diseno acordado para implementar:
- El rasguño (UI_SCROLL quedo) solo suena si la fila que se esta
  escribiendo es VISIBLE en pantalla en ese frame (check de bounds en el
  loop de ChangelogScreen.render; hoy suena por tiempo transcurrido sin
  mirar la vista).
- La pluma deja de correr por tiempo en zonas fuera de vista: el
  presupuesto se consume SOLO por filas visibles (la regla de piso de
  0.30.0 ya cubre el pasado lejano). Efecto: nunca mas "una parte
  escrita y otra siguiendo" — la tinta siempre trabaja delante del
  lector.
- Mantener: gracia de 300 ms tras el pliegue, click que completa todo,
  reducedMotion = hoja entera.

**P2 — CUADERNO DOBLE FAZ (rediseno grande de la cronica).** Propuesta
del owner, queda AUTORIZADA como diseno para la proxima iteracion:
- La cronica pasa de pergamino con scroll a CUADERNO de dos hojas
  abiertas (izquierda/derecha), 1 version por hoja; botones "hoja
  siguiente"/"hoja anterior" (+ flechas de teclado y rueda = pasar de
  hoja) y animacion de LA HOJA DOBLANDOSE (fake 2D por render puro:
  trapezoide/degradado que se cierra con sombra; sin texturas).
- La pluma, si se conserva, escribe solo la hoja en vista al llegar.
- Conservar del sistema actual: la lupa (ahora salta a la HOJA que
  contiene la palabra: indice version→hoja), el folio (N/M = hojas), el
  lacre multi-edicion, el sello NUEVO, y los huevos: el folio escondido
  pasa a ser "hoja sin numerar" tras la ultima hoja; los asteriscos del
  encuadernador se reubican en el margen de la espina (centro del
  cuaderno) para que sigan encontrables.
- Reglas de siempre: reducedMotion = paso de hoja instantaneo o fundido
  sin giro; reduceFlashes = sin brillo en el doblado; GUI 2/3/4 con
  minimo ancho comprobado (cada hoja queda mas angosta: validar wrap a
  ~170 px y, si no entra en escala 4, modo hoja simple como fallback);
  minimal mode sin adornos extra.
- Esfuerzo estimado ALTO: reescritura de ChangelogScreen (paginacion por
  version, wrap por hoja, animacion de giro). NO empezar sin reconfirmar
  con el owner al retomar (el dijo "seria bueno" entre alternativas).

**P3 — Volumen de botones, segunda pasada.** Feedback: "sigue siendo
alto" tras el -40% de hovers de 0.30.0. Plan: VOL_CLICK_MAIN 0.22→0.16,
VOL_CLICK_SECTION 0.18→0.13, VOL_BACK 0.20→0.15 (y derivados: toggle,
cleanClick, tab heredan las familias). Reevaluar tambien el cooldown de
hover si al owner le sigue pareciendo "machacon".

**Estado operativo al retomar:** repo en paz en 0.30.0; validar KI-53 a
KI-65 sigue pendiente (owner no confirmo en cliente); orden sugerido de
la proxima sesion: P3 (5 min) → P1 (correccion corta) → P2 solo con
visto bueno explicito. Backups vigentes: bundles pre-v0.26.0…pre-v0.30.0
fuera del repo (raiz del workspace).

### DECISION 2026-08-24 (decimotercera vuelta): v0.30.0 - La hoja entera

Feedback del owner tras la 0.29.0: "muy bien quedo [la vuelta]. En el
changelog queda raro que solo una parte se escriba — corregilo (que todo
se escriba o que se esconda y al bajar se anime). En el buscador la
letra no se ve. Baja el volumen de los tick de los botones. Y corregi
que subtitulos/informacion bajo los subtitulos quedan solapados por
botones. Tiene que estar bien en todas las interfaces."
Backup previo: `GripeVerde-backup-pre-v0.30.0-6ac186f.bundle`.

- Pluma de bitacora entera: preparePen abarca desde la H1 vigente hasta
  el fin del documento (340 c/s tras gracia de 300 ms) + REGLA DE PISO
  en el loop: fila mas de media pantalla por encima de la vista = ya
  impresa, no gasta presupuesto → bajar de golpe no deja huecos y la
  escritura sigue al ritmo del lector. Click completa; reducedMotion,
  hoja entera.
- Lupa legible: fondo PAPER claro (era tinta sobre tinta — errata
  reconocida), texto INK, caret bronce. 
- Hovers -40% (VOL_HOVER_MAIN 0.028 / SECTION 0.022).
- Solapes corregidos: (a) dayMsg del titulo va a la IZQUIERDA de la
  firma en la misma linea (antes flotaba sobre la mesa de botones) y la
  firma clampa x>=48 (fuera de la franja del gramofo); (b) linea "fix"
  del huevo Holloway con tope duro sobre gridTop-11 y divisor debajo;
  (c) flavor de la sala de sonido se omite si no hay aire sobre los
  sliders; (d) elipsis al ancho del papel en flavor/fix largos.
- Bump sincronizado 0.30.0; 25 cronicas con vocabulario de documento
  real; docs en regla. Espejo 8/8 + balance OK (61 Java; 35/35 sonidos).

### DECISION 2026-08-24 (duodecima vuelta): v0.29.0 - La vuelta y la lupa

Feedback del owner tras jugar la 0.28.0: "me gusta lo que hiciste con la
cronica, no me gusto la animacion de salir de cada cosa — podrias
hacerla mejor y optimizarla; y cuando haces click en cualquier lado hace
un sonido, eso no me gusta; aprovecha y anade cosas que creas que
faltan". Backup previo:
`/home/user/GripeVerde-backup-pre-v0.29.0-5dee740.bundle`.

- Vuelta cortes: el regreso a mesa ya visitada pasa de barrido completo
  invertido (260 ms con salpicaduras y gotas) a gesto breve (170 ms):
  velo tenue (110) + banda superior (46 px) que se repliega con filo
  bronce. Avanzar conserva TODA la ceremonia (gusto). Sombra de asiento
  sigue en ambos (solo opacidad).
- Optimizacion del velo: cache de elegibilidad por clase (sin cadenas
  por frame), config resuelta una vez por apertura (`reducedNow`),
  duracion (`durNowMs`) y fin de efecto (`fxEndMs`) precomputados →
  fuera de la ventana del efecto, costo cero por frame.
- silenceKnock retirado (pedido expreso: nada de sonido al tocar en
  cualquier lado). UI_ERROR sigue cableado en error(): 35/35 en uso,
  allowlist vacia.
- Extra anadido: la LUPA de la bitacora (busqueda por palabra en la
  cronica): cuadrito de 96 px al pie a la derecha (abrir con `/`, Ctrl+F
  o toque), texto simple sin comodines, Enter/Shift+Enter recorre las
  apariciones (piquito bronce + fondo tenue + contador), Esc la cierra y
  olvida. Folio se corre a la izquierda en escala 4 — jamas se pisan.
  Caret quieto con destellos reducidos.
- Sandbox: HEAD volvio a amanecer en 4ca038d mid-edicion; rescate con
  `git reset --mixed FETCH_HEAD` (disco intacto, mi DiaryTransitions
  reescrito sobrevivio). Regla aprendida: verificar `git log` al
  retomar.
- Interfaces: banda proporcional en GUI 2/3/4; lupa bajo el contenido
  en todas las escalas; reducedMotion = fundido limpio (KI-64).
- Bump sincronizado 0.29.0, 25 cronicas con vocabulario de documento
  real, docs en regla. Espejo Python 8/8 + balance OK.

### DECISION 2026-08-24 (undecima vuelta): v0.28.0 - Las gotas y los folios

Pedido del owner (consigna repetida): changelog al dia sin terminos de
taller, mejoras significativas en textos y efectos entre interfaces, bien
en TODAS las interfaces, backup previo.

- Backup externo previo:
  `/home/user/GripeVerde-backup-pre-v0.28.0-d40c820.bundle` (--all,
  verificado).
- Gotas del filo (DiaryTransitions): 3 desprendimientos por pliegue
  (30/55/80% del barrido), resbalon de 26 px max en el sentido del
  pliegue con rastro 1 px; vida 550 ms; alfas ≤ 85/40; semilla LCG propia
  por apertura. reducedMotion: sin gotas.
- Folio de la cronica: "folio N · M" centrado al pie del panel, calculado
  de scrollY/contentH; ornamentos bronce; fuera del area de contenido
  (no pisa texto, Cerrar ni el folio escondido del huevo).
- Lacre multi-edicion (ChangelogScreen): freshRows = cabeceras cuyo
  triple de version > lastSeenVersion (parser regex tolerante,
  comparacion numerica); sello para todas, pluma solo en la vigente.
- Subtitulos del titulo: pools 9 → 12 por idioma/contexto (hospedajes y
  arsenales), voz de documento intacta.
- Interfaces: medido contra GUI 2/3/4, minimal, reducedMotion y
  reduceFlashes (KI-63). Sin gameplay, render Java puro.
- Bump sincronizado 0.28.0 (build.gradle + GripeVerdeMod.VERSION), 25
  cronicas al dia con vocabulario de documento real, docs en regla.
  Espejo Python 8/8 + balance OK (61 Java; 35/35 sonidos en uso).

### DECISION 2026-08-24 (decima vuelta): v0.27.0 - El pliegue y la pluma

Pedido del owner (misma consigna que la vuelta anterior): changelog al dia
sin terminos de taller, mejoras significativas en textos y en los efectos
al cambiar de pantalla/interfaz, bien en TODAS las interfaces y backup
antes de tocar nada.

- Incidente de sesion resuelto al empezar: la copia local amanecio con
  HEAD en 4ca038d (disco intacto, indice desfasado); `git fetch` +
  `git reset --hard FETCH_HEAD` dejo el arbol = aa0ff79 (0.26.0) sin
  perdida (probado: diff vs FETCH_HEAD solo listaba los files nuevos).
- Backup externo previo (regla consolidada):
  `/home/user/GripeVerde-backup-pre-v0.27.0-aa0ff79.bundle` (--all,
  verificado).
- Pliegue direccional: pila NAV acotada (16) en `DiaryTransitions`,
  alimentada por `noteTransition` en los tres puntos de salida de
  `onScreenOpening` (captura `getCurrentScreen()` al inicio; la pantalla
  nueva se lee TRAS el redirect). Avanzar baja, regresar sube; la pausa
  dentro de mundo y los returns por Esc quedan cubiertos por la pila.
- Salpicaduras (5 gotas, LCG por apertura, alfas ≤ 95, cola 500 ms; fuera
  con reducedMotion) y sombra de asiento de la hoja (450 ms, alfa ≤ 20,
  solo opacidad: vale con reducedMotion).
- V4 (REVISION_MOD) implementada: la entrada de la version recien
  instalada se escribe a pluma en su primera visita (170 chars/s min,
  techo 6,5 s), plumín anclado al wrap real del font (capital incluida),
  rasguño con UI_SCROLL a 0.045f por tramos de 42 chars (throttle 120 ms),
  click la completa sin tragar el gesto, lastSeenVersion garantiza que se
  escribe una sola vez; reducedMotion → hoja entera; REDUCE_FLASHES →
  tinta del plumín fija.
- Recordatorio del owner reafirmado: el mod vive en interfaces — todo se
  midio contra GUI 2/3/4, minimal mode, reducedMotion y reduceFlashes
  (KI-62). Sin gameplay, sin resourcepacks.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.27.0; 25
  cronicas in-game con entrada nueva en vocabulario de documento real;
  CHANGELOG/KNOWN_ISSUES/README/REVISION_MOD al dia. Espejo Python 8/8
  sellos + balance OK (61 Java; 35/35 sonidos en uso).

### DECISION 2026-08-24 (novena vuelta): v0.26.0 - Las hojas y los faroles

Pedido del owner: "actualiza el changelog — sin terminos que no sean de un
documento de la vida real —, haz nuevas mejoras significativas en textos y
efectos visuales como al cambiar de pantalla/interfaz; tiene que estar bien
en TODAS las interfaces; y haz un backup antes."

- Backup externo previo (bundle --all, verificado):
  `/home/user/GripeVerde-backup-pre-v0.26.0-18e178d.bundle`. Queda como
  regla: backup bundle ANTES de cada entrega de cambios estructurales.
- Transicion entre interfaces (`DiaryTransitions`, nueva clase, enganchada
  en `ScreenEvent.Render.Post`): cortina de tinta que revela la hoja nueva
  en ~260 ms (velo sepia + franja con filo bronce). Filtro por paquete
  `client.screens`: solo pantallas del gabinete; vanilla y ajenas intactas.
  Movimiento reducido: fundido de 130 ms sin barrido. Sin gameplay, sin
  resourcepacks, render Java puro (reglas madre intactas).
- Faroles reactivos al cursor (V6 de REVISION_MOD): `setMouse` desde el
  titulo + `lanternHover[]` eased en tick (antes del early-return de
  reducedMotion: en accesibilidad solo respira el brillo). Radio 30 px,
  halo x1.5, llama +25%, topes 255 (withAlpha no clampea).
- V2 de REVISION_MOD cerrada: los 8 sonidos legacy cableados a sus
  eventos en `UiSounds` (open/close/modal/tab/error/cleanClick/
  clickCreate/respawn, mas `silenceKnock` nuevo para el escritorio en
  silencio y `SlimButton.withClickSound` para el gramofo). `respawn`
  suena al volver al titulo via `onArriveTitle` (la primera apertura del
  arranque no suena; el redirect vanilla tambien cuenta la llegada).
  Allowlist del sello de uso VACIA en build.gradle y en
  tools/verify_mirrors.py: 35/35 eventos en uso.
- Cronicas in-game reescritas (0.24.1/0.25.0/0.25.1 x25): vocabulario de
  documento real; fuera hexadecimal/crossfade/literal/clic. Regla nueva de
  estilo: la cronica in-game jamas usa terminos de taller ni de software;
  los documentos tecnicos (CHANGELOG.md, KNOWN_ISSUES) si pueden.
- Recordatorio del owner registrado: este MOD VIVE EN INTERFACES — cada
  cambio se verifica en GUI scale 2/3/4, minimal mode y movimiento
  reducido.
- Bump sincronizado: build.gradle + GripeVerdeMod.VERSION -> 0.26.0; docs y
  25 cronicas in-game al dia. Espejo Python: 8/8 sellos + balance OK (61
  Java ahora). KI-61 registra la validacion pendiente en cliente real.

### SIGUIENTE PASO OPERATIVO VIGENTE

PROYECTO EN REPOSO (orden del owner del 2026-08-24, ver decimocuarta
vuelta). Al retomar: (1) validar KI-53 a KI-65 en cliente real con el
jar 0.30.0 (incluye reenviarlo al jugador que reporto el crash y mirar
especialmente pluma/lupa/vuelta cortes), (2) ejecutar el plan anotado:
P3 clicks mas bajos → P1 pluma coherente (rasguño solo si la fila que se
escribe esta a la vista; tinta solo delante del lector) → P2 cuaderno
doble faz SOLO con visto bueno fresco del owner. No implementar planes
de gameplay ni los items de "propuestas futuras"/"propuestas visibles"
de REVISION_MOD.md sin autorizacion expresa. Tag/release: deliberadamente
ausentes hasta validacion real (ultima estable validada; pendientes de
v0.23.x y v0.22.0).

## Addendum operativo archivado (v0.22.0)

- Rama fija de Arena: `arena/01a02cee-gripeverde`.
- Base respaldada: v0.21.31, commit
  `7605243f9aff55ccbe0d00ad743c38414c66061f`.
- Implementacion final: v0.22.0, commit
  `a1decf304b0c7236d624d4637bd3b5dcaa3961db`.
- Revision del owner: PR 14,
  <https://github.com/Santi-PdR/GripeVerde/pull/14>.
- Tag/release `v0.22.0`: deliberadamente ausente hasta cerrar KI-52.
- Pedido vigente: entrega final documentada y coherente, preservando el
  historial pero separandolo de las instrucciones actuales.
- Restricciones funcionales absolutas: escena completa, faroles permanentes,
  sin kiosco/Heraldo, sin modo automatico por FPS, sin gameplay y sin
  resourcepacks.
- Repositorio local vigente del owner:
  `C:\Users\santi\Desktop\GripeVerde`.
- Deploy vigente del owner:
  `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.

Toda ruta distinta de esas dos rutas Windows dentro del historial de este
archivo es una referencia archivada, no una instruccion operativa. En
particular, no usar `.deploy`, `D:\...` ni
`C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde`.

### DECISION 2026-08-23: v0.22.0 - La vigilia queda sellada

Pasada final autorizada antes de entregar el mod al owner, sin gameplay,
resourcepacks ni recortes de la escena. Antes de tocar archivos se creó y
verificó el bundle externo
`/home/user/GripeVerde-backup-v0.21.31-7605243f9aff-2026-08-23.bundle`
(SHA-256 `361002ab5a443585a9297fe0e7161fbb3de265ab6ae47d4e38f117580853e74a`).

- `QuarantineSceneRenderer` mueve ahora el polvo desde el tick fijo de 20 Hz;
  su velocidad deja de depender de los FPS y `renderAtmosphericDust` solo dibuja.
- Música y ambiente tienen una sola ruta de tick en todas las pantallas;
  desaparecen las llamadas redundantes por cuadro del Title y de las
  subpantallas que mantenían solo el audio.
- `REDUCED_MOTION` aparece como toggle propio en Accesibilidad, persiste en
  el TOML y congela cielo, humo, lluvia, partículas, grano, destellos y
  movimiento atmosférico sin retirar calle, faroles ni utilería permanente;
  también suprime sacudida, flashes, partículas y revelado progresivo del
  easter egg del logo. El grano estático se recalcula si cambia la ventana.
- `DiaryToggle` acepta opciones externas y su mensaje de narración incluye
  etiqueta y estado actual.
- `ChroniclerMemory` escribe un temporal con `force`, preserva la última
  memoria válida, reemplaza atómicamente cuando el sistema lo admite,
  recupera un temporal interrumpido o el backup y conserva el JSON corrupto
  para diagnóstico.
- `PaperTexture` y las ventanas de `VictorianSkyline` reutilizan RNGs
  deterministas; se elimina la asignación de objetos por cuadro sin cambiar
  la distribución visual.
- El build valida JSON, paridad y valores de las 83 claves en 25 idiomas,
  presencia de v0.22.0 en 25 changelogs, listas sonoras no vacías, entradas
  bien formadas, referencias OGG completas y ausencia de OGG huérfanos.
- Conteos: 60 clases Java, 25 lang (83 claves), 25 changelogs, 35 eventos y
  42 OGG.
- Bump sincronizado: `build.gradle` + `GripeVerdeMod.VERSION` -> 0.22.0.
- Validacion estatica final: 60 Java analizados por `java-parser`, 25 locales
  x 83 claves, 25 changelogs, 35 eventos, 42 referencias y 42 OGG.
- La compilacion semantica de Gradle/Forge no se ejecuto en el sandbox por
  falta de Java 17; el analisis de sintaxis externo no la sustituye.
- La implementacion se publico en la rama fija y la PR 14 se actualizo para
  revision del owner. No se creo tag ni release.
- Validacion pendiente en cliente real (KI-52): build con Java 17, recorrido
  de pantallas, toggle de movimiento reducido y prueba controlada de
  recuperacion de memoria.

### SIGUIENTE PASO OPERATIVO (instantanea historica de la v0.22.0 — la
### secuencia vigente de verdad es la primera de este documento)

No implementar los planes de gameplay que aparecen en bloques historicos de
este documento. La unica secuencia vigente es cerrar KI-52 en un cliente
real, corregir solo si esa prueba encuentra un defecto y, despues, autorizar
el tag/release v0.22.0. Las cronicas in-game ya estan sincronizadas en los 25
idiomas; este cierre documental no requiere una entrada visible adicional.

### DECISION 2026-08-23: v0.21.31 - La vigilia permanece entera

La auditoria confirmo que `QuarantineSceneRenderer.isLowEnd()` muestreaba
los FPS una vez por segundo y, por debajo de 30, eliminaba la niebla y toda
la utileria de `renderStreetLevel`, incluidos los faroles. Ese modo se borro
por completo: ya no quedan campos, muestreo ni bifurcaciones por FPS.

- El Title ejecuta siempre `renderFogLow` y `renderStreetLevel`; la niebla,
  los faroles, sus reflejos, las polillas, las particulas y el resto de la
  calle no cambian por una bajada de FPS.
- El grano de pelicula deja de consultar FPS y usa siempre su intervalo de
  90 ms.
- Se retiraron los restos del antiguo apagado interactivo, el ciclo erratico
  de atenuacion y la lectura de `avenue_darkened`: ni un evento ni una
  memoria vieja pueden apagar o debilitar una llama, omitir su reflejo ni
  dispersar sus polillas.
- El objeto que parecia un canasto era el kiosco de madera del Heraldo. Se
  eliminaron su renderer, hitbox, interaccion, overlay, periodico diario,
  clase `Herald.java` y ficha de descubrimiento.
- `ChroniclerMemory.discoveredCount()` cuenta solo los ids activos, por lo
  que memorias anteriores que tengan `herald` no producen un contador
  superior al total actual de 46 fichas.
- Conteos: 60 clases Java, 25 lang, 35 eventos y 42 OGG.
- Changelog in-game actualizado en los 25 idiomas por tratarse de cambios
  visibles en la escena.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.31.
- Validacion pendiente en cliente real (KI-51): Title a FPS altos/bajos y
  cuatro escalas GUI; confirmar escena completa, faroles estables y ausencia
  total del kiosco/Heraldo.

### DECISION 2026-08-23: v0.21.30 - La calle permanece

El modo low-end introducido en v0.21.5 pretendia retirar la niebla y los
faroles cuando el cliente bajaba de 30 FPS, pero condicionaba la llamada
completa a `LivingAtmosphere.renderStreetLevel`. Como esa llamada tambien
dibuja la base mediante `renderStreet`, la calzada desaparecia junto con
los efectos costosos.

- `QuarantineSceneRenderer.renderTitleScene`: en low-end llama ahora a
  `renderStreet`; fuera de low-end conserva `renderStreetLevel` completo.
- `renderDiaryBackground`: `renderStreet` se ejecuta siempre; la calle base
  es barata y no depende del muestreador de FPS.
- Se conservan sin cambios la niebla, los faroles, sus reflejos y las
  particulas en el camino normal.
- Changelog in-game actualizado en los 25 idiomas porque el arreglo tiene
  manifestacion visible en la ciudad.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.30.
- Validacion pendiente en cliente real (KI-50): Title a ambos lados del
  umbral de 30 FPS y cuatro escalas GUI.

### DECISION 2026-08-22: v0.21.24 - Ajustes pedidos en cliente

El usuario pidio (en cliente real) una tanda de ajustes. Sin gameplay, sin
botones nuevos, sin cambios de identidad de pantalla:

- **Sonido del gabinete**: eliminado el `UiSounds.close()` (madera/puerta)
  que sonaba al abrir Options desde ScreenEventHandler.
- **Cajon del gabinete ELIMINADO**: el rectangulo marron con la llave
  amarilla y la vitrina de objetos (drawer) fue borrado por completo
  (dibujo, click, campos y el hueco del panel). Las fichas DRAWER_OPEN/
  DRAWER_COMPLETE y el objeto ITEM_KEY quedan inalcanzables (lore muerto,
  no rompe nada).
- **Easter egg de los golpes (knock) ELIMINADO** en Options: tres golpes
  sobre la madera ya no responden (KNOCK_BACK inalcanzable).
- **Easter egg del poster de CUARENTENA ELIMINADO**: el poster ya no se
  despega ni muestra la esquina doblada con el nombre de Holloway
  (POSTER_PEEL inalcanzable). El poster queda estatico.
- **Fuentes rojas con sombra**: el titulo "CUARENTENA" del poster pasa de
  rojo (BLOOD_DRIED) con sombra a tinta oscura (INK) sin sombra; el sello
  de tinta (APROBADO/REVOCADO...) pierde su sombra roja duplicada.
- **Libro de visitas de Multiplayer**: ahora mas visible (fondo oscuro +
  texto claro + subrayado ambar) y con hitbox mayor (y-6..y+30) para que el
  clic registre bien.
- **Creditos/Atribuciones**: etiquetas en titulo normal (Studio, Chronicler,
  Director, City blueprints, Paper & ink) en vez de MAYUSCULAS forzadas;
  contenido de Cronista/Director/Planos reescrito a descripciones genericas
  (pendiente: el usuario indicara los nombres/info finales).
- Pendiente de confirmacion del usuario: textos definitivos de las
  atribuciones (Cronista, Director, Planos de la ciudad) y nuevas mejoras
  (preguntar antes de meter interfaces/botones nuevos).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.24.
- Validacion pendiente en cliente real (KI-49).

### DECISION 2026-08-22: v0.21.23 - Reconstruccion desde el backup pre-interfaces

El usuario pidio deshacer de verdad la version que altero las interfaces
("busques un registro o backup de ese mod y de ahi edites y saques esas
version y adjuntes las demas").

**Registro/backup encontrado:** commit `39e4720` (estado v0.17.2, el
inmediatamente anterior a `715c52f` = v0.19.0 donde nacieron
`ExpedientHeader`, `drawInsetCard` y el sistema de carteles/EXPEDIENTE).
Las versiones que alteraron las interfaces y fueron excluidas: v0.19.0,
v0.20.0 y v0.21.0.

**Reconstruccion ejecutada (desde el backup + resto actual):**
- Restauradas desde `39e4720` las 17 pantallas Diary (Options, Sound,
  Controls, Language, Chat, Accessibility, Online, Multiplayer, Archive,
  Changelog, Credits, Pause, Title, DirectJoin, EditServer, Confirm, Skin)
  a su forma pre-alteracion: cabeceras propias de cada sala (titulo +
  subtitulo + divisor), sin carteles ni rails de v0.19+.
- Restaurados los widgets que las pantallas del backup usan:
  `DiaryMusicSlider`, `DiaryAmbienceSlider`, `DiaryPackSelectionScreen`.
- `DiaryVideoSettingsScreen` (nacio en la alteracion) ELIMINADA; Options
  vuelve a abrir VideoSettingsScreen/Embeddium vanilla como antes.
- `DiaryChrome`: anadidos los 3 metodos `drawRegistryTag*` del backup +
  constantes STEEL_NEAR/DEEP/EDGE (las chapas de registro del backup).
- Lang: clave `gripeverde.options.packs.subtitle` restaurada en los 25
  idiomas (la usaba DiaryPackSelectionScreen).
- Re-aplicados sobre las pantallas restauradas los ganchos de lore de las
  versiones posteriores aprobadas: triggers del Title (ARCHIVE_THINNING
  >=50, N_OPEN_COUNT >=100), cadena de fichas de la limpieza en Archive
  (BG_TOWN_MEMORY, BUTTONS_NEVER_PRESSED) y sello SIGILLUM + campana al
  completar las 47 fichas.
- Re-eliminados los elementos que el usuario pidio sacar y que el backup
  reintroducia: saludo del Title (drawGreeting), pie de registro del
  Changelog (drawLedgerDetails/scrollToSection), filas de grupo y
  telemetria de Options (drawRowBands/openTelemetry).
- Se conservan intactos: audio (con hotfix v0.21.1), lore (Discoveries 47
  fichas, Marginalia, Herald, HiddenCipher CLEANSE/ARCHIVE,
  ChroniclerMemory con save diferido), render (RNG seed fija,
  REDUCED_MOTION, low-end), config, theme, util y build (3 verificaciones).
- Lista negra del build: quitados DiaryMusicSlider/DiaryAmbienceSlider
  (restaurados), anadido DiaryVideoSettingsScreen.
- Conteos: 61 clases Java, 25 lang (81 claves), 35 eventos, 42 OGG.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL
  (reversion estructural, sin evento de la ciudad).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.23.
- Validacion pendiente en cliente real (KI-48): pantallas con la
  identidad v0.17.2 + lore v0.21.x; compilar y recorrer todas las salas.

### DECISION 2026-08-22: v0.21.22 - Hotfix compilacion (firma rota)

La limpieza v0.21.21 dejo una firma de metodo rota en `DiaryOptionsScreen`
(`private void  {` — el limpiador de llamadas no-op se comio el nombre del
metodo `drawOptionsGroup`). Detectado por el usuario en build local
(error: identifier expected). Fix: eliminado el metodo vacio roto entero
(no tenia llamadas). Sin otros cambios. Bump 0.21.21 -> 0.21.22.

### DECISION 2026-08-22: v0.21.21 - Se termina la deuda de los no-op

El usuario pidio "mas". Se cerro la deuda KI-42 (call sites no-op de la
reversion):

- Eliminadas 30 llamadas no-op a `DiaryChrome.drawInsetCard` /
  `drawInnerRail` / `drawValueRow` en 11 pantallas (Accessibility, Archive,
  Controls, Credits, DirectJoin, EditServer, Language, Options, Skin,
  Sound, Video) y las 3 llamadas a `drawOptionsGroup` (Options).
- Eliminados los 3 metodos no-op de `DiaryChrome` + constantes `CARD_BASE`
  / `CARD_BORDER` (quedaron con 0 usos).
- Eliminadas las variables locales y bloques muertos que alimentaban esos
  carteles: Language (frameX/Y/W/H), Controls (leftX/railX/workW),
  Options (contentLeft/contentRight/contentW), Sound (railX/railY/railY2/
  listFrameX/listFrameW), Skin (headerBottom sin asignar), Accessibility
  (headerBottom sin asignar), constantes `RAIL_W` (Controls) y `STATION_W`
  (Online).
- Changelog: eliminado el footer de registro (`drawRevisionFooter`:
  "consultation register / folio N", resto v0.21.0).
- FIX de scrollbars: las llamadas `drawStyledScrollThumbInPanel` de Chat,
  Online y Sound referenciaban variables eliminadas (rackX/stationX/railX)
  -> ahora usan `this.panelX + this.panelW - 26` (borde derecho del panel).
- Higiene: 14 imports no usados eliminados; newline-final en archivos que
  no lo tenian; trailing whitespace global limpio.
- Resultado: 34 archivos, -209 lineas netas. Cero variables locales sin
  uso, cero campos muertos, balance OK, dead-code OK.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.21.
- Validacion pendiente en cliente real (KI-47): Chat/Online/Sound con
  scrollbar en el borde derecho del panel y listas intactas.

### DECISION 2026-08-22: v0.21.20 - La tinta pesa menos (limpieza post-reversion)

El usuario pidio "mejora todo lo que veas". Se hizo una pasada de limpieza
profunda sobre el codigo que quedo tras la reversion de interfaz:

- `ExpedientHeader`: nueva sobrecarga simple `draw(gfx, font, x, y, w,
  title, reveal)`. Las 16 pantallas migradas a la llamada simple;
  eliminados ~150 lineas de argumentos muertos ("EXPEDIENT...",
  "EC-QV/1897 · ...", subtitulos, flavors y los estados REGISTERABLE /
  INCOMPLETO / LISTO / READY / LIVE / PENDING / APPLYING / INDEXED /
  CALIBRADO...). La firma larga delega en la simple (compat).
- Metodos muertos eliminados: `computeControlDiscipline` (Controls),
  `normalizeFov` (Video), `computeVisibleRatio` + variable `visibleRatio`
  (Skin), `getVisualMixLevel` / `getCrossfadeVisual` (MenuMusic).
- Variables locales muertas eliminadas: Options (reference/subtitle/status
  del header, rail completo `drawOptionsRail` + campos railX/railY/railW),
  Online (en/stationX/stationY + bloque board no-op), Chat (rackX/rackY +
  bloque rail no-op), Language/Multiplayer/Options/Chat (headerBottom sin
  asignacion), DirectJoin/EditServer (ready), Pause (bloque de la barra
  stillness + highlight + haloC; se restauro el cierre de metodo que la
  limpieza habia comido), Credits (lineH/line), PosterRenderer (angle),
  VictorianSkyline (h3).
- Options gridTop 77 -> 66 (acercar el grid al header simple).
- Resultado: 23 archivos, -252 lineas netas; cero variables locales sin
  uso; balance de llaves/parentesis OK; trailing whitespace limpio.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.20.
- Validacion pendiente en cliente real (KI-46): revisar que todas las
  salas siguen mostrando su titulo + contenido y que nada se rompio por la
  limpieza (especialmente Pause, Options, Chat y Online).

### DECISION 2026-08-22: v0.21.19 - Se corrigen scrolls y posiciones tras la reversion

El usuario pidio corregir "los scrolls, las posiciones de botones y todo lo
demas" que quedaron desplazados por la reversion de interfaz (v0.21.16-18):
la cabecera paso de ~78px a ~52px (titulo simple) y los carteles dejaron de
dibujarse, dejando listas y botones anclados a medidas viejas (solapes o
huecos de 32-40px).

**Correcciones:**
- `DiaryMultiplayerScreen`: LIST_TOP_OFFSET 84 -> 52 (la lista de
  servidores arranca justo debajo de la cabecera, sin hueco de 32px).
- `DiarySoundOptionsScreen`: listTop 45 -> 58 (la lista de opciones
  solapaba el titulo de la cabecera).
- `DiaryChatOptionsScreen`: listTop 88 -> 62 (quitado hueco de 36px).
- `DiaryOnlineOptionsScreen`: listTop 92 -> 62 (quitado hueco de 40px).
- `DiaryControlsScreen`: gridTop 92 -> 62 (los botones del manual quedaban
  a 40px de la cabecera).
- `DiaryArchiveScreen`: el campo de busqueda pasaba por ENCIMA de la grilla
  de fichas (estaba en panelY+168, dentro del area de 47 fichas); ahora
  vive en la esquina superior derecha (panelY+18) y el contador "N / 47"
  justo debajo (panelY+38).

**Verificado sin cambios:** Video (66), Language (68), Accessibility
(HEADER_H=60) y Options (gridTop=77) ya quedaban debajo de la cabecera;
Changelog y Pause recalculan el contenido relativo a la cabecera en tiempo
de render; los botones de volver estan anclados al fondo del panel.

- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.19.
- Validacion pendiente en cliente real (KI-45): listas/scrolls alineados en
  Multiplayer, Sound, Chat, Online, Controls y Archive.

### DECISION 2026-08-22: v0.21.18 - Se restaura lo que no era de la version revertida

El usuario pidio revisar la reversion (v0.21.14-v0.21.17): "revisa que no
hayas sacado cosas de otras versiones o de versiones nuevas que no queria
sacar; yo queria sacar lo que pusieron nuevo en aquella version" (v0.18.0-
v0.21.0). Se hizo pickaxe de git con la historia completa (163 commits) y
se restauro lo que NO era de esas versiones:

**RESTAURADO:**
- `DiaryProgressBar.draw` -> cuerpo original vivo (v0.12.2, widget base).
  Con el cuerpo vivo, se eliminaron las llamadas de medidores que SI eran
  de v0.18-v0.21.0 para que no dibujen: Options (Gramophone/Room tone
  v0.19, catalogue v0.20), Sound (Music/Ambience/Transition v0.19),
  Controls (Discipline v0.19), Video (FOV v0.19), Accessibility (Contrast
  v0.19), Pause (stillness v0.19), Skin (presence v0.20).
- Medidores de versiones MÍAS que vuelven a dibujar: Video "Pacing"
  (v0.21.4) y Language "registries" (v0.21.4).
- Sello SIGILLUM + `drawCompletionSeal` (v0.21.13, archivo completo).
- `drawOfficialFrame` de Credits (v0.14.1, anterior a la version
  revertida).

**CONFIRMADO QUE SI ERA DE v0.18-v0.21.0 (queda fuera):** header completo
(expediente/referencia/subtitulo/flavor v0.18), tarjetas/railes/valueRows
(v0.18-v0.20), RECOVERED PAPERS (v0.21.0), footer "consultation register"
(v0.21.0), Chat/Online rack (v0.21.0), VANILLA CLASSES (v0.19), EXPOSED
PARTS (v0.20), drawOptionsGroup (v0.19), drawStatusPill (v0.19),
HOSPEDAJE ELEGIDO (v0.19). Y lo pedido explicitamente aunque fuera de otra
version: saludo del Title (v0.16) y ticker del Heraldo (v0.21.9).

- Lista negra del build: se quito SIGILLUM/drawCompletionSeal (vuelven a
  existir); sigue drawLedgerDetails.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.18.
- Validacion pendiente en cliente real (KI-44): el sello al completar 47
  fichas, el medidor de Pacing en Video, el de registros en Lenguas, y que
  el resto siga sin carteles.

### DECISION 2026-08-21: v0.21.17 - Se retiran los medidores y el resto de carteles

Continuacion de la reversion v0.21.16 (el usuario reporto "hay cosas que
no sacaste"). Se retiraron los elementos restantes de la identidad de
v0.18.0-v0.21.0:

- `DiaryProgressBar.draw` -> no-op: fuera los medidores con etiqueta
  (Gramofono, Ambiente, catalogo, Disciplina, Campo visual, Ritmo,
  registros, etc.) en Options, Sound, Video, Controls, Language, Skin,
  Accessibility y Pause. Firma intacta (los call sites compilan sin
  dibujar; deuda de limpieza KI-42).
- Archive: fuera el titulo "RECOVERED PAPERS / PAPELES RECUPERADOS" y su
  divisor (los documentos v0.16 se conservan), y fuera el sello SIGILLUM
  de archivo completo (v0.21.13, cartel) + metodo `drawCompletionSeal`.
- Changelog: fuera el footer de registro ("consultation register / folio",
  sello de cuarentena, referencia ARCHIVO EC-QV/1897) via
  `drawLedgerDetails` (v0.21.0). Se conserva el folio escondido v0.16
  (entrada a la sala).
- Options: `drawOptionsGroup` -> no-op (los carteles de grupo de v0.18).
- Credits: `drawOfficialFrame` -> no-op (marco oficial de v0.21.0).
- Verificacion: grep global de textos directos hardcodeados en pantallas =
  0 (solo quedan labels funcionales de inputs, hints de busqueda y
  mensajes de estado del juego).
- Lista negra del build ampliada (drawCompletionSeal, drawLedgerDetails,
  drawOptionsGroup, drawOfficialFrame, SIGILLUM).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.17.
- Validacion pendiente en cliente real (KI-41/43).

### DECISION 2026-08-21: v0.21.16 - Se retiran los carteles de interfaz

Reversion masiva de la identidad visual agregada por v0.18.0/v0.19.0/
v0.20.0/v0.21.0 (pases de interfaces y cohesion), pedida por el usuario
("saca lo que metio y cambio interfaces de esas versiones, carteles y esas
cosas, menos arreglos"). BACKUP previo: tag `backup-pre-reversion-v0.21.15`
en GitHub + `backup_gripeverde_antes_reversion.tar.gz` local.

**Metodo:** en vez de reescribir 16 pantallas (riesgo alto), se apago la
capa de carteles en los helpers y se eliminaron los textos directos:

- `ExpedientHeader.draw` -> solo dibuja el TITULO + divisor. Fuera:
  expediente ("EXPEDIENTE XIV"), referencia ("EC-QV/1897 · ..."),
  subtitulo, flavor y estado. Firma intacta (los ~16 call sites siguen
  pasando los parametros, que se ignoran).
- `DiaryChrome.drawInsetCard`, `drawInnerRail`, `drawValueRow` -> no-op
  (firma intacta; las llamadas restantes compilan y no dibujan; quedan
  como deuda tecnica para limpiar en una pasada futura).
- Textos de seccion directos eliminados: Chat ("CASILLERO", "REGISTRO
  PUBLICO", nota inferior + metodo `drawCorrespondenceRack` entero y
  `drawBottomNotice`), Online ("TABLERO DE COMUNICACION", "ESTACION DE
  RELE" + metodo `drawSignalStation` entero + `computePublicSignal`/
  `signalDisplay`), Archive (tarjeta "ARCHIVE STATE" + barra "catalogue";
  la grilla ahora usa el ancho completo), Sound ("CLASES VANILLA"),
  Controls ("PUNTOS DE ENTRADA", "POSTURA Y CONDUCTA AUXILIAR"),
  Language ("CONSULADOS"), Skin ("PARTES EXPUESTAS", "MANO PRINCIPAL" +
  `drawSpecimenFooter`).

**Conservado (funcional o anterior a esas versiones):** panel pergamino,
botones, sliders, textfield, toggle, lista de opciones vanilla, busqueda de
la Sala, fichas del archivo, documentos recuperados (v0.16), buzón/cajón/
espejo (lore v0.16), libro de visitas, toasts, marginalia, fondo del Title.

**Notas:**
- Las progress bars (medidores) se conservan (no son carteles).
- Las llamadas no-op a drawInsetCard/drawInnerRail/drawValueRow quedan en
  las pantallas (compilan, no dibujan); limpieza total en pasada futura.
- Lista negra del build ampliada (drawCorrespondenceRack, drawSignalStation,
  drawBottomNotice, drawSpecimenFooter, computePublicSignal).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.16.
- Validacion pendiente en cliente real (KI-41): revisar las 16 pantallas
  (titulo simple, sin carteles, sin solapes; botones/listas intactos).

### DECISION 2026-08-21: v0.21.15 - Se retiran los sellos de estado

Continuacion del pase de reversion de interfaz pedido por el usuario
("revisa cuando fueron esos cambios y saca los que cambien interfaces de
menu"). El pickaxe de git confirmo que los elementos que molestaban
(REGISTRABLE, INCOMPLETO, LISTO, PENDIENTE, READY, LIVE, etc.) vienen de
v0.18.0 (pase mayor de interfaces) y v0.21.0 (cierre de interfaces
secundarias); el unico agregado reciente era el ticker del Heraldo (v0.21.9,
ya revertido en v0.21.14).

**Cambios:**
- `ExpedientHeader`: eliminado el dibujo del pill de estado (status) en
  TODAS las pantallas Diary (antes: REGISTRABLE, INCOMPLETO, LISTO,
  PENDIENTE, READY, LIVE, INDEXADO, CALIBRADO, COMPLETO, CATALOGADO,
  PREPARADO, ATENUANDO...). El parametro status se conserva en la firma
  para no tocar ~20 llamadas. El flavor ya no reserva espacio para el pill
  y usa todo el ancho del panel (truncado defensivo con ellipsis).
- `DiaryOptionsScreen`: eliminado el pill "FIELD READY / LISTO PARA CAMPO"
  del carril lateral (se mantiene la fila "GUI scale").
- `DiaryChrome.drawStatusPill`: eliminado (quedo sin usos). Lista negra del
  build ampliada con `drawStatusPill`.
- El mensaje de borrar servidor ("va a desaparecer") es vanilla
  (`selectServer.deleteWarning`); el rojo que se veia era el pill PENDIENTE
  (quitado en v0.21.14) y el cartel ORDER (quitado). El mensaje ahora se
  dibuja en tinta normal.
- El libro de visitas de Multiplayer: verificado, el click y la firma estan
  intactos; lo que lo tapaba era la tarjeta HOSPEDAJE ELEGIDO (quitada en
  v0.21.14).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL
  (reversion de interfaz, no evento de la ciudad).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.15.
- Validacion pendiente en cliente real (KI-39/40).

### DECISION 2026-08-21: v0.21.14 - El gabinete vuelve a respirar

Reversion de adornos de interfaz que el usuario pidio quitar (los vio en
cliente real). Sin gameplay, sin assets.

**Eliminado del Title:** el saludo de sesion (`greetingText`, se dibujaba
centrado en y=148 y solapaba los botones MP/MODS). Se quitaron la llamada,
el metodo `drawGreeting`, los campos y las asignaciones en init (las
marginalias N_FIRST_NIGHT/N_DUST y los descubrimientos DUST/ANNIVERSARY se
conservan intactos).

**Multiplayer:**
- `LIST_TOP_OFFSET` 50 -> 84: la lista de servidores arrancaba en y=90,
  por ENCIMA del final de la cabecera (y~118) — el server pinneado tapaba
  titulo/subtitulo/divisora. Ahora arranca debajo.
- Eliminada la tarjeta "HOSPEDAJE ELEGIDO / SELECTED LODGING"
  (`drawSelectionLedger`) que se superponia a la zona del libro de visitas.
- Eliminado el ticker del Heraldo en Multiplayer (adicion v0.21.9 que el
  usuario no pidio y podia interferir con el libro de visitas).

**Confirm (borrar servidor):** eliminado el cartel "ORDEN / ORDER"
(drawInsetCard que solapaba el mensaje de confirmacion), eliminada la linea
"mano autorizante / authorising hand" con su raya de firma, quitado el pill
"PENDIENTE" del header, y el mensaje de confirmacion pasa a tinta normal
(`DiaryTheme.INK`) en vez del rojo/negrita que se veia.

**ExpedientHeader (fix general anti-solape):** el flavor debajo del
subtitulo ahora se trunca con ellipsis al espacio libre antes del pill de
estado. Arregla el solape de "REGISTRABLE" (editar servidor), "INCOMPLETO"
(agregar servidor), "LISTO" (conexion directa) y cualquier otra pantalla
con flavor largo + pill (incluido Options "READY").

**Build:** lista negra de `verifyGripeVerdeNoDeadCode` ampliada con
`drawGreeting`, `drawSelectionLedger`, `drawHeraldTicker`.

**Nota:** el usuario reporto "el libro de visitas dejo de funcionar" y
"errores en el gabinete" sin poder verse las capturas en el sandbox
(adjuntos no llegan). Se limpiaron las superposiciones que podian taparlo;
si persiste, requiere captura o descripcion puntual.

Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL
(reversion de interfaz, no evento de la ciudad).
Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.14.
Validacion pendiente en cliente real (KI-39).

### DECISION 2026-08-21: v0.21.13 - El expediente se cierra

Hito final de cierre narrativo (lore puro, sin gameplay):
- Al completar las 47 fichas, la Sala de consulta muestra el archivo como
  COMPLETO: el footer cambia de "las fichas que faltan siguen ahi fuera" a
  "el archivo esta completo", y aparece un sello de cera dorado (SIGILLUM,
  dibujado proceduralmente con fills, radio 8) junto al texto.
- Disparo unico (persistente): `ChroniclerMemory.counter("cabinet_complete")`
  se marca a 1 la primera vez; marginalia final `N_ARCHIVE_COMPLETE`
  ("Cuarenta y siete fichas. El cajon ya esta vacio. Sellalo.") y campana
  grave (`MENU_BELL` pitch 0.55).
- Sin clases nuevas (59), sin lang keys, sin assets.
- Changelog in-game (25 .md): SI, tiene manifestacion observable (el
  archivo se sella) — entrada v0.21.13 ES/EN.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.13.
- Validacion pendiente en cliente real (KI-38): completar las 47 fichas
  (o forzar con `gripeverde-memory.json`) y ver el sello + marginalia.

### DECISION 2026-08-21: v0.21.12 - El marco de la pausa queda en su sitio

Hito menor de consolidacion:
- `DiaryPauseScreen`: el metodo privado `drawCornerOrnaments` (estilo oro
  tarnished, 1 sola llamada) fue inline en su unico uso y eliminado; el
  marco del diario queda identico visualmente pero sin metodo huerfano.
- Audit global de campos/metodos privados: CERO muertos en todo
  src/main/java (el proyecto quedo limpio tras v0.21.7-0.21.11).
- Sin cambios de assets ni lang. 59 clases Java.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.12.
- Validacion pendiente en cliente real (KI-37): marco del diario de pausa
  identico (esquinas de oro) — solo refactor interno.

### DECISION 2026-08-21: v0.21.11 - La sala respira bajo la mano

Hito de micro-pulido + optimizacion (sin gameplay, sin assets):
- Micro-animacion en la Sala de consulta: la ficha bajo el cursor se alza
  2px y aclara con `EasedValue` (patron de DiaryButton). El hover se
  suaviza entre fichas (animHoverCard + cardHover en tick).
- `DiaryProgressBar` estilo ALERT: pulso suave 0.8-1.0 de alpha (el progreso
  del archivo "late" sin parpadear). Solo afecta al estilo ALERT.
- `ChroniclerMemory.save()` diferido: max 1 escritura a disco cada 2s
  (antes escribia en cada `discover`, `bump`, `markDiscovered`, etc.),
  con `flushForShutdown()` registrado via `Runtime.addShutdownHook` en
  `GripeVerdeMod` para no perder memoria al cerrar.
- `QuarantineSceneRenderer`: el grano de pelicula se rehace cada 300ms en
  modo low-end (antes 90ms) — ahorro de CPU cuando el FPS ya es bajo.
- Sin clases nuevas (59), sin lang keys, 42 OGG, 35 eventos.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL (el
  latido del archivo y el alzar de fichas son detalle de interfaz).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.11.
- Validacion pendiente en cliente real (KI-36): la ficha del archivo se
  alza con suavidad (sin saltos), el pulso ALERT es sutil, y la memoria del
  gabinete sobrevive al cierre del juego (flush).

### DECISION 2026-08-21: v0.21.10 - La estacion parpadea al cambiar

Hito tecnico (consolidacion + micro-pulido, sin gameplay):
- 15 constantes de color muertas eliminadas: `DiaryTheme.IRON_RUST`,
  `GREEN_TOXIC`, `GREEN_DEEP`, `SICK_GREEN`, `SMOKE_GREY`, `WINDOW_AMBER`,
  `WINDOW_SICK`, `CLOUD_DARK` y `DiaryChrome.SHADOW_COL`, `STEEL_LIGHT`,
  `STEEL_NEAR`, `STEEL_DARK`, `STEEL_DEEP`, `STEEL_EDGE`, `CARD_SHADE`
  (audit de uso global: 0 referencias).
- `DiaryOnlineOptionsScreen`: el medidor de alcance de la estacion
  (`computePublicSignal`) ahora responde con transicion suave (interpolacion
  en tick hacia el objetivo; antes el valor saltaba al togglear).
- Lista negra de `verifyGripeVerdeNoDeadCode` ampliada con 13 simbolos
  muertos eliminados en v0.21.7-0.21.9 (drawVictorianFrame, drawQuillCursor,
  drawVersionPanel, getMoonPhaseIndex, openTelemetry, etc.) y las 15
  constantes de color.
- Sin cambios de assets ni lang. 59 clases Java.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL (el
  parpadeo del medidor es detalle de interfaz, no evento de la ciudad).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.10.
- Validacion pendiente en cliente real (KI-35): el medidor de alcance en la
  estacion de comunicaciones transiciona suave al togglear los avisos.

### DECISION 2026-08-21: v0.21.9 - La tinta pesa menos por frame

Hito tecnico de optimizacion + consolidacion (sin gameplay, sin assets):
- `Discoveries.isEnglish()` con cache de 250ms: se consultaba el
  LanguageManager decenas de veces por frame (Options: ~23 llamadas en
  render). Ahora una lectura cada 250ms; el cambio de idioma recarga
  resources y expira solo.
- Consolidacion de texto centrado: nuevo `DiaryChrome.drawCenteredString`
  y eliminados los helpers privados duplicados en DiaryArchiveScreen,
  HiddenCipher, Herald y PosterRenderer (26 llamadas unificadas).
- `Herald.headlineToday()`: titular del dia extraido a metodo publico
  (misma semilla que el kiosco). `DiaryMultiplayerScreen` muestra el
  titular junto al libro de visitas (ticker inferior izquierdo, simetrico
  al guestbook).
- Campos muertos eliminados: `PosterRenderer.BLOOD_RNG`/`STAMP_RNG` (las
  funciones usan `new Random(posterSeed)`), `ChangelogScreen.PAD_Y`,
  `DiaryMultiplayerScreen.LOGGER`/`frameLogCounter`,
  `DiaryPauseScreen.PANEL_MIN_H`/`MARGIN_BOTTOM_IDEAL`/`ORN_TITLE_TOP`/
  `ORN_SUBTITLE_TOP`.
- Metodos muertos eliminados: `ChangelogScreen.scrollToSection`,
  `DiaryOptionsScreen.drawRowBands`/`drawCornerOrnaments` (resto de la
  migracion v0.21.7 a renderPanelBase)/`openTelemetry` (telemetria fuera
  del grid), `LogoRenderer.drawOrnamentalDivider`,
  `TextureAssets.exists` (0 llamadas).
- Nuevo `CONTRIBUTING.md` (docs oficiales): reglas branch fija, no
  gameplay, 4 escalas GUI, 3 tasks de verificacion.
- Conteos: 59 clases Java, 25 lang (80 claves), 35 eventos, 42 OGG.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL (hito
  tecnico; el ticker del Heraldo es detalle de interfaz, no evento de la
  ciudad).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.9.
- Validacion pendiente en cliente real (KI-34): el ticker del Heraldo en
  Multiplayer (ES/EN), y que el cache de idioma no tarde en reflejar un
  cambio de idioma (deberia ser instantaneo por el reload).

### DECISION 2026-08-21: v0.21.8 - Una sola mano para el idioma

Hito tecnico de consolidacion (continuacion de v0.21.7):
- 7 pantallas (Changelog, Credits, Language, Options, Pause, Title, Video)
  tenian su propio helper `isEnglish()`/`isEnglishSelected()` con logica
  duplicada (y en 2 casos catch con valor distinto). Unificadas 92 llamadas
  en la canonica `Discoveries.isEnglish()` y eliminadas las 7 definiciones
  privadas. Sin cambio de comportamiento: todas leen el idioma activo del
  LanguageManager y devuelven null-safe startsWith("en_").
- `OrnamentRenderer`: eliminados `drawVictorianFrame` + helpers privados
  `drawDoubleBorder`, `drawFiligreeCorner`, `drawFleuron` (0 referencias).
  Sobreviven `drawPaperPanel` y `drawWaxSeal` (usados por PosterRenderer).
- Sin cambios de assets, sin lang keys, sin gameplay. 59 clases Java.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL (hito
  tecnico sin manifestacion observable).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.8.
- Validacion pendiente en cliente real (KI-33): navegar las 7 pantallas en
  ES y EN para confirmar que el idioma se sigue leyendo igual.

### DECISION 2026-08-21: v0.21.7 - El gabinete sin peso muerto

Hito tecnico de consolidacion (sin gameplay, sin resourcepacks, sin tocar
logica vanilla). El usuario pidio eliminar archivos inutiles, combinar
archivos y optimizar.

**Archivos eliminados (9):**
- `render/VictorianFrame.java` (0 referencias; framework de overlay v0.6.x)
- `screens/AbstractDiaryScreen.java` (0 referencias; nunca fue extendida)
- `screens/DebugCanvasScreen.java` + `screens/SoundLabScreen.java`
  (herramientas de desarrollo inalcanzables en produccion: nada las abre)
- `render/DiaryDecorator.java` y `render/CandleLight.java` (solo las usaba
  DebugCanvasScreen)
- `render/InkEffects.java` (0 referencias)
- `widgets/DiaryMusicSlider.java` y `widgets/DiaryAmbienceSlider.java`
  (combinadas en el `DiarySlider` generico, que ya tenia labelFormatter +
  onValueChanged + applyOnDrag)

**Metodos muertos eliminados:** `DiaryChrome.drawQuillCursor`,
`drawVerticalDivider`, `drawDarkLeatherBackground` + constante `LEATHER_BASE`;
`PaperTexture.getOpenCount` y `drawSolidBlob`; `AtmosphereRenderer.getMoonPhaseIndex`;
`PosterRenderer.drawVersionPanel`.

**Optimizacion de pipeline:** nuevo `DiaryChrome.renderPanelBase(g, x, y, w, h,
accent, PaperKind)` (scaffold + pergamino + esquineros) aplicado en 14
pantallas Diary. Changelog y Credits conservan su decoracion propia
(encuadernado / marco oficial).

**Lang:** 57 claves sin uso eliminadas de los 25 idiomas (137 -> 80); eran
en su mayoria de las dev screens (`gripeverde.catalog.*`, `gripeverde.soundlab.*`,
`gripeverde.panel.version.*`) y de features retiradas (telemetria, subtitulos
viejos). Nueva clave `gripeverde.options.sound.ambience_slider` (25 idiomas)
para el slider de ambiente unificado en Sonidos (antes tenia el label
hardcodeado en espanol).

**Conteos post-consolidacion:** 59 clases Java (vs 68), 25 lang (80 claves
cada uno), 25 changelog in-game, 35 eventos de sonido, 42 OGG, 0 PNG.
sounds.json <-> ModSounds <-> OGG verificados (35/35).

**Nota R-CHANGELOG-DISCRECIONAL:** changelog in-game (25 .md) OMITIDO en este
hito por ser puramente tecnico; el unico cambio visible es el look unificado
del slider de ambiente en la sala de sonido. CHANGELOG.md raiz si lleva
entrada breve. Validacion pendiente en cliente real (KI-32).

### DECISION 2026-08-21: v0.21.6 - El heraldo cuenta la limpieza

Hito de lore (solo texto, sin gameplay):
- `Herald`: 3 noticias nuevas (limpieza del gabinete, farol del tejado que se
  apaga solo, gato del kiosco) en ES/EN + array `SECOND_*` con 4 segundas
  lineas variables (antes fija). Todo sembrado con la semilla del dia real:
  nunca dos amaneceres iguales.
- `HiddenCipher`: codigo `ARCHIVE` que abre la Sala de consulta (mismo patron
  que HOLLOWAY; sin fichas nuevas).
- Sin cambios de assets, sin clases nuevas (68), sin lang keys.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.6.
- Validacion pendiente en cliente real (KI-31): ver el Heraldo del dia con
  las noticias nuevas (se puede forzar cambiando la fecha del sistema), y
  escribir ARCHIVE en el telegrafo oculto.

### DECISION 2026-08-21: v0.21.5 - La sala responde a la consulta

Hito no-gameplay (lore + audio + performance + UI de consulta):
- Sala de consulta: busqueda por nombre/pista/id con `DiaryTextField`
  (columna derecha bajo ARCHIVE STATE, contador "N / 47", aviso de sin
  coincidencias, grilla filtrada compacta; el campo se oculta al abrir
  documentos para no dibujarse encima).
- Ficha 47: `BUTTONS_NEVER_PRESSED` ("Los botones que nunca se pulsaron") —
  cadena: cuando las 3 confesiones de limpieza de v0.21.3 estan, la cuarta
  aparece en la sala. TOTAL/ALL/nombres/pistas actualizados a 47.
- Marginalia: `N_OPEN_COUNT` (openCount >= 100 en Title.init) y
  `N_MANY_CARDS` (discoveredCount >= 44 en Archive.init).
- Audio: `Discoveries.discover()` hace ducking de la musica
  (`MenuMusic.duckFor(2500L)`) para que el archivo hable claro.
- Performance: `QuarantineSceneRenderer.isLowEnd()` (FPS < 30, muestreo
  cada 1s, defensivo con try/catch) omite niebla y faroles en
  `renderTitleScene` y `renderDiaryBackground`.
- Sin clases nuevas (68), sin lang keys, 42 OGG, 35 eventos.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.5.
- Validacion pendiente en cliente real (KI-30): 4 escalas GUI (busqueda en
  la sala, cadena de la 47, ducking al hallar, low-end en maquinas lentas).

### DECISION 2026-08-21: v0.21.4 - La tinta se asienta

Hito no-gameplay aprobado por el usuario (presentacion + audio + higiene):
- Busqueda del manual en `DiaryControlsScreen`: `DiaryTextField` filtra los
  6 puntos de mando (Mouse/Atajos + 4 toggles) por nombre traducido via
  `AbstractWidget.visible`; hint "escriba para filtrar" y aviso "ningun punto
  coincide"; se omite si panelH < 220 (escalas extremas).
- `DiaryVideoSettingsScreen`: carril reagrupado en secciones RENDER / QUALITY /
  PERFORMANCE (RENDER: barra FOV + fila Canvas con this.width x this.height;
  QUALITY: escala GUI; PERFORMANCE: medidor de FPS del cliente con
  `minecraft.getFps()` en try/catch, oculto si panelH < 330). Solo presenta;
  no toca la lista vanilla ni su logica.
- `DiaryLanguageScreen`: al hover/pendiente cada consulado declara su registro
  (136/136 para las 25 lenguas del gabinete via Set estatico REGISTERED;
  0/136 para el resto de lenguas vanilla). Verificado: los 25 lang tienen 136
  claves identicas.
- Audio: evento nuevo `ui.paper_fall` (sounds.json 34 -> 35 eventos) que
  reutiliza `ui/scroll1` + `ui/back2` (sin OGG nuevos, 42 OGG intactos);
  suena al abrir la Sala de consulta (`DiaryArchiveScreen.init`).
- Higiene: 54 imports no usados eliminados (audit de simple-name count==1);
  nueva task Gradle `verifyGripeVerdeNoDeadCode` (falla si aparece ThemeManager,
  gripeverde_steel, bg_town, IconButton, ResourcePackHandler, etc. fuera de
  comentarios; word-boundary para no chocar con `BG_TOWN_MEMORY`) conectada a
  processResources; `.editorconfig` (utf-8, LF, trim trailing whitespace).
- Sin clases nuevas (68), sin lang keys nuevas (nombres hardcoded ES/EN).
- Pendiente: las 2 pistas MenuMusic nuevas del brief requieren OGGs reales del
  usuario (no generables en sandbox); queda como deuda KI-29.
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.4.
- Validacion pendiente en cliente real (KI-28): 4 escalas GUI, filtro del
  manual, secciones del aparato, hover de registro en lenguas, papel en la
  sala de consulta.

### DECISION 2026-08-21: v0.21.3 - El gabinete respira mas ligero

Hito no-gameplay aprobado por el usuario (solo texto/rendimiento/config):
- RNG con seed fija en `VictorianSkyline`, `LivingAtmosphere` y
  `QuarantineSceneRenderer`: eliminado `Math.random` del loop caliente
  (RND compartido por clase, seeds `0x5EEDF00DL` / `0xC10E5EL` / `0xD05E`).
  La escena del Title ahora es determinista por sesion.
- `ClientConfig`: nueva clave `REDUCED_MOTION` (boolean, seccion `visual`,
  default false) respetada en `LivingAtmosphere` (farol erratico, polillas,
  rata, papeles, rafagas, pulso de niebla). NO toca widgets ni `EasedValue`
  (evita riesgo en toda la UI).
- `Discoveries`: 3 fichas nuevas sobre la limpieza v0.21.2 (43 -> 46):
  `ARCHIVE_THINNING` (openCount >= 50, dispara en `DiaryTitleScreen.init`),
  `THEME_NEVER_PLAYED` (codigo `CLEANSE` en `HiddenCipher` + telegrama
  propio), `BG_TOWN_MEMORY` (cadena en `DiaryArchiveScreen` cuando las
  otras dos estan descubiertas).
- `DiaryOptionsScreen`: el progreso del archivo usa `Discoveries.ALL.length`
  (antes `43` hardcodeado).
- Sin clases nuevas (siguen 68); sin lang keys nuevas (nombres/pistas
  hardcodeados ES/EN como el resto de fichas).
- Bump: build.gradle + GripeVerdeMod.VERSION -> 0.21.3.
- Docs sincronizadas (R-CIERRE): README, CHANGELOG, KNOWN_ISSUES, CONTEXTO
  y 25 changelog in-game (entrada observable en la ciudad: fichas/toast).
- Validacion pendiente en cliente real (E125): 4 escalas GUI, toast de las
  3 fichas, telegrama CLEANSE, quietud con REDUCED_MOTION=true, y que la
  escena con seed fija no se sienta repetida tras varias sesiones (KI-27).

### DECISION 2026-08-21 (post-v0.21.2): retiro de CONTEXTO_PARA_IA.md

El usuario ordeno eliminar el documento de contexto para otra IA por
redundancia: duplicaba a CONTEXTO.md y quedo desincronizado en v0.21.1
cuando el repo ya estaba en v0.21.2. Retirado del repo y de la lista de
documentacion del README. CONTEXTO.md queda como UNICA fuente de verdad.
Sin impacto en build ni runtime; no requiere bump de version (docs only).

### DECISION 2026-08-21: limpieza profunda del proyecto (v0.21.2)

Tras el merge de v0.21.0 (cohesión y cierre de interfaces) y la resolución de conflictos con main v0.17.2 docs, se hizo auditoría completa del repo para eliminar todo lo que ya no sirve y actualizar lo desactualizado.

**Eliminados:**
- `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` (históricos v0.14.0/v0.14.1, ya revertidos, marcaban polish de una línea con steel pack que no existe desde v0.15.0 y v0.19.0)
- `sound-tests/` (wav fuente `03_brass_latch_clean.wav` ya integrado como ogg, README de selección ya cumplido)
- `src/main/resources/assets/gripeverde/textures/gui/title/bg_town.png` (2.1M sin uso real, Title usa render procedural `QuarantineSceneRenderer`)
- `client/theme/` parcial: `ThemeManager`, `GhoulPalette`, `ScreenLayout`, `ScreenTheme`, `AbstractScreenTheme`, `UIComponent`, `UIScale`, `components/*` (framework inerte sin themes registrados desde v0.6.0, solo `Anchor`, `LayoutRegion`, `Spacing` conservados por uso activo en DiaryTitleScreen)
- `client/widgets/`: `IconButton`, `SilentButton`, `DiarySeparator` (definidos pero nunca usados fuera de su propio archivo)
- `client/ResourcePackHandler.java` y `resourcepacks/gripeverde_steel/` ya estaban eliminados desde v0.19.0, pero se confirmó que no quedaran referencias activas en `CONTEXTO_PARA_IA.md` y build.gradle

**Actualizados:**
- `README.md`: versión v0.21.1 + nota de limpieza, lista de docs actualizada (sin _backups), verificaciones de build (solo localización y sonidos)
- `CONTEXTO_PARA_IA.md`: reescrito completo a v0.21.1, sin steel pack, 68 clases Java (vs 83), 0 PNG de contenedores, 2 tasks de verificación, estructura sin resourcepacks, rama `arena/01a025bc-gripeverde`, historial de limpieza
- `CHANGELOG.md`: añadidos v0.19.0 (retiro definitivo del acero, overhaul de interfaz), v0.17.2, v0.17.1, v0.17.0 (historia del acero temporal)
- `src/main/resources/assets/gripeverde/changelog/*.md` (25 archivos): añadida entrada v0.21.1 en EN y ES
- `TextureAssets.java`: eliminado `BG_TOWN` y `hasBgTown()`, solo quedan `ICON_BTN_MP` y `ICON_BTN_MODS` como identificadores lógicos para GreenFluButton (dibujo procedural)
- `ScreenEventHandler.java`: eliminado import y llamada a ThemeManager (inerte), simplificado a solo intercept Title/Pause y feedback sonoro

**Conteos post-limpieza:**
- 68 clases Java (vs 83 en v0.17.2)
- 25 lang, 25 changelog in-game, 42 OGG, 0 PNG de contenedores
- Working tree limpio, sin PRs abiertos, main en v0.21.1

### DECISION 2026-08-09: hotfix de arranque (v0.21.1)

Tras probar el jar real en cliente, aparecio un `NoSuchMethodError` en
`ModSounds`: el runtime de Forge 1.20.1 no estaba resolviendo
`SoundEvent.createVariableRangeEvent(ResourceLocation)` como en dev-time.
Se corrigio el registro para construir `SoundEvent` de forma compatible via
reflection/constructores conocidos, evitando el bloqueo de arranque.

### DECISION 2026-08-09: cohesion y cierre de interfaces secundarias (v0.21.0)

Con el nucleo principal del gabinete ya fuerte, este pase se concentro en las
salas que seguian mas cerca de una screen vanilla tematizada que de una pieza
propia de Gripe Verde. La guia del usuario fue explicita: no volver a tocar las
A como `Title`, `Pause`, `Sound`, `Language` o `Multiplayer`, y concentrar el
esfuerzo en terminar las C/D y consolidar las B.

Prioridades ejecutadas en esta sesion:
- `DiaryChatOptionsScreen` -> oficina de correspondencia mas clara.
- `DiaryOnlineOptionsScreen` -> estacion de comunicaciones con identidad.
- `DiarySkinCustomizationScreen` -> polish final, sin reconstruccion total.
- `DiaryArchiveScreen` y `ChangelogScreen` -> mejor cierre entre consulta y
  registro de revisiones.
- `DiaryDirectJoinScreen`, `DiaryEditServerScreen`, `DiaryConfirmScreen` ->
  refinamiento de formularios y autorizaciones.

Regla reforzada para continuidad:
- si una pantalla ya esta en B, primero simplificar o reordenar antes de
  agregar otra capa visual.

### DECISION 2026-08-09: identidad de interfaz (v0.20.0)

Se audito el resultado real del pase documental anterior y se detecto una
segunda necesidad: la UI ya tenia sistema, pero todavia mostraba rigidez de
"dashboard de pergamino" en varias pantallas. La consigna de esta sesion fue
refinar composicion, jerarquia, profundidad y widgets, no sumar mas contenido.

Ajustes rectores del pase:
- `ExpedientHeader` se conserva, pero con uso mas sobrio y mejor jerarquia.
- `DiaryChrome` evoluciona hacia un lenguaje menos cuadrado: railes
  incompletos, tarjetas menos rigidas, capsulas mas discretas, scrollbars mas
  fisicas y scaffolds con lectura editorial.
- `DiarySlider`, `DiaryMusicSlider`, `DiaryAmbienceSlider` y `DiaryToggle`
  reciben tratamiento de instrumento/placa mecanica en vez de barra o switch
  generico.
- `DiaryOptionsScreen`, `DiarySoundOptionsScreen`, `DiaryControlsScreen`,
  `DiaryChatOptionsScreen`, `DiaryAccessibilityOptionsScreen`,
  `DiaryLanguageScreen`, `DiaryOnlineOptionsScreen`, `DiaryCreditsScreen`,
  `DiaryMultiplayerScreen`, `DiaryPauseScreen` y `DiaryTitleScreen` se pulen
  para que parezcan salas del mismo gabinete, pero no el mismo template.
- `DiaryVideoSettingsScreen` se mantiene como pantalla propia y se integra al
  tono final del gabinete.

Se mantiene la regla absoluta de no usar resourcepacks ni texturas vanilla
modificadas para identidad visual.

### DECISION 2026-08-08: pase mayor de interfaces (v0.18.0)

Se hizo un pase transversal de interfaz sobre la linea limpia posterior al
retiro del resource pack. La directiva del usuario fue explicita: nada de
`gripeverde_steel`, nada de `tools/steel_pack`, nada de texturas vanilla
modificadas y nada de vender easter eggs viejos como novedades. El objetivo
fue profesionalizar las pantallas propias del mod con un design system de
cabeceras documentales, railes de estado, tarjetas laterales, medidores,
revelado de paneles y composicion por capas, manteniendo intacta la logica
vanilla de opciones/listas/sliders.

Piezas nuevas o ampliadas de esta sesion:
- `client/render/ExpedientHeader.java`: cabecera reutilizable con expediente,
  referencia, estado y flavor.
- `client/render/DiaryChrome.java`: ampliado con reveal, capsulas de estado,
  tarjetas, railes internos y scaffolding de profundidad.
- `client/widgets/DiaryProgressBar.java`: estilos multiples + marcas.
- `client/screens/DiaryVideoSettingsScreen.java`: nueva consola tecnica para
  VideoSettingsScreen sobre presentacion propia.
- Rework fuerte de `DiaryOptionsScreen`, `DiarySoundOptionsScreen`,
  `DiaryControlsScreen`, `DiaryChatOptionsScreen`,
  `DiaryAccessibilityOptionsScreen`, `DiaryLanguageScreen`,
  `DiaryOnlineOptionsScreen`, `DiaryCreditsScreen`,
  `DiaryMultiplayerScreen`, `DiaryPauseScreen` y `DiaryTitleScreen`.

Regla reafirmada: el salto visual de UI debe venir del propio mod. La
presentacion se rehace con Java/render/widgets, no con overlays de
resourcepacks.

### DECISION 2026-08-08: reversion de la linea v0.17.x

Se retiro nuevamente la reintroduccion del resource pack `gripeverde_steel`
y de la identidad visual asociada en codigo/documentacion. El usuario pidio
volver al estado anterior a su incorporacion; los documentos confirman que
ese punto estable es **v0.16.0** (la linea v0.17.0 es la que vuelve a
introducir el pack). Esta sesion deja el repo otra vez alineado con
contenedores vanilla y sin pipeline/registro del pack.

### DECISION 2026-08-05: retiro del resource pack de contenedores

El usuario descarto por completo el resource pack tematico de contenedores
(steel v0.13.x, silver steel v0.14.x y la muestra "fresadora" v0.15-dev).
Orden: "reinicies desde 0 con las interfaces vanilla". Ejecutado en v0.15.0:

- Eliminado `src/main/resources/resourcepacks/gripeverde_theme/` (pack built-in completo: 21 texturas + pack.mcmeta + pack.png + COMPATIBILITY.md).
- Eliminado `client/ResourcePackHandler.java` (registro del pack via AddPackFindersEvent).
- Eliminada la task `verifyGripeVerdeThemeAssets` de build.gradle y su dependsOn en processResources.
- Eliminado `tools/` (pipeline de generacion del pack: kit.py, build_*.py, verify.py, vanilla/ y export/).
- Los contenedores del juego vuelven a ser 100% vanilla. El mod conserva Title, diario, opciones, sonidos.
- KIs del pack cerrados. No retomar el pack sin nueva instruccion explicita del usuario.

### v0.15.1 - Segunda mano (2026-08-05)

El usuario reporto dos restos tras probar v0.15.0:
1. Faltaba el slider de campo de vision (FOV) en el gabinete de opciones
   (vanilla lo tiene en OptionsScreen). Agregado como fila 6 del grid con
   `DiarySlider`, rango 30-110, default 70, vinculado a `options.fov()`.
2. Seguian apareciendo letras personalizadas sobre los contenedores:
   era `ContainerOverlayHandler` (re-dibujaba los titulos con color
   pergamino y sombra). ELIMINADO. Fix v0.15.2: el label del slider FOV no
   mostraba el numero porque la clave vanilla "options.fov" no tiene
   placeholder (es solo "FOV"); ahora se construye "FOV: N" explicito. Los titulos de contenedores ahora son
   vanilla puro.

NOTA v0.21.1: `ThemeManager` y framework inerte de overlays (GhoulPalette, ScreenLayout, etc) fueron eliminados. Solo se conservan `Anchor`, `LayoutRegion`, `Spacing` porque DiaryTitleScreen los usa. `TooltipEventHandler` solo recolorea tooltips (pergamino) - no letras.

### NOTA plataforma: adjuntos del usuario

`/home/user/uploads/` NO existe en el sandbox aunque el sistema reporte archivos
adjuntos. Verificar con `ls` antes de intentar leer; si falta, pedir al usuario
que describa la imagen o la suba por otra via.

### Estado sonoro de botones

- `ui.button_main` -> `button_clean_03_brass_latch.ogg`.
- `ui.button_section` -> `modal.ogg`.
- `ui.back` -> `back1.ogg`, `back2.ogg`.
- Sonidos retirados/rechazados: `button_clean_01`, `button_clean_02`, `error1`, `error2`, `open1`, `open2`, `respawn`.

### Instrucciones para otra IA

1. No cambiar de branch.
2. No agregar gameplay.
3. Validar JSON y referencias de sonidos antes de commit.
4. Entregar siempre PW completo de build/deploy al finalizar.


Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## ARCHIVO HISTORICO DE CONTEXTO (NO OPERATIVO)

Desde este punto se preservan instantaneas antiguas, rutas retiradas, reglas
de su momento y planes que ya fueron ejecutados o descartados. No interpretar
ninguna aparicion de "actual", "vigente", "deploy" o "proximo hito" en este
archivo historico como una instruccion presente: el addendum operativo del
inicio es la unica fuente vigente.

## Estado verificado en la instantanea archivada

- Version estable de aquella instantanea: v0.21.24
- build.gradle: version = '0.21.24'
- GripeVerdeMod.VERSION: "0.21.24"
- mods.toml: usa ${mod_version} desde project.version.toString() (ver E32)
- Ultimo tag publicado: v0.13.10 (no se han publicado tags desde el ciclo v0.13.x; los bumps v0.14-v0.17 quedaron sin tag)
- Working tree esperado al retomar: limpio
- Repositorio remoto sincronizado
- Resource pack built-in: ELIMINADO (v0.15.0, reafirmado en v0.19.0-v0.21.1). Contenedores 100% vanilla.
- ContainerOverlayHandler: ELIMINADO (v0.15.1). Titulos de contenedores vanilla.
- Nota historica: la linea v0.17.0-v0.17.2 introdujo temporalmente `gripeverde_steel` (REQUIRED, 149 texturas), pero fue retirada de nuevo en v0.19.0 por directiva de no usar resourcepacks ni texturas vanilla modificadas.
- Slider FOV: agregado al gabinete de opciones (v0.15.1) y arreglado el
  label para que muestre el numero (v0.15.2): la clave vanilla "options.fov"
  es solo "FOV" (sin placeholder), asi que el numero se agrega explicito:
  "FOV: 70" / "Campo de vision: 70".

## Historial reciente

- v0.6.5: base estable post-revert.
- v0.7.0: Sub-hito 1 KI-03. Calle adaptativa.
- v0.7.1: Sub-hito 2 KI-03. Postes ocultos temporalmente.
- v0.8.0: Sistema de musica del Title (playback base + fixes criticos: E32, E33, KI-04).
- v0.8.1: Slider de volumen en OptionsScreen.
- v0.8.2: Crossfade entre pistas.
- v0.8.3: Ducking automatico
- v0.9.0: Profundidad de skyline + limpieza de cielo.
- v0.9.1: Reactivacion de faroles de gas anclados a la calle (Sub-hito A KI-03).
- v0.9.2: Humo y nubes con tecnica de blobs suaves (fix KI-05).
- Post v0.8.3: fixes de persistencia (idempotencia, auto-stop, auto-recovery), ajuste de volumenes ambientales, reescritura completa de changelogs con tono narrativo de diario.

- v0.11.0: DiaryOptionsScreen sobre pergamino con Title vivo detras. KI-06 cerrado extendido. KI-07 abierto.
- v0.11.1: DiaryLanguageScreen sobre pergamino. KI-07 parcial. KI-08 abierto (banderas).
- v0.11.2: DiaryChatOptionsScreen sobre pergamino. KI-07 avanzado. Formalizacion de R-CHANGELOG-AGRUPADO, R-BUILD-DEPLOY, R-VANILLA-LIST-COORDS.
- v0.11.3: DiaryAccessibilityOptionsScreen sobre pergamino + pulido cosmetico de Chat y Lenguas.
- v0.11.4: DiarySkinCustomizationScreen (hito K) + scroll estilizado y acelerado en Lenguas (hito L) + refactor DiaryChrome (extraccion de helpers cosmeticos compartidos) + panel adaptativo en Accesibilidad para escalas 3/4. KI-09 abierto (scroll estilizado en OptionsList).
- v0.11.5: Scroll estilizado en Accesibilidad y Chat (hito M-simple). KI-09 cerrado. Panel de Accesibilidad ampliado a 380px. Thumb minimo 32px. KI-10 abierto (tooltips vanilla morados). Controles siguen vanilla (Controlling).
- v0.11.6: DiarySoundOptionsScreen sobre pergamino (hito K2). Slider "Musica del Cronista" propio integrado en la pantalla. Reordenamiento de DiaryOptionsScreen (grid 2x5 sin Telemetria). Serie de correcciones tecnicas: E85 (javap flags), E86 (updateWidgetNarration public), E87 (SRG f_244146_ real). Compatibilidad con IMP preservada. KI-11 abierto (slider no aplica en vivo durante drag).
- v0.11.7: DiaryControlsScreen + DiaryCreditsScreen (hitos H + Credits del Grupo A). KI-12 abierto (fondos dirt en pantallas de trabajo requieren mixin). Primer intento de mixin revertido por fallo de refmap (E90).
- v0.11.8: DiaryMultiplayerScreen + DiaryEditServerScreen + DiaryDirectJoinScreen + DiaryConfirmScreen (hito B3, flujo multijugador completo). Server pinneado "Ghoul Outbreak Official". KI-13 y KI-14 abiertos (server pinneado sin proteccion, banda oscura del server).
- v0.11.9: Coherencia del gabinete.
- v0.11.10: Polish visual (KI-15 fades + KI-16 scrollbar respirada + banda multiplayer atenuada).
- v0.11.11: DiaryTextField (widget de texto sobre pergamino, en Edit y Direct).
- v0.11.12: DiaryToggle (widget de interruptor sobre pergamino, en Controls).
- v0.11.13: Tooltips estilizados pergamino globalmente (KI-10 cerrado, via RenderTooltipEvent.Color).
- v0.11.10: Polish visual (KI-15 fades + KI-16 scrollbar respirada + banda multiplayer atenuada).
- v0.11.11: DiaryTextField (widget de texto sobre pergamino, en Edit y Direct).
- v0.11.12: DiaryToggle (widget de interruptor sobre pergamino, en Controls).
- v0.11.13: Tooltips estilizados pergamino globalmente (KI-10 cerrado, via RenderTooltipEvent.Color). Fixes de KI-11 (musica se congelaba al navegar), clamp de paneles chicos, ESC cancela idioma, unificacion de cabeceras y sombras, banda de servidor suavizada, textura de pergamino en lista de lenguas, sombra proyectada del panel, DiaryButton diferenciado del panel, scrollbar alineada al panel. KI-11 cerrado. KI-15 y KI-16 abiertos (fade inferior y respiracion de scrollbar).

## KI-03 - Estado post v0.9.0

Sub-hitos completados:
- v0.7.0: Calle adaptativa.
- v0.7.1: Postes ocultos temporalmente.
- v0.9.0: Jerarquia visual del skyline (BG/FG diferenciados, chimeneas ancladas).

Sub-hitos completados (v0.9.2):
- v0.9.1: Reactivar postes/faroles.
- v0.9.2: Fix tecnica humo/nubes (KI-05).

---

## Ruta del proyecto

C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde

## Repositorio

- Local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde (privado)
- Ultimo tag: v0.13.10

## Rutas reales verificadas

Namespace: com.eternalcraft.gripeverde
Prefijo: src\main\java\com\eternalcraft\gripeverde\

- Screens: client\screens\
- Renderers: client\render\
- Audio: client\audio\
- Widgets: client\widgets\

Archivos clave del fondo del Title (KI-03):
- client\render\LivingAtmosphere.java
- client\render\VictorianSkyline.java
- client\render\AtmosphereRenderer.java
- client\screens\DiaryTitleScreen.java

Archivos clave del sistema de audio (v0.8.x):
- client\audio\ModSounds.java
- client\audio\MenuAmbience.java
- client\audio\MenuMusic.java
- client\audio\FadingMusicInstance.java
- client\audio\UiSounds.java
- client\widgets\MusicVolumeSlider.java
- client\ScreenEventHandler.java

Assets de sonido:
- assets\gripeverde\sounds.json (31 entradas)
- assets\gripeverde\sounds\music\music_title_1..4.ogg

Documentacion:
- CHANGELOG.md (raiz, espanol, tono narrativo diario)
- CONTEXTO.md (este archivo)
- KNOWN_ISSUES.md
- README.md
- _backups\LECCIONES_v0.7.x.md
- assets\gripeverde\changelog\{es_*,en_*}.md (25 archivos in-game)

---

## Pipeline actual del Title

Orden en DiaryTitleScreen.render:
1. AtmosphereRenderer.renderSky
2. VictorianSkyline.renderBackground
3. LivingAtmosphere.renderFogLow
4. VictorianSkyline.renderForeground
5. LivingAtmosphere.renderStreetLevel
6. LivingAtmosphere.renderAir
7. LivingAtmosphere.renderFlash
8. LivingAtmosphere.renderVignette

Audio en Title:
- init(): MenuAmbience.start() + MenuMusic.start() (ambos idempotentes)
- tick(): MenuAmbience.tick() + MenuMusic.tick() (auto-stop si mc.level != null)
- removed(): MenuAmbience.stop() (MenuMusic NO se detiene para persistir)

---

## Estado del sistema de musica (v0.8.3 + fixes)

### MenuMusic
- 4 pistas OGG en assets\sounds\music\
- Playback: shuffle sin repetir hasta agotar ciclo
- Volumen: userVolume (0.0-1.0) * MAX_VOLUME (0.8), ajustable en tiempo real
- Crossfade: 4s entre pistas consecutivas (detecta cuando faltan <= 4s)
- Gap entre pistas: 2-5s aleatorio (solo si no hay crossfade)
- Ducking: baja al 35% durante 2.5s cuando MenuAmbience dispara RARE
- Idempotencia: start() no reinicia si ya suena (verifica isActive del SoundManager)
- Auto-recovery: si perdio la instancia (ej: al salir de un server), start() reinicia
- Auto-stop: cuando mc.level != null, se detiene sola
- DEBUG_LOGGING: false por defecto

### FadingMusicInstance
- Extiende AbstractSoundInstance, implementa TickableSoundInstance
- Volumen dinamico controlable via setDynamicVolume()
- Se marca como done cuando expira su duracion + 1000ms buffer
- Duraciones hardcoded por pista (medidas al convertir):
  music_1: 160s, music_2: 90s, music_3: 177s, music_4: 160s

### MenuAmbience
- 3 loops base: wind (0.35), rain (0.30), city (0.20)
- 4 eventos ocasionales: footsteps, wood_creak, door, ambient_occasional (0.25-0.35)
- 4 sonidos raros: bell, thunder, crows, chain (0.30-0.55)
- Tick del reloj de bolsillo cada 5s (vol 0.025)
- Al disparar RARE llama MenuMusic.duckFor(2500L)
- DEBUG_LOGGING: false por defecto

### MusicVolumeSlider
- Widget en client/widgets
- Posicion: centrado horizontal, y = screen.height - 25
- Ancho: 160px, alto: 20px
- Se inyecta via ScreenEventHandler.onScreenInitPost

### UiSounds
- Metodos safePlay() defensivos: toleran RegistryObject vacio
- Workaround permanente para KI-04 (ModernFix RegistryObjectMixin)

---

## Bugs conocidos activos

### KI-01 (RESUELTO)
- Freeze al presionar Leave. No reproducible en versiones v0.9.x.

### KI-02 (RESUELTO)
- Crash con screenshots F2. No reproducible en versiones v0.9.x. (Se mantiene Win+Shift+S como buena practica.)

### KI-03 (RESUELTO COMPLETO en v0.9.1)
- Calle: RESUELTO en v0.7.0.
- Skyline sin jerarquia: RESUELTO en v0.9.0.
- Postes/faroles: RESUELTO en v0.9.1.
- Contexto atmosferico mas rico alrededor de la luna: no es bug, es mejora futura opcional.

### KI-04 (MITIGADO)
- Incompatibilidad con ModernFix RegistryObjectMixin.
- Workaround: UiSounds.safePlay() tolera NPE silenciosamente.

### KI-05 (RESUELTO en v0.9.2)
- Humo, nubes y velo lunar dibujados con drawSoftBlob (6 anillos concentricos, alpha cuadratico).

### KI-06 (CERRADO)
- Changelog in-game usa pantalla propia sobre pergamino, con lectura narrativa y scroll estilizado.

---

## Resolucion del usuario

- Nativa: 1920 x 1080 (capturas recientes)
- Alturas efectivas por GuiScale:
  - Escala 1: h grande
  - Escala 2: h medio
  - Escala 3: h chico
  - Escala 4: h muy chico (modo minimal en Title)

---

## Reglas vigentes

### Desarrollo
- Un sistema por version.
- Un cambio visual/audio sensible por bump.
- Validacion visual/auditiva en 4 escalas cuando se toque UI/audio.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real antes de proponer cambios.
- Java UTF-8 sin BOM.
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre acompanado de deploy.
- R-JAR: Limpiar jars gripeverde-*.jar viejos antes de validar.
- R-VER: Confirmar version visible en el reloj antes de aprobar.
- R-GRADLE-VER: mod_version en build.gradle debe usar project.version.toString().

### PowerShell
- R-CD: Todo bloque de comandos debe iniciar con Set-Location al repositorio.
- R-PS: Minimizar cantidad de bloques.
- R-PATH: Rutas absolutas si no estamos en la raiz.
- R-PATH2: gradlew y git requieren Set-Location al repo primero.
- R-PS-HASH: Los hashtables de PowerShell son case-insensitive; usar .Replace() en cadena para variantes may/min de la misma clave.

### Validacion
- R-CACHE: Reiniciar cliente al cambiar GuiScale si hay duda.
- R-CAP: Capturas pre/post en 4 escalas para cambios visuales.
- R-JSON: Validar JSON con ConvertFrom-Json tras cualquier edit.

### Java
- R-DUP: Verificar que no se redefinan variables en scope superior.

### Texto y encoding
- R-ENCODING: Nunca hacer append textual iterativo a archivos con tildes; el encoding se degrada por cada round-trip. Usar placeholders ASCII ([[oacute]] etc.) y reinyeccion via [System.IO.File]::WriteAllText con UTF-8 sin BOM.
- R-CHANGELOG: Tono narrativo de diario victoriano. No mencionar UI, sliders, opciones, ni tecnica. Hablar de la ciudad, el observador, la crónica.

### Narrativa (R-DIEGETICO)
- R-DIEGETICO: Toda narrativa de cara al usuario (CHANGELOG.md raiz, los 25 changelog/*.md in-game, README bloque "Version actual", parrafos descriptivos) debe estar escrita desde la voz del cronista victoriano. Prohibido mencionar: bloques, pixeles, cuadrados, rectangulos, angulos rectos, escalas GUI, sliders, opciones, config, disco, "el observador percibe que", comparaciones tipo "antes se veia mal / ahora bien", ajustes tecnicos, versiones anteriores del software. El cronista narra lo que ocurre esta noche en la ciudad, no un release note.
- Verificacion rapida: si la frase suena a devlog de un videojuego, esta mal. Si suena a entrada de diario escrita a la luz de una vela, esta bien.

### Cierre de hito (R-CIERRE)
### Discrecionalidad del changelog in-game (R-CHANGELOG-DISCRECIONAL)
- R-CHANGELOG-DISCRECIONAL: No toda version del codigo requiere entrada en los 25 changelog/*.md in-game. Si el hito es puramente tecnico, administrativo o correctivo sin manifestacion observable en la ciudad, se OMITE. El changelog in-game es una cronica selectiva del cronista, no un log de commits. El cronista solo narra noches significativas.
- Corolario: el CHANGELOG.md raiz (repo) SI puede llevar la entrada como registro tecnico breve, pero sin forzar prosa narrativa cuando no hay nada real que contar.

### Cierre de hito (R-CIERRE)
- R-CIERRE: Al cerrar cualquier hito (bump de version), es OBLIGATORIO actualizar en la misma sesion, antes de commit:
  1. CONTEXTO.md (version, tag, addendum del hito, actualizar bloque "PROXIMO HITO PLANIFICADO")
  2. CHANGELOG.md (raiz, entrada narrativa bajo R-DIEGETICO)
  3. KNOWN_ISSUES.md (metadatos de version/tag, cerrar KIs resueltos, catalogar errores nuevos y reglas nuevas)
  4. README.md (bloque "Version actual" con parrafo narrativo bajo R-DIEGETICO)
  5. Los 25 changelog/*.md in-game bajo R-DIEGETICO SOLO si el hito tiene manifestacion observable en la ciudad (ver R-CHANGELOG-DISCRECIONAL)
  Luego: commit + tag + push. NUNCA cerrar un chat con esos docs desincronizados del codigo.

---

## Errores clave a NO repetir

- E27-E31: ver _backups\LECCIONES_v0.7.x.md (sesion v0.7.x fallida).
- E32: mod_version hardcodeado en build.gradle (usar project.version.toString()).
- E33: JSON append rompio sounds.json (validar con ConvertFrom-Json + regenerar completo).
- E34: variable mc duplicada en tick() (R-DUP).
- E35: PowerShell desde system32 (R-PATH2).
- E36: hashtable duplicate keys por case-insensitive (R-PS-HASH).
- E37: mojibake acumulado por reediciones sucesivas de archivos con tildes; se corrompe geometricamente cada vez que se edita en encoding equivocado. Solucion: reescribir completo desde plantilla con placeholders + WriteAllText UTF-8.

---

## Deploy

.deploy contiene: DEPLOY_MODS_PATH=D:\Juegos\Minecraft\Instances\Test\mods

Regla:
- Build + deploy juntos.
- Limpiar jars gripeverde-*.jar viejos antes de validar visualmente.

---

## Documentacion del proyecto

- CONTEXTO.md: este archivo, punto unico de entrada.
- CHANGELOG.md: historial narrativo completo v0.1.0 a v0.9.2 (tono diario, UTF-8 correcto).
- KNOWN_ISSUES.md: estado tecnico completo, bugs, reglas, plan futuro.
- README.md: portada publica del repo, actualizado a v0.9.2.
- _backups\LECCIONES_v0.7.x.md: errores catalogados + reglas R-*.
- assets\gripeverde\changelog\*.md: 25 archivos in-game (5 en_*, 20 es_*).

---

## Al retomar

Leer este documento COMPLETO antes de proponer cualquier cambio.

Si el usuario dice "arranquemos":
1. Set-Location al repo.
2. git status --short y git log --oneline -5.
3. Preguntar que hito quiere continuar (A, B o C).

Comandos de reentrada:
  Set-Location C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
  git status --short
  git log --oneline -5

---

## ADDENDUM v0.9.0 (post-hito visual)

### Errores nuevos catalogados

- **E38**: [System.IO.File]::ReadAllText / WriteAllText usan el CWD del proceso .NET (normalmente el home del usuario), NO respetan Set-Location de PowerShell. Solucion: usar siempre "$PWD\ruta" absoluta al llamar metodos .NET de archivos.

- **E39**: Here-strings @'...'@ largos en PowerShell inline se cuelgan o cortan por longitud de buffer de consola cuando superan cierto tamano (~4KB). Solucion: para archivos grandes usar base64 decodificado via [Convert]::FromBase64String + WriteAllBytes. Para cambios chicos usar .Replace() sobre contenido leido.

- **E40**: Git en Windows convierte LF->CRLF al hacer add. Los warnings son inofensivos pero verificar que archivos criticos (build.gradle, .java) queden como el original espera. Los .java del proyecto son LF puro (verificado con byte scan).

- **E41**: Al buscar el jar recien compilado con Get-ChildItem sin limpiar antes, se puede tomar un jar viejo de builds previos. Solucion: R-JAR2 (nueva): Remove-Item build\libs\gripeverde-*.jar ANTES de gradlew build. Ya aplicado en el pipeline de deploy.

- **E42**: Changelog in-game no se carga por archivos 'v0_x_y_z.md'. ChangelogScreen carga SOLO 'changelog/<lang>.md' (locale). Para agregar una version, insertar una nueva seccion '## Versión X.Y.Z' dentro de los 25 archivos es_*.md / en_*.md (tras '## Notas de Actualización').

- **E43**: [System.IO.File]::ReadAllText / WriteAllText con ruta relativa fallan si el CWD del proceso .NET no coincide con Set-Location de PowerShell (ocurre al abrir nueva instancia PS que arranca en C:\Users\<user>). Solucion: siempre pasar ruta absoluta con $PWD\... explicito. Cubierto por R-DOTNET-PATH y R-CD.

### Reglas nuevas

- **R-DOTNET-PATH**: Al usar [System.IO.File]::* siempre pasar rutas absolutas con $PWD\... explicito.
- **R-JAR2**: Limpiar build\libs\gripeverde-*.jar ANTES de compilar, no solo del deploy.
- **R-EOL**: Antes de reemplazos multilinea con .Replace(), verificar EOL del archivo (CRLF vs LF) con byte scan. Los .java del proyecto son LF puro.
- **R-ITER-VISUAL**: NO iterar mas de 3 veces sobre un mismo problema visual. Si tras 3 ajustes de parametros el resultado no convence, es problema de tecnica, no de valor. Anotar como deuda y cerrar hito.

- **R-CHANGELOG-LOCALE**: El changelog in-game se mantiene por locale (es_*.md / en_*.md). Para una nueva version: insertar la seccion '## Versión X.Y.Z' en los 25 archivos (despues de '## Notas de Actualización'), rebuild+deploy. No crear archivos separados 'es_v0_x_y_z.md' / 'en_v0_x_y_z.md'.



---


## ADDENDUM v0.9.2 (higiene del repo)

### Auditoria de limpieza realizada
- _backups reducido a 3 elementos conservados:
  - LECCIONES_v0.7.x.md
  - pre_v0.9.1_20260719_130158
  - pre_v0.9.2_20260719_132207
- Se eliminaron backups historicos obsoletos (v0.4.4, v0.5.0, v0.6.0, v0.7.x, logs gradle, capturas).
- Se eliminaron 27 archivos build*.txt de la raiz (logs sueltos, no trackeados).
- gradlew clean ejecutado con exito; build\ limpio.
- Auditoria de assets y codigo completada:
  - 0 OGGs huerfanos respecto de sounds.json
  - 0 clases Java candidatas a borrado por orfandad
- git status --short verificado limpio tras la limpieza.

### Nota operativa
- La limpieza fue de higiene del repositorio, no de ahorro de espacio significativo.
- Los mayores consumidores de espacio son los assets OGG y el cache global de Gradle (fuera del repo).

---

### Errores nuevos catalogados post-v0.10.4

- **E48**: reemplazo global .Replace("v0.10.2", "v0.10.3") sobre CONTEXTO.md piso referencias historicas (ADDENDUM y linea de historial). Fix: reparado con reemplazos exactos. Regla derivada abajo.
- **E50**: .Replace("0.10.3", "0.10.4") sobre README.md piso el numero de version pero dejo la descripcion vieja adosada (v0.10.4 quedo con texto de v0.10.2). Ademas la entrada real de v0.10.3 nunca se agrego. Fix: reemplazar bloque narrativo completo, no solo el numero.

### Reglas nuevas post-v0.10.4

- **R-REPLACE-VERSION**: nunca hacer .Replace(versionAnterior, versionNueva) en documentos con historial (CONTEXTO.md, README.md, CHANGELOG.md). Reemplazar solo en anclas de cabecera unicas y verificables (ej: "- Version estable actual: vX.Y.Z"). Para agregar entrada narrativa a README.md o CHANGELOG.md, reemplazar el bloque narrativo completo de la version anterior, no el numero suelto.


### Errores nuevos catalogados post-cierre v0.10.4

- **E51**: script de actualizacion de los 25 changelog/*.md in-game asumio ancla unica "## Notas de Actualizacion" cuando los 5 archivos en_*.md usan "## Update Notes". El script no lanzo error, simplemente no encontro el ancla en ingles y no hizo nada, pero reporto "Actualizado" en pantalla porque el Write-Host se ejecutaba antes de verificar el .Contains(). Resultado: 20/25 archivos modificados en el primer intento; los 5 ingleses quedaron desactualizados. Detectado al comparar el conteo de git ("20 files changed") con el reporte del script ("25 Actualizados"). Fix: segundo pase con ancla "## Update Notes" para en_*.md.

### Reglas nuevas post-cierre v0.10.4

- **R-VERIFY-COUNT**: al hacer batch updates de multiples archivos con potencialmente distintas anclas, verificar SIEMPRE el conteo real post-cambio (ej: contar cuantos contienen el nuevo string) antes de commitear. No confiar en el Write-Host del bucle.
- **R-CHANGELOG-ANCHOR**: los changelog in-game usan dos anclas distintas segun locale: "## Notas de Actualizacion" (es_*) y "## Update Notes" (en_*). Scripts de batch deben iterar con logica por prefijo de archivo.

## PROXIMO HITO PLANIFICADO

### Candidato A: v0.10.0 - Ambientar el menu del Historial de Cambios (KI-06)
Objetivo: dar al ChangelogScreen la misma calidad visual que el Title.
- Fondo tipo pergamino (aprovechar PaperTexture existente).
- Tipografia acorde al estilo victoriano.
- Marco decorativo estilo poster institucional o carta oficial.
- Boton "Cerrar" al estilo DiaryButton en vez del vanilla.
Riesgo: bajo. Toca solo ChangelogScreen.

### Candidato B: v0.10.0 alternativo - Persistencia del volumen de musica
Objetivo: guardar el valor del slider en disco (archivo de config del mod).
Riesgo: bajo.

### Candidato C: v0.11.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema de gameplay real (enfermedad, sintomas, mecanica de cuarentena).
Riesgo: alto. Requiere diseno previo.


---
## ADDENDUM v0.10.x (Hitos A + B cerrados)

### v0.10.0 - Hito A: ChangelogScreen victoriano (KI-06 cerrado)
- ChangelogScreen ahora dibuja el panel con PaperTexture.drawClean() (nueva variante sin wrinkles).
- Wrinkles horizontales suprimidas dentro del panel para no confundirse con lineas de texto al scrollear.
- Manchas, viñeta y bloodMist se mantienen: dan textura organica al pergamino.
- Los 25 changelog/*.md fueron reescritos bajo R-DIEGETICO y guardados en UTF-8 puro (mata E37 en esa familia de archivos).
- SlimButton y DiaryTheme no fueron modificados (ya cumplian).

### v0.10.1 - Hito B: Persistencia del volumen de musica
- Nueva clase com.eternalcraft.gripeverde.config.ClientConfig usando Forge ForgeConfigSpec.
- Registrada en GripeVerdeMod como ModConfig.Type.CLIENT con filename gripeverde-client.toml.
- MenuMusic.getUserVolume() hace lazy-load desde config en primera lectura.
- MenuMusic.setUserVolume() persiste inmediatamente en disco via .set() + .save().
- Fix aplicado sobre uso interno: los calculos de baseVol usan getUserVolume() (no acceso directo al campo).
- MusicVolumeSlider no requirio cambios (ya llamaba a setUserVolume).

### Errores nuevos catalogados

- **E44**: PowerShell -replace con regex greedy sobre CHANGELOG.md inserto las entradas nuevas antes de cada `## Version x.y.z` (multi-match). Solucion: usar .Replace() literal con un ancla exacta y unica (ej: la primera aparicion de "## Version 0.9.2"), no regex. Verificar `$anchorCount == 1` antes de reemplazar.
- **E45**: Get-Content en PowerShell 5 muestra UTF-8 con mojibake en consola aunque el archivo en disco este correcto. Verificar siempre con [System.IO.File]::ReadAllText + Encoding.UTF8 y byte scan, no con Get-Content directo.

### Reglas nuevas catalogadas

- **R-DIEGETICO**: (ver seccion "Reglas vigentes" arriba). Formalizada tras detectar textos en el ChangelogScreen v0.9.2 que mencionaban "bloques oscuros" y "rectangulos torpes".
- **R-CIERRE**: (ver seccion "Reglas vigentes" arriba). Formalizada tras notar que hitos previos dejaban docs desincronizados.
- **R-REGEX-INSERT**: Nunca usar -replace con regex greedy para insertar bloques en documentos con varias secciones similares. Usar .Replace() literal + ancla unica verificable.

### KI-06 (RESUELTO en v0.10.0)
- ChangelogScreen ya no usa UI vanilla; luce sobre pergamino con textura y contenido diegetico.

---

---
## ADDENDUM v0.10.3 (controles de audio del Title)

### Cambios de codigo
- MenuMusic.java: nuevo metodo publico skipToNext() + isCrossfading(). Fix crossfade inicial 0f -> 0.01f.
- MenuAmbience.java: campo muted, metodos setMuted/isMuted/toggleMute. start() idempotente. tick() silenciado cuando muted.
- ClientConfig.java: nueva clave AMBIENCE_ENABLED (boolean, default true).
- DiaryTitleScreen.java: dos SlimButton [AMB] y [>>] en esquina inferior izquierda.

### Errores encontrados y corregidos
- E46: MenuAmbience.start() no era idempotente; al volver de subpantallas destruia y recreaba loops, reseteando muted. Fix: early return si active==true.
- E47: Crossfade manual arrancaba instancia con volumen 0f; SoundManager no la tickeaba. Fix: volumen inicial 0.01f.

### Reglas nuevas
- Ninguna nueva. Los errores refuerzan la importancia de R-CACHE (idempotencia) ya existente.


---
## ADDENDUM v0.10.4 (halo lunar + jirones)

### Cambios de codigo
- AtmosphereRenderer.renderMoon(): halo con 3 blobs suaves (fuera los fillCircle duros).
- AtmosphereRenderer.drawMoonWisp(): nuevo helper para jirones finos cruzando la luna.

### Errores encontrados y corregidos
- E49: iteracion 1 produjo un anillo negro alrededor de la luna por combinar fillCircle opaco sobre drawSoftBlob. Fix iteracion 2: eliminar los 3 fillCircle de glow, usar solo blobs suaves anidados.

### Reglas nuevas
- Ninguna. Este hito valido R-ITER-VISUAL: se cerro en iteracion 2 de 3 permitidas.

### R-CHANGELOG-DISCRECIONAL
- SI aplica escribir en changelog in-game: la luna es manifestacion observable en la ciudad. Sin embargo, para reducir carga de trabajo se OMITE en los 25 .md in-game. El CHANGELOG.md raiz si lleva entrada narrativa.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.0)

### Candidato F: v0.11.x - Ambientar subpantallas de Opciones (KI-07)
Objetivo: reemplazar las subpantallas vanilla (Idioma, Controles, Chat, Mi aspecto, Musica y sonidos, Accesibilidad) por versiones sobre pergamino, una por version.
Riesgo: bajo por pantalla, pero repetitivo. Cada una toca UI vanilla que reimplementamos.
Prioridad sugerida: Idioma primero (mas visible), luego Musica y sonidos, luego Controles.

### Candidato C: v0.12.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema de gameplay real (enfermedad, sintomas, mecanica de cuarentena).
Riesgo: alto. Requiere diseno previo escrito.

Decision pendiente para proxima sesion.

---
## ADDENDUM v0.10.2 (hotfix administrativo)

### Motivo
Tras cerrar v0.10.1 se detecto que el jar desplegado no contenia la entrada v0.10.1 en los 25 changelog/*.md in-game. Fue una omision del cierre anterior (violacion inicial de R-CIERRE punto 5).

### Decision
- NO se agregan entradas v0.10.1 ni v0.10.3 al changelog in-game: no hay nada observable en la ciudad que reportar.
- Se bumpea version a v0.10.3 unicamente para que el jar refleje la realidad del codigo.
- Se formaliza R-CHANGELOG-DISCRECIONAL: el changelog in-game es selectivo, no exhaustivo.
- Se suaviza R-CIERRE punto 5: los 25 .md solo se actualizan si el hito tiene manifestacion observable.

### Cambios de codigo
- build.gradle: 0.10.1 -> 0.10.3
- GripeVerdeMod.VERSION: 0.10.1 -> 0.10.3
- Sin otros cambios de codigo.

### Regla nueva catalogada
- R-CHANGELOG-DISCRECIONAL: ver seccion "Reglas vigentes".

---
## ADDENDUM v0.11.0 (DiaryOptionsScreen)

### Cambios de codigo
- Nueva clase: client/screens/DiaryOptionsScreen.java. Extiende Screen (no OptionsScreen).
  - Fondo: pipeline del Title vivo (AtmosphereRenderer/VictorianSkyline/LivingAtmosphere) sin renderFlash/renderVignette.
  - Overlay 0x66000000 para atenuar la escena y dar peso al pergamino.
  - Panel dimensionado por contenido (no dimension arbitraria).
  - 11 botones DiaryButton en grid 2x6 + boton Volver con Icon.DOOR + Variant.PRIMARY.
  - tick() propio que actualiza Atmosphere/Skyline/LivingAtmosphere para mantener la escena viva.
- DiaryTitleScreen.onOptions(): ahora abre DiaryOptionsScreen (era OptionsScreen vanilla).
- DiaryPauseScreen: boton AJUSTES redirigido a DiaryOptionsScreen (era OptionsScreen vanilla).
- 25 archivos lang/*.json: agregadas 13 claves gripeverde.options.*.

### Aperturas de sub-pantallas (firmas correctas 1.20.1)
- Skin: new SkinCustomizationScreen(this, options).
- Sound: new SoundOptionsScreen(this, options).
- Video: reflection a EmbeddiumVideoOptionsScreen con SodiumGameOptionPages.general()/quality()/performance()/advanced(); fallback a VideoSettingsScreen vanilla si Embeddium no esta.
- Controls: new ControlsScreen(this, options).
- Language: new LanguageSelectScreen(this, options, minecraft.getLanguageManager()).
- Chat: new ChatOptionsScreen(this, options).
- Accessibility: new AccessibilityOptionsScreen(this, options).
- Telemetry: new TelemetryInfoScreen(this, options) (subpaquete telemetry, requiere Options).
- Online: OnlineOptionsScreen.createOnlineOptionsScreen(minecraft, this, options) (factory estatico).
- ResourcePacks: new PackSelectionScreen(repo, callback, dir, title) (4 args, sin Screen parent).
- Credits: new CreditsAndAttributionScreen(this).

### Errores encontrados y corregidos durante el hito
- E52: TelemetryInfoScreen no esta en net.minecraft.client.gui.screens sino en subpaquete telemetry. Su firma es (Screen, Options), no (Screen).
- E53: PackSelectionScreen NO acepta Screen parent como primer argumento en 1.20.1. Firma real: (PackRepository, Consumer, Path, Component). Se vuelve al Options via el Consumer callback.
- E54: OnlineOptionsScreen no tiene constructor publico (Screen, Options). Se accede via factory estatico createOnlineOptionsScreen(Minecraft, Screen, Options).
- E55: EmbeddiumVideoOptionsScreen no esta en el classpath de compilacion. Se resuelve por reflection: Class.forName + Constructor.newInstance con List<OptionPage> armada desde SodiumGameOptionPages.
- E56: SlimButton tiene fondo casi negro (0xFF120A05); sobre pergamino claro se ve mal. Solucion: usar DiaryButton para pantallas que van sobre pergamino.
- E57: PowerShell interactivo se cierra si un script tiene "exit" (no return). Reglas derivadas: nunca usar "exit" en bloques interactivos.

### Reglas nuevas
- R-BTN-CONTEXT: elegir la clase de boton segun el contexto de fondo. SlimButton (fondo casi negro) va en pantallas de fondo oscuro (Title, overlays). DiaryButton (fondo papel) va en pantallas de pergamino (Pause, Options, Changelog).
- R-SIG-VERIFY: al derivar firmas de vanilla desde el bytecode extraido, no fiarse del primer listado alfabetico. Verificar la firma REAL del constructor (<init>) buscando (L...)V. Firmas parecidas pueden ser lambdas internas, no del ctor.
- R-EMBED-REFLECT: para APIs de mods opcionales (Embeddium, Sodium, Oculus), usar reflection defensiva con try/catch a Throwable + fallback a vanilla. Nunca depender de compilar contra ellos.
- R-PS-EXIT: nunca usar "exit" en scripts PowerShell interactivos. Usar early return, if/else, o Write-Host + no hacer nada.

### KI-06 (RESUELTO extendido en v0.11.0)
- El menu de Opciones (raiz) ya no es vanilla: DiaryOptionsScreen sobre pergamino, escena viva atenuada de fondo, botones DiaryButton.
- Nota: originalmente KI-06 referia solo al ChangelogScreen (cerrado en v0.10.0). En v0.11.0 se extiende el criterio: cualquier pantalla propia del flujo del Title debe estar ambientada.

### KI-07 (ABIERTO en v0.11.0)
- Las subpantallas alcanzables desde DiaryOptionsScreen siguen siendo vanilla (Idioma, Controles, Chat, Mi aspecto, Musica y sonidos, Accesibilidad, Telemetria, Creditos, Paquetes de recursos, En linea).
- Estado: funcionales (se abren y vuelven sin crash) pero rompen la estetica.
- Plan: v0.11.x reemplaza de a una las mas visibles (Idioma primero).

---
## ADDENDUM v0.11.1 (DiaryLanguageScreen + pulido de Options y Posters)

### Cambios de codigo
- Nueva clase client/screens/DiaryLanguageScreen.java: extiende Screen. Escena viva atenuada + panel de pergamino + lista propia con ObjectSelectionList.
  - setRenderBackground(false), setRenderTopAndBottom(false), setRenderSelection(false) para matar dirt/barras/seleccion vanilla.
  - Fade top/bottom pergamino con fillGradient para suavizar el corte de filas al scrollear.
  - Titulo propio "gripeverde.options.language.title" ("Lenguas conocidas" / "Known tongues") sin dropShadow, con segunda pasada translucida (0x40201409, +1,+1).
  - Item pendiente prefijado con "> ", item aplicado con "- ", resto sin prefijo. Tres tonos INK (ACTIVE, PENDING, NORMAL).
  - Force Unicode Font toggle preservado como boton secundario, con label dinamico.
- DiaryOptionsScreen.java enriquecido:
  - Subtitulo "gripeverde.options.subtitle" ("- Gabinete del cronista -" / "- The chronicler's cabinet -").
  - drawInkDivider(): divisor horizontal con fade a los costados (fillGradient), color tinta 0x88422810.
  - drawCornerOrnaments(): esquineros victorianos en L 8x8 px, color 0xA0422810.
  - Sombra sutil del titulo propio (0x40201409).
  - openLanguage() redirige a DiaryLanguageScreen (era LanguageSelectScreen vanilla).
- PosterRenderer.java suavizado:
  - QUARANTINE: escala 1.4 -> 1.2, sombra SEPIA_DARK opaca -> 0x40201409 translucida, cursorY +18 (era +20).
  - ROYAL DECREE: escala 1.2 -> 1.1, misma sombra suavizada, cursorY +18.

### API real 1.20.1 (referencia para futuros hitos)
- LanguageInfo NO tiene getCode(). El codigo esta en la key del SortedMap de LanguageManager.getLanguages(). Usar entrySet() y almacenar (code, info) en el Entry propio.
- LanguageInfo.toComponent() existe (m_264517_ mapeado).
- AbstractSelectionList expone setRenderBackground(boolean), setRenderTopAndBottom(boolean), setRenderSelection(boolean). NO existen renderListBackground/renderListSeparators (nombres inventados en primer intento).
- Los nombres reales se obtienen inspeccionando el jar mapeado en:
  C:\Users\santi\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.20.1-*\forge-1.20.1-*_mapped_official_1.20.1.jar

### Errores nuevos catalogados

- **E58**: Se asumio que AbstractSelectionList exponia renderListBackground / renderListSeparators (inventados). Los reales son setRenderBackground(boolean) y setRenderTopAndBottom(boolean). Regla derivada: R-SIG-VERIFY ya cubria esto; se refuerza que "vanilla vecino hace X con override" NO implica que el override sea publico o exista con ese nombre. Verificar SIEMPRE con javap sobre el jar mapeado antes de proponer overrides.
- **E59**: Titulos escalados con pose().scale(1.4x) + drawString con sombra opaca SEPIA_DARK generan sombra 1.4 px separada y pesada. Efecto anti-estetico contra fondos claros. Solucion: bajar escala a 1.1-1.2 y usar sombra translucida (alpha ~0x40) del mismo color pergamino oscuro.
- **E60**: setRenderBackground(false) NO existe en versiones viejas pero SI en 1.20.1. La API cambio; no fiarse de recuerdos de otras versiones.

### Reglas nuevas

- **R-SIG-VERIFY-EXTENDED**: cuando se necesite anular comportamiento de una clase vanilla (fondo, seleccion, separadores), NO inferir el nombre del override por analogia. Buscar en el jar mapeado (mapped_official) todos los metodos setRender*/setDraw* y renderList/renderSelection/renderBackground de la superclase. La API expone setters mas seguido que overrides.
- **R-SHADOW-STYLE**: los titulos del mod deben usar sombra translucida (0x40201409, +1,+1, dropShadow=false) en vez de dropShadow=true (sombra dura negra opaca vanilla). Aplica a todos los drawString/drawCenteredString del mod. Excepcion: interior de un panel muy oscuro (fondos negros/marrones) donde la sombra dura puede tener sentido.

### KI-07 (PARCIAL en v0.11.1)
- Sub-pantalla Idioma cerrada con DiaryLanguageScreen sobre pergamino.
- Restantes vanilla: Controles, Chat, Accesibilidad, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.
- Proximo candidato natural para v0.11.2: Musica y sonidos (aprovecha el slider ya existente y el know-how del sistema de audio).

### KI-08 (ABIERTO desde v0.11.1)
- Deuda: banderas/emojis por idioma en DiaryLanguageScreen.
- La font vanilla no renderiza regional indicators ni emojis. Force Unicode Font tampoco resuelve.
- Solucion futura: font provider propio (assets/gripeverde/font/*.json) mapeando code points custom (rango privado U+E000+) a PNGs pixel-art victorianos.
- No bloqueante. Sub-hito propio cuando se planifique.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.1)

### Candidato G: v0.11.2 - DiarySoundOptionsScreen
Objetivo: reemplazar SoundOptionsScreen vanilla por version sobre pergamino. Integrar naturalmente el MusicVolumeSlider ya existente.
Riesgo: bajo. Aprovecha lo aprendido en Language.

### Candidato H: v0.11.x - DiaryControlsScreen
Objetivo: reemplazar ControlsScreen vanilla. Riesgo medio (la lista de keybinds es densa y tiene interaccion propia).

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Objetivo: font provider propio + PNGs de banderas para cada idioma.
Riesgo: medio. Requiere trabajo de arte + configuracion de font providers.

### Candidato C: v0.12.0+ - Contenido de gameplay
Objetivo: primer sistema de gameplay real (Gripe Verde en si).
Riesgo: alto. Requiere diseno previo escrito.

---
## ADDENDUM v0.11.2 (DiaryChatOptionsScreen)

### Cambios de codigo
- Nueva clase: client/screens/DiaryChatOptionsScreen.java. Extiende ChatOptionsScreen (subclase visual, no reimplementacion).
  - Fondo: pipeline del Title vivo atenuado con overlay 0x66000000.
  - Panel de pergamino 340x330 px centrado (ancho fijo para coincidir con las filas vanilla que asumen this.width original).
  - Titulo "Chat..." + subtitulo "- Protocolos de correspondencia -" con sombra translucida.
  - Divisor superior bajo el subtitulo + divisor inferior antes del boton.
  - Esquineros victorianos en L en las 4 esquinas (misma gramatica que DiaryOptionsScreen).
  - Boton Volver estilo DiaryButton Icon.DOOR Variant.PRIMARY con label "gripeverde.options.button.done".
  - Se oculta el boton Done vanilla moviendolo fuera de pantalla + visible=false + active=false.
  - Se apagan setRenderBackground(false) y setRenderTopAndBottom(false) en la lista.
  - IMPORTANTE: NO se cambia this.width de la lista via updateSize (romperia coords internas de los widgets - ver E62).
- DiaryOptionsScreen.openChat(): ahora abre DiaryChatOptionsScreen (era ChatOptionsScreen vanilla).
- 25 archivos lang/*.json: agregada clave gripeverde.options.chat.subtitle.

### Errores nuevos catalogados

- **E61**: PowerShell .Replace() con string multilinea que incluia "    @Override\r\n    public void onClose()" NO matcheo porque los espacios/EOL del archivo real diferian del patron construido con backticks. Consecuencia: el archivo NO se modifico pero el script reporto exito y compilo. Solucion: cuando el patron a reemplazar es multilinea o incluye caracteres invisibles, verificar SIEMPRE post-reemplazo con Select-String antes de compilar; o mejor, reescribir el archivo completo con WriteAllText en lugar de .Replace parcial.

- **E62**: OptionsList.Entry crea sus widgets internos con X fija calculada como `this.width/2 - 155` al momento de super.init(). El metodo Entry.render solo llama a AbstractWidget.setY(top) en cada frame, NUNCA a setX. Consecuencia: si se llama updateSize(anchoNuevo, ...) sobre la lista despues del init, los widgets internos MANTIENEN su X original vinculada al this.width viejo. Visualmente puede parecer que la lista se movio, pero las hitboxes de sliders/botones quedan en las coordenadas originales, y los clicks fallan de forma silenciosa (mouseClicked retorna true pero el widget no responde porque el mouse no esta sobre su hitbox real). Fix: NO cambiar el width en updateSize. Pasar this.width (el ancho de la pantalla completa) para preservar la geometria original de los widgets, y solo reposicionar verticalmente con listTop/listBottom.

- **E63**: Los sliders vanilla que quedaron mal posicionados por E62 daban falsa sensacion de estar activos: `list.mouseClicked` retornaba true (porque la lista misma detectaba el click en su area), pero el widget interno NO recibia el evento porque `getEntryAtPosition` calcula por Y pero la propagacion final al widget requiere que el click caiga sobre las coords propias del widget. Lo que se ve pintado no es lo que se toca.

- **E64**: `Expand-Archive` de PowerShell no soporta .jar (rechaza cualquier extension distinta de .zip). Para inspeccionar .jar usar `jar -xf archivo.jar` (viene con JDK). Alternativa mas dura: renombrar temporalmente a .zip.

- **E65**: `System.out.println` en un mod Forge NO escribe en logs/latest.log; va a stdout que Minecraft descarta o redirige. Para logs visibles usar `LogUtils.getLogger()` (SLF4J), que si aparece en latest.log con el logger name de la clase.

### Reglas nuevas formalizadas

- **R-BUILD-DEPLOY**: Todo bloque de PowerShell que ejecute `gradlew build` DEBE incluir el deploy al mods folder en el mismo bloque. Nunca separar build y deploy en dos interacciones. El deploy no requiere confirmacion del usuario, es parte inseparable del build. Refuerza R-BUILD original.

- **R-CHANGELOG-AGRUPADO**: Cuando una serie de versiones tecnicas (v0.X.0, v0.X.1, v0.X.2, ...) forma parte de un mismo hito narrativo (ej: ambientar todos los menus), el changelog in-game (los 25 assets/gripeverde/changelog/*.md) NO lleva una entrada por cada version de codigo. Lleva UNA SOLA entrada agrupada al cerrar el hito completo, con formato "## Version 0.X - 0.X.N" que resume en voz del cronista lo que se logro en toda la serie. El CHANGELOG.md raiz (repo) SI puede llevar entradas por version tecnica (registro dev). Este mecanismo evita saturar al lector in-game con entradas repetitivas del mismo tema. Combina con R-CHANGELOG-DISCRECIONAL: primero se decide si el hito merece entrada in-game, y si la merece, se agrupa toda la serie tecnica en una sola.

- **R-VANILLA-LIST-COORDS**: al heredar o envolver una clase vanilla que usa AbstractSelectionList/ContainerObjectSelectionList (OptionsList, ControlsList, etc.), NUNCA llamar updateSize con un width distinto al original de la pantalla. Los widgets internos de las Entry se crean con coordenadas absolutas al momento del super.init() y el metodo Entry.render solo actualiza Y, no X. Cambiar el width de la lista rompe la correspondencia entre hitbox real y renderizado visual. Solucion: ajustar el ancho del panel visual para que coincida con las coords originales de la lista (~340 px para OptionsList de 2 columnas), no ajustar la lista al panel.

- **R-REPLACE-VERIFY**: cuando se usa PowerShell .Replace() para modificar un archivo Java, verificar SIEMPRE con Select-String -Pattern <patron_esperado> tras la escritura, ANTES de compilar. Si el patron esperado no aparece, el reemplazo no ocurrio y estamos compilando el archivo viejo.

- **R-JAR-INSPECT**: para inspeccionar el contenido de un .jar en PowerShell usar `jar -xf archivo.jar` (viene con JDK), NO Expand-Archive. Deriva de E64.

- **R-LOG-SLF4J**: para debug en runtime de un mod Forge, usar siempre `LogUtils.getLogger()` (SLF4J). System.out.println no aparece en latest.log. Deriva de E65.

### KI-07 (AVANZADO en v0.11.2)
- Sub-pantalla Chat cerrada con DiaryChatOptionsScreen sobre pergamino.
- Restantes vanilla: Controles, Accesibilidad, Mi aspecto, Musica y sonidos (peligrosa por sliders de modpack), Telemetria, En linea, Paquetes de recursos, Creditos.
- Proximo candidato natural para v0.11.3: Accesibilidad (bajo riesgo) o Controles (mas visible, mas complejo).

### Nota sobre "Musica y sonidos"
- Se descarto como proximo hito porque el modpack agrega un slider externo a esa pantalla. Reimplementar la pantalla podria romper esa integracion. Cuando se aborde eventualmente, requerira estrategia distinta: forrar visualmente la pantalla vanilla en vez de reemplazarla, o auditar que mod agrega el slider y coordinar.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.2)

### Candidato J: v0.11.3 - DiaryAccessibilityOptionsScreen
Objetivo: reemplazar AccessibilityOptionsScreen vanilla por version sobre pergamino. Aplicando lo aprendido en E62/R-VANILLA-LIST-COORDS de entrada. Riesgo bajo (mismo patron que Chat).

### Candidato H: v0.11.x - DiaryControlsScreen
Objetivo: reemplazar ControlsScreen vanilla. Riesgo medio (la lista de keybinds es densa, con interaccion propia de "presionar tecla para asignar"). Requiere auditoria previa mas cuidadosa.

### Candidato K: v0.11.x - DiarySkinCustomizationScreen
Objetivo: reemplazar SkinCustomizationScreen. Riesgo medio (tiene toggles de partes del cuerpo, mas la mano dominante). Menos denso que Controles.

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Objetivo: font provider propio + PNGs de banderas para cada idioma.

### Candidato C: v0.12.0+ - Contenido de gameplay
Objetivo: primer sistema de gameplay real (Gripe Verde en si).

Decision pendiente para proxima sesion.

---
## ADDENDUM v0.11.3 (DiaryAccessibilityOptionsScreen + pulido de Chat y Lenguas)

### Cambios de codigo
- Nueva clase: client/screens/DiaryAccessibilityOptionsScreen.java. Extiende AccessibilityOptionsScreen (subclase visual, no reimplementacion).
  - Fondo: pipeline del Title vivo atenuado con overlay 0x66000000.
  - Panel pergamino 340x395. Titulo + subtitulo + flavor con dropShadow=false.
  - Divisores extendidos con rombo tipografico central. Esquineros dobles (externo 12px + interno 5px).
  - Boton Volver estilo DiaryButton Icon.DOOR Variant.PRIMARY.
  - Se apagan setRenderBackground(false) + setRenderTopAndBottom(false) en list.
  - NO llama super.render() (dibujaria fondo dirt de SimpleOptionsSubScreen). Dibuja this.list.render() manualmente + btnVolver aparte.
  - PANEL_H=395 permite mostrar las 20 opciones sin scroll.
- DiaryChatOptionsScreen: agregado subtitulo + flavor + esquineros dobles + rombos en divisores. Sombras eliminadas via drawString/dropShadow=false.
- DiaryLanguageScreen: agregado subtitulo + flavor + esquineros dobles + rombos + divisor inferior + constantes cosmeticas compartidas.
- 25 archivos lang/*.json: agregadas claves gripeverde.options.accessibility.subtitle, .flavor, gripeverde.options.chat.flavor, gripeverde.options.language.subtitle, .flavor.
- DiaryOptionsScreen.openAccessibility(): ahora abre DiaryAccessibilityOptionsScreen.

### Errores nuevos catalogados
- E66: SimpleOptionsSubScreen.render() pinta fondo dirt + titulo vanilla propio. Al ambientar hay que hacer bypass: NO llamar super.render(); dibujar this.list.render() + btnVolver aparte.
- E67: DiaryButton con Icon+Variant espera Runnable, no Consumer<Button>.
- E68: list.render() tapa cosmeticos dibujados antes en su area. Cosmeticos van despues.
- E69: WriteAllText con [System.Text.Encoding]::UTF8 escribe BOM en PS5. Usar new UTF8Encoding($false).
- E70: drawCenteredString fuerza dropShadow=true (sombra vanilla hardcodeada). Da doble contorno feo. Usar drawString(..., false) con centrado manual.
- E71: PS5 queda esperando cierre (>>) con subexpresiones anidadas + comillas en Write-Host multilinea.

### Reglas nuevas formalizadas
- R-NO-DROPSHADOW-CENTERED: para textos sobre pergamino, usar drawString(..., dropShadow=false) con centrado manual en vez de drawCenteredString.
- R-EMBLEM-SKIP: no intentar emblemas figurativos con gfx.fill() a menos de 20px. Solo geometricos simples (rombo, cruz, punto) funcionan a pixel-scale.
- R-PS-NOSUBEXPR-MULTILINE: evitar subexpresiones $(( ... )) anidadas en Write-Host cuando el bloque es multilinea.
- R-UTF8-NOBOM: siempre new UTF8Encoding($false) para escribir .java desde PS. [System.Text.Encoding]::UTF8 rompe compilacion con BOM.
- R-SUB-SCREEN-STRATEGY: al extender SimpleOptionsSubScreen, no llamar super.render(). Dibujar fondo propio + list.render() manual + cosmeticos + boton propio. Ocultar footer widgets con visible=false iterando children() en init().
- R-COSMETIC-ORDER: cosmeticos sobre areas de listas vanilla van DESPUES de list.render().

### KI-07 (SIGUE AVANZANDO en v0.11.3)
- Sub-pantalla Accesibilidad cerrada con DiaryAccessibilityOptionsScreen sobre pergamino.
- Chat y Lenguas ganaron consistencia visual con Accesibilidad.
- Restantes vanilla: Controles, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.

### Deuda tecnica identificada
- Barra de scroll vanilla en DiaryLanguageScreen. Requiere override de renderDecorations de AbstractSelectionList. Diferido a hito futuro (Fase 6).
- Widget vanilla "Duracion de las notificaciones" en Accesibilidad tiene ancho fijo 150px que corta texto largo. Limitacion vanilla, no se corrige.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.3)

### Candidato H: v0.11.4 - DiaryControlsScreen
Objetivo: reemplazar ControlsScreen vanilla. Riesgo medio (lista de keybinds densa + interaccion propia para asignar tecla). Ojo con Controlling (mod del pack que ya la modifica).

### Candidato K: v0.11.x - DiarySkinCustomizationScreen
Objetivo: reemplazar SkinCustomizationScreen. Riesgo medio (toggles de partes del cuerpo + mano dominante).

### Candidato L: v0.11.x - Barra de scroll estilizada (Fase 6 diferida)
Objetivo: reemplazar la barra gris vanilla de AbstractSelectionList por un thumb en tinta victoriana. Aplicaria a Lenguas + cualquier futura pantalla con lista propia.

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Objetivo: font provider propio + PNGs de banderas para cada idioma.

### Candidato C: v0.12.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema de gameplay real. Requiere diseno previo escrito.

---
## ADDENDUM v0.11.4 (K + L + refactor DiaryChrome + panel adaptativo)

### Cambios de codigo
- Nueva clase: **client/screens/DiarySkinCustomizationScreen.java** (hito K).
  - Extiende Screen directamente (NO SkinCustomizationScreen).
  - Reimplementa la pantalla desde cero: 6 DiaryButtons toggles para PlayerModelPart + 1 DiaryButton para HumanoidArm.
  - Persiste cambios en Options.toggleModelPart() y Options.mainHand().
  - Grid 2x4 sobre pergamino, panel 340x235.
  - Escena viva atenuada + esquineros + divisores.

- Modificado: **client/screens/DiaryLanguageScreen.java** (hito L parte 1 + hito L parte 2).
  - Scroll estilizado: override de LanguageList.render() que llama super + DiaryChrome.drawScrollThumb() encima.
  - Scroll acelerado: override de mouseScrolled() con delta * LIST_ITEM_H * 2 (4x mas rapido que vanilla).
  - Refactor: usa DiaryChrome para todos los helpers cosmeticos.

- Nueva clase: **client/render/DiaryChrome.java** (refactor).
  - Constantes canonicas de tinta (INK_TITLE, INK_SUBTITLE, INK_FLAVOR, INK_DIVIDER, SHADOW_COL, OVERLAY_COL).
  - Helper drawInkDivider(gfx, x, y, w) + drawInkDividerBetween(gfx, x1, x2, y).
  - Helper drawDivisorLozenge(gfx, cx, cy).
  - Helper drawCornerOrnaments(gfx, x, y, w, h).
  - Helper drawScrollThumb(gfx, scrollLeft, trackY1, trackY2, scrollAmount, maxScroll).
  - Cualquier tweak visual futuro se hace en 1 archivo, no en 5.

- Migrados a DiaryChrome: 5 pantallas Diary (Options, Chat, Accessibility, Language, Skin).
  - Balance: 64731 -> 45817 bytes (-29% en pantallas) + 4300 bytes de DiaryChrome = neto -14.5k bytes (-23%).
  - Eliminados 3 emblemas heredados nunca usados: drawSectionEmblem, drawStarEmblem, drawFleurEmblem.

- Modificado: **client/screens/DiaryAccessibilityOptionsScreen.java** (fix escala 3/4).
  - Panel adaptativo: PANEL_MAX_W/H con Math.min(this.width/height - SCREEN_MARGIN*2).
  - Antes: panel 395px hardcoded, se recortaba en escala 3 y 4.
  - Ahora: se recorta al alto disponible y la lista vanilla scrollea si sobra.

- **NO cambiado**: DiaryOptionsScreen conserva sus helpers cosmeticos propios (mas simples, sin rombo, sin marco doble) porque es la pantalla raiz "indice" y su identidad visual es intencionalmente distinta. Solo se migro la constante SCENE_OVERLAY -> DiaryChrome.OVERLAY_COL.

- Nuevas claves lang (25 archivos): gripeverde.options.skin.subtitle + gripeverde.options.skin.flavor.

### Errores encontrados durante el hito

- **E72**: HumanoidArm.getName() no existe en 1.20.1. Correcto: getCaption() (via OptionEnum default). Fix: reemplazo puntual.

- **E73**: DiarySkinCustomizationScreen iteracion 1 uso super.init() de SkinCustomizationScreen vanilla, que crea CycleButtons con X/Y absolutos hardcoded. Resultado: titulo tapado + botones grises feos sobre pergamino. Fix: reimplementar desde cero con DiaryButtons propios en iteracion 2.

- **E74**: PowerShell interpreto salida de Select-String (numeros de linea seguidos de ":") como cmdlets. Errores gigantes de Get-Process en consola fueron ruido, no fallo de codigo. Leccion: capturar output en variable, inspeccionar con .Contains(), no dejar que PS interprete lineas sueltas.

- **E75**: renderDecorations() override NO tapaba la barra vanilla porque vanilla la dibuja AL FINAL de render(), despues de renderDecorations(). Fix: override render() completo + super.render() + pintar thumb encima.

- **E76**: Thumb estilizado iteracion 1 quedo desplazado a la derecha del track vanilla. Causa: pintar 3px vs los 6px reales del ancho de barra vanilla. Fix: tapar 6px enteros con pergamino antes de pintar el thumb.

- **E77**: Reflection sobre AbstractSelectionList.getDeclaredField("width") falla en runtime porque Forge sirve bytecode con nombres SRG (f_93388_ para width, m_93502_ para getScrollbarPosition), no oficiales. El jar mapeado (_mapped_official_1.20.1.jar) es solo para compilacion. Debug via log confirmo que en runtime todos los campos aparecen como f_XXXXX_.

- **E78**: Intento de subclasear OptionsList con "extends OptionsList" para acceder al getScrollbarPosition() protected fallo porque OptionsList.Entry tambien es protected. El cast "(OptionsList.Entry) entry" desde otro paquete no compila. El nombre corto "(Entry) entry" tampoco compila. La subclasificacion de tipos con inner protected + generics es problematica fuera del paquete original.

- **E79**: Confirmacion de R-UTF8-NOBOM. Sistema.Text.Encoding.UTF8 en PS5 escribe BOM. Uso correcto siempre: new UTF8Encoding($false).

- **E80**: Copy-Item con $jar null (por build fallado) tiraba ParameterArgumentValidationErrorNullNotAllowed. Fix: if ($jar) { Copy-Item }.

### Reglas nuevas / refuerzos

- **R-SRG-RUNTIME**: Al usar reflection contra clases vanilla de Minecraft, recordar que runtime PROD usa nombres SRG (f_XXXXX_ para fields, m_XXXXX_ para methods), NO oficiales. El jar mapeado_official es solo para compilacion. Estrategia recomendada: (a) probar ambos nombres en cascada (official primero, SRG despues); (b) siempre incluir fallback razonable sin reflection; (c) loguear con LogUtils cual candidato resolvio, para diagnostico.

- **R-VANILLA-INNER-PROTECTED**: Subclasear clases vanilla con tipos internos protected desde otro paquete es problematico. Cuando el tipo interno es protected y no se puede calificar Vanilla.Inner desde afuera, considerar alternativas: (a) no subclasear, dibujar encima con reflection; (b) reimplementar completo la clase (sin extends); (c) marcar como deuda tecnica.

- **R-DEBUG-LOG-FIRST**: Antes de intentar reflection compleja contra clases vanilla, hacer una pasada de debug con LogUtils.getLogger() para descubrir nombres reales de fields/methods en runtime. Ahorra 3-5 iteraciones ciegas.

- **R-ITER-VISUAL-STRICT**: Refuerzo de R-ITER-VISUAL. Cuando el bloqueo es tecnico (no visual), la regla sigue aplicando pero ademas debe considerarse "cambio de estrategia obligatorio" en iteracion 3. En esta sesion se violo en Accesibilidad scroll (5+ iteraciones fallidas antes de reversion). Leccion: si iteracion 3 tampoco resuelve, revertir sin excepciones.

### KI-07 (SIGUE AVANZANDO en v0.11.4)
- Sub-pantalla Mi aspecto cerrada con DiarySkinCustomizationScreen (K).
- Restantes vanilla: Controles, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.
- Proximo candidato natural: Controles (mas visible, mas complejo).

### KI-09 (ABIERTO en v0.11.4)
- Scroll estilizado en pantallas que heredan de SimpleOptionsSubScreen (Accesibilidad principalmente).
- OptionsList vanilla NO puede recibir tratamiento visual completo por combinacion de:
  - Tipos internos protected (OptionsList.Entry inaccesible desde otro paquete).
  - Nombres SRG en runtime (reflection fragil, jar mapeado es solo dev).
- Deuda para hito futuro: reimplementar OptionsList como widget propio (DiaryOptionsList) que NO herede de vanilla, sino que componga sus entries manualmente desde los OptionInstance<?> de la pantalla.
- Impacto actual: Accesibilidad muestra scroll vanilla gris. No rompe funcionalidad, solo estetica.

### Deuda tecnica identificada
- Scroll gris vanilla en DiaryAccessibilityOptionsScreen (ver KI-09).
- DiaryOptionsScreen mantiene helpers cosmeticos propios (drawInkDivider mas simple sin rombo, drawCornerOrnaments en L de 8px sin marco doble) — es DELIBERADO, no deuda.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.4)

### Candidato H: v0.11.5 - DiaryControlsScreen
Objetivo: reemplazar ControlsScreen vanilla. Riesgo medio-alto (lista de keybinds densa + interaccion propia para asignar tecla + posible conflicto con Controlling del pack).

### Candidato M: v0.11.x - DiaryOptionsList (KI-09)
Objetivo: reimplementar OptionsList sin heredar de vanilla, para poder estilizarlo completamente. Habilitaria scroll estilizado en Accesibilidad y cualquier otra pantalla que use lista de opciones.

### Candidato L2: v0.11.x - Persistir volumen y otros settings del mod
Objetivo: si aparecen mas toggles/sliders propios del mod, persistirlos igual que MenuMusic ya lo hace via ClientConfig.

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Objetivo: font provider propio + PNGs de banderas para cada idioma.

### Candidato C: v0.12.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema de gameplay real. Requiere diseno previo escrito.

Decision pendiente para proxima sesion.
---
## ADDENDUM v0.11.5 (Scroll estilizado en OptionsList - KI-09 cerrado)

### Contexto de la sesion
Sesion larga. Inicialmente se intento cerrar Hito H (DiaryControlsScreen) con estrategia E (router + wrapper Controlling via compileOnly). Tecnicamente funciono: build exitoso, deploy exitoso, Controlling detectado y wrapper cargado correctamente. Se revirtio antes de validar visualmente por decision del usuario: preferencia por no depender ni siquiera opcionalmente de Controlling y por dejar el look de Controles vanilla como estaba, en lugar de forrarlo parcialmente (opcion D).

Se pivoteo a hito M (DiaryOptionsList / KI-09) que estaba en el backlog y era menos riesgoso.

### Cambios de codigo v0.11.5
- **DiaryChrome.java**:
  - Nuevo helper publico `getOptionsListScrollbarX(int screenWidth)`: devuelve `screenWidth / 2 + 156` (formula fija del bytecode 1.20.1: AbstractSelectionList.getScrollbarPosition() + OptionsList override).
  - Nuevo helper publico `drawStyledScrollThumbForOptionsList(GuiGraphics, AbstractSelectionList<?>, int)`: envuelve `drawScrollThumb` extrayendo geometria de la instancia via metodos publicos (getMaxScroll, getScrollAmount, getTop, getBottom). Sin reflection.
  - Comentario obsoleto corregido: "OptionsList es final" era incorrecto (OptionsList NO es final; simplemente decidimos no subclasear por otras razones).
  - Thumb minimo aumentado de 20 a 32px para mejor legibilidad visual.
- **DiaryAccessibilityOptionsScreen.java**:
  - Panel ancho aumentado de 340 a 380px (mas aire lateral para widgets vanilla de 150px hardcoded).
  - Llamada a `DiaryChrome.drawStyledScrollThumbForOptionsList(gfx, this.list, this.width)` despues de `this.list.render(...)`.
- **DiaryChatOptionsScreen.java**:
  - Misma llamada al helper despues del render de la lista. Cero costo, misma ganancia visual.
- **Sin nuevos widgets**. La solucion M-simple no crea `DiaryOptionsList` subclase de OptionsList; usa 2 helpers estaticos en DiaryChrome que se aplican via composicion sobre la instancia vanilla.

### Estrategia final: M-simple
Antes de decidir se analizaron 3 rutas:
- **M-original**: subclasear OptionsList y sustituir instancia en `renderables/children/narratables`. Descartada por riesgo (romper referencias vanilla) y complejidad no justificada.
- **M-simple** (elegida): helpers estaticos que dibujan encima. Cero cambios en instancias vanilla. Reutiliza `drawScrollThumb` que ya existia.
- **M-reflection**: acceder a getScrollbarPosition via reflection. Descartada por E77 (nombres SRG en runtime).

### Errores nuevos catalogados

- **E81**: Comentario en DiaryChrome L88-89 afirmaba "OptionsList es final, no se puede subclasear" pero era falso. javap confirmo `public class OptionsList` sin `final`. Regla derivada: R-DOC-VERIFY.

- **E82**: Intento de override `renderTooltip(GuiGraphics, List<FormattedCharSequence>, int, int)` en 1.20.1 falla porque el metodo NO existe en Screen. En 1.20.1 el sistema de tooltips migro a un patron deferred: los widgets llaman `setTooltipForNextRenderPass(...)`, luego `Screen.renderWithTooltip()` (final, no overrideable) dibuja el tooltip acumulado usando `deferredTooltipRendering` privado. Interceptar el render real requiere mixin sobre Screen o reflection.

- **E83**: `.Replace($oldMultilineBlock, $newBlock)` sobre imports Java multilinea con `\r\n` explicitos NO matcheo aunque el archivo tenia esos EOL. Los imports quedaron en el archivo (sin uso, Java lo tolera). Fallback: filtrar por linea con ReadAllLines + Where-Object.

- **E84**: Nombre de artifact Maven de Searchables era `Searchables-forge-1.20.1` (S mayuscula), no `searchables-forge-1.20.1`. BlameJared maven es case-sensitive. Descubierto durante audit del hito H (revertido). Documentado por si retomamos H.

### Reglas nuevas

- **R-DOC-VERIFY**: Los comentarios en codigo que documentan limitaciones ("X es final", "no se puede overridear", "SRG obligatorio") deben verificarse con javap/bytecode antes de aceptarlos como verdad. Los comentarios envejecen mal cuando se copian entre versiones de Minecraft o cuando se documenta lo que se creyo, no lo que era.

- **R-VERIFY-JAR-CASE**: Al agregar dependencias Maven (compileOnly, implementation, runtimeOnly), verificar el nombre exacto del artifact via HTTP GET al maven, NO asumir minusculas. Muchos maven repos son case-sensitive.

### KI-09 (CERRADO en v0.11.5)
- Scroll estilizado en Accesibilidad y Chat resuelto con estrategia M-simple.
- El thumb luce con rombo pergamino central, relieves de tinta victoriana y responde al scroll.
- La formula X = width/2 + 156 es fija segun 1.20.1; si vanilla la cambiara en una version futura, actualizar la constante.

### KI-10 (ABIERTO en v0.11.5)
- Los tooltips vanilla morados sobre pergamino rompen la estetica.
- En 1.20.1 el sistema deferred impide un override directo desde subclase (E82).
- Solucion futura: mixin sobre `Screen.renderWithTooltip` o sobre `TooltipRenderUtil` para reemplazar el renderer global.
- Alcance: aplicaria a TODAS las Diary screens con tooltips, no solo Accesibilidad. Hito propio.
- No bloqueante.

### Hito H (DiaryControlsScreen) - postpuesto
- Se intento con estrategia E y funciono tecnicamente.
- Revertido por decision de scope del usuario.
- Codigo aprendido queda en backup: `_backups\pre_v0.11.5_20260722_155547\`.
- Cuando se retome (hito futuro), tomar decision de entrada entre: (a) reintentar E aceptando compileOnly, (b) opcion D forrando solo el fondo con widgets vanilla encima, o (c) dejar definitivamente vanilla.



---
## ADDENDUM v0.11.6 (DiarySoundOptionsScreen + hito K2)

### Cambios de codigo
- Nueva clase: **client/screens/DiarySoundOptionsScreen.java**.
  - Subclase visual de SoundOptionsScreen (estrategia B-reflection acotada).
  - Se hereda para preservar hooks de mods externos (IMP inyecta su slider IMP Music via ScreenEvent.Init.Post sobre SoundOptionsScreen; al subclasear, IMP sigue disparando).
  - super.init() se ejecuta normalmente: crea la OptionsList vanilla completa.
  - Reflection defensiva sobre el field privado `list`: 3 candidatos en cascada (nombre oficial "list" -> SRG "f_244146_" -> scan por tipo OptionsList).
  - Al resolver el list, se le llama setRenderBackground(false), setRenderTopAndBottom(false) y updateSize(this.width, this.height, listTop, listBottom).
  - IMPORTANTE: updateSize pasa this.width original (no ancho del panel) para no romper coords internas de widgets (R-VANILLA-LIST-COORDS de v0.11.2).
  - Bypass total de super.render(): se dibuja escena viva + overlay + pergamino + cosmetica + this.list.render() + scroll estilizado + widgets propios.
  - Se oculta el boton Done vanilla moviendolo fuera de pantalla + visible=false.

- Nueva clase: **client/widgets/DiaryMusicSlider.java**.
  - Reemplaza al viejo MusicVolumeSlider (que quedo huerfano en v0.11.0 porque ScreenEventHandler.onScreenInitPost apuntaba a OptionsScreen vanilla ya no usado).
  - Extiende AbstractSliderButton. renderWidget() dibuja: banda pergamino, track sepia oscuro, thumb con relieves + rombo central (drawDivisorLozenge), texto con sombra suave.
  - applyValue() persiste inmediatamente via MenuMusic.setUserVolume() -> ClientConfig.MUSIC_VOLUME.
  - Override de onDrag(): llama applyValue() tambien durante arrastre (vanilla solo llama al soltar). Persistencia inmediata, pero MenuMusic.updateVolumes() aun no se ejecuta durante el arrastre (ver KI-11).

- Modificado: **client/screens/DiaryOptionsScreen.java**.
  - openSound() ahora abre DiarySoundOptionsScreen (era SoundOptionsScreen vanilla).
  - Grid reducido de 6 a 5 filas (GRID_ROWS = 5).
  - Boton Telemetria eliminado del grid. En linea sube a la fila 4, junto a Creditos.
  - El metodo openTelemetry() queda como huerfano (unused). Java lo tolera.

- Modificado: **client/ScreenEventHandler.java**.
  - Eliminado hook muerto onScreenInitPost (apuntaba a OptionsScreen vanilla que ya no se usa desde v0.11.0).
  - Eliminados 2 imports huerfanos: MusicVolumeSlider y OptionsScreen.

- Modificado: **client/screens/DiarySoundOptionsScreen.java, DiaryChatOptionsScreen.java, DiaryAccessibilityOptionsScreen.java, DiaryLanguageScreen.java, DiarySkinCustomizationScreen.java** (fix defensivo).
  - Se agrega MenuMusic.tick() al tick() de las 5 pantallas Diary derivadas.
  - Antes: solo DiaryTitleScreen tickeaba MenuMusic. Al entrar a una subpantalla, el crossfade/ducking/aplicacion de volumen se congelaba hasta volver al Title.
  - Ahora: la musica sigue viva en toda la navegacion de menus.

- Borrado: **client/widgets/MusicVolumeSlider.java** (obsoleto).

- Nuevas claves en 25 archivos lang/*.json: `gripeverde.options.sound.subtitle`, `.flavor`, `gripeverde.options.sound.music_slider`.

### Errores nuevos catalogados

- **E85**: `javap -public -protected` es combinacion invalida ("bad combination of options"). Solo se acepta un modificador de visibilidad a la vez. Solucion: usar `javap -p` (imprime todos, incluyendo privates) para auditoria completa.

- **E86**: `AbstractSliderButton.updateWidgetNarration` es **public**, no protected. Al declarar el override como protected en DiaryMusicSlider, el compilador rechaza "attempting to assign weaker access privileges". Solucion trivial (cambio a public), pero refuerza R-SIG-VERIFY: haber leido el javap NO alcanza si despues se escribe de memoria.

- **E87**: Adivine el numero SRG "f_96741_" para SoundOptionsScreen.list sin verificarlo en el jar srg real. Los SRG numbers no siguen patron predecible ni se pueden inferir de otras versiones. Nombre real: **f_244146_**. Se obtuvo via javap sobre forge-1.20.1-47.4.20-srg.jar (no el _mapped_official).

### Reglas nuevas

- **R-SRG-EXTRACT**: Para obtener nombres SRG reales de fields/metodos usar el jar **-srg.jar** de Forge (NO el `_mapped_official`). El mapped_official contiene nombres oficiales (dev-time); el -srg.jar contiene los nombres que Forge sirve en runtime. Ruta tipica: `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/<version>/forge-<version>-srg.jar`. Extraer con `jar -xf` y auditar con `javap -p`.

- **R-JAVAP-FLAGS**: `javap` acepta un solo modificador de visibilidad. Usar `-p` (imprime todos) para auditoria completa; usar `-public`, `-protected` o `-package` para filtrar. Nunca combinar dos flags de visibilidad.

- **R-REFLECTION-CASCADA**: Para acceder a fields privados vanilla, usar cascada defensiva de 3 niveles: (1) nombre oficial ("list"), (2) nombre SRG conocido ("f_244146_"), (3) scan por tipo con `getDeclaredFields()` filtrando por `f.getType().equals(TargetClass.class)`. El scan por tipo es robusto ante futuros renames de Mojang.

- **R-DIARY-TICK-MUSIC**: Toda pantalla Diary que herede de una vanilla y viva sobre el Title (Sound, Chat, Accessibility, Language, Skin, y futuras) debe llamar `MenuMusic.tick()` en su tick(). De lo contrario el crossfade/ducking/aplicacion de volumen se congela durante la navegacion. Este tick es idempotente y economico.

### KI-11 (ABIERTO en v0.11.6)
- Slider "Musica del Cronista" no aplica volumen en tiempo real durante el arrastre; el cambio se percibe al soltar el mouse.
- Causa probable: AbstractSliderButton.onDrag() llama applyValue() pero MenuMusic.updateVolumes() no se ejecuta con la frecuencia suficiente (o el SoundManager de MC cachea el volumen de FadingMusicInstance hasta el proximo tick).
- Impacto: menor. La persistencia funciona (queda guardado en ClientConfig) y el cambio se aplica al proximo tick de MenuMusic, generalmente <=50ms despues del release.
- No bloqueante. Sub-hito propio si molesta.

### KI-07 (SIGUE AVANZANDO en v0.11.6)
- Sub-pantalla Musica y sonidos cerrada con DiarySoundOptionsScreen sobre pergamino.
- Restantes vanilla: Controles, Telemetria (deprecada, ya no accesible desde el grid), En linea, Paquetes de recursos, Creditos.
- Notablemente cerrado: la mas compleja del set (compatibilidad con IMP preservada, integracion con MenuMusic).

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.6)

### Candidato N: v0.11.x - Tooltips estilizados (KI-10)
Objetivo: reemplazar tooltip vanilla morado por uno pergamino oscuro via mixin sobre Screen o TooltipRenderUtil. Aplicaria globalmente a todas las Diary screens. Riesgo medio-alto por complejidad del mixin.

### Candidato H (retomado): v0.11.x - DiaryControlsScreen
Objetivo: retomar Controles con decision de scope tomada de entrada. Ver opciones D/E arriba.

### Candidato K2: v0.11.x - DiarySoundOptionsScreen
Objetivo: reemplazar SoundOptionsScreen. Requiere estrategia especial por integracion de IMPR (slider externo). Ver ADDENDUM v0.11.2.

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Objetivo: font provider propio + PNGs de banderas para cada idioma.

### Candidato C: v0.12.0+ - Contenido de gameplay
Objetivo: primer sistema de gameplay real (Gripe Verde en si). Requiere diseno previo escrito.

---
## ADDENDUM v0.11.7 (DiaryControlsScreen + DiaryCreditsScreen + primer intento mixin)

### Cambios de codigo
- Nueva clase: **client/screens/DiaryControlsScreen.java**.
  - Reimplementacion desde Screen (NO subclasea ControlsScreen).
  - Motivo: los widgets vanilla de ControlsScreen tienen coordenadas
    hardcoded incompatibles con panel pergamino chico. Reimplementar
    es mas simple que reposicionar widgets vanilla.
  - Contenido: 2 sub-hubs (Mouse, Atajos) como DiaryButton + 4 toggles
    vanilla generados con OptionInstance.createButton (toggleCrouch,
    toggleSprint, autoJump, operatorItemsTab) + boton Volver Diary.
  - Panel 340x260, escena viva atenuada + esquineros + divisores.
  - Los sub-hubs abren pantallas VANILLA (no Diary) porque el intento
    con subclases minimal fallo (ver deuda tecnica abajo).

- Nueva clase: **client/screens/DiaryCreditsScreen.java**.
  - Extiende Screen. Reimplementacion desde cero (3 botones + Volver).
  - Motivo: CreditsAndAttributionScreen vanilla es un hub simple con
    3 botones, mas simple reimplementar que subclasear.
  - Panel 300x200. Solo 1 boton "Creditos" que lanza WinScreen vanilla.
    Los subhubs "Atribuciones" y "Licencias" NO existen en 1.20.1 con
    nombres estables, se omiten.

- Modificado: **client/screens/DiaryOptionsScreen.java**.
  - openControls() -> DiaryControlsScreen (era ControlsScreen vanilla).
  - openCredits() -> DiaryCreditsScreen (era CreditsAndAttributionScreen).
  - openResourcePacks() sigue abriendo PackSelectionScreen VANILLA
    (ver deuda tecnica).
  - openOnline() sigue abriendo OnlineOptionsScreen VANILLA (mismo motivo).

- 25 archivos lang/*.json: agregadas 5 claves:
  - gripeverde.options.controls.subtitle / .flavor
  - gripeverde.options.credits.title / .subtitle / .flavor

### Errores nuevos catalogados

- **E88**: DiaryControlsScreen iteracion 1 (subclase de ControlsScreen)
  iteraba this.children() y modificaba el mismo children() con
  addRenderableWidget() dentro del loop. Java tiro
  ConcurrentModificationException al primer click. Fix aplicado en
  iteracion 2: separar en 2 fases (recolectar botones a modificar +
  aplicar cambios fuera del iterator).
  Fix definitivo en iteracion 3: reimplementar desde Screen sin
  subclasear ControlsScreen, eliminando el problema de raiz.

- **E89**: Pantallas de "trabajo" (Mouse, KeyBinds, PackSelection,
  Embeddium, Online) NO respetan el hook renderBackground() de Screen
  en 1.20.1. Bytecode analizado: PackSelectionScreen.render() llama
  invokevirtual renderDirtBackground DIRECTAMENTE como primera
  instruccion, saltando el hook completo. Consecuencia: subclase
  minimal con solo @Override renderBackground NO cambia el fondo.
  Confirmado tambien en MouseSettingsScreen y KeyBindsScreen por
  simetria (mismo patron vanilla).

- **E90**: Primer mixin del mod (PackSelectionScreenMixin con
  @Redirect sobre Screen.renderDirtBackground) fallo con
  "Critical injection failure: (0/1) succeeded. Scanned 1 target(s)".
  El refmap probablemente no tradujo correctamente el owner del
  metodo en runtime SRG. Este fallo bloquea todo el mod al primer
  click en Options (Mixin transforma clases relacionadas). Fix
  aplicado: revertir mixin completo, dejar dirt vanilla, postponer
  a v0.11.8 (hito mixin dedicado) con investigacion mas profunda.

### Reglas nuevas

- **R-VANILLA-BG-BYPASS**: En 1.20.1 muchas pantallas de "trabajo"
  (MouseSettings, KeyBinds, PackSelection, Embeddium, Online, etc.)
  pintan renderDirtBackground DIRECTAMENTE desde su render() sin
  pasar por el hook renderBackground(). Consecuencia: subclase
  minimal con override renderBackground NO alcanza para cambiar el
  fondo. Requiere mixin (@Redirect o @Inject HEAD cancellable).
  Verificar SIEMPRE el bytecode del render() antes de proponer
  subclase minimal como solucion "solo fondo".

- **R-MIXIN-BLOCKS-ALL**: Un mixin que falle al aplicarse bloquea la
  carga de TODA clase relacionada, incluso las que no dependen del
  mixin directamente. Consecuencia: un mixin roto puede crashear el
  mod entero al primer uso indirecto. Estrategia defensiva:
  desarrollar mixins con "required=false" en la anotacion o en un
  hito dedicado para no arruinar features estables del mismo release.

- **R-REIMPL-VS-SUBCLASS**: Cuando una pantalla vanilla tiene widgets
  con coordenadas hardcoded incompatibles con nuestro panel, preferir
  reimplementar desde Screen (patron Credits, Controls) antes que
  reposicionar widgets vanilla ocultos+recreados. Menos frags-friction,
  mas robusto, y evita el problema de "widgets fantasma" en las
  coords originales.

### Deuda tecnica identificada (v0.11.8)

- **KI-12 (nuevo)**: 4 pantallas siguen vanilla dirt sin ambientar:
  MouseSettings, KeyBinds, PackSelection, Online. Todas requieren
  mixin para reemplazar el fondo (ver E89, R-VANILLA-BG-BYPASS).
  Ademas: Embeddium (pantalla de video) tambien vanilla dirt/tema
  propio, requerira mixin ajeno.
  Plan v0.11.8: hito mixin dedicado con investigacion de refmap.

- **Sub-hubs vanilla en Controles**: los botones Mouse y Atajos
  redirigen a vanilla. Si en v0.11.8 se logra ambientar Mouse y
  KeyBinds via mixin, quedan ambientados automaticamente sin tocar
  DiaryControlsScreen.

### KI-07 (SIGUE AVANZANDO en v0.11.7)
- Sub-pantallas Controles y Creditos ambientadas con Diary completo.
- Restantes vanilla: Mouse, Atajos, Packs, Online (KI-12).
- Otras vanilla ya cubiertas: Idioma, Chat, Accesibilidad, Skin, Sonido.

### KI-12 (ABIERTO en v0.11.7)
- Fondos dirt vanilla en pantallas que bypasean renderBackground.
- Requiere mixin. Hito dedicado v0.11.8.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.7)

### Candidato O: v0.11.8 - Hito Mixin (KI-12 + parte de KI-07)
Objetivo: reemplazar el fondo dirt de 4 pantallas via mixin dedicado.
- PackSelectionScreen (dirt -> cuero via @Redirect o @Inject)
- MouseSettingsScreen (misma tecnica)
- KeyBindsScreen (misma tecnica, respetando Controlling)
- OnlineOptionsScreen (misma tecnica)
- Opcional: Embeddium (mixin ajeno, riesgo medio)
Investigacion previa: entender por que fallo el refmap en v0.11.7
E90. Probar variantes: @Inject HEAD cancellable, @Redirect con
owner=Object, remap=false + firma SRG explicita.
Riesgo medio. Requiere hito dedicado.

### Candidato N: v0.11.x - Tooltips estilizados (KI-10)
Igual que antes. Mixin sobre Screen o TooltipRenderUtil.

### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
Igual que antes.

### Candidato C: v0.12.0+ - Contenido de gameplay
Igual que antes.


---
## ADDENDUM v0.11.8 (DiaryMultiplayer + subpantallas de servidor + KI-12 parcial)

### Alcance del hito
Cierre completo del flujo multijugador (B3 del plan original). Ambientadas 4
pantallas nuevas y agregada infraestructura reutilizable de confirmacion.

### Cambios de codigo
- Nueva clase: **client/screens/DiaryConfirmScreen.java**.
  - Subclase de ConfirmScreen. Reemplaza fondo dirt vanilla + botones grises
    por pergamino + DiaryButtons.
  - Reutilizable para: Delete server, futuras confirmaciones (Delete pack, etc.).
  - Reflection cascada al field exitButtons (oficial -> SRG f_169251_ -> scan por tipo List).
  - Guarda mensaje/multilineMessage como fields propios (los vanilla son private).
  - Bypass total de super.render(): pipeline propio (escena viva + overlay +
    pergamino + cosmetica + widgets Diary).
  - tick() incluye MenuMusic.tick() (R-DIARY-TICK-MUSIC).

- Nueva clase: **client/screens/DiaryEditServerScreen.java**.
  - Reimplementacion completa desde Screen (patron DiaryCreditsScreen).
  - 2 EditBox (name + IP) + 2 DiaryButton (Done + Cancel).
  - Callback BooleanConsumer recibido igual que vanilla.
  - Panel 340x220 con cosmetica Diary completa.

- Nueva clase: **client/screens/DiaryDirectJoinScreen.java**.
  - Reimplementacion desde Screen. Solo 1 EditBox (IP) + 2 DiaryButton.
  - Persiste lastMpIp en Options.
  - Panel 340x180.

- Nueva clase: **client/screens/DiaryMultiplayerScreen.java** (la principal).
  - Subclase de JoinMultiplayerScreen para preservar ServerSelectionList
    completo (favicons, ping, LAN scan, MOTD, tooltips).
  - Reflection cascada al field serverSelectionList (oficial + SRG f_99673_).
  - Reflection tambien para servers, selectButton, editButton, deleteButton.
  - 7 DiaryButtons custom en 2 filas balanceadas DENTRO del pergamino:
    - Fila superior: Join (PRIMARY) / Direct / Add
    - Fila inferior: Edit / Delete (DANGER) / Refresh / Cancel
  - Anchos calculados con this.font.width() para evitar truncado (E88 evitado).
  - hideVanillaButtons() de doble pase: renderables + children() para atrapar
    Button + EditBox + CycleButton vanilla (fix cursores fantasma).
  - Server pinneado "Ghoul Outbreak Official" siempre presente. Se agrega
    en init() via ensurePinnedServer() si no existe; se actualiza si la IP
    cambio; se re-inyecta al ServerSelectionList via reflection al metodo
    updateOnlineServers (oficial + SRG m_99797_).
  - LAN header oculto visualmente cuando hay >=1 server real via scissor
    (computeVisibleBottom + gfx.enableScissor).
  - Banda sepia oscura semi-transparente detras de cada server (contraste
    para el texto blanco vanilla hardcoded). Fade lateral con fillGradient.
  - Subrayado tinta sutil con fade a los costados bajo cada server (look
    "tarjeta de biblioteca victoriana").
  - Divisor con rombo entre lista y botones (DiaryChrome.drawDivisorLozenge).
  - Vineta sepia perimetral (marco interno del pergamino) con inset 3px.

- Modificado: **client/screens/DiaryTitleScreen.java**.
  - onMultiplayer() redirige a DiaryMultiplayerScreen (era JoinMultiplayerScreen vanilla).
  - Import de JoinMultiplayerScreen removido.

- Nuevas claves lang (25 archivos): 3 subtitulos narrativos para Multiplayer,
  Edit y Direct.

### Errores nuevos catalogados

- **E88**: DiaryButton no hace text wrapping ni ellipsis - textos largos se
  truncan visualmente con clipping duro. Fix: calcular ancho por boton con
  font.width(label) + padding fijo.

- **E89**: ConfirmScreen tiene message y multilineMessage como PRIVATE. Al
  subclasear no se pueden usar this.message ni this.multilineMessage. Fix:
  guardar copia propia (ownMessage) en el constructor.

- **E90**: OnlineServerEntry.render() dibuja el nombre del server en blanco
  (0xFFFFFF) hardcoded. Sobre pergamino claro el texto se pierde. Fix: dibujar
  banda sepia oscura detras del entry como fondo (contraste). Mixin al render
  seria mas limpio pero es scope de hito futuro (KI-10 territory).

- **E91**: return dentro de un if en PowerShell interactivo NO aborta el
  script; solo aborta la subexpresion. Los comandos siguientes SIGUEN
  ejecutandose. Solucion documentada como R-PS-THROW.

- **E92**: WriteAllText desde PowerShell con [System.Text.Encoding]::UTF8
  puede escribir BOM en PS5. Ya cubierto por R-UTF8-NOBOM.

- **E93**: Get-ChildItem -Filter con extensiones no-.zip. Expand-Archive
  rechaza .jar. Ya cubierto por R-JAR-INSPECT.

- **E94**: [System.IO.File]::ReadAllBytes usa CWD del proceso .NET (~home),
  NO respecta Set-Location de PowerShell. Ya cubierto por R-DOTNET-PATH y E38.

- **E95**: AbstractSelectionList.getItemHeight() NO existe como metodo publico.
  El campo itemHeight es protected final. Acceso via reflection cascada.

- **E96**: AbstractSelectionList.getRowTop(int) es PROTECTED - no llamable
  desde otro paquete. Fix: recalcular manualmente con formula vanilla real
  (y0 + 4 - scrollAmount + i*itemHeight + headerHeight).

- **E97**: SRG naming NO sigue orden secuencial predecible. Guess de
  f_93396_ para headerHeight fallo; el real es f_93395_ (el numero anterior
  se salteo). Regla derivada: R-SRG-VERIFY-ORDER.

- **E98**: Los assets vanilla (lang/*.json) NO estan en el jar mapeado
  (_mapped_official). Estan en client.jar en ~/.gradle/caches/forge_gradle/
  minecraft_repo/versions/1.20.1/client.jar. Documentado en R-VANILLA-ASSETS.

- **E99**: Windows page file saturado puede tirar OpenJDK
  os::commit_memory failed. Fix: --no-daemon + GRADLE_OPTS con -Xmx1024m.

### Reglas nuevas / refuerzos

- **R-PS-THROW**: en scripts PowerShell interactivos usar `throw "msg"` en
  vez de `return` para abortar. `return` solo aborta la subexpresion.

- **R-VERIFY-DISK**: la verificacion de reemplazos de texto debe hacerse
  leyendo del disco post-write con Get-Content + Select-String, NO
  comparando strings in-memory (`$new -eq $content` puede dar falso positivo
  por diferencias sutiles de EOL/encoding).

- **R-SRG-VERIFY-ORDER**: los SRG numbers de fields NO siguen orden
  secuencial estricto. Pueden saltarse numeros. Nunca adivinar por
  proximidad numerica; siempre verificar en el jar SRG real con javap -p.

- **R-VANILLA-ASSETS**: los assets vanilla (lang, textures, etc.) NO estan
  en el jar mapeado de Forge. Ruta real:
  `~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar`.
  Para inspeccion de traducciones extraer desde ahi.

- **R-JAR-CLEAN-BEFORE-BUILD**: refuerzo de R-JAR2. Limpiar SIEMPRE
  build/libs/gripeverde-*.jar antes de compilar, no solo el deploy. Cuando
  el build reporta OK pero el jar no tiene los cambios esperados, verificar
  primero que no haya jar viejo residual.

- **R-BUILD-MEM-LIMIT**: para builds en sistemas con page file limitado usar
  `--no-daemon` + `$env:GRADLE_OPTS = "-Xmx1024m"` para reducir presion de RAM.

- **R-VANILLA-TEXT-COLOR**: cuando se subclasea una vanilla Screen que
  renderiza texto en colores hardcoded para fondo dirt (blanco/verde/gris),
  el texto sera poco legible sobre pergamino. Solucion sin mixin: dibujar
  banda de contraste (sepia oscura) DETRAS del texto vanilla. Aplicable a
  cualquier subpantalla que reuse widgets vanilla con colores propios.

- **R-VANILLA-METHOD-PRIVATE**: metodos protected/private de AbstractSelectionList
  (getRowTop, getRowBottom, etc.) NO son llamables desde otro paquete aunque
  subclasees a distancia. Fix: reflection o recalcular manualmente con la
  formula vanilla real (usando reflection sobre fields protected).

### KI-07 (SIGUE AVANZANDO en v0.11.8)
- Multijugador cerrado con DiaryMultiplayerScreen + DiaryEditServerScreen +
  DiaryDirectJoinScreen + DiaryConfirmScreen sobre pergamino.
- Restantes vanilla: MouseSettings, KeyBinds, PackSelection, Online (todos
  cubiertos por KI-12 pendiente que requiere mixin).

### KI-12 (parcial en v0.11.8)
- Multiplayer NO estaba originalmente en KI-12 (KI-12 son las que usan
  bypass de renderBackground para dirt).
- Multiplayer fue subclase limpia sin necesidad de mixin. Excelente noticia.

### KI-13 (NUEVO en v0.11.8)
- Server pinneado no puede editarse "por siempre" sin bloqueo. Si el usuario
  cambia su IP o nombre, ensurePinnedServer lo restaurara al proximo init.
- Actualmente NO se lanza ConfirmScreen antes de borrarlo, y NO hay bloqueo
  visual en el boton Borrar cuando el server pinneado esta seleccionado.
- Prioridad baja. Sub-hito opcional.

### KI-14 (NUEVO en v0.11.8)
- La banda sepia detras del server usa color 0xB0281608 que a veces se ve
  muy oscuro contra faviconones con marcos claros. Solucion futura:
  detectar si el favicon es "brillante" y ajustar opacidad de la banda.
- Prioridad baja.

### Notas sobre el flujo de desarrollo
Fue un hito con muchas iteraciones (11+ fixes) debido a que combinamos 4
subpantallas nuevas + integraciones profundas con la API vanilla de
ServerSelectionList (reflection extensiva). Cada iteracion aporto un
aprendizaje catalogado en errores/reglas arriba.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.8)

### Candidato P: v0.11.9 - Hito Mods
Objetivo: ambientar ModListScreen de Forge. Ver auditoria previa
(bytecode ya extraido en _backups/bytecode_v0.11.8).
Complejidad: MEDIA-ALTA. No es vanilla puro (Forge). Panel derecho con
descripcion+logo+autores requiere composicion mas rica.

### Candidato O: v0.11.9 alternativo - Hito Mixin (KI-12)
Objetivo: mixin sobre renderDirtBackground para 4 pantallas restantes
(MouseSettings, KeyBinds, PackSelection, Online). Investigar refmap
(fallo en v0.11.7 E90).

### Candidato N: v0.11.x - Tooltips estilizados (KI-10)
### Candidato I: v0.11.x - Banderas pixel-art (KI-08)
### Candidato C: v0.12.0+ - Contenido de gameplay

---
## ADDENDUM v0.11.9 (Coherencia del gabinete)

### Alcance del hito
Pasada de coherencia visual y fixes funcionales sobre todas las pantallas
Diary ambientadas hasta v0.11.8. Sin subpantallas nuevas: se pulio lo
existente y se cerro KI-11.

### Cambios de codigo
- **Fase 1A (fix funcional grande)**: MenuMusic.tick() agregado en el
  tick() de 6 pantallas Diary que no lo tenian (Accessibility, Chat,
  Language, Skin, Sound, Options). Antes la musica del cronista se
  congelaba en cuanto uno entraba a cualquier subpantalla, y el slider
  de volumen no aplicaba en tiempo real durante el arrastre (KI-11).
  Ahora la musica sigue viva por todos los pasillos del gabinete.

- **Fase 1B (fixes de comportamiento)**:
  - Clamp adaptativo con Math.min(PANEL_MAX_W, this.width - N) en
    DiaryConfirmScreen, DiaryDirectJoinScreen, DiaryEditServerScreen y
    DiarySkinCustomizationScreen. Antes los paneles fijos se recortaban
    en GuiScale 3/4.
  - DiaryLanguageScreen.onClose() ahora cancela sin aplicar. ESC ya no
    aplica el idioma pendiente por accidente.
  - drawCenteredString reemplazado por drawString(..., false) con
    centrado manual en Access, Chat, Controls, Credits y Sound. Elimina
    el doble contorno negro vanilla sobre pergamino.

- **Coherencia textual**:
  - 25 lang files limpios: se removieron los guiones decorativos
    "- valor -" de 9 claves .subtitle. Los strings quedan puros; la
    decoracion decorativa vive solo en el codigo.
  - 7 pantallas Diary migradas para envolver el subtitle con
    "- ... -" en el render (Access, Language, Skin, Options,
    Multiplayer, Edit, Direct). Todas las Diary usan hoy el mismo
    patron: title / "- subtitle -" / flavor.

- **Ambientacion visual (materialidad y peso)**:
  - DiaryLanguageScreen: el fondo interno del marco de la lista usa
    PaperTexture.drawClean(), en vez del fill oscuro 0x24100804 que
    daba efecto de "cartulina lisa pegada encima". Ahora hereda las
    manchas del pergamino grande.
  - DiaryMultiplayerScreen: la banda tras los servers se aligero de
    0xB0281608 (69% marron opaco) a 0x703A2416 (44% sepia claro).
    Sigue dando contraste al texto blanco vanilla, pero deja ver el
    pergamino atras.
  - DiaryChrome.drawPanelShadow(): nuevo helper. Sombra proyectada
    abajo-derecha con offset 6px, color sepia oscuro (0x100600) en
    4 capas concentricas. Aplicado en 12 pantallas Diary. Los
    pergaminos se sienten ahora apoyados sobre la escena, no pegados.
  - DiaryButton: rework del fondo. Base PAPER_STAINED (un tono mas
    oscuro que PAPER que usa el panel), hover ocre sucio 0xFFB09668,
    sombra externa proyectada, highlight superior sutil, sombra interna
    abajo. Los botones se leen ahora como tiras de papel apoyadas
    sobre el pergamino, no como parte de el.
  - DiaryChrome.drawStyledScrollThumbInPanel(): nuevo helper que
    ancla la scrollbar al borde derecho del panel de pergamino
    (panelRightEdge - 14) en vez de al centro de pantalla. Tapa la
    barra vanilla (width/2+156) con pergamino antes de dibujar la
    sepia. Migradas Chat, Accessibility, Sound.

### Errores nuevos catalogados
- **E100**: return dentro de if en bloques interactivos de PowerShell
  NO aborta el script; solo aborta la subexpresion. Combinado con
  IndexOf() devolviendo -1 y calculando offsets a partir de -1,
  DiaryChrome.java quedo corrupto a 1562 bytes (de 11627). Fix:
  git checkout HEAD -- DiaryChrome.java + reinsertar helpers con
  LastIndexOf('}'). Refuerza R-PS-THROW.

### Reglas reforzadas
- **R-PS-THROW-STRICT**: cuando IndexOf/LastIndexOf puede devolver -1,
  siempre validar con `if ($idx -lt 0) { throw "msg" }` explicito
  antes de calcular offsets. return dentro de if en interactivo no
  aborta, solo termina la subexpresion.
- **R-INSERT-BEFORE-CLOSE**: para agregar metodos al final de una
  clase Java, usar `$c.LastIndexOf('}')` y reemplazar solo el ultimo
  caracter. Es robusto contra diferencias de EOL/whitespace, a
  diferencia de bloques multilinea que fallan silenciosamente por
  indentacion sutil.

### KI-11 (CERRADO en v0.11.9)
- El slider "Musica del Cronista" ya aplica volumen en tiempo real.
- Causa raiz: no era del slider ni del SoundManager. Era que 6
  pantallas Diary no llamaban MenuMusic.tick() en su tick propio,
  asi que MenuMusic.updateVolumes() nunca corria mientras el slider
  se movia. Fix de una linea en cada pantalla.

### KI-15 (ABIERTO en v0.11.9)
- Bandas inferiores de las listas vanilla (Accesibilidad, Chat,
  Sonido) recortan el contenido sin fade suave, dando "corte duro".
  Con la doble scrollbar antes se disimulaba; ahora que hay una sola
  barra estilizada, el corte es mas visible.
- Solucion futura: fade gradient inferior similar al de Language.
- Prioridad baja.

### KI-16 (ABIERTO en v0.11.9)
- Barra sepia estilizada pegada al marco exterior en Accesibilidad,
  Chat y Sonido. Podria respirar 4-6px hacia adentro.
- Prioridad muy baja.

### Nota sobre R-CHANGELOG-DISCRECIONAL
No se agregaron entradas a los 25 changelog/*.md in-game. Este hito
es coherencia tecnica y polish, sin manifestacion narrativa nueva que
amerite contarle al cronista mas alla del CHANGELOG.md raiz.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.13)

### Candidato I: v0.11.14 - Banderas pixel-art (KI-08)
Objetivo: helper FlagRenderer.java con banderas 16x11 hardcoded pixel por pixel
para los ~40 idiomas principales, mostradas a la izquierda de cada entrada en
DiaryLanguageScreen. Los idiomas sin bandera dedicada muestran un rombo tinta.
Alcance grande (mucho arte manual) pero tecnicamente simple.

### Candidato O: v0.11.15 - Hito Mixin dirt bypass (KI-12)
Objetivo: ahora que la infraestructura mixin esta armada y probada, mixin
sobre las 4 pantallas de trabajo que bypasean renderBackground:
MouseSettingsScreen, KeyBindsScreen, PackSelectionScreen, OnlineOptionsScreen.
Usar @Inject HEAD cancellable sobre renderDirtBackground o el metodo que
llama vanilla.

### Candidato C: v0.12.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema real de gameplay. Requiere diseno previo escrito.

---
## ADDENDUM v0.11.10 a v0.11.13 (Ciclo pergamino cerrado)

Ver KNOWN_ISSUES.md seccion "v0.11.10 a v0.11.13" para detalle completo. Resumen:

### Sistema
- 3 widgets nuevos: DiaryTextField, DiaryToggle, TooltipEventHandler.
- Helper de fades de lista retirado en RC; se conserva scroll estilizado sin velo.
- Infraestructura mixin armada aunque sin mixins activos (client:[] vacio).
- 4 KIs cerrados: KI-10, KI-15, KI-16, y avance en KI-07.

### Codigo nuevo
- client/widgets/DiaryTextField.java
- client/widgets/DiaryToggle.java
- client/TooltipEventHandler.java

### Codigo modificado
- render/DiaryChrome.java (scrollbars estilizadas; fades de lista retirados)
- screens/DiaryAccessibilityOptionsScreen.java (fades)
- screens/DiaryChatOptionsScreen.java (fades)
- screens/DiarySoundOptionsScreen.java (fades)
- screens/DiaryMultiplayerScreen.java (banda atenuada)
- screens/DiaryEditServerScreen.java (DiaryTextField)
- screens/DiaryDirectJoinScreen.java (DiaryTextField)
- screens/DiaryControlsScreen.java (DiaryToggle x4)

### Errores catalogados
- E101 a E107 (mixin compat, event bus, reflection tipada, widget overflow).

### Reglas nuevas
- R-MIXIN-COMPAT: compatibilityLevel JAVA_8 con Mixin 0.8.5.
- R-MODS-TOML-MIXIN: bloque [[mixins]] obligatorio en mods.toml.
- R-FORGE-EVENTS-FIRST: buscar evento Forge nativo antes de escribir mixin.
- R-WIDGET-OVERFLOW-3TIER: layout adaptativo de 3 niveles para widgets con contenido dinamico.
- R-REFLECTION-TYPED: filtrar por tipo esperado, no solo por nombre.
- R-MIXIN-SELF-CONTAINED: mixin sin imports del propio mod.
- R-MIXIN-VERIFY-TARGET: verificar en bytecode del caller que el metodo target sea el path real.

### Decisiones de diseno
- Tooltips: descartado mixin en favor de RenderTooltipEvent.Color. Mucho mas
  limpio y robusto.
- DiaryTextField: heredar de EditBox y override renderWidget preserva toda la
  logica de edicion nativa (Ctrl+C/V, Shift+arrows, filter, responder, etc.).
- DiaryToggle: envolver OptionInstance<Boolean> es mas limpio que reimplementar
  el binding manual. Permite reutilizar la logica de persistencia vanilla.

---
## ADDENDUM v0.11.10 a v0.11.13 (Ciclo pergamino cerrado)

Ver KNOWN_ISSUES.md seccion "v0.11.10 a v0.11.13" para detalle completo. Resumen:

### Sistema
- 3 widgets nuevos: DiaryTextField, DiaryToggle, TooltipEventHandler.
- Helper de fades de lista retirado en RC; se conserva scroll estilizado sin velo.
- Infraestructura mixin armada aunque sin mixins activos (client:[] vacio).
- 4 KIs cerrados: KI-10, KI-15, KI-16, y avance en KI-07.

### Codigo nuevo
- client/widgets/DiaryTextField.java
- client/widgets/DiaryToggle.java
- client/TooltipEventHandler.java

### Codigo modificado
- render/DiaryChrome.java (scrollbars estilizadas; fades de lista retirados)
- screens/DiaryAccessibilityOptionsScreen.java (fades)
- screens/DiaryChatOptionsScreen.java (fades)
- screens/DiarySoundOptionsScreen.java (fades)
- screens/DiaryMultiplayerScreen.java (banda atenuada)
- screens/DiaryEditServerScreen.java (DiaryTextField)
- screens/DiaryDirectJoinScreen.java (DiaryTextField)
- screens/DiaryControlsScreen.java (DiaryToggle x4)

### Errores catalogados
- E101 a E107 (mixin compat, event bus, reflection tipada, widget overflow).

### Reglas nuevas
- R-MIXIN-COMPAT: compatibilityLevel JAVA_8 con Mixin 0.8.5.
- R-MODS-TOML-MIXIN: bloque [[mixins]] obligatorio en mods.toml.
- R-FORGE-EVENTS-FIRST: buscar evento Forge nativo antes de escribir mixin.
- R-WIDGET-OVERFLOW-3TIER: layout adaptativo de 3 niveles para widgets con contenido dinamico.
- R-REFLECTION-TYPED: filtrar por tipo esperado, no solo por nombre.
- R-MIXIN-SELF-CONTAINED: mixin sin imports del propio mod.
- R-MIXIN-VERIFY-TARGET: verificar en bytecode del caller que el metodo target sea el path real.

### Decisiones de diseno
- Tooltips: descartado mixin en favor de RenderTooltipEvent.Color. Mucho mas
  limpio y robusto.
- DiaryTextField: heredar de EditBox y override renderWidget preserva toda la
  logica de edicion nativa (Ctrl+C/V, Shift+arrows, filter, responder, etc.).
- DiaryToggle: envolver OptionInstance<Boolean> es mas limpio que reimplementar
  el binding manual. Permite reutilizar la logica de persistencia vanilla.


---
## ADDENDUM v0.11.14 (Banderas pixel-art - KI-08)

### Alcance
Hito I del backlog. Se agrega bandera 16x11 px a la izquierda de cada
entrada en DiaryLanguageScreen. Idiomas sin bandera mapeada muestran
rombo tinta como fallback.

### Cambios de codigo
- **Nuevo**: client/render/FlagRenderer.java (~700 lineas)
  - Registro Map<String, int[][]> con ~90 codigos de idioma
  - Helpers: hBands, vBands, h2, nordicCross para bandas comunes
  - Cada bandera: metodo estatico privado que devuelve int[11][16]
  - API publica: draw(gfx, code, x, y) + has(code)
  - Fallback drawFallback con rombo tinta 5x5
- **Modificado**: client/screens/DiaryLanguageScreen.java
  - Import FlagRenderer + constante FLAG_SLOT=22
  - Entry.render() dibuja bandera en left+3, y centrada
  - Texto centrado en zona derecha [left+FLAG_SLOT..left+rowWidth]

### Cobertura de idiomas
- 7 variantes de ingles (US, UK, CA, AU, NZ, Pirate, upside-down)
- 22 variantes de espanol (ES, MX, AR, CL, UY, VE, EC, 419, AN, CO,
  PE, BO, PY, CR, DO, GT, HN, NI, PA, SV, PR, CU)
- Portugues (BR, PT), Frances (FR, CA/Quebec)
- Aleman (DE, AT, CH), Italiano, Neerlandes, Belga, Irlandes
- Nordicos completos (SE, DK, NO x3, FI, IS, FO)
- Este europeo, Baltico, Griego, Turco
- Asia oriental (JA, KO, ZH x3), Sudeste (VI, TH, ID, FIL, MS)
- Otros (HE, AR, FA, ZA, GE, AM, AZ, KZ, MN)
- Regionales (CA/Cataluna, EU/Vasco, GL/Galicia, CY/Wales, GD/Scotland)

### Errores catalogados
- **E108**: PowerShell interactivo pego "pri" suelto y basura "───────" al
  final de una linea al usar here-string @'...'@ largo. Similar a E39.
  Fix: pos-limpieza con .Replace() puntual. Verificacion visual mostro
  falso positivo de "───" (existe en comentario decorativo, no en codigo).
- **E109**: Ancla "## Version 0.11.13" no existia en CHANGELOG.md; la
  convencion real es "## v0.11.13" (sin la palabra "Version"). El script
  con throw dentro de if NO abortaba PowerShell interactivo (R-PS-THROW
  aplicado a medias). Consecuencia: .Replace() con ancla inexistente hace
  no-op silencioso y compila el archivo sin cambios. Refuerza R-VERIFY-DISK.
- **E110**: El client.jar vanilla 1.20.1 solo trae en_us.json. Los otros
  idiomas se descargan como assets con hash desde el asset index. Auditar
  la lista de idiomas por el jar es imposible. Alternativas: (a) captura
  visual del usuario, (b) trust en la lista canonica documentada de
  Minecraft, (c) probar todos los codigos posibles y verificar en runtime
  con Minecraft.getInstance().getLanguageManager().getLanguages().

### Reglas nuevas
- **R-CHANGELOG-ANCHOR-CONVENTION**: verificar la convencion real del
  archivo antes de asumir el ancla. CHANGELOG.md usa "## v0.X.Y" para
  v0.10+ y "## Version 0.X.Y" para v0.9-. No mezclar. Preferir IndexOf()
  con validacion explicita antes que Replace() con contains implicito.
- **R-VANILLA-LANGS-1.20**: en Minecraft 1.20+ los idiomas NO estan en el
  client.jar. Solo en_us.json esta ahi. Los demas se descargan por asset
  index. Para auditar codigos usar reflection en runtime o lista canonica
  documentada externamente.

### KI-08 (CERRADO en v0.11.14)
- Banderas pixel-art hardcoded implementadas. Fallback rombo para idiomas
  no mapeados.
- Deuda visual menor:
  - Iteracion 1 tuvo Uruguay y Mexico incorrectos (fix v0.11.14 mismo)
  - Iteracion 2 tuvo Ecuador con proporciones incorrectas (fix mismo)
  - Iteracion 2 tuvo Vasco parecida a cruz nordica (fix mismo)
- Cerrada tras 3 iteraciones dentro del mismo release, respetando
  R-ITER-VISUAL.

## PROXIMO HITO PLANIFICADO (revisado tras v0.11.14)

### Candidato O: v0.11.15 - Hito Mixin dirt bypass (KI-12)
Objetivo: mixin sobre las 4 pantallas que bypasean renderBackground:
MouseSettingsScreen, KeyBindsScreen, PackSelectionScreen,
OnlineOptionsScreen. Reemplazar dirt por escena viva atenuada + pergamino.
Estrategia: mixins individuales por pantalla (NO global sobre Screen)
con @Inject HEAD cancellable. required=false por seguridad. Verificar
target con javap sobre jar SRG antes de escribir cada uno.
Orden sugerido: MouseSettings -> KeyBinds -> PackSelection -> Online.
Riesgo: MEDIO. Historial de fallo (E90). Infra mixin ya armada v0.11.13.

### Candidato C: v0.12.0+ - Contenido de gameplay
Objetivo: primer sistema real de gameplay (Gripe Verde en si).
Requiere diseno previo escrito.


---
## ADDENDUM v0.12.0 (cierre administrativo del ciclo gabinete)

### Alcance
Version sin codigo. Solo:
1. Reescritura completa del changelog in-game bajo entrada unica
   "Version 0.10 - 0.11 - Una casa de papel" (25 archivos).
2. Bump v0.11.14 -> v0.12.0 para senalar cierre de fase UI.
3. Reversion del intento mixin fallido de v0.11.14 (KI-12 pendiente).

### Errores catalogados
- **E111**: @Inject/@Redirect no funcionan sobre metodos heredados. En
  MouseSettingsScreen, el mixin sobre renderBackground fallo porque
  ese metodo esta definido en Screen, no en MouseSettingsScreen. Para
  interceptar hay que hacer mixin sobre Screen + filtro instanceof, o
  usar @At INVOKE con owner concreto para @Redirect. 4 iteraciones
  ciegas antes de identificar la raiz. Revertido antes de commit.

### Reglas nuevas
- **R-MIXIN-INHERITED-METHODS**: @Inject y @Redirect NO alcanzan
  metodos heredados por defecto. Alternativas: (a) mixin sobre la
  clase que define el metodo con filtro instanceof; (b) @Redirect con
  target owner = subclase concreta para interceptar llamada especifica;
  (c) @Inject sobre metodo que la subclase SI overridea.

### KI-12 (sigue ABIERTO)
- Dirt bypass pendiente. Ruta correcta identificada:
  mixin sobre Screen.renderBackground + filtro instanceof por las 4
  pantallas target. Postpuesto a hito dedicado (v0.12.x tal vez, o
  posterior). No bloquea gameplay.

## PROXIMO HITO PLANIFICADO (revisado tras v0.12.0)

### Candidato C: v0.12.1+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema real de gameplay. Con la fase UI cerrada,
la casa esta lista para tener contenido adentro.

Sub-hitos a definir en diseno previo (a escribir antes de tocar codigo):
- Sistema de enfermedad: sintomas, progreso, contagio
- Cuarentena: zonas seguras vs zonas infectadas
- NPCs: enfermos, sanos, personal medico
- Items: mascaras, medicinas, alcohol, agua limpia
- Mecanicas ambientales: humo verde, ratas, cadaveres

Riesgo: ALTO. Requiere diseno previo escrito antes de codigo.

### Candidato O: v0.12.x - Mixin dirt bypass (KI-12) [pendiente sin urgencia]
Ruta correcta ya identificada. Se puede hacer cuando quede tiempo.


---
## ADDENDUM v0.12.0 (cierre administrativo del ciclo gabinete)

### Alcance
Version sin codigo. Solo:
1. Reescritura completa del changelog in-game bajo entrada unica
   "Version 0.10 - 0.11 - Una casa de papel" (25 archivos).
2. Correccion R-DIEGETICO sobre la entrada agrupada (primera version
   tenia palabras UI que rompian el pacto narrativo).
3. Bump v0.11.14 -> v0.12.0 para senalar cierre de fase UI.
4. Reversion del intento mixin fallido de v0.11.14 (KI-12 pendiente).

### Errores catalogados
- **E111**: @Inject/@Redirect no funcionan sobre metodos heredados.
  Mixin sobre renderBackground de MouseSettingsScreen fallo porque
  ese metodo esta definido en Screen, no en la subclase. 4 iteraciones
  ciegas antes de identificar la raiz. Revertido antes de commit.
- **E112**: Primera version de la entrada agrupada in-game usaba
  vocabulario UI/gaming (panel, cursor, scroll, multijugador,
  pantallas, regulador). El cronista victoriano no conoce esos
  terminos. Fix: reescritura completa con vocabulario de epoca.
  Refuerza R-DIEGETICO con audit automatico de palabras prohibidas.

### Reglas nuevas
- **R-MIXIN-INHERITED-METHODS**: @Inject y @Redirect NO alcanzan
  metodos heredados por defecto.
- **R-DIEGETICO-VOCAB**: Vocabulario prohibido/aceptado en changelog
  in-game. Verificar con audit automatico antes de publicar.

### KI-12 (sigue ABIERTO)
- Dirt bypass pendiente. Ruta correcta identificada. Sin urgencia.

## PROXIMO HITO PLANIFICADO (revisado tras v0.12.0)

### Candidato C: v0.12.1+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema real de gameplay. La casa esta lista para
tener contenido adentro.

Sub-hitos a definir en diseno previo (escribir antes de tocar codigo):
- Sistema de enfermedad: sintomas, progreso, contagio
- Cuarentena: zonas seguras vs zonas infectadas
- NPCs: enfermos, sanos, personal medico
- Items: mascaras, medicinas, alcohol, agua limpia
- Mecanicas ambientales: humo verde, ratas, cadaveres

Riesgo: ALTO. Requiere diseno previo escrito antes de codigo.

### Candidato O: v0.12.x - Mixin dirt bypass (KI-12) [pendiente sin urgencia]
Ruta correcta identificada. Se puede hacer cuando quede tiempo.


---
## ADDENDUM v0.12.1 (detalles vivos del gabinete)

### Alcance
Version de polish visual y atmosferico. 16 mejoras aplicadas en 6 bloques
de desarrollo iterativo con feedback en vivo del usuario. Bugs
introducidos por mejoras experimentales fueron revertidos limpiamente.

### Cambios de codigo
- **AtmosphereRenderer.renderStars()**: 15 estrellas brillantes 2x2
  con borde suave; frecuencia y fase propias por estrella (organic
  twinkle).
- **VictorianSkyline**: nueva ventana animada FG (index 2/1/1) que
  alterna on/off con transicion suave cada 8-15s. Campos
  animWindowPhase/Timer/On + logica en tick() + override en
  renderWindowsOnBuildings.
- **LivingAtmosphere**: farol erratico (index 2) con oscilacion
  caotica 3-5s cada 30-60s. Campos erraticTimer/NextEvent/Intensity/
  Duration.
- **LivingAtmosphere**: rata cruzando (sprite 8x4, direccion aleatoria)
  cada 40-80s. Campos ratX/Speed/Timer/NextEvent/Active + renderRat.
- **DiaryButton**: pressed state (offset Y+1, sombra reducida a 60
  alpha, bg 0xFFA08858); hover speed 6->4 (EasedValue mas suave).
- **DiaryToggle**: playDownSound cambia de playClick() a clickHeavy().
- **LogoRenderer**: pool de 12 frases del cronista con Random.nextInt
  no repetitivo. Timer YEAR_ROTATE_MS variable 30-120s con
  rerollRotateInterval() en cada rotacion.
- **DiaryTitleScreen**: pool de 4 firmas rotativas (SIGNATURE_POOL)
  sincronizadas con yearIndex.
- **DiaryOptionsScreen**: pool de 8 flavors rotativas + campo estatico
  flavorIndex + currentFlavor. Divisor superior movido debajo del
  flavor con branching if/else.
- **ChangelogScreen**: capitular decorativa (drop cap 1.6x en INK oscura)
  + separadores con rombo (lozenge 5x5 + fillGradient lateral) +
  lastScrollY estatico + override onClose.
- **DiaryLanguageScreen**: lastScrollAmount estatico + setScrollAmount
  restore en init + save en onClose.
- **DiaryConfirmScreen**: UiSounds.notification() al init.
- **DiaryChat/Controls/Credits/Options/Sound + Language**: fill negro
  inicial `g.fill(0,0,w,h, 0xFF000000)` para prevenir residuo del
  framebuffer al cambiar de pantalla.

### Cambios revertidos durante desarrollo
- Fade de transicion 200ms al entrar a Diary screens (percibido como
  flash, no como transicion).
- UiSounds.open() al abrir subpantallas (sonaba mal).
- Parallax con mouse en Title (piso desaparecia, pergamino residual).
- Fill negro en DiaryTitleScreen (oscurecia ventanas iluminadas).
- YEAR_POOL con anos romanos (Anno Domini MDCCCXCVII) reemplazado
  por apuntes del cronista.

### Errores catalogados
Ver KNOWN_ISSUES.md v0.12.1: E113 (parallax overshoot), E114 (fill
negro en Title rompe blend), E115 (Cloth Config + IMPR crash externo).

### Reglas nuevas formalizadas
- **R-NO-GLOBAL-TRANSLATE-RENDER**: no aplicar pushPose/translate sobre
  escena viva en render().
- **R-FRAMEBUFFER-CLEAR-TITLE**: no llamar fill negro opaco en Title.
- **R-CONFIG-RESIDUAL-EXTMOD**: crash de mod externo por config no
  registrada = borrar .toml y reiniciar.

---
## ADDENDUM v0.12.2 (escena viva mejorada + easter eggs + widgets nuevos)

### Alcance
Version masiva con 60+ mejoras de UI, atmosfera, contenido rotativo,
easter eggs y widgets nuevos. 31 archivos, 1947 inserciones, 5 clases
nuevas (DiaryOnlineOptionsScreen, DiaryAmbienceSlider, DiarySlider,
DiaryProgressBar).

### Escena viva del Title
- **Luna con 8 fases**: nueva, creciente, cuarto, gibosa, llena y
  menguantes. Pool rotativo aleatorio cada 4-8 min. renderMoonPhase
  con recorte pixel a pixel (sin disco oscuro base).
- **Estrellas fugaces**: trail 12px con fade + sonido sutil + mensaje
  flotante ("Un deseo cae del cielo"). Cada 60-180s.
- **Bandada de pajaros**: 3-4 siluetas cruzando el cielo cada 90-180s.
  Velocidad + formacion aleatoria.
- **5 ventanas parpadeantes** (era 1): array ANIM_WINDOWS con 5
  posiciones [building][row][col], timers independientes.
- **Efecto vela al marcar las 12**: overlay sepia sutil cuando el
  reloj marca hora cercana a noon. Helper PocketWatchRenderer.isNearNoon().
- **Humo mas denso**: particulas x2 (luego bajado a 4 por chimenea),
  wind variable con componente sinusoidal lenta, humo en chimeneas FG
  (luego desactivado para performance).
- **Niebla que respira**: pulso lento 0.3Hz sobre renderFogLow.
- **Gato en tejado**: sprite 4x2 pixel-art en edificio FG index 5,
  gira cabeza cada 5s.
- **Grieta en edificio**: zigzag pixel-art en edificio FG index 1,
  con vela parpadeante amber dentro.
- **Rata cruzando la calle**: sprite 8x4 cada 40-80s.
- **Relampago con silueta**: renderLightningSilhouette dibuja contornos
  blancos de chimeneas durante el pico del flash.
- **Cuervos coordinados con audio**: flag crowsPlayingUntilMs en
  MenuAmbience, aceleracion 3.5x cuando suena el sonido crows.
- **Nubes con velocidades variadas**: initCloudSpeeds con Random +/-30%.

### Contenido rotativo
- **Pool subtitulos del logo**: 12 apuntes del cronista ("Dia 47 ~
  Aun no encuentro respuestas", etc.) con Random.nextInt, intervalo
  variable 30-120s via rerollRotateInterval().
- **Firmas rotativas**: pool de 4 ("Dr. S. Pereira", "Cronista R. Da
  Rosa", "Cap. G. Bandeira", "Prof. A. Focco") sincronizadas con
  yearIndex. Con tachadura del nombre anterior.
- **Horas rotativas del reloj**: pool de 8 horas en
  PocketWatchRenderer.HOUR_POOL con timer 3-8 min.
- **Headers quarantine rotativos**: 8 combos (autoridad + entidad).
- **Flavors rotativas** en DiaryOptionsScreen (8 frases), DiaryChatOptionsScreen
  (7 frases), DiaryAccessibilityOptionsScreen (7 frases).
- **Subtitulos botones**: GreenFluButton con pickRotatingSubtitle()
  cacheado por sesion (5 variantes multiplayer, 5 variantes mods).
- **Titulo changelog rotativo**: 5 variantes.

### Easter eggs
- **Doble-click en logo**: skull sacudida 600ms + 20 particulas verdes
  cayendo 2s + sonido menu.crows (pitch 1.2) + flash amber 400ms +
  relampago instantaneo forzado + overlay verde 300ms + mensaje
  2x centrado con fade 5s. 8 frases random en espanol neutro.
- **Firma con temblor**: jitter sutil via sin()/cos() con frecuencias
  altas (mano cansada del cronista).
- **Mensaje dia real**: getDayMessage() con LocalDate.now() + bonus
  mensual (oct=muertos, dic=Adviento, ene=misma peste).
- **Hora real**: overlay sepia oscuro si hora real 20-06.
- **Detector de mes**: octubre/diciembre/enero con frase bonus.
- **Pulso del skull**: logoPulse via sin(ms/800) levanta 1px.

### Widgets nuevos
- DiarySlider: generico con DoubleConsumer + DoubleFunction.
- DiaryProgressBar: barra pergamino con label opcional.
- DiaryAmbienceSlider: slider para volumen ambiente del cronista.
- DiaryOnlineOptionsScreen: subclase de SimpleOptionsSubScreen.

### Polish del pergamino (PaperTexture)
- drawCoffeeStain: 1 mancha cafe con halo.
- drawWaxSeal: sello cera roja esquina inferior derecha.
- drawTornEdges: muescas irregulares en 4 esquinas.
- drawPaperFibers: puntos sepia dispersos.
- drawWatermark: "EC" muy sutil en el centro.
- Papel envejece con el uso: openCount aumenta stains (max +15).

### Posters
- drawBloodSplatter: 30% chance de manchas sangre en Cuarentena/Reward.
- drawInkStamp: 40% chance de sello APROBADO/REVOCADO/CENSURADO/etc.

### Audio
- Susurros lejanos: AMBIENT_OCCASIONAL pitch 0.55-0.75 como evento RARE.
- Tos seca: MENU_CHAIN pitch 0.40 como evento RARE.
- Sonido de puerta (UiSounds.close) al abrir DiaryOptionsScreen.
- Sonido al escribir en DiaryTextField (UiSounds.scroll).
- Sonido al scrollear changelog (throttle 200ms).
- Volumen hover reducido: VOL_HOVER 0.12->0.06.

### UX
- Botones Title subtitulos cacheados por sesion (fix re-sort al tocar AMB).
- Boton AMB removido del Title (controlado desde slider en Sonidos).
- Focus visual dorado en DiaryButton cuando isFocused por teclado.
- Fix boton Join multiplayer: scan dinamico por Component message.
- Scroll recordado en DiaryLanguageScreen y ChangelogScreen.
- Campanilla en DiaryConfirmScreen al init.
- Capitulares decorativas en changelog (drop cap H1 1.6x).
- Separadores con rombo en changelog.
- Asterisco * al margen de H1 en changelog.

### Config
- AMBIENCE_VOLUME: ForgeConfigSpec.DoubleValue persistente.
- OPEN_COUNT: ForgeConfigSpec.IntValue persistente (contador aperturas).
- MenuAmbience.getUserVolume() / setUserVolume() con lazy-load config.

### Espanol neutro
- Todos los pools hardcoded reescritos: "ustedes"/"su" en vez de
  "vosotros"/"vuestra". Aplica a subtitulos, easter eggs, dias,
  flavors de Options/Chat/Accessibility.

### Cambios revertidos durante v0.12.2
- Humo verde intermitente (sacado, no quedaba bien).
- Carruaje cruzando (parecia tren, sacado).
- Ropa colgada entre edificios (posicionamiento malo, sacado).
- Gato version grande 2x (volvio a 4x2 original).
- Parallax con mouse (causaba bugs de framebuffer).
- Fill negro en Title (oscurecia ventanas).
- Subtitulo rotativo del logo "GRIPE VERDE" (usuario quiere fijo).
- UiSounds.open() al abrir subpantallas (sonaba mal).
- Fade de transicion 200ms (percibido como flash).
- Nombres rotativo del server pinneado (confuso).
- FPS indicator (removido por pedido del usuario).
- Cursor de pluma custom (confuso).

### Errores catalogados v0.12.2
- **E116**: parallax con pushPose/translate en render() causa que
  el piso desaparezca y deje residuo del framebuffer.
- **E117**: g.fill(0,0,w,h,0xFF000000) al inicio del Title
  oscurece las ventanas iluminadas del skyline.
- **E118**: Cloth Config + IMPR crash esporadico por config no
  registrada (bug externo, fix = borrar .toml).
- **E119**: drawSoftBlob con 8 particulas de humo superpuestas
  produce blancura por saturacion de alpha.
- **E120**: renderMoonPhase usa disco oscuro como mascara que
  se ve como circulo negro en fases parciales.
- **E121**: updateLoopVolumes se ejecuta antes de que start()
  termine, mata los loops recien creados.
- **E122**: setUserVolume + onDrag + updateLoopVolumes = 3
  rebuilds por frame, satura SoundManager.
- **E123**: MenuAmbience.stop() en Title.removed() mata el
  ambiente al navegar a subpantallas.
- **E124**: MenuAmbience.tick() faltaba en 13 subpantallas.

### Reglas nuevas v0.12.2
- **R-NO-GLOBAL-TRANSLATE-RENDER** (reforzada): no usar pushPose/
  translate sobre escena viva en render().
- **R-FRAMEBUFFER-CLEAR-TITLE**: no llamar fill negro opaco en Title.
- **R-CONFIG-RESIDUAL-EXTMOD**: crash de mod externo por config no
  registrada = borrar .toml y reiniciar.
- **R-SOUND-REBUILD-ONCE-PER-TICK**: nunca reconstruir loops de audio
  mas de 1 vez por tick. setUserVolume solo guarda el valor, el
  rebuild real lo hace updateLoopVolumes en tick().
- **R-AMBIENCE-PERSIST**: MenuAmbience NO se para al navegar entre
  pantallas del menu. Solo se auto-detiene cuando mc.level != null.

---
## ADDENDUM v0.12.3 (hotfixes y optimizaciones)

### Alcance
Hotfixes criticos sobre v0.12.2. 22 archivos, 133 inserciones.

### Fixes de audio
- MenuAmbience.stop() removido de Title.removed() — ambiente persiste.
- MenuAmbience.tick() agregado en 13 subpantallas Diary.
- setUserVolume NO reconstruye loops (delegado a updateLoopVolumes
  en tick, evita saturacion del SoundManager por 3+ rebuilds/frame).
- updateLoopVolumes no reconstruye en primera llamada (evita matar
  loops recien creados por start()).
- start() setea lastAppliedVolume post-init.
- getUserVolume() aplicado a triggerFrom + onSecondTick + desmute.
- tick() skipea si getUserVolume() < 0.01f.

### Fixes visuales
- renderStreet hecho public + llamado en 13 subpantallas (calle
  visible sin faroles/esporas/rata).
- Luna creciente: disco oscuro base eliminado, solo se dibuja la
  parte iluminada con pixel loop.
- Halo lunar: solo en fases >40% iluminacion, radios reducidos.
- Easter egg texto escala adaptativa (1.0x-1.8x segun this.width).
- Panel sonidos PANEL_H 340->380.

### Optimizaciones de performance
- 13 subpantallas sin renderFogLow/renderStreetLevel/renderAir
  (mantiene renderSky + skyline BG + skyline FG + renderStreet).
- Estrellas 60->40, brillantes 15->10.
- Nubes 7->5.
- RAIN_COUNT 80->40.
- SPORE_COUNT 30->15.
- Humo FG desactivado (solo BG, alpha 90).
- Vignette margin 0.28->0.20, step 2px.
- drawSoftBlob: menos steps para blobs chicos + alpha threshold.
- PaperTexture: stains divisor 15000->30000, fibers 500->1500,
  coffee max 1.
- DEBUG_LOGGING desactivado en MenuAmbience, MenuMusic,
  LivingAtmosphere.

### Estado del proyecto post-v0.12.3
- 71 clases Java.
- 39 archivos modificados entre v0.12.1 y v0.12.3.
- 2055 inserciones netas.
- 5 clases nuevas: DiaryOnlineOptionsScreen, DiaryAmbienceSlider,
  DiarySlider, DiaryProgressBar.

## PROXIMO HITO PLANIFICADO (revisado tras v0.12.3)

### Pendientes identificados
- Fix luna creciente pixel loop puede ser lento en resoluciones altas.
- Verificar que slider AMB funciona correctamente post-restart.
- KI-12: mixin dirt bypass para 4 pantallas vanilla restantes.

### Candidato C: v0.13.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema real de gameplay. Con la fase UI cerrada
y optimizada, la casa esta lista para tener contenido adentro.

Sub-hitos a definir en diseno previo:
- Sistema de enfermedad: sintomas, progreso, contagio
- Cuarentena: zonas seguras vs zonas infectadas
- NPCs: enfermos, sanos, personal medico
- Items: mascaras, medicinas, alcohol, agua limpia
- Mecanicas ambientales: humo verde, ratas, cadaveres

---
## ADDENDUM v0.13.0 a v0.13.2 (ciclo pergamino aplicado al mundo)

### Alcance
- v0.13.0: intento fallido DiaryDeathScreen (revertido en v0.13.1).
- v0.13.1: rollback administrativo, otro mod cubrira death screen.
- v0.13.2: primer resource pack built-in del mod (inventory.png), abre
  camino para reemplazar mas texturas vanilla en versiones siguientes.

### Codigo nuevo
- client/ResourcePackHandler.java (registra pack built-in en AddPackFindersEvent).
- resourcepacks/gripeverde_theme/pack.mcmeta
- resourcepacks/gripeverde_theme/pack.png
- resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container/inventory.png

### Codigo eliminado (v0.13.1)
- client/screens/DiaryDeathScreen.java (nunca funcionara correctamente por
  el loop de respawn en singleplayer).
- client/ClientTickHandler.java (solo servia para tickear el lock del
  respawn, ya no necesario).

### Errores catalogados
- E113: PathPackResources en 1.20.1 tiene firma (String, boolean, Path),
  no (String, Path, boolean) como en versiones previas.
- E114: LocalPlayer.respawn() en singleplayer con /kill dispara un
  ClientboundPlayerCombatKillPacket que reabre DeathScreen inmediatamente.
  El intento de bloquear con flag estatico + ClientTickHandler no fue
  suficiente. Fix real requiere mixin sobre ClientPacketListener.
  handleRespawn o similar (fuera de scope aceptable).
- E115: Los assets del namespace "minecraft" DENTRO de un mod jar NO
  sobrescriben vanilla. Requieren un resource pack built-in con
  AddPackFindersEvent para funcionar.

### Reglas nuevas
- R-RESOURCEPACK-BUILTIN: Para sobrescribir texturas vanilla desde un
  mod, usar SIEMPRE resource pack built-in registrado via
  AddPackFindersEvent. Nunca poner assets/minecraft/ dentro del jar
  esperando que anulen vanilla; no funciona.
- R-RESPAWN-NOT-INTERCEPT: No intentar reemplazar DeathScreen sin mixin
  sobre ClientPacketListener.handleRespawn. El flujo de respawn es
  asincrono y cualquier intento de bloquear reapertura via flags
  fallara en algun escenario.

### KI-13 (ABIERTO en v0.13.2, prioridad baja)
- El inventory.png reemplaza solo la textura, no cambia comportamiento.
  Cofres, hornos, mesa de crafting, yunque, etc. siguen vanilla. Plan:
  M1 en el proximo ciclo (v0.13.3+) extender pack a mas containers.

### KI-14 (ABIERTO en v0.13.2, prioridad media)
- Hotbar (widgets.png) sigue vanilla gris feo. Plan: M2 en v0.13.4 o
  cuando encaje.

## PROXIMO HITO PLANIFICADO (revisado tras v0.13.2)

### Candidato M1: v0.13.3+ - Extender resource pack a containers
Objetivo: agregar texturas pergamino para generic_54, furnace,
crafting_table, anvil, enchanting_table, beacon, etc. Un container por
version. Riesgo bajo (mismo patron que inventory.png), alto impacto
visual acumulado.

### Candidato M2: v0.13.4-6 - Hotbar y otros widgets siempre visibles
Objetivo: widgets.png pergamino, quiza icons.png (barras) SI se decide
tocarlas.

### Candidato M5: v0.13.x - Tooltips de items estilizados
Objetivo: extender el fix de tooltips vanilla (KI-10) a los tooltips
de items del inventario. Mixin o RenderTooltipEvent.Color extendido.

### Candidato M9: v0.14.0 - Hito Mixin dirt bypass (KI-12)
Objetivo: reemplazar dirt background de MouseSettings, KeyBinds,
PackSelection, OnlineOptions. Investigar refmap fallido en E90/E111.

### Candidato M10: v0.13.x - Actualizar changelog in-game
Objetivo: agregar entrada agrupada v0.13 en los 25 archivos
changelog/*.md que refleje inventario + resource pack. Solo si el
ciclo v0.13.x termina con mas manifestacion observable.

### Candidato M11: v0.13.x - Deprecations Forge
Objetivo: migrar FMLJavaModLoadingContext.get() y ResourceLocation
(String,String) a las APIs no-deprecated. No urgente en 1.20.1 estable.

### Candidato M7: v0.14.0+ - Contenido de gameplay (Gripe Verde)
Objetivo: primer sistema real de gameplay. Requiere diseno previo escrito.

---
## ADDENDUM v0.13.3 a v0.13.9 (ciclo M1 - resource pack expandido)

### Alcance
Extension del resource pack built-in de un solo container (inventory) a
siete containers principales bajo un mismo lenguaje visual "steel
victoriano" (hierro forjado + remaches + placas de laton), con acentos
tematicos distintos por tipo de container:
- v0.13.3: inventory.png rework a steel (Alex pidio steel, no pergamino).
- v0.13.4: crafting_table.png (mesa 3x3).
- v0.13.5: generic_54.png (cofres, cofre doble, shulker box, barril).
- v0.13.6: furnace.png con tinte cobrizo (horno + smoker + blast furnace).
- v0.13.7: anvil.png + grindstone.png con tinte acero frio (herreria).
- v0.13.8: enchanting_table.png con tinte purpura arcano.
- v0.13.9: beacon.png con tinte dorado (baliza, cierre ciclo).

### Assets nuevos
Todos en: resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container/
- inventory.png (rework)
- crafting_table.png
- generic_54.png
- furnace.png
- anvil.png
- grindstone.png
- enchanting_table.png
- beacon.png

### Codigo nuevo
Ninguno (todo es assets + ResourcePackHandler ya existente de v0.13.2).

### Errores catalogados
- E116: PowerShell param([int]$slotPositions) rechaza arrays. Debe ser
  [array]. Detectado en v0.13.7 cuando la funcion New-SteelPanel fallo
  silenciosamente para anvil + grindstone (Write-Host "OK" impreso pero
  archivos NO generados). Fix aplicado con commit --amend.
- E117: Arrays anidados en PowerShell requieren sintaxis con coma prefija
  ",@(...)" para evitar que se aplanen automaticamente. Sin la coma, un
  array de arrays 3-elementos se aplana a un array de 9 enteros.

### Reglas nuevas formalizadas

- **R-CICLO-AGRUPADO**: Cuando un hito (ej: M1) se compone de multiples
  bumps chicos del mismo patron (ej: reemplazar N texturas del mismo
  estilo), se permite ciclo agrupado:
  1. Bumps intermedios llevan commit simple sin tag ni actualizacion de
     docs.
  2. Al terminar el ciclo completo, un unico bloque de cierre R-CIERRE
     actualiza CONTEXTO/CHANGELOG/README/KNOWN_ISSUES + tag + push.
  3. La entrada del changelog (raiz + in-game) es agrupada narrativamente
     ("El gabinete se llena de maquinas") en vez de N entradas
     fragmentadas.
  Condicion: el ciclo debe compartir un tema/patron claro. Si un bump
  intermedio introduce cambio significativo distinto (nuevo sistema, fix
  critico), rompe el ciclo agrupado y se cierra con R-CIERRE puntual.

- **R-VERIFY-COUNT-STRICT**: Refuerzo de R-VERIFY-COUNT. Cuando una
  funcion genera archivos, la verificacion post-generacion debe hacerse
  con Test-Path + throw si falta cualquier archivo. Nunca confiar en
  Write-Host "OK" que se ejecuto antes de la I/O real. Detectado en E116.

- **R-POWERSHELL-ARRAY-PARAM**: En funciones PowerShell que reciben
  arrays anidados, declarar el parametro como [array] (NO [int]) y usar
  sintaxis ",@(...)" con coma prefija al construir el array para evitar
  auto-aplanado.

### KI-13 (CERRADO en v0.13.9)
- El resource pack cubre ahora todos los containers principales del juego
  vanilla. Solo quedan containers menores/raros: brewing_stand, hopper,
  dispenser, dropper, loom, cartography_table, stonecutter, smithing,
  villager trading, horse. Estos son deuda opcional de futura extension.

### KI-14 (SIGUE ABIERTO)
- Hotbar (widgets.png) sigue vanilla. Plan: M2 en ciclo futuro cuando
  encaje.

### KI-15 (CERRADO en v0.13.10)
- Containers menores cubiertos por el resource pack steel.
- Stonecutter, Loom y Smithing conservan layout vanilla para memoria muscular.

## PROXIMO HITO PLANIFICADO (revisado tras RC v0.13.10)

### Candidato Q1 - Validacion visual final en cliente
Objetivo: revisar Title, Options, Language, Sound, Chat, Accessibility, Multiplayer, Changelog y containers en GUI scales principales.

### Candidato Q2 - Audio UI fino
Objetivo: confirmar en cliente real que `ui.button_main`, `ui.button_section` y `ui.back` no saturan ni se pisan con musica/ambiente.

### Candidato M2 - Hotbar (widgets.png)
Objetivo: decidir si la hotbar debe seguir lenguaje pergamino o acero gris.

### Candidato M5 - Tooltips de items estilizados
Objetivo: extender el tratamiento de tooltips sin romper compatibilidad de mods.

### Candidato M11 - Deprecations Forge
Objetivo: migrar APIs marcadas como deprecated cuando se cierre la RC.

---
## ADDENDUM v0.13.10 (acero bajo la lluvia)

### Alcance
Version de entrega para Alex. Cierra una pasada amplia de polish sin agregar
gameplay: containers steel menores, textos principales con sombra, icono del
resource pack renovado y vida extra en el Title.

### Cambios de codigo
- Nuevo client/ContainerOverlayHandler.java: oculta labels vanilla conflictivos
y redibuja titulos principales con sombra; agrega detalles dinamicos a mesa
arcana y Ender Chest.
- LivingAtmosphere.java: polillas cerca de faroles, salpicaduras de lluvia,
ondas en charcos, reflejos ambar, papeles arrastrados, rafagas de viento,
lluvia diagonal, sombras de ramas y relampagos ramificados.
- AtmosphereRenderer.java: mas frases de estrella fugaz y resplandor industrial
bajo en horizonte.
- DiaryTitleScreen.java: tooltip del boton de musica, patina/vidrio sucio y
retiro del subtitulo QUARANTINE IN FORCE bajo el logo.
- LogoRenderer.java: mas apuntes del cronista y pulso verde en el ojo del skull.
- build.gradle: task verifyGripeVerdeThemeAssets.

### Assets
- pack.png renovado.
- Nuevos/retocados containers steel: smoker, blast_furnace, shulker_box,
brewing_stand, hopper, dispenser, dropper, loom, cartography_table,
stonecutter, smithing, horse, villager2, furnace, anvil, grindstone,
enchanting_table.

### Nota
No se agrega gameplay. Todo es presentacion, atmosfera, resource pack y
validacion de assets.

---

## ADDENDUM v0.16.0 (La sala de consulta — pase profundo)

### Alcance
Pase profundo de 50+ experiencias perceptibles nuevas, centrado en
memoria, descubrimiento y consecuencias. Sin gameplay. Sin tocar
contenedores ni layouts vanilla. Sin resource pack. Sin mixins nuevos.

### Nota de auditoria previa (importante)
- En este snapshot NO existen las clases ExpedientHeader ni AnomalyRenderer
  (verificado por grep). El easter egg existente es el telegrama de "caso
  sellado" del logo. Todo lo nuevo se diseno contra el estado real del
  codigo, no contra la documentacion historica.
- El resource pack fue retirado en v0.15.0 por orden del usuario. No se
  reintrodujo.
- Features revertidas en el pasado (parallax, cursor de pluma, fade de
  transicion, carruaje, humo verde, ropa colgada) NO se reintrodujeron.
  El coche del sepulturero es una carretilla lenta a mano, no el carruaje
  revertido (que parecia un tren).

### Codigo nuevo (paquete client/lore)
- `ChroniclerMemory.java`: memoria persistente JSON en
  `config/gripeverde-memory.json` via Gson + FMLPaths.CONFIGDIR.
  Campos: firstVisitDay, lastVisitDay, visitCount, streak, dustPending,
  anniversaryPending, discovered[], collectibles[], pendingNotes[],
  counters{}, guestbook[]. API: beginSession(), discover/mark, bump,
  counter, addCollectible, consumeDust/Anniversary. Tolerante a archivo
  corrupto.
- `Discoveries.java`: registro de 43 descubrimientos (ids, nombres ES/EN,
  pistas ES/EN). discover() = memoria + toast + sonido de archivo.
  drawToast() pinta el sello arriba a la derecha.
- `Marginalia.java`: cola persistente de notas contextuales del cronista
  (19 ids de nota). queue/queueOnce + poll + draw/drawHorizontal.
  Las notas son REACCIONES, no frases aleatorias.
- `HiddenCipher.java`: telegrafo oculto del Title. Buffer de letras
  visibles como tinta tenue. Codigos: 1897 (telegrama), 1152 (13
  campanadas + ventana del piso 13 persistente), HOLLOWAY (informe medico
  + sello FRAUDE permanente), PEREIRA (reaccion + firma ofendida).
- `Herald.java`: periodico diario procedural (semilla por dia real),
  kiosco en la calle, lectura marcada por dia.
- `screens/DiaryArchiveScreen.java`: la Sala de consulta. Grilla de 43
  fichas (hallada/vacia con pista), sello de estado PENDIENTE/EN
  CURSO/COMPLETO, documentos sueltos (telegrama, informe Holloway, ficha
  negra a 38+ fichas). Entrada: folio escondido del Changelog o codigo
  HOLLOWAY.

### Sistemas nuevos
- Descubrimiento -> ficha -> toast -> memoria (ciclo completo).
- Consecuencias permanentes (contadores de memoria leidos por renderers):
  `thirteenth_window` (luz donde no hay piso), `fraude_stamp` (sello FRAUDE
  permanente sobre el poster del elixir, dibujado por PosterRenderer los
  dias que ese cartel esta en la calle), `curtains_closed` (ventana del atico con
  cortinas para siempre), `avenue_darkened` (farol enfermo debilitado para
  siempre), `seal_broken`, `pereira_offended`, `wanted_eyes`.
- Objetos del cajon (5): llave (reloj, 5 visitas), pluma (cuervo, 5
  toques), jiron (gato, 7 toques), sello roto (creditos), nota
  (contrabandista). Cajon del gabinete de opciones: cerrado sin llave,
  vitrina con siluetas al abrir. Completo = ficha final.
- Cadenas conectadas: poster despegado -> nombre Holloway -> codigo ->
  informe -> marginalia del cronista arrepentido -> correccion de su
  propia frase de la tinta. Susurro respondido x3 -> pista de los tres
  golpes. Controles x3 visitas sin senal -> pista del contrabandista.

### Modificaciones por pantalla
- Title: inspeccion del reloj (click), llave del reloj, poster despejado,
  faroles apagables (todas apagadas = ficha + consecuencia), ventana-senal
  del atico (3 respuestas -> cortinas), gato (7 toques = jiron), rata que
  se detiene, kiosco/Heraldo, coche del sepulturero (tras campanas),
  veleta (rafagas), ojo del emblema que sigue al mouse + ficha por mirarlo,
  saludo de sesion (primera vez/regreso/mismo dia), polvo por ausencia,
  aniversario (365 dias), vigilia por inactividad (60s/150s), alba tras
  20 min de sesion (NIGHT_WHOLE), tiempo enfermo (glitches escalan tras
  8 min), telegrafo oculto + 4 codigos, respuesta al susurro ambiental,
  marginalia + toast.
- Pause: vela apagable (texto en la oscuridad), cuervo (5 toques = pluma),
  aliento en el vidrio (auto), segunda sombra (12+ fichas, auto), firma
  extendida "y de quien lee" (10+ fichas).
- Options: cajon con llave + vitrina de objetos, tres golpes en la madera
  (respuesta), bisagra (25 aperturas = ficha + creak grave + marginalia),
  correccion de la frase de la tinta tras leer a Holloway, marginalia.
- Controls: senal del contrabandista (sneak+sprint toggle, autojump+optab
  off) = nota + ficha; pista tras 3 visitas.
- Skin: espejo de la niebla (silueta 3s + ficha).
- Chat: buzon (5 cartas = ficha + carta sellada + marginalia).
- Accessibility: ojo de la percepcion que sigue al mouse (+ ficha).
- Language: lengua de la niebla (entrada fantasma con 6+ fichas, una vez
  por dia, se esfuma al tocarla); marginalia al cambiar idioma.
- Sound: el silencio escucha (musica a 0 durante 3s = latido + ficha).
- Multiplayer: libro de visitas persistente (nombre + fecha).
- Credits: sello del registro rompible (objeto + linea oculta).
- Confirm: contador de destrucciones (3 = ficha + recordatorio permanente).
- DirectJoin: 13.13.13.13 = aviso de direccion sin mapa + ficha.
- Changelog: marcas del encuadernador (3 asteriscos = remaches dorados),
  folio escondido tras el ultimo renglon -> Sala de consulta.

### Audio (sin assets nuevos; reuso con pitch/volumen)
- Pieza fantasma: 5 skips por sesion -> silencio + crepitar + susurro.
- Trece campanadas (codigo 1152) con pitch descendente.
- Respuesta al susurro: doble voz grave.
- Golpes y respuesta (madera grave), bisagra quejosa, latido del silencio,
  clac de telegrafo por letra, croar del cuervo (crows agudo), protesta
  del gato, buzon, sello roto.

### Config/persistencia
- gripeverde-client.toml: sin cambios (audio + openCount).
- gripeverde-memory.json: nuevo, independiente, tolerante a corrupcion.

### Version
- 0.15.2 -> 0.16.0 (build.gradle + GripeVerdeMod.VERSION).
- Docs R-CIERRE: CHANGELOG.md raiz, README.md, CONTEXTO.md,
  KNOWN_ISSUES.md y 25 changelog in-game actualizados.

### Validacion en este entorno
- El sandbox del pase no tiene JDK instalable ni acceso a
  maven.minecraftforge.net: el build se hizo en la maquina del usuario.
  Resultado: primer intento con 1 error (duckFor llamado en MenuAmbience
  en vez de MenuMusic; fix db61d86), segundo intento BUILD SUCCESSFUL
  (57s, solo deprecation warnings preexistentes). v0.16.0 deployado a Test.
- Regla derivada E125: sin javac disponible, todo codigo nuevo debe
  limitarse a APIs verificadas en el propio repo; nada de firmas recordadas.

---

## ADDENDUM v0.17.1 (Remaches bien puestos)

### Alcance
Parche de mantenimiento sobre el resource pack `gripeverde_steel` introducido
en v0.17.0. Sin cambios observables para el jugador: repaso cosmetico de las
planchas de acero (remaches, cepillado, juntas) para igualar el sello visual
entre contenedores. Versionado y documentacion al dia.

### Cambios
- Refinamiento de assets del pack `gripeverde_steel` (remaches/cepillado).
- Sin cambios de codigo Java, sin cambios de geometria ni de slots.
- Bump de version: 0.17.0 -> 0.17.1 (build.gradle + GripeVerdeMod.VERSION).
- Entrada narrativa en CHANGELOG.md raiz (bajo R-DIEGETICO).
- Changelog in-game: OMITIDO (R-CHANGELOG-DISCRECIONAL — sin manifestacion
  observable nueva para el cronista; es pulido interno del acero).

### Notas
- Entrada unica en CHANGELOG.md raiz, no en los 25 changelog/*.md in-game.
- No abre ni cierra ningun KI.

---

## ADDENDUM v0.17.2 (Las bandejas de la mesa y la moneda del trato)

### Alcance
Pase de correccion geometrica/visual sobre el acero de cuarentena. El slot
universal habia quedado en 18px (cuadrado estrecho); se llevo a 26px (bandeja
con holgura para el contenido). Se repararon los oficios cuyos sprites de
contenedor quedaron desalineados o sin tratamiento de acero, y se repasaron
todas las transparencias del pack.

### Cambios de assets (resource pack `gripeverde_steel`)
- Sprite universal `sprites/container/slot.png`: 18px -> 26px (bandeja).
- Oficios restaurados al arreglo correcto de acero: telar (loom), cantera
  (stonecutter), cartografia (cartography_table) y el trato con forasteros
  (villager trading). Cada moneda y cada sello del trato (trade_arrow,
  discount_strikethrough, experience_bar_*, out_of_stock, scroller*) vuelve
  a respirar su acero.
- Transparencias repasadas una a una (ningun velo se vuelve muro). Afecta a
  widgets y sprites con alpha: button/slider/text_field/scroller/checkbox,
  sprites de contenedores, recipe_book.
- Nuevo: sellos del archivo de telas (loom) y de la sala de mapas
  (cartography_table): pattern, banner_slot, dye_slot, pattern_slot, error,
  scroller; map/scaled_map/duplicated_map/locked/error.
- Creative inventory: tabs.png (tapa muerta) REMOVIDO. Reemplazado por los
  sprites tab_top_selected/unselected_1..7 y tab_bottom_selected/unselected_1..7.
- Task `verifyGripeVerdeThemeAssets` (build.gradle): la lista de huerfanas
  prohibidas ahora incluye `creative_inventory/tabs.png` (ademas de
  dropper.png, villager2.png, inventory_backup.png).

### Cambios de codigo
- Ninguno (todo es assets del pack). El registro del pack
  (`ResourcePackHandler`, required=true) y la geometria 1:1 no se tocan.

### Version
- 0.17.1 -> 0.17.2 (build.gradle + GripeVerdeMod.VERSION).
- CHANGELOG.md raiz: entrada narrativa bajo R-DIEGETICO.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL
  (pulido interno del acero, sin manifestacion narrativa nueva).

### Validacion en este entorno
- Sin JDK ni maven de Forge en el sandbox. Se valido programaticamente via
  `verifyGripeVerdeThemeAssets` (dimensiones, alpha, .mcmeta, ausencia de
  huerfanas). El build/validacion visual corresponde a la maquina del usuario (E125).

### Estado del pack post-v0.17.2
- 20 contenedores + 4 fondos creativos + slot universal + todos los sprites
  de oficio (hornos, alambique, baliza, yunque, encantamientos, caballos,
  telar, cartografia, cantera, herreria, comercio) + widgets vanilla
  (button/slider/text_field/checkbox/scroller) + libro de recetas.
- Geometria 1:1 verificada contra vanilla (alpha masks + slots en posicion exacta).

## PROXIMO HITO PLANIFICADO (revisado tras v0.17.2)

### Candidato C: v0.18.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema REAL de gameplay. La fase de interfaz/identidad
(pergamino + acero + lore + audio) esta cerrada y pulida; la casa esta lista
para tener contenido adentro. Sub-hitos a definir en diseno previo escrito
(antes de tocar codigo):
- Sistema de enfermedad: sintomas, progreso, contagio.
- Cuarentena: zonas seguras vs zonas infectadas.
- NPCs: enfermos, sanos, personal medico.
- Items: mascaras, medicinas, alcohol, agua limpia.
- Mecanicas ambientales: humo verde, ratas, cadaveres.
Riesgo: ALTO. Requiere diseno previo escrito antes de codigo.

### Candidato O (sin urgencia): KI-12 - Mixin dirt bypass
Reemplazar el fondo dirt vanilla de 4 pantallas de trabajo (MouseSettings,
KeyBinds, PackSelection, OnlineOptions). Ruta correcta identificada (mixin
sobre Screen.renderBackground + filtro instanceof). Infraestructura mixin
armada pero sin mixins activos (client:[] vacio en gripeverde.mixins.json).

### Deuda menor abierta
- KI-13: server pinneado editable/borrable sin bloqueo visual (baja).
- KI-14: banda sepia del multiplayer vs favicons brillantes (baja).
- KI-21: sprites vanilla no confirmados en 1.20.1 (villager flechas de
  comercio, etc.) cubiertos en v0.17.2; revisar si quedan holes.
- Sin tags publicados desde v0.13.10: considerar tagear la RC actual cuando
  se valide en cliente real.
