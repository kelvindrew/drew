"use client";
import { Container, Typography, TextField, Button, Grid, Box } from '@mui/material';
import Link from 'next/link';

export default function Booking() {
  return (
    <Container maxWidth="md" className="py-10">
      <Link href="/" className="text-blue-600 hover:underline mb-6 inline-block">&larr; Retour à l'accueil</Link>

      <Typography variant="h3" component="h1" gutterBottom>
        Rechercher un trajet
      </Typography>

      <Box component="form" sx={{ mt: 4, p: 4, bgcolor: 'background.paper', borderRadius: 2, boxShadow: 1 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Ville de départ" variant="outlined" placeholder="Ex: Kinshasa" />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Ville d'arrivée" variant="outlined" placeholder="Ex: Lubumbashi" />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Date de départ" type="date" InputLabelProps={{ shrink: true }} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField fullWidth label="Nombre de passagers" type="number" defaultValue={1} inputProps={{ min: 1 }} />
          </Grid>
          <Grid item xs={12}>
            <Button variant="contained" color="primary" size="large" fullWidth>
              Rechercher les bus disponibles
            </Button>
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
}
