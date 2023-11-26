package com.cs407.wakeguard;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    // SelectionMode is when the checkboxes are visible next to every alarm card
    private boolean isSelectionMode = false;

    public AlarmAdapter(List<AlarmCard> alarmList) {
        this.alarmList = alarmList;
    }

    public void setAlarms(List<AlarmCard> alarmList) {
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
        holder.alarmTimeTextView.setText(alarm.getFormattedTime());
        holder.alarmTitleTextView.setText(alarm.getTitle());
        holder.alarmSwitchActive.setChecked(alarm.isActive());
        holder.alarmCheckBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.alarmCheckBox.setChecked(alarm.isSelected());
    }

    @Override
    public int getItemCount(){
        return alarmList.size();
    }

    // Method to set selection mode
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder{
        TextView alarmTimeTextView;
        TextView alarmTitleTextView;
        SwitchMaterial alarmSwitchActive;
        CheckBox alarmCheckBox;

        AlarmViewHolder(View view){
            super(view);
            alarmTimeTextView=view.findViewById(R.id.alarmTimeTextView);
            alarmTitleTextView=view.findViewById(R.id.alarmTitleTextView);
            alarmSwitchActive=view.findViewById(R.id.alarmSwitch);
            alarmCheckBox = view.findViewById(R.id.alarmCheckBox);

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

            /* Setting an onClickListener for every alarm card in RecyclerView. This listener
            * runs when the user taps an alarm card
            * The "view" here is the alarm card, each alarm card is a "view"*/
            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    // Getting the index of the clicked alarm card
                    int clickedAlarmIndex = getAdapterPosition();

                    // Ensuring that a valid alarm was clicked before proceeding
                    if (clickedAlarmIndex != RecyclerView.NO_POSITION){
                        DashboardActivity activity = (DashboardActivity) v.getContext();
                        AlarmCard clickedAlarmCard = alarmList.get(clickedAlarmIndex);
                        if (activity.isSelectionModeActive()){
                            // Toggle checkboxes
                            clickedAlarmCard.setSelected(!clickedAlarmCard.isSelected());
                            notifyItemChanged(clickedAlarmIndex); // updating just the alarm card that had its checkbox ticked.
                        } else{
                            // Getting the specific alarm card using the index
                            // Using an intent to go to the alarm editor screen
                            Intent editAlarmIntent = new Intent(v.getContext(), AlarmEditorActivity.class);
                            // TODO: YOU WILL NEED TO PASS THE INFO, (MAYBE THE ALARM ID) TO THE INTENT SO THAT THE CORRESPONDING ALARM INFO STORED IN THE DB IS RETREIVED.

                            v.getContext().startActivity(editAlarmIntent);
                        }
                    }
                }
            });

            /**
             * Enters selection mode when the user long presses any of the alarm cards
             */
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DashboardActivity activity = (DashboardActivity) v.getContext();
                    if (!activity.isSelectionModeActive()) {
                        activity.enterSelectionMode();
                    }
                    return true;
                }
            });

            alarmCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        AlarmCard alarm = alarmList.get(position);
                        alarm.setSelected(alarmCheckBox.isChecked());
                    }
                }
            });
        }
    }
}
