package backend.academy.linktracker.scrapper.infrastructure.push;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class WebPushCrypto {

    private static final String CURVE = "secp256r1";
    private static final int SALT_LENGTH = 16;
    private static final int RECORD_SIZE = 4096;
    private static final int GCM_TAG_BITS = 128;
    private static final int PUBLIC_KEY_LENGTH = 65;
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebPushCrypto() {
    }

    public static byte[] encrypt(String recipientPublicKeyBase64Url, String authSecretBase64Url, String payload)
            throws GeneralSecurityException {
        byte[] recipientKey = base64UrlDecode(recipientPublicKeyBase64Url);
        byte[] authSecret = base64UrlDecode(authSecretBase64Url);
        byte[] plaintext = payload.getBytes(StandardCharsets.UTF_8);

        if (plaintext.length + 1 > RECORD_SIZE - 16) {
            throw new IllegalArgumentException("Сообщение слишком длинное для одной записи aes128gcm");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        KeyPair ephemeral = generateEcdhKeyPair();
        byte[] senderKey = rawPublicKey((ECPublicKey) ephemeral.getPublic());
        byte[] ecdhSecret = ecdh(ephemeral.getPrivate(), recipientKey);

        byte[] ikm = hkdf(
                authSecret,
                ecdhSecret,
                concat("WebPush: info".getBytes(StandardCharsets.US_ASCII), new byte[] {0}, recipientKey, senderKey),
                32);
        byte[] prkKey = hkdf(ikm, authSecret, concat("Content-Encoding: auth".getBytes(StandardCharsets.US_ASCII),
                new byte[] {0}), 32);
        byte[] contentEncryptionKey = hkdf(salt, prkKey,
                concat("Content-Encoding: aes128gcm".getBytes(StandardCharsets.US_ASCII), new byte[] {0}), 16);
        byte[] nonce = hkdf(salt, prkKey,
                concat("Content-Encoding: nonce".getBytes(StandardCharsets.US_ASCII), new byte[] {0}), 12);

        byte[] paddedRecord = Arrays.copyOf(plaintext, plaintext.length + 1);
        paddedRecord[plaintext.length] = 0x02;

        byte[] ciphertext = aesGcmEncrypt(contentEncryptionKey, nonce, paddedRecord);

        byte[] header = new byte[16 + 4 + 1 + senderKey.length];
        System.arraycopy(salt, 0, header, 0, SALT_LENGTH);
        header[16] = (byte) ((RECORD_SIZE >>> 24) & 0xff);
        header[17] = (byte) ((RECORD_SIZE >>> 16) & 0xff);
        header[18] = (byte) ((RECORD_SIZE >>> 8) & 0xff);
        header[19] = (byte) (RECORD_SIZE & 0xff);
        header[20] = (byte) senderKey.length;
        System.arraycopy(senderKey, 0, header, 21, senderKey.length);

        return concat(header, ciphertext);
    }

    public static String vapidAuthorizationHeader(
            String privateKeyBase64Url, String publicKeyBase64Url, String endpoint, String subject)
            throws GeneralSecurityException {
        PrivateKey privateKey = privateKeyFromPkcs8(privateKeyBase64Url);
        String origin = originOf(endpoint);
        long expiresAt = Instant.now().plusSeconds(12 * 3600).getEpochSecond();

        String header = base64Url("{\"typ\":\"JWT\",\"alg\":\"ES256\"}");
        String claims = base64Url("{\"aud\":\"" + origin + "\",\"exp\":" + expiresAt + ",\"sub\":\"" + subject + "\"}");
        String signingInput = header + "." + claims;

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
        String joseSignature = base64UrlRaw(derSignatureToJose(signature.sign()));

        return "vapid t=" + signingInput + "." + joseSignature + ", k=" + publicKeyBase64Url;
    }

    public static String[] generateVapidKeyPair() throws GeneralSecurityException {
        KeyPair pair = generateEcdhKeyPair();
        return new String[] {
            base64UrlRaw(rawPublicKey((ECPublicKey) pair.getPublic())),
            base64UrlRaw(pair.getPrivate().getEncoded()),
        };
    }

    static KeyPair generateEcdhKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(secp256r1Params());
        return generator.generateKeyPair();
    }

    static ECParameterSpec secp256r1Params() throws GeneralSecurityException {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec(CURVE));
        return parameters.getParameterSpec(ECParameterSpec.class);
    }

    static PublicKey publicKeyFromRaw(byte[] raw) throws GeneralSecurityException {
        if (raw.length != PUBLIC_KEY_LENGTH || raw[0] != 0x04) {
            throw new IllegalArgumentException("Ожидается uncompressed P-256 публичный ключ из 65 байт");
        }
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(raw, 1, 33));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(raw, 33, 65));
        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), secp256r1Params()));
    }

    static PrivateKey privateKeyFromPkcs8(String base64UrlPkcs8) throws GeneralSecurityException {
        byte[] der = base64UrlDecode(base64UrlPkcs8);
        return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    static byte[] rawPublicKey(ECPublicKey publicKey) {
        ECPoint w = publicKey.getW();
        byte[] x = unsignedBytes(w.getAffineX(), 32);
        byte[] y = unsignedBytes(w.getAffineY(), 32);
        byte[] raw = new byte[PUBLIC_KEY_LENGTH];
        raw[0] = 0x04;
        System.arraycopy(x, 0, raw, 1, 32);
        System.arraycopy(y, 0, raw, 33, 32);
        return raw;
    }

    static byte[] ecdh(PrivateKey ephemeralPrivate, byte[] recipientRawPublic) throws GeneralSecurityException {
        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init(ephemeralPrivate);
        agreement.doPhase(publicKeyFromRaw(recipientRawPublic), true);
        return unsignedBytes(new BigInteger(1, agreement.generateSecret()), 32);
    }

    static byte[] aesGcmEncrypt(byte[] key, byte[] nonce, byte[] plaintext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_BITS, nonce));
        return cipher.doFinal(plaintext);
    }

    static byte[] hkdf(byte[] salt, byte[] ikm, byte[] info, int length) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt.length == 0 ? new byte[32] : salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(ikm);

        ByteArrayOutputStream okm = new ByteArrayOutputStream();
        byte[] previous = new byte[0];
        int counter = 1;
        while (okm.size() < length) {
            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            mac.update(previous);
            mac.update(info);
            mac.update((byte) counter);
            previous = mac.doFinal();
            okm.writeBytes(previous);
            counter++;
        }
        return Arrays.copyOf(okm.toByteArray(), length);
    }

    private static byte[] derSignatureToJose(byte[] der) {
        int index = 2;
        int rLength = der[index + 1] & 0xff;
        byte[] r = Arrays.copyOfRange(der, index + 2, index + 2 + rLength);
        index += 2 + rLength;
        int sLength = der[index + 1] & 0xff;
        byte[] s = Arrays.copyOfRange(der, index + 2, index + 2 + sLength);

        byte[] jose = new byte[64];
        copyRightAligned(r, jose, 0, 32);
        copyRightAligned(s, jose, 32, 32);
        return jose;
    }

    private static void copyRightAligned(byte[] source, byte[] target, int offset, int length) {
        int copyLength = Math.min(source.length, length);
        System.arraycopy(source, source.length - copyLength, target, offset + length - copyLength, copyLength);
    }

    static byte[] unsignedBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        byte[] out = new byte[length];
        int copyLength = Math.min(bytes.length, length);
        System.arraycopy(bytes, bytes.length - copyLength, out, length - copyLength, copyLength);
        return out;
    }

    static String originOf(String endpoint) {
        var uri = java.net.URI.create(endpoint);
        String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        return uri.getScheme() + "://" + uri.getHost() + port;
    }

    static byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    static String base64Url(String value) {
        return base64UrlRaw(value.getBytes(StandardCharsets.UTF_8));
    }

    static String base64UrlRaw(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] part : parts) {
            total += part.length;
        }
        byte[] out = new byte[total];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, out, offset, part.length);
            offset += part.length;
        }
        return out;
    }
}
