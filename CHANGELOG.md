## v0.288.0 - El reloj de bolsillo se parece a un reloj de verdad

- Esfera grande: marcas de minuto, firma del relojero y "cuerda: llena".
- Reloj pequeno: muesca de las 11:52 (consistencia con la inspeccion).
- Fuera la leyenda de version bajo el bisel de la portada; subrayado del distintivo de musica.

## v0.287.0 - Las listas dicen cual se lleva

- Mundos: `selectedWorldName` en la cabecera (por reflexion sobre `LevelSummary`), oculto si hay filtro.
- Puestos: direccion elegida en la franja del pie + "puerta conocida" via `RecentAddresses`.

## v0.286.0 - El cuaderno y el parte afinan la mano

- Diario: fuera la pista "ctrl+f"; "pulsa /" en su lugar; "recuerda: ..." con la lupa cerrada; contador en rojo sin hallazgos.
- Parte: "N sin tecla" en el pie y nombre de ranura junto al numero (`DispatchLibrary.name`).

## v0.285.0 - Remate de las hojas: cada una dice lo que tiene y como se sale

- Sonido: resumen de musica, ambiente e interfaz; aviso de silencio total; folio II.
- Aspecto: aviso cuando no hay prendas puestas; folio VIII.
- Idioma IV, controles III y creditos IX con folio y salida al pie.

## v0.284.0 - La entrada directa recuerda las ultimas puertas

- Nueva clase `RecentAddresses` (config/gripeverde-recents.txt, 3 direcciones).
- Entrada directa: fichas "recientes:" con su raya, clic que rellena el campo.
- Se anota al entrar, solo si la direccion tiene forma de direccion.

## v0.283.0 - La portada responde a la mano

- Firma: `sigX/sigY/sigW` + subrayado al pasar + clic que abre `DiaryCreditsScreen`.
- Distintivo de musica: `musicBadgeX/Y/W` + clic que abre `DiarySoundOptionsScreen`.
- Geometria tomada de lo dibujado, tambien en la firma compacta.

## v0.282.0 - Las hojas de ajustes dicen lo que tienen puesto

- Chat: `chatSummaryLine` con opacidad, ancho y escala.
- Online: `onlineSummaryLine` con avisos de realms y listado publico.
- Accesibilidad: recuento de ajustes y "Esc vuelve" en el margen.

## v0.281.0 - Repaso de la casa

- Muerte: el renglon de cortesia solo si cabe antes del pie.
- `docs/CONTEXTO_MODMENU.md`: tabla de las 15 tandas y nota del revisor `tools/lint_java.py`.
- Cierre del ciclo de 15 tandas.

## v0.280.0 - La lupa devuelve el lector donde estaba y el cajon dice su folio

- Diario: `scrollBeforeSearch` / `searchWasOpen` devuelven el punto de lectura al cerrar la busqueda.
- Packs: folio "hoja 1" y "Esc vuelve" en el pie, con guarda de ancho (360 px).

## v0.279.0 - El telegrama de deceso dice mas y la ropa suena

- Muerte: causa (`deathCause`), lugar + coordenadas (`deathWhere`), dia, hora y nivel (`deathWhen`); papel de 72 a 92 px.
- Aspecto: `UiSounds.toggle()` al cambiar prenda o mano; `skinModelLine` con brazos finos o anchos.
- CI: `tools/lint_java.py` antes de Gradle (anotaciones huerfanas, variables repetidas, sentencias cortadas).

## v0.278.0 - El arsenal dice sobre que esta montado

- Arsenal: `forgeLine()` bajo el recuento, chapa `REG. XVII`, folio y pista de salida.
- Fix: sentencia suelta en el pie del parte (`count = count + ...`).

## v0.277.0 - El parte se ordena solo y el cuaderno marca el paso

- Parte: foco en la primera ranura vacia al abrir y al cambiar de hoja.
- Parte: contador "quedan N" de la ranura activa en el pie; "faltan N" y aviso de hoja vacia.
- Diario: muesca de ultima lectura, filete de progreso con porcentaje, "fin del cuaderno" con golpe de tapa.
- Fix: `tw`/`tx` duplicados en `ChangelogScreen.render`.

## v0.276.0 - El cuaderno recuerda donde lo dejaste

- Muesca de "aqui lo dejaste" en la barra (`openedAtScrollY`), con etiqueta al pasar.
- Filete de progreso al pie + porcentaje al acercarse (`% leido`).
- "fin del cuaderno" centrado y golpe de tapa una vez por llegada (`endKnocked`).
- Fix de compilacion: `getSeed()` no existe en `ClientLevel`; se usa `getSingleplayerServer()`.

## v0.275.0 - La pausa dice en que esta la partida

- `drawWorldStatus`: hora del mundo, clima, modo, dificultad, almas conectadas y "sin retorno" en hardcore.
- `drawSeedOnHover`: la semilla aparece al acercar la mano a esos renglones.
- Guarda de altura para no pisar los botones en paneles bajos.

## v0.274.0 - El reloj de bolsillo dice mas

- Segundero real en la vista del reloj.
- Muesca roja a las 11:52 en el bisel.
- Columna derecha: hora escrita, dia de la semana y "atrasa 3 min".
- Fix: `@Override` mal puesto en `DiaryControlsScreen.auditKeys` (no compilaba).

## v0.273.0 - La hoja de controles vigila las teclas

- `auditKeys()`: choques y teclas sin asignar, recalculados 20 veces por segundo.
- Aviso en el margen izquierdo, en rojo si hay choques; "sin choques" en tinta suave si no.
- Al pasar el raton, nombre de una tecla en conflicto.
- Clic en el aviso abre `KeyBindsScreen`.

## v0.272.0 - Todas las hojas suenan igual

- `paperSoundDone` + `UiSounds.open()` en 17 hojas (una vez por apertura, no al redimensionar).
- `onClose()` con puerta en `DiaryPauseScreen` y `DiaryPackSelectionScreen`.
- Chapas unicas: aspecto VIII, confirmacion XIV, packs XVI, mundos XVIII, sonido solo II.
- Fix de compilacion: `@Override` duplicado en `DiaryMultiplayerScreen.charTyped`.

## v0.271.0 - La hoja de puestos tambien lleva cuenta

- `serversFileLine`: peso de `servers.dat` y fecha en romanos bajo el recuento.
- `drawEmptyNote` cuando no hay puestos anotados.
- Folio "hoja 1" en la esquina del panel.
- `UiSounds.open()` al abrir y pluma al escribir (`charTyped`).

## v0.270.0 - El archivo dice lo que pesa

- `measureArchiveAsync` + `formatBytes`: peso real de `saves/` en la cabecera.
- `todayStamp`: fecha en romanos junto al peso.
- Filtro activo: "se ven N" y linea "busca: ...".
- `UiSounds.open()` al abrir y `onClose()` con puerta.
- Rueda (`mouseScrolled`), pluma (`charTyped`) y golpe al elegir fila (`mouseClicked`).
- Folio "hoja 1" en el pie.

## v0.269.0 - Menos llaves, mas papel

- Fuera `Ctrl+N`/`Ctrl+E` en puestos, `Ctrl+N`/`Ctrl+S` en mundos, `Ctrl+1..0` en el parte, `Ctrl+F` en el diario y la pista de `Ctrl+V`.
- `Ctrl+S` en la portada sigue abriendo los mundos propios y no se anuncia en ningun sitio.
- Fuera el modo cine con `F1`.
- Reloj: bajo la hora, el dia y si es de dia o de noche (`dayLine()`).
- Mundos propios: `drawEmptyNote` cuando el archivo esta vacio.

## v0.268.0 - Teclas de lista y cabeceras que informan

- Puestos: `Ctrl+N` anade, `Ctrl+E` edita, recordatorio bajo el recuento.
- Mundos: `Ctrl+N` crea y `Ctrl+S` juega (`pressVanilla`), pie nuevo.
- Arsenal: `onClose` con puerta. Diario: la lupa recuerda la ultima busqueda (`lastQuery`).
- Idioma: `currentLanguageName` bajo el recuento.
- Creditos: linea de Minecraft/Forge y la hora del brote.
- Sonido: "suena: N/M" bajo el titulo.
- Portada: `F1` alterna el modo cine.

## v0.267.0 - El sonido y el parte, a mano

- `DiaryMusicSlider` / `DiaryAmbienceSlider`: flechas y RePag/AvPag, aviso de hover, golpe al soltar, etiqueta "en silencio" a cero.
- Hoja de sonido: chapa `REG. VI` y `onClose` con puerta.
- Parte: clic derecho vacia el mensaje (`fieldRowAt`, clave nueva `dispatch.slot.emptied`), "sin tecla" en ranuras libres y pie con el gesto.

## v0.266.0 - Fichas que caben y teclas que faltaban

- Aspecto: rejilla medida contra la hoja, recuento `drawWornCount` y avisos por prenda.
- Pausa: `VOLVER · Esc` en la etiqueta y el aviso.
- Parte de CEDA: chapa `REG. XIII`.
- Arsenal: `setSuggestion` en el buscador y recuento de modificaciones arriba a la derecha.
- Confirmacion: `Enter` firma el si, `Esc` se va.

## v0.265.0 - Lo que tapaba y lo que faltaba

- Multijugador: fuera `drawFooterNotes` y el divisor inferior que pisaban los botones.
- Portada: fuera `drawBottomMarks` (fecha/version al pie); se cruzaba con la firma.
- Arsenal: `paintRowsInk` cubre la fila de Forge y reescribe nombre/version en tinta.
- Portada: `Ctrl+S` vuelve a abrir `DiarySelectWorldScreen` (sin anuncio).

## v0.264.0 - Teclado y puertas

- `UiSounds.back()` con freno de 140 ms y sonido de puerta en 12 hojas que cerraban mudas.
- `DiarySlider.keyPressed`: flechas y RePag/AvPag mueven el valor con el foco encima.
- Controles: rejilla medida contra el panel (no desborda) y avisos de raton/atajos.
- Archivador de paquetes: recuento de materiales al pie.

## v0.263.0 - Los puestos de la lista

- `DiaryChrome.addressReadout` / `looksLikeAddress`: anfitrion y puerto a la vista, forma minima validada.
- Conexion directa: lectura bajo el campo, aviso en rojo, `Entrar` apagado si la direccion no vale, pie de teclas.
- Ficha de servidor: `Tab`/`Enter` entre campos, aviso de lo que falta, pie de teclas.
- Lista de puestos: doble clic entra, aviso del boton con el nombre, `Esc` con sonido de puerta, `F5` relee.

## v0.262.0 - Ocho remates de papel

- Diario: el indice dice cuantos dias hay; el folio del pie vuelve al principio al tocarlo.
- Diario: `Esc` cierra el indice antes que la hoja.
- Difuntos: fecha del deceso, firma `por la casa, S.P.` y mancha de tinta junto al sello.
- Mundos: `setHint` propio del buscador.
- Audio: `playHover` limitado a uno cada 55 ms.
- Portada: `drawMusicBadge` con la pieza actual y el total.

## v0.261.0 - Doce cosas que se ven

- Portada: `drawBottomMarks` (fecha real + version al pie derecho) y hora escrita al pasar el puntero por el reloj.
- Reloj: marca del taller `S.P.` grabada en el bisel.
- Pausa: chapa `REG. VII` y `drawVoicesLine` (voces del parte).
- Parte: recordatorio de teclas al pie y raton dibujado en ranuras con `isMouse`.
- Mundos: contador en la cabecera, `drawKeysHint`, y `Supr` que dispara el borrado del juego.
- Multijugador: `drawFooterNotes` con teclas y direccion del puesto elegido.

## v0.260.0 - El reloj dice la fecha

- `PocketWatchRenderer`: cuarta linea de la tapa con `hoy: <dia> · <mes romano>` (`ROMAN_MONTHS`), apareciendo con la misma tinta progresiva.

## v0.259.0 - Las fuentes, a la vista

- `DiaryCreditsScreen.drawSources`: bloque de fuentes reales del diario, con `DiaryChrome.fitText` contra `panelW - 48`.

## v0.258.0 - Cuaderno que suena y botones que avisan

- `ChangelogScreen.toggleIndex`: sonido `UI_PAPER_FALL` al abrir/cerrar el indice.
- `DiaryMultiplayerScreen.withEnter`: el aviso de los dos botones de entrar menciona `Enter`.

## v0.257.0 - Las dos ventanillas del reloj

- `PocketWatchRenderer.drawDateAperture`: ventanilla de fecha a las tres con `Calendar.DAY_OF_MONTH`; el `III` se omite cuando hay ventanilla.
- `ScreenEventHandler`: canto gastado (`drawWornBorder`) en las pantallas vestidas por evento.

## v0.256.0 - Las pantallas que quedaban crudas

- `ScreenEventHandler.isHouseRawScreen`: crear/editar mundo, LAN, desconexion, avisos, confirmaciones y enlaces.
- `PaperButtons.dressPlain`: viste solo `Button` exactos, para no romper pestanas ni selectores ciclicos.
- `ScreenEvent.BackgroundRendered`: la escena de la noche tapa el fondo de tierra de esas pantallas.

## v0.255.0 - La lista de mods tambien lleva fichas

- `DiaryModListScreen`: `findList()` sobre `AbstractSelectionList`, `setRenderBackground(false)` + `setRenderTopAndBottom(false)`, `itemHeight` por reflexion.
- `drawRowSlips`: recorte de papel por mod con hover; guardas de geometria.

## v0.254.0 - La lista de mundos dejo de taparse

- `DiarySelectWorldScreen.init`: `setRenderBackground(false)` + `setRenderTopAndBottom(false)` sobre `WorldSelectionList` (el fondo crudo que tapaba cabecera y pie).
- `layoutAroundPaper`: caja de busqueda a `HEADER_H + 3`, lista de `HEADER_H + 24` hasta los botones vestidos.

## v0.253.0 - El boton vestido sabe si esta apagado

- `PaperButtons`: mapa `ORIGINALS` (WeakHashMap), accion protegida por `origin.active`, `syncStates()` llamado desde `ScreenEvent.Render.Post`.
- `DiarySlider.mouseReleased`: sonido de clic al soltar.
- `ChangelogScreen`: fecha de la entrada leida al pie, antes del folio, con guarda de ancho.

## v0.252.0 - El reloj da la hora

- `PocketWatchRenderer.getCurrentHour()` devuelve la hora local real; se borra la lista de horas inventadas y `isNearNoon()` pasa a depender de la hora.
- `DiaryPauseScreen.drawWorldLine`: dia de la partida y dimension bajo el sello de fecha, recortado al panel.

## v0.251.0 - El diario, escrito de nuevo

- `diary/bitacora_es.md` y `bitacora_en.md` reescritos: 13 entradas, sin referencias a juegos, con origen real de cada cosa (etimologia ghul/Nights, jockey, cuirassier napoleonico; Danzig, Ragusa 1377, Filadelfia 1793, Hamburgo 1892, Oporto 1899).
- `DiaryMultiplayerScreen`: hover por fila, lacre del puesto oficial, `Enter` entra y `Supr` borra.
- `DiarySelectWorldScreen`: hover por fila y `Enter` abre el mundo elegido.

## v0.250.0 - La luna del reloj

- `PocketWatchRenderer`: ventanilla de luna a las seis con fase real (`moonPhase`, epoca 2000-01-06), aro de laton, `VI` reemplazado cuando corresponde y linea de fase en la tapa.
- `PocketWatchRenderer`: la punta del segundero lleva punto.
- `DiaryConfirmScreen.render`: sombra, hoja, adornos y widgets en bloques `try` separados; la confirmacion no puede quedar cruda.

## v0.249.0 - El parte se ata al raton y al numpad

- `DiaryDispatchScreen`: clic 4/5 (y el medio) atan la ranura a ese boton del raton; clic izquierdo escucha una tecla, derecho borra.
- `DispatchLibrary.keyNameOf`: los laterales se rotulan `Lateral 4/5` y el numpad `NUM 1..0`, distinto del `1` de la fila.
- `keyPressed`: Shift, Ctrl y Alt sueltos ya no se pueden atar; suena el sello al guardar.
- `UiSounds.hoverMain`: capa de hoja bajo el tic (cooldown propio) y `playDownSound` por variante (principal/peligro/secundario).
- `DiarySelectWorldScreen`: `renderBackground` pinta la escena y la pagina de registro lleva `drawWornRim`.

## v0.248.0 - Se fue la puerta de servicio

- Se borra el registro de fichas: `DiaryArchiveScreen`, `DiaryAdvancementsScreen` y `DiaryStatsScreen` salen del mod.
- La pausa pierde PARTES y REGISTRO; quedan VOLVER, GABINETE, Parte de CEDA y RETIRARSE.
- Fuera la puerta de servicio: cerradura de la portada, `Ctrl+S`, tres golpes y `KNOCK_BACK`. Un jugador entra por la puerta de siempre.
- Fuera las fichas sin disparador que dependian de esas pantallas (`DRAWER_OPEN`, `DRAWER_COMPLETE`); la tapa del reloj cuenta noches en vez de fichas.
- La pantalla de muerte se rehace: el telegrama pasa a 296x64 en la esquina superior izquierda y deja libre el titulo y el puntaje.

## v0.247.0 - Avances y estadísticas en la casa

- `DiaryAdvancementsScreen extends AdvancementsScreen` y `DiaryStatsScreen extends StatsScreen`: escena de fondo, cabecera de
  40 px con chapa REG. XI / REG. XII, pie de papel bajo el grupo de botones (vestidos con `PaperButtons`).
- Pausa: `openRecords` / `openStats` usan las pantallas nuevas.
- Sala de consulta: ficha no conseguida → `MENU_WOOD_CREAK` y `N_DRAWER_HINT` una sola vez.

## v0.246.0 - Tapa del reloj, mancha de la pausa y mandos

- `PocketWatchRenderer.drawInspection`: tercera linea con `discoveredCount()/ALL.length` y versión al pie; el parámetro `version`
  deja de estar sin uso.
- `DiaryPauseScreen`: clic en cualquiera de las dos manchas (`bloodSpotAt`, radio 12 px) dispara `N_DOCTOR` una sola vez.
- `DiarySlider`: sonido de paso del mouse con memoria de estado.

## v0.245.0 - Las ocho fichas sin disparador

- Restaurados los disparadores de `AVENUE_DARK` (portada inactiva 40 s), `DRAWER_OPEN` / `DRAWER_COMPLETE` (fichas abiertas
  en la sala de consulta), `LETTERS` (primera voz escrita en el parte), `GUESTBOOK` (entrar al multijugador), `BROKEN_SEAL`
  (folio prohibido), `SEAL_BREAKER` (telegrama) y `KNOCK_BACK` (tres aperturas de la puerta de servicio).
- `DiaryTitleScreen.drawPocketWatch` recibe el mouse y dibuja la ayuda del reloj al pasar por encima.
- Verificación: `Discoveries.ALL` (45) sin fichas inalcanzables.

## v0.244.0 - Sello de pulsación y lista de mods

- `PaperTexture.drawSealRing(g,x,y,w,h,k)`: anillo de lacre que se abre 1-5 px y se apaga en 9 ticks (α150→0).
- `DiaryButton`, `SlimButton`, `GreenFluButton`: `sealTicks` por pulsación; se anula con Movimiento reducido (y en partida, `DiaryButton`).
- `DiaryModListScreen extends ModListScreen`: escena de fondo, botones vestidos con `PaperButtons` y hoja calculada bajo el grupo;
  `onMods()` de la portada pasa a usarla.

## v0.243.0 - Registro de mundos y una sola llave

- Portada: `Ctrl+S` otra vez es la única llave; la cerradura solo aparece con Ctrl sostenido.
- `DiarySelectWorldScreen`: página de registro (`drawTornSheet`) bajo la lista y banda oscura `0x6E2A1C10` por mundo con filete
  de cierre; medidas (`itemHeight` por reflexión, márgenes, topes) cacheadas en `init`.

## v0.242.0 - Mundos guardados con tinta de la casa

- `DiarySelectWorldScreen extends SelectWorldScreen`: escena de fondo, cabecera de papel (46 px) y papel calculado bajo el grupo
  de botones; lista y busqueda del juego renderizadas tal cual (sin `super.render` para no duplicar el titulo vanilla).
- `PaperButtons` (nuevo, `client/render`): alta de widgets por reflexion y vestido de botones vanilla reutilizable; `DeathPaper`
  pasa a usarlo.
- Portada: `Ctrl+S` o `Shift+S` abren los mundos; cerradura visible mientras se sostiene la tecla (clicable), sonido de cadena
  al entrar y hoja que cae en las tres lentes.

## v0.241.0 - Registro, multijugador y descripción

- `SlimButton.Icon.FILING` con `drawFileDrawer`; la portada gana el botón de registro (`DiaryArchiveScreen`) a
  `width-100` cuando el ancho lo permite.
- Pausa: la hoja pasa de `PAPER_LIGHT` opaco a α238.
- `DiaryMultiplayerScreen`: cada puesto sobre `drawTornSlip` (210), renglones de registro detrás de la lista y recuento
  movido a `panelW-74`.
- `mods.toml`: descripción nueva con lo que trae el mod.

## v0.240.0 - Ficha de aspecto, parte de CEDA y muerte

- `DiarySkinCustomizationScreen`: fuera el espejo (`drawMirror`, silueta de niebla y clic); `Discoveries.MIRROR` sale de `ALL`
  para que el archivo pueda completarse.
- `DiaryFieldGuideScreen` borrado y su botón retirado de la pausa (la rejilla pasa a 6 botones, 3 filas en compacto).
- `DiaryDispatchScreen`: pie en tres bandası (ayuda, recuento, botones), botones `<` `>` de 26×20 y rótulo de página al
  final de la línea de ayuda; el área de voces termina 8 px sobre el divisor del pie.
