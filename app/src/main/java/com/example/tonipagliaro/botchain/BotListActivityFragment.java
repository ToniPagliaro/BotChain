package com.example.tonipagliaro.botchain;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Toni Pagliaro on 28/11/2016.
 */
public class BotListActivityFragment extends Fragment {

    ApplicationState appState;
    public BotListActivityFragment(){

    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bot_list, container,false);





        BotListActivity activity=(BotListActivity)getActivity();
       // ArrayList<String> indirizzi =activity.getIndirizzi();



       Map<String,String> mappaIndirizzi=activity.getMappaIndirizzi();

        ArrayList<String> indirizzi=new ArrayList<String>();

        for(String s : mappaIndirizzi.keySet()){
            indirizzi.add(s+"stato: "+mappaIndirizzi.get(s));
        }

       // ArrayList<String> indirizzi=getArguments().getStringArrayList("listaBot");
        for(String a : indirizzi)
            Log.d("App","Bot ListActivity Fragment indirizzi"+ a.toString());





        final String[] comandi = {
                "Comando 1",
                "Comando 2",
                "Comando 3",
                "Comando 4",
                "Comando 5",
                "Comando 6",
                "Comando 7",
                "Comando 8",
                "Comando 9",
                "Comando 10",

        };


       // List<String> listBots = new ArrayList<String>(Arrays.asList(bots));




  //     ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,R.id.list_bot_textview,listBots);

 /*       ImageView iv=(ImageView) rootView.findViewById(R.id.img);
        for(String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase("ok"))
                iv.setImageResource(R.drawable.green);
            else {
                iv.setImageResource(R.drawable.red);

            }
        }
*/

       ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,R.id.list_bot_textview,indirizzi);


        ListView listView=(ListView)rootView.findViewById(R.id.listView_bot);

        View header = inflater.inflate(R.layout.header_list,container,false);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //ottenere l'indirizzo del bot selezionato

                final Dialog dialog = new Dialog(getActivity());
                dialog.setTitle("Seleziona comando");
                View v=inflater.inflate(R.layout.popup_window,null,false);
                dialog.setContentView(v);

                List<String> listComandi = new ArrayList<String>(Arrays.asList(comandi));

                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.item_comandi,R.id.commandText,listComandi);
                ListView listView=(ListView)v.findViewById(R.id.list_comandi);

                listView.setAdapter(adapter);


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        //selezione del comando da inviare al bot selezionato
                        dialog.dismiss();
                    }
                });

            dialog.show();


            }
        });

        return rootView;
    }

}
