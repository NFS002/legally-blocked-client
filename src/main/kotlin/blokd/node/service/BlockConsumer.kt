package blokd.node.service

import blokd.block.Block
import blokd.block.BlockChain
import blokd.block.cache.Fcache
import blokd.block.cache.PCache
import blokd.extensions.*
import blokd.node.KAFKA_CLIENT_ID
import blokd.node.KAFKA_GROUP_ID
import blokd.node.KAFKA_TOPIC
import blokd.node.loadKafkaConfig
import blokd.node.serializer.BlockDeserializer
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import java.time.Duration.ofMillis
import java.util.Properties
import kotlin.concurrent.thread

object BlockConsumer {

    private val LOGGER = Logger.getLogger(this::class.java)


    private fun loadConsumerConfig(): Properties {
        val properties = loadKafkaConfig()
        properties[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        properties[VALUE_DESERIALIZER_CLASS_CONFIG] = BlockDeserializer::class.java.name
        properties[JSON_VALUE_TYPE] = Block::class.java
        properties[GROUP_ID_CONFIG] = KAFKA_GROUP_ID
        //properties[CLIENT_ID_CONFIG] =  KAFKA_CLIENT_ID
        properties[AUTO_OFFSET_RESET_CONFIG] = "largest"
        properties[ENABLE_AUTO_COMMIT_CONFIG] = false
        properties[AUTO_OFFSET_RESET_CONFIG] = "latest"

        return properties
    }

    private fun buildFromCache(nextHeight:Int) {
        LOGGER.debug("Checking cache for blocks with suggested height of ${nextHeight}")
        val nextBlock: Block? = Fcache.get(nextHeight)
        LOGGER.debug("Found ${nextBlock ?: "no matching blocks"}")
        nextBlock?.also {
            BlockChain.add(it)
            LOGGER.info("Succesfully added block $it to chain, now at height ${BlockChain.nextHeight}")
            buildFromCache(nextHeight + 1)
        }
    }

    fun consume() {
        // Load properties from disk.
        val properties = loadConsumerConfig()

        val blockConsumer = KafkaConsumer<String, Block>(properties).apply {
            subscribe(listOf(KAFKA_TOPIC))
        }

        val keyPair = PRIMARY_KEYPAIR
        blockConsumer.poll(ofMillis(10000)).forEach { record ->
            val key = record.key()
            val block = record.value()

            LOGGER.info("Consumed record with key $key and value $block")

            val isSigned = block.isSignedBy(keyPair.public)
            val signedBlock = block.copy()

            isSigned.then {
                LOGGER.info("Block is already signed")
            }


            (!isSigned).then {
                //TODO("Uncomment line below")
                //signedBlock.sign(keyPair.private, keyId = keyPair.public.id())
                thread {
                    LOGGER.info("Publishing signed block")
                    BlockProducer.publish(signedBlock)
                }
            }
            (signedBlock.validate()).ifTrue {
                BlockChain.canAccept(signedBlock).ifTrue {
                    val res = runCatching {
                        BlockChain.add(block = block)
                    }
                    res.onSuccess {
                        val nextHeight = BlockChain.nextHeight
                        LOGGER.info(
                            "Block $signedBlock was successfully added. Chain is now at" +
                                    "$nextHeight"
                        )
                        buildFromCache(nextHeight)
                    }.onFailure {
                        LOGGER.error(
                            "Unable to add block $signedBlock that was previously marked as valid and at the expected height. " +
                                    "Your blockchain may be out of sync, attempting recovery", it
                        )
                        throw it
                    }
                }.ifFalse {
                    val cache = if (signedBlock.expectedHeight > BlockChain.nextHeight) Fcache else PCache
                    cache.add(signedBlock)
                }
            }.ifFalse {
                LOGGER.info(
                    "Block $signedBlock is invalid or missing signatures. " +
                    "Block has been signed and is queued to be returned to sender."
                )
            }
        }
    }
}