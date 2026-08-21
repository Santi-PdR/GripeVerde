# Microdetalles — Polish Extremo (100+)

Lista auditada de pequeños detalles que separan bueno de excelente. Todos aplicados en v0.14.0 extreme polish.

> ⚠️ **DOCUMENTO HISTÓRICO (v0.14.0).** No refleja el estado actual (v0.17.2).
> Válido como catálogo de intención de polish, pero con estas salvedades:
> - La sección **"Contenedores vanilla" (items 101-106)** y cualquier
>   referencia a `ContainerOverlayHandler` / `drawContainerRivets` / rivets
>   dibujados por código son **OBSOLETAS**: esa clase se eliminó en v0.15.1 y
>   los remaches hoy vienen de las texturas del pack `gripeverde_steel`
>   (v0.17.0+), no de overlay por código. La "verificación 64x64" del pack.png
>   también cambió: ahora es 128x128.
> - El resto (botones DiaryButton/GreenFluButton, iconos, scrollbars,
>   pergaminos/sellos, ciudad/atmósfera, menú principal, audio, QA/compat)
>   sigue siendo referencia útil del nivel de detalle perseguido.
> - Estado actual vigente: `CONTEXTO.md` y `KNOWN_ISSUES.md`.

## Papeles y pergaminos (15)
1. Ledger base #E9DDC2 vs Correspondence #EFEBD0 vs Archive #E2CF9E vs Technical #E6DBC0 vs Medical #ECE8C8 vs Official #E8D7B0 — no plantilla única.
2. Fibers densidad por kind: Archive 90, Ledger 70, Technical 50, Correspondence 35, Medical 20.
3. Stains count por kind: Archive 7, Ledger 5, Official 4, Correspondence 3, Medical 1, Technical 2.
4. Edge burnish Archive más oscuro en borde derecho (shelf wear).
5. Bottom tea tint Archive 26 alpha vs Medical 10 vs resto 18.
6. Medical + cross verde faint en watermark + green tea top 8 alpha.
7. Pinholes Archive doble (segundo 18 alpha).
8. Registry notches solo Official/Archive, Official con segunda línea 10 alpha.
9. Binding fibers solo Ledger/Official, Ledger 12 max, Official 8 max.
10. Wax seal color por kind: Official #7A1F18 rojo profundo, Archive #6B3D1A marrón, Ledger #8C5A2E bronce.
11. Micro ink dot 28% chance cerca margen derecho, 32-60 alpha, 1px + segundo 18 alpha aleatorio.
12. Correspondence sin torn edges ni micro dot (carta limpia).
13. Technical grid vertical faint cada 32px, 8 alpha.
14. Correspondence ruling azul #6B9AC8 18 alpha.
15. Vignette alpha por kind: Archive 62 top, Correspondence 52/16.

## Botones DiaryButton (18)
16. WearSeed per-instance `(x*31+y*17+w*13+h*7 ^ hash)` → tintShift -3..+3 para no plantilla.
17. Disabled no shadow, flat `D8CAB0 210 alpha`.
18. Disabled crosshatch diagonal cada 8px, 22 alpha.
19. Pressed sombra 48 alpha vs normal 90/38 double layer.
20. Pressed bg `A08254` vs hover `B89A6E` vs base `PAPER_STAINED` con shift.
21. Highlight superior `FFEEDD 84 alpha` 1px.
22. Inner shadows: top 58 normal, 88 pressed; bottom 88 normal, 118 pressed; extra bevel +1px top 68 alpha en pressed.
23. Double inner border solo PRIMARY con accent 70 pressed /45 normal.
24. Pinholes 88 active /42 disabled en 4 esquinas.
25. Wear micro dots 2 por botón, 32% chance, `PAPER_DARK 34 alpha`.
26. Focus ring gold `D0A04A` 1px exterior + soft outer glow 70 alpha 1px más afuera.
27. Text shadow 1px `INK_BLACK 40 alpha` solo active.
28. Underline wavy: for i in tw, yOff = 0/1/0 pattern, accent con t*190 alpha.
29. Ellipsis con `plainSubstrByWidth` reserva `...` width.
30. Hover anim 4.5 speed, eased cubic out.
31. PressTicks 7 ticks (antes 6) visual sink 1px.
32. Border lerp `SEPIA_DARK -> accent`.
33. Icon shadow 1px offset antes de color.

