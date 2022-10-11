package blokd.node.serializer

import blokd.block.Block
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.util.JSONPObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.json.JSONObject

class BlockSerializer : Serializer<Block> {

    val mapper = jacksonObjectMapper()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        configs?.also {
            configureObjectMapper(mapper)
        }
    }

    override fun serialize(topic: String?, data: Block?): ByteArray {
        return mapper.writeValueAsBytes(data)
    }
}

class BlockDeserializer : Deserializer<Block> {

    val mapper = jacksonObjectMapper()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        configs?.also {
            configureObjectMapper(mapper)
        }
    }

    override fun deserialize(topic: String?, data: ByteArray?): Block? {
        val jsonObject = mapper.readValue(data, JsonBlock::class.java)
        val block = mapper.readValue(data, Block::class.java)
        println("DESERIALIZED BLOCK AS JSONBlock: $jsonObject")
        println("DESERIALIZED BLOCK: $block")
        return block
    }
}
