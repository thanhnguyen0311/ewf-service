package com.danny.ewf_service.entity;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ImageUrls {
    private List<String> dim;
    private List<String> img;
}