## Botones GreenFluButton (14)
34. WearSeed similar, wear dots 2 con 40% chance.
35. PressTicks 7, y offset 1px sink.
36. Shadow double layer 88/32 alpha, no shadow cuando pressed.
37. Background steel dark #18100C → hover #2E1E18 → pressed #201410.
38. Top brass edge lerp `BRONZE_DARK -> REDCOAT` + inner highlight 22+28*t alpha.
39. Bottom burnish 62 alpha.
40. Hover veil rojo `REDCOAT t*28 alpha`.
41. Rivets 4 esquinas, rivetOff 90 alpha para bottom.
42. Side bar con easing growth `barRed = (1-t)*((h-6)/2)`.
43. Spark `AMBER_FLAME t*165` + secondary 80 alpha.
44. Separator `BRONZE_DARK 118` + screws top/bottom 60 alpha.
45. Icon shadow 150 alpha, icon color lerp `SEPIA_LIGHT -> AMBER_FLAME`.
46. Title shadow 1px 70 alpha, title lerp `BONE_WHITE -> PAPER`, subtitle `SEPIA_LIGHT -> OCHRE`.
47. Bottom micro line steel wear 54 alpha.

## Iconos (10)
48. Hand: palma 4x5, dedo 9-12, punta 11-12, muñeca.
49. Scroll: rollos izq/der 2px, cuerpo central 180 alpha, líneas texto 4-5 y 6-7.
50. Chart: 4 barras alturas distintas + base 9-10.
51. Gear: cuerpo 3-9, dientes 4 lados, hueco central PAPER.
52. Door: cuerpo 2-10, arco 3-9 + 4-8, interior 100 alpha, pomo 8-9.
53. Language: caja 1-11, líneas 3-4 y 5-6 y 7-8 con 180 alpha tercera.
54. Chat: burbuja + cola 3-5/8-10, líneas PAPER.
55. Pack: caja 2-10, sombra 130 alpha, línea PAPER 4-8.
56. Access: cruz hombre con brazos 3-5/7-9 en 8-10.
57. Online: cruz ejes con highlight 120 alpha.

## Scrollbars y listas (8)
58. Thumb min 28 (era 20) max trackH-8, formula `trackH*trackH/(trackH+max*1.2)`.
59. Thumb steel `6B4A2E BB` + left highlight `A67A5A D0` + right dark `2E1E12 70` + top `8E6A35 D0` + bottom `201E05 70`.
60. Centro lozenge parchment + rivets top/bottom 48/60 alpha.
61. Track `F0E5C8` + inner line `422810 30` 2px inset.
62. Tapado barra vanilla condicional `vanillaBarX < panelRightEdge-4` evita columna fantasma.
63. Scroll sound throttle: 60ms scroll, 200ms changelog, 120ms section scroll.
64. SmoothScroll easing 0.42 snap 0.30, clampa target si lista clamp.

## Pergamino y sellos (10)
65. Coffee stain solo si kind!=MEDICAL, Medical usa disinfectant green 22 alpha.
66. Wax seal highlight `FFAA88 70` en -2,-3.
67. Torn edges notches 2 Archive 3, 78/92 alpha.
68. VignetteFast 4 gradients: top `62/20` archive vs `52/20` resto.
69. Watermark EC 10px s=10, Official 14 alpha, Archive 18 alpha, Medical + cross 10 alpha.
70. Micro imperfection near margin right.
71. Bloody 2+rand2 splatters, fast circle.
72. SoftBlob 2 circles (a, a/3) vs per-pixel sqrt.
73. FillCircleFast scanline sqrt.
74. Fibers optimized `w*h/1600+base/2`.

