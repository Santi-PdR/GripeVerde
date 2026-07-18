# Eternal Craft: Gripe Verde - Contexto maestro

Lee este documento COMPLETO antes de responder. Contiene toda la informacion
necesaria para retomar el desarrollo sin repetir errores.

---

## Estado actual verificado

- Version estable actual: v0.7.1
- build.gradle: version = '0.7.1'
- GripeVerdeMod.VERSION: "0.7.1"
- mods.toml: usa $mod_version (NO tocar directamente)
- Ultimo tag publicado: v0.7.1
- Working tree esperado al retomar: limpio
- Ultimo commit: docs: actualizar contexto maestro post v0.7.1

## Historial reciente

- v0.6.5: base estable. Fix crashes al perder foco. Punto de retorno post-revert.
- v0.7.0: Sub-hito 1 KI-03. Calle adaptativa del Title. Aprobada visualmente.
- v0.7.1: Sub-hito 2 KI-03. Postes/faroles ocultos temporalmente en todas las escalas.

## Historial de la sesion v0.7.x FALLIDA (contexto critico)

Antes de v0.7.0 exitoso, hubo una sesion completa que fue REVERTIDA.
Se intento resolver KI-03 en 6 iteraciones (v0.7.0 a v0.7.1) sin exito.

Causa raiz del fracaso:
- Intentar resolver 3 sistemas interdependientes al mismo tiempo:
  posicion de acera + altura de postes + extension de edificios FG.
- Cada fix rompia otra escala silenciosamente.
- No se valido incrementalmente.
- No habia capturas de referencia.
- Se usaron formulas cross-file (getSidewalkY en A llamada desde B).

Esa sesion fallida genero los errores E27-E31 documentados mas abajo.
La sesion actual (exitosa) aplico las lecciones y logro cerrar v0.7.0 y v0.7.1
sin romper nada, usando sub-hitos atomicos.

---

## Ruta del proyecto

C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde

## Repositorio

- Local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde (privado)
- Ultimo tag publicado: v0.7.1

## Rutas reales verificadas (IMPORTANTE - no asumir otras)

Namespace: com.eternalcraft.gripeverde
Prefijo: src\main\java\com\eternalcraft\gripeverde\

- Screens: client\screens\ (NO client\gui\screen\)
- Renderers: client\render\ (NO client\rendering\)

Archivos clave del fondo del Title:
- client\render\LivingAtmosphere.java (~290 lineas, v0.7.1)
- client\render\VictorianSkyline.java (~240 lineas, v0.7.1)
- client\render\AtmosphereRenderer.java (~124 lineas, sin cambios desde v0.6.5)
- client\screens\DiaryTitleScreen.java (sin cambios desde v0.6.5)

---

## Pipeline actual del Title (orden de render en DiaryTitleScreen.render)

1. AtmosphereRenderer.renderSky        (cielo, luna, estrellas, nubes)
2. VictorianSkyline.renderBackground   (edificios BG lejanos + chimeneas + humo)
3. LivingAtmosphere.renderFogLow       (niebla ocre horizontal)
4. VictorianSkyline.renderForeground   (edificios FG cercanos + ventanas + puertas)
5. LivingAtmosphere.renderStreetLevel  (calle + ground fog + postes [ocultos] + esporas)
6. LivingAtmosphere.renderAir          (lluvia + cuervos)
7. LivingAtmosphere.renderFlash        (rayo ocasional)
8. LivingAtmosphere.renderVignette     (vineta oscura en bordes)

El pipeline NO se ha modificado. Los cambios de v0.7.0 y v0.7.1 fueron
internos a los metodos, no al orden de llamadas.

---

## Estado de cada sistema del Title

### renderStreet() - LivingAtmosphere (v0.7.0)
- Calle adaptativa por escala con presets por altura efectiva.
- Usa if/else con umbrales h>=800, h>=400, h>=260.
- Parametros por escala: streetTop, rowCount, topCols, bottomCols,
  curbHighlightH, curbShadowH, sheenAlpha, seamAlpha.
- Aprobada visualmente. NO TOCAR en v0.7.2 salvo emergencia.

### getLanternPositions() - VictorianSkyline (v0.7.1)
- Devuelve new int[0][0] en todas las escalas.
- Postes/faroles completamente ocultos.
- El sistema de renderGasLantern sigue intacto en LivingAtmosphere;
  solo no recibe coordenadas.
- Reactivar en v0.7.3 cuando el skyline los soporte visualmente.

### FG_BUILDINGS - VictorianSkyline (sin cambios desde v0.6.5)
- 9 edificios de primer plano definidos como float[][].
- Formato: {x_start, y_bottom, width, height} en proporcionales 0-1.
- Valores y_bottom van de h*0.78 a h*0.84.
- En escala 1 (h=864) dejan un vacio de ~150px entre su base y la calle.
- Este vacio es el objetivo principal de v0.7.2.

### BG_BUILDINGS - VictorianSkyline (sin cambios desde v0.6.5)
- 15 edificios de fondo lejano.
- No se tocaran en v0.7.2.

---

## Bugs conocidos activos (KI-03 - PRIORIDAD)

Estado tras v0.7.1:

- Calle definida: RESUELTO en v0.7.0.
- Postes flotantes: MITIGADO en v0.7.1 (ocultos temporalmente).
- Vacio entre skyline FG y calle en escala 1: ACTIVO (prioridad v0.7.2).
- Skyline demasiado poblado / sin jerarquia: ACTIVO.
- Luna solitaria sin nubes contextuales: ACTIVO.

