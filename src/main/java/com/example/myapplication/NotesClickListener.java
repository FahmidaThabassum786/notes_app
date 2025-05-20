package com.example.myapplication;

import android.view.View;

import androidx.cardview.widget.CardView;

import com.example.myapplication.Models.Notes;

public interface NotesClickListener {
    void onClick(Notes notes);
    void onLongClick(Notes notes, CardView cardview);

    void onLongClick(Notes notes, View view);
}
