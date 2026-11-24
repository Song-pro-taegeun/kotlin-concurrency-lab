package com.songtaegeun.concurrencylab.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Kotlin Concurrency Lab API")
                .description("Kotlin Coroutines와 Java Virtual Threads 비교 학습 API")
                .version("v1"),
        )
}
