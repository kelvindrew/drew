using SQLite;
using System;

namespace SmartMediaTransferAIDesktop.Models
{
    [Table("Transfers")]
    public class TransferRecord
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }

        [Indexed]
        public string FileHash { get; set; } = string.Empty;

        public string FileName { get; set; } = string.Empty;
        public string OriginalPath { get; set; } = string.Empty;
        public string DestinationPath { get; set; } = string.Empty;
        public long SizeBytes { get; set; }

        [Indexed]
        public string DeviceId { get; set; } = string.Empty;

        public DateTime TransferDate { get; set; }
        public string Category { get; set; } = string.Empty;

        // e.g., "Completed", "Interrupted", "Failed"
        public string Status { get; set; } = string.Empty;
    }

    [Table("TrustedDevices")]
    public class TrustedDevice
    {
        [PrimaryKey]
        public string DeviceId { get; set; } = string.Empty;

        public string DeviceName { get; set; } = string.Empty;
        public string PublicKey { get; set; } = string.Empty;
        public DateTime FirstSeen { get; set; }
        public DateTime LastConnected { get; set; }
        public bool IsAuthorized { get; set; }
    }
}
