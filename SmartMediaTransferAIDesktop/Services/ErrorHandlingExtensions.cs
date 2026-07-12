using System;
using System.Diagnostics;
using System.Threading.Tasks;

namespace SmartMediaTransferAIDesktop.Services
{
    public static class ErrorHandlingExtensions
    {
        public static void SafeFireAndForget(this Task task)
        {
            task.ContinueWith(t =>
            {
                if (t.IsFaulted && t.Exception != null)
                {
                    foreach (var ex in t.Exception.InnerExceptions)
                    {
                        Debug.WriteLine($"[SafeFireAndForget] Exception: {ex.Message}");
                    }
                }
            }, TaskContinuationOptions.OnlyOnFaulted);
        }
    }
}
