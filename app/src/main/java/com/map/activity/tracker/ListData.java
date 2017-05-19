package com.map.activity.tracker;

/* class to create list view item values*/
class ListData {
    private String routeName;
    private String startTime;
    private String endTime;

    ListData(String routeName, String startTime, String endTime) {
        this.routeName = routeName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    String getRouteName() {
        return routeName;
    }

    String getStartTime() {
        return startTime;
    }

    String getEndTime() {
        return endTime;
    }

}
