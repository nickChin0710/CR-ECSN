/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-05-09  V1.00.00  ryan       program initial                            *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard 
* 111-01-19  V1.00.03  machao      系统弱扫：Throw Inside Finally        *
******************************************************************************/
package ptrm02;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import java.io.*;
import java.net.*;


public class Ptrm0787 extends BaseProc {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-

      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_bpgm");
      // dddw_list("dddw_prog_list","bil_prog","prog_code","prog_name","where 1=1 order by
      // prog_code");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

  }

  @Override
  public void queryRead() throws Exception {

  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {
    if (empty(wp.itemStr("ex_batch"))) {
      alertErr2("批次名稱參數不能空白");
      return;
    }
    String[] exBatch = wp.itemStr("ex_batch").split("\\s");
    if (exBatch[0].indexOf(".sh") >= 0) {
      fCallShell();
    } else {
      fCallBatch();
    }
  }

  void fCallBatch() throws Exception {
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    // --callbatch
    batch.callBatch(wp.itemStr("ex_batch"), wp.loginUser);
    wp.colSet("proc_mesg", wp.itemStr("ex_batch") + "," + batch.getMesg());
    wp.colSet("ex_batch", "");
  }

  void fCallShell() throws IOException {
    String sendData = "", msg = "";
    // InetAddress sAddress= InetAddress.getByName(wp.request.getRemoteAddr());
    String lsSql =
        " select wf_value,wf_value2 from PTR_sys_parm where wf_parm='SYSPARM' and wf_key = 'CALLBATCH' ";
    sqlSelect(lsSql);
    // ip and port
    String host = sqlStr("wf_value");
    int port = (int) sqlNum("wf_value2");


    // seqno
    String lsModSeqno = " select ecs_modseq.nextval AS MOD_SEQNO from dual ";
    sqlSelect(lsModSeqno);

    String MOD_SEQNO = sqlStr("MOD_SEQNO");

    // 傳送的參數
    // crdp0028
    String exBpgm = wp.itemStr("ex_batch");

    // shell 路徑
    sendData = wp.loginUser + " " + wp.request.getRemoteHost() + " " + wp.itemStr("MOD_PGM") + " "
        + "/usr/bin/ksh" + " " + System.getenv("PROJ_HOME") + "/shell/" + exBpgm + " "
        + String.format("%020d", Long.valueOf(MOD_SEQNO));

    // insert ptr_callbatch
    String lsIns = "insert into ptr_callbatch ( " + " batch_seqno,  " + "program_code, "
        + "start_date," + "user_id," + "workstation_name," + "client_program," + "parameter_data"
        + ")values(  " + " :batch_seqno," + " :program_code," + " :start_date," + " :user_id,"
        + " :workstation_name," + " :client_program , " + " :parameter_data ) ";
    setString("batch_seqno", String.format("%020d", Long.valueOf(MOD_SEQNO)));
    setString("program_code", exBpgm);
    setString("start_date", getSysDate());
    setString("user_id", wp.loginUser);
    setString("workstation_name", wp.request.getRemoteHost()); // sAddress.getHostName());
    setString("client_program", wp.itemStr("MOD_PGM"));
    setString("parameter_data", sendData);
    sqlExec(lsIns);
    if (sqlRowNum <= 0) {
      msg = " ERROR:insert ptr_callbatch";
    } else {
      sqlCommit(1);
    }
    Socket socket = null;

    try {
      socket = new Socket(host, port);
      DataInputStream input = null;
      DataOutputStream output = null;
      msg += "Starting...  \n";

      try {
        while (true) {

          output = new DataOutputStream(socket.getOutputStream());
          msg += "Send data : [" + sendData + "] \n";
          output.write(sendData.getBytes());
          // output.writeUTF(sendData);
          output.flush();

          input = new DataInputStream(socket.getInputStream());
          int inputLen = 0;
          byte[] inData = new byte[2048];

          inputLen = input.read(inData, 0, inData.length);
          if (inputLen > 0) {
            msg += "response data : [" + new String(inData, 0, inputLen) + "] \n";
          } else {
            msg += "無回傳資料   \n";
          }
          break;
        }
      } catch (Exception e) {
        msg += "Exception : " + e.getMessage() + "  \n";
      } finally {
        if (input != null)
          input.close();
        if (output != null)
          output.close();
        msg += "Terminated..\n";
      }
    } catch (IOException e) {
      msg += "Exception2 : " + e.getMessage() + "  \n";
      e.printStackTrace();
    } 
//    finally {
//      if (socket != null)
//        socket.close();
//      // if(consoleInput != null ) consoleInput.close();
//      msg += "Socked Closed...  \n ";
//    }
    try {
    	if (socket != null)
          socket.close();
    }catch(Exception e){
    	msg += "Socked Closed...  \n ";
    }
    wp.colSet("proc_mesg", msg);
    wp.colSet("ex_batch", "");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }


}
