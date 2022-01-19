package com.example.android.androidskeletonapp.ui.cold_chain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;
import com.example.android.androidskeletonapp.ui.main.MyDatabaseHelper;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class ColdChain extends AppCompatActivity {

    public static Intent getIntent(Context context){
        return new Intent(context, ColdChain.class);
    }
    MyDatabaseHelper myDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cold_chain);
        Toolbar toolbar = findViewById(R.id.coldChainToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Sdk.d2().trackedEntityModule().trackedEntityInstanceDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();
        Sdk.d2().eventModule().eventDownloader()
                .limit(10).limitByOrgunit(false).limitByProgram(false).download();

        //Denne iden funker - jeg får events på FGsKhDsTBEU
        //String testTeiUid = "FGsKhDsTBEU";
        //String enrollmentId = "6";

        addEvent("g5oklCs7xIg","SDuMzcGLh8i","aecqgkE5quA", "iMDPax84iAN");
    }

    // Creats a new event on this date foreach element in json file, fetches it's metadata dataElementUid and sets it value to temperature.
    public void addEvent (String enrollmentID, String programUid , String programStageId, String ouUid) {
        String defaultOptionCombo = Sdk.d2().categoryModule().categoryOptionCombos()
                .byDisplayName().eq("default").one().blockingGet().uid();

        try {
            ArrayList<String> dbResult = getDBresult();
            System.out.println("HEEEEERRRR!!!::: " + dbResult);
            String temp = dbResult.get(2).substring(0,4);
            System.out.println("db result temp:  !!! " + temp);

                //makes a new event and returns it eventUID
                String eventUid = Sdk.d2().eventModule().events().blockingAdd(
                        EventCreateProjection.builder()
                                .enrollment(enrollmentID)
                                .program(programUid)
                                .programStage(programStageId)
                                .organisationUnit(ouUid)
                                .attributeOptionCombo(defaultOptionCombo)
                                .build()
                );

                //sets the created-, enrollment- and event-date to this date.
                System.out.println("Ny Event UID :::::  " + eventUid);

                //Super important to have correct date format or it wont upload!!

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = format.format(new Date());
                Date date = format.parse (dateString);

                Sdk.d2().eventModule().events().uid(eventUid).setEventDate(date);
                Sdk.d2().enrollmentModule().enrollments().uid("g5oklCs7xIg").setEnrollmentDate(date);
                Sdk.d2().eventModule().events().uid(eventUid).setCompletedDate(date);


                //Get's the event's dataElement UID and set's a value to the event
                List<ProgramStageDataElement> programDataElem = Sdk.d2().programModule().programStageDataElements().blockingGet();
                for (ProgramStageDataElement elem: programDataElem
                ) {
                    Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, elem.dataElement().uid()).blockingSet(temp);
                    System.out.println(Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, elem.dataElement().uid()).blockingGet());
                }

                //set eventStatus and upload
                Sdk.d2().eventModule().events().uid(eventUid).setStatus(EventStatus.COMPLETED);
                Sdk.d2().eventModule().events().upload();

        } catch (D2Error | ParseException d2Error) {
            d2Error.printStackTrace();
        }
    }


    private ArrayList<String> getDBresult(){
        myDB = new MyDatabaseHelper(ColdChain.this);
        return myDB.returnAllData();
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
