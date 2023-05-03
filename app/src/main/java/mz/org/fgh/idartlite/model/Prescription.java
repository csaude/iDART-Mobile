package mz.org.fgh.idartlite.model;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.dao.prescription.PrescriptionDaoImpl;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;

@DatabaseTable(tableName = "prescription", daoClass = PrescriptionDaoImpl.class)
public class Prescription extends BaseModel {

	public static final String TABLE_NAME = "Prescription";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_PRESCRIPTION_DATE = "prescription_date";
	public static final String COLUMN_SUPPLY = "supply";
	public static final String COLUMN_EXPIRY_DATE = "expiry_date";
	public static final String COLUMN_URGENT_PRESCRIPTION = "urgent_prescription";
	public static final String COLUMN_URGENT_NOTES = "urgent_notes";
	public static final String COLUMN_REGIMEN_ID = "regimen_id";
	public static final String COLUMN_LINE_ID = "line_id";
	public static final String COLUMN_DISPENSE_TYPE_ID = "dispense_type_id";
	public static final String COLUMN_PRESCRIPTION_SEQ = "prescription_seq";
	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_PATIENT_ID = "patient_id";
	public static final String COLUMN_DISEASE_TYPE_ID = "disease_type_id";
	public static final String COLUMN_PROPHYLAXY_FOLLOW_UP = "prophylaxy_follow_up";

	public static final String DURATION_TWO_WEEKS = "2 Semanas";
	public static final String DURATION_ONE_MONTH = "1 Mês";
	public static final String DURATION_TWO_MONTHS = "2 Meses";
	public static final String DURATION_THREE_MONTHS = "3 Meses";
	public static final String DURATION_FOUR_MONTHS = "4 Meses";
	public static final String DURATION_FIVE_MONTHS = "5 Meses";
	public static final String DURATION_SIX_MONTHS = "6 Meses";

	public static final String URGENT_PRESCRIPTION = "Especial";
	public static final String NOT_URGENT_PRESCRIPTION = "Não Especial";

	@DatabaseField(columnName = COLUMN_ID, generatedId = true)
	private int id;

	@DatabaseField(columnName = COLUMN_PRESCRIPTION_DATE)
	private Date prescriptionDate;

	@DatabaseField(columnName = COLUMN_SUPPLY)
	private int supply;

	@DatabaseField(columnName = COLUMN_EXPIRY_DATE)
	private Date expiryDate;

	@DatabaseField(columnName = COLUMN_URGENT_PRESCRIPTION)
	private String urgentPrescription;

	@DatabaseField(columnName = COLUMN_URGENT_NOTES)
	private String urgentNotes;

