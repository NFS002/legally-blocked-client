@file:JvmName("Example2")

package blokd.node.app

import blokd.node.service.BlockConsumer
import org.apache.log4j.PropertyConfigurator

fun main() {
    PropertyConfigurator.configure("/Users/noah/projects/legally-blocked/src/main/resources/log4j.properties")
    BlockConsumer.consume()
}