Otros bugs conocidos:
- KI-01: Freeze al presionar Leave. No reproducido. No tocar.
- KI-02: Crash screenshots F2. Workaround: Win+Shift+S.

---

## Errores tecnicos a NO repetir (E27-E31)

Estos errores fueron catalogados tras la sesion v0.7.x fallida.
Archivo completo: _backups\LECCIONES_v0.7.x.md

### E27 - Replaces multi-cambio simultaneos
No hacer multiples replaces en un solo bloque PowerShell.
Un replace, una verificacion visual. Si falla, se sabe cual fue.

### E28 - Formulas cross-file
No crear funciones en archivo A que se llaman desde archivo B para
calcular posiciones. Si un valor lo usan dos renderers, hardcodearlo
en cada uno o pasarlo como parametro explicito.
Ejemplo del fracaso: getSidewalkY() en LivingAtmosphere llamada
desde VictorianSkyline. Cambiar la formula en A rompia B silenciosamente.

### E29 - Sin capturas de referencia
Antes de tocar el fondo del Title, sacar capturas en las 4 escalas
y guardarlas. Sin capturas previas no se puede validar objetivamente
que el cambio mejora algo.

### E30 - Extrapolar entre escalas
No confiar en "en escala X funciona igual que Y".
Cada escala se comporta diferente porque los proporcionales (h*0.84)
se traducen a pixeles absolutos distintos:
  Escala 1: h=864, h*0.84 = 725px
  Escala 2: h=432, h*0.84 = 363px
  Escala 3: h=288, h*0.84 = 241px
  Escala 4: h=216, h*0.84 = 181px
Validar SIEMPRE en las 4.

### E31 - Sistemas interdependientes en mismo bump
REGLA ABSOLUTA. Un sistema modificado por bump. Un bump = una version.
Si el sistema A depende del sistema B, tocar solo uno, validar,
commitear, y despues tocar el otro.

---

## Reglas operativas vigentes

### Desarrollo
- UN sistema por version. UN cambio visual sensible por bump.
- Validacion visual obligatoria en 4 escalas cuando se toque el fondo.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real (leer codigo, git status) antes de proponer cambios.
- Java UTF-8 sin BOM (usar UTF8Encoding(false) en PowerShell).
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre acompanado de deploy. Nunca uno sin el otro.
- R-JAR: Limpiar jars gripeverde-*.jar viejos del deploy path antes de validar.
- R-VER: Validar la version visible en el reloj del Title antes de aprobar o rechazar.

### PowerShell
- R-PS: Minimizar cantidad de bloques. Agrupar lectura + accion + verificacion.
- R-PATH: Si no estamos en la raiz del repo, usar rutas absolutas.
- Bloques cortos con if/else en misma linea cuando sea posible.

### Validacion visual
- R-CACHE: LivingAtmosphere cachea lanternPositions en variable estatica.
  Al cambiar GuiScale durante pruebas, reiniciar cliente para no validar datos viejos.
- R-CAP: Capturas pre/post en 4 escalas antes de validar cambios visuales importantes.

---

## Resolucion del usuario

- Monitor: 1536 x 864 nativo
- Alturas efectivas por GuiScale:
  - Escala 1: h=864
  - Escala 2: h=432
  - Escala 3: h=288
  - Escala 4: h=216

---

## Deploy

Archivo .deploy en raiz del repo:
  DEPLOY_MODS_PATH=D:\Juegos\Minecraft\Instances\Test\mods

Parseo fragil (E23): filtrar por DEPLOY_MODS_PATH= y no asumir primera linea.

---

## Documentacion del proyecto

- CONTEXTO.md: este archivo. Punto unico de entrada para sesiones nuevas.
- CHANGELOG.md: historial narrativo orientado a jugadores.
- KNOWN_ISSUES.md: estado tecnico completo, bugs, reglas, plan futuro.
- _backups\LECCIONES_v0.7.x.md: errores catalogados de la sesion fallida + reglas R-*.

---

## Proximo sub-hito: v0.7.2 - Cerrar vacio skyline-calle

Objetivo:
- Cerrar el vacio visual entre edificios FG y calle, especialmente en escala 1.
- En h=864 hay ~150px de vacio entre la base de los edificios FG y la calle.

Alcance recomendado:
- Tocar SOLO VictorianSkyline.java.
- Ajustar FG_BUILDINGS (extender y2 hacia abajo) o logica de renderForeground.
- NO tocar LivingAtmosphere.renderStreet().
- NO reactivar postes en el mismo bump.
- Validar en 4 escalas.

Criterio de exito:
- La calle sigue viendose bien (no invadida por edificios).
- El skyline baja lo suficiente para que no haya vacio grande visible.
- No aparecen postes/faroles todavia.
- Validado visualmente en escalas 1, 2, 3, 4.

Sub-hito siguiente (v0.7.3):
- Reactivar postes/faroles por escala cuando el skyline los soporte.

---

## Deuda tecnica pendiente

- ICON_BTN_MP e ICON_BTN_MODS sin PNG real.
- Framework UI subutilizado (solo Title lo consume).
- ThemeManager sin themes registrados.
- Changelogs localizados pueden requerir sincronizacion.

---

## Al retomar

Leer este documento COMPLETO antes de proponer cualquier cambio.

Si el usuario dice "arranquemos":
1. Ejecutar comandos de reentrada para verificar estado limpio.
2. Leer _backups\LECCIONES_v0.7.x.md si no se ha leido.
3. Proceder con v0.7.2 siguiendo el alcance recomendado arriba.
4. Preguntar solo si hay duda real.

Comandos de reentrada:
  Set-Location C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
  git status --short
  git log --oneline -5