package com.medilens.dto.provider;

import java.util.ArrayList;
import java.util.List;

public class CareNavigationResponseDto {
    private List<SpecialtyRecommendationDto> recommendations = new ArrayList<>();
    private List<ProviderDto> nearbyProviders = new ArrayList<>();
    private String statutoryDisclaimer;

    public CareNavigationResponseDto() {}

    public CareNavigationResponseDto(List<SpecialtyRecommendationDto> recommendations,
                                     List<ProviderDto> nearbyProviders,
                                     String statutoryDisclaimer) {
        this.recommendations = recommendations;
        this.nearbyProviders = nearbyProviders;
        this.statutoryDisclaimer = statutoryDisclaimer;
    }

    public List<SpecialtyRecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<SpecialtyRecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public List<ProviderDto> getNearbyProviders() {
        return nearbyProviders;
    }

    public void setNearbyProviders(List<ProviderDto> nearbyProviders) {
        this.nearbyProviders = nearbyProviders;
    }

    public String getStatutoryDisclaimer() {
        return statutoryDisclaimer;
    }

    public void setStatutoryDisclaimer(String statutoryDisclaimer) {
        this.statutoryDisclaimer = statutoryDisclaimer;
    }
}
