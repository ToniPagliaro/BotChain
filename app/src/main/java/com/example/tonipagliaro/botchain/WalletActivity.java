package com.example.tonipagliaro.botchain;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WalletActivity extends AppCompatActivity {

    ApplicationState appState;

    TextView textView_Wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        appState = (ApplicationState) this.getApplication();

        textView_Wallet = (TextView) this.findViewById(R.id.textView_wallet);
        String balance = appState.wallet.getBalance().toFriendlyString();
        textView_Wallet.setText("Balance: " +balance +"\n");

        new TaskWalletInfo().execute();
    }


    private class TaskWalletInfo extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... params) {
            String wallet = appState.wallet.toString(true, false, false, null);
            return wallet;
        }

        @Override
        protected void onPostExecute(String s) {
            textView_Wallet.append("-- WALLET --\n" +s);
        }
    }
}
