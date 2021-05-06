package com.robgulley.hwint

import kotlinx.coroutines.CoroutineScope

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int): I2cDevice {
        TODO("Not yet implemented")
    }

    actual fun openGpio(pin: Int): GpioPin {
        TODO("Not yet implemented")
    }

    actual fun openUartDevice(
        uartName: String,
        coroutineScope: CoroutineScope?
    ): UartDevice {
        TODO("Not yet implemented")
    }
}