- `DeathPaper`: aviso único de 340×78 centrado arriba; `onInit` viste los botones de la pantalla de muerte con `DiaryButton`
  (el original se oculta sólo si el sustituto quedó registrado) sin duplicar su lógica.

## v0.239.0 - Aviso de ficha sobre recorte

- `PaperTexture.drawTornSlip(g,x,y,w,h,paperRgb,inkRgb,alpha)`: cuerpo, dientes y pelusa con el alfa escalado (cuerpo α, dientes
  α×236/255, pelusa α×90/255, sombra α/4); mismas medidas de desgarro que `drawTornSheet`.
- `Discoveries.drawToast`: sustituye el relleno recto y los cuatro filetes bronce por `drawTornSlip`; se conservan el fundido
  (que respeta Movimiento reducido), el sello y los dos rótulos.

## v0.238.0 - Campos de texto sin caja

- `DiaryTextField`: el borde de 1 px (sepia, o lacre con foco) pasa a `PaperTexture.drawWornBorder`; se mantienen el brillo
  superior y los tres filetes de relieve.
- `ChangelogScreen.drawSearchBox`: el marco bronce del buscador pasa a `drawWornBorder`, dibujado después del relleno de papel
  para que el canto no quede tapado.

## v0.237.0 - Parte de defunción

- `DeathPaper` (nuevo, `client/render`): franjas `drawTornSheet` OFFICIAL arriba (42 px, entra desde el borde) y al pie
  (38 px, sólo si `h >= 300`); encabezado con raya a lápiz, registro `EC-QV/1897` y sello de lacre (`REDCOAT` α190/210).
- `ScreenEventHandler.onScreenRenderPost`: dibuja `DeathPaper` antes del corte por partida, que es donde ocurre la muerte.
- No se sustituye `DeathScreen`: sus textos, su causa y sus botones de reaparecer/volver al título quedan intactos.

## v0.236.0 - Los mandos pierden el marco

- `DiarySlider` (0x88422810), `DiaryMusicSlider` y `DiaryAmbienceSlider` (0x60422810) y `DiaryProgressBar` (0xB0422810) cambian
  su rectángulo de 1 px por `PaperTexture.drawWornBorder`.
- Sin cambios de comportamiento: mismos valores, mismos tiempos, misma marca de umbral.

## v0.235.0 - Los botones dejan de ser cajas

- `PaperTexture.drawWornBorder(g,x,y,w,h,color,seed)`: filete a mano — tramos de 5 px en adelante con jitter de 1 px arriba,
  abajo y en los laterales; determinista por geometría.
- `DiaryButton`: usa `drawWornBorder` y se elimina `drawBorder`. `SlimButton` y `DiaryToggle`: mismo filete a mano.
- El anillo de foco de teclado queda igual (aviso, no papel).

## v0.234.0 - Indice por meses, pelusa y cantos de contenedores

- `ChangelogScreen`: `buildIndexRows` agrupa las fechas por mes (`MONTH_NAMES`, `monthOf`); las cabeceras de mes no son
  pulsables (`indexRowAt` devuelve la sección), llevan raya a lápiz y `toggleIndex` centra por fila. Con 3 meses + 11 fechas
  el tope de 12 renglones y el desplazamiento de 0.231.0 entran en juego.
- `PaperTexture.drawTornEdge`: pelusa (`0x005A4526`, α70-119) dentro del mordisco cada 3 tramos y fibra (`0x00806A44`, α110)
  que sobresale cada 7; se aplica a todas las hojas rasgadas.
- `PaperTexture.drawWornRim` + `ScreenEventHandler.onContainerRenderPost`: canto gastado (1 px, tinta α125/80 y sombra α34)
  en `AbstractContainerScreen` — inventario, cofres, hornos y demás contenedores.

## v0.233.0 - La pausa termina rasgada

- `PaperTexture.drawTornRim`: canto rasgado para hojas pintadas a mano — dientes de 1–3 px hacia afuera (papel α242) y sombra de 1–2 px
  hacia adentro, más un diente suelto en los laterales; determinista por geometría, sin RNG por cuadro ni animación.
- `DiaryPauseScreen`: `drawPanel` y `renderWorldSafe` sustituyen el doble filete sepia/bronce y los esquineros de oro por `drawTornRim`;
  se eliminan `drawDoubleBorder`, `drawRect` y `drawCornerOrnaments`.

## v0.232.0 - Se va el ultimo rectangulo

- `ChangelogScreen`: el registro técnico (modo no diario) pasa de `drawSheet` + cuatro filetes `BRONZE` a `drawTornSheet`
  ARCHIVE; se eliminan los filetes rectos de ambos modos.
- `DiaryLanguageScreen`: el marco de tinta de 1 px de la lista se sustituye por `drawTornSheet` CORRESPONDENCE con sombra corta.
- `DiaryPackSelectionScreen`: la hoja grande bajo las dos listas pasa a `drawTornSheet` ARCHIVE.
- `DiaryChrome`: se eliminan `drawPanelScaffold` y `renderPanelBase` (sin uso).

## v0.231.0 - Recortes al pie e indice con tope

- `DiaryArchiveScreen.drawPaperDoc`: los tres papeles recuperados pasan de recuadro recto a `drawTornSheet` CORRESPONDENCE
  (124×20, sombra corta); el hover aclara el cuerpo del recorte y subraya el rótulo con `drawPencilRule`; el área de clic
  sube a 20 px.
- `ChangelogScreen` modo diario: el índice topa en `INDEX_MAX_ROWS = 12` renglones (`indexHeight`, `indexVisibleRows`),
  con desplazamiento propio (`indexScroll`, rueda dentro de la hoja, marca al costado) y centrado en la entrada actual al
  abrirlo (`toggleIndex`); la hoja abierta se queda clics y rueda (`overIndex`).

## v0.230.0 - Documentos sobre recorte

- `DiaryArchiveScreen.renderDocument`: cada documento se apoya en un `drawTornSheet` LEDGER con sombra corta
  (márgenes 24 px, alto `panelH-78`).

## v0.229.0 - Indice con marca de lectura y firma sobre papel

- `ChangelogScreen`: `currentDiarySection(probeY)` marca la entrada en curso; realce del renglón bajo el puntero.
- `DiaryTitleScreen.drawSignature`: la firma va sobre una tira rasgada (`drawTornSheet`, 16 px) con subrayado a lápiz
  y, sobre papel, en tinta oscura; el nombre viejo pasa a `SEPIA_MID` α170.

## v0.228.0 - Notas al margen y luz de lampara

- `ChangelogScreen` modo diario: la viñeta se cambia por dos trazos de pluma; el borde superior e inferior del
  área de lectura recibe un degradado tenue (`fillGradient`, α24).

## v0.227.0 - El diario tiene indice

- `ChangelogScreen` modo diario: `buildDiarySections`, hoja de índice (`drawIndexOverlay`, LEDGER rasgada),
  `indexRowAt`/`jumpToDiarySection` y botón «Índice» al pie.
- `Marginalia.drawHorizontal`: borde superior e inferior rasgado en vez de recto.

## v0.226.0 - Cada entrada es su propia hoja

- `ChangelogScreen` modo diario: `drawDiaryScraps` dibuja un fragmento rasgado (LEDGER, sombra corta) por sección,
  con recorte derivado del índice de la sección.

## v0.225.0 - Los filetes llevan repaso a lapiz

- `DiaryChrome.drawInkDivider`: hasta cinco tramos de lápiz (α55) bajo el filete, deterministas por posición y ancho.
  Afecta a las 14 pantallas que usan el divisor común.

## v0.224.0 - Todas las hojas del gabinete llegan rasgadas

- `drawTornSheet` en accesibilidad, chat, confirmación, controles, unión directa, edición de servidor, guía,
  idioma, multijugador, opciones en línea, personalización, sonido, despacho y la nota de la portada.

## v0.223.0 - Las fechas se escriben enteras

- `ChangelogScreen` (modo diario): las entradas H1 se dibujan sin capitular, en `INK_TITLE`, con subrayado a lápiz
  y mancha de tinta al margen.

## v0.222.0 - Todas las hojas envejecen igual

- `PaperTexture.drawWear` público (para hojas pintadas a mano) y `drawTornSheet` en archivo, créditos y ajustes.
- `DiaryPauseScreen`: el panel recibe `PaperTexture.drawWear` además de sus manchas de sangre.

## v0.221.0 - Portada limpia, bitacora convertida en diario y papel envejecido

- `DiaryTitleScreen`: fuera `drawGlassPatina`, el huevo del emblema (`handleLogoClick`/`breakWaxSeal`,
  mensajes, esquirlas, particulas, relampago) y el boton de manual de campo; el diario ocupa su sitio.
- `ChangelogScreen(parent, diaryMode)`: carga `diary/bitacora_es|en.md`, hoja rasgada, tachaduras (`LT.STRIKE`),
  subrayado a lapiz y mancha de tinta en las fechas, boton "Versiones".
- `PaperTexture`: `drawAge` (humedad, suciedad, bordes gastados) en toda hoja, `drawTornSheet`/`drawTornSeam`.
- `DiaryChrome`: `drawInkBlot`, `drawPencilRule`, `drawStrike`.
- Nuevo `assets/gripeverde/diary/bitacora_{es,en}.md`: el diario de supervivencia con todo el lore del servidor.

## v0.220.0 - El archivo muestra su avance

- `DiaryArchiveScreen`: barra de avance de 1 px bajo el recuento (ocre `0xFF8A6A2D`, marca verde al completar);
  la pista de teclado solo se dibuja si queda hueco bajo los papeles sueltos.

## v0.219.0 - La cronica se apoya como las demas hojas

- `ChangelogScreen`: `drawPanelShadow` + `drawCornerOrnaments` y `drawRegistryTag` ("REG. XV") cuando el título deja hueco.

## v0.218.0 - La sala de consulta se recorre con el teclado

- `DiaryArchiveScreen`: `focusCard` con esquinas de foco (`drawFocusCorners`), flechas + Enter (`keyPressed`),
  `cardAt(...)`/`openCard(...)`/`docForCard(...)` para abrir la ficha pulsada y pista de teclado al pie.

## v0.217.0 - Dos luces que ahora se ven

- `AtmosphereRenderer.renderSearchlights`: halo de radio 8 sobre la lámpara de cada torre (`drawSoftBlob`).
- `VictorianSkyline.renderGhoulWatcher`: halo de radio 4 tras los ojos del ghoul.

## v0.216.0 - La ciudad lejana se mide una vez

- `VictorianSkyline`: `ensureFarLayout(w,h)` resuelve `farRects`/`farWindows` por resolución para `renderFarSkyline`;
  `renderGhoulWatcher` guarda `ghoulWx/Wy/Ew/Eh` en vez de recalcular la ventana.

## v0.215.0 - Los campos de texto dejan de repetir su recorte

- `DiaryTextField`: el recorte visible se guarda (`lastVisibleValue/DisplayPos/InnerW/Text`) y `formatSafe`
  tiene dos casillas (`fmtSeqA/B`) para los tramos previo y posterior al cursor.

## v0.214.0 - La hora y la niebla dejan de preguntarse

- `LivingAtmosphere.renderVignette`: la hora real se cachea un minuto (`realHour()`), en vez de `LocalTime.now()` por cuadro.
- `LivingAtmosphere.renderFogLow`: franjas en `FOG_LOW_BANDS` (constante) y una sola pasada por franja en vez de dos mitades.

## v0.213.0 - Medir la letra una sola vez

- `DiaryTypography.width(font, String|Component)` pasa por `widthMemo`, ahora atado a la fuente (`widthMemoFont`):
  la clave es el texto pelado y la memoria se tira al cambiar tipografía o idioma.
- `ChangelogScreen`: titulo, sello y prefijo de viñeta miden con `DiaryChrome.widthCached`.

## v0.212.0 - La geometria de los haces de las torres

- `AtmosphereRenderer.drawBeam`: `beamSegments(angleBucket, ...)` con `BEAM_MEMO` (ángulo en pasos de 1/1024 rad,
  tope 256) memoriza los tramos relativos al origen; el vaivén ya no recalcula la haces.

## v0.211.0 - El sello de descubrimiento y la nota al margen

- `Discoveries.drawToast`: recorte de cabecera/nombre y ancho de la placa memorizados (`memToast*`, `memVisible*`).
- `Marginalia.layoutLines`: guarda `cachedLayoutLinesWidth`; `drawHorizontal` ya no remide cada línea.

## v0.210.0 - Las ultimas traducciones y el ancho de los rotulos

- Las 11 últimas llamadas a `Component.translatable(...).getString()` pasan por `DiaryChrome.tr(...)`
  (CEDA, guía de campo, multijugador, portada): ninguna traducción se resuelve a mano.
- `DiaryTypography.drawFitted`/`drawFittedLeft` usan `widthMemo` (`WIDTH_MEMO`, tope 512).

## v0.209.0 - El pincel blando y la cuenta del idioma

- `AtmosphereRenderer.drawSoftBlob`: `blobBands(r, rowStep)` con `BLOB_BANDS` memoriza el ancho de cada franja
  por radio; sólo el alfa del anillo se calcula por cuadro.
- `DiaryChrome.trFit` usa `fitCached`.

## v0.208.0 - La luna guarda su forma

- `AtmosphereRenderer`: `moonSpans(radius, shape)` memoriza los tramos de la luna por radio y fase;
  `drawMoonSpans` los pinta. `renderMoonPhase` sólo elige la forma y `renderMoonPhaseDifference` desaparece.

## v0.207.0 - La calle recuerda sus adoquines

- `LivingAtmosphere`: `ensureStreetLayout(w,h)` resuelve por resolución hileras, columnas, juntas, acera, cunetas,
  reflejo y banda del pie (`stTop`, `stRowY1/Y2/Offset/Step`, `stSheen*`, `stGutterW`, ...); `renderStreet` los usa.

## v0.206.0 - Fachadas y banderas

- `VictorianSkyline`: `ensureFacadeLayout(w,h)` cachea rectángulos de fachada de fondo y frente (`bgX1..`, `fgX1..`,
  `fgScaledW/H`); `renderBackground` y `renderForeground` los recorren.
- `FlagRenderer`: `runsOf(grid)` con `FLAG_RUNS` (IdentityHashMap) precalcula los tramos de color de cada bandera;
  los tonos del marco pasan a constantes.

## v0.205.0 - Las listas largas y las chimeneas

- `DiaryLanguageScreen`: nombre de la lengua de la niebla, rótulo de cada fila y código medidos con
  `DiaryChrome.widthCached`; `DiaryArchiveScreen`: sellos, firma del lacre y leyenda de vuelta también.
- `VictorianSkyline`: `ensureChimneyLayout`/`fillChimneyRects` cachean el cuerpo de cada chimenea por resolución.

## v0.204.0 - Los botones, las palancas y las placas tambien miden una vez

- `DiaryChrome.drawCenteredString` usa `widthCached`.
- `DiaryToggle`, `DiarySlider`, `SlimButton`, `DiaryProgressBar`, `DiaryAmbienceSlider` y `DiaryMusicSlider`
  miden sus rótulos por `DiaryChrome.widthCached` (y toman la fuente una vez).

## v0.203.0 - Barrido general: todas las pantallas miden una vez

- `DiaryChrome`: `fitCached`, `widthCached` y `trFitCached` con `FIT_CACHE`/`WIDTH_CACHE` (tope 256, vaciado
  al cambiar de idioma). Sustituyen las llamadas por cuadro en 15 pantallas.
- `DiaryTitleScreen`: la firma compacta (`fitString`) y los anchos de firma quedan memorizados.

## v0.202.0 - Los creditos y la sala de consulta miden su cabecera una vez

- `DiaryCreditsScreen`: `refreshTextCache()` memoriza título/subtítulo/sentencia, versión y nota del pie;
  `drawSection` cachea el ancho de cada etiqueta (`labelWidths`).
- `DiaryArchiveScreen`: `refreshHeaderCache(en, found, total)` memoriza título, subtítulo, recuento y sello.

## v0.201.0 - El multijugador y la pausa dibujan su titulo una vez

- `DiaryMultiplayerScreen`: `refreshHeaderCache(maxWidth)` memoriza título/subtítulo recortados y su X;
  `drawServerCount` cachea frase y ancho.
- `DiaryPauseScreen`: `drawPanelTitle` guarda ancho del título, escala y subtítulo recortado por panel/idioma.

## v0.200.0 - La ventana de idioma memoriza su cabecera

- `DiaryLanguageScreen`: `refreshHeaderCache()` guarda título/subtítulo/sentencia ya ajustados y centrados
  (`headerTitle*`, `headerSub*`, `headerFlavor*`); la cuenta de lenguas cachea su frase y su ancho.
- `ChangelogScreen`: el contador de la lupa (`countSearchText`/`countSearchW`) sólo se rehace al cambiar la
  búsqueda, la aparición actual o la fuente.

## v0.199.0 - La cronica deja de partir sus renglones en cada cuadro

- `ChangelogScreen`: `WrapCache` dentro de `Row` (`whole`/`body`) memoriza las líneas de cada renglón por ancho;
  `rowHeight` y `renderRow` comparten el mismo corte.
- El folio del pie se guarda (`cachedFolio*`) y sólo se rehace al cambiar de página o de fuente.

## v0.198.0 - El humo de las chimeneas tiene sus bocas fijas

- `VictorianSkyline`: `ensureSmokeAnchors`/`fillSmokeAnchors` cachean por resolución la boca de cada chimenea
  (`smokeBg*`/`smokeFg*`, centinela `NO_ANCHOR`); `renderSmokeGroup` recibe los arrays ya resueltos.
- `renderSmokeGroup`: la deriva por chimenea (`windDeep`) sale del bucle de partículas, y los puffs fuera de
  pantalla o con alpha < 6 no llegan a `drawSoftBlob`.

## v0.197.0 - La ciudad y la guia dejan de medir

- `VictorianSkyline`: el trazado de las ventanas (posicion, tamano, suerte y color) se resuelve una vez por
  resolucion (`ensureWindowLayout`/`collectWindows`) y se reutiliza; en el fondo el color va precalculado.
- `DiaryFieldGuideScreen`: la pista del pie se centra al construir la cabecera (`headerHintX`), no por cuadro.

## v0.196.0 - El parte de CEDA deja de armar sus renglones

- `DiaryDispatchScreen`: `SLOT_NUMBERS` (20 cadenas "01".."20") en lugar de `String.format("%02d", ...)` por fila y cuadro;
  `keyLabel(slot)` cachea el nombre de la tecla junto al binding que lo produjo (`DispatchLibrary.key`).

## v0.195.0 - Las pantallas dejan de traducir sesenta veces por segundo

- **`DiaryChrome.tr(key)`**: memoria de traducciones invalidada por `LanguageManager.getSelected()`; `trFit(key, font, maxW)`
  para las cabeceras recortadas. Reemplaza 34 `Component.translatable(...).getString()` por cuadro en 20 pantallas y widgets
  (títulos, subtítulos, sabores, pistas y mensajes de forma fija).
- `DiaryLanguageScreen.drawLanguageCount`: "25 lenguas"/"25 languages" en el margen izquierdo del título si `panelW >= 300`
  y no choca con la placa REG. IV.

## v0.194.0 - Opciones que no traducen en cada cuadro

- `DiaryOptionsScreen`: cabecera (título + subtítulo) memorizada por fuente y ancho (`headerTitleFitted`/`headerSubtitleFitted`),
  sin `translatable(...).getString()` por cuadro.
- `DiaryToggle`: `baseLabelStr` traducido en el constructor; valor cacheado por `Component` (`cachedValLabel`/`cachedValStr`).
- `DiarySlider`: `cachedMessage()` y una sola llamada a `Minecraft.getInstance().font` por cuadro.
- `LivingAtmosphere.renderStreet`: juntas de adoquines cada dos columnas en las hileras del medio (la última las conserva todas).
- `renderSearchlights`: derrame de luz de 1 px en el suelo bajo cada torre.

## v0.193.0 - Menos cuentas por cuadro en las pantallas de texto

- `DiaryFieldGuideScreen`: **culling** de filas fuera del viewport (arriba no se mide ni se dibuja, abajo corta) +
  `GuideRow.cachedWidth` + cabecera (`title`/`subtitle`/`hint`) memorizada por fuente y ancho (`refreshHeaderCache`).
- `DiaryArchiveScreen`: `ensureCardLabels(cw, found)` cachea los 46 rótulos (y el ancho de `?`); `drawWrapped` cachea
  `font.split` por texto y ancho.
- `DiaryTypography.fitString`: **memo** de recortes con dos niveles (ancho+modo → texto), 48/256 topes y limpieza al
  cambiar la fuente. Beneficia botones, toggles, sliders y cabeceras.
- `DiaryButton`: `cachedFit`/`cachedWidth` por texto, fuente y ancho.

## v0.192.0 - Las nubes, de blob a masa

- `AtmosphereRenderer.renderClouds`: dos `drawSoftBlob` por nube (~30 fills cada uno, ~310 por cuadro) → dos **masas de bandas**
  (`drawCloudMass`, 16 fills por nube, ~80 por cuadro).
- `drawCloudMass(g, cx, cy, halfW, halfH, alpha, color)`: bandas 3-8 siguiendo la elipse, alpha `0.45 + 0.55·(1-edge)`.
- **Borde de luna**: las nubes a menos de su ancho de la luna reciben una línea de 1 px (`0x00726042`) con alpha por distancia.

## v0.191.0 - La ciudad enciende otras luces

