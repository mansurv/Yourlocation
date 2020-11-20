package com.netmontools.lookatnet.ui.log_view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.netmontools.lookatnet.App;
import com.netmontools.lookatnet.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class LogFragment extends Fragment {

    private LogViewModel logViewModel;
    private MenuItem mMenuItemRead;
    public TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logViewModel =
                ViewModelProviders.of(this).get(LogViewModel.class);
        View root = inflater.inflate(R.layout.fragment_log, container, false);

        textView = root.findViewById(R.id.text_log);
        openFile("log.dat");
        /*logViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.activity_log, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                return true;
            case R.id.action_read:
                //mToolbar.setTitle("Previous log");
                openFile("log1.dat");
                return true;
            default:
                return false;
        }
    }

    private void openFile(String fileName) {
        try {
            InputStream inputStream = App.instance.openFileInput(fileName);
            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                inputStream.close();
                textView.setText(builder.toString());
            }

        } catch (Throwable t) {
            Toast.makeText(App.instance,
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
}