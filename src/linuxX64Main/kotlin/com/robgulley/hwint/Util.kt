package com.robgulley.hwint

import kotlinx.cinterop.toKString
import platform.posix.posix_errno
import platform.posix.strerror

inline fun Int.ensureUnixCallResult(op: String, predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throw Error("${posix_errno()} for $op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

inline fun Long.ensureUnixCallResult(op: String, predicate: (Long) -> Boolean): Long {
    if (!predicate(this)) {
        throw Error("${posix_errno()} for $op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

inline fun <T> T.ensureUnixCallResult(op: String, predicate: (T) -> Boolean): T {
    if (!predicate(this)) {
        throw Error("${posix_errno()} for $op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}