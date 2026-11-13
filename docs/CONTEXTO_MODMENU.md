# Contexto de continuidad: GripeVerde ModMenu

Actualizado: 2026-11-13

## Proyecto y reglas de trabajo

- Repositorio: [Santi-PdR/GripeVerde](https://github.com/Santi-PdR/GripeVerde)
- Rama de trabajo: `codex/v0.104-ghoul-warning-poster`
- Pull request: [#90](https://github.com/Santi-PdR/GripeVerde/pull/90)
- Versión de esta rama: `0.173.0` (anterior entregada: `0.172.0`)
- El usuario quiere seguir mejorando el mod de forma continua hasta que diga que pare.
- El trabajo se hace sobre el código fuente alojado en GitHub; no se clona ni se edita el
  proyecto local del usuario.
- No se controla Minecraft ni la PC del usuario para pruebas visuales. El CI acredita
  compilación, recursos y localización; no sustituye una revisión dentro del juego.
- Las interfaces del juego se mantienen instantáneas: no se agregan animaciones al
  inventario, la pausa ni el mundo. Las únicas piezas animadas siguen siendo las del menú
  y respetan Movimiento reducido y Destellos reducidos.
- Al entregar una versión: subir la versión, generar el JAR por CI, actualizar la nota y el
  PR, e instalar el artefacto en la carpeta de mods cuando el equipo del usuario esté
  accesible.

## Estado confirmado al retomar (2026-11-13)

- El CI de la rama terminó en `success`: run `40763347401` (workflow *Build GripeVerde*),
  artefacto `GripeVerde-jar`, artifact ID `12849186641`, 8.742.493 bytes,
  SHA-256 `9520432c3027a3695b4e1246c2f8a3298cdc8ce11ab0cbff517d6fc914508c60`.
- `0.173.0` compila en verde en el HEAD actual (`afc3ca4`): run `43445167974`, artefacto
  `GripeVerde-jar`, artifact ID `14046694088`, 8.748.366 bytes, digest del ZIP publicado
  por GitHub `sha256:c256392a24ede9ab129dcc56064776925d167bcbb7ecad62aea2718882e9c37f`
  (es el hash del `.zip` del artefacto, no del JAR: el del JAR solo puede calcularse
  abriendo el ZIP). El run `43440351036` (artifact `14044838504`) fue el primero de la
  versión. Abajo se explica por qué no se pudo instalar desde aquí.
- `0.172.0` **no** se instaló en la instancia del usuario; la carpeta de mods de destino
  sigue siendo `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`.
- **Instalación bloqueada en esta sesión por dos motivos ajenos al mod:** el entorno es
  Linux (la ruta `D:\...` no existe como montaje) y el host de descarga de artefactos
  (`productionresultssa*.blob.core.windows.net`) no es alcanzable desde el sandbox, así que
  tampoco se pudo verificar aquí el SHA-256 del JAR. Queda listo
  `tools/install-artifact.ps1`, que valida `mods.toml`, admite `-ExpectedSha256` (el hash del
  JAR ya extraído), reemplaza solo `gripeverde-*.jar` y avisa o aparta cualquier archivo que
  no sea `.jar`. Con el ZIP sin abrir, el hash a contrastar es el del ZIP:
  `c256392a24ede9ab129dcc56064776925d167bcbb7ecad62aea2718882e9c37f`.
- La sesión actual corre en un entorno Linux remoto: `D:\...` no existe como ruta
  montada, así que la instalación queda pendiente de que la carpeta sea accesible.
- El validador local `python3 tools/verify_mirrors.py` reproduce los sellos de Gradle
  (idiomas, sonidos, código muerto, versión, duraciones, subtítulos, uso de sonidos,
  literales y balance) y pasa en verde con `0.173.0`.

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
2. **Instalar `0.173.0`** en la carpeta de mods cuando el equipo del usuario sea accesible:
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

Confirmar con el usuario si autoriza corregir CopyL en `Santi-PdR/Lclient` (doble pasada de
texto y fuente huérfana), instalar `0.173.0` cuando la carpeta de mods sea accesible,
actualizar la descripción del PR #90 y recoger su revisión visual del nuevo aviso antes de
seguir con la siguiente tanda.
