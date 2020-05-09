package rma.shivam.audiorecorder.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Constant;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.History;

public class HistoryAdapter extends ArrayAdapter<History> {

    Context context;
    int resource;
    List<History> objects;
    EventCallback eventCallback;

    public HistoryAdapter(@NonNull Context context, int resource, @NonNull List<History> objects, EventCallback eventCallback) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        this.eventCallback = eventCallback;
    }

    public static class ViewHolder{
        TextView sessionId;
        TextView appName;
        TextView dateTime;
        TextView audioUploaded;
        TextView csvUploaded;
        ImageView delete;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        History history = objects.get(position);
        viewHolder.sessionId = convertView.findViewById(R.id.history_card_session_id);
        viewHolder.appName = convertView.findViewById(R.id.history_card_app_name);
        viewHolder.dateTime = convertView.findViewById(R.id.history_card_date_time);
        viewHolder.audioUploaded = convertView.findViewById(R.id.history_card_audio_uploaded);
        viewHolder.csvUploaded = convertView.findViewById(R.id.history_card_csv_uploaded);
        viewHolder.delete = convertView.findViewById(R.id.history_card_delete);

        viewHolder.sessionId.setText(history.getSession_id());
        viewHolder.appName.setText(history.getApp_name());
        String dt = Utils.getTimestampInFormat(Long.parseLong(history.getDate_time()),"dd MMM yyyy, hh:mm:ss a");
        viewHolder.dateTime.setText(dt);
        String audioState = history.getAudio_uploaded();
        viewHolder.audioUploaded.setText(audioState);
//        eventCallback.onAudioTriggered(audioState);
        String csvState = history.getCsv_uploaded();
        viewHolder.csvUploaded.setText(csvState);
//        eventCallback.onCsvTriggered(csvState);
        if(csvState.equals(Constant.UPLOADED) || audioState.equals(Constant.UPLOADED)){
            viewHolder.delete.setVisibility(View.GONE);
        }else {
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    History history = objects.get(position);
                    eventCallback.onDeleteClick(history);
                }
            });
        }

        return convertView;
    }

    public interface EventCallback{
        void onDeleteClick(History history);
//        void onCsvTriggered(String state);
//        void onAudioTriggered(String state);
    }
}
