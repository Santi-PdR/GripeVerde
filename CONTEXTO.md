# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.8.3
- build.gradle: version = '0.8.3'
- GripeVerdeMod.VERSION: "0.8.3"
- mods.toml: usa ${mod_version} (expansion desde project.version, ver E32)
- Ultimo tag publicado: v0.8.3
- Working tree esperado al retomar: limpio

## Historial reciente

- v0.6.5: base estable post-revert.
- v0.7.0: Sub-hito 1 KI-03. Calle adaptativa.
- v0.7.1: Sub-hito 2 KI-03. Postes ocultos temporalmente.
- v0.8.0: Sistema de musica del Title (playback base + fixes criticos: E32, E33, KI-04).
- v0.8.1: Slider de volumen en OptionsScreen.
- v0.8.2: Crossfade entre pistas.
- v0.8.3: Ducking automatico + persistencia entre pantallas.

## KI-03 esta en pausa

Se pausaron los sub-hitos 3 y 4 de KI-03 (v0.7.2 y v0.7.3) para completar
el hito v0.8 (sistema de musica). Se retomaran despues.

---

## Ruta del proyecto

C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde

## Repositorio

- Local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde (privado)
- Ultimo tag publicado: v0.8.3

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
- assets\gripeverde\sounds.json
- assets\gripeverde\sounds\music\music_title_1..4.ogg

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
- init(): MenuAmbience.start() + MenuMusic.start() (idempotente)
- tick(): MenuAmbience.tick() + MenuMusic.tick() (auto-stop si mc.level != null)
- removed(): MenuAmbience.stop() (MenuMusic NO se detiene para persistir)

---

## Estado del sistema de musica (v0.8.3)

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

### FadingMusicInstance
- Extiende AbstractSoundInstance implementa TickableSoundInstance
- Volumen dinamico controlable via setDynamicVolume()
- Se marca como done cuando expira su duracion + 1000ms buffer
- Duraciones hardcoded por pista (medidas al convertir):
  music_1: 160s, music_2: 90s, music_3: 177s, music_4: 160s

### MusicVolumeSlider
- Widget nuevo en OptionsScreen
- Posicion: centrado horizontal, y = screen.height - 25
- Ancho: 160px, alto: 20px
- Se inyecta via ScreenEventHandler.onScreenInitPost

---

## Bugs conocidos activos KI-03

- Calle: resuelta.
- Postes/faroles: ocultos temporalmente.
- Vacio entre skyline y calle: activo, prioridad v0.7.2.
- Skyline demasiado poblado/sin jerarquia: activo.
- Luna sin nubes contextuales: activo.

## Otros bugs

- KI-04: Incompatibilidad ModernFix RegistryObjectMixin. MITIGADO via UiSounds.safePlay().

---

## Resolucion del usuario

- Nativa: 1920 x 1080 (ver capturas recientes) / historicamente 1536 x 864
- Alturas efectivas por GuiScale:
  - Escala 1: h grande
  - Escala 2: h medio
  - Escala 3: h chico
  - Escala 4: h muy chico (modo minimal en Title)

---

## Reglas vigentes

### Desarrollo
- Un sistema por version.
- Un cambio visual sensible por bump.
- Validacion visual/auditiva obligatoria.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real antes de proponer cambios.
- Java UTF-8 sin BOM.
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre con deploy.
- R-JAR: Limpiar jars viejos antes de validar.
- R-VER: Confirmar version en el reloj antes de aprobar.
- R-GRADLE-VER: mod_version debe usar project.version.toString().

### PowerShell
- R-PS: Minimizar bloques.
- R-PATH: Rutas absolutas si no estamos en la raiz.
- R-PATH2: gradlew/git requieren Set-Location al repo primero.

### Validacion
- R-CACHE: Reiniciar cliente al cambiar GuiScale si hay duda.
- R-CAP: Capturas pre/post en 4 escalas para cambios visuales.
- R-JSON: Validar JSON con ConvertFrom-Json tras cualquier edit.

### Java
- R-DUP: Verificar que no se redefinan variables en scope.

---

## Errores clave a NO repetir

- E27-E31: ver _backups\LECCIONES_v0.7.x.md
- E32: mod_version hardcodeado en build.gradle (usar project.version.toString())
- E33: JSON append rompio sounds.json (validar con ConvertFrom-Json)
- E34: variable mc duplicada en tick() (R-DUP)
- E35: PowerShell desde system32 (R-PATH2)

---

## Deploy

.deploy contiene: DEPLOY_MODS_PATH=D:\Juegos\Minecraft\Instances\Test\mods

Regla:
- Build + deploy juntos.
- Limpiar jars gripeverde-*.jar viejos antes de validar visualmente.

---

## Documentacion del proyecto

- CONTEXTO.md: este archivo, punto unico de entrada.
- CHANGELOG.md: historial narrativo orientado a jugadores.
- KNOWN_ISSUES.md: estado tecnico completo, bugs, reglas, plan futuro.
- _backups\LECCIONES_v0.7.x.md: errores catalogados de la sesion fallida + reglas R-*.

---

## Proximo sub-hito

### v0.7.2 - Cerrar vacio skyline-calle (retomar KI-03)

Objetivo:
- Cerrar el vacio visual entre edificios FG y calle, especialmente en escala 1.

Alcance recomendado:
- Tocar SOLO VictorianSkyline.
- Ajustar FG_BUILDINGS.
- NO tocar LivingAtmosphere.renderStreet().
- NO reactivar postes en el mismo bump.
- Validar en 4 escalas.

Sub-hito siguiente (v0.7.3):
- Reactivar postes/faroles por escala.

### v0.8.4+ (opcional)
- Persistencia de volumen en disco.
- Boton skip/next.
- Curvas de fade personalizadas.

---

## Al retomar

Leer este documento COMPLETO antes de proponer cualquier cambio.

Si el usuario dice "arranquemos":
1. Set-Location al repo.
2. git status --short y git log --oneline -5.
3. Preguntar si retomar KI-03 (v0.7.2) o expandir v0.8.

Comandos de reentrada:
  Set-Location C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
  git status --short
  git log --oneline -5