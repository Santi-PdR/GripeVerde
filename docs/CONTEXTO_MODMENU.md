# Contexto de continuidad: GripeVerde ModMenu

Actualizado: 2026-11-13

- **Cola de notas en ejecución, posterior a `70af1da`:** `queue` y
  `queueOnce` ignoran IDs nulos/vacíos/blancos antes de inicializar memoria.
  La inserción respeta el mismo límite de 256 notas que la carga, sin expulsar
  pendientes ni encolar guardados por duplicados o rechazos. `queueOnce`
  marca la nota solo si se aceptó o ya estaba pendiente (también con cola
  llena); un rechazo por falta de espacio permite reintentar más adelante.
  Se conservan IDs desconocidos no blancos sin trim; no se añade whitelist.
  Ampliada `verifyMemoryDataRegression` con las APIs reales y CONFIGDIR
  temporal: inválidos sin E/S, deduplicación, marcador once, cola llena,
  reintento y persistencia/recarga del orden y marcadores. Comprobaciones
  locales estructurales aprobadas; compilación y ejecución pendientes de CI.
  Sin cambio de versión/esquema ni release, ni prueba de Minecraft gráfico.

- **Marginalia: distribución y caché, posterior a `c51733d`:** la caché
  incluye la identidad de fuente, texto y límites; nunca rescata líneas de
  otra nota cuando la nueva no cabe. Cada línea se ajusta al ancho, también
  las palabras largas, sin puntos suspensivos. Si no cabe ningún texto, la
  nota activa se difiere en vez de consumir tiempo invisible.
  Nueva suite `tools/test_marginalia_layout.py`: métodos reales de distribución
  y acortado con fuente doble, 16 escenarios más guardas de integración.
  El candidato `14a1727` compiló y pasó los 16 escenarios nuevos y las suites
  previas en [CI 45062755763](https://github.com/Santi-PdR/GripeVerde/actions/runs/45062755763).
  Artefacto `GripeVerde-jar` ID `14770596108`. Localmente solo se ejecutaron
  guardas estructurales (sin JDK). No prueba rendering, recarga de recursos
  sobre la misma instancia de fuente ni temporización real.
  Sin cambio de versión, esquema de memoria ni publicación.

- **Datos nulos y límites de memoria, posterior a `b48903e`:** se retiran
  IDs nulos/vacíos de descubrimientos, objetos y notas; entradas nulas del
  libro de firmas, y claves vacías/valores nulos de contadores. La limpieza
  precede los límites de 256 notas/4096 firmas, para que los huecos no expulsen
  entradas válidas posteriores. Se conservan IDs desconocidos no vacíos y el
  texto de firmas sin trim. Las APIs de memoria ignoran IDs/claves inválidos
  sin crear memoria ni encolar guardados; no se aplica una whitelist nueva.
  `bump` satura en Integer.MAX_VALUE en vez de volver negativo, manteniendo
  valores genéricos con signo; en el máximo no encola un guardado inútil.
  Esquemas <=0 parten de 1 antes del pipeline, evitando recorridos arbitrarios
  desde un entero negativo extremo. No cambia el esquema ni se mide tiempo.
  Nueva tarea obligatoria `verifyMemoryDataRegression`, con biblioteca/Gson/
  archivos reales: limpieza antes de límites, APIs, saturación y migración.
  El candidato `2fb0c67` compiló y pasó las pruebas nuevas de datos y
  todas las suites previas en
  [CI 45061875430](https://github.com/Santi-PdR/GripeVerde/actions/runs/45061875430).
  Artefacto actual `GripeVerde-jar` ID `14770075729`. Sin JDK local ni prueba
  Minecraft/red ni bump/release. La protección de esquemas futuros sigue
  aplicándose antes de interpretar estos campos y sus regresiones pasan.
  Asset v0.401.0 reconsultado tras publish: mismo ID/tamaño/digest.

- **Memoria de esquema futuro, posterior a `0705ed6`:** migrate solo
  evitaba bajar el número de esquema, pero los guardados posteriores podían
  reescribir el JSON con Data y borrar campos desconocidos. Ahora se inspecciona
  version antes de deserializar campos. Si principal, temporal o respaldo usa
  un esquema mayor que 3, se activa solo lectura durante toda la sesión: los
  cambios quedan únicamente en memoria de sesión y se avisa en el log. No se
  interpreta el contenido futuro, migra, renombra como corrupto ni se guarda
  encima. La comprobación se repite antes de escribir para detectar archivos
  nuevos aparecidos tras cargar. Se conservan las migraciones 1/2 -> 3 y la
  recuperación de copias conocidas. Para volver a persistir hace falta
  reiniciar con archivos/mod compatibles; no borrar copias automáticamente.
  Nueva tarea obligatoria check/build `verifyMemorySchemaRegression` con
  biblioteca/Gson/archivos reales: versiones numéricas, en cadena, enormes,
  campos con tipos futuros, las tres copias, guardados y recuperación normal.
  El candidato `f46025a` compiló y pasó las pruebas nuevas de preservación,
  migración y recuperación, junto a todas las suites previas, en
  [CI 45061015824](https://github.com/Santi-PdR/GripeVerde/actions/runs/45061015824).
  Artefacto actual `GripeVerde-jar` ID `14769198919`. Sin JDK local ni prueba
  Minecraft/red. Sin bump/release ni cambio de esquema. Asset v0.401.0
  reconsultado tras publish: mismo ID/tamaño/digest.
  Alcance: metadatos version legibles; no sincroniza ediciones externas
  concurrentes con el reemplazo del archivo ni interpreta formatos futuros
  sin ese metadato.

- **Espera del writer de memoria al cerrar, posterior a `53ba411`:**
  `flushForShutdown` era synchronized y esperaba al executor mientras retenía
  el monitor que `writeNow` necesita. Si había trabajo esperando el monitor,
  el worker no podía terminar hasta agotar los 3 s de espera, y podía quedar
  una escritura redundante. La espera sale del monitor; la comprobación de
  savePending y el intento final siguen sincronizados con el writer. Se
  conserva el presupuesto de espera y el tratamiento de interrupciones.
  Nuevas pruebas: 8 escenarios de métodos reales extraídos, executor real
  vigilado y E/S simulada (incluye ownership del monitor, fallo, timeout,
  interrupción y rechazo), más 3 guardas; ambos workflows exigen JDK 17.
  `verifyMemoryShutdownRegression` añade 12 comprobaciones de la biblioteca,
  executor y JSON reales en CONFIGDIR temporal, requerida por check/build.
  El candidato `02b2b3b` compiló y pasó los 8 escenarios, 3 guardas y
  12 comprobaciones de persistencia real, junto con todas las suites previas,
  en [CI 45059924937](https://github.com/Santi-PdR/GripeVerde/actions/runs/45059924937).
  Artefacto actual `GripeVerde-jar` ID `14769047321`. Sin JDK local. No mide
  tiempos de cierre ni ejecuta Minecraft; no es una garantía de persistencia
  ante corte eléctrico. Asset v0.401.0 reconsultado tras publish: mismo
  ID/tamaño/digest.
  Sin bump/release ni cambios de esquema de memoria.

- **Centro de cabecera Multijugador, posterior a `1874d7f`:** en ventanas
  de hasta 284 px lógicos el recorte mantiene su mínimo de 64 px, pero el
  centro sigue cambiando. La caché de título/subtítulo no tenía ese centro en
  la clave, dejando posiciones viejas al redimensionar. Se incluye el centro
  sin alterar el recorte ni el tamaño de la lista. La suite de cabecera añade
  6 escenarios (centro con recorte fijo, cache hit, fuente, título, subtítulo y
  ancho), total 16 escenarios Java y 3 guardas. El candidato `16b3a9c`
  compiló y pasó las pruebas nuevas y todas las suites anteriores en
  [CI 45059302545](https://github.com/Santi-PdR/GripeVerde/actions/runs/45059302545).
  Artefacto actual `GripeVerde-jar` ID `14768558762`. Son pruebas de métodos
  reales con fuente simulada, no rendering; falta revisar en Minecraft. Sin
  JDK local ni bump/release. Asset v0.401.0 reconsultado tras publish:
  mismo ID/tamaño/digest.

- **Contador de conocidas en Multijugador, posterior a `8c8c2b8`:** la
  caché de cabecera omitía el tamaño del historial reciente. Al volver de una
  conexión directa podía conservar el valor antiguo mientras no cambiaran
  servidores guardados, idioma, fuente o ancho. Se incluye conocidas en la
  clave y se consulta el tamaño de la lista en memoria antes de refrescar.
  La composición y ajuste del texto se extraen a `refreshServerCountCache`
  sin cambiar textos, reglas de espacio ni la lista vanilla. Nueva suite
  `tools/test_server_count.py`: 10 escenarios de método Java real con fuente
  simulada y 3 guardas estructurales; ambos workflows exigen JDK 17.
  El candidato `1874d7f` compiló y pasó los 10 escenarios y suites previas
  en [CI 45059028352](https://github.com/Santi-PdR/GripeVerde/actions/runs/45059028352).
  Sin JDK local ni prueba de render, cliente o red. Sin bump/release ni cambios
  en servers.dat.

- **Historial de servidores recientes, posterior a `f4c89b4`:** devuelve
  instantáneas inmutables (sin exponer la caché mutable), y lee en streaming
  hasta tres entradas únicas, en vez de cargar todo el archivo. `push` rechaza
  controles internos y UTF-16 mal formado para no romper el formato de una
  dirección por línea; conserva Unicode válido e IPv6 sin hacer DNS ni validar
  conexiones. Usa CONFIGDIR de Forge y el mismo nombre/formato de archivo.
  Escribe en temporal vecino y reemplaza atómicamente si el sistema lo admite,
  con fallback no atómico; no trunca el destino antes de escribir. Si falla,
  conserva la visita en memoria y otro push (incluso de la misma dirección)
  vuelve a intentar guardar. Los fallos se registran en vez de silenciarse.
  Nueva tarea `verifyRecentAddressesRegression` obligatoria en check/build:
  biblioteca y archivos reales, orden/límite/duplicados, inmutabilidad,
  Unicode, cola sin leer y fallo/reintento. El candidato `82117d5` compiló
  y pasó las pruebas nuevas y suites previas en
  [CI 45053023880](https://github.com/Santi-PdR/GripeVerde/actions/runs/45053023880),
  artefacto actual `GripeVerde-jar` ID `14765514124`. Sin JDK local ni prueba
  Minecraft/red. Asset v0.401.0 reconsultado tras publish: mismo ID/tamaño/digest.
  No hay garantía de durabilidad ante corte eléctrico, reintento automático ni
  persistencia de cambios pendientes al cerrar el proceso. Sin bump/release.

- **Protección del original ilegible CEDA, posterior a `7884ead`:** si
  falla la lectura del JSON propio, antes de cualquier reemplazo (importación
  CopyL o guardado posterior) se copian sus bytes a un archivo vecino único
  `gripeverde-dispatch.json.corrupt-*.bak`. No se pisan ni borran backups
  anteriores. Si no se puede conservar el original, save devuelve false,
  mantiene cambios en memoria pendientes y no reemplaza el destino. Sin CopyL
  no se escribe nada solo por abrir; el backup se difiere al primer guardado.
  Guardados normales posteriores no crean nuevas copias del mismo original.
  La suite real `verifyDispatchLoadingRegression` añade JSON truncado,
  raíces inválidas y UTF-8 inválido, con/sin CopyL, backups previos, bloqueo
  de recuperación y reintento. Añade un escenario de permisos POSIX que se
  omite explícitamente donde no se puedan aplicar (sin POSIX o proceso root).
  El candidato `6c5fd5a` compiló y pasó las suites en
  [CI 45051992150](https://github.com/Santi-PdR/GripeVerde/actions/runs/45051992150),
  artefacto `GripeVerde-jar` ID `14765723276`. El primer run `45051605076`
  compiló pero falló por timeout al resolver mappings en Maven Forge; no por
  un fallo de compilación o aserción. Sin JDK local. Asset v0.401.0 reconsultado
  tras publish: mismo ID/tamaño/digest. No recupera automáticamente el texto
  del archivo roto; permite rescate manual. No hay
  garantía frente a corte eléctrico ni ediciones externas concurrentes.
  Sin bump/release ni prueba Minecraft.

- **Foco y overlays en atajos CEDA, posterior a `01bea15`:** el sondeo de
  atajos se suspende si la ventana no está activa o hay un overlay de carga.
  Se reinicia el estado de flancos, también sin cliente/ventana, para no
  interpretar una tecla/botón ya mantenido al regresar como pulsación nueva.
  Debe soltarse y pulsarse otra vez. No cambia teclas, options.txt ni el envío
  explícito desde la ficha. Nueva suite `tools/test_dispatch_hotkeys.py` con
  17 escenarios que ejecutan métodos Java reales extraídos usando dobles de
  cliente/GLFW, más 3 guardas estructurales. Se exige JDK 17 en ambos workflows.
  El candidato `0cfc627` compiló y pasó los 17 escenarios, las guardas y
  todas las suites previas en
  [CI 45049746885](https://github.com/Santi-PdR/GripeVerde/actions/runs/45049746885).
  Artefacto actual `GripeVerde-jar` ID `14764659379`. El primer run detectó
  variables locales duplicadas en el switch del harness; corregido solo en
  el test. No es prueba del foco del SO, input nativo, eventos Minecraft ni
  red. Sin JDK local ni prueba en cliente. Sin bump/release: asset v0.401.0
  reconsultado tras publish, mismo ID/tamaño/digest.

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

### DiaryTextField: margen lógico, posterior a `f173ab8`

- El renderer usa x+4 y ancho útil de EditBox; con bordered=false el clic
  no descontaba esos 4 px y el ancho útil era 8 px mayor que el espacio de
  texto. Se conserva bordered=true como geometría lógica, también cuando
  los helpers compartidos invocan setBordered(false). No se dibuja marco
  vanilla porque renderWidget no llama al renderer padre. Los EditBox vanilla
  siguen permitiendo su modo sin borde. Fuente contrastada: EditBox 1.20.1,
  métodos onClick/getInnerWidth, revisión `c129e3e` del repositorio citado.
- `./gradlew verifyTextFieldGeometryRegression`, incluida en check/build,
  añade 12 comprobaciones con instancias reales de DiaryTextField/EditBox:
  constructor, cambio de ancho, helpers, petición de quitar borde y aislamiento
  entre instancias. Usa fuente nula solo para comprobar geometría; no dibuja,
  no inyecta clics ni necesita una instancia de Minecraft. El candidato
  `ae796c7` compiló y pasó las suites en
  [CI 45042852283](https://github.com/Santi-PdR/GripeVerde/actions/runs/45042852283),
  incluidos 12 casos geométricos y 38 de direcciones. Artefacto actual
  `GripeVerde-jar`, ID `14762691152`, incluye las correcciones previas de
  Idiomas/scroll/Paquetes. Sin bump/release: asset v0.401.0 conserva
  ID/tamaño/digest después de publish.
- Prueba pendiente en cliente: texto largo, clic en cada extremo/interior,
  selección con Shift y ratón, desplazamiento horizontal y redimensión en
  Conexión directa/Editar servidor. Contrastar campos vanilla también.
  No se identifica este desfase como causa de los clics fantasma generales.

### Direcciones de servidor, posterior a `a1d58c7`

- DiaryChrome valida con `ServerAddress.isValidAddress` y muestra host/puerto
  con `parseString/getHost/getPort`, sin DNS. IPv6 entre corchetes ya no se
  rechaza por la whitelist, y el último segmento de IPv6 desnuda no se toma
  como puerto. Puertos numéricos fuera de rango se rechazan según vanilla;
  se mantienen fuera los espacios y controles internos. Un host sintáctico
  válido no garantiza existencia ni conectividad. Host IDN se muestra en ASCII.
- Editar servidor guarda trim de la dirección validada, igual que Conexión
  directa. No persiste cambios al cancelar ni modifica el proceso de conexión.
- `./gradlew verifyServerAddressRegression` ejecuta 38 comprobaciones de los
  helpers reales del mod contra el parser real de Minecraft: DNS/IDN, IPv4,
  IPv6, puertos y readouts en ambos idiomas. No usa DNS, sockets ni renderer.
  Requerido por check/build. Aprobado junto al build de `f173ab8` en CI
  `45042533041`; sin JDK local.
- Validar en cliente Conexión directa y Editar servidor con IPv4/DNS, IPv6
  con/sin puerto, puerto inválido y espacios exteriores. Revisar estado del
  botón, ayuda de host/puerto y valor guardado; no se hizo esa prueba aquí.

### Idiomas: reconstrucción y selección, posterior a `d4109ba`

- init conserva el idioma pendiente válido y el scroll de la lista previa;
  primera apertura (-1) centra el aplicado, y cero ya se restaura como scroll.
  Si desapareció el idioma pendiente se usa el aplicado. Se invalida la caché
  de cabecera para recalcular coordenadas al redimensionar, incluso cuando
  panelW sigue limitado a 420 px.
- LanguageList.setSelected sincroniza la selección pendiente de las flechas
  vanilla, sin aplicar aún ni convertir la fila de Niebla en idioma. No cambia
  Listo/Enter (confirma) ni Esc (cancela), ni aplica con solo mover el foco.
- `tools/test_language_screen.py`: 3 verificaciones de conexión pasan en local;
  11 casos compilan métodos reales extraídos con dobles de servicios/GUI, no la
  pantalla entera ni su despacho real de foco. Los 11 casos y la compilación
  de `a1d58c7` pasaron en CI `45042270304`; `--require-java` impide omitirlos
  allí. Las demás suites locales pasan.
- Revisión pendiente en cliente: elegir otro idioma, desplazar y redimensionar
  (también mediante fuente Unicode), comprobar selección/scroll; volver al
  principio y reabrir; flechas y Listo; flechas y Esc sin aplicar; aumentar el
  ancho de ventana con panel ya al máximo y verificar cabecera centrada.
  Sin bump/release y sin atribuir estos fallos a los clics fantasma generales.

### Scroll compartido, posterior a `9f3f9a1`

- SmoothScroll registra la lista y el último valor que escribió. Si barra,
  teclado/ensureVisible u otro código desplaza la lista, descarta el destino
  inercial antiguo; también lo hace al recibir otra instancia de lista.
  Mantiene la acumulación de rueda, setTarget, snap y la rama sin easing
  durante un mundo abierto. Afecta a los menús que ya usan este helper:
  Idiomas, Accesibilidad, Chat, Sonidos, Online y Multijugador.
- `python3 tools/test_smooth_scroll.py --require-java` compila el helper real
  con dobles mínimos de Minecraft y AbstractSelectionList y ejecuta 15 casos
  en procesos aislados: rueda/inercia, arrastre, teclado, lista nueva,
  setTarget, mundo abierto, límites, fracciones, snap y entradas nulas.
  No ejecuta el renderer ni eventos reales. En local se omitieron por falta
  de JDK; se añadieron como paso obligatorio en CI y su plantilla.
- Pasan espejos 8/8, lint 72 Java, guardas 18/18, gate 7/7 e instalador 7/7.
  El candidato `30d268f` compiló y ejecutó los 15 escenarios Java en
  [CI 45041710607](https://github.com/Santi-PdR/GripeVerde/actions/runs/45041710607).
  Prueba en Minecraft pendiente. Artefacto actual `GripeVerde-jar`, ID
  `14762220269`, incluye las correcciones previas de Paquetes. Sin bump,
  release ni cambios de gameplay; asset v0.401.0 conserva ID/tamaño/digest.
- Revisar manualmente: rueda y después arrastre de barra en ambos sentidos,
  teclado que haga visible una fila fuera de pantalla, repetir con rueda aún
  en movimiento, redimensionar y alternar menús con/sin mundo abierto. La
  lista no debe saltar de vuelta al destino viejo. No acredita clics fantasma.

### Alcance del scissor, posterior a `9dbd0ef`

- Geometría, children e índice inicial se resuelven antes de adquirir el
  recorte. `enableScissor` va inmediatamente antes del try y `disableScissor`
  en finally. Tanto banda opaca como texto/separadores quedan dentro del
  alcance protegido; se elimina el catch que permitía dibujar sin recorte.
- Guardas estáticas 18/18: seis mutaciones adicionales comprueban rechazo
  de pintura previa al recorte, activación fallida ignorada, operaciones
  desprotegidas y liberación condicional, ausente o duplicada. No inyectan
  fallos en OpenGL ni ejecutan Minecraft. También pasan lint de 72 Java,
  espejos 8/8, gate 7/7, instalador 7/7, py_compile y diff.
- El candidato `f152571` compiló y pasó las suites en
  [CI 45041210093](https://github.com/Santi-PdR/GripeVerde/actions/runs/45041210093).
  Artefacto actual `GripeVerde-jar`, ID `14761956917`, incluye los cambios
  anteriores de Paquetes. Prueba en cliente pendiente. Asset v0.401.0
  reconsultado tras publish: mismo ID, tamaño y digest. No modifica versión,
  release ni despacho de entrada. La prueba manual de scroll debe verificar
  también que encabezados y botones siguen visibles tras cada columna.

### Descripciones de dos líneas, posterior a `64261bb`

- Las descripciones normales y las de compatibilidad comparten MultiLineLabel
  de hasta dos líneas. Se mantiene el Component original, con saltos/estilos;
  título a `rowTop + 1` y descripción a `rowTop + 12`, interlínea de 10 px,
  como vanilla 1.20.1. El scissor recorta sin desplazar el origen de la fila.
- Caché por identidad de fila, acotada a 256 entradas; compara Component y
  ancho, conserva una copia del texto y se limpia en init y removed. Evita
  volver a dividir las descripciones estables en cada fotograma. No es una
  medición de rendimiento ni acredita comportamiento del cliente.
- Pasan guardas 16/16, lint 72 Java, espejos 8/8, gate 7/7 e instalador 7/7.
  Candidato `e9e4896` compilado y suites aprobadas en
  [CI 45040439067](https://github.com/Santi-PdR/GripeVerde/actions/runs/45040439067).
  Artefacto actual `GripeVerde-jar`, ID `14760938281`: incluye los candidatos
  anteriores de scroll y compatibilidad. Prueba en cliente pendiente.
  Sin bump ni release; asset v0.401.0 conserva ID/tamaño/digest tras publish.
- Añadir a la revisión en Minecraft: descripciones cortas, largas, vacías,
  con saltos explícitos y estilos; hover de incompatibilidad y vuelta al texto
  normal, escala/redimensión, salida/reapertura y recarga de packs. Confirmar
  que los tres renglones caben, el último no se corta y el texto se actualiza.

### Avisos de compatibilidad, posterior a `d1d4e83`

- El repintado ahora repone el título `pack.incompatible` y la descripción de
  `PackCompatibility` en dos líneas cuando vanilla muestra ese aviso.
  Fuente contrastada: `TransferableSelectionList.PackEntry.render` en la
  revisión 1.20.1 `c129e3e` enlazada más abajo. Conserva la condición
  `!isFixedPosition() || !isRequired()`, hover real dentro del viewport,
  selección con foco y modo táctil; no modifica handlers ni confirmaciones.
- Guardas de listas: 14/14, con once mutaciones nuevas de condiciones y
  repintado; lint de 72 Java, espejos 8/8, gate 7/7 e instalador 7/7 pasan.
  El candidato `8708784` compiló y pasó las suites en
  [CI 45039859475](https://github.com/Santi-PdR/GripeVerde/actions/runs/45039859475).
  El primer intento falló por usar Entry protegida; ahora se consulta el hover
  por la interfaz pública GuiEventListener y se protege esa decisión con lint.
  Prueba en cliente pendiente; sin bump ni release. Asset v0.401.0 sin cambios
  de ID, tamaño ni digest tras la CI. Artefacto actual: `GripeVerde-jar`,
  ID `14760749545`; reemplaza al candidato de scroll como objeto de prueba.
- Ampliar la prueba manual a packs compatibles e incompatibles, disponibles
  y seleccionados, hover de icono/texto/márgenes, navegación por teclado,
  modo táctil y entradas fijas/obligatorias. Verificar que el aviso aparece
  solo donde vanilla lo mostraría, desaparece al perder hover/foco y que
  la confirmación antes de activar un pack incompatible sigue funcionando.

### Candidato `4d37582`, posterior a `0aa6a25`

- Paquetes mantiene texto y separador en las coordenadas reales de la fila
  al recortar por scroll. No recalcula el origen del texto con `drawTop` ni
  oculta toda la fila cuando solo quedan unos píxeles visibles; usa el scissor.
- `tools/test_list_render_guards.py`: 12/12, incluyendo mutaciones de las
  cinco condiciones de recorte y el modelo de 4.536 casos antes ejecutado
  de forma auxiliar. Conectado al workflow activo y a la plantilla de CI.
- Pasan espejos 8/8, lint (72 Java), gate 7/7, instalador 7/7 y py_compile.
  El candidato `4d37582` compiló en
  [CI 45034644163](https://github.com/Santi-PdR/GripeVerde/actions/runs/45034644163),
  incluida la suite de listas. No se probó en Minecraft. No cambia la versión
  ni el release: asset de v0.401.0 reconsultado, mismo ID, tamaño y hash.
- Candidato para prueba manual: artefacto `GripeVerde-jar` de esa ejecución
  (ID `14758874321`). No confundirlo con el JAR del release v0.401.0:
  comparten número de versión, no el código. `install-latest.ps1` instala el
  release, no este candidato. No se ejecutó ningún instalador en la PC del usuario.

#### Comprobación pendiente en cliente (no realizada)

1. Descargar el artefacto de la ejecución exacta y probar en una instancia
   de prueba, con un solo JAR de GripeVerde. No mezclar candidato y release.
2. En ambas columnas de Paquetes, desplazar lentamente hasta que primera y
   última fila estén recortadas: texto e icono deben avanzar juntos, sin texto
   pegado al borde ni separadores que aparezcan antes de su fila.
3. Con más de 64 packs, pasar el índice 64 y llegar al final: comprobar nombres,
   descripciones y retorno al principio, sin filas borradas por el repintado.
4. Repetir con escalas de GUI distintas y tras redimensionar/reabrir la pantalla.
5. Comprobar activar, quitar y reordenar desde el icono; scroll y arrastre de
   barra; navegación por teclado, Enter y Shift+flechas. No cambiaron sus handlers.
6. Revisar también packs incompatibles y sus avisos/confirmaciones: su apariencia
   no queda acreditada por esta corrección de scroll ni por las pruebas estáticas.

## Proyecto y reglas de trabajo

- Repositorio: [Santi-PdR/GripeVerde](https://github.com/Santi-PdR/GripeVerde), público.
- Rama activa de la sesión: `arena/48be547c-gripeverde`, basada en `02e85bc`.
  La plataforma fija el nombre: trabajar y publicar solamente en esta rama.
- **Última publicación verificada: v0.401.0**, tag dirigido al commit
  `e0f845007681e92e747e04127169f483e159456b`; CI `44997077455` verde en
  `build` y `publish`. JAR de 10.855.632 bytes, SHA-256
  `dd37d443b7307c1b3d0d09fc5befdaee6d1dd887bfca002d6e7bc3670ef0ee88`.
- v0.401.0 alinea el fallback de Mundos con el orden de redondeo de vanilla
  para el scroll fraccionario. Es acotado, no resuelve por sí solo el reporte
  de clics fantasma y no cambia el diseño visual. Paquetes, Un Jugador y la
  revisión de interacción siguen pendientes de prueba dentro de Minecraft.
- Candidato sin bump/publicación en Paquetes: los rótulos propios quedaban
  debajo del repintado opaco y la franja borrada no cubría los 157 px de texto
  que usa vanilla. Se restauran los encabezados al final, se amplía la cobertura
  hasta después del icono (donde viven sus flechas), se usa la descripción
  extendida y se limita el papel al viewport. El repintado ahora sigue el scroll
  sin cortar las listas en 64 packs. Despacho vanilla intacto; confirmar aspecto
  y acciones en Minecraft antes de publicar.
- Contraste de fuentes para este candidato:
  [TransferableSelectionList 1.20.1](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/client/gui/screens/packs/TransferableSelectionList.java)
  fija filas de 36 px, encabezado de 13 px, icono de 32 px, texto desde
  `rowLeft + 34`, ancho máximo de 157 px y flechas/clics sobre el icono.
  [AbstractSelectionList](https://github.com/Blackjack200/minecraft_client_1_20_1/blob/c129e3e3e6ca89a731d4cfc35bb606c6bd2cae2c/net/minecraft/client/gui/components/AbstractSelectionList.java)
  calcula `rowTop = y0 + 4 - (int) scroll + index * itemHeight + headerHeight`.
  [El parche Forge 1.20.1](https://github.com/MinecraftForge/MinecraftForge/blob/1.20.1/patches/minecraft/net/minecraft/client/gui/components/AbstractSelectionList.java.patch)
  añade `getTop/getBottom/getLeft/getWidth`, usados por el mod.
  `python3 tools/test_list_render_guards.py` pasa 9/9: muta fuentes en memoria
  para comprobar que el lint rechaza regresiones; no compila ni ejecuta Java.
  También pasan espejos 8/8, lint completo (72 Java), gate 7/7, instalador 7/7,
  py_compile y diff. El commit `1532c19` compiló correctamente en
  [CI 44999893206](https://github.com/Santi-PdR/GripeVerde/actions/runs/44999893206).
  El modelo aritmético del viewport pasó 4.536 casos, sin ejecutar Java.
- El API de GitHub vuelve a informar el mismo tamaño y digest para el asset
  `778229607` de v0.401.0; el tag sigue en `e0f8450`. No se ha inspeccionado
  de nuevo su contenido binario. El candidato sigue separado del release.
- El usuario quiere seguir mejorando el mod de forma continua hasta que diga que pare.
  El trabajo se hace sobre el repositorio; no se edita el proyecto local del usuario.
- No se controla Minecraft ni la PC del usuario para pruebas visuales. El CI acredita
  compilación, recursos y localización; no sustituye la revisión dentro del juego.
  **Pendiente de confirmación del usuario:** Paquetes de recursos, clics fantasma,
  interfaz de Un Jugador y revisión de Mundos de las entregas anteriores.
- Las interfaces del juego se mantienen instantáneas: no se agregan animaciones al
  inventario, la pausa ni el mundo. Las únicas piezas animadas siguen siendo las del
  menú y respetan Movimiento reducido y Destellos reducidos.
- **Entrega de cada versión:** instalador PowerShell para descargar el JAR más
  reciente, verificar tamaño/SHA-256 y metadata interna, sustituir sin guardar
  copias de respaldo y limpiar la carpeta heredada si existe. No se guarda JAR
  generado en el repositorio ni en el workspace.
- `tools/install-latest.ps1` consulta metadatos del API público, descarga el asset
  directamente (API como fallback) y valida tamaño, digest, `META-INF/MANIFEST.MF`,
  `mods.toml`, ID y versión antes de instalar. Comprueba el archivo instalado y
  solo entonces retira JAR viejos de GripeVerde y la carpeta heredada
  `<instancia>\gripeverde-jar-backups`; no crea backups. El usuario pidió que el
  instalador no se ejecute desde este sandbox en su PC.
- Los binarios de release no se descargan desde este sandbox: el host del asset no está
  en su allowlist. Los metadatos y digest sí se consultan por `api.github.com`.
- `.github/workflows/build.yml` compila `main` y `arena/**`, corre además los tests
  estáticos del instalador y publica automáticamente cuando `release-tag.txt` coincide
  con la versión de `build.gradle`. Las operaciones GitHub se hacen con `git`/`gh`;
  si falla la autenticación, pedir que el usuario reconecte GitHub en Arena. Nunca
  pedir tokens ni credenciales por chat.

## Estado histórico al retomar (0.209.0; 2026-11-13)

- `0.209.0` **publicada y en verde**: commit `83733da`, run `43681287004` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.209.0/gripeverde-0.209.0.jar`
  (9.623.762 bytes, `sha256:6a078e063d7424a93e52279097372bdf163b2e3282669a0e3d2fb3e7f22d853a`).
- `0.208.0` **publicada y en verde**: commit `171c786`, run `43680897376` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.208.0/gripeverde-0.208.0.jar`
  (9.618.270 bytes, `sha256:b7be58bd82c93b595b48c421868c2a7546586452576aa9aa0178487a85bcafa7`).
- `0.207.0` **publicada y en verde**: commit `3339b83`, run `43680333979` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.207.0/gripeverde-0.207.0.jar`
  (9.611.076 bytes, `sha256:fed7989cc2f0bf19f966067a904899fff2d5294d45b182d0cc2611fb2d992efa`).
- `0.206.0` **publicada y en verde**: commit `b8e9568`, run `43679757158` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.206.0/gripeverde-0.206.0.jar`
  (9.604.355 bytes, `sha256:899a9cb01931c63b1997b544e5412773ad156343ff7da65929e9e8efda49be2a`).
- `0.205.0` **publicada y en verde**: commit `1843b50`, run `43678725728` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.205.0/gripeverde-0.205.0.jar`
  (9.597.318 bytes, `sha256:56fcb8653c61033b4db097f460ebc91fe31a34b216147efb69959604767e1990`).
- `0.204.0` **publicada y en verde**: commit `0fa48d4`, run `43678068929` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.204.0/gripeverde-0.204.0.jar`
  (9.590.921 bytes, `sha256:26ed240c5a410c68d0d8d3be98e152b5539eeab524b33570328abe48f0090b0b`).
- `0.203.0` **publicada y en verde**: commit `47233a2`, run `43677388823` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.203.0/gripeverde-0.203.0.jar`
  (9.584.659 bytes, `sha256:7fd891fe40de4d9e42fd8cba0ce9684c367aeb91f60d73cab4ed75c278b7935c`).
- `0.202.0` **publicada y en verde**: commit `bb52060`, run `43676713476` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.202.0/gripeverde-0.202.0.jar`
  (9.577.020 bytes, `sha256:43f2d2c29fe7b0a4268f4f9976138101b3af9d9dbab3c40d6d212db62b228069`).
- `0.201.0` **publicada y en verde**: commit `3d4157d`, run `43676268367` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.201.0/gripeverde-0.201.0.jar`
  (9.570.861 bytes, `sha256:6d4ae97c63f33b1efcf51f8347d2d018ee2d5eedaf37138bf6af47668a1f2544`).
- `0.200.0` **publicada y en verde**: commit `f93a043`, run `43675423197` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.200.0/gripeverde-0.200.0.jar`
  (9.566.187 bytes, `sha256:2f90547d814b5476b83f8df9db900a8f8a794345577753304d027f4003ba40e2`).
- `0.199.0` **publicada y en verde**: commit `0484f09`, run `43674954683` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.199.0/gripeverde-0.199.0.jar`
  (9.560.605 bytes, `sha256:bc70d5fe53c6c30dbe869c5950c88b10c85bce51c2bbaf7ff7bb0e78d198773f`).
- `0.198.0` **publicada y en verde**: commit `d02b0a6`, run `43674325866` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.198.0/gripeverde-0.198.0.jar`
  (9.553.686 bytes, `sha256:f6c6ada8e9d70b34d24f46f8bce19aa3fc1566da188f85dbf4fd112c1e25bfe2`).
- `0.197.0` **publicada y en verde**: commit `581aa08`, run `43673937244` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.197.0/gripeverde-0.197.0.jar`
  (9.547.303 bytes, `sha256:312ac52d54c55172432241720d62392c8fda32ceaae6ac0a11cd06d5848fc586`).
- `0.196.0` **publicada y en verde**: commit `1cb5ae5`, run `43673361232` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.196.0/gripeverde-0.196.0.jar`
  (9.539.559 bytes, `sha256:b11fd71b430a7aa4d803c11d46027858a5b00be42b12a04b6e5102abf5b3288a`).
- `0.195.0` **publicada y en verde**: commit `e047359`, run `43672735772` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.195.0/gripeverde-0.195.0.jar`
  (9.534.213 bytes, `sha256:f5ba3395f4dec0029e9533a9363c33868a166474013e7ba319d7e331ad49c2a0`).
- `0.194.0` **publicada y en verde**: commit `1254406`, run `43671291169` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.194.0/gripeverde-0.194.0.jar`
  (9.525.523 bytes, `sha256:63d821846c31939af4da52f94711aebe9e32bc6925af2a92c0a7abb01efb2046`).
- `0.193.0` **publicada y en verde**: commit `1ee2cb9`, run `43670254939` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.193.0/gripeverde-0.193.0.jar`
  (9.518.959 bytes, `sha256:2afad2ca517502e77a685b0ea463c316852d70316b801d9d11e691663bcb7530`).
- `0.192.0` **publicada y en verde**: commit `b659159`, run `43669485111` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.192.0/gripeverde-0.192.0.jar`
  (9.507.369 bytes, `sha256:3cb607b2d9d3e648800f18cee2c4156a1fbdff6e4eead097315015fd42b1ed45`).
- `0.191.0` **publicada y en verde**: commit `53c70d4`, run `43667386955` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.191.0/gripeverde-0.191.0.jar`
  (9.500.074 bytes, `sha256:e0fa2517c614525844934e6b48dd96a4268af4b86288cb68a5efb3d6f1fdfc77`).
- `0.190.0` **publicada y en verde**: commit `d17e159`, run `43665994353` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.190.0/gripeverde-0.190.0.jar`
  (9.489.144 bytes, `sha256:47ccc41a4b6682429a5c65c3c00180a30c9dde713bb272168e7160d5638c2167`).
- `0.189.0` **publicada y en verde**: commit `aaffb19`, run `43661318515` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.189.0/gripeverde-0.189.0.jar`
  (9.479.721 bytes, `sha256:d9a0ac644a1fc29bb0e8065c13b4a3dec4ad876bfb0b0c8b95e1d3aee72cf76b`).
- `0.188.0` **publicada y en verde**: commit `f3f9ebe` (código) + `4f6a742` (marca de release),
  runs `43563691927` y `43563871253` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.188.0/gripeverde-0.188.0.jar`
  (9.468.634 bytes, `sha256:f6156383f502a2cefe12a83f7fd53661492bcb430e8860be734e601052a1f429`).
- `0.187.0` **publicada y en verde**: commit `66892bf`, run `43562261753` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.187.0/gripeverde-0.187.0.jar`
  (9.473.682 bytes, `sha256:de2af5753eec1eaecd33f5b4fd9125b6b28f1b531f50aa755b5387eaabb7ef21`).
- `0.186.0` **publicada y en verde**: commit `9e04eea`, run `43562046827`; 9.467.409 bytes,
  `sha256:e45043063a440962b25937d0555a45323971cba0b03f170b6f935837d9b2ec3c`.
- `0.185.0` **publicada y en verde**: commit `1d78204`; 9.457.763 bytes.
- `0.184.2` **publicada y en verde**: commit `2fb99d2`, run `43560967913` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.2/gripeverde-0.184.2.jar`
  (9.454.696 bytes, `sha256:b0dd549004183497b8d21a932a4ea69b33603483ff056d07a3f3b4e6f5fcf148`).
- `0.184.1` **publicada y en verde**: commit `0f333ce`, run `43560722736` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.1/gripeverde-0.184.1.jar`
  (9.451.941 bytes, `sha256:61ef4579f9d005eaee844f33f1d03e8864d98ae619e97a39d59616c34e9a1d08`).
- `0.184.0` **publicada y en verde**: commit `b2057af`, run `43560446633` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.0/gripeverde-0.184.0.jar`
  (9.447.179 bytes, `sha256:7540d78f63b947b50cea73dfc630873a46d26074f715b38e2c970619f474b22d`).
- `0.183.0` **publicada y en verde**: commit `26a2767`, run `43559194671` (`build` y
  `publish` en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.183.0/gripeverde-0.183.0.jar`
  (9.453.806 bytes, `sha256:0eeed14773484cb9d79b2071c2006e40beebd5aa57e154489a8bc1761792f9ef`).
  El intento previo `8ced11e` cayó por un import faltante de `DiaryChrome` en
  `ChangelogScreen`.
- **Rutina del usuario:** cuando dice «Sigue» hay que trabajar el mod al menos 30 minutos
  (mejoras, errores, interfaces, fondo, efectos, acciones, fuentes) y cerrar con un resumen
  en lista de lo añadido.
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


## Tandas de esta rama (15)

| # | tema | versión |
|---|---|---|
| 1 | llaves y portada (solo `Ctrl+S`, sin carteles) | 0.269.0 |
| 2 | mundos guardados: peso, fecha, filtro, folio, sonidos | 0.270.0 |
| 3 | puestos: peso de `servers.dat`, aviso vacío, folio | 0.272.0 |
| 4 | todas las hojas: papel al abrir, puerta al cerrar, chapas únicas | 0.272.0 |
| 5 | controles: choques y teclas sin asignar | 0.273.0 |
| 6 | reloj: segundero, muesca de las 11:52, columna de datos | 0.274.0 |
| 7 | pausa: hora, clima, modo, dificultad, almas, semilla | 0.278.0 |
| 8 | diario: última lectura, progreso, final del cuaderno | 0.278.0 |
| 9 | parte: foco en la primera vacía, «faltan N» | 0.278.0 |
| 10 | arsenal: Forge, `REG. XVII`, folio | 0.278.0 |
| 11 | aspecto: sonido al cambiar, tipo de piel | 0.279.0 |
| 12 | muerte: causa, lugar, coordenadas, día, hora, nivel | 0.279.0 |
| 13 | idioma y packs: folio y salida | 0.280.0 |
| 14 | diario: la lupa devuelve el punto de lectura | 0.280.0 |
| 15 | repaso global: guardas y documento | 0.281.0 |

## Revisor propio de Java

`tools/lint_java.py` corre en CI antes de Gradle. Caza tres cosas que costaron
builds rojos: `@Override` huérfano, variables locales repetidas en ámbitos
anidados y sentencias cortadas (línea que empieza por operador tras un `;` o `}`).


## Segunda vuelta (tandas 16–23)

| # | tema | versión |
|---|---|---|
| 16 | chat, online y accesibilidad: resumen en palabras | 0.282.0 |
| 17 | portada: la firma abre créditos, el distintivo abre sonido | 0.283.0 |
| 18 | entrada directa: tres direcciones recordadas | 0.284.0 |
| 19 | hojas: estado de las tres capas de sonido, avisos y folios | 0.285.0 |
| 20 | diario y parte: sin pistas de atajos, memoria visible, ranuras sin tecla | 0.286.0 |
| 21 | mundos y puestos: la ficha y la puerta elegidas | 0.288.0 |
| 22 | reloj y portada: marcas de minuto, relojero, cuerda, sin leyenda | 0.288.0 |
| 23 | repaso: guardas de ancho y alto, tablas y folios | 0.289.0 |
| 24 | diario: palabras del cuaderno y dias leidos en el indice | 0.290.0 |
| 25 | pausa: coordenadas, fps y copiar la semilla | 0.291.0 |
| 26 | fichas y creditos: por donde va el archivo | 0.292.0 |
| 27 | reparaciones: zonas fantasma, version del cuaderno | 0.293.0 |
| 28 | cierre de la segunda vuelta: tablas y registro | 0.294.0 |
| 29 | HUD: el reloj de bolsillo aparece en partida | 0.295.0 |
| 30 | la casa: hoja propia con los interruptores | 0.296.0 |
| 31 | la casa: volumenes y volver todo a su sitio | 0.297.0 |
| 32 | la ficha nueva avisa dentro de la partida | 0.298.0 |
| 33 | la casa: el cuadro de lo que ya sabe | 0.299.0 |
| 34 | cierre de la tercera pasada: cola de fichas | 0.300.0 |
| 35 | repaso de solapamientos: reloj, hojas y muerte | 0.301.0 |
| 36 | arsenal con descripcion y mundos con ficha | 0.302.0 |
| 37 | puestos: nombre y cartel en tinta | 0.303.0 |
| 38 | mundos en tinta y subtitulos acotados | 0.304.0 |
| 39 | pausa: dia y lugar al pie; casa avisa del mapa | 0.305.0 |
| 40 | folios fuera y arsenal con identificador | 0.306.0 |
| 41 | la casa se abre desde la portada | 0.307.0 |
| 42 | coordenadas en su papelito | 0.308.0 |
| 43 | arsenal: la ficha del mod en papel | 0.309.0 |
| 44 | chat y en linea: estado en la cabecera | 0.310.0 |
| 45 | paquetes: titulo y botones de la casa | 0.311.0 |
| 46 | el reloj de la partida dice la fecha | 0.312.0 |
| 47 | multijugador: pie ordenado y puertas conocidas | 0.313.0 |
| 48 | la hoja de video, vestida | 0.314.0 |
| 49 | el README para pasar el mod de mano | 0.315.0 |
| 50 | la casa entra en ventanas bajas | 0.316.0 |
| 51 | la portada tiene imagen (ilustracion) | 0.317.0 |
| 52 | el recuento cede antes que pisar el titulo | 0.318.0 |
| 53 | conexion directa: recientes acotadas al papel | 0.319.0 |
| 54 | con minimapa, el reloj va a la columna derecha | 0.320.0 |
| 55 | pausa: el estado se aparta de la vela | 0.321.0 |
| 56 | las filas LAN llevan recorte oscuro | 0.322.0 |
| 57 | la cabecera LAN se reescribe en tinta | 0.323.0 |
| 58 | las fichas del diario se piden sitio | 0.324.0 |
| 59 | la casa se abre desde los ajustes de campo | 0.325.0 |
| 60 | el aviso de ficha cuelga debajo de las lentes | 0.326.0 |
| 61 | estado del proyecto al dia | 0.327.0 |
| 62 | recuento y folio se remiden al redimensionar | 0.328.0 |
| 63 | el aviso baja al rincon en portadas bajas | 0.329.0 |
| 64 | las rejillas se anclan al pie | 0.330.0 |
| 65 | el mod ya tiene su sello | 0.331.0 |
| 66 | el pie de Sonido se calla en hoja apretada | 0.332.0 |
| 67 | la documentacion dice la verdad de hoy | 0.333.0 |
| 68 | la puerta de un jugador vuelve a la portada | 0.334.0 |
| 69 | un jugador en ventanas bajas y apretadas | 0.335.0 |
| 70 | la aguja del mando no cruza la etiqueta | 0.336.0 |
| 71 | la lista de Accesibilidad no se corta a media ficha | 0.337.0 |
| 72 | el pie de Multijugador deja libre la fila de fichas | 0.338.0 |
| 73 | la hoja de Mundos guardados ya no se parte en vertical | 0.339.0 |
| 74 | la pausa pierde dia, hora y coordenadas; el parte de muerte se aparta | 0.340.0 |
| 75 | el papel de coordenadas adopta el parte del minimapa | 0.341.0 |
| 76 | la firma de la portada y el grabado del reloj se leen enteros | 0.342.0 |
| 77 | las cajas grises del juego se acompasan al papel | 0.343.0 |
| 78 | las filas del juego se vuelven fichas de registro | 0.344.0 |
| 79 | el parte de muerte ya no repite la causa del juego | 0.345.0 |
| 80 | el archivador de paquetes se acompasa al papel | 0.346.0 |
| 81 | el arsenal de mods deja de repetir tiras | 0.347.0 |
| 82 | la ficha del proyecto se pone al dia | 0.348.0 |
| 83 | los pies de teclas dejan el canto roto | 0.349.0 |
| 84 | el pie de Sonido sube del canto | 0.350.0 |
| 85 | el reloj del HUD deja de recortar el clima | 0.351.0 |
| 86 | Ronda de arreglos visuales por hojas | 0.352.0 |
| 87 | Ronda de arreglos visuales por hojas | 0.353.0 |
| 88 | Ronda de arreglos visuales por hojas | 0.354.0 |
| 89 | Ronda de arreglos visuales por hojas | 0.355.0 |
| 90 | Ronda de arreglos visuales por hojas | 0.356.0 |
| 91 | Ronda de arreglos visuales por hojas | 0.357.0 |
| 92 | Ronda de mejoras de menu: mandos y fondo | 0.358.0 |
| 93 | Ronda de mejoras de menu: mandos | 0.359.0 |
| 94 | Ronda de mejoras de menu: rendimiento y hojas | 0.360.0 |
| 95 | Ronda de mejoras de menu: hojas y HUD | 0.361.0 |
| 96 | Ronda de mejoras de menu: buscadores | 0.362.0 |
| 97 | Ronda de mejoras de menu: coordenadas fuera | 0.363.0 |
| 98 | Ronda de mejoras de menu: hojas y mandos | 0.364.0 |
| 99 | Ronda de mejoras de menu: mundos, muerte y packs | 0.365.0 |
| 100 | Ronda de mejoras de menu: mundos y muerte | 0.366.0 |
| 101 | Ronda de mejoras de menu: JourneyMap y Multijugador | 0.367.0 |
| 102 | Ronda de mejoras de menu: portada, JourneyMap y arsenal | 0.368.0 |

## Folios del gabinete

| chapa | hoja |
|---|---|
| I | opciones |
| II | sonido |
| III | controles |
| IV | idioma |
| V | chat |
| VI | accesibilidad |
| VII | pausa |
| VIII | aspecto |
| IX | créditos |
| X | puestos |
| XI | editar puesto |
| XII | entrada directa |
| XIII | parte de CEDA |
| XIV | confirmación |
| XV | cuaderno (changelog) |
| XVI | recursos |
| XVII | arsenal |
| XVIII | mundos guardados |

## Cambios de 0.368.0 — Portada con paralaje, anillo del mapa en dos envíos y arsenal de dos renglones

- Portada: la niebla baja y la alta se corren con el puntero, en sentidos contrarios, para separar los planos (con movimiento reducido queda quieta). JourneyMap: la geometria del anillo se calcula solo al cambiar tamano o posicion y se dibuja en dos tandas, en vez de cuatrocientas llamadas de relleno y sus senos por cuadro. Arsenal: la descripcion del mod bajo la mano se dobla en dos renglones. Revision: se repasaron todas las busquedas por nombre de clase, campo o metodo del juego y no queda ninguna en las pantallas.

## Cambios de 0.367.0 — JourneyMap con canto de hoja y los botones de Multijugador encendidos

- JourneyMap 5.10.3: capa propia encima del HUD que dibuja el canto de hoja alrededor del minimapa circular (y marco de papel si es cuadrado), y tema del mapa copiado en memoria con fondo de papel y tinta para las coordenadas, el bioma y la hora. Multijugador: Editar y Borrar se deciden por la ficha del servidor y no por el nombre de la clase (que va ofuscado), la cabecera de red local se reconoce igual, y el nombre, la dirección y el MOTD se leen de frente. Mundos: el modo de juego ya no se busca por nombre de metodo.

## Cambios de 0.366.0 — Mundos con fichas de verdad y el parte de defunción sin letra del juego

- Mundos: la ficha de cada mundo se busca por TIPO (LevelSummary) y no por nombre de clase, que en produccion va ofuscado y nunca coincidia: de ahi las filas en blanco con la letra del juego encima. La hoja de la lista es opaca (sin fantasma), los numeros de orden van sobre el papel y la hoja baja hasta la segunda fila de botones. Muerte: los rotulos del juego (titulo, causa y puntaje) se apagan por tipo de campo, sin depender de nombres; la causa se guarda antes de apagarla y el parte queda centrado.

## Cambios de 0.365.0 — Mundos como Multijugador, parte de defunción centrado y packs sin solape

- Mundos: hoja a lo ancho con 32 px de margen, hoja de registro pegada sobre el fondo de tierra de la lista y fichas de la casa encima (número de orden, nombre, modo y fecha); buscador dentro de la hoja; el pie de botones queda sobre el mismo papel; alto de renglón con respaldo si el juego no lo deja leer. Muerte: el lienzo se vuelca antes de pegar el parte, que ahora se centra en el hueco y siempre tapa rótulo, causa y puntaje. Packs: franja de cabecera opaca que cubre el título y el subtítulo del juego y los escribe en tinta. Lenguas: fuera la cuenta de lenguas y el «ahora: …» del margen.

## Cambios de 0.364.0 — Hojas más juntas y mandos que se leen

- Hojas: Créditos sube sus dos fichas y las centra sobre el papel, con el filete del pie por encima; Mundos guardados ciñe la hoja a la lista y a sus botones, con numero de orden en el margen; el Arsenal clava la descripcion al canto derecho en 210 px y la dobla por palabras hasta llenar la hoja. Mandos: Musica y Sonido pasan a etiqueta arriba y carril abajo con mando chico, y el volumen de interfaz se mueve al arrastrar. Muerte: el parte se asienta sobre fondo opaco y ya no se transparenta el rotulo del juego. Fondo y optimizacion: calima de horizonte en la portada y cielo abreviado en las hojas del diario, sin estrellas ni pajaros por cuadro.

## Cambios de 0.363.0 — Fuera la opción de coordenadas del reloj; las escribe el mapa

- HUD: se retira la opcion watchCoords y el papelito de coordenadas; el reloj queda con hora, dia, clima y rato dentro. La casa gana el pestillo de la pausa propia en el sitio que dejo libre. Sonido: el volumen de interfaz se mueve mientras arrastras.

## Cambios de 0.362.0 — El buscador del arsenal también va sobre papel

- Arsenal (XVII): el buscador de Forge pierde el borde y el fondo, con papelito de la casa por detras, letra en tinta y filete de foco, igual que el de Mundos guardados.

## Cambios de 0.361.0 — Mundos guardados con fichas sobre el papel, como Multijugador

- Mundos: fuera la banda oscura de cada mundo; ahora filete fino entre fichas y velo tibio bajo la mano, igual que Multijugador. HUD: el ancho de la tira se mide con cache y, con minimapa, ni se calculan las coordenadas ni el bioma.

## Cambios de 0.360.0 — Reloj y fechas con un solo calendario, y la hoja de mundos ya no se monta sobre la cabecera

- Optimizacion: el reloj de la portada ya no arma cuatro Calendar y tres arrays por cuadro; fecha del parte, del registro de mundos y del pie de Multijugador cacheadas por minuto. Mundos: la hoja empieza donde termina la cabecera, sin canto desgarrado cruzado. Mandos bajos: sin muescas cuando no cabe la etiqueta.

## Cambios de 0.359.0 — Mandos legibles en ventanas bajas y teclas Inicio/Fin

- Mandos: con menos de 18 px de alto el carril se pega al borde, las muescas bajan y la etiqueta sube (antes la letra caia sobre el carril). Inicio y Fin llevan el mando a 0 % y 100 % de un golpe en los tres deslizadores.

## Cambios de 0.358.0 — Mandos sin tirones, niebla con paralaje y mundo guardado con fecha en columna

- Mandos: applyValue ya no se llama dos veces por cuadro y el JSON de config se guarda al soltar (Sonido, La casa). Portada: segunda niebla mas alta que corre al reves, con paralaje. Hojas: charco de luz de la lampara para que el papel tenga contraste. Mundos: el modo a la izquierda y la fecha pegada al margen derecho; el parte de defuncion cachea lugar y momento.

## Cambios de 0.357.0 — La pantalla de muerte se queda con el parte en el centro y sin el rótulo del juego

- Muerte: el papel pasa al centro y tapa el rotulo «Estas muerto», la causa y el puntaje; el parte es lo unico que se lee y las fichas quedan debajo con su aire.

## Cambios de 0.356.0 — El buscador de Mundos guardados deja la caja negra y pasa a papel

- Mundos guardados: el campo de busqueda ya no es una caja negra vanilla; papel de la casa, letra en tinta y filete de foco. Chat: la lista garantiza un renglon en ventanas muy bajas.

## Cambios de 0.355.0 — Ventanas bajas: la hoja de Accesibilidad se comprime y su pie deja de pisar la lista

- Accesibilidad: por debajo de ~210 px de panel la cabecera baja a 34 y el pie a 80; se calla la frase rotativa y el recuento del margen deja de pisar el título. Chat: la lista garantiza siempre un renglón aunque la ventana sea muy baja.

## Cambios de 0.354.0 — El reloj deja las coordenadas al mapa, la hoja de mundos llega a los botones y las columnas de packs se visten de papel

- Reloj/HUD: con JourneyMap (o Xaero) el reloj ya no repite las coordenadas en un papelito aparte; el parte del mapa es el único.\n- Mundos guardados: la hoja baja hasta el grupo de botones y se va el papel pegado del pie.\n- Packs: las dos columnas llevan velo tibio y marco de papel, sin tocar su letra ni sus mandos.

## Cambios de 0.353.0 — Sombras que ya no se transparentan bajo la hoja y cabecera limpia en Packs

- Sombras: se dibujan solo por fuera del panel, en dos bandas con caída suave; dejan de verse como un manchón bajo el papel.\n- Packs: la franja de cabecera se limpia de borde a borde y el título va en tinta, como en Multijugador.

## Cambios de 0.352.0 — Portada sin ficha de jugador, hoja de mundos a lo ancho y listas que no se salen del papel

- Portada: fuera las tres fichas «Un jugador»; el cuaderno se abre con Ctrl+S.\n- Mundos guardados: la hoja ocupa el ancho y reintenta sus medidas, para que las filas caigan dentro del papel.\n- Sonido, Accesibilidad y Chat: la lista se recorta al papel.\n- Deslizadores de Sonido: rótulo medido y partido si el mando lo cruza.\n- Idioma: la cuenta de lenguas y el «ahora:» ya no se enciman con el subtítulo.

## Cambios de 0.351.0 — El reloj del HUD deja de recortar el clima

- HUD: la tira del reloj se mide con su texto (hasta 200 px) y el clima ya no sale recortado; el lacre sigue en su esquina.

## Cambios de 0.350.0 — El pie de Sonido sube del canto

- Sonido: el resumen y el folio del pie suben seis píxeles y dejan el canto roto libre.

## Cambios de 0.349.0 — Los pies de teclas dejan el canto roto

- Conexión rápida y Editar servidor: el pie de teclas sube sobre las fichas y se calla si no hay hueco.

## Cambios de 0.348.0 — La ficha del proyecto se pone al dia

- Documentación: CONTRIBUTING con la versión publicada, KNOWN_ISSUES en 0.348.0 con lo cerrado y lo abierto, y CONTEXTO con la tanda 0.336.0–0.347.0.

## Cambios de 0.347.0 — El arsenal de mods deja de repetir tiras y de pisar el pie

- Lista de mods: la tira de papel de cada renglón ya no se encima con la siguiente.
- Aviso «Esc vuelve»: sube del canto del pie y exige ancho mínimo.
- Limpieza: fuera el fichero auxiliar que se coló en 0.346.0.

## Cambios de 0.346.0 — El archivador de paquetes se acompasa al papel

- Paquetes de recursos: velo cálido y filetes de tinta sobre las dos listas, sin tocar su letra.

## Cambios de 0.345.0 — El parte de defunción deja de repetir la causa del juego

- Muerte: la causa se lee una vez y se calla la del juego; el rótulo grande y el puntaje no se tocan.

## Cambios de 0.344.0 — Las fichas del juego se vuelven fichas de registro

- Sonido, Chat y Accesibilidad: fichas de registro (banda, filete y etiqueta en tinta clara) sobre las filas del juego.
- Los deslizadores recuperan carril y aguja con su valor real.

## Cambios de 0.343.0 — Las cajas grises del juego se acompasan al papel

- Sonido, Chat y Accesibilidad: velo cálido y filetes de tinta sobre las fichas vanilla, sin tocar su letra ni sus mandos.

## Cambios de 0.342.0 — La firma de la portada y el grabado del reloj se leen enteros

- Portada: margen derecho y separación entre la firma vigente y la tachada; si no caben, solo la vigente.
- Inspección del reloj: el grabado de la tapa sube de contraste y se calla cuando no hay alto.

## Cambios de 0.341.0 — El papel de las coordenadas adopta el parte del mapa

- HUD: con minimapa, el papel de coordenadas muestra x · z · y y el bioma, en nuestro papel y nuestra letra.
- El minimapa no se modifica.

## Cambios de 0.340.0 — La pausa se queda sin parte de campaña y el parte de defunción se aparta del rótulo

- Pausa: fuera la franja de día, hora, clima y coordenadas, y sus solapes con el filete.
- Muerte: el papel se queda en su columna y el pie reparte registro, fecha y sello.

## Cambios de 0.339.0 — La hoja de Mundos guardados deja de estar partida en vertical

- Mundos guardados: el canal y el tirador del juego se repintan en papel y aguja de tinta; la hoja ya no queda partida.
- Cabecera: recuento, peso y fecha en una sola línea, dentro del papel.
- El folio pasa al margen izquierdo de la cabecera.

## Cambios de 0.338.0 — El expediente de Multijugador deja libre el pie

- Multijugador: folio y peso del archivo al margen de la cabecera, medidos contra las fichas del pie.
- Opciones: la frase del cronista y su corrección solo se escriben si caben sobre la grilla.

## Cambios de 0.337.0 — La hoja de Accesibilidad deja de cortar la lista

- Accesibilidad: la línea del margen ya no pisa el ojo; la lista termina en renglones enteros.

## Cambios de 0.336.0 — aguja y pie

- **`DiarySlider`**: `plainSubstrByWidth` para partir la etiqueta alrededor de la aguja.
- **Sonido**: `flavorFits()` decide si se escribe el disco suelto; pie medido contra `btL/btR`.

## Cambios de 0.335.0 — un jugador en toda portada

- **Portada**: ficha en el modo baja (`singleYMin >= 40`) y en el carril compacto (`singleRailY + BTN_SLIM_H <= height - compactEdge`).

## Cambios de 0.334.0 — un jugador en la portada

- **Portada**: `SlimButton` «Un jugador» (`BTN_SLIM_W*2 + GAP_SMALL`) sobre Ajustes/Salir si `singleY >= 40`.
- **Portada**: `setTooltip` en los dos `GreenFluButton` grandes.

## Cambios de 0.333.0 — documentacion al dia

- **`CONTRIBUTING.md`**, **`CONTEXTO.md`**, **`REVISION_MOD.md`**, **`KNOWN_ISSUES.md`**: version, baseline y estado vigentes.

## Cambios de 0.332.0 — pie de Sonido

- **Sonido**: `roomy` y `folioSolo` exigen `!compactLayout()`.

## Cambios de 0.331.0 — sello del mod

- **`mods.toml`**: `logoFile="logo.png"`; el sello va en la raiz de recursos.

## Cambios de 0.330.0 — rejillas sujetas al pie

- **Aspecto**: `gridTop` = `min(74, footerTop - 4 - used)` desde `panelY`.
- **Accesibilidad**: el alto de la lista nunca baja de un renglon.

## Cambios de 0.329.0 — aviso en portadas bajas

- **Discoveries.drawToast**: en la portada, `y = height - 34` si el alto llega a 260; si no, `y = 40`.

## Cambios de 0.328.0 — caches con el ancho dentro

- **Multijugador**: `countLimit` (hueco medido) invalida el texto cacheado.
- **Diario**: `cachedFolioPanelW` invalida el folio cacheado.

## Cambios de 0.327.0 — estado del proyecto

- **`KNOWN_ISSUES.md`**: ESTADO a 0.327.0 con lo cerrado, el CI y lo que falta de prueba visual.

## Cambios de 0.326.0 — aviso y lentes

- **Discoveries.drawToast**: `y = 40` cuando la pantalla es la portada.

## Cambios de 0.325.0 — la casa en partida

- **Ajustes de campo**: `DiaryButton` «La casa» (icono SCROLL) a la izquierda de Volver cuando `backX - 92 > panelX + 6`.

## Cambios de 0.324.0 — fichas del pie del diario

- **Bitacora**: `beside` con guarda `fY > panelY()+30`; sin sitio al costado, suben un renglon.

## Cambios de 0.323.0 — cabecera LAN

- **Multijugador**: `paintLanHeaderInk` (solo con la lista vacia) tapa la cabecera con `drawTornSlip` y escribe la linea en `INK_SUBTITLE`.

## Cambios de 0.322.0 — filas LAN y licencia

- **Multijugador**: `lan` deja de saltarse; recorte oscuro `0x33180C` y texto del juego intacto.
- **README**: permisos en claro (descargar y jugar si, redistribuir no).

## Cambios de 0.321.0 — estado junto a la vela

- **Pausa**: `leftInset` 46 con vela, 14 en ventana apretada.

## Cambios de 0.320.0 — reloj y minimapa

- **HUD**: esquina efectiva `3` (columna derecha sobre las coordenadas) cuando hay minimapa.

## Cambios de 0.319.0 — recientes acotadas

- **Conexion directa**: tope `panelW - 48` en la fila de recientes y margen izquierdo minimo.

## Cambios de 0.318.0 — recuento medido

- **Multijugador**: `limit` = hueco entre el titulo y la chapa; se prueba con conocidas, sin ellas y, si no cabe, nada.

## Cambios de 0.317.0 — imagen de portada

- **`docs/banner.jpg`** + cabecera del README con la aclaracion de que es ilustracion.

## Cambios de 0.316.0 — la casa en ventanas bajas

- **La casa**: `rowStep` adaptativo, pestillo acotado, franja unica sobre los botones.

## Cambios de 0.315.0 — el README de la casa

- **`README.md`**: portada con que es, requisitos, instalacion y enlaces; historial debajo.

## Cambios de 0.314.0 — video vestido

- **Pantallas crudas**: `VideoSettingsScreen` agregada a `isHouseRawScreen`.

## Cambios de 0.313.0 — pie de los puestos

- **Multijugador**: elegido a la derecha, peso limitado a la mitad izquierda, puertas conocidas en el recuento.

## Cambios de 0.312.0 — fecha en el reloj

- **HUD**: fecha romana en la primera linea; tira de 116 px.

## Cambios de 0.311.0 — paquetes al estilo de la casa

- **Paquetes**: titulo en tinta y botones vestidos; guardas de pie.

## Cambios de 0.310.0 — estado en la cabecera

- **Chat / En línea**: el resumen sube al renglon de la cita; el pie queda libre.

## Cambios de 0.309.0 — ficha del mod en papel

- **Arsenal**: `drawInfoPage` (nombre, version, id, autores y descripcion).
- **Parte**: subtitulo hasta la chapa.

## Cambios de 0.308.0 — coordenadas aparte

- **HUD**: sin parte de coordenadas (las escribe el mapa); el reloj son tres renglones limpios y la opcion `watchCoords` se retiro del mod en 0.363.0.

## Cambios de 0.307.0 — la casa en la portada

- **Portada**: lente nueva que abre `DiaryHouseScreen`.

## Cambios de 0.306.0 — folios fuera

- **Folios**: se retiran de Aspecto, Créditos, Controles e Idioma (pisaban Volver).
- **Arsenal**: `modId` bajo la version.

## Cambios de 0.305.0 — dia y lugar al pie

- **Pausa**: `dayAndPlace` como tercera linea del pie.
- **Casa**: la esquina indica cuando hay minimapa y el reloj se aparta solo.

## Cambios de 0.304.0 — mundos en tinta

- **Mundos**: `paintWorldRowsInk` con nombre, modo y fecha.
- **Chat / En línea**: subtitulo hasta la chapa.

## Cambios de 0.303.0 — los puestos se leen

- **Multijugador**: `paintServerRowsInk` reescribe nombre y cartel; marca de puerta conocida.

## Cambios de 0.302.0 — arsenal y mundos

- **Arsenal**: descripcion al pasar la mano; folio al filo del papel.
- **Mundos**: tira con nombre, modo y fecha de la ultima partida.

## Cambios de 0.301.0 — repaso de solapamientos

- **Reloj**: sin ventanilla de luna ni frase al pasar la mano.
- **Estrella fugaz**: nota al pie izquierdo.
- **Hojas**: multijugador, sonido, opciones, pausa, muerte, mundos, arsenal y accesibilidad sin encimados.
- **Casa**: nombres enteros y ayuda al pasar la mano; se retira «El cuadro».

## Cambios de 0.300.0 — trescientas versiones

- **Cola**: `enqueueGameToast`, maximo cuatro avisos, 2,8 s cada uno.
- **Cierre**: tercera pasada cerrada (0.295.0–0.300.0).

## Cambios de 0.299.0 — el cuadro de la casa

- **Hoja nueva** `DiaryHouseLedgerScreen` con fichas, objetos, aperturas, racha, dias y version.
- **Portapapeles**: «Copiar el cuadro» y aviso «copiado».
- **Anchos**: la fila de tres botones se calcula segun el panel.

## Cambios de 0.298.0 — la ficha nueva avisa en partida

- **HUD**: `Discoveries.drawGameToast` + overlay `gripeverde_ficha`.
- **Refactor**: `paintSlip` compartido por el aviso de menu y el de partida.

## Cambios de 0.297.0 — volumenes y boton de fabrica

- **Ruedas**: `MUSIC_VOLUME`, `AMBIENCE_VOLUME`, `UI_VOLUME` en la hoja de la casa.
- **Boton**: «Todo a su sitio» devuelve pestillos, ruedas y esquina a fabrica.
- **Adaptacion**: con menos de 290 px de alto la hoja omite las ruedas.

## Cambios de 0.296.0 — la casa tiene su hoja

- **Hoja nueva** `DiaryHouseScreen` con seis `DiaryToggle` y boton de esquina.
- Entrada desde `DiaryCreditsScreen` (engranaje a la izquierda de Volver).
- `WatchHud`: lacre quieto con movimiento reducido.

## Cambios de 0.295.0 — el reloj tambien esta en la partida

- **HUD**: `WatchHud` + `WatchHudEvents` con tres opciones nuevas en la config del cliente.

## Cambios de 0.294.0 — cierre de la segunda vuelta

- Sin funciones nuevas: tablas al dia y verificadores limpios.

## Cambios de 0.293.0 — reparaciones de la casa

- **Portada**: zonas de clic reiniciadas cada cuadro. **Docs**: version de rama corregida.

## Cambios de 0.292.0 — el sello cuenta por donde va el archivo

- **Fichas**: contador en el sello de descubrimiento y en los creditos.

## Cambios de 0.291.0 — la pausa dice donde estas y deja copiar la semilla

- **Pausa**: coordenadas, fps y copia de la semilla con aviso.

## Cambios de 0.290.0 — el cuaderno cuenta lo que lleva escrito

- **Diario**: recuento de palabras al pasar por el filete; punto en los dias leidos del indice.

## Cambios de 0.289.0 — repaso de la segunda vuelta

- **Guardas** en mundos, puestos y sonido. **Docs**: tabla de tandas 16-23 y tabla de folios.

## Cambios de 0.288.0 — el reloj se parece a un reloj de verdad

- **Reloj**: marcas de minuto, firma del relojero y cuerda. **Portada**: misma muesca, sin leyenda de version, distintivo subrayado.

## Cambios de 0.287.0 — las listas dicen cual se lleva

- **Mundos**: nombre del elegido en la cabecera. **Puestos**: direccion elegida y "puerta conocida".

## Cambios de 0.286.0 — el cuaderno y el parte afinan la mano

- **Diario**: sin pistas de atajos, memoria visible de la busqueda y contador rojo sin hallazgos.
- **Parte**: ranuras sin tecla y nombre de ranura.

## Cambios de 0.285.0 — remate de las hojas

- **Sonido**: estado de las tres capas y aviso de silencio. **Aspecto**: aviso sin prendas.
- **Folios** al pie en sonido, aspecto, idioma, controles y creditos.

## Cambios de 0.284.0 — la entrada directa recuerda las ultimas puertas

- **Entrada directa**: `RecentAddresses` (3 direcciones en `config/gripeverde-recents.txt`), fichas clicables.

## Cambios de 0.283.0 — la portada responde a la mano

- **Portada**: la firma abre creditos y el distintivo de musica abre sonido, con subrayado al pasar.

## Cambios de 0.282.0 — las hojas de ajustes dicen lo que tienen puesto

- **Segunda vuelta**, tanda 16: resumen en palabras en chat, online y accesibilidad.

## Cambios de 0.281.0 — repaso de la casa

- **Muerte**: guarda del renglon de cortesia. **Docs**: tabla de 15 tandas + revisor de Java.

## Cambios de 0.280.0 — la lupa devuelve el lector y el cajon dice su folio

- **Diario**: al cerrar la busqueda vuelve el punto de lectura.
- **Packs**: folio y salida en el pie.

## Cambios de 0.279.0 — el deceso dice mas y la ropa suena

- **Muerte**: causa, lugar y coordenadas, dia/hora/nivel; papel 92 px.
- **Aspecto**: sonido al cambiar prenda o mano, tipo de piel.
- **CI**: `lint_java.py` (nuevo) antes de Gradle.

## Cambios de 0.278.0 — el arsenal dice sobre que esta montado

- **Arsenal**: version de Forge, `REG. XVII`, folio y "Esc vuelve". Fix del pie del parte.

## Cambios de 0.277.0 — el parte se ordena solo y el cuaderno marca el paso

- **Parte**: foco en la primera vacia, contador de ranura y "faltan N".
- **Diario**: muesca de ultima lectura, progreso al pie, fin del cuaderno con tapa.

## Cambios de 0.276.0 — el cuaderno recuerda donde lo dejaste

- **Diario**: muesca de ultima lectura, filete de progreso con porcentaje, "fin del cuaderno" con golpe de tapa. **Pausa**: semilla desde `getSingleplayerServer()`.

## Cambios de 0.275.0 — la pausa dice en que esta la partida

- **Pausa**: `drawWorldStatus` (hora, clima, modo, dificultad, almas, hardcore) y `drawSeedOnHover` (semilla al pasar).

## Cambios de 0.274.0 — el reloj de bolsillo dice mas

- **Reloj**: segundero, muesca de las 11:52 y columna de hora/dia/atraso. Fix de compilacion en `DiaryControlsScreen`.

## Cambios de 0.273.0 — la hoja de controles vigila las teclas

- **Controles**: `auditKeys` (choques + sin tecla), muestra del nombre al pasar, clic que abre `KeyBindsScreen`.

## Cambios de 0.272.0 — todas las hojas suenan igual

- **Sonido de apertura** en 17 hojas con guarda `paperSoundDone`; **puerta** al cerrar pausa y packs.
- **Chapas unicas** en el registro (VIII, XIV, XVI, XVIII, II).

## Cambios de 0.271.0 — la hoja de puestos lleva cuenta

- **Puestos**: peso de `servers.dat` + fecha, aviso de lista vacia, folio, hoja al abrir y pluma al buscar.

## Cambios de 0.270.0 — el archivo dice lo que pesa

- **Mundos**: peso del archivo en hilo aparte, fecha en romanos, "se ven N" al filtrar, folio y tres sonidos nuevos (hoja, rueda, pluma, fila).

## Cambios de 0.269.0 — menos llaves, mas papel

- **Una sola llave**: `Ctrl+S` en la portada. Nada mas, y sin carteles.
- Fuera el modo cine con `F1`; el reloj dice dia y luz; los mundos vacios se anuncian.

## Cambios de 0.268.0 — teclas de lista y cabeceras

- **Puestos**: `Ctrl+N`/`Ctrl+E` + aviso bajo el recuento (lo que 0.267.0 dejo fuera del parte).
- **Mundos**: `Ctrl+N`/`Ctrl+S`, `pressVanilla`, pie.
- **Arsenal**: `onClose` con `UiSounds.back()`.
- **Diario**: `lastQuery` + `openSearchWithLastQuery`.
- **Idioma**: `currentLanguageName`. **Creditos**: plataforma y 11:52. **Sonido**: pieza actual. **Portada**: `F1`.

## Cambios de 0.267.0 — sonido y parte

- **Mandos**: `keyPressed` (2% / 10%), `mouseReleased` con golpe, `wasHovered`, `livePreview`, etiqueta de silencio.
- **Sonido**: `REG. VI`, `onClose`.
- **Parte**: clic derecho vacia (`fieldRowAt`), `sin tecla`, pie nuevo. `EXPECTED = 191` en `tools/verify_mirrors.py` (25 lang con `dispatch.slot.emptied`).

## Cambios de 0.266.0 — fichas y teclas

- **Aspecto**: rejilla con alto/hueco calculados (`btnH`, `gapY`), `drawWornCount`, tooltips.
- **Pausa**: `· Esc` en el boton de volver.
- **Parte**: `REG. XIII`.
- **Arsenal**: `dressSearchBox`, `modCount`.
- **Confirmacion**: `keyPressed` con Enter/Esc.

## Cambios de 0.265.0 — solapes y tinta

- **Multijugador**: se quitan la banda de teclas al pie y el divisor inferior (chocaban con las dos filas de botones).
- **Portada**: se quitan fecha/version del pie (chocaban con la firma).
- **Arsenal**: `paintRowsInk` + `modNameAndVersion` + `stripCodes`; nombre en `0xFF1A1108`, version en `0xFF6B5533`, barra roja en el elegido.
- **Portada**: `Ctrl+S` → `onSingleplayer()`.

## Cambios de 0.264.0 — teclado y puertas

- **Audio**: `UiSounds.back()` limitado a uno cada 140 ms; `onClose` con puerta en Controls, Options, Language, Credits, Chat, Online, Accessibility, Skin, Confirm, Direct y Edit.
- **Widgets**: `DiarySlider.keyPressed` (flechas, RePag/AvPag).
- **Controles**: `btnW` responsive y tooltips de las dos sub-hojas.
- **Paquetes**: `archiveCount` al pie.

## Cambios de 0.263.0 — los puestos de la lista

- **DiaryChrome**: `addressReadout(ip,en)` y `looksLikeAddress(ip)`.
- **Conexion directa**: lectura host/puerto, aviso rojo, `Entrar` gateado, pie de teclas.
- **Ficha de servidor**: `Tab`/`Enter`, aviso de campo que falta, pie de teclas.
- **Lista**: doble clic, tooltip con el nombre, `Esc` con `UiSounds.back()`, `F5`.

## Cambios de 0.262.0 — ocho remates

- **Diario**: dias en el titulo del indice, folio clicable hacia arriba, `Esc` cierra el indice.
- **Difuntos**: fecha + firma + mancha.
- **Mundos**: pista del buscador.
- **Audio**: `playHover` con freno de 55 ms.
- **Portada**: pieza de musica a la vista.

## Cambios de 0.261.0 — doce mejoras visibles

- **Portada**: fecha/version al pie derecho (`width>=480`, fuera de compacto) y hora en numeros al pasar por el reloj.
- **Reloj**: `S.P.` en el bisel (`radius>=34`).
- **Pausa**: `REG. VII`, voces del parte.
- **Parte**: teclas al pie, glifo de raton.
- **Mundos**: contador, linea de teclas, `Supr`.
- **Multijugador**: banda de teclas + direccion elegida.

## Cambios de 0.260.0 — fecha en la tapa

- **Reloj**: linea `hoy: 13 · XI` bajo la luna, con `ROMAN_MONTHS` y la misma aparicion progresiva (`chars > 38`).

## Cambios de 0.259.0 — fuentes del cuaderno en creditos

- **Creditos**: `drawSources` lista Ragusa 1377, Filadelfia 1793, Hamburgo 1892, Oporto 1899, el ghul arabe y la coraza de 1800.

## Cambios de 0.258.0 — sonido del indice y atajos anunciados

- **Diario**: `toggleIndex` reproduce `UI_PAPER_FALL`.
- **Multijugador**: `withEnter(label)` agrega `· Enter` al tooltip de entrar (normal y compacto).

## Cambios de 0.257.0 — fecha en el reloj y canto en las pantallas ajenas

- **Reloj**: ventanilla de fecha a las 3 (ancho `max(11, texto+4)`), `III` omitido; convive con la luna a las 6.
- **Pantallas por evento**: `drawWornBorder` de 1 px en `Render.Post`.

## Cambios de 0.256.0 — vestir sin subclasear

- **Eventos**: `Init.Post` viste (`dressPlain`) y `BackgroundRendered` tapa el fondo de las pantallas ajenas listadas en `isHouseRawScreen`.
- **Cuidado**: solo `child.getClass() == Button.class`, para no congelar pestanas ni `CycleButton`.

## Cambios de 0.255.0 — la lista de mods

- **Fondo**: la lista de Forge se apaga igual que la de mundos.
- **Fichas**: `drawRowSlips` con `getRowLeft/getRowWidth/getTop/getBottom/getScrollAmount` y `itemHeight` reflectivo.

## Cambios de 0.254.0 — el fondo de la lista de mundos

- **Causa del bug**: `AbstractSelectionList` pinta el fondo de la pantalla por cuadro; en mundos faltaba `setRenderBackground(false)` (multijugador ya lo tenia).
- **Layout**: `layoutAroundPaper()` mueve la caja de busqueda y reencuadra la lista entre cabecera y botones.

## Cambios de 0.253.0 — botones vestidos con estado y diario con fecha al pie

- **PaperButtons**: `syncStates()` copia `active`; la accion del sustituto no corre si el original esta apagado.
- **DiarySlider**: clic al soltar.
- **ChangelogScreen**: `diarySectionLabels[section]` al pie cuando hay sitio.

## Cambios de 0.252.0 — hora real y la pausa con contexto

- **Reloj**: `getCurrentHour()` usa `Calendar` local; `HOUR_POOL` eliminado; `isNearNoon()` recalculado.
- **Pausa**: `drawWorldLine` dibuja `dia N - <lugar>` bajo la fecha, con `plainSubstrByWidth`.

## Cambios de 0.251.0 — el diario y el teclado en las listas

- **Diario**: `bitacora_es.md` / `bitacora_en.md` reescritos (13 secciones); el indice de dias sigue armandose solo desde los `##`, con `Enero` ya en el calendario.
- **Multijugador**: `drawEntryBackgrounds(gfx,mouseX,mouseY)` con hover y lacre; `keyPressed` con Enter/Supr.
- **Mundos**: hover en `drawRegisterPage` y `keyPressed` con Enter sobre el boton `selectWorld.select`.

## Cambios de 0.250.0 — la luna del reloj y la confirmacion por partes

- **Reloj**: `moonPhase(nowMs)` (mes sinodico 29,530588 desde 2000-01-06 18:14 UTC), `drawMoonAperture` fila por fila, `moonName` para la cuarta linea de la tapa, punto en la punta del segundero.
- **Confirmacion**: `DiaryConfirmScreen.render` parte el dibujo en bloques independientes.
- **Lore**: la fiebre entra por puerto de rio, con las fiebres de Hamburgo 1892 y Oporto 1899 como referencia real; sin nombrar juegos.

## Cambios de 0.249.0 — parte de CEDA, sonidos y lista de mundos

- **Parte de CEDA**: `mouseClicked` ata la ranura con los botones 3/4 (y 2); `DispatchLibrary.keyNameOf` rotula laterales y numpad; `isModifierKey` evita atar modificadores.
- **Sonidos**: `UiSounds.hoverMain` suma capa de hoja (`hover_paper`, 200 ms); `DiaryButton.playDownSound` elige por `Variant`.
- **Lista de mundos**: `renderBackground` con escena y `drawWornRim` sobre la pagina.

## Cambios de 0.248.0 — se borra el registro y la puerta de servicio

- **Registro de fichas eliminado del mod**: `DiaryArchiveScreen`, `DiaryAdvancementsScreen` y `DiaryStatsScreen` borrados; la pausa vuelve a cuatro botones.
- **Puerta de servicio eliminada**: sin cerradura en la portada, sin `Ctrl+S`, sin `serviceKnocks`, sin `KNOCK_BACK`. `DiarySelectWorldScreen` queda como pantalla vestida del acceso normal a un jugador.
- **Reloj**: la tercera linea de la tapa cuenta noches (`ClientConfig.OPEN_COUNT`) en vez de fichas.
- **Death screen**: `DeathPaper` pasa a 296x64 anclado en (8,8).

## Cambios de 0.247.0 — últimas pantallas

- Avances y estadísticas: subclases con fondo de escena, cabecera y pie de papel; la lógica de la lista sigue siendo vanilla.
- Cajón vacío en la sala de consulta con sonido y pista.

## Cambios de 0.246.0 — reloj, pausa y mandos

- La inspección del reloj informa el avance del archivo y la versión.
- Manchas de la pausa clicables (marginalia `N_DOCTOR`); `DiarySlider` con sonido de hover.

## Cambios de 0.245.0 — fichas alcanzables

- Las 45 fichas de `Discoveries.ALL` tienen disparador otra vez; el gabinete puede completarse.
- El reloj muestra su ayuda al pasar el mouse.

## Cambios de 0.244.0 — sello y mods

- Sello de pulsación compartido por los tres tipos de botón (9 ticks, `drawSealRing`).
- `DiaryModListScreen` para la lista de Forge; sin título propio para no duplicar el de Forge.

## Cambios de 0.243.0 — página de registro

- Atajo: sólo `Ctrl+S`. `DiarySelectWorldScreen.cacheListMetrics()` guarda alturas y bordes al abrir.
- Cada mundo sobre banda oscura para que el texto vanilla siga legible.

## Cambios de 0.242.0 — puerta de servicio

- `DiarySelectWorldScreen` sustituye a `SelectWorldScreen` en la portada; botones vestidos con `PaperButtons.dressAll`.
- Cerradura de servicio en `x=8, y=height-56, 132x20`, visible con Ctrl/Shift apretado.

## Cambios de 0.241.0 — portada, multijugador y pausa

- Tercer icono de la portada: Registro (gabinete). Icono nuevo `FILING`.
- Hoja de pausa α238; multijugador con recortes por fila; descripción del mod reescrita.

## Cambios de 0.240.0 — telegrama y pie del parte

- `DeathPaper`: papel de 340×78 a `y=8`; `SkinButtons` por `ScreenEvent.Init.Post` con alta del widget por reflexión
  (`renderables`/`narratables` de `Screen`), revirtiendo si no se pudo registrar.
- Pie del parte: `footerDivY = panelH-66`, ayuda `+4`, recuento `+15`, botones en `panelH-14-alto`.

## Cambios de 0.239.0 — avisos como recortes

- Nuevo `PaperTexture.drawTornSlip(...)` con alfa por parámetro (para avisos que se desvanecen).
- `Discoveries.drawToast`: recorte rasgado en lugar de tarjeta; rótulos a `y+4`/`y+13` para no tocar el desgarro.

## Cambios de 0.238.0 — campos de texto

- `DiaryTextField` (usado en servidores, unirse directo e idiomas): `drawWornBorder` con el color de foco.
- Buscador del diario: `drawWornBorder` bronce α90/200, después del relleno; semilla `(sx-1)*31+(sy-1)*17+SEARCH_W`.

## Cambios de 0.237.0 — parte de defunción

- `DeathPaper.render(g, screen)`: sólo actúa sobre `DeathScreen`; franja superior en `y=-8` (42 px) y pie en `h-30` (38 px,
  omitido si `h < 300` para no tapar botones en ventanas bajas). Tinta `REDCOAT` para el sello.
- Enganche en `ScreenEventHandler.onScreenRenderPost` antes del `return` de partida. Sin animación: es un overlay estático.

## Cambios de 0.236.0 — mandos sin marco

- `PaperTexture.drawWornBorder` aplicado a `DiarySlider`, `DiaryMusicSlider`, `DiaryAmbienceSlider` y `DiaryProgressBar`
  (semilla `x*31+y*17+w`, color de borde propio de cada mando).
- Sin tocar valores, ticks ni el pulso de ALERT (que ya respeta Movimiento/Destellos reducidos).

## Cambios de 0.235.0 — botones sin caja

- `PaperTexture.drawWornBorder`: tramos `max(5,w/20)` en horizontal y `max(4,h/8)` en vertical, cada uno desplazado 0-1 px;
  semilla `x*31+y*17+w` (estable al pulsar, que desplaza el dibujo un píxel).
- Aplicado en `DiaryButton` (fuera `drawBorder`), `SlimButton` (pestañas del pie) y `DiaryToggle` (marco exterior).

## Cambios de 0.234.0 — meses, pelusa y contenedores

- Indice: `buildIndexRows` (cabecera por mes + fechas con sangría 18 px), `indexRowIsMonth`/`indexRowSection`, `labelAt`;
  cabeceras no pulsables; `toggleIndex` centra por fila de índice.
- `PaperTexture.drawTornEdge`: pelusa y fibra en el canto (todas las hojas rasgadas).
- `PaperTexture.drawWornRim` + `ScreenEventHandler.onContainerRenderPost` (`AbstractContainerScreen.getGuiLeft/getGuiTop/getXSize/getYSize`):
  canto gastado estático en inventario y contenedores. Los tooltips NO se pueden tocar: Forge 1.20.x no tiene evento post-tooltip.

## Cambios de 0.233.0 — la pausa termina rasgada

- `PaperTexture.drawTornRim(g,x,y,w,h,paper,shadow)`: dientes de 1–3 px hacia afuera + sombra de 1–2 px adentro, diente suelto cada
  tres tramos en los laterales; semilla derivada de la geometría (sin RNG por cuadro).
- `DiaryPauseScreen`: `drawPanel` (con brillo, sangre y `drawWear`) y `renderWorldSafe` (partida) usan `drawTornRim` con
  `PAPER_LIGHT`; fuera `drawDoubleBorder`/`drawRect`/`drawCornerOrnaments`.

## Cambios de 0.232.0 — no quedan rectangulos

- `ChangelogScreen`: modo registro técnico → `drawTornSheet` ARCHIVE; fuera los cuatro filetes `BRONZE` en ambos modos.
- `DiaryLanguageScreen`: fuera el marco de 1 px; la lista se lee sobre `drawTornSheet` CORRESPONDENCE (sombra `0x1E302315`).
- `DiaryPackSelectionScreen`: la hoja grande (blotter) → `drawTornSheet` ARCHIVE.
- `DiaryChrome`: eliminados `drawPanelScaffold` y `renderPanelBase`, sin uso desde hace versiones.

## Cambios de 0.231.0 — recortes al pie e indice con tope

- `DiaryArchiveScreen.drawPaperDoc`: los tres papeles recuperados (telegrama, informe, ficha negra) pasan a
  `PaperTexture.drawTornSheet` `CORRESPONDENCE` de 124×20 con sombra corta; hover = cuerpo aclarado + subrayado a lápiz;
  clic/hover de 20 px.
- `ChangelogScreen` modo diario: `INDEX_MAX_ROWS = 12`, `indexHeight`/`indexVisibleRows`/`indexMaxScroll`,
  `indexScroll` propio con rueda dentro de la hoja y marca de desplazamiento al costado, `toggleIndex` (abre centrado en
  la entrada actual) y `overIndex` (la hoja se queda clics y rueda).

## Cambios de 0.230.0 — documentos recortados

- `DiaryArchiveScreen.renderDocument`: recorte `LEDGER` rasgado bajo el documento, sombra corta al pie.

## Cambios de 0.229.0 — lectura y firma

- Índice: raya de lacre en la entrada actual (`currentDiarySection`), banda de realce bajo el puntero.
- Portada: la firma se apoya en una tira rasgada de 16 px con sombra; sobre papel usa `INK_TITLE`.

## Cambios de 0.228.0 — vinetas y luz

- Diario: viñeta → dos trazos (`0x006B5533` α170/90) manteniendo `prefixW` y altos; degradados α24 arriba/abajo.

## Cambios de 0.227.0 — indice del diario

- Índice: botones «Versiones» + «Índice» al pie; `drawIndexOverlay` (hoja LEDGER rasgada, 240 px, 13 px/línea),
  `indexRowAt` y `jumpToDiarySection` (`setScrollTarget(off-6)`).
- `Marginalia`: borde rasgado arriba/abajo (semilla `cardX*31+cardWidth*17`).

## Cambios de 0.226.0 — fragmentos en el diario

- `ChangelogScreen.drawDiaryScraps`: una hoja rasgada `LEDGER` por sección (corte por `H1`), sombra corta,
  recorte `1 + (seed % 5)` derivado de `start*31+i`.

## Cambios de 0.225.0 — repaso a lapiz en los filetes

- `DiaryChrome.drawInkDivider`: tramos de lápiz `y+3`, α55, tope 5, semilla `x*31+w*17`.

## Cambios de 0.224.0 — papel rasgado en todo el gabinete

- `drawTornSheet` en 15 hojas más (accesibilidad, chat, confirmación, controles, unión directa, edición de servidor,
  guía, idioma, multijugador, opciones en línea, personalización, sonido, despacho, nota de portada).
- Quedan con borde recto sólo: el registro técnico (`ChangelogScreen` modo registro), el marco interior del idioma,
  la hoja de packs (casi pantalla completa) y las hojas pintadas a mano de la pausa.

## Cambios de 0.223.0 — fechas sin capitular

- `ChangelogScreen` modo diario: `H1` se pinta entero en `INK_TITLE` con `drawPencilRule` + `drawInkBlot`.
- El diario vive en `assets/gripeverde/diary/bitacora_{es,en}.md` (fechas, tachaduras `~~ `, notas `- `).

## Cambios de 0.222.0 — papel rasgado en todas las hojas

- `PaperTexture.drawWear` (público) y `drawTornSheet` en `DiaryArchiveScreen`, `DiaryCreditsScreen`,
  `DiaryOptionsScreen`; `DiaryPauseScreen` añade `drawWear`.

## Cambios de 0.221.0 — portada limpia y diario

- `DiaryTitleScreen`: sin cristal (`drawGlassPatina`), sin huevo del emblema y sin botón de guía; botón «Diario»
  (icono libro) → `new ChangelogScreen(this, true)`.
- `ChangelogScreen(Screen, boolean diary)`: `diary/bitacora_es.md` / `bitacora_en.md`, `LT.STRIKE` (`~~ `),
  hoja rasgada, lápiz + mancha en las fechas, botón «Versiones».
- `PaperTexture.drawAge/drawTornSheet/drawTornSeam`; `DiaryChrome.drawInkBlot/drawPencilRule/drawStrike`.

## Cambios de 0.220.0 — avance del archivo

- `DiaryArchiveScreen`: barra de avance a `panelY+54` (track α55, relleno ocre, marca verde al completar);
  la pista de teclado se dibuja sólo si `panelY+panelH-46 >= docsY+20`.

## Cambios de 0.219.0 — cronica con el mismo marco

- `ChangelogScreen`: sombra + esquinas + chapa `REG. XV` (solo si `pw>=400` y el título deja hueco).

## Cambios de 0.218.0 — archivo por teclado

- `DiaryArchiveScreen`: `focusCard`, `drawFocusCorners` (esquinas 1px lacre `0xFF5C0A08`), flechas ±1/±5 y
  Enter (`257`/`335`) → `openCard`; `cardAt` para el clic; pista «flechas / enter» a `panelY+panelH-46`.

## Cambios de 0.217.0 — dos luces

- Halos: radio 8 α30/26 sobre las lámparas de las torres y radio 4 α20 tras los ojos del ghoul
  (`drawSoftBlob`, franjas ya memorizadas por `BLOB_BANDS`).

## Cambios de 0.216.0 — ciudad lejana

- `VictorianSkyline`: `ensureFarLayout(w,h)` (`farRects`, `farWindows`) y la ventana del ghoul memorizada
  (`ghoulWx/Wy/Ew/Eh`); `renderFarSkyline` y `renderGhoulWatcher` sólo pintan.

## Cambios de 0.215.0 — campos de texto

- `DiaryTextField`: recorte visible memorizado por texto/desplazamiento/ancho; `formatSafe` con dos casillas
  (`fmtSeqA/B`) en vez de armar el tramo con formato en cada cuadro.

## Cambios de 0.214.0 — hora y niebla

- `LivingAtmosphere`: `realHour()` cachea la hora real un minuto (velo nocturno); `FOG_LOW_BANDS` constante
  y una sola pasada por franja en `renderFogLow`.

## Cambios de 0.213.0 — medir la letra una vez

- `DiaryTypography.width(...)` → `widthMemo`, atado a la fuente (`widthMemoFont`), clave = texto;
  `ChangelogScreen` mide titulo/sello/viñeta con `DiaryChrome.widthCached`.

## Cambios de 0.212.0 — haces de las torres

- `AtmosphereRenderer`: `beamSegments(bucket,length,maxHalf,alpha,color)` + `BEAM_MEMO` (ángulo 1/1024 rad,
  tope 256, tramos relativos al origen); `drawBeam` sólo pinta.

## Cambios de 0.211.0 — sello y nota al margen

- `Discoveries.drawToast`: campos `memToastFont/Width/Name/English/BoxWidth` y `memVisibleHeader/Name`.
- `Marginalia`: `cachedLayoutLinesWidth` se calcula en `layoutLines` y lo usa `drawHorizontal`.

## Cambios de 0.210.0 — traducciones y anchos de rótulo

- `DiaryChrome.tr` es ya la única vía de traducción: no queda ningún `Component.translatable(...).getString()`
  fuera de `tr` (`grep -c` = 1, la propia implementación).
- `DiaryTypography`: `widthMemo` + `WIDTH_MEMO` (tope 512) usados por `drawFitted`/`drawFittedLeft`.

## Cambios de 0.209.0 — franjas del pincel blando

- `AtmosphereRenderer`: `blobBands(r, rowStep)` + `BLOB_BANDS` (HashMap, tope 128, clave `(r<<3)|rowStep`) memoriza
  `{low, dx}` por franja; `drawSoftBlob` sólo recalcula alfa por anillo.
- `DiaryChrome.trFit` → `fitCached`.

## Cambios de 0.208.0 — forma de la luna memorizada

- `AtmosphereRenderer`: `moonSpans(radius, shape)` + `moonSpans`/`moonShape*` (tramos `{fila, x1, x2}` relativos
  al centro); `drawMoonSpans` los pinta. `renderMoonPhase` sólo elige forma; se borraron `renderMoonPhaseDifference`
  y `fillCircle` (sin uso).

## Cambios de 0.207.0 — calle memorizada

- `LivingAtmosphere`: `ensureStreetLayout(w,h)` + campos `stTop/stBottom/stHeight/stCurb*/stUpperBand*/stSheen*/
  stSeamAlpha/stReflTop/stGutterW/stBottomBandTop` y arrays `stRowY1/Y2/Offset/Step`; `renderStreet` sólo dibuja.

## Cambios de 0.206.0 — fachadas y banderas

- `VictorianSkyline`: `ensureFacadeLayout(w,h)` + arrays `bgX1/X2/Y1/Y2`, `fgX1/X2/Y1/Y2`, `fgScaledW/H`;
  `renderBackground` y `renderForeground` iteran por índice (la puerta sale de `fgScaledW/H`).
- `FlagRenderer`: `FLAG_RUNS` (`IdentityHashMap<int[][], int[]>`) + `runsOf(grid)`; `FRAME_OUTER/INNER_LIGHT/INNER_DARK/FOLD`.

## Cambios de 0.205.0 — listas largas y chimeneas

- `DiaryLanguageScreen`: `widthCached` en nombre de la niebla, rótulo de fila y código de lengua.
- `DiaryArchiveScreen`: `widthCached` en sellos de documento, `SIGILLUM` y leyenda de vuelta.
- `VictorianSkyline`: `ensureChimneyLayout(w,h)` + `fillChimneyRects(...)` (arrays `chimBg*`/`chimFg*`, `NO_ANCHOR`).

## Cambios de 0.204.0 — widgets y centrado común

- `DiaryChrome.drawCenteredString` → `widthCached`.
- `DiaryToggle`, `DiarySlider`, `SlimButton`, `DiaryProgressBar`, `DiaryAmbienceSlider`, `DiaryMusicSlider`:
  `DiaryChrome.widthCached` en lugar de `DiaryTypography.width`/`font.width` por cuadro.

## Cambios de 0.203.0 — barrido general de medidas

- `DiaryChrome`: `fitCached(font,text,maxW)`, `widthCached(font,text)`, `trFitCached(key,font,maxW)`;
  `FIT_CACHE`/`WIDTH_CACHE` (tope 256, se vacían con el idioma en `tr`).
- 15 pantallas usan ya los helpers (`DiaryAccessibilityOptions`, `DiaryChatOptions`, `DiaryConfirm`, `DiaryControls`,
  `DiaryOnlineOptions`, `DiaryPackSelection`, `DiarySkinCustomization`, `DiarySoundOptions`, `DiaryDirectJoin`,
  `DiaryEditServer`, `DiaryDispatch`, `DiaryTitle`).
- `DiaryTitleScreen`: `cachedCompactSig*` para la firma recortada en modo compacto.

## Cambios de 0.202.0 — cabeceras de créditos y archivo

- `DiaryCreditsScreen`: `refreshTextCache()` (fuente, `panelW`, inglés, título) guarda `headerTitle*`, `headerSub*`,
  `headerFlavor*`, `versionText/W`, `noteText/W`; `drawSection` usa `labelWidths` (HashMap).
- `DiaryArchiveScreen`: `refreshHeaderCache(en, found, total)` guarda `headerTitle/Sub/Prog/Stamp/StampCol/StampW`.

## Cambios de 0.201.0 — cabeceras de multijugador y pausa

- `DiaryMultiplayerScreen`: `refreshHeaderCache(maxWidth)` (clave: fuente, ancho, título, subtítulo) guarda
  `headerTitle/TitleX`, `headerSubtitle/SubX`; `drawServerCount` cachea `countText/countWidth`.
- `DiaryPauseScreen.drawPanelTitle`: `headerTitleW`/`headerScale`/`headerSubtitle`/`headerSubW` en caché.

## Cambios de 0.200.0 — cabecera de idioma memorizada

- `DiaryLanguageScreen`: `refreshHeaderCache()` (clave: fuente, `panelW`, título) guarda `headerTitle/TitleX`,
  `headerSubtitle/SubX`, `headerFlavor/FlavorX`; `drawLanguageCount` cachea frase y ancho (`countText*`).
- `ChangelogScreen`: contador de la lupa cacheado (`countSearchText/W/Hits/Pos/Empty/Font`).

## Cambios de 0.199.0 — renglones de la crónica memorizados

- `ChangelogScreen.Row`: `WrapCache whole`/`body` (fuente + ancho como clave); `rowHeight` mide con la misma
  entrada que usa `renderRow`, así que el `font.split` de cada renglón visible ocurre una vez por ancho.
- Folio del pie: `cachedFolio`/`cachedFolioWidth`/`cachedFolioCur`/`cachedFolioPages`/`cachedFolioFont`.

## Cambios de 0.198.0 — bocas de chimenea memorizadas

- `VictorianSkyline`: `ensureSmokeAnchors(w,h)` + `fillSmokeAnchors(...)` cachean por resolución
  `smokeBgX/W/Top` y `smokeFgX/W/Top` (`NO_ANCHOR` = `Integer.MIN_VALUE`); `renderSmokeGroup` usa los arrays.
- `renderSmokeGroup`: `windDeep` fuera del bucle de partículas; `continue` si el puff cae fuera de pantalla
  (`sy + sz < 0 || sy - sz > h`) o si `sA < 6`.

## Cambios de 0.197.0 — trazado de ventanas memorizado

- `VictorianSkyline`: `ensureWindowLayout(w,h)` + `collectWindows(...)` resuelven una vez por resolución
  posición/tamaño/suerte de cada ventana; arrays paralelos `winFg*`/`winBg*`; en el fondo `winBgColor`
  ya trae el ARGB final. El dibujo por cuadro queda en fills (con `Math.sin` sólo para el parpadeo del frente).
- `DiaryFieldGuideScreen`: `headerHintX` calculado en `refreshHeaderCache`.

## Cambios de 0.196.0 — el parte de CEDA

- `DiaryDispatchScreen`: `SLOT_NUMBERS[]` estático (sin `String.format` por fila) y `keyLabel(slot)` con caché por binding
  (`keyLabels`/`keyBindings`, `Integer.MIN_VALUE` como centinela).

## Cambios de 0.195.0 — traducciones memorizadas

- `DiaryChrome.tr(key)` + `trFit(key, font, maxW)`: memoria invalidada por `LanguageManager.getSelected()`; 34 llamadas
  por cuadro reemplazadas en 20 pantallas/widgets (el import se agregó donde faltaba).
- `DiaryLanguageScreen.drawLanguageCount(g, titleY)`: contador de lenguas a la izquierda del título (`panelW >= 300`,
  margen derecho reservado para REG. IV).

## Cambios de 0.194.0 — opciones y calle

- `DiaryOptionsScreen`: `headerTitleFitted()`/`headerSubtitleFitted()` (fuente + `panelW` + texto del título).
- `DiaryToggle`: `baseLabelStr` final; valor cacheado por `Component`.
- `DiarySlider`: `cachedMessage()` + un `font` por cuadro.
- Adoquines: juntas cada dos columnas salvo en la hilera más cercana; derrame de luz de los reflectores en el suelo.

## Cambios de 0.193.0 — texto sin recalcular

- `DiaryFieldGuideScreen`: culling por viewport en el bucle de filas (`y + LINE_HEIGHT < top - 4` → continue, `y > bottom + 4` → break),
  `GuideRow.cachedWidth`, y `refreshHeaderCache` (fuente + `panelW` + clave de subtítulo).
- `DiaryArchiveScreen`: `ensureCardLabels(cw, found)` + `questionW`; `drawWrapped` con `wrapCache` (fuente + ancho).
- `DiaryTypography.fitString`: memo de dos niveles (`maxWidth+modo` → `texto`), topes 48/256, `clear()` al cambiar de fuente.
- `DiaryButton`: `cachedFit`/`cachedWidth`.

## Cambios de 0.192.0 — nubes por bandas

- `renderClouds`: los dos blobs por nube pasan a **dos `drawCloudMass`** (bandas 3-8 siguiendo la elipse): ~310 → ~80 rellenos
  por cuadro.
- **Borde de luna** en las nubes cercanas (`moonCenterX`, línea de 1 px `0x00726042`, alpha por distancia).

## Cambios de 0.191.0 — ventanas, humo y asfalto

- `VictorianSkyline.renderWindowsOnBuildings`: `lodSkip` a 1600 px = **3** (antes 2), divisores FG 16/18 (antes 14/16),
  ventana encendida FG sin travesaño horizontal (5 → 4 fills). Las ventanas bajan a ~la mitad.
- Tres tonos de luz en las ventanas del fondo, deterministas por `(row, col, bi)`.
- `renderSmokeGroup`: 3 puffs por chimenea, `sz` ×1.35.
- `LivingAtmosphere.renderStreet`: sin juntas en la hilera lejana + filo de agua por hilera.

## Cambios de 0.190.0 — el cielo y los blobs blandos

- `AtmosphereRenderer.drawSoftBlob`: el anillo exterior ahora lleva alpha (`0.22 + 0.55·t`), así que el radio se conserva
  con menos anillos: `steps = radius >= 14 ? 3 : 2`, `rowStep = radius >= 24 ? 4 : radius >= 12 ? 3 : 2`. Halo de la luna,
  nubes y humo pasan de ~1.500 a ~500 rellenos por cuadro.
- `renderStars`: brillantes con halo + núcleo (5 → 2 fills).
- `renderSkyGradient`: franja fría `0x00182A38` encima del resplandor ámbar + línea de horizonte.
- `renderSearchlights`: lámpara en cada torre.
- `moonCenterX(w)` público; `LivingAtmosphere.renderStreet`: reflejo de la luna (`fillGradientV` + 3 destellos).

## Cambios de 0.189.0 — menos rellenos en la pausa

- `DiaryPauseScreen.drawPanel`: el brillo era un `g.fill` por fila del panel (~300 por cuadro) → **2 `fillGradient`**
  (de `PAPER_HIGHLIGHT` α0 a α140 en el 40 % de la altura, y de vuelta).
- `DiaryPauseScreen.drawVignette`: 40 pasos de 1 px × 4 lados (160 fills) → **10 bandas de 4 px** (40 fills, `t²·70`).
- `DiaryPauseScreen.drawBloodSpotWithSplatter`: doble bucle píxel por píxel (113 fills por mancha) → **un tramo por
  fila** (`sqrt(r²-dy²)`, α vertical). La pausa entera queda en ~85 llamadas de relleno por cuadro.
- `DiaryChrome.drawRegistryTagAt`: 13 fills de una fila → **3** (`STEEL_MID`, `+4` en y+2, `-2` en y+h-4); se llama en
  las 21 pantallas.
- `DiaryLanguageScreen.LanguageEntry`: hebra `0x1C422810` al pie de cada renglón, sólo si no está elegido ni hover.

## Cambios de 0.188.0 — la ciudad sin periódicos y con reflectores

- **Los periódicos de la portada se fueron del todo.** Se borraron `PosterRenderer` (943 líneas) y
  `OrnamentRenderer` (156, sólo lo usaba el otro), más `drawPosterLeft`/`drawPosterRight`,
  `overQuarantinePoster`, `wantedPosterRect`, `wantedPressing`/`wantedPressStartMs` y las dos
  apariciones que sólo se desbloqueaban ahí (`Discoveries.WANTED_EYES` y `POSTER_PEEL`). Los nombres
  están en la lista `banned` de `tools/verify_mirrors.py`. El arte queda en el historial (tag `v0.187.0`).
- **Layout de la portada sin `POSTER_*`**: `LEFT_COLUMN_W = 180` y
  `WATCH_COLUMN_W = WATCH_RADIUS * 2 + 20`; la x de la mesa de botones y la del reloj quedan idénticas
  a 0.187.0, y la ficha del cronista conserva su carril.
- **Fondo de la portada**: `VictorianSkyline.renderFarSkyline` (capa lejana: 14 rectángulos + 6 ventanas
  fijas, color `0xFF2A2116`) llamada antes de `BG_BUILDINGS`, y `AtmosphereRenderer.renderSearchlights`
  (dos haces cruzados, 18 tramos de 2 px cada uno, `0x00D8C48A`; con movimiento reducido se quedan
  quietos y en ventanas menores a 480×240 no se dibujan). Se llama desde
  `QuarantineSceneRenderer.renderTitleScene`, entre el skyline y el primer plano.
- **Optimización de esta tanda**: `FlagRenderer.drawGrid` pinta cada fila por tramos del mismo color
  (`~100 → ~25` rellenos por bandera); `DiaryLanguageScreen.LanguageEntry` memoriza etiqueta y ancho en
  el constructor (tres variantes: normal, pendiente, aplicada); `VictorianSkyline.renderCrack` eran
  tres bucles de un píxel de alto por cuadro y ahora son tres rectángulos.
- **Multijugador**: botones de `20` a `22` px de alto con `rowGap` 12, y contador de servidores anotados
  arriba a la derecha del título (`drawServerCount`, sin claves nuevas).
- **Pausa**: botón `gripeverde.pause.btn.ceda` (icono `CHAT`) que abre `DiaryDispatchScreen` sobre la
  pausa; la rejilla pasa a 7 filas y a 4×2 cuando `ph < 250`. Clave nueva → 190 por lengua.

## Cambios de 0.187.0 — vuelven las hojas del diario

El usuario aclaró que su pedido de sacar «hojas» se refería a los **periódicos de la portada**, no
al papel de las pantallas. Por eso:

- `PaperTexture.drawSheet(...)` vuelve a poner hoja opaca (`SHEET_ALPHA = 243`) con filete interior,
  tres manchas y dos fibras derivadas del panel (deterministas, sin RNG por cuadro): unas once
  llamadas de relleno por panel, la mitad que la hoja de 0.183.0.
- Las **21 pantallas** y `DiaryChrome.drawPanel` usan `drawSheet`; `drawVeil` deja de usarse.
- Los periódicos (`PosterRenderer.drawQuarantineNotice`, `drawRotatingNoticeRight`) **no se tocan**:
  siguen en la portada. Si el usuario quiere que salgan, es un cambio de una línea en
  `DiaryTitleScreen.drawPosterLeft/drawPosterRight`.

## Cambios de 0.186.0 — botones que respiran y el menú liviano

- Portada: `BTN_W 264`, `BTN_H 34`, `BTN_SLIM_W 126`, `BTN_SLIM_H 28`; la etiqueta se **encoge**
  (`DiaryTypography.drawFitted`, mínimo 0.62) en vez de recortarse con puntos suspensivos.
- Subrayado de `DiaryButton` en tramos de 3 px (antes un `fill` por píxel).
- `AtmosphereRenderer.drawSoftBlob`: **4 anillos y franjas de 2 px** (antes 6 anillos con un `fill`
  por scanline); halo de la luna de 3 blobs a 2. El humo de chimeneas y las nubes usan el mismo pincel.
- **Pendiente de confirmar con el usuario:** el pedido «las hojas devuélvelas». Los periódicos de la
  portada (`PosterRenderer`: bando, paciente cero, el tren) **no se tocaron nunca**; lo que cambió en
  0.184.0 fue el papel de los paneles de las 21 pantallas (velo translúcido). Queda por decidir si se
  quitan los periódicos de la portada o si vuelve el papel de hoja a los paneles.

## Cambios de 0.185.0 — veinte voces y el papel limpio de verdad

- **CEDA con veinte voces** (`DispatchLibrary.SLOT_COUNT = 20`), sin cambiar el formato del archivo.
  Paginación con **rueda**, **RePag/AvPag** y **clic derecho** en el rótulo; hasta doce renglones por
  página. `Ctrl`+`1..9/0` manda la voz y con `Shift` las 11..20; `Enter` baja al campo siguiente. Los
  números van en tinta fuerte cuando la voz ya tiene mensaje y el pie cuenta cuántas van
  (`gripeverde.dispatch.slot.count`, clave nueva).
- **`PaperTexture` reducido a lo que se usa**: `drawVeil` (dos sobrecargas), `PaperKind` y la paleta.
  Se borraron 23 métodos de adornos que ya nadie llamaba y `incrementOpenCount`; el verificador
  prohíbe esos nombres (`drawClean`, `drawWatermark`, `drawWaxSeal`, `drawRainDrops`, …).
- **Fuera el sello de cera del panel de pausa**: era un adorno en la hoja y costaba dos bucles
  píxel por píxel (más de mil `fill` por cuadro).
- **`DiaryTypography.fitString` por bisección** en vez de armar la cadena carácter por carácter.

## Cambios de 0.184.0 — el diario se saca la hoja de encima

Tanda de correcciones grandes, casi toda de interfaz:

- **Parte de CEDA rehecho.** `DiaryDispatchScreen` ya no tiene renglones libres ni ranuras
  al costado: es una lista de mensajes (veinte desde 0.185.0), cada uno con su campo y su tecla a la derecha.
  Se fueron ENVIAR y ENVIAR TODO (no hay envío en lote desde la ficha; el mensaje sale con su
  tecla mientras jugás). Clic en la tecla deja la ficha escuchando, Esc cancela, Supr o clic
  derecho borra. Autoguardado a los 700 ms de la última letra y otra vez al cerrar. Si no
  caben diez, paginación con el rótulo `▸ página/total` en el pie.
- **Fuera los renglones de niveles** de la ficha de sonido (`Niveles: …` y `Niveles de
  Minecraft: …`); quedan el título y el disco que suena. Se retiraron también sus tres claves
  de idioma.
- **La música del cronista ya no depende de la música vanilla:** música y ambiente del mod
  emiten por el canal `MASTER` (`FadingMusicInstance`, `LoopingAmbienceInstance`,
  `MenuAmbience.ambientOneShot`), así que los sliders Música y Ambiente de Minecraft no los
  apagan ni los atenúan. Manda el slider propio y, por encima, el maestro.
- **Ninguna pantalla dibuja hojas con adornos.** `PaperTexture.drawVeil` deja un velo
  translúcido (`VEIL_ALPHA = 196`) y un filete: se ve el fondo del menú, sin marca de agua
  (el "robot" del centro), sin sello de cera, sin gotas ni fibras. 21 pantallas migradas.
- **Sello NUEVO entero.** `drawFreshStamp` mide el lacre completo y lo mete dentro del área
  visible; si no cabe entero, no se dibuja, en vez de aparecer cortado como "NUEV".
- **Botones de portada más chicos:** `BTN_W 248`, `BTN_H 32`, `BTN_SLIM_W 112`,
  `BTN_SLIM_H 26` (antes 300×40 y 130×32).
- **Fuera el huevo de teclear cifras y fechas:** `HiddenCipher` eliminado, junto con los tres códigos que
  sólo él abría (`code_1152`, `code_pereira`, `code_doctor`). También se fueron las marcas del encuadernador y el folio escondido de la
  crónica. Los únicos huevos que quedan son los del fondo.
- **Velocidad:** la crónica memoriza el alto de cada renglón (`ensureRowHeights`/`heightAt`);
  antes volvía a cortar todo el texto en cada cuadro para saber dónde cae el scroll.
- `verify_mirrors.py` gana identificadores prohibidos nuevos (`HiddenCipher`, `onCharTyped`,
  `isChiming`, `asteriskRects`, `hiddenFolioRect`, `BINDER_MARKS`, `drawNewSeal`) para que
  nada de eso vuelva. Claves de idioma: **189** (antes 201).

## Cambios de 0.183.0 — ningún adorno contesta antes que los botones

Quedaban tres pantallas de lectura donde un adorno con clic se consultaba antes que los
widgets, la misma falla que la portada dejó atrás en 0.178.0.

- **`DiaryChrome.overAnyWidget(screen, x, y)`**: ayudante único (usa `screen.children()` y
  `widget.isActive()`), pensado para que cada pantalla pregunte primero por sus botones.
- **`DiaryFieldGuideScreen`**: `headingAt()` sólo corre si el clic no cayó en un widget; aire
  nuevo con `LINE_HEIGHT` 11→12, `PARAGRAPH_GAP` 4→7 y `SECTION_GAP` 6→10.
- **`DiaryArchiveScreen`**: los widgets se consultan antes que los papeles sueltos y que la
  banda de cierre (`panelY + panelH - 56`), que se quedaba clics; `CARD_GAP` 5→7.
- **`ChangelogScreen`**: usa el ayudante compartido (se borra la copia local de 0.182.0).
- **`DiaryDispatchScreen`**: `Ctrl`+`1`..`9` y `Ctrl`+`0` (también teclado numérico) mandan esa
  ranura; la ayuda de la ranura lo dice en los 25 idiomas.
- **Lint nuevo en `tools/verify_mirrors.py`**: rechaza `net.minecraft.util.Util` (no existe: es
  `net.minecraft.Util`) y cualquier clase de apoyo usada sin import (se omiten misma clase,
  mismo paquete y nombres completos). Probado en negativo: al quitar el import de
  `DiaryChrome` el verificador falla con `ChangelogScreen.java: usa DiaryChrome sin importarlo`.
- **CI**: el lint corre **antes** de Gradle (falla en segundos, no en dos minutos) y un push
  sin PR ahora escribe el error como **comentario de commit** (`contents: write`).

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

## Próximo paso técnico recomendado (histórico; versión 0.181.0)

Que el usuario pruebe `0.181.0` sacando CopyL de `mods`: la ficha importa los diez mensajes al primer
arranque y los atajos pasan a responder. Revisar a ojo la columna de ranuras. Queda pendiente llevar el
mismo aire a las pantallas de CopyL: el push a `Santi-PdR/Lclient` sigue en 403, así que ese cambio
viaja como parche en `tools/copyl/`.

## Reglas aprendidas (2026-11-13, versiones 0.378–0.382)

Cuatro trampas que ya costaron una captura cada una. Van aquí para no repetirlas:

1. **El juego puede pintar su fondo DESPUÉS de `renderBackground`.**
   `PackSelectionScreen` pinta su tierra por su cuenta, así que una hoja dibujada
   en `renderBackground` se pierde. Cuando la pantalla es así, el papel se pinta
   desde `render()` —encima de todo— y después se **repiten** las filas y los
   botones que la hoja tapó (`redrawOnPage`), en el mismo orden que el juego.
   Antes de mover nada de z, comprobar quién pinta el fondo y en qué orden.
   Desde 0.383.0 el patrón es uno solo y está en `DiaryChrome.renderWidgets`:
   `super.render()` **primero**, después la hoja y la cosmética de la casa, y al
   final `renderWidgets(...)` para repetir fichas y campos sobre el papel. Con
   ese orden no hay fondo del juego que tape la hoja ni rotulo blanco que quede
   encima. `tools/lint_java.py` avisa de las pantallas que dejan `super.render()`
   como última llamada.

2. **Ningún clic izquierdo del cuerpo de una lista se le pasa al juego.**
   Recolocar la `x` y llamar a `super.mouseClicked` acaba abriendo un mundo
   cuando el jugador toca a la derecha de una ficha. En el cuerpo del registro se
   marca la fila nosotros (`setSelected`) y se devuelve `true`: para entrar está
   el botón de la casa o Enter.

3. **Tooltips y avisos flotantes se pintan al final del cuadro.**
   En el inventario y con JEI, los iconos se dibujan después del cartel y salían
   encima. Se guardan los renglones en `RenderTooltipEvent.Pre` y se pintan en
   `ScreenEvent.Render.Post` con prioridad `LOWEST`. La ficha va opaca (255).

4. **Las filas de las listas del juego no son lo que parecen.**
   La fila del archivador de packs no implementa `PackSelectionModel.Entry`: la
   lleva dentro. Se saca por tipo de campo (`asPackEntry`), nunca por `instanceof`
   directo ni por nombre de campo (van ofuscados).

## 0.384.0 - textos y filas

- **Nada de puntos suspensivos.** El corte de textos vive en un solo sitio:
  `DiaryChrome.shorten(font, texto, ancho)`. Corta en la última palabra completa
  que quepa; si no cabe ni una, devuelve vacío. Las copias viejas
  (`ellipsizeTo` en cinco pantallas, `ellipsize` del Despacho, `fitText` y
  `DiaryTypography.fitString`) ahora delegan ahí, así que cambiar la política es
  tocar un método. `tools/lint_java.py` avisa si vuelve a aparecer un `"..."`
  dentro de un `drawString(`.
- **Brillo, lacre y fila elegida van DESPUÉS de la banda.** En Multijugador la
  banda de papel tapaba los tres. Regla: primero se tapa lo del juego, después
  se marca la fila. Y la banda debe usar el mismo papel que la ficha
  (`0xE6D2A6`) y cubrirla entera, o queda un reborde que parece una raya.

## 0.385.0 - listas del juego y chapas

- **La lista del juego siempre se viste.** Si una pantalla hereda de una del
  juego con lista, hay que hacer las cuatro cosas: recortar a la hoja
  (`enableScissor` en el panel), `washVanillaRows`, `paintOptionRowsInk` y
  `drawStyledScrollThumbInPanel`. `DiaryOnlineOptionsScreen` era la única que no
  lo hacía.
- **Regla del brillo:** cualquier banda o tira opaca que tape la fila del juego
  tiene que repintar el hover después (`over` con coordenadas del puntero). Ya
  pasó en Multijugador, en el archivador y en las cuatro hojas de ajustes.
  `paintOptionRowsInk` acepta `mouseX/mouseY` para eso.
- **Chapas de registro:** una por hoja, de I a XIX más XX (Aspecto).
  `tools/lint_java.py` avisa si se repite un número.

## 0.386.0 - lo que se pinta antes se borra

- El filete entre servidores vivía en `drawEntryFrames`, que corría ANTES de
  `paintServerRowsInk`: la banda de papel lo tapaba entero. Ya no existe ese
  método; el filete va dentro del paso de tinta, después del tinte.
  Regla general: **si algo se pinta antes de la banda, no existe**.
- Las hojas que se cierran tienen que sonar: `UiSounds.back()` en `onClose`.
  Faltaban en `ChangelogScreen` y `DiaryHouseScreen`. El guardia de 140 ms evita
  el doble aviso cuando el botón ya sonó.

## 0.387.0 - clics y reflexion

- **Nunca tragarse un clic sin reenviarlo.** En Mundos el candado de 0.382 se
  comia todos los clics del cuerpo y marcaba por reflexión con el nombre
  `setSelected`, que en el juego real no existe: la pantalla quedaba muda.
  La regla ahora es: fichas de la casa primero (`clickOurWidget`), después el
  clic al juego con la x puesta sobre la columna, y un **freno de 350 ms** para
  que el juego no pueda leer dos clics seguidos como doble clic.
- Las flechas usan `selectRow` (reflexión) y, si devuelve false, el
  `mouseClicked` de la fila (`GuiEventListener`, nombre que **no** se ofusca),
  con el mismo freno y pegando a la izquierda, lejos de los iconos de la fila.
- La barra de scroll se arregla sola al reenviar la x sin recortarla.

## 0.388.0 - reflexion y controles invisibles

- **Reflexion: siempre con barrido por forma.** Buscar por nombre
  (`getDeclaredMethod("setSelected")`) no sirve en el juego real: los nombres
  vienen cambiados y el `find` devuelve null. Hay que dejar una cascada por
  forma (`getDeclaredMethods()` y mirar parametros/tipo de retorno) o una
  comprobacion de que el resultado quedo como se esperaba. `tools/lint_java.py`
  avisa de cada busqueda por nombre sin barrido cerca; si el nombre es de un mod
  externo (JourneyMap, Embeddium, Forge), se marca con `// reflexion-ok`.
- **Una pantalla que no llama a `super.render` tiene que dibujar todos sus
  widgets.** En Mundos se dibujaban a mano las fichas vestidas y el buscador; un
  boton del juego que no se vistio quedaba invisible pero clickeable. Ahora hay
  una red de seguridad que recorre `children()` y pinta lo que falte.

## 0.389.0 - pantallas alcanzables

- `isHouseRawScreen` (ScreenEventHandler) es la lista de pantallas del juego que se
  visten por evento: fondo de noche + `dressPlain`. Ahora incluye
  `MouseSettingsScreen` y `controls.KeyBindsScreen`, que se abren desde Controles.
  Si aparece otra pantalla vanilla alcanzable desde la casa, va ahí.
- Multijugador: la fila de red local se pinta en papel y tinta dentro de
  `paintServerRowsInk`, con el mismo texto que la cabecera de lista vacía.
- `tools/lint_java.py`: nueva regla `notas_de_widgets` -pantalla sin `super.render`
  y sin recorrer widgets-. Se silencia con `// widgets-ok`.

## 0.394.0 - el buscador de Mundos conserva Enter y Supr

- **Foco de búsqueda:** `isSearchBoxFocused()` reconoce el `EditBox` visible con foco; `keyPressed` delega primero a `super`, antes de sus atajos para entrar o borrar el mundo elegido. Enter no abre el mundo y Supr edita la consulta. Al salir del buscador, las flechas siguen recorriendo la lista.
- **Regresión cubierta:** `tools/lint_java.py` falla si el guard de foco desaparece o queda detrás de `GLFW_KEY_ENTER`/`GLFW_KEY_DELETE`. La prueba del guard pasó tanto con el código actual como al retirar el guard temporalmente.
- **Build resiliente:** se añadió Maven Central como repositorio canónico de artefactos auxiliares que no estén reflejados en Forge Maven. El primer CI de 0.394.0 falló al no hallar `maven-artifact-3.8.5.jar` en Forge Maven; el siguiente build y publicación pasó.
- **Tags de release:** `build.yml` ahora crea tags con `--target "$GITHUB_SHA"`. Se corrigió el tag recién publicado `v0.394.0` para que apunte al commit construido `94e0908`, no al `main` antiguo.
- CI `44932587727` compiló y publicó correctamente. Release `v0.394.0`: 10.816.790 bytes, SHA-256 `351352ca283cecd3941666f554bd915f8ac1e1aa61aadec5679757f93bc5072d`. La prueba interactiva en Minecraft sigue pendiente.

## 0.393.0 - capas, clics y estilos originales

- **Multijugador:** se conserva el tamaño real de ambos ejes del favicon y se escala la imagen completa dentro de la ficha. La lista vanilla se vacía antes de cubrir cada fila, evitando que texto/colores naranjas y elementos de sus buffers aparezcan por encima del papel.
- **Un jugador:** flechas, Re Pág/Av Pág e Inicio/Fin se envían a la lista vanilla enfocada; se elimina el clic sintético de navegación. El segundo clic en una fila ya seleccionada se consume; los controles Editar/Borrar y la canaleta de scroll evitan el freno. `PaperButtons.syncStates()` se ejecuta antes del dibujo y de la entrada.
- **JourneyMap:** se retiró la modificación del tema completo. Solo se cambia el canto del minimapa y el fondo/tinta de `labelBottom` para las coordenadas; no se modifican fuentes, botones, barras, retícula ni pantalla completa.
- **Tooltips:** `TooltipEventHandler` ya no cancela `RenderTooltipEvent.Pre`, no reemplaza componentes ni aplana/recolorea texto. Minecraft dibuja sus componentes originales en su capa z=400; el evento Color conserva solo el fondo/borde pergamino.
- **Paquetes:** `GuiGraphics.flush()` se llama antes de tapar las listas y reescribir títulos; el buffer del título se vuelca antes de la banda opaca.
- CI `44924191863` compiló y publicó correctamente. Release `v0.393.0`: 10.812.961 bytes, SHA-256 `71f4f1b35399163b84287f5bb12bb13e5696fc0355e5e3c73d7c8409f0ebcd0e`. La confirmación visual sigue pendiente.

## 0.392.0 - mundos con geometria exacta en toda la hoja

- **Una sola coordenada para la fila.** `rowTopAt(i, scroll)` usa el inicio
  absoluto que devuelve la lista; el respaldo mantiene la cuenta anterior si
  la reflexión no se puede invocar. Fichas de mundos, folios, hover y hit-test
  pasan por ese helper.
- **Teclado y scroll.** Al moverse con flechas o Inicio/Fin, la pantalla mide
  cuánto sobresale la fila seleccionada y mueve el scroll justo lo necesario;
  el tope usa `getMaxScroll()` de la lista. La aguja lateral usa ese mismo
  máximo real y limita su tamaño al alto visible.

## 0.391.0 - tooltips en tinta y filas medidas por la lista

- **Tooltips de texto:** los carteles sin objeto se leen desde los componentes
  de texto del juego y se pasan a papel y tinta sin sombra; si aparece un
  componente gráfico que no se puede leer como texto, se deja el tooltip vanilla
  entero. Para los tooltips dentro de pantallas se conserva la esquina ya
  calculada con `ClientTooltipPositioner` y se pinta después del resto; no se
  recalcula desde la posición del ratón. El camino sin pantalla usa el mismo
  colocador.
- **Listas del juego:** `DiaryChrome.rowTopOf()`/`rowStepOf()` comparten la
  búsqueda por forma del método `int -> int` que devuelve el inicio de fila.
  `washVanillaRows` y `paintOptionRowsInk` ya no dependen solamente del nombre
  ofuscable del alto de fila; el Arsenal y el archivador de Packs usan esa
  geometría también para sus fichas y rótulos.
- **Deslizadores vanilla:** la aguja intenta primero el campo conocido y, si
  cambió de nombre, busca un valor `double` dentro del rango normalizado.

## 0.390.0 - geometria y JourneyMap

- **La lista dice dónde empieza cada fila.** `resolveRowTopMethod()` busca un
  método `int -> int` de la lista en el que `f(1) - f(0)` dé el alto de renglón
  (10..80) y se queda el que devuelve el número menor (arriba, no abajo). Con
  eso `rowTopOf(i)` da la geometría exacta del juego, sin depender de
  `itemHeight`/`y0`/`headerHeight`. Está en Multijugador y en Mundos; en Mundos
  además manda en `entryAt`, o sea en qué fila cae el clic.
- **JourneyMap:** `safeWrite` ignora valores nulos. `spec()` devuelve null cuando
  el `ColorSpec` no tiene el constructor esperado y escribir ese nulo dejaba el
  marco sin color.
- **Diario de JourneyMap:** `note()` anota cada mensaje distinto una vez (tope 12),
  no solo el primero. El primero siempre es "el minimapa aun no tiene medidas".
- **`tools/install-latest.ps1`:** sin token obligatorio; primero descarga directa
  del asset y, si el repo fuera privado, reintenta por la API con token.
