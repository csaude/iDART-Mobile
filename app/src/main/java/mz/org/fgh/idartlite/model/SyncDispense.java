package mz.org.fgh.idartlite.model;

import java.util.Date;

public class SyncDispense {

    private Integer id;
    private Integer clinicalstage;
    private Character current;
    private Date date;
    private Integer doctor;
    private Integer duration;
    private Character modified;
    private int patient;
    private Double weight;
    private String reasonforupdate;
    private String notes;
    private Date enddate;
    private String drugtypes;
    private String regimenome;
    private Date datainicionoutroservico;
    private String motivomudanca;
    private String linhanome;
    private Character ppe;
    private Character ptv;
    private Character tb;
    private Character tpi;
    private Character tpc;
    private int dispensatrimestral;
    private String tipodt;
    private Character gaac;
    private Character af;
    private Character ca;
    private Character ccr;
    private Character saaj;
    private Character fr;
    private String amountpertime;
    private Date dispensedate;
    private String drugname;
    private Date expirydate;
    private String patientid;
    private String patientfirstname;
    private String patientlastname;
    private String dateexpectedstring;
    private Date pickupdate;
    private Integer timesperday;
    private Integer weekssupply;
    private String qtyinhand;
    private String summaryqtyinhand;
    private String qtyinlastbatch;
    private Character syncstatus;
    private int mainclinic;
    private String mainclinicname;
    private String mainclinicuuid;
    private String prescriptionid;
    private String tipods;
    private int dispensasemestral;
    private String durationsentence;
    private Character dc;
    private Character prep;
    private Character ce;
    private Character cpn;
    private Character prescricaoespecial;
    private String motivocriacaoespecial;
    private String syncuuid;
    private String uuidopenmrs;
    private String clinicuuid;
    private String username;
    private String tipodoenca;

    public String getTipodoenca() {
        return tipodoenca;
    }

