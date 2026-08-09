# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.20.0 - El gabinete encuentra su propia mano**

Tras ordenar su estructura y aprender modales de imprenta, el gabinete dio
un paso mas: varias pantallas que aun se sentian como formularios vanilla
vestidos sobre papel fueron rehechas como piezas propias del mismo mundo.
La sala de consulta, el registro de revisiones, las fichas de conexion, el
espejo de observacion y los formularios de autorizacion ya no parecen
pantallas sueltas con un marco tematico, sino partes de una oficina comun,
con composicion mas fisica, mejor jerarquia y una identidad visual mas
convincente.

## Resumen visible del Mod Menu

Eternal Craft: Gripe Verde convierte los menus de Minecraft en un gabinete victoriano bajo cuarentena. El menu principal muestra una ciudad oscura con lluvia, niebla, reloj de bolsillo, carteles oficiales, musica y ambiente propio, mientras que cada sala del gabinete adopta una identidad concreta: Options como mesa central, Sound como sala instrumental, Video como aparato de observacion, Controls como manual operativo, Language como archivo linguistico, Chat como correspondencia, Accessibility como expediente de percepcion, Multiplayer como registro de hospedajes, Credits como acta final y las pantallas de formulario como documentos oficiales. El objetivo ya no es solo tematizar vanilla, sino hacer que la interfaz parezca realmente propia de Gripe Verde.

Los contenedores del juego conservan su aspecto vanilla original; el mod no altera las texturas de los contenedores ni usa resourcepacks. El mod es client-side y no agrega gameplay.

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

### La memoria del gabinete (v0.16.0)
- Memoria persistente propia (descubrimientos, objetos, noches seguidas, ausencias) separada del ajuste de audio.
- 43 descubrimientos con fichas, sellos y pistas encadenadas; algunas consecuencias son permanentes.
- Marginalia contextual del cronista: notas que responden a lo que el lector hace.
- La Sala de consulta: detras del ultimo folio de la cronica, un archivo con las fichas halladas y documentos recuperados.

### Otros
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Pantallas implementadas

- **Title** - menu principal victoriano completo con musica y ambiente sonoro.
- **Pause** - diario del superviviente (R14, aprobada).
- **Changelog** - historial narrativo en pergamino.
- **Options** - gabinete central con agrupacion visual, carril de estado y slider FOV integrado.
- **Sound** - sala acustica con medidores, estado de mezcla y sliders propios del mod.
- **Video** - aparato de observacion sobre consola vanilla, con presentacion tecnica propia.
- **Controls** - manual operativo con resumen de postura y protocolo.
- **Language** - archivo linguistico con estado actual/pediente y presentacion de consulados.
- **Chat** - correspondencia con carril documental y archivo de entregas.
- **Accessibility** - expediente de observacion con lectura por capas.
- **Online / Multiplayer** - registros de conexion y hospedaje sobre presentacion propia.
- **Credits** - ultimo archivo del gabinete.
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
