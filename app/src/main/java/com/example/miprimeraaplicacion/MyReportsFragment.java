package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.activities.DetailReportActivity;
import com.example.miprimeraaplicacion.adapters.ReportAdapter;
import com.example.miprimeraaplicacion.models.Report;

import java.util.ArrayList;
import java.util.List;

public class MyReportsFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private RecyclerView myReportsRecyclerView;
    private ReportAdapter reportAdapter;
    private List<Report> reportList;
    private ProgressBar progressBar;
    private TextView noReportsTextView;
    private Button filterAllBtn, filterPendingBtn, filterResolvedBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    public MyReportsFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_my_reports, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Manejar caso donde el usuario no está logueado (ej. redirigir a LoginActivity)
            Toast.makeText(getContext(), "Debes iniciar sesión para ver tus reportes.", Toast.LENGTH_LONG).show();
            // Considerar lanzar una actividad de login o deshabilitar funcionalidades
            return view; // Retorna la vista sin cargar datos
        }

        initViews(view);
        setupRecyclerView();
        setupFilterButtons();

        loadMyReports("all"); // Carga todos los reportes por defecto

        return view;
    }

    private void initViews(View view) {
        myReportsRecyclerView = view.findViewById(R.id.my_reports_recycler_view);
        progressBar = view.findViewById(R.id.my_reports_progress_bar);
        noReportsTextView = view.findViewById(R.id.no_reports_tv);
        filterAllBtn = view.findViewById(R.id.filter_all_btn);
        filterPendingBtn = view.findViewById(R.id.filter_pending_btn);
        filterResolvedBtn = view.findViewById(R.id.filter_resolved_btn);
    }

    private void setupRecyclerView() {
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(getContext(), reportList, this); // 'this' como listener
        myReportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myReportsRecyclerView.setAdapter(reportAdapter);
    }

    private void setupFilterButtons() {
        filterAllBtn.setOnClickListener(v -> loadMyReports("all"));
        filterPendingBtn.setOnClickListener(v -> loadMyReports("pendiente"));
        filterResolvedBtn.setOnClickListener(v -> loadMyReports("resuelto"));
    }

    private void loadMyReports(String filterStatus) {
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        noReportsTextView.setVisibility(View.GONE);
        reportList.clear();
        reportAdapter.notifyDataSetChanged();

        Query query = db.collection("reports").whereEqualTo("userId", currentUserId);

        if (!filterStatus.equals("all")) {
            query = query.whereEqualTo("status", filterStatus);
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> { // Usamos addSnapshotListener para actualizaciones en tiempo real
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshots != null) {
                        reportList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Report report = doc.toObject(Report.class);
                            if (report != null) {
                                // Firestore no guarda el ID del documento dentro del objeto
                                // Si quieres usarlo, debes configurarlo manualmente
                                report.setId(doc.getId());
                                reportList.add(report);
                            }
                        }
                        reportAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        if (reportList.isEmpty()) {
                            noReportsTextView.setVisibility(View.VISIBLE);
                        } else {
                            noReportsTextView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onReportClick(Report report) {
        // Cuando un reporte es clickeado, abrir DetailReportActivity
        Intent intent = new Intent(getContext(), DetailReportActivity.class);
        intent.putExtra("reportId", report.getId()); // Pasa el ID del reporte
        startActivity(intent);
    }
}
