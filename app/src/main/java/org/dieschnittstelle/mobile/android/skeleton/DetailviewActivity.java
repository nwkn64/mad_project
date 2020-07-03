package org.dieschnittstelle.mobile.android.skeleton;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;

import java.util.ArrayList;

public class DetailviewActivity extends AppCompatActivity {

    public static final String ARG_ITEM = "item";

    public static final int CALL_CONTACT_PICKER = 0;


    private DataItem item;
    private ActivityDetailviewBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detailview);

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
        }
        binding.setController(this);


        this.showFeedbackMessage("Item has contacts" + this.item.getContacts());

//ueberprüft ob wir kontakte haben
     if (item.getContacts() != null && item.getContacts().size() >0) {
         item.getContacts().forEach(contactUriString -> {
            this.showContactDetails(Uri.parse(contactUriString), 4);
         });
     }
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
            case R.id.doSomethingelse:
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

    public void onRequestPermissionResult(int requestCode, @NonNull String[]permissions, @NonNull int[] grantResult) {

    }

  private void showContactDetails(Uri contactid, int requestCode) {
            int hasReadContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED){

                ActivityCompat
                        .requestPermissions(
                                DetailviewActivity.this,
                                new String[] { Manifest.permission.READ_CONTACTS },
                                requestCode);

                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
               return;
            }
            else {
                showFeedbackMessage("Contact Permission have been franted!");
            }
            Cursor cursor = getContentResolver().query(contactid, null, null, null, null);
            if (cursor.moveToFirst()) {
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String internalContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                showFeedbackMessage("Got result from Contact picker" + contactName + " with id " + internalContactId);

                }
            }

    private void showFeedbackMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    }
}



