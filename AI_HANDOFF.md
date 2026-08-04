# Handoff para otra IA - Eternal Craft: Gripe Verde

## Estado actual

- Proyecto: `Santi-PdR/GripeVerde`
- Branch obligatoria: `arena/019fbe66-gripeverde`
- Commit actual de trabajo: `45fa59e fix: categorizar sonidos de botones`
- Version del mod: `0.13.10`
- Minecraft: `1.20.1`
- Forge: `47.4.20`
- Java: `17`
- Jar esperado: `build/libs/gripeverde-0.13.10.jar`
- Repo local del usuario: `C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde`
- Deploy Test: `D:\Juegos\Minecraft\Instances\Test\mods`

## Objetivo del proyecto

Gripe Verde es un mod client-side de identidad visual, sonora y de navegacion para Minecraft Forge. El objetivo actual no es agregar gameplay, sino convertir la experiencia de menus, pantallas y resource pack en una interfaz victoriana profesional, coherente y compatible con modpacks.

El tono del proyecto es diegetico: cuarentena victoriana, cronista, ciudad enferma, gabinete, pergamino, acero gris y sonido de mecanismos/latches. Evitar textos que suenen a devlog dentro del juego.

## Reglas activas del usuario

- El usuario habla en espanol y quiere respuestas directas.
- No implementar gameplay.
- Tras cualquier cambio de codigo/assets/docs, entregar siempre PowerShell completo para actualizar, compilar y desplegar a Test.
- No entregar solo `$expectedCommit`.
- Mantener shortcuts ocultos solamente:
  - `Ctrl+S` en Title -> Singleplayer.
  - `Ctrl+0` en mundo/server -> salir al Title.
- No mostrar hints visibles de esos shortcuts.
- No tocar una zona que el usuario diga que ya esta solucionada.
- En cambios de containers vanilla, conservar layout y slots reconocibles de Minecraft; solo cambiar paleta/estetica.
- Mantener compatibilidad con mods: no aplicar overlays a backpacks/containers modded.

## Estado visual y UI

Pantallas principales implementadas con estilo Diary/Gripe Verde:

- Title victoriano con ciudad nocturna viva.
- Pause tipo diario.
- Options como gabinete de opciones.
- Language con banderas y scroll suave.
- Sound options con sliders propios de musica/ambiente.
- Chat options.
- Accessibility options.
- Skin customization.
- Controls hub.
- Online options.
- Credits hub.
- Multiplayer, Direct Join, Edit Server y Confirm.
- Changelog in-game narrativo.
- SoundLab interno.

## Resource pack / containers

Resource pack integrado: `src/main/resources/resourcepacks/gripeverde_theme`

Containers cubiertos en `assets/minecraft/textures/gui/container/`:

- inventory
- crafting_table
- generic_54
- furnace
- smoker
- blast_furnace
- shulker_box
- anvil
- grindstone
- enchanting_table
- beacon
- brewing_stand
- hopper
- dispenser
- dropper
- loom
- cartography_table
- stonecutter
- smithing
- horse
- villager2

Notas importantes:

- `villager2.png` debe ser `512x256`.
- El resto de containers debe ser `256x256`.
- `pack.png` debe ser `64x64`.
- Stonecutter, Loom y Smithing deben conservar layout vanilla.
- Smithing no debe dibujar preview/maniqui adicional desde codigo.

## Sonidos UI actuales

Archivos UI activos importantes:

- `button_clean_03_brass_latch.ogg` -> botones generales/principales.
- `modal.ogg` -> botones que abren secciones del gabinete.
- `back1.ogg`, `back2.ogg` -> volver/cerrar/destruir.
- `scroll1.ogg`, `scroll2.ogg` -> scroll.

Eventos relevantes en `sounds.json`:

- `ui.button_clean`
- `ui.button_main`
- `ui.button_section`
- `ui.back`
- `ui.scroll`

Archivos rechazados por el usuario y eliminados del flujo:

