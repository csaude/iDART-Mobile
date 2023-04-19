package mz.org.fgh.idartlite.model;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.dispense.DispenseTypeDaoImpl;
import mz.org.fgh.idartlite.util.Utilities;

@DatabaseTable(tableName = "Dispense_type", daoClass = DispenseTypeDaoImpl.class)
public class DispenseType extends BaseModel implements Listble {

    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_DESCRIPTION = "description";

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = COLUMN_CODE)
    private String code;

    @DatabaseField(columnName = COLUMN_DESCRIPTION)
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int getDrawable() {
        return 0;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispenseType that = (DispenseType) o;
        if (!Utilities.stringHasValue(description) && id <= 0) return false;
        return description.equals(that.description);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
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

    public boolean isMonthlyDispense(){
        return this.description.equalsIgnoreCase("Dispensa Mensal (DM)");
    }

    public boolean isThreeMonthsDispense(){
        return this.description.equalsIgnoreCase("Dispensa Trimestral (DT)");
    }

    public boolean isSixMonthsDispense(){
        return this.description.equalsIgnoreCase("Dispensa Semestral (DS)");
    }

    public boolean isOtherDispense(){
        return this.description.equalsIgnoreCase("Outro");
    }
}
