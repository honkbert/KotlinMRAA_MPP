import kotlinx.cinterop.*
import mraa.*

actual class GpioPin(private val pinName: String) : Closeable {
    constructor(pin: Int) : this(mraa_get_pin_name(pin)?.toKString().orEmpty())

    private val gpioContext = mraa_gpio_init_by_name(pinName.cstr)

    var value: Boolean
        get() {
            if (direction != Direction.IN) throw Exception("can't read input on output gpio pin $pinName")
            return mraa_gpio_read(gpioContext).toBoolean()
        }
        set(value) {
            if (direction == Direction.IN) throw Exception("can't write output on input gpio pin $pinName")
            mraa_gpio_write(gpioContext, value.toInt())
        }

    fun on() { value = true }
    fun off() { value = false }

    var direction: Direction = Direction.IN
    set(value) {
        field = value
        mraa_gpio_dir(gpioContext, value.value)
    }

    var inputMode: InputMode = InputMode.ACTIVE_HIGH
    set(value) {
        field = value
        mraa_gpio_input_mode(gpioContext, value.value)
    }

    var edgeTriggerType: EdgeTriggerType = EdgeTriggerType.NONE
        set(value) {
        mraa_gpio_edge_mode(gpioContext, value.value)
        field = value
    }

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

    enum class EdgeTriggerType (val value: UInt){
        NONE(MRAA_GPIO_EDGE_NONE),
        BOTH(MRAA_GPIO_EDGE_BOTH),
        RISING(MRAA_GPIO_EDGE_RISING),
        FALLING(MRAA_GPIO_EDGE_FALLING)
    }

    override fun close() {
        callback = null
    }

    private  var callback: (() -> Unit)? = null

    fun registerGpioCallback(callback: () -> Unit) {
        this.callback = callback
        val fptr = staticCFunction{ foo: CPointer<out CPointed>? ->  }
        mraa_gpio_isr(gpioContext, edgeTriggerType.value, fptr, null)
    }

    fun unregisterGpioCallback() {
        mraa_gpio_isr_exit(gpioContext)
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