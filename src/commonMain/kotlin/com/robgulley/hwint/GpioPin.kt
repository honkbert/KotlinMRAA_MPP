package com.robgulley.hwint

import co.touchlab.stately.isolate.IsolateState
import kotlinx.coroutines.flow.MutableSharedFlow

object GpioPinTriggers {
    val gpioTriggers = IsolateState { MutableSharedFlow<Long>() }
}
