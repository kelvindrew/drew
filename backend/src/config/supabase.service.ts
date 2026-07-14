import { Injectable } from '@nestjs/common';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class SupabaseService {
  private client: SupabaseClient;

  constructor(private configService: ConfigService) {
    // Initialisation du client Supabase pour les opérations serveurs (Bypass RLS si nécessaire ou impersonation)
    const supabaseUrl = this.configService.get<string>('SUPABASE_URL');
    const supabaseKey = this.configService.get<string>('SUPABASE_SERVICE_ROLE_KEY');

    if (supabaseUrl && supabaseKey) {
        this.client = createClient(supabaseUrl, supabaseKey);
    }
  }

  getClient(): SupabaseClient {
    return this.client;
  }
}