- `button_clean_01_paper_tick.ogg`
- `button_clean_02_wooden_key.ogg`
- `error1.ogg`
- `error2.ogg`
- `open1.ogg`
- `open2.ogg`
- `respawn.ogg`

No reintroducir esos sonidos sin pedir confirmacion.

## Codigo clave

- Version:
  - `build.gradle`
  - `src/main/java/com/eternalcraft/gripeverde/GripeVerdeMod.java`
- Eventos de pantalla:
  - `src/main/java/com/eternalcraft/gripeverde/client/ScreenEventHandler.java`
- Compat containers vanilla:
  - `src/main/java/com/eternalcraft/gripeverde/client/ContainerOverlayHandler.java`
- Audio:
  - `src/main/java/com/eternalcraft/gripeverde/client/audio/UiSounds.java`
  - `src/main/java/com/eternalcraft/gripeverde/client/audio/ModSounds.java`
  - `src/main/java/com/eternalcraft/gripeverde/client/audio/MenuMusic.java`
  - `src/main/java/com/eternalcraft/gripeverde/client/audio/MenuAmbience.java`
- Render:
  - `AtmosphereRenderer.java`
  - `LivingAtmosphere.java`
  - `VictorianSkyline.java`
  - `PaperTexture.java`
  - `DiaryChrome.java`
  - `LogoRenderer.java`
  - `PosterRenderer.java`
- Widgets:
  - `DiaryButton.java`
  - `SlimButton.java`
  - `GreenFluButton.java`
  - `DiarySlider.java`
  - `DiaryTextField.java`
  - `DiaryToggle.java`
- Scroll suave:
  - `src/main/java/com/eternalcraft/gripeverde/client/util/SmoothScroll.java`

## Validaciones importantes

Antes de entregar cambios:

1. `git status --short`
2. Validar JSON:
   - `src/main/resources/assets/gripeverde/lang/*.json`
   - `src/main/resources/assets/gripeverde/sounds.json`
3. Verificar que `sounds.json` no apunte a OGG faltantes.
4. Verificar dimensiones del resource pack:
   - `pack.png` 64x64
   - `villager2.png` 512x256
   - containers restantes 256x256
5. Build local si el entorno tiene Java:
   - `gradlew.bat build --no-daemon` en Windows del usuario.

## PowerShell base requerido por el usuario

Despues de cualquier commit nuevo, entregar un script completo que:

- Force-fetch de `arena/019fbe66-gripeverde`.
- Haga `git switch main`.
- Haga `git reset --hard origin/arena/019fbe66-gripeverde`.
- Haga `git clean -fd`.
- Verifique el commit esperado exacto.
- Verifique version en `build.gradle` y `GripeVerdeMod.java`.
- Limpie `build/libs/gripeverde-*.jar`.
- Ejecute `gradlew.bat build --no-daemon`.
- Si falla el build, no despliegue.
- Limpie jars viejos en `D:\Juegos\Minecraft\Instances\Test\mods`.
- Copie `gripeverde-0.13.10.jar` al Test.

## Estado documental

Documentos relevantes actualizados en esta sesion:

- `README.md`
- `CHANGELOG.md`
- `KNOWN_ISSUES.md`
- `CONTEXTO.md`
- `AI_HANDOFF.md`

## Riesgos / cuidados

- No usar imports/clases inexistentes de Minecraft 1.20.1.
- No usar `EnderChestScreen`; detectar Ender Chest por titulo `container.enderchest`.
- No modificar containers modded desde `ContainerOverlayHandler`; restringir a paquete vanilla `net.minecraft.client.gui.screens.inventory.`.
- PowerShell: evitar strings tipo `v$expectedVersion:`; usar `${expectedVersion}`.
- PowerShell/.NET: usar rutas absolutas para escritura/lectura de archivos.
- No volver a meter sonidos rechazados por el usuario.
- No meter overlays decorativos que cubran slots o elementos interactivos vanilla.
