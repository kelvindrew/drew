import { Router } from 'express';
import { placeBet } from '../controllers/bot.controller';

export const botRouter = Router();

// Endpoint to trigger a bet
botRouter.post('/place-bet', placeBet);
