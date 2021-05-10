package com.robgulley.hwint


expect class GpioPin(pinNum: Int) {
    var value: Boolean

    suspend fun on()
    suspend fun off()

    var direction: Direction
    var inputMode: InputMode
    var edgeTriggerType: EdgeTriggerType
    var outputMode: OutputMode

    companion object {
        enum class Direction {
            OUT, IN, OUT_START_LOW, OUT_START_HIGH,
        }

        enum class InputMode {
            ACTIVE_HIGH, ACTIVE_LOW
        }

        enum class EdgeTriggerType {
            NONE, BOTH, RISING, FALLING
        }

        enum class OutputMode {
            STRONG, PULLUP, PULLDOWN, HIZ,
        }
    }

    fun close()
    fun registerGpioCallback(callback: GpioCallback)
    fun unregisterGpioCallback()
    fun listenForTriggers()
    fun stopListenForTriggers()
}