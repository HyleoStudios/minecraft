package proxy.redis.util

import com.google.common.collect.Multimap
import com.google.common.collect.Multiset
import com.google.common.io.ByteArrayDataOutput

object Serializations {
    fun serializeMultiset(collection: Multiset<String>, output: ByteArrayDataOutput) {
        output.writeInt(collection.elementSet().size)
        for (entry in collection.entrySet()) {
            output.writeUTF(entry.element!!)
            output.writeInt(entry.count)
        }
    }

    fun serializeMultimap(collection: Multimap<String?, String?>, includeNames: Boolean, output: ByteArrayDataOutput) {
        output.writeInt(collection.keySet().size)
        for ((key, value) in collection.asMap()) {
            output.writeUTF(key!!)
            if (includeNames) {
                serializeCollection(value, output)
            } else {
                output.writeInt(value.size)
            }
        }
    }

    fun serializeCollection(collection: Collection<*>, output: ByteArrayDataOutput) {
        output.writeInt(collection.size)
        for (o in collection) {
            output.writeUTF(o.toString())
        }
    }
}