package com.contentplatform.service;

import com.contentplatform.model.BlogArticle;
import com.contentplatform.model.User;
import com.contentplatform.dto.BlogGenerationRequest;
import com.contentplatform.repository.BlogArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class BlogService {
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private BlogArticleRepository blogRepository;
    
    public BlogArticle generateBlog(BlogGenerationRequest request, User author) {
        // Generate content using OpenAI
        String content = openAIService.generateBlogContent(
            request.getKeywords(), 
            request.getTone(), 
            request.getWordCount()
        );
        
        String title = openAIService.generateTitle(request.getKeywords(), request.getTone());
        
        // Create blog article
        BlogArticle article = new BlogArticle();
        article.setTitle(title);
        article.setContent(content);
        article.setTone(BlogArticle.Tone.valueOf(request.getTone().toUpperCase()));
        article.setAuthor(author);
        
        // Generate SEO metadata
        article.setMetaDescription(generateMetaDescription(content));
        article.setTags(generateTags(request.getKeywords()));
        article.setEstimatedReadingTime(calculateReadingTime(content));
        article.setSeoScore(calculateSEOScore(title, content, request.getKeywords()));
        
        return blogRepository.save(article);
    }
    
    public BlogArticle saveBlog(BlogArticle article) {
        return blogRepository.save(article);
    }
    
    public List<BlogArticle> getUserBlogs(User author) {
        return blogRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
    
    public BlogArticle getBlogById(Long id) {
        return blogRepository.findById(id).orElse(null);
    }
    
    private String generateMetaDescription(String content) {
        String[] sentences = content.split("\\. ");
        if (sentences.length > 0) {
            String desc = sentences[0];
            return desc.length() > 150 ? desc.substring(0, 147) + "..." : desc;
        }
        return "Generated blog article";
    }
    
    private List<String> generateTags(String keywords) {
        return Arrays.asList(keywords.toLowerCase().split("[,\\s]+"));
    }
    
    private Integer calculateReadingTime(String content) {
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200); // Average reading speed: 200 words/minute
    }
    
    private Double calculateSEOScore(String title, String content, String keywords) {
        double score = 0.0;
        
        // Title length check (50-60 characters is optimal)
        if (title.length() >= 50 && title.length() <= 60) score += 20;
        else if (title.length() >= 30) score += 10;
        
        // Keyword presence in title
        if (title.toLowerCase().contains(keywords.toLowerCase())) score += 25;
        
        // Content length check
        int wordCount = content.split("\\s+").length;
        if (wordCount >= 300) score += 20;
        if (wordCount >= 1000) score += 10;
        
        // Keyword density (should be 1-3%)
        String[] words = content.toLowerCase().split("\\s+");
        long keywordCount = Arrays.stream(words)
            .filter(word -> word.contains(keywords.toLowerCase()))
            .count();
        double density = (double) keywordCount / words.length * 100;
        if (density >= 1 && density <= 3) score += 25;
        
        return Math.min(100.0, score);
    }
}