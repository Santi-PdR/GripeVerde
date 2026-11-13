# Contexto de continuidad: GripeVerde ModMenu

Actualizado: 2026-11-13

## Proyecto y reglas de trabajo

- Repositorio: [Santi-PdR/GripeVerde](https://github.com/Santi-PdR/GripeVerde)
- Rama de trabajo: `codex/v0.104-ghoul-warning-poster`
- Pull request: [#90](https://github.com/Santi-PdR/GripeVerde/pull/90)
- Versión de esta rama: `0.172.0`
- El usuario quiere seguir mejorando el mod de forma continua hasta que diga que pare.
- Trabajar el código directamente en GitHub mediante GitHub MCP. No clonar ni editar el código fuente localmente.
- No controlar Minecraft ni la PC para hacer pruebas visuales. El usuario pidió explícitamente “no controles PC”.
- Mantener las interfaces del juego instantáneas y quietas: no añadir animaciones al inventario, pausa o mundo que afecten el gameplay.
- Al entregar una versión, cambiar la versión y generar el JAR. El usuario indicó que los JAR finales deben quedar en `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`, sin dejar versiones anteriores de GripeVerde.
- En esa carpeta deben quedar únicamente archivos JAR. Conservar los demás mods; reemplazar solo el JAR antiguo de GripeVerde.
- El estado de esta tarea estaba pausado por pedido del usuario; ya pidió un contexto para continuar con otra IA.

## Último estado confirmado

- Commit de código: `34639471ae4fbd10c14d9d09d78bbecb39e09ee9`
- GitHub Actions run `40763347401`, workflow Build GripeVerde, terminó en `success`.
- Build, validación de recursos/localización y subida del JAR: correctos.
- Artefacto: `GripeVerde-jar`, artifact ID `12849186641`, tamaño 8,742,493 bytes, SHA-256 declarado por GitHub: `9520432c3027a3695b4e1246c2f8a3298cdc8ce11ab0cbff517d6fc914508c60`.
- El JAR de `0.172.0` **no ha sido instalado todavía** en la instancia del usuario. Debe descargarse desde el artefacto de Actions, validar su contenido y checksum, y reemplazar el JAR previo de GripeVerde en la carpeta indicada.
- La descripción del PR todavía reflejaba principalmente `0.171.0` la última vez que se leyó; actualizarla si se retoma.

## Cambios de 0.172.0

- Se añadieron las 25 variantes regionales de inglés y español al metadata de idioma del resource pack para que Minecraft pueda ofrecerlas, incluyendo `es_uy` (Español de Uruguay).
- La comprobación de localización ahora verifica que los 25 idiomas esperados estén declarados. Los archivos de traducción y crónica ya cubren esos mismos 25 locales.
- La portada incorpora un retrato geométrico estático de un Ghoul en el aviso de cuarentena, solo en hojas grandes. Se intentó dejarlo apartado del sello de tinta.
- La versión del mod y Gradle se actualizaron a `0.172.0`.
- El aviso y sus adornos son estáticos y solo se dibujan en la pantalla principal; no agregan animaciones a mundos o inventarios.

## Pendientes visibles del usuario

1. **CEDA Dispatch / CopyL:** la pantalla de edición de mensajes muestra textos duplicados y superpuestos (“Mensaje 1” y el contenido parecen dibujarse dos veces). Otra captura mostró caracteres de cuadros vacíos. El usuario la considera fea y pidió corregir diseño y tipografía. La fuente del problema puede ser CopyL o su integración; no afirmar que está resuelto.
2. **Aviso CEDA de la pantalla principal:** el usuario lo considera feo y poco acorde al mod; pidió que la entidad se parezca más al Ghoul del mod. La versión actual tiene un retrato pequeño de píxeles, pero todavía no ha sido revisada visualmente por el usuario.
3. **Revisar el PR:** título actual `v0.172: regional languages and a ghoul quarantine notice`; actualizar el cuerpo con los cambios de 0.172 y el resultado del build.
4. **Instalar el artefacto:** descargar y reemplazar únicamente el JAR GripeVerde viejo por `0.172.0`; verificar que no queden JARs antiguos de GripeVerde ni archivos que no sean JAR en la carpeta mods.
5. **Investigar CopyL:** no se localizó el repositorio fuente de CopyL en la búsqueda previa de los repositorios conectados. El JAR local conocido era `copyl-3.9.4.jar`. Buscar de nuevo el repositorio correcto por GitHub MCP o pedir al usuario el enlace si no aparece. No editar el JAR distribuido como sustituto de los fuentes sin evaluar el alcance.

## Contexto de estilo y contenido

El mod se inspira en Ghouls/Left 4 Dead 2 y presenta una epidemia ficticia en el año 1800, ocultada como “Gripe Verde”. Los infectados son personas vivas transformadas por una infección acelerada, con piel azulada y ojos azules brillantes. La interfaz debe sentirse como papeles, avisos y anotaciones de época con señales de cuarentena, CEDA, tinta roja y azul Ghoul. El tono de diarios y crónicas debe sonar natural, como testimonios humanos, no como instrucciones técnicas.

Los idiomas principales son inglés y español, con sus variantes regionales; toda variante `en_*` debe usar inglés y toda variante `es_*` español. No usar otros idiomas.

## Skills consultadas anteriormente

- `minecraft-modding`
- `ui-skills`
- `frontend-design`
- `fix-what-i-pointed-at`

Mantener el consejo de diseño aplicado: interfaces distinguidas y legibles, tipografía vanilla clara cuando las fuentes personalizadas generen tofu, jerarquía visual, sin sombras de texto excesivas, sin solapamientos, y con animación limitada a la portada si acaso. El juego en curso y las pantallas de inventario/pausa deben seguir libres de animaciones añadidas.

## Próximo paso técnico recomendado

Al retomar, confirmar acceso a GitHub y permisos de escritura para la carpeta mods, descargar el artefacto `12849186641`, instalar/verificar `0.172.0`, actualizar la descripción del PR, y luego investigar/arreglar el panel CopyL y rediseñar el aviso CEDA/Ghoul en una siguiente versión.