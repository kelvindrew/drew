'use client';
import { Container, Typography, Box, Grid, Card, CardContent } from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PhoneIcon from '@mui/icons-material/Phone';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import Link from 'next/link';

export default function Agencies() {
  const agencies = [
    {
      city: "Lubumbashi",
      name: "Agence Centrale Lubumbashi",
      address: "Avenue de la Révolution, Q/Makutano",
      phone: "+243 99 000 00 00",
      hours: "Lun - Dim: 05h00 - 18h00"
    },
    {
      city: "Kolwezi",
      name: "Terminal VIP Kolwezi",
      address: "Quartier Industriel, Près du rond-point",
      phone: "+243 81 000 00 00",
      hours: "Lun - Dim: 06h00 - 17h30"
    },
    {
      city: "Likasi",
      name: "Relais Likasi",
      address: "Centre-ville, Likasi",
      phone: "+243 82 000 00 00",
      hours: "Lun - Sam: 07h00 - 16h00"
    }
  ];

  return (
    <Container maxWidth="lg" className="py-10" sx={{ minHeight: '100vh', pt: 10 }}>
      <Link href="/" style={{ color: '#E3000F', textDecoration: 'none', marginBottom: '24px', display: 'inline-block' }}>
        &larr; Retour à l'accueil
      </Link>

      <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 'bold', color: 'secondary.main', mb: 2 }}>
        Nos Agences & Terminaux
      </Typography>
      <Typography variant="subtitle1" color="textSecondary" sx={{ mb: 6 }}>
        Retrouvez les points de départ, d'arrivée et de dépôt de colis Mulykap à travers le Katanga.
      </Typography>

      <Grid container spacing={4}>
        {agencies.map((agency, index) => (
          <Grid item xs={12} md={4} key={index}>
            <Card sx={{ height: '100%', borderRadius: 3, boxShadow: '0 8px 24px rgba(0,0,0,0.05)' }}>
              <Box sx={{ bgcolor: 'secondary.main', color: 'white', p: 2, textAlign: 'center' }}>
                <Typography variant="h5" sx={{ fontWeight: 'bold' }}>{agency.city}</Typography>
              </Box>
              <CardContent sx={{ p: 4 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 600, color: 'primary.main' }}>
                  {agency.name}
                </Typography>

                <Box sx={{ display: 'flex', gap: 2, mb: 2, mt: 3, alignItems: 'flex-start' }}>
                  <LocationOnIcon color="action" />
                  <Typography variant="body2" color="textSecondary">{agency.address}</Typography>
                </Box>

                <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
                  <PhoneIcon color="action" />
                  <Typography variant="body2" color="textSecondary">{agency.phone}</Typography>
                </Box>

                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <AccessTimeIcon color="action" />
                  <Typography variant="body2" color="textSecondary">{agency.hours}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}
