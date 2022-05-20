package mz.org.fgh.idartlite.model;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;
import java.util.Objects;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.clinic.ClinicDaoImpl;
import mz.org.fgh.idartlite.util.Utilities;

@DatabaseTable(tableName = "clinic", daoClass = ClinicDaoImpl.class)
public class Clinic extends BaseModel implements Listble {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_CLINIC_NAME = "clinic_name";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_PHARMACY_TYPE_ID = "pharmacy_type_id";
	public static final String COLUMN_REST_ID = "restid";

	private static final long serialVersionUID = 7682159171372981952L;

	@DatabaseField(columnName = COLUMN_ID, generatedId = true)
	private int id;

	@DatabaseField(columnName = COLUMN_CODE)
	private String code;

	@DatabaseField(columnName = COLUMN_CLINIC_NAME)
	private String clinicName;

	@DatabaseField(columnName = COLUMN_PHONE)
	private String phone;

	@DatabaseField(columnName = COLUMN_ADDRESS)
	private String address;

	@DatabaseField(columnName = COLUMN_UUID)
	private String uuid;

	@DatabaseField(columnName = COLUMN_PHARMACY_TYPE_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private PharmacyType pharmacyType;

	@DatabaseField(columnName = COLUMN_REST_ID)
	private int restid;


	private List<ClinicSector> clinicSectorList;


	public Clinic() {
	}

	public Clinic(int id, String clinicName) {
		this.id = id;
		this.clinicName = clinicName;
	}

	public Clinic(int id, String code, String clinicName) {
		this.id = id;
		this.code = code;
		this.clinicName = clinicName;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getPosition() {
		return 0;
	}

	@Override
	public String getDescription() {
		return clinicName;
	}

	@Override
	public int getQuantity() {
		return 0;
	}

	@Override
	public int getDrawable() {
		return R.drawable.ic_add_clinic;
	}

	@Override
	public int compareTo(BaseModel baseModel) {
		return 0;
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

	public String getClinicName() {
		return clinicName;
	}

	public void setClinicName(String clinicName) {
		this.clinicName = clinicName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public PharmacyType getPharmacyType() {
		return pharmacyType;
	}

	public void setPharmacyType(PharmacyType pharmacyType) {
		this.pharmacyType = pharmacyType;
	}

	public int getRestId() { return restid; }

	public void setRestId(int restid) { this.restid = restid; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Clinic clinic = (Clinic) o;
		if (!Utilities.stringHasValue(code) || !Utilities.stringHasValue(clinic.code)) return false;
		return code.equals(clinic.code) &&
				uuid.equals(clinic.uuid);
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public int hashCode() {
		return Objects.hash(code, uuid);
	}

	@Override
	public String toString() {
		if(clinicName == null && address == null){
			return " ";
		}else{
			return  clinicName;
		}
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

	public List<ClinicSector> getClinicSectorList() {
		return clinicSectorList;
	}

	public void setClinicSectorList(List<ClinicSector> clinicSectorList) {
		this.clinicSectorList = clinicSectorList;
	}

	public boolean isSanitaryUnit() {
		return getPharmacyType().getDescription().contains("Unidade Sanit");
	}

	public boolean isPrivateOrComunitClinic() {
		System.out.println(getPharmacyType().getDescription());
		return getPharmacyType().getDescription().contains("Privada") || getPharmacyType().getDescription().contains("Comunit");
	}

	public boolean isPrivatClinic() {
		return getPharmacyType().getDescription().contains("Privada");
	}

	// Visibility Validation
	public boolean mustValidateStock() {
		return this.isPrivateOrComunitClinic();
	}
}
