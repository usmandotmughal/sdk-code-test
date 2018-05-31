package com.adtrax;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.adtrax.adt_android_sdk.ADTRaxLib;
import com.adtrax.adt_android_sdk.ADTRaxProperties;
import com.adtrax.adt_android_sdk.EventsType;
import com.adtraxsdk.sample.R;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)this.findViewById(R.id.sendInstall);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usermail = ((TextView)findViewById(R.id.userMail)).getText().toString();
                String devkey = ((TextView)findViewById(R.id.devkey)).getText().toString();
                String devsecret = ((TextView)findViewById(R.id.devsecret)).getText().toString();
                String appuserId = ((TextView)findViewById(R.id.appUserID)).getText().toString();
                String channel = ((TextView)findViewById(R.id.channel)).getText().toString();

                ADTRaxLib adtRaxLib = ADTRaxLib.getInstance();

                adtRaxLib.setDevKey(devkey);
                adtRaxLib.setDevSecrect(devsecret);
                adtRaxLib.setAppUserID(appuserId);
                adtRaxLib.setUserEmail(usermail);
                adtRaxLib.setChannel(channel);

                Intent intent = new Intent();
                intent.setAction("com.android.vending.INSTALL_REFERRER");
                intent.putExtra("referrer", "ADTRax_Test");
                intent.putExtra("gaid", "0");
                intent.setPackage("com.adtraxsdk.sample");

                sendBroadcast(intent);
            }
        });

        Button buttonOpen = (Button)this.findViewById(R.id.sendOpen);

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ADTRaxLib.getInstance().clearEvent();
                ADTRaxLib.getInstance().setPrevScreenName("Not");
                ADTRaxLib.getInstance().setCurrentScreenName("MainActivity");

                ADTRaxLib.getInstance().sendEventTracking(EventsType.EVENT_OPEN, EventsType.STATIC_EVENT, true);
            }
        });

        Button buttonScreen = (Button)this.findViewById(R.id.sendScreen);

        buttonScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ADTRaxLib.getInstance().clearEvent();
                ADTRaxLib.getInstance().setPrevScreenName("TestActivity");
                ADTRaxLib.getInstance().setCurrentScreenName("MainActivity");

                ADTRaxLib.getInstance().sendEventTracking(EventsType.EVENT_SCREEN, EventsType.STATIC_EVENT, true);
            }
        });

        Button buttnDynamic = (Button)this.findViewById(R.id.sendDynamic);

        buttnDynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ADTRaxLib.getInstance().clearEvent();
                ADTRaxLib.getInstance().setPrevScreenName("TestActivity");
                ADTRaxLib.getInstance().setCurrentScreenName("MainActivity");
                ADTRaxLib.getInstance().setParam1Event("parameter1");
                ADTRaxLib.getInstance().setParam2Event("parameter2");

                Map<String, Object > e = new HashMap<>();
                e.put(ADTRaxProperties.ADT_CURRENCY, 100);
                e.put(ADTRaxProperties.ADT_CATEGORY, "A");
                e.put(ADTRaxProperties.ADT_QUANTITY, 5);
                ADTRaxLib.getInstance().sendEventTracking("Dynamic", EventsType.DYNAMIC_EVENT, e);
            }
        });

    }
}
