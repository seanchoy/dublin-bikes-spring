package ie.dublinbikes.station;

public class StationNotFoundException extends RuntimeException {

    private final int number;

    public StationNotFoundException(int number) {
        super("Station " + number + " does not exist");
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