- `VictorianSkyline.renderWindowsOnBuildings`: LOD `lodSkip` 2 → **3** a partir de 1600 px; fachadas de primer plano con divisores 14/16 → **16/18**; ventana encendida de primer plano 5 → **4 fills** (sin travesaño horizontal). Las ventanas quedan en ~la mitad.
- Ventanas encendidas del fondo con **tres tonos** (`0x00C8B060` gas frío, `0x00E0A83C` ámbar pleno, `0x008A6A20` vela vieja) elegidos por sitio (`floorMod(row*7 + col*13 + bi*5, 6)`).
- `renderSmokeGroup`: 4 → **3 puffs** por chimenea, `sz` ×1.2 → **×1.35**.
- `LivingAtmosphere.renderStreet`: la hilera más lejana no dibuja juntas y cada hilera lleva un filo de agua (`0x007C6740`, α `seamAlpha/4`).

## v0.190.0 - El cielo cuesta un tercio

- `AtmosphereRenderer.drawSoftBlob` (halo de la luna, nubes y humo de chimeneas): anillos 4 → **3/2** y franjas de 2 px → **2-4 px según radio**, con el anillo exterior a alpha propio (antes le tocaba 0 y no se veía). Los tres usos bajan de ~1.500 a ~500 rellenos por cuadro.
- `renderStars`: estrella brillante 5 fills en cruz → **2** (halo + núcleo).
- `renderSkyGradient`: franja fría `0x00182A38` sobre el resplandor ámbar + línea de horizonte (`0x001A1208`, α40).
- `renderSearchlights`: lámpara de cada torre (2 fills por torre).
- `AtmosphereRenderer.moonCenterX(w)` público; `LivingAtmosphere.renderStreet` dibuja el **reflejo de la luna** en el asfalto (un `fillGradientV` + 3 destellos).

## v0.189.0 - La pausa que ya no reza trescientas veces

- `DiaryPauseScreen.drawPanel`: el brillo del panel era un `fill` por fila (~300/cuadro) → **2 `fillGradient`** (sube hasta el 40 % de la altura y baja).
- `DiaryPauseScreen.drawVignette`: 40 pasos de 1 px por lado (160 fills) → **10 bandas de 4 px** (40 fills).
- `DiaryPauseScreen.drawBloodSpotWithSplatter`: círculo píxel por píxel (113 fills × 2 manchas) → **un tramo por fila** (≈22 + núcleo + salpicadura).
  Total de la pausa: ~650 → ~85 llamadas de relleno por cuadro.
- `DiaryChrome.drawRegistryTagAt`: 13 fills de una fila para el acero cepillado → **3** (base + 2 líneas de brillo).
- `DiaryLanguageScreen`: hebra tenue al pie de cada renglón (1 fill, sólo si no está elegido ni bajo el puntero).

## v0.188.0 - La ciudad sin periódicos y con reflectores

- **Fuera los periódicos de la portada**: se borran `PosterRenderer` (943 líneas) y `OrnamentRenderer` (156), junto con `drawPosterLeft`, `drawPosterRight`, `overQuarantinePoster`, `wantedPosterRect`, el estado de pulsación y las dos apariciones que sólo se desbloqueaban ahí (`WANTED_EYES`, `POSTER_PEEL`). Los nombres quedan prohibidos en el verificador.
- `DiaryTitleScreen`: layout recalculado sin `POSTER_*` → `LEFT_COLUMN_W = 180`, `WATCH_COLUMN_W = WATCH_RADIUS * 2 + 20` (la x de los botones y del reloj no se mueve).
- **Fondo nuevo**: `VictorianSkyline.renderFarSkyline` (tercera capa de tejados, 14 rectángulos + 6 ventanas fijas) y `AtmosphereRenderer.renderSearchlights` (dos haces cruzados, 18 tramos cada uno, quietos con movimiento reducido).
- **Optimización**: `FlagRenderer.drawGrid` pinta por tramos RLE (~100 → ~25 rellenos por bandera); `DiaryLanguageScreen` memoriza etiqueta y ancho por entrada; la grieta de `VictorianSkyline.renderCrack` pasa de ~100 fills de 1 px a 3 rectángulos.
- **Multijugador**: botones de 20 → 22 px, más aire entre filas y contador de servidores anotados arriba a la derecha.
- **Pausa**: botón nuevo `gripeverde.pause.btn.ceda` que abre `DiaryDispatchScreen` sin salir de la partida (7 filas / 4×2 en ventanas bajas); clave 190 en las 25 lenguas.

## v0.187.0 - Vuelven las hojas del diario

- `PaperTexture.drawSheet(x, y, w, h[, PaperKind])`: hoja opaca (`SHEET_ALPHA = 243`) con filete interior, tres manchas y dos fibras **deterministas** (derivadas del panel, sin RNG por cuadro); unas once llamadas de relleno por panel.
- Las 21 pantallas y `DiaryChrome.drawPanel` vuelven a la hoja: `drawVeil` deja de usarse (queda disponible).
- Los periódicos de la portada (`PosterRenderer`) siguen intactos.

## v0.186.0 - Botones que respiran y un menú más liviano

- Portada: `BTN_W 264`, `BTN_H 34`, `BTN_SLIM_W 126`, `BTN_SLIM_H 28`.
- `DiaryTypography.drawFitted` / `drawFittedLeft`: la etiqueta se encoge (hasta 0.62) antes que recortarse; `SlimButton` y `DiaryButton` la usan.
- Subrayado de `DiaryButton` en tramos de tres píxeles (antes un `fill` por píxel y por cuadro).
- `AtmosphereRenderer.drawSoftBlob`: cuatro anillos y franjas de dos píxeles (antes seis anillos por scanline); el halo de la luna pasa de tres blobs a dos. Afecta también al humo de las chimeneas y a las nubes.

## v0.185.0 - Veinte voces y un papel limpio de verdad

- `DispatchLibrary.SLOT_COUNT` 16→20 (el formato del archivo no cambia; las ranuras viejas conservan su sitio).
- `DiaryDispatchScreen`: paginación con rueda, RePag/AvPag y clic derecho en el rótulo; hasta doce renglones por página; `Ctrl`+`1..9/0` manda la voz y con `Shift` las 11..20; `Enter` pasa al campo siguiente; números en tinta fuerte cuando la voz tiene mensaje y conteo de voces con texto en el pie.
- `PaperTexture` reducido a `drawVeil` + paleta: se borran 23 métodos de adornos que ya no se llamaban (marca de agua, sello, gotas, fibras, manchas, arrugas, muescas, tea, borde quemado) y se retira `incrementOpenCount`.
- `DiaryPauseScreen`: fuera el sello de cera del panel (dos bucles píxel por píxel, más de mil fills por cuadro).
- `DiaryTypography.fitString`: búsqueda binaria por puntos de código en vez de armar la cadena carácter por carácter.
- `verify_mirrors.py`: prohibidos los nombres de los adornos retirados.

## v0.184.2 - Dieciséis voces en el parte

- `DispatchLibrary.SLOT_COUNT` 10→16: el formato del archivo no cambia, las ranuras viejas conservan su sitio y las nuevas entran vacías.
- `DiaryDispatchScreen`: hasta doce renglones por página antes de paginar (antes diez), y el comentario de cabecera al día.

## v0.184.1 - El velo se aclara y el campo vacío se explica

- `PaperTexture.VEIL_ALPHA` 196→172 (67 %): la tinta se lee, el fondo se adivina; ya no parece una hoja tapando la escena.
- `DiaryDispatchScreen`: los campos vacíos muestran la pista `gripeverde.dispatch.slot.empty` (clave nueva en los 25 idiomas, 187 en total) y se retira un campo sin uso.

## v0.184.0 - El diario se saca la hoja de encima

- `DiaryDispatchScreen` rehecha: lista de diez mensajes con campo y tecla; sin ENVIAR ni ENVIAR TODO (el mensaje sale con su tecla), autoguardado a los 700 ms y al cerrar, paginación `▸ n/total`. Clic en la tecla escucha, Esc cancela, Supr o clic derecho borra.
- `DiarySoundOptionsScreen`: fuera `Niveles: …` y `Niveles de Minecraft: …`; quedan el título y el disco sonando, y se retiran las tres claves de idioma.
- Música y ambiente del mod al canal `MASTER` (`FadingMusicInstance`, `LoopingAmbienceInstance`, `MenuAmbience.ambientOneShot`): el slider de música de vanilla ya no los apaga.
- `PaperTexture.drawVeil` (velo `VEIL_ALPHA = 196` + filete) en 21 pantallas: se ve el fondo, sin marca de agua, sello de cera ni gotas.
- `drawFreshStamp`: el sello NUEVO se mide entero y se mete dentro del área visible, o no se dibuja; nunca aparece cortado.
- Portada: `BTN_W 248`, `BTN_H 32`, `BTN_SLIM_W 112`, `BTN_SLIM_H 26` (antes 300×40 y 130×32).
- Huevos: `HiddenCipher` eliminado con `code_1152`, `code_pereira` y `code_doctor`, y fuera las marcas del encuadernador y el folio escondido. Quedan sólo los del fondo.
- Velocidad: `ChangelogScreen` memoriza el alto de cada renglón (`ensureRowHeights`/`heightAt`) en vez de volver a cortar toda la crónica por cuadro.
- `verify_mirrors.py`: identificadores prohibidos nuevos (`HiddenCipher`, `onCharTyped`, `isChiming`, `asteriskRects`, `hiddenFolioRect`, `BINDER_MARKS`, `drawNewSeal`).

## v0.183.0 - Ningún adorno contesta antes que los botones

- `DiaryChrome.overAnyWidget(screen, x, y)`: un solo ayudante (usa `this.children()` y `isActive()`) para que los adornos con clic pregunten primero por los widgets.
- `DiaryFieldGuideScreen`: guarda de widgets antes de `headingAt()`; aire con `LINE_HEIGHT` 11→12, `PARAGRAPH_GAP` 4→7 y `SECTION_GAP` 6→10.
- `DiaryArchiveScreen`: los botones se consultan antes que los papeles sueltos y que la banda de cierre (`panelH - 56`), que se quedaba clics de widgets; `CARD_GAP` 5→7.
- `ChangelogScreen`: pasa a usar el ayudante compartido (se borra la copia local de 0.182.0).
- `DiaryDispatchScreen`: `Ctrl`+`1`..`9`/`0` (y teclado numérico) manda la ranura de ese número; la ayuda de la ranura lo dice en los 25 idiomas.

## v0.182.0 - La crónica respira

`ChangelogScreen` era la hoja más apretada del diario y la única sin auditar (22 `return true`). Ritmo nuevo: `LINE_H` 11→12, `H1_LINE_H` 16→18, `H2`/`BOLD` 13→14, `GAP_H` 10→13, `SEP_H` 12→16, `PAD_Y` 26→30. El margen lateral pasa de 56 fijo a `clamp(12, width/8, 56)`, así que a 320 px el panel crece de 208 a 240. El botón Cerrar se mide contra el panel (`min(130, panelW-24)`, mínimo 84) y ya no sobresale en ventanas mínimas. Las marcas del encuadernador y el folio escondido ahora preguntan primero por los widgets (`overAnyWidget`, con `this.children()`), como la portada desde 0.178.0. La rueda no suena ni se queda el gesto cuando `maxScroll == 0`. El CI además compila `arena/**`.

## v0.181.0 - Los mensajes viejos se vienen con nosotros

`DispatchLibrary` lee `config/copyl-messages.json` (nombres, textos y teclas de las diez ranuras) y `copyl.json`/`lclient.json` (tecla de apertura) una sola vez, y guarda la copia propia en `config/gripeverde-dispatch.json`. `DispatchHotkeys` honra esas teclas cuando CopyL no está instalado (chat, o comando si el texto empieza con `/`), con guarda de flanco para que mantener apretado no dispare tanda. La ficha muestra las ranuras en columna (≥420 px de ancho) o como tira de chips: clic envía, Shift carga, Ctrl guarda la línea, clic derecho fija tecla. 9 claves nuevas × 25 idiomas (200 en el espejo).

## v0.180.0 - El parte tiene su propia mesa

El parte de CEDA era un puente por reflexión: solo aparecía con CopyL instalado y abría su pantalla. Ahora es una página del diario (`DiaryDispatchScreen`): ocho renglones numerados, 12 px de aire entre ellos, carril de 34 px para los números y dos filas de botones. Enter envía el renglón actual, Tab baja, el borrador sobrevive al cerrar y el envío en tanda espera 10 ticks entre mensajes. El acceso de la portada está siempre (ya no depende de CopyL) y el panel de CopyL, si está, se abre desde la ficha. 14 claves nuevas × 25 idiomas (191 en el espejo).

## v0.179.0 - El cronista habla en toda ventana

La ficha de saludo pide 180 px de carril y 44 px de alto, así que en ventanas de menos de ~620 px de ancho (y en las anchas de menos de ~330 px de alto) el cronista se quedaba callado. Ahora, cuando la ficha no cabe, la misma frase sale como una línea centrada en la cabecera, entre el control de música y los iconos, recortada a `width - 160`. Si la ficha cabe, nada cambia.

## v0.178.0 - Ningún adorno roba un clic

Los adornos con clic de la portada (rata, gato, ventana del ático, cartel del buscado) se comprobaban antes que los widgets y varios devolvían `true`, así que el clic no llegaba al botón: a 427x240 la ventana del ático cae 12 px dentro de Multijugador, y por encima de ~300 px de alto la rata cruza la mesa y Ajustes/Salir. Ahora todos preguntan primero por los botones reales. La pausa tenía el mismo problema: su vela tapaba el último botón de la izquierda en ventanas de 235 a 260 px de alto.

## v0.177.0 - El susurro encuentra su banda

El susurro de la vigilia nacía a un tercio de la altura de la ventana, dentro del emblema en cualquier ventana de menos de ~410 px de alto: a 427x240 cruzaba el filete de la fecha y a 700x400 se posaba sobre el renglón del día. Ahora busca la banda libre entre el emblema y la mesa de botones, y si no cabe ni una línea baja al pie sobre la firma. La mesa de botones se mide con una sola fórmula, compartida con el emblema.

## v0.176.0 - Ajustes y Salir se apartan

En ventanas de menos de 620 px de ancho, Ajustes y Salir colgaban de una fila fija en y=36, encima del emblema, que empieza en y=30. A 427x240 —el tamaño de una ventana de 854x480 con escala GUI 2— tapaban 32 px del escudo. Ahora van en un carril propio a la derecha de Multijugador y Mods, dentro de la banda de la mesa de botones, y el grupo se centra sobre el eje del emblema. Sin cambios durante la partida.

## v0.175.0 - El emblema cede ante los botones

En ventanas bajas el emblema de la portada deja de crecer hacia la mesa de botones: se encoge entero y baja su ancla si hace falta. A 427x240, el tamaño que da una ventana de 854x480 con escala GUI 2, el bloque completo tapaba 45 px del botón de multijugador. La ficha del cronista sigue la misma regla y se coloca debajo del emblema, o espera si no hay banda libre. La cadena del reloj de bolsillo arranca por debajo de los iconos de acceso y el clic del emblema ya no dispara su sello sobre un botón.

## v0.174.0 - Los carteles vuelven a respirar

Los cuatro bandos rotativos de la portada se componen como un bloque centrado: nota de rescate, se busca, decreto real y etiqueta del elixir. En carteles altos el texto deja de colgar del borde superior; en los más bajos los pasos se ajustan para que la última línea no toque el filete. El precio del elixir mantiene un renglón de aire sobre el borde inferior.

## v0.173.0 - El aviso tiene un rostro

El bando de cuarentena de la portada abre con un grabado mayor del infectado, enmarcado como placa de imprenta, con ojos azules y rótulo de nota de campo. La orden legal y el sello CEDA ocupan zonas distintas, así que no se superponen en carteles bajos ni estrechos. La nota de campo CEDA reutiliza la misma placa, sustituye el ojo suelto por un sello CEDA y omite el retrato cuando no hay altura. Sin animaciones nuevas fuera de la portada.

## v0.172.0 - Rostro para el aviso de cuarentena

El bando de la portada incorpora un retrato estático de un Ghoul de ojos azules, colocado en el margen inferior para no cubrir el texto. Se declaran en Minecraft las 25 variantes regionales del mod, incluido `es_uy`, y las verificaciones exigen metadatos seleccionables para todas.

## v0.169.0 - Variantes regionales para elegir

Se declaran ante Minecraft las variantes regionales adicionales del español para que aparezcan en la lista de idiomas. La validación también exige sus metadatos y mantiene comprobados los 25 archivos de traducción y sus crónicas.

## v0.168.0 - Inglés y español en todas sus regiones

Se completan los 25 códigos regionales declarados: cada uno tiene su archivo de traducción y su crónica. La compilación ahora verifica el conjunto completo, la paridad de claves y la versión vigente de cada crónica.

## v0.167.0 - Más aire en la nota de rescate

La nota de rescate de la portada deja un renglón completo entre cada instrucción, incluso en el cartel compacto. La última frase ya no se pega al divisor y el cartel no añade movimiento ni trabajo durante la partida. Se añaden los códigos regionales de inglés y español de Minecraft 1.20.1, incluido el español uruguayo.

## v0.166.0 - Una prueba para el sonido de la calle

El control de ambiente ahora reproduce un crujido breve por el mismo canal de Minecraft que usan la lluvia y el viento. Sirve para comprobar el volumen del mod y los niveles Ambiente y Maestro; el botón queda inactivo durante una partida.

## v0.165.0 - La portada recuerda sin consultar cada fotograma

La luz de la ventana y las cortinas del edificio conservan su estado en memoria durante la sesion. El dibujo de la portada ya no crea un arreglo ni consulta los recuerdos guardados a cada fotograma.

## v0.162.0 - Foco visible en los iconos

Los botones pequeños de la portada ahora muestran un contorno ámbar al recibir el foco del teclado. El indicador es estático para no añadir movimiento al juego.

## v0.161.0 - Rótulos más legibles

La segunda línea de los botones principales conserva suficiente contraste sobre el fondo oscuro, incluso al pasar el cursor. El tono sigue dentro de la paleta de latón envejecido.

## v0.160.0 - Ajustes más claros

Se simplificaron los nombres de las secciones de ajustes en inglés y español. Los avisos de sonido explican qué controles revisar sin fórmulas, y la nota de rescate distingue con claridad el inventario y el cuerpo del compañero.

## v0.159.0 - Letras sin cuadros

Los rótulos del diario ahora se dibujan como texto normal de Minecraft, sin estilos de fuente heredados. Se refuerza la comprobación de compilación para detectar si alguien vuelve a introducir un estilo de fuente personalizado en esos textos.

## v0.158.0 - La crónica vuelve a poner las tildes

Las notas de la estrella fugaz y del telegrama sellado recuperaron las tildes y la ñ que se habían perdido. Sus frases en español ahora se leen como fueron escritas.

## v0.157.0 - Un telegrama que deja respirar la portada

La nota que aparece al tocar el sello ahora es más angosta y dura menos. El secreto permanece, pero el papel vuelve a dejar ver el cielo y las hojas de la portada.

## v0.156.0 - El cielo guarda la nota

La frase que acompaña a la estrella fugaz conserva sus renglones mientras no cambien el mensaje, la letra o el espacio disponible. La nota se ve igual, pero la portada deja de volver a partir y medir lo mismo en cada frame.

## v0.155.0 - Letras legibles

La letra personalizada había dejado cuadros vacíos en la portada. El diario vuelve a la letra normal de Minecraft, y las crónicas mantienen su voz natural. También cambié la pista del pequeño ojo para que suene como una nota encontrada, no como una explicación del menú.

## Version 0.154.0 - Notes from the living

The field guide now reads like a survivor's account instead of a system briefing. Its English and Spanish notes explain how Ghouls change, what to do when a teammate is pinned or downed, and what each difficulty brings, while preserving the server's mechanics.

## Version 0.153.0 - Clearer letters in the diary

The title screen, options, field guide, and shared controls now use Atkinson Hyperlegible with consistent text measurements. Button labels no longer draw a second dark copy, so they stay crisp against the paper. The font is bundled with its license and adds no animation or rendering work in worlds or inventories.

## Version 0.152.0 - Ink that stays put

The menu buttons' tiny paper-wear marks are laid out once and reused on every draw. The paper keeps its same look without reseeding and sampling a random generator every frame.

## Version 0.151.0 - A signal without blinking

When Reduced Motion is on, the attic signal window stays steady instead of flashing its answer. The clue remains available, and the menu atmosphere stays still.

# Eternal Craft: Gripe Verde

## Version 0.150.0 - The field guide fits the page

The field guide now fits short windows and high GUI scales, wraps its notes to the paper width, and keeps the return button visible. In compact layouts, the reading hint moves into the heading so it does not compete with the page controls.

## Version 0.149.0 - Columns that hold their place

The title-screen column layout and left notice bounds are reused until the window size changes. Redraws no longer recreate those layout regions each frame, while paper placement, resize behavior, and poster interaction remain the same.

## Version 0.148.0 - Still paper, lighter frames

The title-screen notices now reuse their translated copy keys and paper-cut details instead of building temporary arrays on every draw. Seeded blood flecks use allocation-free arithmetic in place of a fresh random generator each frame. The marks remain fixed to the menu and never render over the world or inventory.

## Version 0.147.0 - CEDA dispatch at hand

A small CEDA note on the title screen opens the optional CEDA Dispatch settings when that mod is installed. Both screens now share quiet parchment and sepia styling. Ink stamps stay in the lower paper margin and skip short sheets; their fixed design avoids a random allocation each menu frame. The shortcut adds no gameplay or inventory effects.

## Version 0.146.0 - Room for the reward

The wanted poster now keeps its portrait and wax seal clear of the reward lines on shorter windows. The drawing stays fixed to the title screen.

## Version 0.145.0 - The right address keeps its place

When both an official Outbreak address and a renamed saved entry exist, the multiplayer page now pins the matching address first. If only the renamed entry remains, its custom address and label stay intact.

## Version 0.144.0 - The cry that brought us back

