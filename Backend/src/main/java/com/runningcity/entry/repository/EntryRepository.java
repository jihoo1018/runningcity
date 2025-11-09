package com.runningcity.entry.repository;

import com.runningcity.entry.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    Optional<Entry> findByBaseId(Long baseId);
    List<Entry> findByGroupNo(Long groupNo);

}
