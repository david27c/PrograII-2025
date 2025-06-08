package com.example.miprimeraaplicacion;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.UUID;

public class DBFirebase {

    private static final String TAG = "DBFirebase";
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    private final Context context; // Para usar Toasts si es necesario

    public DBFirebase(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    // Interfaz de callback para una única denuncia
    public interface DenunciaCallback {
        void onSuccess(Denuncia denunciaGuardada);
        void onFailure(Exception e);
    }

    // Interfaz de callback para una lista de denuncias (¡NUEVA!)
    public interface ListDenunciasCallback {
        void onSuccess(List<Denuncia> denuncias);
        void onFailure(Exception e);
    }

    // Colección de denuncias
    private CollectionReference getDenunciasCollection() {
        return db.collection("denuncias");
    }

    /**
     * Guarda una denuncia en Firestore, incluyendo la subida de imagen si existe.
     * La URL de la imagen en el objeto denuncia será actualizada antes de guardar en Firestore.
     * @param denuncia El objeto Denuncia a guardar.
     * @param imageUri URI local de la imagen a subir (puede ser null).
     * @param callback Callback para manejar el éxito o fracaso de la operación.
     */
    public void guardarDenuncia(Denuncia denuncia, Uri imageUri, final DenunciaCallback callback) {
        if (imageUri != null) {
            final StorageReference fotoRef = storageRef.child("images/" + UUID.randomUUID().toString());
            fotoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        denuncia.setUrlImagen(uri.toString());
                        // Ahora que la URL de la imagen está establecida, guarda la denuncia
                        saveDenunciaToFirestore(denuncia, callback);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener URL de descarga de imagen: " + e.getMessage());
                        Toast.makeText(context, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(e);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al subir imagen: " + e.getMessage());
                        Toast.makeText(context, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onFailure(e);
                    });
        } else {
            // Si no hay imagen, guarda la denuncia directamente
            saveDenunciaToFirestore(denuncia, callback);
        }
    }

    /**
     * Método auxiliar para guardar la denuncia en Firestore.
     * @param denuncia El objeto Denuncia a guardar.
     * @param callback Callback para manejar el éxito o fracaso.
     */
    private void saveDenunciaToFirestore(Denuncia denuncia, final DenunciaCallback callback) {
        getDenunciasCollection().document(denuncia.getIdDenuncia())
                .set(denuncia)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Denuncia guardada en Firestore: " + denuncia.getIdDenuncia());
                    callback.onSuccess(denuncia); // Pasa la denuncia con la URL de la imagen si se subió
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar denuncia en Firestore: " + e.getMessage());
                    Toast.makeText(context, "Error al guardar denuncia en la nube: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });
    }

    /**
     * Obtiene una denuncia específica por su ID desde Firestore.
     * @param denunciaId ID de la denuncia.
     * @param callback Callback para manejar el resultado.
     */
    public void obtenerDenunciaPorId(String denunciaId, final DenunciaCallback callback) {
        getDenunciasCollection().document(denunciaId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Denuncia denuncia = documentSnapshot.toObject(Denuncia.class);
                        if (denuncia != null) {
                            callback.onSuccess(denuncia);
                        } else {
                            callback.onFailure(new Exception("Denuncia encontrada pero no pudo ser convertida."));
                        }
                    } else {
                        callback.onFailure(new Exception("Denuncia no encontrada."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener denuncia por ID: " + e.getMessage());
                    callback.onFailure(e);
                });
    }


    /**
     * Obtiene todas las denuncias de un usuario específico desde Firestore.
     * @param userId El ID del usuario.
     * @param statusFilter El estado por el que filtrar (o null para todos los estados).
     * @param callback Callback para manejar la lista de denuncias obtenidas.
     */
    public void obtenerDenunciasDeUsuario(String userId, String statusFilter, final ListDenunciasCallback callback) {
        Query query = getDenunciasCollection().whereEqualTo("idUsuario", userId);

        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Todos")) {
            query = query.whereEqualTo("estado", statusFilter);
        }

        query.orderBy("fechaHora", Query.Direction.DESCENDING) // Ordenar por fecha
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Denuncia> denuncias = queryDocumentSnapshots.toObjects(Denuncia.class);
                    callback.onSuccess(denuncias);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener denuncias del usuario: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    /**
     * Obtiene todas las denuncias desde Firestore (para la pantalla Home, por ejemplo).
     * @param callback Callback para manejar la lista de denuncias obtenidas.
     */
    public void obtenerTodasLasDenuncias(final ListDenunciasCallback callback) {
        getDenunciasCollection().orderBy("fechaHora", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Denuncia> denuncias = queryDocumentSnapshots.toObjects(Denuncia.class);
                    callback.onSuccess(denuncias);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener todas las denuncias: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // Aquí puedes añadir otros métodos como actualizarDenuncia, eliminarDenuncia, etc.
}