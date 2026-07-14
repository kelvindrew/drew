using System.Windows.Controls;
using SmartMediaTransferAIDesktop.ViewModels;

namespace SmartMediaTransferAIDesktop.Views
{
    public partial class IntelligencePage : Page
    {
        public IntelligencePage()
        {
            InitializeComponent();
            DataContext = App.Current.Services.GetService(typeof(IntelligenceViewModel));
        }
    }
}
