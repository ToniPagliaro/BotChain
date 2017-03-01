package com.example.tonipagliaro.botchain;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class QuestActivity extends AppCompatActivity {

    TextView text_bot, text_os, text_username, text_userhome, text_ping_of_death, text_balance;

    static ApplicationState appstate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_bot);

        appstate = (ApplicationState) this.getApplication();

        String address = this.getIntent().getExtras().getString("bot");

        text_bot = (TextView) this.findViewById(R.id.textView_address);
        text_os = (TextView) this.findViewById(R.id.textView_os);
        text_username = (TextView) this.findViewById(R.id.textView_username);
        text_userhome = (TextView) this.findViewById(R.id.textView_userhome);
        text_ping_of_death = (TextView) this.findViewById(R.id.textView_ping_of_death);
        text_balance = (TextView) this.findViewById(R.id.textView_balance);

        text_bot.setText("Bot: " +address);

        String os = appstate.db.getOS(address);
        String username = appstate.db.getUsername(address);
        String userhome = appstate.db.getUserhome(address);
        String pingOfDeath = appstate.db.getPingOfDeath(address);
        String balance = appstate.db.getBalance(address);

        if (os != "")
            text_os.setText("OS: " +os);
        if (username != "")
            text_username.setText("Username: " +username);
        if (userhome != "")
            text_userhome.setText("Userhome: " +userhome);
        if (pingOfDeath != "")
            text_ping_of_death.setText("Ping Of Death: " +pingOfDeath);
        if (balance != "")
            text_balance.setText("Balance: " +balance);

    }
}
