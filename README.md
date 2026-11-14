**v0.183.0 - Ningún adorno contesta antes que los botones**

El manual de campo y el archivo ya no se comen los clics de los botones, el manual gana aire entre renglones, párrafos y secciones, y en el parte de CEDA Ctrl+1..0 manda la ranura de ese número.

**v0.182.0 - La crónica respira**

El historial de cambios deja de estar apretado: más aire entre renglones, títulos y versiones, margen que sigue a la ventana y botones que ya no roban clics ni sobresalen en ventanas chicas.

## Instalar la última versión

PowerShell, con el juego cerrado. Baja el `.jar` del último release directo a la carpeta de mods,
reemplaza el anterior y verifica el tamaño:

```powershell
$ProgressPreference='SilentlyContinue'; $m='D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods'; if(-not (Test-Path $m)){throw "No existe $m"}; $a=(Invoke-RestMethod 'https://api.github.com/repos/Santi-PdR/GripeVerde/releases/latest' -UseBasicParsing).assets | Where-Object { $_.name -like 'gripeverde-*.jar' } | Select-Object -First 1; if(-not $a){throw 'El ultimo release no tiene JAR'}; Get-ChildItem $m -Filter '*gripeverde*.jar' -ErrorAction SilentlyContinue | ForEach-Object { try{ Remove-Item $_.FullName -Force -ErrorAction Stop }catch{ throw "Cerra Minecraft y repeti: $($_.Name) esta en uso" } }; $out=Join-Path $m $a.name; Invoke-WebRequest -Uri $a.browser_download_url -OutFile $out -UseBasicParsing; $f=Get-Item $out; if($f.Length -ne $a.size){ throw "Descarga incompleta: $($f.Length) de $($a.size) bytes" }; "OK: $($f.Name) ($($f.Length) bytes) en $m"
```

**v0.181.0 - Los mensajes viejos se vienen con nosotros**

La ficha del parte lee los diez mensajes rápidos de CopyL (nombres, textos y teclas) y se queda con copia propia: CopyL ya puede salir de la carpeta de mods sin perder nada. Clic envía, Shift carga, Ctrl guarda y clic derecho fija la tecla.

**v0.180.0 - El parte tiene su propia mesa**

El parte de CEDA deja de depender de CopyL: es una página del diario con ocho renglones numerados, aire entre uno y otro, Enter para enviar y Tab para bajar. El borrador queda al cerrar la ficha y el envío en tanda espera medio segundo entre mensajes. Con CopyL instalado, su panel se abre desde la esquina.

**v0.179.0 - El cronista habla en toda ventana**

Cuando la ficha de saludo no cabe, el cronista deja su frase en una línea centrada en la cabecera: ya se le oye en ventanas estrechas y en las anchas y bajas. Si la ficha cabe, todo queda igual.

**v0.178.0 - Ningún adorno roba un clic**

La ventana del ático, la rata, el gato y los carteles ya no se comen los clics de los botones: preguntan primero por ellos. La vela de la pausa también. Sin cambios durante la partida.

**v0.177.0 - El susurro encuentra su banda**

El susurro de la vigilia ya no cae dentro del emblema: busca la banda libre entre el emblema y los botones y, si no cabe, baja al pie. Sin cambios durante la partida.

**v0.176.0 - Ajustes y Salir se apartan**

En ventanas estrechas Ajustes y Salir ya no cuelgan encima del emblema: pasan a un carril a la derecha de Multijugador y Mods, dentro de la banda de los botones. Sin cambios durante la partida.

**v0.175.0 - El emblema cede ante los botones**

En ventanas bajas el emblema de la portada se encoge entero para no caer sobre los botones, y la ficha del cronista se aparta o espera. La cadena del reloj arranca debajo de los iconos de arriba. Sin cambios durante la partida.

**v0.174.0 - Los carteles vuelven a respirar**

Los cuatro bandos que rotan en la portada se centran como un bloque y ajustan sus espacios en carteles bajos. El precio del elixir ya no se pega al borde inferior. Sin animaciones nuevas fuera de la portada.

