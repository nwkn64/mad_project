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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    public static final String ARG_LOCATION = "location";
    public static final String ARG_COORDS = "coordinates";


    public static final int CALL_CONTACT_PICKER = 7;
    public static final int LOCATION_RESULT = 8;
    public static final int CONTACT_PERMISSION_RESULT = 9;

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


    private ArrayAdapter<String> contactsViewAdapter;
    private View contactList;


    private static Button locationBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);


        btnTime = findViewById(R.id.BtnTime);
        btnDate = findViewById(R.id.BtnDate);
        tvTime = findViewById(R.id.TvTime);
        tvDate = findViewById(R.id.TvDate);


        FloatingActionButton fab = binding.getRoot().findViewById(R.id.fab);
        locationBtn = binding.getRoot().findViewById(R.id.locationBtn);


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

        locationBtn.setOnClickListener((view) -> {

            Intent callMapView = new Intent(this, MapsActivity.class);
            callMapView.putExtra("item", item);
            startActivityForResult(callMapView, LOCATION_RESULT);

        });


        item = (DataItem) getIntent().getSerializableExtra(ARG_ITEM);
        if (item == null) {
            item = new DataItem();
            item.setFavourite(false);
            item.setChecked(false);
        }

        this.showFeedbackMessage("Item has contacts" + item.getContacts());
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.item.getContacts());

        ListView listView = (ListView) findViewById(R.id.contactsList);
        listView.setAdapter(itemsAdapter);

        binding.setController(this);


        if (item.getContacts() != null && item.getContacts().size() > 0) {
            item.getContacts().forEach(contactUriString -> {
                this.showContactDetails(Uri.parse(contactUriString), CONTACT_PERMISSION_RESULT);
            });
        }

        binding.setController(this);

        System.out.println(item.getGeoCoordinates());


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

                Intent returnData = new Intent();

                returnData.putExtra(ARG_ITEM, this.item);

                this.setResult(5, returnData);

                finish();
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
        }
        else if (requestCode == LOCATION_RESULT && resultCode == Activity.RESULT_OK) {
            // The "MapsActivity" finished and we parse the result.
            String loc = data.getStringExtra(ARG_LOCATION);
            String coor = data.getStringExtra(ARG_COORDS);
            // We now update THIS item.
            item.setGeoCoordinates(coor);
            item.setLocation(loc);

            binding.invalidateAll();
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
        showContactDetails(contactid, CONTACT_PERMISSION_RESULT);
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
                String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int phoneNumberType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2));

                if (phoneNumberType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    Log.i("DetailViewActivity", "found mobile Number: " + number);
                } else {
                    Log.i("DetailViewActivity", "found other Number: " + number);

                }
            }
            Log.i("DetailViewActivity", "no further phone numbers found");

            Cursor emailCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "= ?",
                    new String[]{internalContactId},
                    null,
                    null
            );

            while (emailCursor.moveToNext()) {
                String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                Log.i("DetailViewActivity", "email is" + email);

            }
            Log.i("DetailViewActivity", "no further email found");

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

                // When a time was set before:
                if(item.getTimeTime() > 0)
                {
                    calendar.setTimeInMillis(item.getTimeTime());
                }
                timePickerDialog = new TimePickerDialog(DetailviewActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar timeCalendar = Calendar.getInstance();
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeCalendar.set(Calendar.MINUTE, minute);
                        String timestring = DateUtils.formatDateTime(DetailviewActivity.this, timeCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
                        item.setTime(timestring);
                       item.setTime(timestring);
                       item.setTimeTime(timeCalendar.getTimeInMillis());
                        tvTime.setText("Uhrzeit:" + timestring);
                    }
                    //Voreinstellung aktuelle Uhrzeit
                    //Zeitzone nach Location
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(DetailviewActivity.this));

                timePickerDialog.show();
                break;
            }

            case R.id.BtnDate:

                // When a date was set before:
                if(item.getDateTime() > 0)
                {
                    calendar.setTimeInMillis(item.getDateTime());
                }
                datePickerDialog = new DatePickerDialog(DetailviewActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar dateCalendar = Calendar.getInstance();

                        dateCalendar.set(Calendar.YEAR, year);
                        dateCalendar.set(Calendar.MONTH, month);
                        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        String dateString = DateUtils.formatDateTime(DetailviewActivity.this, dateCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                        item.setDate(dateString);
                        item.setDateTime(dateCalendar.getTimeInMillis());

                        tvDate.setText("Datum:" + dateString);
                    }
                },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }
    }
}