	@DatabaseField(columnName = COLUMN_REGIMEN_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private TherapeuticRegimen therapeuticRegimen;

	@DatabaseField(columnName = COLUMN_LINE_ID, canBeNull = true, foreign = true, foreignAutoRefresh = true)
	private TherapeuticLine therapeuticLine;

	@DatabaseField(columnName = COLUMN_DISPENSE_TYPE_ID, canBeNull = true, foreign = true, foreignAutoRefresh = true)
	private DispenseType dispenseType;

	@DatabaseField(columnName = COLUMN_PRESCRIPTION_SEQ)
	private String prescriptionSeq;

	@DatabaseField(columnName = COLUMN_UUID)
	private String uuid;

	@DatabaseField(columnName = COLUMN_PATIENT_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Patient patient;

	@DatabaseField(columnName = COLUMN_SYNC_STATUS)
	private String syncStatus;

	@DatabaseField(columnName = COLUMN_DISEASE_TYPE_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private DiseaseType diseaseType;

	@DatabaseField(columnName = COLUMN_PROPHYLAXY_FOLLOW_UP, canBeNull = true)
	private String prophylaxyFollowUp;

	private List<PrescribedDrug> prescribedDrugs;

	private List<Dispense> dispenses;

	public String getProphylaxyFollowUp() {
		return prophylaxyFollowUp;
	}

	public void setProphylaxyFollowUp(String prophylaxyFollowUp) {
		this.prophylaxyFollowUp = prophylaxyFollowUp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getPrescriptionDate() {
		return prescriptionDate;
	}

	public void setPrescriptionDate(Date prescriptionDate) {
		this.prescriptionDate = prescriptionDate;
	}

	public int getSupply() {
		return supply;
	}

	public void setSupply(int supply) {
		this.supply = supply;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getUrgentPrescription() {
		return urgentPrescription;
	}

	public void setUrgentPrescription(String urgentPrescription) {
		this.urgentPrescription = urgentPrescription;
	}

	public String getUrgentNotes() {
		return urgentNotes;
	}

	public void setUrgentNotes(String urgentNotes) {
		this.urgentNotes = urgentNotes;
	}

	public TherapeuticRegimen getTherapeuticRegimen() {
		return therapeuticRegimen;
	}


	public void setTherapeuticRegimen(TherapeuticRegimen therapeuticRegimen) {
		this.therapeuticRegimen = therapeuticRegimen;
	}

	public TherapeuticLine getTherapeuticLine() {
		return therapeuticLine;
	}

	public void setTherapeuticLine(TherapeuticLine therapeuticLine) {
		this.therapeuticLine = therapeuticLine;
	}

	public String getPrescriptionSeq() {
		return prescriptionSeq;
	}

	public void setPrescriptionSeq(String prescriptionSeq) {
		this.prescriptionSeq = prescriptionSeq;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public DispenseType getDispenseType() {
		return dispenseType;
	}

	public void setDispenseType(DispenseType dispenseType) {
		this.dispenseType = dispenseType;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(String syncStatus) {
		this.syncStatus = syncStatus;
	}

	public List<PrescribedDrug> getPrescribedDrugs() {
		return prescribedDrugs;
	}

	public void setPrescribedDrugs(List<PrescribedDrug> prescribedDrugs) {
		this.prescribedDrugs = prescribedDrugs;
	}

	public void setDiseaseType(DiseaseType diseaseType) {
		this.diseaseType = diseaseType;
	}

	public DiseaseType getDiseaseType() {
		return diseaseType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Prescription that = (Prescription) o;
		return prescriptionDate.equals(that.prescriptionDate) &&
				prescriptionSeq.equals(that.prescriptionSeq) &&
				uuid.equals(that.uuid);
	}

	public double getDurationInMonths(){
		if (expiryDate == null) return  0;
		return DateUtilities.dateDiff(this.prescriptionDate, this.expiryDate, DateUtilities.MONTH_FORMAT);
	}

	public int getTimeLeftInMonths(){

		int leftTime = getSupply()-getTotalDispenseSupplied();

       return getDurationMonths(leftTime);
	}

	public String getDurationToUserUI(){
		if (this.supply == 2) return DURATION_TWO_WEEKS;
		if (this.supply == 4) return DURATION_ONE_MONTH;
		if (this.supply == 8) return DURATION_TWO_MONTHS;
		if (this.supply == 12) return DURATION_THREE_MONTHS;
		if (this.supply == 16) return DURATION_FOUR_MONTHS;
		if (this.supply == 20) return DURATION_FIVE_MONTHS;
		if (this.supply == 24) return DURATION_SIX_MONTHS;

		return "Não definido";
	}

	public int getDurationMonths(int supplyLeft){
		if (supplyLeft == 2) return 0;
		if (supplyLeft == 4) return 1;
		if (supplyLeft == 8) return 2;
		if (supplyLeft == 12) return 3;
		if (supplyLeft== 16) return 4;
		if (supplyLeft== 20) return 5;
		if (supplyLeft == 24) return 6;

		return 0;
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	public int hashCode() {
		return Objects.hash(prescriptionDate, prescriptionSeq, uuid);
	}

	@Override
	public String toString() {
		return "Prescription{" +
				"prescriptionDate=" + prescriptionDate +
				", supply=" + supply +
				", expiryDate=" + expiryDate +
				", urgentPrescription=" + urgentPrescription +
				", urgentNotes='" + urgentNotes + '\'' +
				", dispenseType=" + dispenseType +
				", prescriptionSeq='" + prescriptionSeq + '\'' +
				", uuid='" + uuid + '\'' +
				", syncStatus='" + syncStatus + '\'' +
				'}';
	}

	public boolean isUrgent(){
		return  URGENT_PRESCRIPTION.equalsIgnoreCase(this.urgentPrescription);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public String getDuration(Date pickupDate, Date nextPickupDate){
		//Parsing the date
		LocalDate pickupLocalDate = pickupDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		LocalDate nextPickupLocalDate  = nextPickupDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		//calculating number of days in between
		long noOfDaysBetween = ChronoUnit.DAYS.between(pickupLocalDate, nextPickupLocalDate);

		return String.valueOf(noOfDaysBetween);

	}

    public String validate(Context context) {
		if (this.prescriptionDate == null) return context.getString(R.string.prescription_date_mandatory);
		if(DateUtilities.dateDiff(this.prescriptionDate, DateUtilities.getCurrentDate(), DateUtilities.DAY_FORMAT) > 0) {
			return context.getString(R.string.prescription_date_not_correct);
		}
		if(this.supply <= 0) return context.getString(R.string.prescription_durationmandatory);
		if(this.dispenseType == null || !Utilities.stringHasValue(this.dispenseType.getDescription())) return context.getString(R.string.prescription_dispense_type_mandatory);
		if(this.therapeuticRegimen == null || !Utilities.stringHasValue(this.therapeuticRegimen.getDescription())) return context.getString(R.string.regimen_mandatory);
		if((this.therapeuticLine == null && this.diseaseType.getCode() == "TARV" || this.diseaseType.getCode() == "TARV" && !Utilities.stringHasValue(this.therapeuticLine.getDescription()))) return context.getString(R.string.prescription_line_mandatory);

		if (!Utilities.listHasElements(this.prescribedDrugs)) return context.getString(R.string.prescription_drugs_mandatory);
		if (isUrgent() && !Utilities.stringHasValue(this.urgentNotes)) return context.getString(R.string.prescription_urgent_notes_mandatory);

		return "";
    }

	public List<Dispense> getDispenses() {
		return dispenses;
	}

	public void setDispenses(List<Dispense> dispenses) {
		this.dispenses = dispenses;
	}

	public boolean isClosed() {
		if (!Utilities.listHasElements(this.dispenses) && this.expiryDate == null) return false;

		if (this.supply > getTotalDispenseSupplied()) return false;

		return true;
	}

	public int getTotalDispenseSupplied(){
		int totalDispenseSupply = 0;

		if (!Utilities.listHasElements(this.dispenses)) return 0;

		for (Dispense dispense : this.dispenses){
			totalDispenseSupply += dispense.getSupply();
		}

		return totalDispenseSupply;
	}

	public void generateNextSeq() {
		long lastSeq = 0;
		try{
			if (Utilities.stringHasValue(this.prescriptionSeq)){
				lastSeq = Long.parseLong(this.prescriptionSeq);
			}else {
				lastSeq = 0;
			}
		}catch (Exception e){
			e.printStackTrace();
		}


		setPrescriptionSeq(String.valueOf(Utilities.garantirXCaracterOnNumber(lastSeq+1, 4)));
	}

	public String getUiId(){
		return Utilities.concatStrings(this.getPrescriptionDiseaseType().concat(this.patient.getNid()), this.prescriptionSeq, "-");
	}

	public boolean isSyncStatusReady(){
		return isSyncStatusReady(this.syncStatus);
	}

	public String getPrescriptionAsString(){
		String prescriptionData = "";

		prescriptionData = Utilities.concatStrings(prescriptionData, "Este paciente contém uma Prescrição anterior válida/sem duração com o id "+this.getUiId() +" (detalhes abaixo).", "\n\n");

		if(this.therapeuticRegimen != null){
			prescriptionData = Utilities.concatStrings(prescriptionData, "Regime: " +this.therapeuticRegimen.getDescription(), "\n");
		}else{
			prescriptionData = Utilities.concatStrings(prescriptionData, "Regime: não definido!" , "\n");
		}
		prescriptionData = Utilities.concatStrings(prescriptionData, "Duração: "+getDurationToUserUI(), "\n");
		prescriptionData = Utilities.concatStrings(prescriptionData, "Registado em: "+ DateUtilities.formatToDDMMYYYY(this.prescriptionDate), "\n");
		prescriptionData = Utilities.concatStrings(prescriptionData, "Medicamentos na Prescrição: \n"+getPrescribedDrugsAsString(), "\n");
		prescriptionData = Utilities.concatStrings(prescriptionData, "\n Gostaria de apagar esta Prescrição anterior, e substituí-la com a que esta para criar? ", "");

		return prescriptionData;
	}

	public String getPrescribedDrugsAsString(){
		String drugs = "";

		if (Utilities.listHasElements(this.prescribedDrugs)) {
			for (PrescribedDrug prescribedDrug: this.prescribedDrugs) {
				drugs = Utilities.concatStrings(drugs, prescribedDrug.getDrug().getDescription(), "; ");
			}
		}


		return drugs;
	}

	public String getPrescriptionDiseaseType(){
		return this.diseaseType != null ? this.diseaseType.getDescription()+": " : "TARV: ";
	}

	@Override
	public String isValid(Context context) {
		return validate(context);
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