Reaching the teamwork chapter can leave one last recollection from the chronicler: a cry carried across the town, and strangers running back for one another. It appears once, in the chosen language.

## Version 0.143.0 - The fever still had a pulse

A witness's recollection now accompanies the infection chapter. It tells of the moment the survivors understood that the fever had not killed its victims. The note appears once, in English or Spanish.

## Version 0.142.0 - The note waits at the right page

The rescue recollection now appears whichever way the reader reaches that section: by turning to its mark, scrolling through the pages, or using the keyboard. It remains a one-time note.

## Version 0.141.0 - The way back through the mud

Reading the rescue protocol leaves a new note from the chronicler. It is a one-time recollection of a frightened group turning back for one of their own, written in the chosen language and shown on a later page.

## Version 0.140.0 - The address through the fog

The official Outbreak server is now recognized even if its saved address uses different capitalization or has stray spaces. Older addresses are migrated the same way, so the field guide won't add a second copy of the server.

## Version 0.139.0 - The Outbreak keeps its name

The pinned Ghoul Outbreak server now appears in the selected language. Returning players keep their own server name, while an old saved Outbreak address is updated even if it was renamed.

## Version 0.138.0 - The pen waits for the reader

The chronicle now writes only while its next line is visible. Scrolling away pauses the ink, long frame delays cannot make it jump ahead, and the quiet scratch follows visible writing less often.

## Version 0.137.0 - The doctor's mark finds its place

The elixir's fraud mark now sits in a dedicated space below the bottle copy and disappears when a short poster has no room for it. English readers see “FRAUD”; Spanish readers see “FRAUDE.”

## Version 0.136.0 - A light crossing the language barrier

The shooting-star note now follows the chosen English or Spanish language while it is visible and wraps inside the screen on small windows.

## Version 0.135.0 - A note that crosses the language barrier

The hidden message behind the ministry seal now changes with the selected English or Spanish language, even while the note is still on screen.

## Version 0.134.0 - The chronicler keeps your tongue

The title greeting, its paper label, and the quiet vigil line now follow the selected English or Spanish language. Their wrapped text is also reused until the words, window width, or font changes.

## Version 0.131.0 - Ambient controls reach every layer

Menu ambience one-shots now use Minecraft's Ambient sound category, matching the looping beds and the Ambient slider. Music remains in Music, and button effects remain in the UI/Effects path.

## Version 0.132.0 - The gramophone keeps its note

The title-screen music button now rebuilds its tooltip only when the music state or language changes, instead of recreating it every tick.

## Version 0.133.0 - The notice keeps its lines

The title-screen typewriter notice now reuses its wrapped lines and cursor width until the visible text, window size, or font changes. This avoids repeating the same text layout work between frames.


## Version 0.130.0 - A note in the margins

Rewrite the smuggler's hint in the chronicler's natural voice in English and Spanish, removing control-like wording while keeping its in-world clue.


## Version 0.129.0 - Words from the watch

Polish the English and Spanish title-screen lines so they sound natural aloud, keep the period tone, and use correct Spanish accents. The button subtitles remain static text and still rotate only in the title screen.


## Version 0.128.0 - Sickly glow on the horizon

The title-screen city gains a faint, fixed green haze behind the skyline, hinting at the outbreak. It is menu-only, uses a few translucent fills, and adds no animation or world rendering.


## Version 0.127.0 - Worn paper, clear notice

The elixir notice now keeps its fraud stamp below the copy, with a safe margin from the price and bottle line. Notice sheets gain subtle paper depth, a fine inner rule, and uneven cut edges. All details stay still and only render on menu papers.


## Version 0.126.0 - Words stay on the paper

Every translated line on the quarantine, elixir, reward, and royal decree notices now fits inside the inner paper edge. Scaled titles use the available width too, so Spanish copy cannot spill beyond the border or into the folded corner.

## Version 0.125.0 - The empty desk

The long-idle discovery now reads like a story in the chronicler's room: its name and clue speak of an empty desk and the ghosts who gather there, not the controls used to uncover it. The secret itself and when it appears are unchanged.

## Version 0.124.0 - A note from the night watch

The four title greetings now sound like the chronicler speaking to someone beside him, in short, everyday English and Spanish. They keep their Ghoul-era setting without mentioning menus, controls, or machinery.

## Version 0.123.0 - Clouds without frame waste

The title sky keeps its cloud shapes ready instead of rebuilding them on every frame. Their movement and appearance are unchanged, with fewer short-lived allocations while the menu is open.

## Version 0.122.0 - A stamp with room to breathe

The elixir's FRAUDE mark now follows the bottle line instead of being pushed up from the card's bottom edge, so it no longer lands over the words on short sheets. On unusually small layouts it stays off the copy. The title notices also have more clearly cut corners, with the ink border following the same paper edge. These are still marks on a still title screen.

## Version 0.121.0 - Sections you can spot

The field manual now prints a small number beside each section. The numbers match the 1–7 shortcuts shown at the bottom, so readers can see where each jump will take them. The marks are still ink on paper; nothing moves during play.

## Version 0.120.0 - Rescue advice that fits

The rescue notice now keeps every instruction on the page at narrow sizes. It tells the crew how to free a PIN, lift a fallen survivor, avoid interrupting the rescue, and find a defibrillator.

## Version 0.119.0 - No one left behind

The daily CEDA papers now include a short rescue notice: a teammate must free anyone caught in a PIN, lift a fallen survivor without taking a hit, and find a defibrillator in the loot if someone dies. It stays on the title screen and never moves during an expedition.

## Version 0.118.0 - Hojas con bordes de otra época

Las esquinas de los avisos ya no parecen cortadas por una máquina: cada hoja tiene un perfil apenas irregular, con pliegues y fibras gastadas que se quedan quietos mientras juegas. El sello rojo del elixir ocupa su propio margen, debajo del precio y sin tapar las letras. En la pantalla principal, la hoja de cuarentena sigue abriendo el manual al tocarla. Las crónicas del narrador cuentan lo que ocurre con palabras de todos los días, tanto en español como en inglés.

## Version 0.117.0 - Lighter return dust

- Cache the fixed specks shown on the title screen after a long absence.
- Reuse their coordinates and opacity on each frame, recalculating only after a window resize.
- Keep the same still effect and its title-screen-only behavior.

## Version 0.116.0 - Keyboard-ready multiplayer hints

- Show full action tooltips when a custom multiplayer button receives keyboard focus, as well as on hover.
- Keep the familiar 500 ms delay and anchor focused hints to the button.
- Preserve still gameplay screens and the existing mouse layout.


## Version 0.115.0 - Full multiplayer action hints

- Restore the full localized action tooltips on the custom multiplayer buttons, with Minecraft's familiar delay and edge-aware placement.
- Keep the server list and its artwork in the same layout; tooltip work only runs for a hovered action button.
- Do not add animation or sound to live gameplay.

## Version 0.114.0 - Lighter server list rendering

- Read vanilla server-list row dimensions after layout and on resize instead of searching private fields for every rendered card.
- Keep the official server address and all player-edited entries untouched.
- Preserve the same list layout, artwork, ping, and LAN behavior.

## Version 0.113.0 - A book instead of a letter

- Replace the language-dependent single-letter guide shortcut with a small engraved open-ledger icon.
- Keep the button's full localized field-guide name available to keyboard and screen-reader users.
- Preserve the existing tooltip, sound behavior, title-screen layout, and still gameplay interfaces.

## Version 0.112.0 - Clean field guide headings

- Keep clickable section names as plain printed headings rather than drawing small button panels over the text.
- Clip all section navigation to the guide viewport so off-screen headings cannot cover the footer or close control.
- Preserve keyboard and screen-reader navigation with descriptive, silent heading targets; add no gameplay animation.

## Version 0.111.0 - Field guide at a glance

- Add a small, labeled field-guide button beside the title-screen chronicle control.
- Keep it available in compact layouts and route it to the same bilingual guide used by the quarantine poster and pause menu.
- Use the existing translated guide hint; no new notice, gameplay animation, tick, or world effect.

## Version 0.110.0 - Clearer button ink

- Keep disabled button labels in dark sepia ink so their muted paper state remains readable.
- Preserve the single, unshadowed text pass and measure clipped labels only once.

## Version 0.109.0 - Notes worth reading

- Wrap the chronicler's notes into a small paper card so longer bilingual lines remain readable instead of being cut off at the screen edge.
- Keep the note card above its panel, with a quiet CEDA wax mark and no new world or inventory animation.
- Leave notes queued in the archive when a small viewport has no room for a readable margin.

## Version 0.108.0 - Music on command

- Add a saved pause and resume control to the sound panel without changing the chosen music level.
- Keep custom and vanilla menu music suppressed when the user pauses the soundtrack.
- Disable menu track controls during live gameplay; no new world or inventory animations.

## Version 0.107.0 - Eyes in the quarantine

- Add a tiny, fixed pair of blue Ghoul eyes to one distant title-screen window.
- Place it behind the city fog and keep it out of diary overlays and live worlds.
- Use fixed pixels only: no new ticks, particles, sound, or gameplay effects.

## Version 0.106.0 - Steadier paper

- Cache each deterministic paper-stain pattern so repeated menu renders reuse it instead of creating a random generator each frame.
- Draw the Ghoul report's three body lines directly, avoiding a temporary array on the title-screen render path.
- Preserve the paper appearance and static menu-only rendering.

## Version 0.105.0 - Quiet notes

- Respect Reduced motion across marginal notes, discovery notices, and the sound-panel tuning fork.
- Show notes immediately and keep the discovery seal steady when reduced motion is enabled.
- Keep the same behavior for users who leave the preference off; live-session interfaces stay still.

## Version 0.104.0 - The field report

- Add a fourth daily title-screen document with CEDA guidance on living Ghouls, mutations, PINs, and staying together.
- Give the notice a weathered-paper treatment and a fixed blue-eye ink mark.
- Keep the poster bilingual and static; it adds no animation to worlds, servers, or inventory screens.

## Version 0.103.0 - CEDA's cover story

- Name CEDA as the agency that concealed the 1800 outbreak as common flu.
- State plainly in both languages that Ghouls are infected survivors, not raised dead.
- Preserve the field guide's existing section layout and behavior.

## Version 0.102.0 - The royal decree

- Add a fixed crown-in-wax seal to the daily royal decree notice.
- Keep the seal inside the paper border with a restrained Victorian palette.
- Use static geometry only on the title screen; no world or inventory animation.

## Version 0.101.0 - Clearer sound guidance

- Explain that Minecraft Master and the matching category both multiply mod music and ambience.
- Shorten the bilingual slider tooltips while preserving the exact controls to check.
- Keep audio playback and world behavior unchanged.

## Version 0.100.0 - Compact sound readout

- Keep Minecraft Master, Music, and Ambient percentages visible on narrow sound screens.
- Use concise bilingual labels without clipping the last category.
- Preserve the full category names in the regular layout.

## Version 0.99.0 - Master volume visibility

- Show Minecraft's Master, Music, and Ambient levels together in the sound cabinet.
- Make the last system-wide mute point visible when diagnosing quiet menu audio.
- Keep the readout observational; it does not change audio or world behavior.

## Version 0.98.0 - First-response signals

- Give PIN, downed, death, and defibrillator guidance quiet color-coded rails for faster scanning.
- Keep the field guide's copy regular-weight and preserve its existing keyboard and scroll controls.
- Draw only fixed shapes; no gameplay animation or extra world effects.

## Version 0.97.0 - The chronicler's field note

- Show title greetings on a fuller archive-paper slip with a catalog heading in both languages.
- Improve spacing and ink hierarchy while keeping the text regular-weight and easy to read.
- Keep the paper detail static and confined to the title screen.

## Version 0.96.0 - Minecraft sound levels

- Show Minecraft's Music and Ambient category volumes beside the mod's mix in the sound cabinet.
- Keep the compact sound layout readable while exposing the system-category levels.
- Bump localization parity to cover the new bilingual label.

## Version 0.95.0 - Live sound levels

- Show the current mod music, ambience, and interface-effect levels in the sound cabinet subtitle.
- Keep the readout synchronized with the sliders and the saved accessibility volume settings.

## Version 0.94.0 - The chronicler speaks

- Show the session greeting on the title screen as a quiet, static paper note for twelve seconds.
- Keep the greeting out of compact layouts and active worlds; fit the text to its paper slip.
- Prevent long English and Spanish vigil lines from spilling beyond narrow title windows.

## Version 0.93.0 - Recover the fallen

- Add the death-site body location to the field guide's downed and rescue protocols.
- Keep the instructions available in English and Spanish.

## Version 0.92.0 - Polished Spanish archive copy

- Restore accents and opening punctuation in Spanish telegrams and hidden archive lines.
- Clarify the Spanish contraband note while preserving the discovery conditions.

## Version 0.91.0 - Readable archive notes

- Set archive marginal notes in a horizontal paper slip above the records at regular window sizes.
- Keep the existing margin note only on short screens to avoid covering the archive heading.
- Correct Spanish accents in archive status text.

## Version 0.90.0 - Bilingual title notes

- Move chronicler greetings and long-idle vigil lines into the English and Spanish language files.
- Restore Spanish accents and use consistent direct address.

## Version 0.89.0 - Legible Ghoul headings

- Use dark teal ink for field-guide section headings, including hover and keyboard focus states.
- Keep the bright Ghoul eye colors on small decorative accents and preserve regular font weight.

## Version 0.88.0 - Readable archival ink

- Increase contrast for regular-weight secondary copy and remove an extra fade from the corrected options note.
- Preserve the typeface weight; keep hierarchy in spacing and color.

## Version 0.87.0 - Balanced paper grain

- Reduce repeated paper fibre draw calls on full-size menu panels and keep the grain visible with slightly longer fixed strokes.
- Preserve the lightweight live-world paper path.

## Version 0.86.0 - Compact discovery notices

- Shorten discovery feedback to a smaller bilingual slip with a 224 px width cap and clearer single-line hierarchy.
- Preserve the title-screen silence and suppress notices while a world is active.

## Version 0.85.0 - Weathered recovered papers

- Give recovered archive documents fixed paper wear, a folded corner, and a short shadow without adding per-tick work.

## Version 0.84.0 - A clearer soundtrack console

- Show the current menu record and transition state in the sound cabinet.
- Add a compact next-record control beside the music slider and disable skipping while a crossfade is underway or the music is stopped.
- Keep record status localized in English and Spanish and preserve static controls in a live world.

## Version 0.83.0 - Polished Spanish copy

- Correct missing accents in quarantine posters, the pause screen, the field guide, accessibility text, and sound settings.
- Replace plural Spain commands with consistent direct address and clarify Spanish sound controls.
- Preserve English/Spanish localization keys and formatting placeholders.

## Version 0.82.0 - Readable chronicler notes

- Keep horizontal marginal notes inside narrow windows and place them on a quiet strip of paper for contrast.
- Restore Spanish accents in the chronicler's notes.
- Keep notes hidden while a world is active.

## Version 0.81.0 - Aged field paper

- Add restrained, fixed corner wear and a fold highlight to field papers shown over a live world.
- Keep the effect deterministic and free of animation or per-tick work.

## Version 0.80.0 - Responsive discovery notices

- Keep discovery seals inside narrow windows and shorten long localized titles before drawing them.
- Correct accents across Spanish discovery names and archive hints.

## Version 0.79.0 - Explain the Minecraft audio categories

- Clarify that menu music also follows Minecraft's Music category.
- Clarify that menu ambience loops also follow Minecraft's Ambient category.
- Keep the mod's independent music, ambience, and UI effect controls.

## Version 0.78.0 - More readable field guide

- Add paragraph spacing and increase line height in the survivor field manual.
- Keep section jumps, scroll bookmarks, and live-world rendering unchanged.

## Version 0.77.0 - Respect the edited server address

- Preserve a custom address entered for the pinned Ghoul Outbreak server.
- Migrate only the previous built-in host to `Outbreak.exaroton.me:24652`.
- Continue adding the official entry when it is missing and keep it at the top.

## Version 0.76.0 - Static controls during play

- Make custom mod UI controls snap to their current hover and press states while a live world is open.
- Keep title-menu motion available when no world is loaded.
- Reuse the shared motion policy so buttons, toggles, and alert accents follow the same rule.

## Version 0.75.0 - Compact title signature

- Show only the current signature in narrow title layouts.
- Fit it between the music control and the right edge to prevent footer overlap.
- Preserve the crossed-out previous signature on wide layouts.

## Version 0.74.0 - Lighter field-guide headings

- Use the diary's regular centered text renderer for both field-guide headings.
- Remove the automatic dark drop shadow that made those headings look heavier than surrounding text.

## Version 0.73.0 - Keep notes off the main desk

- Keep queued contextual marginalia off the title screen so it cannot overlap the bottom controls.
- Preserve the note for the next paper screen that displays the archive or controls.

## Version 0.72.0 - Ambience control restored

- Add a visible ambience on/off control beside its volume slider in sound settings.
- Show the current state and a localized tooltip in English and Spanish.

## Version 0.71.0 - Quiet title notices

- Remove transient discovery cards from the title screen.
- Keep discoveries recorded in the archive and preserve contextual marginal notes.

## Version 0.70.0 - Responsive title menu

- Reflow title actions on narrow GUI widths so the main buttons, options, quit, and music controls stay reachable.
- Hide side posters and the pocket watch in compact layouts to reduce clutter.
- Keep this layout work on the title screen; it adds no animation or per-tick work in a world or server.

## Version 0.69.0 - Persist audio sliders on release

- Keep music and ambience volume responsive while dragging.
- Save each volume setting once when the player releases its slider, instead of writing config repeatedly during the drag.

## Version 0.68.0 - One-shot menu audio shutdown

- Stop menu music and ambience once when the client enters a world, instead of invoking both stop paths every client tick.
- Rearm the one-shot guard after returning to the title menu.

## Version 0.67.0 - Safer withdrawal from Outbreak

- Ask for confirmation before disconnecting from a remote co-op session.
- Make Ctrl+0 follow the opt-in debug shortcut setting instead of disconnecting unconditionally from the pause screen.

## Version 0.66.0 - Field guide for Outbreak rules

- Warn that solo exploration leaves pinned players without a rescue.
- Explain that chat demonstrations may depend on race, evolution, items, or mission requirements.
- Clarify that higher difficulty ranks raise general infected stats as well as adding challenges and global events.

## Version 0.65.0 - Movimiento reducido completo

- El brillo de los faroles ahora responde al cursor sin interpolación cuando el movimiento reducido está activo.
- Reutilizar la búsqueda de ventanas animadas durante cada renderizado, sin cambiar su aspecto.

## Version 0.64.0 - Menu audio stops with the world

- Stop the menu soundtrack and ambience as soon as a world becomes active, even if the menu screen no longer ticks during connection or gameplay.

## Version 0.63.0 - Consistent fallback language

- Use Spanish auxiliary copy only for the included es_es locale; unsupported Minecraft languages fall back to English instead of being mislabeled as Spanish.

## Version 0.62.0 - Official server ready to join

- Preselect the pinned Ghoul Outbreak entry when multiplayer opens with no selection; the player still has to press Join.

## Version 0.61.0 - Lighter server-list refresh

- Remove the explicit synchronous server-file reload immediately before vanilla creates the refreshed multiplayer screen; let the new screen load the saved list once.

## Version 0.60.0 - Consistent reduced motion

- Honor the reduced-motion setting across animated controls, title-screen details, logo and pocket watch; status alerts remain static.

## Version 0.59.0 - Official server within reach

- Keep the pinned Ghoul Outbreak server in the first multiplayer row, refresh the visible list immediately, and update its address without adding duplicates.

## Version 0.58.0 - Quiet lore during live play

- Suppress animated discovery toasts while a world is active.
- Keep queued marginalia untouched during live sessions, avoiding file reads/writes and UI sound playback from pause screens; notes remain available when the player returns to the menu.

## Version 0.57.0 - Audio mix status

- Show the current music, ambience, and interface SFX levels in the sound cabinet subtitle, including on compact screens.
- Route title music through Minecraft's Music category so its standard sound control affects the soundtrack as expected.
- Clarify that the sound preview uses the current mod SFX level and that Minecraft's master volume can still mute it.

## Version 0.56.0 - Compact sound cabinet

- Reflowed the sound settings for short windows so the options list, three volume controls, sound test, and return button stay within the paper panel.
- Kept the menu-only silent-record discovery out of live sessions and removed an opaque prefill that hid the paused world behind its fixed veil.
- Preserved sound levels, settings, and immediate in-world scrolling.

## Version 0.55.0 - Spanish copy pass

- Corrected missing accent marks across Spanish menu text and replaced the last Spain-plural instruction with consistent direct address.
- Preserved the English and Spanish locale key set and all screen behavior.

## Version 0.54.0 - Sound check guidance

- Clarified that the interface sound preview uses the current SFX level and that Minecraft's master volume also affects audibility.
- Kept the preview and all UI sounds under the same existing volume controls.

## Version 0.53.0 - Clear field manual

- Polished Spanish field-guide wording with consistent direct address, corrected accents, and clearer revive instructions.
- Kept English and Spanish key counts aligned; no gameplay logic, animation, or active-world rendering changes.

## Version 0.52.0 - Field manual from pause

- Added a pause-menu shortcut to the field guide, with a compact localized label and full tooltip.
- Expanded the pause actions into a responsive six-action layout; returning from the guide now goes back to the pause screen with a matching label.
- Kept the in-world guide as a fixed paper overlay with no screen-transition animation or moving backdrop.

## Version 0.51.0 - Field rescue protocol

- Added a quick response section to the field guide for the global trapped alert, freeing a PIN, reviving a downed teammate, and the loot-only defibrillator.
- Added keyboard navigation for all seven guide sections while preserving existing saved section bookmarks.

