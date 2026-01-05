package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;

public class CheckSum {
    /* 计算TCP报文段校验和：只需校验TCP首部中的seq、ack和sum（sum视为0），以及TCP数据字段 */
    public static short computeChkSum(TCP_PACKET tcpPack) {
        long sum = 0;

        TCP_HEADER header = tcpPack.getTcpH();
        int[] data = tcpPack.getTcpS().getData();

        // seq (32 bits) → two 16-bit words
        int seq = header.getTh_seq();
        sum += (seq >>> 16) & 0xFFFF;
        sum += seq & 0xFFFF;

        // ack (32 bits)
        int ack = header.getTh_ack();
        sum += (ack >>> 16) & 0xFFFF;
        sum += ack & 0xFFFF;

        // data (each int → two 16-bit words)
        if (data != null) {
            for (int val : data) {
                sum += (val >>> 16) & 0xFFFF;
                sum += val & 0xFFFF;
            }
        }

        // carry folding
        while ((sum >> 16) != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }

        // one's complement
        sum = ~sum;

        return (short) (sum & 0xFFFF);
    }
}