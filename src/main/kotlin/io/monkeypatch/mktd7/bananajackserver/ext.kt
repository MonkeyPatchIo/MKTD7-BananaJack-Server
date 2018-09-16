package io.monkeypatch.mktd7.bananajackserver

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import java.security.MessageDigest
import java.time.Duration

val Int.seconds: Duration
    get() = Duration.ofSeconds(toLong())

val Int.millis: Duration
    get() = Duration.ofMillis(toLong())


val String.sha1: String
    get() {
        val md = MessageDigest.getInstance("SHA-1")
        md.reset()
        val bytes = md.digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

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

