package gay.extremist.plugins.routes

import gay.extremist.dao.accountDao
import gay.extremist.data_classes.*
import gay.extremist.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Route.createAccountRoutes() = route("/accounts") {
    get { handleGetAllAccounts() }

    route("/token") { get { handleGetToken() } }

    route("/register") { post { handleAccountRegistration() } }

    route("/{id}") {3
        get { handleGetAccount() }
        delete { handleDeleteAccount() }

        post { call.respond(HttpStatusCode.NotImplemented) }
    }

    // TODO CREATE RELATION-BASED ROUTES
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteAccount() {
    val optHeaders = optionalHeaders(headerToken)
    val accountId = idParameter() ?: return

    val token = optHeaders[headerToken] ?: return call.respond(ErrorResponse.accountTokenNotProvided)
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.accountNotFound)
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    accountDao.deleteAccount(accountId)
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAccount() {

    val headers = requiredHeaders(headerAccountId) ?: return
    val optHeaders = optionalHeaders(headerToken)
    val accountId = convert(headers[headerAccountId], String::toInt) ?: return

    val token = optHeaders[headerToken]
    val accountWithToken = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.accountNotFound)

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


private suspend fun PipelineContext<Unit, ApplicationCall>.handleAccountRegistration() {
    val account = call.receiveCatching<RegisterAccount>().onFailureOrNull {
        call.respond(ErrorResponse.accountSchema.apply { data = it.message })
    } ?: return

    val finalAccount = accountDao.createAccount(account.username, account.email, account.password)
    finalAccount ?: return call.respond(ErrorResponse.accountUsernameOrEmailTaken)

    call.respond(RegisteredAccount(finalAccount.id.value, finalAccount.token))
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetToken() {
    val account = call.receiveCatching<LoginAccount>().onFailureOrNull {
        call.respond(ErrorResponse.accountSchema.apply { data = it.message })
    } ?: return

    val token = accountDao.getToken(account.username, account.password)
        ?: return call.respond(ErrorResponse.accountIncorrectCredentials)

    call.respond(token)
}


// DONE
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAllAccounts() {
    val headers = requiredHeaders("secret")
    if (headers?.get("secret") != "meow") return

    val accounts = accountDao.readAccountAll().also {
        if (it.isEmpty()) return call.respond(ErrorResponse.accountNotFound)
    }

    call.respond(accounts.map {
        PrivilegedAccessAccount(it.id.value, it.username, it.email, it.password, it.token)
    })
}