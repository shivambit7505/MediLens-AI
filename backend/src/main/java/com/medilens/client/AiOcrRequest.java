package com.medilens.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiOcrRequest(
    @JsonProperty("storage_path") String storagePath,
    @JsonProperty("page_number") int pageNumber
) {}
