package com.example.tonipagliaro.botchain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Toni Pagliaro on 28/11/2016.
 */
public class BotListActivityFragment extends Fragment {
    public BotListActivityFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bot_list, container,false);


        String[] bots = {
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
                "mkYQv8TyXFUXFYZCxHTywcZnv75r5z9s7b",
        };



        List<String> listBots = new ArrayList<String>(Arrays.asList(bots));



        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,R.id.list_bot_textview,listBots);



        ListView listView=(ListView)rootView.findViewById(R.id.listView_bot);

        View header = inflater.inflate(R.layout.header_list,container,false);
        listView.addHeaderView(header);
        listView.setAdapter(adapter);
        return rootView;
    }
}
