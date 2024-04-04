package gay.extremist.data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TagResponse(
    val id: Int,
    val tag: String,
    val category: String?
)
