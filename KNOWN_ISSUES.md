# KNOWN_ISSUES - Eternal Craft: Gripe Verde

Ultima actualizacion: v0.10.2
Estado: EN DESARROLLO
Version estable actual: v0.10.2
Ultimo tag publicado: v0.10.2
Hitos recientes: v0.10.2 (bump administrativo, changelog in-game intencionalmente sin entrada); v0.10.1 (persistencia del volumen de musica); v0.10.0 (ChangelogScreen sobre pergamino + reescritura diegetica de las 25 cronicas, KI-06 cerrado); v0.9.2 (fix humo/nubes, KI-05 cerrado); v0.9.1 (faroles reactivados, KI-03 cerrado completo); v0.9.0 hito visual del skyline; sistema de musica (v0.8.x) estable.

---

## 1. Version actual

- Proyecto: Eternal Craft: Gripe Verde
- Base: Minecraft Forge 1.20.1
- Version desplegada: 0.10.2
- Tipo: mod de interfaz + audio
- Ambientacion: ciudad victoriana bajo cuarentena, Green Flu
- Estado general: compilando limpio. Sistema de musica del Title con persistencia (v0.10.1). ChangelogScreen sobre pergamino y cronicas diegeticas (v0.10.0). Mejoras visuales previas de v0.9.x estables.

---

## 2. Sistemas activos

### Title / DiaryTitleScreen
Pipeline de fondo (estructura estable desde v0.7.1; ajustes visuales en v0.9.0):
1. AtmosphereRenderer.renderSky
2. VictorianSkyline.renderBackground
3. LivingAtmosphere.renderFogLow
4. VictorianSkyline.renderForeground
5. LivingAtmosphere.renderStreetLevel
6. LivingAtmosphere.renderAir
7. LivingAtmosphere.renderFlash
8. LivingAtmosphere.renderVignette

### MenuMusic (v0.8.3)
- 4 pistas registradas: music_title_1..4 (OGG stream)
- Playback aleatorio (shuffle), sin repeticion hasta completar ciclo
- Crossfade de 4s entre pistas
- Ducking automatico (35% durante 2.5s) al reproducir SFX RARE
- Volumen ajustable en tiempo real via MusicVolumeSlider
- Idempotente: start() no reinicia si ya suena
- Auto-stop al detectar mc.level != null (jugador en mundo)
- Persistencia: sobrevive a apertura de Options y subpantallas
- FadingMusicInstance: soporta volumen dinamico sin recrear el sample

### MenuAmbience
- Sin cambios funcionales; integracion nueva: llama a MenuMusic.duckFor(2500L) al disparar sonidos RARE

### MusicVolumeSlider
- Widget nuevo en client/widgets
- Se inyecta en OptionsScreen via ScreenEventHandler.onScreenInitPost
- Posicion: centrado horizontal, y = screen.height - 25 (pie de pagina, seguro para todas las escalas)

### UiSounds
- Version defensiva con safePlay() que tolera RegistryObject vacio
- Workaround para incompatibilidad con ModernFix RegistryObjectMixin

### LivingAtmosphere
- Sin cambios desde v0.7.0 (calle adaptativa)

### VictorianSkyline
- Sin cambios desde v0.7.1 (postes ocultos)

---

## 3. Problemas conocidos activos

### KI-01 - Freeze al presionar Leave (RESUELTO)
Sintoma original: podia quedar en "Saving world".
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Sospecha historica: entorno, RAM, GPU, Embeddium.

### KI-02 - Crash al hacer screenshots F2 (RESUELTO)
Sintoma original: NVAPI + Embeddium + NVIDIA.
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Buena practica sugerida: usar Windows + Shift + S como alternativa segura.

### KI-03 - Fondo del Title con defectos visuales (RESUELTO COMPLETO en v0.9.1)
- Calle definida: RESUELTO en v0.7.0.
- Postes/faroles flotantes: RESUELTO en v0.9.1 (reactivados con anclaje a la calle, densidad por escala GUI).
- Vacio entre skyline FG y calle: ATENUADO por jerarquia visual de v0.9.0.
- Skyline demasiado poblado / sin jerarquia: RESUELTO en v0.9.0 (BG/FG diferenciados).
- Luna solitaria: contexto atmosferico mas rico alrededor de la luna queda como mejora futura opcional (no es bug).

