package mz.org.fgh.idartlite.view.reports;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mz.org.fgh.idartlite.R;
import mz.org.fgh.idartlite.adapter.recyclerview.report.PatientAwaitingStatisticDispenseReportAdapter;
import mz.org.fgh.idartlite.base.activity.BaseActivity;
import mz.org.fgh.idartlite.base.viewModel.BaseViewModel;
import mz.org.fgh.idartlite.databinding.ActivityDispenseByDispenseTypeReportBinding;
import mz.org.fgh.idartlite.databinding.ActivityPatientsAwaitingStatisticsBinding;
import mz.org.fgh.idartlite.listener.recyclerView.IOnLoadMoreListener;
import mz.org.fgh.idartlite.service.dispense.DispenseService;
import mz.org.fgh.idartlite.service.dispense.IDispenseService;
import mz.org.fgh.idartlite.util.DateUtilities;
import mz.org.fgh.idartlite.util.Utilities;
import mz.org.fgh.idartlite.view.about.AboutActivity;
import mz.org.fgh.idartlite.viewmodel.report.AwatingPatientsStatisticReportVM;
import mz.org.fgh.idartlite.viewmodel.report.DispenseByDispenseTypeStatisticReportVM;

public class DispenseByDispenseTypeReportActivity extends BaseActivity {

    String end;
    private RecyclerView recyclerDispenses;
    private ActivityDispenseByDispenseTypeReportBinding dispenseStatisticReportBinding;
    private PatientAwaitingStatisticDispenseReportAdapter adapter;
    private IDispenseService dispenseService;

    private static final String TAG = "DispenseStatisticReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dispenseStatisticReportBinding=   DataBindingUtil.setContentView(this, R.layout.activity_dispense_by_dispense_type_report);

        dispenseService= new DispenseService(getApplication(), getCurrentUser());
        recyclerDispenses = dispenseStatisticReportBinding.reyclerPatient;

        dispenseStatisticReportBinding.setViewModel(getRelatedViewModel());

        dispenseStatisticReportBinding.executePendingBindings();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerDispenses.setLayoutManager(layoutManager);
        recyclerDispenses.setHasFixedSize(true);
        recyclerDispenses.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        if (adapter == null) {
            // try {
            //  adapter = new ContentListDispenseAdapter(recyclerDispenses, dispenseService.getAllDispenses(), this);
            // } catch (SQLException e) {
            //    e.printStackTrace();
            // }
            recyclerDispenses.setAdapter(adapter);
        }

        dispenseStatisticReportBinding.edtSearchParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(DispenseByDispenseTypeReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        dispenseStatisticReportBinding.edtSearchParam.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        dispenseStatisticReportBinding.edtSearchParam.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    int mYear, mMonth, mDay;

                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(DispenseByDispenseTypeReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            dispenseStatisticReportBinding.edtSearchParam.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);


                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

        dispenseStatisticReportBinding.edtSearchParam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(DispenseByDispenseTypeReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        dispenseStatisticReportBinding.edtSearchParam2.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);


                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        dispenseStatisticReportBinding.edtSearchParam2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    int mYear, mMonth, mDay;

                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(DispenseByDispenseTypeReportActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            dispenseStatisticReportBinding.edtSearchParam2.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);



                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

    }

    @SuppressLint("RestrictedApi")
    public void generatePdfButton(boolean show){
        FloatingActionButton generatePdf = dispenseStatisticReportBinding.generatePdf;
        if(show) generatePdf.setVisibility(View.VISIBLE);
        else {generatePdf.setVisibility(View.GONE);}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //Back button
            case R.id.about:
                //If this activity started from other activity
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void displaySearchResult() {
        if (adapter == null) {

            adapter = new PatientAwaitingStatisticDispenseReportAdapter(recyclerDispenses, getRelatedViewModel().getProcessedRecs(), this);

            recyclerDispenses.setAdapter(adapter);
        }

        /*if (adapter.getOnLoadMoreListener() == null) {
            adapter.setOnLoadMoreListener(new IOnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    getRelatedViewModel().loadMoreRecords(recyclerDispenses, adapter);
                }
            });
        }*/

    }

    public void createPdfDocument() throws IOException, DocumentException {
        createPdf(getRelatedViewModel().getAllDisplyedRecords());
    }


    @SuppressLint("LongLogTag")
    private void createPdf(List dispenses) throws IOException, DocumentException {
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/sdcard");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }
        String pdfname = "ResumoPacientesEsperados"+ DateUtilities.formatToDDMMYYYY(new Date())+".pdf";
        File pdfFile = new File(docsFolder.getAbsolutePath(), pdfname);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4);

        PdfPTable tableImage = new PdfPTable(1);
        tableImage.setWidthPercentage(100);
        tableImage.setWidths(new float[]{3});
        tableImage.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        tableImage.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        tableImage.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;

        Drawable d = getResources().getDrawable(R.mipmap.ic_misau);
        Bitmap bmp =((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = Image.getInstance(stream.toByteArray());
        image.setWidthPercentage(80);
        image.scaleToFit(105,55);
        cell = new PdfPCell(image);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setUseAscender(true);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(2f);
        tableImage.addCell(cell);

        PdfPTable table = new PdfPTable(new float[]{4, 4, 3, 3, 3});
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(50);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(getString(R.string.regime_terapeutico));
        table.addCell(getString(R.string.dispensa_mensal_dm));
        table.addCell(getString(R.string.dispensa_trimestral_dt));
        table.addCell(getString(R.string.dispensa_semestral));
        table.addCell(getString(R.string.total_dispensas));
        table.setHeaderRows(1);

        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(BaseColor.GRAY);
        }

        for (Object dispense : dispenses){

            HashMap<String, Object> itemresult = (HashMap<String, Object>) (Object) dispense;

            table.addCell(String.valueOf(itemresult.get("regimeTerapeutico")));
            table.addCell(String.valueOf(itemresult.get("totalGeralDM")));
            table.addCell(String.valueOf(itemresult.get("totalGeralDT")));
            table.addCell(String.valueOf(itemresult.get("totalGeralDS")));
            table.addCell(String.valueOf(itemresult.get("totalGeral")));
        }

        PdfWriter.getInstance(document, output);
        document.open();
        document.add(tableImage);
        // document.add(image);
        Font f = new Font(Font.FontFamily.TIMES_ROMAN, 16.0f, Font.UNDERLINE, BaseColor.RED);
        Font g = new Font(Font.FontFamily.TIMES_ROMAN, 20.0f, Font.NORMAL, BaseColor.RED);

        Paragraph titulo = new Paragraph(" Dispensas por Regime e Tipo de Dispensa\n", g);
        titulo.setAlignment(Element.ALIGN_CENTER);

        Paragraph subTitulo = new Paragraph("Período de "+getRelatedViewModel().getSearchParam()+" à "+getRelatedViewModel().getSearchParam2()+ "\n\n", f);
        subTitulo.setAlignment(Element.ALIGN_CENTER);

        document.add(titulo);
        document.add(subTitulo);

        document.add(table);

        document.close();

        Utilities.previewPdfFiles(this,pdfFile );
    }


    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(DispenseByDispenseTypeStatisticReportVM.class);
    }

    @Override
    public DispenseByDispenseTypeStatisticReportVM getRelatedViewModel() {
        return (DispenseByDispenseTypeStatisticReportVM) super.getRelatedViewModel();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}