package kr.ync.tripplan.traveltest;

public enum TravelType {
    FREE_EXPLORER("자유로운 탐험가", "즉흥적인 여행과 낯선 골목 탐방을 즐기는 유형"),
    RELAXED_HEALER("여유로운 힐링러", "조용한 휴식과 재충전을 중요시하는 유형"),
    CULTURE_LOVER("감성 문화러버", "박물관, 역사, 예술을 즐기는 유형"),
    FOOD_EXPLORER("미식 탐구가", "현지 맛집과 시장 탐방을 즐기는 유형");

    private final String displayName;
    private final String description;

    TravelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}