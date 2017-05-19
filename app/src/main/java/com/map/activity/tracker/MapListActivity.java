package com.map.activity.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

//List activity to display list of card view with saved routes information.
public class MapListActivity extends AppCompatActivity implements RecyclerViewAdapter.ClickListener {
    private MapUtils mapUtils;
    private ArrayList<ListData> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        mapUtils = MapUtils.getInstance();
        RecyclerView listView = (RecyclerView) findViewById(R.id.recyclerview);
        TextView textView = (TextView) findViewById(R.id.listinfo);

        arrayList = mapUtils.getAllRouteList();
        if (arrayList != null) {
            textView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(arrayList);
            recyclerViewAdapter.setOnItemClickListener(this);
            listView.setHasFixedSize(true);
            listView.setAdapter(recyclerViewAdapter);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            listView.setLayoutManager(llm);

        } else {
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    //starting activity when item clicked to display saved route.
    @Override
    public void onItemClick(int position, View v) {
        ArrayList<LatLng> list;
        list = mapUtils.getRouteValues(arrayList.get(position).getRouteName());

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("routevalues", list);
        Intent intent = new Intent(MapListActivity.this, MapsActivity.class);
        intent.putExtra("extras", bundle);
        startActivity(intent);

    }
}
