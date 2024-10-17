package eu.dissco.dataexporter.domain;

import java.util.UUID;

public record JobResult(
    UUID id,
    String downloadLink
) {

}
