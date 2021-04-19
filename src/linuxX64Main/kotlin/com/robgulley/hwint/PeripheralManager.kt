package com.robgulley.hwint

import kotlinx.cinterop.*
import mraa.*

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int): I2cDevice {
        return I2cDeviceImpl(bus, address)
    }

    actual fun openGpio(pin: Int): GpioPin {
        return GpioPin(pin)
    }

    private fun error(bus: String, device: Int, register: Int): Nothing =
        error("No such register ${register.toHexString()} on device ${device.toHexString()} $bus")
}

class I2cDeviceImpl(busName: String, private val address: Int) : I2cDevice {

    private val bus: Int = mraa_i2c_lookup(busName)

    private val i2cContext: mraa_i2c_context = mraa_i2c_init(bus).ensureUnixCallResult("init i2c $bus") { it != null} !!

    override fun readRegByte(register: Int): Byte {
        return mraa_i2c_read_byte_data(i2cContext, register.convert()).toByte()
    }

    override fun readRegWord(register: Int): Int {
        return mraa_i2c_read_word_data(i2cContext, register.convert())
    }

    override fun writeRegByte(register: Int, data: Byte) {
        TODO("Not yet implemented")
    }

    override fun writeRegWord(register: Int, data: Short) {
        TODO("Not yet implemented")
    }

    override fun read(dataArray: ByteArray, address: Int) {
        dataArray.usePinned { bytes ->
            mraa_i2c_read_bytes_data(
                i2cContext,
                address.convert(),
                bytes.addressOf(0).reinterpret(),
                dataArray.size
            )
        }
    }

    override fun write(data: ByteArray, address: Int) {
        TODO("Not yet implemented")
    }

    override fun readRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        dataArray.usePinned { bytes ->
            mraa_i2c_read_bytes_data(
                i2cContext,
                address.convert(),
                bytes.addressOf(0).reinterpret(),
                length
            )
        }
    }

    override fun writeRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        dataArray.usePinned { bytes ->
            mraa_i2c_write(
                i2cContext,
                bytes.addressOf(0).reinterpret(),
                length
            ).let { if (it.convert<Int>() != 0) throw Exception("write failed!") }
            //TODO com.robgulley.hwint.ensureUnixCallResult
        }
    }

    fun close() {
        mraa_i2c_stop(i2cContext)
    }

}