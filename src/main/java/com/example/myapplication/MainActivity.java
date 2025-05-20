package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.myapplication.Adapters.NotesListAdapter;
import com.example.myapplication.Database.RoomDB;
import com.example.myapplication.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    SearchView searchView_home;
    Button btnUnpin, btnDelete;
    Notes selectedNote;

    ActivityResultLauncher<Intent> noteTakerActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            @SuppressLint("WrongConstant") Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        searchView_home = findViewById(R.id.searchView_home);
        Button btnPin = findViewById(R.id.btnPin);
        btnUnpin = findViewById(R.id.btnUnpin);
        btnDelete = findViewById(R.id.btnDelete);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();
        updateRecycler(notes);

        noteTakerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Notes newNote = (Notes) result.getData().getSerializableExtra("note");
                            database.mainDAO().insert(newNote);
                            refreshNotes();
                        }
                    }
                });

        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            noteTakerActivityResultLauncher.launch(intent);
        });

        btnPin.setOnClickListener(v -> {
            if (selectedNote != null) {
                selectedNote.setPinned(true);
                database.mainDAO().update(selectedNote.getID(), selectedNote.getTitle(), selectedNote.getNotes(), true);
                refreshNotes();
                Toast.makeText(MainActivity.this, "Note pinned", Toast.LENGTH_SHORT).show();
            }
        });

        btnUnpin.setOnClickListener(v -> {
            if (selectedNote != null) {
                selectedNote.setPinned(false);
                database.mainDAO().update(selectedNote.getID(), selectedNote.getTitle(), selectedNote.getNotes(), false);
                refreshNotes();
                Toast.makeText(MainActivity.this, "Note unpinned", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedNote != null) {
                database.mainDAO().delete(selectedNote);
                refreshNotes();
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private void refreshNotes() {
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        notesListAdapter.notifyDataSetChanged();
    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase()) ||
                    singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNote = notes;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select Action");

            String[] options = notes.isPinned() ? new String[]{"Unpin", "Delete"} : new String[]{"Pin", "Delete"};

            builder.setItems(options, (dialog, which) -> {
                if (options[which].equals("Pin")) {
                    notes.setPinned(true);
                    database.mainDAO().update(notes.getID(), notes.getTitle(), notes.getNotes(), true);
                    Toast.makeText(MainActivity.this, "Note pinned", Toast.LENGTH_SHORT).show();
                } else if (options[which].equals("Unpin")) {
                    notes.setPinned(false);
                    database.mainDAO().update(notes.getID(), notes.getTitle(), notes.getNotes(), false);
                    Toast.makeText(MainActivity.this, "Note unpinned", Toast.LENGTH_SHORT).show();
                } else if (options[which].equals("Delete")) {
                    database.mainDAO().delete(notes);
                    Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                }
                refreshNotes();
            });

            builder.show();
        }

        @Override
        public void onLongClick(Notes notes, View view) {

        }
    };
}