using System;
using System.Collections.Generic;
using System.IO;
using System.Text.Json;
using System.Threading.Tasks;
using SmartMediaTransferAIDesktop.Models;

namespace SmartMediaTransferAIDesktop.Services
{
    public class RuleExportService
    {
        private readonly DatabaseService _dbService;

        public RuleExportService(DatabaseService dbService)
        {
            _dbService = dbService;
        }

        public async Task ExportRulesToJsonAsync(string exportPath)
        {
            var rules = await _dbService.GetActiveRulesAsync();
            var json = JsonSerializer.Serialize(rules, new JsonSerializerOptions { WriteIndented = true });
            await File.WriteAllTextAsync(exportPath, json);
        }

        public async Task ImportRulesFromJsonAsync(string importPath)
        {
            var json = await File.ReadAllTextAsync(importPath);
            var rules = JsonSerializer.Deserialize<List<ArchivingRule>>(json);

            if (rules != null)
            {
                foreach (var rule in rules)
                {
                    // Reset ID so SQLite creates new entries instead of overriding existing IDs incorrectly
                    rule.Id = 0;
                    await _dbService.SaveArchivingRuleAsync(rule);
                }
            }
        }
    }
}
