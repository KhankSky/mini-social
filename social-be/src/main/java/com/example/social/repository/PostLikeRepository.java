package com.example.social.repository;

import com.example.social.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId")
    List<PostLike> findByPostId(@Param("postId") Long postId);

    @Query("SELECT pl FROM PostLike pl WHERE pl.user.id = :userId")
    List<PostLike> findByUserId(@Param("userId") Long userId);

    long countByPostId(Long postId);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}
