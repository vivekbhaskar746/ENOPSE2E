import React from 'react';
import { Container, Typography } from '@mui/material';

const NewsFeed: React.FC = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Personalized News Feed
      </Typography>
      <Typography variant="body1">
        News feed module coming soon...
      </Typography>
    </Container>
  );
};

export default NewsFeed;