### KI-04 - Incompatibilidad con ModernFix RegistryObjectMixin
Sintoma: NullPointerException en RegistryObject.get() al hoverear botones.
Causa: mixin de ModernFix sobre net.minecraftforge.registries.RegistryObject.
Workaround: UiSounds.safePlay() tolera excepcion y omite el sonido sin crashear.
Estado: MITIGADO.

### Deuda tecnica activa
- ICON_BTN_MP e ICON_BTN_MODS sin PNG real.
- Framework UI subutilizado (solo Title lo consume).
- ThemeManager sin themes registrados.
- Slider de musica no persiste en disco (se pierde al cerrar juego).

---

## 4. Historial relevante reciente

### v0.7.0 - Sub-hito 1 KI-03
- renderStreet() adaptativo agregado.

### v0.7.1 - Sub-hito 2 KI-03
- getLanternPositions() devuelve int[0][0].

### v0.8.0 - Sistema de musica base
- 4 pistas OGG registradas.
- MenuMusic con playback secuencial shuffle.
- Fix critico: sounds.json corrupto por append incorrecto.
- Fix critico: mod_version hardcodeado en build.gradle (E32).
- Fix critico: UiSounds defensivo contra ModernFix (KI-04).

### v0.8.1 - Slider de volumen
- MusicVolumeSlider widget.
- Inyeccion en OptionsScreen via ScreenEventHandler.
- Reubicado 3 veces por problemas de escala; posicion final: pie de pagina.

### v0.8.2 - Crossfade
- FadingMusicInstance creado.
- MenuMusic reescrito para soportar dos instancias simultaneas.

### v0.8.3 - Ducking + persistencia
- MenuAmbience llama duckFor() en RARE.
- MenuMusic.start() idempotente.
- Auto-stop cuando jugador entra a un mundo.
- Se detecta perdida de instancia al salir de servidor y se reinicia.

---

## 5. Reglas vigentes

### Desarrollo
- UN sistema por version.
- UN cambio visual sensible por bump.
- Validacion visual/auditiva en 4 escalas cuando se toque UI/audio.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real antes de proponer cambios.
- Java UTF-8 sin BOM (UTF8Encoding false).
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre acompanado de deploy.
- R-JAR: Limpiar jars gripeverde-*.jar viejos del deploy path antes de validar.
- R-VER: Validar la version visible en el reloj antes de aprobar o rechazar.
- R-GRADLE-VER: mod_version en build.gradle debe usar project.version.toString(), nunca hardcodear.

### PowerShell
- R-CD: Todo bloque de comandos debe iniciar con Set-Location al repositorio para evitar errores de ruta (E35).
- R-PS: Minimizar cantidad de bloques PowerShell.
- R-PATH: Usar rutas absolutas si no se esta parado en la raiz del repo.
- R-PATH2: gradlew y git tambien requieren estar en la raiz del repo. Si el bloque falla y estamos en system32, Set-Location primero.

### Validacion
- R-CACHE: LivingAtmosphere cachea lanternPositions; reiniciar cliente al cambiar GuiScale si hay duda.
- R-CAP: Capturas pre/post en 4 escalas antes de validar cambios visuales.
- R-JSON: Validar JSON con ConvertFrom-Json tras cualquier append/edit a sounds.json u otros JSONs criticos.

### Java
- R-DUP: Cuando se agregan bloques nuevos que declaran variables locales, verificar que no existan ya en scope superior del metodo.

---

## 6. Errores historicos importantes

### E20 - Escritura con BOM rompio compilacion
Solucion: usar UTF8Encoding(false) al escribir Java desde PowerShell.

### E21/E23 - Parseo ingenuo de .deploy
Solucion: filtrar por DEPLOY_MODS_PATH= y no asumir primera linea.

