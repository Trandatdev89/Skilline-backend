//package com.project01.skillineserver.mapper;
//
//import com.project01.skillineserver.dto.reponse.ResultExamResponse;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class QuizAttemptMapper {
//
//    public ResultExamResponse toResultExamResponse(Long quizId,Double totalScore, List<AnswerRes> answerRes){
//        return ResultExamResponse.builder()
//                .quizId(quizId)
//                .answerRes(answerRes)
//                .totalScore(totalScore)
//                .build();
//    }
//}
