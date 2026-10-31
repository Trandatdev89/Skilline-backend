// Test case cho springboot-xss.yaml - chay bang: semgrep --test semgrep-rules/
package test;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import jakarta.validation.Valid;

class ReviewServiceTest {

    // KB1 - lab goc: builder lay truc tiep comment tu request roi save
    public void createReview(ReviewReq reviewReq) {
        // ruleid: springboot-stored-xss-unsanitized-input
        reviewRepository.save(ReviewEntity.builder()
                .courseId(reviewReq.courseId())
                .comment(reviewReq.comment())
                .rating(reviewReq.rating())
                .build());
    }

    // KB2 - da va bang Jsoup.clean
    public void createReviewSafe(ReviewReq reviewReq) {
        // ok: springboot-stored-xss-unsanitized-input
        reviewRepository.save(ReviewEntity.builder()
                .courseId(reviewReq.courseId())
                .comment(Jsoup.clean(reviewReq.comment(), Safelist.none()))
                .build());
    }

    // KB3 - khuon mau setter + save o mot service khac
    public void updateCourse(CourseReq courseReq) {
        CourseEntity course = courseRepository.findById(courseReq.id()).orElseThrow();
        course.setDescription(courseReq.description());
        // ruleid: springboot-stored-xss-unsanitized-input
        courseRepository.save(course);
    }

    public void updateCourseSafe(CourseReq courseReq) {
        CourseEntity course = courseRepository.findById(courseReq.id()).orElseThrow();
        course.setDescription(XssSanitizer.cleanText(courseReq.description()));
        course.setTitle(HtmlUtils.htmlEscape(courseReq.title()));
        // ok: springboot-stored-xss-unsanitized-input
        courseRepository.save(course);
    }

    // Truong khong phai van ban (so luong, id) -> khong canh bao
    public void updatePrice(CourseReq courseReq) {
        CourseEntity course = new CourseEntity();
        course.setPrice(courseReq.price());
        // ok: springboot-stored-xss-unsanitized-input
        courseRepository.save(course);
    }

    // Du lieu doc lai tu CSDL -> khong phai source
    public void login(LoginRequest loginRequest) {
        UserEntity user = (UserEntity) userDetailsService.loadUserByUsername(loginRequest.getUsername());
        user.setLastLogin(now());
        // ok: springboot-stored-xss-unsanitized-input
        userRepository.save(user);
    }
}

class ReviewControllerTest {
    // ruleid: springboot-requestbody-missing-valid
    public ApiResponse<?> create(@RequestBody ReviewReq reviewReq) {
        return service.create(reviewReq);
    }

    // ok: springboot-requestbody-missing-valid
    public ApiResponse<?> createValid(@Valid @RequestBody ReviewReq reviewReq) {
        return service.create(reviewReq);
    }
}
