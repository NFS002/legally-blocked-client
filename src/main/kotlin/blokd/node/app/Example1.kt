@file:JvmName("Example1")

package blokd.node.app

import blokd.actions.Contract
import blokd.block.Block
import blokd.extensions.PRIMARY_KEYPAIR
import blokd.extensions.id
import blokd.extensions.newKeypair
import blokd.node.service.BlockProducer
import org.apache.log4j.PropertyConfigurator

fun main() {
    PropertyConfigurator.configure("/Users/noah/projects/legally-blocked/src/main/resources/log4j.properties")
    val text = "This is a contract (v2) for Kefei! Will she sign it?"
    val keyPair1 = PRIMARY_KEYPAIR
    val keyPair2 = newKeypair()
    val contract = Contract(text, owner = keyPair1.public, intendedRecipient = keyPair2.public)
    contract.sign(keyPair1.private)
    val block = Block()
    block.addBlockData(contract)
    block.sign(keyPair1.private, keyId = keyPair1.public.id())
    BlockProducer.publish(block)
}