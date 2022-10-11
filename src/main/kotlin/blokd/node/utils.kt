package blokd.node

import blokd.extensions.*
import java.io.FileInputStream

import java.util.*

val CONFIGKEY__KAFKA_CONFIG_PATH = "kafka.config"

val KAFKA_GROUP_ID = PRIMARY_KEYPAIR.public.id()

val KAFKA_CLIENT_ID = "a42a9fe0-9870-48bb-86cf-c96b87ff0fbc"

//val KAFKA_CLIENT_ID = UUID.randomUUID().toString()

const val KAFKA_TOPIC: String = "blocks_v4"

private val KAFKA_CONFIG_PATH = BASE_PROPERTIES.getProperty(CONFIGKEY__KAFKA_CONFIG_PATH)


fun loadKafkaConfig() = FileInputStream(KAFKA_CONFIG_PATH).use {
    Properties().apply {
        this.load(it)
    }
}
