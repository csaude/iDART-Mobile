package mz.org.fgh.idartlite.service.dispense;

import android.app.Application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.DispensedDrug;
import mz.org.fgh.idartlite.model.Drug;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.Stock;
import mz.org.fgh.idartlite.model.StockReportData;
import mz.org.fgh.idartlite.model.User;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.service.stock.IStockAlertService;
import mz.org.fgh.idartlite.service.stock.StockAlertService;
import mz.org.fgh.idartlite.service.stock.StockService;
import mz.org.fgh.idartlite.util.DateUtilities;

public class DispenseService extends BaseService<Dispense> implements IDispenseService {

    private StockService stockService;

    private PrescriptionService prescriptionService;

    private DispenseDrugService dispenseDrugService;

    private IStockAlertService stockAlertService;

    public DispenseService(Application application, User currUser) {
        super(application, currUser);

        initServices(application, currUser);

    }

    private void initServices(Application application, User currUser) {
        this.stockService = new StockService(application, currUser);
        this.prescriptionService = new PrescriptionService(application, currUser);
        this.dispenseDrugService = new DispenseDrugService(application, currUser);
        this.stockAlertService = new StockAlertService(application, currUser);
    }

    public DispenseService(Application application) {
        super(application);
        initServices(application, null);
    }

    @Override
    public void save(Dispense record) throws SQLException {

    }

    @Override
    public void update(Dispense record) throws SQLException {

    }

    public List<Dispense> getAllNotVoidedDispenseByPrescription(Prescription prescription) throws SQLException {
        return getDataBaseHelper().getDispenseDao().getAllNotVoidedByPrescription(prescription);
    }

    public void createDispense(Dispense dispense) throws SQLException {

        getDataBaseHelper().getDispenseDao().create(dispense);

        if (dispense.getDispensedDrugs() != null) {
            this.saveDispensedDrugs(dispense.getDispensedDrugs(), dispense);
        }
    }

    public void saveDispensedDrugs(List<DispensedDrug> dispensedDrugs, Dispense dispense) throws SQLException {
        for (DispensedDrug dispensedDrug : dispensedDrugs) {
            dispensedDrug.setDispense(dispense);
            getDataBaseHelper().getDispensedDrugDao().create(dispensedDrug);

            this.updateStock(dispensedDrug);
        }
    }

    public void udpateDispense(Dispense dispense) throws SQLException {
        getDataBaseHelper().getDispenseDao().update(dispense);
    }

    public void deleteDispense(Dispense dispense) throws SQLException {
        getDataBaseHelper().getDispenseDao().delete(dispense);
        this.putBackStockMovimentForDispensedDrug(dispense);

        Prescription prescription = dispense.getPrescription();
        if (prescription.getExpiryDate() != null) {
            prescription.setExpiryDate(null);
            this.prescriptionService.updatePrescriptionEntity(prescription);
        }
    }


    public List<Dispense> getAllOfPatient(Patient patient) throws SQLException {

        return getDataBaseHelper().getDispenseDao().getAllOfPatient(getApplication(), patient);
    }

    public Dispense getLastDispenseFromPrescription(Prescription prescription) throws SQLException {

        return getDataBaseHelper().getDispenseDao().getLastDispensePrescription(prescription);
    }

    public long countAllOfPrescription(Prescription prescription) throws SQLException {
        return getDataBaseHelper().getDispenseDao().countAllOfPrescription(prescription);
    }

    public void saveOrUpdateDispense(Dispense dispense, boolean updateStock) throws SQLException {
        getDataBaseHelper().getDispenseDao().createOrUpdate(dispense);
        List<DispensedDrug> dispensedDrugs = this.dispenseDrugService.findDispensedDrugByDispenseId(dispense.getId());

        if (dispensedDrugs.size() == 0) {
            this.saveOrUpdateDispensedDrugs(dispense.getDispensedDrugs(), dispense, updateStock);
        } else {
            this.saveOrUpdateDispensedDrugs(dispensedDrugs, dispense, updateStock);
        }

        if (dispense.getPrescription().getExpiryDate() != null) {
            Prescription prescription = dispense.getPrescription();
            prescription.setExpiryDate(dispense.getPickupDate());
        //    prescription.setSyncStatus(BaseModel.SYNC_SATUS_SENT);
            this.prescriptionService.updatePrescriptionEntity(prescription);
        }
    }

    public void saveOrUpdateDispensedDrugs(List<DispensedDrug> dispensedDrugs, Dispense dispense, boolean updateStock) throws SQLException {
        for (DispensedDrug dispensedDrug : dispensedDrugs) {
            dispensedDrug.setDispense(dispense);
            getDataBaseHelper().getDispensedDrugDao().createOrUpdate(dispensedDrug);

            if (updateStock) this.updateStock(dispensedDrug);
        }
    }

