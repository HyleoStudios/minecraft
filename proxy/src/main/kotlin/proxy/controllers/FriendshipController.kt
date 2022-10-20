package proxy.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
@RequestMapping("v1/friendship/")
class FriendshipController {

    @GetMapping
    fun friends(@RequestBody player: UUID): Nothing = TODO()

}