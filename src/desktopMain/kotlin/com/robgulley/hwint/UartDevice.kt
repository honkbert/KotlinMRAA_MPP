package com.robgulley.hwint

@ExperimentalUnsignedTypes
expect class UartDevice(uartName: String) {

    companion object {
        /// UART Parity
        enum class UartParity {
            UART_PARITY_NONE,

            /**< No parity */
            UART_PARITY_EVEN,

            /**< Even parity */
            UART_PARITY_ODD,

            /**< Odd parity */
            UART_PARITY_MARK,

            /**< Mark parity, always 1 */
            UART_PARITY_SPACE,
            /**< Space parity, always 0 */
        }

        /// Modem control lines.
        enum class UartModemControlLine {
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
            /**< Data set ready */
        }

        // Hardware Flow Control
        enum class UartHardwareFlowControl {
            UART_HARDWARE_FLOW_CONTROL_NONE,

            /**< No hardware flow control */
            UART_HARDWARE_FLOW_CONTROL_AUTO_RTSCTS,
            /**< Auto RTS/CTS */
        }

        /// Flush queue selection
        enum class UartFlushDirection {
            UART_FLUSH_IN,

            /**< Flushes data received but not read */
            UART_FLUSH_OUT,

            /**< Flushes data written but not transmitted */
            UART_FLUSH_IN_OUT,
            /**< Flushes both in and out */
        }
    }

    /// Writes to a UART device.
/// @param data Data to write.
/// @param len Size of the data to write.
/// @param bytes_written Output pointer to the number of bytes written.
/// @return 0 on success, errno on error.
    fun write(data: ByteArray, len: Long): Int

    /// Reads from a UART device.
/// @param data Buffer to read the data into.
/// @param len Number of bytes to read.
/// @param bytes_read Output pointer to the number of bytes read.
/// @return 0 on success, errno on error.
    fun read(data: ByteArray, len: Long): Int

    /// Sets the input and output speed of a UART device.
    /// @param baudrate Speed in baud.
    var baudRate: Int

    /// Sets number of stop bits for the UART device.
    /// @param stop_bits Number of stop bits. Typically 1 or 2.
    var stopBits: Int

    /// Sets the data size of a character for the UART device.
    /// @param data_size Number of bits per character. Typically between 5 and 8.
    var dataSize: Int

    /// Sets the parity mode for the UART device.
    var parity: UartParity

    /// Sets the hardware flow control mode for the UART device.
    var hardwareFlowControl: UartHardwareFlowControl

    /// Sets the modem control bits for the UART device.
    /// @param lines Lines to set. UartModemControlLine values OR'ed together.
/// @return 0 on success, errno on error.
    fun setModemControl(lines: UInt): Int

    /// Clears the modem control bits for the UART device.
    /// @param lines Lines to clear. UartModemControlLine values OR'ed together.
/// @return 0 on success, errno on error.
    fun clearModemControl(lines: UInt): Int

    /// Sends a break to the UART device.
    /// @param duration Duration of break transmission in milliseconds. If 0,
/// transmits zero-valued bits for at least 0.25 seconds, and not more
/// than 0.5 seconds.
/// @return 0 on success, errno on error.
    fun sendBreak(durationMillis: Int)

    /// Flushes specified queue for the UART device.
    /// @param direction Direction to flush.
/// @return 0 on success, errno on error.
    fun flush(direction: UartFlushDirection)

    fun close()
}