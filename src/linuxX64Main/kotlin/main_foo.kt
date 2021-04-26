import com.robgulley.Sleep
import com.robgulley.hwint.GpioPin
import com.robgulley.hwint.PeripheralManager
import com.robgulley.hwint.format
import com.robgulley.hwint.test.MPU6050
import com.robgulley.hwint.toHexString
import kotlinx.cinterop.convert
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import platform.posix.sleep
import kotlin.native.concurrent.freeze
import kotlin.system.getTimeMicros

fun main_foo(args: Array<String>): Unit = runBlocking {
    kotlin.io.println("Hello, Kotlin/Native!")

    gpioInterruptTest(args[0].toInt(), args[1].toInt(), args[2].toInt(), this)
    i2cReadWriteTest(this)
}

private suspend fun i2cReadWriteTest(coroutineScope: CoroutineScope) = coroutineScope.launch  {
    val peripheralManager = PeripheralManager()
    val mpu6050 = MPU6050(peripheralManager, this)
    delay(500)

    launch { mpu6050.accelData.collect { kotlin.io.println("accel: x: ${it.x.format(3)}\ty: ${it.y.format(3)}\tz: ${it.z.format(3)}") } }
    launch { mpu6050.gyroData.collect { kotlin.io.println("gyro:  x: ${it.x.format(3)}\ty: ${it.y.format(3)}\tz: ${it.z.format(3)}") } }
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

    println("who am I? expect: 0x73, actual ${whoAmIByte.toHexString()}")
    println("who am I? expect: 0x73, actual ${whoAmIWord.toHexString()}")
    println("who am I? expect: 0x73, actual ${whoAmIBuffer.toHexString()}")
}

private fun gpioInterruptTest(sourcePin: Int, targetPin: Int, repeat: Int, coroutineScope: CoroutineScope) = coroutineScope.launch {
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
        println("start")
        val list = mutableListOf<String>()
        println("start flow")
        receiveChannel.consumeEach { list.add(it) }
//            flow.take(4).collect { list.add(it) }
        println("got flow")
        list.forEach { kotlin.io.println(it) }
//        }
    }

    pinTarget.registerGpioCallback {
        val time = (getTimeMicros() - started) / 1000000.0
        broadcastChannel.offer("$time")
    }
    println("directions set")

    for (count in 1..repeat) {
        println("test $count of $repeat")
        pinSource.off()
        println("source off")
        delay(250)
        println("slept .25 second")
        pinSource.on()
        println("source on")
        delay(250)
        println("slept .25 second")
        pinSource.off()
        println("off")
    }

    pinSource.close()
    pinTarget.unregisterGpioCallback()
    pinTarget.close()
    println("closed")
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
        println("slept .5 second, target pin reads $read2")
        pinSource.off()
        println("off")
    }
    pinSource.close()
    pinTarget.close()
    println("closed")
}

private fun basicGpioLoop(pins: Array<Int>) = runBlocking {
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

val started = getTimeMicros()

fun println(msg: String) {
    val time = (getTimeMicros() - started) / 1000000.0
    kotlin.io.println("$time: $msg")
}
