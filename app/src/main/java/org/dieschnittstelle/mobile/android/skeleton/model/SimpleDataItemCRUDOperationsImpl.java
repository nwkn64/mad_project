package org.dieschnittstelle.mobile.android.skeleton.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleDataItemCRUDOperationsImpl implements IDataItemCRUDOperations {

    private static String[] ITEM_NAMES = new String[]{"lorem", "ipsum", "dolor", "sit"};

    @Override
    public DataItem createDataItem(DataItem item) {
        return null;
    }

    @Override
    public List<DataItem> readAllDataItems() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        List<DataItem> items = new ArrayList<>();
        for (String name : ITEM_NAMES) {
            items.add(new DataItem(name));
        }
        return items;
    }

    @Override
    public DataItem readDataItem() {
        return null;
    }

    @Override
    public boolean updateDataItem(DataItem item) {
        return false;
    }

    @Override
    public boolean deleteDataItem(long id) {
        return false;
    }
}
