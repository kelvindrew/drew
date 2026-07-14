import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import OpenAI from 'openai';

@Injectable()
export class AiService {
  private openai: OpenAI;

  constructor(private configService: ConfigService) {
    this.openai = new OpenAI({
      apiKey: this.configService.get<string>('OPENAI_API_KEY'),
    });
  }

  /**
   * Analyse le comportement suspect dans le chat
   */
  async analyzeMessageToxicity(message: string): Promise<number> {
    // Logique d'analyse de toxicité via LLM ou API de modération
    // Retourne un score de risque
    return 0.1;
  }

  /**
   * Suggère des profils compatibles basés sur les préférences
   */
  async getMatchingScore(clientInterests: string[], companionInterests: string[]): Promise<number> {
      // Logique de vectorisation ou prompt LLM pour calculer l'affinité
      return 85.5;
  }
}
