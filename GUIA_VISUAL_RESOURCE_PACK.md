# Sistema de Diseño y Guía Visual Definitiva: Master Steel
**Eternal Craft: Gripe Verde — Minecraft Forge 1.20.1**

---

## 1. Filosofía & Visión General (El Pensamiento Mojang)

El objetivo fundamental de este Resource Pack es alcanzar el estándar de una **edición oficial alternativa de Minecraft**: un universo donde las interfaces fueron concebidas como herramientas de acero industrial limpio, sobrio y ligero.

### Los Tres Principios Inmutables
1. **Espacio Vacío Intencional:** Grandes áreas de chapa lisa y despejada para que la atención del jugador esté al 100% en los **ítems y el inventario**.
2. **Contraste Suave y Descansado:** Los biseles y relieves tienen diferencias lumínicas mínimas (12–15%), evitando contrastes duros que fatiguen la vista en sesiones prolongadas.
3. **Lectura Visual Inmediata:** Un jugador debe identificar en menos de 100 milisegundos qué es un slot, dónde está la salida y cuál es el estado de una máquina.

---

## 2. Paleta Relacional Matemática (Sin Colores Absolutos Rígidos)

Para garantizar consistencia y permitir ajustes globales de luminancia sin romper la armonía entre componentes, todos los valores se definen a partir de **relaciones matemáticas** respecto a la base del acero:

```
┌─────────────────────────┬───────────────────────────────┬────────────────────────────────────────────────────────┐
│ Componente              │ Fórmula Relacional            │ Valor Calibrado de Referencia                          │
├─────────────────────────┼───────────────────────────────┼────────────────────────────────────────────────────────┤
│ Steel Base (B)          │ Punto de partida (Luminoso)   │ #909398  (RGB: 144, 147, 152 | V: 59.6% | Sat: 5.3%)   │
│ Steel Highlight         │ B + 12% de luminancia         │ #ACB0B4  (RGB: 172, 175, 180 | Bisel superior/izq.)    │
│ Steel Shadow            │ B - 14% de luminancia         │ #707378  (RGB: 112, 115, 120 | Bisel inferior/der.)    │
│ Outer Iron Border       │ B - 45% de luminancia         │ #24262A  (RGB:  36,  38,  42 | Contorno 1px silueta)   │
│ Slot Base (S)           │ B - 22% de luminancia         │ #606368  (RGB:  96,  99, 104 | Fondo pozo 16x16 px)    │
│ Slot Shadow             │ S - 16% de luminancia         │ #404348  (RGB:  64,  67,  72 | Sombra interior 1px)    │
│ Slot Highlight          │ S + 18% de luminancia         │ #888B90  (RGB: 136, 139, 144 | Luz interior 1px)       │
│ Bronze Accent (Mecán.)  │ Hue: 42°, Sat: 55%, Val: 78%  │ #CCA858  (RGB: 204, 168,  88 | Flechas y medidores)    │
│ Bronze Shadow           │ Bronze - 35% de luminancia    │ #645024  (RGB: 100,  80,  36 | Sombra de flecha 1px)   │
│ Bronze Specular         │ Bronze + 15% de luminancia    │ #EAC674  (RGB: 234, 198, 116 | Brillo de flecha 1px)   │
└─────────────────────────┴───────────────────────────────┴────────────────────────────────────────────────────────┘
```

