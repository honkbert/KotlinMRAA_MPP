package com.robgulley.hwint

import kotlinx.cinterop.toKString
import mraa.MRAA_SUCCESS
import mraa.mraa_result_t
import platform.posix.posix_errno
import platform.posix.strerror

internal inline fun <T> T.ensurePosixCallResult(op: String, predicate: (T) -> Boolean): T {
    if (!predicate(this)) {
        val message = "${posix_errno()} for $op: ${strerror(posix_errno())!!.toKString()}"
        when {
            op.contains("i2c", ignoreCase = true) -> throwMraaI2CException(message)
            op.contains("gpio", ignoreCase = true) -> throwMraaGPIOException(message)
            op.contains("uart", ignoreCase = true) -> throwMraaUARTException(message)
            else -> throwMraaException(message)
        }
    }
    return this
}

internal fun mraa_result_t.ensureSuccess(debugString: String) =
    this.ensurePosixCallResult(debugString) { it == MRAA_SUCCESS }

class MraaException(override val message: String?) : RuntimeException()
class MraaI2CException(override val message: String?) : RuntimeException()
class MraaGPIOException(override val message: String?) : RuntimeException()
class MraaUARTException(override val message: String?) : RuntimeException()

internal fun throwMraaException(message: String): Nothing =
    throw MraaException("unknown device or operation: $message")

internal fun throwMraaI2CException(message: String): Nothing =
    throw MraaI2CException(message)

internal fun throwMraaGPIOException(message: String): Nothing =
    throw MraaGPIOException(message)

internal fun throwMraaUARTException(message: String): Nothing =
    throw MraaUARTException(message)