# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.5.0** - Sistema de historial de cambios integrado al menu principal.

## Caracteristicas principales

- Menu principal completamente rediseñado con estetica victoriana.
- Ambient audio por capas: viento, lluvia, ciudad, eventos ocasionales, presagios.
- Historial de cambios accesible desde el menu, con contenido localizado por idioma.
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Build

Ejecutar: gradlew.bat build

El jar se genera en build/libs/gripeverde-VERSION.jar

## Deploy

Ruta de deploy configurable en .deploy (variable DEPLOY_MODS_PATH).
El script de deploy copia el jar generado al directorio de mods indicado.

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- CHANGELOG.md - historial narrativo de versiones (orientado a jugadores)
- KNOWN_ISSUES.md - estado tecnico, decisiones activas, errores documentados

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17