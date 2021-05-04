@file:Suppress("unused")

package com.robgulley.hwint

import kotlinx.cinterop.*
import mraa.*

@ExperimentalUnsignedTypes
actual class UartDevice actual constructor(uartName: String) {

    private val _uartDeviceContext: mraa_uart_contextVar = nativeHeap.alloc()
    private val uartDeviceContext: mraa_uart_context get() = _uartDeviceContext.value!!

    init {
        _uartDeviceContext.value = mraa_uart_init(uartName.toInt())
            .ensurePosixCallResult("uart init")
            { it != null }!!
    }

    actual fun write(data: ByteArray, len: Long): Int {
        return mraa_uart_write(
            uartDeviceContext,
            data.toKString(),
            len.convert()
        ).ensurePosixCallResult("uart write") { it != -1 }
    }

    actual fun read(data: ByteArray, len: Long): Int {
        return mraa_uart_read(
            uartDeviceContext,
            data.pin().addressOf(0),
            len.convert()
        ).ensurePosixCallResult("uart read") { op -> op != -1 }
    }

    actual var baudRate: Int = 0
        set(value) {
            mraa_uart_set_baudrate(uartDeviceContext, value.convert())
            field = value
        }
    actual var stopBits: Int = 0
        set(value) {
            mraa_uart_set_mode(uartDeviceContext, dataSize, parity.value, stopBits)
            field = value
        }
    actual var dataSize: Int = 0
        set(value) {
            mraa_uart_set_mode(uartDeviceContext, dataSize, parity.value, stopBits)
            field = value
        }
    actual var parity: UartParity = UartParity.UART_PARITY_NONE
        set(value) {
            mraa_uart_set_mode(uartDeviceContext, dataSize, value.value, stopBits)
            field = value
        }
    actual var hardwareFlowControl: UartHardwareFlowControl
        get() = TODO("Not yet implemented")
        set(value) {
            mraa_uart_set_flowcontrol(
                uartDeviceContext,
                0,
                if (value == UartHardwareFlowControl.UART_HARDWARE_FLOW_CONTROL_AUTO_RTSCTS) 1u else 0u
            )
        }

    actual fun setModemControl(lines: UInt): Int {
        TODO("Not yet implemented")
    }

    actual fun clearModemControl(lines: UInt): Int {
        TODO("Not yet implemented")
    }

    actual fun sendBreak(durationMillis: Int) {
        mraa_uart_sendbreak(uartDeviceContext, durationMillis).ensureSuccess("uart send break")
    }

    actual fun flush(direction: UartFlushDirection) {
        mraa_uart_flush(uartDeviceContext).ensureSuccess("uart flush")
    }

    actual fun registerUartDeviceCallback(uartDeviceCallback: UartDeviceCallback) {

    }

    actual fun unregisterUartDeviceCallback(uartDeviceCallback: UartDeviceCallback) {

    }

    actual fun close() {
        mraa_uart_stop(uartDeviceContext)
        nativeHeap.free(_uartDeviceContext)
    }

    actual companion object {
        /// UART Parity
        actual enum class UartParity(val value: UInt) {
            UART_PARITY_NONE(0u),
            /**< No parity */
            UART_PARITY_EVEN(1u),
            /**< Even parity */
            UART_PARITY_ODD(2u),
            /**< Odd parity */
            UART_PARITY_MARK(3u),
            /**< Mark parity, always 1 */
            UART_PARITY_SPACE(4u),
            /**< Space parity, always 0 */
        }

        /// Modem control lines.
        actual enum class UartModemControlLine(val value: UByte) {
            UART_MODEM_CONTROL_LE((1 shl 0).toUByte()),
            /**< Data set ready/Line enable */
            UART_MODEM_CONTROL_DTR((1 shl 1).toUByte()),
            /**< Data terminal ready */
            UART_MODEM_CONTROL_RTS((1 shl 2).toUByte()),
            /**< Request to send */
            UART_MODEM_CONTROL_ST((1 shl 3).toUByte()),
            /**< Secondary TXD */
            UART_MODEM_CONTROL_SR((1 shl 4).toUByte()),
            /**< Secondary RXD */
            UART_MODEM_CONTROL_CTS((1 shl 5).toUByte()),
            /**< Clear to send */
            UART_MODEM_CONTROL_CD((1 shl 6).toUByte()),
            /**< Data carrier detect */
            UART_MODEM_CONTROL_RI((1 shl 7).toUByte()),
            /**< Ring */
            UART_MODEM_CONTROL_DSR((1 shl 8).toUByte()),
            /**< Data set ready */
        }

        // Hardware Flow Control
        actual enum class UartHardwareFlowControl(val value: UInt) {
            UART_HARDWARE_FLOW_CONTROL_NONE(0u),
            /**< No hardware flow control */
            UART_HARDWARE_FLOW_CONTROL_AUTO_RTSCTS(1u),
            /**< Auto RTS/CTS */
        }

        /// Flush queue selection
        actual enum class UartFlushDirection(val value: UInt) {
            UART_FLUSH_IN(0u),
            /**< Flushes data received but not read */
            UART_FLUSH_OUT(1u),
            /**< Flushes data written but not transmitted */
            UART_FLUSH_IN_OUT(2u),
            /**< Flushes both in and out */
        }
    }

}