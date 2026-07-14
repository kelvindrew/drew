using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Net.Http;
using System.Threading.Tasks;
using System.Reflection;

namespace SmartMediaTransferAIDesktop.Services
{
    public class UpdateService
    {
        private const string UpdateCheckUrl = "https://api.example.com/smartmediatransfer/updates/latest";
        private readonly HttpClient _httpClient;

        public UpdateService()
        {
            _httpClient = new HttpClient();
        }

        public async Task<bool> CheckForUpdatesAsync()
        {
            try
            {
                // In production, fetch JSON, compare Semantic Versioning
                // var response = await _httpClient.GetStringAsync(UpdateCheckUrl);
                // var latestVersion = ParseVersion(response);
                // var currentVersion = Assembly.GetExecutingAssembly().GetName().Version;
                // return latestVersion > currentVersion;
                return false; // Mock
            }
            catch
            {
                return false;
            }
        }

        public async Task DownloadAndApplyUpdateAsync(string downloadUrl)
        {
            try
            {
                string tempZip = Path.Combine(Path.GetTempPath(), "SmartMediaUpdate.zip");
                string extractDir = Path.Combine(Path.GetTempPath(), "SmartMediaUpdate_Extract");

                // 1. Download Update
                var response = await _httpClient.GetAsync(downloadUrl);
                using (var fs = new FileStream(tempZip, FileMode.Create))
                {
                    await response.Content.CopyToAsync(fs);
                }

                // 2. Extract
                if (Directory.Exists(extractDir)) Directory.Delete(extractDir, true);
                ZipFile.ExtractToDirectory(tempZip, extractDir);

                // 3. Prepare Auto-Update Batch Script for Portable Mode
                string currentAppPath = AppContext.BaseDirectory;
                string scriptPath = Path.Combine(Path.GetTempPath(), "update_smart_media.bat");

                string scriptContent = $@"
@echo off
echo Updating Smart Media Transfer AI...
timeout /t 3 /nobreak >nul
xcopy /s /y /q ""{extractDir}\*"" ""{currentAppPath}""
start """" ""{Path.Combine(currentAppPath, "SmartMediaTransferAIDesktop.exe")}""
del ""%~f0""
";
                await File.WriteAllTextAsync(scriptPath, scriptContent);

                // 4. Execute script and exit current app
                var processInfo = new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = $"/c \"{scriptPath}\"",
                    CreateNoWindow = true,
                    UseShellExecute = false
                };

                Process.Start(processInfo);
                Environment.Exit(0);
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Update failed: {ex.Message}");
            }
        }
    }
}
