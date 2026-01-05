/***************************2.1: ACK/NACK**************************** 
 * Feng Hong; 2015-12-09
 */
package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.message.*;

public class TCP_Sender extends TCP_Sender_ADT {
    private TCP_PACKET tcpPack;
    private volatile int flag = 0;

    public TCP_Sender() {
        super();
        super.initTCP_Sender(this);
    }

    @Override
    public void rdt_send(int dataIndex, int[] appData) {
        tcpH.setTh_seq(dataIndex * appData.length + 1);
        tcpS.setData(appData);
        tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);
        tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
        tcpPack.setTcpH(tcpH);

        udt_send(tcpPack);
        flag = 0;

        // 等待正确 ACK
        while (flag == 0) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void udt_send(TCP_PACKET stcpPack) {
        tcpH.setTh_eflag((byte) 1); // 数据包允许出错（eFlag=1）
        client.send(stcpPack);
    }

    @Override
    public void waitACK() {
        // 不再在此处理 ACK，逻辑已移至 recv()
    }

    @Override
    public void recv(TCP_PACKET recvPack) {
        int ack = recvPack.getTcpH().getTh_ack();
        System.out.println("Receive ACK Number: " + ack);

        if (tcpPack != null) {
            if (ack == tcpPack.getTcpH().getTh_seq()) {
                System.out.println("Clear: " + ack);
                flag = 1;
            } else if (ack == -1) {
                System.out.println("NAK received, retransmitting: " + tcpPack.getTcpH().getTh_seq());
                udt_send(tcpPack);
            } else {
                // 收到损坏的 ACK（如 -218885020），保守重传
                System.out.println("Corrupted ACK: " + ack + ", retransmitting...");
                udt_send(tcpPack);
            }
        }
        System.out.println();
    }
}