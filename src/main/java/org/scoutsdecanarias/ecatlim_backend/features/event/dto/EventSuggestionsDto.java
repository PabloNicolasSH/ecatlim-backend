package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import java.util.List;

public record EventSuggestionsDto(
        List<String> locations,
        List<String> transferBankNumbers,
        List<String> transferCodes
) {
}
