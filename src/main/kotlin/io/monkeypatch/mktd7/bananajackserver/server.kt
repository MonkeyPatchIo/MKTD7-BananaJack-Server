package io.monkeypatch.mktd7.bananajackserver

import io.javalin.Javalin
import java.util.concurrent.ExecutionException

fun server(serverConfig: ServerConfig): Javalin =
    Javalin.create()
        .enableCorsForAllOrigins()
//        .enableDebugLogging()
        .enableAutogeneratedEtags()
        .enableRouteOverview("/help")
        .enableStaticFiles("public")
        .disableStartupBanner()
        .port(serverConfig.port)
        .routes(apiRest)
        .ws("/ws/:room", apiWs)
        .exception(NoSuchElementException::class.java) { e, ctx ->
            ctx.status(404).result(e.localizedMessage)
        }
        .exception(IllegalArgumentException::class.java) { e, ctx ->
            ctx.status(400).result(e.localizedMessage)
        }
        .exception(ExecutionException::class.java) { e, ctx ->
            when(e.cause) {
                null -> ctx.status(400).result(e.localizedMessage)
                is IllegalArgumentException -> ctx.status(400).result(e.localizedMessage)
                is NoSuchElementException -> ctx.status(404).result(e.localizedMessage)
            }
        }