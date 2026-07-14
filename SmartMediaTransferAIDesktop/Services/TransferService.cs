using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Net.Security;
using System.Security.Authentication;
using System.Threading;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Text.Json;
using System.Text;
using SmartMediaTransferAIDesktop.Models;
using System.Collections.Generic;

namespace SmartMediaTransferAIDesktop.Services
{
    public class TransferService : ITransferService
    {
        private const int TransferPort = 8080;
        private TcpListener? _listener;
        private CancellationTokenSource? _cancellationTokenSource;
        private readonly SecurityService _securityService;
        private readonly DatabaseService _databaseService;
        private readonly FileOrganizationService _fileOrgService;

        public TransferService()
        {
            _securityService = new SecurityService();
            _databaseService = new DatabaseService();
            _fileOrgService = new FileOrganizationService();
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
            catch (OperationCanceledException) { }
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

                    var deviceId = await PerformHandshakeAsync(sslStream, token);
                    if (string.IsNullOrEmpty(deviceId)) return;

                    await ProcessProtocolLoopAsync(sslStream, deviceId, token);
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Client handling exception: {ex.Message}");
                }
            }
        }

        private async Task<string> PerformHandshakeAsync(SslStream sslStream, CancellationToken token)
        {
            var lengthBuffer = new byte[4];
            await sslStream.ReadExactlyAsync(lengthBuffer, 0, 4, token);
            int requestLength = BitConverter.ToInt32(lengthBuffer);

            var requestBuffer = new byte[requestLength];
            await sslStream.ReadExactlyAsync(requestBuffer, 0, requestLength, token);
            var jsonRequest = Encoding.UTF8.GetString(requestBuffer);

            var handshakeReq = JsonSerializer.Deserialize<SyncHandshakeRequest>(jsonRequest);
            if (handshakeReq == null) return string.Empty;

            var response = new SyncHandshakeResponse();
            var pendingTransfers = await _databaseService.GetPendingTransfersForDeviceAsync(handshakeReq.DeviceId);

            foreach (var proposed in handshakeReq.ProposedFiles)
            {
                bool isAlreadyVerified = await _databaseService.IsFileAlreadyTransferredAsync(proposed.FileHash);
                if (isAlreadyVerified)
                {
                    response.FileStatuses.Add(new SyncFileStatus { TransferId = proposed.TransferId, Action = "Skip", BytesReceived = proposed.SizeBytes });
                    continue;
                }

                var existingRecord = pendingTransfers.Find(r => r.TransferId == proposed.TransferId);
                if (existingRecord != null)
                {
                    if (existingRecord.BytesTransferred > 0 && existingRecord.BytesTransferred < existingRecord.SizeBytes)
                    {
                        response.FileStatuses.Add(new SyncFileStatus { TransferId = proposed.TransferId, Action = "Resume", BytesReceived = existingRecord.BytesTransferred });
                    }
                    else
                    {
                        response.FileStatuses.Add(new SyncFileStatus { TransferId = proposed.TransferId, Action = "Send", BytesReceived = 0 });
                    }
                }
                else
                {
                    var newRecord = new TransferRecord
                    {
                        TransferId = proposed.TransferId,
                        FileId = proposed.FileId,
                        FileHash = proposed.FileHash,
                        FileName = proposed.FileName,
                        SizeBytes = proposed.SizeBytes,
                        DeviceId = handshakeReq.DeviceId,
                        State = TransferState.Pending
                    };
                    await _databaseService.RecordTransferAsync(newRecord);
                    response.FileStatuses.Add(new SyncFileStatus { TransferId = proposed.TransferId, Action = "Send", BytesReceived = 0 });
                }
            }

            var jsonResponse = JsonSerializer.Serialize(response);
            var responseBytes = Encoding.UTF8.GetBytes(jsonResponse);
            var responseLengthBytes = BitConverter.GetBytes(responseBytes.Length);

            await sslStream.WriteAsync(responseLengthBytes, token);
            await sslStream.WriteAsync(responseBytes, token);

            return handshakeReq.DeviceId;
        }

        private async Task ProcessProtocolLoopAsync(SslStream sslStream, string deviceId, CancellationToken token)
        {
            // Simple protocol header: Type (1 byte), Length (4 bytes), Payload
            // Type 1: Chunk
            // Type 2: Validation Response (From Android -> PC)
            var typeBuffer = new byte[1];
            var lengthBuffer = new byte[4];

            while (!token.IsCancellationRequested)
            {
                int read = await sslStream.ReadAsync(typeBuffer, 0, 1, token);
                if (read == 0) break; // Client disconnected gracefully

                await sslStream.ReadExactlyAsync(lengthBuffer, 0, 4, token);
                int payloadLength = BitConverter.ToInt32(lengthBuffer);

                if (typeBuffer[0] == 1) // Chunk
                {
                    await ProcessChunkAsync(sslStream, payloadLength, deviceId, token);
                }
                else if (typeBuffer[0] == 2) // Validation Response
                {
                    await ProcessValidationResponseAsync(sslStream, payloadLength, deviceId, token);
                }
            }
        }

        private async Task ProcessChunkAsync(SslStream sslStream, int payloadLength, string deviceId, CancellationToken token)
        {
            // Read header length (first 4 bytes of payload)
            var headerLengthBuffer = new byte[4];
            await sslStream.ReadExactlyAsync(headerLengthBuffer, 0, 4, token);
            int headerLength = BitConverter.ToInt32(headerLengthBuffer);

            var headerBuffer = new byte[headerLength];
            await sslStream.ReadExactlyAsync(headerBuffer, 0, headerLength, token);
            var headerJson = Encoding.UTF8.GetString(headerBuffer);
            var header = JsonSerializer.Deserialize<FileChunkHeader>(headerJson);
            if (header == null) return;

            string tempFilePath = Path.Combine(Path.GetTempPath(), $"SmartMediaAI_{header.TransferId}.tmp");

            var chunkBuffer = new byte[header.ChunkSize];
            await sslStream.ReadExactlyAsync(chunkBuffer, 0, header.ChunkSize, token);

            using (var fs = new FileStream(tempFilePath, FileMode.OpenOrCreate, FileAccess.Write, FileShare.None))
            {
                fs.Seek(header.Offset, SeekOrigin.Begin);
                await fs.WriteAsync(chunkBuffer, 0, header.ChunkSize, token);
            }

            var pending = await _databaseService.GetPendingTransfersForDeviceAsync(deviceId);
            var record = pending.Find(r => r.TransferId == header.TransferId);

            if (record != null)
            {
                record.State = TransferState.InProgress;
                record.BytesTransferred = header.Offset + header.ChunkSize;

                if (record.BytesTransferred >= record.SizeBytes)
                {
                    record.State = TransferState.Completed;
                    record.DestinationPath = _fileOrgService.DetermineDestinationPath(record.FileName, "Downloads", DateTime.Now);

                    if (File.Exists(record.DestinationPath)) File.Delete(record.DestinationPath);
                    File.Move(tempFilePath, record.DestinationPath);

                    await _databaseService.RecordTransferAsync(record);

                    // 1. Calculate final SHA256 of the received file
                    string computedHash = _fileOrgService.ComputeSHA256(record.DestinationPath);

                    // 2. Send Validation Request to Android
                    var valReq = new ValidationRequest { TransferId = record.TransferId, ComputedHash = computedHash };
                    var valReqJson = JsonSerializer.Serialize(valReq);
                    var valReqBytes = Encoding.UTF8.GetBytes(valReqJson);

                    var outTypeBuffer = new byte[] { 2 }; // Type 2 = Validation Request
                    var outLengthBuffer = BitConverter.GetBytes(valReqBytes.Length);

                    await sslStream.WriteAsync(outTypeBuffer, token);
                    await sslStream.WriteAsync(outLengthBuffer, token);
                    await sslStream.WriteAsync(valReqBytes, token);
                }
                else
                {
                    await _databaseService.RecordTransferAsync(record);
                }
            }
        }

        private async Task ProcessValidationResponseAsync(SslStream sslStream, int payloadLength, string deviceId, CancellationToken token)
        {
            var buffer = new byte[payloadLength];
            await sslStream.ReadExactlyAsync(buffer, 0, payloadLength, token);
            var json = Encoding.UTF8.GetString(buffer);
            var response = JsonSerializer.Deserialize<ValidationResponse>(json);
            if (response == null) return;

            var pending = await _databaseService.GetPendingTransfersForDeviceAsync(deviceId);
            var record = pending.Find(r => r.TransferId == response.TransferId);

            if (record != null)
            {
                if (response.IsValid)
                {
                    record.State = TransferState.Verified;
                    record.TransferCompletionDate = DateTime.UtcNow;
                    await _databaseService.RecordTransferAsync(record);
                    Debug.WriteLine($"Transfer {record.TransferId} Validated & Verified successfully.");
                }
                else
                {
                    record.State = TransferState.Error;
                    record.BytesTransferred = 0; // Reset to force re-download on next sync
                    if (File.Exists(record.DestinationPath)) File.Delete(record.DestinationPath);
                    await _databaseService.RecordTransferAsync(record);
                    Debug.WriteLine($"Transfer {record.TransferId} Failed Validation.");
                }
            }
        }
    }
}
