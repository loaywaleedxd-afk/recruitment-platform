package com.example.user_auth.repository;
import com.example.user_auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     /*search b al email htla2yh field f aluser esmo mail htdwar 3lyha
    ma3mola b ala5as 3shan a search b almail w hya optional 3shan lw mfysh
    mail b alesm da mttla3sh error ttla3o eno msh mwgod bs*/
    Optional<User> findByEmail(String email);
    /*almain logic bta3ha eno lama hd ygy y3ml signup w mailo mosta3mal abl kda
    my2darsh y sign up byh tany*/
    boolean existsByEmail(String email);
}