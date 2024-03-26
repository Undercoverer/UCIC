package gay.extremist.data_classes

import kotlinx.serialization.Serializable

@Serializable
data class RegisterAccount(val username: String, val email: String, val password: String)

@Serializable
data class RegisteredAccount(val accountId: Int, val token: String)

@Serializable
data class LoginAccount(val username: String, val password: String)

@Serializable
data class UnprivilegedAccessAccount(val accountID: Int, val username: String, val email: String)

@Serializable
data class PrivilegedAccessAccount(val accountID: Int, val username: String, val email: String, val password: String, val token: String)
