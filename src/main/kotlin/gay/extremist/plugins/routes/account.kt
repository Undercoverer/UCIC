package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.data_classes.ErrorResponse
import gay.extremist.models.LoginAccount
import gay.extremist.models.RegisterAccount
import gay.extremist.models.RegisteredAccount
import gay.extremist.models.UnprivilegedAccount
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createAccountRoutes() = route("/account") {
    val accountDao = accountDao

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

    // ALMOST DONE TODO HANDLE USERNAME OR EMAIL ALREADY EXISTING
    route("/register") {
        post {
            val account = runCatching {
                call.receive<RegisterAccount>()
            }.run {
                getOrNull() ?: return@post call.respond(ErrorResponse.accountSchema.apply {
                    data = exceptionOrNull()?.message
                })
            }

            val accountWithToken = accountDao.createAccount(account.username, account.email, account.password)
            call.respond(RegisteredAccount(accountWithToken.id.value, accountWithToken.token))
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
                token -> call.respond(accountWithToken)
                else -> call.respond(
                    UnprivilegedAccount(
                        accountWithToken.id.value,
                        accountWithToken.username,
                        accountWithToken.email
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

            if (account.token != token) return@delete call.respond(ErrorResponse.tokenInvalid)

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

            call.respond(HttpStatusCode.NotImplemented, "Enpoint Not implemented")
        }
    }

    // TODO CREATE RELATION-BASED ROUTES
}