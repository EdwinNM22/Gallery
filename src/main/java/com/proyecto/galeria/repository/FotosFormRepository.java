package com.proyecto.galeria.repository;

import com.proyecto.galeria.model.FotosForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotosFormRepository extends JpaRepository<FotosForm, Integer> {
    List<FotosForm> findByForm_Id(Integer formId);
}