    public void setTipodoenca(String tipodoenca) {
        this.tipodoenca = tipodoenca;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClinicalstage() {
        return clinicalstage;
    }

    public String getClinicuuid() {
        return clinicuuid;
    }

    public void setClinicuuid(String clinicuuid) {
        this.clinicuuid = clinicuuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClinicalstage(Integer clinicalstage) {
        this.clinicalstage = clinicalstage;
    }

    public Character getCurrent() {
        return current;
    }

    public void setCurrent(Character current) {
        this.current = current;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getDoctor() {
        return doctor;
    }

    public void setDoctor(Integer doctor) {
        this.doctor = doctor;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Character getModified() {
        return modified;
    }

    public void setModified(Character modified) {
        this.modified = modified;
    }

    public int getPatient() {
        return patient;
    }

    public void setPatient(int patient) {
        this.patient = patient;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getReasonforupdate() {
        return reasonforupdate;
    }

    public void setReasonforupdate(String reasonforupdate) {
        this.reasonforupdate = reasonforupdate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getDrugtypes() {
        return drugtypes;
    }

    public void setDrugtypes(String drugtypes) {
        this.drugtypes = drugtypes;
    }

    public String getRegimenome() {
        return regimenome;
    }

    public void setRegimenome(String regimenome) {
        this.regimenome = regimenome;
    }

    public Date getDatainicionoutroservico() {
        return datainicionoutroservico;
    }

    public void setDatainicionoutroservico(Date datainicionoutroservico) {
        this.datainicionoutroservico = datainicionoutroservico;
    }

    public String getMotivomudanca() {
        return motivomudanca;
    }

    public void setMotivomudanca(String motivomudanca) {
        this.motivomudanca = motivomudanca;
    }

    public String getLinhanome() {
        return linhanome;
    }

    public void setLinhanome(String linhanome) {
        this.linhanome = linhanome;
    }

    public Character getPpe() {
        return ppe;
    }

    public void setPpe(Character ppe) {
        this.ppe = ppe;
    }

    public Character getPtv() {
        return ptv;
    }

    public void setPtv(Character ptv) {
        this.ptv = ptv;
    }

    public Character getTb() {
        return tb;
    }

    public void setTb(Character tb) {
        this.tb = tb;
    }

    public Character getTpi() {
        return tpi;
    }

    public void setTpi(Character tpi) {
        this.tpi = tpi;
    }

    public Character getTpc() {
        return tpc;
    }

    public void setTpc(Character tpc) {
        this.tpc = tpc;
    }

    public int getDispensatrimestral() {
        return dispensatrimestral;
    }

    public void setDispensatrimestral(int dispensatrimestral) {
        this.dispensatrimestral = dispensatrimestral;
    }

    public String getTipodt() {
        return tipodt;
    }

    public void setTipodt(String tipodt) {
        this.tipodt = tipodt;
    }

    public Character getGaac() {
        return gaac;
    }

    public void setGaac(Character gaac) {
        this.gaac = gaac;
    }

    public Character getAf() {
        return af;
    }

    public void setAf(Character af) {
        this.af = af;
    }

    public Character getCa() {
        return ca;
    }

    public void setCa(Character ca) {
        this.ca = ca;
    }

    public Character getCcr() {
        return ccr;
    }

    public void setCcr(Character ccr) {
        this.ccr = ccr;
    }

    public Character getSaaj() {
        return saaj;
    }

    public void setSaaj(Character saaj) {
        this.saaj = saaj;
    }

    public Character getFr() {
        return fr;
    }

    public void setFr(Character fr) {
        this.fr = fr;
    }

    public String getAmountpertime() {
        return amountpertime;
    }

    public void setAmountpertime(String amountpertime) {
        this.amountpertime = amountpertime;
    }

    public Date getDispensedate() {
        return dispensedate;
    }

    public void setDispensedate(Date dispensedate) {
        this.dispensedate = dispensedate;
    }

    public String getDrugname() {
        return drugname;
    }

    public void setDrugname(String drugname) {
        this.drugname = drugname;
    }

    public Date getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(Date expirydate) {
        this.expirydate = expirydate;
    }

    public String getPatientid() {
        return patientid;
    }

    public void setPatientid(String patientid) {
        this.patientid = patientid;
    }

    public String getPatientfirstname() {
        return patientfirstname;
    }

    public void setPatientfirstname(String patientfirstname) {
        this.patientfirstname = patientfirstname;
    }

    public String getPatientlastname() {
        return patientlastname;
    }

    public void setPatientlastname(String patientlastname) {
        this.patientlastname = patientlastname;
    }

    public String getDateexpectedstring() {
        return dateexpectedstring;
    }

    public void setDateexpectedstring(String dateexpectedstring) {
        this.dateexpectedstring = dateexpectedstring;
    }

    public Date getPickupdate() {
        return pickupdate;
    }

    public void setPickupdate(Date pickupdate) {
        this.pickupdate = pickupdate;
    }

    public Integer getTimesperday() {
        return timesperday;
    }

    public void setTimesperday(Integer timesperday) {
        this.timesperday = timesperday;
    }

    public Integer getWeekssupply() {
        return weekssupply;
    }

    public void setWeekssupply(Integer weekssupply) {
        this.weekssupply = weekssupply;
    }

    public String getQtyinhand() {
        return qtyinhand;
    }

    public void setQtyinhand(String qtyinhand) {
        this.qtyinhand = qtyinhand;
    }

    public String getSummaryqtyinhand() {
        return summaryqtyinhand;
    }

    public void setSummaryqtyinhand(String summaryqtyinhand) {
        this.summaryqtyinhand = summaryqtyinhand;
    }

    public String getQtyinlastbatch() {
        return qtyinlastbatch;
    }

    public void setQtyinlastbatch(String qtyinlastbatch) {
        this.qtyinlastbatch = qtyinlastbatch;
    }

    public Character getSyncstatus() {
        return syncstatus;
    }

    public void setSyncstatus(Character syncstatus) {
        this.syncstatus = syncstatus;
    }

    public int getMainclinic() {
        return mainclinic;
    }

    public void setMainclinic(int mainclinic) {
        this.mainclinic = mainclinic;
    }

    public String getMainclinicname() {
        return mainclinicname;
    }

    public void setMainclinicname(String mainclinicname) {
        this.mainclinicname = mainclinicname;
    }

    public String getMainclinicuuid() {
        return mainclinicuuid;
    }

    public void setMainclinicuuid(String mainclinicuuid) {
        this.mainclinicuuid = mainclinicuuid;
    }

    public String getPrescriptionid() {
        return prescriptionid;
    }

    public void setPrescriptionid(String prescriptionid) {
        this.prescriptionid = prescriptionid;
    }

    public String getTipods() {
        return tipods;
    }

    public void setTipods(String tipods) {
        this.tipods = tipods;
    }

    public int getDispensasemestral() {
        return dispensasemestral;
    }

    public void setDispensasemestral(int dispensasemestral) {
        this.dispensasemestral = dispensasemestral;
    }

    public String getDurationsentence() {
        return durationsentence;
    }

    public void setDurationsentence(String durationsentence) {
        this.durationsentence = durationsentence;
    }

    public Character getDc() {
        return dc;
    }

    public void setDc(Character dc) {
        this.dc = dc;
    }

    public Character getPrep() {
        return prep;
    }

    public void setPrep(Character prep) {
        this.prep = prep;
    }

    public Character getCe() {
        return ce;
    }

    public void setCe(Character ce) {
        this.ce = ce;
    }

    public Character getCpn() {
        return cpn;
    }

    public void setCpn(Character cpn) {
        this.cpn = cpn;
    }

    public Character getPrescricaoespecial() {
        return prescricaoespecial;
    }

    public void setPrescricaoespecial(Character prescricaoespecial) {
        this.prescricaoespecial = prescricaoespecial;
    }

    public String getMotivocriacaoespecial() {
        return motivocriacaoespecial;
    }

    public void setMotivocriacaoespecial(String motivocriacaoespecial) {
        this.motivocriacaoespecial = motivocriacaoespecial;
    }

    public String getSyncuuid() {
        return syncuuid;
    }

    public void setSyncuuid(String syncuuid) {
        this.syncuuid = syncuuid;
    }

    public String getUuidopenmrs() {
        return uuidopenmrs;
    }

    public void setUuidopenmrs(String uuidopenmrs) {
        this.uuidopenmrs = uuidopenmrs;
    }
}
