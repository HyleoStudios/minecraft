package proxy.redis

import com.squareup.okhttp.Dispatcher
import com.squareup.okhttp.OkHttpClient
import java.util.concurrent.Executors

object MojangHttpClient : OkHttpClient() {
    init {
        dispatcher = Dispatcher(Executors.newFixedThreadPool(6))
    }
}