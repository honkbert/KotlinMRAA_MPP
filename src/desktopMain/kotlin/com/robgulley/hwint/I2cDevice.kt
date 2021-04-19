package com.robgulley.hwint

interface I2cDevice {
    fun readRegByte(register: Int): Byte
    fun readRegWord(register: Int): Int

    fun writeRegByte(register: Int, data: Byte)
    fun writeRegWord(register: Int, data: Short)

    fun read(dataArray: ByteArray, address: Int)
    fun write(data: ByteArray, address: Int)

    fun readRegBuffer(register: Int, dataArray: ByteArray, length: Int)
    fun writeRegBuffer(register: Int, dataArray: ByteArray, length: Int)

    fun close()
}