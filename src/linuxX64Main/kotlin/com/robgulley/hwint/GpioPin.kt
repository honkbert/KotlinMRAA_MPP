package com.robgulley.hwint

import kotlinx.cinterop.*
import mraa.*
import kotlin.native.concurrent.freeze

@OptIn(ExperimentalUnsignedTypes::class)
actual class GpioPin actual constructor(pinNum: Int) {
    private val pin = pinNum

    private val _gpioContext: mraa_gpio_contextVar = nativeHeap.alloc()
    private val gpioContext: mraa_gpio_context
        get() = _gpioContext.value!!

    init {
        _gpioContext.value = mraa_gpio_init(pin).ensurePosixCallResult("init gpio $pin") { it != null }!!
    }

    actual var value: Boolean
        get() {
            if (direction != Direction.IN) throw Throwable("can't read input on output gpio pin $pin")
            return mraa_gpio_read(gpioContext).ensurePosixCallResult("read $pin") { it != -1 }
                .toBoolean()
        }
        set(value) {
            if (direction == Direction.IN) throw Throwable("can't write output on input gpio pin $pin")
            mraa_gpio_write(
                gpioContext,
                value.toInt()
            ).ensureSuccess("write $value to $pin")
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
            ).ensureSuccess("set ${value.name} pin: $pin")
        }

    actual var inputMode: InputMode = InputMode.ACTIVE_HIGH
        set(value) {
            field = value
            mraa_gpio_input_mode(
                gpioContext,
                value.value
            ).ensureSuccess("set ${value.name} pin: $pin")
        }

    actual var edgeTriggerType: EdgeTriggerType = EdgeTriggerType.NONE
        set(value) {
            field = value
            mraa_gpio_edge_mode(
                gpioContext,
                value.value
            ).ensureSuccess("set ${value.name} pin: $pin")
        }

    actual var outputMode: OutputMode = OutputMode.STRONG
        set(value) {
            field = value
            mraa_gpio_mode(
                gpioContext,
                value.value
            ).ensureSuccess("set ${value.name} pin: $pin")
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
        unregisterGpioCallback()
        mraa_gpio_close(gpioContext)
        nativeHeap.free(_gpioContext)
    }

    actual fun registerGpioCallback(callback: GpioCallback) {
        val argVoidPtr = StableRef.create(callback.freeze()).asCPointer()
        val funPtr = staticCFunction<COpaquePointer?, Unit> { args ->
            val stableRef = args?.asStableRef<GpioCallback>()
            val func = stableRef?.get()
            func?.invoke()
        }
        mraa_gpio_isr(
            gpioContext,
            edgeTriggerType.value,
            funPtr,
            argVoidPtr
        ).ensureSuccess("set interrupt")
    }

    actual fun unregisterGpioCallback() {
        mraa_gpio_isr_exit(gpioContext)
        //TODO figure out how to dispose callback
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