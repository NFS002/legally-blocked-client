package blokd.node.service

import blokd.block.Block
import blokd.node.KAFKA_GROUP_ID
import blokd.node.KAFKA_TOPIC
import blokd.node.loadKafkaConfig
import blokd.node.serializer.BlockSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.util.*

object BlockProducer {

    private val LOGGER = Logger.getLogger(this::class.java)

    // Create topic in Confluent Cloud
    private fun createTopic(properties: Properties): Result<Void> {
        LOGGER.debug {
            "Attempting creation of topic '$KAFKA_TOPIC'"
        }
        val newTopic = NewTopic(KAFKA_TOPIC, 1, 3)

        return runCatching {
            with(AdminClient.create(properties)) {
                createTopics(listOf(newTopic)).all().get()
            }
        }
    }

    private fun loadProducerConfig(): Properties {
        val props = loadKafkaConfig()
        // Add additional properties.
        props[ACKS_CONFIG] = "all"
        props[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName
        props[VALUE_SERIALIZER_CLASS_CONFIG] = BlockSerializer::class.qualifiedName
        return props
    }

    fun publish(block: Block) {
        val props = loadProducerConfig()
        val res = createTopic(props)
        this.handleTopicCreationResult(res)

        KafkaProducer<String, Block>(props).use { producer ->
            val key = "${KAFKA_GROUP_ID}:${block.header}"
            producer.send(ProducerRecord(KAFKA_TOPIC, key, block)) { m: RecordMetadata, e: Exception? ->
                this.handleBlockPublishResult(block, m, e)
            }
        }
    }

    private fun handleTopicCreationResult(result: Result<Void>) {
        result.onSuccess {
            "Topic creation for '${KAFKA_TOPIC}' succeeded"
        }.onFailure { exc ->
            val logLevel: Level = if (exc is TopicExistsException) Level.WARN else Level.ERROR
            LOGGER.log(logLevel) {
                "Topic creation for '${KAFKA_TOPIC}' failed: $exc"
            }
        }
    }

    private fun handleBlockPublishResult(block: Block, metadata: RecordMetadata, exception: Exception?) {
        exception?.let {
            LOGGER.error {
                "Publishing block '${block}' to'${KAFKA_TOPIC}' failed: $exception"
            }
            throw it
        } ?: run {
            LOGGER.error {
                "Publishing block '${block}' to'${metadata.topic()}' succeeded."
            }
        }
    }
}
