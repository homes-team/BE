package com.homes.backend.domain.review.service;

import com.homes.backend.domain.review.dto.request.ReviewCreateReqDto;
import com.homes.backend.domain.review.dto.response.ReviewListRespDto;
import com.homes.backend.domain.review.entity.Review;
import com.homes.backend.domain.review.exception.ReviewErrorCode;
import com.homes.backend.domain.review.repository.ReviewRepository;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * 특정 대상(중개사 등)에 대한 리뷰 작성
     */
    @Transactional
    public void createReview(Long targetUserId, Long reviewerId, ReviewCreateReqDto request) {
        if (targetUserId.equals(reviewerId)) {
            throw new CustomException(ReviewErrorCode.CANNOT_REVIEW_SELF);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (reviewRepository.existsByReviewerIdAndTargetUserId(reviewerId, targetUserId)) {
            throw new CustomException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        Review review = Review.builder()
                .score(request.score())
                .content(request.content())
                .targetUser(targetUser)
                .reviewer(reviewer)
                .build();

        reviewRepository.save(review);
    }

    /**
     * 특정 대상(중개사 등)이 받은 리뷰 목록 조회 (최신순)
     */
    public List<ReviewListRespDto> getReviews(Long targetUserId) {
        return reviewRepository.findAllByTargetUserIdOrderByCreatedAtDesc(targetUserId).stream()
                .map(ReviewListRespDto::from)
                .toList();
    }
}
