# Contexto de continuidad: GripeVerde ModMenu

Actualizado: 2026-11-13

## Proyecto y reglas de trabajo

- Repositorio: [Santi-PdR/GripeVerde](https://github.com/Santi-PdR/GripeVerde) — **público**
  (`visibility: public`, confirmado con la API). El script corto de instalación funciona sin
  token, igual que el de Photon/Fisk.
- Rama de trabajo de la sesión: `arena/48be547c-gripeverde` (base `02e85bc`) — la plataforma
  fija el nombre, no se renombra. Todo el trabajo va ahí; el PR sale de esa rama.
- Versión de esta rama: `0.243.0` (anteriores entregadas: `0.229.0`, `0.228.0`, `0.209.0`, `0.208.0`, `0.207.0`, `0.206.0`, `0.205.0`, `0.204.0`, `0.203.0`, `0.202.0`, `0.201.0`, `0.200.0`, `0.199.0`, `0.198.0`, `0.197.0`, `0.196.0`, `0.195.0`, `0.194.0`, `0.193.0`, `0.192.0`, `0.191.0`, `0.190.0`, `0.189.0`, `0.188.0`, `0.187.0`, `0.186.0`, `0.185.0`, `0.184.2`, `0.184.1`, `0.183.0`)
- El usuario quiere seguir mejorando el mod de forma continua hasta que diga que pare.
- El trabajo se hace sobre el código fuente alojado en GitHub; no se clona ni se edita el
  proyecto local del usuario.
- No se controla Minecraft ni la PC del usuario para pruebas visuales. El CI acredita
  compilación, recursos y localización; no sustituye una revisión dentro del juego.
- Las interfaces del juego se mantienen instantáneas: no se agregan animaciones al
  inventario, la pausa ni el mundo. Las únicas piezas animadas siguen siendo las del menú
  y respetan Movimiento reducido y Destellos reducidos.
- **Entrega de cada versión:** el usuario no descarga archivos del workspace. Al terminar cada
  tanda hay que darle **un script de PowerShell para pegar**, que baja el `.jar` del último
  release directo a su carpeta de mods, respalda el anterior y verifica tamaño y SHA-256 contra
  lo que publica la API. No se guarda ninguna copia del JAR en el repositorio ni en el workspace.
  Tampoco se dispara la descarga desde el sandbox: la red de este entorno solo alcanza
  `github.com`, `api.github.com` y pocos hosts más, y el binario del release viene de
  `release-assets.githubusercontent.com` (da `EOF` acá).
- El script vive en `tools/install-latest.ps1` y usa, en este orden: `$env:GITHUB_TOKEN`,
  `gh auth token`, `git credential fill` (host github.com). Si no hay ninguna, corta con un
  mensaje que dice qué hacer. Verifica `size` y `digest` (sha256) que publica la API del
  asset, comprueba que el ZIP tenga `META-INF/MANIFEST.MF` y `mods.toml`, respalda los
  `*gripeverde*.jar` viejos en `<instancia>\gripeverde-jar-backups` y revierte si algo falla.
- El usuario **autorizó** pasar el repo a público (2026-11-13), pero **la integración de esta
  sesión no puede**: `gh repo edit` de esta versión no tiene el flag de consecuencias y
  `PATCH /repos` con `private=false` devuelve `403 Resource not accessible by integration`
  (falta permiso de administración). Queda en manos del usuario:
  `Settings > General > Danger Zone > Change repository visibility > Make public`.
  Repos hermanos de referencia, todos **públicos**: `Fisk-Superheroes`, `Photon-2`, `Lclient`.
  Los comandos cortos que el usuario probó con éxito salen de ahí, sin token.
- Mientras siga privado, `tools/install-latest.ps1` usa token si lo encuentra y, cuando el repo
  pase a público, funciona igual sin token (el fallback sin credenciales ya está escrito y, si
  recibe un 404 sin token, avisa exactamente eso).
- **Cambio de rama de la sesión (2026-11-13):** el checkout pasó a
  `arena/48be547c-gripeverde`, que arranca en `02e85bc` (0.103.0). Todo el trabajo de
  0.104→0.181 vive en `codex/v0.104-ghoul-warning-poster` @ `027f5f7`, que es la cabeza del
  PR #90. La rama de la sesión estaba en 0.173.0; se integró con `-s ours` para conservar su
  historial y dejar el árbol moderno.

## Estado confirmado al retomar (2026-11-13)

- `0.209.0` **publicada y en verde**: commit `83733da`, run `43681287004` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.209.0/gripeverde-0.209.0.jar`
  (9.623.762 bytes, `sha256:6a078e063d7424a93e52279097372bdf163b2e3282669a0e3d2fb3e7f22d853a`).
- `0.208.0` **publicada y en verde**: commit `171c786`, run `43680897376` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.208.0/gripeverde-0.208.0.jar`
  (9.618.270 bytes, `sha256:b7be58bd82c93b595b48c421868c2a7546586452576aa9aa0178487a85bcafa7`).
- `0.207.0` **publicada y en verde**: commit `3339b83`, run `43680333979` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.207.0/gripeverde-0.207.0.jar`
  (9.611.076 bytes, `sha256:fed7989cc2f0bf19f966067a904899fff2d5294d45b182d0cc2611fb2d992efa`).
- `0.206.0` **publicada y en verde**: commit `b8e9568`, run `43679757158` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.206.0/gripeverde-0.206.0.jar`
  (9.604.355 bytes, `sha256:899a9cb01931c63b1997b544e5412773ad156343ff7da65929e9e8efda49be2a`).
- `0.205.0` **publicada y en verde**: commit `1843b50`, run `43678725728` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.205.0/gripeverde-0.205.0.jar`
  (9.597.318 bytes, `sha256:56fcb8653c61033b4db097f460ebc91fe31a34b216147efb69959604767e1990`).
- `0.204.0` **publicada y en verde**: commit `0fa48d4`, run `43678068929` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.204.0/gripeverde-0.204.0.jar`
  (9.590.921 bytes, `sha256:26ed240c5a410c68d0d8d3be98e152b5539eeab524b33570328abe48f0090b0b`).
- `0.203.0` **publicada y en verde**: commit `47233a2`, run `43677388823` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.203.0/gripeverde-0.203.0.jar`
  (9.584.659 bytes, `sha256:7fd891fe40de4d9e42fd8cba0ce9684c367aeb91f60d73cab4ed75c278b7935c`).
- `0.202.0` **publicada y en verde**: commit `bb52060`, run `43676713476` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.202.0/gripeverde-0.202.0.jar`
  (9.577.020 bytes, `sha256:43f2d2c29fe7b0a4268f4f9976138101b3af9d9dbab3c40d6d212db62b228069`).
- `0.201.0` **publicada y en verde**: commit `3d4157d`, run `43676268367` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.201.0/gripeverde-0.201.0.jar`
  (9.570.861 bytes, `sha256:6d4ae97c63f33b1efcf51f8347d2d018ee2d5eedaf37138bf6af47668a1f2544`).
- `0.200.0` **publicada y en verde**: commit `f93a043`, run `43675423197` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.200.0/gripeverde-0.200.0.jar`
  (9.566.187 bytes, `sha256:2f90547d814b5476b83f8df9db900a8f8a794345577753304d027f4003ba40e2`).
- `0.199.0` **publicada y en verde**: commit `0484f09`, run `43674954683` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.199.0/gripeverde-0.199.0.jar`
  (9.560.605 bytes, `sha256:bc70d5fe53c6c30dbe869c5950c88b10c85bce51c2bbaf7ff7bb0e78d198773f`).
- `0.198.0` **publicada y en verde**: commit `d02b0a6`, run `43674325866` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.198.0/gripeverde-0.198.0.jar`
  (9.553.686 bytes, `sha256:f6c6ada8e9d70b34d24f46f8bce19aa3fc1566da188f85dbf4fd112c1e25bfe2`).
- `0.197.0` **publicada y en verde**: commit `581aa08`, run `43673937244` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.197.0/gripeverde-0.197.0.jar`
  (9.547.303 bytes, `sha256:312ac52d54c55172432241720d62392c8fda32ceaae6ac0a11cd06d5848fc586`).
- `0.196.0` **publicada y en verde**: commit `1cb5ae5`, run `43673361232` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.196.0/gripeverde-0.196.0.jar`
  (9.539.559 bytes, `sha256:b11fd71b430a7aa4d803c11d46027858a5b00be42b12a04b6e5102abf5b3288a`).
- `0.195.0` **publicada y en verde**: commit `e047359`, run `43672735772` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.195.0/gripeverde-0.195.0.jar`
  (9.534.213 bytes, `sha256:f5ba3395f4dec0029e9533a9363c33868a166474013e7ba319d7e331ad49c2a0`).
- `0.194.0` **publicada y en verde**: commit `1254406`, run `43671291169` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.194.0/gripeverde-0.194.0.jar`
  (9.525.523 bytes, `sha256:63d821846c31939af4da52f94711aebe9e32bc6925af2a92c0a7abb01efb2046`).
- `0.193.0` **publicada y en verde**: commit `1ee2cb9`, run `43670254939` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.193.0/gripeverde-0.193.0.jar`
  (9.518.959 bytes, `sha256:2afad2ca517502e77a685b0ea463c316852d70316b801d9d11e691663bcb7530`).
- `0.192.0` **publicada y en verde**: commit `b659159`, run `43669485111` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.192.0/gripeverde-0.192.0.jar`
  (9.507.369 bytes, `sha256:3cb607b2d9d3e648800f18cee2c4156a1fbdff6e4eead097315015fd42b1ed45`).
- `0.191.0` **publicada y en verde**: commit `53c70d4`, run `43667386955` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.191.0/gripeverde-0.191.0.jar`
  (9.500.074 bytes, `sha256:e0fa2517c614525844934e6b48dd96a4268af4b86288cb68a5efb3d6f1fdfc77`).
- `0.190.0` **publicada y en verde**: commit `d17e159`, run `43665994353` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.190.0/gripeverde-0.190.0.jar`
  (9.489.144 bytes, `sha256:47ccc41a4b6682429a5c65c3c00180a30c9dde713bb272168e7160d5638c2167`).
- `0.189.0` **publicada y en verde**: commit `aaffb19`, run `43661318515` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.189.0/gripeverde-0.189.0.jar`
  (9.479.721 bytes, `sha256:d9a0ac644a1fc29bb0e8065c13b4a3dec4ad876bfb0b0c8b95e1d3aee72cf76b`).
- `0.188.0` **publicada y en verde**: commit `f3f9ebe` (código) + `4f6a742` (marca de release),
  runs `43563691927` y `43563871253` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.188.0/gripeverde-0.188.0.jar`
  (9.468.634 bytes, `sha256:f6156383f502a2cefe12a83f7fd53661492bcb430e8860be734e601052a1f429`).
- `0.187.0` **publicada y en verde**: commit `66892bf`, run `43562261753` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.187.0/gripeverde-0.187.0.jar`
  (9.473.682 bytes, `sha256:de2af5753eec1eaecd33f5b4fd9125b6b28f1b531f50aa755b5387eaabb7ef21`).
- `0.186.0` **publicada y en verde**: commit `9e04eea`, run `43562046827`; 9.467.409 bytes,
  `sha256:e45043063a440962b25937d0555a45323971cba0b03f170b6f935837d9b2ec3c`.
- `0.185.0` **publicada y en verde**: commit `1d78204`; 9.457.763 bytes.
- `0.184.2` **publicada y en verde**: commit `2fb99d2`, run `43560967913` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.2/gripeverde-0.184.2.jar`
  (9.454.696 bytes, `sha256:b0dd549004183497b8d21a932a4ea69b33603483ff056d07a3f3b4e6f5fcf148`).
- `0.184.1` **publicada y en verde**: commit `0f333ce`, run `43560722736` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.1/gripeverde-0.184.1.jar`
  (9.451.941 bytes, `sha256:61ef4579f9d005eaee844f33f1d03e8864d98ae619e97a39d59616c34e9a1d08`).
- `0.184.0` **publicada y en verde**: commit `b2057af`, run `43560446633` (`build` y `publish`
  en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.184.0/gripeverde-0.184.0.jar`
  (9.447.179 bytes, `sha256:7540d78f63b947b50cea73dfc630873a46d26074f715b38e2c970619f474b22d`).
- `0.183.0` **publicada y en verde**: commit `26a2767`, run `43559194671` (`build` y
  `publish` en verde). `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.183.0/gripeverde-0.183.0.jar`
  (9.453.806 bytes, `sha256:0eeed14773484cb9d79b2071c2006e40beebd5aa57e154489a8bc1761792f9ef`).
  El intento previo `8ced11e` cayó por un import faltante de `DiaryChrome` en
  `ChangelogScreen`.
- **Rutina del usuario:** cuando dice «Sigue» hay que trabajar el mod al menos 30 minutos
  (mejoras, errores, interfaces, fondo, efectos, acciones, fuentes) y cerrar con un resumen
  en lista de lo añadido.
- `0.182.0` **publicada y en verde** desde la rama de la sesión: commit `a5d2bfd`,
  run `43558073201` (`build` y `publish` en verde).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.182.0/gripeverde-0.182.0.jar`
  (9.447.525 bytes, `sha256:9ea5102c09f527c1acd404f3cf9c467e122d6bc188d7766e4be4e540b9db7e14`).
- El repo **ya es público** (el usuario lo cambió en Settings): el comando corto de
  instalación vuelve a funcionar sin token, como el de Photon/Fisk.
- `.github/workflows/build.yml` también compila `arena/**`, así que la rama de la sesión tiene
  sus propios runs y releases.
- `0.181.0` **publicada y en verde**: commit de código `421be29`, run `43550635643`
  (un intento previo `8d995dc` cayó por reimportar `net.minecraft.util.Util`; el
  comentario automático del PR lo señaló en tres líneas).
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.181.0/gripeverde-0.181.0.jar`
  (9.440.233 bytes).
- `0.180.0` **publicada y en verde**: commit de código `2fc259c`, run `43549058805`.
  El JAR ya no viaja como ZIP de artefacto: el job `publish` lo adjunta a un
  release, así que la descarga es directa a
  `https://github.com/Santi-PdR/GripeVerde/releases/download/v0.180.0/gripeverde-0.180.0.jar`
  (9.416.179 bytes).
- `0.179.0`: commit `8ee47fc`, run `43545600000`, artefacto `14094161283`,
  8.753.677 bytes (ZIP).
- Referencias anteriores: `0.178.0` run `43543676142` (`7cd9d9f`; el primer intento
  `29e88f3` no compiló por leer el campo `this.children`), `0.177.0` run `43539899164`
  (`d9e7a52`), `0.176.0` run `43538221683` (`531a91d`), `0.175.0` run `43453123285`
  (`754b224`), `0.174.0` run `43449824926` (`eedfaa9`), `0.173.0` run `43447945730`
  (`9d81d3d`), `0.172.0` run `40763347401` con SHA-256 del JAR
  `9520432c3027a3695b4e1246c2f8a3298cdc8ce11ab0cbff517d6fc914508c60`.
- `0.172.0` **no** se instaló en la instancia del usuario; la carpeta de mods de destino
  sigue siendo `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`.
- **Instalación bloqueada en esta sesión por dos motivos ajenos al mod:** el entorno es
  Linux (la ruta `D:\...` no existe como montaje) y el host de descarga de artefactos
  (`productionresultssa*.blob.core.windows.net`) no es alcanzable desde el sandbox, así que
  tampoco se pudo verificar aquí el SHA-256 del JAR. Queda listo
  `tools/install-artifact.ps1`, que valida `mods.toml`, admite `-ExpectedSha256` (el hash del
  JAR ya extraído), reemplaza solo `gripeverde-*.jar` y avisa o aparta cualquier archivo que
  no sea `.jar`. Con el ZIP sin abrir, el hash a contrastar es el del ZIP:
  `5d6ddc32d264efc6b9159f419d305ec6472034ba282d16fefe2a2493820e076e`.
- La sesión actual corre en un entorno Linux remoto: `D:\...` no existe como ruta
  montada, así que la instalación queda pendiente de que la carpeta sea accesible.
- El validador local `python3 tools/verify_mirrors.py` reproduce los sellos de Gradle
  (idiomas, sonidos, código muerto, versión, duraciones, subtítulos, uso de sonidos,
  literales y balance) y pasa en verde con `0.175.0`.

## Cambios de 0.243.0 — página de registro

- Atajo: sólo `Ctrl+S`. `DiarySelectWorldScreen.cacheListMetrics()` guarda alturas y bordes al abrir.
- Cada mundo sobre banda oscura para que el texto vanilla siga legible.

## Cambios de 0.242.0 — puerta de servicio

- `DiarySelectWorldScreen` sustituye a `SelectWorldScreen` en la portada; botones vestidos con `PaperButtons.dressAll`.
- Cerradura de servicio en `x=8, y=height-56, 132x20`, visible con Ctrl/Shift apretado.

## Cambios de 0.241.0 — portada, multijugador y pausa

- Tercer icono de la portada: Registro (gabinete). Icono nuevo `FILING`.
- Hoja de pausa α238; multijugador con recortes por fila; descripción del mod reescrita.

## Cambios de 0.240.0 — telegrama y pie del parte

- `DeathPaper`: papel de 340×78 a `y=8`; `SkinButtons` por `ScreenEvent.Init.Post` con alta del widget por reflexión
  (`renderables`/`narratables` de `Screen`), revirtiendo si no se pudo registrar.
- Pie del parte: `footerDivY = panelH-66`, ayuda `+4`, recuento `+15`, botones en `panelH-14-alto`.

## Cambios de 0.239.0 — avisos como recortes

- Nuevo `PaperTexture.drawTornSlip(...)` con alfa por parámetro (para avisos que se desvanecen).
- `Discoveries.drawToast`: recorte rasgado en lugar de tarjeta; rótulos a `y+4`/`y+13` para no tocar el desgarro.

## Cambios de 0.238.0 — campos de texto

- `DiaryTextField` (usado en servidores, unirse directo e idiomas): `drawWornBorder` con el color de foco.
- Buscador del diario: `drawWornBorder` bronce α90/200, después del relleno; semilla `(sx-1)*31+(sy-1)*17+SEARCH_W`.

## Cambios de 0.237.0 — parte de defunción

- `DeathPaper.render(g, screen)`: sólo actúa sobre `DeathScreen`; franja superior en `y=-8` (42 px) y pie en `h-30` (38 px,
  omitido si `h < 300` para no tapar botones en ventanas bajas). Tinta `REDCOAT` para el sello.
- Enganche en `ScreenEventHandler.onScreenRenderPost` antes del `return` de partida. Sin animación: es un overlay estático.

## Cambios de 0.236.0 — mandos sin marco

- `PaperTexture.drawWornBorder` aplicado a `DiarySlider`, `DiaryMusicSlider`, `DiaryAmbienceSlider` y `DiaryProgressBar`
  (semilla `x*31+y*17+w`, color de borde propio de cada mando).
- Sin tocar valores, ticks ni el pulso de ALERT (que ya respeta Movimiento/Destellos reducidos).

## Cambios de 0.235.0 — botones sin caja

- `PaperTexture.drawWornBorder`: tramos `max(5,w/20)` en horizontal y `max(4,h/8)` en vertical, cada uno desplazado 0-1 px;
  semilla `x*31+y*17+w` (estable al pulsar, que desplaza el dibujo un píxel).
- Aplicado en `DiaryButton` (fuera `drawBorder`), `SlimButton` (pestañas del pie) y `DiaryToggle` (marco exterior).

## Cambios de 0.234.0 — meses, pelusa y contenedores

- Indice: `buildIndexRows` (cabecera por mes + fechas con sangría 18 px), `indexRowIsMonth`/`indexRowSection`, `labelAt`;
  cabeceras no pulsables; `toggleIndex` centra por fila de índice.
- `PaperTexture.drawTornEdge`: pelusa y fibra en el canto (todas las hojas rasgadas).
- `PaperTexture.drawWornRim` + `ScreenEventHandler.onContainerRenderPost` (`AbstractContainerScreen.getGuiLeft/getGuiTop/getXSize/getYSize`):
  canto gastado estático en inventario y contenedores. Los tooltips NO se pueden tocar: Forge 1.20.x no tiene evento post-tooltip.

## Cambios de 0.233.0 — la pausa termina rasgada

- `PaperTexture.drawTornRim(g,x,y,w,h,paper,shadow)`: dientes de 1–3 px hacia afuera + sombra de 1–2 px adentro, diente suelto cada
  tres tramos en los laterales; semilla derivada de la geometría (sin RNG por cuadro).
- `DiaryPauseScreen`: `drawPanel` (con brillo, sangre y `drawWear`) y `renderWorldSafe` (partida) usan `drawTornRim` con
  `PAPER_LIGHT`; fuera `drawDoubleBorder`/`drawRect`/`drawCornerOrnaments`.

## Cambios de 0.232.0 — no quedan rectangulos

- `ChangelogScreen`: modo registro técnico → `drawTornSheet` ARCHIVE; fuera los cuatro filetes `BRONZE` en ambos modos.
- `DiaryLanguageScreen`: fuera el marco de 1 px; la lista se lee sobre `drawTornSheet` CORRESPONDENCE (sombra `0x1E302315`).
- `DiaryPackSelectionScreen`: la hoja grande (blotter) → `drawTornSheet` ARCHIVE.
- `DiaryChrome`: eliminados `drawPanelScaffold` y `renderPanelBase`, sin uso desde hace versiones.

## Cambios de 0.231.0 — recortes al pie e indice con tope

- `DiaryArchiveScreen.drawPaperDoc`: los tres papeles recuperados (telegrama, informe, ficha negra) pasan a
  `PaperTexture.drawTornSheet` `CORRESPONDENCE` de 124×20 con sombra corta; hover = cuerpo aclarado + subrayado a lápiz;
  clic/hover de 20 px.
- `ChangelogScreen` modo diario: `INDEX_MAX_ROWS = 12`, `indexHeight`/`indexVisibleRows`/`indexMaxScroll`,
  `indexScroll` propio con rueda dentro de la hoja y marca de desplazamiento al costado, `toggleIndex` (abre centrado en
  la entrada actual) y `overIndex` (la hoja se queda clics y rueda).

## Cambios de 0.230.0 — documentos recortados

- `DiaryArchiveScreen.renderDocument`: recorte `LEDGER` rasgado bajo el documento, sombra corta al pie.

## Cambios de 0.229.0 — lectura y firma

- Índice: raya de lacre en la entrada actual (`currentDiarySection`), banda de realce bajo el puntero.
- Portada: la firma se apoya en una tira rasgada de 16 px con sombra; sobre papel usa `INK_TITLE`.

## Cambios de 0.228.0 — vinetas y luz

- Diario: viñeta → dos trazos (`0x006B5533` α170/90) manteniendo `prefixW` y altos; degradados α24 arriba/abajo.

## Cambios de 0.227.0 — indice del diario

- Índice: botones «Versiones» + «Índice» al pie; `drawIndexOverlay` (hoja LEDGER rasgada, 240 px, 13 px/línea),
  `indexRowAt` y `jumpToDiarySection` (`setScrollTarget(off-6)`).
- `Marginalia`: borde rasgado arriba/abajo (semilla `cardX*31+cardWidth*17`).

## Cambios de 0.226.0 — fragmentos en el diario

- `ChangelogScreen.drawDiaryScraps`: una hoja rasgada `LEDGER` por sección (corte por `H1`), sombra corta,
  recorte `1 + (seed % 5)` derivado de `start*31+i`.

## Cambios de 0.225.0 — repaso a lapiz en los filetes

- `DiaryChrome.drawInkDivider`: tramos de lápiz `y+3`, α55, tope 5, semilla `x*31+w*17`.

## Cambios de 0.224.0 — papel rasgado en todo el gabinete

- `drawTornSheet` en 15 hojas más (accesibilidad, chat, confirmación, controles, unión directa, edición de servidor,
  guía, idioma, multijugador, opciones en línea, personalización, sonido, despacho, nota de portada).
- Quedan con borde recto sólo: el registro técnico (`ChangelogScreen` modo registro), el marco interior del idioma,
  la hoja de packs (casi pantalla completa) y las hojas pintadas a mano de la pausa.

## Cambios de 0.223.0 — fechas sin capitular

- `ChangelogScreen` modo diario: `H1` se pinta entero en `INK_TITLE` con `drawPencilRule` + `drawInkBlot`.
- El diario vive en `assets/gripeverde/diary/bitacora_{es,en}.md` (fechas, tachaduras `~~ `, notas `- `).

## Cambios de 0.222.0 — papel rasgado en todas las hojas

- `PaperTexture.drawWear` (público) y `drawTornSheet` en `DiaryArchiveScreen`, `DiaryCreditsScreen`,
  `DiaryOptionsScreen`; `DiaryPauseScreen` añade `drawWear`.

## Cambios de 0.221.0 — portada limpia y diario

- `DiaryTitleScreen`: sin cristal (`drawGlassPatina`), sin huevo del emblema y sin botón de guía; botón «Diario»
  (icono libro) → `new ChangelogScreen(this, true)`.
- `ChangelogScreen(Screen, boolean diary)`: `diary/bitacora_es.md` / `bitacora_en.md`, `LT.STRIKE` (`~~ `),
  hoja rasgada, lápiz + mancha en las fechas, botón «Versiones».
- `PaperTexture.drawAge/drawTornSheet/drawTornSeam`; `DiaryChrome.drawInkBlot/drawPencilRule/drawStrike`.

## Cambios de 0.220.0 — avance del archivo

- `DiaryArchiveScreen`: barra de avance a `panelY+54` (track α55, relleno ocre, marca verde al completar);
  la pista de teclado se dibuja sólo si `panelY+panelH-46 >= docsY+20`.

## Cambios de 0.219.0 — cronica con el mismo marco

- `ChangelogScreen`: sombra + esquinas + chapa `REG. XV` (solo si `pw>=400` y el título deja hueco).

## Cambios de 0.218.0 — archivo por teclado

- `DiaryArchiveScreen`: `focusCard`, `drawFocusCorners` (esquinas 1px lacre `0xFF5C0A08`), flechas ±1/±5 y
  Enter (`257`/`335`) → `openCard`; `cardAt` para el clic; pista «flechas / enter» a `panelY+panelH-46`.

## Cambios de 0.217.0 — dos luces

- Halos: radio 8 α30/26 sobre las lámparas de las torres y radio 4 α20 tras los ojos del ghoul
  (`drawSoftBlob`, franjas ya memorizadas por `BLOB_BANDS`).

## Cambios de 0.216.0 — ciudad lejana

- `VictorianSkyline`: `ensureFarLayout(w,h)` (`farRects`, `farWindows`) y la ventana del ghoul memorizada
  (`ghoulWx/Wy/Ew/Eh`); `renderFarSkyline` y `renderGhoulWatcher` sólo pintan.

## Cambios de 0.215.0 — campos de texto

- `DiaryTextField`: recorte visible memorizado por texto/desplazamiento/ancho; `formatSafe` con dos casillas
  (`fmtSeqA/B`) en vez de armar el tramo con formato en cada cuadro.

## Cambios de 0.214.0 — hora y niebla

- `LivingAtmosphere`: `realHour()` cachea la hora real un minuto (velo nocturno); `FOG_LOW_BANDS` constante
  y una sola pasada por franja en `renderFogLow`.

## Cambios de 0.213.0 — medir la letra una vez

- `DiaryTypography.width(...)` → `widthMemo`, atado a la fuente (`widthMemoFont`), clave = texto;
  `ChangelogScreen` mide titulo/sello/viñeta con `DiaryChrome.widthCached`.

## Cambios de 0.212.0 — haces de las torres

- `AtmosphereRenderer`: `beamSegments(bucket,length,maxHalf,alpha,color)` + `BEAM_MEMO` (ángulo 1/1024 rad,
  tope 256, tramos relativos al origen); `drawBeam` sólo pinta.

## Cambios de 0.211.0 — sello y nota al margen

- `Discoveries.drawToast`: campos `memToastFont/Width/Name/English/BoxWidth` y `memVisibleHeader/Name`.
- `Marginalia`: `cachedLayoutLinesWidth` se calcula en `layoutLines` y lo usa `drawHorizontal`.

## Cambios de 0.210.0 — traducciones y anchos de rótulo

- `DiaryChrome.tr` es ya la única vía de traducción: no queda ningún `Component.translatable(...).getString()`
  fuera de `tr` (`grep -c` = 1, la propia implementación).
- `DiaryTypography`: `widthMemo` + `WIDTH_MEMO` (tope 512) usados por `drawFitted`/`drawFittedLeft`.

## Cambios de 0.209.0 — franjas del pincel blando

- `AtmosphereRenderer`: `blobBands(r, rowStep)` + `BLOB_BANDS` (HashMap, tope 128, clave `(r<<3)|rowStep`) memoriza
  `{low, dx}` por franja; `drawSoftBlob` sólo recalcula alfa por anillo.
- `DiaryChrome.trFit` → `fitCached`.

## Cambios de 0.208.0 — forma de la luna memorizada

- `AtmosphereRenderer`: `moonSpans(radius, shape)` + `moonSpans`/`moonShape*` (tramos `{fila, x1, x2}` relativos
  al centro); `drawMoonSpans` los pinta. `renderMoonPhase` sólo elige forma; se borraron `renderMoonPhaseDifference`
  y `fillCircle` (sin uso).

## Cambios de 0.207.0 — calle memorizada

- `LivingAtmosphere`: `ensureStreetLayout(w,h)` + campos `stTop/stBottom/stHeight/stCurb*/stUpperBand*/stSheen*/
  stSeamAlpha/stReflTop/stGutterW/stBottomBandTop` y arrays `stRowY1/Y2/Offset/Step`; `renderStreet` sólo dibuja.

## Cambios de 0.206.0 — fachadas y banderas

- `VictorianSkyline`: `ensureFacadeLayout(w,h)` + arrays `bgX1/X2/Y1/Y2`, `fgX1/X2/Y1/Y2`, `fgScaledW/H`;
  `renderBackground` y `renderForeground` iteran por índice (la puerta sale de `fgScaledW/H`).
- `FlagRenderer`: `FLAG_RUNS` (`IdentityHashMap<int[][], int[]>`) + `runsOf(grid)`; `FRAME_OUTER/INNER_LIGHT/INNER_DARK/FOLD`.

## Cambios de 0.205.0 — listas largas y chimeneas

- `DiaryLanguageScreen`: `widthCached` en nombre de la niebla, rótulo de fila y código de lengua.
- `DiaryArchiveScreen`: `widthCached` en sellos de documento, `SIGILLUM` y leyenda de vuelta.
- `VictorianSkyline`: `ensureChimneyLayout(w,h)` + `fillChimneyRects(...)` (arrays `chimBg*`/`chimFg*`, `NO_ANCHOR`).

## Cambios de 0.204.0 — widgets y centrado común

- `DiaryChrome.drawCenteredString` → `widthCached`.
- `DiaryToggle`, `DiarySlider`, `SlimButton`, `DiaryProgressBar`, `DiaryAmbienceSlider`, `DiaryMusicSlider`:
  `DiaryChrome.widthCached` en lugar de `DiaryTypography.width`/`font.width` por cuadro.

## Cambios de 0.203.0 — barrido general de medidas

- `DiaryChrome`: `fitCached(font,text,maxW)`, `widthCached(font,text)`, `trFitCached(key,font,maxW)`;
  `FIT_CACHE`/`WIDTH_CACHE` (tope 256, se vacían con el idioma en `tr`).
- 15 pantallas usan ya los helpers (`DiaryAccessibilityOptions`, `DiaryChatOptions`, `DiaryConfirm`, `DiaryControls`,
  `DiaryOnlineOptions`, `DiaryPackSelection`, `DiarySkinCustomization`, `DiarySoundOptions`, `DiaryDirectJoin`,
  `DiaryEditServer`, `DiaryDispatch`, `DiaryTitle`).
- `DiaryTitleScreen`: `cachedCompactSig*` para la firma recortada en modo compacto.

## Cambios de 0.202.0 — cabeceras de créditos y archivo

- `DiaryCreditsScreen`: `refreshTextCache()` (fuente, `panelW`, inglés, título) guarda `headerTitle*`, `headerSub*`,
  `headerFlavor*`, `versionText/W`, `noteText/W`; `drawSection` usa `labelWidths` (HashMap).
- `DiaryArchiveScreen`: `refreshHeaderCache(en, found, total)` guarda `headerTitle/Sub/Prog/Stamp/StampCol/StampW`.

## Cambios de 0.201.0 — cabeceras de multijugador y pausa

- `DiaryMultiplayerScreen`: `refreshHeaderCache(maxWidth)` (clave: fuente, ancho, título, subtítulo) guarda
  `headerTitle/TitleX`, `headerSubtitle/SubX`; `drawServerCount` cachea `countText/countWidth`.
- `DiaryPauseScreen.drawPanelTitle`: `headerTitleW`/`headerScale`/`headerSubtitle`/`headerSubW` en caché.

## Cambios de 0.200.0 — cabecera de idioma memorizada

- `DiaryLanguageScreen`: `refreshHeaderCache()` (clave: fuente, `panelW`, título) guarda `headerTitle/TitleX`,
  `headerSubtitle/SubX`, `headerFlavor/FlavorX`; `drawLanguageCount` cachea frase y ancho (`countText*`).
- `ChangelogScreen`: contador de la lupa cacheado (`countSearchText/W/Hits/Pos/Empty/Font`).

## Cambios de 0.199.0 — renglones de la crónica memorizados

- `ChangelogScreen.Row`: `WrapCache whole`/`body` (fuente + ancho como clave); `rowHeight` mide con la misma
  entrada que usa `renderRow`, así que el `font.split` de cada renglón visible ocurre una vez por ancho.
- Folio del pie: `cachedFolio`/`cachedFolioWidth`/`cachedFolioCur`/`cachedFolioPages`/`cachedFolioFont`.

## Cambios de 0.198.0 — bocas de chimenea memorizadas

- `VictorianSkyline`: `ensureSmokeAnchors(w,h)` + `fillSmokeAnchors(...)` cachean por resolución
  `smokeBgX/W/Top` y `smokeFgX/W/Top` (`NO_ANCHOR` = `Integer.MIN_VALUE`); `renderSmokeGroup` usa los arrays.
- `renderSmokeGroup`: `windDeep` fuera del bucle de partículas; `continue` si el puff cae fuera de pantalla
  (`sy + sz < 0 || sy - sz > h`) o si `sA < 6`.

## Cambios de 0.197.0 — trazado de ventanas memorizado

- `VictorianSkyline`: `ensureWindowLayout(w,h)` + `collectWindows(...)` resuelven una vez por resolución
  posición/tamaño/suerte de cada ventana; arrays paralelos `winFg*`/`winBg*`; en el fondo `winBgColor`
  ya trae el ARGB final. El dibujo por cuadro queda en fills (con `Math.sin` sólo para el parpadeo del frente).
- `DiaryFieldGuideScreen`: `headerHintX` calculado en `refreshHeaderCache`.

## Cambios de 0.196.0 — el parte de CEDA

- `DiaryDispatchScreen`: `SLOT_NUMBERS[]` estático (sin `String.format` por fila) y `keyLabel(slot)` con caché por binding
  (`keyLabels`/`keyBindings`, `Integer.MIN_VALUE` como centinela).

## Cambios de 0.195.0 — traducciones memorizadas

- `DiaryChrome.tr(key)` + `trFit(key, font, maxW)`: memoria invalidada por `LanguageManager.getSelected()`; 34 llamadas
  por cuadro reemplazadas en 20 pantallas/widgets (el import se agregó donde faltaba).
- `DiaryLanguageScreen.drawLanguageCount(g, titleY)`: contador de lenguas a la izquierda del título (`panelW >= 300`,
  margen derecho reservado para REG. IV).

## Cambios de 0.194.0 — opciones y calle

- `DiaryOptionsScreen`: `headerTitleFitted()`/`headerSubtitleFitted()` (fuente + `panelW` + texto del título).
- `DiaryToggle`: `baseLabelStr` final; valor cacheado por `Component`.
- `DiarySlider`: `cachedMessage()` + un `font` por cuadro.
- Adoquines: juntas cada dos columnas salvo en la hilera más cercana; derrame de luz de los reflectores en el suelo.

## Cambios de 0.193.0 — texto sin recalcular

- `DiaryFieldGuideScreen`: culling por viewport en el bucle de filas (`y + LINE_HEIGHT < top - 4` → continue, `y > bottom + 4` → break),
  `GuideRow.cachedWidth`, y `refreshHeaderCache` (fuente + `panelW` + clave de subtítulo).
- `DiaryArchiveScreen`: `ensureCardLabels(cw, found)` + `questionW`; `drawWrapped` con `wrapCache` (fuente + ancho).
- `DiaryTypography.fitString`: memo de dos niveles (`maxWidth+modo` → `texto`), topes 48/256, `clear()` al cambiar de fuente.
- `DiaryButton`: `cachedFit`/`cachedWidth`.

## Cambios de 0.192.0 — nubes por bandas

- `renderClouds`: los dos blobs por nube pasan a **dos `drawCloudMass`** (bandas 3-8 siguiendo la elipse): ~310 → ~80 rellenos
  por cuadro.
- **Borde de luna** en las nubes cercanas (`moonCenterX`, línea de 1 px `0x00726042`, alpha por distancia).

## Cambios de 0.191.0 — ventanas, humo y asfalto

- `VictorianSkyline.renderWindowsOnBuildings`: `lodSkip` a 1600 px = **3** (antes 2), divisores FG 16/18 (antes 14/16),
  ventana encendida FG sin travesaño horizontal (5 → 4 fills). Las ventanas bajan a ~la mitad.
- Tres tonos de luz en las ventanas del fondo, deterministas por `(row, col, bi)`.
- `renderSmokeGroup`: 3 puffs por chimenea, `sz` ×1.35.
- `LivingAtmosphere.renderStreet`: sin juntas en la hilera lejana + filo de agua por hilera.

## Cambios de 0.190.0 — el cielo y los blobs blandos

- `AtmosphereRenderer.drawSoftBlob`: el anillo exterior ahora lleva alpha (`0.22 + 0.55·t`), así que el radio se conserva
  con menos anillos: `steps = radius >= 14 ? 3 : 2`, `rowStep = radius >= 24 ? 4 : radius >= 12 ? 3 : 2`. Halo de la luna,
  nubes y humo pasan de ~1.500 a ~500 rellenos por cuadro.
- `renderStars`: brillantes con halo + núcleo (5 → 2 fills).
- `renderSkyGradient`: franja fría `0x00182A38` encima del resplandor ámbar + línea de horizonte.
- `renderSearchlights`: lámpara en cada torre.
- `moonCenterX(w)` público; `LivingAtmosphere.renderStreet`: reflejo de la luna (`fillGradientV` + 3 destellos).

## Cambios de 0.189.0 — menos rellenos en la pausa

- `DiaryPauseScreen.drawPanel`: el brillo era un `g.fill` por fila del panel (~300 por cuadro) → **2 `fillGradient`**
  (de `PAPER_HIGHLIGHT` α0 a α140 en el 40 % de la altura, y de vuelta).
- `DiaryPauseScreen.drawVignette`: 40 pasos de 1 px × 4 lados (160 fills) → **10 bandas de 4 px** (40 fills, `t²·70`).
- `DiaryPauseScreen.drawBloodSpotWithSplatter`: doble bucle píxel por píxel (113 fills por mancha) → **un tramo por
  fila** (`sqrt(r²-dy²)`, α vertical). La pausa entera queda en ~85 llamadas de relleno por cuadro.
- `DiaryChrome.drawRegistryTagAt`: 13 fills de una fila → **3** (`STEEL_MID`, `+4` en y+2, `-2` en y+h-4); se llama en
  las 21 pantallas.
- `DiaryLanguageScreen.LanguageEntry`: hebra `0x1C422810` al pie de cada renglón, sólo si no está elegido ni hover.

## Cambios de 0.188.0 — la ciudad sin periódicos y con reflectores

- **Los periódicos de la portada se fueron del todo.** Se borraron `PosterRenderer` (943 líneas) y
  `OrnamentRenderer` (156, sólo lo usaba el otro), más `drawPosterLeft`/`drawPosterRight`,
  `overQuarantinePoster`, `wantedPosterRect`, `wantedPressing`/`wantedPressStartMs` y las dos
  apariciones que sólo se desbloqueaban ahí (`Discoveries.WANTED_EYES` y `POSTER_PEEL`). Los nombres
  están en la lista `banned` de `tools/verify_mirrors.py`. El arte queda en el historial (tag `v0.187.0`).
- **Layout de la portada sin `POSTER_*`**: `LEFT_COLUMN_W = 180` y
  `WATCH_COLUMN_W = WATCH_RADIUS * 2 + 20`; la x de la mesa de botones y la del reloj quedan idénticas
  a 0.187.0, y la ficha del cronista conserva su carril.
- **Fondo de la portada**: `VictorianSkyline.renderFarSkyline` (capa lejana: 14 rectángulos + 6 ventanas
  fijas, color `0xFF2A2116`) llamada antes de `BG_BUILDINGS`, y `AtmosphereRenderer.renderSearchlights`
  (dos haces cruzados, 18 tramos de 2 px cada uno, `0x00D8C48A`; con movimiento reducido se quedan
  quietos y en ventanas menores a 480×240 no se dibujan). Se llama desde
  `QuarantineSceneRenderer.renderTitleScene`, entre el skyline y el primer plano.
- **Optimización de esta tanda**: `FlagRenderer.drawGrid` pinta cada fila por tramos del mismo color
  (`~100 → ~25` rellenos por bandera); `DiaryLanguageScreen.LanguageEntry` memoriza etiqueta y ancho en
  el constructor (tres variantes: normal, pendiente, aplicada); `VictorianSkyline.renderCrack` eran
  tres bucles de un píxel de alto por cuadro y ahora son tres rectángulos.
- **Multijugador**: botones de `20` a `22` px de alto con `rowGap` 12, y contador de servidores anotados
  arriba a la derecha del título (`drawServerCount`, sin claves nuevas).
- **Pausa**: botón `gripeverde.pause.btn.ceda` (icono `CHAT`) que abre `DiaryDispatchScreen` sobre la
  pausa; la rejilla pasa a 7 filas y a 4×2 cuando `ph < 250`. Clave nueva → 190 por lengua.

## Cambios de 0.187.0 — vuelven las hojas del diario

El usuario aclaró que su pedido de sacar «hojas» se refería a los **periódicos de la portada**, no
al papel de las pantallas. Por eso:

- `PaperTexture.drawSheet(...)` vuelve a poner hoja opaca (`SHEET_ALPHA = 243`) con filete interior,
  tres manchas y dos fibras derivadas del panel (deterministas, sin RNG por cuadro): unas once
  llamadas de relleno por panel, la mitad que la hoja de 0.183.0.
- Las **21 pantallas** y `DiaryChrome.drawPanel` usan `drawSheet`; `drawVeil` deja de usarse.
- Los periódicos (`PosterRenderer.drawQuarantineNotice`, `drawRotatingNoticeRight`) **no se tocan**:
  siguen en la portada. Si el usuario quiere que salgan, es un cambio de una línea en
  `DiaryTitleScreen.drawPosterLeft/drawPosterRight`.

## Cambios de 0.186.0 — botones que respiran y el menú liviano

- Portada: `BTN_W 264`, `BTN_H 34`, `BTN_SLIM_W 126`, `BTN_SLIM_H 28`; la etiqueta se **encoge**
  (`DiaryTypography.drawFitted`, mínimo 0.62) en vez de recortarse con puntos suspensivos.
- Subrayado de `DiaryButton` en tramos de 3 px (antes un `fill` por píxel).
- `AtmosphereRenderer.drawSoftBlob`: **4 anillos y franjas de 2 px** (antes 6 anillos con un `fill`
  por scanline); halo de la luna de 3 blobs a 2. El humo de chimeneas y las nubes usan el mismo pincel.
- **Pendiente de confirmar con el usuario:** el pedido «las hojas devuélvelas». Los periódicos de la
  portada (`PosterRenderer`: bando, paciente cero, el tren) **no se tocaron nunca**; lo que cambió en
  0.184.0 fue el papel de los paneles de las 21 pantallas (velo translúcido). Queda por decidir si se
  quitan los periódicos de la portada o si vuelve el papel de hoja a los paneles.

## Cambios de 0.185.0 — veinte voces y el papel limpio de verdad

- **CEDA con veinte voces** (`DispatchLibrary.SLOT_COUNT = 20`), sin cambiar el formato del archivo.
  Paginación con **rueda**, **RePag/AvPag** y **clic derecho** en el rótulo; hasta doce renglones por
  página. `Ctrl`+`1..9/0` manda la voz y con `Shift` las 11..20; `Enter` baja al campo siguiente. Los
  números van en tinta fuerte cuando la voz ya tiene mensaje y el pie cuenta cuántas van
  (`gripeverde.dispatch.slot.count`, clave nueva).
- **`PaperTexture` reducido a lo que se usa**: `drawVeil` (dos sobrecargas), `PaperKind` y la paleta.
  Se borraron 23 métodos de adornos que ya nadie llamaba y `incrementOpenCount`; el verificador
  prohíbe esos nombres (`drawClean`, `drawWatermark`, `drawWaxSeal`, `drawRainDrops`, …).
- **Fuera el sello de cera del panel de pausa**: era un adorno en la hoja y costaba dos bucles
  píxel por píxel (más de mil `fill` por cuadro).
- **`DiaryTypography.fitString` por bisección** en vez de armar la cadena carácter por carácter.

## Cambios de 0.184.0 — el diario se saca la hoja de encima

Tanda de correcciones grandes, casi toda de interfaz:

- **Parte de CEDA rehecho.** `DiaryDispatchScreen` ya no tiene renglones libres ni ranuras
  al costado: es una lista de mensajes (veinte desde 0.185.0), cada uno con su campo y su tecla a la derecha.
  Se fueron ENVIAR y ENVIAR TODO (no hay envío en lote desde la ficha; el mensaje sale con su
  tecla mientras jugás). Clic en la tecla deja la ficha escuchando, Esc cancela, Supr o clic
  derecho borra. Autoguardado a los 700 ms de la última letra y otra vez al cerrar. Si no
  caben diez, paginación con el rótulo `▸ página/total` en el pie.
- **Fuera los renglones de niveles** de la ficha de sonido (`Niveles: …` y `Niveles de
  Minecraft: …`); quedan el título y el disco que suena. Se retiraron también sus tres claves
  de idioma.
- **La música del cronista ya no depende de la música vanilla:** música y ambiente del mod
  emiten por el canal `MASTER` (`FadingMusicInstance`, `LoopingAmbienceInstance`,
  `MenuAmbience.ambientOneShot`), así que los sliders Música y Ambiente de Minecraft no los
  apagan ni los atenúan. Manda el slider propio y, por encima, el maestro.
- **Ninguna pantalla dibuja hojas con adornos.** `PaperTexture.drawVeil` deja un velo
  translúcido (`VEIL_ALPHA = 196`) y un filete: se ve el fondo del menú, sin marca de agua
  (el "robot" del centro), sin sello de cera, sin gotas ni fibras. 21 pantallas migradas.
- **Sello NUEVO entero.** `drawFreshStamp` mide el lacre completo y lo mete dentro del área
  visible; si no cabe entero, no se dibuja, en vez de aparecer cortado como "NUEV".
- **Botones de portada más chicos:** `BTN_W 248`, `BTN_H 32`, `BTN_SLIM_W 112`,
  `BTN_SLIM_H 26` (antes 300×40 y 130×32).
- **Fuera el huevo de teclear cifras y fechas:** `HiddenCipher` eliminado, junto con los tres códigos que
  sólo él abría (`code_1152`, `code_pereira`, `code_doctor`). También se fueron las marcas del encuadernador y el folio escondido de la
  crónica. Los únicos huevos que quedan son los del fondo.
- **Velocidad:** la crónica memoriza el alto de cada renglón (`ensureRowHeights`/`heightAt`);
  antes volvía a cortar todo el texto en cada cuadro para saber dónde cae el scroll.
- `verify_mirrors.py` gana identificadores prohibidos nuevos (`HiddenCipher`, `onCharTyped`,
  `isChiming`, `asteriskRects`, `hiddenFolioRect`, `BINDER_MARKS`, `drawNewSeal`) para que
  nada de eso vuelva. Claves de idioma: **189** (antes 201).

## Cambios de 0.183.0 — ningún adorno contesta antes que los botones

Quedaban tres pantallas de lectura donde un adorno con clic se consultaba antes que los
widgets, la misma falla que la portada dejó atrás en 0.178.0.

- **`DiaryChrome.overAnyWidget(screen, x, y)`**: ayudante único (usa `screen.children()` y
  `widget.isActive()`), pensado para que cada pantalla pregunte primero por sus botones.
- **`DiaryFieldGuideScreen`**: `headingAt()` sólo corre si el clic no cayó en un widget; aire
  nuevo con `LINE_HEIGHT` 11→12, `PARAGRAPH_GAP` 4→7 y `SECTION_GAP` 6→10.
- **`DiaryArchiveScreen`**: los widgets se consultan antes que los papeles sueltos y que la
  banda de cierre (`panelY + panelH - 56`), que se quedaba clics; `CARD_GAP` 5→7.
- **`ChangelogScreen`**: usa el ayudante compartido (se borra la copia local de 0.182.0).
- **`DiaryDispatchScreen`**: `Ctrl`+`1`..`9` y `Ctrl`+`0` (también teclado numérico) mandan esa
  ranura; la ayuda de la ranura lo dice en los 25 idiomas.
- **Lint nuevo en `tools/verify_mirrors.py`**: rechaza `net.minecraft.util.Util` (no existe: es
  `net.minecraft.Util`) y cualquier clase de apoyo usada sin import (se omiten misma clase,
  mismo paquete y nombres completos). Probado en negativo: al quitar el import de
  `DiaryChrome` el verificador falla con `ChangelogScreen.java: usa DiaryChrome sin importarlo`.
- **CI**: el lint corre **antes** de Gradle (falla en segundos, no en dos minutos) y un push
  sin PR ahora escribe el error como **comentario de commit** (`contents: write`).

## Cambios de 0.182.0 — la crónica respira

`ChangelogScreen` (1066 líneas) era la hoja más apretada del diario y la única que nunca se
había auditado: 22 `return true` entre lupa, barra, marcas del encuadernador y folio escondido.

- **Ritmo**: `LINE_H` 11→12, `H1_LINE_H` 16→18, `H2_LINE_H` y `BOLD_LINE_H` 13→14, `GAP_H`
  10→13, `SEP_H` 12→16, `PAD_Y` 26→30.
- **Margen lateral**: `PANEL_MX = 56` fijo pasó a `panelMx() = clamp(12, width/8, 56)`. A 320 px
  el panel crece de 208 a 240 de ancho; `panelW()` tiene piso de 120.
- **Botón Cerrar**: `closeW = clamp(84, min(130, panelW-24))` y centrado sobre el panel; en
  ventanas mínimas ya no sobresale de la hoja.
- **Clics**: las marcas del encuadernador y el folio escondido se consultan **después** de los
  widgets (`overAnyWidget()` con `this.children()`, igual que la portada desde 0.178.0).
- **Rueda**: si `maxScroll <= 0` devuelve `super.mouseScrolled` y no suena; si el gesto no movió
  nada, tampoco.
- **CI**: `.github/workflows/build.yml` compila también `arena/**`.

## Cambios de 0.181.0 — los mensajes de CopyL se vienen con nosotros

CopyL guardaba los diez mensajes rápidos en `config/copyl-messages.json`
(`names[10]`, `messages[10]`, `keyCodes[10]`, con `-1` libre, teclas GLFW crudas
y botones de mouse como `1000 + n`), y la tecla de apertura en `config/copyl.json`
(`openKey`, Alt derecho por defecto; `lclient.json` como legado). Desinstalarlo se
llevaba todo eso puesto.

- **`client/integration/DispatchLibrary`**: lee esos archivos **una sola vez** y
  guarda la copia propia en `config/gripeverde-dispatch.json`
  (`importedFrom`, `openKey`, `slots[]` con `name`, `message`, `key`). Nunca
  escribe ni borra archivos de CopyL. Normaliza igual que CopyL: una sola línea,
  texto ≤ 256, nombre ≤ 24, tecla válida o `-1`.
- **`client/DispatchHotkeys`**: sondea el estado físico del teclado (sin
  `KeyMapping`, para no tocar `options.txt`) y envía la ranura al flanco de
  bajada; `/algo` va por `sendCommand`, el resto por `sendChat`. Se **apaga
  entero mientras CopyL esté instalado** (`CopyLCompatibility.isAvailable()`),
  así ningún mensaje sale dos veces. La tecla de apertura levanta la ficha.
- **La ficha**: con `panelW >= 420` la ficha mide hasta 520 de ancho y abre una
  **columna de diez ranuras** a la derecha (nombre a la izquierda, tecla a la
  derecha, 22 px por fila); en ventanas angostas las ranuras son una **tira de
  chips numerados** arriba del formulario. Clic envía, `Shift`+clic carga el
  texto en el primer renglón libre, `Ctrl`+clic guarda el renglón en esa ranura y
  clic derecho fija tecla (Esc cancela, Supr borra).
- La línea de estado avisa: cuántas ranuras tienen texto, que CopyL está
  instalado y sus teclas dormidas, y la ayuda de la ranura bajo el puntero.
- **Idiomas**: 9 claves nuevas × 25 locales; el espejo pasa de 191 a **200**.
- **Para el usuario**: se puede sacar `copyl-*.jar` de `mods` sin perder los
  mensajes; al primer arranque la ficha los importa y los atajos pasan a
  responder. Si CopyL sigue instalado, siguen mandando los suyos.

## Cambios de 0.180.0 — el parte tiene su propia mesa

El acceso de CEDA de la portada era un puente por reflexión: solo aparecía si
`ModList` encontraba `copyl` y abría `CopyLSettingsScreen` de otro mod. Ahora el
formulario es nuestro (`DiaryDispatchScreen`) y el puente queda solo como puerta
opcional dentro de la ficha.

- **Siempre hay entrada**: el icono de la portada (x = `width - 76`, 20x20) ya no
  depende de CopyL; con CopyL instalado, su panel se abre desde la esquina
  superior derecha de la ficha (20x20, icono `DISPATCH_NOTE`).
- **Aire de sobra**: ocho renglones (`MAX_LINES`) de 16 px con **12 px de hueco**
  (`ROW_STEP = 28`), carril de **34 px** para el número (`01`..`08`) y un filete
  de tinta antes del pie. CopyL apilaba los renglones sin carril ni hueco.
- **Pie en dos filas** (`FOOTER_H = 84`): línea de estado y cuatro botones
  (ENVIAR, ENVIAR TODO, LIMPIAR, VOLVER) con 10 px entre ellos y `btnInset` de
  12 a 22 px según el ancho.
- **Comportamiento**: `Enter` envía el renglón enfocado (o el primero con texto) y
  pasa al siguiente; `Tab`/`Shift+Tab` recorren los renglones; el envío en tanda
  espera **10 ticks** entre mensajes; el borrador vive en un `static String[]`
  para que cerrar la ficha no borre lo escrito. Sin conexión, el estado avisa y
  no se envía nada (`player.connection == null`).
- **Tamaños**: la ficha mide `min(400, max(240, width - 24))` por
  `min(380, max(190, height - 24))`; los renglones visibles salen de
  `(rowsBottom - rowsTop) / 28` acotado a 3..8. Con el tope viejo de 300 px de
  alto solo cabían 5, así que la ficha creció a 380: ahora 320x240 y 427x240 dan
  3 renglones, 480x270 da 4, 640x360 da 7 y desde 700x400 hasta 1920x1080 dan
  los 8 completos.
- **Idiomas**: 14 claves nuevas (`gripeverde.dispatch.*`) en los 25 locales; el
  espejo pasa de 177 a **191** claves y se actualizó en `verify_mirrors.py` y en
  `build.gradle`.
- **Entrega**: `release-tag.txt` marca la versión a publicar; el job `publish`
  solo crea el release si el tag coincide con `build.gradle` y el release no
  existe, así que un push normal no publica nada y nunca se sobrescribe un
  release viejo con un build nuevo.

## Cambios de 0.179.0 — el cronista habla en toda ventana

`renderChroniclerNote()` salía en silencio en cuanto `isMinimalMode()` era cierto
(toda ventana de menos de 620 px de ancho, o de menos de 290 px de alto), y
también cuando la ficha no tenía carril (`cardW < 180`) o altura. Resultado: en
427x240 —la ventana por defecto— el saludo del cronista **nunca** se veía.

- `drawChroniclerCard()` es ahora la ficha completa y devuelve `false` en vez de
  salir por su cuenta: carril menor de 180 px, sin altura entre `logoBottom()` y
  `buttonTableTop()`, o lista de líneas vacía.
- `drawGreetingHeader()` dibuja la misma frase en **una línea centrada en y=10**,
  recortada a `width - 160` con `fitString`. La cabecera estaba libre en todos los
  tamaños: el control de música ocupa x 8..44 y los iconos de acceso empiezan en
  `width - 76`, así que quedan 36 px a la izquierda y 4 px a la derecha.
- Se cachea por ancho, fuente y texto, como el resto de notas de la portada.
- Cuando la ficha cabe, no cambia nada: 620x360, 700x400, 854x480, 1024x600,
  1280x720 y 1920x1080 siguen con ficha (390x44, o 204/224/284 donde el carril
  manda). Pasan a cabecera 320..619 px de ancho y las anchas por debajo de
  ~330 px de alto (620x290, 700x300, 720x340, 854x240).
- Sin colisión con el susurro de la vigilia: la cabecera está en y=10..19 y
  `vigilLineY()` nunca devuelve menos de `logoBottom() + GAP_MEDIUM` ni más abajo
  del pie.

## Cambios de 0.178.0 — ningún adorno roba un clic

`DiaryTitleScreen.mouseClicked` comprueba primero los puntos calientes
decorativos y después llama a `super.mouseClicked`, que es quien reparte el clic
entre los widgets. Varios de esos adornos devolvían `true`, así que un clic
destinado a un botón se quedaba en el adorno:

| Adorno | Tamaño | Solape con botones |
| --- | --- | --- |
| Ventana del ático (`signalWindowRect`) | 427x240 | 12 px dentro de la pareja (x 254..264, y 138..150 sobre Multijugador 116..204) |
| Rata (`ratHit`, y = 0,90 h) | 854x480 | 8 px dentro de la mesa y de Ajustes/Salir |
| Rata | 1280x720 y 1920x1080 | 2 a 8 px dentro de la pareja |
| Gato (`catRect`) | 427x240 | 7 px, pero ya estaba excluido por `isMinimalMode()` |

- `overAnyButton(mouseX, mouseY)` recorre `this.children` y pregunta a los
  widgets reales (`AbstractWidget.visible && isMouseOver`), así que no duplica la
  geometría de `init()` ni puede desincronizarse como las fórmulas repetidas.
- La bandera se calcula una vez por clic y protege reloj, bando de cuarentena,
  cartel del buscado, gato, rata, ventana del ático y clic del emblema.
- `DiaryPauseScreen`: la vela (`y = panelH - 46..panelH - 4`) se metía hasta 12 px
  dentro del último botón de la izquierda en la rejilla de dos columnas de las
  ventanas de 235 a 260 px de alto; ahora vela y cuervo también preguntan por los
  botones.
- El zumbido del clic sigue igual: si no hay botón debajo, el adorno responde como
  siempre.

## Cambios de 0.177.0 — el susurro de la vigilia encuentra su banda

`drawVigilLine` dibujaba el susurro de la vigilia en `height / 3 + 24`, una
posición fija que en **todas** las ventanas de menos de unos 410 px de alto caía
dentro del emblema:

| Tamaño | Emblema | Susurro antes | Ahora |
| --- | --- | --- | --- |
| 427x240 | 30..108 | 104..113, sobre el filete de la fecha | 216..225, al pie sobre la firma |
| 560x315 | 30..161 | 129..138, sobre «GRIPE VERDE» | 169..178, banda libre |
| 700x400 | 30..161 | 157..166, sobre el renglón del día | 169..178, banda libre |
| 854x480 | 30..161 | 184..193 | 184..193 (sin cambios) |
| 1024x600 | 30..161 | 224..233 | 224..233 (sin cambios) |

- `vigilLineY()` conserva la posición original cuando ya cae en banda limpia y,
  si no, busca la banda entre `logoBottom() + GAP_MEDIUM` (8 px bajo el filete
  de la fecha) y `buttonTableTop() - GAP_SMALL`; cuando no hay ni una línea de
  alto, baja al pie en `height - 24`, que deja 3 px sobre la firma
  (`height - 12`).
- El susurro nunca coincide con la ficha del cronista: la ficha se muestra 8 s
  al abrir la portada y el susurro exige 60 s sin interacción.
- `estimateFirstButtonTop()` pasa a devolver `buttonTableTop()`: era otra copia
  de la fórmula de la mesa derivada de `leftColumn()`, que en ventanas estrechas
  no coincide porque el grupo se centra. Ahora emblema, mesa y caja de telegrama
  comparten una sola fuente de verdad.
- Auditoría: 17 anchos (320 a 1920) x 15 altos (180 a 1080) = 255 combinaciones,
  sin fallos de susurro sobre el emblema, sobre la mesa, sobre la firma ni fuera
  de pantalla.

## Cambios de 0.176.0 — Ajustes y Salir se apartan del emblema

Defecto que la auditoría de 0.175.0 no vio porque solo comparaba el emblema con
la pareja principal: en **todas** las ventanas de menos de 620 px de ancho, la
fila de Ajustes y Salir se colocaba en `y = max(8, min(36, btnY - 36))`, o sea
en `y = 36..68`, justo sobre el emblema, que empieza en `LOGO_TOP = 30`. A
427x240 el escudo ocupa `y = 30..108` y los dos botones tapaban 32 px de su
franja media; a 560x315 tapaban 32 px de un emblema que ahí llega hasta 161.

- La pareja principal y el carril comparten banda vertical: el carril vive en
  `[btnY + (88 - 68) / 2, ...]`, o sea dentro de la mesa, que el emblema ya
  respeta desde 0.175.0. No hay solape posible por construcción, no por cálculo
  de distancias.
- `compactSlimRailWidth() = clamp(28 % del ancho interior, 40, 110)`: el carril
  nunca se lleva más de lo que la pareja necesita para su rótulo y subtítulo.
- El grupo (pareja + 4 px + carril) se centra con
  `btnX = max(compactInset, (width - groupW) / 2)`; a 320 px ocupa justo el
  ancho interior (198 + 4 + 78 = 280) y en ventanas más anchas queda sobre el
  eje del emblema, como los carteles.
- `btnY` en compacto pasa a ser `buttonTableTop()` directamente: antes se
  derivaba de `LayoutRegion.screen(...).inset(btnX)`, y con el grupo centrado
  `btnX` ya no es el margen, así que ambas fórmulas podían discrepar.
- `compactInset()` centraliza el margen compacto que estaba repetido en
  `init()` y en `buttonTableTop()`.

Auditoría: 13 anchos (320 a 619) x 10 altos (180 a 480) = 130 combinaciones, sin
fallos de carril fuera de pantalla, carril sobre la pareja, carril fuera de la
banda, emblema sobre la mesa, aire menor que `GAP_MEDIUM`, pareja menor de
120 px ni carril menor de 64 px. Las ventanas de 620 px o más no cambian
(conservan Ajustes y Salir en la columna derecha).

## Cambios de 0.175.0 — la portada deja de pisar sus propios botones

Auditoría de layout de `DiaryTitleScreen` con las fórmulas exactas del código. Cuatro
defectos reproducibles, corregidos solo tras medirlos:

- **Emblema sobre la mesa de botones.** El bloque del emblema (`LogoRenderer`) ocupa
  `y = 30..161`. La mesa de botones arranca en
  `height - inset - 88 - 16` (`inset` = 20, o `clamp(width/16, 8, 20)` en modo compacto).
  A **427x240** —el tamaño por defecto de una ventana 854x480 con escala GUI 2— los botones
  empiezan en 116, así que el emblema los invadía 45 px; igual en 480x270 (invadía 15 px).
  Ahora `logoScale()` mide la banda libre y encoge el bloque completo (`BLOCK_H`, `BLOCK_W`)
  con una transformación de pose alrededor de su ancla; por debajo de `LOGO_MIN_SCALE = 0.5`
  se omite antes que dibujarlo encima de los botones.
- **Ficha del cronista.** Estaba clavada en `y = 168`. En 620x290 y 700x300 la mesa de
  botones empieza en 166 y 176, así que la ficha caía encima de «Multijugador». Ahora se
  coloca bajo `logoBottom()` y se sube al límite de la mesa, o no se dibuja si no cabe.
- **Cadena del reloj de bolsillo.** Se dibujaba desde `topY = 0` y cruzaba la fila de
  iconos superiores (crónica, guía, CEDA), que ocupa `y 8..28` en el mismo carril de la
  derecha. Arranca en `ICON_ROW_BOTTOM + GAP_SMALL`.
- **Zona de clic del emblema.** Seguía siendo `y 30..130` con caja fija, de modo que en
  ventanas de 240 px el borde de «Multijugador» (y=116) disparaba el sello del emblema. Usa
  el alto real escalado, y el clic del ojo de la calavera se recalcula a la misma escala.

Verificado con la geometría real en 320x240, 427x240, 480x270, 560x315, 620x290, 700x300,
700x400, 854x480, 1280x720 y 1920x1080: emblema y ficha siempre terminan al menos 8 px por
encima de la mesa de botones, o se ocultan.

## Cambios de 0.173.0

- **Aviso CEDA de la portada (nota de campo Ghoul).** Pasa a leerse como ficha de campo:
  cabecera CEDA en tinta azul, filete, una placa de imprenta con el retrato del infectado y
  un rótulo de dos renglones. El retrato se rediseñó por completo (cráneo ovalado,
  mandíbula angosta, ojos azules con brillo, levita con cuello abierto) y se dibuja con
  formas fijas, adaptándose al tamaño del cartel. En carteles bajos el retrato se omite en
  lugar de comprimir el texto.
- **Pie sin solapes.** El aviso final («NUNCA VIAJES SOLO») y el sello CEDA ocupan zonas
  distintas: el sello circular de ojo queda en el margen izquierdo y el texto se limita a
  su derecha. Sustituye al ojo suelto anterior y ya no puede montarse sobre el parte.
- **Bando de cuarentena.** El retrato del pie se agrandó y ahora convive con la pena en dos
  columnas; el espaciado se compacta en carteles bajos y el sello de cera solo se dibuja
  si queda aire real bajo el último renglón.
- **Nueva clave de idioma** `gripeverde.poster.ghoul.caption` en los 25 locales
  (inglés/español regional, incluido `es_uy`). El total de claves pasa de 176 a 177 y la
  validación de Gradle y el espejo en Python se actualizaron.
- **Integración CEDA Dispatch.** `CopyLCompatibility` ahora registra en `latest.log` la
  versión instalada de CopyL y si dibuja sus paneles con texto plano o con la fuente
  personalizada. No cambia la pantalla, que sigue siendo de CopyL.
- Versión del mod, `build.gradle`, crónicas de los 25 locales, `CHANGELOG.md` y `README.md`
  actualizados a `0.173.0`.

## Investigación de CEDA Dispatch / CopyL (evidencia)

Repositorio fuente localizado: [Santi-PdR/Lclient](https://github.com/Santi-PdR/Lclient).
El JAR que usa el usuario, `copyl-3.9.4.jar`, corresponde a la rama
`codex/ceda-dispatch-visuals` (mods.toml `version="3.9.4"`, PR #18 de ese repo), no a
`main`, que quedó en 3.3.0.

Hallazgos concretos sobre los dos síntomas reportados:

1. **Caracteres cuadrados (tofu).** El commit `bdf77b8` («Use a clearer typeface across
   CEDA Dispatch», base 3.9.0) añadió `assets/copyl/font/AtkinsonHyperlegible-Regular.ttf`
   y `assets/copyl/font/dispatch.json` (proveedor `ttf`, `size: 9`, `oversample: 2`) y pasó
   todos los textos de los paneles por `DispatchText.styled()`, que aplicaba
   `style.withFont(copyl:dispatch)`. El commit `22f8c0f` («v3.9.1: use reliable default
   text in CEDA panels») retiró ese `withFont` y dejó `DispatchText.plain()`, con un
   comentario explícito: «so an unavailable custom font cannot become tofu boxes». Es
   decir, el propio CopyL documentó que los cuadros venían de su fuente personalizada.
   El `.ttf` y su JSON siguen empaquetados aunque ya nadie los usa.
2. **Textos superpuestos o duplicados.** El editor pasó a un dibujo híbrido:
   `MessageEditorScreen.render()` llama a `super.render()` (que ya pinta cada `EditBox` con
   su texto y su sombra mediante `AbstractWidget.render`), después `graphics.flush()` y
   luego `DispatchEditBox.drawFlatText()` vuelve a pintar el mismo texto sin sombra sobre un
   `fill` de papel. Son dos pasadas sobre el mismo píxel; además `getScreenX()` se mide con
   el ancho real de la fuente pero el repintado se desplaza usando `DispatchText.width`,
   con lo que cualquier diferencia de métrica desplaza el segundo texto. Las etiquetas
   centradas de `DispatchCard`/`drawCards` también se redibujan tras el flush.
3. **Integración de GripeVerde.** Es un puente por reflexión
   (`CopyLSettingsScreen(Screen)`); no envuelve ni sustituye la pantalla, no aplica estilos
   y no cambia la fuente global, así que no puede producir ni el tofu ni el doble dibujo.
   Su única vía de efecto visual sería abrir la pantalla equivocada, y CopyL 3.9.4 mantiene
   la clase y el constructor. El fallo está en CopyL; GripeVerde solo lo abre.
4. **Pendiente de verificación en cliente.** Confirmar en `latest.log` la línea
   `CEDA Dispatch x.y.z draws its panels with plain Minecraft text` (o el aviso si está en
   una versión con fuente) y comprobar si el JAR instalado es 3.9.4. Si el usuario tiene
   3.9.0–3.9.3, los cuadros se explican por la fuente; si ya tiene 3.9.4, lo que queda es
   el doble dibujo del editor.
5. **`build-output` de Lclient está desactualizado**: publica `version.txt` = 3.3.0 con
   SHA-256 propio, así que no debe usarse como fuente del JAR 3.9.4.

Corrección preparada para CopyL 3.9.5, en `tools/copyl/0001-copyl-3.9.5-one-text-pass.patch`
(SHA-256 `856be3c7…bef298`, 1.106 líneas, aplicado con `git am` limpio sobre `27854f6`):
quita la segunda pasada de texto del editor y la subclase `DispatchEditBox`, deja 6 px
entre los campos y la regla del margen para que las etiquetas no queden bajo las cajas,
borra el `.ttf`, `dispatch.json` y la licencia sin uso, y hace que el CI de Lclient falle si
vuelve a empaquetarse algo bajo `assets/copyl/font/`. Sube la versión a 3.9.5.

**Bloqueo del entorno:** la integración de GitHub de esta sesión puede escribir en
`Santi-PdR/GripeVerde`, pero responde `403 Resource not accessible by integration` al
crear refs o empujar en `Santi-PdR/Lclient`. Por eso la corrección queda como parche
reproducible; aplicarlo requiere una sesión con permiso sobre ese repositorio (o hacerlo a
mano con los pasos de `tools/copyl/README.md`).

## Pendientes

1. **Publicar CopyL 3.9.5:** aplicar `tools/copyl/0001-copyl-3.9.5-one-text-pass.patch` en
   `Santi-PdR/Lclient` (rama `codex/ceda-dispatch-visuals` o PR #18), dejar que el CI
   genere `CopyL-3.9.5` y sustituir `copyl-3.9.4.jar` en la carpeta de mods. El usuario ya
   autorizó la corrección; falta un entorno con permiso de escritura sobre Lclient.
2. **Instalar `0.175.0`** en la carpeta de mods cuando el equipo del usuario sea accesible:
   descargar el artefacto del CI, validar contenido y SHA-256, retirar solo los JAR
   antiguos de GripeVerde (y la versión previa de CopyL si se publica una nueva) y dejar la
   carpeta con archivos `.jar` únicamente. En una máquina Windows con la instancia montada:
   `powershell -ExecutionPolicy Bypass -File tools\install-artifact.ps1 -ArtifactZip <zip del artefacto>`.
3. **Revisión visual del usuario** del nuevo aviso Ghoul y del bando: el retrato se validó
   con una simulación de píxeles 1:1 del código, no dentro de Minecraft.
4. Actualizar la descripción del PR #90 con `0.173.0`, el resultado del CI y estos
   pendientes.

## Contexto de estilo y contenido

El mod se inspira en Ghouls/Left 4 Dead 2 y presenta una epidemia ficticia en el año 1800,
ocultada como «Gripe Verde». Los infectados son personas vivas transformadas, con piel
azulada y ojos azules brillantes: siguen siendo personas, no cadáveres. La interfaz debe
sentirse como papeles, avisos y anotaciones de época con señales de cuarentena, CEDA, tinta
roja y azul Ghoul. El tono de diarios y crónicas debe sonar natural, como testimonios
humanos, no como instrucciones técnicas.

Los idiomas principales son inglés y español, con sus variantes regionales; toda variante
`en_*` debe usar inglés y toda variante `es_*` español. No usar otros idiomas.

## Consejo de diseño vigente

Interfaces distinguidas y legibles, tipografía vanilla clara cuando una fuente personalizada
pueda producir tofu, jerarquía visual, sin sombras de texto excesivas, sin solapamientos y
con animación limitada a la portada. El juego en curso y las pantallas de
inventario/pausa deben seguir libres de animaciones añadidas.

## Próximo paso técnico recomendado

Que el usuario pruebe `0.181.0` sacando CopyL de `mods`: la ficha importa los diez mensajes al primer
arranque y los atajos pasan a responder. Revisar a ojo la columna de ranuras. Queda pendiente llevar el
mismo aire a las pantallas de CopyL: el push a `Santi-PdR/Lclient` sigue en 403, así que ese cambio
viaja como parche en `tools/copyl/`.
