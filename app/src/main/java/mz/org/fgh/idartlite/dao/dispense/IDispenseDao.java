package mz.org.fgh.idartlite.dao.dispense;

import android.app.Application;

import mz.org.fgh.idartlite.dao.generic.IGenericDao;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IDispenseDao extends IGenericDao<Dispense, Integer> {

    List<Dispense> getAllNotVoidedByPrescription(Prescription prescription) throws SQLException;

    long countAllOfPrescription(Prescription prescription) throws SQLException;

    public List<Dispense> getAllOfPatient(Application application, Patient patient) throws SQLException;

    public Dispense getLastDispensePrescription(Prescription prescription)  throws SQLException;

    public List<Dispense> getDispensesBetweenStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate,long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesNonSyncBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate,long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesBetweenStartDateAndEndDate(Application application,Date startDate, Date endDate) throws SQLException;

    public List<Dispense> getAllDispensesByStatusAndNotVoided(String status) throws SQLException;

    public List<Dispense> getDispensesBetweenNextPickppDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getDispensesBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getAbsentPatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getActivePatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Application application,Date startDate, Date endDate, long offset, long limit) throws SQLException;

    public List<Dispense> getAllDispensesToRemoveByDates(Date dateToRemove) throws SQLException;

    public List<Dispense> getAllByPrescription(Prescription prescription) throws SQLException;
}
