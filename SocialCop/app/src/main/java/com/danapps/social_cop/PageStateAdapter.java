package com.danapps.social_cop;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PageStateAdapter extends FragmentStateAdapter {
    public PageStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new ProvideLocation();
        } else if (position == 1) {
            fragment = new ProvideProof();
        } else {
            fragment = new ProvideDetails();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
