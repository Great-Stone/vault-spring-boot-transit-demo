package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataRepository extends JpaRepository<RawData, Long> {
    // 사용자 정의 쿼리 메서드 추가 가능
}