### E27 - Replaces multi-cambio simultaneos
Regla: un replace por vez, verificacion inmediata.

### E28 - Formulas cross-file
Regla: no crear dependencias silenciosas entre renderers.

### E29 - Sin capturas de referencia
Regla: capturas pre/post en 4 escalas.

### E30 - Extrapolar entre escalas
Regla: validar escala por escala.

### E31 - Sistemas interdependientes en mismo bump
Regla absoluta: un sistema por version.

### E32 - mod_version hardcodeado en build.gradle
Sintoma: jar dice v0.7.1 en el nombre pero mods.toml interno decia v0.6.1.
Causa: build.gradle tenia mod_version: '0.6.1' hardcodeado en vez de project.version.toString().
Solucion: usar project.version.toString() para que Gradle expanda dinamicamente.

### E33 - JSON append rompio sounds.json
Sintoma: WARN Invalid sounds.json, todos los sonidos del mod silenciados.
Causa: append manual con string dejo coma huerfana antes del cierre del JSON.
Solucion: regenerar el JSON completo desde plantilla + validar con ConvertFrom-Json.

### E34 - Redefinicion de variable local en bloque agregado
Sintoma: BUILD FAILED, variable mc is already defined in method tick().
Causa: se agrego "Minecraft mc = Minecraft.getInstance()" al inicio de tick() sin verificar que otro bloque interior ya la definia.
Solucion: R-DUP.

### E35 - PowerShell corriendo desde system32
Sintoma: .\gradlew no se encuentra, git fatal not a git repository.
Causa: bloque no hizo Set-Location al repo antes de comandos de build/git.
Solucion: R-PATH2. Siempre Set-Location $repo al inicio de bloques con gradlew/git.

---

## 7. Plan futuro inmediato

- v0.9.1: COMPLETADO (faroles reactivados).
- v0.9.2: COMPLETADO (humo/nubes con blobs suaves, KI-05 cerrado).
- (Opcional) v0.10.0: Persistencia del volumen en disco / mejoras de calidad de vida del sistema de musica.
- Deuda visual pendiente: contexto atmosferico mas rico alrededor de la luna (nubes, halo mayor).

## 8. Estado tecnico

- Repo local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde
- Deploy: D:\Juegos\Minecraft\Instances\Test\mods
- Ultimo tag: v0.9.2
- Build limpio.
- Working tree esperado: limpio antes de proximas sesiones.

### KI-06 - Menu del Historial de Cambios sin ambientacion victoriana (ACTIVO)
- La pantalla ChangelogScreen usa UI vanilla de Minecraft (fondo dirt, tipografia default, boton "Cerrar" plano).
- Rompe totalmente la inmersion victoriana establecida por el Title.
- Solucion propuesta: aplicar pergamino tipo PaperTexture, tipografia acorde, marco decorativo estilo poster, boton al estilo DiaryButton.
- Prioridad: media-alta. Es la primera pantalla que el jugador abre desde el Title, y actualmente rompe el hechizo.
- Planificado: v0.10.0 o v0.11.0 (segun prioridad frente a persistencia de volumen).
### KI-05 (RESUELTO en v0.9.2)
- Humo de chimeneas, nubes del skyline y nube frente a la luna se dibujaban como rectangulos por g.fill.
- Solucion aplicada: AtmosphereRenderer.drawSoftBlob con 6 anillos concentricos y alpha cuadratico decreciente.
- Nubes reducidas a 2 blobs con radio capado a 20px para evitar gasto excesivo en radios grandes.

---

## E43 - System.IO.File con ruta relativa

Sintoma: [System.IO.File]::ReadAllText / WriteAllText con ruta relativa fallan si el CWD del proceso .NET no coincide con Set-Location de PowerShell (ocurre al abrir nueva instancia PS que arranca en C:\\Users\\<user>).
Solucion: siempre pasar ruta absoluta con $PWD\\... explicito. Cubierto por R-DOTNET-PATH y R-CD.
