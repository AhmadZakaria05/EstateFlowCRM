package com.EstateFlowCRM.commons.ErrorResponse;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
}
