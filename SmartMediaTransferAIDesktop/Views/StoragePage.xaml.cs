using System.Windows.Controls;
using SmartMediaTransferAIDesktop.ViewModels;

namespace SmartMediaTransferAIDesktop.Views
{
    public partial class StoragePage : Page
    {
        public StoragePage()
        {
            InitializeComponent();
            DataContext = App.Current.Services.GetService(typeof(StorageViewModel));
        }
    }
}
