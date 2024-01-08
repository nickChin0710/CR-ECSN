/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/12  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名                                                                                     *
* 110-11-05  V1.00.04  Yangbo       joint sql replace to parameters way    *
* 112-05-03  V1.00.05  Ryan       移除團體代號欄位與相關邏輯                                                                  *
***************************************************************************/
package mktm01;

import mktm02.Mktm1110Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0200 extends BaseEdit {
  private String PROGNAME = "聯名機構基本資料維護程式110/03/31 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1110Func func = null;
  String memberCorpNo;
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_member_corp_no"), "a.member_corp_no", "like%");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, "
        + "a.member_corp_no," + "a.member_name," + "a.acct_no"  ;
    wp.daoTable = " mkt_member a ";
    wp.whereOrder = " order by a.member_corp_no ";
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
//	  memberCorpNo = wp.itemStr2("data_k1");
	  memberCorpNo = itemKk("data_k1");
      dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    wp.selectSQL = "hex(a.rowid) as rowid,"
        + "a.member_corp_no," + "a.member_name,"
        + "a.acct_no," + "a.active_status,"
        + "a.crt_date," + "a.crt_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
        + "a.mod_user,a.mod_pgm , " + "a.apr_date," + "a.apr_flag,"+ "a.apr_user";
    wp.daoTable = "mkt_member a";
    wp.whereStr = "WHERE 1=1 ";
    wp.whereStr = wp.whereStr + sqlRowId(memberCorpNo, "a.rowid");
    pageSelect();
    memberCorpNo=wp.colStr("member_corp_no");
    if (sqlNotFind()) {
        alertErr2("查無資料, key= " + "[" + memberCorpNo + "]");
      return;
    }
    wp.setListCount(0);
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    Mktm0200Func func = new Mktm0200Func(wp);
    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());

    this.sqlCommit(rc);
  }
  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
    try {
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************
  public void dataRead2() throws Exception {
	    memberCorpNo=wp.colStr("ll_member_corp_no");
	    wp.selectSQL = "hex(a.rowid) as rowid,"
	        + "a.member_corp_no," + "a.member_name,"
	        + "a.acct_no," + "a.active_status,"
	        + "a.crt_date," + "a.crt_user," + "to_char(a.mod_time,'yyyymmdd') as mod_time,"
	        + "a.mod_user,a.mod_pgm , " + "a.apr_date," + "a.apr_flag,"+ "a.apr_user";
	    wp.daoTable = "mkt_member a";
	    wp.whereStr = "WHERE 1=1 ";
	    wp.whereStr = wp.whereStr +  sqlCol(memberCorpNo, "a.member_corp_no");
	    pageSelect();
	    if (sqlNotFind()) {
	        alertErr2("查無資料, key= " + "[" + memberCorpNo + "]");
	      return;
	    }
	    wp.setListCount(0);
	    checkButtonOff();
	  }
} // End of class
