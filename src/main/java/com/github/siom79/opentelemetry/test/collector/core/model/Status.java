package com.github.siom79.opentelemetry.test.collector.core.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Status {

    public enum StatusCode {
        STATUS_CODE_UNSET,
        STATUS_CODE_OK,
        STATUS_CODE_ERROR
    }

    private String message;
    private StatusCode code;
}
