import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Chip,
  Box
} from '@mui/material';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { blogService } from '../services/api.tsx';

interface BlogGenerationRequest {
  keywords: string;
  tone: string;
  wordCount: number;
  targetAudience?: string;
  focusKeyword?: string;
}

interface BlogArticle {
  id?: number;
  title: string;
  content: string;
  metaDescription: string;
  tags: string[];
  estimatedReadingTime: number;
  tone: string;
  seoScore: number;
  isPublished: boolean;
}

const BlogGenerator: React.FC = () => {
  const [request, setRequest] = useState<BlogGenerationRequest>({
    keywords: '',
    tone: 'PROFESSIONAL',
    wordCount: 800,
    targetAudience: '',
    focusKeyword: ''
  });
  
  const [generatedBlog, setGeneratedBlog] = useState<BlogArticle | null>(null);
  const [loading, setLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    try {
      const response = await blogService.generateBlog(request);
      setGeneratedBlog(response.data);
      setEditMode(true);
    } catch (error) {
      console.error('Error generating blog:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (generatedBlog) {
      try {
        await blogService.saveBlog(generatedBlog);
        alert('Blog saved successfully!');
      } catch (error) {
        console.error('Error saving blog:', error);
      }
    }
  };

  const handlePublish = async () => {
    if (generatedBlog?.id) {
      try {
        await blogService.publishBlog(generatedBlog.id);
        setGeneratedBlog({ ...generatedBlog, isPublished: true });
        alert('Blog published successfully!');
      } catch (error) {
        console.error('Error publishing blog:', error);
      }
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        AI Blog Generator
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Blog Configuration
            </Typography>
            
            <TextField
              fullWidth
              label="Keywords"
              value={request.keywords}
              onChange={(e) => setRequest({ ...request, keywords: e.target.value })}
              margin="normal"
              placeholder="e.g., artificial intelligence, machine learning"
            />
            
            <FormControl fullWidth margin="normal">
              <InputLabel>Tone</InputLabel>
              <Select
                value={request.tone}
                onChange={(e) => setRequest({ ...request, tone: e.target.value })}
              >
                <MenuItem value="PROFESSIONAL">Professional</MenuItem>
                <MenuItem value="CASUAL">Casual</MenuItem>
                <MenuItem value="HUMOROUS">Humorous</MenuItem>
                <MenuItem value="TECHNICAL">Technical</MenuItem>
                <MenuItem value="FRIENDLY">Friendly</MenuItem>
              </Select>
            </FormControl>
            
            <TextField
              fullWidth
              label="Word Count"
              type="number"
              value={request.wordCount}
              onChange={(e) => setRequest({ ...request, wordCount: parseInt(e.target.value) })}
              margin="normal"
              inputProps={{ min: 100, max: 3000 }}
            />
            
            <TextField
              fullWidth
              label="Target Audience (Optional)"
              value={request.targetAudience}
              onChange={(e) => setRequest({ ...request, targetAudience: e.target.value })}
              margin="normal"
            />
            
            <Button
              fullWidth
              variant="contained"
              onClick={handleGenerate}
              disabled={loading || !request.keywords}
              sx={{ mt: 2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Generate Blog'}
            </Button>
          </Paper>
        </Grid>
        
        <Grid item xs={12} md={8}>
          {generatedBlog && (
            <Paper sx={{ p: 3 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Generated Article</Typography>
                <Box>
                  <Button onClick={handleSave} sx={{ mr: 1 }}>Save Draft</Button>
                  <Button variant="contained" onClick={handlePublish}>Publish</Button>
                </Box>
              </Box>
              
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="subtitle2" color="textSecondary">
                    SEO Score: {generatedBlog.seoScore?.toFixed(1)}/100
                  </Typography>
                  <Typography variant="subtitle2" color="textSecondary">
                    Reading Time: {generatedBlog.estimatedReadingTime} min
                  </Typography>
                  <Box mt={1}>
                    {generatedBlog.tags?.map((tag, index) => (
                      <Chip key={index} label={tag} size="small" sx={{ mr: 0.5 }} />
                    ))}
                  </Box>
                </CardContent>
              </Card>
              
              <TextField
                fullWidth
                label="Title"
                value={generatedBlog.title}
                onChange={(e) => setGeneratedBlog({ ...generatedBlog, title: e.target.value })}
                margin="normal"
              />
              
              <TextField
                fullWidth
                label="Meta Description"
                value={generatedBlog.metaDescription}
                onChange={(e) => setGeneratedBlog({ ...generatedBlog, metaDescription: e.target.value })}
                margin="normal"
                multiline
                rows={2}
              />
              
              <Box mt={2}>
                <Typography variant="subtitle1" gutterBottom>Content</Typography>
                <ReactQuill
                  value={generatedBlog.content}
                  onChange={(content) => setGeneratedBlog({ ...generatedBlog, content })}
                  style={{ height: '400px', marginBottom: '50px' }}
                />
              </Box>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};

export default BlogGenerator;
