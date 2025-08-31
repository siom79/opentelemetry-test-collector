package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnyValue {

    public enum Type {
        STRING,
        BOOLEAN,
        INT,
        DOUBLE,
        BYTE,
        KVLIST
    }

    private Type type;
    private String stringValue;
    private Boolean booleanValue;
    private Long integerValue;
    private Double doubleValue;
    private byte[] byteValue;
    private List<KeyValue> kvList;
}
