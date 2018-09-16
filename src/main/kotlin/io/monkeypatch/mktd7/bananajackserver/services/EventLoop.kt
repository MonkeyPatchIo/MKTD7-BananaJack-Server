package io.monkeypatch.mktd7.bananajackserver.services

import io.monkeypatch.mktd7.bananajackserver.millis
import io.monkeypatch.mktd7.bananajackserver.seconds
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object EventLoop {
    private val ec = Executors.newScheduledThreadPool(1)

    val shortTimeout = 2.seconds
    val longTimeout = 30.seconds

    fun <T> schedule(duration: Duration, block: () -> T): CompletableFuture<T> =
        ec.schedule(Callable<T> {
            try {
                return@Callable block()
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }, duration.toMillis(), TimeUnit.MILLISECONDS)
            .let { scheduledFuture ->
                CompletableFuture.supplyAsync {
                    scheduledFuture.get()
                }
            }

    fun <T> immediate(block: () -> T): CompletableFuture<T> =
        schedule(1.millis, block)
}