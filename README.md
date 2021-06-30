# KotlinMRAA
This is a Kotlin Native wrapper for some (but not all) of the Linux `libmraa` C bindings: https://github.com/eclipse/mraa. Right now only GPIO, I2C, and UART are supported. 

## Concurrency
Because of the current undesirable threading/memory model of KN, callbacks to user code are not _really_ supported, so I added queue-bound map* as a shared global object that stores inbound GPIO triggers. It's less than ideal, but it works for now until JetBrains comes out with a proper multi-threaded model for KN.

## Platform
The only platform I've published for is `LinuxX64`. This could change in the future if there are requests from people running `libmraa` on other flavors of Linux.

*big thanks to https://github.com/touchlab/Stately
