package com.example.tutorial.network

import com.example.tutorial.model.Post
import retrofit2.http.GET
import retrofit2.http.Path

interface JsonPlaceholderApi {
    // Retrofit realiza la petición de forma asíncrona, sin bloquear la interfaz.
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post
}
