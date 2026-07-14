using System;
using System.IO;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace SmartMediaTransferAIDesktop.Services
{
    public class SecurityService
    {
        private readonly byte[] _aesKey;
        private readonly byte[] _aesIv;

        public SecurityService()
        {
            _aesKey = new byte[32]; // 256-bit
            _aesIv = new byte[16];  // 128-bit

            using var rng = RandomNumberGenerator.Create();
            rng.GetBytes(_aesKey);
            rng.GetBytes(_aesIv);
        }

        public X509Certificate2 GenerateSelfSignedCertificate()
        {
            using var rsa = RSA.Create(2048);
            var req = new CertificateRequest("cn=SmartMediaTransferAI", rsa, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);
            var cert = req.CreateSelfSigned(DateTimeOffset.Now, DateTimeOffset.Now.AddYears(5));
            var pfx = cert.Export(X509ContentType.Pfx, "password");

#pragma warning disable SYSLIB0057
            return new X509Certificate2(pfx, "password");
#pragma warning restore SYSLIB0057
        }

        public CryptoStream GetEncryptionStream(Stream innerStream)
        {
            var aes = Aes.Create();
            aes.Key = _aesKey;
            aes.IV = _aesIv;
            var encryptor = aes.CreateEncryptor();
            return new CryptoStream(innerStream, encryptor, CryptoStreamMode.Write);
        }

        public CryptoStream GetDecryptionStream(Stream innerStream)
        {
            var aes = Aes.Create();
            aes.Key = _aesKey;
            aes.IV = _aesIv;
            var decryptor = aes.CreateDecryptor();
            return new CryptoStream(innerStream, decryptor, CryptoStreamMode.Read);
        }
    }
}
