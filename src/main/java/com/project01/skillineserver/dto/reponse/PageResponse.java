package com.project01.skillineserver.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse <T>{
    private int page;
    private int size;
    private List<T> list;
    private int totalPages;
    private long totalElements;
}
