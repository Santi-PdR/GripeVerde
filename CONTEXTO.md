# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.13.9
- build.gradle: version = '0.13.9'
- GripeVerdeMod.VERSION: "0.13.9"
- mods.toml: usa ${mod_version} desde project.version.toString() (ver E32)
- Ultimo tag publicado: v0.13.9
- Working tree esperado al retomar: limpio
- Repositorio remoto sincronizado

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
- Ultimo tag: v0.13.9

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

### KI-06 (ACTIVO)
- Menu del Historial de Cambios usa UI vanilla de Minecraft (fondo dirt, tipografia default).
- Rompe la ambientacion victoriana del Title.
- Planificado para v0.10.x u v0.11.x.

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
- 1 helper nuevo en DiaryChrome: drawListFades.
- Infraestructura mixin armada aunque sin mixins activos (client:[] vacio).
- 4 KIs cerrados: KI-10, KI-15, KI-16, y avance en KI-07.

### Codigo nuevo
- client/widgets/DiaryTextField.java
- client/widgets/DiaryToggle.java
- client/TooltipEventHandler.java

### Codigo modificado
- render/DiaryChrome.java (drawListFades + drawStyledScrollThumbInPanel condicional)
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
- 1 helper nuevo en DiaryChrome: drawListFades.
- Infraestructura mixin armada aunque sin mixins activos (client:[] vacio).
- 4 KIs cerrados: KI-10, KI-15, KI-16, y avance en KI-07.

### Codigo nuevo
- client/widgets/DiaryTextField.java
- client/widgets/DiaryToggle.java
- client/TooltipEventHandler.java

### Codigo modificado
- render/DiaryChrome.java (drawListFades + drawStyledScrollThumbInPanel condicional)
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
DiaryProgressBar, DiarySeparator).

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
- DiarySeparator: draw, drawWithLozenge, drawDouble, drawVertical.
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
  DiarySlider, DiaryProgressBar, DiarySeparator.

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

### KI-15 (NUEVO en v0.13.9)
- Containers menores no cubiertos por M1: brewing_stand, hopper, dispenser,
  dropper, loom, cartography_table, stonecutter, smithing, villager,
  horse. Deuda opcional. Bajo impacto (uso poco frecuente).

## PROXIMO HITO PLANIFICADO (revisado tras v0.13.9)

### Candidato M1.2: v0.13.10+ - Containers menores restantes
Objetivo: extender el pack a brewing_stand, hopper, dispenser, dropper,
loom, cartography_table, stonecutter, smithing, villager, horse. Ciclo
opcional, bajo impacto, alto costo relativo. Podria hacerse en un solo
bump agrupado si Alex/usuario lo pide.

### Candidato M2: v0.13.x - Hotbar (widgets.png)
Objetivo: hotbar pergamino/steel. Duda pendiente: mantener pergamino
victoriano (coherente con menus) o steel (coherente con containers).
Requiere decision de scope de arte.

### Candidato M5: v0.13.x - Tooltips de items estilizados
Objetivo: extender fix de tooltips (KI-10) a tooltips de items en el
inventario. Mixin o RenderTooltipEvent.Color extendido.

### Candidato M9: v0.14.0 - Hito Mixin dirt bypass (KI-12)
Objetivo: reemplazar dirt background de MouseSettings, KeyBinds,
PackSelection, OnlineOptions. Ruta identificada, hito dedicado.

### Candidato M10: v0.13.x - Changelog in-game agrupado v0.13
Objetivo: agregar entrada agrupada "El gabinete se llena de maquinas"
en los 25 archivos changelog/*.md in-game. INCLUIDO en este cierre.

### Candidato M11: v0.13.x - Deprecations Forge
Objetivo: migrar FMLJavaModLoadingContext.get() y ResourceLocation
(String,String) a APIs no-deprecated. No urgente en 1.20.1 estable.

### Candidato M7: v0.14.0+ - Contenido de gameplay (Gripe Verde)
Objetivo: primer sistema real de gameplay. Requiere diseno previo escrito.