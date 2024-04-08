package gay.extremist.data_classes

import kotlinx.serialization.Serializable


@Serializable
data class VideoResponse(
    val videoId: Int,
    val title: String,
    val description: String,
    val videoPath: String,
    val tags: List<TagResponse>,
    val creator: VideoCreator,
    val viewCount: Int,
    val uploadDate: String
)

@Serializable
data class VideoListObject(
    val id: Int,
    val title: String,
    val videoPath: String
)