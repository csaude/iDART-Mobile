package mz.org.fgh.idartlite.workSchedule.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mz.org.fgh.idartlite.base.rest.ServiceWatcher;
import mz.org.fgh.idartlite.base.service.BaseService;
import mz.org.fgh.idartlite.model.AppSettings;
import mz.org.fgh.idartlite.model.ClinicInformation;
import mz.org.fgh.idartlite.model.DiseaseType;
import mz.org.fgh.idartlite.model.Dispense;
import mz.org.fgh.idartlite.model.patient.Patient;
import mz.org.fgh.idartlite.model.Prescription;
import mz.org.fgh.idartlite.model.inventory.Iventory;
import mz.org.fgh.idartlite.service.clinicInfo.ClinicInfoService;
import mz.org.fgh.idartlite.service.clinicInfo.IClinicInfoService;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.service.drug.DiseaseTypeService;
import mz.org.fgh.idartlite.service.drug.IDiseaseTypeService;
import mz.org.fgh.idartlite.service.patient.IPatientService;
import mz.org.fgh.idartlite.service.patient.PatientService;
import mz.org.fgh.idartlite.service.prescription.IPrescriptionService;
import mz.org.fgh.idartlite.service.prescription.PrescriptionService;
import mz.org.fgh.idartlite.service.settings.AppSettingsService;
import mz.org.fgh.idartlite.service.settings.IAppSettingsService;
import mz.org.fgh.idartlite.service.stock.IIventoryService;
import mz.org.fgh.idartlite.service.stock.IventoryService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;

import static mz.org.fgh.idartlite.view.home.ui.settings.AppSettingsFragment.REMOVAL_MESSAGE_STATUS;

public class RemoveDataSyncWorker extends Worker {

    public static final String TAG = "data_removal_job";
    private static final long DELAY = 6000;

    public RemoveDataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        IDispenseService dispenseService = new DispenseService(BaseService.getApp(), null);
        IPrescriptionService prescriptionService = new PrescriptionService(BaseService.getApp(), null);
        IClinicInfoService clinicInfoService = new ClinicInfoService(BaseService.getApp(), null);
        IAppSettingsService appSettings = new AppSettingsService(BaseService.getApp(), null);
        IIventoryService inventoryService = new IventoryService(BaseService.getApp(), null);
        IPatientService patientService = new PatientService(BaseService.getApp(), null);
        IDiseaseTypeService diseaseTypeService = new DiseaseTypeService(BaseService.getApp(), null);

        ServiceWatcher watcher = ServiceWatcher.fastCreate(ServiceWatcher.TYPE_REMOVAL);

        try {

            AppSettings appSetting= appSettings.getRemovalDataSettings();

            Date dateToRemove = DateUtilities.getStatisticCalendarDateToRemoveByDate(DateUtilities.getCurrentDate(),Integer.parseInt(appSetting.getValue()));

            List<Patient> patients = patientService.getALLPatient();

            List<DiseaseType> diseaseTypes = diseaseTypeService.getAllDiseaseTypes();

            for (Patient patient: patients) {
                List<ClinicInformation> clinicInformationsToRemove = clinicInfoService.getAllClinicInformationToRemoveByDateAndPatient(patient,dateToRemove);
                for (DiseaseType diseaseType: diseaseTypes) {

                    List<Prescription> prescriptions = prescriptionService.getAllPrescriptionToRemoveByDateAndPatientAndDiseaseType(patient, diseaseType, dateToRemove);

                    List<Dispense> dispensesToRemove = new ArrayList<>();
                    List<Prescription> prescriptionsToRemove = new ArrayList<>();
                    boolean cantRemovePrescription = false;
                    Prescription lastPrescriptionDb = prescriptionService.getLastPatientPrescriptionByDiseaseType(patient, diseaseType);

                    if (!prescriptions.isEmpty()) {
                        Prescription lastPrescription = prescriptions.get(0);


                        if (lastPrescription.equals(lastPrescriptionDb)) {

                            List<Dispense> dispenses = dispenseService.getAllDispensesByPrescription(lastPrescriptionDb);

                            int totalPrescriptionSupply = lastPrescriptionDb.getSupply();
                            if (Utilities.listHasElements(dispenses)) {
                                totalPrescriptionSupply -= dispenses.get(0).getSupply();
                                dispenses.remove(0);
                            }

                            for (Dispense dispense : dispenses) {
                                if (!dispense.isSyncStatusReady(dispense.getSyncStatus()) || dispense.isVoided()) {
                                    dispensesToRemove.add(dispense);
                                }
                                totalPrescriptionSupply -= dispense.getSupply();
                            }
                            lastPrescriptionDb.setSupply(totalPrescriptionSupply);
                            prescriptions.remove(0);
                        }

                        for (Prescription prescriptionToRemove :
                                prescriptions) {

                            List<Dispense> dispenses = dispenseService.getAllDispensesByPrescription(prescriptionToRemove);

                            for (Dispense dispense :
                                    dispenses) {
                                if (!dispense.isSyncStatusReady(dispense.getSyncStatus()) || dispense.isVoided()) {
                                    dispensesToRemove.add(dispense);
                                } else {
                                    cantRemovePrescription = true;
                                }
                            }

                            if (!cantRemovePrescription)
                                prescriptionsToRemove.add(prescriptionToRemove);
                        }

                    }


                if ( Utilities.listHasElements(clinicInformationsToRemove)) {
                    ClinicInformation clinicInformationFirst = clinicInformationsToRemove.get(0);
                    ClinicInformation lastClinicInformationPatient= clinicInfoService.getLastPatientClinicInformation(patient);

                    if(clinicInformationFirst.equals(lastClinicInformationPatient)) clinicInformationsToRemove.remove(0);
                }

                for (Dispense dispense : dispensesToRemove) {
                    dispenseService.deleteDispenseAndDispensedDrugs(dispense);
                }
                for (Prescription prescriptionToRemove:
                        prescriptionsToRemove) {
                    prescriptionService.deletePrescriptionAndPrescribedDrugs(prescriptionToRemove);
                }
                for (ClinicInformation clinicInformation:
                        clinicInformationsToRemove){
                    clinicInfoService.deleteClinicInfo(clinicInformation);
                }

                prescriptionService.updatePrescriptionEntity(lastPrescriptionDb);
           }
            }

           try {
                List<Iventory> inventorysToRemove = inventoryService.getPastInventoryToRemove(dateToRemove);
               Iventory lastInventory= inventoryService.getLastInventory();
               if (inventorysToRemove != null && Utilities.listHasElements(inventorysToRemove))
               if(inventorysToRemove.get(0).equals(lastInventory)) inventorysToRemove.remove(0);
               for (Iventory iventory : inventorysToRemove) {
                   inventoryService.removeInventoryAndStockAdjusment(iventory);
               }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        watcher.addUpdates("Remoção de Dados Terminada");
        while (!watcher.hasUpdates()){
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException exception) {

            }
        }

        Data outputData = new Data.Builder().putString(REMOVAL_MESSAGE_STATUS, watcher.getUpdates()).build();

        return Result.success(outputData);
    }
}
