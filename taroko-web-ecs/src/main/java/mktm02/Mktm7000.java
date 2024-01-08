/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-06  V1.00.00  Andy       program initial                            *
* 107-07-23  V1.00.01  Andy       Update Ui                                  *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
* 112/04/07  V1.00.04  Zuwei Su    上傳失敗仍提示成功            *
******************************************************************************/
package mktm02;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

import java.io.*;
import java.net.*;

public class Mktm7000 extends BaseAction {
  String mExPgm = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      // -資料處理_上傳檔案-
      strAction = "UPLOAD";
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
    } else if (eqIgno(wp.buttonCode, "C2")) {
      strAction = "C2";
      setExample(); // itemchange 依下拉選項改變參數資訊
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理_下載檔案-
      strAction = "C";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {
    String lsSql = "";
    lsSql = "select remote_dir " + "from ecs_ref_ip_addr " + "where ref_ip_code = 'TAROKO_FTP'";
    sqlSelect(lsSql);

    String exPutSvrpath = sqlStr("remote_dir") + "/media/mkt";
    wp.colSet("ex_put_svrpath", exPutSvrpath);
  }

  @Override
  public void dddwSelect() {
    // String ls_dept_no = wp.login_deptNo;
    try {

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_put_id");
      dddwList("dddw_put_id", "ptr_sys_parm", "wf_key", "wf_desc",
          " where 1=1 and wf_parm = 'FTPPARM' " + " and wf_key like 'MKTPUT%'");

      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_get_id");
      dddwList("dddw_get_id", "ptr_sys_parm", "wf_key", "wf_desc",
          " where 1=1 and wf_parm = 'FTPPARM' " + " and wf_key like 'MKTGET%'");
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
  public void procFunc() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    dataProcess();
  }

  public void dataProcess() throws Exception {

    // 上傳檔案
    if (strAction.equals("UPLOAD")) {
      String inputFile = wp.itemStr("zz_file_name");
      String msg = "";

      try {
        taroko.com.TarokoFTP ftp = new taroko.com.TarokoFTP();
        // server端原始上載檔案位置
        ftp.localPath = TarokoParm.getInstance().getDataRoot() + "/upload";

        // 取/ecs/ecs後的路徑
        String exPutSvrpath = wp.itemStr("ex_put_svrpath");
        String pathExPutSvrpath = exPutSvrpath.substring(9);

        // ****遠端路徑2種設定方式
        // ftp.set_remotePath(ex_topath); //set_remotePath :完整路徑
        ftp.setRemotePath2(pathExPutSvrpath); // set_remotePath2 : media...以後路徑
        wp.showLogMessage("I", "", "path_wf_value=" + pathExPutSvrpath);

        // 檔名
        ftp.fileName = inputFile;

        ftp.ftpMode = "BIN";
        if (ftp.putFile(wp) != 0) {
          alertErr("上傳檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
          return;
        }
        msg = ftp.getMesg();
        wp.showLogMessage("I", "", "22222222222222222");
        alertMsg("資料上傳成功");
      } catch (Exception ex) {
        msg = ex.getMessage();
        errmsg("資料上傳失敗");
        alertErr2("資料上傳失敗");

      }
      wp.colSet("proc_mesg", msg);
    }

    // 下載檔案
    if (strAction.equals("C")) {

      String wfKey = wp.itemStr("ex_get_id");
      if (empty(wfKey)) {
        alertErr("請先選擇作業名稱!!");
        return;
      }
      String exGetSvrfile = wp.itemStr("ex_get_svrfile");
      if (empty(exGetSvrfile)) {
        alertErr("下載檔名不可空白!!");
        return;
      }
      String msg = "";

      try {
        taroko.com.TarokoFTP ftp = new taroko.com.TarokoFTP();
        // ftp.set_remotePath(ex_frompath); //set_remotePath 完整路徑
        // ftp.set_remotePath2("media/rpt"); // set_remotePath2 : media...以後路徑

        // 取/ecs/ecs後的路徑
        String wfValue = wp.itemStr("wf_value");
        String pathWfValue = wfValue.substring(9);
        ftp.setRemotePath2(pathWfValue); // set_remotePath2 : media...以後路徑
        wp.showLogMessage("I", "", "path_wf_value=" + pathWfValue);

        // ftp.fileName = "POST0001.DAT";
        ftp.fileName = exGetSvrfile;
        ftp.localPath = TarokoParm.getInstance().getWorkDir();
        // ftp.ftpMode = "BIN";
        ftp.ftpMode = "BIN";
        int rc = ftp.getFile(wp);
        // if (ftp.get_File(wp) != 0) {
        if (rc != 0) {
          alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
        } else {
          // wp.setDownload(ftp.fileName); //轉碼下載
          wp.setDownload(ftp.fileName); // 不轉碼下載
          // ****不轉碼直接開
          // wp.linkMode = "Y";
//           wp.linkURL = wp.request.getScheme() + "://"+wp.request.getServerName()+wp.request.getContextPath()+"/WebData/work/"+ftp.fileName;

        }
        msg = ftp.getMesg();
      } catch (Exception ex) {
        msg = ex.getMessage();
        // msg = "FTP 下載檔案失敗";
        if (msg.indexOf("路徑名稱中的檔案或目錄不存在") > -1) {
          msg = "下載檔案不存在!!";
        }

        if (msg.indexOf("A file or directory in the path name does not exist") > -1) {
          msg = "下載檔案不存在!!";
        }
      }
      wp.colSet("proc_mesg", msg);
      alertErr(msg);

    }


  }

  public void setExample() {
    String exPutId = wp.colStr("ex_put_id");
    String lsSql = "select wf_value,wf_value2 from ptr_sys_parm " + "where 1=1 ";
    lsSql += sqlCol(exPutId, "wf_key");
    sqlSelect(lsSql);
    if (sqlRowNum > 0) {
      wp.colSet("ex_examlp1", sqlStr("wf_value2"));
      wp.colSet("ex_pgname", sqlStr("wf_value2"));

      String lsSql2 = "";
      lsSql2 = "select remote_dir " + "from ecs_ref_ip_addr " + "where ref_ip_code = 'TAROKO_FTP'";
      sqlSelect(lsSql2);

      String exPutSvrpath = sqlStr("wf_value");
      wp.colSet("ex_put_svrpath", exPutSvrpath);
    }
    String exGetId = wp.colStr("ex_get_id");
    String lsSql2 = "select wf_value,wf_value2 from ptr_sys_parm " + "where 1=1 ";
    lsSql2 += sqlCol(exGetId, "wf_key");
    sqlSelect(lsSql2);
    if (sqlRowNum > 0) {
      wp.colSet("ex_get_svrpath", sqlStr("wf_value"));
      wp.colSet("ex_get_svrfile", sqlStr("wf_value2"));
      wp.colSet("wf_value", sqlStr("wf_value"));
    }

  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnModeAud("XX");
  }

  @Override
  public void userAction() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

}
