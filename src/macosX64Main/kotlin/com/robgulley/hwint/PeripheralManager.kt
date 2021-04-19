package com.robgulley.hwint

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int): I2cDevice {
        TODO("Not yet implemented")
    }

    actual fun openGpio(pin: Int): GpioPin {
        TODO("Not yet implemented")
    }
}