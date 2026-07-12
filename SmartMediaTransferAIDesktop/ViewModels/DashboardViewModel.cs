using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using SmartMediaTransferAIDesktop.Services;
using System.Threading.Tasks;

namespace SmartMediaTransferAIDesktop.ViewModels
{
    public partial class DashboardViewModel : ObservableObject
    {
        private readonly DatabaseService _databaseService;

        [ObservableProperty]
        private string _totalStorageUsed = "0 GB";

        [ObservableProperty]
        private double _storageProgress = 0;

        [ObservableProperty]
        private int _totalFiles = 0;

        [ObservableProperty]
        private int _trustedDevicesCount = 0;

        public DashboardViewModel(DatabaseService databaseService)
        {
            _databaseService = databaseService;
            LoadDataAsync();
        }

        private async void LoadDataAsync()
        {
            // Note: In a real app, calculate real aggregates from database
            TotalFiles = 12408; // Placeholder bound value
            TrustedDevicesCount = 3;
            TotalStorageUsed = "145.2 GB";
            StorageProgress = 65;
        }
    }
}
