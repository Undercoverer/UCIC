package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object AccountFollowsAccount: Table() {
    val follower: Column<EntityID<Int>> = reference("followerID", Accounts, onDelete = ReferenceOption.CASCADE)
    val account: Column<EntityID<Int>> = reference("accountID", Accounts, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(
        follower, account
    )
}