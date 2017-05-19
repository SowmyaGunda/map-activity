package com.map.activity.tracker;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

//Recycler adapter to display list of cardviews
class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private static ClickListener clickListener;
    private ArrayList<ListData> myValues;

    RecyclerViewAdapter(ArrayList<ListData> myValues) {
        this.myValues = myValues;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.listviewitem, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.routeName.setText(myValues.get(position).getRouteName());
        holder.startTime.setText(myValues.get(position).getStartTime());
        holder.endTime.setText(myValues.get(position).getEndTime());
    }

    @Override
    public int getItemCount() {
        return myValues.size();
    }

    void setOnItemClickListener(ClickListener clickListener) {
        RecyclerViewAdapter.clickListener = clickListener;

    }

    //interface to provide onitem click functionality to recycler view.
    interface ClickListener {
        void onItemClick(int position, View v);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView routeName;
        private TextView startTime;
        private TextView endTime;

        MyViewHolder(View itemView) {
            super(itemView);
            routeName = (TextView) itemView.findViewById(R.id.text_routename);
            startTime = (TextView) itemView.findViewById(R.id.text_starttime);
            endTime = (TextView) itemView.findViewById(R.id.text_endtime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i("Check log", "onClick");
            clickListener.onItemClick(getAdapterPosition(), v);

        }
    }
}
