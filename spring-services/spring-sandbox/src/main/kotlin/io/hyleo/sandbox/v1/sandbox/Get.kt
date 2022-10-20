package io.hyleo.sandbox.v1.sandbox

import io.hyleo.sandbox.sandbox.Sandbox
import io.hyleo.sandbox.sandbox.SandboxRepository
import io.hyleo.sandbox.sandbox.SandboxService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController("/v1/sandbox")
class Get {

    @Autowired
    lateinit var service: SandboxService

    @GetMapping
    @ResponseBody
    fun byId(@RequestParam(name = "user") id: UUID): Sandbox? = service.getById(id).orElse(null)

    @GetMapping
    @ResponseBody
    fun ownedSandboxes(@RequestParam(name = "user") id: UUID) = service.getAllByOwner(id)

}