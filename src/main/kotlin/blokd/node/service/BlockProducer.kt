package blokd.node.service

import blokd.block.Block
import blokd.node.KAFKA_TOPIC
import blokd.node.loadKafkaConfig
import blokd.node.serializer.BlockSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.CreateTopicsResult
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
    private fun createTopic(properties: Properties): Result<CreateTopicsResult> {
        return runCatching {
            with(AdminClient.create(properties)) {
                LOGGER.debug("Attempting creation of topic '$KAFKA_TOPIC'")
                val newTopic = NewTopic(KAFKA_TOPIC, 1, 3)
                createTopics(listOf(newTopic))
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

    fun publish(block: Block, kafkaClientId:String) {
        val props = loadProducerConfig()
        val res = createTopic(props)
        this.handleTopicCreationResult(res)

        KafkaProducer<String, Block>(props).use { producer ->
            println("Publishing with key $kafkaClientId")
            producer.send(ProducerRecord(KAFKA_TOPIC, kafkaClientId, block)) { m: RecordMetadata, e: Exception? ->
                this.handleBlockPublishResult(block, m, e)
            }
        }
    }

    private fun handleTopicCreationResult(topicCreationResult: Result<CreateTopicsResult>) {
        topicCreationResult.onSuccess {
            "Topic creation for '${KAFKA_TOPIC}' succeeded"
        }.onFailure { exc ->
            val logLevel: Level = if (exc.cause is TopicExistsException) Level.WARN else Level.ERROR
            LOGGER.log(logLevel, "Topic creation for '${KAFKA_TOPIC}' failed: $exc")
        }
    }

    private fun handleBlockPublishResult(block: Block, metadata: RecordMetadata, exception: Exception?) {
        exception?.let {
            LOGGER.error("Publishing block '${block}' to'${KAFKA_TOPIC}' failed: $exception")
            throw it
        } ?: run {
            LOGGER.info("Publishing block '${block}' to topic '${metadata.topic()}' succeeded.")
        }
    }
}
