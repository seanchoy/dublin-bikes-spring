package ie.dublinbikes.station.domain;

/** Part 1.2：见练习册 Part 1 */
public final class GeoUtils {

    static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoUtils() {
    }

    public static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        // TODO 1.2: 把 Python 版的 haversine 改写成 Java（公式在题目文档里）
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lng2 - lng1);

        double a = Math.pow(Math.sin(dPhi/2),2) + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(dLambda/2), 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    }
}
