package gay.extremist.plugins

import gay.extremist.dao.AccountDaoImpl
import gay.extremist.dao.dao
import gay.extremist.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.math.E

fun Application.configureRouting() {
    // TODO Configure Routing
    routing {
        // Accounts
        route("/account") {
            val accountDao = dao
            route("/token") {
                get {
                    val account = call.receive<LoginAccount>()
                    val accountId = accountDao.getIdByUsername(account.username)
                    val accountWithToken = accountDao.account(accountId)
                    if (accountWithToken?.password == account.password) {
                        call.respond(accountWithToken.token)
                    } else {
                        throw Exception("Invalid password")
                    }
                }
            }
            route("/register") {
                post {
                    val account = call.receive<RegisterAccount>()
                    val accountWithToken = accountDao.addNewAccount(account.username, account.email, account.password)
                    call.respond(accountWithToken ?: throw Exception("Account creation failed"))
                }
            }
            route("/{id}") {

                get {
                    val account = call.parameters["id"]?.toInt()?.let { acc -> accountDao.account(acc) }
                    when (account) {
                        is Account -> {
                            if (account.token == call.request.headers["token"]) {
                                call.respond(account)
                            } else {
                                call.respond(UnprivilegedAccount(account.id.value , account.username, account.email))
                            }
                        }

                        else -> throw Exception("Account not found")
                    }
                }
                delete {
                    val account = call.parameters["id"]?.toInt()?.let { acc -> accountDao.account(acc) }
                    when (account) {
                        is Account -> {
                            if (account.token == call.request.headers["token"]) {
                                accountDao.deleteAccount(account.id.value)
                                call.respond(account)
                            } else {
                                throw Exception("Account access token not provided")
                            }
                        }

                        else -> throw Exception("Account not found")
                    }
                }
                post {
                    val account = call.receive<Account>()
                    if (account.token != call.request.headers["token"]) {
                        throw Exception("Account access token not provided")
                    }
                    accountDao.editAccount(account.id.value, account.username, account.email, account.password)
                    call.respond(account)
                }
            }
        }
        // Videos
        route("/videos") {

        }
        // Tags
        route("/tags") {

        }
    }
}
