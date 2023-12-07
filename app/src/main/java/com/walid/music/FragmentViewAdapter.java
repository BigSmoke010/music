package com.walid.music;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentViewAdapter extends FragmentStateAdapter {
    private String[] names = {"Home", "Albums"};

    public FragmentViewAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Home();
            case 1:
                return new Albums();
        }
        return new Home();
    }

    @Override
    public int getItemCount() {
        return names.length;
    }
}