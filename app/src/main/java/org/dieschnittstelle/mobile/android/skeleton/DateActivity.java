//Datepicker 2


package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Date;


class DatePickerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailview);


        DatePicker picker = (DatePicker) findViewById(R.id.picker);

        final TextView dateAsText = (TextView) findViewById(R.id.dateAsText);
        dateAsText.setText(new Date().toString());

        picker.init(picker.getYear(), picker.getMonth(), picker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Date dt = new Date(year - 1900, monthOfYear, dayOfMonth);

                dateAsText.setText(dt.toString());

                if (year < 2019) {
                    startActivity(new Intent(DatePickerActivity.this, DetailviewActivity.class));
                }
            }
        });
    }
}