package com.amptelecom.android.app.network;

import com.amptelecom.android.app.chatnew.model.ChatConversationsResponse;
import com.amptelecom.android.app.chatnew.model.ChatMessagesResponse;
import com.amptelecom.android.app.chatnew.model.GeneralResponse;
import com.amptelecom.android.app.contacts.ContactResponse;
import com.amptelecom.android.app.network.model.LoginData;
import com.amptelecom.android.app.network.model.ServerResponse;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    @FormUrlEncoded
    @POST("smartphone2/getContacts/")
    Call<ServerResponse<ContactResponse>> getContacts(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid);

    @GET
    Call<ServerResponse<List<LoginData>>> getLoginDetails(@Url String url);

    @FormUrlEncoded
    @POST("smartphone2/getConversationID/")
    Call<GeneralResponse> createChatRoom(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid,
            @Field("sms_to") String to,
            @Field("cc") String cc);

    @FormUrlEncoded
    @POST("smartphone2/getConversations/")
    Call<ChatConversationsResponse> getConversations(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid);

    @FormUrlEncoded
    @POST("smartphone2/getConversations/")
    Call<ResponseBody> getConversations2(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid);

    @FormUrlEncoded
    @POST("smartphone2/fetchConversationHistory/")
    Call<ChatMessagesResponse> getMessages(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid,
            @Field("last_id") String last_id,
            @Field("last_sent_id") String last_sent_id,
            @Field("conversationid") String conversationid);

    @FormUrlEncoded
    //    @POST("smartphone2/fetchCoversationHistory/")
    @POST("smartphone2/fetchConversationHistory/")
    Call<ResponseBody> getMessages2(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid,
            @Field("last_id") String last_id,
            @Field("last_sent_id") String last_sent_id,
            @Field("conversationid") String conversationid);

    @FormUrlEncoded
    @POST("smartphone2/sendMessage/")
    Call<ChatMessagesResponse> sendMessage(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid,
            @Field("sms_to") String sms_to,
            @Field("cc") String cc,
            @Field("sms_body") String sms_body,
            @Field("attachments") String attachments,
            @Field("conversationid") String conversationid);

    @FormUrlEncoded
    @POST("smartphone2/pushToken")
    Call<ServerResponse> updatePushToken(
            @Field("extension") String extension,
            @Field("realm") String host,
            @Field("pw") String password,
            @Field("installid") String uuid,
            @Field("token") String token);
}
