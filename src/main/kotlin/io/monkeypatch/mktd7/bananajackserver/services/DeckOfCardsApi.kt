package io.monkeypatch.mktd7.bananajackserver.services

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Feign
import feign.Logger
import feign.Param
import feign.RequestLine
import feign.jackson.JacksonDecoder
import feign.slf4j.Slf4jLogger
import io.monkeypatch.mktd7.bananajackserver.jsonMapper
import io.monkeypatch.mktd7.bananajackserver.models.Card
import io.monkeypatch.mktd7.bananajackserver.models.CardSuit
import io.monkeypatch.mktd7.bananajackserver.models.CardValue


interface DeckOfCardsApi {

    data class Deck(
        @JsonProperty("deck_id") val id: String,
        val success: Boolean,
        val remaining: Int,
        val shuffled: Boolean
    )

    data class DeckDraw(
        @JsonProperty("deck_id") val id: String,
        val success: Boolean,
        val remaining: Int,
        val cards: List<DeckCard>
    )

    data class DeckCard(
        val code: String,
        val suit: String,
        val image: String,
        val images: Map<String, String>,
        val value: String
    ) {
        fun toCard(): Card =
            Card( code, CardValue[value],CardSuit[suit])

    }


    @RequestLine("GET /api/deck/new/shuffle/?deck_count={count}")
    fun newDeck(@Param("count") count: Int = 6): Deck


    @RequestLine("GET /api/deck/{deckId}/draw/?count={count}")
    fun draw(@Param("deckId") deckId: String, @Param("count") count: Int = 2): DeckDraw


    companion object {

        val api: DeckOfCardsApi =
            Feign.builder()
                .decoder(JacksonDecoder(jsonMapper))
                .logger(Slf4jLogger())
                .logLevel(Logger.Level.BASIC)
                .requestInterceptor {
                    it
                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "deflate")
                        .header("Host", "deckofcardsapi.com")
                        .header("User-Agent", "Feign")
                }
                .target(DeckOfCardsApi::class.java, "https://deckofcardsapi.com")
    }
}



