package de.mygrades.main.rest;

import java.util.List;

import de.mygrades.database.dao.University;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Interface which defines all REST endpoints.
 */
public interface RestApi {

    @GET("/universities")
    List<University> getUniversities();

    @GET("/universities/{university_id}")
    University getUniversity(@Path("university_id") int universityId);

    @POST("/wishlist")
    Void createWish();

    @POST("/universities/{university_id}/errors")
    Void createError(@Path("university_id") int universityId);
}
