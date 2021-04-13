import kotlinx.cinterop.convert
import platform.posix.sleep

fun main() {
    println("Hello, Kotlin/Native!")
    val peripheralManager = PeripheralManager()
//    val device = peripheralManager.openI2cDevice("I2C1", 0x0)
//    device.close()
    val pin = peripheralManager.openGpio("BCM22")
    pin.direction = GpioPin.Direction.OUT_START_LOW
    pin.on()
    sleep(1.convert())
    pin.off()
}