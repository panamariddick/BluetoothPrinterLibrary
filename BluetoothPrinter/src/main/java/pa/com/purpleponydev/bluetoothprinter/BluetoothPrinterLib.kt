package pa.com.purpleponydev.bluetoothprinter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.CharacterSetECI
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothPrinterLib(
    applicationContext: Context,
    paperWidth: Int = DEFAULT_PAPER_WIDTH
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    companion object {
        // Valor por defecto para el ancho del papel || Default value for paper width
        const val DEFAULT_PAPER_WIDTH = 400
    }
    /**
     * Cierra la conexión Bluetooth liberando recursos.
     */
    fun closeConnection() {
        try {
            outputStream?.closeQuietly()
            bluetoothSocket?.closeQuietly()
        } catch (e: IOException) {
            Timber.e("*----> Mensaje||Message: Error al cerrar conexión con la impresora | Error closing connection with printer")
        }
    }
    private fun OutputStream?.closeQuietly() {
        try {
            this?.close()
        } catch (e: IOException) {
            Timber.w(e, "Error al cerrar OutputStream/Error closing OutputStream")
        }
    }

    private fun BluetoothSocket?.closeQuietly() {
        try {
            this?.close()
        } catch (e: IOException) {
            Timber.w(e, "Error al cerrar BluetoothSocket/Error closing BluetoothSocket")
        }
    }

    // Función para abrir la conexión Bluetooth | Function to open the Bluetooth connection
    fun connectToPrinter(address: String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(address).also {
            if (it == null) Timber.e("*----> Mensaje||Message: No se encontró el dispositivo con la dirección proporcionada | Device was not found with the given address")
        } ?: return false

        return runCatching {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid).apply { connect() }
            outputStream = bluetoothSocket?.outputStream
            true
        }.getOrElse { e ->
            Timber.e(e, "Error al conectar con la impresora | Error connecting to the printer: ${e.message}")
            closeConnection()
            false
        }
    }
    //Funcion para enviar una StrinBuilder a la impresora | Function to send a StrinBuilder to the printer
    fun printData(data: StringBuilder) {
        try {
            if (outputStream != null) {
                // Agregar un salto de línea antes de imprimir los datos | Add a line break before printing the data
                outputStream?.write(byteArrayOf(0x0A)) // Salto de línea | line break
                outputStream?.write(data.toString().toByteArray(Charsets.UTF_8))
                outputStream?.write(byteArrayOf(0x0A)) // Salto de línea | line break
                outputStream?.flush()
            } else {
                Timber.e("No hay conexión con la impresora | There is no connection to the printer")
            }
        } catch (e: IOException) {
            Timber.e("Error al enviar datos a la impresora | Error sending data to printer: ${e.message}")
        }
    }

    // Función para imprimir un texto | Function to print text
    fun printText(text: String) {
        try {
            outputStream?.write(text.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Función para imprimir una imagen o QR | Function to print an image or QR
    fun printBitmap(bitmap: Bitmap) {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Timber.d("Bitmap es nulo o tiene dimensiones incorrectas | Bitmap is null or has incorrect dimensions.")
            return
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 600, bitmap.height, false)
        val bmpWidth = resizedBitmap.width
        val bmpHeight = resizedBitmap.height
        val buffer = ByteArray((bmpWidth + 7) / 8 * bmpHeight)

        var k = 0
        for (y in 0 until bmpHeight) {
            for (x in 0 until bmpWidth step 8) {
                var byte = 0
                for (i in 0 until 8) {
                    if (x + i < bmpWidth && resizedBitmap.getPixel(x + i, y) != Color.WHITE) {
                        byte = byte or (1 shl (7 - i))
                    }
                }
                buffer[k++] = byte.toByte()
            }
        }

        val command = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00,
            (600 / 8).toByte(), ((600 / 8) shr 8).toByte(),
            bmpHeight.toByte(), (bmpHeight shr 8).toByte()
        )

        try {
            outputStream?.write(command)
            outputStream?.write(buffer)
            outputStream?.write(byteArrayOf(0x0A)) // Nueva línea después de la imagen | New line after image

            // Restablecer alineación a la izquierda para el texto siguiente | Reset left alignment for next text
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00)) // Alinear a la izquierda | Align left
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    //Funcion imprimir un QR (Problemas algunas impresoras) | Function to print a QR (Problems on some printers)
    fun printBitmapQR(bitmap: Bitmap, paperWidth: Int = 400, fixedSize: Int = 400) {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Timber.d("Bitmap es nulo o tiene dimensiones incorrectas | Bitmap is null or has incorrect dimensions.")
            return
        }

        // Redimensionar el bitmap a 400x400 píxeles | Resize the bitmap to 400x400 pixels
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, fixedSize, fixedSize, false)

        val bmpWidth = resizedBitmap.width
        val bmpHeight = resizedBitmap.height

        // Calcular el margen izquierdo para centrar la imagen | Calculate the left margin to center the image
        val marginWidth = (paperWidth - bmpWidth) / 2
        val buffer = ByteArray((paperWidth + 7) / 8 * bmpHeight)

        var k = 0
        for (y in 0 until bmpHeight) {
            // Rellenar el margen izquierdo | Fill the left margin
            for (m in 0 until marginWidth step 8) {
                buffer[k++] = 0x00 // Espacio en blanco para el margen | Blank space for margin
            }
            // Convertir cada fila de píxeles del bitmap a bytes | Convert each row of pixels in the bitmap to bytes
            for (x in 0 until bmpWidth step 8) {
                var byte = 0
                for (i in 0 until 8) {
                    if (x + i < bmpWidth && resizedBitmap.getPixel(x + i, y) != Color.WHITE) {
                        byte = byte or (1 shl (7 - i))
                    }
                }
                buffer[k++] = byte.toByte()
            }
        }

        val command = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00,
            (paperWidth / 8).toByte(), ((paperWidth / 8) shr 8).toByte(),
            bmpHeight.toByte(), (bmpHeight shr 8).toByte()
        )

        try {
            outputStream?.write(command)
            outputStream?.write(buffer)
            outputStream?.write(byteArrayOf(0x0A)) // Nueva línea después de la imagen | New line after image

            // Restablecer alineación a la izquierda para el texto siguiente | Reset left alignment for next text
            outputStream?.write(byteArrayOf(0x1B, 0x61, 0x00)) // Alinear a la izquierda | Align left
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    //Funcion para generar un codigo QR | Function to generate a QR code
    @Throws(WriterException::class)
    fun generateQR(value: String) : Bitmap? {
        val bitMatrix: BitMatrix
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.CHARACTER_SET] = CharacterSetECI.UTF8

            bitMatrix = MultiFormatWriter().encode(
                value,
                BarcodeFormat.QR_CODE,
                300,
                300,
                hints
            )
        }catch (Illegalargumentexception : IllegalArgumentException){
            return null
        }

        val bitMatrixWidth = bitMatrix.width

        val bitMatrixHeight = bitMatrix.height

        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth

            for (x in 0 until bitMatrixWidth) {

                pixels[offset + x] = if (bitMatrix.get(x, y))
                    Color.BLACK
                else
                    Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, bitMatrixWidth, 0, 0, bitMatrixWidth, bitMatrixHeight)

        return bitmap
    }

    //Funcion para ajustar e imprimir el Bitmap considerando el tamaño del papel | function to adjust and print the Bitmap considering the paper size
    fun padAndPrintBitmap(bitmap: Bitmap, paperWidth: Int) {
        // Calcular padding en caso de que el bitmap sea más pequeño que el ancho del papel | Calculate padding in case the bitmap is smaller than the width of the paper
        val padding_x = if (bitmap.width < paperWidth) (paperWidth - bitmap.width) / 2 else 0
        val paddedBitmap = pad(bitmap, padding_x, 0)

        // Enviar el bitmap con padding a la función de impresión | Send the bitmap with padding to the print function
        printBitmap(paddedBitmap)
    }

    // Función de padding para centrar el bitmap en el ancho especificado | Padding function to center the bitmap at the specified width
    fun pad(src: Bitmap, paddingX: Int, paddingY: Int): Bitmap {
        val outputImage = Bitmap.createBitmap(src.width + paddingX * 2, src.height + paddingY * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputImage)
        canvas.drawARGB(255, 255, 255, 255) // Color blanco de fondo | background white color
        canvas.drawBitmap(src, paddingX.toFloat(), paddingY.toFloat(), null)
        return outputImage
    }

}
