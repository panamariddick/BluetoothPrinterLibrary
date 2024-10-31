package pa.com.purpleponydev.bluetoothprinter.helpers

/**
 *Puedes utilizar estas funciones sin necesidad de instanciar la clase
 */

class GeneralTextHelper {
    companion object {
        fun centerText(text: String, totalWidth: Int): String {
            val stringBuilder = StringBuilder()

            val textLength = text.length
            val totalSpaces = totalWidth - textLength

            if (totalSpaces <= 0) {
                return text
            }

            val leftSpaces = totalSpaces / 2
            val rightSpaces = totalSpaces - leftSpaces

            stringBuilder.append(" ".repeat(leftSpaces))
            stringBuilder.append(text)
            stringBuilder.append(" ".repeat(rightSpaces))

            return stringBuilder.toString()
        }

        fun centerTextFooter(text: String, totalWidth: Int): String {
            val words = text.split(" ")  // Dividir el texto en palabras | Split text into words
            val result = StringBuilder()
            var currentLine = StringBuilder()

            for (word in words) {
                // Verificar si añadir la siguiente palabra excedería el límite de longitud | Check if adding the next word would exceed the length limit
                if (currentLine.length + word.length + 1 > totalWidth) {
                    // Centrar la línea actual y agregarla al resultado | Center the current line and add it to the result
                    result.append(centerLine(currentLine.toString().trim(), totalWidth))
                    result.append(System.getProperty("line.separator"))
                    // Comenzar una nueva línea con la palabra actual | Start a new line with the current word
                    currentLine = StringBuilder(word)
                } else {
                    if (currentLine.isNotEmpty()) {
                        currentLine.append(" ")  // Añadir espacio entre palabras | Add space between words
                    }
                    currentLine.append(word)
                }
            }

            // Añadir la última línea centrada | Add the last centered line
            if (currentLine.isNotEmpty()) {
                result.append(centerLine(currentLine.toString().trim(), totalWidth))
            }

            return result.toString()
        }

        // Función auxiliar para centrar una línea de texto | Helper function to center a line of text
        private fun centerLine(text: String, totalWidth: Int): String {
            val stringBuilder = StringBuilder()

            val textLength = text.length
            val totalSpaces = totalWidth - textLength

            if (totalSpaces <= 0) {
                return text
            }

            val leftSpaces = totalSpaces / 2
            val rightSpaces = totalSpaces - leftSpaces

            stringBuilder.append(" ".repeat(leftSpaces))
            stringBuilder.append(text)
            stringBuilder.append(" ".repeat(rightSpaces))

            return stringBuilder.toString()
        }
    }
}