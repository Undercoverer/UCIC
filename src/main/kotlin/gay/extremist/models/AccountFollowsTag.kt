package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object AccountFollowsTag: Table() {
    private val follower: Column<EntityID<Int>> = reference("followerID", Accounts)
    private val tag: Column<EntityID<Int>> = reference("tagID", Tags)
    override val primaryKey = PrimaryKey(
        follower, tag, name="PK_AccountFollowsTag_fol_tag"
    )
}