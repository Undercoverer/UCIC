package gay.extremist.data_classes

import kotlinx.serialization.Serializable

@Serializable
data class NewPlaylistData(val name: String, val description: String)

@Serializable
data class PlaylistResponse(val id: Int, val owner: Int, val name: String, val description: String, val videos: List<Int>)