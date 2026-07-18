# KNOWN_ISSUES - Eternal Craft: Gripe Verde

Ultima actualizacion: v0.7.1
Estado: EN DESARROLLO
Version estable actual: v0.7.1
Ultimo tag publicado: v0.7.1
Objetivo reciente: KI-03 dividido en sub-hitos seguros despues de la sesion v0.7.x fallida.

---

## 1. Version actual

- Proyecto: Eternal Craft: Gripe Verde
- Base: Minecraft Forge 1.20.1
- Version desplegada: 0.7.1
- Tipo: mod de interfaz
- Ambientacion: ciudad victoriana bajo cuarentena, Green Flu
- Estado general: compilando limpio, Title estable, calle del fondo definida, postes ocultos temporalmente.

---

## 2. Sistemas activos

### Title / DiaryTitleScreen
Pipeline actual de fondo:
1. AtmosphereRenderer.renderSky
2. VictorianSkyline.renderBackground
3. LivingAtmosphere.renderFogLow
4. VictorianSkyline.renderForeground
5. LivingAtmosphere.renderStreetLevel
6. LivingAtmosphere.renderAir
7. LivingAtmosphere.renderFlash
8. LivingAtmosphere.renderVignette

### LivingAtmosphere
- Renderiza niebla baja, calle, niebla de suelo, esporas, lluvia, cuervos, rayo y vineta.
- Desde v0.7.0 incluye renderStreet() adaptativo por escala.
- La calle esta aprobada visualmente.

### VictorianSkyline
- Renderiza edificios BG, edificios FG, ventanas, chimeneas y humo.
- Desde v0.7.1 getLanternPositions() devuelve new int[0][0].
- Los postes/faroles quedan ocultos temporalmente hasta v0.7.2/v0.7.3.

### MenuAmbience
- Sistema de ambiente sonoro activo.
- Estable.

### Changelog
- Pantalla in-game activa.
- Documentacion narrativa principal en CHANGELOG.md.

### Framework UI
- Layout adaptativo funcionando.
- Modo minimal activo bajo altura efectiva menor a 290.

---

## 3. Problemas conocidos activos

### KI-01 - Freeze al presionar Leave
Sintoma: puede quedar en "Saving world".
Estado: no reproducido controladamente.
Sospecha: entorno, RAM, GPU, Embeddium.
Accion: no modificar sin evidencia en logs.

### KI-02 - Crash al hacer screenshots F2
Sintoma: NVAPI + Embeddium + NVIDIA.
Workaround: Windows + Shift + S.

### KI-03 - Fondo del Title con defectos visuales
Estado actual tras v0.7.1:
- Calle definida: RESUELTO en v0.7.0.
- Postes/faroles flotantes: MITIGADO en v0.7.1 ocultandolos temporalmente.
- Vacio entre skyline y calle, especialmente escala 1: ACTIVO.
- Skyline demasiado poblado y sin jerarquia: ACTIVO.
- Luna solitaria sin nubes contextuales: ACTIVO.

Decision vigente:
- No reactivar postes hasta que el skyline/calle los soporte visualmente.
- No tocar calle en v0.7.2 salvo emergencia.
- No mezclar reposicion de postes con extension de edificios en el mismo bump.

### Deuda tecnica activa
- ICON_BTN_MP e ICON_BTN_MODS sin PNG real.
- Framework UI subutilizado.
- ThemeManager sin themes registrados.
- Changelog localizado puede requerir sincronizacion posterior con nuevas versiones.

---

## 4. Historial relevante reciente

### v0.6.5
- Base estable confirmada despues de revertir la sesion v0.7.x fallida.
- Fix de estabilidad al perder foco/minimizar.

### v0.7.0
- Sub-hito 1 KI-03.
- Se agrego renderStreet() decorativo/adaptativo en LivingAtmosphere.
- No se tocaron postes ni edificios.
- Calle aprobada visualmente.

### v0.7.1
- Sub-hito 2 KI-03.
- Se intento reposicionar postes por escala, pero en escalas grandes seguian leyendose mal por falta de soporte del skyline.
- Decision final: ocultar postes en todas las escalas mediante getLanternPositions() -> new int[0][0].
- Build, deploy, commit, push y tag completados.

---

## 5. Reglas vigentes

- Java UTF-8 sin BOM (UTF8Encoding false).
- PowerShell: bloques cortos; si hay if/else, mantener estructura clara y verificable.
- Auditar estado real antes de proponer cambios.
- Un sistema por version.
- Un cambio por bump cuando sea visualmente sensible.
- Validar visualmente en las 4 escalas cuando se toque el fondo del Title.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Build siempre acompanado de deploy.
- Minimizar cantidad de comandos PowerShell.
- Usar rutas absolutas si no se esta parado en la raiz del repo.
- Limpiar jars viejos del deploy path antes de validar.
- Validar la version visible en el reloj antes de aprobar o rechazar.

---

## 6. Errores historicos importantes

### E20 - Escritura con BOM rompio compilacion
Solucion: usar UTF8Encoding(false) al escribir Java desde PowerShell.

### E21/E23 - Parseo ingenuo de .deploy
Solucion: filtrar por DEPLOY_MODS_PATH= y no asumir que la primera linea sirve.

### E27 - Replaces multi-cambio simultaneos
Regla: un replace por vez, verificacion inmediata.

### E28 - Formulas cross-file
Regla: no crear dependencias silenciosas entre renderers.

### E29 - Sin capturas de referencia
Regla: capturas pre/post en 4 escalas antes de validar cambios visuales importantes.

### E30 - Extrapolar entre escalas
Regla: validar escala por escala.

### E31 - Sistemas interdependientes en mismo bump
Regla absoluta: un sistema por version.

---

## 7. Plan futuro inmediato

### v0.7.2 - Sub-hito 3 KI-03
Objetivo: cerrar el vacio entre skyline y calle.

Alcance recomendado:
- Tocar solo VictorianSkyline.renderForeground / datos FG.
- No tocar renderStreet().
- No reactivar postes en el mismo bump salvo que el usuario apruebe explicitamente dividirlo o extender alcance.
- Validar en 4 escalas.
- Si se reactivan postes, hacerlo en v0.7.3.

### v0.7.3
- Reactivar postes/faroles por escala cuando el skyline ya los soporte.

### Posterior
- Mejorar jerarquia del skyline.
- Agregar nubes/contexto visual cerca de la luna.
- Sincronizar changelogs localizados si se decide.

---

## 8. Estado tecnico

- Repo local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde
- Deploy: D:\Juegos\Minecraft\Instances\Test\mods
- Ultimo tag: v0.7.1
- Build limpio.
- Working tree esperado: limpio antes de iniciar v0.7.2.