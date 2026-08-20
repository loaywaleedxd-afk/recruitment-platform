package com.example.user_auth.repository;

import com.example.user_auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
 //almain usage bta3tha lama al user y refresh b3d ma y logout
 //w ataked en altoken dy usable wala la
    Optional<RefreshToken> findByToken(String token);

    @Modifying  //makes the query writes mnghyrha spring byftkr enk ht select
    // btmark al altokens aly invalid b al userid bta3hom bs alkalam da ll tokens aly lesa active bs
    //w by7slha revok lama t logout aw tghyr alpassword msln
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.revoked = false")
    //btaolk kam wahda 7asalha update
    int revokeAllForUser(@Param("userId") Long userId);

    @Modifying
    //btmsa7 ay token expaired abl wa2t mo7dad
    @Query("delete from RefreshToken t where t.expiryDate < :cutoff")
    //bt2oly 3dad al deleted rows aw altokens
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}