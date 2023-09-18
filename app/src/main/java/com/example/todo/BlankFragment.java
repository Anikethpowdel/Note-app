package com.example.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class BlankFragment extends Fragment {
    private OnNoteAddedListener noteAddedListener;

    public interface OnNoteAddedListener {
        void onNoteAdded(String title, String content);
    }

    public void setOnNoteAddedListener(OnNoteAddedListener listener) {
        noteAddedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        EditText ti = rootView.findViewById(R.id.ti);
        EditText c = rootView.findViewById(R.id.c);

        Button btnCancel = rootView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the entered data
                ti.setText("");
                c.setText("");
            }
        });

        Button button = rootView.findViewById(R.id.b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = ti.getText().toString();
                String content = c.getText().toString();

                // Save the data to SharedPreferences
                saveNoteToSharedPreferences(title, content);

                if (noteAddedListener != null) {
                    noteAddedListener.onNoteAdded(title, content); // Notify the listener
                }

                Toast.makeText(getContext(), "Note added", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void saveNoteToSharedPreferences(String title, String content) {
        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyNotes", Context.MODE_PRIVATE);

        // Get the SharedPreferences editor to write data
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Generate a unique key for each note (e.g., using a timestamp)
        String noteKey = String.valueOf(System.currentTimeMillis());

        // Save the title and content using the generated key
        editor.putString(noteKey + "_title", title);
        editor.putString(noteKey + "_content", content);

        // Commit the changes to SharedPreferences
        editor.apply();
    }
}
