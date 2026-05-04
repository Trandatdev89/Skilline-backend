package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.LectureResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.LectureReq;

import java.io.IOException;
import java.util.List;

public interface LectureService {
    void save(LectureReq lectureReq) throws IOException, InterruptedException;
    PageResponse<LectureResponse> getListLecture(int page, int size, String sort, String keyword, Long courseId);

    void delete(List<String> ids);
}