**v0.173.0 - El aviso tiene un rostro**

El aviso de cuarentena de la portada abre con un grabado mayor de un hombre infectado, enmarcado como placa de imprenta y con los ojos azules de los Ghouls. La orden y el sello CEDA ya no comparten espacio, así que no se superponen en pantallas bajas. La nota de campo CEDA usa la misma placa y cambia el ojo suelto por un sello propio. Todo sigue quieto durante la partida.

**v0.167.0 - Más aire en la nota de rescate**

La nota CEDA de rescate separa las instrucciones según la altura real de la fuente para que la última línea no se monte sobre el divisor, también en el cartel compacto. El menú incluye inglés y español para las variantes regionales de Minecraft 1.20.1, incluido el español de Uruguay.

**v0.162.0 - Foco visible en los iconos**

Los botones pequeños de la portada ahora muestran un contorno ámbar cuando reciben el foco del teclado. El indicador es fijo y no añade animaciones.

**v0.161.0 - Rótulos más legibles**

Ajusté el color de la segunda línea de los botones principales para que siga destacando al pasar el cursor, sin perder el tono de latón envejecido.

**v0.160.0 - Ajustes más claros**

Las secciones de ajustes ahora tienen nombres fáciles de reconocer. Acorté los avisos de sonido y corregí la nota de rescate para que quede claro qué conserva y cómo vuelve el compañero.

**v0.159.0 - Letras sin cuadros**

Los rótulos del diario vuelven a dibujarse como texto normal de Minecraft, igual que el logotipo. Así evitamos estilos de fuente heredados que podían convertir las letras en cuadros vacíos.

# Eternal Craft: Gripe Verde

**v0.158.0 - Notas en español bien escritas**

Las notas secretas de la estrella y del sello ya llevan las tildes y la ñ que faltaban.

**v0.157.0 - Un telegrama menos invasivo**

El mensaje secreto del sello ocupa menos espacio y se retira antes para dejar la portada a la vista.

**v0.156.0 - Avisos estelares más ligeros**

La nota de la estrella fugaz conserva sus líneas ya calculadas mientras el texto, la letra y el ancho no cambian, para no volver a medirla en cada frame.

**v0.155.0 - Letras claras otra vez**

Retiré la fuente que estaba dejando cuadrados en el menú. Los textos vuelven a la letra normal de Minecraft; la portada y las partidas no reciben efectos nuevos.

**v0.154.0 - Una guía escrita por supervivientes**

El manual cuenta el brote con voz natural y explica, en inglés y español, cómo ayudar a un compañero atrapado o incapacitado y qué esperar de cada dificultad. La tipografía legible y el ambiente de cuarentena victoriana se mantienen en el menú, sin efectos sobre mundos ni inventarios.

**v0.80.0 - Avisos de descubrimiento legibles**

Los sellos de descubrimiento ahora caben en ventanas estrechas y acortan los títulos largos sin salirse de la pantalla. También corregí tildes en los nombres y pistas en español.

**v0.79.0 - Guía clara para los controles de sonido**

Los reguladores del mod ahora explican qué categoría de sonido de Minecraft también debe estar activa para oír la música y el ambiente.

**v0.78.0 - Manual de campo más legible**

La guía separa cada nota, da más aire al texto y mantiene el desplazamiento y los marcadores de lectura. El ritmo más claro facilita consultar protocolos durante una partida sin animaciones.

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientación victoriana bajo cuarentena, epidemia Green Flu.

## Versión actual

**v0.77.0 - La dirección del servidor respeta tus cambios**

La pantalla multijugador conserva la IP que edites para el servidor fijado. Solo migra la dirección oficial anterior que usaban versiones viejas del mod.

**v0.76.0 - Controles quietos durante la partida**

Los botones y otros controles propios del mod ya no animan su respuesta al pasar el cursor o pulsarlos mientras hay un mundo abierto. La portada conserva su acabado atmosférico.

**v0.75.0 - Firma adaptable en la portada**

En ventanas estrechas, la portada muestra solo la firma vigente y la recorta al espacio disponible para evitar que choque con el control de música. En pantallas amplias se conserva la firma anterior tachada.

