package com.avaya.wiki.common;

//   Snowflake ID Format:
//   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
//   -----------------------------------------------------------------
//   |0|                        timestamp (31)                       |
//   -----------------------------------------------------------------
//   0               1               2               3
//
//   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
//   -----------------------------------------------------------------
//   |  timestamp (10)   |  machine_id (10)  |     sequence (12)     |
//   -----------------------------------------------------------------
//   4               5               6               7
//
// machine_id (10) = datacenter_id (5) | worker_id (5)

public class SnowflakeIdGenerator {

    // custom epoch, 00:00 on Dec 1st, 2025, GMT
    private static final long CUSTOM_EPOCH = 1764547200000L;

    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;

    private long lastTimestamp = -1L;
    private long sequence = 0;

    private final long machineId;

    public SnowflakeIdGenerator() {
        this(1L);
    }

    public SnowflakeIdGenerator(long machineId) {
        this.machineId = machineId;
    }

    public synchronized long getNextID() {
        long currentMillis = System.currentTimeMillis();
        long timestamp = currentMillis - CUSTOM_EPOCH;
        if (timestamp == lastTimestamp) {
            sequence++;
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return (timestamp << TIMESTAMP_SHIFT) | (machineId << MACHINE_ID_SHIFT) | sequence;
    }

}
