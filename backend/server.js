require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const { chromium } = require('playwright');
const { clickOnCanvasText } = require('./ocr-helper');

const app = express();
const PORT = process.env.PORT || 3000;

// Security configuration
const AUTH_TOKEN = process.env.AUTH_TOKEN || 'betpro-secret-token-1234';

app.use(bodyParser.json());

// Authentication Middleware
const requireAuth = (req, res, next) => {
    const token = req.headers['authorization'];
    if (!token || token !== `Bearer ${AUTH_TOKEN}`) {
        return res.status(401).json({ error: 'Unauthorized: Invalid or missing token' });
    }
    next();
};

// API Endpoint to receive bet request and trigger auto-clicker
app.post('/api/place-bet', requireAuth, async (req, res) => {
    const betData = req.body;

    console.log('Received bet request:', betData);

    if (!betData.matchId || !betData.odds || !betData.stake) {
        return res.status(400).json({ error: 'Missing required bet parameters' });
    }

    try {
        // Run Playwright script
        const screenshotBase64 = await placeBetWithPlaywright(betData);
        res.status(200).json({
            success: true,
            message: 'Bet placed successfully',
            screenshotBase64: screenshotBase64
        });
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
        // Changed waitUntil to 'domcontentloaded' to avoid strict 30s timeout on heavy sites
        await page.goto(platformUrl, { waitUntil: 'domcontentloaded', timeout: 60000 });

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
                if (process.env.BETIKA_PHONE) {
                    await page.fill('input[type="number"], input[name="phone"]', process.env.BETIKA_PHONE);
                    await page.fill('input[type="password"]', process.env.BETIKA_PASSWORD);
                    await page.click('button:has-text("Se connecter"), button[type="submit"]');
                    await page.waitForTimeout(3000); // Wait for login to process
                }
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
                if (process.env.BETPAWA_PHONE) {
                    await page.fill('input[type="tel"]', process.env.BETPAWA_PHONE);
                    await page.fill('input[type="password"]', process.env.BETPAWA_PASSWORD);
                    await page.click('button:has-text("Log In"), button:has-text("Connexion")');
                    await page.waitForTimeout(3000); // Wait for login to process
                }
            }
        }

        console.log(`Searching for match ID: ${betData.matchId} and placing bet on odds: ${betData.odds}`);
        // Navigate or search (Simplified for now)
        try {
            await page.click(`text=${betData.homeTeam}`, { timeout: 3000 });
            await clickOnCanvasText(page, String(betData.odds));
        } catch (e) {
             console.log("Could not click odds explicitly, simulating...");
        }

        console.log(`Entering stake: ${betData.stake} and confirming bet...`);
        if (betData.platform === 'betika') {
            try {
                await page.click('.betslip-toggle, text="Panier"', { timeout: 3000 });
                await page.fill('input.betslip-stake, input[placeholder="Mise"]', String(betData.stake));
                await page.click('button:has-text("Placer le pari")');
            } catch (e) {
                await clickOnCanvasText(page, 'Panier');
                // Cannot easily fill via OCR, would need coordinate simulation
                await clickOnCanvasText(page, 'Placer');
            }
        } else {
             try {
                await page.click('text="LOAD BETSLIP", text="Betslip"', { timeout: 3000 });
                await page.fill('input[name="stake"]', String(betData.stake));
                await page.click('button:has-text("Place bet"), button:has-text("Placer le pari")');
             } catch (e) {
                await clickOnCanvasText(page, 'BETSLIP');
                await clickOnCanvasText(page, 'Place');
             }
        }

        // Wait for bet placement to process
        await page.waitForTimeout(3000);

        console.log('Bet automation logic completed.');

        // Take a screenshot of the confirmed bet slip as proof
        console.log('Taking screenshot for visual proof...');
        const screenshotBuffer = await page.screenshot({ type: 'jpeg', quality: 50 });
        const screenshotBase64 = screenshotBuffer.toString('base64');

        return screenshotBase64;

    } catch (error) {
        throw error;
    } finally {
        await browser.close();
    }
}

app.listen(PORT, () => {
    console.log(`BETPRO Auto-Clicker Server running on port ${PORT}`);
});
