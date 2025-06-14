package com.example.veggiecoach;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return DiaryFragment.newInstance();  // 영농일지 프래그먼트 (나중에 구현)
            case 1:
            default:
                return TodoListFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
