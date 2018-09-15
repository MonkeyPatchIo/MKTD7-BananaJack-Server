package io.monkeypatch.mktd7.bananajackserver

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


fun <T> List<T>.peek(): Pair<T, List<T>> {
    if (isEmpty()) throw NoSuchElementException("Empty list")
    return first() to drop(1)
}

fun <T> ScheduledExecutorService.schedule(duration: Duration, block: () -> T): ScheduledFuture<T> =
    this.schedule(Callable<T> {
        try {
            return@Callable block()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }, duration.toMillis(), TimeUnit.MILLISECONDS)

val Int.seconds: Duration
    get() = Duration.ofSeconds(toLong())

val Int.millis: Duration
    get() = Duration.ofMillis(toLong())

// Jackson

inline fun <reified T> SimpleModule.addSerializer(crossinline block: (T, JsonGenerator) -> Unit) =
    this.addSerializer(T::class.java, object : JsonSerializer<T>() {
        override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider?) =
            block(value, gen)
    })!!

fun JsonNode.getString(attr: String): String =
    (this.get(attr) as TextNode).textValue()

fun JsonNode.getInt(attr: String): Int =
    (this.get(attr) as IntNode).intValue()

inline fun <reified T> SimpleModule.addDeserializer(crossinline block: (JsonParser) -> T) =
    this.addDeserializer(T::class.java, object : JsonDeserializer<T>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): T =
            block(p)
    })!!

