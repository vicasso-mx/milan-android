# MILAN Android

MILAN Android es una aplicación nativa de aprendizaje que reproduce el recorrido de compra del perfil cliente de MILAN. Está escrita en Kotlin y usa Jetpack Compose con Material 3.

El proyecto `../milan-mct` fue la referencia funcional y visual para el catálogo, los precios, el carrito, la autenticación de demostración y el flujo de pedidos. MILAN Android es independiente: no comparte código C#, no se conecta al sistema original y no modifica ese repositorio.

## Alcance

- Catálogo local con los 12 productos de la referencia e imágenes incluidas como recursos Android.
- Carrito con cantidades, subtotales, total en MXN y compra sin autenticación previa.
- Acceso de demostración con las dos cuentas indicadas abajo; ambas reciben funciones de cliente.
- Revisión de pedido, confirmación simulada, agradecimiento, historial y detalle de solo lectura.
- Sin backend, API, base de datos, almacenamiento persistente ni servicios de pago.

«Pagar ahora» solamente confirma un pedido en memoria. No solicita información bancaria ni procesa pagos reales.

## Tecnologías y versiones

- Kotlin: 2.3.21.
- Android Gradle Plugin: 8.13.2.
- Gradle Wrapper: 8.13.
- Jetpack Compose BOM: 2025.12.00.
- Activity Compose: 1.12.0.
- Material 3: versión administrada por Compose BOM.
- JUnit: 4.13.2.
- Android SDK: `compileSdk` 36, `targetSdk` 36, `minSdk` 26.
- JDK: 21 para ejecutar Gradle; el bytecode de la app compila para JVM 17.

Las versiones están declaradas en `gradle/libs.versions.toml`, `app/build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`. No se requiere Gradle instalado globalmente.

## Requisitos en Windows 11

- JDK 21 de 64 bits.
- Android SDK con Android Platform 36 y Build Tools 36.0.0 para compilar.
- Gradle Wrapper del repositorio (`gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` y `gradle-wrapper.properties`).
- PowerShell y acceso de red la primera vez que se descarga Gradle o se resuelven dependencias.
- Opcional para ejecutar en emulador: Emulator, Platform Tools, imagen `system-images;android-35;google_apis;x86_64`, virtualización de CPU y WHPX disponible.

No se necesita Android Studio. En el entorno usado para esta verificación, JDK y SDK están en las rutas siguientes; ajusta los valores si están instalados en otra ubicación. Estas asignaciones solo afectan la ventana actual de PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:ANDROID_HOME = 'C:\Program Files (x86)\Android\android-sdk'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:ANDROID_HOME\platform-tools;$env:Path"
```

Para instalar componentes sin Android Studio, usa `sdkmanager.bat` de Command-line Tools. Selecciona una carpeta SDK con permisos de escritura y ajusta `$sdk` a su ruta:

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" --sdk_root=$sdk `
  'platform-tools' 'platforms;android-36' 'build-tools;36.0.0' `
  'emulator' 'system-images;android-35;google_apis;x86_64'
```

Acepta las licencias que solicite `sdkmanager`. Android Platform 35 puede añadirse también para compilar contra API 35; la configuración actual compila contra API 36.

## Compilar y probar desde PowerShell

Desde la raíz del repositorio, configura `JAVA_HOME`, `ANDROID_HOME` y `ANDROID_SDK_ROOT` como se muestra arriba y ejecuta:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

El APK de depuración se genera en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

La primera ejecución descarga Gradle 8.13 y las dependencias declaradas desde Google Maven y Maven Central. Las pruebas unitarias no necesitan emulador.

## Emulador e instalación del APK

El AVD usado para la verificación se llama `MILAN_API_35`, con Android API 35, imagen Google APIs x86_64, 2 GB de RAM y 2 núcleos virtuales. Para iniciar ese AVD con el SDK local del usuario:

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_AVD_HOME = "$env:USERPROFILE\.android\avd"
& "$sdk\emulator\emulator.exe" -avd MILAN_API_35 -memory 2048 -cores 2 -gpu auto
```

Con el emulador iniciado, instala y abre la aplicación:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb install -r 'app\build\outputs\apk\debug\app-debug.apk'
& $adb shell monkey -p com.doceolab.milan 1
```

Si `adb devices` muestra más de un dispositivo, añade `-s emulator-5554` (o el identificador correspondiente) antes del subcomando.

## Credenciales de demostración

- Correo: `cliente@doceolab.com` o `admin@doceolab.com`.
- Contraseña: `Pass@word1`.

La autenticación es local y educativa; no es un mecanismo apropiado para proteger información real. Las dos cuentas tienen las mismas opciones de cliente.

## Arquitectura

```text
app/src/main/java/com/doceolab/milan/
├── data/       Catálogo, autenticación simulada y repositorios en memoria
├── model/      Producto, línea de carrito, línea de pedido y pedido
├── ui/         Pantallas Compose, formato MXN y tema Material 3
└── viewmodel/  Estado de navegación, sesión, carrito y pedidos
```

`MainActivity` conserva un `StoreViewModel` mediante el `ViewModelStore` de Android. Las pantallas observan sus `StateFlow`; los componentes visuales no acceden a almacenamiento ni servicios externos. El carrito usa centavos enteros y operaciones exactas; el formato de pantalla convierte los centavos a `BigDecimal` para evitar redondeo binario.

## Estado y ciclo de vida

La identidad, el carrito, las compras pendientes, el pedido activo y el historial viven exclusivamente en memoria dentro del `StoreViewModel` y repositorios en memoria. Por ello se conservan al navegar y durante cambios de configuración mientras Android conserva el `ViewModel`, y pueden perderse al terminar el proceso. Cerrar sesión elimina identidad, carrito, pedidos y estado de compra, vuelve al catálogo y deja inaccesibles los destinos protegidos.

No se usan Room, SQLite, DataStore, SharedPreferences, archivos transaccionales, backend ni sincronización. El manifiesto desactiva las copias de seguridad de la app.

## Pruebas y verificación

Ejecuta todas las pruebas locales con:

```powershell
.\gradlew.bat testDebugUnitTest
```

Las pruebas cubren catálogo, carrito, credenciales, navegación protegida, confirmación única, instantáneas de pedido, cálculos, orden del historial y limpieza al cerrar sesión. La navegación, las pantallas y el flujo de compra también se verifican manualmente en el AVD API 35. No se añadió un conjunto de pruebas instrumentadas: para el alcance de esta app educativa se usa la cobertura unitaria del dominio y una comprobación UI directa en el emulador.

Resultados de la última validación integral:

- `testDebugUnitTest`: 28 pruebas aprobadas, 0 fallidas.
- `assembleDebug`: compilación correcta.
- Emulador API 35 x86_64: catálogo, login desde el carrito, revisión, confirmación simulada, agradecimiento, historial, detalle, seguir comprando y cierre de sesión comprobados manualmente.
- No se requieren dependencias faltantes para compilar o ejecutar la app.

El SDK instalado bajo `Program Files` puede emitir avisos al no poder actualizar sus metadatos de solo lectura. No impidieron las pruebas ni la generación del APK.

## Exclusiones

No se implementan registro, recuperación de contraseña, perfil de cuenta, permisos administrativos, gestión de inventario, direcciones, facturación, pagos reales, notificaciones, analítica, persistencia local, API ni servicios de sincronización. El estado de sesión y de compra se pierde al terminar el proceso.
