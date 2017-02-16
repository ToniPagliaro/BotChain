package com.example.tonipagliaro.botchain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class QuestActivity extends AppCompatActivity {

    TextView text_bot, text_os, text_username, text_userhome;

    static ApplicationState appstate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_bot);

        appstate = (ApplicationState) this.getApplication();

        String address = this.getIntent().getExtras().getString("bot");

        text_bot = (TextView) this.findViewById(R.id.textView_Address);
        text_os = (TextView) this.findViewById(R.id.textView_OS);
        text_username = (TextView) this.findViewById(R.id.textView_Username);
        text_userhome = (TextView) this.findViewById(R.id.textView_Userhome);

        text_bot.setText(address);

        String os = appstate.db.getOS(address);
        String username = appstate.db.getUsername(address);
        String userhome = appstate.db.getUserhome(address);

        if (os != "")
            text_os.setText(os);
        if (username != "")
            text_username.setText(username);
        if (userhome != "")
            text_userhome.setText(userhome);

    }
}
