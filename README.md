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
    val qrBitmap = bluetoothPrinter.generateQR("https://openai.com")
    qrBitmap?.let { bluetoothPrinter.printBitmap(it) }

    // Cerrar conexión
    bluetoothPrinter.closeConnection()
} else {
    Timber.e("No se pudo conectar a la impresora.")
}
