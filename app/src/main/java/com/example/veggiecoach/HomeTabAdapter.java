package com.example.veggiecoach;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeTabAdapter extends FragmentStateAdapter {

    public HomeTabAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return (position == 0) ? TodoListFragment.newInstance() : DiaryFragment.newInstance();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
