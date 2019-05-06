package test.kozachenkotest.Api

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import test.kozachenkotest.Models.MainResponse

interface ApiService {

    @GET(URL)
    fun getPlaces(@Query("client_id") id: String,
                         @Query("client_secret") secret: String,
                         @Query("v") v: String,
                         @Query("ll") latLng: String,
                         @Query("query") query: String,
                         @Query("limit") limit: String): Observable<MainResponse>

    companion object Factory {
        const val URL = "https://api.foursquare.com/v2/venues/explore/"
    
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(URL)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }

}
