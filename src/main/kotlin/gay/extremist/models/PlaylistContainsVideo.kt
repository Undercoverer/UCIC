package gay.extremist.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PlaylistContainsVideo: Table() {
    private val playlist = reference("playlistID", Playlists, onDelete = ReferenceOption.CASCADE)
    private val video = reference("videoID", Videos,  onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(
        playlist, video
    )
}