**v0.74.0 - Encabezados de lectura limpia**

La guía de campo ahora usa el mismo texto centrado sin sombra negra automática que el resto del diario, para mantener títulos y cuerpo con un peso parejo.

**v0.73.0 - Notas fuera de la mesa principal**

Las notas contextuales ya no aparecen sobre los botones de la portada. Se conservan en la cola y se muestran dentro de las páginas de papel al abrirlas.

**v0.72.0 - Control recuperado del ambiente**

El gabinete de sonido vuelve a incluir un interruptor visible para activar o silenciar el ambiente del menú, con estado y ayuda traducidos.

**v0.71.0 - Avisos discretos en la portada**

La tarjeta emergente de descubrimiento ya no se superpone a la pantalla principal. Los hallazgos se siguen guardando en el archivo y las notas contextuales se mantienen.

**v0.70.0 - Menú adaptable a pantallas angostas**

En ventanas estrechas, los botones del título se reorganizan para evitar solapamientos; los carteles laterales se ocultan y los controles de música, opciones y salida permanecen accesibles.

**v0.69.0 - Audio fluido sin guardados repetidos**

Los deslizadores de música y ambiente actualizan el sonido al moverlos y guardan el ajuste una sola vez al soltar el control.

**v0.68.0 - Menos trabajo por tick durante la partida**

Al entrar a un mundo, la música y la ambientación del menú ahora se detienen una sola vez. La comprobación se rearma al volver al menú.

**v0.67.0 - Salida remota segura**

El menú de pausa confirma antes de desconectar de una expedición remota. Ctrl+0 solo actúa si se activa el atajo de depuración en la configuración.

**v0.66.0 - Guía para las reglas de Outbreak**

El manual aclara la progresión de los infectados, la importancia de no explorar en solitario y los requisitos que pueden limitar acciones vistas en el chat.

**v0.65.0 - Movimiento reducido y renderizado ágil**

El brillo de los faroles responde al instante cuando el movimiento reducido está activo y el perfil urbano evita repetir búsquedas por ventana en cada fotograma.

**v0.64.0 - Audio del menú detenido en partida**

La música y el ambiente del menú se apagan al entrar a un mundo, aunque la pantalla haya dejado de actualizarse al conectar.

**v0.63.0 - Idioma de respaldo coherente**

Los textos auxiliares usan español solo cuando Minecraft está en es_es; en cualquier idioma no incluido, el mod recurre al inglés junto con sus traducciones disponibles.

**v0.62.0 - Ghoul Outbreak listo al entrar**

El servidor oficial aparece seleccionado al abrir multijugador si no hay otro seleccionado. El jugador conserva el control y debe pulsar “Entrar” para conectarse.

**v0.61.0 - Actualización ligera de servidores**

La lista multijugador ya no vuelve a leer el archivo de servidores justo antes de abrir una lista nueva. Minecraft carga una sola vez los datos guardados al inicializar la pantalla, evitando trabajo de disco duplicado al agregar, editar, borrar o refrescar servidores.

**v0.60.0 - Movimiento reducido coherente**

La opción de movimiento reducido elimina los pulsos, balanceos y efectos de hover de la interfaz, incluidos los botones, el reloj, el logotipo y los destellos de estado.

**v0.59.0 - Servidor oficial siempre a mano**

La entrada fijada de Ghoul Outbreak aparece siempre en la primera fila del multijugador, actualiza su dirección y evita duplicados.

**v0.58.0 - Avisos estáticos durante la partida**

Los avisos de hallazgo y las notas marginales esperan al menú, evitando lecturas, escrituras y sonidos desde pantallas abiertas con el mundo activo.

**v0.57.0 - Niveles de audio visibles**

El gabinete sonoro muestra los niveles actuales de música, ambiente y efectos de interfaz. La banda sonora del menú ahora respeta la categoría Música de Minecraft, y la prueba aclara cómo distinguir el nivel SFX del volumen maestro.

**v0.56.0 - Gabinete sonoro compacto**

