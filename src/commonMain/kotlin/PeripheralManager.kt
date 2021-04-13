
expect class PeripheralManager {
    fun openI2cDevice(bus: String, address: Int): I2cDevice
    fun openGpio(pinName: String): GpioPin


}

fun Int.toHexString(): String {
    val base = this.toString(16).toUpperCase()
    return if (base.length % 2 == 1) {
        "0x0$base"
    } else "0x$base"
}

fun Short.toHexString(): String = "0x" + this.toString(16).toUpperCase().padStart(4, '0')
fun Byte.toHexString(): String = "0x" + this.toString(16).toUpperCase().padStart(2, '0')