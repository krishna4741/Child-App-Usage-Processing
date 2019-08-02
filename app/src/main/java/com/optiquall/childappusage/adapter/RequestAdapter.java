package com.optiquall.childappusage.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.MySingleton;
import com.optiquall.childappusage.data.Request;
import com.optiquall.childappusage.ui.ConfirmRequestActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private static final String KEY_ACCEPTANCE_STATUS = "req_status";
    private static final String KEY_PARENT_EMAIL = "from_parent";
    private static final String KEY_CHILD_EMAIL = "to_child";
    private static final String acceptance_status_url = BASE_URL + "/acceptance_status_url.php";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private Context context;
    private List<Request> list;
    private String TAG = "RequestAdapter";

    public RequestAdapter(Context context, List<Request> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Request request = list.get(position);
        Log.e(TAG, "onBindViewHolder: " + request.getReq_status());

        holder.from_parent.setText(request.getFrom_parent());
        holder.to_child.setText(String.valueOf(request.getTo_child()));
        if (request.getReq_status().equals("0")) {

            holder.reject_button.setVisibility(View.VISIBLE);
            holder.accept_button.setVisibility(View.VISIBLE);

        } else if (request.getReq_status().equals("1")) {


            holder.reject_button.setVisibility(View.GONE);
            holder.accept_button.setText("Accepted");
            holder.accept_button.setVisibility(View.VISIBLE);

        } else if (request.getReq_status().equals("2")) {

            holder.accept_button.setVisibility(View.GONE);
            holder.reject_button.setText("Rejected");
            holder.reject_button.setVisibility(View.VISIBLE);
        }
        holder.accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "accept_button: onClick: ");
                String status = "1";//for acceptance
                sendAcceptance(request.getFrom_parent(), request.getTo_child(), status);
                ((ConfirmRequestActivity) context).getNewData();
            }
        });

    }

    private void sendAcceptance(String fromParent, String to_child, String status) {

        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_PARENT_EMAIL, fromParent);
            request.put(KEY_CHILD_EMAIL, to_child);
            request.put(KEY_ACCEPTANCE_STATUS, status);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Log.e(TAG, "sendAcceptance: " + request.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.POST, acceptance_status_url, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.getInt(KEY_STATUS) == 0) {
                                //Set the user session
                                Toast.makeText(context, response.optString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();

                            } else if (response.getInt(KEY_STATUS) == 1) {


                            } else {
                                Toast.makeText(context,
                                        response.getString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();

                            }


                            Log.e(TAG, "onResponse: " + response.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: " + error.toString());

                        //Display error message whenever an error occurs
                        Toast.makeText(context,
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(context).addToRequestQueue(jsArrayRequest);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView from_parent, to_child;
        Button accept_button, reject_button;


        public ViewHolder(View itemView) {
            super(itemView);

            from_parent = itemView.findViewById(R.id.from_parent);

            to_child = itemView.findViewById(R.id.to_child);
            accept_button = itemView.findViewById(R.id.accept_button);
            reject_button = itemView.findViewById(R.id.reject_button);

        }
    }


}
