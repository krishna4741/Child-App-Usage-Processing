package com.optiquall.childappusage.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;


public class AppPreference extends AppCompatActivity {

    //keys
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    private final static String PREF_REFRESH_IMAGEURL = "get_imageurl";
    public static SharedPreferences sharedPreferences;
    private static String TRIP_TITLE = "trip_title";
    private static String CHILD_EMAIL = "child_email";
    private static String PARENT_EMAIL = "parent_email";
    private final String PREF_EMAIL = "e_mail";
    private final String PASSWORD = "password";
    private final String PREF_USER = "user_name";
    private final String FINGERPRINTENABLED = "false";
    private final String CLIENT_ID = "CLIENT_ID";
    private final String PREF_ENCODE = "encode_data";
    private final String PREF_ACCESS_TOKEN = "access_token";
    private final String PREF_REFRESH_TOKEN = "refresh_token";
    private String CONTACT_LIST = "contact_list";
    private String PREF_EMAIL_REMEMBER = "e_mail_remember";
    private String FCM_TOKEN = "fcm_token";
    private String NOTI_CHECKUSERID = "checknotification";
    private String NOTI_USERID = "notifctitle";
    private String NOTI_DESTINATION = "message";
    private String NOTI_NOTECTIONID = "notificationid";
    private String HOME_ADDRESS = "home_address";
    private String NOTIF_COUNT = "noti_count";
    private String SELECT_ITEM = "seleted_item";
    private String SELECT_ITEM_NAME = "seleted_item_name";
    private String CLIENT_SECRET = "client_secret";
    private String DEST_SHORT_NAME = "dest_short_name";
    private String DEST_COMP_NAME = "dest_comp_name";
    private String expired_requestName = "expired_requestName";
    private String expired_webserviceUrl = "expired_webserviceUrl";
    private String s_expired_requestParams = "s_expired_requestParams";
    private String expired_webMethod = "expired_webMethod";
    private String expired_getCache = "is_expired_getCache";
    private String isUserLoggedIn = "isUserLoggedIn";
    private String fb_idToken = "fb_idToken";
    private String google_idToken = "google_idToken";
    private String login_from = "login_location";
    private String LOCATION_UPDATE_FLAG = "true";

    public AppPreference(Context mContext) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getParentEmail() {

        return sharedPreferences.getString(PARENT_EMAIL, "");
    }

    public void setParentEmail(String parentEmail) {
        sharedPreferences.edit().putString(PARENT_EMAIL, parentEmail).apply();
    }

    public String getChildEmail() {

        return sharedPreferences.getString(CHILD_EMAIL, "");
    }

    public void setChildEmail(String childEmail) {

        sharedPreferences.edit().putString(CHILD_EMAIL, childEmail).apply();
    }

    public String getLocation_update_flag() {

        return sharedPreferences.getString(LOCATION_UPDATE_FLAG, "");
    }

    public void setLocation_update_flag(String location_update_flag) {
        sharedPreferences.edit().putString(LOCATION_UPDATE_FLAG, location_update_flag).apply();
    }

    public String getTripTitle() {
        return sharedPreferences.getString(TRIP_TITLE, "");
    }

    public void setTripTitle(String tripTitle) {

        sharedPreferences.edit().putString(TRIP_TITLE, tripTitle).apply();
    }

    public String getDEST_SHORT_NAME() {

        return sharedPreferences.getString(DEST_SHORT_NAME, "");
    }

    public void setDEST_SHORT_NAME(String dest_short_name) {

        sharedPreferences.edit().putString(DEST_SHORT_NAME, dest_short_name).apply();
    }

    public String getDEST_COMP_NAME() {

        return sharedPreferences.getString(DEST_COMP_NAME, "");
    }

    public void setDEST_COMP_NAME(String dest_comp_name) {
        sharedPreferences.edit().putString(DEST_COMP_NAME, dest_comp_name).apply();
    }

    public String getLogin_from() {

        return sharedPreferences.getString(login_from, "");
    }

    public void setLogin_from(String login_location) {
        sharedPreferences.edit().putString(login_from, login_location).apply();

    }

    public String getIsUserLoggedIn() {
        return sharedPreferences.getString(isUserLoggedIn, "");
    }

    public void setIsUserLoggedIn(String status) {
        sharedPreferences.edit().putString(isUserLoggedIn, status).apply();
    }

    public String getFb_idToken() {

        return sharedPreferences.getString(fb_idToken, "");
    }

    public void setFb_idToken(String fToken) {

        sharedPreferences.edit().putString(fb_idToken, fToken).apply();
    }

    public String getGoogle_idToken() {
        return sharedPreferences.getString(google_idToken, "");

    }


//    public String getExpired_requestName() {
//        return sharedPreferences.getString(expired_requestName, "");
//    }
//
//    public void setExpired_requestName(String reqname) {
//        sharedPreferences.edit().putString(expired_requestName, reqname).apply();
//    }
//    public String getexpired_webserviceUrl() {
//        return sharedPreferences.getString(expired_webserviceUrl, "");
//    }
//
//    public void setexpired_webserviceUrl(String webUrl) {
//        sharedPreferences.edit().putString(expired_webserviceUrl, reqname).apply();
//    }

    public void setGoogle_idToken(String gidToken) {

        sharedPreferences.edit().putString(google_idToken, gidToken).apply();
    }

    public String getFINGERPRINTENABLED() {
        return sharedPreferences.getString(FINGERPRINTENABLED, "");
    }

    public void setFINGERPRINTENABLED(String status) {
        sharedPreferences.edit().putString(FINGERPRINTENABLED, status).apply();

    }

    public String getClientId() {
        return sharedPreferences.getString(CLIENT_ID, "");
    }

