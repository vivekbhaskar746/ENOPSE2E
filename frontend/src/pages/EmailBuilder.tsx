import React from 'react';
import { Container, Typography } from '@mui/material';

const EmailBuilder: React.FC = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Email Campaign Builder
      </Typography>
      <Typography variant="body1">
        Email builder module coming soon...
      </Typography>
    </Container>
  );
};

export default EmailBuilder;