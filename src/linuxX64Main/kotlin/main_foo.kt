import com.robgulley.hwint.*
import com.robgulley.hwint.test.MPU6050
import com.robgulley.time.Sleep
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import platform.posix.sleep
import kotlin.native.concurrent.freeze
import kotlin.system.getTimeMicros

@OptIn(ExperimentalUnsignedTypes::class)
fun main_foo(args: Array<String>): Unit = runBlocking {
    println("Hello, Kotlin/Native!")
    uartTest(args[0], this)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun uartTest(uartName: String, coroutineScope: CoroutineScope) = coroutineScope.launch {
    val CHUNK_SIZE = 512
    val uartDevice = PeripheralManager().openUartDevice(uartName, this)
    uartDevice.baudRate = 9600
    uartDevice.registerUartDeviceCallback(object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            val buffer = ByteArray(CHUNK_SIZE)
            var count: Int
            while (uart.read(buffer, buffer.size.toLong()).also { count = it } > 0) {
                println("${buffer.toKString()} --- $count")
            }
            return true
        }


        override fun onUartDeviceError(uart: UartDevice, error: Int) {
            throw Exception("uart device error $error")
        }

    })
}

private suspend fun i2cReadWriteTest(coroutineScope: CoroutineScope) = coroutineScope.launch {
    val peripheralManager = PeripheralManager()
    val mpu6050 = MPU6050(peripheralManager, this)
    delay(500)

    launch { mpu6050.accelData.collect { println("accel: x: ${it.x.format(3)}\ty: ${it.y.format(3)}\tz: ${it.z.format(3)}") } }
    launch { mpu6050.gyroData.collect { println("gyro:  x: ${it.x.format(3)}\ty: ${it.y.format(3)}\tz: ${it.z.format(3)}") } }
    mpu6050.startRead()
    delay(7500)
    mpu6050.close()
    mpu6050.stopRead()
    currentCoroutineContext().cancelChildren()
}

private fun i2cHelloTest() = runBlocking {
    val peripheralManager = PeripheralManager()
    val i2cDevice = peripheralManager.openI2cDevice("I2C0", 0x68)
    delay(100)
    val whoAmIByte = i2cDevice.readRegByte(0x75)
    val whoAmIWord = i2cDevice.readRegWord(0x75)
    val whoAmIBuffer = ByteArray(1).also { i2cDevice.readRegBuffer(0x75, it, 1) }.first()
    i2cDevice.close()

    printlnTime("who am I? expect: 0x73, actual ${whoAmIByte.toHexString()}")
    printlnTime("who am I? expect: 0x73, actual ${whoAmIWord.toHexString()}")
    printlnTime("who am I? expect: 0x73, actual ${whoAmIBuffer.toHexString()}")
}

private fun gpioInterruptTest(sourcePin: Int, targetPin: Int, repeat: Int, coroutineScope: CoroutineScope) =
    coroutineScope.launch {
        val peripheralManager = PeripheralManager()
        val pinSource = peripheralManager.openGpio(sourcePin)
        delay(100)
        val pinTarget = peripheralManager.openGpio(targetPin)
        delay(100)

        pinSource.direction = GpioPin.Companion.Direction.OUT
        pinTarget.direction = GpioPin.Companion.Direction.IN
        pinTarget.edgeTriggerType = GpioPin.Companion.EdgeTriggerType.RISING

//    val flow = MutableSharedFlow<String>(replay = 4, extraBufferCapacity = 4).freeze()
        val broadcastChannel = BroadcastChannel<String>(BUFFERED).freeze()
        val receiveChannel = broadcastChannel.openSubscription().freeze()

        launch {
//        CoroutineWorker.withContext(IODispatcher) {
            printlnTime("start")
            val list = mutableListOf<String>()
            printlnTime("start flow")
            receiveChannel.consumeEach { list.add(it) }
//            flow.take(4).collect { list.add(it) }
            printlnTime("got flow")
            list.forEach { println(it) }
//        }
        }

        pinTarget.registerGpioCallback {
            val time = (getTimeMicros() - started) / 1000000.0
            broadcastChannel.offer("$time")
        }
        printlnTime("directions set")

        for (count in 1..repeat) {
            printlnTime("test $count of $repeat")
            pinSource.off()
            printlnTime("source off")
            delay(250)
            printlnTime("slept .25 second")
            pinSource.on()
            printlnTime("source on")
            delay(250)
            printlnTime("slept .25 second")
            pinSource.off()
            printlnTime("off")
        }

        pinSource.close()
        pinTarget.unregisterGpioCallback()
        pinTarget.close()
        printlnTime("closed")
        broadcastChannel.close()
    }

private fun basicGpioRead(sourcePin: Int, targetPin: Int, repeat: Int) = runBlocking {
    val peripheralManager = PeripheralManager()
    val pinSource = peripheralManager.openGpio(sourcePin)
    Sleep.blockFor(100)
    val pinTarget = peripheralManager.openGpio(targetPin)
    Sleep.blockFor(100)
    pinSource.direction = GpioPin.Companion.Direction.OUT
    pinTarget.direction = GpioPin.Companion.Direction.IN
    printlnTime("directions set")
    for (count in 1..repeat) {
        printlnTime("test $count of $repeat")
        pinSource.off()
        printlnTime("source off")
        Sleep.blockFor(500)
        val read1 = pinTarget.value
        printlnTime("slept .5 second, target pin reads $read1")
        pinSource.on()
        printlnTime("source on")
        Sleep.blockFor(500)
        val read2 = pinTarget.value
        printlnTime("slept .5 second, target pin reads $read2")
        pinSource.off()
        printlnTime("off")
    }
    pinSource.close()
    pinTarget.close()
    printlnTime("closed")
}

private fun basicGpioLoop(pins: Array<Int>) = runBlocking {
    val peripheralManager = PeripheralManager()
    for (i in pins) {
        try {
            printlnTime("try pin $i")
            val pin = peripheralManager.openGpio(i)
            printlnTime("init'd")
            pin.direction = GpioPin.Companion.Direction.OUT
            printlnTime("out set")
            pin.off()
            printlnTime("off")
            sleep(1.convert())
            printlnTime("slept 1")
            pin.on()
            printlnTime("on")
            sleep(1.convert())
            printlnTime("slept 1")
            pin.off()
            printlnTime("off")
            pin.close()
            printlnTime("closed")
        } catch (e: Throwable) {
            println(e)
        }
    }
}

val started = getTimeMicros()

fun printlnTime(msg: String) {
    val time = (getTimeMicros() - started) / 1000000.0
    println("$time: $msg")
}
