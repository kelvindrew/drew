import { Request, Response } from 'express';
import { BetikaService } from '../services/betika.service';

const betikaService = new BetikaService();

export const placeBet = async (req: Request, res: Response): Promise<void> => {
  try {
    const { eventId, amount, odds, bookmaker } = req.body;

    // Auth token check could be added here
    const token = req.headers.authorization;
    if (token !== `Bearer ${process.env.BACKEND_API_KEY}`) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }

    if (!eventId || !amount) {
      res.status(400).json({ error: 'Missing required parameters' });
      return;
    }

    let result;
    if (bookmaker === 'betika') {
       result = await betikaService.placeBet(eventId, amount, odds);
    } else {
       // Support for betpawa etc.
       res.status(400).json({ error: 'Unsupported bookmaker' });
       return;
    }

    res.json({ success: true, result });
  } catch (error: any) {
    console.error('Error placing bet:', error);
    res.status(500).json({ error: 'Failed to place bet', details: error.message });
  }
};
