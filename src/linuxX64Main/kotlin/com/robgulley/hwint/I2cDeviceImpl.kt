package com.robgulley.hwint

import com.robgulley.Sleep
import kotlinx.cinterop.*
import mraa.*

class I2cDeviceImpl(busName: String, address: Int) : I2cDevice {

    private val bus: Int = busName.toUpperCase().replace("I2C", "").toInt()

    private val _i2cContext: mraa_i2c_contextVar = nativeHeap.alloc()
    private val i2cBusContext: mraa_i2c_context
        get() = _i2cContext.value!!

    init {
        mraa_init()
        _i2cContext.value = mraa_i2c_init(bus).ensurePosixCallResult("init i2c $bus") { it != null }!!
        Sleep.blockFor(100)
        mraa_i2c_address(i2cBusContext, address.convert()).ensureSuccess("init i2c device ${address.toHexString()}")
    }

    override fun readRegByte(register: Int): Byte {
        return mraa_i2c_read_byte_data(
            i2cBusContext,
            register.convert()
        ).ensurePosixCallResult("i2c read byte") { it != -1 }.toByte()
    }

    override fun readRegWord(register: Int): Int {
        return mraa_i2c_read_word_data(
            i2cBusContext,
            register.convert()
        ).ensurePosixCallResult("i2c read word") { it != -1 }
    }

    override fun readRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        dataArray.usePinned { bytes ->
            mraa_i2c_read_bytes_data(
                i2cBusContext,
                register.convert(),
                bytes.addressOf(0).reinterpret(),
                length
            ).ensurePosixCallResult("i2c read buffer") { it != -1 }
        }
    }

    override fun writeRegByte(register: Int, data: Byte) {
        mraa_i2c_write_byte_data(
            i2cBusContext,
            data.convert(),
            register.convert()
        ).ensureSuccess("i2c write reg ${register.toHexString()} byte ${data.toHexString()}")
    }

    override fun writeRegWord(register: Int, data: Short) {
        mraa_i2c_write_word_data(
            i2cBusContext,
            data.convert(),
            register.convert()
        ).ensureSuccess("i2c write reg ${register.toHexString()} word ${data.toHexString()}")
    }

    override fun writeRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        dataArray.usePinned { bytes ->
              mraa_i2c_write(
                i2cBusContext,
                bytes.addressOf(0).reinterpret(),
                length
            ).ensureSuccess("i2c write buffer ${register.toHexString()} count: $length")
        }
    }

    override fun close() {
        mraa_i2c_stop(i2cBusContext)
        nativeHeap.free(_i2cContext)
    }
}