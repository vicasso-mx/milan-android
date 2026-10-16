# MILAN Android

Aplicación nativa de aprendizaje para Android, implementada con Kotlin, Jetpack Compose y Material 3. Esta entrega cubre la estructura inicial, el catálogo local y el carrito en memoria. No incluye autenticación, revisión de pedido, confirmación de compra ni historial.

## Requisitos

- JDK 21 (64 bits).
- Android SDK con plataformas API 35/36 y Build Tools 36.0.0.
- Gradle Wrapper incluido; no requiere Gradle global.
- Windows PowerShell para los comandos siguientes.

En la sesión actual, configure variables solo para el proceso de compilación:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:ANDROID_HOME = 'C:\Program Files (x86)\Android\android-sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

## Compilar y probar

```powershell
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`. La primera ejecución descarga Gradle 8.13 y las dependencias de Maven Central/Google Maven.

## Tecnologías y estructura

- `model`: productos y líneas del carrito.
- `data`: catálogo estático y repositorio de carrito en memoria.
- `viewmodel`: estado de navegación inicial y acciones de la interfaz.
- `ui`: catálogo, carrito y tema Material 3 con colores MILAN.
- `app/src/main/res/drawable-nodpi`: logotipo, banner y 12 imágenes originales de producto.
- `app/src/test`: pruebas unitarias de catálogo y reglas del carrito.

El paquete de la aplicación es `com.doceolab.milan`; `minSdk` es 26 y `compileSdk`/`targetSdk` son 36. Los precios se expresan en centavos para calcular subtotales y total sin aritmética de punto flotante, y se muestran como MXN.

## Alcance de esta entrega

Se puede recorrer el catálogo, agregar productos al carrito, aumentar cantidades, disminuirlas hasta una unidad, eliminar líneas y consultar subtotales y total. «Finalizar compra» está deshabilitado hasta implementar las fases posteriores autorizadas. El estado permanece en memoria durante navegación y cambios de configuración, y se pierde si termina el proceso.

