package com.example.WatPlan.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.WatPlan.Handlers.ConnectionHandler;
import com.example.WatPlan.R;

public class NotificationActivity extends AppCompatActivity {
    private static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Button downloadButton = findViewById(R.id.downloadButton);
        TextView messageTextView = findViewById(R.id.messageTextView);

        messageTextView.setText(getResources().getString(R.string.new_version_available));
        downloadButton.setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse(ConnectionHandler.getBaseUrl()+ "home")))
        );
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean active) {
        NotificationActivity.active = active;
    }

    @Override
    protected void onStop() {
        super.onStop();
        setActive(false);
    }
}
