@file:Suppress("unused", "EXPERIMENTAL_IS_NOT_ENABLED")

package com.robgulley.hwint

import co.touchlab.stately.concurrency.AtomicBoolean
import com.robgulley.time.Time
import kotlinx.cinterop.*
import mraa.*

@OptIn(ExperimentalUnsignedTypes::class)
actual class GpioPin actual constructor(pinNum: Int) {
    private val pin = pinNum
    private val pinValuePointer = StableRef.create(pin).asCPointer()

    private var listenForTriggers: AtomicBoolean = AtomicBoolean(false)

    private val _gpioContext: mraa_gpio_contextVar = nativeHeap.alloc()
    private val gpioContext: mraa_gpio_context
        get() = _gpioContext.value!!

    init {
        _gpioContext.value = mraa_gpio_init(pin).ensurePosixCallResult("init gpio $pin") { it != null }!!
    }

    actual var value: Boolean
        get() {
            if (direction != Direction.IN) throw MraaException("can't read input on output gpio pin $pin")
            return mraa_gpio_read(gpioContext).ensurePosixCallResult("gpio read $pin") { it != -1 }
                .toBoolean()
        }
        set(value) {
            if (direction == Direction.IN) throw MraaException("can't write output on input gpio pin $pin")
            mraa_gpio_write(
                gpioContext,
                value.toInt()
            ).ensureSuccess("gpio write $value to $pin")
        }

    actual suspend fun on() {
        value = true
    }

    actual suspend fun off() {
        value = false
    }

    actual var direction: Direction = Direction.IN
        set(value) {
            field = value
            mraa_gpio_dir(
                gpioContext,
                value.value
            ).ensureSuccess("gpio set ${value.name} pin: $pin")
        }

    actual var inputMode: InputMode = InputMode.ACTIVE_HIGH
        set(value) {
            field = value
            mraa_gpio_input_mode(
                gpioContext,
                value.value
            ).ensureSuccess("gpio set ${value.name} pin: $pin")
        }

    actual var edgeTriggerType: EdgeTriggerType = EdgeTriggerType.NONE
        set(value) {
            field = value
            mraa_gpio_edge_mode(
                gpioContext,
                value.value
            ).ensureSuccess("gpio set ${value.name} pin: $pin")
        }

    actual var outputMode: OutputMode = OutputMode.STRONG
        set(value) {
            field = value
            mraa_gpio_mode(
                gpioContext,
                value.value
            ).ensureSuccess("gpio set ${value.name} pin: $pin")
        }

    actual companion object {
        actual enum class Direction(val value: UInt) {
            OUT(MRAA_GPIO_OUT),
            IN(MRAA_GPIO_IN),
            OUT_START_LOW(MRAA_GPIO_OUT_LOW),
            OUT_START_HIGH(MRAA_GPIO_OUT_HIGH),
        }

        actual enum class InputMode(val value: UInt) {
            ACTIVE_HIGH(MRAA_GPIO_ACTIVE_HIGH),
            ACTIVE_LOW(MRAA_GPIO_ACTIVE_LOW)
        }

        actual enum class EdgeTriggerType(val value: UInt) {
            NONE(MRAA_GPIO_EDGE_NONE),
            BOTH(MRAA_GPIO_EDGE_BOTH),
            RISING(MRAA_GPIO_EDGE_RISING),
            FALLING(MRAA_GPIO_EDGE_FALLING)
        }

        actual enum class OutputMode(val value: UInt) {
            STRONG(MRAA_GPIO_STRONG),
            PULLUP(MRAA_GPIO_PULLUP),
            PULLDOWN(MRAA_GPIO_PULLDOWN),
            HIZ(MRAA_GPIO_HIZ),
        }
    }

    actual fun close() {
        mraa_gpio_close(gpioContext)
        nativeHeap.free(_gpioContext)
    }

    actual fun boardNanoTime() = Time.now().nanoTime

    actual fun listenForTriggers() {
        listenForTriggers.value = true
        val funPtr = staticCFunction<COpaquePointer?, Unit> { args ->
            initRuntimeIfNeeded()
            val stableRef = args?.asStableRef<Int>()
            val pinNum = stableRef?.get() ?: -1
            val timestamp = Time.now().nanoTime
            GpioPinTriggers.gpioTriggers.access { map -> map.getOrPut(pinNum) { mutableListOf() }.add(timestamp) }
        }
        mraa_gpio_isr(
            gpioContext,
            edgeTriggerType.value,
            funPtr,
            pinValuePointer
        ).ensureSuccess("gpio set interrupt")
    }

    actual fun stopListenForTriggers() {
        if (listenForTriggers.value) {
            mraa_gpio_isr_exit(gpioContext)
            pinValuePointer.asStableRef<Int>().dispose()
            listenForTriggers.value = false
        }
    }

    private fun Boolean.toInt() = when (this) {
        true -> 1
        false -> 0
    }

    private fun Int.toBoolean() = when (this) {
        1 -> true
        else -> false
    }
}