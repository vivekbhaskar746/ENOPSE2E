import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Navbar from './components/Navbar.tsx';
import Dashboard from './pages/Dashboard.tsx';
import BlogGenerator from './pages/BlogGenerator.tsx';
import NewsFeed from './pages/NewsFeed.tsx';
import EmailBuilder from './pages/EmailBuilder.tsx';
import Login from './pages/Login.tsx';
import { AuthProvider} from './contexts/AuthProvider.tsx';
import { useAuth } from './contexts/useAuth.tsx'

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <div className="App">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/" element={
                <ProtectedRoute>
                  <Navbar />
                  <Dashboard />
                </ProtectedRoute>
              } />
              <Route path="/blog-generator" element={
                <ProtectedRoute>
                  <Navbar />
                  <BlogGenerator />
                </ProtectedRoute>
              } />
              <Route path="/news-feed" element={
                <ProtectedRoute>
                  <Navbar />
                  <NewsFeed />
                </ProtectedRoute>
              } />
              <Route path="/email-builder" element={
                <ProtectedRoute>
                  <Navbar />
                  <EmailBuilder />
                </ProtectedRoute>
              } />
            </Routes>
          </div>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
