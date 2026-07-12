'use client';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { ReactNode } from 'react';

const mulykapTheme = createTheme({
  palette: {
    primary: {
      main: '#E3000F', // Mulykap Red
      light: '#ff513c',
      dark: '#a60000',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#1A1A24', // Deep Blue/Black
      light: '#41414d',
      dark: '#000000',
      contrastText: '#ffffff',
    },
    background: {
      default: '#F5F7FA', // Soft off-white for premium feel
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontWeight: 700 },
    h2: { fontWeight: 700 },
    h3: { fontWeight: 600 },
    h4: { fontWeight: 600 },
    h5: { fontWeight: 500 },
    h6: { fontWeight: 500 },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '10px 24px',
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(227, 0, 15, 0.2)',
          },
        },
        containedPrimary: {
          background: 'linear-gradient(45deg, #E3000F 30%, #ff513c 90%)',
        }
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0 4px 20px rgba(0,0,0,0.05)',
          transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: '0 12px 30px rgba(0,0,0,0.1)',
          },
        },
      },
    },
  },
});

export default function ThemeRegistry({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider theme={mulykapTheme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}
