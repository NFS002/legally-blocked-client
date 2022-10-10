package blokd.node

import blokd.extensions.*
import java.io.FileInputStream

import java.util.*

val CONFIGKEY__KAFKA_CONFIG_PATH = "kafka.config"

val KAFKA_GROUP_ID = PRIMARY_KEYPAIR.public.id()

const val KAFKA_TOPIC: String = "blocks_v2"

private val KAFKA_CONFIG_PATH = BASE_PROPERTIES.getProperty(CONFIGKEY__KAFKA_CONFIG_PATH)


fun loadKafkaConfig() = FileInputStream(KAFKA_CONFIG_PATH).use {
    Properties().apply {
        this.load(it)
    }
}
