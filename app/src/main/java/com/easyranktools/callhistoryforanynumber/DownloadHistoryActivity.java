package com.easyranktools.callhistoryforanynumber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class DownloadHistoryActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_LOG_PERMISSION = 100;

    private ProgressBar progressBar;
    private TextView subText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_history);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        subText = findViewById(R.id.subText);

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_CALL_LOG_PERMISSION);
        } else {
            startDownloadProcess();
        }
    }

    private void startDownloadProcess() {
        new Handler().postDelayed(this::fetchCallHistory, 3500);
    }

    private void fetchCallHistory() {
        StringBuilder builder = new StringBuilder();
        builder.append("Call History\n");
        builder.append("==============================\n\n");

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null, null, null,
                    CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                while (cursor.moveToNext()) {
                    String number = cursor.getString(numberIndex);
                    int type = cursor.getInt(typeIndex);
                    long dateMillis = cursor.getLong(dateIndex);
                    String duration = cursor.getString(durationIndex);

                    String typeStr;
                    switch (type) {
                        case CallLog.Calls.INCOMING_TYPE: typeStr = "Incoming"; break;
                        case CallLog.Calls.OUTGOING_TYPE: typeStr = "Outgoing"; break;
                        case CallLog.Calls.MISSED_TYPE: typeStr = "Missed"; break;
                        case CallLog.Calls.REJECTED_TYPE: typeStr = "Rejected"; break;
                        default: typeStr = "Other";
                    }

                    builder.append("Number: ").append(number).append("\n");
                    builder.append("Type: ").append(typeStr).append("\n");
                    builder.append("Date: ").append(new Date(dateMillis).toString()).append("\n");
                    builder.append("Duration: ").append(duration).append(" sec").append("\n");
                    builder.append("-----------------------------\n");
                }
            }

            generateTxtFile(builder.toString());

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void generateTxtFile(String data) {
        try {

            File file = new File(getExternalFilesDir(null), "my_Call_history.txt");

// ✅ Get content:// URI
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

// ✅ Grant temporary read permission
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Call History"));

            ///
//            File file = new File(getExternalFilesDir(null), "my_call_history.txt");
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(data.getBytes());
//            fos.close();
//
//            shareFile(file);

        } catch (Exception e) {
            Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "My Call History");
        intent.putExtra(Intent.EXTRA_TEXT, "Here is my phone call history.");
        startActivity(Intent.createChooser(intent, "Share Call History"));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CALL_LOG_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadProcess();
            } else {
                Toast.makeText(this, "Permission denied to read call logs", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
