# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.10.3
- build.gradle: version = '0.10.3'
- GripeVerdeMod.VERSION: "0.10.3"
- mods.toml: usa ${mod_version} desde project.version.toString() (ver E32)
- Ultimo tag publicado: v0.10.3
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
- Ultimo tag: v0.10.3

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

## PROXIMO HITO PLANIFICADO (revisado tras v0.10.1)

### Candidato C: v0.11.0+ - Contenido de gameplay (Gripe Verde en si)
Objetivo: primer sistema de gameplay real (enfermedad, sintomas, mecanica de cuarentena).
Riesgo: alto. Requiere diseno previo escrito.

### Candidato D: v0.10.3 - Mejoras visuales del Title pendientes
Objetivo: contexto atmosferico mas rico alrededor de la luna (halo, jirones de nube que la crucen).
Riesgo: bajo. Toca solo LivingAtmosphere/AtmosphereRenderer.

### Candidato E: v0.10.3 alternativo - Persistir mas opciones del usuario
Objetivo: extender ClientConfig con volumenes de MenuAmbience (wind/rain/city separados), o un toggle "silenciar ambiente".
Riesgo: bajo. Reutiliza el framework de v0.10.1.

Decision pendiente para proxima sesion.

---
## ADDENDUM v0.10.3 (hotfix administrativo)

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