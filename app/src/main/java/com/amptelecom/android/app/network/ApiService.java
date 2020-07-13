package com.amptelecom.android.app.network;

import com.amptelecom.android.app.network.model.Contacts;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("/smartphone2/getContacts")
    Call<List<Contacts>> getContacts(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid);
}
