import asyncio
import os
import cv2
import pytesseract
from PIL import Image
from utils.logger import logger

# Note: Playwright needs to be installed in the environment (playwright install)
try:
    from playwright.async_api import async_playwright
    PLAYWRIGHT_AVAILABLE = True
except ImportError:
    PLAYWRIGHT_AVAILABLE = False
    logger.warning("Playwright not installed. Web scraping features will be disabled.")

class VisionScraper:
    def __init__(self):
        self.browser = None
        self.playwright = None
        os.makedirs("screenshots", exist_ok=True)

    async def start(self):
        if not PLAYWRIGHT_AVAILABLE:
            return
        self.playwright = await async_playwright().start()
        self.browser = await self.playwright.chromium.launch(headless=True)
        logger.info("Playwright browser started.")

    async def stop(self):
        if self.browser:
            await self.browser.close()
        if self.playwright:
            await self.playwright.stop()

    async def scrape_and_ocr(self, url):
        if not PLAYWRIGHT_AVAILABLE or not self.browser:
            logger.error("Scraping not available.")
            return None

        try:
            page = await self.browser.new_page()
            await page.goto(url, wait_until="networkidle")

            screenshot_path = f"screenshots/fallback_{url.replace('://', '_').replace('/', '_')}.png"
            await page.screenshot(path=screenshot_path)
            logger.info(f"Saved screenshot to {screenshot_path}")

            await page.close()

            # OCR Processing with OpenCV & Tesseract
            extracted_text = self._perform_ocr(screenshot_path)

            return {"status": "scraped", "text": extracted_text, "screenshot": screenshot_path}

        except Exception as e:
            logger.error(f"Error scraping {url}: {e}", exc_info=True)
            return None

    def _perform_ocr(self, image_path):
        try:
            # Load image with OpenCV
            img = cv2.imread(image_path)
            # Convert to grayscale
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            # Apply threshold to make text stand out
            _, thresh = cv2.threshold(gray, 150, 255, cv2.THRESH_BINARY_INV)

            # Run Tesseract OCR
            text = pytesseract.image_to_string(thresh, lang='eng+fra')
            logger.info("OCR extraction completed successfully.")
            return text.strip()
        except Exception as e:
            logger.error(f"OCR failed on {image_path}: {e}")
            return ""
