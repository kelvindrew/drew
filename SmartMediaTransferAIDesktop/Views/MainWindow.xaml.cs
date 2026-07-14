using System.Windows;
using Wpf.Ui.Controls;
using SmartMediaTransferAIDesktop.ViewModels;

namespace SmartMediaTransferAIDesktop.Views
{
    public partial class MainWindow : FluentWindow
    {
        public MainWindow()
        {
            InitializeComponent();
            DataContext = App.Current.Services.GetService(typeof(MainViewModel));
        }
    }
}
