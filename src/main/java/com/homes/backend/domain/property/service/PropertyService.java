package com.homes.backend.domain.property.service;

import com.homes.backend.domain.property.dto.request.PropertyCreateReqDto;
import com.homes.backend.domain.property.dto.request.PropertyMapSearchReqDto;
import com.homes.backend.domain.property.dto.request.PropertyUpdateReqDto;
import com.homes.backend.domain.property.dto.response.PropertyDetailRespDto;
import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyImage;
import com.homes.backend.domain.property.exception.PropertyErrorCode;
import com.homes.backend.domain.property.repository.PropertyRepository;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.util.LocalFileUploader;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final LocalFileUploader localFileUploader; // S3 대신 로컬 업로더 주입
    private final UserRepository userRepository;

    /**
     * GPS 표준인 4326(WGS84) 기반으로 Point를 만들어주는 팩토리
     */
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 매물 등록 (Create)
     */
    @Transactional
    public Long createProperty(PropertyCreateReqDto reqDto, Long userId, List<MultipartFile> images) throws IOException {
        User user=userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(UserErrorCode.USER_NOT_FOUND));

        /**
         *  Double 위도/경도를 공간 데이터(Point)로 변환
         *  (주의: Coordinate는 X(경도), Y(위도) 순서로 넣음)
          */
        Point point = geometryFactory.createPoint(new Coordinate(reqDto.longitude(), reqDto.latitude()));

        /**
         *  자동 부제목 생성 로직 (예: "서울시 강남구 역삼동 123" -> "강남구 역삼동 원/투룸")
         */
        String generatedTitle = generateAutomatedTitle(reqDto.address(), reqDto.propertyType().getDescription());

        Property property = Property.builder()
                .user(user)
                .title(generatedTitle)
                .description(reqDto.description())
                .address(reqDto.address())
                .detailAddress(reqDto.detailAddress())
                .tradeType(reqDto.tradeType())
                .propertyType(reqDto.propertyType())
                .deposit(reqDto.deposit())
                .monthlyRent(reqDto.monthlyRent())
                .maintenanceFee(reqDto.maintenanceFee())
                .totalFloors(reqDto.totalFloors())
                .currentFloor(reqDto.currentFloor())
                .area(reqDto.area())
                .coordinate(point)
                .desiredBrokerageFee(reqDto.desiredBrokerageFee())
                .tags(reqDto.tags())
                .aiScore(85) // 가짜 AI 점수를 넣어두고, 추후 AI 모델 연동 시 고도화 예정
                .build();

        /**
         * 다중이미지 업로드
          */
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = localFileUploader.upload(images.get(i), "properties"); // 파라미터 2개(파일, 폴더명)

                boolean isThumbnail = (i == 0);

                PropertyImage propertyImage = PropertyImage.builder()
                        .imageUrl(imageUrl)
                        .isThumbnail(isThumbnail)
                        .property(property)
                        .build();

                property.getImages().add(propertyImage);
            }
        }

        return propertyRepository.save(property).getId();
    }

    /**
     * 주소 기반 자동 부제목 조합기
     */
    private String generateAutomatedTitle(String fullAddress, String propertyDescription) {
        String[] addressParts = fullAddress.split(" ");
        /**
         * 카카오 주소 API 결과 : 보통 "시 구 동 ..." 순서로 배치
         * -> 구&동 가져오기
         */
        if (addressParts.length >= 3) {
            return addressParts[1] + " " + addressParts[2] + " " + propertyDescription; // 결과: "강남구 역삼동 원룸"
        }

        return fullAddress + " " + propertyDescription; // 만약 주소가 짧은 예외 케이스라면 전체 주소를 사용
    }

    /**
     * 전체 매물 리스트 조회 (Read)
     */
    @Transactional(readOnly = true)
    public List<PropertyListRespDto> getAllProperties() {
        return propertyRepository.findAllByOrderByIdDesc().stream()
                .map(PropertyListRespDto::from)
                .toList();
    }

    /**
     * 매물 상세 조회 (Read)
     */
    @Transactional(readOnly = true)
    public PropertyDetailRespDto getProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        return PropertyDetailRespDto.from(property);
    }

    /**
     * 소유권 검증 (내 매물이 맞는지 확인하는 로직)
     */
    public void validateOwnership(Property property, Long userId) {
        if (!property.getUser().getId().equals(userId)) {
            throw new CustomException(PropertyErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    /**
     * 매물 삭제 (Delete)
     */
    @Transactional
    public void deleteProperty(Long propertyId, Long userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        validateOwnership(property, userId);
        propertyRepository.delete(property);
    }


    /**
     * 매물 수정 (Update)
     */
    @Transactional
    public void updateProperty(Long propertyId, PropertyUpdateReqDto reqDto, List<MultipartFile> newImages, Long userId) throws IOException {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        validateOwnership(property, userId);

        /**
         * 수정된 데이터에 맞춰 위경도 Point 변환 및 자동 부제목 재조립
         */
        Point point = geometryFactory.createPoint(new Coordinate(reqDto.longitude(), reqDto.latitude()));
        String updatedTitle = generateAutomatedTitle(reqDto.address(), reqDto.propertyType().getDescription());

        property.update(
                updatedTitle, reqDto.description(), reqDto.address(), reqDto.detailAddress(),
                reqDto.tradeType(), reqDto.propertyType(), reqDto.deposit(),
                reqDto.monthlyRent(), reqDto.maintenanceFee(), reqDto.totalFloors(),
                reqDto.currentFloor(), reqDto.area(), point,
                reqDto.desiredBrokerageFee(), reqDto.tags()
        );

        /**
         * 새 이미지 업로드 시
         */
        if (newImages != null && !newImages.isEmpty()) {

            property.getImages().clear();

            for (int i = 0; i < newImages.size(); i++) {
                String imageUrl = localFileUploader.upload(newImages.get(i), "properties"); // 파라미터 2개(파일, 폴더명)

                PropertyImage propertyImage = PropertyImage.builder()
                        .imageUrl(imageUrl)
                        .isThumbnail(i == 0)
                        .property(property)
                        .build();

                property.getImages().add(propertyImage);
            }
        }

    }

    /**
     * 유저가 내놓았던 집 확인(마이페이지)
     */
    @Transactional(readOnly = true)
    public List<PropertyListRespDto> getMyProperties(Long userId){
        List<Property> myProperties = propertyRepository.findAllByUserId(userId);

        return myProperties.stream()
                .map(PropertyListRespDto::from)
                .toList();
    }

    /**
     * 지도 영역 내 매물 검색 및 다중 필터링
     */
    @Transactional(readOnly = true)
    public List<PropertyListRespDto> searchMapProperties(PropertyMapSearchReqDto reqDto) {

        /**
         * Bounding Box 생성
         */
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(reqDto.swLng(), reqDto.swLat()), // 남서 (좌측 하단) - 시작점
                new Coordinate(reqDto.swLng(), reqDto.neLat()), // 북서 (좌측 상단)
                new Coordinate(reqDto.neLng(), reqDto.neLat()), // 북동 (우측 상단)
                new Coordinate(reqDto.neLng(), reqDto.swLat()), // 남동 (우측 하단)
                new Coordinate(reqDto.swLng(), reqDto.swLat()) // 남서 (좌측 하단) - 끝점
        };
        Polygon boundingBox = geometryFactory.createPolygon(coords);
        boundingBox.setSRID(4326); // 4236: GPS(WGS84) 표준 좌표계

        /**
         * 키워드가 null이 아닐 때만 앞뒤에 % 붙여주기
         */
        String searchKeyword = reqDto.keyword();
        if (searchKeyword == null || searchKeyword.isBlank()) {
            searchKeyword = null; // 공백이나 빈 문자열이 들어오면 null로 취급!
        } else {
            searchKeyword = "%" + searchKeyword + "%";
        }

        /**
         * 필터링 조건
         */
        List<Property> properties = propertyRepository.findPropertiesByMapAndFilters(
                boundingBox,
                reqDto.tradeType(),
                reqDto.propertyType(),
                reqDto.minDeposit(),
                reqDto.maxDeposit(),
                reqDto.minMonthlyRent(),
                reqDto.maxMonthlyRent(),
                searchKeyword
        );

        return properties.stream()
                .map(PropertyListRespDto::from)
                .toList();
    }
}
