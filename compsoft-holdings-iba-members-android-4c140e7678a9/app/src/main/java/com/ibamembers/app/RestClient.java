package com.ibamembers.app;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ibamembers.BuildConfig;
import com.ibamembers.R;
import com.ibamembers.app.gcm.job.DevicePushTokenModel;
import com.ibamembers.conference.event.job.ConferenceBuildingEventResponse;
import com.ibamembers.conference.event.job.ConferenceResponse;
import com.ibamembers.content.job.ContentLibraryModel;
import com.ibamembers.login.LoginResponse;
import com.ibamembers.messages.job.AllMessagesModel;
import com.ibamembers.messages.job.MessageStatusDeleted;
import com.ibamembers.messages.job.MessageStatusRead;
import com.ibamembers.messages.job.MessageStatusReceived;
import com.ibamembers.profile.job.ProfileModel;
import com.ibamembers.profile.job.RefreshResponse;
import com.ibamembers.profile.job.SetBiographyJob;
import com.ibamembers.profile.message.job.GetMessageConferenceResponse;
import com.ibamembers.profile.message.job.GetMessageResponse;
import com.ibamembers.profile.message.job.HideMessageConferenceRequest;
import com.ibamembers.profile.message.job.SendMessageRequest;
import com.ibamembers.profile.message.job.SendMessageResponse;
import com.ibamembers.search.job.ProfileSnippetModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public class RestClient {
    private ApiService apiService;

    private final String DATE_HEADER_KEY = "Date";
    private final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private final String XAUTH_HEADER_KEY = "X-Auth";
    private final String SESSION_TOKEN_HEADER_KEY = "UserKey";
    private final String APP_VERSION = "AppVersion";
    private final String CACHE_CONTROL = "Cache-Control";

//    public static final String PREFIX_PATH = "IBAMembersApp/api/v2"; //local url prefix
    public static final String PREFIX_PATH = "api/v2";                 //live url prefix

    public static final String LOGIN_PATH = PREFIX_PATH + "/login";
    public static final String GET_PROFILE_PATH = PREFIX_PATH + "/profile";
    public static final String SEARCH_PATH = PREFIX_PATH + "/profile";
    public static final String REFRESH_PATH = PREFIX_PATH +  "/refresh";
    public static final String UPDATE_PROFILE_PICTURE_PATH = PREFIX_PATH +  "/profileimage";
    public static final String MAKE_PROFILE_PUBLIC_PATH = PREFIX_PATH + "/MakeProfilePublic";
    public static final String SET_BIOGRAPHY_PATH = PREFIX_PATH +  "/Profile";
    public static final String MESSAGES_PATH = PREFIX_PATH +  "/message";
    public static final String GET_CONTENT_LIBRARY = PREFIX_PATH + "/contentlibrary";
    public static final String DEVICE_PUSH = PREFIX_PATH + "/device";
    public static final String CONFERENCE_PATH = PREFIX_PATH + "/Conference";
    public static final String CONFERENCE_EVENT_PATH = PREFIX_PATH + "/Conference/{conferenceId}/buildingevents";
    public static final String MESSAGE_PATH = PREFIX_PATH + "/P2P";
    public static final String MESSAGE_CONNECTIONS_PATH = PREFIX_PATH + "/P2PConnections";
    public static final String MESSAGE_READ_PATH = PREFIX_PATH + "/P2P/{messageId}/read";

    private App app;
    private String XAuth;

    public RestClient(App app) {
        this.app = app;
    }

    public RestClient(App app, String XAuth) {
        this.app = app;
        this.XAuth = XAuth;
    }

    public ApiService getApiService() {
        return getApiService(null);
    }

    public ApiService getApiService(String authorization) {
        return createAPIService(authorization);
    }

    public ApiService createAPIService(final String authorization) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .create();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String date = getDateHeader(app);
                Request.Builder builder = request.newBuilder();

                builder.addHeader(DATE_HEADER_KEY, date);
                builder.addHeader(CACHE_CONTROL, "no-cache");
                builder.addHeader("Accept-Encoding", "identity");

                if (authorization != null) {
                    //Used for login only
                    builder.addHeader(AUTHORIZATION_HEADER_KEY, authorization);
                } else {
                    //used for every other calls
                    builder.addHeader(SESSION_TOKEN_HEADER_KEY, getSessionToken(app));
                    builder.addHeader(APP_VERSION, app.getString(R.string.app_version));

                }

                if (XAuth == null) {
                    String path = chain.request().url().encodedPath();
                    String apiKey = app.getServiceApiKey();
                    XAuth = getXAuth(path, date, RestClient.getAuthorizationHeader(null, null, getSessionToken(app)), apiKey);
                }

                builder.addHeader(XAUTH_HEADER_KEY, XAuth);

                request = builder
                        .method(request.method(), request.body())
                        .build();

                return chain.proceed(request);
            }
        });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(httpLoggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(app.getResources().getString(R.string.rest_client_api_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        return retrofit.create(ApiService.class);
    }



    public static String getAuthorizationHeader(@Nullable String username, @Nullable String password, @Nullable String sessionToken) {
        StringBuilder builder = new StringBuilder();

        String authPath;
        if (sessionToken != null) {
            authPath = sessionToken;
        }
        else {
            authPath = username + ":" + password;
            authPath = new String(Base64.encode(authPath.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
        }

        String auth = authPath;

        if (sessionToken != null) {
            builder.append("UserKey ");
        }
        else {
            builder.append("Basic ");
        }

        builder.append(auth);
        return builder.toString();
    }

    public static String getXAuth(String path, String date, String authentication, String apiKey) {
        try {
            String request = path + " " + date + " " + authentication;

            byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
            SecretKey signingKey = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HMACSHA256");
            Mac mac = Mac.getInstance("HMACSHA256");
            mac.init(signingKey);
            byte[] digest = mac.doFinal(requestBytes);

            return Base64.encodeToString(digest, Base64.NO_WRAP);
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            Log.e("BaseRestClient", e.getMessage());
        }

        return null;
    }

    public static String getDateHeader(App app) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(app.getApplicationContext().getString(R.string.date_formats_api_date), Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = dateFormat.format(cal.getTime());
        StringBuilder builder = new StringBuilder();
        builder.append(date);
        builder.append(" GMT");
        return builder.toString();
    }

    public static String getSessionToken(App app) {
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                return settingDao.getSessionToken();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static class HttpException extends Exception {

        private final int statusCode;

        public HttpException(int statusCode) {
            this.statusCode = statusCode;
        }

        public HttpException(int statusCode, Throwable cause) {
            super(cause);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean is400ClientError() {
            return statusCode >= 400 && statusCode < 500;
        }
    }

    private static class DateDeserializer implements JsonDeserializer<Date> {

        private final SimpleDateFormat dateTimeFormat;
        private final SimpleDateFormat dateFormat;

        private DateDeserializer() {
            dateTimeFormat = createFormatter("yyyy-MM-dd\'T\'HH:mm:ss");
            dateFormat = createFormatter("yyyy-MM-dd");
        }

        private SimpleDateFormat createFormatter(String format) {
            SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.UK);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter;
        }

        @Override
        public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
            String date = element.getAsString();
            try {
                return dateTimeFormat.parse(date);
            } catch (ParseException e) {
                try {
                    return dateFormat.parse(date);
                } catch (ParseException e2) {
                    Log.e("DateDeserializer", "Failed to parse date: " + date);
                    return null;
                }
            }
        }
    }

    public interface ApiService {

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(LOGIN_PATH)
        Call<LoginResponse> login();

        @Headers({
                "Accept: application/json",
				"Content-Type: application/json"})
        @GET(GET_PROFILE_PATH)
        Call<ProfileModel> getProfile(@Query("userId") int userId);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(REFRESH_PATH)
        Call<RefreshResponse> refresh();

        @Multipart
        @Headers({
                "Accept: application/json",
                "Content-CategoryType: application/json"})
        @PUT(UPDATE_PROFILE_PICTURE_PATH)
        Call<BaseResponse> uploadProfilePicture(@Part MultipartBody.Part filePart);

        @Headers({
                "Accept: application/json",
				"Content-Type: application/json"})
        @PUT(MAKE_PROFILE_PUBLIC_PATH)
        Call<BaseResponse> makeProfilePublic();

        @Headers({
                "Accept: application/json",
				"Content-Type: application/json"})
        @PUT(SET_BIOGRAPHY_PATH)
        Call<Void> setBiography(@Body SetBiographyJob.BiographyRequest biographyRequest);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(SEARCH_PATH)
        Call<ProfileSnippetModel[]> search(@QueryMap Map<String, String> searchMap, @Query("skip") int skip, @Query("take") int take);

        /**
         * MESSAGES
         */
        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(MESSAGES_PATH)
        Call<AllMessagesModel> getAllMessage(@Query("start") int start, @Query("length") int length);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(MESSAGES_PATH)
        Call<AllMessagesModel> getSingleMessage(@Query("AppUserMessageId") int appUserMessageId);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(MESSAGES_PATH)
        Call<AllMessagesModel> setMessageStatusReceived(@Body MessageStatusReceived messageStatusReceived);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(MESSAGES_PATH)
        Call<AllMessagesModel> setMessageStatusRead(@Body MessageStatusRead messageStatusRead);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(MESSAGES_PATH)
        Call<Void> setMessageStatusDeleted(@Body MessageStatusDeleted messageStatusDeleted);


        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @POST(MESSAGE_PATH)
        Call<SendMessageResponse> sendMessage(@Body SendMessageRequest sendMessageRequest);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(MESSAGE_PATH)
        Call<GetMessageResponse> getMessage(@Query("id") int userId, @Query("skip") int skip, @Query("take") int take);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(MESSAGE_CONNECTIONS_PATH)
        Call<List<GetMessageConferenceResponse>> getMessageConnections();

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @POST(MESSAGE_READ_PATH)
        Call<Boolean> setProfileMessageRead(@Path("messageId") int messageId);

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(MESSAGE_PATH)
        Call<Boolean> setProfileMessageAsHidden(@Body HideMessageConferenceRequest request);

        /**
         * CONTENT
         */
        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(GET_CONTENT_LIBRARY)
        Call<ContentLibraryModel> getContentLibrary(@Query("start") int start, @Query("length") int length);

        /**
         * DEVICE
         */
        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @PUT(DEVICE_PUSH)
        Call<Void> setDevicePushToken(@Body DevicePushTokenModel devicePushTokenModel);

        /**
         * CONFERENCE EVENTS
         */

        @Headers({
                "Accept: application/json",
                "Content-Type: application/json"})
        @GET(CONFERENCE_PATH)
        Call<ConferenceResponse> getConference(@Query("id") int conferenceId);

        @Headers({
                "Accept: application/json",
                "Content-CategoryType: application/json"})
        @GET(CONFERENCE_EVENT_PATH+"?take=1000")
        Call<ConferenceBuildingEventResponse> getConferenceEvents(@Path("conferenceId") int conferenceId);
    }
}