## Version 0.50.0 - Localized headers fit every panel

- Added a shared single-line fitting helper for localized headers and flavor lines across the remaining settings, archive, and confirmation screens.
- Wrapped confirmation text to the actual panel width at compact resolutions. Kept English and Spanish content, existing navigation, and gameplay rendering intact.

## Version 0.49.0 - Multiplayer actions for compact screens

- Kept all seven multiplayer actions inside the available panel width at compact resolutions, using short localized labels with full-name tooltips when needed.
- Clamped the multiplayer title and subtitle to the parchment width. Kept the existing action order and keyboard navigation, with no gameplay animation added.


## Version 0.48.0 - Server forms that fit

- Resized direct-connect and server-edit panels, text fields, and action buttons from the actual viewport and panel dimensions.
- Kept both forms' buttons inside their parchment at compact resolutions, clipped long headings and field labels, and retained full action names in hover tooltips.
- Positioned connection feedback inside the direct-connect panel so it remains visible on short screens.

## Version 0.47.0 - Compact survivor pause screen

- Reflowed pause actions into a two-column layout when the available viewport is short, keeping the last action full width and above the hotbar.
- Adapted the paper header and separators to compact viewports, and clipped long subtitles to the paper width.
- Added localized hover labels to pause controls and removed decorative overlays from compact layouts when they would collide with actions.
- Kept the in-world pause panel static and lightweight.

## Version 0.46.0 - Responsive options cabinet

- The options cabinet now fits compact window sizes with smaller, carefully spaced controls and a shorter header.
- Full English and Spanish button names remain available through hover tooltips when labels need truncation.
- Long title and subtitle text is clipped to the paper panel, and the FOV slider follows the adaptive grid width.


## Version 0.45.0 - UI sound control

- Added a persistent interface SFX volume slider and a test sound button to the sound cabinet.
- The slider controls button, hover, scroll, notification and custom UI sound playback while respecting Minecraft's own audio settings.


## Version 0.44.0 - Clearer survivor screens

- Removed the rotating lower-right quarantine dispatch from the title screen.
- Updated the pinned community server address to `Outbreak.exaroton.me:24652`.
- Kept only the primary US English and Spain Spanish locale files.
- Removed the duplicate text pass that made button lettering look too bold.
- Raised UI sound levels and kept Minecraft's normal sound controls in charge.
- Added quiet archival folds to official papers and a fixed, lightweight paper style for screens over a live world.
- Explicitly bypassed diary transitions for the inventory and while a world is active.


## v0.43.0 - Efectos quietos durante la partida

El diario ya no suaviza el desplazamiento ni anima destellos, el espejo, el ojo o el diapasón mientras hay un mundo abierto. La navegación responde de inmediato.

## v0.42.0 - El diario deja respirar la partida

En un mundo, el diario usa un velo y un panel estáticos: pausa cielo, tormenta, grano, partículas, ornamentos animados, transiciones y la interpolación del hover. Las opciones siguen legibles sobre el mundo. El menú principal conserva su ambientación.

## v0.41.0 - Escalas de dificultad sin censura

El manual ahora enumera las dificultades activas hasta INSURGENCY e INFERNUM, además de los rangos BACKBREAKING, DOOMSDAY y ARMAGEDDON marcados WIP. También explica sus eventos globales y ghouls más fuertes.

## v0.40.0 - El marcador sigue el capítulo

El manual conserva el avance relativo dentro del capítulo. Cambiar el tamaño de ventana o la escala de interfaz ya no te envía a otro pasaje; los controles narrados siguen la barra al arrastrarla.

## v0.39.0 - El marcador sobrevive al cierre

El manual de campo conserva dónde ibas incluso después de cerrar el juego. Ábrelo de nuevo y continúa desde el mismo pasaje.

## v0.38.0 - El manual recuerda la lectura

Al volver a abrir el manual de CEDA durante la misma sesión, regresa al pasaje que estabas leyendo. La rueda, los saltos entre capítulos y el arrastre de la barra mantienen actualizado el marcador.

## v0.37.0 - El manual se deja leer

El manual de campo ahora es accesible con el narrador: Tab recorre sus seis capítulos y cada parada anuncia el encabezado junto con sus protocolos. Enter salta al capítulo elegido; el foco queda marcado en azul sobre el papel. El pie también recuerda este recorrido.

## v0.36.0 - El origen de la fiebre

El manual amplía el expediente de infección: recoge que la genética, la salud y las costumbres locales influyen en las mutaciones, y revela el encubrimiento de 1800. Las autoridades llamaron al brote una gripe común; los ghouls siguen vivos y la Gripe Verde no reanima cadáveres.

## v0.35.0 - Saltos entre capítulos

El manual permite saltar directamente entre sus seis capítulos: haz clic en un encabezado o pulsa 1–6. El título se ilumina y subraya al pasar el cursor para señalar que se puede abrir. El pie resume los nuevos atajos de navegación.

## v0.34.0 - Razas del cordón

El manual de campo suma un expediente sobre las razas de los supervivientes: más de 300 posibilidades, desde humanos, tiburones y cíborgs, con evoluciones V1–V4+ sujetas a acciones y misiones propias de cada rareza. También registra la escala completa de rarezas, compartida por razas, artefactos y objetos.

## v0.33.0 - Navegacion del manual

El manual de campo ahora se puede recorrer con rueda, flechas y RePág/AvPág; Inicio y Fin saltan al comienzo o al cierre. La barra lateral también responde al arrastre y el pie explica los controles. Las tres formas de navegación respetan los límites del papel y mantienen el regreso al refugio al alcance.

## v0.32.0 - El manual de campo

La copia de campo de CEDA se abre al pulsar el cartel de cuarentena. Reune los protocolos reales del servidor: los ghouls siguen vivos, los ataques PIN exigen rescate, el derribo se revierte con ayuda, la muerte requiere desfibrilador, los recursos menguan y las dificultades escalan. El manual se desplaza con la rueda, vuelve al cartel y adapta sus textos al idioma del juego; unos detalles azul hielo evocan los ojos de los infectados.

## v0.31.0 - Partes de cuarentena

La portada ahora publica un parte de cuarentena distinto cada jornada: CEDA insiste en que solo es una gripe, mientras los ojos azules y las mutaciones cuentan otra historia. Los avisos también recuerdan que las reservas menguan, que un compañero debe liberar al atrapado y que el grupo ha de replegarse junto. En octubre, el cronista ya no llama muertos a los ghouls: siguen vivos. El parte conserva el margen de la firma y se oculta si la ventana no ofrece espacio.

## v0.30.0 - La hoja entera

Feedback del owner tras la 0.29.0: la vuelta quedo bien, pero la pluma
que escribe solo UN tramo queda rara; la letra de la lupa no se veia;
los ticks de hover muy altos; y hay textos pisados por botones. Backup
externo previo (`/home/user/GripeVerde-backup-pre-v0.30.0-6ac186f.bundle`,
--all, verificado).

- **La pluma escribe la bitacora ENTERA** (fusion de sus dos propuestas):
  desde la entrada vigente hasta el fin del documento, el texto se
  escribe mientras el lector baja (ritmo 340 chars/s tras 300 ms de
  gracia). Regla de piso: lo que queda mas de media pantalla por encima
  se considera impreso y no gasta tinta — bajar de golpe con la barra
  jamas deja huecos, y quien sube encuentra todo seco. El click sigue
  completando todo al instante; con movimiento reducido, hoja entera.
- **La lupa ya se lee**: el cuadrito era tinta sobre tinta (errata
  reconocida) — ahora es etiqueta de papel claro (`PAPER` al 240) con
  letra de tinta; caret bronce legible sobre ella.
- **Ticks de hover -40%**: `VOL_HOVER_MAIN` 0.045→0.028,
  `VOL_HOVER_SECTION` 0.035→0.022 (pedido expreso). Clicks intactos.
- **Fixes de solape (reporte del owner)**:
  - Título: el mensaje del día ya no flota sobre la mesa de botones — se
    lee a la IZQUIERDA de la firma en la misma línea (se omite si no hay
    aire), y la firma jamas entra en la franja del gramofo (max clamp
    x≥48).
  - Gabinete de opciones: la línea "(no: la tinta ya no seca)" del huevo
    Holloway quedaba DEBAJO de la primera fila de botones — ahora clamp
    duro sobre la grilla, divisor siempre por debajo, y flavor/fix con
    elipsis al ancho del papel.
  - Sala de sonido: la cita del flavor se omite si no hay aire real
    sobre los sliders, y con elipsis al ancho del panel.
- Interfaces debidas: pluma y marcas en pixeles logicos dentro del
  scissor; clamps de texto probados en GUI scale 2/3/4 y minimal mode;
  reducedMotion y reduceFlashes respetados en todo lo nuevo (KI-65).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.29.0 - La vuelta y la lupa

Feedback directo del owner tras jugar la 0.28.0: la cronica gusta, la
animacion al SALIR de cada cosa no. Backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.29.0-5dee740.bundle`, --all,
verificado).

- **La vuelta cortes (rediseño de la transicion de regreso)**: volver a
  una mesa ya visitada ya no corre el barrido completo al reves — ahora
  la hoja cae de arriba en 170 ms: velo tenue (110 vs 150) + banda
  superior de tinta que se repliega (46 px → 0) con filo bronce. Sin
  salpicaduras, sin gotas, sin sombra extra. Toda la ceremonia (barrido
  largo, salpicaduras, gotas) queda reservada para AVANZAR, donde gusto.
  La sombra de asiento sigue en ambos sentidos (solo opacidad, 450 ms).
- **Optimizacion del velo** (`DiaryTransitions`): cache de elegibilidad
  por clase (fin de `getName().startsWith` por frame), `reducedMotion`
  resuelto una vez por apertura, duracion y fin de efecto precomputados
  (`durNowMs`/`fxEndMs`) — fuera de la ventana del efecto el render es
  costo cero (dos restas y un return).
- **Fuera el golpe sordo** (pedido expreso): el escritorio ya no suena
  al tocarlo en cualquier lado; `answerWhisper` vuelve a su forma pura
  (solo el susurro abierto contesta) y `UiSounds.silenceKnock` se retira
  del catalogo. `UI_ERROR` sigue cableado en `error()` — 35/35 en uso.
- **La lupa de la bitacora**: cuadrito al pie a la derecha del papel (96
  px, se abre con `/`, Ctrl+F o tocandolo). Escribis una palabra (hasta
  24 caracteres, texto simple sin comodines) y la cronica salta a cada
  aparicion con Enter / Shift+Enter, piquito bronce en cada renglon
  coincidente y fondo tenue en la actual; contador "N/M" (o "0") a la
  izquierda. Esc la cierra y olvida la pregunta. El folio comparte la
  franja: si el panel es angosto (escala 4) se corre a la izquierda —
  jamas se pisan. Caret parpadeante (fijo con destellos reducidos).
- Interfaces debidas: la vuelta corta se ve bien en GUI 2/3/4 (banda
  proporcional, nada anclado al centro), la lupa vive bajo el contenido
  dentro del panel en todas las escalas, y reducedMotion mantiene el
  fundido limpio (sin vuelta ni ceremonia). KI-64.

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.28.0 - Las gotas y los folios

Misma consigna del owner: mejoras significativas en textos y en los efectos
al cambiar de pantalla/interfaz, correctas en TODAS las interfaces, con
backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.28.0-d40c820.bundle`, --all,
verificado).

- **La tinta fresca gotea (transiciones)**: del filo de la cortina se
  desprenden 3 gotas en momentos distintos del pliegue (30/55/80% del
  barrido) que resbalan hasta 26 px EN EL SENTIDO del pliegue con rastro
  fino (trail 1 px, alfa ≤ 40; gota 2x2, alfa ≤ 85) y se desvanecen en
  550 ms. Posiciones por LCG con la semilla de cada apertura. Con
  movimiento reducido: sin gotas (fundido limpio, como siempre).
- **La cronica gana folio**: al pie del papel, centrado, "folio N · M"
  calculado del scroll real (paginas = alto visible; N = hoja actual) con
  ornamentos de bronce a los lados. Bajo el area de contenido y dentro
  del panel: nunca pisa texto, botones ni el folio escondido (ese vive
  dentro del scrolleo). Funciona en GUI 2/3/4 y con resize en vivo.
- **Sello NUEVO en TODAS las entradas saltadas**: si el gabinete avanzo
  mas de una edicion desde `lastSeenVersion` (p.ej. 0.25.2 → 0.28.0),
  todas las cabeceras con triple de version posterior a la ultima leida
  amanecen lacradas esta visita (comparacion numerica por triple, parser
  tolerante). La pluma de 0.27.0 sigue escribiendo SOLO la entrada de la
  version instalada.
- **Tablon del titulo con lineas nuevas**: cada pool de subtitulos
  rotativos pasa de 9 a 12 lineas por idioma (ES/EN), misma voz de
  documento: "Los hospedajes abren sus libretas", "Un cordon se sostiene
  entre todos", "Escriban su nombre en la guardia" (hospedajes) y "Cada
  pieza lleva su sello", "Lo que el cordon permite", "Cuenten las
  lamparas antes de salir" (arsenales).
- Interfaces debidas: gotas solo durante transiciones del gabinete;
  folio y sellos en pixeles logicos dentro del panel; probado contra
  escala GUI 2/3/4, minimal mode, movimiento reducido y destellos
  reducidos (KI-63). Reglas madre intactas: sin gameplay, render Java
  puro.

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.27.0 - El pliegue y la pluma

Misma receta pedida por el owner: mejoras significativas en textos y en los
efectos al cambiar de pantalla/interfaz, bien en TODAS las interfaces, con
backup externo previo
(`/home/user/GripeVerde-backup-pre-v0.27.0-aa0ff79.bundle`, --all,
verificado).

- **El pliegue tiene sentido (direccion de la cortina)**: `DiaryTransitions`
  gana pila de navegacion (`NAV`, acotada a 16) alimentada desde
  `ScreenEventHandler.noteTransition` — la pantalla vieja se captura al
  inicio de `onScreenOpening` y la nueva se lee TRAS el redirect vanilla, en
  los tres puntos de salida del metodo. Avanzar a una mesa no visitada → la
  cortina de tinta baja (como en 0.26.0); regresar a una mesa ya abierta
  (Cerrar, Esc, volver al titulo, salir de la pausa) → la cortina sube, la
  hoja vuelve al pliegue. Sin pista del origen (arranque), siempre baja.
- **El filo salpica**: 5 gotas de tinta diminutas tras el borde de la
  cortina, posiciones por LCG con semilla propia de cada apertura (estables
  durante la transicion), alfas ≤ 95 y cola de 500 ms tras el destape. Sin
  destellos; con movimiento reducido no hay salpicaduras (solo fundido).
- **La hoja se acuesta**: sombra interior de 6 px que decae en 450 ms tras
  la cortina (alfa max 20 → 0). Es solo opacidad — tambien presente con
  movimiento reducido, nunca tapa texto ni botones.
- **V4 implementada — la cronica se escribe a pluma**: la entrada de la
  version recien instalada se escribe letra a letra en su primera visita
  (ritmo minimo 170 chars/s, techo duro de 6,5 s para entradas largas).
  Plumín dibujado al final del ultimo trazo siguiendo el wrap real del
  font (tambien la capital del encabezado), parpadeo de tinta suave (fijo
  en 170 con destellos reducidos) y rasguño quedo (`UI_SCROLL`, 0.045f,
  un golpe por tramo de 42 chars con throttle de 120 ms). Un click la
  termina al instante; con movimiento reducido la hoja llega entera.
  Persistencia: la misma `lastSeenVersion` de 0.24.0 — solo la primera
  visita de cada entrada se escribe.
- Interfaces debidas: todo en pixeles logicos y dentro del scissor de la
  cronica; layout intacto; probado contra escala GUI 2/3/4, minimal mode,
  movimiento reducido y destellos reducidos (KI-62). Sin gameplay, sin
  resourcepacks: render Java puro (reglas madre intactas).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8 sellos
+ balance OK.

## v0.26.0 - Las hojas y los faroles

