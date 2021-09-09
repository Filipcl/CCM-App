package com.example.android.androidskeletonapp.ui.cold_chain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class ColdChain extends AppCompatActivity {

    public static Intent getIntent(Context context){
        return new Intent(context, ColdChain.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cold_chain);
        Toolbar toolbar = findViewById(R.id.coldChainToolbar);

        setContentView(R.layout.content_cold_chain);
        TextView teival = findViewById(R.id.teiInfo);
        TextView tempval = findViewById(R.id.temperatureData);
        TextView createdval = findViewById(R.id.created);
        View addTemp = findViewById(R.id.addBtn);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Observable<D2Progress> downloadData = downloadTrackedEntityInstances();
        System.out.println("download data : " + downloadData);
        Observable<D2Progress> downloadEvents = downloadSingleEvents();
        System.out.println("download events : " + downloadEvents);

         String[] TeiUids = new String[] {"JroBkjBFKr6" ,"wJPMhboEUiw" ,"FGsKhDsTBEU" ,"Mk3wwXMD8Rm","uC5WM4DrtPN","t2YszIriCWS"};

         //Denne iden funker - jeg får events på FGsKhDsTBEU
        String testTeiUid = "FGsKhDsTBEU";
        String enrollmentId = "6";

        List<String> teiUids = getTeiUids();

        for(int i = 0; i < teiUids.size(); i++){
           System.out.println("TEIUIDS :: " + teiUids.get(i));
        }
        List<Enrollment> enrollmentID = getEnrollmentIds(testTeiUid);
        for(int i = 0; i < enrollmentID.size(); i++){
            System.out.println("EnrollmentUIDS :: " + enrollmentID.get(i));
        }
        List<Event> eventList = getEvents(enrollmentId);

        System.out.println("Eventlist ::-- " + eventList);

        getEventsTeiData(testTeiUid);

        //setter temperatur verdien til string
        teival.setText("TeiUID : " + testTeiUid + "\n EnrollemntID : " + enrollmentId);
        tempval.setText("Temperature: " + getTeiTempValue(testTeiUid));
        createdval.setText("Created: " + getTeiCreatedValue(testTeiUid));

    }

    private String getTeiTempValue(String testTeiUid){
        List<TrackedEntityDataValue> teiAttributeValues= getEventsTeiData(testTeiUid);

        for (int i = 0; i < teiAttributeValues.size() ; i++) {
            System.out.println("Temp-data: " + teiAttributeValues.get(i).value());
            return teiAttributeValues.get(i).value();
        }
        return null;
    }

    private String getTeiCreatedValue(String testTeiUid){
        List<TrackedEntityDataValue> teiAttributeValues= getEventsTeiData(testTeiUid);

        for (int i = 0; i < teiAttributeValues.size() ; i++) {
            System.out.println("Created: " + teiAttributeValues.get(i).created());
            return teiAttributeValues.get(i).created().toString();
        }
        return null;
    }

    //Printer ut tei values - temp finner du unner value
    private List<TrackedEntityDataValue> getEventsTeiData(String testTeiUid){
        List<Event> eventDir= getEventsDirectly(testTeiUid);
        for (int i = 0; i < eventDir.size() ; i++) {
           return eventDir.get(i).trackedEntityDataValues();
        }
        return null;
    }

    // henter events direkte ved å bruke teiuid
    private List<Event> getEventsDirectly(String testTeiUid){
        return Sdk.d2().eventModule().events().byTrackedEntityInstanceUids(Collections.singletonList(testTeiUid)).withTrackedEntityDataValues().blockingGet();
    }

    private List<Event> getEvents(String testEnrollmentId){
        return  Sdk.d2().eventModule().events().byEnrollmentUid().eq(testEnrollmentId).withTrackedEntityDataValues().blockingGet();
    }

    private List<Enrollment> getEnrollmentIds(String testTeiUid){
           return Sdk.d2().enrollmentModule().enrollments().byTrackedEntityInstance().eq(testTeiUid).blockingGet();
    }

    private List<String> getTeiUids(){
        return Sdk.d2().trackedEntityModule().trackedEntityInstances().blockingGetUids();
    }


    private Observable<D2Progress> downloadTrackedEntityInstances() {
        return Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
    }

    private Observable<D2Progress> downloadSingleEvents() {
        return Sdk.d2().eventModule().eventDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
    }

    public void toast(View v) {
        Toast.makeText(ColdChain.this, "Laster opp ny data fra sensor",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}


