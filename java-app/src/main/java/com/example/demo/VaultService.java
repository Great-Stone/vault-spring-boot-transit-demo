package com.example.demo;
import java.lang.String;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.VaultException;

@Service
public class VaultService {
  private final VaultOperations vaultOperations;

  @Value("${vault.transit.keyName}")
  private String transitKeyName;

  public VaultService(VaultOperations vaultOperations) {
    this.vaultOperations = vaultOperations;
  }

  public String encryptData(String dataToEncrypt) {
    Plaintext plaintext = Plaintext.of(dataToEncrypt);
    String encryptedData = vaultOperations.opsForTransit().encrypt(transitKeyName, plaintext).getCiphertext();
    return encryptedData;
  }

  public String decryptData(String encryptedData) {
    if (encryptedData.startsWith("vault:")) {
      try {
        String decryptedData = vaultOperations.opsForTransit().decrypt(transitKeyName, encryptedData);
        return decryptedData;
      } catch (VaultException e) {
        System.out.println(e.getMessage());
        if (e.getMessage().contains("too old")) {
          return encryptedData;
        }
        return "Decryption Error";
      }
    } else {
      return encryptedData;
    }
  }

  public String rewrapData(String encryptChiperText) {
    String rewrapChiperText = vaultOperations.opsForTransit().rewrap(transitKeyName, encryptChiperText);

    return rewrapChiperText;
  }

  public byte[] encryptData(byte[] dataToEncrypt) {
    // byte 배열을 Plaintext 객체로 변환
    Plaintext plaintext = Plaintext.of(dataToEncrypt);

    // 데이터 암호화
    byte[] encryptedData = vaultOperations.opsForTransit().encrypt(transitKeyName, plaintext).getCiphertext()
        .getBytes();

    return encryptedData;
  }

  public byte[] decryptData(byte[] encryptedData) {
    try {
      // byte 배열을 Plaintext 객체로 변환
      String text = new String(encryptedData, "UTF-8");

      // 데이터 복호화
      byte[] decryptedData = vaultOperations.opsForTransit().decrypt(transitKeyName, text).getBytes("UTF-8");

      return decryptedData;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace(); // 예외 처리 또는 로깅
      return null; // 예외가 발생하면 null 또는 다른 적절한 값 반환
    }
  }
}
