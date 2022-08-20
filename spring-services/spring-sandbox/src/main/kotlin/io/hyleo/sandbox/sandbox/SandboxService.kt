package io.hyleo.sandbox.sandbox


import lombok.extern.log4j.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

@Service
class SandboxService {

    @Autowired
    lateinit var repository: SandboxRepository

    companion object {
        const val CACHE = "sandboxes"
        const val CACHE_EXPIRATION_RATE = 5 * 60 * 1000L // 5 minutes
    }

    @CacheEvict(allEntries = true, value = [CACHE])
    @Scheduled(fixedDelay = CACHE_EXPIRATION_RATE, initialDelay = 500)
    fun evictCache() {
    }

    @Cacheable(CACHE)
    fun getById(id: UUID) = repository.findById(id)

    @Cacheable("sandboxes")
    fun getAllByOwner(owner: UUID) = repository.findAllByOwner(owner)

}