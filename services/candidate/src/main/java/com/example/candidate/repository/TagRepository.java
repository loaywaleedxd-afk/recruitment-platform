package com.example.candidate.repository;

import com.example.candidate.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

     // ayz atghada byha lw hd kateb haga upper case w hd tany katbha lower case 3shan a5lyhom homa alatnyn tag wahed
     Optional<Tag> findByNameIgnoreCase(String name);
    //bttaked fy duplicates wala la
    boolean existsByNameIgnoreCase(String name);
}