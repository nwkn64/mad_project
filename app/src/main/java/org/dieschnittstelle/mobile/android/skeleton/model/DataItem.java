package org.dieschnittstelle.mobile.android.skeleton.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
public class DataItem implements Serializable {


    private static final String CONTACTS_SEPARATOR = "--;;--";
    //  private static long idcount = 0;
    @PrimaryKey(autoGenerate = true)
    private long id;// = ++idcount;

    private String name;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    private String description;


    @SerializedName("done")
    private boolean checked;


    private boolean favourite;

    private String date;
    private long dateTime = 0;
    private String time;
    private long timeTime = 0;

    public String getDate() {
        return date;
    }
    public long getDateTime() {
        return dateTime;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public void setDateTime(long datetime) {
        this.dateTime = datetime;
    }

    public String getTime() {
        return time;
    }

    public void setTimeTime (long timetime){
        timeTime = timetime;
    }
    public long getTimeTime() {
        return timeTime;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Ignore
    private List<String> contacts = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    private String contactsStr;

    public DataItem() {

    }

    public DataItem(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem dataItem = (DataItem) o;
        return id == dataItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }


    public String getContactsStr() {
        beforePersist();
        return contactsStr;
    }

    public void setContactsStr(String contactsStr) {
        this.contactsStr = contactsStr;
        afterLoad();
    }

    public void beforePersist() {
        if (this.contacts != null) {
            this.contactsStr = this.contacts
                    .stream()
                    .collect(Collectors.joining(CONTACTS_SEPARATOR));
        }
    }

    public DataItem afterLoad() {
        if (this.contactsStr != null) {
            this.contacts = Arrays.asList(this.contactsStr.split(CONTACTS_SEPARATOR))
                    .stream()
                    .map(c -> c.trim())
                    .collect(Collectors.toList());
            this.contactsStr = null;
        }
        return this;
    }

    //ueberfaelligkeit datum

    public boolean isExpired() {
        try {
            if(expiry == 0){
                return false;
            }
            long now = System.currentTimeMillis();
            long diff = expiry - now;
            if(diff < 0){
                return true;
            }
            else {
                return false;
            }
        } catch(Exception e){
            return false;
        }
    }
}


