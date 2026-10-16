# Aportes identificables de Roger y Johan

La consigna exige contribuciones identificables y defensa individual. La preparación asistida de esta
base no reemplaza la participación de ambos integrantes. No atribuir cambios a quien no los realizó.

## Roger — propuesta de aporte real

Probar la app en su emulador y ajustar un texto o validación según lo observado. Agregar en
`docs/prueba-roger.md` fecha, emulador utilizado, pasos, resultado real y problema encontrado si lo hubiera.
Puede adjuntar una captura propia de Inicio y Lugares después de la descarga.

## Johan — propuesta de aporte real

Ejecutar el backend, agregar un lugar real de la facultad a `backend/src/main/resources/data.sql`
con un ID nuevo y actualizar la prueba que verifica la cantidad de lugares. Registrar en
`docs/prueba-johan.md` el endpoint consultado y su resultado real. Una opción más pequeña es
documentar una corrección real de configuración descubierta durante la instalación.

## Cómo registrar un aporte

Cada uno configura Git con su identidad real, sin copiar la del compañero:

```text
git config user.name "Tu nombre"
git config user.email "Tu correo asociado a GitHub"
git status
git add ruta/del/archivo/modificado
git commit -m "Describir el cambio real que realizaste"
git pull --rebase
git push
```

Si Johan no tiene permiso para subir, Roger debe invitar su cuenta desde Settings → Collaborators
del repositorio, o Johan puede trabajar en un fork y abrir un pull request.
Evitar modificar simultáneamente el mismo archivo. No subir `local.properties`, bases de datos,
contraseñas, carpetas `build` o `target`.

## Preguntas para preparar la defensa

1. ¿Por qué la app consulta SQLite y no directamente el servidor?
2. ¿Qué diferencia hay entre un reporte pendiente y uno sincronizado?
3. ¿Qué hace `10.0.2.2` en el emulador?
4. ¿Por qué el trabajo de red se ejecuta fuera del hilo principal?
5. ¿Por qué cada reporte conserva el mismo UUID al reintentar su envío?
6. ¿Qué hace cada tabla y cómo se relacionan sus claves?
