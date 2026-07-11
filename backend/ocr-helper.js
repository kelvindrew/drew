const Tesseract = require('tesseract.js');

/**
 * Prend une capture d'écran de la page, utilise l'OCR pour trouver un texte spécifique,
 * puis effectue un clic de souris simulé au centre de ce texte.
 * Utile pour les boutons rendus en <canvas> ou sous forme d'images.
 *
 * @param {import('playwright').Page} page - L'instance de la page Playwright.
 * @param {string} targetText - Le texte exact (ou partiel) à rechercher (insensible à la casse).
 * @returns {Promise<boolean>} True si le texte a été trouvé et cliqué, False sinon.
 */
async function clickOnCanvasText(page, targetText) {
    console.log(`[OCR] Starting search for text: "${targetText}"`);
    try {
        // 1. Take a full page screenshot
        const screenshotBuffer = await page.screenshot({ type: 'png' });

        // 2. Perform OCR analysis using Tesseract
        console.log('[OCR] Processing image...');
        // Using OEM 3 (Default) and PSM 11 (Sparse text, find as much text as possible in no particular order)
        const { data: { words } } = await Tesseract.recognize(
            screenshotBuffer,
            'eng+fra', // Support English and French
            { logger: m => {} } // Mute logs
        );

        // 3. Find the word
        const targetLower = targetText.toLowerCase();
        let foundWord = null;

        for (const word of words) {
            if (word.text.toLowerCase().includes(targetLower)) {
                foundWord = word;
                break;
            }
        }

        if (!foundWord) {
            console.log(`[OCR] Target text "${targetText}" not found on screen.`);
            return false;
        }

        console.log(`[OCR] Text "${foundWord.text}" found at bounding box:`, foundWord.bbox);

        // 4. Calculate the center point of the bounding box
        const bbox = foundWord.bbox;
        const centerX = bbox.x0 + ((bbox.x1 - bbox.x0) / 2);
        const centerY = bbox.y0 + ((bbox.y1 - bbox.y0) / 2);

        // 5. Simulate human mouse movement and click
        console.log(`[OCR] Moving mouse to (${centerX}, ${centerY}) and clicking...`);
        await page.mouse.move(centerX, centerY);
        await page.waitForTimeout(200); // Slight human delay
        await page.mouse.down();
        await page.waitForTimeout(100);
        await page.mouse.up();

        return true;

    } catch (error) {
        console.error('[OCR] Error during canvas text click:', error);
        return false;
    }
}

module.exports = { clickOnCanvasText };
