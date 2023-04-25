package mz.org.fgh.idartlite.base.viewModel;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mz.org.fgh.idartlite.BR;
import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.generic.AbstractRecycleViewAdapter;
import mz.org.fgh.idartlite.base.model.BaseModel;
import mz.org.fgh.idartlite.base.rest.BaseRestService;
import mz.org.fgh.idartlite.listener.rest.RestResponseListener;
import mz.org.fgh.idartlite.searchparams.AbstractSearchParams;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;

public abstract class SearchVM<T extends BaseModel> extends BaseViewModel implements SearchPaginator<T>, RestResponseListener<T> {

    public static final int PAGE_SIZE = 20;

    public static final int RECORDS_PER_SEARCH = 100;

    public static final String PREPARING_SEARCH = "PREPARING_SEARCH";

    public static final String PERFORMING_SEARCH = "PERFORMING_SEARCH";

    public static final String LOADING_MORE_RECORDS = "LOADING_MORE_RECORDS";

    public static final String SEARCH_FINISHED = "SEARCH_FINISHED";

    protected boolean paginatedSearch;

    protected List<T> searchResults;

    protected List<T> allDisplyedRecords;

    protected AbstractSearchParams<T> searchParams;

    protected int pageSize;

    protected boolean onlineSearch;

    protected String onlineRequestError;

    protected String searchStatus;

    protected boolean isPdfGeneration;

    protected boolean isFullLoad;

    protected List<?> processedRecs;


    private AbstractRecycleViewAdapter<T> adapter;

    public SearchVM(@NonNull Application application) {
        super(application);

        this.searchStatus = PREPARING_SEARCH;

        this.allDisplyedRecords = new ArrayList<>();
        this.searchResults = new ArrayList<>();
        this.processedRecs = new ArrayList<>();

        activatePaginatedSearch();

        this.searchParams = initSearchParams();
    }

    @Bindable
    public boolean isOnlineSearch() {
        return onlineSearch;
    }

    public void setOnlineSearch(boolean onlineSearch) {
        this.onlineSearch = onlineSearch;
        notifyPropertyChanged(BR.onlineSearch);
    }

    public void changeReportSearchMode(String searchType) {
        this.setOnlineSearch(!searchType.equals(getApplication().getString(R.string.local)));
    }

