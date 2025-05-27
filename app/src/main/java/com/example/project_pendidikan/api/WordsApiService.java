package com.example.project_pendidikan.api;

import com.example.project_pendidikan.model.WordResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WordsApiService {
    @GET("{word}")
    Call<List<WordResponse>> getWord(@Path("word") String word);
}
