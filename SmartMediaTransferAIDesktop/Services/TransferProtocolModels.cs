namespace SmartMediaTransferAIDesktop.Services
{
    // Represents a chunk of a file being transferred
    public class FileChunkHeader
    {
        public string TransferId { get; set; } = string.Empty;
        public long Offset { get; set; }
        public int ChunkSize { get; set; }
    }
}