El gabinete de sonido se reorganizó para ventanas bajas: la lista, los controles de volumen, la prueba y el botón de regreso permanecen dentro del pergamino. La escena del mundo mantiene un velo fijo y no dispara el hallazgo del gramófono silencioso.

**v0.55.0 - Repaso de textos en español**

Se corrigieron tildes omitidas y se unificó el trato al jugador en los textos españoles del menú.

**v0.54.0 - Prueba de sonido clara**

El botón de prueba ahora explica que usa el nivel de efectos de interfaz y que el volumen general de Minecraft también afecta lo que se oye.

**v0.53.0 - Manual de campo claro**

Se pulió la copia española del manual: tratamiento uniforme, tildes corregidas y pasos de reanimación más claros. No cambia el juego ni agrega animaciones mientras la partida está activa.

**v0.52.0 - Manual de campo desde la pausa**

La guía de supervivencia se abre desde el menú de pausa y vuelve al mismo menú. En el mundo activo se mantiene como una hoja fija, sin transición ni fondo móvil.

**v0.51.0 - Protocolo de rescate**

La guía explica el aviso global de jugador atrapado, la liberación de un PIN, el levantamiento de un compañero y el desfibrilador que se encuentra en el botín.

## Historial anterior

**v0.30.0 - La hoja entera**

La pluma ya no escribe un tramo solo: la bitácora entera se escribe al
ritmo de tu lectura mientras bajás (lo lejano arriba se da por impreso;
sin huecos jamás). La lupa se arregló — etiqueta de papel claro con
letra de tinta, se lee perfecto. Los ticks de hover bajaron la voz. Y
los textos que quedaban bajo botones (mensaje del día, la línea corregida
de Holloway, citas de sonido) ya nunca se pisan ni se salen del papel.

**v0.29.0 - La vuelta y la lupa**

Vuelta cortés: regresar de una mesa ya no corre la ceremonia completa de
la tinta — la hoja cae de arriba en 170 ms y se acomoda; el barrido, las
salpicaduras y las gotas quedan solo para avanzar. El escritorio ya no
suena al tocarlo en silencio (pedido del dueño del estudio). Y la
bitácora gana lupa: un cuadrito al pie para buscar una palabra y saltar
de una aparición a la siguiente (Enter / Shift+Enter; Esc la olvida).

**v0.28.0 - Las gotas y los folios**

La tinta del pliegue ya no pasa en seco: del filo se desprenden gotas que
resbalan un trecho breve dejando su rastro fino (fuera con movimiento
reducido). La crónica gana folio al pie del papel — dice en qué hoja vas
y cuántas hay, y avanza solo con la lectura — y si el gabinete saltó más
de una edición, TODAS las entradas nuevas amanecen lacradas, no solo la
última. El tablón del título aprendió tres líneas nuevas por idioma.

**v0.27.0 - El pliegue y la pluma**

La cortina de tinta entre interfaces ahora tiene sentido: baja al avanzar
a una mesa nueva y sube al regresar a una ya visitada, como quien vuelve
una página del tomo; el filo salpica gotas diminutas y la hoja nueva se
acuesta con una sombra breve. Y la crónica aprendió oficio: la entrada de
la edición recién llegada se escribe delante del lector, letra a letra,
con plumín que descansa en el último trazo — un toque la termina, y con
movimiento reducido llega entera.

**v0.26.0 - Las hojas y los faroles**

Al cambiar de interfaz dentro del gabinete, la hoja nueva se revela con
una cortina de tinta (fundido breve con movimiento reducido, nada en
pantallas ajenas). Los faroles de la avenida reconocen la mano del lector:
su halo respira más amplio al acercarla. El gabinete también afinó el
oído: cada mesa se abre con su propio toque, el escritorio responde con
un golpe sordo cuando el aire calla, el gramófono anuncia pieza nueva y
volver a la vigilia vuelve a sentarte. Y las crónicas recientes fueron
reescritas con pluma de documento de la vida real.

**v0.25.2 - El telegrama no pisa la mesa** (hotfix de layout: el cartel de
los easter eggs y el sello FRAUDE ya nunca se montan sobre los botones o el
texto del cartel en GUI scale 3-4)

