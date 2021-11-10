package br.com.brunoxkk0.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class SecurityUtils {

    public static final int AES_KEY_LENGTH = 128;
    public static final int RSA_KEY_LENGTH = 2048;

    private static KeyFactory keyFactory = null;

    static {
        try {
            keyFactory = KeyFactory.getInstance("RSA"); /* Inicializa o KeyFactory de RSA utilizado para gerar as chaves.*/
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gera a chave pública baseada na entrada.
     *
     * @param key um byte[] da chave.
     *
     * @return chave pública.
     *
     * @exception Exception caso ocorra algum error ao tentar gerar a chave.
     **/
    public static PublicKey publicKeyFromString(byte[] key) throws Exception {
        return keyFactory.generatePublic(new X509EncodedKeySpec(key));
    }

    /**
     * Gera a chave privada baseada na entrada.
     *
     * @param key um byte[] da chave.
     *
     * @return chave privada.
     *
     * @exception Exception caso ocorra algum erro ao tentar gerar a chave.
     **/
    public static PrivateKey privateKeyFromString(String key) throws Exception {
        return keyFactory.generatePrivate(new X509EncodedKeySpec(key.getBytes()));
    }

    /**
     * Gera a chave AES baseada na string em Base64.
     *
     * @param key uma String com os dados em Base64.
     *
     * @return chave AES.
     **/
    public static SecretKey aesKeyFromBase64(String key) {
        byte[] k = Base64.getDecoder().decode(key);
        return new SecretKeySpec(Arrays.copyOf(k, 16), "AES");
    }

    /**
     * Gera a chave AES baseada no byte[] em Base64.
     *
     * @param key um byte[] com os dados em Base64.
     *
     * @return chave AES.
     **/
    public static SecretKey aesKeyFromBase64(byte[] key) {
        byte[] k = Base64.getDecoder().decode(key);
        return new SecretKeySpec(Arrays.copyOf(k, 16), "AES");
    }

    /**
     * Criptografa os dados contidos na source utilizando a chave RSA pública
     * informada.
     *
     * @param key chave RSA pública.
     * @param source dados a serem criptografados.
     *
     * @return byte[] com os dados criptografados.
     **/
    public static byte[] encrypt(PublicKey key, byte[] source) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptCipher.doFinal(source);
    }

    /**
     * Descriptografa os dados contidos na source utilizando a chave RSA privada
     * informada.
     *
     * @param key chave RSA privada.
     * @param source dados a serem descriptografados.
     *
     * @return byte[] com os dados descriptografados.
     **/
    public static byte[] decrypt(PrivateKey key, byte[] source) throws Exception {
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        return decryptCipher.doFinal(source);
    }

    /**
     * Gera um par de chaves RSA utilizando o tamanho {@link #RSA_KEY_LENGTH}.
     *
     * @return par de chaves RSA.
     *
     * @throws Exception caso ocorra algum erro ao tentar gerar a chave.
     */
    public static KeyPair genKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(RSA_KEY_LENGTH);
        return generator.generateKeyPair();
    }

    /**
     * Gera uma chave AES utilizando o tamanho {@link #AES_KEY_LENGTH}.
     *
     * @return chave AES.
     *
     * @throws Exception caso ocorra algum erro ao tentar gerar a chave.
     */
    public static SecretKey genAESKey() throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(AES_KEY_LENGTH);
        return generator.generateKey();
    }

    /**
     * Criptografa os dados contidos na source utilizando a chave AES informada,
     * e codifica em Base64.
     *
     * @param key chave AES.
     * @param source dados a serem criptografados.
     *
     * @return String com os dados criptografados e codificados em Base64.
     **/
    public static String encryptAES(SecretKey key, byte[] source) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(encryptCipher.doFinal(source));
    }

    /**
     * Descriptografa os dados contidos na source utilizando a chave AES informada.
     *
     * @param key chave AES.
     * @param source dados a serem criptografados codificados em Base64.
     *
     * @return String com os dados descriptografados.
     **/
    public static String decryptAES(SecretKey key, byte[] source) throws Exception {
        Cipher decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(decryptCipher.doFinal(Base64.getDecoder().decode(source)));
    }

    /**
     * Codifica os dados informados em Base64 e converte em String.
     *
     * @param data dados a serem codificados.
     * @return dados codificados.
     */
    public static String asBase64ToString(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Codifica os dados informados em Base64 e converte em String.
     *
     * @param data dados a serem codificados.
     * @return dados codificados.
     */
    public static String asBase64ToString(String data){
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Codifica os dados informados em Base64.
     *
     * @param data dados a serem codificados.
     * @return dados codificados.
     */
    public static byte[] asBase64(byte[] data){
        return Base64.getEncoder().encode(data);
    }

    /**
     * Codifica os dados informados em Base64.
     *
     * @param data dados a serem codificados.
     * @return dados codificados.
     */
    public static byte[] asBase64(String data){
        return Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodifica os dados informados em Base64 e converte em String.
     *
     * @param data dados a serem decodificados.
     * @return dados decodificados.
     */
    public static String fromBase64ToString(byte[] data){
        return new String(Base64.getDecoder().decode(data));
    }

    /**
     * Decodifica os dados informados em Base64 e converte em String.
     *
     * @param data dados a serem decodificados.
     * @return dados decodificados.
     */
    public static String fromBase64ToString(String data){
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    /**
     * Decodifica os dados informados em Base64.
     *
     * @param data dados a serem decodificados.
     * @return dados decodificados.
     */
    public static byte[] fromBase64(byte[] data){
        return Base64.getDecoder().decode(data);
    }

    /**
     * Decodifica os dados informados em Base64.
     *
     * @param data dados a serem decodificados.
     * @return dados decodificados.
     */
    public static byte[] fromBase64(String data){
        return Base64.getDecoder().decode(data);
    }

}
