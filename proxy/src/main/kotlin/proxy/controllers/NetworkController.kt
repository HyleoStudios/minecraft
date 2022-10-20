package proxy.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("v1/network")
class NetworkController {

    @GetMapping("pods")
    fun pods(): Nothing = TODO()

    @GetMapping("proxies")
    fun proxies(): Nothing = TODO()

    @GetMapping("servers")
    fun servers(): Nothing = TODO()

    @GetMapping("players")
    fun online(): Nothing = TODO()

}