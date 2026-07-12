using System.Windows.Controls;
using SmartMediaTransferAIDesktop.ViewModels;

namespace SmartMediaTransferAIDesktop.Views
{
    public partial class DashboardPage : Page
    {
        public DashboardPage()
        {
            InitializeComponent();
            DataContext = App.Current.Services.GetService(typeof(DashboardViewModel));
        }
    }
}
