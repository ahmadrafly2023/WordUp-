package com.example.project_pendidikan.api;

import com.example.project_pendidikan.model.DatamuseWord;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DatamuseService {
    @GET("words")
    Call<List<DatamuseWord>> getRelatedWords(
        @Query("ml") String word,
        @Query("md") String metadata
    );

    @GET("words")
    Call<List<DatamuseWord>> getSynonyms(
        @Query("rel_syn") String word,
        @Query("md") String metadata
    );
}
