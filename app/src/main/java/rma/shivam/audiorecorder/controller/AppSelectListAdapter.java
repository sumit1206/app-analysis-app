package rma.shivam.audiorecorder.controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import rma.shivam.audiorecorder.R;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.AppData;

public class AppSelectListAdapter extends ArrayAdapter<AppData> {

    Context context;
    int resource;
    List<AppData> objects;

    public AppSelectListAdapter(@NonNull Context context, int resource, @NonNull List<AppData> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    public static class ViewHolder{
        ImageView icon;
        TextView name;
        CheckBox checkBox;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);
        final ViewHolder viewHolder = new ViewHolder();
        AppData data = objects.get(position);
        viewHolder.icon = convertView.findViewById(R.id.select_app_icon);
        viewHolder.name = convertView.findViewById(R.id.select_app_name);
        viewHolder.checkBox = convertView.findViewById(R.id.select_app_checkbox);
        Drawable dr_icon = Utils.getPackageIcon(context,data.getAppPackage());
        if(dr_icon != null) {
            viewHolder.icon.setImageDrawable(dr_icon);
        }
        viewHolder.name.setText(data.getAppName());
        viewHolder.checkBox.setChecked(data.isSelected());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppData data = objects.get(position);
                data.setSelected(viewHolder.checkBox.isChecked());
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.checkBox.performClick();
            }
        });
        return convertView;
    }
}
