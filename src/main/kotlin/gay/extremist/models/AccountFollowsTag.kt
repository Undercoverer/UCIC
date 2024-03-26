package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object AccountFollowsTag: Table() {
    private val follower: Column<EntityID<Int>> = reference("followerID", Accounts, onDelete = ReferenceOption.CASCADE)
    private val tag: Column<EntityID<Int>> = reference("tagID", Tags, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(
        follower, tag
    )
}