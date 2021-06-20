package nl.marc_apps.weather.station_data

enum class WindDirection(val direction: Int) {
    N(0),
    NNE(22_50),
    NE(45_00),
    ENE(67_50),
    E(90_00),
    ESE(112_50),
    SE(135_00),
    SSE(157_50),
    S(180_00),
    SSW(202_50),
    SW(225_00),
    WSW(247_50),
    W(270_00),
    WNW(292_50),
    NW(315_00),
    NNW(337_50)
}

enum class WindDirectionDutch(val direction: Int) {
    N(0),
    NNO(22_50),
    NO(45_00),
    ONO(67_50),
    O(90_00),
    OZO(112_50),
    ZO(135_00),
    ZZO(157_50),
    Z(180_00),
    ZZW(202_50),
    ZW(225_00),
    WZW(247_50),
    W(270_00),
    WNW(292_50),
    NW(315_00),
    NNW(337_50)
}

val WindDirectionDutch.english: WindDirection
    get() = WindDirection.values().find { it.direction == this.direction }!!