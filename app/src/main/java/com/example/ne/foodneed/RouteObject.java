package com.example.ne.foodneed;

import java.util.List;

/**
 * Created by ADMIN on 9/23/2017.
 */
public class RouteObject {
    private List<LegsObject> legs;
    public RouteObject(List<LegsObject> legs) {
        this.legs = legs;
    }
    public List<LegsObject> getLegs() {
        return legs;
    }
}
