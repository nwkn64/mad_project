package org.dieschnittstelle.mobile.android.skeleton.model;

import java.util.List;

public interface IDataItemCRUDOperations {

    public DataItem createDataItem(DataItem item);

    public List<DataItem> readAllDataItems();

    public DataItem readDataItem();

    public boolean updateDataItem(DataItem item);

    public boolean deleteDataItem(long id);
}
