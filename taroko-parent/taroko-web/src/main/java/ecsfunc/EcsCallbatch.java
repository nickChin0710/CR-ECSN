/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
* 109-12-24   V1.00.02  Justin       parameterize sql
* 110-01-08  V1.00.03  tanwei        修改意義不明確變量                    
* 111-01-18  V1.00.04  Justin     fix Throw Inside Finally                  *    
* 111-02-14  V1.00.05  Justin     big5 -> MS950                             * 
******************************************************************************/
package ecsfunc;
/** online call batch
 * 2019-1216   JH    call shell
 * 2019-1205   JH    callBatch.auth
 * 2019-0829   JH    modify
   2019-0509:  JH    seq_callbatch.nextval
   2019-0426:  JH    modify errmsg
   18-1128:    JH    pgm.format[Xxx.XxxX999]
   18-1127:    JH    no-call socket
 * 2018-0910:	JH		comm_APPC_2
 * 2018-0823:	JH		debug
 *
 * */

import java.sql.SQLException;
import java.io.*;
import java.net.*;
import taroko.base.BaseSQL;

public class EcsCallbatch extends BaseSQL {

  private String host = "";
  private int port = 0;
  private String batchSeq = "";
  private String sendData = "";
  public String strRespData = "";
  public boolean debug = false;
  int timeOut = 6000; // 毫秒
  // private boolean bshell=false;

  taroko.com.TarokoCommon wp = null;

  public EcsCallbatch() {}

  public EcsCallbatch(taroko.com.TarokoCommon wr) {
    wp = wr;
  }

  public void setConn(taroko.com.TarokoCommon wr) {
    wp = wr;
  }

  public void timeOut(int sec) {
    timeOut = sec * 1000;
  }

  public String batchSeqno() {
    return batchSeq; // String.format("%020d", Long.valueOf(_batch_seq));
  }

  public int callBatch(String sendData, String user) throws Exception {
    // -檢核權限-
    if (empty(user)) {
      errmsg("使用者代碼: 不可空白");
      return rc;
    }

    String lsPgm = sendData.trim() + " ";
    lsPgm = commString.left(lsPgm, lsPgm.indexOf(" "));
    if (lsPgm.indexOf(".") > 0) {
      lsPgm = commString.mid(lsPgm, lsPgm.indexOf(".") + 1);
    }

    if (getAuthRun(lsPgm.toLowerCase(), user) == false) {
      errmsg("使用者無執行批次程式(%s)權限", lsPgm);
      return rc;
    }

    return onlineCallBatch(sendData);
  }

  public int callBatch(String sendData) throws Exception {
    // -不檢核權限-
    // String ls_mod_user=wp.loginUser;
    // return call_Batch(a_send_data,ls_mod_user);
    return onlineCallBatch(sendData);
  }

  private int onlineCallBatch(String aSendData) throws Exception {
    strRespData = "";
    msgOK();
    if (empty(aSendData)) {
      errmsg("傳送資料不可空白");
      return rc;
    }

    sendData = aSendData;

    setIpPort("CALLBATCH");
    if (rc != 1)
      return rc;

    insertPtrCallbatch();
    if (rc != 1)
      return rc;

    onlineCallBatch();
    if (rc != 1) {
      updatePtrCallbatch();
    } else {
      setMesg("啟動批次程式成功, 處理序號: " + this.batchSeqno());
      rc = 1;
    }
    return rc;
  }

  public boolean getAuthRun(String pgm, String user) {
    if (empty(pgm) || empty(user))
      return false;

    // -權限-
    String lsSql =
        "select sum(decode(A.aut_query,'Y',1,0)) as xx_cnt " + " from sec_authority A, sec_user B"
            + " where A.user_level = B.usr_level "
            + " and lcase(A.wf_winid) = ? "
            + " and B.usr_id = ? "
            + " and  LOCATE(A.group_id,ucase(B.usr_group)) >0" ;

    try {
      this.sqlSelect(wp.getConn(), lsSql, new Object[] {pgm.toLowerCase(), user});
      if (sqlRowNum > 0 && sqlInt("xx_cnt") > 0)
        return true;
    } catch (Exception ex) {
      errmsg("error: " + ex.getMessage());
      return false;
    }

    return false;
  }

