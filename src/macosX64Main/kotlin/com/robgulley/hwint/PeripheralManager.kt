package com.robgulley.hwint

actual class PeripheralManager actual constructor() {
    actual fun openI2cDevice(bus: String, address: Int): com.robgulley.hwint.I2cDevice {
        TODO("Not yet implemented")
    }

    actual fun openGpio(pin: Int): GpioPin {
        TODO("Not yet implemented")
    }

    actual fun openUartDevice(uartName: String): UartDevice {
        TODO("Not yet implemented")
    }
}