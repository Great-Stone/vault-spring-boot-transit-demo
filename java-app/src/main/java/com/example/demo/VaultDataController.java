package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.lang.String;

import java.util.List;
import java.util.Optional;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.bouncycastle.util.Iterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Controller
public class VaultDataController {
  @Autowired
  private VaultDataRepository vaultDataRepository;

  @Autowired
  private RawDataRepository rawDataRepository;

  @Autowired
  private VaultService vaultService;

  @Value("${s3.enable}")
  private boolean s3Enable;

  @Value("${s3.bucket.name}")
  private String s3BucketName;

  private final S3Client s3Client = S3Client.create();

  private String encodeFileName(String fileName) throws UnsupportedEncodingException {
    String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
    return encodedFileName.replaceAll("\\+", "%20");
  }

  @GetMapping("/")
  public String getAllData(Model model) {
    List<VaultData> vaultData = vaultDataRepository.findAll();
    List<RawData> rawData = rawDataRepository.findAll();

    model.addAttribute("vault_data", vaultData);
    model.addAttribute("raw_data", rawData);
    model.addAttribute("s3Enable", s3Enable);
    model.addAttribute("s3BucketName", s3BucketName);
    model.addAttribute("newData", new VaultData());

    if (s3Enable) {
      // List 객체 요청
      ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
          .bucket(s3BucketName)
          .build();

      ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

      // for (S3Object s3Object : listObjectsV2Response.contents()) {
      // System.out.println("File name: " + s3Object.key());
      // System.out.println("File size: " + s3Object.size());
      // System.out.println("Last modified date: " + s3Object.lastModified());
      // }

      model.addAttribute("file_data", listObjectsV2Response.contents());

    } else {
      model.addAttribute("file_data", null);
    }

    return "dataList.html"; // Thymeleaf 템플릿 이름
  }

  @PostMapping("/insertData")
  public String insertData(VaultData newData) {
    vaultDataRepository.save(newData);
    return "redirect:/"; // 다시 데이터 목록으로 리다이렉트
  }

  @GetMapping("/rewrapData/{id}")
  public String rewrapData(@PathVariable Long id) {
    // id를 사용하여 VaultData 엔터티를 찾습니다.
    Optional<RawData> optionalVaultData = rawDataRepository.findById(id);

    if (optionalVaultData.isPresent()) {
        RawData rawData = optionalVaultData.get();
        String encryptChiperText = rawData.getData();

        System.out.println("********************");
        System.out.println(encryptChiperText);
        
        String rewrapChiperText = vaultService.rewrapData(encryptChiperText);
        rawData.setData(rewrapChiperText);

        rawDataRepository.save(rawData);
    } else {
        // 해당 id를 가진 VaultData 엔터티를 찾을 수 없는 경우의 처리
        // (예: 로그 출력, 예외 발생 등)
    }

    return "redirect:/"; // 다시 데이터 목록으로 리다이렉트
  }

  @PostMapping("/uploadEncrypt")
  public ResponseEntity<?> uploadEncrypte(@RequestParam("file") MultipartFile file) {
    // 여기에 파일 처리 로직을 추가
    if (!file.isEmpty()) {
      try {
        // // 파일 읽기
        byte[] fileData = file.getBytes();

        // Base64 Encoding
        byte[] encoded = Base64.getEncoder().encode(fileData);

        // 파일 데이터 암호화
        byte[] encryptedData = vaultService.encryptData(encoded);

        // 파일 이름
        String originalFilename = file.getOriginalFilename();
        String suffix = ".enc";
        File tempFile = new File(originalFilename + suffix);

        // S3 버킷 이름과 저장할 파일 이름 설정
        if (s3Enable) {
          // AWS S3에 파일 업로드
          PutObjectRequest putObjectRequest = PutObjectRequest.builder()
              .bucket(s3BucketName)
              .key(tempFile.getName())
              .build();

          s3Client.putObject(putObjectRequest, RequestBody.fromBytes(encryptedData));

          return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();

        } else {

          String encodedFileName = encodeFileName(tempFile.getName());

          // 바이트 배열을 사용하여 새로운 FileInputStream 생성
          try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            fileOutputStream.write(encryptedData);
          }

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
          headers.setContentDispositionFormData("attachment", encodedFileName);

          System.out.println(tempFile.getName());

          FileInputStream fileInputStream = new FileInputStream(tempFile);
          InputStreamResource resource = new InputStreamResource(fileInputStream);

          return ResponseEntity.ok()
              .headers(headers)
              .contentLength(tempFile.length())
              .body(resource);
        }

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

        // 확장자 검출
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.endsWith(".enc")) {
          originalFilename = originalFilename.substring(0, originalFilename.length() - 4);
        }

        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        File tempFile = new File(originalFilename);
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
          fileOutputStream.write(decoded);
        }

        // FileInputStream fileInputStream = new FileInputStream(tempFile);

        // 바이트 배열을 사용하여 새로운 FileInputStream 생성
        String encodedFileName = encodeFileName(tempFile.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFileName);

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

  @GetMapping("/downloadFileRaw/{fileName}")
  public ResponseEntity<InputStreamResource> downloadFileRaw(@PathVariable String fileName) {
    try {
      // S3에서 파일 다운로드 로직
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(s3BucketName)
          .key(fileName)
          .build();

      // S3에서 파일을 다운로드
      ResponseInputStream objectData = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());

      // InputStreamResource로 변환
      InputStreamResource resource = new InputStreamResource(objectData);

      // 파일 이름 인코딩
      String encodedFileName = encodeFileName(fileName);

      // 응답을 구성하고 반환
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
          .body(resource);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return ResponseEntity.notFound().build(); // 파일이 없거나 처리에 실패한 경우 404 응답 반환
  }

  @GetMapping("/downloadFileDec/{fileName}")
  public ResponseEntity<InputStreamResource> downloadFileDec(@PathVariable String fileName) {
    try {
      // S3에서 파일 다운로드 로직
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(s3BucketName)
          .key(fileName)
          .build();

      // S3에서 파일을 다운로드
      ResponseInputStream objectData = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());

      // 파일 읽기
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[1024];

      while ((nRead = objectData.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }

      buffer.flush();
      byte[] fileData = buffer.toByteArray();

      // 파일 데이터 복호화
      byte[] decryptedData = vaultService.decryptData(fileData);

      // Base64 Decoding
      byte[] decoded = Base64.getDecoder().decode(decryptedData);

      // 확장자 검출
      String originalFilename = fileName;
      if (originalFilename.endsWith(".enc")) {
        originalFilename = originalFilename.substring(0, originalFilename.length() - 4);
      }

      // 바이트 배열을 사용하여 새로운 FileInputStream 생성
      File tempFile = new File(originalFilename);
      try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
        fileOutputStream.write(decoded);
      }

      // 바이트 배열을 사용하여 새로운 FileInputStream 생성
      String encodedFileName = encodeFileName(tempFile.getName());

      InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));

      // 응답을 구성하고 반환
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
          .body(resource);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return ResponseEntity.notFound().build(); // 파일이 없거나 처리에 실패한 경우 404 응답 반환
  }
}