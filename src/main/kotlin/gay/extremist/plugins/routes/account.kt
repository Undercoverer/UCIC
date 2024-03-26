package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.data_classes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createAccountRoutes() = route("/accounts") {
    val accountDao = accountDao

    get {
        val secret = call.request.headers["secret"]
        if (secret != "meow") return@get

        val accounts = accountDao.readAccountAll()
        if (accounts.isEmpty()) return@get call.respond(ErrorResponse.accountNotFound)

        call.respond(accounts.map {
            PrivilegedAccessAccount(it.id.value, it.username, it.email, it.password, it.token)
        })
    }

    // DONE
    route("/token") {
        get {
            val account = runCatching {
                call.receive<LoginAccount>()
            }.run {
                getOrNull() ?: return@get call.respond(ErrorResponse.accountSchema.apply {
                    data = exceptionOrNull()?.message
                })
            }

            val token = accountDao.getToken(account.username, account.password)
            token ?: return@get call.respond(ErrorResponse.accountIncorrectCredentials)
            call.respond(token)
        }
    }

    // DONE
    route("/register") {
        post {
            val account = runCatching {
                call.receive<RegisterAccount>()
            }.run {
                getOrNull() ?: return@post call.respond(ErrorResponse.accountSchema.apply {
                    data = exceptionOrNull()?.message
                })
            }

            val finalAccount = accountDao.createAccount(account.username, account.email, account.password)
            finalAccount ?: return@post call.respond(ErrorResponse.accountUsernameOrEmailTaken)

            call.respond(RegisteredAccount(finalAccount.id.value, finalAccount.token))
        }
    }

    // DONE TODO IMPLEMENT POST REQUEST TO CHANGE ACCOUNT INFO
    route("/{id}") {
        get {
            val account = call.parameters["id"]
            account ?: return@get call.respond(ErrorResponse.accountIdNotProvided)

            runCatching { account.toInt() }.onFailure {
                return@get call.respond(ErrorResponse.accountNonNumericId)
            }

            val token = call.request.headers["token"]
            val accountWithToken = accountDao.readAccount(account.toInt())
            accountWithToken ?: return@get call.respond(ErrorResponse.accountNotFound)

            when (accountWithToken.token) {
                token -> call.respond(
                    PrivilegedAccessAccount(
                        accountWithToken.id.value,
                        accountWithToken.username,
                        accountWithToken.email,
                        accountWithToken.password,
                        accountWithToken.token
                    )
                )

                else -> call.respond(
                    UnprivilegedAccessAccount(
                        accountWithToken.id.value, accountWithToken.username, accountWithToken.email
                    )
                )
            }
        }

        delete {
            val accountId = call.parameters["id"]
            accountId ?: return@delete call.respond(ErrorResponse.accountIdNotProvided)

            runCatching { accountId.toInt() }.onFailure {
                return@delete call.respond(ErrorResponse.accountNonNumericId)
            }

            val token = call.request.headers["token"]
            token ?: return@delete call.respond(ErrorResponse.accountTokenNotProvided)

            val account = accountDao.readAccount(accountId.toInt())
            account ?: return@delete call.respond(ErrorResponse.accountNotFound)

            if (account.token != token) return@delete call.respond(ErrorResponse.accountTokenInvalid)

            accountDao.deleteAccount(accountId.toInt())
            call.respond(status = HttpStatusCode.OK, message = "Account deleted")
        }

        post {
//            // Once we have things we care about changing
//            val newAccountInfo = runCatching {
//                call.receive<UpdateAccount>()
//            }.run {
//                getOrNull() ?: return@post call.respond(ErrorResponse.accountSchema.apply {
//                    data = exceptionOrNull()?.message
//                })
//            }
//
//            val accountId = call.parameters["id"]
//            accountId ?: return@post call.respond(ErrorResponse.accountIdNotProvided)
//
//            val token = call.request.headers["token"]
//            token ?: return@post call.respond(ErrorResponse.accountTokenNotProvided)
//
//            val account = accountDao.readAccount(accountId.toInt())

            call.respond(HttpStatusCode.NotImplemented, "Endpoint Not implemented")
        }
    }

    // TODO CREATE RELATION-BASED ROUTES
}