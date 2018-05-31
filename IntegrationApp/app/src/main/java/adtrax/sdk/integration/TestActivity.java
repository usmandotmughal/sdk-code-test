package adtrax.sdk.integration;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.packageList)
    AutoCompleteTextView packageListView;

    @BindView(R.id.testButton)
    Button mTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedPackage = packageListView.getText().toString();
                if (TextUtils.isEmpty(selectedPackage)) {
                    packageListView.setError(getString(R.string.set_packageName));
                }
                else {
                    startTesting(selectedPackage);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<ApplicationInfo> packageNames = this.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        for (int i = 0; i < packageNames.size(); i ++) {
            Log.e("almond", packageNames.get(i).packageName);
        }

        PackageAdapter adapter = new PackageAdapter(this, packageNames);

        packageListView.setThreshold(2);
        packageListView.setAdapter(adapter);
    }

    private void startTesting(final String packageName) {
        if (!isPackageExisted(packageName)) {
            showAlertDialog(getString(R.string.no_exist_package), getString(R.string.check_package_name));
            return;
        }

        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent.setAction("com.android.vending.INSTALL_REFERRER");
        intent.setPackage(packageName);

        List<ResolveInfo>  info = pm.queryBroadcastReceivers(intent, 0);
        Toast.makeText(this, R.string.check_configuration, Toast.LENGTH_SHORT);

        if (info.size() == 0) {
            showAlertDialog(getString(R.string.no_installer), getString(R.string.check_receiver));
            return;
        }

        Intent startIntent = pm.getLaunchIntentForPackage(packageName);

        if (startIntent == null) {
            Toast.makeText(this, R.string.no_exist_activity, Toast.LENGTH_SHORT);
            return;
        }

        this.startActivity(startIntent);

        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent();
                intent.setAction("com.android.vending.INSTALL_REFERRER");
                intent.setPackage(packageName);
                intent.putExtra("referrer", "ADTRax_Test");
                intent.putExtra("TestingIntegrationMode", "true");
                intent.putExtra("gaid", "0");
                sendBroadcast(intent);
            }
        };

        mHandler.sendEmptyMessageDelayed(0, 1000);

    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(targetPackage, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
