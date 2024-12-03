package com.wwz.kitchen.util;

import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * Created by wenzhi.wang.
 * on 2024/12/3.
 */
public class SnowflakeIdUtil {
    // ============================== 一些常量 ==============================

    // 机器ID所占的位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据标识ID所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 序列号所占的位数
    private static final long SEQUENCE_BITS = 12L;

    // 工作机器ID最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 数据中心ID最大值
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    // 序列号最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 每一部分的位移
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    // 上次生成ID的时间戳
    private static long lastTimestamp = -1L;

    // 序列号
    private long sequence = 0L;

    // 工作机器ID (0~31)
    private final long workerId;

    // 数据中心ID (0~31)
    private final long dataCenterId;

    // ============================== 构造方法 ==============================

    /**
     * @param workerId     工作机器ID (0~31)
     * @param dataCenterId 数据中心ID (0~31)
     */
    public SnowflakeIdUtil(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenter ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    // ============================== 生成唯一ID ==============================

    /**
     * 生成唯一ID
     * @return 唯一ID
     */
    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        // 如果当前时间小于上次生成ID的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " +
                    (lastTimestamp - timestamp) + " milliseconds");
        }

        // 如果是同一毫秒内生成的ID，则按照序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 如果序列号溢出，则阻塞直到下一毫秒
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内生成ID，序列号归零
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 构造最终的ID
        return ((timestamp - SnowflakeConstants.EPOCH) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    // ============================== 等待下一毫秒 ==============================

    /**
     * 等待到下一个毫秒
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    // ============================== 获取机器ID ==============================

    /**
     * 获取机器ID
     * @return 当前机器ID
     */
    public static long getWorkerId() {
        long workerId = -1L;
        try {
            // 获取当前机器的 MAC 地址
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(java.net.InetAddress.getLocalHost());
            if (networkInterface != null) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    // 获取机器ID，取前6个字节中的后2个字节作为机器ID
                    workerId = ((mac[mac.length - 2] & 0xFF) << 8) | (mac[mac.length - 1] & 0xFF);
                    workerId = workerId & MAX_WORKER_ID; // 保证workerId不超过最大值
                }
            }
        } catch (SocketException | java.net.UnknownHostException e) {
            e.printStackTrace();
        }
        return workerId;
    }

    // ============================== 常量 ==============================

    /**
     * 雪花算法常量
     */
    public static class SnowflakeConstants {
        // 自定义的纪元（时间戳的起始点，避免和系统时间戳冲突）
        public static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00.000
    }

    public static void main(String[] args) {
        // 获取机器ID
        long workerId = SnowflakeIdUtil.getWorkerId();
        // 假设我们的数据中心ID为 1
        long dataCenterId = 1;

        SnowflakeIdUtil idWorker = new SnowflakeIdUtil(workerId, dataCenterId);

        // 生成 10 个唯一 ID
        for (int i = 0; i < 10; i++) {
            System.out.println("Generated ID: " + idWorker.generateId());
        }
    }
}
