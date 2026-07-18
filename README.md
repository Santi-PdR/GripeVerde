# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

v0.7.1 - Calle del Title definida y postes ocultos temporalmente hasta cerrar el vacio skyline-calle en v0.7.2.

## Caracteristicas principales

- Menu principal victoriano con framework UI adaptativo.
- Fondo atmosferico del Title con cielo nocturno, luna, lluvia, niebla ocre, skyline victoriano y calle mojada.
- Calle decorativa adaptativa por escala, agregada en v0.7.0.
- Postes/faroles temporalmente ocultos desde v0.7.1 hasta resolver el soporte visual del skyline.
- Reloj de bolsillo animado con manecillas detenidas en 11:52, segundero mecanico con glitches, oscilacion pendular y tic-tac sincronizado con las marcas horarias.
- Posters institucionales (Quarantine, Wanted: Patient Zero).
- Firma easter egg de S. Pereira, MDCCCXCVII.
- Modo minimal automatico para pantallas pequenas.
- Sistema de ambiente sonoro por capas: viento, lluvia, ciudad, eventos ocasionales, presagios raros.
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Pantallas implementadas

- Title - menu principal victoriano completo
- Pause - diario del superviviente (R14, aprobada)
- Changelog - historial narrativo en pergamino
- DebugCanvas - herramienta interna de desarrollo
- SoundLab - herramienta interna de audio

## Build

Ejecutar: .\gradlew build

El jar se genera en build/libs/gripeverde-VERSION.jar

## Deploy

Ruta de deploy configurable en .deploy

Reglas actuales:
- Todo build de validacion debe ir acompanado de deploy.
- Antes de validar visualmente, limpiar jars viejos del mismo mod en la carpeta mods.
- Validar en pantalla la version mostrada por el reloj antes de aprobar o rechazar un cambio visual.

## Idiomas soportados

Ingles: en_us, en_gb, en_ca, en_au, en_nz
Espanol: es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- CONTEXTO.md - contexto maestro actual para retomar sesiones.
- CHANGELOG.md - historial narrativo de versiones orientado a jugadores.
- KNOWN_ISSUES.md - estado tecnico, decisiones activas, errores documentados.
- _backups\LECCIONES_v0.7.x.md - lecciones tecnicas de la sesion v0.7.x fallida y reglas nuevas.

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8