    public List<Dispense> getAllDispensesByStatus(String status) throws SQLException {

        List<Dispense> typeList = getDataBaseHelper().getDispenseDao().getAllDispensesByStatusAndNotVoided(status);

        if (typeList != null)
            if (!typeList.isEmpty())
                return typeList;

        return null;
    }

    private void updateStock(DispensedDrug dispensedDrug) throws SQLException {

        Stock stock = dispensedDrug.getStock();
        int actualStockMoviment = stock.getStockMoviment();
        int qtySupplied = dispensedDrug.getQuantitySupplied();
        int finalStockMoviment = actualStockMoviment - qtySupplied;

        stock.setStockMoviment(finalStockMoviment);

        if (stock.isSyncStatusReady(BaseModel.SYNC_SATUS_READY))
            stock.setSyncStatus(BaseModel.SYNC_SATUS_READY);
        else if (stock.isSyncStatusReady(BaseModel.SYNC_SATUS_SENT))
            stock.setSyncStatus(BaseModel.SYNC_SATUS_UPDATED);

        this.stockService.updateStock(stock);
    }

    private void putBackStockMovimentForDispensedDrug(Dispense dispense) throws SQLException {

        List<DispensedDrug> dispensedDrugs = this.dispenseDrugService.findDispensedDrugByDispenseId(dispense.getId());

        for (DispensedDrug dispensedDrug : dispensedDrugs
        ) {
            Stock stock = dispensedDrug.getStock();
            int currentStockMoviment = stock.getStockMoviment();
            int qtySupplied = dispensedDrug.getQuantitySupplied();
            int finalStockMoviment = currentStockMoviment + qtySupplied;

            stock.setStockMoviment(finalStockMoviment);
            if (stock.isSyncStatusReady(BaseModel.SYNC_SATUS_READY))
                stock.setSyncStatus(BaseModel.SYNC_SATUS_READY);
            else if (stock.isSyncStatusReady(BaseModel.SYNC_SATUS_SENT))
                stock.setSyncStatus(BaseModel.SYNC_SATUS_UPDATED);

            this.stockService.updateStock(stock);
        }
    }

    public List<Dispense> getDispensesBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException{
        return getDataBaseHelper().getDispenseDao().getDispensesBetweenStartDateAndEndDateWithLimit(getApplication(),startDate,endDate,offset,limit);
    }

    public List<Dispense> getDispensesUsBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException{
        return getDataBaseHelper().getDispenseDao().getDispensesUsBetweenStartDateAndEndDateWithLimit(getApplication(),startDate,endDate,offset,limit);
    }

    public List<Dispense> getDispensesNonSyncBetweenStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException{
        return getDataBaseHelper().getDispenseDao().getDispensesNonSyncBetweenStartDateAndEndDateWithLimit(startDate,endDate,offset,limit);
    }

    public List<Dispense> getDispensesBetweenStartDateAndEndDate(Date startDate, Date endDate) throws SQLException{
        return getDataBaseHelper().getDispenseDao().getDispensesBetweenStartDateAndEndDate(getApplication(),startDate,endDate);
    }


