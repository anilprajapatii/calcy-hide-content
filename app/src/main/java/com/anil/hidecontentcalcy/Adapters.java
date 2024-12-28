package com.anil.hidecontentcalcy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class Adapters extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> fileList;
    private int selectedPosition = -1;
    private final String fileType;

    public Adapters(Context context, List<String> fileList, String fileType) {
        super(context, fileType.equals("audio") ? R.layout.list_item_audio : R.layout.list_item_document, fileList);
        this.context = context;
        this.fileList = fileList;
        this.fileType = fileType;
        Log.d("ff", "FILETYPE " + fileType);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Log.d("fileType", "Inflating view for file type: " + fileType);
            if (fileType.equals("audio")) {
                convertView = inflater.inflate(R.layout.list_item_audio, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.list_item_document, parent, false);  // Correct layout for documents
            }
        }

        // For "audio" files
        if (fileType.equals("audio")) {
            String audioFile = fileList.get(position);
            Log.d("audioFile", "audioFile: " + audioFile);  // Debugging output for audio files

            ImageView audioIcon = convertView.findViewById(R.id.audioIcon);
            TextView audioFileName = convertView.findViewById(R.id.audioFileName);

            audioIcon.setImageResource(R.drawable.ic_audio);
            audioFileName.setText(audioFile);

            if (position == selectedPosition) {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_purple));  // Selected item color
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));  // Default background
            }

            convertView.setOnClickListener(v -> {
                selectedPosition = position;
                Log.d("KKK", "audiolist: " + fileList);
                notifyDataSetChanged();
            });
        }
        else if (fileType.equals("document")) {
            String documentName = fileList.get(position);
            Log.d("documentName", "documentName: " + documentName);

            TextView documentTextView = convertView.findViewById(R.id.documentFileName);  // Correct ID for document name
            documentTextView.setText(documentName);

            if (position == selectedPosition) {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_purple));  // Selected item color
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));  // Default background
            }

            convertView.setOnClickListener(v -> {
                selectedPosition = position;
                Log.d("DDD", "documentList: " + fileList);
                notifyDataSetChanged();
            });
        }

        return convertView;
    }

}
