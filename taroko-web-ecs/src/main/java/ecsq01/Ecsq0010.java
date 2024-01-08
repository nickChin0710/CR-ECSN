/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-12-07  V1.00.00  批次作業監控紀錄查詢                                  *
* 109-04-24 V1.00.02  yanghan  修改了變量名稱和方法名稱*                                                                           *
* 109/07/14  V1.00.03  Zuwei       rename MEGA_batch.log ==> BANK_batch.log                           *
******************************************************************************/

package ecsq01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;
//import com.enterprisedt.net.ftp.*;
import it.sauronsoftware.ftp4j.*;
import java.io.*;

public class Ecsq0010 extends BaseAction {

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "Q":
        queryFunc(); /* 查詢功能 */
        break;
      case "L":
        strAction = ""; /* 清畫面 */
        clearFunc();
        break;
      case "M":
        queryRead(); /* 瀏覽功能 : skip-page */
        break;
      case "DL":
        downloadLogFile(); /* 下載 LOG FILE */
        break;
      default:
        break;
    }

    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    // wp.col_set(0,"inqu_date",inqu_date);

    String lsWhere = "";
    lsWhere = " where 1=1 " + sqlCol(wp.itemStr2("inqu_date"), "LOG_START_DATE")
        + sqlCol(wp.itemStr2("inqu_prog_name"), "PROGRAM_NAME", "like%");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();
    wp.selectSQL = "program_name ," + "log_start_date ," + "log_start_time ," + "log_end_date ,"
        + "log_end_time ," + "duration_time ," + "status_code," + "mesg_data ";
    wp.daoTable = "ECS_FLOW_CONTROL";
    wp.whereOrder = "order by log_start_date,log_start_time";
    pageQuery();
    wp.setListCount(0);
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
  }

  @Override
  public void dddwSelect() {}

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {}

  @Override
  public void initPage() {}

  @Override
  public void initButton() {}

  @Override
  public void procFunc() {}

  @Override
  public void userAction() {}

  @Override
  public void saveFunc() throws Exception {}

  public void downloadLogFile() throws Exception {

    wp.showLogMessage("I", "", "downloadLogFile " + wp.itemStr2("log_type"));

    String remoteName = "";
    String msg = "";

    if (wp.itemStr2("log_type").equals("BATCH")) {
      remoteName = "BANK_batch.log";
    } else {
      remoteName = "Taroko_Log4j.log";
    }

    String localName = remoteName.substring(0, remoteName.length() - 4);

    String logDate = "";
    if (wp.itemStr2("inqu_date").length() == 8 && !wp.itemStr2("inqu_date").equals(wp.sysDate)) {
      logDate = "." + wp.itemStr2("inqu_date").substring(0, 4) + "-"
          + wp.itemStr2("inqu_date").substring(4, 6) + "-"
          + wp.itemStr2("inqu_date").substring(6, 8);
    }

    remoteName = remoteName + logDate;

    wp.showLogMessage("I", "", "downloadLogFile ftp start " + remoteName);
    try {
      taroko.com.TarokoFTP ftp = new taroko.com.TarokoFTP();
      ftp.localPath = TarokoParm.getInstance().getWorkDir() + "/WebData/work";

      if (wp.itemStr2("log_type").equals("BATCH")) {
        ftp.setRemotePath2("log");
      } else {
        ftp.setHostName(wp.request.getServerName(), "batuser", "batuser");
        ftp.setRemotePath("/ECS/EcsWeb/data/logs");
      }
      ftp.fileName = remoteName;
      ftp.ftpMode = "BIN";
      if (ftp.getFile(wp) != 0) {
        alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
        return;
      }
      msg = ftp.getMesg();
    } catch (Exception ex) {
      msg = ex.getMessage();
      alertErr2("下載檔案失敗: ," + msg);
      return;
    }
    alertMsg("下載檔案完成" + msg);
    wp.showLogMessage("I", "", "downloadLogFile ftp ended " + remoteName);
    wp.setDownload(remoteName);
  }

} // end of class
