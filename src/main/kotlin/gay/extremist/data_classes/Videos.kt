package gay.extremist.data_classes

import kotlinx.serialization.Serializable


@Serializable
data class VideoResponse(
    val videoId: Int,
    val title: String,
    val description: String,
    val videoPath: String,
    val map: List<String>,
    val value: Int,
    val uploadDate: String
)