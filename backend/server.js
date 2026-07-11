const express = require('express');
const bodyParser = require('body-parser');
const { chromium } = require('playwright');
const { clickOnCanvasText } = require('./ocr-helper');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(bodyParser.json());

// API Endpoint to receive bet request and trigger auto-clicker
app.post('/api/place-bet', async (req, res) => {
    const betData = req.body;

    console.log('Received bet request:', betData);

    if (!betData.matchId || !betData.odds || !betData.stake) {
        return res.status(400).json({ error: 'Missing required bet parameters' });
    }

    try {
        // Run Playwright script in background
        await placeBetWithPlaywright(betData);
        res.status(200).json({ success: true, message: 'Bet placement process started successfully' });
    } catch (error) {
        console.error('Error in Playwright automation:', error);
        res.status(500).json({ success: false, error: 'Failed to execute bet placement' });
    }
});

async function placeBetWithPlaywright(betData) {
    console.log('Starting Playwright browser for bet:', betData);

    // Launch headless browser (set headless: false for debugging)
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    try {
        // Skeleton logic for Betpawa / Betika
        const platformUrl = betData.platform === 'betika' ? 'https://www.betika.cd' : 'https://www.betpawa.cd';

        console.log(`Navigating to ${platformUrl}`);
        await page.goto(platformUrl, { waitUntil: 'networkidle' });

        console.log('Attempting login...');
        if (betData.platform === 'betika') {
            // Betika login selectors (with OCR Fallback for Canvas/Image buttons)
            let loginClicked = false;
            try {
                await page.click('a.top-session-button, button:has-text("Connexion")', { timeout: 3000 });
                loginClicked = true;
            } catch (e) {
                console.log('Standard login selector failed, attempting OCR Fallback...');
                loginClicked = await clickOnCanvasText(page, 'Connexion');
            }

            if (loginClicked) {
                // await page.fill('input[type="number"], input[name="phone"]', process.env.PHONE_NUMBER);
                // await page.fill('input[type="password"]', process.env.PASSWORD);
                // await page.click('button:has-text("Se connecter"), button[type="submit"]');
            }
        } else {
            // Betpawa login selectors
            let loginClicked = false;
            try {
                await page.click('a.link:has-text("Login"), a:has-text("Connexion")', { timeout: 3000 });
                loginClicked = true;
            } catch (e) {
                console.log('Standard login selector failed, attempting OCR Fallback...');
                loginClicked = await clickOnCanvasText(page, 'Login');
            }

            if (loginClicked) {
                // await page.fill('input[type="tel"]', process.env.PHONE_NUMBER);
                // await page.fill('input[type="password"]', process.env.PASSWORD);
                // await page.click('button:has-text("Log In"), button:has-text("Connexion")');
            }
        }

        console.log(`Searching for match ID: ${betData.matchId} and placing bet on odds: ${betData.odds}`);
        // If odds are in a canvas grid:
        // await clickOnCanvasText(page, String(betData.odds));

        console.log(`Entering stake: ${betData.stake} and confirming bet...`);
        if (betData.platform === 'betika') {
            // await clickOnCanvasText(page, 'Panier');
            // await page.fill('input.betslip-stake, input[placeholder="Mise"]', String(betData.stake));
            // await clickOnCanvasText(page, 'Placer');
        } else {
            // await clickOnCanvasText(page, 'BETSLIP');
            // await page.fill('input[name="stake"]', String(betData.stake));
            // await clickOnCanvasText(page, 'Place bet');
        }

        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 2000));

        console.log('Bet automation logic generated with real site selectors (simulated execution).');

    } catch (error) {
        throw error;
    } finally {
        await browser.close();
    }
}

app.listen(PORT, () => {
    console.log(`BETPRO Auto-Clicker Server running on port ${PORT}`);
});
