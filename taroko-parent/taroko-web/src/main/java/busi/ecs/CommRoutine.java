/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 107/05/14  V1.00.03  Allen Ho   init                                       *
* 109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 111-01-18  V1.00.06  Justin     fix Throw Inside Finally                  *
******************************************************************************/
package busi.ecs;

import taroko.com.TarokoCommon;
import java.io.*;
import java.net.*;

public class CommRoutine extends busi.FuncBase {
  // ************************************************************************
  public String getSeqno(String seqName) {
    String sql1 = "select NEXTVAL FOR " + seqName + " AS MOD_SEQNO " + " from SYSIBM.SYSDUMMY1";

    wp.logSql = false;
    this.sqlSelect(sql1);

    if (rc == -1)
      return "0";

    String output = String.format("%010.0f", this.colNum("MOD_SEQNO"));
    while (output.length() < 10)
      output = "0" + output;

    return output;
  }

  // ************************************************************************
  public String getBusinDate() {
    dateTime();
    String sql1 =
        "select business_date as business_date " + " from ptr_businday "
            + " FETCH FIRST 1 ROWs ONLY";

    wp.logSql = false;
    this.sqlSelect(sql1);

    if (rc == -1)
      return wp.sysDate;

    return this.colStr("business_date");
  }

  // ************************************************************************
  public String getObjectOwner(String objType, String objCode) {
    String sql1 =
        "select unit_code as unit_code " + " from ecs_object_owner " + " WHERE obj_code = '"
            + objCode + "' " + " AND   obj_type = '" + objType + "' ";

    wp.logSql = false;
    this.sqlSelect(sql1);

    if (rc == -1)
      return "";

    return this.colStr("unit_code");
  }

  // ************************************************************************
  public void callBatch(String[] rcvStr) throws Exception {
    String sendData = "";
    String msg = "";
    String msg1 = "";
    String startPgm = rcvStr[0];
    String callUsr = rcvStr[1];
    String callPgm = rcvStr[2];
    String callWkn = rcvStr[3];
    String parmStr = "";
    for (int inti = 4; inti < rcvStr.length; inti++)
      if (rcvStr[inti].length() > 0)
        parmStr = parmStr + " " + rcvStr[inti];

    String lsSql =
        " select wf_value,wf_value2 from PTR_sys_parm where wf_parm='SYSPARM' and wf_key = 'CALLBATCH' ";
    sqlSelect(lsSql);

    // ip and port
    String host = this.colStr("wf_value");
    int port = (int) this.colNum("wf_value2");

    // seqno
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    String modSeqno = comr.getSeqno("ECS_MODSEQ");

    // insert ptr_callbatch
    String lsIns =
        "insert into ptr_callbatch ( " + " batch_seqno,  " + "program_code, " + "start_date,"
            + "user_id," + "workstation_name," + "client_program" + ")values(  " + " :batch_seqno,"
            + " :program_code," + " :start_date," + " :user_id," + " :workstation_name,"
            + " :client_program ) ";

    setString("batch_seqno", String.format("%020d", Long.valueOf(modSeqno)));
    wp.log("STEP 1 =[" + String.format("%020d", Long.valueOf(modSeqno)));
    setString("program_code", startPgm);
    wp.log("STEP 2 =[" + startPgm);
    setString("start_date", getSysDate());
    wp.log("STEP 1 =[" + getSysDate());
    setString("user_id", callUsr);
    wp.log("STEP 3 =[" + callUsr);
    setString("workstation_name", callWkn);
    wp.log("STEP 4 =[" + callWkn);
    setString("client_program", callPgm);
    wp.log("STEP 5 =[" + callPgm);
    sqlExec(lsIns);

    wp.log("aaaaaaaaaaaaaa=[" + sqlRowNum + "]");
    if (sqlRowNum <= 0)
      msg = " ERROR:insert ptr_callbatch";
    else
      sqlCommit(1);

    try (Socket socket = new Socket(host, port);){

      msg += "Starting...  \n";

      try {
        while (true) {
          // 傳送的參數
          /*
           * sendData = callUsr + " " + callWkn + " " + callPgm + " " + "/usr/bin/ksh" + " \"" +
           * System.getenv("PROJ_HOME")+"/etc/brun " + startPgm + "\" " + parmStr + " " +
           * String.format("%020d", Long.valueOf(MOD_SEQNO));
           */

          String batchSeqno = String.format("%020d", Long.valueOf(modSeqno));
          sendData = startPgm + " " + batchSeqno;

          try(DataOutputStream output = new DataOutputStream(socket.getOutputStream());){
        	  msg += "Send data : [" + sendData + "] \n";
              msg1 = "批次序號:[" + batchSeqno + "] \n";
              output.write(sendData.getBytes());
              // output.writeUTF(sendData);
              output.flush();
          }
          
          try(DataInputStream input = new DataInputStream(socket.getInputStream());){
        	  int inputLen = 0;
              byte[] inData = new byte[2048];

              inputLen = input.read(inData, 0, inData.length);
              if (inputLen > 0) {
                  msg += "response data : [" + new String(inData, 0, inputLen) + "] \n";
                  msg1 += "回應訊息:[" + new String(inData, 0, inputLen) + "] \n";
                } else
                  msg += "無回傳資料   \n";
          }

          break;
        }
      } catch (Exception e) {
        msg += "Exception : " + e.getMessage() + "  \n";
      } 
    } catch (IOException e) {
      msg += "Exception2 : " + e.getMessage() + "  \n";
      e.printStackTrace();
    } 

    wp.colSet("proc_mesg", msg);
    wp.colSet("proc_mesg1", msg1);
  }
  // ************************************************************************

} // end program
