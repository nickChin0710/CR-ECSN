/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/04/04  V1.00.01   machao      Initial                                *
* 112/04/04  V1.00.02   machao      修訂調整                                *
* 112/05/04  V1.00.03   Zuwei Su    查詢詳情異常                             *
* 112/05/11  V1.00.04   machao      跳转詳情頁修訂調整                        *
***************************************************************************/
package mktm01;

import java.util.ArrayList;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0920 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private final String PROGNAME = "金庫幣活動回饋參數設定  112/05/04  V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0920Func func = null;
  String rowid;
  String activeCode;
  String activeGroupId;
  String kk1, kk2, kk3, kk4;
  String km1, km2, km3, km4;
  String fstAprFlag = "";
  String orgTabName = "MKT_GOLDBILL_PARM";
  String controlTabName = "";
  int qFrom = 0;
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
      wp.itemSet("aud_type", "A");
      insertFunc();
    }else if (eqIgno(wp.buttonCode, "U")){/* 更新功能 */
    	 strAction = "U3";
         updateFuncU3R();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    }  else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " 
    		+ sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
    		+ sqlCol(wp.itemStr("ex_active_type"),"a.active_type");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code,"
            + "a.active_name,"
            + "a.active_type,"
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.crt_user,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_code";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commfuncActType("active_type");
    wp.setPageValue();
  }


  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N")) {
    	controlTabName = orgTabName + "_t";
    	rowid = itemKk("data_k1");
    	activeCode = itemKk("data_k2");
        qFrom = 1;
    }else {
    	wp.colSet("apr_flag", "Y");
    }
    rowid = itemKk("data_k1");
    activeCode = itemKk("data_k2");
    
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code,"
            + "a.active_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.active_type,"
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.active_date_s,"
            + "a.active_date_e,"
            + "a.feedback_cycle,"
            + "a.feedback_dd,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeCode, "a.active_code");

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    wp.colSet("aud_type", "Y");
    checkButtonOff();
    activeGroupId = wp.colStr("active_code");
    commfuncAudType("aud_type");
    dataReadR3R();
  }
  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    kk1 = itemKk("ROWID");
    km1 = wp.itemStr("active_code");
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1) {
          dataReadR3R();
        }
    } else {
      km1 = wp.itemStr("active_code");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }
  
  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
//    controlTabName = orgTabName ;
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_code,"
            + "a.active_name,"
            + "a.stop_flag,"
            + "a.stop_date,"
            + "a.active_type,"
            + "a.purchase_date_s,"
            + "a.purchase_date_e,"
            + "a.active_date_s,"
            + "a.active_date_e,"
            + "a.feedback_cycle,"
            + "a.feedback_dd,"
            + "a.aud_type,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " 
            + sqlCol(wp.colStr("active_code"), "a.active_code") 
            + sqlCol(wp.colStr("active_type"), "a.active_type") ;

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
  }


// ************************************************************************
  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm01.Mktm0920Func func = new mktm01.Mktm0920Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
	  try {
	  wp.initOption = "--";
      wp.optionKey = "";
      if (wp.colStr("active_code").length() > 0) {
      }
		this.dddwList("dddw_active_code", "mkt_goldbill_parm_t", "active_code", "active_name",
		          "where 1=1 order by active_code");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

//************************************************************************
 void commfuncActType(String cde1) {
   if (cde1 == null || cde1.trim().length() == 0)
     return;
   String[] cde = {"1", "2"};
   String[] txt = {"新申辦電子帳單", "全新戶-自動扣繳"};

   for (int ii = 0; ii < wp.selectCnt; ii++) {
     wp.colSet(ii, "comm_func_" + cde1, "");
     for (int inti = 0; inti < cde.length; inti++)
       if (wp.colStr(ii, cde1).equals(cde[inti])) {
         wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
         break;
       }
   }
 }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
      buttonOff("uplmrcd_disable");
    } else {
      wp.colSet("uplmrcd_disable", "");
    }
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnmrcd_disable");
    buttonOff("uplmrcd_disable");

    return;
  }
  // ************************************************************************

} // End of class
