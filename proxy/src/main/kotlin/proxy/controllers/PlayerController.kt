package proxy.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@Controller
@RequestMapping("v1/player")
class PlayerController {

    @GetMapping("proxy")
    fun proxy(@RequestBody player: UUID): Nothing = TODO()

    @GetMapping("server")
    fun server(@RequestBody player: UUID): Nothing = TODO()

}