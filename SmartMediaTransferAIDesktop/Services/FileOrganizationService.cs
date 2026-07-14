using System;
using System.IO;
using System.Security.Cryptography;

namespace SmartMediaTransferAIDesktop.Services
{
    public class FileOrganizationService
    {
        private readonly string _baseStoragePath;

        public FileOrganizationService()
        {
            _baseStoragePath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments),
                "SmartMediaTransferAI");
        }

        public string DetermineDestinationPath(string fileName, string category, DateTime fileDate)
        {
            string year = fileDate.ToString("yyyy");
            string month = fileDate.ToString("MM");
            string day = fileDate.ToString("dd");

            string folderPath = Path.Combine(_baseStoragePath, category, year, month, day);
            Directory.CreateDirectory(folderPath);

            return Path.Combine(folderPath, fileName);
        }

        public string ComputeSHA256(string filePath)
        {
            using (var sha256 = SHA256.Create())
            {
                using (var stream = File.OpenRead(filePath))
                {
                    var hash = sha256.ComputeHash(stream);
                    return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
                }
            }
        }
    }
}
