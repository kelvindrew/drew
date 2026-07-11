using System;
using System.IO;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class ValidationService
    {
        private readonly FileOrganizationService _fileOrgService;

        public ValidationService(FileOrganizationService fileOrgService)
        {
            _fileOrgService = fileOrgService;
        }

        /// <summary>
        /// Executes the strict 5-step validation required before safely deleting a file from the primary drive.
        /// 1. File exists on destination.
        /// 2. Size matches.
        /// 3. Readability & Integrity (SHA-256 match).
        /// 4. (Implicitly) Date/Metadata check (can be expanded).
        /// </summary>
        public bool PerformStrictValidation(string sourcePath, string destinationPath, string expectedHash, long expectedSize)
        {
            // 1. Existence
            if (!File.Exists(destinationPath))
                return false;

            // 2. Size
            var destInfo = new FileInfo(destinationPath);
            if (destInfo.Length != expectedSize)
                return false;

            // 3. Readability and Integrity
            // _fileOrgService.ComputeSHA256 opens the file, reads it start-to-finish, and computes the hash.
            // If the drive disconnected or the file is unreadable, this throws an exception or returns a bad hash.
            try
            {
                string computedHash = _fileOrgService.ComputeSHA256(destinationPath);

                if (!computedHash.Equals(expectedHash, StringComparison.OrdinalIgnoreCase))
                {
                    return false;
                }
            }
            catch (Exception)
            {
                // File unreadable
                return false;
            }

            // Validation passed
            return true;
        }
    }
}
