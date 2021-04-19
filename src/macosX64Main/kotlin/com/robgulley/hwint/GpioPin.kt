package com.robgulley.hwint

actual class GpioPin actual constructor(pinNum: Int) {

    actual var value: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun on() {
    }

    actual fun off() {
    }

    actual var direction: Direction
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var inputMode: InputMode
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var edgeTriggerType: EdgeTriggerType
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var outputMode: OutputMode
        get() = TODO("Not yet implemented")
        set(value) {}

    actual companion object {
        actual enum class Direction {
            OUT, IN, OUT_START_LOW, OUT_START_HIGH,
        }

        actual enum class InputMode {
            ACTIVE_HIGH, ACTIVE_LOW
        }

        actual enum class EdgeTriggerType {
            NONE, BOTH, RISING, FALLING
        }

        actual enum class OutputMode {
            STRONG, PULLUP, PULLDOWN, HIZ,
        }

    }

    actual fun close() {
    }

    actual fun registerGpioCallback(callback: GpioCallback) {

    }

    actual fun unregisterGpioCallback() {
    }


}