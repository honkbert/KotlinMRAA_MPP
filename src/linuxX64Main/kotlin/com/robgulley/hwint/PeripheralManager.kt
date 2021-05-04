package com.robgulley.hwint

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int): I2cDevice {
        return I2cDeviceImpl(bus, address)
    }

    actual fun openGpio(pin: Int): GpioPin {
        return GpioPin(pin)
    }

    actual fun openUartDevice(uartName: String):UartDevice {
        return UartDevice(uartName)
    }
}