namespace SmartMediaTransferAIDesktop.Services
{
    public class ValidationRequest
    {
        public string TransferId { get; set; } = string.Empty;
        public string ComputedHash { get; set; } = string.Empty;
    }

    public class ValidationResponse
    {
        public string TransferId { get; set; } = string.Empty;
        public bool IsValid { get; set; }
    }
}
