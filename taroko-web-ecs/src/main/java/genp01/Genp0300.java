/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-09  V1.00.00  Andy       program initial                            *
* 108-12-03  V1.00.01  Andy       add userAuth_run()                         *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        * 
*  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package genp01;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

import java.io.*;
import java.net.*;

public class Genp0300 extends BaseProc {

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
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      dataProcess1();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      // -資料處理-
      set_path();
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
    wp.colSet("ex_serverpath", "/ecs/ecs/media/bil");
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_bpgm");
      // dddw_list("dddw_prog_list","bil_prog","prog_code","prog_name","where
      // 1=1 order by prog_code");
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
    String ex_fromfile = wp.itemStr("ex_fromfile");
    String msg = "";
    try {
      taroko.com.TarokoFTP ftp = new taroko.com.TarokoFTP();
      // ftp.set_remotePath(ex_frompath); //set_remotePath 完整路徑
      ftp.setRemotePath2("media/gen"); // set_remotePath2 : media...以後路徑
      // ftp.fileName = "POST0001.DAT";
      ftp.fileName = ex_fromfile;
      ftp.localPath = TarokoParm.getInstance().getWorkDir();
      // ftp.ftpMode = "BIN";
      ftp.ftpMode = "BIN";
      if (ftp.getFile(wp) != 0) {
        alertErr("下載檔案失敗: ", ftp.fileName + "; err=" + ftp.getMesg());
      } else {
        wp.setDownload(ftp.fileName);
        // wp.linkMode = "Y";
        // wp.linkURL =
//         wp.request.getScheme() + "://"+wp.request.getServerName()+wp.request.getContextPath()+"/WebData/work/"+ftp.fileName;

      }
      msg = ftp.getMesg();
    } catch (Exception ex) {
      msg = ex.getMessage();
    }
    wp.colSet("proc_mesg", msg);
  }

  public void dataProcess1() throws Exception {
    ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
    // --callbatch
    batch.callBatch(wp.itemStr("ex_pgname"), wp.loginUser);
    wp.colSet("proc_mesg", wp.itemStr("ex_pgname") + "," + batch.getMesg());
    wp.colSet("ex_pgname", "");

    return;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  public void set_path() {
    String myfile = "";
    myfile = wp.colStr("myfile");
    wp.colSet("ex_fromfile", myfile);
  }

}
