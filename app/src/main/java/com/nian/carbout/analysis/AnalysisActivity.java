package com.nian.carbout.analysis;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.nian.carbout.DataBaseHelper;
import com.nian.carbout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    private ArrayList<co2_item> data = new ArrayList<>();
    private PieChart myPieChart;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_analysis);

        getSupportActionBar().hide();

        Toolbar toolbar = findViewById(R.id.toolbarAnalysis);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        setPieChart();
        setData();
        setRecycleView();
        setSwitch();
    }


    public void setSwitch()
    {
        tv = findViewById(R.id.chart_detail);
        SwitchCompat sw = findViewById(R.id.switch_analysis);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    myPieChart.setVisibility(View.VISIBLE);
                    tv.setText("?????????????????????");
                }
                else
                {
                    myPieChart.setVisibility(View.GONE);
                    tv.setText("?????????????????????");
                }
            }
        });
    }



    public void setPieChart()
    {
        myPieChart = findViewById(R.id.MyPieChart);
        List<PieEntry> entries = new ArrayList<>();


        entries.add(new PieEntry(24f, "?????????"));
        entries.add(new PieEntry(13.3f, "??????"));
        entries.add(new PieEntry(26.8f, "??????"));
        entries.add(new PieEntry(16.5f, "??????"));
        entries.add(new PieEntry(19.4f, "?????????"));

        PieDataSet set = new PieDataSet(entries, "?????????????????????");
        set.setColors(Color.rgb(100, 186, 228),
                Color.rgb(182, 217, 94),
                Color.rgb(255, 135, 127),
                Color.rgb(247, 195, 106),
                Color.rgb(203, 171 , 255));

        set.setValueTextSize(20f);
        set.setValueTextColor(Color.rgb(255, 255, 255));
        myPieChart.getLegend().setEnabled(false);
        myPieChart.setDescription(null);


        PieData data = new PieData(set);
        myPieChart.setData(data);
        myPieChart.setHoleColor(Color.TRANSPARENT);
        myPieChart.setHoleRadius(40f);
        myPieChart.setTransparentCircleRadius(45f);
        myPieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);
        myPieChart.invalidate(); // refresh
    }


    public void setData()
    {
        DataBaseHelper dataHelper = new DataBaseHelper(this, "co2.sqlite", null, 1);
        SQLiteDatabase db = dataHelper.getWritableDatabase();

        //??????????????????SQLite????????????????????????
        Cursor c = db.rawQuery("SELECT * FROM main", null);
        c.moveToFirst();

        for(int i = 0; i < c.getCount(); i++) {
            //????????????????????????????????????

            data.add(new co2_item(
                    c.getString(2),
                    c.getInt(1),
                    c.getInt(3),
                    c.getInt(0)));
            //??????????????????
            c.moveToNext();
        }
        c.close();
    }

    public void setRecycleView()
    {
        RecyclerView recyclerView = findViewById(R.id.RecyclerViewInAnalysis);

        //????????????
        //data.add(new co2_item("????????????","2018-04-06",160,46456465L));

        Collections.reverse(data);

        MyAdapter adapter = new MyAdapter(this, data);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}
