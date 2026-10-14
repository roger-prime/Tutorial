# Tutorial — Proyecto Práctico TIC

Aplicación Android en Kotlin y Jetpack Compose, con dos botones en una columna,
un saludo centrado y un diseño azul con bordes redondeados.

## Botones

- **Consultar API:** solicita una publicación real a la API de prueba
  [JSONPlaceholder](https://jsonplaceholder.typicode.com/), muestra su título en
  un Toast y registra el resultado en Logcat. Mientras espera, el botón indica
  «Consultando...» y queda desactivado para evitar peticiones repetidas.
- **Sumar un clic:** funciona de forma local, incrementa el contador visible,
  muestra un Toast con el nuevo total y registra cada pulsación en Logcat.
  El contador conserva su valor al girar el dispositivo.

La consulta es `GET https://jsonplaceholder.typicode.com/posts/1`.
Los datos son ficticios y el servicio no requiere una clave de API.

## Abrir y ejecutar

1. Abrir la carpeta del proyecto en Android Studio.
2. Sincronizar Gradle para descargar Retrofit, Gson y las corrutinas.
3. Elegir un emulador o un teléfono con conexión a Internet.
4. Ejecutar la configuración **app** con **Run**.

El repositorio ya incluye el permiso `android.permission.INTERNET`.
El Preview muestra el diseño; la prueba de Toast y Logcat se hace ejecutando la app
en el emulador o en un teléfono.

## Ver los mensajes en Logcat

1. Con la app ejecutándose, abrir **View > Tool Windows > Logcat**.
2. Seleccionar el mismo dispositivo en el que se ejecutó la app.
3. Escribir este filtro en la barra de búsqueda:

   ```text
   package:mine tag:Tutorial level:DEBUG
   ```

4. Pulsar **Consultar API**. Se registra la pulsación de inmediato y, cuando
   responde el servidor, aparecen el ID, el usuario, el título y el contenido:

   ```text
   Botón Consultar API presionado: GET /posts/1
   JSONPlaceholder respondió: publicación 1
   ID: 1
   Usuario: 1
   Título: ...
   Contenido: ...
   ```

5. En el teléfono aparece un Toast con el título recibido. Android puede recortar
   los títulos largos a dos líneas; el título completo queda en Logcat.
6. Pulsar **Sumar un clic** para ver el contador, el Toast «¡Clic registrado!
   Total: 1» y el mensaje correspondiente en Logcat. Este botón funciona sin Internet.

Si falla la consulta, se muestra un Toast de error y se escribe la excepción con
`Log.e`. El botón vuelve a habilitarse para reintentar. Si la pantalla se cierra
o gira durante una consulta, esta se cancela sin mostrar un falso error de conexión.

## Archivos principales

| Archivo | Función |
| --- | --- |
| `MainActivity.kt` | Pantalla, acciones de los botones, Toast y Logcat. |
| `model/Post.kt` | Campos de la publicación recibida. |
| `network/JsonPlaceholderApi.kt` | Ruta GET de la API. |
| `network/RetrofitClient.kt` | Dirección del servidor y conversión de JSON. |
| `AndroidManifest.xml` | Permiso de Internet. |

## Comprobaciones

Las pruebas de `JsonPlaceholderApiTest` usan un servidor local simulado para
verificar la ruta, la lectura de todos los campos y el tratamiento de un error HTTP.

```bash
# Linux/macOS
bash gradlew :app:testDebugUnitTest :app:assembleDebug
```

```powershell
# Terminal de Android Studio en Windows
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Para probar manualmente un fallo de red, desconectar Internet en el dispositivo
antes de consultar y esperar el resultado. Luego reconectar y volver a pulsar el botón.

Referencias: [JSONPlaceholder](https://jsonplaceholder.typicode.com/),
[Logcat](https://developer.android.com/studio/debug/logcat) y
[Toast](https://developer.android.com/guide/topics/ui/notifiers/toasts).
