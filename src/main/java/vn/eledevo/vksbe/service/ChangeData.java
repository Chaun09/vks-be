package vn.eledevo.vksbe.service;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;

import vn.eledevo.vksbe.dto.request.DataChange;

public class ChangeData<T> {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "0123456789abcdef0123456789abcdef"; // 256 bit
    private static final String IV = "0123456789abcdef"; // 128 bit
    private static final Gson gson = new Gson();

    public static String encrypt(DataChange obj) throws Exception {
        String json = gson.toJson(obj);
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(json.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static <T> T decrypt(String encryptedData, Class<T> clazz) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted =
                cipher.doFinal(Base64.getDecoder().decode(encryptedData)); // Đảm bảo rằng bạn đang giải mã Base64
        String json = new String(decrypted);
        return gson.fromJson(json, clazz);
    }
}
