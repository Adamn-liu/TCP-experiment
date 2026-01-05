/***************************2.1: ACK/NACK*****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.*;
import com.ouc.tcp.tool.TCP_TOOL;

public class TCP_Receiver extends TCP_Receiver_ADT {
    private TCP_PACKET ackPack;
    int sequence = 1;

    public TCP_Receiver() {
        super();
        super.initTCP_Receiver(this);
    }

    @Override
    public void rdt_recv(TCP_PACKET recvPack) {
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
            // 校验成功 → ACK
            tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
            ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
            tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
            reply(ackPack);
            dataQueue.add(recvPack.getTcpS().getData());
            sequence++;
        } else {
            // 校验失败 → NAK
            System.out.println("Recieve Computed: " + CheckSum.computeChkSum(recvPack));
            System.out.println("Received Packet Sum: " + recvPack.getTcpH().getTh_sum());
            System.out.println("Problem: Packet Number: " + recvPack.getTcpH().getTh_seq() + " + InnerSeq: " + sequence);
            tcpH.setTh_ack(-1);
            ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
            tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
            reply(ackPack);
        }
        System.out.println();

        if (dataQueue.size() == 20)
            deliver_data();
    }

    @Override
    public void deliver_data() {
        File fw = new File("recvData.txt");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(fw, true));
            while (!dataQueue.isEmpty()) {
                int[] data = dataQueue.poll();
                for (int i = 0; i < data.length; i++) {
                    writer.write(data[i] + "\n");
                }
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reply(TCP_PACKET replyPack) {
        // ✅ 关键修复：ACK/NAK 不能出错！
        tcpH.setTh_eflag((byte) 0); // eFlag=0 → ACK信道可靠
        client.send(replyPack);
    }
}