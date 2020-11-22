package com.netmontools.lookatnet.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.netmontools.lookatnet.ui.local.view.LocalFragment;
import com.netmontools.lookatnet.ui.remote.view.FilesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int CARD_ITEM_SIZE = 2;
    LocalFragment localFragment = new LocalFragment();
    FilesFragment filesFragment = new FilesFragment();

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull @Override public Fragment createFragment(int position) {
        if (position == 0) return localFragment;
        else return filesFragment;
    }

    @Override public int getItemCount() {
        return CARD_ITEM_SIZE;
    }
}
