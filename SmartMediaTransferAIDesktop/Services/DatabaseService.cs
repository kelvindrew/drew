using SQLite;
using System;
using System.IO;
using System.Threading.Tasks;
using SmartMediaTransferAIDesktop.Models;
using System.Collections.Generic;

namespace SmartMediaTransferAIDesktop.Services
{
    public class DatabaseService : IDatabaseService
    {
        private SQLiteAsyncConnection? _db;

        public async void Initialize()
        {
            await InitAsync();
        }

        private async Task InitAsync()
        {
            if (_db != null)
                return;

            var databasePath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "SmartMediaTransferAI",
                "transfer_data.db");

            Directory.CreateDirectory(Path.GetDirectoryName(databasePath)!);

            _db = new SQLiteAsyncConnection(databasePath);
            await _db.CreateTableAsync<TransferRecord>();
            await _db.CreateTableAsync<TrustedDevice>();

            await _db.CreateTableAsync<StorageDrive>();
            await _db.CreateTableAsync<ArchivingTask>();
            await _db.CreateTableAsync<ArchivingRule>();
            await _db.CreateTableAsync<CategoryModel>();
            await _db.CreateTableAsync<UserDecision>();

            await SeedDefaultCategoriesAsync();
        }

        private async Task SeedDefaultCategoriesAsync()
        {
            var count = await _db!.Table<CategoryModel>().CountAsync();
            if (count == 0)
            {
                var defaults = new List<CategoryModel>
                {
                    new CategoryModel { Name = "Films", IsSystemDefault = true },
                    new CategoryModel { Name = "Séries", IsSystemDefault = true },
                    new CategoryModel { Name = "Animés", IsSystemDefault = true },
                    new CategoryModel { Name = "Jeux PC", IsSystemDefault = true },
                    new CategoryModel { Name = "Jeux PlayStation", IsSystemDefault = true },
                    new CategoryModel { Name = "Photos", IsSystemDefault = true },
                    new CategoryModel { Name = "Vidéos", IsSystemDefault = true },
                    new CategoryModel { Name = "Musiques", IsSystemDefault = true },
                    new CategoryModel { Name = "Documents", IsSystemDefault = true },
                    new CategoryModel { Name = "Archives", IsSystemDefault = true },
                    new CategoryModel { Name = "Logiciels", IsSystemDefault = true },
                    new CategoryModel { Name = "Projets", IsSystemDefault = true }
                };
                await _db.InsertAllAsync(defaults);
            }
        }

        public async Task<bool> IsFileAlreadyTransferredAsync(string fileHash)
        {
            await InitAsync();
            var record = await _db!.Table<TransferRecord>()
                                   .Where(r => r.FileHash == fileHash && r.State == TransferState.Verified)
                                   .FirstOrDefaultAsync();
            return record != null;
        }

        public async Task RecordTransferAsync(TransferRecord record)
        {
            await InitAsync();
            await _db!.InsertOrReplaceAsync(record);
        }

        public async Task<List<TransferRecord>> GetPendingTransfersForDeviceAsync(string deviceId)
        {
            await InitAsync();
            return await _db!.Table<TransferRecord>()
                             .Where(r => r.DeviceId == deviceId && r.State != TransferState.Verified)
                             .OrderByDescending(r => r.Priority)
                             .ThenBy(r => r.AddedToQueueDate)
                             .ToListAsync();
        }

        public async Task UpdateStorageDriveAsync(StorageDrive drive)
        {
            await InitAsync();
            await _db!.InsertOrReplaceAsync(drive);
        }

        public async Task<List<StorageDrive>> GetConnectedDrivesAsync()
        {
            await InitAsync();
            return await _db!.Table<StorageDrive>().Where(d => d.IsConnected).ToListAsync();
        }

        public async Task<List<ArchivingTask>> GetPendingArchivingTasksAsync()
        {
            await InitAsync();
            return await _db!.Table<ArchivingTask>()
                             .Where(t => t.State != TransferState.Verified)
                             .OrderByDescending(t => t.Priority)
                             .ThenBy(t => t.AddedToQueueDate)
                             .ToListAsync();
        }

        public async Task RecordArchivingTaskAsync(ArchivingTask task)
        {
            await InitAsync();
            await _db!.InsertOrReplaceAsync(task);
        }

        public async Task<List<ArchivingRule>> GetActiveRulesAsync()
        {
            await InitAsync();
            return await _db!.Table<ArchivingRule>()
                             .Where(r => r.IsEnabled)
                             .OrderByDescending(r => r.Priority)
                             .ToListAsync();
        }

        public async Task SaveArchivingRuleAsync(ArchivingRule rule)
        {
            await InitAsync();
            await _db!.InsertOrReplaceAsync(rule);
        }

        public async Task SaveUserDecisionAsync(UserDecision decision)
        {
            await InitAsync();
            await _db!.InsertAsync(decision);
        }

        public async Task<List<UserDecision>> GetUserDecisionsAsync()
        {
            await InitAsync();
            return await _db!.Table<UserDecision>().OrderByDescending(d => d.DecisionDate).ToListAsync();
        }

        public async Task<List<CategoryModel>> GetCategoriesAsync()
        {
            await InitAsync();
            return await _db!.Table<CategoryModel>().ToListAsync();
        }
    }
}
