package com.robgulley.hwint

import kotlin.math.roundToInt

expect class PeripheralManager constructor() {
    fun openI2cDevice(bus: String, address: Int): I2cDevice
    fun openGpio(pin: Int): GpioPin
}

fun Int.toHexString(): String {
    val base = this.toString(16).toUpperCase()
    return if (base.length % 2 == 1) {
        "0x0$base"
    } else "0x$base"
}

fun Short.toHexString(): String = "0x" + this.toString(16).toUpperCase().padStart(4, '0')
fun UByte.toHexString(): String = "0x" + this.toString(16).toUpperCase().padStart(4, '0')
fun Byte.toHexString(): String = "0x" + this.toString(16).toUpperCase().padStart(2, '0')

internal fun Double.format(digits: Int): String {
    var dotAt = 1
    repeat(digits) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return ((roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt).toString()
}