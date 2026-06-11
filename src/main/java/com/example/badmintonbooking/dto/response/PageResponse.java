package com.example.badmintonbooking.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;       // Danh sách dữ liệu trang hiện tại
    private int pageNumber;        // Trang hiện tại (bắt đầu từ 0)
    private int pageSize;          // Số phần tử mỗi trang
    private long totalElements;    // Tổng số phần tử toàn bộ
    private int totalPages;        // Tổng số trang
    private boolean last;          // Có phải trang cuối không

    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
