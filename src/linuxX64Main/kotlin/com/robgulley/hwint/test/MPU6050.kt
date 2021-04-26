package com.robgulley.hwint.test


import com.robgulley.Sleep
import com.robgulley.hwint.PeripheralManager
import com.robgulley.vector.Vector
import kotlinx.cinterop.convert
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MPU6050(peripheralManager: PeripheralManager, coroutineScope: CoroutineScope, bus: Int = 0) {
    /* register definitions */
    private val MPU6050_ADDR = 0x68
    private val MPU6050_REG_PWR_MGMT_1 = 0x6b
    private val MPU6050_REG_RAW_ACCEL_X = 0x3b
    private val MPU6050_REG_RAW_ACCEL_Y = 0x3d
    private val MPU6050_REG_RAW_ACCEL_Z = 0x3f
    private val MPU6050_REG_RAW_GYRO_X = 0x43
    private val MPU6050_REG_RAW_GYRO_Y = 0x45
    private val MPU6050_REG_RAW_GYRO_Z = 0x47

    /* bit definitions */
    private val MPU6050_RESET = 0x80u
    private val MPU6050_SLEEP: UByte = ((1 shl 6).toUByte())
    private val MPU6050_PLL_GYRO_X: UByte = ((1 shl 1).toUByte())

    /* accelerometer scale factor for (+/-)2g */
    private val MPU6050_ACCEL_SCALE = 16384.0

    /* gyroscope scale factor for (+/-)250/s */
    private val MPU6050_GYRO_SCALE = 131.0

    private val _accelData = MutableStateFlow(Vector.ZeroVector)
    val accelData: Flow<Vector> get() = _accelData

    private val _gyroData = MutableStateFlow(Vector.ZeroVector)
    val gyroData: Flow<Vector> get() = _gyroData

    private val device = peripheralManager.openI2cDevice(bus.toString(), MPU6050_ADDR)

    init {
        device.writeRegByte(MPU6050_REG_PWR_MGMT_1, MPU6050_RESET.toByte())
        Sleep.blockFor(100)
        val pwrMgmt = device.readRegByte(MPU6050_REG_PWR_MGMT_1).toUByte()
        Sleep.blockFor(100)
        var data = pwrMgmt
        data = data or MPU6050_PLL_GYRO_X
        data = data and MPU6050_SLEEP.inv()
        device.writeRegByte(MPU6050_REG_PWR_MGMT_1, data.convert())
        Sleep.blockFor(100)
    }

    fun close() {
        device.close()
    }

    private val readJob = coroutineScope.launch(start = CoroutineStart.LAZY) {
        while (isActive) {
            val accelVector = Vector(
                device.readRegWord(MPU6050_REG_RAW_ACCEL_X) / MPU6050_ACCEL_SCALE,
                device.readRegWord(MPU6050_REG_RAW_ACCEL_Y) / MPU6050_ACCEL_SCALE,
                device.readRegWord(MPU6050_REG_RAW_ACCEL_Z) / MPU6050_ACCEL_SCALE
            )
            delay(10)

            val gyroVector = Vector(
                device.readRegWord(MPU6050_REG_RAW_GYRO_X) / MPU6050_GYRO_SCALE,
                device.readRegWord(MPU6050_REG_RAW_GYRO_Y) / MPU6050_GYRO_SCALE,
                device.readRegWord(MPU6050_REG_RAW_GYRO_Z) / MPU6050_GYRO_SCALE
            )

            delay(510)
            _accelData.emit(accelVector)
            _gyroData.emit(gyroVector)
        }
    }

    fun startRead() {
        readJob.start()
    }

    fun stopRead() {
        readJob.cancel()
    }
}