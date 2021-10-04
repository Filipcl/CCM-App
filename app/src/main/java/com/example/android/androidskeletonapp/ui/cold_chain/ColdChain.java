package com.example.android.androidskeletonapp.ui.cold_chain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.category.CategoryOptionComboCollectionRepository;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class ColdChain extends AppCompatActivity {


    public static Intent getIntent(Context context){
        return new Intent(context, ColdChain.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cold_chain);
        Toolbar toolbar = findViewById(R.id.coldChainToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setContentView(R.layout.content_cold_chain);
        TextView teival = findViewById(R.id.teiInfo);
        TextView tempval = findViewById(R.id.temperatureData);
        TextView createdval = findViewById(R.id.created);
        View addTemp = findViewById(R.id.addBtn);


        Observable<D2Progress> downloadData = downloadTrackedEntityInstances();
        System.out.println("download data : " + downloadData);
        Observable<D2Progress> downloadEvents = downloadSingleEvents();
        System.out.println("download events : " + downloadEvents);

        String[] TeiUids = new String[] {"JroBkjBFKr6" ,"wJPMhboEUiw" ,"FGsKhDsTBEU" ,"Mk3wwXMD8Rm","uC5WM4DrtPN","t2YszIriCWS"};
        String eventExample = " TrackedEntityDataValue{id=6, event=sLQQrsScOgZ, created=Tue Jun 15 21:30:02 GMT 2021, lastUpdated=Tue Jun 15 21:30:02 GMT 2021, dataElement=zEouq1KNiZO, storedBy=null, value=6.6, providedElsewhere=false}";

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

        List<TrackedEntityDataValue> eventListValues = getEventsTeiData(testTeiUid);

        System.out.println(eventListValues);

        for (int i = 0; i < eventListValues.size() ; i++) {
            System.out.println("EventsValues: " + eventListValues.get(i));
        }


        //setter temperatur verdien til string
        teival.setText("TeiUID : " + testTeiUid + "\n EnrollemntID : " + enrollmentId);
        tempval.setText("Temperature: " + getTeiTempValue(testTeiUid));
        createdval.setText("Created: " + getTeiCreatedValue(testTeiUid));

        addEvent("g5oklCs7xIg","SDuMzcGLh8i","aecqgkE5quA", "iMDPax84iAN");

    }



    //setMetode -- set på vent ;)
    public boolean addEvent (String enrollmentID, String programUid , String programStageId, String ouUid) {
        String defaultOptionCombo = Sdk.d2().categoryModule().categoryOptionCombos()
                .byDisplayName().eq("default").one().blockingGet().uid();
        try {
            String eventUid = Sdk.d2().eventModule().events().blockingAdd(
                    EventCreateProjection.builder()
                            .enrollment(enrollmentID)
                            .program(programUid)
                            .programStage(programStageId)
                            .organisationUnit(ouUid)
                            .attributeOptionCombo(defaultOptionCombo)
                            .build()
            );

            System.out.println("Ny Event UID :::::  " + eventUid);
            Sdk.d2().eventModule().events().uid(eventUid).setEventDate(new Date());
            Sdk.d2().enrollmentModule().enrollments().uid("g5oklCs7xIg").setEnrollmentDate(new Date());
            Sdk.d2().eventModule().events().uid(eventUid).setCompletedDate(new Date());

            Sdk.d2().enrollmentModule().enrollments().uid("g5oklCs7xIg").setStatus(EnrollmentStatus.ACTIVE);

            List<Event> attributes = Sdk.d2().eventModule()
                    .events()
                    .byUid().eq(eventUid)
                    .withTrackedEntityDataValues()
                    .blockingGet();

            TrackedEntityDataValue addTempToEvent = TrackedEntityDataValue.builder().event(eventUid).value("69").created(new Date()).build();

            for (Event at : attributes) {
                at.trackedEntityDataValues().add(addTempToEvent);
                System.out.println("Event  : " + at);
                System.out.println("AT ::: " + at.trackedEntityDataValues().add(addTempToEvent));
                System.out.println(at.trackedEntityDataValues());
            }

            List<ProgramStage> programStages = Sdk.d2().programModule().programStages().blockingGet();


            for ( ProgramStage p : programStages) {
                System.out.println("Innenfor -- : " + p.reportDateToUse() );
            }

            Sdk.d2().eventModule().events().uid(eventUid).setStatus(EventStatus.COMPLETED);
            Sdk.d2().eventModule().events().upload();
            return true;
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
            return false;
        }
    }

    //Metoden fyller inn textview med json-data og viser en toast ved onClick.
    public void addJsonToView(View v) {
        TextView jsonData = findViewById(R.id.tempJson);
        try {
            jsonData.setText(getJson());

        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(ColdChain.this, "Data lagt til",
                Toast.LENGTH_LONG).show();
    }

    // Leser json filen som man finner i Res/raw og returnerer json-objektene som stringer.
    private String getJson() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.temp_small);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        return writer.toString();
    }

    // Henter eventdata på en tei og kegger det til i en liste, itererer over lista og returnerer value som er temperaturen til den tei-en.
    private String getTeiTempValue(String testTeiUid){
        List<TrackedEntityDataValue> teiAttributeValues= getEventsTeiData(testTeiUid);

        for (int i = 0; i < teiAttributeValues.size() ; i++) {
            System.out.println("Temp-data: " + teiAttributeValues.get(i).value());
            return teiAttributeValues.get(i).value();
        }
        return null;
    }

    // Itererer over Tei sine events og returnerer veriden når eventen er opprettet i string format.
    private String getTeiCreatedValue(String testTeiUid){
        List<TrackedEntityDataValue> teiAttributeValues= getEventsTeiData(testTeiUid);

        for (int i = 0; i < teiAttributeValues.size() ; i++) {
            System.out.println("Created: " + teiAttributeValues.get(i).created());
            return teiAttributeValues.get(i).created().toString();
        }
        return null;
    }

    //Returnerer en tei sine datavalues fra en event.
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

    // Kan også hente events på denne måten
    private List<Event> getEvents(String testEnrollmentId){
        return  Sdk.d2().eventModule().events().byEnrollmentUid().eq(testEnrollmentId).withTrackedEntityDataValues().blockingGet();
    }

    // Henter ut ennrolmentid til ein tei
    private List<Enrollment> getEnrollmentIds(String testTeiUid){
        return Sdk.d2().enrollmentModule().enrollments().byTrackedEntityInstance().eq(testTeiUid).blockingGet();
    }

    // Returnerer alle UID til TEI i et program
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
