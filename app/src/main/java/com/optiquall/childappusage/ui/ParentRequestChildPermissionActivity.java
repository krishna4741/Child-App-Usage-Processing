package com.optiquall.childappusage.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.adapter.RequestAcceptedUser;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.app.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;

public class ParentRequestChildPermissionActivity extends AppCompatActivity {

    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String PARENT_EMAIL = "parentEmail";
    private static final String CHILD_EMAIL = "childEmail";
    private static final String KEY_EMPTY = "";
    private String parentEmail, childEmail;
    private TextInputEditText editChildEmail;
    private TextInputLayout p_editChildEmail;
    private ImageView search_email;
    private TextView txt_email_found;
    private Button buttonRequest;
    private ProgressDialog pDialog;
    private String sendNotificationUrl = BASE_URL + "/sendNotification.php";
    private String TAG = "ParentRequestChildPermissionActivity";
    private AppPreference appPreference;
    private RecyclerView mList;
    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<String> childList;
    private RecyclerView.Adapter adapter;
    private String KEY_PARENT_EMAIL = "parentEmail";
    private String getAcceptedRequestsUrl = BASE_URL + "/requestAcceptedUsers.php";
    private SwipeRefreshLayout mSwipe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_request_child_permission_layout);
        ActionBar actionBar = getSupportActionBar();
        appPreference = new AppPreference(this);
        if (actionBar != null) {
            actionBar.setTitle("Usage Access permission");
            try {
                parentEmail = appPreference.getParentEmail();
                Log.e(TAG, "onCreate: parentEmail " + parentEmail);
                actionBar.setSubtitle(parentEmail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initViews();
        onClicks();
    }


    private void initViews() {
        editChildEmail = findViewById(R.id.editChildEmail);
        p_editChildEmail = findViewById(R.id.p_editChildEmail);
        search_email = findViewById(R.id.search_email);
        txt_email_found = findViewById(R.id.txt_email_found);
        buttonRequest = findViewById(R.id.buttonRequest);
        mList = findViewById(R.id.acceptedChildRecyclerview);
        mSwipe = findViewById(R.id.swipe_refresh);

        childList = new ArrayList<>();
        adapter = new RequestAcceptedUser(ParentRequestChildPermissionActivity.this, childList);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());

        //  mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        // mList.addItemDecoration(new DividerItemDecoration(this, 0));
        mList.setAdapter(adapter);

        getNewData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.parent_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {

          PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
            Bundle bundle = new Bundle();
            Intent intent = new Intent(ParentRequestChildPermissionActivity.this, LoginTabActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            bundle.putString("TabNumber", "1");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
//            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public void getNewData() {
        childList.clear();
        adapter.notifyDataSetChanged();
        final JSONObject jsonobj = new JSONObject();
        try {
            //Populate the request parameters
            jsonobj.put(KEY_PARENT_EMAIL, parentEmail);
            Log.e(TAG, "onCreate: parentEmail " + parentEmail);
            Log.e(TAG, "getAcceptedURL: " + jsonobj.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Volley.newRequestQueue(this)
                .add(new JsonRequest<JSONArray>(com.android.volley.Request.Method.POST,
                             getAcceptedRequestsUrl,
                             jsonobj.toString(),
                             new Response.Listener<JSONArray>() {
                                 @Override
                                 public void onResponse(JSONArray jsonArray) {

                                     Log.e(TAG, "onResponse:  jsonArray" + jsonArray);
                                     if (jsonArray == null) {
                                         Toast.makeText(ParentRequestChildPermissionActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                     } else {

                                         for (int i = 0; i < jsonArray.length(); i++) {
                                             try {
                                                 JSONObject jsonObject = jsonArray.optJSONObject(i);
                                                 childList.add((jsonObject.optString("to_child")));
                                             } catch (Exception e) {
                                                 e.printStackTrace();
                                             }

                                         }
                                         adapter.notifyDataSetChanged();
                                     }
                                     mSwipe.setRefreshing(false);
                                 }
                             }, new Response.ErrorListener() {
                         @Override
                         public void onErrorResponse(VolleyError volleyError) {
                             VolleyLog.d("Login request", "Error: " + volleyError.getMessage());
                             Log.e(TAG, "Volley Error:" + volleyError.getMessage());
                             //Toast.makeText(ParentRequestChildPermissionActivity.this, "Unable to connect to server, try again later", Toast.LENGTH_LONG).show();
                             mSwipe.setRefreshing(false);
                         }
                     }) {
                         @Override
                         protected Map<String, String> getParams() throws AuthFailureError {


                             Map<String, String> params = new HashMap<String, String>();
                             // params.put("uniquesessiontokenid", "39676161-b890-4d10-8c96-7aa3d9724119");

                             params.put(KEY_PARENT_EMAIL, parentEmail);

                             return super.getParams();
                         }

                         @Override
                         public String getBodyContentType() {
                             return "application/json";
                         }

                         @Override
                         protected Response<JSONArray> parseNetworkResponse(NetworkResponse networkResponse) {


                             try {
                                 String jsonString = new String(networkResponse.data,
                                         HttpHeaderParser
                                                 .parseCharset(networkResponse.headers));
                                 return Response.success(new JSONArray(jsonString),
                                         HttpHeaderParser
                                                 .parseCacheHeaders(networkResponse));
                             } catch (UnsupportedEncodingException e) {
                                 return Response.error(new ParseError(e));
                             } catch (JSONException je) {
                                 return Response.error(new ParseError(je));
                             }

                             //  return null;
                         }
                     }
                );

    }


    private void onClicks() {
        editChildEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search_email.performClick();
                    return true;
                }
                return false;
            }
        });

        search_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                childEmail = editChildEmail.getText().toString().trim();
                if (validEmail(childEmail)) {
                    sendRequestToChild();
                } else {
                    //email error here
                    p_editChildEmail.setError("Enter valid email.");
                    editChildEmail.requestFocus();
                }
            }

        });
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_email.performClick();
            }
        });
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewData();
            }
        });

    }


    private boolean validEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void displayLoader() {
        pDialog = new ProgressDialog(ParentRequestChildPermissionActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void sendRequestToChild() {
        displayLoader();
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(CHILD_EMAIL, childEmail);
            request.put(PARENT_EMAIL, parentEmail);
            Log.e(TAG, "sendRequestToChild: " + request.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, sendNotificationUrl, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        try {
                            //Check if user got registered successfully
                            if (response.getInt(KEY_STATUS) == 0) {
                                //Set the user session
                                txt_email_found.setVisibility(View.VISIBLE);

                            } else if (response.getInt(KEY_STATUS) == 1) {
                                //Display error message if username is already existsing
                                txt_email_found.setVisibility(View.VISIBLE);
                                txt_email_found.setText(response.optString(KEY_MESSAGE));
                                txt_email_found.setTextColor(Color.parseColor("#FF6347"));
                                editChildEmail.requestFocus();

                            } else if (response.getInt(KEY_STATUS) == 2) {
                                //Display error message if username is already existsing
                                txt_email_found.setVisibility(View.VISIBLE);
                                txt_email_found.setText(response.optString(KEY_MESSAGE));
                                txt_email_found.setTextColor(Color.parseColor("#FF6347"));
                                editChildEmail.requestFocus();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();

                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity();
        } else {
            this.finish();
            System.exit(0);
        }
    }

}
