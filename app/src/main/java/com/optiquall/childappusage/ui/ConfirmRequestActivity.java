package com.optiquall.childappusage.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.adapter.RequestAdapter;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.data.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;

public class ConfirmRequestActivity extends AppCompatActivity {

    private RecyclerView mList;

    private LinearLayoutManager linearLayoutManager;
    private DividerItemDecoration dividerItemDecoration;
    private List<Request> requestList;
    private RecyclerView.Adapter adapter;
    private String getAllRequestsUrl = BASE_URL + "/getAllRequests.php";
    private String TAG = "ConfirmRequestActivity";
    private String KEY_CHILD_EMAIL = "childEmail";
    private String childEmail;
    private SwipeRefreshLayout mSwipe;
    private AppPreference appPreference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Parent Request/s");
        }

        initViews();
        onclicks();
    }

    private void onclicks() {
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewData();
            }
        });
    }

    private void initViews() {
        appPreference = new AppPreference(this);

        mList = findViewById(R.id.main_list);
        mSwipe = findViewById(R.id.swipe_refresh);

        childEmail = appPreference.getChildEmail();
        if (childEmail.isEmpty()) {
            childEmail = getIntent().getStringExtra("childEmail");
        }
        Log.e(TAG, "initViews: childEmail: "+childEmail );
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(ConfirmRequestActivity.this, requestList);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(mList.getContext(), linearLayoutManager.getOrientation());

        mList.setHasFixedSize(true);
        mList.setLayoutManager(linearLayoutManager);
        mList.addItemDecoration(new DividerItemDecoration(this, 0));
        mList.setAdapter(adapter);
        getNewData();
    }


    public void getNewData() {
        requestList.clear();
        adapter.notifyDataSetChanged();
        final JSONObject jsonobj = new JSONObject();
        try {
            //Populate the request parameters
            jsonobj.put(KEY_CHILD_EMAIL, childEmail);
            Log.e(TAG, "getNewData: " + jsonobj.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Volley.newRequestQueue(this)
                .add(new JsonRequest<JSONArray>(com.android.volley.Request.Method.POST,
                             getAllRequestsUrl,
                             jsonobj.toString(),
                             new Response.Listener<JSONArray>() {
                                 @Override
                                 public void onResponse(JSONArray jsonArray) {
                                     mSwipe.setRefreshing(false);
                                     Log.e(TAG, "onResponse:  jsonArray" + jsonArray);
                                     if (jsonArray == null) {
                                         Toast.makeText(ConfirmRequestActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                     } else {

                                         for (int i = 0; i < jsonArray.length(); i++) {
                                             try {
                                                 Request request = new Request();
                                                 JSONObject jsonObject = jsonArray.optJSONObject(i);
                                                 request.setTo_child(jsonObject.optString("to_child"));
                                                 request.setFrom_parent(jsonObject.optString("from_parent"));
                                                 request.setReq_status(jsonObject.optString("req_status"));
                                                 requestList.add(request);
                                             } catch (Exception e) {
                                                 e.printStackTrace();
                                             }

                                         }
                                         adapter.notifyDataSetChanged();
                                     }
                                 }
                             }, new Response.ErrorListener() {
                         @Override
                         public void onErrorResponse(VolleyError volleyError) {
                             VolleyLog.d("Login request", "Error: " + volleyError.getMessage());
                             Log.e(TAG, "Volley Error:" + volleyError.getMessage());
                             Toast.makeText(ConfirmRequestActivity.this, "Unable to connect to server, try again later", Toast.LENGTH_LONG).show();
                             mSwipe.setRefreshing(false);
                         }
                     }) {
                         @Override
                         protected Map<String, String> getParams() throws AuthFailureError {


                             Map<String, String> params = new HashMap<String, String>();
                             // params.put("uniquesessiontokenid", "39676161-b890-4d10-8c96-7aa3d9724119");

                             params.put(KEY_CHILD_EMAIL, childEmail);

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

    public void parseData(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            Log.e(TAG, "parseData: " + jsonObject.toString(2));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
/*
    private void getData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_CHILD_EMAIL, childEmail);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (com.android.volley.Request.Method.POST, getAllRequestsUrl, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JsonArrayRequest response) {
                        try {
                            progressDialog.dismiss();
                            Log.e(TAG, "onResponse: " + response.toString(2));

                            JSONObject obj = new JSONObject(response.toString(2));
                            JSONArray jsonArray = new JSONArray();
//simply put obj into jsonArray
                            jsonArray.put(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "onErrorResponse: " + error.toString());
                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);


      */
/*  JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, getAllRequestsUrl, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.e(TAG, "onResponse: ");
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        Request request = new Request();
                        request.setFrom_parent(jsonObject.optString("from_parent"));
                        request.setTo_child(jsonObject.optString("to_child"));
                        request.setTo_child(jsonObject.optString("req_status"));

                        requestList.add(request);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
                Log.e(TAG, "onResponse: size: " + requestList.size());
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                progressDialog.dismiss();
            }


            @Override
            //This Override is showing an error "Method does not override method from its superclass"
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("param1", "one");
                params.put("param2", "two");
                return params;
            }

        }


        );
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);*//*

    }
*/

}
