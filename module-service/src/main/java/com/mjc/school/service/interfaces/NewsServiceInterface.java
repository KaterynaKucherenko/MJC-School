package com.mjc.school.service.interfaces;

import com.mjc.school.service.dto.NewsDtoRequest;
import com.mjc.school.service.dto.NewsDtoResponse;

import java.util.List;

public interface NewsServiceInterface extends BaseService<NewsDtoRequest, NewsDtoResponse, Long> {
    List<NewsDtoResponse> readListOfNewsByParams(List<String> tagName, List<Long> tagId, String authorName, String title, String content);
}
