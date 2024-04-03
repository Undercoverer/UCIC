package gay.extremist.data_classes

import kotlinx.serialization.Serializable


@Serializable
data class VideoResponse(
    val videoId: Int,
    val title: String,
    val description: String,
    val videoPath: String,
    val tags: List<String>,
    val creatorId: Int,
    val uploadDate: String
)