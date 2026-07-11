using System.Collections.Generic;

namespace SmartMediaTransferAIDesktop.Services
{
    public class SyncHandshakeRequest
    {
        public string DeviceId { get; set; } = string.Empty;
        // The list of files the Android device wants to send
        public List<SyncFileItem> ProposedFiles { get; set; } = new();
    }

    public class SyncHandshakeResponse
    {
        // What files the PC already has, or partially has, and their status
        public List<SyncFileStatus> FileStatuses { get; set; } = new();
    }

    public class SyncFileItem
    {
        public string TransferId { get; set; } = string.Empty;
        public string FileId { get; set; } = string.Empty;
        public string FileHash { get; set; } = string.Empty;
        public string FileName { get; set; } = string.Empty;
        public long SizeBytes { get; set; }
    }

    public class SyncFileStatus
    {
        public string TransferId { get; set; } = string.Empty;
        public string Action { get; set; } = "Send"; // "Skip", "Resume", "Send"
        public long BytesReceived { get; set; } = 0;
    }
}
