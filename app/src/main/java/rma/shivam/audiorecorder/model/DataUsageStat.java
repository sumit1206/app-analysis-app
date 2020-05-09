package rma.shivam.audiorecorder.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class DataUsageStat {
    private long timeInMilis;
    private double throughput;

    public DataUsageStat(long timeInMilis, double throughput) {
        this.timeInMilis = timeInMilis;
        this.throughput = throughput;
    }

    private long getTimeInMilis() {
        return timeInMilis;
    }

    private double getThroughput() {
        return throughput;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timeInMilis)
                .append(" : ")
                .append(throughput);
        return stringBuilder.toString();
    }

    public static long getFirstBitRecTime(ArrayList<DataUsageStat> dataUsageStats){
        long firstBitRecTime = 0;
        if(dataUsageStats.isEmpty()){
            return firstBitRecTime;
        }
        ArrayList<DataUsageStat> tempDataUsageStats = new ArrayList<>();
        for(int i = dataUsageStats.size()-1; i >= 0; i--){
            tempDataUsageStats.add(dataUsageStats.get(i));
        }
        dataUsageStats = tempDataUsageStats;
        for(DataUsageStat dataUsageStat : dataUsageStats){
            int position = dataUsageStats.indexOf(dataUsageStat);
            if(position == dataUsageStats.size() - 1){
                firstBitRecTime = dataUsageStat.getTimeInMilis();
                break;
            }
            DataUsageStat prevUsageStat = dataUsageStats.get(position+1);
            if(prevUsageStat.getThroughput() > dataUsageStat.getThroughput() || prevUsageStat.getThroughput() == 0){
                firstBitRecTime = dataUsageStat.getTimeInMilis();
                break;
            }
        }
        return firstBitRecTime;
    }
}
