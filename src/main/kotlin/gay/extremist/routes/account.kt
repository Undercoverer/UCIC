package gay.extremist.routes

import gay.extremist.dao.accountDao
import gay.extremist.models.*
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

    route("/{id}") {
        get { handleGetAccount() }
        delete { handleDeleteAccount() }
        post { call.respond(HttpStatusCode.NotImplemented) }

        route("/videos") {
            get { handleGetAccountVideos() }
        }
        route("/playlists") {
            get { handleGetAccountPlaylists() }
        }
        route("/tags") {
            get { handleGetFollowedTags() }
        }
        route("/following"){
            get { handleGetFollowedAccounts()}
        }
    }
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetFollowedAccounts() {
    val headers = requiredHeaders(headerToken) ?: return
    val accountId = idParameter() ?: return

    val token = headers[headerToken] ?: return call.respond(ErrorResponse.notProvided("Token"))
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val accounts = accountDao.getFollowedAccounts(accountId)
    call.respond(accounts.map(Account::toDisplayResponse))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetFollowedTags() {
    val headers = requiredHeaders(headerToken) ?: return
    val accountId = idParameter() ?: return

    val token = headers[headerToken] ?: return call.respond(ErrorResponse.notProvided("Token"))
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val tags = accountDao.getFollowedTags(accountId)
    call.respond(tags.map(Tag::toDisplayResponse))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAccountVideos() {
    val accountId = idParameter() ?: return
    accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    val videos = accountDao.getVideosFromAccount(accountId)
    call.respond(videos.map(Video::toDisplayResponse))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAccountPlaylists() {
    val accountId = idParameter() ?: return
    accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    val playlists = accountDao.getPlaylistsFromAccount(accountId)
    call.respond(playlists.map(Playlist::toDisplayResponse))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleDeleteAccount() {
    val headers = requiredHeaders(headerToken) ?: return
    val accountId = idParameter() ?: return

    val token = headers[headerToken] ?: return call.respond(ErrorResponse.notProvided("Token"))
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    accountDao.deleteAccount(accountId)
    call.respond(HttpStatusCode.NoContent)
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAccount() {
    val accountId = idParameter() ?: return
    val headers = requiredHeaders(headerToken) ?: return

    val token = headers[headerToken] ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != token) return call.respond(ErrorResponse.accountTokenInvalid)
    call.respond(account.toResponse())
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleAccountRegistration() {
    val account = call.receiveCatching<RegistrationAccount>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return

    val finalAccount = accountDao.createAccount(account.username, account.email, account.password)
    finalAccount ?: return call.respond(ErrorResponse.accountUsernameOrEmailTaken)

    call.respond(finalAccount.toRegisteredAccountResponse())
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetToken() {
    val loginAccount = call.receiveCatching<LoginAccount>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return

    val token = accountDao.getToken(loginAccount.username, loginAccount.password)
        ?: return call.respond(ErrorResponse.accountIncorrectCredentials)

    val accountId = accountDao.getIdByUsername(loginAccount.username) ?: return

    call.respond(RegisteredAccount(accountId, token))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAllAccounts() {
    val headers = requiredHeaders("secret")
    if (headers?.get("secret") != "meow") return

    val accounts = accountDao.readAccountAll().also {
        if (it.isEmpty()) return call.respond(ErrorResponse.notFound("Account"))
    }

    call.respond(accounts.map(Account::toResponse))
}