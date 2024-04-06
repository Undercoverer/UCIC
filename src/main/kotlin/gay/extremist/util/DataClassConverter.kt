package gay.extremist.util

import io.ktor.util.reflect.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.LazySizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

inline fun <reified Dao: Entity<*>, reified Response> Dao.toResponse() : Response = this::class.members.let { doaParameters ->
    val response = Response::class
    val responseObjectParameters = Response::class.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.parameters
    response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.call(
        *responseObjectParameters.map { responseParam ->
            doaParameters.find { it.name == responseParam.name }?.let { doaParameter ->
                when{
                    doaParameter.call(this)?.instanceOf(EntityID::class) ?: false -> (doaParameter.call(this) as EntityID<*>).value
                    doaParameter.call(this)?.instanceOf(IntEntity::class) ?: false -> (doaParameter.call(this) as Entity<*>).handleObject(responseParam.type)
                    doaParameter.call(this)?.instanceOf(LazySizedCollection::class) ?: false -> (doaParameter.call(this) as LazySizedCollection<Entity<*>>).handleList(responseParam.type)
                    doaParameter.call(this)?.instanceOf(SizedIterable::class) ?: false -> (doaParameter.call(this) as SizedIterable<Entity<*>>).handleList(responseParam.type)

                    else -> doaParameter.call(this)
                }
            }
        }.toTypedArray()
    )
}

inline fun <reified Dao: Entity<*>, reified Response> SizedIterable<Dao>.toResponse() : List<Response> = this.map { dao ->
    dao::class.members.let { doaParameters ->
        val response = Response::class
        val responseObjectParameters = Response::class.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.parameters
        response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.call(
            *responseObjectParameters.map { responseParam ->
                doaParameters.find { it.name == responseParam.name }?.let { doaParameter ->
                    when{
                        doaParameter.call(dao)?.instanceOf(EntityID::class) ?: false -> (doaParameter.call(dao) as EntityID<*>).value
                        doaParameter.call(dao)?.instanceOf(LazySizedCollection::class) ?: false -> (doaParameter.call(dao) as LazySizedCollection<Entity<*>>).handleList(responseParam.type)
                        doaParameter.call(dao)?.instanceOf(SizedIterable::class) ?: false -> (doaParameter.call(dao) as SizedIterable<Entity<*>>).handleList(responseParam.type)
                        doaParameter.call(dao)?.instanceOf(IntEntity::class) ?: false -> (doaParameter.call(dao) as Entity<*>).handleObject(responseParam.type)
                        else -> try {
                            doaParameter.call(dao)
                        } catch (e: Exception){
                            dao.handleObject(responseParam.type)
                        }
                    }
                }
            }.toTypedArray()
        )
    }
}

fun <Dao: Entity<*>> Dao.handleObject(responseParam: KType) : Any = this::class.members.let { doaParameters ->
    val response = responseParam.jvmErasure
    response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.call(
        *response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.parameters.map { responseParam ->
            doaParameters.find { it.name == responseParam.name }?.let { doaParameter ->
                when{
                    doaParameter.call(this)?.instanceOf(EntityID::class) ?: false -> (doaParameter.call(this) as EntityID<*>).value
                    doaParameter.call(this)?.instanceOf(LazySizedCollection::class) ?: false -> (doaParameter.call(this) as LazySizedCollection<Entity<*>>).handleList(responseParam.type)
                    doaParameter.call(this)?.instanceOf(SizedIterable::class) ?: false -> (doaParameter.call(this) as SizedIterable<Entity<*>>).handleList(responseParam.type)
                    doaParameter.call(this)?.instanceOf(IntEntity::class) ?: false -> (doaParameter.call(this) as Entity<*>).handleObject(responseParam.type)
                    else -> doaParameter.call(this)
                }
            }
        }.toTypedArray()
    )
}

inline fun <reified Dao: Entity<*>> LazySizedCollection<Dao>.handleList(responseParam: KType): List<*> = this.wrapper.map { daoEntry ->
    val response = responseParam.arguments.first().type!!.jvmErasure
    response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.call(
        *response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.parameters.map { responseParam ->
            daoEntry::class.members.find { it.name == responseParam.name }?.let { doaParameter ->
                when{
                    doaParameter.call(daoEntry)?.instanceOf(EntityID::class) ?: false -> (doaParameter.call(daoEntry) as EntityID<*>).value
                    doaParameter.call(daoEntry)?.instanceOf(IntEntity::class) ?: false -> (doaParameter.call(daoEntry) as Entity<*>).handleObject(responseParam.type)
                    doaParameter.call(daoEntry)?.instanceOf(LazySizedCollection::class) ?: false -> (doaParameter.call(daoEntry) as LazySizedCollection<Entity<*>>).innerList(responseParam.type)
                    doaParameter.call(daoEntry)?.instanceOf(SizedIterable::class) ?: false -> (doaParameter.call(daoEntry) as SizedIterable<Entity<*>>).innerList(responseParam.type)
                    else -> try {
                        doaParameter.call(daoEntry)
                    } catch (e: Exception){
                        daoEntry.handleObject(responseParam.type)
                    }
                }
            }
        }.toTypedArray()
    )
}

inline fun <reified Dao: Entity<*>> SizedIterable<Dao>.handleList(responseParam: KType): List<*> = this.map { daoEntry ->
    val response = responseParam.arguments.first().type!!.jvmErasure
    response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.call(
        *response.constructors.first { !it.parameters.any { it.name == "serializationConstructorMarker" } }.parameters.map { responseParam ->
            daoEntry::class.members.find { it.name == responseParam.name }?.let { doaParameter ->
                when{
                    doaParameter.call(daoEntry)?.instanceOf(EntityID::class) ?: false -> (doaParameter.call(daoEntry) as EntityID<*>).value
                    doaParameter.call(daoEntry)?.instanceOf(IntEntity::class) ?: false-> (doaParameter.call(daoEntry) as Entity<*>).handleObject(responseParam.type)
                    doaParameter.call(daoEntry)?.instanceOf(LazySizedCollection::class) ?: false-> (doaParameter.call(daoEntry) as LazySizedCollection<Entity<*>>).innerList(responseParam.type)
                    doaParameter.call(daoEntry)?.instanceOf(SizedIterable::class) ?: false-> (doaParameter.call(daoEntry) as SizedIterable<Entity<*>>).innerList(responseParam.type)
                    else -> try {
                        doaParameter.call(daoEntry)
                    } catch (e: Exception){
                        daoEntry.handleObject(responseParam.type)
                    }
                }
            }
        }.toTypedArray()
    )
}

fun LazySizedCollection<Entity<*>>.innerList(responseParam: KType) = this.handleList(responseParam)

fun SizedIterable<Entity<*>>.innerList(responseParam: KType) = this.handleList(responseParam)