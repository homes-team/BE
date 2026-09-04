package com.homes.backend.global.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;

@Slf4j
@Component
public class ExifExtractor {
    public record ExifData(Double latitude, Double longitude) {}

    /**
     * 이미지 URL에서 EXIF GPS 정보를 추출합니다.
     */
    public ExifData extractGpsFromUrl(String imageUrl) {
        try (InputStream is = new URL(imageUrl).openStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            // GPS 디렉토리 추출
            GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDir == null || gpsDir.getGeoLocation() == null) {
                return null; // GPS 정보가 없는 경우 (스크린샷, 웹 다운로드 이미지 등)
            }

            double lat = gpsDir.getGeoLocation().getLatitude();
            double lon = gpsDir.getGeoLocation().getLongitude();

            return new ExifData(lat, lon);

        } catch (Exception e) {
            log.error("EXIF 추출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}
