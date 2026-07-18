# Eternal Craft: Gripe Verde - Contexto maestro

## Estado actual

- Version estable actual: v0.7.1
- Ultimo tag publicado: v0.7.1
- Working tree esperado al retomar: limpio
- Base reciente:
  - v0.6.5: base estable post-revert
  - v0.7.0: calle adaptativa del Title
  - v0.7.1: postes/faroles ocultos temporalmente

## Ruta del proyecto

C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde

## Rutas reales verificadas

src\main\java\com\eternalcraft\gripeverde\client\screens\
src\main\java\com\eternalcraft\gripeverde\client\render\

Archivos clave:
- src\main\java\com\eternalcraft\gripeverde\client\render\LivingAtmosphere.java
- src\main\java\com\eternalcraft\gripeverde\client\render\VictorianSkyline.java
- src\main\java\com\eternalcraft\gripeverde\client\render\AtmosphereRenderer.java
- src\main\java\com\eternalcraft\gripeverde\client\screens\DiaryTitleScreen.java

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

## Estado de v0.7.0

Sub-hito 1 KI-03.

Cambio:
- Se agrego renderStreet() en LivingAtmosphere.
- Calle adaptativa por altura efectiva.
- No se tocaron postes ni edificios.

Resultado:
- Calle aprobada visualmente.
- No tocar renderStreet() en v0.7.2 salvo emergencia.

## Estado de v0.7.1

Sub-hito 2 KI-03.

Cambio final:
- VictorianSkyline.getLanternPositions(int w, int h) devuelve new int[0][0].
- Postes/faroles ocultos en todas las escalas.

Motivo:
- Reposicionar postes sin cerrar el vacio skyline-calle generaba lectura visual mala.
- Se difiere la reactivacion hasta que el skyline soporte los postes.

## Bugs conocidos activos KI-03

- Calle: resuelta.
- Postes/faroles: ocultos temporalmente.
- Vacio entre skyline y calle: activo, prioridad v0.7.2.
- Skyline demasiado poblado/sin jerarquia: activo.
- Luna sin nubes contextuales: activo.

## Resolucion del usuario

- Nativa: 1536 x 864
- Alturas efectivas por GuiScale:
  - Escala 1: h=864
  - Escala 2: h=432
  - Escala 3: h=288
  - Escala 4: h=216

## Reglas vigentes

- Un sistema por version.
- Un cambio visual sensible por bump.
- Validacion visual obligatoria en 4 escalas cuando se toque el fondo.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Build siempre acompanado de deploy.
- Minimizar bloques PowerShell.
- Usar UTF-8 sin BOM para Java.
- Usar rutas absolutas si no se esta en la raiz del repo.
- Limpiar jars viejos en deploy path antes de validar.
- Confirmar version visible en el reloj antes de aprobar/rechazar.
- No confiar en cache estatica de renderers al cambiar GuiScale; reiniciar cliente si hay duda.

## Lecciones v0.7.x fallida

Archivo completo: _backups\LECCIONES_v0.7.x.md

Errores clave:
- E27: no replaces multi-cambio.
- E28: evitar formulas cross-file.
- E29: capturas pre/post en 4 escalas.
- E30: no extrapolar entre escalas.
- E31: no sistemas interdependientes en mismo bump.

## Deploy

.deploy contiene: DEPLOY_MODS_PATH=D:\Juegos\Minecraft\Instances\Test\mods

Regla:
- Build + deploy juntos.
- Limpiar jars gripeverde-*.jar viejos antes de validar visualmente.

## Proximo sub-hito

### v0.7.2 - Cerrar vacio skyline-calle

Objetivo:
- Cerrar el vacio visual entre edificios FG y calle, especialmente escala 1.

Alcance recomendado:
- Tocar solo VictorianSkyline.
- Ajustar FG_BUILDINGS o logica local de foreground.
- No tocar LivingAtmosphere.renderStreet().
- No reactivar postes en el mismo bump, salvo aprobacion explicita.

Criterio de exito:
- La calle sigue viendose bien.
- El skyline baja/sube lo suficiente para que no haya vacio grande.
- No aparecen postes/faroles todavia.
- Validado en escalas 1, 2, 3, 4.

## Comandos de reentrada

Set-Location C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
git status --short
git log --oneline -3