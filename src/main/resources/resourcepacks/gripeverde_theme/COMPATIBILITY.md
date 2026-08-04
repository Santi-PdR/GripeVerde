# Eternal Craft — Silver Steel (resource pack)

Documento técnico del resource pack integrado de interfaces de contenedor.

**Version base:** Minecraft 1.20.1 (Forge 47.x) — `pack_format 15`
**Ubicacion:** `resourcepacks/gripeverde_theme/`
**Estado:** reemplazo completo del prototipo anterior (descartado por no cumplir
la direccion artistica). Las 20 texturas fueron reconstruidas desde cero sobre
las texturas vanilla originales de 1.20.1.

---

## 1. Garantia de compatibilidad (regla de oro)

> **No se movio ni un solo pixel funcional.** Todo lo que el codigo del juego
> o de mods blit desde estas texturas sigue estando en la posicion exacta.

Garantias por construccion, verificadas pixel a pixel contra el jar vanilla:

| Garantia | Metodo |
|---|---|
| Dimensiones identicas | 256x256 (villager2: 512x256), igual que vanilla |
| Canal alfa identico | cada pixel con alfa < 255 en vanilla se copia verbatim |
| Iconos funcionales identicos | regiones bliteadas por codigo (llama, flechas, burbujas, check de baliza, libro de encantamientos, iconos de patrones del telar, recetas del cortapiedra, espada de herreria, grieta de dano del yunque, marcadores rojos, chispas, mapa de cartografia, muelle de la afiladora) se copian VERBATIM desde vanilla |
| Geometria de slots identica | los pixeles oscuros/blancos estructurales de vanilla (marcos de slot, L de sombra, brillos) se recolorean a acero **en las mismas coordenadas exactas** |
| Transparencias parciales | anti-alias de bordes: preservado verbatim (nunca se fuerza alfa 255) |

Script de verificacion (usado en el desarrollo): compara alfa, iconos,
estructura oscura/blanca y audita la paleta (sin marrones, sin saturados).

## 2. Compatibilidad con mods

El pack **solo reemplaza texturas** (`assets/minecraft/textures/gui/container/*.png`).
No toca codigo, layouts de menus, ni registros. Consecuencias:

- **JEI / EMI**: dibujan sus paneles y overlays por separado sobre la GUI; al no
  cambiar posiciones ni transparencias, sus recetas, tooltips y highlights se
  dibujan exactamente donde vanilla. Compatible.
- **Curios**: usa sus propias texturas de slots (no las de contenedores) y
  renderiza en la pantalla de inventario por su cuenta. El panel de fondo del
  inventario (steel) se ve detras de sus slots sin conflictos. Compatible.
- **Backpacked / Backpacks / Sophisticated Backpacks**: usan GUIs propias con
  texturas propias. Ninguna textura vanilla de contenedores se modifica en
  posicion, por lo que cualquier GUI de mod que reutilice `generic_54.png`,
  `inventory.png`, etc. hereda el look steel **sin romperse** (los slots siguen
  en las mismas coordenadas; los items renderizan encima igual que en vanilla).
- **Cualquier mod que blitee sub-rectangulos de estas texturas** (ej. flechas
  de progreso de hornos custom, barriles, cofres de mods): las regiones
  funcionales estan byte-identicas a vanilla, asi que su blit funciona igual.
- **Mods de "slot highlight" / tintas / overlays**: operan sobre las
  coordenadas de los slots del menu, no sobre la textura. Sin cambios.

## 3. Decisiones de diseno (por que se ve asi)

- **Material**: acero gris plateado frio (hue ~210, saturacion < 8%), grano
  cepillado fino no repetitivo (ruido determinista por textura), biseles de
  1px, juntas de placa, remaches y tornillos.
- **Slots**: interiores ligeramente rehundidos (acero oscuro `#646E7B`) con el
  marco oscuro y el brillo interior en las posiciones vanilla exactas.
- **Identidad por bloque** (detalles grabados unicos, misma coleccion):
  - inventario: acero limpio, remaches, junta entre zonas
  - mesa de crafteo: grano horizontal, escuadras en L, regla grabada
  - cofre (generic_54): linea de etiqueta, juntas de seccion, remaches
  - horno: caja de fuego rehundida, ascua naranja mate, rejillas verticales
  - ahumador: persianas horizontales de extraccion, linea de chimenea
  - alto horno: rejillas en cheuron, pernos grandes
  - soporte de pociones: frasco alquimico grabado con acento teal apagado
  - mesa de encantamientos: librero con tinte amatista apagado, destellos
  - yunque: remaches pesados (la grieta roja de dano se preserva vanilla)
  - afiladora: icono vanilla preservado, ranuras rehundidas
  - herreria: diamante grabado (la espada vanilla se preserva)
  - cortapiedra: pozo de salida rehundido, sierra grabada
  - baliza: rombo con acento dorado palido, estructura oscura vanilla
  - telar: pozo de vista previa, hilos de urdimbre grabados
  - cartografia: rosa de los vientos (el mapa de vista previa conserva su
    pergamino vanilla: es un mapa, no metal)
  - caballeriza: herradura grabada, bahia de ranuras rehundida
  - shulker: costuras diagonales de caparazon, diamante central
  - aldeano: moneda grabada con acento dorado palido
- **Elementos vanilla preservados a proposito**: llama y flechas de progreso,
  burbujas de pociones, check de la baliza, libro de encantamientos, patrones
  del telar, recetas del cortapiedra, mapa de cartografia, grieta de dano del
  yunque. Son iconos funcionales o artefactos con identidad propia; tocarlos
  romperia la sensacion "oficial".

## 4. Cambios respecto al prototipo anterior

- **Todas las texturas fueron descartadas y regeneradas** desde vanilla.
- Eliminado `dropper.png`: **vanilla 1.20.1 no tiene esa textura** (el dropper
  reutiliza `dispenser.png`). El archivo del prototipo era un asset muerto.
  La tarea `verifyGripeVerdeThemeAssets` de Gradle fue actualizada.
- `pack.mcmeta` y `pack.png` renovados; nombre del pack: "Eternal Craft - Silver Steel".
- Color de titulos de contenedores (ContainerOverlayHandler) ajustado a acero
  frio para coherencia visual (unico cambio de codigo; solo apariencia).

## 5. Limitaciones conocidas

- El build completo (`gradlew build`) requiere acceso a maven.minecraftforge.net;
  en entornos sin red el jar no puede ensamblarse (las texturas y el codigo
  estan listos; validar con build local).
- La auditoria reporta algunos pixeles "saturados" que corresponden a iconos
  vanilla preservados o a los acentos deliberados (ascua, amatista, dorado
  palido, teal) — todos en cantidades minimas y muy desaturados.
