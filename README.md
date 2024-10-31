# Ejemplo de Uso de BluetoothPrinterLib

A continuación se muestra un ejemplo de cómo utilizar la biblioteca BluetoothPrinterLib en una aplicación Android:

```kotlin
val bluetoothPrinter = BluetoothPrinterLib(applicationContext)

// Conectar a la impresora
val isConnected = bluetoothPrinter.connectToPrinter("00:11:22:33:44:55")

if (isConnected) {
    // Enviar texto
    bluetoothPrinter.printText("¡Hola, impresora!")
    
    //Enviar un StringBuilder
    bluetoothPrinter.printData(strData) //strData: Texto con saltos de linea mas amplios
    
    // Generar y enviar código QR
    val qrBitmap = bluetoothPrinter.generateQR("https://play.google.com/store/apps/details?id=admin.purple.metrobuspanamasaldo&hl=es_419")
    qrBitmap?.let { bluetoothPrinter.printBitmap(it) }

    // Cerrar conexión
    bluetoothPrinter.closeConnection()
} else {
    Timber.e("No se pudo conectar a la impresora.")
}

## Compilación del archivo .aar

Para compilar el archivo `.aar`, utiliza el siguiente comando en la consola:

```bash
./gradlew BluetoothPrinter:assemble

Una vez finalizado el proceso, puedes ubicar el archivo .aar en la siguiente carpeta:
\build\outputs\aar\BluetoothPrinter-release.aar
