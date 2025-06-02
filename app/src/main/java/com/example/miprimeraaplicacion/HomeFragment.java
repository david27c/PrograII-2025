package com.example.miprimeraaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.miprimeraaplicacion.R;
import com.example.miprimeraaplicacion.activities.DetailReportActivity;
import com.example.miprimeraaplicacion.adapters.ReportAdapter;
import com.example.miprimeraaplicacion.models.Report;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ReportAdapter.OnReportClickListener {

    private RecyclerView homeRecyclerView;
    private ReportAdapter reportAdapter;
    private List<Report> reportList;
    private ProgressBar progressBar;
    private TextView noReportsAvailableTextView;

    private FirebaseFirestore db;

    public HomeFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupRecyclerView();
        loadAllReports();

        return view;
    }

    private void initViews(View view) {
        homeRecyclerView = view.findViewById(R.id.home_recycler_view);
        progressBar = view.findViewById(R.id.home_progress_bar);
        noReportsAvailableTextView = view.findViewById(R.id.no_reports_available_tv);
    }

    private void setupRecyclerView() {
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(getContext(), reportList, this); // 'this' como listener
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecyclerView.setAdapter(reportAdapter);
    }

    private void loadAllReports() {
        progressBar.setVisibility(View.VISIBLE);
        noReportsAvailableTextView.setVisibility(View.GONE);
        reportList.clear();
        reportAdapter.notifyDataSetChanged();

        // Consulta para obtener todos los reportes, ordenados por fecha de creación descendente
        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> { // Usamos addSnapshotListener para actualizaciones en tiempo real
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshots != null) {
                        reportList.clear(); // Limpiar la lista antes de añadir los nuevos datos
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Report report = doc.toObject(Report.class);
                            if (report != null) {
                                report.setId(doc.getId()); // Asigna el ID del documento
                                reportList.add(report);
                            }
                        }
                        reportAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        if (reportList.isEmpty()) {
                            noReportsAvailableTextView.setVisibility(View.VISIBLE);
                        } else {
                            noReportsAvailableTextView.setVisibility(View.GONE);
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