### Justificación de la Luminancia de Base ($B$):
- **Contraste con Texto Vanilla (`#404040`):** Con $B = \text{\#909398}$, el ratio de contraste es de **$3.33 : 1$**, lo que permite que títulos ("Crafting", "Inventory", "Enchant", etc.) se lean con total claridad y ligereza sin requerir sombras de texto forzadas.
- **Tono:** Acero gris neutro con una levísima calidez de fundición industrial ($H = 218^\circ$), nunca azulado, nunca oscuro como hierro fundido viejo, nunca marrón.

---

## 3. Protocolo de Validación del Slot Maestro (Top Priority)

El **Master Slot ($18 \times 18$ px)** es el componente más importante de todo el mod. Debe superar con éxito la prueba de contraste con los **6 ítems de control**:

```
Pruebas de Legibilidad del Master Slot (#606368):
  1. [ ] Slot Vacío:           Fondo 100% limpio, sin sombras dithered ni tramados sucios.
  2. [ ] Con Carbón:           Ítem negro puro (#181818) -> Contraste nítido 3.2:1 contra el fondo.
  3. [ ] Con Netherite:        Ítem gris oscuro metálico -> Silueta claramente distinguible.
  4. [ ] Con Hierro:           Ítem gris claro metálico -> Recorte limpio contra el pozo de 16x16.
  5. [ ] Con Diamante:         Ítem cian brillante -> Resalte limpio sin encandilar.
  6. [ ] Con Cuarzo:           Ítem blanco puro (#FFFFFF) -> Contraste 5.6:1 sin perder bordes.
```

> **Regla de Bloqueo:** Si cualquiera de estos 6 ítems presenta dificultad de lectura visual en el Master Slot, **se detiene el proceso y se recalibra el slot antes de continuar**.

---

## 4. Familia Visual & Reutilización de Componentes (Design System)

Las 21 interfaces no se construyen como 21 archivos independientes, sino como combinaciones estrictas de componentes del **Design System**:

```
                       ┌─────────────────────────┐
                       │      MASTER PANEL       │
                       │ (Chapa lisa B: #909398) │
                       └────────────┬────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌──────────────┐
│ MASTER BORDER│             │ MASTER SLOT  │             │ MASTER ARROW │
│  (1px 45°)   │             │  (18x18 px)  │             │  (#CCA858)   │
└──────────────┘             └──────────────┘             └──────────────┘
```

- `MASTER PANEL`: Placa base uniforme con relieve perimetral sutil de 1px.
- `MASTER SLOT`: Matriz repetitiva de ranuras de $18 \times 18$ px ($9 \times 3$, $9 \times 6$, $3 \times 3$, $2 \times 2$).
- `MASTER BORDER`: Borde exterior de hierro oscuro (`#24262A`) con luz superior (`#ACB0B4`) y sombra inferior (`#707378`).
- `MASTER ARROW`: Indicador de transformación funcional en bronce sobrio (`#CCA858`).

---

## 5. Reglas de Oro Inmutables

### Regla de Consistencia Estricta
> *"Ninguna interfaz puede introducir un nuevo elemento gráfico si dicho elemento no aparece previamente en otra interfaz o no cumple una función mecánica específica."*

### Prohibición Expresa de "Embellecer"
> *"No intentar mejorar una interfaz."*  
> *"No intentar hacerla más bonita."*  
> *"No intentar hacerla más detallada."*  
> *"Solo reinterpretar la interfaz vanilla utilizando el lenguaje visual Master Steel."*

### Reglas Estrictas del Bronce
- **SÍ:** Únicamente para flechas de progreso móvil, agujas de medición y tubos de destilación.
- **NO:** Nunca como marco, nunca como fondo, nunca como adorno.

---

## 6. La Interfaz Maestra (Template de Referencia)

Antes de reconstruir las 21 interfaces, se diseñó la **Master GUI Template** (`tools/master_gui_preview.png`) como banco de pruebas del Design System:

```
┌────────────────────────────────────────────────────────────────────────┐
│  Master Design System Template (176x166 px)                           │
│                                                                        │
│      ┌──┐┌──┐                  ┌──┐                                    │
│      │  ││  │       ──►        │  │  (Output Slot 26x26)               │
│      └──┘└──┘   (#CCA858)      └──┘                                    │
│      ┌──┐┌──┐                                                          │
│      │  ││  │                                                          │
│      └──┘└──┘                                                          │
│                                                                        │
│  ┌──┬──┬──┬──┬──┬──┬──┬──┬──┐                                          │
│  │  │  │  │  │  │  │  │  │  │                                          │
│  ├──┼──┼──┼──┼──┼──┼──┼──┼──┤  MATRIZ MASTER SLOT 9x3                 │
│  │  │  │  │  │  │  │  │  │  │  (#606368 / 1px #404348 / #888B90)      │
│  ├──┼──┼──┼──┼──┼──┼──┼──┼──┤                                          │
│  │  │  │  │  │  │  │  │  │  │                                          │
│  └──┴──┴──┴──┴──┴──┴──┴──┴──┘                                          │
│  ┌──┬──┬──┬──┬──┬──┬──┬──┬──┐                                          │
│  │  │  │  │  │  │  │  │  │  │  HOTBAR MASTER SLOT 9x1                 │
│  └──┴──┴──┴──┴──┴──┴──┴──┴──┘                                          │
└────────────────────────────────────────────────────────────────────────┘
```
