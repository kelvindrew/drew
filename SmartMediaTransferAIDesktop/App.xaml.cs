using System;
using System.Windows;
using Microsoft.Extensions.DependencyInjection;
using SmartMediaTransferAIDesktop.ViewModels;
using SmartMediaTransferAIDesktop.Services;

namespace SmartMediaTransferAIDesktop
{
    public partial class App : Application
    {
        public new static App Current => (App)Application.Current;
        public IServiceProvider Services { get; }

        public App()
        {
            Services = ConfigureServices();
        }

        private static IServiceProvider ConfigureServices()
        {
            var services = new ServiceCollection();

            // Register Services
            services.AddSingleton<IDatabaseService, DatabaseService>();
            services.AddSingleton<DatabaseService>(); // for direct inject
            services.AddSingleton<ITransferService, TransferService>();
            services.AddSingleton<IDiscoveryService, DiscoveryService>();
            services.AddSingleton<FileOrganizationService>();
            services.AddSingleton<DiskManagerService>();
            services.AddSingleton<ArchivingService>();
            services.AddSingleton<TmdbService>();
            services.AddSingleton<RuleEngineService>();

            // Register ViewModels
            services.AddTransient<MainViewModel>();
            services.AddTransient<DashboardViewModel>();
            services.AddTransient<TransferViewModel>();
            services.AddTransient<StorageViewModel>();
            services.AddTransient<IntelligenceViewModel>();

            return services.BuildServiceProvider();
        }
    }
}
