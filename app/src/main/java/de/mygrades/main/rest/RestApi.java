package de.mygrades.main.rest;

import java.util.List;

import de.mygrades.database.dao.University;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Interface which defines all REST endpoints.
 */
public interface RestApi {

    @GET("/universities")
    List<University> getUniversities(@Header("Updated-At-Server") String updatedAtServer);

    @GET("/universities/{university_id}?detailed=true")
    University getUniversity(@Path("university_id") long universityId, @Header("Updated-At-Server") String updatedAtServer);

    @POST("/wishlist")
    Void createWish();

    @POST("/universities/{university_id}/errors")
    Void createError(@Path("university_id") long universityId);
}
