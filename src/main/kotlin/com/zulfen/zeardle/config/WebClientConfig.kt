package com.zulfen.zeardle.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun musicBrainzClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://musicbrainz.org/ws/2/")
            .build()
    }

    @Bean
    fun coverArtArchiveClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://coverartarchive.org/")
            .build()
    }

    @Bean
    fun soundCloudClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api-widget.soundcloud.com/")
            .build()
    }

}
