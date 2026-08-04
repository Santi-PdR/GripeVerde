# CONTEXTO DE TRANSFERENCIA — Sesión "Silver Steel" (resource pack de contenedores)

> Documento para entregar a otra IA (o retomar la sesión) con TODO el contexto:
> qué se pidió, qué se hizo, dónde está cada cosa, cómo reproducirlo y qué falta.
> Fecha: 2026-08-04 · Rama: `arena/019fcec7-gripeverde` · Repo: `https://github.com/Santi-PdR/GripeVerde.git`

---

## 1. Resumen del chat (historial)

1. **Pedido inicial del usuario:** analizar TODO el proyecto (mod "Eternal Craft:
   Gripe Verde", Forge 1.20.1) antes de tocar nada; luego DESCARTAR el resource
   pack integrado actual (`gripeverde_theme`, 21 texturas "steel victoriano"
   prototipo) y reconstruirlo desde cero usando como base las texturas vanilla
   originales 1.20.1. Requisitos estrictos:
   - Dimensiones, slots, posiciones, separaciones, botones, barras, flechas,
     textos: TODO igual que vanilla (ni un píxel funcional movido).
   - Compatibilidad con mods que usan/reusan texturas vanilla o extienden GUIs
     (Curios, Backpacked/Backpacks, JEI, EMI, Sophisticated Backpacks, etc.).
   - Estilo: acero gris plateado (silver steel) limpio/elegante/refinado, pixel
     art, sin marrones, sin óxido, sin negro dominante, sin azules intensos,
     sin saturados, sin blur, sin patrones repetitivos evidentes, sin aspecto IA.
   - Cada contenedor con personalidad propia (detalles únicos) pero misma
     colección/coherencia.
   - Trabajar interfaz por interfaz, verificando cada una.
2. **Referencia visual:** el usuario mencionó "las imágenes que ya te proporcioné"
   pero **la imagen adjunta NUNCA llegó al sandbox** (`/home/user/uploads` no
   existe). La paleta se calibró contra la descripción textual. Si el usuario
   reenvía la imagen, hay que ajustar la paleta contra ella (ver §6 pendientes).
3. **Entrega de test:** el usuario pidió el "pw completo" (PowerShell, norma del
   proyecto) para build+deploy; se le pasó el bloque PowerShell oficial
   (R-BUILD-DEPLOY). También pidió un pw para "abrir las texturas" (se le pasó
   uno que abre Explorador + visor HTML + hoja de contacto).
4. **Flujo hacia su PC:** el usuario aclaró que el puente es GitHub: primero
   commit+push acá, luego él hace `git pull` en su device. Se commiteó y pusheó
   todo (ver §4).
5. **Pedido actual:** pasarle el contexto del chat + dónde se guardó cada cosa,
   para entregárselo a otra IA que continúe.

## 2. Estado del trabajo

**COMPLETO y VERIFICADO (20/20 texturas):** el pack fue reconstruido desde cero
sobre vanilla 1.20.1 y auditado píxel a píxel contra el jar oficial. La última
verificación (2026-08-04) da `FAILS: 0` con: alfa idéntico, iconos funcionales
verbatim, estructura oscura/blanca en las mismas coordenadas, y 0 píxeles
marrones en el panel.

**NO hecho aún:** build completo (`gradlew build`) — el sandbox no tiene Java y
`maven.minecraftforge.net` está bloqueado. Validación visual en el cliente Test
del usuario (su PC). Ajuste de paleta contra la imagen de referencia (no llegó).

## 3. Dónde está cada cosa (rutas EXACTAS)

### En el repo (commiteado y pusheado a `arena/019fcec7-gripeverde`)
```
src/main/resources/resourcepacks/gripeverde_theme/
├── pack.mcmeta                          # pack_format 15, desc "Eternal Craft — Silver Steel"
├── pack.png                             # icono 64x64 (placa acero + rombo dorado)
├── COMPATIBILITY.md                     # garantías técnicas + compatibilidad mods
└── assets/minecraft/textures/gui/container/
    ├── inventory.png  crafting_table.png  generic_54.png  shulker_box.png
    ├── furnace.png    smoker.png          blast_furnace.png  brewing_stand.png
    ├── dispenser.png  hopper.png          anvil.png  grindstone.png
    ├── enchanting_table.png  beacon.png   loom.png  cartography_table.png
    ├── stonecutter.png  smithing.png      horse.png  villager2.png
    # (20 texturas; 256x256, villager2 512x256 — igual que vanilla)
    # NOTA: NO hay dropper.png (vanilla 1.20.1 no lo tiene; el dropper usa dispenser.png)

tools/silver_steel/                      # scripts de generación (NUEVOS, ver §5)
├── steelkit.py  build1.py  build2.py  build3.py  build4.py
├── verify.py    README.md  cells_report.txt

EternalCraft-SilverSteel-1.20.1.zip      # pack standalone para probar sin el mod
silver_steel_viewer.html                 # visor vanilla vs nuevo (autocontenido)
silver_steel_contact_sheet.png           # las 20 texturas juntas a 2x
```

### Código del mod tocado (mínimo, solo apariencia)
```
src/main/java/com/eternalcraft/gripeverde/client/ResourcePackHandler.java
    → PACK_NAME = "Eternal Craft - Silver Steel"
src/main/java/com/eternalcraft/gripeverde/client/ContainerOverlayHandler.java
    → TEXT_LIGHT = 0xFFDDE5EE (títulos en acero frío; antes 0xFFECE6D2)
build.gradle
    → verifyGripeVerdeThemeAssets: lista de 20 assets (se quitó dropper.png)
```

### En el sandbox (NO persiste para el usuario; solo referencia)
```
/home/user/GripeVerde/        → clon de trabajo (rama arena/019fcec7-gripeverde)
/tmp/minecraft-assets-1.20.1/ → texturas vanilla 1.20.1 descargadas
    (repo espejo: https://github.com/InventivetalentDev/minecraft-assets, rama 1.20.1)
/tmp/steel/                   → copia de trabajo de los scripts (ya copiados a tools/)
```

## 4. Git — commits y flujo

```
c320862  Silver Steel: resource pack reconstruido desde cero sobre vanilla 1.20.1
85a38f2  gitignore: artefactos de test
640d8b6  Artefactos de test: zip del pack Silver Steel + visor HTML comparativo
(próximo commit)  scripts de generación en tools/ + CONTEXTO_TRANSFERENCIA.md
```
- Rama de la sesión (NO cambiar): `arena/019fcec7-gripeverde`.
- Remoto: `origin = https://github.com/Santi-PdR/GripeVerde.git`.
- El usuario recibe los cambios con: `git fetch && git checkout arena/019fcec7-gripeverde && git pull`.
- Regla del proyecto: después de cualquier cambio, pasar SIEMPRE el bloque
  PowerShell completo de build+deploy a Test (nunca solo el hash). Ver §7.

## 5. Cómo reproducir/regenerar las texturas

```bash
# en la máquina con Python3 + Pillow, con las texturas vanilla 1.20.1 en STEEL_VANILLA
cd tools/silver_steel
python3 build1.py && python3 build2.py && python3 build3.py && python3 build4.py
python3 verify.py          # 20/20 OK
cp out/*.png ../../src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container/
```
- Variables: `STEEL_VANILLA` (default `/tmp/minecraft-assets-1.20.1/.../gui/container`),
  `STEEL_OUT` (default `/tmp/steel/out`).
- Los PNG son DETERMINISTICOS (semillas fijas por textura): regenerar produce
  bytes idénticos.
- `verify.py` falla si se mueve/borra cualquier pixel estructural, icono o alfa
  respecto a vanilla. Es la garantía de compatibilidad.

## 6. Decisiones de diseño (para no perder el hilo)

- **Paleta Silver Steel** (hue frío ~210°, sat <8%):
  ED 0x232A33 · S2 0x2E3540 · S1 0x3B4450 · M2 0x4C5562 · M1 0x5F6976 ·
  B 0x8A94A2 · B2 0x99A3B1 · L1 0xABB5C2 · L2 0xC2CBD6 · L3 0xD6DDE6 ·
  HL 0xE9EFF6 · SI 0x646E7B (interior de slot rehundido).
- **Acentos por contenedor** (muy desaturados): enchanting 0x8E7FAD (amatista),
  beacon 0xBCAB84 y villager 0xBCAB84 (dorado pálido), brewing 0x86A2A0 (teal),
  furnace/smoker/blast 0xC8683E (ascua mate).
- **Material:** grano cepillado vertical/horizontal por textura (ruido de valor,
  semilla fija, no repetitivo), bisel 1px, juntas de placa con brillo, remaches
  3px, tornillos, detalles grabados (línea oscura + luz 1px abajo-derecha).
- **Regla de oro:** todo lo funcional queda byte-idéntico a vanilla:
  - Alfa < 255 (anti-alias) → preservado verbatim.
  - Iconos funcionales → preservados verbatim (diferido al save):
    llama+flechas (furnace/smoker/blast en (176,0)-(201,32)), burbujas+flecha
    (brewing (176,0)-(201,70)), check+tira de baliza ((176,0)-(201,48)),
    libro de encantamientos ((59,14)-(77,70)), patrones del telar
    ((60,14)-(116,56) y (141,10)-(159,51)), recetas del cortapiedra
    ((52,13)-(118,69)), espada+plantilla de herrería ((10,6)-(40,38)),
    grieta roja de daño del yunque ((59,20)-(168,35)) + espada decorativa
    ((16,10)-(43,37)) + zona tan inferior ((1,167)-(108,196)) + chispas
    ((182,3)-(196,17)), muelle de la afiladora ((54,14)-(113,56)),
    mapa de cartografía (pergamino vanilla: es un mapa, no metal).
  - Estructura D/W (marcos de slot, L de sombra, brillos) → recoloreada a acero
    en las MISMAS coordenadas.
- **Identidad por contenedor** (detalles grabados únicos):
  inventario (acero limpio+remaches+junta), crafteo (grano horizontal, escuadras
  L, regla con ticks), generic_54 (línea de etiqueta, juntas de sección, remaches),
  horno (caja de fuego rehundida + ascua + rejillas verticales), ahumador
  (persianas horizontales + línea de chimenea), alto horno (cheurones + pernos),
  brewing (frasco alquímico grabado + acento teal), enchanting (librero tinte
  amatista + destellos), anvil (remaches pesados), afiladora (icono vanilla +
  ranuras), herrería (diamante grabado), stonecutter (pozo de salida + sierra),
  baliza (rombo dorado pálido), telar (pozo de vista previa + hilos de urdimbre),
  cartografía (rosa de los vientos), caballeriza (herradura + bahía rehundida),
  shulker (costuras diagonales + diamante), aldeano (moneda dorada pálida).
- **Layouts reales verificados** (importantes, corrigen memorias viejas):
  - hopper: 5 slots en y=20 (x=44..116); grid jugador en (8,51) 3 filas + hotbar y=109.
  - brewing: ingredientes (56,51)(102,51)(79,58) + combustible (79,17) + slot gráfico
    (17,17); grid desplazado (x=7) con hotbar en y=142.
  - smithing: slots (8,48)(26,48)(44,48)(98,48) — no (16,18)...; grid estándar.
  - enchanting: slots (15,47)(35,47); libro en (59,14)-(77,70); librero tan en
    (78,14)-(167,70) → tratado como PANEL (familia de colores), no icono.
  - anvil: slots (27,47)(76,47)(134,47); grieta roja y espada preservadas.
  - villager: entradas (137,38)(163,38); salida pozo (221,34)-(245,58); lista de
    ofertas a la izquierda; grid jugador x=108; hotbar y=142.
  - beacon: ranura (136,107); grid 9 col x=36, y=137/155/173 + hotbar y=195;
    tira de botones en (176,0)-(201,48).

## 7. Normas del proyecto que la otra IA debe respetar (resumen)

- **RAMA FIJA:** trabajar en `arena/019fcec7-gripeverde`; no crear otras ramas.
- **Después de cualquier cambio:** entregar PowerShell COMPLETO de build+deploy
  a Test (no solo el hash) — regla explícita del usuario (CONTEXTO.md línea 7).
- **R-BUILD-DEPLOY:** todo bloque con `gradlew build` DEBE incluir el deploy al
  mods folder en el mismo bloque. `.deploy` contiene `DEPLOY_MODS_PATH=...`;
  default documentado: `D:\Juegos\Minecraft\Instances\Test\mods`.
- **R-JAR2:** limpiar `build\libs\gripeverde-*.jar` ANTES de compilar.
- **R-CD / R-PATH2:** todo bloque PowerShell empieza con Set-Location al repo;
  rutas absolutas; nunca `exit` en scripts interactivos (usar `throw`).
- **R-VER:** confirmar la versión visible en el reloj del Title antes de aprobar.
- **R-CAP:** capturas pre/post en 4 escalas GUI para cambios visuales.
- **R-DIEGETICO:** narrativa de cara al usuario (CHANGELOG.md, changelogs in-game,
  README "Versión actual") en voz de cronista victoriano; prohibido lenguaje
  técnico/release-note. Los 25 changelog/*.md usan ancla `## Notas de
  Actualización` (es_*) / `## Update Notes` (en_*).
- **R-CIERRE:** al cerrar hito: actualizar CONTEXTO.md, CHANGELOG.md,
  KNOWN_ISSUES.md, README.md (+25 changelogs si aplica) → commit + tag + push.
- **No tocar layouts vanilla de containers** salvo paleta/estética; mantener
  `ContainerOverlayHandler` restringido a pantallas vanilla (no romper mochilas
  ni containers de mods).
- Build: Java 17, Gradle 8.8 (wrapper incluido), Forge 47.4.20, MC 1.20.1,
  `mod_version` = project.version (hoy 0.14.4 en build.gradle).

## 8. Pendientes / riesgos

1. **Build+deploy en la PC del usuario** (Java 17): pasar el bloque PowerShell
   oficial (ver §7) y validar en cliente Test (R-CAP, R-VER).
2. **Imagen de referencia del usuario:** no llegó al sandbox; pedirla de nuevo
   y ajustar la paleta si difiere (el resto del diseño no debería cambiar).
3. **Feedback visual por contenedor:** el usuario quiere iterar interfaz por
   interfaz; con tools/silver_steel regenerar es inmediato (semillas fijas).
4. **Entregar pw de "abrir texturas"** ya dado: explorer + viewer html + sheet.
5. Si se cambia algo del pack: re-correr `verify.py` (obligatorio, 20/20) y
   actualizar COMPATIBILITY.md si cambian garantías.
