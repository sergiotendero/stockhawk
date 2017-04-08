package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Activity for showing a stock prices over time
 */
public class StockDetailActivity extends AppCompatActivity {

    @BindView(R.id.stock_chart)
    LineChart stockChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        // For allowing up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String symbol = intent.getStringExtra(Intent.EXTRA_TEXT);

            // Search symbol in content provider
            Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);

            // If a result is recovered
            if (cursor.moveToNext()) {
                // Get historyData from cursor
                String historyData = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));

                // Create a line data set for chart
                LineDataSet dataSet = new LineDataSet(createEntries(historyData), symbol);
                dataSet.setColor(ContextCompat.getColor(this, R.color.material_blue_500));

                // set data
                LineData lineData = new LineData(dataSet);
                stockChart.setData(lineData);

                // set description label
                Description chartDescription = new Description();
                chartDescription.setText("Price evolution");
                stockChart.setDescription(chartDescription);

                // hide x labels because they are too much
                stockChart.getXAxis().setDrawLabels(false);

                // invalidate for refreshing view
                stockChart.invalidate();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a list of entries for a chart from the history data
     *
     * @param historyData history data of a stock in csv format
     * @return list of entries form the history data
     */
    private List<Entry> createEntries(String historyData) {

        // historyData is in csv format so each line is a row
        List<String> items = Arrays.asList(historyData.split("\\n"));

        List<Entry> entries = new ArrayList<Entry>();

        // Each row represent a pair data-stock price
        for (String item : items) {
            // First part is date and second is stock price
            String[] parts = item.split(",");
            Entry entry = new Entry(Float.parseFloat(parts[0]), Float.parseFloat(parts[1].trim()));
            entries.add(entry);
        }

        // In LineChart entries have to be ordered by the x-position
        Collections.sort(entries, new EntryXComparator());

        return entries;
    }
}
