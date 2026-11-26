package com.songtaegeun.concurrencylab.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class VirtualThreadConfig {
    @Bean(destroyMethod = "shutdown")
    fun orderSummaryVirtualThreadExecutor(): ExecutorService =
        Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual()
                .name("order-summary-vt-", 0)
                .factory(),
        )
}
