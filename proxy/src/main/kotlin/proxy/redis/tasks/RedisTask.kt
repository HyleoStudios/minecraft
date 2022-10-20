package proxy.redis.tasks

import proxy.redis.jedis.JedisSummoner
import redis.clients.jedis.UnifiedJedis
import java.util.concurrent.Callable

abstract class RedisTask<V> : Runnable, Callable<V?> {

    @Throws(Exception::class)
    override fun call() = execute()

    abstract fun unifiedJedisTask(unifiedJedis: UnifiedJedis): V

    override fun run() {
        execute()
    }

    fun execute() = unifiedJedisTask(JedisSummoner)


}