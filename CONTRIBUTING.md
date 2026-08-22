# Contributing - Eternal Craft: Gripe Verde

Guia corta para tocar el mod sin romper las reglas del proyecto.

## Reglas base

1. **Rama fija:** el trabajo de cada sesion vive en su rama `arena/*-gripeverde`.
   No cambiar de branch ni mergear ramas viejas sin verificar `git log` y
   `build.gradle version` (ver E-STEEL-REGRESSION).
2. **PROHIBIDO gameplay:** no agregar sistemas de enfermedad, cuarentena
   jugable, NPCs, items, bloques, recetas ni efectos que alteren la
   supervivencia. El mod es solo interfaz + audio + memoria narrativa.
3. **Sin resourcepacks:** nada de `gripeverde_steel`, `tools/steel_pack` ni
   texturas vanilla modificadas (R-NO-RESOURCEPACK). La identidad visual se
   hace con Java/render/widgets.
4. **No tocar logica vanilla:** solo presentacion (R-VANILLA-LOGIC). Los
   contenedores son 100% vanilla (0 PNG).
5. **Un sistema por version** y un cambio visual/audio sensible por bump.

## Antes de proponer un cambio

- Leer `CONTEXTO.md` COMPLETO (reglas R-*, errores E-*).
- Auditar el estado real: `git status`, `find src -name "*.java" | wc -l`,
  `cat build.gradle | grep version`.
- Sin JDK en el sandbox (E125): solo APIs verificadas en el propio repo.

## Validacion

- **4 escalas GUI** para cualquier cambio de UI (scale 1-4; reiniciar cliente
  al cambiar escala si hay duda).
- Validar JSON tras editar lang/sounds (`python3 -c "import json; json.load(...)"`).
- Build local: `.\gradlew build` → `build/libs/gripeverde-<VERSION>.jar`.
- Deploy: build siempre acompañado de deploy; limpiar `gripeverde-*.jar`
  viejos antes de validar; confirmar la version en el reloj del Title.

## Verificaciones de build activas (processResources)

- `verifyGripeVerdeLocalization` — 25 lang + 25 changelog in-game presentes.
- `verifyGripeVerdeSoundAssets` — sounds.json ↔ OGG existentes.
- `verifyGripeVerdeNoDeadCode` — falla si reaparece codigo desterrado
  (ThemeManager, steel pack, bg_town, IconButton, dev screens, etc.).

## Codigo

- UTF-8 sin BOM, LF, sin trailing whitespace (ver `.editorconfig`).
- Commits descriptivos: `feat:` / `fix:` / `docs:` / `chore:` + version.
- No dejar clases ni metodos sin uso; si algo ya no sirve, borrarlo y
  documentarlo en CONTEXTO.md y KNOWN_ISSUES.md.

## Cierre de hito (R-CIERRE)

Al bumpear version, sincronizar en la misma sesion: CONTEXTO.md,
CHANGELOG.md, KNOWN_ISSUES.md, README.md y los 25 changelog in-game (solo
si el hito tiene manifestacion observable, ver R-CHANGELOG-DISCRECIONAL).
Luego commit + push (los tags se publican cuando el usuario valida en
cliente real).
