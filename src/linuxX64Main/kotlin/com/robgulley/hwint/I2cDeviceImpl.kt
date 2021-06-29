@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.robgulley.hwint

import com.robgulley.time.Sleep
import kotlinx.cinterop.*
import mraa.*
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_unlock
import kotlin.time.ExperimentalTime

class I2cDeviceImpl(busName: String, private val address: Int, private val sync: Boolean) : I2cDevice {

    private val bus: Int = busName.uppercase().replace("I2C", "").toInt()

    private val _i2cContext: mraa_i2c_contextVar = nativeHeap.alloc()
    private val i2cBusContext: mraa_i2c_context?
        get() = _i2cContext.value


    init {
        synchro {
            _i2cContext.value = mraa_i2c_init(bus).ensurePosixCallResult("init i2c $bus") { it != null }!!
            Sleep.blockFor(100)
            mraa_i2c_address(i2cBusContext, address.convert()).ensureSuccess("init i2c device ${address.toHexString()}")
            Sleep.blockFor(100)
        }
    }

    override fun readRegByte(register: Int): Byte {
        return synchro {
            mraa_i2c_read_byte_data(
                i2cBusContext,
                register.convert()
            ).ensurePosixCallResult("i2c read byte") { it != -1 }.toByte()
        }
    }

    override fun readRegWord(register: Int): Int {
        return synchro {
            mraa_i2c_read_word_data(
                i2cBusContext,
                register.convert()
            ).ensurePosixCallResult("i2c read word") { it != -1 }
        }
    }

    override fun readRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        dataArray.usePinned { bytes ->
            synchro {
                mraa_i2c_read_bytes_data(
                    i2cBusContext,
                    register.convert(),
                    bytes.addressOf(0).reinterpret(),
                    length
                ).ensurePosixCallResult("i2c read buffer") { it != -1 }
            }
        }
    }

    override fun writeRegByte(register: Int, data: Byte) {
        synchro {
            mraa_i2c_write_byte_data(
                i2cBusContext,
                data.convert(),
                register.convert()
            ).ensureSuccess("i2c write reg ${register.toHexString()} byte ${data.toHexString()}")
        }
    }

    override fun writeRegWord(register: Int, data: Short) {
        synchro {
            mraa_i2c_write_word_data(
                i2cBusContext,
                data.convert(),
                register.convert()
            ).ensureSuccess("i2c write reg ${register.toHexString()} word ${data.toHexString()}")
        }
    }

    override fun writeRegBuffer(register: Int, dataArray: ByteArray, length: Int) {
        synchro {
            dataArray.usePinned { bytes ->
                mraa_i2c_write(
                    i2cBusContext,
                    bytes.addressOf(0).reinterpret(),
                    length
                ).ensureSuccess("i2c write buffer ${register.toHexString()} count: $length")
            }
        }
    }

    override fun close() {
        synchro {
            _i2cContext.value?.let {
                mraa_i2c_stop(i2cBusContext)
                _i2cContext.value = null
            }
        }
    }

    override fun bulk(action: I2cDevice.() -> Unit) {
        synchro { action(this) }
    }

    override fun isOpen(): Boolean = _i2cContext.value != null

    @OptIn(ExperimentalTime::class)
    private fun <T> synchro(action: () -> T): T {
        if (sync) pthread_mutex_lock(Locks.i2c.ptr).ensurePosixCallResult("lock") { it == 0 }
        val result = action()
        if (sync) pthread_mutex_unlock(Locks.i2c.ptr).ensurePosixCallResult("unlock") { it == 0 }
        return result
    }
}

