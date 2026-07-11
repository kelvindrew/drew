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

    public enum DriveTypeEnum
    {
        Unknown,
        InternalSSD,
        InternalHDD,
        ExternalUSB,
        ExternalSSD,
        ExternalHDD,
        NetworkNAS
    }

    [Table("StorageDrives")]
    public class StorageDrive
    {
        [PrimaryKey]
        public string DriveId { get; set; } = string.Empty; // e.g., Volume Serial Number or UUID

        public string DriveLetter { get; set; } = string.Empty;
        public string VolumeLabel { get; set; } = string.Empty;
        public DriveTypeEnum Type { get; set; } = DriveTypeEnum.Unknown;
        public string FileSystem { get; set; } = string.Empty;

        public long TotalSizeBytes { get; set; }
        public long FreeSizeBytes { get; set; }

        public bool IsConnected { get; set; }
        public DateTime LastSeen { get; set; }
    }

    [Table("ArchivingTasks")]
    public class ArchivingTask
    {
        [PrimaryKey]
        public string TaskId { get; set; } = Guid.NewGuid().ToString();

        public string SourcePath { get; set; } = string.Empty;
        public string TargetDriveId { get; set; } = string.Empty;
        public string DestinationPath { get; set; } = string.Empty;

        public string FileHash { get; set; } = string.Empty;
        public long SizeBytes { get; set; }
        public long BytesTransferred { get; set; }

        public TransferState State { get; set; } = TransferState.Pending;
        public TransferMode Mode { get; set; } = TransferMode.Move;

        public int Priority { get; set; } = 0;
        public DateTime AddedToQueueDate { get; set; } = DateTime.UtcNow;
        public DateTime? CompletionDate { get; set; }
    }

    [Table("Categories")]
    public class CategoryModel
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string DefaultPath { get; set; } = string.Empty;
        public bool IsSystemDefault { get; set; } = false;
    }

    [Table("ArchivingRules")]
    public class ArchivingRule
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }

        public string Name { get; set; } = string.Empty;

        // e.g. "FileSize", "FileType", "DriveCapacity", "Category", "FileNameRegex", "TMDBMatch"
        public string ConditionType { get; set; } = string.Empty;

        public string ConditionValue { get; set; } = string.Empty;

        public TransferMode ActionType { get; set; } = TransferMode.Move;
        public string TargetDriveId { get; set; } = string.Empty;
        public string TargetDirectory { get; set; } = string.Empty; // e.g., "E:\Library\Movies"

        public int Priority { get; set; } = 100;
        public int CategoryId { get; set; } = 0;

        public bool IsEnabled { get; set; } = true;
        public bool IsAIRecommended { get; set; } = false;
    }

    public enum DecisionType
    {
        Accept,
        Reject,
        Modify
    }

    [Table("UserDecisions")]
    public class UserDecision
    {
        [PrimaryKey, AutoIncrement]
        public int Id { get; set; }

        public string OriginalPath { get; set; } = string.Empty;
        public string SuggestedDestination { get; set; } = string.Empty;
        public string ActualDestination { get; set; } = string.Empty;

        public string RuleTriggeredName { get; set; } = string.Empty;
        public DecisionType Decision { get; set; }
        public DateTime DecisionDate { get; set; } = DateTime.UtcNow;

        // Features for the ML-like model
        public string FileExtension { get; set; } = string.Empty;
        public long SizeBytes { get; set; }
        public string AnalyzedType { get; set; } = string.Empty; // e.g. "Movie", "TVShow"
    }
}
