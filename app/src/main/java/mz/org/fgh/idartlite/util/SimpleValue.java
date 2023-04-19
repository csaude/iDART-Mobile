package mz.org.fgh.idartlite.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;

public class SimpleValue extends BaseModel implements Listble {

    private int id;

    private String description;

    private String code;

    private String qty;

    public SimpleValue(String description) {
        this.description = description;
    }

    public SimpleValue() {
    }

    public SimpleValue(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public SimpleValue(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static SimpleValue fastCreate(String description){
        return new SimpleValue(description);
    }

    public static SimpleValue fastCreate(int id, String description){
        return new SimpleValue(id, description);
    }

    public static SimpleValue fastCreate(int id){
        return new SimpleValue(id);
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof SimpleValue)) return false;
        SimpleValue that = (SimpleValue) o;

        if (that == null || (that.id <= 0 && !Utilities.stringHasValue(that.description))) return false;

        if (id > 0 && that.id > 0) return id == that.id;

        if (Utilities.stringHasValue(this.description)) return this.description.equals(that.description);

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int compareTo(Object o) {
        return compareTo((BaseModel) o);
    }

    @Override
    public int getPosition() {
        return listPosition;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void setListPosition(int listPosition) {
        super.setListPosition(listPosition);
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

    @Override
    public int getQuantity() {
        return 0;
    }

    @Override
    public int getDrawable() {
        return 0;
    }

    @Override
    public int compareTo(BaseModel baseModel) {
        if (this.getPosition() == 0 || ((SimpleValue) baseModel).getPosition() == 0) return 0;

        return this.getPosition() - ((SimpleValue) baseModel).getPosition();
    }
}
