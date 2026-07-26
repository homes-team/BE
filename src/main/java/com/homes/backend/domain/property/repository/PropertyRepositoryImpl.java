package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.homes.backend.domain.property.entity.QProperty.property;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryImpl implements PropertyRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    /**
     * [하이브리드 추천 및 개인화 필터링 메인 로직]
     *
     * 1. 하이브리드 점수 산정:
     *    - AI 추천 매물 점수 (aiScore, 50%): 모델이 분석한 객관적 추천 지표
     *    - 유저 찜하기 횟수 (favoriteCount, 45%): 대중적으로 검증된 인기도 지표
     *    - 최근 본 방 가중치 보너스 (5%): 단순 클릭으로 인한 노이즈를 고려하여 가중치를 낮게 설정하되,
     *      유저가 최근에 조회했던 매물일 경우 점수를 추가로 가산하여 개인화 체감도를 높임.
     *
     * 2. 필수 조건:
     *    - 사용자의 검색 필터 값과 상관없이, 조회되는 매물은 무조건 '거래 가능한 상태(AVAILABLE)'이면서 '허위 매물로 신고되거나 의심되지 않은 안전한 매물'이어야 합니다.
     *
     * 3. 동적 개인화 필터링:
     *    - 사용자가 입력한 검색 조건(최소/최대 가격, 선호 지역 등)에 따라 유연하게 쿼리를 조작합니다.
     *    - 값이 null이거나 빈 값일 경우 해당 조건은 쿼리에서 자동으로 무시(제외)되어 전체 매물 대상 조회로 유연하게 동작합니다.
     */
    @Override
    public List<Property> findHybridRecommendations(Double minPrice, Double maxPrice, String preferredRegion, List<Long> recentViewedIds, int limit) {

        /**
         * 가중치 점수 연산 (AI 점수 50% + 찜하기 50%)
         */
        NumberExpression<Double> recommendationScore = property.aiScore.doubleValue().multiply(0.5)
                .add(property.favoriteCount.doubleValue().multiply(0.45));

        /**
         * 최근 본 방(5%) 가중치 보너스 적용
         * - 최근 본 방 ID 리스트에 포함되어 있다면 0.05(5%)를 더하고, 아니면 0.0을 더함
         */
        if (recentViewedIds != null && !recentViewedIds.isEmpty()) {
            NumberExpression<Double> recentBonus = Expressions.cases()
                    .when(property.id.in(recentViewedIds)).then(0.05)
                    .otherwise(0.0);

            recommendationScore = recommendationScore.add(recentBonus);
        }

        return queryFactory
                .selectFrom(property)
                .where(
                        // 필수 조건: 거래 가능하며 허위 매물이 아닌 것
                        property.status.eq(PropertyStatus.AVAILABLE),
                        property.isSuspicious.eq(false),
                        property.reportCount.eq(0),

                        // 동적 조건: 값이 null이면 무시됨
                        priceBetween(minPrice, maxPrice),
                        regionContains(preferredRegion)
                )
                .orderBy(recommendationScore.desc(), property.createdAt.desc()) // 점수 내림차순, 같으면 최신순
                .limit(limit)
                .fetch();
    }

    /**
     * 동적 쿼리 - 가격 범위
     */
    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice == null && maxPrice == null) return null;
        if (minPrice != null && maxPrice == null) return property.deposit.goe(minPrice);
        if (minPrice == null && maxPrice != null) return property.deposit.loe(maxPrice);
        return property.deposit.between(minPrice, maxPrice);
    }

    /**
     * 동적 쿼리 - 선호 지역 포함 여부
     */
    private BooleanExpression regionContains(String preferredRegion) {
        if (preferredRegion == null || preferredRegion.isBlank()) return null;
        return property.address.contains(preferredRegion);
    }
}
