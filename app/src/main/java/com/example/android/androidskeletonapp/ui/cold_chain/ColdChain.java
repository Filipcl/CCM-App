package com.example.android.androidskeletonapp.ui.cold_chain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.ui.programs.ProgramsActivity;

public class ColdChain extends AppCompatActivity {

    public static Intent getIntent(Context context){
        return new Intent(context, ColdChain.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cold_chain);
    }
}