## Ciudad y atmósfera (15)
75. Film grain 48 dots regen 90ms, alpha 3-12, gris 8-25.
76. Dust motes 18 floating 0.0006-0.0018 speed, sin rot, alpha sin wave.
77. Factory glow 16*breath alpha 0.22Hz (era static), top 60% h, bot h/18 + second band 1/40.
78. Moon phase O(r) intervals, not pixel.
79. Halo solo >40% illumination.
80. Star cache per w/h, 60 total, 15 bright 2x2 + edge 1px 1/3 alpha.
81. Shooting star trail 12 len, message 3s + sound scroll 0.15 vol.
82. Birds 4, wing sin*2, count 3-4, spacing 0.035, start -0.1/1.1.
83. Clouds 5 with initCloudSpeeds +-30% variation.
84. FogLow 3 bands breathing 0.3Hz +-25% breath, offset %w -w + bandW*2 fill.
85. StreetLevel groundFog 3 capas vs 5, w*1.7 vs w*3, pulse 0.28+0.09*layer.
86. Lantern halos 2 fills vs 12, haloR 28*flicker, floorR 48*flicker.
87. Branch shadows alpha 34+wind*18, sway sin* (5+wind*5).
88. Moths 9 angle phase, radius 5-13, alpha 35-170 sin 4Hz.
89. Spores 15, pulse 0.2+0.8, alpha yPct*2.

## Menú principal (10)
90. Logo shadow behind skull 70 alpha  -9..+10 / +2..+30.
91. Logo title shadow 1px 120 alpha, subtitle 90 alpha.
92. Year plaque strip `PAPER_DARK 38` padded 10px + blot 55 alpha.
93. Glass patina 26 points + 6 streaks, alpha sin*(7).
94. Posters left maxPosterH = leftCol.h - totalBtnH - GAP*2, min 140, H 260 max.
95. PosterRight topReserved WATCH*2+30+GAP, botReserved BTN_SLIM+GAP*2.
96. PocketWatch swing sin 6px, chain longChain linkCount totalLen/4, brass cap.
97. Glass shine diagonal 5 strips alpha 70-14, thickness 2px, offset bandCenter -1.1..1.1.
98. Easter egg box 220-330 clamped width-96, safeTop 76 minimal else 166, safeBottom buttonTop-16, typewriter 22ms/char, dot pulse sin 120ms, cursor green.
99. Day message cache per date+lang, month bonus Oct/Dec/Jan, dow Mon-Sun.
100. Skip music button tooltip active `Change piece` vs `gramophone turning`.

## Contenedores vanilla (6)
101. Rivets 4 corners `6B4A2E` + highlight `C89A68` 1px, per container.
102. Wear scratch `1A000000` 22-36, 4-5 y.
103. Ender chest motes 7 with size 1/2 + glow 88 alpha.
104. Enchant text shadow `120B20` + text `C8B7F0` + level `91FF42`.
105. Sparkles 10 points tick/180, skip if tick%4, y jitter tick%3-1.
106. Label shadow true, TEXT_LIGHT `ECE6D2`, inventory special 97,8.

## Audio (5)
107. Toggle distinct sound 0.92 vol 1.08+0.06 pitch.
108. Type 0.055 vol 0.92+0.12 pitch paper tap.
109. Hover_main 0.045 vol 0.95+0.10 pitch, hover_section 0.035 0.88+0.08.
110. Cooldowns: hover_main 180, hover_section 220, button_main 90, button_section 120, back 140, scroll 60, notif 400.
111. Ducking 35% 2.5s on RARE bell/thunder/crows/chain/whispers/cough.

## QA / Compat (6)
112. Panel clamp `Math.min(PANEL_MAX_W, width-40)` for all Diary.
113. Minimal mode threshold 290 oculta decorativos.
114. Ellipsis with `plainSubstrByWidth` para ES largos (20+ locale).
115. SupportsScreen filter `inventory.*` only, no modded backpacks.
116. Tooltip palette `EFE0C8/D4A98A` bg + `7A5A32/3D2310` border for legibilidad.
117. SmoothScroll attach snapToCurrent to avoid jump on init.
118. ...

Total 118 microdetalles auditados / implementados.
