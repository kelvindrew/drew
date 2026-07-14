import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
// Importation fictive des modules pour illustrer l'architecture
// import { UsersModule } from './modules/users/users.module';
// import { BookingsModule } from './modules/bookings/bookings.module';
// import { PaymentsModule } from './modules/payments/payments.module';
// import { AiModule } from './modules/ai/ai.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true, // Configuration accessible partout (Stripe, OpenAI, Supabase)
    }),
    // UsersModule,
    // BookingsModule,
    // PaymentsModule,
    // AiModule,
  ],
  controllers: [],
  providers: [],
})
export class AppModule {}