**v0.25.1 - El cilindro original** (hotfix: la lluvia y el viento vuelven
a los masters originales amplificados — se retira la síntesis de 0.24.0;
mezcla de lluvia 0.30 → 0.45)

**v0.25.0 - El que mira la tormenta**

Tres easter eggs nuevos en la vigilia (archivo: 52 fichas): en noche de
tormenta, quedarse a ver tres relámpagos suma ficha; la lluvia se deja
atrapar con un clic (la gota vuelve al cielo y los botones nunca se
bloquean); y escribir DOCTOR en el telégrafo oculto trae un susurro, una
campana baja y una firma que sigue llegando.

Incluye la serie 0.24.x completa — **v0.24.1 - La errata del cronista**
(hotfix de build: la 0.24.0 no compilaba por una P ilegal en un literal
hexadecimal; ya corregida, con un sello nuevo en el espejo que lintea
literales numéricos Java) sobre **v0.24.0 - El viento vuelve a soplar**:

El ambiente del gabinete por fin se escucha: el loop de viento estaba
grabado a -64 dB RMS (físicamente inaudible) y la lluvia a -32 dB. Se
regeneran por síntesis procedural (`tools/audio/regenerate_menu_ambience.py`,
reproducible) y se refuerzan ciudad, susurros y pasos; el slider de ambiente
ya manda sobre algo y la calle deja de sonar al entrar a un mundo. Visibles
nuevas: la Crónica se abre en la entrada de la versión instalada con sello de
lacre "NUEVO", ciertas sesiones (~14 %) arrancan en Noche de tormenta (lluvia
doble, truenos frecuentes, ficha propia) y la lluvia resbala en gotas por los
pergaminos. Easter eggs: el lacre del telegrama se rompe a golpes y un
diapasón oculto en Opciones de sonido afina la sala (archivo: 50 fichas).

**v0.23.2 - El telegrama llega intacto**

Hotfix de crash en campo reportado con registro completo desde una
instancia real (v0.22.0): el telegrama del easter egg del título podía
dibujarse con 0 letras visibles y la lista vacía de renglones detenía el
cliente con `ArrayIndexOutOfBoundsException`. El conteo de líneas queda
acotado por el tamaño real de la lista y el overlay decorativo blindado.

**v0.23.1 - Sellos y relojería**

Parche de mantenimiento duro sobre v0.23.0: dos sellos de build nuevos
(subtítulos resueltos en los 25 idiomas y ningún evento de sonido registrado
sin uso fuera del allowlist legacy documentado), memoria del cronista con
migración versionada + sanitize de bounds, y la capa de ambiente migrada al
mismo reloj monotónico que la música (toda la relojería del mod es ya
inmune a ajustes de hora del SO). La CI se activa en un paso con
`tools/ci/activar-ci.ps1` (la sesión no pudo subir el workflow por permisos).

**v0.23.0 - El gramófono guarda la palabra**

Pasada transversal autorizada tras la revisión técnica completa del mod
(hallazgos en `REVISION_MOD.md`). El gramófono ya no comparte la noche con
la música vanilla del menú: se silencia el `MusicManager` mientras el
gabinete está abierto, y un watchdog reanuda la playlist tras recargas del
motor de sonido (F3+T, cambio de packs) en vez de quedar muda para siempre.

Accesibilidad nueva: Destellos reducidos (fotosensibilidad, separada de
Movimiento reducido), subtítulos reales para los ocho eventos ambientales en
los 25 idiomas y la etiqueta del slider de ambiente por fin traducida. El mod
queda inerte en servidores dedicados (eliminado el registro manual que
duplicaba eventos y podía crashear un server), se despoja de la
infraestructura vacía de Mixins, y solo reemplaza Title/Pause cuando son
clases exactamente vanilla, con apagado por config para convivir con otros
mods. La memoria del cronista se escribe fuera del hilo de render y los
temporizadores usan reloj monotónico.

