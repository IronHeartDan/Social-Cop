package com.danapps.social_cop;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

public class ProvideDetails extends Fragment {

    public boolean isSet = false;
    private LinearLayout linearLayout;
    private ImageView thank_you;
    public String desc = null;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_provide_details, container, false);
        AddIssue addIssue = (AddIssue) getActivity();
        linearLayout = view.findViewById(R.id.con_desc);
        thank_you = view.findViewById(R.id.thank_you);
        EditText input_description = view.findViewById(R.id.input_description);
        progressBar = view.findViewById(R.id.progress_circular);


        input_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                desc = s.toString().trim();
            }
        });

        return view;
    }

    public void thankYou() {
        linearLayout.setVisibility(View.GONE);
        thank_you.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }
}