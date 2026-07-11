using SQLite;
using System;

namespace SmartMediaTransferAIDesktop.Models
{
    public enum TransferState
    {
        Pending,
        InProgress,
        Paused,
        Completed,
        Error,
        Verified
    }

    public enum TransferMode
    {
        Copy,
        Move
    }

    [Table("Transfers")]
    public class TransferRecord
    {
        [PrimaryKey]
        public string TransferId { get; set; } = Guid.NewGuid().ToString(); // Unique transfer ID

        [Indexed]
        public string FileId { get; set; } = string.Empty; // Unique file ID

        [Indexed]
        public string FileHash { get; set; } = string.Empty; // SHA-256

        public string FileName { get; set; } = string.Empty;
        public string OriginalPath { get; set; } = string.Empty;
        public string DestinationPath { get; set; } = string.Empty;

        public long SizeBytes { get; set; }
        public long BytesTransferred { get; set; } // For resuming

        [Indexed]
        public string DeviceId { get; set; } = string.Empty;

        public DateTime FileCreationDate { get; set; }
        public DateTime AddedToQueueDate { get; set; } = DateTime.UtcNow;
        public DateTime? TransferCompletionDate { get; set; }

        public string Category { get; set; } = string.Empty;

        public int Priority { get; set; } = 0; // Higher number = higher priority

        public TransferState State { get; set; } = TransferState.Pending;
        public TransferMode Mode { get; set; } = TransferMode.Copy;
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
