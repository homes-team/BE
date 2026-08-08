package com.homes.backend.global.init;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homes.backend.domain.property.entity.Station;
import com.homes.backend.domain.property.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StationDataInitializer implements ApplicationRunner {

    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (stationRepository.count() > 0) {
            log.info("✅ 지하철역 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        log.info("지하철역 데이터 초기화를 시작합니다...");

        try {
            // 파일이 반드시 src/main/resources 폴더 바로 아래에 있어야 합니다!
            InputStream inputStream = new ClassPathResource("transportation_processed.geojson").getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode features = rootNode.path("features");

            List<Station> stationsToSave = new ArrayList<>();

            for (JsonNode feature : features) {
                JsonNode properties = feature.path("properties");
                String poiType = properties.path("poi_type").asText();

                if ("지하철역".equals(poiType)) {
                    double lat = properties.path("lat").asDouble();
                    double lon = properties.path("lon").asDouble();
                    Point point = geometryFactory.createPoint(new Coordinate(lon, lat));

                    // '역역' -> '역' 으로 정제
                    String rawPoiName = properties.path("poi_name").asText();
                    String cleanPoiName = (rawPoiName != null && rawPoiName.endsWith("역역"))
                            ? rawPoiName.replace("역역", "역")
                            : rawPoiName;

                    Station station = Station.builder()
                            .poiId(properties.path("poi_id").asText())
                            .poiName(cleanPoiName)
                            .poiType(poiType)
                            .coordinate(point)
                            .build();

                    stationsToSave.add(station);
                }
            }

            stationRepository.saveAll(stationsToSave);
            log.info("지하철역 데이터 {}개 저장 완료!", stationsToSave.size());

        } catch (Exception e) {
            log.error("❌ 지하철역 데이터 초기화 중 에러 발생 (파일 경로 확인 필요)", e);
            throw new IllegalStateException("지하철역 데이터 초기화에 실패했습니다.", e);
        }
    }
}