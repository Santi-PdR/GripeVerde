# Eternal Craft: Gripe Verde

## Estado para traspaso IA (commit 45fa59e)

- Version actual: `0.13.10`.
- Rama de trabajo fija: `arena/019fbe66-gripeverde`.
- Commit de referencia: `45fa59e fix: categorizar sonidos de botones`.
- Objetivo activo: polish visual/sonoro/UI client-side; no gameplay.
- Build objetivo: Minecraft `1.20.1`, Forge `47.4.20`, Java `17`.
- Deploy Test del usuario: `D:\Juegos\Minecraft\Instances\Test\mods`.
- Documento de traspaso detallado: `AI_HANDOFF.md`.

### Audio UI vigente

Los botones usan categorias sonoras separadas:

- `ui.button_main`: botones principales y entrada general, usando `button_clean_03_brass_latch.ogg`.
- `ui.button_section`: botones que abren secciones del gabinete, usando `modal.ogg`.
- `ui.back`: volver/cerrar/salir, usando `back1.ogg` y `back2.ogg`.

No reintroducir los sonidos rechazados: `button_clean_01`, `button_clean_02`, `error1`, `error2`, `open1`, `open2`, `respawn`.


Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.13.10 - Acero bajo la lluvia**

El gabinete vuelve a abrir sus maquinas con una disciplina mas severa:
los hornos, cajas menores, mesas de oficio, inventarios de caballeriza y
libros de intercambio obedecen ahora al mismo acero gris, remachado y
sobrio. La mesa arcana conserva su purpura de vigilia, pero sus renglones
respiran entre chispas y destellos, como si cada palabra hubiese sido
escrita con una aguja cargada de electricidad.

Afuera, la noche tambien gano oficio. La lluvia ya no cae sin dejar
memoria: salpica la calle, dibuja ondas en los charcos y se inclina cuando
el viento arrecia. Los faroles proyectan reflejos ambar sobre el empedrado,
pequenas polillas los rondan, y papeles de cuarentena se arrastran por la
avenida mientras las ramas ensucian el vidrio con sombras moviles.

El paquete se activa por defecto al instalar el mod y puede desactivarse
desde Ajustes si el jugador prefiere el aspecto vanilla.

## Caracteristicas principales

### Menu principal victoriano
- Framework UI adaptativo con modo minimal automatico para pantallas pequenas.
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

### Otros
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Pantallas implementadas

- **Title** - menu principal victoriano completo con musica y ambiente sonoro.
- **Pause** - diario del superviviente (R14, aprobada).
- **Changelog** - historial narrativo en pergamino.
- **Options** - pantalla vanilla extendida con slider de musica del menu.
- **DebugCanvas** - herramienta interna de desarrollo.
- **SoundLab** - herramienta interna de audio.

## Build

Ejecutar: `.\gradlew build`

El jar se genera en `build/libs/gripeverde-VERSION.jar`

## Deploy

Ruta de deploy configurable en `.deploy`.

Reglas actuales:
- Todo build de validacion debe ir acompanado de deploy.
- Antes de validar visualmente, limpiar jars viejos del mismo mod en la carpeta mods.
- Validar en pantalla la version mostrada por el reloj antes de aprobar o rechazar un cambio.

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- `CONTEXTO.md` - contexto maestro actual para retomar sesiones.
- `CHANGELOG.md` - historial narrativo de versiones orientado a jugadores.
- `KNOWN_ISSUES.md` - estado tecnico, decisiones activas, errores documentados.
- `_backups\LECCIONES_v0.7.x.md` - lecciones tecnicas y reglas operativas.

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8
