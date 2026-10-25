package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** station 表的一行。站点的静态信息：名字、位置、总车位数。 */
@Entity
@Table(name = "station")
public class StationEntity {

    @Id
    private int number;

    @Column(nullable = false)
    private String name;

    private String address;

    private double lat;

    private double lng;

    private boolean banking;

    @Column(name = "bike_stands")
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
