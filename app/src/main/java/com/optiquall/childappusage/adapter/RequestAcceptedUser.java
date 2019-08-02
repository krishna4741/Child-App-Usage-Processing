package com.optiquall.childappusage.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.optiquall.childappusage.R;
import com.optiquall.childappusage.ui.ParentDashActivity;

import java.util.List;

public class RequestAcceptedUser extends RecyclerView.Adapter<RequestAcceptedUser.ViewHolder> {
    List<String> list;
    Context context;
    String TAG = "RequestAcceptedUser";

    public RequestAcceptedUser(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_child_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final String childEmail = list.get(position);
        holder.txt_child_email.setText(childEmail);
        holder.view_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: view detail pressed ");
                Intent viewDetailsActivity = new Intent(context, ParentDashActivity.class);
                viewDetailsActivity.putExtra("email", childEmail);
                context.startActivity(viewDetailsActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_child_email;
        Button view_details;

        public ViewHolder(View itemView) {
            super(itemView);
            view_details = itemView.findViewById(R.id.view_details);
            txt_child_email = itemView.findViewById(R.id.txt_child_email);
        }
    }


}
