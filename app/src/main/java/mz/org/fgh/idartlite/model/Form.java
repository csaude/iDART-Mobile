package mz.org.fgh.idartlite.model;


import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.drug.FormDaoImpl;

@DatabaseTable(tableName = "form", daoClass = FormDaoImpl.class)
public class Form extends BaseModel {

    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_REST_ID = "restid";

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = COLUMN_UNIT)
    private String unit;

    @DatabaseField(columnName = COLUMN_DESCRIPTION)
    private String description;

    @DatabaseField(columnName = COLUMN_REST_ID)
    private int restid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRestId() { return restid; }

    public void setRestId(int restid) { this.restid = restid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Form form = (Form) o;
        return id == form.id &&
                unit.equals(form.unit) &&
                description.equals(form.description);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(id, unit, description);
    }

    @Override
    public String toString() {
        return "Form{" +
                "id=" + id +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public String isValid(Context context) {
        return null;
    }

    @Override
    public String canBeEdited(Context context) {
        return null;
    }

    @Override
    public String canBeRemoved(Context context) {
        return null;
    }
}
