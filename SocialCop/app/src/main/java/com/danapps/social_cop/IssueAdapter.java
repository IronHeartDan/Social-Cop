package com.danapps.social_cop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class IssueAdapter extends FirestoreRecyclerAdapter<MinIssue, IssueAdapter.Holder> {

    public IssueAdapter(@NonNull FirestoreRecyclerOptions<MinIssue> options) {
        super(options);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_1, parent, false);
        } else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_2, parent, false);
        } else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_3, parent, false);

        return new Holder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, int position, @NonNull MinIssue model) {
        holder.issue_locality.setText(model.getLocality());
        Glide.with(holder.issue_img).load(model.getProof()).into(holder.issue_img);
        holder.issue_status.setText(String.valueOf(model.getStatus()) + "%");
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView issue_img;
        TextView issue_locality, issue_status;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.issue_img = itemView.findViewById(R.id.issue_img);
            this.issue_locality = itemView.findViewById(R.id.issue_locality);
            this.issue_status = itemView.findViewById(R.id.issue_status);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getStatus() > 50) {
            return 1;
        } else if (getItem(position).getStatus() < 50) {
            return 2;
        } else
            return 3;
    }
}