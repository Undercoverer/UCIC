package gay.extremist.util

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

suspend fun PipelineContext<Unit, ApplicationCall>.requiredHeaders(vararg headers: String): Map<String, String>? =
    with(call) {
        val missing = headers.filter { request.headers[it] == null }
        if (missing.isNotEmpty()) respond(ErrorResponse.headersNotProvided(missing)).also { return null }

        return headers.associateWith { request.headers[it]!! }
    }

fun PipelineContext<Unit, ApplicationCall>.optionalHeaders(vararg headers: String): Map<String, String?> = with(call) {
    return headers.associateWithNullable { request.headers[it] }
}

suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.convert(
    value: String?, converter: (String) -> T
): T? {
    if (value == null) {
        throw IllegalArgumentException("Value cannot be null")
    }
    return runCatching {
        converter(value)
    }.onFailure {
        call.respond(
            ErrorResponse.conversionError(
                value, T::class.simpleName ?: "Unknown type", it.message ?: "Unknown error"
            )
        )
    }.getOrNull()
}

suspend fun PipelineContext<Unit, ApplicationCall>.idParameter(): Int? {
    return when (val idString = call.parameters["id"]) {
        null -> call.respond(ErrorResponse.notProvided("id")).run { null }
        else -> return convert(idString, String::toInt)
    }
}

suspend inline fun <reified T> ApplicationCall.receiveCatching(): Result<T> {
    return runCatching {
        receiveNullable<T>()!!
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.onFailureOrNull(action: (exception: Throwable) -> Unit): T? {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }

    val containedVal = this.getOrNull()
    when {
        (containedVal == null) -> action(Exception("Unknown error")).also { return null }
        isSuccess -> return containedVal
        else -> action(this.exceptionOrNull()!!).also { return null }
    }
}

inline fun <K, V> Array<out K>.associateWithNullable(valueSelector: (K) -> V?): Map<K, V?> {
    val result = LinkedHashMap<K, V?>(size.coerceAtLeast(16))
    return associateWithTo(result, valueSelector)
}