'use client';
import { Container, Typography, Box, Accordion, AccordionSummary, AccordionDetails } from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import Link from 'next/link';

export default function FAQ() {
  const faqs = [
    {
      q: "Comment puis-je réserver un billet de bus VIP ?",
      a: "Vous pouvez réserver votre billet directement sur cette plateforme en allant dans la section 'Réserver'. Sélectionnez vos villes de départ et d'arrivée, choisissez votre siège et payez via mobile money ou carte bancaire."
    },
    {
      q: "Quels sont les avantages du service Mulykap VIP ?",
      a: "Nos bus VIP sont équipés de climatisation, de sièges inclinables ultra-confortables, de prises USB pour recharger vos appareils, de toilettes à bord et d'une connexion Wi-Fi sur certains trajets."
    },
    {
      q: "Comment suivre mon colis ?",
      a: "Rendez-vous dans la section 'Colis', entrez votre numéro de suivi (ex: PKG-1234) et vous verrez l'état d'avancement de votre expédition en temps réel."
    },
    {
      q: "Quelles sont les villes desservies ?",
      a: "Nous desservons principalement l'axe Lubumbashi - Likasi - Kolwezi avec des départs réguliers tous les jours."
    }
  ];

  return (
    <Container maxWidth="md" className="py-10" sx={{ minHeight: '100vh', pt: 10 }}>
      <Link href="/" style={{ color: '#E3000F', textDecoration: 'none', marginBottom: '24px', display: 'inline-block' }}>
        &larr; Retour à l'accueil
      </Link>

      <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 'bold', color: 'secondary.main', mb: 6 }}>
        Foire Aux Questions
      </Typography>

      <Box>
        {faqs.map((faq, index) => (
          <Accordion key={index} sx={{ mb: 2, borderRadius: '8px !important', '&:before': { display: 'none' }, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon color="primary" />} sx={{ p: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{faq.q}</Typography>
            </AccordionSummary>
            <AccordionDetails sx={{ px: 3, pb: 3, color: 'text.secondary' }}>
              <Typography>
                {faq.a}
              </Typography>
            </AccordionDetails>
          </Accordion>
        ))}
      </Box>
    </Container>
  );
}
