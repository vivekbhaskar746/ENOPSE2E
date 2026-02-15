package com.contentplatform.repository;

import com.contentplatform.model.BlogArticle;
import com.contentplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlogArticleRepository extends JpaRepository<BlogArticle, Long> {
    List<BlogArticle> findByAuthorOrderByCreatedAtDesc(User author);
    List<BlogArticle> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    @Query("SELECT b FROM BlogArticle b WHERE b.isPublished = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BlogArticle> searchPublishedArticles(String keyword);
}