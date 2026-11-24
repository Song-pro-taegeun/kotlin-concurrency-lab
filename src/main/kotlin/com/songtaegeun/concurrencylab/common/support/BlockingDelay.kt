package com.songtaegeun.concurrencylab.common.support

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BlockingDelay(
    @Value("\${lab.blocking-delay:500ms}")
    private val duration: Duration,
) {
    fun simulateIo() {
        Thread.sleep(duration.toMillis())
    }
}