    public List<StockReportData> getStockAlertReportMonthPeriod() throws SQLException{
        this.stockAlertService.clearOldData();
        Date startDate = null;
        Date endDate = null;

        if (DateUtilities.getDayOfMonth(DateUtilities.getCurrentDate()) >= 20) {
            startDate = DateUtilities.setDays(DateUtilities.getDateOfPreviousDays(DateUtilities.getCurrentDate(),30), 21);
        } else {
            startDate = DateUtilities.setDays(DateUtilities.addMonth(DateUtilities.getCurrentDate(), -2), 21);
        }
        endDate = DateUtilities.setDays(DateUtilities.addMonth(startDate, 1), 20);

        List<Dispense> dispenses= getDispensesBetweenStartDateAndEndDate(startDate, endDate);

        List<DispensedDrug> dispensedDrugs = getDataBaseHelper().getDispensedDrugDao().getDispensedDrugsByDispenses(dispenses);

        Map<Drug,List<DispensedDrug>> drugsMap=new HashMap<>();
        Set<Stock> stocksList=new HashSet<>();
        List<StockReportData> stockReportData= new ArrayList<>();

        for (int i=0;i<dispensedDrugs.size();i++){
            Drug value = null;

            value = dispensedDrugs.get(i).getStock().getDrug();
            List<Stock> stocks= stockService.getAllStocksByDrug(dispensedDrugs.get(i).getStock().getDrug());

            stocksList.add(dispensedDrugs.get(i).getStock());
            stocksList.addAll(stocks);
            Collection<DispensedDrug> al = drugsMap.get(value);

            if (al == null)
            {
                drugsMap.put(value, (List<DispensedDrug>) (al = new ArrayList<DispensedDrug>()));
            }


                al.add(dispensedDrugs.get(i));

        }

        Set<Drug> drugs=drugsMap.keySet();

        for (Drug drug: drugsMap.keySet()) {
            StockReportData stockReport= new StockReportData();

            stockReport.setDrugDescription(drug.getDescription());
            stockReport.setDrug(drug);
            List<DispensedDrug> dispenseDrugs= drugsMap.get(drug);
            int quantityDispensed=0; // same as maxConsumption
            int stockActual=0;

            for (DispensedDrug dispenseD: dispenseDrugs) {
                quantityDispensed+=dispenseD.getQuantitySupplied();
            }

            for (Stock stock : stocksList){
                if( drug.equals(stock.getDrug())){
                    stockActual +=  stock.getStockMoviment();
                }
            }

            if(stockActual==0){
                stockActual=dispenseDrugs.get(0).getStock().getStockMoviment();
            }
          //  stockActual-=quantityDispensed;
          //  int validStock=quantityDispensed/3;

          //  int validStock=quantityDispensed;


            stockReport.setMaximumConsumption(Integer.toString(quantityDispensed));
            stockReport.setValidStock(Integer.toString(quantityDispensed));

        /*    if(validStock >= stockActual) {
                stockReport.setStockDescription("Ruptura de Stock");
            }
            else if(quantityDispensed >= stockActual) {
                stockReport.setStockDescription("Stock Baixo");
            }
            else if(validStock < stockActual) {
                stockReport.setStockDescription(" Acima do Consumo Máximo");
            }*/

            if(quantityDispensed > stockActual) {
                stockReport.setStockDescription("Ruptura de Stock");
            }
            else if(quantityDispensed < stockActual) {
                stockReport.setStockDescription("Acima do Consumo Máximo");
            }
            else if(quantityDispensed == stockActual) {
                stockReport.setStockDescription("Stock Normal");
            }
            this.stockAlertService.save(stockReport);
            stockReportData.add(stockReport);
        }

        return stockReportData;



    }

    @Override
    public List<Dispense> getDispensesBetweenNextPickupDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        return getDataBaseHelper().getDispenseDao().getDispensesBetweenNextPickppDateStartDateAndEndDateWithLimit(getApplication(),startDate,endDate,offset,limit);
    }

    @Override
    public List<Dispense> getAbsentPatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException {

        List<Dispense> dispenses = getDataBaseHelper().getDispenseDao().getAbsentPatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(getApplication(),startDate,endDate,offset,limit);
        List<Dispense> reportDispenses = new ArrayList<>();
        int orderNumber=1;
        for (Dispense dispense:
                dispenses) {
            if(DateUtilities.getDaysBetween(dispense.getNextPickupDate(),endDate) >= 5){
                dispense.setOrderNumber(orderNumber);
                reportDispenses.add(dispense);
                orderNumber++;
            }
        }


        return reportDispenses;
    }


    
    @Override
    public List<Dispense> getActivePatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(Date startDate, Date endDate, long offset, long limit) throws SQLException {
        List<Dispense> dispenses = getDataBaseHelper().getDispenseDao().getActivePatientsBetweenNextPickppDateStartDateAndEndDateWithLimit(getApplication(),startDate,endDate,offset,limit);
//        List<Dispense> reportDispenses = new ArrayList<>();
//        int orderNumber=1;
//        for (Dispense dispense:
//                dispenses) {
//            if(DateUtilities.getDaysBetween(dispense.getNextPickupDate(),endDate) >= 5){
//                dispense.setOrderNumber(orderNumber);
//                reportDispenses.add(dispense);
//                orderNumber++;
//            }
//        }


//        return reportDispenses;
        return dispenses;
    }

    @Override
    public List<Dispense> getAllDispensesToRemoveByDates(Date dateToRemove) throws SQLException {
        return getDataBaseHelper().getDispenseDao().getAllDispensesToRemoveByDates(dateToRemove);
    }

    @Override
    public void deleteDispenseAndDispensedDrugs(Dispense dispense) throws SQLException {

        List<DispensedDrug> dispensedDrugs = this.dispenseDrugService.findDispensedDrugByDispenseId(dispense.getId());
       this.dispenseDrugService.deleteDispensedDrugs(dispensedDrugs);
        getDataBaseHelper().getDispenseDao().delete(dispense);

    }

    @Override
    public List<Dispense> getAllDispensesByPrescription(Prescription prescription) throws SQLException {
        return getDataBaseHelper().getDispenseDao().getAllByPrescription(prescription);
    }


}
