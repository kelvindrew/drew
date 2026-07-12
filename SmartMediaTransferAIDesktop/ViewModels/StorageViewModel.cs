using CommunityToolkit.Mvvm.ComponentModel;
using System.Collections.ObjectModel;
using SmartMediaTransferAIDesktop.Services;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.ViewModels
{
    public partial class StorageViewModel : ObservableObject
    {
        private readonly DiskManagerService _diskManager;
        private readonly DatabaseService _databaseService;

        public ObservableCollection<StorageDrive> ConnectedDrives { get; } = new();
        public ObservableCollection<ArchivingTask> ArchivingQueue { get; } = new();

        public StorageViewModel(DiskManagerService diskManager, DatabaseService databaseService)
        {
            _diskManager = diskManager;
            _databaseService = databaseService;
            LoadDataAsync();
        }

        private async void LoadDataAsync()
        {
            var drives = await _databaseService.GetConnectedDrivesAsync();
            foreach (var d in drives) ConnectedDrives.Add(d);

            var tasks = await _databaseService.GetPendingArchivingTasksAsync();
            foreach (var t in tasks) ArchivingQueue.Add(t);
        }
    }
}
