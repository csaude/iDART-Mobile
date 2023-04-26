package mz.org.fgh.idartlite.model;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

import mz.org.fgh.idartlite.adapter.recyclerview.listable.Listble;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.drug.TherapeuticRegimenDaoImpl;
import mz.org.fgh.idartlite.util.Utilities;

@DatabaseTable(tableName = "therapeutic_regimen", daoClass = TherapeuticRegimenDaoImpl.class)
public class TherapeuticRegimen extends BaseModel implements Listble {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_REGIMEN_CODE = "regimen_code";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_REST_ID = "restid";
	public static final String COLUMN_DISEASE_TYPE_ID = "disease_type_id";

	@DatabaseField(columnName = COLUMN_ID, generatedId = true)
	private int id;

	@DatabaseField(columnName = COLUMN_REGIMEN_CODE)
	private String regimenCode;

	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	private String description;

	@DatabaseField(columnName = COLUMN_REST_ID)
	private int restid;

	@DatabaseField(columnName = COLUMN_DISEASE_TYPE_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private DiseaseType diseaseType;

	public int getId() {
		return id;
	}

	@Override
	public int getPosition() {
		return 0;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegimenCode() {
		return regimenCode;
	}

	public void setRegimenCode(String regimenCode) {
		this.regimenCode = regimenCode;
	}

	public String getDescription() {
		return description;
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
	public String getCode() {
		return regimenCode;
	}

	@Override
	public int compareTo(BaseModel baseModel) {
		return 0;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRestId() { return restid; }

	public void setRestId(int restid) { this.restid = restid; }

	public DiseaseType getDiseaseType() {
		return diseaseType;
	}

	public void setDiseaseType(DiseaseType diseaseType) {
		this.diseaseType = diseaseType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TherapeuticRegimen that = (TherapeuticRegimen) o;
		if (!Utilities.stringHasValue(regimenCode) && id <= 0) return false;
		return regimenCode.equals(that.regimenCode) &&
				description.equals(that.description);
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public int hashCode() {
		return Objects.hash(regimenCode, description);
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
}
