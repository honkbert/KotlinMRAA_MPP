package com.robgulley.hwint

import kotlinx.coroutines.CoroutineScope

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int): I2cDevice {
        return I2cDeviceImpl(bus, address)
    }

    actual fun openGpio(pin: Int): GpioPin {
        return GpioPin(pin)
    }

    actual fun openUartDevice(uartName: String, coroutineScope: CoroutineScope?): UartDevice {
        return UartDevice(uartName, coroutineScope)
    }
}