    public void initSearch() {
        try {
            if (isSearchOnProgress()) return;

            String errors = validateBeforeSearch();
            if (Utilities.stringHasValue(errors)) {
                displayErrors(errors);
                return;
            }

            this.pageSize = getPageSize();

            this.allDisplyedRecords.clear();
            this.searchResults.clear();


            if (isPaginatedSearch()) {
                if (isOnlineSearch() && !canDisplayRecsAfterInitSearch()) {
                    doOnlineSearch(0, RECORDS_PER_SEARCH);
                } else this.searchResults = doSearch(0, RECORDS_PER_SEARCH);
            }else {
                if (isOnlineSearch() && !canDisplayRecsAfterInitSearch()) {
                    doOnlineSearch(0, 0);
                } else this.searchResults = doSearch(0, 0);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!isOnlineSearch() || canDisplayRecsAfterInitSearch()) {
            if (Utilities.listHasElements(this.searchResults)) {
                loadFirstPage();

                changeSearchStatusToFinished();
                this.processedRecs = getAllDisplyedRecords();

                displaySearchResults();
            } else {
                doOnNoRecordFound();
            }

            notifyChange();
        }
    }

    protected boolean canDisplayRecsAfterInitSearch() {
        return false;
    }

    protected void doAfterFirstOnlineSearch() {
        if (!isFullLoad) loadFirstPage();
        displaySearchResults();
        notifyChange();
    }

    @Override
    public void doOnlineSearch(long offset, long limit) throws SQLException {
        if (!Utilities.listHasElements(getSearchResults())) changeSearchStatusToPerforming();
        else changeSearchStatusToLoadingMore();
    }

    public String validateBeforeSearch(){
        if (getSearchParams() == null) return  null;

        if (getSearchParams().isByDateInterval()) return validateDateInterval();

        return null;
    }

    private String validateDateInterval() {
        if (DateUtilities.dateDiff(getSearchParams().getEndDate(), getSearchParams().getStartdate(), DateUtilities.DAY_FORMAT) < 0){
            return "A data inicio deve ser menor que a data fim.";
        }
        return null;
    }

    private void displayErrors(String errors) {
        Utilities.displayAlertDialog(getRelatedActivity(), errors).show();
    }

    protected abstract void doOnNoRecordFound();

    private void loadFirstPage() {
        if (isPaginatedSearch()) {
            if (getSearchResults().size() < pageSize) {
                pageSize = getSearchResults().size();
            }

            for (int i = 0; i <= pageSize - 1; i++) {
                getAllDisplyedRecords().add(getSearchResults().get(i));
            }
        }else {
            getAllDisplyedRecords().clear();
            getAllDisplyedRecords().addAll(getSearchResults());
        }
    }

    public AbstractSearchParams<T> getSearchParams() {
        return searchParams;
    }

    protected void fullLoadRecords() throws SQLException {
        if (!isOnlineSearch()) {
            List<T> recs = getNextRecordsToDisplay();

            while (Utilities.listHasElements(recs)) {
                recs = getNextRecordsToDisplay();
            }
            changeSearchStatusToFinished();

            if (isPdfGeneration) {
                setPdfGeneration(false);
                getLoadingDialog().dismisDialog();
                createPdfDocument();
            }
        } else {
            getAllDisplyedRecords().clear();
            getAllDisplyedRecords().addAll(getSearchResults());


            getNextRecordsToDisplay();
        }
    }

    private List<T> getNextRecordsToDisplay() throws SQLException {

        int end = 0;
        List<T> recs = new ArrayList<>();

        if ((allDisplyedRecords.size()+pageSize) > this.searchResults.size() && !isPdfGeneration()){
            if (isOnlineSearch()) {
                doOnlineSearch(getSearchResults().size(), RECORDS_PER_SEARCH);
            } else recs = doSearch(getSearchResults().size(), RECORDS_PER_SEARCH);

            if (!onlineSearch) {
                if (Utilities.listHasElements(recs)) {
                    this.searchResults.addAll(recs);
                    if ((allDisplyedRecords.size() + pageSize) > this.searchResults.size()) {
                        end = this.searchResults.size() - 1;
                    } else {
                        end = allDisplyedRecords.size() + pageSize - 1;
                    }
                } else {
                    end = this.searchResults.size() - 1;
                }
            }
        }else {
            end = allDisplyedRecords.size()+pageSize-1;
            // if(isPdfGeneration() && isOnlineSearch()) doOnlineSearch(getSearchResults().size(), RECORDS_PER_SEARCH);
            if (onlineSearch && isPdfGeneration) {
                doOnlineSearch(getSearchResults().size(), RECORDS_PER_SEARCH);
              //  loadNewRecords(end);
            }else if(onlineSearch) loadNewRecords(end);
        }

        if (!onlineSearch) {
            if(end >= this.searchResults.size())  end=this.searchResults.size()-1;
            loadNewRecords(end);
            if(allDisplyedRecords.size() != this.searchResults.size())  recs.addAll(allDisplyedRecords);

        }
        return recs;
    }

    private void loadNewRecords(int end) {
        for (int i = allDisplyedRecords.size(); i <= end; i++) {
            allDisplyedRecords.add(this.searchResults.get(i));
        }
    }

    private void doAfterOnlineLoadMoreRecords(List<T> newRecs) {
        if (isPdfGeneration || isFullLoad) {
            if (Utilities.listHasElements(newRecs)) {
                getAllDisplyedRecords().addAll(newRecs);
                try {
                    getNextRecordsToDisplay();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } else {
                changeSearchStatusToFinished();
                if (isPdfGeneration) {
                    setPdfGeneration(false);
                    getLoadingDialog().dismisDialog();
                    createPdfDocument();
                } else {
                    getAllDisplyedRecords().clear();
                    getAllDisplyedRecords().addAll((Collection<? extends T>) doBeforeDisplay(getSearchResults()));

                    this.processedRecs = (List<?>) doBeforeDisplay(getSearchResults());
                    doAfterFirstOnlineSearch();
                }
            }
        } else {
            int end;
            if (Utilities.listHasElements(newRecs)) {
                if ((allDisplyedRecords.size() + pageSize) > this.searchResults.size()) {
                    end = this.searchResults.size() - 1;
                } else {
                    end = allDisplyedRecords.size() + pageSize - 1;
                }
            } else {
                end = this.searchResults.size() - 1;
            }
            getAllDisplyedRecords().remove(getAllDisplyedRecords().size() - 1);
            adapter.notifyItemRemoved(getAllDisplyedRecords().size());

            loadNewRecords(end);

            adapter.notifyDataSetChanged();
            adapter.setLoaded();
        }
    }

    public List<T> getSearchResults() {
        return searchResults;
    }

    @Bindable
    public List<T> getAllDisplyedRecords() {
        return allDisplyedRecords;
    }

    private int getPageSize() {
        return PAGE_SIZE;
    }

    private boolean mustSearchMore() {
        return (allDisplyedRecords.size()+pageSize) > this.searchResults.size();
    }

    public void loadMoreRecords(RecyclerView rv, AbstractRecycleViewAdapter<T> adapter) {
        this.adapter = adapter;

        if (isPaginatedSearch() && getAllDisplyedRecords().size() <= getSearchResults().size() && getSearchResults().size() >= PAGE_SIZE){
            getAllDisplyedRecords().add(null);
            rv.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemInserted(getAllDisplyedRecords().size() - 1);
                }
            });

            new Handler().postDelayed(() -> {
                if (!isOnlineSearch() || !mustSearchMore()) {
                    getAllDisplyedRecords().remove(getAllDisplyedRecords().size() - 1);
                    adapter.notifyItemRemoved(getAllDisplyedRecords().size());
                }

                try {
                    getNextRecordsToDisplay();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                adapter.setLoaded();

            }, getDelayMillis());
        }
    }

