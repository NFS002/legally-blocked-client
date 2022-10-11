package blokd.node.app

import blokd.actions.Contract
import blokd.block.Block
import blokd.extensions.PRIMARY_KEYPAIR
import blokd.extensions.id
import blokd.extensions.newKeypair
import blokd.node.serializer.configureObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun main() {
    val text = "This is a contract (v2) for Kefei! Will she sign it?"
    val keyPair1 = PRIMARY_KEYPAIR
    val keyPair2 = newKeypair()
    val contract = Contract(text, owner = keyPair1.public, intendedRecipient = keyPair2.public)
    contract.sign(keyPair1.private)
    val block = Block(blockData = listOf(contract))
    block.sign(keyPair1.private, id = keyPair1.public.id())
    val mapper = jacksonObjectMapper()
    configureObjectMapper(mapper)
    val jsonString = mapper.writeValueAsString(block)
    println(jsonString)
    val newBlock = mapper.readValue(jsonString, Block::class.java)
    println(newBlock)
}