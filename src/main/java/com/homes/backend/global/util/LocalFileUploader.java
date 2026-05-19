package com.homes.backend.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LocalFileUploader {
    /**
     * 컴퓨터에 임시로 이미지가 저장될 폴더 경로
     */
    private final String LOCAL_DIR = "C://homes_images//";

    public String upload(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) return null;
        try {
            String uuidFileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destination = new File(LOCAL_DIR + uuidFileName);

            if (!destination.getParentFile().exists()) destination.getParentFile().mkdirs();
            file.transferTo(destination);

            log.info("로컬 테스트용 이미지 저장 완료: {}", destination.getAbsolutePath());
            return "http://localhost:8080/temp-images/" + uuidFileName; // 프론트나 스웨거에서 일단 확인할 수 있도록 가짜 URL 형태 반환

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패");
        }
    }
}