using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text.Json;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using System.Diagnostics;

namespace SmartMediaTransferAIDesktop.Services
{
    public class TmdbService
    {
        private readonly string _apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3NjJhMzIxZTczZTczYWU5ZTg2MWY4M2ZiZTc2MzMyOSIsIm5iZiI6MTc4MzEwMjIxNi40MDQsInN1YiI6IjZhNDdmYjA4ZGY5NTZlYmIwNmVlZjM0OCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.p5yneMHX2-dRJ70UvKTo8pJ0DsX87hS9r28RjlAvdRA";
        private readonly HttpClient _httpClient;

        // Common TV show patterns: S01E01, 1x01, Season 1 Episode 1
        private readonly Regex _tvShowRegex = new Regex(@"[sS]\d{2}[eE]\d{2}|\d{1,2}x\d{2}|(?i)Season\s*\d+\s*Episode\s*\d+", RegexOptions.Compiled);

        public TmdbService()
        {
            _httpClient = new HttpClient();
            _httpClient.BaseAddress = new Uri("https://api.themoviedb.org/3/");
            _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _apiToken);
            _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }

        public async Task<string> AnalyzeVideoContentAsync(string filename)
        {
            // 1. Regex check for TV Shows
            if (_tvShowRegex.IsMatch(filename))
            {
                return "Séries";
            }

            // 2. Clean filename for search
            string cleanName = CleanFilename(filename);

            // 3. Fallback to API check for movies
            try
            {
                var response = await _httpClient.GetAsync($"search/movie?query={Uri.EscapeDataString(cleanName)}&language=fr-FR&page=1");
                if (response.IsSuccessStatusCode)
                {
                    var jsonString = await response.Content.ReadAsStringAsync();
                    using var doc = JsonDocument.Parse(jsonString);
                    var results = doc.RootElement.GetProperty("results");

                    if (results.GetArrayLength() > 0)
                    {
                        return "Films";
                    }
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"TMDB API Error: {ex.Message}");
            }

            // 4. Default if API fails or nothing found, but it's video
            return "Vidéos";
        }

        private string CleanFilename(string filename)
        {
            // Remove extension
            int extIdx = filename.LastIndexOf('.');
            if (extIdx > 0) filename = filename.Substring(0, extIdx);

            // Remove common tags [1080p], (2023), x264, etc.
            filename = Regex.Replace(filename, @"\[.*?\]|\(.*?\)", "");
            filename = Regex.Replace(filename, @"(?i)(1080p|720p|4k|2160p|x264|x265|hevc|bluray|webrip)", "");
            filename = filename.Replace(".", " ").Replace("_", " ").Trim();

            return filename;
        }
    }
}
