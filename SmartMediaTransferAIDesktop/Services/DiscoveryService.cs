using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace SmartMediaTransferAIDesktop.Services
{
    public class DiscoveryService : IDiscoveryService
    {
        private const int DiscoveryPort = 5353; // Standard mDNS port
        private const string MulticastGroup = "224.0.0.251";
        private CancellationTokenSource? _cancellationTokenSource;
        private UdpClient? _udpClient;

        public void StartDiscovery()
        {
            _cancellationTokenSource = new CancellationTokenSource();
            _ = ListenForMulticastAsync(_cancellationTokenSource.Token);
            _ = AnnouncePresenceAsync(_cancellationTokenSource.Token);
        }

        public void StopDiscovery()
        {
            _cancellationTokenSource?.Cancel();
            _udpClient?.Close();
        }

        private async Task ListenForMulticastAsync(CancellationToken token)
        {
            try
            {
                _udpClient = new UdpClient();
                _udpClient.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
                _udpClient.Client.Bind(new IPEndPoint(IPAddress.Any, DiscoveryPort));
                _udpClient.JoinMulticastGroup(IPAddress.Parse(MulticastGroup));

                while (!token.IsCancellationRequested)
                {
                    var result = await _udpClient.ReceiveAsync(token);
                    var message = Encoding.UTF8.GetString(result.Buffer);

                    if (message.Contains("SmartMediaTransferAI_Android"))
                    {
                        Debug.WriteLine($"Found Android Device at: {result.RemoteEndPoint}");
                        // Here we would typically raise an event to prompt the user to authorize the device
                        // or automatically initiate a handshake if it's already a trusted device.
                    }
                }
            }
            catch (OperationCanceledException)
            {
                // Expected when stopping
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error listening for multicast: {ex.Message}");
            }
        }

        private async Task AnnouncePresenceAsync(CancellationToken token)
        {
            try
            {
                using var sender = new UdpClient();
                var endpoint = new IPEndPoint(IPAddress.Parse(MulticastGroup), DiscoveryPort);
                var message = Encoding.UTF8.GetBytes("SmartMediaTransferAI_Desktop:" + Environment.MachineName);

                while (!token.IsCancellationRequested)
                {
                    await sender.SendAsync(message, message.Length, endpoint);
                    await Task.Delay(TimeSpan.FromSeconds(5), token);
                }
            }
            catch (OperationCanceledException)
            {
                // Expected
            }
        }
    }
}
