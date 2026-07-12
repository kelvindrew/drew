import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { botRouter } from './routes/bot.routes';
import morgan from 'morgan';

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

app.use('/api/bot', botRouter);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'betpro-backend' });
});

app.listen(port, () => {
  console.log(`BetPro Backend listening at http://localhost:${port}`);
});
