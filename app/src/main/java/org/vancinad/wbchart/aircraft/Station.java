package org.vancinad.wbchart.aircraft;

public class Station {
    String name;
    double arm; //distance from datum
    double maxWeight; // maximum weight at this station, if any

    Station(double arm, String name) {
        this.name = name;
        this.arm = arm;
        this.maxWeight = Double.MAX_VALUE;
    }

    Station(double arm, String name, double maxWeight) {
        this.name = name;
        this.arm = arm;
        this.maxWeight = maxWeight;
    }


    public String getName() {
        return name;
    }

    public double getArm() {
        return arm;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

}
