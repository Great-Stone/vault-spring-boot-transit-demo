package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VaultDataController {
  @Autowired
  private VaultDataRepository vaultDataRepository;

  @Autowired
  private RawDataRepository rawDataRepository;

  @Autowired
  private VaultService vaultService;

  @GetMapping("/")
  public String getAllData(Model model) {
    Iterable<VaultData> vaultData = vaultDataRepository.findAll();
    Iterable<RawData> rawData = rawDataRepository.findAll();

    model.addAttribute("vault_data", vaultData);
    model.addAttribute("raw_data", rawData);
    model.addAttribute("newData", new VaultData()); // 빈 데이터 객체
    return "dataList.html"; // Thymeleaf 템플릿 이름
  }

  @PostMapping("/insertData")
  public String insertData(VaultData newData) {
    vaultDataRepository.save(newData);
    return "redirect:/"; // 다시 데이터 목록으로 리다이렉트
  }

  @PostMapping("/uploadEncryptAndSave")
  public String uploadEncryptAndSave(@RequestParam("file") MultipartFile file) {
    // 여기에 파일 처리 로직을 추가
    if (!file.isEmpty()) {
      try {
        // // 파일 저장
        String FilePath = "/Users/gs/Downloads/" + file.getOriginalFilename();

        String tempFilePath = FilePath + ".tmp";
        file.transferTo(new File(tempFilePath));

        // // 파일 읽기
        FileInputStream fileInputStream = new FileInputStream(tempFilePath);
        byte[] fileData = fileInputStream.readAllBytes();
        fileInputStream.close();

        // 파일 데이터 암호화
        byte[] encryptedData = vaultService.encryptData(fileData);

        // 암호화된 데이터 저장
        String encryptedFilePath = FilePath + ".enc";
        FileOutputStream encryptedFileOutputStream = new FileOutputStream(encryptedFilePath);
        encryptedFileOutputStream.write(encryptedData);
        encryptedFileOutputStream.close();
        
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("FileEmpty:" + file.getOriginalFilename());
    }
    return "redirect:/"; // 다시 데이터 목록으로 리다이렉트
  }

  @PostMapping("/uploadEncrypt")
  public ResponseEntity<InputStreamResource> uploadEncrypte(@RequestParam("file") MultipartFile file) {
    // 여기에 파일 처리 로직을 추가
    if (!file.isEmpty()) {
      try {
        // // 파일 읽기
        byte[] fileData = file.getBytes();

        // Base64 Encoding
        byte[] encoded = Base64.getEncoder().encode(fileData);

        // 파일 데이터 암호화
        byte[] encryptedData = vaultService.encryptData(encoded);

        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        File tempFile = File.createTempFile("encrypted", ".txt"); // 임시 파일 생성
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        fileOutputStream.write(encryptedData);
        fileOutputStream.close();


        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", tempFile.getName());

        System.out.println(tempFile.getName());

        FileInputStream fileInputStream = new FileInputStream(tempFile);
        InputStreamResource resource = new InputStreamResource(fileInputStream);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(tempFile.length())
                .body(resource);
        
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    return ResponseEntity.notFound().build(); // 파일이 없거나 처리에 실패한 경우 404 응답 반환
  }

  @PostMapping("/uploadDecrypt")
  public ResponseEntity<InputStreamResource> uploadDecrypt(@RequestParam("file") MultipartFile file) {
    // 여기에 파일 처리 로직을 추가
    if (!file.isEmpty()) {
      try {
        // // 파일 읽기
        byte[] fileData = file.getBytes();

        // 파일 데이터 복호화
        byte[] decryptedData = vaultService.decryptData(fileData);

        // Base64 Decoding
        byte[] decoded = Base64.getDecoder().decode(decryptedData);

        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        File tempFile = File.createTempFile("decrypted", ".txt"); // 임시 파일 생성
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        fileOutputStream.write(decoded);
        fileOutputStream.close();

        // FileInputStream fileInputStream = new FileInputStream(tempFile);

        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", tempFile.getName());

        System.out.println(tempFile.getName());

        InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(tempFile.length())
                .body(resource);
        
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("FileEmpty:" + file.getOriginalFilename());
    }
    
    return ResponseEntity.notFound().build(); // 파일이 없거나 처리에 실패한 경우 404 응답 반환
  }
}