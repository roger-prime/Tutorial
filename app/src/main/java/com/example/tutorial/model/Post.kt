package com.example.tutorial.model

import com.google.gson.annotations.SerializedName

// Campos que devuelve https://jsonplaceholder.typicode.com/posts/1.
data class Post(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
