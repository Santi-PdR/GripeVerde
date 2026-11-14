# Corrección de CEDA Dispatch (CopyL) 3.9.5 — parche listo

`0001-copyl-3.9.5-one-text-pass.patch` corrige los dos fallos reportados en la pantalla
de edición de CEDA Dispatch. Es un parche `git format-patch` de un commit real aplicado
sobre `Santi-PdR/Lclient`, rama `codex/ceda-dispatch-visuals` @ `27854f6` (versión 3.9.4).

- SHA-256 del parche: `856be3c7cfdc9b8d5c662fdb091c7d98343ff4e2ab1cedb6dd7b8f60c8bef298`
- Aplicado y verificado en un clon limpio: `git am` entra sin conflictos (commit
  «Release 3.9.5: one text pass and no custom panel fonts»).

## Por qué está aquí y no empujado

La integración de GitHub de esta sesión puede escribir en `Santi-PdR/GripeVerde`, pero
devuelve `403 Resource not accessible by integration` al crear refs o empujar en
`Santi-PdR/Lclient`. Por eso el cambio queda como parche reproducible en lugar de una rama
publicada.

## Qué corrige

1. **Texto duplicado o superpuesto en el editor.** `MessageEditorScreen.render()` dibujaba
   los campos dos veces: `super.render()` ya pinta cada `EditBox` (texto, resaltado y
   cursor) y después una segunda pasada pintaba el mismo texto tras `graphics.flush()`.
   Se elimina esa pasada y la subclase `DispatchEditBox` que solo existía para ella; los
   campos vuelven a ser `EditBox` de vanilla con su propio color de tinta.
2. **Etiquetas bajo los campos.** Las tarjetas de slot dejan 6 px entre los campos y la
   regla del margen, así el número de slot y la etiqueta `CHAT`/`CMD` no quedan debajo de
   una caja de texto. Todas las filas se miden desde el borde de la tarjeta.
3. **Glifos cuadrados (tofu).** Se borra el proveedor de fuente sin uso
   (`assets/copyl/font/dispatch.json`, `AtkinsonHyperlegible-Regular.ttf` y `OFL.txt`).
   El CI ahora falla si vuelve a empaquetarse algo bajo `assets/copyl/font/`.
4. Versión `3.9.5` en `build.gradle` y `mods.toml`, más `CHANGELOG.md` y `README.md`.

## Cómo aplicarlo

```bash
git clone https://github.com/Santi-PdR/Lclient.git
cd Lclient
git checkout codex/ceda-dispatch-visuals
git am /ruta/a/0001-copyl-3.9.5-one-text-pass.patch
git push origin codex/ceda-dispatch-visuals
```

El workflow *Build* de Lclient compila, valida el JAR empaquetado y publica el artefacto
`CopyL-3.9.5`. Con eso, la instalación en la carpeta de mods reemplaza el
`copyl-3.9.4.jar` anterior.

## Verificación ya realizada (sin Minecraft)

- `git am` limpio sobre `27854f6`.
- Balance de llaves y paréntesis en los `.java` del proyecto: correcto.
- Paridad de claves en los 25 archivos de idioma y 13 idiomas declarados en `pack.mcmeta`:
  sin cambios.
- Búsqueda final: no quedan referencias a `drawFlatText`, `dispatchFont` ni `Atkinson`.
- Compilación real: pendiente, la ejecuta el CI de Lclient al empujar la rama.
