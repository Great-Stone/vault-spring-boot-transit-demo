package com.example.demo;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "vault_data") // 테이블 이름 명시
public class RawData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "data")
  private String data;

  @Column(name = "date_created")
  private Date dateCreated;

   // Getter 메서드
   public Long getId() {
    return id;
  }

  public String getData() {
      return data;
  }

  public Date getDateCreated() {
      return dateCreated;
  }

  // Setter 메서드
  public void setId(Long id) {
      this.id = id;
  }

  public void setData(String data) {
      this.data = data;
  }

  public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
  }
}