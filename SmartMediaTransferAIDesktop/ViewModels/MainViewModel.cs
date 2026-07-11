using CommunityToolkit.Mvvm.ComponentModel;

namespace SmartMediaTransferAIDesktop.ViewModels
{
    public partial class MainViewModel : ObservableObject
    {
        [ObservableProperty]
        private string _applicationTitle = "Smart Media Transfer AI Desktop";
    }
}
