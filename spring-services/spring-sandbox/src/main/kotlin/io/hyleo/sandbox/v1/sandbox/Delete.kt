package io.hyleo.sandbox.v1.sandbox

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController("/v1/sandbox")
class Delete {

    @DeleteMapping
    fun delete(@RequestParam(name = "id") id: UUID) {
        TODO("Not implemented")
    }
}