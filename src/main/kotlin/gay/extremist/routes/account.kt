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

    route("/login") { post { handleAccountLogin() } }

    route("/register") { post { handleAccountRegistration() } }

    route("/{id}") {
        get { handleGetAccount() }
        delete { handleDeleteAccount() }
        post { handleUpdateAccount() }

        route("/creator") {
            get { handleGetDisplayAccount() }
        }

        route("/videos") {
            get { handleGetAccountVideos() }
        }
        route("/playlists") {
            get { handleGetAccountPlaylists() }
        }
        route("/tags") {
            get { handleGetFollowedTags() }
        }
        route("/following") {
            get { handleGetFollowedAccounts() }
        }
        route("/follow") {
            post { handleFollowAccount() }
        }
        route("/unfollow") {
            post { handleUnfollowAccount() }
        }

        route("/recommended-videos") {
            get { handleGetRecommendedVideos() }
        }
    }

    route("/search") {
        get { handleSearchAccounts() }
    }
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleUpdateAccount() {
    val headers = requiredHeaders(headerToken, headerAccountId) ?: return
    val account = call.receiveCatching<RegistrationAccount>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return

    val originalAccount = accountDao.readAccount(headers[headerAccountId]?.toInt() ?: -1) ?: return call.respond(ErrorResponse.notFound("Account"))

    val finalAccount = accountDao.updateAccount(originalAccount.id.value, account.username, account.email, account.password) ?: return

    call.respond(finalAccount.toResponse())
}


// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetRecommendedVideos() {
    val accountId = idParameter() ?: return
    val queryParameters = requiredQueryParameters("by")  ?: return
    val headers = requiredHeaders(headerToken) ?: return
    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (account.token != headers[headerToken]) return call.respond(ErrorResponse.accountTokenInvalid)

    val byMethod = queryParameters["by"] ?: return

    val videos = when (byMethod) {
        "tags" -> accountDao.getRecommendedVideosByFollowedTags(accountId)
        "following" -> accountDao.getRecommendedVideosByFollowedAccounts(accountId)
        else -> return call.respond(ErrorResponse.notProvided("By method"))
    }
    call.respond(videos.map(Video::toDisplayResponse))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleFollowAccount() {
    val headers = requiredHeaders(headerToken, headerAccountId) ?: return
    val yourAccountId = convert(headers[headerAccountId], String::toInt) ?: return
    val theirAccountId = idParameter() ?: return
    val token = headers[headerToken] ?: return

    val follower = accountDao.readAccount(yourAccountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (follower.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val creator = accountDao.readAccount(theirAccountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    if (accountDao.addFollowedAccount(yourAccountId, creator)) {
        call.respondText("$yourAccountId followed $theirAccountId successfully")
    } else {
        call.respond(ErrorResponse.alreadyExists("Account"))
    }
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleUnfollowAccount() {
    val headers = requiredHeaders(headerToken, headerAccountId) ?: return
    val yourAccountId = convert(headers[headerAccountId], String::toInt) ?: return
    val theirAccountId = idParameter() ?: return
    val token = headers[headerToken] ?: return

    val follower = accountDao.readAccount(yourAccountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    if (follower.token != token) return call.respond(ErrorResponse.accountTokenInvalid)

    val creator = accountDao.readAccount(theirAccountId) ?: return call.respond(ErrorResponse.notFound("Account"))

    if (accountDao.removeFollowedAccount(yourAccountId, creator)) {
        call.respondText("$yourAccountId unfollowed $theirAccountId successfully")
    } else {
        call.respond(ErrorResponse.notFollowed("Account"))
    }
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleSearchAccounts() {
    val queryParameters = requiredQueryParameters("q") ?: return
    val optionalQueryParameters = optionalQueryParameters("fuzzy")
    val fuzzy = optionalQueryParameters["fuzzy"]?.toBoolean() ?: false
    val query = queryParameters["q"] ?: return call.respond(ErrorResponse.notProvided("Query"))
    val accounts = if (fuzzy) accountDao.searchAccountsFuzzy(query) else accountDao.searchAccounts(query)
    call.respond(accounts.map(Account::toDisplayResponse))
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
    call.respond(tags.map(Tag::toResponse))
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

private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetDisplayAccount() {
    val accountId = idParameter() ?: return

    val account = accountDao.readAccount(accountId) ?: return call.respond(ErrorResponse.notFound("Account"))
    call.respond(account.toDisplayResponse())
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
private suspend fun PipelineContext<Unit, ApplicationCall>.handleAccountLogin() {
    val loginAccount = call.receiveCatching<LoginAccount>().onFailureOrNull {
        call.respond(ErrorResponse.schema.apply { data = it.message })
    } ?: return

    println(loginAccount.email + " : " + loginAccount.password + "-----------------------------------------------------------------------------------------")
    val token = accountDao.getToken(loginAccount.email, loginAccount.password)
        ?: return call.respond(ErrorResponse.accountIncorrectCredentials)

    val accountId = accountDao.getIdByEmail(loginAccount.email) ?: return
    println(token + "--------------------------------------------------------------------------------------------------------------------------------------")

    call.respond(RegisteredAccount(token, accountId))
}

// 100% Done
private suspend fun PipelineContext<Unit, ApplicationCall>.handleGetAllAccounts() {
    val headers = requiredHeaders("secret") ?: return
    if (headers["secret"] != "meow") return

    val accounts = accountDao.readAccountAll().also {
        if (it.isEmpty()) return call.respond(ErrorResponse.notFound("Accounts"))
    }

    call.respond(accounts.map(Account::toResponse))
}