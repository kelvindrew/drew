using System;
using System.Collections.Generic;
using System.IO;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class HeuristicAIEngine
    {
        private const long SizeThresholdLarge = 5_000_000_000; // 5 GB
        private const double SystemDriveWarningThreshold = 0.8; // 80%

        // Video extensions likely to be movies or series
        private readonly HashSet<string> _videoExtensions = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            ".mp4", ".mkv", ".avi", ".mov"
        };

        public List<ArchivingRule> EvaluateSystemAndSuggestRules(StorageDrive systemDrive, List<StorageDrive> externalDrives)
        {
            var suggestions = new List<ArchivingRule>();

            if (externalDrives.Count == 0) return suggestions; // Nowhere to archive

            var bestExternalDrive = externalDrives[0]; // Simplification: pick first

            // Rule 1: System drive almost full
            if ((double)(systemDrive.TotalSizeBytes - systemDrive.FreeSizeBytes) / systemDrive.TotalSizeBytes > SystemDriveWarningThreshold)
            {
                suggestions.Add(new ArchivingRule
                {
                    Name = "System Drive Full - Move Large Files",
                    ConditionType = "FileSize",
                    ConditionValue = ">1000000000", // > 1 GB
                    ActionType = TransferMode.Move,
                    TargetDriveId = bestExternalDrive.DriveId
                });
            }

            // Rule 2: Move big video files
            suggestions.Add(new ArchivingRule
            {
                Name = "Auto-Archive Large Videos",
                ConditionType = "FileTypeAndSize",
                ConditionValue = "Video,>5000000000", // > 5GB
                ActionType = TransferMode.Move,
                TargetDriveId = bestExternalDrive.DriveId
            });

            return suggestions;
        }

        public bool ShouldFileBeArchived(string filePath, long size, ArchivingRule rule)
        {
            try
            {
                if (rule.ConditionType == "FileSize")
                {
                    if (rule.ConditionValue.StartsWith(">"))
                    {
                        if (long.TryParse(rule.ConditionValue.Substring(1), out long threshold))
                            return size > threshold;
                    }
                }
                else if (rule.ConditionType == "FileTypeAndSize")
                {
                    var parts = rule.ConditionValue.Split(',');
                    if (parts.Length == 2 && parts[0] == "Video" && parts[1].StartsWith(">"))
                    {
                        var ext = Path.GetExtension(filePath);
                        if (_videoExtensions.Contains(ext))
                        {
                            if (long.TryParse(parts[1].Substring(1), out long threshold))
                                return size > threshold;
                        }
                    }
                }
            }
            catch { }
            return false;
        }
    }
}
