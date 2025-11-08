package com.example.backendspring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    @Value("${training.image.storage.path:/app/training_images}")
    private String storageBasePath;

    /**
     * 이미지 파일을 영구 저장소에 저장
     *
     * @param imageFile 저장할 이미지 파일
     * @param userUniqueCode 유저 고유 코드
     * @return 저장된 파일의 경로
     * @throws IOException 파일 저장 실패 시
     */
    public String saveImage(MultipartFile imageFile, String userUniqueCode) throws IOException {
        log.info("이미지 저장 시작 - 유저: {}, 파일명: {}", userUniqueCode, imageFile.getOriginalFilename());

        // 저장 디렉토리 생성 (날짜별로 폴더 구분)
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path directoryPath = Paths.get(storageBasePath, userUniqueCode, dateFolder);

        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
            log.info("디렉토리 생성 완료: {}", directoryPath);
        }

        // 고유한 파일명 생성 (UUID + 타임스탬프 + 원본 확장자)
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String uniqueFilename = String.format("%s_%s%s",
            UUID.randomUUID().toString(),
            timestamp,
            fileExtension
        );

        // 파일 저장
        Path filePath = directoryPath.resolve(uniqueFilename);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String savedPath = filePath.toString();
        log.info("이미지 저장 완료: {}", savedPath);

        return savedPath;
    }

    /**
     * 저장된 이미지 파일 삭제
     *
     * @param imagePath 삭제할 이미지 경로
     * @return 삭제 성공 여부
     */
    public boolean deleteImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("이미지 삭제 완료: {}", imagePath);
                return true;
            } else {
                log.warn("삭제할 이미지가 존재하지 않음: {}", imagePath);
                return false;
            }
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", imagePath, e);
            return false;
        }
    }

    /**
     * 이미지 파일 존재 여부 확인
     *
     * @param imagePath 확인할 이미지 경로
     * @return 파일 존재 여부
     */
    public boolean exists(String imagePath) {
        return Files.exists(Paths.get(imagePath));
    }
}
