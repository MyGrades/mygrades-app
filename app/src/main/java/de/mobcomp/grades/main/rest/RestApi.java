package de.mobcomp.grades.main.rest;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Interface which defines all REST endpoints.
 */
public interface RestApi {

    @GET("/universities")
    Void getUniversities();

    @GET("/universities/{university_id}")
    Void getUniversity(@Path("university_id") int universityId);

    @POST("/wishlist")
    Void createWish();

    @POST("/universities/{university_id}/errors")
    Void createError(@Path("university_id") int universityId);
}
