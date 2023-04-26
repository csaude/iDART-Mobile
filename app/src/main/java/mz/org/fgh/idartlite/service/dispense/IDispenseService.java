package mz.org.fgh.idartlite.service.dispense;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.service.IBaseService;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.StockReportData;


public interface IDispenseService extends IBaseService<Dispense> {

    public List<Dispense> getAllNotVoidedDispenseByPrescription(Prescription prescription) throws SQLException;

    public void createDispense(Dispense dispense) throws SQLException;

    public void saveDispensedDrugs(List<DispensedDrug> dispensedDrugs, Dispense dispense) throws SQLException ;

    public void udpateDispense(Dispense dispense) throws SQLException;

    public void deleteDispense(Dispense dispense) throws SQLException;

    public List<Dispense> getAllOfPatient(Patient patient) throws SQLException;

    public Dispense getLastDispenseFromPrescription(Prescription prescription) throws SQLException;

    public long countAllOfPrescription(Prescription prescription) throws SQLException ;

    public void saveOrUpdateDispense(Dispense dispense, boolean updateStock) throws SQLException;

    public void saveOrUpdateDispensedDrugs(List<DispensedDrug> dispensedDrugs, Dispense dispense, boolean updateStock) throws SQLException;

    public List<Dispense> getAllDispensesByStatus(String status) throws SQLException;

    public List<Dispense> getDispensesBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesUsBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesNonSyncBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesBetweenStartDateAndEndDate(Date startDate, Date endDate) throws SQLException;

    public List<StockReportData> getStockAlertReportMonthPeriod() throws SQLException;

    public List<Dispense> getDispensesBetweenNextPickupDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getAbsentPatientsBetweenNextPickppDateStartDateAndEndDateWithLimit( Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getActivePatientsBetweenNextPickppDateStartDateAndEndDateWithLimit( Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getAllDispensesToRemoveByDates(Date dateToRemove) throws SQLException;

    public void deleteDispenseAndDispensedDrugs(Dispense dispense) throws SQLException;

    public List<Dispense> getAllDispensesByPrescription(Prescription prescription) throws SQLException;
}
