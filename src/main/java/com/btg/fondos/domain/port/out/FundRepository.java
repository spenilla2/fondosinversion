package com.btg.fondos.domain.port.out;

import com.btg.fondos.domain.model.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository {

    Optional<Fund> findById(String id);

    List<Fund> findAll();
}