    public void setClientId(String clientId) {
        sharedPreferences.edit().putString(CLIENT_ID, clientId).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(PREF_EMAIL, "");
    }


    public void setEmail(String email) {
        sharedPreferences.edit().putString(PREF_EMAIL, email).apply();

    }

    public String getUserFullName() {
        return sharedPreferences.getString(PREF_USER, "");
    }

    public void setUserFullName(String username) {
        sharedPreferences.edit().putString(PREF_USER, username).apply();
    }


    public String getPassword() {
        return sharedPreferences.getString(PASSWORD, "");
    }

    public void setPassword(String password) {
        sharedPreferences.edit().putString(PASSWORD, password).apply();
    }

    public boolean isLoggedIn() {
//To retrieve the status of login status.
        return sharedPreferences.getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean status) {
//To store the status of login status.
        sharedPreferences.edit().putBoolean(PREF_IS_LOGGED_IN, status).apply();
    }

    public void setLogOut() {
//To store the status of login status.
// false for setting logged out
        sharedPreferences.edit().putBoolean(PREF_IS_LOGGED_IN, false).apply();
        setEmail(null);
        setPassword(null);

    }

    public String getEncodedata() {
        return sharedPreferences.getString(PREF_ENCODE, "");
    }

    public void setEncodedata(String s) {
        sharedPreferences.edit().putString(PREF_ENCODE, s).apply();
    }


    public int getSELECT_ITEM() {
        return sharedPreferences.getInt(SELECT_ITEM, 0);
    }

    public void setSELECT_ITEM(int seleted) {
        sharedPreferences.edit().putInt(SELECT_ITEM, seleted).apply();
    }

    public String getSELECT_ITEM_NAME() {
        return sharedPreferences.getString(SELECT_ITEM_NAME, "");
    }

    public void setSELECT_ITEM_NAME(String seleteditemname) {
        sharedPreferences.edit().putString(SELECT_ITEM_NAME, seleteditemname).apply();
    }


   /* public int getNOTIF_COUNT() {
        return sharedPreferences.getInt(NOTIF_COUNT, 0);
    }

    public void setNOTIF_COUNT(int notif_count) {
        sharedPreferences.edit().putInt(NOTIF_COUNT, notif_count).apply();
    }*/


    public String get_access_token() {
        return sharedPreferences.getString(PREF_ACCESS_TOKEN, "");
    }

    public void set_access_token(String access_token) {
        sharedPreferences.edit().putString(PREF_ACCESS_TOKEN, access_token).apply();

    }

    public String get_refresh_token() {
        return sharedPreferences.getString(PREF_REFRESH_TOKEN, "");

    }

    public void set_refresh_token(String refresh_token) {
        sharedPreferences.edit().putString(PREF_REFRESH_TOKEN, refresh_token).apply();

    }


    public void setimageurl(String imageUrl) {
        sharedPreferences.edit().putString(PREF_REFRESH_IMAGEURL, imageUrl).apply();
    }

    public String getImgeUrl() {
        return sharedPreferences.getString(PREF_REFRESH_IMAGEURL, "");
    }


    public String getcontactlist() {
        {
            return sharedPreferences.getString(CONTACT_LIST, "");
        }
    }

    public void setRemembermeEmail(String s) {
        sharedPreferences.edit().putString(PREF_EMAIL_REMEMBER, s).apply();

    }

    public String getEmailRememberme() {
        return sharedPreferences.getString(PREF_EMAIL_REMEMBER, "");
    }

    public void setFCMToken(String token) {
        sharedPreferences.edit().putString(FCM_TOKEN, token).apply();

    }

    public String getFCMTOKEN() {
        return sharedPreferences.getString(FCM_TOKEN, "");
    }


    public String getNOTI_CHECKUSERID() {
        return sharedPreferences.getString(NOTI_CHECKUSERID, "");
    }

    public void setNOTI_CHECKUSERID(String notificationcheck) {
        sharedPreferences.edit().putString(NOTI_CHECKUSERID, notificationcheck).apply();
    }

    public String getNOTEUSERID() {
        return sharedPreferences.getString(NOTI_USERID, "");
    }

    public void setNOTEUSERID(String notiuserid) {
        sharedPreferences.edit().putString(NOTI_USERID, notiuserid).apply();

    }

    public String getNOTI_NOTECTIONID() {
        return sharedPreferences.getString(NOTI_NOTECTIONID, "");
    }

    public void setNOTI_NOTECTIONID(String notificationid) {
        sharedPreferences.edit().putString(NOTI_NOTECTIONID, notificationid).apply();
    }

    public String getNOTI_DESTINATION() {
        return sharedPreferences.getString(NOTI_DESTINATION, "");
    }

    public void setNOTI_DESTINATION(String notedestination) {
        sharedPreferences.edit().putString(NOTI_DESTINATION, notedestination).apply();

    }


    public String getContactList(Context mContext) {
        return sharedPreferences.getString(CONTACT_LIST, "");
    }

    public void setHomeAddress(String optString) {
        sharedPreferences.edit().putString(HOME_ADDRESS, optString).apply();

    }

    public String get_HomeAddress() {
        return sharedPreferences.getString(HOME_ADDRESS, "");

    }

    public int getNOTIF_COUNT() {
        return sharedPreferences.getInt(NOTIF_COUNT, 0);
    }

    public void setNOTIF_COUNT(int notif_count) {
        sharedPreferences.edit().putInt(NOTIF_COUNT, notif_count).apply();
    }

    public String getclient_secret() {
        return sharedPreferences.getString(CLIENT_SECRET, "");
    }

    public void setclient_secret(String clientId) {
        sharedPreferences.edit().putString(CLIENT_SECRET, clientId).apply();
    }


}