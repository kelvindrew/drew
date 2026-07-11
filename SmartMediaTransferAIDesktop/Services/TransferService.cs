using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Net.Security;
using System.Security.Authentication;
using System.Threading;
using System.Threading.Tasks;
using System.Diagnostics;

namespace SmartMediaTransferAIDesktop.Services
{
    public class TransferService : ITransferService
    {
        private const int TransferPort = 8080;
        private TcpListener? _listener;
        private CancellationTokenSource? _cancellationTokenSource;
        private readonly SecurityService _securityService;

        public TransferService()
        {
            _securityService = new SecurityService();
        }

        public void StartServer()
        {
            _cancellationTokenSource = new CancellationTokenSource();
            _listener = new TcpListener(IPAddress.Any, TransferPort);
            _listener.Start();

            _ = AcceptClientsAsync(_cancellationTokenSource.Token);
        }

        public void StopServer()
        {
            _cancellationTokenSource?.Cancel();
            _listener?.Stop();
        }

        private async Task AcceptClientsAsync(CancellationToken token)
        {
            try
            {
                var serverCertificate = _securityService.GenerateSelfSignedCertificate();

                while (!token.IsCancellationRequested)
                {
                    var client = await _listener!.AcceptTcpClientAsync(token);
                    _ = HandleClientAsync(client, serverCertificate, token);
                }
            }
            catch (OperationCanceledException)
            {
                // Expected when shutting down
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Server exception: {ex.Message}");
            }
        }

        private async Task HandleClientAsync(TcpClient client, System.Security.Cryptography.X509Certificates.X509Certificate serverCertificate, CancellationToken token)
        {
            using (client)
            using (var stream = client.GetStream())
            using (var sslStream = new SslStream(stream, false))
            {
                try
                {
                    await sslStream.AuthenticateAsServerAsync(serverCertificate, clientCertificateRequired: false, SslProtocols.Tls13 | SslProtocols.Tls12, checkCertificateRevocation: true);

                    // For demonstration: simple file receive logic inside the secure TLS channel.
                    // In a production scenario, we would use _securityService.GetDecryptionStream() here if an extra layer of AES-256 over TLS is explicitly required by the protocol.

                    var buffer = new byte[81920]; // 80KB buffer for high speed
                    int bytesRead;

                    // Note: Needs a proper protocol header to know the filename and size.
                    // This is a placeholder for the actual receive loop.
                    string tempPath = Path.GetTempFileName();
                    using var fs = new FileStream(tempPath, FileMode.Create, FileAccess.Write, FileShare.None, buffer.Length, useAsync: true);

                    while ((bytesRead = await sslStream.ReadAsync(buffer, 0, buffer.Length, token)) > 0)
                    {
                        await fs.WriteAsync(buffer, 0, bytesRead, token);
                    }

                    Debug.WriteLine("Transfer completed successfully.");
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Client handling exception: {ex.Message}");
                }
            }
        }
    }
}
