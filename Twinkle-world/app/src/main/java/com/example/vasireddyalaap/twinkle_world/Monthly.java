package com.example.vasireddyalaap.twinkle_world;


import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Monthly extends Fragment {


    private static final String TAG = Monthly.class.getSimpleName();

    UsageStatsManager mUsageStatsManager;
    UsageListAdapter mUsageListAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    Button mOpenUsageSettingButton;
    Spinner mSpinner;
    int n3 = 0;

    public Monthly() {
        // Required empty public constructor
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsageStatsManager = (UsageStatsManager) getActivity()
                .getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monthly, container, false);
    }

    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new UsageListAdapter();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);

        //chart
        BarChart Piechart_Monthly = (BarChart) rootView.findViewById(R.id.chart_Monthly);
        List<BarEntry> entries_monthly = new ArrayList<>();
        //chart

        long TimeInForGround = 0;
        long time = System.currentTimeMillis();
        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();
        List<UsageStats> Stats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, time - (24*60*60*1000), time);

        if (Stats.size() == 0) {
            Log.i(TAG, "The user may not allow the access to apps usage. ");
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }
        else{

        }
        for (UsageStats usageStats : Stats) {
            CustomUsageStats customUsageStats = new CustomUsageStats();
            customUsageStats.usageStats = usageStats;
            TimeInForGround = customUsageStats.usageStats.getTotalTimeInForeground();

            customUsageStats.PackageName = customUsageStats.usageStats.getPackageName();

            customUsageStats.AppUsageTime = (int) ((TimeInForGround / (1000 * 60)));
            PackageManager packageManager = getActivity().getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(customUsageStats.PackageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
            }
            customUsageStats.AppName = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            try {
                Drawable appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                customUsageStats.appIcon = appIcon;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, String.format("App Icon is not found for %s",
                        customUsageStats.usageStats.getPackageName()));
                customUsageStats.appIcon = getActivity()
                        .getDrawable(R.drawable.ic_default_app_launcher);
            }
            customUsageStatsList.add(customUsageStats);
            Collections.sort(customUsageStatsList, new Daily.LastTimeLaunchedComparatorDesc());
            if(n3 < 5){
                entries_monthly.add(new BarEntry((float)n3,(float)customUsageStats.AppUsageTime,customUsageStats.AppName));
                n3++;
            }

        }


        BarDataSet set = new BarDataSet(entries_monthly, "Yearly usage");
        set.setColors(ColorTemplate.getHoloBlue());
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        Piechart_Monthly.setData(data);
        Piechart_Monthly.setFitBars(true); // make the x-axis fit exactly all bars
        Piechart_Monthly.invalidate(); // refresh
        mUsageListAdapter.setCustomUsageStatsList(customUsageStatsList);
        mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);

    }
    private static class LastTimeLaunchedComparatorDesc implements Comparator<CustomUsageStats> {

        @Override
        public int compare(CustomUsageStats left, CustomUsageStats right) {
            return Long.compare(right.AppUsageTime, left.AppUsageTime);
        }
    }


}
