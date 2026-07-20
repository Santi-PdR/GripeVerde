# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.11.2
- build.gradle: version = '0.11.2'
- GripeVerdeMod.VERSION: "0.11.2"
- mods.toml: usa ${mod_version} desde project.version.toString() (ver E32)
- Ultimo tag publicado: v0.11.2
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
- Ultimo tag: v0.11.2

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
