/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Aoyulan       updated for project coding standard   *
* 109-01-04  V1.00.03   shiyuqi       修改无意义命名                                                                                      *  
******************************************************************************/
package colq01;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;
import busi.SqlPrepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Colq1210 extends BaseEdit {
  CommString commString = new CommString();
  String mProgName = "colq1210";

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      /* 匯入列印ID test */
      strAction = "UPLOAD";
      procUploadFile();
    }

    initButton();
  }

  private boolean getWhereStr() throws Exception {
    wp.whereStr = "where 1=1 ";

    if (empty(wp.itemStr("exReportType")) == false) {
      wp.whereStr += " and report_type = :report_type ";
      setString("report_type", wp.itemStr("exReportType"));
    }
    if (empty(wp.itemStr("exId")) == false) {
      wp.whereStr += " and id_no like :id_no ";
      setString("id_no", wp.itemStr("exId") + "%");
    }

    // -page control-
    wp.queryWhere = wp.whereStr;

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    if (getWhereStr() == false)
      return;

    wp.selectSQL = "id_no, " + "report_status, " + "report_type, " + "crt_date, " + "crt_time, "
        + "mod_time, " + "mod_pgm ";

    wp.daoTable = "col_jcic_s0_list ";

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    listWkdata();
    wp.setPageValue();
  }

  void listWkdata() throws Exception {
    String wkData = "", wkData1 = "";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wkData = wp.colStr(ii, "report_type");
      wp.colSet(ii, "tt_report_type", commString.decode(wkData, ",1,2", ",S01,S02"));
      wkData1 = commString.decode(wkData, ",1,2", ",COLQ1210_S01,COLQ1210_S02");

      wkData = wp.colStr(ii, "report_status");
      wp.colSet(ii, "tt_report_status", wfPtrSysIdtabDesc(wkData1, wkData));
    }
  }

  String wfPtrSysIdtabDesc(String wftype, String wfid) throws Exception {
    String rtn = "";
    String lsSql = "select wf_id||' ['||wf_desc||']' wf_desc from ptr_sys_idtab "
        + "where wf_type= :wf_type and wf_id = :wf_id ";
    setString("wf_type", wftype);
    setString("wf_id", wfid);

    sqlSelect(lsSql);
    if (sqlRowNum == 0) {
      rtn = wfid;
    } else {
      rtn = sqlStr("wf_desc");
    }
    return rtn;
  }

  @Override
  public void querySelect() throws Exception {

  }

  @Override
  public void dataRead() throws Exception {

  }

  public void dataProcess() throws Exception {

  }

  public void procUploadFile() throws Exception {
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
    }
    if (itemIsempty("exReportType")) {
      alertErr2("資料類別：不可空白");
      return;
    }

    fileDataImp();
  }

  void fileDataImp() throws Exception {
    TarokoFileAccess tf = new TarokoFileAccess(wp);

    String rportType = wp.itemStr("exReportType");
    String inputFile = wp.itemStr("zz_file_name");
    int fi = tf.openInputText(inputFile, "UTF-8"); // 決定上傳檔內碼
    // int fi = tf.openInputText(inputFile,"MS950");
    if (fi == -1)
      return;

    if (deleteColJcicS0List(rportType) != 1)
      return;

    // server端 檔案存放位置
    String outputFile = inputFile + ".err";
    File childFile = new File(TarokoParm.getInstance().getWorkDir(), outputFile);
    FileOutputStream fos = null;
    OutputStreamWriter osw = null;
    BufferedWriter bw = null;

    try {
      fos = new FileOutputStream(childFile);
      osw = new OutputStreamWriter(fos, "UTF-8");
      bw = new BufferedWriter(osw);

      String temp = "", lsStr = "";
      int llOk = 0, llErr = 0;
      while (true) {
        temp = tf.readTextFile(fi);
        if (tf.endFile[fi].equals("Y"))
          break;

        if (empty(temp))
          continue;

        String lsStatus = "", lsId = "";
        String[] lsData = temp.split(",", -1);
        if (lsData.length < 2)
          continue;
        lsStatus = lsData[0];
        lsId = lsData[1];
        if (empty(lsId)) {
          llErr++;
          bw.write(temp + "; error=ID空白");
          bw.newLine();
          continue;
        }

        // 若insert 失敗，將此行資料寫入錯誤資料檔。
        if (insertColJcicS0List(rportType, lsStatus, lsId) != 1) {
          llErr++;
          bw.write(temp + "; SQLerror=" + sqlErrtext);
          bw.newLine();
        } else {
          llOk++;
        }
      }
      // 若資料處理均成功，則顯示完成訊息。否則，就rollback，並顯示錯誤訊息。
      if (llErr == 0) {
        sqlCommit(1);
        lsStr = "資料匯入完成, 成功筆數=" + llOk;
      } else {
        sqlCommit(0);
        lsStr = "資料匯入失敗, 錯誤筆數=" + llErr;
      }
      wp.dispMesg = lsStr;
      wp.alertMesg = "<script language='javascript'> alert('" + lsStr + "')</script>";
    } finally {
      // releases resources with the stream
      if (bw != null) {
        try {
          bw.flush();
          bw.close();
        } catch (Exception e) {
        }
      }
      if (osw != null) {
        try {
          osw.close();
        } catch (Exception e) {
        }
      }
      if (fos != null) {
        try {
          fos.close();
        } catch (Exception e) {
        }
      }
    }
    tf.closeInputText(fi);
    tf.deleteFile(inputFile);

    return;
  }

  int deleteColJcicS0List(String varsReportType) {
    String lsSql = "delete col_jcic_s0_list where report_type = :report_type ";
    setString("report_type", varsReportType);
    sqlExec(lsSql);
    // if (sql_nrow <= 0) {
    if (sqlCode < 0) {
      rc = 0;
      alertErr("Delete col_jcic_s0_list error:" + sqlErrtext);
    } else {
      rc = 1;
    }

    sqlCommit(rc);
    return rc;
  }

  int insertColJcicS0List(String varsReportType, String varsReportStatus, String varsIdNo) {
    SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("col_jcic_s0_list");
    sp.addsql("  crt_date ", "  to_char(sysdate,'yyyymmdd') ");
    sp.addsql(", crt_time ", ", to_char(sysdate,'hh24miss') ");
    sp.ppstr("report_type", varsReportType);
    sp.ppstr("report_status", varsReportStatus);
    sp.addsql(", id_p_seqno ", ", uf_idno_pseqno('" + varsIdNo + "') ");
    sp.ppstr("id_no", varsIdNo);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_pgm", mProgName);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    // if (sql_nrow <= 0) {
    if (sqlCode < 0) {
      rc = 0;
    } else {
      rc = 1;
    }
    return rc;
  }

  @Override
  public void saveFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