Entrega pedida por el owner: mejoras visibles significativas ("efectos al
cambiar de pantalla/interfaz, etc"), revision de textos y, antes de tocar
nada, backup externo
(`/home/user/GripeVerde-backup-pre-v0.26.0-18e178d.bundle`, --all).

- **El pasar de las hojas (transicion entre interfaces)**: nueva
  `DiaryTransitions` enganchada a `ScreenEvent.Render.Post` en
  `ScreenEventHandler` — al abrir cualquier pantalla del paquete
  `client.screens` (titulo, opciones, cronica, archivo, sonido, pausa...),
  una cortina de tinta baja revelando la hoja nueva en ~260 ms con velo
  sepia disolvente y franja de tinta con filo de bronce. Con movimiento
  reducido solo fundido de 130 ms, sin barrido. Pantallas vanilla/ajenas:
  cero efecto. Dibuja en Render.Post: cubre botones incluidos solo esos
  260 ms, nunca interfiere con clics (es impermeable al input).
- **Faroles reactivos (V6 de REVISION_MOD)**: `LivingAtmosphere.setMouse`
  desde el titulo + `lanternHover[]` suavizado por tick (ease ~110 ms);
  la mano cerca de un farol (radio 30 px sobre la lampara) levanta su
  llama y ensancha su halo x1.5, todo con topes 255 (withAlpha no
  clampea). Funciona con movimiento reducido (no hay movimiento nuevo,
  solo respiracion de brillo) y en cualquier escala GUI/altura (4-6
  faroles segun alto).
- **Revision de textos (crónicas in-game)**: reescritas las entradas
  0.24.1/0.25.0/0.25.1 en los 25 idiomas con vocabulario de documento de
  la vida real — fuera "hexadecimal", "crossfade", "literal numerico",
  "clic"; dentro "rotativa", "cilindro madre", "toque", "sello de
  imprenta".
- **El gabinete afina el oido (V2 de REVISION_MOD, resto)**: los 8 sonidos
  legacy por fin suenan con sus eventos propios (estaban grabados y
  registrados; faltaba el cable) — `UiSounds.open/close/modal/tab/error/
  cleanClick/clickCreate/respawn` cableados a `UI_OPEN/UI_CLOSE/UI_MODAL/
  UI_TAB/UI_ERROR/UI_BUTTON_CLEAN/UI_CLICK_CREATE/UI_RESPAWN`. Tres
  momentos nuevos audibles: el escritorio responde con golpe sordo
  (`UiSounds.silenceKnock`, cooldown 480 ms) cuando tocas la vigilia sin
  susurro en el aire; el gramofono del titulo anuncia pieza nueva con
  `clickCreate` (SlimButton gana hook `withClickSound`, los demas botones
  intactos); y volver al titulo desde otra mesa te "vuelve a sentar" con
  `respawn` (`onArriveTitle` en ScreenEventHandler; la primera apertura
  del arranque no suena). Allowlist del sello de uso de sonidos vacia en
  Gradle y en el espejo Python: 35/35 eventos en uso.
- ERICA recuerda: este mod VIVE en interfaces; cada cambio visual va
  probado contra escala GUI 2/3/4 y minimal mode (KI-61).

Conteos: 61 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(los 35 en uso; allowlist legacy vacia), 42 OGG. Espejo Python: 8/8
sellos + balance OK.

## v0.25.2 - El telegrama no pisa la mesa

Hotfix de layout (parche), reportado por el owner probando GUI scale 3-4:

- **Cartel del easter egg (telegrama "CASO")**: los dos clamps de Y se
  anulaban entre si — uno lo subia para esquivar la mesa de botones y el
  siguiente lo volvia a clavar en `safeTop` fijo, de modo que con poca
  pantalla logica (escala 3-4) el cartel caia encima de los botones y el
  texto quedaba ilegible. Regla nueva y monotona: jamas tocar la zona de
  botones, jamas salirse por arriba; si no hay banda libre, el cartel sube
  sobre la franja del logo. Ademas `boxW` ahora tiene techo en el ancho
  real de pantalla.
- **Sello FRAUDE** (cartel del elixir, tras HOLLOWAY): estaba fijado a 46
  px del borde inferior del poster; en carteles bajos (escala 3-4) se
  montaba sobre las lineas del cuerpo y podia salirse del papel. Ahora se
  estampa siempre DESPUES del texto (`cursorY + 6`), nunca por encima, y
  con tope duro dentro del poster, incluido clamp horizontal.
- **Tooling/documentos**: la ruta de deploy vigente queda documentada como
  comandos PowerShell INLINE (fetch+reset+build+copia por glob), sin .ps1
  nuevos, y la regla permanente del owner: cada entrega termina con ese
  bloque listo para pegar. `tools\deploy.ps1` queda como alternativa
  corregida.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos, 42 OGG.
Espejo Python: 8/8 sellos + balance OK.

## v0.25.1 - El cilindro original

Hotfix de contenido (parche): la lluvia sintetizada de 0.24.0 "sonaba
bugeada" (reporte del owner al escucharla en cliente). Decision: retirar
la sintesis y restaurar los masters originales de lluvia y viento,
amplificados en lugar de regrabados — usar "los sonidos que ya existian",
pedido expreso del owner.

- **Lluvia**: master original (el de -32 dB RMS) con ganancia pura,
  normalizado a pico ~0.83 sin tocar la cresta (~19): nada de limitadores
  (el primer intento de soft-knee genero NaN y picos >1.0 — descartado y
  rehecho desde el master). Su volumen de mezcla sube 0.30 -> 0.45 en
  `MenuAmbience` para que vuelva a sonar clara sin siseo aplastado.
- **Viento**: master original (el de -64 dB RMS) amplificado a RMS ~0.085
  (pico 0.80 tras decodificacion).
- **Costuras medidas**: ambos masters empalmaban limpios (seam 0.0076 y
  0.0000), asi que no llevan crossfade: loops identicos a los originales.
- `tools/audio/regenerate_menu_ambience.py` queda archivado con nota en
  cabecera: ya no es la fuente de esos dos loops (registro historico del
  intento de sintesis).

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.25.0 - El que mira la tormenta

Entrega menor de easter eggs pedida por el owner ("algunos easter eggs
mas"): tres secretos nuevos en la vigilia, cableados sobre mecanica que ya
existia (tormenta, lluvia, telegrafo oculto).

- `storm_watcher`: `LivingAtmosphere` cuenta ahora cada relampago que nace
  (programado o forzado) y la pantalla del titulo marca una linea base al
  abrirse en noche de tormenta: al tercer destello nuevo, ficha y nota
  (`N_FLASH3`). Con destellos reducidos no hay relampagos y la ficha no se
  regala: respeta accesibilidad.
- `drop_catcher`: la lluvia es tocable. `LivingAtmosphere.rainDropAt` hace
  hit-test contra las 40 gotas en vuelo con tolerancia generosa; atrapar una
  la devuelve al cielo (`scareDrop`), suena un plink de aguja (`menu/tick`
  a 1.9) y suma ficha + nota (`N_DROP`). La zona de botones queda excluida:
  ningun clic sobre la mesa se pierde por culpa de una gota.
- `code_doctor`: palabra nueva del telegrafo oculto — escribir DOCTOR en la
  vigilia trae susurro y campana baja, ficha, contador `doctor_signed` y
  nota (`N_DOCTOR`). El elixir tuvo un medico; el oficio sigue en pie.

El archivo crece a 52 fichas; nombres y pistas viven en el registry bilingue
de `Discoveries` (cero claves lang nuevas: el conteo sigue en 93). Notas de
Marginalia nuevas: N_FLASH3, N_DROP, N_DOCTOR.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.24.1 - La errata del cronista

Hotfix de build (parche): la semilla de las gotas de lluvia de V5 se escribio
como `0xD20PL` —la P no es digito hexadecimal— y la 0.24.0 no compilaba
(`error: malformed floating-point literal` en `PaperTexture.java:95`,
detectado por el owner en el primer `deploy.ps1`; en el sandbox no hay JDK
y el espejo Python solo verifica balance de llaves, por eso se escapo).
Corregida a `0xCA1DAL` ("la caida"): comportamiento identico, gotas
deterministas con otra secuencia. Para que ninguna errata de esta familia
vuelva a salir sellada, el espejo Python suma un octavo sello:
`verifyJavaLiterals` valida todos los literales numericos (hex/binario/
octal/decimal/flotante, sufijos incluidos) de los 60 .java tras retirar
comentarios y cadenas. Sin cambios funcionales: el contenido de 0.24.0
queda intacto. Para Agus y para cualquier distribucion: usar 0.24.1 —la
0.24.0 jamas existio como jar.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 8/8 sellos +
balance OK.

## v0.24.0 - El viento vuelve a soplar

Entrega menor de superficie visible, pedida por el owner junto al segundo
crash-log de Agus: "corrige que no se escucha el ambiente", mejoras visibles
(V1+V3+V5) y mas easter eggs.

El ambiente no se oia por contenido, no por codigo: el loop de viento estaba
grabado a -64 dB RMS (inaudible a cualquier ganancia razonable) y la lluvia
a -32 dB. Se regeneran viento y lluvia por sintesis procedural
(`tools/audio/regenerate_menu_ambience.py`, reproducible e idempotente,
con crossfade de extremos para loops sin chasquido) y se refuerzan ciudad,
susurros y un paso. El regulador de ambiente por fin manda sobre algo, y de
regalo el gabinete deja de sonar dentro de los mundos (fuga historica:
los loops seguian activos con un nivel cargado).

Mejoras visibles: la Cronica se abre en la entrada de la version instalada
tras cada actualizacion y la marca con un sello de lacre "NUEVO" durante la
visita (V1; `ChroniclerMemory.lastSeenVersion`); algunas sesiones (~14%)
arrancan en Noche de tormenta, con lluvia doble, relampagos frecuentes,
mezcla ambiente al mando de la lluvia y truenos casi seguros (V3); y la
lluvia resbala en gotas por todos los pergaminos (V5; quieta con movimiento
reducido). Easter eggs nuevos: el lacre del telegrama se rompe a golpes
(`seal_breaker`, estrena `ui.paper_fall`) y un diapason oculto junto a los
reguladores afina la sala (`sound_keeper`, estrena `ui.success`); el
descubrimiento `storm_night` completa el trio. El archivo crece a 50 fichas
y sus textos de "cuarenta y tantas fichas" ahora computan la cifra solos.

Conteos: 60 Java, 25 lang (93 claves), 25 cronicas, 35 eventos de sonido
(33 en uso; 8 legacy en allowlist), 42 OGG. Espejo Python: 7/7 sellos +
balance OK.

## v0.23.2 - El telegrama llega intacto

Hotfix de crash en campo (parche): el easter egg que dispara el clic en el
sello del titulo podia tumbar el cliente en el primer fotograma. El efecto
maquina de escribir abre el telegrama con 0 letras visibles durante los
primeros ~22 ms, `font.split("")` devuelve una lista vacia y el dibuje
indexaba `lines.get(0)` tras forzar un minimo de un renglon:
`ArrayIndexOutOfBoundsException` en
`DiaryTitleScreen.drawEasterEggMessage`, con crashes en cadena bajo
NotEnoughCrashes. Reportado por un jugador real con Java 17 y la edicion
v0.22.0 (gracias por el registro completo).

Correccion: el conteo de renglones del telegrama queda acotado por el
tamano real de la lista (0 permitido: el sobre espera su primera letra) y
la llamada al overlay, puramente decorativa, queda envuelta para que
ningun fallo de ornamento pueda volver a detener el render del cliente.
Se auditaron ademas los otros 5 usos de `font.split` del codigo
(ChangelogScreen, DiaryArchiveScreen): todos iteran en forma segura ante
listas vacias, y el resto de los accesos indexados a listas ya tenian su
guardia de vacio correspondiente.

## v0.23.1 - Sellos y relojería

Entrega de mantenimiento dura (sin cambios de escena, por eso va como
parche): el owner autorizo implementar el lote recomendado de la revision.

Dos sellos nuevos de build: `verifyGripeVerdeSubtitles` (toda clave de
subtitulo no vacia de sounds.json debe existir en los 25 idiomas) y
`verifyGripeVerdeSoundUsage` (ningun SoundEvent registrado puede quedar
sin uso en el codigo; los 10 eventos legacy sin uso quedan en allowlist
grandfathered, documentados en ModSounds, hasta decidir su destino).

La memoria del cronista gana sanitize de bounds (numeros imposibles de un
JSON tocado a mano se corrigen en silencio) y un pipeline de migracion
versionada documentado (SCHEMA_VERSION + pasos N→N+1) para futuros
esquemas. La capa de ambiente (MenuAmbience) migra al mismo reloj
monotonico nanoTime que la musica, junto con el rastreo de calle de
LivingAtmosphere; los temporizadores del mod ya no dependen del reloj de
pared del SO.

La CI se entrega lista para activar con `tools/ci/activar-ci.ps1` (la
sesion no tiene permiso `workflows`; el script la activa con las
credenciales locales del owner en un paso).

## v0.23.0 - El gramófono guarda la palabra

Pasada transversal pedida expresamente tras la revision tecnica del
2026-08-23 (ver REVISION_MOD.md), sin gameplay ni resourcepacks y con
backup externo verificado antes de tocar archivos.

Audio blindado: la banda sonora vanilla del menu calla mientras el gabinete
esta abierto (MusicManager disparaba Musics.MENU a los 20-600 ticks y sonaba
encima de las pistas propias), y un watchdog detecta instancias fantasma del
motor de sonido: tras un F3+T la playlist vuelve sola en vez de quedar muda
para siempre. Los temporizadores de musica, cooldowns UI y duracion de pistas
usan ahora reloj monotonico (nanoTime). La escritura de la memoria del
cronista (temporal + force + reemplazo atomico) sale del hilo de render hacia
un executor monohilo, sin micro-tirones y con cierre ordenado.

Accesibilidad: nueva ayuda visible "Destellos reducidos" (fotosensibilidad,
separada de Movimiento reducido) que apaga relampagos sin congelar el resto
de la escena; los ocho eventos ambientales (pasos, madera, puerta, susurro,
campana, cuervos, cadenas, trueno) ganan subtitulos reales en los 25 idiomas;
el deslizador de ambiente corrige su etiqueta hardcodeada y usa la clave
traducida que ya existia.

Ingenieria: el mod ya no registra manualmente su escucha de pantallas
(eliminada la doble entrega de eventos y el crash latente en servidores
dedicados; el mod queda inerte fuera del cliente), se retira por completo la
infraestructura de Mixins vacia (JSON, plugin, refmap, mods.toml) y el
namespace de GameTest sin tests, y el atajo oculto Ctrl+0 pasa a ser opt-in
via config. Las pantallas solo se reemplazan cuando la clase es exactamente
vanilla (ningun mod de menu ajeno es barrido) y ambos reemplazos pueden
apagarse por config para convivir con otros mods. El build crece con dos
sellos nuevos (sincronia de version Gradle/VERSION y duraciones reales de
las pistas OGG parseadas como Vorbis) y una CI de GitHub Actions compila
cada push con JDK 17 y publica el jar como artifact: el bloqueo de KI-52 por
falta de Java 17 en el sandbox tiene ahora via automatica de validacion.
Conteos: 60 clases Java, 25 idiomas con 93 claves, 25 cronicas, 35 eventos,
42 OGG. Version final 0.23.0.

El tag y el release v0.23.0 se reservan hasta completar en cliente real la
validacion registrada como KI-53 (ver KNOWN_ISSUES.md).

## v0.22.0 - La vigilia queda sellada

La simulacion atmosferica y el audio quedan reunidos en un tick fijo de
20 Hz: el polvo ya no acelera con los FPS y se eliminan los ticks redundantes
desde render. Movimiento reducido pasa a la pantalla de Accesibilidad y
congela cielo, clima, humo, particulas, grano y destellos sin borrar calle,
faroles ni utileria; los toggles anuncian tambien su estado al narrador.

La memoria del cronista deja de sobrescribir su unico JSON: escribe un
temporal durable, conserva la ultima copia valida, reemplaza atomicamente y
recupera un temporal interrumpido o el backup cuando encuentra corrupcion,
preservando el archivo dañado. El papel y las ventanas reutilizan RNGs
deterministas para reducir asignaciones sin cambiar su aspecto. El build
valida paridad de idiomas, changelogs y el registro sonoro completo. Version
final 0.22.0.

El expediente queda listo para revision del owner en la PR 14. El tag y el
release v0.22.0 se reservan hasta completar en un cliente real la validacion
registrada como KI-52.

## v0.21.31 - La vigilia permanece entera

Se elimino por completo el modo automatico de pocos FPS: ya no existe el
muestreador, la escena del Title siempre conserva niebla, faroles, reflejos,
particulas y utileria, y el grano vuelve a un intervalo estable. Tambien se
limpio el estado heredado y el ciclo erratico que podian debilitar un farol
o suprimir su reflejo.
El objeto de madera de la carretera fue identificado como el kiosco del
Heraldo y retirado junto con su hitbox, periodico diario, overlay, clase y
ficha de descubrimiento. El archivo cuenta ahora 46 fichas activas.

## v0.21.30 - La calle permanece

Cuando la maquina que sostiene la vigilia aminora el paso, la ciudad aun
puede desprenderse de la niebla y apagar los adornos mas costosos. La
calzada, sin embargo, ya no desaparece con ellos: el empedrado mojado
permanece bajo los edificios y devuelve suelo a la vigilia.

## v0.21.24 - Ajustes pedidos en cliente

Registro tecnico: se retiro el sonido de madera al abrir el gabinete, el
cajon con la llave y su vitrina, el juego de los tres golpes sobre la mesa
y el despegue de la hoja de cuarentena; el titulo del cartel y los sellos
de tinta pierden el rojo sombreado; el libro de visitas del registro de
hospedajes se ve y responde mejor; y las atribuciones usan etiquetas en
titulo normal.

## v0.21.23 - Se reconstruye desde el registro previo

Registro tecnico: se localizo en el historial el estado anterior a la
version que altero las interfaces (el registro v0.17.2) y desde el se
reconstruyeron las diecisiete salas del gabinete a su forma original:
cabeceras propias de cada sala, sin carteles ni paneles de la epoca
revertida. Se conservan intactas las demas versiones: el arreglo del
registro sonoro, la limpieza, la memoria del gabinete con sus cuarenta y
siete fichas, el periodico, el telegrafo y los arreglos de audio y
rendimiento. El sello que cierra el archivo, las fichas de la limpieza y
los avisos de las cien aperturas siguen activos.

## v0.21.22 - Hotfix compilacion

Registro tecnico: la limpieza de la v0.21.21 dejo una firma de metodo rota
en el gabinete que impedia compilar. Corregido.

## v0.21.21 - Se termina la deuda de los no-op

Registro tecnico: se eliminaron las ultimas treinta llamadas a los carteles
que ya no se dibujan, los tres helpers muertos que las atendian, las
variables que los alimentaban y el pie de registro de la cronica. Las
barras de desplazamiento de tres salas, que habian quedado referenciando
medidas eliminadas, se anclaron al borde derecho del panel. Sin cambios
visibles de fondo ni de gameplay.

## v0.21.20 - La tinta pesa menos

Registro tecnico: limpieza profunda tras la reversion. Las cabeceras se
redujeron a la llamada simple (se eliminaron mas de ciento cincuenta lineas
de textos documentales muertos), se retiraron metodos y variables que ya no
alimentaban a nada (incluido el carril lateral del gabinete), y el grid de
botones del gabinete quedo mas cerca de su cabecera. Sin cambios de
gameplay ni de recursos.

## v0.21.19 - Se corrigen scrolls y posiciones tras la reversion

Registro tecnico: tras retirar los carteles y reducir las cabeceras al
titulo, varias salas quedaron con las listas y los mandos desplazados. Se
realinearon las listas del registro de hospedajes, la sala acustica, la
correspondencia y la estacion de comunicaciones, los botones del manual de
campo, y el campo de busqueda de la sala de consulta quedo en la esquina
superior derecha, fuera de las fichas.

## v0.21.18 - Se restaura lo que no era de la version revertida

Registro tecnico: se reviso la reversion con el historial completo y se
devolvieron los elementos que no pertenecian a los pases de interfaz
revertidos: el sello dorado que cierra el archivo al reunir las cuarenta y
siete fichas, el medidor de ritmo del aparato de observacion, el medidor de
registros del archivo de lenguas y el marco de las atribuciones. Los
medidores de las salas de la epoca revertida (gramofono, ambiente,
disciplina, campo visual y compania) quedan retirados.

## v0.21.17 - Se retiran los medidores y el resto de carteles

Registro tecnico: continuacion de la reversion. Se retiraron los medidores
con etiqueta (gramofono, ambiente, disciplina, campo visual, ritmo y
compania), el sello de archivo completo, el pie de registro de la cronica,
los carteles de grupo del gabinete y el marco oficial de las atribuciones.
Quedan solo titulo, contenido y mandos.

## v0.21.16 - Se retiran los carteles de interfaz

Registro tecnico: reversion de la identidad visual agregada por los pases
de interfaz. Las cabeceras vuelven al titulo simple; los carteles, tableros
y casilleros de las salas (correspondencia, comunicaciones, consulta,
sonido, controles, lenguas, aspecto) fueron retirados; las tarjetas y
railes laterales dejaron de dibujarse. Se conserva todo lo funcional
(botones, listas, busqueda, memoria, libro de visitas) y los arreglos.

## v0.21.15 - Se retiran los sellos de estado

Registro tecnico: se retiraron los sellos de estado de las cabeceras de
todas las salas (REGISTRABLE, LISTO, PENDIENTE, READY y compania) y el
sello del carril del gabinete; las frases bajo los subtitulos recuperan
todo el ancho. Continuacion de la reversion de la v0.21.14. Sin cambios de
gameplay ni de recursos.

## v0.21.14 - El gabinete vuelve a respirar

Registro tecnico: reversiones pedidas en cliente. Se retiro el saludo que
aparecia sobre los botones del menu principal; en el registro de hospedajes
la lista vuelve a empezar debajo de la cabecera, desaparece la tarjeta del
hospedaje elegido y el aviso del periodico; en las autorizaciones se van el
cartel de orden, la linea de la mano autorizante y el sello de estado; y
las frases bajo los subtitulos ya no se topan con los sellos de estado en
ninguna sala. Sin cambios de gameplay ni de recursos.

## v0.21.13 - El expediente se cierra

La sala de consulta, cuando por fin reune todas las fichas, deja de decir
que faltan. El archivo se declara completo, un sello de cera dorada lo
firma con la palabra SIGILLUM, y el cronista anota al margen: cuarenta y
siete fichas; el cajon ya esta vacio; sellalo. La campana del campanario
suena una vez, grave, para que nadie lo dude.

## v0.21.12 - El marco de la pausa queda en su sitio

Registro tecnico: el marco de oro del diario de pausa fue escrito en su
lugar definitivo, sin piezas sueltas. El gabinete, ademas, quedo sin un
solo metodo o campo privado sin uso: todo lo que existe se usa.

## v0.21.11 - La sala respira bajo la mano

La sala de consulta ahora responde al tacto: la ficha bajo el dedo se alza
un instante, y el medidor del archivo late con un pulso tenue, como un
corazon de papel. La memoria del cronista ya no se escribe a cada paso: se
asienta de tanto en tanto, y al apagar la lampara se guarda entera antes de
que la casa quede a oscuras.

## v0.21.10 - La estacion parpadea al cambiar

Registro tecnico: el medidor de alcance de la estacion de comunicaciones ya
no salta de golpe al mover los avisos: se desliza como lo haria una aguja
sobre papel. Se retiraron quince tonos de tinta que ninguna sala usaba, y
la vigilancia de codigo muerto aprende a nombrar lo que fue desterrado.

## v0.21.9 - La tinta pesa menos por frame

Registro tecnico: el gabinete consultaba el idioma del lector decenas de
veces por cada parpadeo; ahora lo pregunta una sola vez cada instante y se
lo guarda. El texto centrado se escribe con una sola mano en cuatro salas,
y el registro de hospedajes muestra, junto al libro de visitas, el titular
del periodico del dia. Se retiraron campos y metodos que nadie usaba, y se
anadio la guia de contribucion al proyecto.

## v0.21.8 - Una sola mano para el idioma

Registro tecnico: siete salas del gabinete consultaban el idioma del lector
cada una con su propia receta; ahora todas preguntan a la misma mano, y se
retiraron los ornamentos de marco que nadie usaba. Sin cambios visibles ni
de gameplay.

## v0.21.7 - El gabinete sin peso muerto

Registro tecnico: consolidacion y limpieza. Se eliminaron 9 clases sin uso
o duplicadas (incluidas las herramientas de desarrollo inalcanzables y los
dos sliders de sonido, combinados en el generico), 7 metodos muertos y 57
claves de idioma huerfanas; el pipeline de 14 pantallas comparte un unico
helper de panel. Sin cambios de gameplay ni de recursos; la sala de sonido
unifica el aspecto de sus dos reguladores. 59 clases Java, 25 idiomas, 35
eventos de sonido, 42 OGG.

## v0.21.6 - El heraldo cuenta la limpieza

El periodico del kiosco encontro palabras nuevas para la noche: el gabinete
del distrito vaciado de papeles viejos, un farol del tejado que parpadeo
tres veces y se apago solo, el gato que bajo al kiosco y no robo nada. La
letra menuda de la segunda columna tambien cambia de amanecer en amanecer.
Y quien conozca la palabra que abre el archivo podra pedirle a la sala que
le muestre las fichas que faltan.

## v0.21.5 - La sala responde a la consulta

La sala de consulta aprendio a responder. Quien busque entre las fichas por
su nombre, por su pista o por la palabra que las nombra, vera solo las que
importan; y cuando el archivo confiese tres fantasmas de la limpieza, un
cuarto recuerda la mesa que nunca se uso. La musica cede un instante cuando
el archivo habla, y cuando la maquina que sostiene la vigilia va lenta, la
calle se despoja de niebla y faroles para no detenerse.

## v0.21.4 - La tinta se asienta

Esta noche el gabinete ordeno sus utensilios. El manual de campo acepta que se
le pregunte por nombre: quien busque entre sus paginas encuentra el mando que
necesita sin revolver el resto. El aparato de observacion separa sus
secciones —lo que pinta, lo que muestra, lo que mide— y anade una aguja que
lee el pulso de la maquina. Los consulados de lenguas declaran, al pasar la
mano, cuantas paginas de su registro guarda el gabinete. Y en la sala de
consulta, al abrir la puerta, un papel cae al suelo.

El gabinete tambien se ordeno por dentro: se retiraron referencias que ya no
nombraban a nada, y se anadio una vigilancia que avisa si vuelve a aparecer lo
que fue desterrado.

## v0.21.3 - El gabinete respira mas ligero

Esta noche el gabinete aprendio a no gastar fuerzas. La ciudad del cristal
repite sus nubes, su humo y su lluvia con la misma semilla, como si la
vigilia se guardara en la memoria; y quien lo pida puede aquietar el
movimiento de la escena entera: el farol deja de temblar, la polilla se
posa, la rata deja de cruzar y el papel espera el viento.

El archivo, ademas, confiesa tres cosas que retiro en la limpieza: el
archivo que se adelgaza, el tema que nunca sono y la calle que nunca
existio. Quien pronuncie la palabra de la limpieza recibira un telegrama
del propio cronista; y quien encuentre dos de esas confesiones podra pedir
la tercera.

## v0.21.2 - Limpieza profunda del proyecto

La oficina no cambió de aspecto, pero sí de peso. Tras cerrar filas en v0.21.0 y corregir el registro sonoro en v0.21.1, el gabinete se sometió a una auditoría completa: todo lo que ya no servía fue retirado, y todo lo que estaba desactualizado fue puesto al día.

Se eliminaron los tres documentos históricos de auditoría (v0.14.x), las muestras de audio de prueba, una textura de 2.1M sin uso (`bg_town.png`), el framework inerte de temas (`ThemeManager`, `GhoulPalette`, componentes de overlay sin registrar) y tres widgets muertos (`IconButton`, `SilentButton`, `DiarySeparator`). `TextureAssets` perdió su constante muerta y `ScreenEventHandler` dejó de delegar a un ThemeManager que no hacía nada.

Se actualizaron `README.md` (v0.21.2 + nota de limpieza), `CONTEXTO_PARA_IA.md` (reescrito a v0.21.2, 68 clases, 0 PNG), `CHANGELOG.md` (añadidos v0.19.0 y v0.17.x faltantes), 25 changelogs in-game (entrada v0.21.1 y v0.21.2), `CONTEXTO.md` (nuevo addendum de limpieza) y `KNOWN_ISSUES.md` (sección v0.21.1 ampliada a v0.21.2).

El resultado: 68 clases Java (vs 83 en v0.17.2), 25 idiomas, 25 crónicas, 42 OGG, 0 PNG de contenedores, working tree limpio y sin PRs abiertos.

## v0.21.1 - Correccion del registro sonoro

La oficina no cambio de aspecto esta noche, pero si de disciplina interna.
Los sonidos del gabinete quedaron registrados de una manera mas segura para
que el archivo pueda abrirse sin tropezar al comienzo de la vigilia.

## v0.21.0 - La oficina aprende a cerrar filas

No todas las salas del gabinete habian alcanzado la misma madurez. Algunas,
aun bajo buena tinta, seguian recordando demasiado a un formulario prestado.
Esta noche se corrigio ese desajuste. La oficina de correspondencia, la
estacion de comunicaciones, la sala de consulta, el registro de revisiones y
las fichas de entrada fueron ordenadas con una mano mas firme, de modo que
cada una se reconozca al instante por su oficio y no solo por su titulo.

Hay menos rigidez de tablero y mas disciplina de archivo. Las hojas de ruta
se llenan como formularios verdaderos; la correspondencia se reparte como en
un casillero de oficina; las senales del distrito se leen como trafico de
estacion; y las fichas de consulta muestran mejor su peso dentro del mueble.

## v0.20.0 - El gabinete encuentra su propia mano

El gabinete ya tenia su estructura; esta noche aprendio a comportarse como
una obra terminada. Las cabeceras dejaron de alzar tanto la voz, los paneles
se ordenaron con mas aire, y las piezas de mando perdieron rigidez de tablero
para parecer objetos reales de una mesa de trabajo: placas, reglas, railes,
instrumentos y hojas superpuestas. El lector ya no ve solo opciones reunidas
sobre pergamino; ve una oficina entera donde cada sala tiene su propia manera
de presentarse.

La sala acustica mide el pulso de sus canales; el aparato de observacion
expone sus calibraciones con modales de laboratorio; el manual de mandos
resume el porte de la mano; la correspondencia distingue el correo del rumor;
las lenguas conocidas lucen como un archivo vivo; la portada y la pausa se
sienten mas hermanas del gabinete central. Todo ello sin vestir la ciudad con
texturas ajenas: solo papel, tinta, sombra, metal apagado y composicion.

## v0.19.0 - El acero se retira de nuevo, la interfaz respira

El gabinete volvió a mirar sus propias manos y decidió que el acero de cuarentena,
aunque vistoso, no le pertenecía. Las planchas que cubrían los contenedores en
v0.17.0-v0.17.2 fueron retiradas de nuevo por directiva explícita: nada de
`gripeverde_steel`, nada de texturas vanilla modificadas. La identidad visual
debía venir del propio mod —papel, tinta, sombra, metal apagado y composición—
no de un resource pack superpuesto.

Con la mesa limpia, se hizo un pase transversal de interfaz: cabeceras
documentales con expediente y estado, rieles internos, tarjetas laterales,
medidores con marcas, revelado de paneles y scaffolding de profundidad.
`ExpedientHeader` y `DiaryChrome` evolucionaron hacia un lenguaje menos cuadrado:
rieles incompletos, cápsulas discretas, scrollbars más físicas. `DiaryVideoSettingsScreen`
nació como consola técnica propia. Options, Sound, Controls, Chat, Accessibility,
Language, Online, Credits, Multiplayer y Pause se pulieron para parecer salas del
mismo gabinete, no el mismo template.

## v0.18.0 - El gabinete toma forma definitiva

La casa del cronista ha dejado de apoyarse en la pura costumbre. Esta noche,
por fin, sus salas parecen haber sido pensadas como un mismo oficio. El
registro central abre con una mesa mejor ordenada; cada ala del gabinete lleva
su propia cabecera, su referencia de archivo, su nota de estado y sus piezas
laterales. El archivo de lenguas, la sala acustica, el manual de mandos, la
hoja de observacion, el escritorio de correspondencia y el acta final de
atribuciones ya no son la misma hoja con distinto titulo: cada una conserva la
familia del gabinete, pero muestra su propio caracter.

Los medidores del sonido respiran, los listados quedan encuadernados dentro de
sus railes, los paneles se revelan con mas ceremonia, y hasta la portada de la
vigilia dispone ahora sus rutas y mandos con mejor jerarquia. No hubo que
vestir nada con planchas ajenas: el oficio se logro con la propia mano del
mod, sobre el papel, la tinta, la sombra y la luz.

## v0.17.2 - Las bandejas de la mesa y la moneda del trato

Parche de mantenimiento sobre el acero de cuarentena. El slot universal pasó
de 18px a 26px, las transparencias fueron repasadas para que el pergamino
respire mejor, y el viejo `tabs.png` fue reemplazado por sprites individuales
`tab_top` / `tab_bottom` selected/unselected 1-7. Remaches y cepillado más
discretos, sellos de cuarentena con mejor lectura. La geometría vanilla
permaneció intacta: solo presentación.

## v0.17.1 - Remaches bien puestos

Segunda mano sobre el acero. Se ajustaron los remaches, el brillo del cepillado
y las juntas de los contenedores para que el acero pareciera realmente clavado
a la mesa, no pintado. Correcciones menores de alineación en hornos y cofres.

## v0.17.0 - El acero vuelve a la cuarentena

El gabinete volvió a vestirse con planchas de acero. Tras el retiro completo
del resource pack en v0.15.0, esta versión reintrodujo `gripeverde_steel` como
pack built-in REQUIRED: 20 contenedores + 4 fondos creativos + sprites de
contenedores, widgets y recipe_book. Presentación 1:1, geometría vanilla intacta,
solo acero cepillado, slots hundidos, juntas con remaches y sellos de cuarentena.
`ResourcePackHandler` registraba el pack via `AddPackFindersEvent`, y
`verifyGripeVerdeThemeAssets` verificaba dimensiones, alpha y ausencia de huérfanas.
La línea v0.17.0-v0.17.2 fue luego retirada de nuevo en v0.19.0 por directiva de
no usar resourcepacks.

## v0.16.0 - La sala de consulta

El gabinete ha dejado de ser un mueble. Esta noche aprendio a recordar.

Todo lo que el lector encuentra queda escrito en una memoria que no se
borra al cerrar los ojos: las fichas halladas, los objetos pequenos, las
noches seguidas, las ausencias largas. El polvo se acumula sobre los
papeles cuando nadie vuelve durante dias, y la mesa de trabajo sabe ya
cuantas manos la abrieron. El gabinete saluda al que llega por primera
vez, reconoce al que regresa, y si alguien se queda demasiado quieto
frente a los folios, pregunta en voz baja si sigue ahi.

El cronista ya no escribe solo. Deja notas al margen que responden a lo
que el lector hace, corrige su propia letra cuando algo lo contraria, y
firma distinto cuando oye su nombre dicho en voz alta. Hay quien jura que
la madera devolvio tres golpes.

La calle se deja tocar: los faroles se apagan y se encienden a mano, y si
la avenida entera queda a oscuras, algo cambia para siempre. El gato del
tejado paga sus deudas despacio, a razon de un jiron por visita. La rata,
a veces, se detiene a mirar. Cuando doblan las campanas conviene esperar:
un carro cruza despues, muy despacio, y nadie lo conduce con prisa. Una
veleta nueva obedece al viento sobre el tejado mas alto, y cada dia el
kiosco guarda un Heraldo distinto, escrito con las noticias que la ciudad
se atreve a decir.

El reloj de bolsillo se puede detener en la mano. Tiene una inscripcion.
Y quien lo visita suficiente descubre que el tiempo guarda cosas pequenas
que se caen si uno insiste.

Hay codigos que abren puertas: el ano de la peste, la hora del brote, el
nombre del medico del elixir, el nombre del propio cronista. Cada uno
deja algo distinto: un telegrama, trece campanadas y una luz donde no hay
piso, una confesion que mancha el cartel del elixir para siempre, una
firma ofendida. El gramofono, girado mas alla de su ultima pieza, trae lo
que nadie grabo.

Cada sala del gabinete guarda ahora algo suyo: un cajon cerrado que acepta
una llave pequena, una senal de contrabando escrita en las costumbres de
las manos, un espejo que muestra como te ve la niebla, un buzon que
acumula cartas sin respuesta, un ojo que sigue al que mira, una lengua
que ningun consulado reconoce, un sello que nadie debia romper, un libro
de visitas para el atlas de hospederias, una direccion que no esta en
ningun mapa, y un silencio que, si uno se queda quieto, late.

En la pausa, la vela puede apagarse, y entonces se lee lo que la
oscuridad lee. El cuervo cobra su peaje. Algo respira del otro lado del
vidrio, y quien ha encontrado bastante llega a ver una segunda sombra.

Y detras del ultimo folio de la cronica, pasado el renglon final, hay una
sala nueva: la Sala de consulta. Cuarenta y tres fichas. Las que faltan
siguen ahi fuera.


## v0.15.2 - La cifra del campo

La mesa de opciones enseno al campo de vision a decir su numero en voz
alta. Cada posicion del deslizador se lee ahora con su cifra exacta,
como manda el oficio.


## v0.15.1 - La segunda mano

La misma noche, una mano mas fina volvio sobre el gabinete. La mesa de
opciones recupero el campo de vision, que habia quedado fuera de alcance
desde la ultima reorganizacion, y las ultimas letras escritas a mano sobre
los muebles de trabajo fueron borradas. Ningun rotulo propio queda ya
sobre las cajas: solo las palabras que el oficio original escribe.


## v0.15.0 - El oficio vuelve a su madera

El gabinete retiro las planchas de acero que cubrian las mesas de trabajo.
Los hornos, los cajones, el yunque y las mesas de oficio recuperan el
aspecto que tuvieron siempre: el gris claro de la fabrica comun, sin
remaches, sin molduras, sin sellos. Cada espacio vuelve a reconocerse de
un vistazo, como en los dias anteriores a la peste.

La decision fue firme: la casa conserva su voz, su musica y sus registros,
pero el trabajo manual vuelve a manos del oficio original de Minecraft.
Ningun mueble lleva ya el nombre de la casa; todos llevan el suyo propio.


## v0.13.10 RC - Pulido de gabinete y sonidos

El gabinete recibio una limpieza de oficio: los pestillos sonoros quedaron separados por funcion, los registros abandonaron herramientas temporales y las notas de continuidad fueron devueltas a los documentos centrales. La cronica publica conserva solo aquello que pertenece al viajero y al cronista.


## v0.13.10 - Acero bajo la lluvia

La ciudad ha recibido una mano mas firme sobre sus maquinas. Los hornos,
las cajas menores, la afiladora, el yunque, la mesa de corte, la mesa de
herreria, los registros de comercio y las caballerizas comparten ahora un
mismo acero gris, sobrio y remachado. La mesa arcana conserva su purpura,
pero sus renglones respiran entre particulas y brillos discretos.

Tambien la calle aprendio nuevos gestos: la lluvia salpica el empedrado,
los charcos forman ondas, los faroles dejan reflejos ambar, las polillas
orbitan las llamas y los papeles de cuarentena cruzan el suelo cuando
sopla el viento. En las alturas, ramas oscuras ensucian el vidrio y los
relampagos cortan la noche con trazos mas vivos.

## v0.13.3 - v0.13.9 - El gabinete se llena de maquinas

Durante siete noches, el gabinete se extendio hasta abarcar los talleres
que rodeaban al despacho del cronista. La mesa de trabajo, los arcones de
almacen, los hornos de la fragua, el yunque del herrero, la piedra de
amolar, la mesa arcana donde los libros susurran nombres antiguos, y hasta
la torre de baliza que corona los tejados de la ciudad: todo lo que hasta
hoy era carpinteria comun de madera anonima se ha transformado en hierro
forjado, remaches de latón y placas de acero pulido. Cada compartimento
lleva ahora la forma de un hueco troquelado en la plancha, y los mas
importantes estan bordeados por un halo dorado que anuncia su oficio.

Cada estacion mantiene su propio caracter dentro de la misma fabrica. Los
hornos brillan con un tinte cobrizo que recuerda al metal recien recocido;
la fragua del yunque respira el gris azulado del acero frio; la mesa de
encantamientos se cubre de un purpura profundo, casi ceremonial; y la
torre de baliza descansa sobre un fondo dorado como una joya en su vitrina.

El pergamino victoriano sigue reinando en los despachos, la correspondencia
y las lenguas conocidas. El acero remachado toma ahora el mando en cada
lugar donde el jugador trabaja con sus manos.
## v0.13.2 - Un inventario de pergamino

El gabinete del cronista ha empezado a expandirse mas alla del vestibulo.
El inventario del jugador, hasta ahora una mesa gris de fabrica, se ha
convertido en una carpeta de pergamino con marco doble, esquineros
victorianos y pequenos rombos ornamentales en cada lado. Los espacios
donde uno guarda sus enseres se ven ahora como casillas de tinta hundida,
como si el catalogo estuviera esperando el sello del funcionario. La
crafting mesa 2x2, la armadura, el escudo: todo mantiene la posicion
que uno ya conocia, solo que ahora todo parece pertenecer al mismo libro.

Aviso practico: el paquete de recursos "Gripe Verde - Tema Victoriano"
se activa por defecto, pero cualquiera puede desactivarlo desde Ajustes
si prefiere volver a la mesa gris.

## v0.13.1 - Reversion del acta

El intento de escribir un acta de defuncion para cada muerte no llego
a buen puerto. Otros gabinetes se ocuparan de ese oficio. La pantalla
al morir vuelve a ser la vanilla, y este mod deja el terreno libre para
que otro haga el trabajo con mas cuidado.

## v0.13.0 - Un acta que no llegó a ser (revertido)

Version experimental. Se genero una pantalla de acta de defuncion
victoriana al morir el jugador, pero durante las pruebas aparecieron
problemas de bucle infinito al respawnear y decidimos revertir el
sistema completo en v0.13.1.

## v0.12.4 - Cronica al dia

Actualizacion administrativa: la cronica in-game (los 25 archivos de
changelog en cada lengua) recibio la entrada "Detalles en la Penumbra",
que recoge los cambios visuales acumulados durante toda la serie v0.12.

## v0.12.3 - Hotfixes y optimizaciones

Correcciones criticas sobre v0.12.2. El ambiente ya no se congela al
navegar entre pantallas del menu, el slider de ambiente responde en
tiempo real, la calle se dibuja tambien en las subpantallas del menu,
y varias pasadas de optimizacion reducen la carga grafica en escalas
altas del GUI.

## v0.12.2 - Detalles vivos del gabinete

La escena viva del Title recibe una explosion de contenido: la luna
tiene ocho fases que rotan cada varios minutos, aparecen estrellas
fugaces con estelas cortas, bandadas de pajaros cruzan el cielo, se
encienden y apagan cinco ventanas independientes, hay un gato en el
tejado, una grieta con vela dentro de un edificio, ratas cruzando la
calle, y varios pools rotativos de textos: subtitulos del logo, firmas
del cronista, horas del reloj, cabeceras de posters, flavors de opciones.
Ademas hay un easter egg al hacer doble-click en el logo, con efectos
visuales y sonoros dramaticos.

Se introducen widgets nuevos (DiarySlider, DiaryProgressBar,
DiarySeparator, DiaryOnlineOptionsScreen, DiaryAmbienceSlider), y el
pergamino envejece con el uso a traves de un contador de aperturas
persistido en la configuracion.
## v0.12.1 - La ciudad se acuerda de respirar

Muchos detalles menores que juntos cambian el pulso del gabinete.

En el cielo, algunas estrellas pesan mas que otras: unas parpadean con
prisa, otras arden lentas y firmes. Cada una tiene su propio compas. Una
ventana perdida en algun piso alto se enciende y se apaga sin razon
aparente, cada tantos minutos, como si alguien no pudiera decidirse a
dormir. Un farol del extremo derecho comienza de pronto a temblar
erraticamente, tres o cuatro segundos de convulsion amarilla, y luego
vuelve a su cadencia mansa. Cada tanto una silueta pequena cruza la
calle abajo, rapida, oscura: probablemente una rata.

Bajo el nombre del proyecto, donde antes descansaba una fecha en cifras
romanas, ahora aparece una linea distinta cada vez: apuntes del que
escribe, contando dias, midiendo silencios. "Dia 47. Aun no encuentro
respuestas." "Dia 118. Aun no cede la fiebre." "Dia 178. Persisto."
Cada tantos minutos cambia; cada arranque del juego elige una nueva al
azar. Al pie de la pagina, la firma tambien rota entre cuatro manos:
S. Pereira, R. Da Rosa, G. Bandeira, A. Focco.

El gabinete de ajustes gano una frase rotativa del cronista bajo el
subtitulo, distinta cada vez que se abre. Los botones se sienten con
mas peso: al presionar se hunden apenas un pixel, la sombra debajo se
achica, el color se oscurece. El hover ganó suavidad: ya no cambia de
golpe, se acomoda. Los interruptores tienen un click mas grave y
mecanico que los botones normales.

El historial de cambios luce mejor: cada titulo grande empieza con una
capital destacada en tinta oscura, y entre secciones hay un divisor con
rombo central y lineas que se desvanecen a los costados, como en una
carta antigua. Ademas recuerda la posicion en la que quedaste, para no
tener que buscarla al volver.

En Lenguas, la lista tambien recuerda el scroll. En los cuadros de
confirmacion suena una campanilla al aparecer. Y cuando alguna pantalla
del gabinete queda a medio dibujar, ya no aparecen restos del despacho
anterior encima.

## v0.12.0 - Cierre del ciclo del gabinete

Version administrativa que marca el cierre del ciclo largo v0.10 - v0.11
(el gabinete pergamino). Sin codigo nuevo: solo consolidacion narrativa,
reagrupamiento del changelog in-game bajo la entrada unica "Una casa de
papel", y salto de version para senalar que la fase de UI esta cerrada.

Lo que venga a partir de aqui sera contenido de gameplay: la Gripe Verde
en si misma, con sus sintomas, sus cuarentenas y sus mecanicas de
sobrevivencia.

## v0.11.14 - Banderas de las voces

Cada lengua que la ciudad reconoce lleva ahora su ensena junto al nombre.
Franjas, cruces, estrellas y lunas: pequenos estandartes que ondean al
costado de cada linea, como los pendones alineados en el frente de un
consulado. La mirada aprende antes por el color que por el trazo.

No todas las voces tienen bandera propia. Algunas viejas jergas, dialectos
del interior o inventos de la calle se anuncian con un simple rombo de
tinta: un sello anonimo, digno de quienes hablan sin patria fija.
## v0.11.13 - El gabinete se ajusta a la mano

En esta noche cuatro cosas menores ganan el gabinete. La primera es
de peso: la musica del cronista responde ahora al instante cuando la
mano acaricia el regulador. Ya no hay que esperar a soltar la aguja
para que el volumen obedezca; el sonido se pliega al gesto como debe
plegarse.

Las tarjetas del atlas de ciudades respiran ahora con contornos mas
suaves; el rojo alarmante de los puertos inalcanzables ha sido
atenuado por una sombra sepia oscura que descansa detras del ping,
sin agredir al pergamino ni ocultar el aviso.

Los libros de correspondencia y los auxilios y percepciones
recibieron un desvanecido pergamino en su ribete superior e inferior,
de modo que el contenido no se corta con arista dura sino que se
desvanece contra los margenes como tinta seca.

En la bitacora de comandos, los interruptores de conducta -
agacharse, correr, salto automatico, pestana de administrador - dejan
atras su tono metalico de fabrica y visten hoy tira de papel ocre,
con circulillo relleno cuando la conducta esta activa y hueco cuando
descansa. Textos que exceden la caja se recortan con puntos
suspensivos discretos.

Los apuntes de asentamiento nuevo y las travesias directas se
escriben ahora sobre papel: los antiguos rectangulos negros de
captura de texto fueron reemplazados por bandas de pergamino con
borde de tinta, cursor de tinta parpadeante y seleccion sepia.

Y por sobre todo, cada nota al margen que el gabinete susurra al
pasar el cursor - antes ventanas de un morado extranjero - viste hoy
pergamino con borde tinta. La ciudad victoriana ha ganado sus ultimas
esquinas.

## v0.11.13 - El gabinete se ajusta a la mano

En esta noche cuatro cosas menores ganan el gabinete. La primera es
de peso: la musica del cronista responde ahora al instante cuando la
mano acaricia el regulador. Ya no hay que esperar a soltar la aguja
para que el volumen obedezca; el sonido se pliega al gesto como debe
plegarse.

Las tarjetas del atlas de ciudades respiran ahora con contornos mas
suaves; el rojo alarmante de los puertos inalcanzables ha sido
atenuado por una sombra sepia oscura que descansa detras del ping,
sin agredir al pergamino ni ocultar el aviso.

Los libros de correspondencia y los auxilios y percepciones
recibieron un desvanecido pergamino en su ribete superior e inferior,
de modo que el contenido no se corta con arista dura sino que se
desvanece contra los margenes como tinta seca.

En la bitacora de comandos, los interruptores de conducta -
agacharse, correr, salto automatico, pestana de administrador - dejan
atras su tono metalico de fabrica y visten hoy tira de papel ocre,
con circulillo relleno cuando la conducta esta activa y hueco cuando
descansa. Textos que exceden la caja se recortan con puntos
suspensivos discretos.

Los apuntes de asentamiento nuevo y las travesias directas se
escriben ahora sobre papel: los antiguos rectangulos negros de
captura de texto fueron reemplazados por bandas de pergamino con
borde de tinta, cursor de tinta parpadeante y seleccion sepia.

Y por sobre todo, cada nota al margen que el gabinete susurra al
pasar el cursor - antes ventanas de un morado extranjero - viste hoy
pergamino con borde tinta. La ciudad victoriana ha ganado sus ultimas
esquinas.

## v0.11.9 - Coherencia del gabinete

En esta noche el gabinete respira una sola tinta. Todas las hojas que
se abren desde las Opciones - las lenguas conocidas, las camaras y
resonancias, los protocolos de correspondencia, los auxilios y
percepciones, la efigie y porte del cronista, la bitacora de comandos,
las menciones y agradecimientos, el atlas de ciudades - llevan hoy la
misma caligrafia en sus encabezados, el mismo trazo bajo los titulos,
la misma lozanga entre los divisores.

Los pergaminos, antes livianos como recortes pegados al aire, proyectan
ahora sombra sobre la escena atenuada. Las tiras de papel que sirven de
botones descansan claramente sobre el fondo, con un tono ocre mas
oscuro que las distingue del pergamino sobre el que se apoyan.

La lista de lenguas dejo atras su cartulina lisa y ahora comparte la
textura de manchas del gran pergamino. La banda tras el asentamiento
listado en el atlas se aligero: ya no es un parche marron sino una
sombra sepia que da lectura sin agredir. La barra por la que se corren
las hojas largas corre ahora contra el borde del pergamino, no en el
medio de la escena.

La musica del cronista, que antes se detenia en cuanto uno cruzaba
hacia una sub-hoja, sigue viva ahora por todos los pasillos del
gabinete. Y en la hoja de lenguas, retirarse con la tecla de escape
descarta el cambio, como corresponde a quien se arrepiente de haber
elegido otra voz.
## v0.11.8 - El atlas de puertos ajenos

En esta noche el cronista ha completado el capitulo dedicado a la
correspondencia con ciudades distantes. El atlas de puertos ajenos ya
no se despliega sobre paneles ordinarios de fabrica: cada mapa, cada
apunte de asentamiento nuevo, cada travesia hacia un puerto sin
cartografiar se abre ahora sobre pergamino, envuelto en la misma
paciencia con que se anota una carta a la luz de una vela.

La entrada del observatorio oficial de la Gripe Verde ha quedado
grabada en el atlas por siempre. Cualquier viajero que consulte
estas paginas encontrara alli el nombre y las coordenadas del
observatorio principal, incluso si trata de arrancarlo del papel.

Las senales de balizamiento lejanas y los ecos de puertos cercanos
han sido apartados a otra pagina, para que el ojo no se distraiga
cuando ya hay ciudades reconocidas en el atlas.

Bajo cada asentamiento reposa ahora una tarjeta sepia con su nombre
y descripcion, y una linea de tinta tenue a modo de firma.

## Notas de Actualización

## v0.11.7 - Dos salas mas del gabinete

Dos habitaciones mas del gabinete recibieron la mano del cronista.

La sala de Controles amanecio ordenada sobre pergamino: los dos porticos que llevan al cuidado del raton y a las llaves de la casa quedan alli mismo, junto a las cuatro pequenas voluntades sobre agacharse, correr, saltar y la pestana del administrador. El divisor y los angulos ya son como los del resto del gabinete.

Las Menciones y agradecimientos tambien se disponen ahora sobre papel, en su hoja propia, y desde ahi se llega a la larga secuencia de nombres que hicieron posible la cronica.

Cuatro puertas siguen abriendo a habitaciones oscuras: las opciones del raton, el tomo de atajos, la seleccion de paquetes y las opciones en linea. El cronista las visitara con calma en la proxima entrega.

### Del diario
- La sala de Controles se acomodo sobre pergamino, con sus divisores y esquineros.
- Las Menciones y agradecimientos ganaron hoja propia sobre papel.
- Los dos porticos de Controles llevan al raton y a las llaves de la casa.
- Cuatro puertas del gabinete siguen conduciendo a habitaciones oscuras, en espera.
## v0.11.6 - La camara de musica del gabinete

Anoche la puerta que decia "Musica y sonidos" dejo de conducir a una sala prestada de otra casa. Detras de ella se abre ahora un ala propia, tapizada del mismo pergamino que el resto del gabinete, con sus divisores en filete y sus esquineros dobles. Los antiguos tiradores de piedra siguen alli, ordenados como siempre, y entre ellos permanece la lampara que ilumina las noches de escritura. Pero al pie del panel hay un tirador nuevo, empotrado en laton, con una marca de tinta victoriana en el centro. El cronista lo llama Musica del Cronista, y con el ajusta a su gusto el volumen del organillo lejano. El valor elegido queda anotado en el diario de configuraciones, para que no se pierda al cerrar la sala.

En el mismo movimiento, el vestibulo mayor se acomodo. La placa que decia "Telemetria" nunca perteneció del todo a esta casa: era un aviso oficial, redactado en otro idioma, que hablaba de datos y medidas ajenas a la ciudad. Fue retirada sin ceremonia. Los diez tiradores restantes se acomodaron en cinco filas simetricas, y la placa de "Correspondencia en linea" subio a compartir la ultima fila con la de "Autores y agradecimientos", cerrando el conjunto con simetria.

### Del diario
- Se abrio un ala propia para la camara de musica, sobre pergamino.
- Un tirador de laton nuevo, la Musica del Cronista, permite ajustar el organillo lejano.
- El valor del tirador queda escrito en el diario de configuraciones.
- La placa de Telemetria fue retirada del vestibulo mayor.
- Los diez tiradores restantes se acomodaron en cinco filas simetricas.

---

## v0.11.5 - El escriba afila su pluma

La sala de accesibilidad y la de protocolos de correspondencia comparten esta noche un pequeno hallazgo: cuando el lector desliza el pergamino con la rueda, el marcador de renglones ya no es una plancha de hierro grisacea, sino una lengueta de tinta con su rombo central, como el que se estreno en el registro de lenguas. Es un detalle menor y sin embargo, cuando uno recorre las opciones de arriba a abajo, la ciudad se siente mas de una sola pieza. El gabinete de accesibilidad se ha ensanchado un poco para dar mas aire a los widgets que se apretujaban contra los bordes.

### Del diario
- El marcador de renglones se ha vestido de tinta en la sala de accesibilidad.
- La sala de correspondencia recibio el mismo cambio, sin ceremonia.
- El pergamino de accesibilidad se ha ensanchado; los renglones respiran mejor.

---
## v0.11.3 (2026-07-XX)

El cronista abre dos nuevas puertas de su gabinete. En una halla ajustes para sus propios sentidos: la brasa que titila, el trueno que sacude, la voz que dicta las palabras impresas. En la otra, las convenciones para dirigirse a otros que caminen la ciudad: la pluma que responde, el papel donde llegan las noticias, el silencio que se guarda. Ambas salas huelen a pergamino, a tinta seca y a lampara de aceite. Los marcos se han labrado con esquineros dobles y los divisores llevan un rombo tipografico en el centro. Bajo cada titulo se ha anotado, con letra tenue, la naturaleza de la sala.
## v0.11.2 - Protocolos de correspondencia

Esta noche los protocolos de correspondencia tambien han sido acogidos por el diario. La pantalla en que se ordenan las reglas del chat luce ahora sobre pergamino, con las mismas divisas en las esquinas y las mismas rayas de tinta que ordenan el gabinete y el registro de lenguas. Nueve renglones caben con holgura entre los margenes, y el boton de retorno responde al mismo gesto que ya se conoce.

### Del diario
- Los protocolos de correspondencia se han ordenado sobre pergamino.
- La pantalla adopta las divisas y renglones del gabinete y del registro de lenguas.
- Los ajustes internos se conservan intactos; solo la presentacion ha cambiado.

---
## v0.11.1 - Lenguas conocidas

El gabinete que ya lucia sobre pergamino se ha ordenado con oficio. Divisas discretas guardan sus cuatro esquinas; dos rayas de tinta separan lo que se lee de lo que se firma; un renglon bajo el titulo anuncia la mano del cronista. Y en su interior, por fin, la lista de lenguas del mundo se ha vuelto legible: cada una en su renglon limpio, sin la mancha vulgar que las tapaba, la elegida marcada con una flecha, las demas esperando su turno. Tambien los edictos de la calle han bajado la voz. Cuarentena y Real Decreto ya no gritan al observador; ahora respiran con el resto del papel.

### Del diario
- Las lenguas del mundo se han ordenado sobre pergamino, limpias y legibles.
- El gabinete recibio divisas en sus esquinas y renglones que dividen sus partes.
- Los edictos de Cuarentena y Real Decreto se han suavizado en su tipografia.

---
## v0.11.0 — El gabinete de ajustes

Hasta esta noche, quien buscaba modificar las condiciones bajo las cuales el diario era consultado se topaba con una hoja tosca, escrita a maquina, ajena por completo al espiritu del cuaderno. El contraste era violento: se salia de la vigilia y se caia en una oficina mal iluminada. Esta noche, el gabinete tambien ha sido incorporado. Sobre el mismo pergamino y con la misma tinta, el observador encuentra ahora dispuestas las opciones de la vigilia. La ciudad sigue viva detras del panel, atenuada, sus faroles latiendo con paciencia mientras uno ajusta lo que necesite ajustar.

### Del diario
- El gabinete de ajustes luce sobre pergamino, con la ciudad viva de fondo.
- Los ajustes son alcanzables desde la vigilia inicial y tambien desde la pausa nocturna.

---
## v0.10.4 — Halo lunar y jirones de niebla

Esta noche la luna ha ganado un halo tenue, como si el vapor de las calderas y la humedad del Támesis se le pegaran alrededor. Tres jirones finos la cruzan lentamente, sin llegar a taparla del todo. La ciudad respira mas honda bajo esa luz enferma.
## v0.10.3 — Controles de audio del Title

- Boton `>>` en el menu principal para pasar a la siguiente pista con crossfade.
- Boton `AMB` en el menu principal para silenciar o reactivar el ambiente.
- Persistencia del estado de ambiente en `gripeverde-client.toml`.
- `MenuAmbience.start()` ahora es idempotente.
- Fix del skip manual: la nueva pista ya no entra muda al iniciar el crossfade.
## v0.10.2 — Corrección administrativa

Ajuste interno del diario para que el sello de la edición actual coincida con la última noche registrada. Sin novedades observables en la ciudad.

---

## v0.10.1 — La memoria de la mano

Hasta ahora, cada vez que el observador volvía al diario, la ciudad recuperaba su voz al mismo volumen invariable, como si nadie hubiera tocado nunca la clavija. Esta noche eso ha cambiado. El diario recuerda con qué fuerza se le pidió por última vez que sonara la calle, y respeta ese ajuste al reabrirlo. Un pequeño acto de cortesía hacia quien lo consulta.

### Del diario
- El diario conserva el volumen elegido para el rumor de la ciudad.
- Ya no vuelve al valor de fábrica al cerrarse la vigilia.

---

## v0.10.0 — El diario se viste con la solemnidad que merece

Durante semanas, las páginas dedicadas al historial de la ciudad tuvieron un aspecto pobre, como notas garabateadas en el reverso de un sobre. Esta noche por fin lucen como deben: fibra de pergamino auténtica bajo los dedos, manchas de vela y de tinta antigua, el peso de un documento oficial. Además, las crónicas mismas han sido reescritas. Antes se colaban en ellas expresiones extrañas, propias de un ingeniero de gabinete y no de un cronista de calle. Han sido depuradas. Ahora todo lo que se lee en el diario es lo que el ojo, la niebla y el gas verían.

### Del diario
- El historial luce sobre pergamino con textura y viñeta.
- Se han eliminado descripciones impropias de un caballero observador.
- Las crónicas antiguas han sido reescritas en un tono más fiel a la ciudad.

---

## Versión 0.9.2

**El humo ya no corta el cielo.**

Hasta hace poco, los penachos que ascendían desde las chimeneas parecían recortados con tijeras: bloques oscuros suspendidos entre los tejados, más propios de un decorado apresurado que del aire cargado de una ciudad viva. Esta noche el hollín se comporta como debe. Sube en volutas suaves, se disuelve en la bruma, y las nubes que lo acompañan por el firmamento han perdido sus ángulos rectos. La luna, cuando le corresponde, se oculta tras un velo redondo y no tras un rectángulo torpe.

### Del diario
- El humo de las chimeneas se eleva con formas suaves y naturales.
- Las nubes del cielo nocturno han perdido sus bordes cuadrados.
- El velo que cubre la luna se disuelve con delicadeza.

---

## Versión 0.9.1

**Los faroles vuelven a arder.**

Durante meses, la avenida ha permanecido en penumbra: las farolas de gas fueron retiradas por reformas que nunca terminaron, y sólo la luz turbia que escapaba de las ventanas alcanzaba el empedrado. Esta noche los operarios han regresado. Los postes de hierro forjado están de nuevo en su sitio, uno tras otro a lo largo de la calle, y sus llamas titilan bajo las cúpulas de vidrio. Los charcos de la calzada mojada devuelven un reflejo dorado que hacía tiempo faltaba.

### Del diario
- Los faroles de gas han sido reinstalados a lo largo de la avenida.
- Sus llamas apoyan con firmeza sobre la piedra, sin flotar en el aire.
- El número de faroles se adapta con discreción al ojo del observador.

---

## Versión 0.9.0

**La ciudad ha ganado profundidad.**

Al mirar por la ventana del despacho, algo ha cambiado en el horizonte. Las luces que arden en los edificios lejanos ya no compiten con las que titilan tras los cristales cercanos: se han vuelto puntos tímidos, como si supieran que están muy lejos y no quisieran molestar. Las chimeneas, en cambio, han encontrado por fin su lugar. Coronan los tejados con firmeza, y sus penachos de hollín suben rectos hacia el cielo cargado.

El aire también se ha despejado de esas franjas grises que antes cruzaban el firmamento sin razón aparente. Ahora sólo queda la bruma legítima, la que siempre estuvo, la que pertenece a esta ciudad enferma.

### Del diario
- Las ventanas de los edificios lejanos se ven distintas de las cercanas: profundidad real.
- Las chimeneas están ancladas a sus edificios, ya no flotan en el vacío.
- El cielo ha quedado limpio de artefactos que no le pertenecían.

---
## Versión 0.8.3

**Hay silencios que se guardan por respeto.**

Cuando las campanas de la parroquia doblan a lo lejos, cuando un trueno hiende el cielo o cuando los cuervos alzan vuelo entre los tejados, la música de la ciudad se aparta con discreción. No calla del todo; sólo baja la voz, como quien se descubre la cabeza al paso de un cortejo, y espera. Cuando el presagio se disuelve en la niebla, la melodía regresa al primer plano sin prisa.

También los hilos de la memoria se han vuelto más firmes. Aunque se aparte la mirada del diario un instante o se busquen otros apuntes, la canción de la ciudad ya no se interrumpe sin motivo. Persiste, acompañando al observador en su vigilia.

### Del diario
- La música se aparta ante las campanas, los truenos y el vuelo de los cuervos.
- El lamento de la ciudad persiste a través de las páginas.
- Se detiene sólo cuando el deber llama a salir al mundo.

---

## Versión 0.8.2

**Las melodías se entrelazan sin quiebre.**

Antes, cuando una pieza terminaba, el silencio caía de golpe. Ahora es distinto. Antes de que una melodía se apague del todo, la siguiente ya comienza a susurrar por debajo. Ambas conviven unos segundos como dos voces en un mismo cuarto, hasta que una se despide y la otra llega. La costura no se nota.

### Del diario
- Transiciones suaves entre las canciones de la noche.
- Sin cortes bruscos ni silencios prolongados.

---

## Versión 0.8.1

**Ahora puedes decidir cuánto quieres oír.**

No todos los oídos son iguales. Se ha dispuesto un pequeño control para que quien lee decida con qué fuerza le llega el lamento de la ciudad. Puede acallarlo si el momento pide calma, o dejar que vuele libre si el ánimo lo permite. El ajuste es inmediato y la melodía se acomoda al instante, obedeciendo la mano.

---

## Versión 0.8.0

**La ciudad ha encontrado su canción.**

Durante mucho tiempo, la calle sólo hablaba con sus propios ruidos: el viento, la lluvia y el eco de los que caminan. Pero ahora, de alguna parte que no podemos señalar, ha llegado la música. Cuatro melodías se turnan en la noche, mezclándose con la tormenta sin taparla. Es la primera vez que este diario tiene una voz que lo acompaña.

---

## Versión 0.7.1

**Las farolas se apagan por prudencia.**

Los viejos postes de gas ya no alumbran la calle del diario. Su luz, antes suspendida sobre un suelo incierto, confundía más de lo que guiaba. Por ahora han sido apagados. No han sido retirados de la ciudad; sólo esperan a que las calles tengan cimientos suficientes para sostenerlos. Es mejor una calle oscura que una mentira iluminada.

---

## Versión 0.7.0

**La calle vuelve a existir.**

Bajo la niebla ocre y la lluvia constante, la ciudad recupera algo que parecía perdido: una superficie bajo los pies. No es una avenida clara ni una plaza orgullosa. Es una calle oscura, húmeda, apenas sugerida por adoquines y reflejos. Pero basta. Por primera vez en mucho tiempo, la vista ya no cae al vacío.

---

## Versión 0.6.5

**La ventana ya no castiga la mirada que se aparta.**

Se corrigieron los males que hacían tambalear el diario al minimizar la ventana o al apartar el foco de ella. Ahora puede quedar en segundo plano sin precipitar la sesión al desastre.

---

## Versión 0.6.2

**El reloj respira.**

Aquel viejo reloj de bolsillo, que durante tanto tiempo pareció un mero adorno, ahora late con un pulso mecánico suave. Cada cinco segundos, cuando el segundero alcanza una de las marcas negras, un chasquido discreto llega al oído, como si el mecanismo, largo tiempo olvidado, hubiese recordado cómo trabajar.

### Un mecanismo vivo
- El segundero tickea en perfecta armonía con sus fallos.
- El reloj de bolsillo sólo suena cuando está a la vista. En las ventanas más pequeñas, descansa en silencio.

---

## Versión 0.6.1

**Algo se mueve sobre el escritorio.**

El reloj de bolsillo ha dejado de ser un retrato inmóvil. Ahora cuelga de una cadena larga que desciende desde el mismísimo borde superior del diario, y oscila con un lento pulso pendular, como si una corriente oculta lo acariciase.

Su segundero avanza con precisión mecánica, pero no siempre. De cuando en cuando tropieza: a veces se demora en una marca más de lo debido, a veces salta hacia adelante sin aviso, a veces retrocede un tiempo antes de continuar.

---

## Versión 0.6.0

**La crónica cierra un capítulo y abre el siguiente.**

El diario ha sido puesto en orden. Cada página, cada adorno, cada sonido: revisado y sellado. Lo que estaba disperso ahora está encuadernado.

---

## Versión 0.5.7

**Un reloj se detuvo a las once y cincuenta y dos.**

En la esquina superior derecha del diario ha aparecido un viejo reloj de bolsillo. Sus manecillas de bronce marcan siempre la misma hora: el instante en que todo se detuvo.

### La ciudad respira mejor
- El diario ahora se ajusta con más gracia a cualquier ventana.
- Los avisos oficiales y el aviso de recompensa encuentran su lugar sin estorbar.
- En pantallas más pequeñas, las páginas se simplifican por sí solas, mostrando sólo lo esencial.

---

## Versión 0.5.0

**El diario recuerda cada paso del camino.**

Ahora podrás consultar el historial completo de novedades directamente desde el menú principal. Un discreto símbolo aparece en la esquina superior derecha, esperando ser descubierto.

---

## Versión 0.4.4

**La ciudad ha cobrado vida.**

Las calles ya no permanecen en silencio. Al asomarte al menú principal podrás oír el pulso de una urbe atrapada por la Gripe Verde.

### Voces de la ciudad
- El viento recorre las callejuelas empedradas.
- La lluvia repica sobre los adoquines.
- Un murmullo distante se alza desde los tejados.
- Las campanas de la iglesia repican sin razón aparente.
- Un trueno rompe el cielo cargado de humo.
- Los cuervos anuncian lo que nadie quiere ver.

---

## Versión 0.2.5

**El diario luce más vivo que nunca.**

Las páginas han sido restauradas con esmero. Cada detalle busca transmitir la atmósfera de una ciudad en tinieblas.

- Las velas iluminan con un halo cálido y tembloroso.
- Las manchas de sangre se muestran con crudeza sobre el papel.
- Los sellos de cera lucen con relieve y sombra.
- El papel envejecido cubre las páginas sin cortes ni bordes.

---

## Versión 0.1.0

**Todo comienza aquí.**

La ciudad despierta bajo las primeras señales de la Gripe Verde. El diario abre su cubierta por primera vez.
