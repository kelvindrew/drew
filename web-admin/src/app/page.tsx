"use client";
import Link from 'next/link';
import { Button, Container, Typography, Box, Grid, Card, CardContent } from '@mui/material';
import DirectionsBusIcon from '@mui/icons-material/DirectionsBus';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import DashboardIcon from '@mui/icons-material/Dashboard';

export default function Home() {
  return (
    <Container maxWidth="lg" className="min-h-screen py-10">
      <Box textAlign="center" mb={10}>
        <Typography variant="h2" component="h1" gutterBottom className="font-bold text-blue-800">
          Mulykap Clone
        </Typography>
        <Typography variant="h5" color="textSecondary" paragraph>
          Réservation de billets de bus, expédition de colis et gestion complète.
        </Typography>
      </Box>

      <Grid container spacing={4} justifyContent="center">
        <Grid item xs={12} md={4}>
          <Card className="h-full flex flex-col justify-between hover:shadow-xl transition-shadow">
            <CardContent className="text-center">
              <DirectionsBusIcon sx={{ fontSize: 60, color: '#1976d2', mb: 2 }} />
              <Typography variant="h5" component="h2" gutterBottom>
                Réservation
              </Typography>
              <Typography color="textSecondary">
                Réservez vos billets pour toutes les destinations avec sélection de siège.
              </Typography>
            </CardContent>
            <Box p={2} textAlign="center">
              <Link href="/booking" passHref>
                <Button variant="contained" color="primary" fullWidth>
                  Réserver un billet
                </Button>
              </Link>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card className="h-full flex flex-col justify-between hover:shadow-xl transition-shadow">
            <CardContent className="text-center">
              <LocalShippingIcon sx={{ fontSize: 60, color: '#2e7d32', mb: 2 }} />
              <Typography variant="h5" component="h2" gutterBottom>
                Colis
              </Typography>
              <Typography color="textSecondary">
                Envoyez vos colis en toute sécurité et suivez-les en temps réel.
              </Typography>
            </CardContent>
            <Box p={2} textAlign="center">
              <Link href="/packages" passHref>
                <Button variant="contained" color="success" fullWidth>
                  Expédier un colis
                </Button>
              </Link>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card className="h-full flex flex-col justify-between hover:shadow-xl transition-shadow">
            <CardContent className="text-center">
              <DashboardIcon sx={{ fontSize: 60, color: '#9c27b0', mb: 2 }} />
              <Typography variant="h5" component="h2" gutterBottom>
                Admin
              </Typography>
              <Typography color="textSecondary">
                Gérez les flottes, agences, trajets et utilisateurs.
              </Typography>
            </CardContent>
            <Box p={2} textAlign="center">
              <Link href="/admin" passHref>
                <Button variant="contained" color="secondary" fullWidth>
                  Tableau de bord
                </Button>
              </Link>
            </Box>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
}
