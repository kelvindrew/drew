using System;
using System.IO;
using System.Linq;
using System.Management;
using System.Threading;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Collections.Generic;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class DiskManagerService
    {
        private readonly DatabaseService _databaseService;
        private CancellationTokenSource? _cancellationTokenSource;

        public event Action<StorageDrive>? DriveConnected;
        public event Action<StorageDrive>? DriveDisconnected;

        public DiskManagerService()
        {
            _databaseService = new DatabaseService();
        }

        public void StartMonitoring()
        {
            _cancellationTokenSource = new CancellationTokenSource();
            _ = MonitorDrivesAsync(_cancellationTokenSource.Token);
        }

        public void StopMonitoring()
        {
            _cancellationTokenSource?.Cancel();
        }

        private async Task MonitorDrivesAsync(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await ScanAndSynchronizeDrivesAsync();
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Disk monitoring error: {ex.Message}");
                }

                await Task.Delay(TimeSpan.FromSeconds(5), token); // Check every 5 seconds
            }
        }

        private async Task ScanAndSynchronizeDrivesAsync()
        {
            var systemDrives = DriveInfo.GetDrives()
                .Where(d => d.IsReady && d.DriveType != DriveType.CDRom)
                .ToList();

            var currentlyConnectedIds = new HashSet<string>();

            foreach (var drive in systemDrives)
            {
                string driveId = GetVolumeSerial(drive.Name.Substring(0, 2)); // e.g. "C:"
                if (string.IsNullOrEmpty(driveId)) driveId = drive.Name; // Fallback

                currentlyConnectedIds.Add(driveId);

                var storageDrive = new StorageDrive
                {
                    DriveId = driveId,
                    DriveLetter = drive.Name,
                    VolumeLabel = drive.VolumeLabel,
                    FileSystem = drive.DriveFormat,
                    TotalSizeBytes = drive.TotalSize,
                    FreeSizeBytes = drive.AvailableFreeSpace,
                    IsConnected = true,
                    LastSeen = DateTime.UtcNow,
                    Type = DetermineDriveType(drive)
                };

                // Check SMART / Temperature if possible (requires admin, wraps in try-catch)
                CheckSmartStatus(storageDrive);

                await _databaseService.UpdateStorageDriveAsync(storageDrive);
                DriveConnected?.Invoke(storageDrive);
            }

            // Mark disconnected drives
            var previouslyConnected = await _databaseService.GetConnectedDrivesAsync();
            foreach (var prev in previouslyConnected)
            {
                if (!currentlyConnectedIds.Contains(prev.DriveId))
                {
                    prev.IsConnected = false;
                    await _databaseService.UpdateStorageDriveAsync(prev);
                    DriveDisconnected?.Invoke(prev);
                }
            }
        }

        private string GetVolumeSerial(string driveLetter)
        {
            if (OperatingSystem.IsWindows())
            {
                try
                {
                    using var searcher = new ManagementObjectSearcher($"SELECT VolumeSerialNumber FROM Win32_LogicalDisk WHERE DeviceID = '{driveLetter}'");
                    foreach (ManagementObject obj in searcher.Get())
                    {
                        return obj["VolumeSerialNumber"]?.ToString() ?? string.Empty;
                    }
                }
                catch { } // Permissions or WMI missing
            }
            return string.Empty;
        }

        private DriveTypeEnum DetermineDriveType(DriveInfo drive)
        {
            if (drive.DriveType == DriveType.Network) return DriveTypeEnum.NetworkNAS;
            if (drive.DriveType == DriveType.Removable) return DriveTypeEnum.ExternalUSB;

            // Heuristic for SSD vs HDD could be done via MSFT_PhysicalDisk in Storage namespace
            // For now, default to Unknown for fixed. We assume C: is InternalSSD.
            if (drive.DriveType == DriveType.Fixed)
            {
                return drive.Name.StartsWith("C", StringComparison.OrdinalIgnoreCase)
                       ? DriveTypeEnum.InternalSSD
                       : DriveTypeEnum.InternalHDD;
            }

            return DriveTypeEnum.Unknown;
        }

        private void CheckSmartStatus(StorageDrive drive)
        {
            // Implementation placeholder for deep WMI call `MSStorageDriver_ATAPISmartData`
            // Requires Administrator privileges. Fails gracefully if not admin.
        }
    }
}
