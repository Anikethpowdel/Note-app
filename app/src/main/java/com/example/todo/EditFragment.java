package com.example.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;

public class EditFragment extends Fragment {
    private OnNoteEditedListener noteEditedListener;
    private int position;
    private String title;
    private String content;

    public interface OnNoteEditedListener {
        void onNoteEdited(int position, String title, String content);
    }

    public void setOnNoteEditedListener(OnNoteEditedListener listener) {
        noteEditedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

        EditText ti = rootView.findViewById(R.id.ti);
        EditText c = rootView.findViewById(R.id.c);

        // Retrieve the position, title, and content from the arguments Bundle
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt("position");
            title = args.getString("title");
            content = args.getString("content");
        }

        ti.setText(title);
        c.setText(content);

        Button btnSave = rootView.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the edited title and content
                String editedTitle = ti.getText().toString();
                String editedContent = c.getText().toString();

                // Save the edited data to SharedPreferences
                saveEditedNoteToSharedPreferences(position, editedTitle, editedContent);

                if (noteEditedListener != null) {
                    noteEditedListener.onNoteEdited(position, editedTitle, editedContent);
                }
            }
        });

        return rootView;
    }

    private void saveEditedNoteToSharedPreferences(int position, String title, String content) {
        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyNotes", Context.MODE_PRIVATE);

        // Get the SharedPreferences editor to write data
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Construct the key for the edited note based on its position
        String noteKey = "note_" + position;

        // Save the edited title and content using the key
        editor.putString(noteKey + "_title", title);
        editor.putString(noteKey + "_content", content);

        // Commit the changes to SharedPreferences
        editor.apply();
    }
}
