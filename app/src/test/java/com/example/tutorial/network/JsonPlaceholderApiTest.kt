package com.example.tutorial.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JsonPlaceholderApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: JsonPlaceholderApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JsonPlaceholderApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getPostReadsAllFieldsFromTheRequestedPost() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(
                    """{"userId":7,"id":12,"title":"Título de prueba","body":"Primera línea\nSegunda línea"}"""
                )
        )

        val post = api.getPost(12)

        assertEquals(7, post.userId)
        assertEquals(12, post.id)
        assertEquals("Título de prueba", post.title)
        assertEquals("Primera línea\nSegunda línea", post.body)
        assertEquals("/posts/12", server.takeRequest(1, TimeUnit.SECONDS)?.path)
    }

    @Test
    fun getPostPropagatesHttpErrorsToTheCaller() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))

        try {
            api.getPost(999)
            fail("Se esperaba un error HTTP para mostrar el Toast de error.")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
    }
}
