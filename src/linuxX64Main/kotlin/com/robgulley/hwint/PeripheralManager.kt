package com.robgulley.hwint

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.posix.*

actual class PeripheralManager {
    actual fun openI2cDevice(bus: String, address: Int, sync: Boolean): I2cDevice {
        return I2cDeviceImpl(bus, address, sync)
    }

    actual fun openGpio(pin: Int): GpioPin {
        return GpioPin(pin)
    }

    actual fun openUartDevice(uartName: String): UartDevice {
        return UartDevice(uartName)
    }
}

object Locks {
    val i2c = nativeHeap.alloc<pthread_mutex_t>()
    private val i2cAttr = nativeHeap.alloc<pthread_mutexattr_t>()

    init {
        pthread_mutexattr_init(i2cAttr.ptr).ensurePosixCallResult("mutex attr init") { it == 0 }
        pthread_mutexattr_settype(
            i2cAttr.ptr,
            PTHREAD_MUTEX_RECURSIVE.convert()
        ).ensurePosixCallResult("mutex attr set type") { it == 0 }
        pthread_mutex_init(i2c.ptr, i2cAttr.ptr).ensurePosixCallResult("mutex init") { it == 0 }
    }
}