El build añade dos sellos (sincronía de versión y duraciones reales de las
pistas parseadas como Vorbis) y un **workflow de CI listo para activar**
(`tools/ci/build.yml` → `.github/workflows/`): compila cada push con JDK 17
y publica el jar como artifact.

### Estado de entrega

- Implementación vigente: rama `arena/01a02f45-gripeverde` (v0.30.0).
- Base: commit `4ca038d` (merge de la PR 14, v0.22.0 ya mergeada).
- Validación estática y documental completada; árbol local y remoto limpio.
- La compilación semántica contra Forge corre en el PC del owner y, una vez
  activada, en la CI (JDK 17); la validación dentro de Minecraft sigue bajo
  KI-53.
- No existe tag/release v0.23.0: debe publicarse solo después de validar el
  cliente real (junto con el pendiente de v0.22.0).

## Resumen visible del Mod Menu

Eternal Craft: Gripe Verde convierte los menus de Minecraft en un gabinete victoriano bajo cuarentena. El menu principal muestra una ciudad oscura con lluvia, niebla, reloj de bolsillo, carteles oficiales, musica y ambiente propio. Las salas personalizadas conservan cabeceras y funciones diferenciadas sobre pergamino; Video abre la interfaz de Embeddium cuando esta disponible y usa la pantalla vanilla como fallback. El conjunto tematiza la navegacion sin alterar la logica del juego.

Los contenedores del juego conservan su aspecto vanilla original; el mod no altera las texturas de los contenedores ni usa resourcepacks. El mod es client-side y no agrega gameplay.

## Caracteristicas principales

### Menu principal victoriano
- Layout responsivo para pantallas pequenas, decidido por espacio disponible y
  nunca por FPS; la calle y los faroles permanentes no dependen del rendimiento.
- Fondo atmosferico del Title con cielo nocturno, luna, lluvia, niebla ocre, skyline victoriano y calle mojada.
- Calle decorativa adaptativa por escala (v0.7.0).
- Faroles de gas reactivados en v0.9.1 con anclaje a la calle y densidad adaptativa por escala GUI.
- Reloj de bolsillo animado con manecillas detenidas en 11:52, segundero mecanico con glitches, oscilacion pendular y tic-tac sincronizado con las marcas horarias.
- Posters institucionales (Quarantine, Wanted: Patient Zero).
- Firma easter egg de S. Pereira, MDCCCXCVII.
- Gabinete de ajustes (Opciones) sobre pergamino con Title vivo detras, alcanzable desde el Title y desde la pausa (v0.11.0).

### Sistema de audio del menu (hito v0.8)
- 4 pistas de musica del menu con shuffle aleatorio.
- Crossfade de 4 segundos entre pistas consecutivas.
- Ducking automatico: la musica baja cuando suenan campanas, truenos, cadenas o cuervos.
- Slider de volumen "Menu Music" en la pantalla de Opciones, ajustable en tiempo real.
- Persistencia: la musica sobrevive a Options y subpantallas, se detiene sola al entrar a un mundo, y arranca de nuevo al volver.
- Sistema de ambiente sonoro por capas: viento, lluvia, ciudad, eventos ocasionales, presagios raros.

### La memoria del gabinete (v0.16.0)
- Memoria persistente propia (descubrimientos, objetos, noches seguidas, ausencias) separada del ajuste de audio.
- 50 descubrimientos con fichas, sellos y pistas encadenadas; algunas consecuencias son permanentes.
- Marginalia contextual del cronista: notas que responden a lo que el lector hace.
- La Sala de consulta: detras del ultimo folio de la cronica, un archivo con las fichas halladas y documentos recuperados.

### Otros
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).
- v0.23.0: subtitulos de los eventos ambientales en los 25 idiomas, ayuda
  Destellos reducidos (fotosensibilidad), apagado del gramofono vanilla del
  menu, convivencia configurable con otros mods de menu y CI con Java 17.

## Pantallas implementadas

