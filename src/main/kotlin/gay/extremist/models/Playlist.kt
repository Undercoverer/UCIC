package gay.extremist.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Playlists: IntIdTable() {
    val ownerId: Column<EntityID<Int>> = reference("ownerID", Accounts)
    val name: Column<String> = varchar("name", 255)
    val description: Column<String> = varchar("description", 255)
}

class Playlist(id: EntityID<Int>): Entity<Int>(id){
    companion object: EntityClass<Int, Playlist>(Playlists)

    var owner by Account referencedOn Playlists.ownerId
    var name by Playlists.name
    var description by Playlists.description

    var videos by Video via PlaylistContainsVideo
}