package com.robgulley.hwint

import kotlinx.cinterop.toKString
import mraa.MRAA_SUCCESS
import mraa.mraa_result_t
import platform.posix.posix_errno
import platform.posix.strerror

inline fun <T> T.ensurePosixCallResult(op: String, predicate: (T) -> Boolean): T {
    if (!predicate(this)) {
        throw Error("${posix_errno()} for $op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

fun mraa_result_t.ensureSuccess(debugString: String) = this.ensurePosixCallResult(debugString) { it == MRAA_SUCCESS }