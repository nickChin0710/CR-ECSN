/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V1.00.00    Zuwei     coding standard, rename field method & format                   *
*                                                                            *
*****************************************************************************/
package com;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQControl {
  private int maxQueue = 20;
  private MQQueueManager qMgr = null;
  private MQQueue[] queue = new MQQueue[maxQueue];

  private MQGetMessageOptions getOptions = null;
  private int qi = 0;

  public String mqHost = "";
  public String mqPort = "";

  public String qMgrName = "R61P";
  public String sendChannel = "ECS.CHANNEL";
  public String recvChannel = "ECS.CHANNEL";
  public String apQName = "";
  public String userId = "mqm";
  public String passwrod = "mqm";

  public String mqType = "O";
  public boolean[] queueEnd = new boolean[maxQueue];

  MQPutMessageOptions pmo = new MQPutMessageOptions();
  MQMessage outMesg = new MQMessage();

  public MQControl() {
    super();
    for (int i = 0; i < maxQueue; i++) {
      queueEnd[i] = false;
    }
  }

  public int connectMQ() throws Exception {

    MQEnvironment.hostname = mqHost;
    MQEnvironment.port = Integer.parseInt(mqPort);
    if (mqType.equals("O")) {
      MQEnvironment.channel = sendChannel;
    } else {
      MQEnvironment.channel = recvChannel;
    }
    MQEnvironment.userID = userId;
    MQEnvironment.password = passwrod;

    qMgr = new MQQueueManager(qMgrName);

    int openOptions = 0;
    if (mqType.equals("O")) {
      openOptions = MQC.MQOO_OUTPUT + MQC.MQOO_FAIL_IF_QUIESCING;
      outMesg.format = MQC.MQFMT_STRING;
      outMesg.feedback = MQC.MQFB_NONE;
      outMesg.messageType = MQC.MQMT_DATAGRAM;
    } else {
      openOptions = MQC.MQOO_INQUIRE + MQC.MQOO_FAIL_IF_QUIESCING + MQC.MQOO_INPUT_SHARED;
      getOptions = new MQGetMessageOptions();
      // getOptions.options = MQC.MQGMO_NO_WAIT + MQC.MQGMO_FAIL_IF_QUIESCING + MQC.MQGMO_CONVERT;
      getOptions.options = MQC.MQGMO_WAIT;
      getOptions.waitInterval = 30000;
    }

    queue[qi] = qMgr.accessQueue(apQName, openOptions, null, // default q manager
        null, // no dynamic q name
        null); // no alternate user id

    // System.out.println("Connect MQ "+apQName+" sucess !!");
    if (mqType.equals("I")) {
      int depth = queue[qi].getCurrentDepth();
      if (depth == 0) {
        queueEnd[qi] = true;
      }
    }

    int k = qi;
    qi++;
    return k;
  }

  public String readMQ(int i) throws Exception {

    String queueData = "";
    int depth = queue[i].getCurrentDepth();
    if (depth == 0) {
      queueEnd[i] = true;
      return "";
    }

    MQMessage message = new MQMessage();
    queue[i].get(message, getOptions);
    queueData = message.readLine();
    // message.clearMessage();
    return queueData;
  }

  public int getMessageCount(int i) throws Exception {
    int depth = queue[i].getCurrentDepth();
    return depth;
  }

  public byte[] readMQbyte(int i) throws Exception {
    int depth = queue[i].getCurrentDepth();
    if (depth == 0) {
      queueEnd[i] = true;
      return null;
    }

    MQMessage message = new MQMessage();
    queue[i].get(message, getOptions);

    int dataLen = message.getTotalMessageLength();
    byte[] outData = new byte[dataLen];
    message.readFully(outData, 0, dataLen);
    message = null;
    return outData;
  }

  // write MQ
  public void writeMQ(int i, String queueData) throws Exception {
    outMesg.clearMessage();
    outMesg.messageId = MQC.MQMI_NONE;
    outMesg.correlationId = MQC.MQCI_NONE;
    outMesg.writeString(queueData);
    queue[i].put(outMesg, pmo);

    return;
  }

  // write MQ
  public void writeMQbyte(int i, byte[] queueData, int writeLen) throws Exception {
    outMesg.clearMessage();
    outMesg.messageId = MQC.MQMI_NONE;
    outMesg.correlationId = MQC.MQCI_NONE;
    outMesg.write(queueData, 0, writeLen);
    queue[i].put(outMesg, pmo);

    return;
  }


  public void closeMq(int i) throws Exception {
    queue[i].close();
    return;
  }

  public void disConnectMQ() throws Exception {
    qMgr.disconnect();
    return;
  }

} // end of class MQControl