    protected int getDelayMillis() {
        if (isOnlineSearch()) return 12000;

        return 6000;
    }

    @Bindable
    public String getStartDate() {
        return DateUtilities.formatToDDMMYYYY(getSearchParams().getStartdate());
    }

    public void setStartDate(String startDate) {
        getSearchParams().setStartdate(DateUtilities.createDate(startDate, DateUtilities.DATE_FORMAT));
        notifyPropertyChanged(BR.startDate);
    }

    @Bindable
    public String getEndDate() {
        return DateUtilities.formatToDDMMYYYY(getSearchParams().getEndDate());
    }

    public void setEndDate(String endDate) {
        getSearchParams().setEndDate(DateUtilities.createDateWithTime(endDate, DateUtilities.END_DAY_TIME, DateUtilities.DATE_TIME_FORMAT));
        notifyPropertyChanged(BR.endDate);
    }

    public void deActivatePaginatedSearch(){
        this.paginatedSearch = false;
    }

    public void activatePaginatedSearch(){
        this.paginatedSearch = true;
    }

    public boolean isPaginatedSearch(){
        return this.paginatedSearch;
    }

    public void generatePDF() {

        getLoadingDialog().startLoadingDialog();

        changeSearchStatusToPerforming();

        setPdfGeneration(true);

        try {
            fullLoadRecords();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void doOnResponse(String flag, List<T> objects) {
        if (flag.equals(BaseRestService.REQUEST_SUCESS)) {
            if (!isFullLoad) {
                this.searchResults.addAll((Collection<? extends T>) doBeforeDisplay(objects));
            }else {
                this.searchResults.addAll(objects);
            }

        } else if (flag.equals(BaseRestService.REQUEST_NO_DATA)) {
            this.onlineRequestError = "Não foram encontrados registos para o período indicado.";
        } else {
            this.onlineRequestError = flag;
        }
        changeSearchStatusToFinished();

        if (!Utilities.listHasElements(objects) && !Utilities.listHasElements(this.searchResults)) {
            doOnNoRecordFound();
        } else if (isFullLoad || ((!Utilities.listHasElements(objects) && Utilities.listHasElements(this.searchResults)) || (Utilities.listHasElements(objects) && this.searchResults.size() > RECORDS_PER_SEARCH))) {
            doAfterOnlineLoadMoreRecords(objects);
        } else if ((Utilities.listHasElements(objects) || this.searchResults.size() <= RECORDS_PER_SEARCH) && !isFullLoad) {
            doAfterFirstOnlineSearch();
        }
    }

    public void setFullLoad(boolean fullLoad) {
        isFullLoad = fullLoad;
    }

    @Override
    public Collection<?> doBeforeDisplay(List<T> objects) {
        return objects;
    }

    protected void changeSearchStatusToPerforming () {
        this.searchStatus = PERFORMING_SEARCH;
        notifyChange();
    }

    protected void changeSearchStatusToFinished() {
        this.searchStatus = SEARCH_FINISHED;
        notifyChange();
    }

    protected void changeSearchStatusToLoadingMore() {
        this.searchStatus = LOADING_MORE_RECORDS;
        notifyChange();
    }

    public boolean isPdfGeneration() {
        return isPdfGeneration;
    }

    public void setPdfGeneration(boolean pdfGeneration) {
        isPdfGeneration = pdfGeneration;
        notifyChange();
    }

    public boolean isSearchOnLoadingMore () {
        return this.searchStatus.equals(LOADING_MORE_RECORDS);
    }

    public boolean isSearchOnProgress () {
        return this.searchStatus.equals(PERFORMING_SEARCH);
    }

    public boolean isSearchOnFinished () {
        return this.searchStatus.equals(SEARCH_FINISHED);
    }


    @Override
    public void doOnRestSucessResponse(String flag) {

    }

    @Override
    public void doOnRestErrorResponse(String errormsg) {

    }

    @Override
    public void doOnRestSucessResponseObjects(String flag, List<T> objects) {

    }

    public List<?> getProcessedRecs() {
        return processedRecs;
    }

}
