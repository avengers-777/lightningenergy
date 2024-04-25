package com.alameda.lightningenergy.utils;

import com.google.protobuf.ByteString;

public class BlockUtils {
    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;
    public static final long BLOCK_TIME_MILLIS = 3000;
    public static final long UNLOCK_BLOCK_COUNT = 403200L;
    public static long getBlockHeightByTimestamp(long blockHeightRecord, long updateTimeRecord,long timestamp) {
        long timeDifferenceInSeconds = (timestamp - updateTimeRecord) / 1000;
        long currentBlockHeight = blockHeightRecord + (timeDifferenceInSeconds / 3);
        return currentBlockHeight;
    }
    public static double getDoubleBlockHeightByTimestamp(long blockHeightRecord, long updateTimeRecord, long timestamp) {
        double timeDifferenceInSeconds = (double) (timestamp - updateTimeRecord) / 1000;
        double currentBlockHeight = blockHeightRecord + (timeDifferenceInSeconds / 3);
        return currentBlockHeight;
    }


    public static double calculateCurrentBlockHeightDifference(long blockHeight, long updateTime, long scanBlockHeight) {
        return getDoubleBlockHeightByTimestamp(blockHeight, updateTime,System.currentTimeMillis()) - scanBlockHeight;
    }



    public static String ByteStringToString(ByteString byteString) {

        // 将ByteString转换为字节数组
        byte[] bytes = byteString.toByteArray();
        // 使用正确的字符编码将字节数组转换为字符串
        // 这里假设使用UTF-8编码，这是非常常见的
        String string = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);

       return string;
    }
    public static long calculateResourceCurrentConsumption(long initialConsumption, long consumptionStartTime) {

        long currentTime = System.currentTimeMillis();

        long millisPassed = currentTime - consumptionStartTime;

        long recovery = (millisPassed * initialConsumption) / ONE_DAY_IN_MILLIS;

        long currentConsumption = initialConsumption - recovery;

        return Math.max(currentConsumption, 0);
    }
    public static void main(String[] args) {
        // 测试函数
        long initialConsumption = 2400;
        long consumptionStartTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000); // 假设消耗开始于13小时前
        long currentConsumption = calculateResourceCurrentConsumption(initialConsumption, consumptionStartTime);

        System.out.println("当前消耗量: " + currentConsumption);
    }


}
