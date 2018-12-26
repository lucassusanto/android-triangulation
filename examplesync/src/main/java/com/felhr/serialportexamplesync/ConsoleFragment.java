package com.felhr.serialportexamplesync;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class ConsoleFragment extends Fragment {
    private TextView txConsole;
    private EditText txCmd;
    private Button btnSend;
    private Button btnClear;

    // MainActivity <- ConsoleFragment
    OnConsoleMessageListener consoleMessageListener;

    public interface OnConsoleMessageListener {
        public void onNewCommandInvoked(String message);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_console, container, false);

        txConsole = view.findViewById(R.id.txConsole);
        txCmd = view.findViewById(R.id.txCmd);
        btnSend = view.findViewById(R.id.btnSend);
        btnClear = view.findViewById(R.id.btnClear);

        txConsole.setMovementMethod(new ScrollingMovementMethod());

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String msg = txCmd.getText().toString();

            if(!msg.equals("")) {
                consoleMessageListener.onNewCommandInvoked(msg);

                txConsole.append("> " + msg + "\n");
                txCmd.setText("");
            }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txConsole.setText("");
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            consoleMessageListener = (OnConsoleMessageListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must override onNewCommandInvoked");
        }
    }

    // ConsoleFragment <- MainActivity
    public void appendToConsole(String msg) {
        txConsole.append(msg);
    }
}
