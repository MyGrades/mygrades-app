package de.mygrades.main.rest;

import java.util.List;

import de.mygrades.database.dao.University;
import de.mygrades.main.processor.ErrorProcessor;
import de.mygrades.main.processor.WishProcessor;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Interface which defines all REST endpoints.
 */
public interface RestApi {

    @GET("/universities")
    List<University> getUniversities(@Query("published") boolean publishedOnly,
                                     @Header("Updated-At-Server-Published") String updatedAtServerPublished,
                                     @Header("Updated-At-Server-Unpublished") String updatedAtServerUnpublished);

    @GET("/universities/{university_id}?detailed=true")
    University getUniversity(@Path("university_id") long universityId,
                             @Header("Updated-At-Server") String updatedAtServer);

    @POST("/wishes")
    Void postWish(@Body WishProcessor.Wish wish);

    @POST("/errors")
    Void postError(@Body ErrorProcessor.Error error);
}
