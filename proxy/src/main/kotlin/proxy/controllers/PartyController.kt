package proxy.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("v1/party")
class PartyController {

    @GetMapping
    fun party(@RequestBody player: UUID) : Nothing = TODO()

}