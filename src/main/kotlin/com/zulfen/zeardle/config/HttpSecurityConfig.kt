package com.zulfen.zeardle.config

import com.zulfen.zeardle.services.auth.DiscordOAuth2Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.session.HttpSessionEventPublisher

@Configuration
@EnableWebSecurity
class HttpSecurityConfig @Autowired constructor(
    val discordOAuth2Service: DiscordOAuth2Service
) : WebSecurityConfiguration() {

    @Bean
    fun sessionRegistry() = SessionRegistryImpl()

    @Bean
    fun httpSessionEventPublisher() = HttpSessionEventPublisher()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf {  }
            oauth2Login { }
            oauth2Client {  }
        }

        return http.build()
    }

}