package com.insurance.model.domain;

import lombok.Getter;

@Getter
public enum StateMarketplace {
    CA("California", "Covered California", "https://www.coveredca.com/"),
    CO("Colorado", "Connect for Health Colorado", "https://connectforhealthco.com/"),
    CT("Connecticut", "Access Health CT", "https://www.accesshealthct.com/"),
    DC("District of Columbia", "DC Health Link", "https://dchealthlink.com/"),
    GA("Georgia", "Georgia Access", "https://www.georgia.gov/health-insurance"),
    ID("Idaho", "Your Health Idaho", "https://www.yourhealthidaho.org/"),
    KY("Kentucky", "Kynect", "https://kynect.ky.gov/"),
    ME("Maine", "CoverMe", "https://www.coverme.gov/"),
    MD("Maryland", "Maryland Health Connection", "https://www.marylandhealthconnection.gov/"),
    MA("Massachusetts", "Health Connector", "https://www.mahealthconnector.org/"),
    MN("Minnesota", "MNsure", "https://www.mnsure.org/"),
    NV("Nevada", "Nevada Health Link", "https://www.nevadahealthlink.com/"),
    NJ("New Jersey", "Get Covered NJ", "https://nj.gov/getcoverednj/"),
    NM("New Mexico", "beWellnm", "https://www.bewellnm.com/"),
    NY("New York", "New York State of Health", "https://nystateofhealth.ny.gov/"),
    PA("Pennsylvania", "Pennie", "https://pennie.com/"),
    RI("Rhode Island", "HealthSource RI", "https://healthsourceri.com/"),
    VT("Vermont", "Vermont Health Connect", "https://portal.healthconnect.vermont.gov/"),
    VA("Virginia", "Virginia's Insurance Marketplace", "https://www.marketplace.virginia.gov/"),
    WA("Washington", "Washington Healthplanfinder", "https://www.wahealthplanfinder.org/");

    private final String stateName;
    private final String marketplaceName;
    private final String websiteUrl;

    StateMarketplace(String stateName, String marketplaceName, String websiteUrl) {
        this.stateName = stateName;
        this.marketplaceName = marketplaceName;
        this.websiteUrl = websiteUrl;
    }

    public static StateMarketplace findByStateCode(String stateCode) {
        try {
            return valueOf(stateCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}