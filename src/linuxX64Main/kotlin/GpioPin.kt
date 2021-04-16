import kotlinx.cinterop.*
import mraa.*

@OptIn(ExperimentalUnsignedTypes::class)
actual class GpioPin(private val pin: Int) : Closeable {

    private val _gpioContext: mraa_gpio_contextVar = nativeHeap.alloc()
    private val gpioContext: mraa_gpio_context
        get() = _gpioContext.value!!

    init {
        _gpioContext.value = mraa_gpio_init(pin).ensureUnixCallResult("init gpio $pin") { it != null }!!
    }

    var value: Boolean
        get() {
            if (direction != Direction.IN) throw Throwable("can't read input on output gpio pin $pin")
            return mraa_gpio_read(gpioContext).ensureUnixCallResult("read $pin") { it != -1 }
                .toBoolean()
        }
        set(value) {
            if (direction == Direction.IN) throw Throwable("can't write output on input gpio pin $pin")
            mraa_gpio_write(
                gpioContext,
                value.toInt()
            )
        }

    fun on() {
        value = true
    }

    fun off() {
        value = false
    }

    var direction: Direction = Direction.IN
        set(value) {
            field = value
            mraa_gpio_dir(
                gpioContext,
                value.value
            )
        }

    var inputMode: InputMode = InputMode.ACTIVE_HIGH
        set(value) {
            field = value
            mraa_gpio_input_mode(
                gpioContext,
                value.value
            ).ensureUnixCallResult("set ${value.name} pin: $pin") { it != MRAA_SUCCESS }
        }

    var edgeTriggerType: EdgeTriggerType = EdgeTriggerType.NONE
        set(value) {
            field = value
            mraa_gpio_edge_mode(
                gpioContext,
                value.value
            ).ensureUnixCallResult("set ${value.name} pin: $pin") { it != MRAA_SUCCESS }
        }

    var outputMode: OutputMode = OutputMode.STRONG
        set(value) {
            field = value
            mraa_gpio_mode(
                gpioContext,
                value.value
            ).ensureUnixCallResult("set ${value.name} pin: $pin") { it != MRAA_SUCCESS }
        }

    companion object {
        enum class Direction(val value: UInt) {
            OUT(MRAA_GPIO_OUT),
            IN(MRAA_GPIO_IN),
            OUT_START_LOW(MRAA_GPIO_OUT_LOW),
            OUT_START_HIGH(MRAA_GPIO_OUT_HIGH),
        }

        enum class InputMode(val value: UInt) {
            ACTIVE_HIGH(MRAA_GPIO_ACTIVE_HIGH),
            ACTIVE_LOW(MRAA_GPIO_ACTIVE_LOW)
        }

        enum class EdgeTriggerType(val value: UInt) {
            NONE(MRAA_GPIO_EDGE_NONE),
            BOTH(MRAA_GPIO_EDGE_BOTH),
            RISING(MRAA_GPIO_EDGE_RISING),
            FALLING(MRAA_GPIO_EDGE_FALLING)
        }

        enum class OutputMode(val value: UInt) {
            STRONG(MRAA_GPIO_STRONG),
            PULLUP(MRAA_GPIO_PULLUP),
            PULLDOWN(MRAA_GPIO_PULLDOWN),
            HIZ(MRAA_GPIO_HIZ),
        }
    }

    override fun close() {
        callback = null
        mraa_gpio_close(gpioContext)
        nativeHeap.free(_gpioContext)
    }

    private var callback: (() -> Unit)? = null

    fun registerGpioCallback(callback: () -> Unit) {
        this.callback = callback
        val fptr = staticCFunction { foo: CPointer<out CPointed>? -> }
        mraa_gpio_isr(
            gpioContext,
            edgeTriggerType.value,
            fptr,
            null
        ).ensureUnixCallResult("register $pin callback") { it != MRAA_SUCCESS }
    }

    fun unregisterGpioCallback() {
        mraa_gpio_isr_exit(gpioContext).ensureUnixCallResult("close $pin callback") { it != MRAA_SUCCESS }
        callback = null
    }

    private fun Boolean.toInt() = when (this) {
        true -> 1
        false -> 0
    }

    private fun Int.toBoolean() = when (this) {
        1 -> true
        else -> false
    }
}