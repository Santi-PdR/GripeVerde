# KNOWN_ISSUES - Eternal Craft: Gripe Verde

> Ultima actualizacion: v0.5.7 (sesion cerrada)
> Estado: EN DESARROLLO
> Version estable actual: v0.5.7
> Objetivo cumplido en esta sesion: Framework UI + migracion de Title + reloj victoriano + modo minimal

---

## 1. Version actual

- Proyecto: Eternal Craft: Gripe Verde
- Base: Minecraft Forge 1.20.1
- Version desplegada: 0.5.7
- Tipo: mod de interfaz
- Ambientacion: ciudad victoriana bajo cuarentena, Green Flu
- Estado general: compilando limpio, Title migrado, layout adaptativo funcionando

---

## 2. Sistemas activos

### MenuAmbience
- 26 eventos en sounds.json
- DEBUG_LOGGING = false
- Estable

### Changelog
- 25 idiomas sincronizados
- Fallback a ingles

### Framework UI (v0.6.0)
- UIComponent, Spacing, ScreenLayout instalados
- Consumidor activo: DiaryTitleScreen

### DiaryTitleScreen (v0.5.7)
- Columnas adaptativas con LayoutRegion
- Reloj de bolsillo victoriano (PocketWatchRenderer)
- Modo minimal activo bajo altura < 290
- Botones Options/Quit apilados en modo minimal
- Firma easter egg en esquina inferior derecha

---

## 3. Problemas conocidos

### KI-01 - Freeze al presionar Leave
Sintoma: puede quedar en "Saving world".
Estado: no reproducido controladamente.
Sospecha: entorno (RAM, GPU, Embeddium).
Accion: no modificar sin evidencia en logs.

### KI-02 - Crash al hacer screenshots F2
Sintoma: NVAPI + Embeddium + NVIDIA.
Workaround: Windows + Shift + S.

### KI-03 - Fondo del Title con defectos visuales (pre-existente)
Sintoma: postes flotando, chimeneas raras, calle poco marcada, halos no siempre en el piso.
Estado: archivos VictorianSkyline, LivingAtmosphere y AtmosphereRenderer NO fueron modificados en esta sesion.
Causa: defectos preexistentes a v0.5.0 que se disimulaban con paneles grandes ahora reemplazados.
Accion: agendado para v0.7.0 como trabajo de arte visual.

### Deuda tecnica activa
- ICON_BTN_MP e ICON_BTN_MODS sin PNG real
- Framework UI subutilizado (solo Title lo consume)
- ThemeManager sin themes registrados

---

## 4. Historial de esta sesion

### v0.5.0 (base estable)
- Punto de partida.

### Fase 0 - Limpieza
- Backups creados, .bak eliminados, TextureAssets reducido, DiaryDeathScreen eliminado de ThemeManager, TODO residual eliminado.

### Fase 2 - Framework
- UIComponent, Spacing, ScreenLayout creados.

### Fase 3 - Migracion iterativa
- 0.5.1: primera migracion con LayoutRegion
- 0.5.2: layout adaptativo del poster izquierdo
- 0.5.3: columnas derecha adaptativas + easter egg firma
- 0.5.4: auto-hide de paneles
- 0.5.5: reloj de bolsillo victoriano (reemplaza Chronicle)
- 0.5.6: umbral minimal ajustado a 290
- 0.5.7: botones Options/Quit apilados verticalmente en modo minimal

---

## 5. Reglas vigentes

- Java UTF-8 sin BOM (UTF8Encoding false)
- No usar exit N en PowerShell interactivo
- No modificar DiaryPauseScreen (R14)
- No modificar renderers de fondo sin plan explicito
- Auditar estado real antes de proponer cambios
- Cada cambio visible bumpea version
- Deploy solo tras build exitoso

---

## 6. Errores historicos importantes

(Se mantienen E1 a E19 sin cambios)

### E20 - Escritura con BOM rompio compilacion
Solucion: usar UTF8Encoding(false) al escribir Java desde PowerShell.

### E21 - Parseo ingenuo de .deploy
Solucion: filtrar por CLAVE=VALOR, no tomar primera linea.

### E22 - Umbrales de auto-hide muy permisivos
Solucion: subir a 290 y agregar modo minimal explicito con reorganizacion de botones.

---

## 7. Plan futuro

### Cierre formal v0.5.x -> v0.6.0
- Actualizar CHANGELOG.md
- Actualizar 25 changelogs localizados
- Considerar `git init`
- Bump final a 0.6.0

### v0.7.0
- Death Screen
- Trabajo de arte visual del fondo (KI-03)
- Crear PNG de ICON_BTN_MP e ICON_BTN_MODS
- Posible mano zombi desde borde inferior

### v0.8.0+
- Bestiario, creditos, notificaciones
- Nuevas pantallas vanilla si se aprueban

---

## 8. Estado tecnico

- Backups validados en _backups/
- Deploy operativo a D:\Juegos\Minecraft\Instances\Test\mods
- Build limpio, sin errores
- Compilando en 20-30 segundos

v0.5.7 estable y desplegada.
Proxima sesion: preguntar si cerrar formalmente v0.6.0, atacar KI-03 o empezar v0.7.0.