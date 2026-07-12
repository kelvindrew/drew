'use client';
import Link from 'next/link';
import { Button, Container, Typography, Box, Grid, Card, CardContent, Divider, Chip } from '@mui/material';
import DirectionsBusIcon from '@mui/icons-material/DirectionsBus';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import DashboardIcon from '@mui/icons-material/Dashboard';
import SecurityIcon from '@mui/icons-material/Security';
import WifiIcon from '@mui/icons-material/Wifi';
import EventSeatIcon from '@mui/icons-material/EventSeat';
import FacebookIcon from '@mui/icons-material/Facebook';
import TwitterIcon from '@mui/icons-material/Twitter';
import InstagramIcon from '@mui/icons-material/Instagram';
import LocationOnIcon from '@mui/icons-material/LocationOn';

export default function Home() {
  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navbar / Header Mock */}
      <Box sx={{ bgcolor: 'secondary.main', color: 'white', py: 2 }}>
        <Container maxWidth="xl" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h5" sx={{ fontWeight: 800, letterSpacing: 1 }}>
            MULYKAP<span style={{ color: '#E3000F' }}>.</span>
          </Typography>
          <Box sx={{ display: 'flex', gap: 3 }}>
            <Link href="/" style={{ color: 'white', textDecoration: 'none', fontWeight: 500 }}>Accueil</Link>
            <Link href="/booking" style={{ color: 'white', textDecoration: 'none', fontWeight: 500 }}>Réserver</Link>
            <Link href="/packages" style={{ color: 'white', textDecoration: 'none', fontWeight: 500 }}>Colis</Link>
            <Link href="#about" style={{ color: 'white', textDecoration: 'none', fontWeight: 500 }}>À propos</Link>
          </Box>
        </Container>
      </Box>

      {/* Hero Section */}
      <Box sx={{
        background: 'linear-gradient(to right bottom, #1A1A24, #2a2a3b)',
        color: 'white',
        py: { xs: 8, md: 12 },
        position: 'relative',
        overflow: 'hidden'
      }}>
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Grid container spacing={4} alignItems="center">
            <Grid item xs={12} md={7}>
              <Chip label="Service Premium Katanga" color="primary" sx={{ mb: 3, fontWeight: 'bold' }} />
              <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 800, lineHeight: 1.2 }}>
                L&apos;excellence du <br/>
                <span style={{ color: '#E3000F' }}>transport</span> en RDC.
              </Typography>
              <Typography variant="h6" sx={{ mb: 4, color: 'rgba(255,255,255,0.8)', fontWeight: 400, maxWidth: '600px' }}>
                Voyagez avec un confort absolu et une sécurité optimale. Découvrez nos services VIP entre Lubumbashi, Kolwezi et Likasi, ou expédiez vos colis en toute sérénité.
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Link href="/booking" passHref>
                  <Button variant="contained" color="primary" size="large" startIcon={<DirectionsBusIcon />}>
                    Acheter un billet
                  </Button>
                </Link>
                <Link href="/packages" passHref>
                  <Button variant="outlined" size="large" sx={{ color: 'white', borderColor: 'white', '&:hover': { borderColor: '#E3000F' } }} startIcon={<LocalShippingIcon />}>
                    Suivre un colis
                  </Button>
                </Link>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 10, flexGrow: 1 }}>
        <Box textAlign="center" mb={8}>
          <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 700, color: 'secondary.main' }}>
            Nos Services
          </Typography>
          <Typography variant="subtitle1" color="textSecondary">
            Des solutions fiables pour vos déplacements et vos expéditions.
          </Typography>
        </Box>

        <Grid container spacing={4} justifyContent="center">
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <CardContent sx={{ textAlign: 'center', p: 4, flexGrow: 1 }}>
                <Box sx={{ bgcolor: 'rgba(227, 0, 15, 0.1)', width: 80, height: 80, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', mx: 'auto', mb: 3 }}>
                  <DirectionsBusIcon sx={{ fontSize: 40, color: 'primary.main' }} />
                </Box>
                <Typography variant="h5" component="h3" gutterBottom sx={{ fontWeight: 600 }}>
                  Transport Passagers VIP
                </Typography>
                <Typography color="textSecondary" sx={{ mb: 3 }}>
                  Flotte moderne, sièges inclinables, Wi-Fi à bord et respect strict des horaires. Votre confort est notre priorité absolue.
                </Typography>
              </CardContent>
              <Box p={3} pt={0} textAlign="center">
                <Link href="/booking" passHref>
                  <Button variant="outlined" color="primary" fullWidth>Réserver</Button>
                </Link>
              </Box>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <CardContent sx={{ textAlign: 'center', p: 4, flexGrow: 1 }}>
                <Box sx={{ bgcolor: 'rgba(227, 0, 15, 0.1)', width: 80, height: 80, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', mx: 'auto', mb: 3 }}>
                  <LocalShippingIcon sx={{ fontSize: 40, color: 'primary.main' }} />
                </Box>
                <Typography variant="h5" component="h3" gutterBottom sx={{ fontWeight: 600 }}>
                  Logistique & Colis
                </Typography>
                <Typography color="textSecondary" sx={{ mb: 3 }}>
                  Expédition rapide et sécurisée de vos courriers et marchandises. Suivi en temps réel et notification à la livraison.
                </Typography>
              </CardContent>
              <Box p={3} pt={0} textAlign="center">
                <Link href="/packages" passHref>
                  <Button variant="outlined" color="primary" fullWidth>Expédier</Button>
                </Link>
              </Box>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <CardContent sx={{ textAlign: 'center', p: 4, flexGrow: 1 }}>
                <Box sx={{ bgcolor: 'rgba(26, 26, 36, 0.1)', width: 80, height: 80, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', mx: 'auto', mb: 3 }}>
                  <DashboardIcon sx={{ fontSize: 40, color: 'secondary.main' }} />
                </Box>
                <Typography variant="h5" component="h3" gutterBottom sx={{ fontWeight: 600 }}>
                  Espace Professionnel
                </Typography>
                <Typography color="textSecondary" sx={{ mb: 3 }}>
                  Accédez au tableau de bord pour gérer vos réservations d&apos;entreprise, factures, flottes et statistiques d&apos;expédition.
                </Typography>
              </CardContent>
              <Box p={3} pt={0} textAlign="center">
                <Link href="/admin" passHref>
                  <Button variant="outlined" color="secondary" fullWidth>Connexion Admin</Button>
                </Link>
              </Box>
            </Card>
          </Grid>
        </Grid>
      </Container>

      {/* Pourquoi nous choisir */}
      <Box id="about" sx={{ bgcolor: 'white', py: 10 }}>
        <Container maxWidth="lg">
          <Grid container spacing={6} alignItems="center">
            <Grid item xs={12} md={6}>
              <Typography variant="overline" color="primary" sx={{ fontWeight: 'bold', letterSpacing: 1.5 }}>
                À PROPOS DE MULYKAP
              </Typography>
              <Typography variant="h3" component="h2" sx={{ fontWeight: 700, mb: 3, mt: 1, color: 'secondary.main' }}>
                Voyagez au-delà <br/> de vos attentes
              </Typography>
              <Typography variant="body1" color="textSecondary" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                Mulykap est une entreprise leader dans le secteur du transport interurbain et de la logistique en République Démocratique du Congo. Forte de plusieurs années d&apos;expérience, notre mission est de redéfinir les standards du voyage sur route en offrant un service hautement sécurisé, ponctuel et incroyablement confortable.
              </Typography>

              <Grid container spacing={3}>
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <SecurityIcon color="primary" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Sécurité Maximale</Typography>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <EventSeatIcon color="primary" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Confort VIP</Typography>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <WifiIcon color="primary" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Wi-Fi & Prises USB</Typography>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LocationOnIcon color="primary" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Ponctualité garantie</Typography>
                  </Box>
                </Grid>
              </Grid>
            </Grid>
            <Grid item xs={12} md={6}>
               <Box sx={{
                  bgcolor: 'secondary.main',
                  borderRadius: 4,
                  height: 400,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 20px 40px rgba(0,0,0,0.2)'
               }}>
                  <Typography variant="h4" color="white" sx={{ fontWeight: 'bold', opacity: 0.5 }}>
                     [Image Bus Mulykap VIP]
                  </Typography>
               </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

      {/* Footer */}
      <Box sx={{ bgcolor: 'secondary.main', color: 'white', pt: 8, pb: 4, mt: 'auto' }}>
        <Container maxWidth="lg">
          <Grid container spacing={4} sx={{ mb: 6 }}>
            <Grid item xs={12} md={4}>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 3 }}>
                MULYKAP<span style={{ color: '#E3000F' }}>.</span>
              </Typography>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)', mb: 3, maxWidth: 300 }}>
                L&apos;excellence du transport et de la logistique en RDC. Votre sécurité et votre confort sont notre priorité.
              </Typography>
              <Box sx={{ display: 'flex', gap: 2 }}>
                <FacebookIcon sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: 'white', cursor: 'pointer' } }} />
                <TwitterIcon sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: 'white', cursor: 'pointer' } }} />
                <InstagramIcon sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: 'white', cursor: 'pointer' } }} />
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>Liens Rapides</Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <Link href="/booking" style={{ color: 'rgba(255,255,255,0.7)', textDecoration: 'none' }}>Réserver un billet</Link>
                <Link href="/packages" style={{ color: 'rgba(255,255,255,0.7)', textDecoration: 'none' }}>Suivi de colis</Link>
                <Link href="/agencies" style={{ color: 'rgba(255,255,255,0.7)', textDecoration: 'none' }}>Nos Agences</Link>
                <Link href="/faq" style={{ color: 'rgba(255,255,255,0.7)', textDecoration: 'none' }}>Foire Aux Questions</Link>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>Contactez-nous</Typography>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)', mb: 1 }}>
                <strong>Siège social:</strong> Lubumbashi, Haut-Katanga, RDC
              </Typography>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)', mb: 1 }}>
                <strong>Téléphone:</strong> +243 00 000 00 00
              </Typography>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)', mb: 1 }}>
                <strong>Email:</strong> contact@mulykap-clone.cd
              </Typography>
            </Grid>
          </Grid>
          <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)', mb: 4 }} />
          <Typography variant="body2" align="center" sx={{ color: 'rgba(255,255,255,0.5)' }}>
            &copy; {new Date().getFullYear()} Mulykap Clone. Tous droits réservés. (Projet de démonstration)
          </Typography>
        </Container>
      </Box>
    </Box>
  );
}
