package com.foxmike.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.foxmike.android.utils.TextTimestamp;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2019-04-21.
 */

public class ExplorerNavigationAdapter  extends SmartFragmentStatePagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private String today;

    public ExplorerNavigationAdapter(FragmentManager fragmentManager, String today) {
        super(fragmentManager);
        this.today = today;
    }
    // Our custom method that populates this Adapter with Fragments
    public void addFragments(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public CharSequence getPageTitle(int position) {
        Long todayTimestamp = System.currentTimeMillis();
        TextTimestamp textTimestamp;
        Long dayTimestamp;
        String tabText;

        switch (position) {
            case 0:
                return today;
            case 1:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(1).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 2:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(2).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 3:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(3).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 4:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(4).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 5:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(5).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 6:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(6).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 7:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(7).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 8:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(8).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 9:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(9).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 10:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(10).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 11:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(11).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 12:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(12).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            case 13:
                dayTimestamp = new DateTime(todayTimestamp).plusDays(13).getMillis();
                textTimestamp = new TextTimestamp(dayTimestamp);
                tabText = textTimestamp.textDay() + "\n" + textTimestamp.textNumberDay();
                return tabText;
            default:
                return null;
        }
    }
}
