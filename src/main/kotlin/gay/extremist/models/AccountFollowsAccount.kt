package gay.extremist.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object AccountFollowsAccount: Table() {
    val follower: Column<EntityID<Int>> = reference("followerID", Accounts)
    val account: Column<EntityID<Int>> = reference("accountID", Accounts)
    override val primaryKey = PrimaryKey(
        follower, account, name="PK_AccountFollowsAccount_fol_acc"
    )
}