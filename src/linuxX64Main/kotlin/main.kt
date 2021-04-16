import com.robgulley.Sleep
import kotlinx.cinterop.convert
import platform.posix.sleep

fun main(args: Array<String>) {
    println("Hello, Kotlin/Native!")
   basicRead(args[0].toInt(), args[1].toInt(), args[2].toInt())
}
private fun basicRead(sourcePin: Int, targetPin: Int, repeat:Int){
    val peripheralManager = PeripheralManager()
    val pinSource = peripheralManager.openGpio(sourcePin)
    Sleep.blockFor(100)
    val pinTarget = peripheralManager.openGpio(targetPin)
    Sleep.blockFor(100)
    pinSource.direction = GpioPin.Companion.Direction.OUT
    pinTarget.direction = GpioPin.Companion.Direction.IN
    println("directions set")
    for (count in 1..repeat) {
        println("test $count of $repeat")
        pinSource.off()
        println("source off")
        Sleep.blockFor(500)
        val read1 = pinTarget.value
        println("slept .5 second, target pin reads $read1")
        pinSource.on()
        println("source on")
        Sleep.blockFor(500)
        val read2 = pinTarget.value
        println("slept .5 sceond, target pin reads $read2")
        pinSource.off()
        println("off")
    }
    pinSource.close()
    pinTarget.close()
    println("closed")
}

private fun basicLoop(pins: Array<Int>) {
    val peripheralManager = PeripheralManager()
    for (i in pins) {
        try {
            println("try pin $i")
            val pin = peripheralManager.openGpio(i)
            println("init'd")
            pin.direction = GpioPin.Companion.Direction.OUT
            println("out set")
            pin.off()
            println("off")
            sleep(1.convert())
            println("slept 1")
            pin.on()
            println("on")
            sleep(1.convert())
            println("slept 1")
            pin.off()
            println("off")
            pin.close()
            println("closed")
        } catch (e: Throwable) {
            println(e)
        }
    }
}