package ie.dublinbikes.station.persistence;

/**
 * station 表的一行。站点的静态信息：名字、位置、总车位数。
 *
 * Part 4.1：见练习册 Part 4。表结构在 src/main/resources/db/migration/V1__create_station_tables.sql
 */
// TODO 4.1: 把这个类标记成 JPA 实体，对应 station 表
public class StationEntity {

    // TODO 4.1: number 是主键
    private int number;

    // TODO 4.1: name 列不允许为空
    private String name;

    private String address;

    private double lat;

    private double lng;

    private boolean banking;

    // TODO 4.1: 显式指定列名 bike_stands（其实 Spring Boot 会自动把 camelCase 转成 snake_case，这里练习一下写法）
    private int bikeStands;

    private String status;

    /** JPA 用反射创建对象时需要一个无参构造函数，protected 是为了不让业务代码误用。 */
    protected StationEntity() {
    }

    public StationEntity(int number) {
        this.number = number;
    }

    public void updateDetails(String name, String address, double lat, double lng,
                              boolean banking, int bikeStands, String status) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.banking = banking;
        this.bikeStands = bikeStands;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isBanking() {
        return banking;
    }

    public int getBikeStands() {
        return bikeStands;
    }

    public String getStatus() {
        return status;
    }
}
