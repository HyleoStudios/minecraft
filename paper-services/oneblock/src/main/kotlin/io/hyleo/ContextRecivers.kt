package io.hyleo


class Logger(val name: String) {
    fun log(message: String) = println("[$name]: $message")
}

class Notifier(val name: String) {
    fun notify(message: String) = println("Notifier: $message")
}


context (Logger, Notifier)
fun store(s: String) {
    log("Stored $s on disk via ${this@Logger.name}")
    notify("Successfully stored $s on disk")
}

fun main() = with(receiver = Logger(name = "Main")) {
    with(receiver = Notifier(name = "The best notifier")) {
        store("Txt file")
    }
}