package dh.mygrades.main.rest;

import java.util.List;

import dh.mygrades.database.dao.University;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Interface which defines all REST endpoints.
 */
public interface RestApi {

    @GET("/universities.json")
    List<University> getUniversities(@Query("published") boolean publishedOnly,
                                     @Header("Updated-At-Server-Published") String updatedAtServerPublished,
                                     @Header("Updated-At-Server-Unpublished") String updatedAtServerUnpublished);

    @GET("/universities/{university_id}.json")
    University getUniversity(@Path("university_id") long universityId,
                             @Header("Updated-At-Server") String updatedAtServer);
}