  private void onlineCallBatch() throws Exception {
    Socket socket = null;
    DataInputStream input = null;
    DataOutputStream output = null;

    // -add package-Name-
    sendData = sendData.trim();
    String lsPgm = commString.left(sendData, sendData.indexOf(" "));
    if (lsPgm.indexOf("_") <= 0) {
      if (lsPgm.indexOf(".") <= 0)
        sendData = commString.left(lsPgm, 3) + "." + sendData;
    }
    // else {
    // //0000 10.6.9.185 crdp0040 /usr/bin/ksh /ECS/ecs/shell/sh_d_crd001 00000000000907316348
    // _send_data ="/usr/bin/ksh "+_send_data;
    // }

    try {
      socket = new Socket(host, port);
      socket.setSoTimeout(timeOut);

      output = new DataOutputStream(socket.getOutputStream());
      output.write(sendData.getBytes());
      output.flush();

      input = new DataInputStream(socket.getInputStream());
      int inputLen = 0;
      byte[] inData = new byte[2048];

      inputLen = input.read(inData, 0, inData.length);
      if (inputLen > 0) {
        strRespData = new String(inData, 0, inputLen);
      }
    } catch (IOException e) {
      errmsg("Socket IO error; " + e.getMessage());
      // e.printStackTrace();
    } finally {
      if (input != null)
        try {
          input.close();
        } catch (Exception e) {
        }
      if (output != null)
        try {
          output.close();
        } catch (Exception e) {
        }
      if (socket != null)
        try {
          socket.close();
        } catch (Exception e) {
        }
    }
  }

  void updatePtrCallbatch() throws SQLException {
    String lsProcDesc = "IP=" + host + ", Port=" + port;
    String sql1 =
        "update ptr_callbatch set" + " error_code ='9999'," + " error_desc =substr(?,1,100)"
            + ", execute_date_s =" + commSqlStr.sysYYmd + ", execute_time_s =" + commSqlStr.sysTime
            + ", execute_date_e =" + commSqlStr.sysYYmd + ", execute_time_e =" + commSqlStr.sysTime
            + ", proc_desc =?" + ", process_flag ='Y'" + " where batch_seqno =?";
    // Savepoint save1 = wp.getConn().setSavepoint();
    this.sqlExec(wp.getConn(), sql1, new Object[] {this.getMesg(), lsProcDesc, batchSeqno()});
    if (sqlRowNum > 0) {
      wp.commitOnly();
    } else
      wp.getConn().rollback();
  }

  void insertPtrCallbatch() throws SQLException {
    String[] sendDataName = new String[] {sendData, " "};
    String lsPgm = commString.token(sendDataName);
    String lsParm = sendDataName[0];
    // --
    String lsPkg = "";
    boolean lbShell = (lsPgm.indexOf("_") > 0);
    if (!lbShell) {
      int pgmNum = lsPgm.indexOf(".");
      if (pgmNum > 0) {
        lsPgm = commString.mid(lsPgm, pgmNum + 1);
        lsPkg = commString.mid(lsPgm, 0, pgmNum);
      } else if (lsPgm.length() == 7) {
        lsPgm =
            lsPgm.substring(0, 1).toUpperCase() + commString.mid(lsPgm, 1, 2)
                + commString.mid(lsPgm, 3, 1).toUpperCase() + commString.mid(lsPgm, 4);
        lsPkg = commString.left(lsPgm, 3);
      }
    }
    // --
    if (empty(lsPkg)) {
      sendData = lsPgm + " " + lsParm + " " + batchSeqno();
    } else {
      sendData = lsPkg + "." + lsPgm + " " + lsParm + " " + batchSeqno();
    }

    String lsSend = "";
    if (lbShell)
      lsSend = "/usr/bin/ksh " + sendData;
    else
      lsSend = sendData;

    String sql1 =
        "insert into ptr_callbatch ( " + " batch_seqno,  " + "program_code, " + "start_date, "
            + "start_time, " + "user_id, " + "client_program, " + "parameter_data " + ")values(  "
            + " ?," + " ?," + commSqlStr.sysYYmd + "," + commSqlStr.sysTime + "," + " ?, " // :user_id,"
            // +" :workstation_name,"
            + " ?, " // :client_program, "
            + " ? " // :param_data"
            + " ) ";
    sqlExec(wp.getConn(), sql1, new Object[] {batchSeqno(), lsPgm, wp.loginUser, wp.modPgm(),
        lsSend});
    if (sqlRowNum <= 0) {
      errmsg("insert ptr_callbatch error");
      wp.rollbackOnly();
    } else
      wp.commitOnly();

  }

  void setIpPort(String akey) {
    String sql1 =
        "select wf_value as serv_ip,wf_value2 as serv_port"
            + ", wf_value3 as serv_ip2, wf_value4 as serv_port2"
            + ", lpad(to_char(sysdate,'yy')||lpad(seq_callbatch.nextval,8,'0'),20,'0') as batch_seq"
            + " from PTR_sys_parm " + " where wf_parm='SYSPARM' and wf_key =?";
    Object[] obj = new Object[] {akey};
    try {
      this.sqlSelect(wp.getConn(), sql1, obj);
    } catch (Exception e) {
      wp.log("set_Ip_port.err, " + e.getMessage());
    }
    if (sqlRowNum <= 0) {
      errmsg("無法取得 CallBatch: IP, Port-No");
      return;
    }
    host = sqlStr("serv_ip");
    port = sqlInt("serv_port");
    batchSeq = sqlStr("batch_seq");
    if (wp.localHost()) {
      host = sqlStr("serv_ip2");
      port = sqlInt("serv_port2");
    }
  }

