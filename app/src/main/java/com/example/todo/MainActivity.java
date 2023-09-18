package com.example.todo;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.todo.BlankFragment;
import com.example.todo.EditFragment;
import com.example.todo.MyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        BlankFragment.OnNoteAddedListener, EditFragment.OnNoteEditedListener {

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<String> data = new ArrayList<>();
    private FragmentManager fragmentManager;
    private Button btnAdd;

    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_POSITION = "position";
    private static final String SHARED_PREFERENCES_NAME = "MyNotes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load notes from SharedPreferences and set them in the adapter
        data = loadNotesFromSharedPreferences();
        adapter = new MyAdapter(data);
        recyclerView.setAdapter(adapter);

        btnAdd = findViewById(R.id.button_add);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlankFragment();
            }
        });

        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String itemData) {
                openEditFragment(position, getTitleFromItem(itemData), getContentFromItem(itemData));
            }
        });

        adapter.setOnDeleteClickListener(new MyAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmationDialog(position);
            }
        });
    }

    private void openBlankFragment() {
        BlankFragment frag = new BlankFragment();
        frag.setOnNoteAddedListener(this);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
        btnAdd.setVisibility(View.GONE);
    }

    private void openEditFragment(int position, String title, String content) {
        EditFragment editFragment = new EditFragment();
        editFragment.setOnNoteEditedListener(this);

        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_CONTENT, content);
        bundle.putInt(KEY_POSITION, position);

        editFragment.setArguments(bundle);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, editFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onNoteAdded(String title, String content) {
        data.add("Title: " + title + "\nContent: " + content);
        adapter.notifyItemInserted(data.size() - 1);

        // Save the new note to SharedPreferences
        saveNoteToSharedPreferences(title, content);

        fragmentManager.popBackStack();
        btnAdd.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNoteEdited(int position, String title, String content) {
        data.set(position, "Title: " + title + "\nContent: " + content);
        adapter.notifyItemChanged(position);

        // Update the edited note in SharedPreferences
        updateNoteInSharedPreferences(position, title, content);

        fragmentManager.popBackStack();
    }

    @Override
    public void onBackPressed() {
        btnAdd.setVisibility(View.VISIBLE);
        super.onBackPressed();
    }

    private String getTitleFromItem(String itemData) {
        String[] parts = itemData.split("\n");
        if (parts.length >= 1) {
            String titlePart = parts[0];
            return titlePart.substring("Title: ".length());
        }
        return "";
    }

    private String getContentFromItem(String itemData) {
        String[] parts = itemData.split("\n");
        if (parts.length >= 2) {
            String contentPart = parts[1];
            return contentPart.substring("Content: ".length());
        }
        return "";
    }

    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                data.remove(position);
                adapter.notifyItemRemoved(position);

                // Remove the note from SharedPreferences
                removeNoteFromSharedPreferences(position);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveNoteToSharedPreferences(String title, String content) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int noteCount = sharedPreferences.getInt("noteCount", 0);
        String noteKey = "note_" + noteCount;

        editor.putString(noteKey + "_title", title);
        editor.putString(noteKey + "_content", content);
        editor.putInt("noteCount", noteCount + 1);

        editor.apply();
    }

    private void updateNoteInSharedPreferences(int position, String title, String content) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String noteKey = "note_" + position;

        editor.putString(noteKey + "_title", title);
        editor.putString(noteKey + "_content", content);

        editor.apply();
    }

    private void removeNoteFromSharedPreferences(int position) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Shift note keys to maintain continuity
        int noteCount = sharedPreferences.getInt("noteCount", 0);
        for (int i = position + 1; i < noteCount; i++) {
            String sourceKey = "note_" + i;
            String destinationKey = "note_" + (i - 1);

            String title = sharedPreferences.getString(sourceKey + "_title", "");
            String content = sharedPreferences.getString(sourceKey + "_content", "");

            editor.putString(destinationKey + "_title", title);
            editor.putString(destinationKey + "_content", content);
        }

        // Decrement noteCount
        editor.putInt("noteCount", noteCount - 1);

        // Remove the last note
        String lastNoteKey = "note_" + (noteCount - 1);
        editor.remove(lastNoteKey + "_title");
        editor.remove(lastNoteKey + "_content");

        editor.apply();
    }

    private List<String> loadNotesFromSharedPreferences() {
        List<String> notes = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        int noteCount = sharedPreferences.getInt("noteCount", 0);

        for (int i = 0; i < noteCount; i++) {
            String noteKey = "note_" + i;
            String title = sharedPreferences.getString(noteKey + "_title", "");
            String content = sharedPreferences.getString(noteKey + "_content", "");
            notes.add("Title: " + title + "\nContent: " + content);
        }

        return notes;
    }
}
