package com.danny.ewf_service.payload.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class PagingResponse<T> {
    private List<T> data;         // The actual data (paginated content)
    private int currentPage;      // The current page number
    private int totalPages;       // Total number of pages available
    private int pageSize;         // Number of items per page
    private long totalElements;   // Total number of elements in the result set

}
