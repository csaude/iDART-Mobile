package mz.org.fgh.idartlite.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mz.org.fgh.idartlite.util.Utilities;

public class ServiceWatcher {

    public static final String SERVICE_RUNNING = "RUNNING";
    public static final String SERVICE_STOPPED = "STOPPED";
    public static final String TYPE_DOWNLOAD = "TYPE_DOWNLOAD";
    public static final String TYPE_UPLOAD = "TYPE_UPLOAD";
    public static final String TYPE_REMOVAL = "TYPE_REMOVAL";
    private String serviceStatus;

    private String serviceName;

    private String requestedUrl;

    private List<String> updateList;

    private String type;

    private int recordsToSend;

    private int sentRecords;
    private String update;

    public ServiceWatcher(String serviceName, String requestedUrl) {
        this.serviceName = serviceName;
        this.requestedUrl = requestedUrl;
        this.type = TYPE_DOWNLOAD;
    }

    public ServiceWatcher(String serviceName, String requestedUrl, String type) {
        this.serviceName = serviceName;
        this.requestedUrl = requestedUrl;
        this.type = type;
    }

    public ServiceWatcher(String serviceName, String requestedUrl, String type, int recordsToSend) {
        this.serviceName = serviceName;
        this.requestedUrl = requestedUrl;
        this.type = type;
        this.recordsToSend = recordsToSend;
    }

    public ServiceWatcher(String type) {
        this.type = type;
    }

    public void setServiceAsRunning(){
        this.serviceStatus = SERVICE_RUNNING;
    }

    public void setServiceAsStopped(){
        this.serviceStatus = SERVICE_STOPPED;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public boolean isRunning(){
        return this.serviceStatus.equals(SERVICE_RUNNING);
    }

    public boolean isStopped(){
        return this.serviceStatus.equals(SERVICE_STOPPED);
    }

    public static ServiceWatcher fastCreate(String serviceName, String requestedUrl){
        return new ServiceWatcher(serviceName, requestedUrl);
    }

    public static ServiceWatcher fastCreate(String type){
        return new ServiceWatcher(type);
    }

    public static ServiceWatcher fastCreate(){
        return new ServiceWatcher(null);
    }

    public static ServiceWatcher fastCreate(String serviceName, String requestedUrl, String type){
        return new ServiceWatcher(serviceName, requestedUrl, type);
    }

    public static ServiceWatcher fastCreateUploadType(String serviceName, String requestedUrl, int recordsToSend){
        return new ServiceWatcher(serviceName, requestedUrl, TYPE_UPLOAD, recordsToSend);
    }

    public static ServiceWatcher fastCreateUploadType(String serviceName, int recordsToSend){
        return new ServiceWatcher(serviceName, null, TYPE_UPLOAD, recordsToSend);
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceWatcher)) return false;
        ServiceWatcher that = (ServiceWatcher) o;
        return serviceName.equals(that.serviceName) &&
                requestedUrl.equals(that.requestedUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, requestedUrl);
    }

    @Override
    public String toString() {
        return "ServiceWatcher{" +
                "serviceStatus='" + serviceStatus + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", requestedUrl='" + requestedUrl + '\'' +
                ", type='" + type + '\'' +
                ", recordsToSend=" + recordsToSend +
                ", sentRecords=" + sentRecords +
                '}';
    }

    public int getRecordsToSend() {
        return recordsToSend;
    }

    public void setRecordsToSend(int recordsToSend) {
        this.recordsToSend = recordsToSend;
    }

    public int getSentRecords() {
        return sentRecords;
    }

    public void setSentRecords(int sentRecords) {
        this.sentRecords = sentRecords;
    }

    public void increaseSentRecords(){
        this.sentRecords++;
    }

    public boolean isAllSent(){
        return this.recordsToSend == this.sentRecords;
    }

    public void setRequestedUrl(String requestedUrl) {
        this.requestedUrl = requestedUrl;
    }

    public boolean isUploadService(){
        return this.type.equals(TYPE_UPLOAD);
    }

    public void addUpdates(String s) {
        if (this.updateList == null) this.updateList = new ArrayList<>();

        this.updateList.add(s);
    }

    public String getUpdates() {
        if (!Utilities.listHasElements(updateList)) return null;

        String msg = null;
        for (String s : updateList){
            msg = Utilities.stringHasValue(msg) ? msg+ System.lineSeparator() + s : s;
        }
        return msg;
    }

    public void setUpdates(String s) {
        this.update = s;
    }

    public String getUpdate() {
        return update;
    }

    public boolean hasUpdates() {
        return Utilities.listHasElements(this.updateList);
    }
}
