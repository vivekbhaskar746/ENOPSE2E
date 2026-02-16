package com.contentplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class BlogGenerationRequest {
    @NotBlank
    private String keywords;
    
    @NotBlank
    private String tone;
    
    @Min(100)
    @Max(3000)
    private Integer wordCount;
    
    private String targetAudience;
    private String focusKeyword;

    public BlogGenerationRequest() {}

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }

    public String getFocusKeyword() { return focusKeyword; }
    public void setFocusKeyword(String focusKeyword) { this.focusKeyword = focusKeyword; }
}