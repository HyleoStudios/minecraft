package io.hyleo.sandbox.v1.sandbox

import io.hyleo.sandbox.SandboxApplication
import io.hyleo.sandbox.sandbox.Sandbox
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("/v1/sandbox")
class Put {

    @PutMapping
    @ResponseBody
    fun put(
        @RequestParam(name = "owner") owner: UUID ,
        @RequestParam(name = "mode") mode: String,
        @RequestParam(name = "name") name: String
    ): Sandbox {


        TODO("Not implemented")
    }
}