package com.example.tonipagliaro.botchain;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class BotListActivity extends AppCompatActivity {
    ArrayList<String> indirizzi=new ArrayList<String>();
    ApplicationState appState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        appState = (ApplicationState) getApplication();

       // String a="jakhaklfha";
        //String b="djfhakjfhkahfkl";
        indirizzi=appState.indirizzi;


       // indirizzi.add(a);
        //indirizzi.add(b);

/*        Bundle bundle=new Bundle();
        bundle.putStringArrayList("listaBot", indirizzi);
        for(String s : indirizzi)
            Log.e("Bot ListActivity indirizzi",s.toString());

        BotListActivityFragment bf=new BotListActivityFragment();
        bf.setArguments(bundle);
*/

        setContentView(R.layout.activity_bot_list);
    }

public ArrayList<String> getIndirizzi(){
    return indirizzi;
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
