package com.firebasechat.library;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Objects;


public class ChatSession<T extends Serializable> {
    public static final int ADD_DOCUMENT_ID_TO_MAP = 0;
    public static final int DO_NOT_ADD_DOCUMENT_ID_TO_MAP = 1;
    public static final String DOC_ID_KEY = "ChatSession_documentID";
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

    public void startListening(String path, final ChatListener chatListener, final int addIdToMap, final boolean getAllDocTypes,final DocumentChange.Type type) {
        this.path = path;
        CollectionReference ref = db.collection(path);
        EventListener<QuerySnapshot> event = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "Returned new documents");
                //add new message to array
                ArrayList<Map<String, Object>> newMessages = new ArrayList<>();
                //prepare new messages...
                for (DocumentChange a : Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges()) {
                    if (getAllDocTypes || a.getType().equals(type)) {
                        Map<String, Object> map = a.getDocument().getData();
                        if (addIdToMap == ADD_DOCUMENT_ID_TO_MAP) {
                            map.put(DOC_ID_KEY, a.getDocument().getId());
                        }
                        newMessages.add(map);
                    }

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

    public void sendMessage(T message, final MessageCallback callback) {
        db.collection(path).add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.e(TAG, "onSuccess: message added successfully");
                if (callback != null) {
                    callback.onSuccess(documentReference.getId());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: sending message failed");
                if (callback != null) {
                    callback.onFailure();
                }
            }
        });
    }

    public void deleteMessage(final String id, final MessageCallback callback) {
        db.collection(path).document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "deleteMessage: Message successfully deleted from db");
                if (callback != null) {
                    callback.onSuccess(id);
                }

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
        void onResponse(ArrayList<Map<String, Object>> response);

        void onFailure();
    }

    public interface MessageCallback {
        void onSuccess(String MessageId);

        void onFailure();
    }
}