package proxy.redis.jedis

import proxy.contexts.PluginContext
import redis.clients.jedis.JedisPubSub

context (PluginContext)
class JedisPubSubHandler : JedisPubSub() {

    override fun onMessage(s: String, s2: String) {
        if (s2.trim { it <= ' ' }.isEmpty()) return
        plugin.executeAsync {
            val event = plugin.createPubSubEvent(s, s2)
            plugin.fireEvent(event)
        }
    }

}