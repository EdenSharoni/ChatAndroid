package com.example.chatandroid;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class ChatSession<T extends Serializable> {
    private final static String TAG = ChatSession.class.getSimpleName();
    private FirebaseFirestore db;
    private ArrayList<Map<String, Object>> messages = new ArrayList<>();
    private String path;

    public ChatSession(FirebaseFirestore db) {
        this.db = db;
    }

    public void initChat(String path, final ChatListener chatListener) {
        this.path = path;
        db.collection(path).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "Returned new documents");
                //add new message to array
                ArrayList<Map<String, Object>> newMessages = new ArrayList<>();
                //prepare new messages...
                for (DocumentChange a : queryDocumentSnapshots.getDocumentChanges()) {
                    newMessages.add(a.getDocument().getData());
                }
                messages.addAll(newMessages);
                chatListener.onResponse(newMessages);
            }
        });
    }

    public void sendMessage(T message) {
        db.collection(path).add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.e(TAG, "onSuccess: message added successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: sending message failed");
            }
        });
    }


    public void setMessages(ArrayList<Map<String, Object>> messages) {
        this.messages = messages;
    }


    public interface ChatListener {
        void onFailure();

        void onResponse(ArrayList<Map<String, Object>> response);
    }

}