- **Title** - menu principal victoriano completo con musica y ambiente sonoro.
- **Pause** - diario del superviviente (R14, aprobada).
- **Changelog** - historial narrativo en pergamino.
- **Options** - gabinete central con accesos propios y slider FOV integrado.
- **Sound** - sala acustica sobre pergamino y sliders propios del mod.
- **Video** - Embeddium si esta instalado; pantalla de video vanilla como fallback.
- **Controls** - manual operativo con toggles sobre pergamino.
- **Language** - archivo linguistico con lista y seleccion localizadas.
- **Chat** - protocolos de correspondencia sobre pergamino.
- **Accessibility** - opciones vanilla mas el toggle persistente de Movimiento reducido.
- **Online / Multiplayer** - registros de conexion y hospedaje con presentacion propia.
- **Credits** - ultimo archivo del gabinete.
- **Archive** - sala de consulta (50 fichas).
- **DirectJoin / EditServer / Confirm** - formularios y autorizaciones pulidos en v0.21.0.

## Build

Con Java 17, ejecutar: `.\gradlew.bat clean build`

El jar se genera en `build\libs\gripeverde-VERSION.jar`

Verificaciones activas en `processResources`:
- `verifyGripeVerdeLocalization` (JSON válido, 93 claves en paridad y
  v0.23.0 presente en los 25 changelogs).
- `verifyGripeVerdeSoundAssets` (eventos completos, referencias existentes y ningún OGG huérfano).
- `verifyGripeVerdeNoDeadCode` (símbolos retirados fuera del código productivo).
- `verifyGripeVerdeVersionSync` (`GripeVerdeMod.VERSION` == version Gradle).
- `verifyGripeVerdeMusicDurations` (duración real Vorbis de cada OGG de
  música vs `TRACK_DURATIONS_MS`, tolerancia 1.5 s).
- `verifyGripeVerdeSubtitles` (toda clave de subtítulo no vacía existe en
  los 25 idiomas).
- `verifyGripeVerdeSoundUsage` (ningún SoundEvent registrado sin uso en el
  código; el allowlist legacy quedó vacío en v0.26.0: los 8 eventos
  restantes se cablearon a sus momentos, V2 de REVISION_MOD cerrada).

Ademas, `tools/ci/build.yml` es un workflow de CI listo: al moverlo a
`.github/workflows/build.yml` compila cada push/PR con JDK 17 y publica el
jar como artifact. La sesion no pudo colocarlo directamente porque el token
de GitHub no tiene permiso `workflows`.

## Deploy

Repositorio local actual: `C:\Users\santi\Desktop\GripeVerde`.

Instancia de prueba de SKLauncher:
`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.

Antes de cada validacion se eliminan los jars anteriores de Gripe Verde, se
compila y se copia exactamente el jar de la version vigente. La version
mostrada por el reloj debe comprobarse en pantalla antes de aprobar la
entrega.

Flujo vigente para actualizar, compilar y desplegar: **comandos PowerShell
inline** (fetch + `reset --hard FETCH_HEAD`, `gradlew clean build` y copia
del jar por glob a la instancia test-1), sin archivos `.ps1` nuevos;
`tools\deploy.ps1` queda corregido como alternativa equivalente. Regla
permanente: cada entrega de cambios termina con ese bloque listo para pegar.
El script lee la version de `build.gradle`, hace fetch/reset de la rama de
sesion, limpia los jars antiguos, compila y copia el jar exacto.

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- `CONTRIBUTING.md` - guia corta de contribucion (branch fija, no gameplay, validacion).
- `CONTEXTO.md` - contexto maestro actual para retomar sesiones v0.23.0.
- `CHANGELOG.md` - historial narrativo de versiones orientado a jugadores (v0.10.0 → v0.23.0).
- `KNOWN_ISSUES.md` - estado tecnico, decisiones activas, errores documentados, KIs.
- `REVISION_MOD.md` - revision tecnica completa del 2026-08-23 (origen de v0.23.0) junto con las propuestas futuras pendientes de autorizacion.
- `README.md` - esta portada.

Históricos eliminados en v0.21.2: `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` (eran referencia de polish v0.14.0, ya revertido) y `CONTEXTO_PARA_IA.md` (resumen redundante de `CONTEXTO.md`).

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8
