package com.firebasechat.library;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;


public class ChatSession<T extends Serializable> {

    private final static String TAG = ChatSession.class.getSimpleName();
    private FirebaseFirestore db;
    private String path;
    private String fieldToSort = null;
    private ListenerRegistration listenerRegistration;

    public ChatSession(FirebaseFirestore db) {
        this.db = db;
    }

    public void addOrderByFieldValue(String field) {
        fieldToSort = field;
    }


    // returns the removed field
    public String removeOrderByFieldValue() {
        String f = fieldToSort;
        fieldToSort = null;
        return f;
    }

    public void startListening(String path, final ChatListener chatListener) {
        this.path = path;
        CollectionReference ref = db.collection(path);
        EventListener<QuerySnapshot> event = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "Returned new documents");
                //add new message to array
                ArrayList<Map<String, Object>> newMessages = new ArrayList<>();
                //prepare new messages...
                for (DocumentChange a : queryDocumentSnapshots.getDocumentChanges()) {
                    newMessages.add(a.getDocument().getData());
                }
                chatListener.onResponse(newMessages);
            }
        };

        Query query = null;
        if (fieldToSort != null) {
            query = ref.orderBy(fieldToSort);
        }

        if (query == null) {
            listenerRegistration = ref.addSnapshotListener(event);
        } else {
            listenerRegistration = query.addSnapshotListener(event);
        }
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

    public void stop() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }

    }

    public interface ChatListener {
        void onFailure();

        void onResponse(ArrayList<Map<String, Object>> response);
    }
}