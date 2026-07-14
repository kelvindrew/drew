import httpx
import json
import asyncio
from utils.logger import logger

class LocalAIAssistant:
    def __init__(self):
        try:
            with open("config.json", "r", encoding="utf-8") as f:
                self.config = json.load(f)["ai"]
        except Exception:
            logger.error("Failed to load AI config", exc_info=True)
            self.config = {
                "endpoint": "http://localhost:11434/api/generate",
                "model": "llama3",
                "temperature": 0.3
            }

    async def generate_report(self, match_context):
        """
        Generates a summary and tactical analysis based on match statistics.
        Expects a local AI endpoint like Ollama running.
        """
        prompt = f"""
        En tant qu'expert en analyse sportive, rédige un rapport clair et concis (en français) sur ce match en te basant sur les statistiques suivantes :

        {json.dumps(match_context, indent=2, ensure_ascii=False)}

        Points à aborder :
        1. Résumé des forces/faiblesses
        2. Tendance générale
        3. Facteurs clés (domicile/extérieur, séries)
        """

        payload = {
            "model": self.config.get("model", "llama3"),
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": self.config.get("temperature", 0.3)
            }
        }

        logger.info(f"Requesting AI analysis using model {payload['model']}...")

        try:
            async with httpx.AsyncClient(timeout=120) as client:
                response = await client.post(self.config["endpoint"], json=payload)
                response.raise_for_status()
                data = response.json()
                return data.get("response", "Erreur: pas de réponse du modèle.")
        except httpx.ConnectError:
            msg = "Erreur de connexion à l'IA locale. Assurez-vous que le serveur (ex: Ollama) tourne sur le port configuré."
            logger.warning(msg)
            return msg
        except Exception as e:
            logger.error(f"Erreur lors de la génération du rapport IA: {e}", exc_info=True)
            return "Une erreur inattendue est survenue lors de l'analyse."
