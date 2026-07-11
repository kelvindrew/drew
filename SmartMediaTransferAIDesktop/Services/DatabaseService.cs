using SQLite;
using System;
using System.IO;
using System.Threading.Tasks;
using SmartMediaTransferAIDesktop.Models;

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
        }

        public async Task<bool> IsFileAlreadyTransferredAsync(string fileHash)
        {
            await InitAsync();
            var record = await _db!.Table<TransferRecord>()
                                   .Where(r => r.FileHash == fileHash && r.Status == "Completed")
                                   .FirstOrDefaultAsync();
            return record != null;
        }

        public async Task RecordTransferAsync(TransferRecord record)
        {
            await InitAsync();
            await _db!.InsertAsync(record);
        }
    }
}
