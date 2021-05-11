package com.robgulley.hwint

object GpioPinTriggers {
    val gpioTriggers = co.touchlab.stately.isolate.IsolateState { mutableMapOf<Int, MutableList<Long>>() }
}
