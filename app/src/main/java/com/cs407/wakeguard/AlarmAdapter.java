package com.cs407.wakeguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * An adapter in Android development is a bridge between the UI component
 * and the data source that fills data into the UI Component.
 * It acts as a middleman that takes data from a source (like an
 * array or a list) and converts each item into a view that can
 * be added into the RecyclerView or ListView.
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmCard> alarmList;

    public AlarmAdapter(List<AlarmCard> alarmList) {
        this.alarmList = alarmList;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_card, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        AlarmCard alarm = alarmList.get(position);
        holder.alarmTimeTextView.setText(alarm.getTime());
        holder.alarmTitleTextView.setText(alarm.getTitle());
        holder.alarmSwitchActive.setChecked(alarm.isActive());
        // Bind other views in the card
    }

    @Override
    public int getItemCount(){
        return alarmList.size();
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder{
        TextView alarmTimeTextView;
        TextView alarmTitleTextView;
        SwitchMaterial alarmSwitchActive;

        AlarmViewHolder(View view){
            super(view);
            alarmTimeTextView=view.findViewById(R.id.alarmTimeTextView);
            alarmTitleTextView=view.findViewById(R.id.alarmTitleTextView);
            alarmSwitchActive=view.findViewById(R.id.alarmSwitch);
            alarmSwitchActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    /*retrieves the position of the item in the adapter. This position
                    is used to reference the corresponding AlarmCard object in your alarmList.*/
                    int position = getAdapterPosition();
                    /*The NO_POSITION check is important because it ensures that you only
                    proceed if the ViewHolder is still valid and displaying a valid item.
                    If the ViewHolder has been removed and no longer represents any item
                    in the list, getAdapterPosition() will return RecyclerView.NO_POSITION*/
                    if (position != RecyclerView.NO_POSITION){
                        // Update alarm's active state
                        AlarmCard alarm = alarmList.get(position);
                        alarm.setActive(isChecked);
                        // TODO: UPDATE THE DATABASE HERE TO INDICATE THAT THIS ALARM IS NOW INACTIVE
                    }
                }
            });
        }
    }

}
