package com.robgulley.hwint

import kotlinx.coroutines.CoroutineScope

@ExperimentalUnsignedTypes
actual class UartDevice actual constructor(
    uartName: String,
    coroutineScope: CoroutineScope?
) {
    actual companion object {
        /// UART Parity
        actual enum class UartParity {
            /**< Space parity, always 0 */
            UART_PARITY_NONE,

            /**< No parity */
            UART_PARITY_EVEN,

            /**< Even parity */
            UART_PARITY_ODD,

            /**< Odd parity */
            UART_PARITY_MARK,

            /**< Mark parity, always 1 */
            UART_PARITY_SPACE,
        }

        /// Modem control lines.
        actual enum class UartModemControlLine {
            /**< Data set ready */
            UART_MODEM_CONTROL_LE,

            /**< Data set ready/Line enable */
            UART_MODEM_CONTROL_DTR,

            /**< Data terminal ready */
            UART_MODEM_CONTROL_RTS,

            /**< Request to send */
            UART_MODEM_CONTROL_ST,

            /**< Secondary TXD */
            UART_MODEM_CONTROL_SR,

            /**< Secondary RXD */
            UART_MODEM_CONTROL_CTS,

            /**< Clear to send */
            UART_MODEM_CONTROL_CD,

            /**< Data carrier detect */
            UART_MODEM_CONTROL_RI,

            /**< Ring */
            UART_MODEM_CONTROL_DSR,
        }

        // Hardware Flow Control
        actual enum class UartHardwareFlowControl {
            /**< Auto RTS/CTS */
            UART_HARDWARE_FLOW_CONTROL_NONE,

            /**< No hardware flow control */
            UART_HARDWARE_FLOW_CONTROL_AUTO_RTSCTS,
        }

        /// Flush queue selection
        actual enum class UartFlushDirection {
            /**< Flushes both in and out */
            UART_FLUSH_IN,

            /**< Flushes data received but not read */
            UART_FLUSH_OUT,

            /**< Flushes data written but not transmitted */
            UART_FLUSH_IN_OUT,
        }

    }

    actual fun write(data: ByteArray, len: Long): Int {
        TODO("Not yet implemented")
    }

    actual fun read(data: ByteArray, len: Long): Int {
        TODO("Not yet implemented")
    }

    actual var baudRate: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var stopBits: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var dataSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var parity: UartParity
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var hardwareFlowControl: UartHardwareFlowControl
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun setModemControl(lines: UInt): Int {
        TODO("Not yet implemented")
    }

    actual fun clearModemControl(lines: UInt): Int {
        TODO("Not yet implemented")
    }

    actual fun sendBreak(durationMillis: Int) {
    }

    actual fun flush(direction: UartFlushDirection) {
    }

    actual fun registerUartDeviceCallback(uartDeviceCallback: UartDeviceCallback) {
    }

    actual fun unregisterUartDeviceCallback(uartDeviceCallback: UartDeviceCallback) {
    }

    actual fun close() {
    }

}