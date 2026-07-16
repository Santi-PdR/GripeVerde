# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.6.2** - El reloj cobra vida. Segundero animado, oscilación pendular y sonido mecánico sincronizado con las marcas horarias.

## Caracteristicas principales

- Menu principal victoriano con framework UI adaptativo.
- Reloj de bolsillo animado con manecillas detenidas en 11:52, segundero mecánico con glitches, oscilación pendular y tic-tac sincronizado con las marcas horarias.
- Posters institucionales (Quarantine, Wanted: Patient Zero).
- Firma easter egg de S. Pereira, MDCCCXCVII.
- Modo minimal automatico para pantallas pequeñas.
- Sistema de ambiente sonoro por capas: viento, lluvia, ciudad, eventos ocasionales, presagios raros.
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Pantallas implementadas

- **Title** - menu principal victoriano completo
- **Pause** - diario del superviviente (R14, aprobada)
- **Changelog** - historial narrativo en pergamino
- **DebugCanvas** - herramienta interna de desarrollo
- **SoundLab** - herramienta interna de audio

## Build

Ejecutar: `gradlew.bat build`

El jar se genera en `build/libs/gripeverde-VERSION.jar`

## Deploy

Ruta de deploy configurable en `.deploy` (no versionado).
El script de deploy copia el jar generado al directorio de mods indicado.

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- `CHANGELOG.md` - historial narrativo de versiones (orientado a jugadores)
- `KNOWN_ISSUES.md` - estado tecnico, decisiones activas, errores documentados

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8