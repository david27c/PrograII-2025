package com.example.miprimeraaplicacion;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface reporteDao {
    @Insert
    void insert(ReporteLocal reporte);

    @Update
    void update(ReporteLocal reporte);

    @Query("SELECT * FROM local_reports WHERE status = 'pendiente_envio' AND userId = :userId")
    List<ReporteLocal> getPendingReports(String userId);

    @Query("SELECT * FROM local_reports WHERE userId = :userId ORDER BY timestamp DESC")
    List<ReporteLocal> getAllReports(String userId);

    @Query("DELETE FROM local_reports WHERE id = :reportId")
    void deleteReportById(int reportId);
}