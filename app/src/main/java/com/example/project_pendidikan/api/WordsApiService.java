package com.example.project_pendidikan.api;

import com.example.project_pendidikan.model.WordResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface WordsApiService {
    @Headers({
        "X-RapidAPI-Host: wordsapiv1.p.rapidapi.com",
        "X-RapidAPI-Key: YOUR_API_KEY_HERE" // Ganti dengan API key Anda
    })
    @GET("words/{word}")
    Call<WordResponse> getWord(@Path("word") String word);
}
