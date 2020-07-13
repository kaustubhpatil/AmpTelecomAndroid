package com.amptelecom.android.app.network;

import com.amptelecom.android.app.contacts.ContactReponse;
import com.amptelecom.android.app.network.model.LoginData;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    @FormUrlEncoded
    @POST("smartphone2/getContacts/")
    Call<ContactReponse> getContacts(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid);

    @GET
    Call<List<LoginData>> getLoginDetails(@Url String url);
}
