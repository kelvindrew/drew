using CommunityToolkit.Mvvm.ComponentModel;
using System.Collections.ObjectModel;
using SmartMediaTransferAIDesktop.Services;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.ViewModels
{
    public partial class IntelligenceViewModel : ObservableObject
    {
        private readonly DatabaseService _databaseService;

        public ObservableCollection<ArchivingRule> ActiveRules { get; } = new();

        [ObservableProperty]
        private bool _hasPendingConfirmation = true;

        [ObservableProperty]
        private string _pendingConfirmationFileName = "Inception_1080p.mkv";

        [ObservableProperty]
        private string _pendingConfirmationDestination = "D:\\Bibliothèque\\Films\\Inception";

        public IntelligenceViewModel(DatabaseService databaseService)
        {
            _databaseService = databaseService;
            LoadDataAsync();
        }

        private async void LoadDataAsync()
        {
            var rules = await _databaseService.GetActiveRulesAsync();
            foreach (var r in rules) ActiveRules.Add(r);
        }
    }
}
