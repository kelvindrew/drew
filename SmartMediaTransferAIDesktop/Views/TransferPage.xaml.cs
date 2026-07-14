using System;
using System.Windows.Controls;
using SkiaSharp;
using SkiaSharp.Skottie;
using SkiaSharp.Views.Desktop;

namespace SmartMediaTransferAIDesktop.Views
{
    public partial class TransferPage : Page
    {
        private Animation? _lottieAnimation;

        public TransferPage()
        {
            InitializeComponent();
            LoadLottieAnimation();
        }

        private void LoadLottieAnimation()
        {
            // In production, this loads a rich 120fps JSON animation simulating the 3D phone-to-PC transfer
            try
            {
                // Placeholder logic: _lottieAnimation = Animation.Create("Assets/transfer_animation.json");
            }
            catch (Exception)
            {
                // Fallback to static XAML icons handled gracefully
            }
        }

        private void LottiePlayer_PaintSurface(object sender, SkiaSharp.Views.Desktop.SKPaintSurfaceEventArgs e)
        {
            if (_lottieAnimation == null) return;

            var canvas = e.Surface.Canvas;
            canvas.Clear(SKColors.Transparent);

            // Render the animation at the current frame based on time
            // TimeSpan time = ... calculate elapsed time
            // _lottieAnimation.SeekFrameTime((float)time.TotalSeconds);
            // _lottieAnimation.Render(canvas, new SKRect(0, 0, e.Info.Width, e.Info.Height));
        }
    }
}
