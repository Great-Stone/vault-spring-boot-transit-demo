package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultDataRepository extends JpaRepository<VaultData, Long> {
    // 사용자 정의 쿼리 메서드 추가 가능
}
