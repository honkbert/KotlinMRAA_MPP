package com.robgulley.hwint.test


import com.robgulley.hwint.ensurePosixCallResult
import kotlinx.cinterop.convert
import kotlinx.cinterop.staticCFunction
import mraa.*
import platform.posix.*
import platform.posix.uint8_t
import kotlin.native.concurrent.AtomicInt


const val I2C_BUS = 0

/* register definitions */
const val MPU6050_ADDR: uint8_t = 0x68u
const val MPU6050_REG_PWR_MGMT_1: uint8_t = 0x6bu
const val MPU6050_REG_RAW_ACCEL_X: uint8_t = 0x3bu
const val MPU6050_REG_RAW_ACCEL_Y: uint8_t = 0x3du
const val MPU6050_REG_RAW_ACCEL_Z: uint8_t = 0x3fu
const val MPU6050_REG_RAW_GYRO_X: uint8_t = 0x43u
const val MPU6050_REG_RAW_GYRO_Y: uint8_t = 0x45u
const val MPU6050_REG_RAW_GYRO_Z: uint8_t = 0x47u

/* bit definitions */
const val MPU6050_RESET: uint8_t = 0x80u
const val MPU6050_SLEEP = (1 shl 6)
const val MPU6050_PLL_GYRO_X = (1 shl 1)

/* accelerometer scale factor for (+/-)2g */
const val MPU6050_ACCEL_SCALE = 16384.0

/* gyroscope scale factor for (+/-)250/s */
const val MPU6050_GYRO_SCALE = 131.0

val flag: AtomicInt = AtomicInt(1.convert())

private fun i2cReadWord(dev: mraa_i2c_context, command: uint8_t): int16_t {
    return mraa_i2c_read_word_data(dev, command).convert()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun straightCtest(): Int {
    println("starting main")
    var status: mraa_result_t = MRAA_SUCCESS
    val accel_data = arrayOf<int16_t>(0, 0, 0)
    val gyro_data = arrayOf<int16_t>(0, 0, 0)
    var ret: Int = 0

    /* install signal handler */
    val funPtr = staticCFunction<Int, Unit> { signum ->
        if (signum == SIGINT) {
            fprintf(stdout, "Exiting...\n")
            flag.value = 0
        }
    }
    signal(SIGINT, funPtr)

    /* initialize mraa for the platform (not needed most of the times) */
    mraa_init()

    //! [Interesting]
    /* initialize I2C bus */
    println("initializing bus $I2C_BUS")
    val init = mraa_i2c_init(I2C_BUS)
    if (init == NULL) {
        println("Failed to initialize I2C")
        mraa_deinit()
        return EXIT_FAILURE
    }
    val i2c = init!!

    /* set slave address */
    println("set slave address")
    status = mraa_i2c_address(i2c, MPU6050_ADDR)
    println("$status == $MRAA_SUCCESS")
    if (status != MRAA_SUCCESS) {
        return err_exit(status, i2c)
    }

    /* reset the sensor */
    println("reset sensor")
    status = mraa_i2c_write_byte_data(i2c, MPU6050_RESET, MPU6050_REG_PWR_MGMT_1)
    println("$status == $MRAA_SUCCESS")
    if (status != MRAA_SUCCESS) {
        return err_exit(status, i2c)
    }

    println("start sleep")
    sleep(2)

    /* configure power management register */
    println("config power mgmt read")
    ret = mraa_i2c_read_byte_data(i2c, MPU6050_REG_PWR_MGMT_1).ensurePosixCallResult("i2c pwr mgmt read") { it != -1 }

    var data: uint8_t = ret.convert()
    data = data or MPU6050_PLL_GYRO_X.convert()
    data = data and MPU6050_SLEEP.inv().convert()

    println("set power mgmnt")
    status = mraa_i2c_write_byte_data(i2c, data, MPU6050_REG_PWR_MGMT_1)
    println("$status == $MRAA_SUCCESS")
    if (status != MRAA_SUCCESS) {
        return err_exit(status, i2c)
    }

    println("start sleep")
    sleep(1)

    println("start read loop")
    while (flag.value != 0) {
        /* read raw accel data */
        accel_data[0] = (i2cReadWord(i2c, MPU6050_REG_RAW_ACCEL_X) / MPU6050_ACCEL_SCALE).toInt().toShort()
        accel_data[1] = (i2cReadWord(i2c, MPU6050_REG_RAW_ACCEL_Y) / MPU6050_ACCEL_SCALE).toInt().toShort()
        accel_data[2] = (i2cReadWord(i2c, MPU6050_REG_RAW_ACCEL_Z) / MPU6050_ACCEL_SCALE).toInt().toShort()

        /* read raw gyro data */
        gyro_data[0] = (i2cReadWord(i2c, MPU6050_REG_RAW_GYRO_X) / MPU6050_GYRO_SCALE).toInt().toShort()
        gyro_data[1] = (i2cReadWord(i2c, MPU6050_REG_RAW_GYRO_Y) / MPU6050_GYRO_SCALE).toInt().toShort()
        gyro_data[2] = (i2cReadWord(i2c, MPU6050_REG_RAW_GYRO_Z) / MPU6050_GYRO_SCALE).toInt().toShort()

        fprintf(stdout, "accel: x:%d y:%d z:%d\n", accel_data[0], accel_data[1], accel_data[2])
        fprintf(stdout, "gyro: x:%d y:%d z:%d\n\n", gyro_data[0], gyro_data[1], gyro_data[2])

        sleep(1)
    }

    /* stop i2c */
    mraa_i2c_stop(i2c)

    //! [Interesting]
    /* deinitialize mraa for the platform (not needed most of the times) */
    mraa_deinit()

    return EXIT_SUCCESS


}

fun err_exit(status: mraa_result_t, i2c: mraa_i2c_context): Int {
    mraa_result_print(status)

    /* stop i2c */
    mraa_i2c_stop(i2c)

    /* deinitialize mraa for the platform (not needed most of the times) */
    mraa_deinit()

    return EXIT_FAILURE
}