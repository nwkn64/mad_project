package org.dieschnittstelle.mobile.android.skeleton;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailviewActivity extends AppCompatActivity {

    public static final String ARG_ITEM = "item";

    public static final int CALL_CONTACT_PICKER = 0;

    //Datepicker1
    //Elemente des Datepickers
    Button btnTime, btnDate;
    TextView tvTime, tvDate;

    //Dialoge Date Picker1
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;

    Calendar calendar = Calendar.getInstance();

    private DataItem item;
    private ActivityDetailviewBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);


        btnTime = findViewById(R.id.BtnTime);
        btnDate = findViewById(R.id.BtnDate);
        tvTime = findViewById(R.id.TvTime);
        tvDate = findViewById(R.id.TvDate);


        FloatingActionButton fab = binding.getRoot().findViewById(R.id.fab);
        EditText itemName = binding.getRoot().findViewById(R.id.itemName);

        itemName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {
                    if (textView.getText().toString().trim().length() == 0) {
                        textView.setError("You need to input a name");
                    } else {
                        fab.setEnabled(true);
                    }
                }
                return false;
            }
        });
        this.item = (DataItem) getIntent().getSerializableExtra(ARG_ITEM);
        if (item == null) {
            this.item = new DataItem();
            this.item.setFavourite(false);
            this.item.setChecked(false);
            System.out.println(this.item);
        }

        binding.setController(this);


        this.showFeedbackMessage("Item has contacts" + this.item.getContacts());

//ueberprüft ob wir kontakte haben
        if (item.getContacts() != null && item.getContacts().size() > 0) {
            item.getContacts().forEach(contactUriString -> {
                this.showContactDetails(Uri.parse(contactUriString), 4);
            });
        }

        btnTime.setOnClickListener((view) -> {
            onClick(view);
        });

        btnDate.setOnClickListener((view) -> {
            onClick(view);
        });
    }

    public void onSaveItem(View view) {
        Intent returnData = new Intent();

        returnData.putExtra(ARG_ITEM, this.item);

        this.setResult(Activity.RESULT_OK, returnData);

        finish();
    }

    public DataItem getItem() {
        return item;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addContact:
                selectAndAddContact();
                return true;
            case R.id.deleteItem:
                Toast.makeText(this, "Something else was selected...",
                        Toast.LENGTH_SHORT).show();
                return true;

        }
        return false;
    }

    private void selectAndAddContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, CALL_CONTACT_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CALL_CONTACT_PICKER && resultCode == Activity.RESULT_OK) {
            addSelectedContactToContacts(data.getData());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addSelectedContactToContacts(Uri contactid) {

        if (item.getContacts() != null) {
            item.setContacts(new ArrayList<>());
        }
        if (item.getContacts().indexOf(contactid.toString()) == -1) {
            item.getContacts().add(contactid.toString());
        }
        showContactDetails(contactid, 4);
    }
//modifiziert

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {

    }

    private void showContactDetails(Uri contactid, int requestCode) {
        int hasReadContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(
                            DetailviewActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            requestCode);

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
            return;
        } else {
            showFeedbackMessage("Contact Permission have been granted!");
        }
        Cursor cursor = getContentResolver().query(contactid, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String internalContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));


            showFeedbackMessage("Got result from Contact picker" + contactName + " with id " + internalContactId);


            Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone._ID + "= ?",
                    new String[]{internalContactId},
                    null,
                    null
            );

            while (phoneCursor.moveToNext()) {
                String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            }
         /*   if (item.getContacts() == null) {
                item.setContacts(new ArrayList<>());
            }
            if (item.getContacts().indexOf(internalContactId) == -1) {
                item.getContacts().add(contactid.toString());
            }

          */
        }
    }

    private void showFeedbackMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }


    //Datum und Uhrzeit Date1
    public void onClick(View v) {
        calendar = Calendar.getInstance();
        switch (v.getId()) {
            case R.id.BtnTime: {
                timePickerDialog = new TimePickerDialog(DetailviewActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar timeCalendar = Calendar.getInstance();
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeCalendar.set(Calendar.MINUTE, minute);
                        String timestring = DateUtils.formatDateTime(DetailviewActivity.this, timeCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
                        item.setTime(timestring);
                        tvTime.setText("Uhrzeit:" + timestring);
                    }
                    //Voreinstellung aktuelle Uhrzeit
                    //Zeitzone nach Location
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(DetailviewActivity.this));

                timePickerDialog.show();
                break;
            }

            case R.id.BtnDate:

                datePickerDialog = new DatePickerDialog(DetailviewActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar dateCalendar = Calendar.getInstance();

                        dateCalendar.set(Calendar.YEAR, year);
                        dateCalendar.set(Calendar.MONTH, month);
                        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // String dateString = DateUtils.formatDateTime(DetailviewActivity.this, dateCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
                        String dateString = String.valueOf(dayOfMonth) + "." + String.valueOf(month) + "." + String.valueOf(year);
                        item.setDate(dateString);

                        tvDate.setText("Datum:" + dateString);
                    }
                },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }
    }
}






