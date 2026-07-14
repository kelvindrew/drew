using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class RuleEvaluationResult
    {
        public bool NeedsConfirmation { get; set; }
        public ArchivingRule? SelectedRule { get; set; }
        public List<ArchivingRule> ConflictingRules { get; set; } = new();
        public string ComputedCategory { get; set; } = string.Empty;
    }

    public class RuleEngineService
    {
        private readonly DatabaseService _dbService;
        private readonly TmdbService _tmdbService;

        public RuleEngineService(DatabaseService dbService, TmdbService tmdbService)
        {
            _dbService = dbService;
            _tmdbService = tmdbService;
        }

        public async Task<RuleEvaluationResult> EvaluateFileAsync(string filePath, long fileSize)
        {
            var result = new RuleEvaluationResult();

            // 1. Determine Category (using TmdbService for video)
            string extension = Path.GetExtension(filePath).ToLowerInvariant();
            result.ComputedCategory = await CategorizeFileAsync(filePath, extension);

            // 2. Fetch User Rules
            var allRules = await _dbService.GetActiveRulesAsync(); // ordered by Priority descending

            var matchedRules = new List<ArchivingRule>();

            foreach (var rule in allRules)
            {
                if (MatchesRule(filePath, fileSize, extension, result.ComputedCategory, rule))
                {
                    matchedRules.Add(rule);
                }
            }

            // 3. AI Learning Feedback Loop (Weight adjustment based on past rejections)
            matchedRules = await ApplyLearningWeightsAsync(matchedRules, extension, result.ComputedCategory);

            // 4. Handle Conflicts
            if (matchedRules.Count == 0)
            {
                // Fallback: AI Suggestion based on Category
                var aiRule = GenerateDynamicSuggestion(result.ComputedCategory);
                result.SelectedRule = aiRule;
                result.NeedsConfirmation = true; // Always confirm raw AI suggestions the first time
            }
            else if (matchedRules.Count == 1)
            {
                result.SelectedRule = matchedRules[0];
                result.NeedsConfirmation = false;
            }
            else
            {
                // Multiple matches - check if priorities are equal (conflict)
                var topPriority = matchedRules[0].Priority;
                var tops = matchedRules.Where(r => r.Priority == topPriority).ToList();

                if (tops.Count > 1)
                {
                    result.NeedsConfirmation = true;
                    result.ConflictingRules = tops;
                }
                else
                {
                    result.SelectedRule = tops[0];
                    result.NeedsConfirmation = false;
                }
            }

            return result;
        }

        private async Task<string> CategorizeFileAsync(string filePath, string extension)
        {
            if (extension == ".mp4" || extension == ".mkv" || extension == ".avi")
            {
                return await _tmdbService.AnalyzeVideoContentAsync(Path.GetFileName(filePath));
            }
            else if (extension == ".jpg" || extension == ".png") return "Photos";
            else if (extension == ".mp3" || extension == ".flac") return "Musiques";
            else if (extension == ".pdf" || extension == ".docx") return "Documents";
            else if (extension == ".zip" || extension == ".rar") return "Archives";
            else if (extension == ".exe" || extension == ".msi") return "Logiciels";

            return "Divers";
        }

        private bool MatchesRule(string filePath, long size, string extension, string category, ArchivingRule rule)
        {
            switch (rule.ConditionType)
            {
                case "Category":
                    return string.Equals(rule.ConditionValue, category, StringComparison.OrdinalIgnoreCase);
                case "FileSize":
                    if (rule.ConditionValue.StartsWith(">") && long.TryParse(rule.ConditionValue.Substring(1), out long ts))
                        return size > ts;
                    break;
                case "Extension":
                    return string.Equals(rule.ConditionValue, extension, StringComparison.OrdinalIgnoreCase);
                case "SourceFolder":
                    return filePath.StartsWith(rule.ConditionValue, StringComparison.OrdinalIgnoreCase);
            }
            return false;
        }

        private async Task<List<ArchivingRule>> ApplyLearningWeightsAsync(List<ArchivingRule> rules, string extension, string category)
        {
            // If the user previously rejected a specific rule heavily for this category, we artificially lower its priority here.
            var decisions = await _dbService.GetUserDecisionsAsync();
            var categoryDecisions = decisions.Where(d => d.AnalyzedType == category && d.Decision == DecisionType.Reject).ToList();

            foreach (var rule in rules.ToList()) // copy for iteration
            {
                int rejectCount = categoryDecisions.Count(d => d.RuleTriggeredName == rule.Name);
                if (rejectCount > 3)
                {
                    // Downgrade priority internally if user keeps rejecting this exact rule for this type
                    rule.Priority -= 10;
                }
            }

            return rules.OrderByDescending(r => r.Priority).ToList();
        }

        private ArchivingRule GenerateDynamicSuggestion(string category)
        {
            return new ArchivingRule
            {
                Name = $"Suggestion IA - {category}",
                ConditionType = "Category",
                ConditionValue = category,
                TargetDirectory = $"E:\\Bibliothèque\\{category}", // Mock dynamic root
                IsAIRecommended = true,
                Priority = 0 // Lowest priority
            };
        }
    }
}
