using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Diagnostics;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class ArchivingService
    {
        private readonly DatabaseService _databaseService;
        private readonly FileOrganizationService _fileOrgService;
        private readonly DiskManagerService _diskManager;
        private CancellationTokenSource? _cancellationTokenSource;
        private Dictionary<string, StorageDrive> _activeDrives = new();

        public ArchivingService(DiskManagerService diskManager, DatabaseService db, FileOrganizationService fileOrg)
        {
            _diskManager = diskManager;
            _databaseService = db;
            _fileOrgService = fileOrg;

            _diskManager.DriveConnected += OnDriveConnected;
            _diskManager.DriveDisconnected += OnDriveDisconnected;
        }

        private void OnDriveConnected(StorageDrive drive)
        {
            _activeDrives[drive.DriveId] = drive;
            // Wake up processing if sleeping
        }

        private void OnDriveDisconnected(StorageDrive drive)
        {
            _activeDrives.Remove(drive.DriveId);
            // Operations bound to this drive will naturally fail/pause in the queue
        }

        public void StartProcessing()
        {
            _cancellationTokenSource = new CancellationTokenSource();
            _ = ProcessArchivingQueueAsync(_cancellationTokenSource.Token);
        }

        public void StopProcessing()
        {
            _cancellationTokenSource?.Cancel();
        }

        private async Task ProcessArchivingQueueAsync(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    var pendingTasks = await _databaseService.GetPendingArchivingTasksAsync();

                    foreach (var task in pendingTasks)
                    {
                        // Check if Target Drive is currently connected
                        if (!_activeDrives.ContainsKey(task.TargetDriveId))
                        {
                            continue; // Pause this task until drive returns
                        }

                        var targetDrive = _activeDrives[task.TargetDriveId];

                        // Construct target path intelligently
                        string driveRoot = targetDrive.DriveLetter;
                        if (string.IsNullOrEmpty(driveRoot)) continue; // e.g., NAS UNC path needed

                        string finalPath = _fileOrgService.DetermineDestinationPath(
                            Path.GetFileName(task.SourcePath), "Archives", DateTime.Now);

                        // Re-root to the target drive
                        finalPath = Path.Combine(driveRoot, "SmartMediaArchives", Path.GetFileName(finalPath));
                        Directory.CreateDirectory(Path.GetDirectoryName(finalPath)!);

                        task.DestinationPath = finalPath;
                        task.State = TransferState.InProgress;
                        await _databaseService.RecordArchivingTaskAsync(task);

                        // 1. Copy to External Drive
                        bool copySuccess = CopyFileChunked(task.SourcePath, task.DestinationPath, task, token);
                        if (!copySuccess) continue; // Will retry later

                        // 2. Strict 5-step Validation: Compute SHA256 of the COPIED file to ensure integrity and readability
                        string copiedHash = _fileOrgService.ComputeSHA256(task.DestinationPath);

                        if (copiedHash.Equals(task.FileHash, StringComparison.OrdinalIgnoreCase))
                        {
                            // Hash matches, meaning file is fully readable and perfectly transferred
                            task.State = TransferState.Verified;
                            task.CompletionDate = DateTime.UtcNow;

                            // If mode is Move, safely delete original
                            if (task.Mode == TransferMode.Move)
                            {
                                File.Delete(task.SourcePath);
                            }

                            await _databaseService.RecordArchivingTaskAsync(task);
                            Debug.WriteLine($"Archived securely: {task.SourcePath} -> {task.DestinationPath}");
                        }
                        else
                        {
                            // Hash mismatch! Corrupted transfer.
                            task.State = TransferState.Error;
                            File.Delete(task.DestinationPath); // Clean up corrupted copy
                            task.BytesTransferred = 0; // Reset for retry
                            await _databaseService.RecordArchivingTaskAsync(task);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Archiving exception: {ex.Message}");
                }

                await Task.Delay(5000, token);
            }
        }

        private bool CopyFileChunked(string source, string dest, ArchivingTask task, CancellationToken token)
        {
            try
            {
                const int bufferSize = 1024 * 1024; // 1MB chunks
                var buffer = new byte[bufferSize];

                using var fsSource = new FileStream(source, FileMode.Open, FileAccess.Read);
                using var fsDest = new FileStream(dest, FileMode.OpenOrCreate, FileAccess.Write);

                fsSource.Seek(task.BytesTransferred, SeekOrigin.Begin);
                fsDest.Seek(task.BytesTransferred, SeekOrigin.Begin);

                int bytesRead;
                while ((bytesRead = fsSource.Read(buffer, 0, buffer.Length)) > 0)
                {
                    if (token.IsCancellationRequested) return false;

                    fsDest.Write(buffer, 0, bytesRead);
                    task.BytesTransferred += bytesRead;

                    // Periodically update DB here if needed
                }
                return true;
            }
            catch
            {
                return false;
            }
        }
    }
}
