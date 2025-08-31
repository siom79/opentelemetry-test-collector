package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.resource.Resource;
import org.springframework.stereotype.Service;

@Service
public class ResourceModelMapper {

    private final CommonModelMapper commonModelMapper;

    public ResourceModelMapper(CommonModelMapper commonModelMapper) {
        this.commonModelMapper = commonModelMapper;
    }

    public Resource mapResource(io.opentelemetry.proto.resource.v1.Resource resource) {
        return Resource.builder()
                .droppedAttributesCount(resource.getDroppedAttributesCount())
                .attributes(this.commonModelMapper.mapKeyValueList(resource.getAttributesList()))
                .build();
    }
}
