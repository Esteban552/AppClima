# PulsoClima para Wear OS

Aplicación nativa de clima para relojes Wear OS, escrita en Kotlin y Jetpack Compose. Usa la ubicación del reloj y datos reales de [Open-Meteo](https://open-meteo.com/) sin API key.

## Funciones

- Condiciones actuales, sensación térmica y descripción WMO.
- Humedad, viento, presión atmosférica y nubosidad.
- Pronóstico horario para las próximas 12 horas.
- Pronóstico de cinco días con máximas, mínimas y lluvia.
- Amanecer y atardecer en la zona horaria local.
- Ubicación del reloj con permiso en tiempo de ejecución.
- Fallback visible a Ciudad de México si no se concede ubicación.
- Caché del último reporte cuando no hay conexión.
- Diseño para pantallas redondas y cuadradas, compatible con corona rotatoria.

## Requisitos

- Android Studio con JDK 17.
- Android SDK 35.
- Emulador Wear OS con API 30 o posterior, o un reloj físico.

## Ejecutar en un emulador

1. Abrí el proyecto `AppClima` en Android Studio.
2. Esperá a que Gradle sincronice e instale las dependencias.
3. En **Device Manager**, elegí **Create device > Wear OS > Wear OS Small Round**.
4. Descargá una imagen de Wear OS API 35 con Google APIs y arrancá el emulador.
5. Seleccioná el dispositivo Wear OS y ejecutá la configuración `app`.
6. Aceptá el permiso de ubicación. Para simular otra ciudad, usá **Extended controls > Location** en el emulador y actualizá desde el botón inferior.

Si denegás el permiso, la app sigue funcionando con una ubicación de referencia claramente indicada.

## Verificar

Desde la terminal integrada de Android Studio:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Arquitectura

- `data/`: modelos, cliente HTTP Open-Meteo, parser y caché.
- `location/`: lectura de ubicación y nombre de ciudad.
- `MainViewModel`: estado, carga, fallback y recuperación offline.
- `ui/`: tema y pantalla Compose para Wear OS.

Open-Meteo entrega el pronóstico. La atribución también se muestra dentro de la aplicación.
