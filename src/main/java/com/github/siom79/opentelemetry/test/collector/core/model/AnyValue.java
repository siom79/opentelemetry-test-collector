package com.github.siom79.opentelemetry.test.collector.core.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private boolean booleanValue;
    private Long integerValue;
    private Double doubleValue;
    private byte[] byteValue;
    private List<KeyValue> kvList;
}
