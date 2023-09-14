package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostLoad;

@Component
public class VaultEncryptionListener {

    @Autowired
    private VaultService vaultService;

    @PostPersist
    public void encryptData(VaultData entity) {
        // 엔티티의 민감한 데이터를 암호화
        String encryptedData = vaultService.encryptData(entity.getData());
        System.out.println("***************************");
        System.out.println(encryptedData);
        System.out.println("***************************");
        entity.setData(encryptedData);
    }

    @PostLoad
    public void decryptData(VaultData entity) {
        // 데이터를 복호화
        String decryptedData = vaultService.decryptData(entity.getData());
        entity.setData(decryptedData);
    }
}
