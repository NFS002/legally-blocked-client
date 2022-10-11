package blokd.node.serializer

import blokd.actions.BlockData
import blokd.extensions.shorten
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class JsonBlock @JsonCreator constructor(
    @JsonProperty val previousHash: String,
    @JsonProperty val expectedHeight: Int,
    @JsonProperty val blockData: List<BlockData>,
    @JsonProperty var signatures: Map<String, ByteArray>,
    @JsonProperty val header: String,
    @JsonProperty val nonce:Long,
    @JsonProperty val timestamp: Long
)
