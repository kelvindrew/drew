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
            services.AddSingleton<ITransferService, TransferService>();
            services.AddSingleton<IDiscoveryService, DiscoveryService>();
            services.AddSingleton<IDatabaseService, DatabaseService>();

            // Register ViewModels
            services.AddTransient<MainViewModel>();
            services.AddTransient<DashboardViewModel>();
            services.AddTransient<TransferViewModel>();

            return services.BuildServiceProvider();
        }
    }
}
