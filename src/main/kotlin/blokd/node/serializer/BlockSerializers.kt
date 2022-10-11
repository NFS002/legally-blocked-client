package blokd.node.serializer

import blokd.block.Block
import blokd.block.JsonBlock
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

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
        val newBlock = Block(previousHash = jsonObject.previousHash, expectedHeight = jsonObject.expectedHeight)
        jsonObject.blockData.forEach{ b -> newBlock.addBlockData(b) }
        newBlock.signatures = jsonObject.signatures
        newBlock.nonce = jsonObject.nonce
        newBlock.header = jsonObject.header
        println("DESERIALIZED BLOCK: $newBlock")
        return newBlock
    }
}
