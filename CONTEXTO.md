# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.8.3
- build.gradle: version = '0.8.3'
- GripeVerdeMod.VERSION: "0.8.3"
- mods.toml: usa ${mod_version} desde project.version.toString() (ver E32)
- Ultimo tag publicado: v0.8.3
- Working tree esperado al retomar: limpio
- Repositorio remoto sincronizado

## Historial reciente

- v0.6.5: base estable post-revert.
- v0.7.0: Sub-hito 1 KI-03. Calle adaptativa.
- v0.7.1: Sub-hito 2 KI-03. Postes ocultos temporalmente.
- v0.8.0: Sistema de musica del Title (playback base + fixes criticos: E32, E33, KI-04).
- v0.8.1: Slider de volumen en OptionsScreen.
- v0.8.2: Crossfade entre pistas.
- v0.8.3: Ducking automatico + persistencia entre pantallas.
- Post v0.8.3: fixes de persistencia (idempotencia, auto-stop, auto-recovery), ajuste de volumenes ambientales, reescritura completa de changelogs con tono narrativo de diario.

## KI-03 esta en pausa

Sub-hitos 3 y 4 (v0.7.2 y v0.7.3) diferidos tras completar hito v0.8.
Se retomaran despues del sistema de musica.

---

## Ruta del proyecto

C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde

## Repositorio

- Local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde (privado)
- Ultimo tag: v0.8.3

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

### KI-03 (EN PAUSA)
- Calle: RESUELTO en v0.7.0.
- Postes/faroles: MITIGADO en v0.7.1 (ocultos temporalmente).
- Vacio entre skyline FG y calle en escala 1: ACTIVO (prioridad futura v0.7.2).
- Skyline demasiado poblado / sin jerarquia: ACTIVO.
- Luna solitaria sin nubes contextuales: ACTIVO.

### KI-04 (MITIGADO)
- Incompatibilidad con ModernFix RegistryObjectMixin.
- Workaround: UiSounds.safePlay() tolera NPE silenciosamente.

### KI-01 (no reproducido)
- Freeze al presionar Leave. Posible entorno.

### KI-02 (workaround conocido)
- Crash screenshots F2 con NVAPI+Embeddium+NVIDIA. Usar Win+Shift+S.

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
- CHANGELOG.md: historial narrativo completo v0.1.0 a v0.8.3 (tono diario, UTF-8 correcto).
- KNOWN_ISSUES.md: estado tecnico completo, bugs, reglas, plan futuro.
- README.md: portada publica del repo, actualizado a v0.8.3.
- _backups\LECCIONES_v0.7.x.md: errores catalogados + reglas R-*.
- assets\gripeverde\changelog\*.md: 25 archivos in-game (5 en_*, 20 es_*).

---

## Proximo sub-hito

Opciones disponibles:

### Opcion A: Retomar KI-03 (v0.7.2)
Objetivo:
- Cerrar el vacio visual entre edificios FG y calle, especialmente escala 1.

Alcance recomendado:
- Tocar SOLO VictorianSkyline (FG_BUILDINGS).
- NO tocar LivingAtmosphere.renderStreet().
- NO reactivar postes en el mismo bump.
- Validar en 4 escalas.

Sub-hito siguiente (v0.7.3):
- Reactivar postes/faroles por escala cuando el skyline los soporte.

### Opcion B: Expandir v0.8.4+
- Persistencia del volumen en disco (config file).
- Boton skip/next track.
- Curvas de fade personalizadas.
- Playlist configurable por el usuario.

### Opcion C: Nuevo hito
- Pantalla de creditos.
- Death screen tematica.
- Bestiario / notas.

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