package com.purplevarun.springredisdemo.repository;

import com.purplevarun.springredisdemo.model.NumberEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NumberRepository extends JpaRepository<NumberEntry, UUID> {
}
