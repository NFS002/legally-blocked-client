package blokd.node.serializer

import blokd.actions.Contract
import blokd.block.Block
import blokd.extensions.id
import blokd.extensions.newKeypair
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlockSerializationTest {

    lateinit var mapper:ObjectMapper

    @Before
    fun before() {
        mapper = configureObjectMapper(jacksonObjectMapper())

    }

    /* Serialization/Deserialisation test:
     * (De)serialize a Block to and from json
     * and check the result is equal to the block we
     * started with.
     */
    @Test
    fun canSerializeAndDeserialize() {
        val text = "This is a contract (v2) for Kefei! Will she sign it?"
        val keyPair1 = newKeypair()
        val keyPair2 = newKeypair()
        val contract = Contract(text, owner = keyPair1.public, intendedRecipient = keyPair2.public)
        contract.sign(keyPair1.private)
        val block = Block(blockData = listOf(contract))
        block.sign(keyPair1.private, id = keyPair1.public.id())
        val jsonString = mapper.writeValueAsString(block)
        val newBlock = mapper.readValue(jsonString, Block::class.java)
        assertTrue(block.equals(newBlock))
    }
}