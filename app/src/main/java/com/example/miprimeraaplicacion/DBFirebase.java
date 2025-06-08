package com.example.miprimeraaplicacion;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions; // Importar SetOptions para inicializar documento
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBFirebase {

    private static final String TAG = "DBFirebase";
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    private final Context context;

    public DBFirebase(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    // --- Interfaces de Callback ---

    public abstract static class VoidCallback {
        public abstract void onSuccess();
        public abstract void onFailure(Exception e);
    }

    public interface DenunciaCallback {
        void onSuccess(Denuncia denunciaGuardada);
        void onFailure(Exception e);
    }

    public interface ListDenunciasCallback { // Usada para listas de denuncias
        void onSuccess(List<Denuncia> denuncias);
        void onFailure(Exception e);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    // NUEVA INTERFAZ: Para la subida de imágenes a Storage
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    // --- Referencias a Colecciones ---

    private CollectionReference getDenunciasCollection() {
        return db.collection("denuncias");
    }

    private CollectionReference getUsersCollection() {
        return db.collection("users"); // Colección para los datos de perfil de usuario
    }

    // --- Métodos de Denuncias ---

    /**
     * Guarda una denuncia en Firestore, incluyendo la subida de imagen si existe.
     * @param denuncia El objeto Denuncia a guardar.
     * @param imageUri URI local de la imagen a subir (puede ser null).
     * @param callback Callback para manejar el éxito o fracaso de la operación.
     */
    public void guardarDenuncia(Denuncia denuncia, Uri imageUri, final DenunciaCallback callback) {
        // Genera un ID único para la denuncia si no tiene uno
        if (denuncia.getIdDenuncia() == null || denuncia.getIdDenuncia().isEmpty()) {
            denuncia.setIdDenuncia(getDenunciasCollection().document().getId());
        }

        if (imageUri != null) {
            final StorageReference fotoRef = storageRef.child("images/" + denuncia.getIdDenuncia() + "_" + UUID.randomUUID().toString());
            fotoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        denuncia.setUrlImagen(uri.toString());
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
            saveDenunciaToFirestore(denuncia, callback);
        }
    }

    private void saveDenunciaToFirestore(Denuncia denuncia, final DenunciaCallback callback) {
        getDenunciasCollection().document(denuncia.getIdDenuncia())
                .set(denuncia)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Denuncia guardada en Firestore: " + denuncia.getIdDenuncia());
                    callback.onSuccess(denuncia);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar denuncia en Firestore: " + e.getMessage());
                    Toast.makeText(context, "Error al guardar denuncia en la nube: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });
    }

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

    public void obtenerDenunciasDeUsuario(String userId, String statusFilter, final ListDenunciasCallback callback) {
        Query query = getDenunciasCollection().whereEqualTo("idUsuario", userId);

        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Todos")) {
            query = query.whereEqualTo("estado", statusFilter);
        }

        query.orderBy("fechaHora", Query.Direction.DESCENDING)
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

    public void eliminarDenuncia(String denunciaId, final VoidCallback callback) {
        getDenunciasCollection().document(denunciaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Denuncia eliminada correctamente: " + denunciaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar denuncia: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    public void agregarComentarioADenuncia(String denunciaId, Map<String, Object> comment, final VoidCallback callback) {
        getDenunciasCollection().document(denunciaId)
                .update("comments", FieldValue.arrayUnion(comment))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comentario agregado correctamente a denuncia: " + denunciaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al agregar comentario a denuncia: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // --- Métodos de Perfil de Usuario (Completados) ---

    /**
     * Inicializa un documento de usuario en Firestore si no existe.
     * @param uid ID del usuario.
     * @param email Correo electrónico del usuario.
     * @param callback Callback para el éxito o fracaso.
     */
    public void inicializarDocumentoUsuario(String uid, String email, final VoidCallback callback) {
        User newUser = new User(uid, "", email, "", "", ""); // Crea un User básico
        getUsersCollection().document(uid).set(newUser, SetOptions.merge()) // Usa merge para no sobrescribir si ya existe
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documento de usuario inicializado: " + uid);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al inicializar documento de usuario: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    /**
     * Actualiza los campos del perfil de usuario en Firestore.
     * @param userId ID del usuario.
     * @param updates Un mapa con los campos a actualizar y sus nuevos valores.
     * @param callback Callback para el éxito o fracaso.
     */
    public void actualizarPerfilUsuario(String userId, Map<String, Object> updates, final VoidCallback callback) {
        getUsersCollection().document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Perfil de usuario actualizado: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar perfil de usuario: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    /**
     * Sube una imagen de perfil a Firebase Storage y devuelve su URL.
     * @param imageUri URI local de la imagen a subir.
     * @param userId ID del usuario (para nombrar la imagen en Storage).
     * @param callback Callback para el éxito (con la URL) o fracaso.
     */
    public void subirImagenPerfil(Uri imageUri, String userId, final ImageUploadCallback callback) {
        StorageReference fileReference = storageRef.child("profile_images/" + userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d(TAG, "Imagen de perfil subida: " + uri.toString());
                            callback.onSuccess(uri.toString());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al obtener URL de descarga de imagen de perfil: " + e.getMessage());
                            callback.onFailure(e);
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir imagen de perfil: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    /**
     * Obtiene los datos de un usuario específico desde Firestore.
     * @param userId El ID del usuario.
     * @param callback Callback para manejar el objeto User obtenido.
     */
    public void obtenerDatosDeUsuario(String userId, final UserCallback callback) {
        getUsersCollection().document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            Log.d(TAG, "Datos de usuario obtenidos: " + userId);
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("Datos de usuario encontrados pero no pudieron ser convertidos."));
                        }
                    } else {
                        callback.onSuccess(null); // Usuario no encontrado, devuelve null User
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener datos de usuario: " + e.getMessage());
                    callback.onFailure(e);
                });
    }
}