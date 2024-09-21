package com.zulfen.zeardle.services

import com.zulfen.zeardle.entities.Role
import com.zulfen.zeardle.entities.ZeardleUser
import com.zulfen.zeardle.repositories.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RoleService @Autowired constructor(
    val roleRepository: RoleRepository
) {

    fun getAllRoles(): List<Role> {
        return roleRepository.findAll()
    }

    fun findUsersWithRole(role: Role): List<ZeardleUser> {
        return roleRepository.findUsersWithRole(role)
    }



}