  void selectEcsRefIpAddr(String ipCode) {
    String sql1 = "select ref_ip, port_no" + " from ecs_ref_ip_addr" + " where ref_ip_code =?";
    try {
      this.sqlSelect(wp.getConn(), sql1, new Object[] {ipCode});
      if (sqlRowNum <= 0) {
        errmsg("無法取得 [%s]: IP, Port-No", ipCode);
      }
      host = sqlStr("ref_ip");
      port = sqlInt("port_no");
    } catch (Exception ex) {
      errmsg("無法取得 [%s]: IP, Port-No", ipCode);
    }
  }

  String[] appcLocalHost() {
    String[] aaData = new String[200];
    try {
      taroko.com.TarokoFileAccess ooFile = new taroko.com.TarokoFileAccess(wp);
      int liFileNum = ooFile.openInputText("appc_recvdata", "");
      for (int ii = 0; ii < 200; ii++) {
        String ss = ooFile.readTextFile(liFileNum);
        if (empty(ss))
          break;
        aaData[ii] = ss;
      }
      ooFile.closeInputText(liFileNum);
      return aaData;
    } catch (Exception ex) {
    }
    return aaData;
  }

  public String commAppc(String sendbuf, int aiLen) throws IOException {
    if (wp.localHost()) {
      String[] ss = appcLocalHost();
      return ss[0];
    }

    // =======================================
    int recvbufLen = 4096;
    String recvbuf = "";
    selectEcsRefIpAddr("APPC");
    String server = host;
    int iport = port;
    if (aiLen > 0)
      recvbufLen = aiLen;

    try (Socket socket = new Socket(server, iport);){
      
      socket.setSoTimeout(timeOut);

      DataInputStream input = null;
      DataOutputStream output = null;

      wp.log(">>>APPC: Starting...====================================================");
      wp.log(">>>APPC.send[%s]", sendbuf);
      try {
        while (true) {
          output = new DataOutputStream(socket.getOutputStream());
          output.write(sendbuf.getBytes());
          output.flush();

          input = new DataInputStream(socket.getInputStream());
          int inputLen = 0;
          byte[] inData = new byte[recvbufLen];

          wp.log("Read data1 : ---------------");
          inputLen = input.read(inData, 0, inData.length);
          wp.log("Read data2 : [" + new String(inData, 0, inputLen) + "], len=" + inputLen);
          if (inputLen > 0)
            recvbuf = new String(inData, 0, inputLen, "MS950");
          break;
        }
      } catch (Exception e) {
        wp.log("Socket time out," + e.getMessage());
        errmsg("資訊處理未回應");
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
        wp.log(">>>APPC: Terminated...===================================================");
      }
    } catch (IOException e) {
      wp.log(">>>Exception : " + e.getMessage());
      e.printStackTrace();
    } 

    wp.log(">>>recvbuf : [" + recvbuf + "]");
    return recvbuf;
  }

  public String[] commAppc2(String sendbuf, int aiLen) throws IOException {
    if (wp.localHost()) {
      return appcLocalHost();
    }

    String recvbuf = "";

    selectEcsRefIpAddr("APPC");
    String server = host;
    int iport = port;
    int liRecvRow = 0;
    int recvbufLen = 4096;
    if (aiLen > 0)
      recvbufLen = aiLen;

    String[] laRecvbuff = new String[200];

    try (Socket socket = new Socket(server, iport);){
      
      socket.setSoTimeout(timeOut);

      DataInputStream input = null;
      DataOutputStream output = null;

      wp.log(">>>APPC-2: Starting...====================================================");
      wp.log(">>>APPC.send[%s]", sendbuf);
      try {
        output = new DataOutputStream(socket.getOutputStream());
        output.write(sendbuf.getBytes());
        output.flush();
        // -MAX.200-
        wp.log(">>>APPC.read ----------------------");
        for (int ll = 0; ll < 200; ll++) {
          input = new DataInputStream(socket.getInputStream());
          int inputLen = 0;
          byte[] inData = new byte[recvbufLen];

          inputLen = input.read(inData, 0, inData.length);
          wp.log("Read data2: %s.[%s], len=%s", ll, new String(inData, 0, inputLen), inputLen);
          if (inputLen > 0) {
            recvbuf = new String(inData, 0, inputLen, "MS950");
            liRecvRow++;
            // wp.col_set(li_recv_row,"appc_data",recvbuf);
            laRecvbuff[ll] = recvbuf;
            if (recvbuf.equalsIgnoreCase("ok but 0 bytes recvd")) {
              break;
            }
            continue;
          }
          break;
        }
      } catch (Exception e) {
        wp.log("Socket time out," + e.getMessage());
        errmsg("資訊處理未回應");
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
        wp.log(">>>APPC-2: Terminated...===================================================");
      }
    } catch (IOException e) {
      wp.log(">>>Exception : " + e.getMessage());
      e.printStackTrace();
    } 
    
    return laRecvbuff;
  }


}
