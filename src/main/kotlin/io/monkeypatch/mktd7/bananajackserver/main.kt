package io.monkeypatch.mktd7.bananajackserver

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.javalin.json.JavalinJackson


data class ServerConfig(val port: Int) {
    companion object {
        fun fromConfig(): ServerConfig =
            ConfigFactory.load()
                .extract("server")
    }
}


fun main(args: Array<String>) {
    val config = ServerConfig.fromConfig()

    JavalinJackson.configure(jsonMapper)

    server(config).start()
}