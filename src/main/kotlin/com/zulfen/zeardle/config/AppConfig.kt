package com.zulfen.zeardle.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@EnableConfigurationProperties(ZeardleConfig::class)
@PropertySource("classpath:zeardle.yml")
class AppConfig {
    @Bean
    fun zeardleConfig(): ZeardleConfig {
        return ZeardleConfig()
    }
}