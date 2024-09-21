package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Role
import com.zulfen.zeardle.entities.ZeardleUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface RoleRepository : ReactiveCrudRepository<Role, Long> {
    fun findByName(name: String): Mono<Role>
    fun findUsersWithRole(role: Role): List<ZeardleUser>
}