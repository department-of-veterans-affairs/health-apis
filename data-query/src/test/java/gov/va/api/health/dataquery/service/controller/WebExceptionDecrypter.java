package gov.va.api.health.dataquery.service.controller;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Tool to decrypt encrypted values in operation outcomes. */
public class WebExceptionDecrypter {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Example: web-exception-decryptor.sh key cipherText");
      return;
    }

    String encryptionKey = args[0];
    String cipherText = args[1];

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    byte[] allBytes = Base64.getDecoder().decode(cipherText);
    byte[] iv = Arrays.copyOfRange(allBytes, 0, cipher.getBlockSize());
    Key key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
    byte[] enBytes = Arrays.copyOfRange(allBytes, cipher.getBlockSize(), allBytes.length);
    String decrypted = new String(cipher.doFinal(enBytes), "UTF-8");

    System.out.println(decrypted);
  }
}
