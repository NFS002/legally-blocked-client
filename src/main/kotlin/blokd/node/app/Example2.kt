@file:JvmName("Example2")

package blokd.node.app

import blokd.node.service.BlockConsumer
import org.apache.log4j.PropertyConfigurator

const val KAFKA_CLIENT_ID_2 = "EXAMPLE-2"

fun main() {
    PropertyConfigurator.configure("/Users/noah/projects/legally-blocked/src/main/resources/log4j.properties")
    BlockConsumer.consume(kafkaClientId = KAFKA_CLIENT_ID_2)
}