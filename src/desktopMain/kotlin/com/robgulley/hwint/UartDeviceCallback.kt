package com.robgulley.hwint

@OptIn(ExperimentalUnsignedTypes::class)
interface UartDeviceCallback {
    fun onUartDeviceDataAvailable(uart: UartDevice): Boolean
    fun onUartDeviceError(uart: UartDevice, error: Int)
}