/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/07  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名    
* 111-08-08  V1.00.03   machao      數據覆核過程中數據添加不完整Bug處理                                                                        *   
***************************************************************************/
package mktp02;

import mktp02.Mktp1030Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1030 extends BaseProc {
  private String PROGNAME = "高鐵車廂請款明細檔覆核作業處理程式108/11/07 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp1030Func func = null;
  String rowid;
  String serialNo;
  String fstAprFlag = "";
  String orgTabName = "mkt_thsr_uptxn_t";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int[] datachk_cnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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
    } else if (eqIgno(wp.buttonCode, "C")) {// 資料處理 -/
      strAction = "A";
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
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
    		+ sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%")
        + sqlStrend(wp.itemStr("ex_trans_date_s"), wp.itemStr("ex_trans_date_e"), "a.trans_date");

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

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.aud_type," + "a.serial_no," + "a.auth_flag," + "a.trans_type," + "a.trans_date,"
        + "a.error_desc," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.crt_user, a.trans_date";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCrtUser("comm_crt_user");
    commAuthFkag("comm_auth_flag");
    commTransType("comm_trans_type");
    commfuncAudType("aud_type");

    // list_wkdata();
    wp.setPageValue();
  }

// ************************************************************************
  @Override
  public void querySelect() throws Exception {

    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (qFrom == 0)
      if (wp.itemStr("kk_serial_no").length() == 0) {
        alertErr("查詢鍵必須輸入");
        return;
      }
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.aud_type,"
        + "a.serial_no as serial_no," + "a.crt_user," + "a.pay_cardid," + "a.auth_flag,"
        + "a.error_desc," + "a.trans_date," + "a.trans_time," + "a.card_no,"
        + "a.authentication_code";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(serialNo, "a.serial_no");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commAuthFkag("comm_auth_flag");
    commCrtUser("comm_crt_user");
    checkButtonOff();
    serialNo = wp.colStr("serial_no");
    listWkdataAft();
    if (!wp.colStr("aud_type").equals("A"))
      dataReadR3R();
    else {
      commfuncAudType("aud_type");
      listWkdataSpace();
    }
  }

  // ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = "MKT_THSR_UPTXN";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.serial_no as serial_no," + "a.crt_user as bef_crt_user,"
        + "a.pay_cardid as bef_pay_cardid," + "a.auth_flag as bef_auth_flag,"
        + "a.error_desc as bef_error_desc," + "a.trans_date as bef_trans_date,"
        + "a.trans_time as bef_trans_time," + "a.card_no as bef_card_no,"
        + "a.authentication_code as bef_authentication_code";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(serialNo, "a.serial_no");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commAuthFkag("comm_auth_flag");
    commCrtUser("comm_crt_user");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {}

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("auth_flag").equals(wp.colStr("bef_auth_flag")))
      wp.colSet("opt_auth_flag", "Y");
    commAuthFkag("comm_auth_flag");
    commAuthFkag("comm_bef_auth_flag");

    if (!wp.colStr("error_desc").equals(wp.colStr("bef_error_desc")))
      wp.colSet("opt_error_desc", "Y");

    if (!wp.colStr("trans_date").equals(wp.colStr("bef_trans_date")))
      wp.colSet("opt_trans_date", "Y");

    if (!wp.colStr("trans_time").equals(wp.colStr("bef_trans_time")))
      wp.colSet("opt_trans_time", "Y");

    if (!wp.colStr("card_no").equals(wp.colStr("bef_card_no")))
      wp.colSet("opt_card_no", "Y");

    if (!wp.colStr("authentication_code").equals(wp.colStr("bef_authentication_code")))
      wp.colSet("opt_authentication_code", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("auth_flag", "");
      wp.colSet("error_desc", "");
      wp.colSet("trans_date", "");
      wp.colSet("trans_time", "");
      wp.colSet("card_no", "");
      wp.colSet("authentication_code", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("auth_flag").length() == 0)
      wp.colSet("opt_auth_flag", "Y");

    if (wp.colStr("error_desc").length() == 0)
      wp.colSet("opt_error_desc", "Y");

    if (wp.colStr("trans_date").length() == 0)
      wp.colSet("opt_trans_date", "Y");

    if (wp.colStr("trans_time").length() == 0)
      wp.colSet("opt_trans_time", "Y");

    if (wp.colStr("card_no").length() == 0)
      wp.colSet("opt_card_no", "Y");

    if (wp.colStr("authentication_code").length() == 0)
      wp.colSet("opt_authentication_code", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp02.Mktp1030Func func = new mktp02.Mktp1030Func(wp);

    String[] lsSerialNo = wp.itemBuff("serial_no");
    String[] lsAudType = wp.itemBuff("aud_type");
    String[] lsCrtUser = wp.itemBuff("crt_user");
    String[] lsRowid = wp.itemBuff("rowid");
    String[] opt = wp.itemBuff("opt");
    wp.listCount[0] = lsAudType.length;

    int rr = -1;
    wp.selectCnt = lsAudType.length;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) (this.toNum(opt[ii]) - 1);
      if (rr < 0)
        continue;
      wp.log("" + ii + "-ON." + lsRowid[rr]);

      wp.colSet(rr, "ok_flag", "-");
      if (lsCrtUser[rr].equals(wp.loginUser)) {
        ilAuth++;
        wp.colSet(rr, "ok_flag", "F");
        continue;
      }

      func.varsSet("serial_no", lsSerialNo[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A"))
        rc = func.dbInsertA4();
      else if (lsAudType[rr].equals("U"))
        rc = func.dbUpdate_U4();
      else if (lsAudType[rr].equals("D"))
        rc = func.dbDeleteD4();

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
    	commCrtUser("comm_crt_user");
        commAuthFkag("comm_auth_flag");
        commTransType("comm_trans_type");
        commfuncAudType("aud_type");

        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        func.dbDelete();
        this.sqlCommit(rc);
        continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
      this.sqlCommit(0);
    }

    alertMsg("放行處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr + "; 權限問題=" + ilAuth);
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
	  String ls_sql ="";
	  try {
	       if ((wp.respHtml.equals("mktp1030")))
	         {
	          wp.initOption ="--";
	          wp.optionKey = "";
	          if (wp.colStr("ex_crt_user").length()>0)
	             {
	             wp.optionKey = wp.colStr("ex_crt_user");
	             }
	          ls_sql = "";
	          ls_sql =  procDynamicDddwCrtUser1(wp.colStr("ex_crt_user"));
	          wp.optionKey = wp.colStr("ex_crt_user");
	          dddwList("dddw_crt_user_1", ls_sql);
	         }
	      } catch(Exception ex){}
  }

  // ************************************************************************
  private void commCrtUser(String s1) throws Exception {
	  commCrtUser(s1,0);
	  return;
	
}
  
//************************************************************************
public void commCrtUser(String s1,int bef_type) throws Exception 
{
 String columnData="";
 String sql1 = "";
 String befStr="";
 if (bef_type==1) befStr="bef_";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " usr_cname as column_usr_cname "
           + " from sec_user "
           + " where 1 = 1 "
           + " and   usr_id = '"+wp.colStr(ii,befStr+"crt_user")+"'"
           ;
      if (wp.colStr(ii,befStr+"crt_user").length()==0)
         {
          wp.colSet(ii, s1, columnData);
          continue;
         }
      sqlSelect(sql1);

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, s1, columnData);
     }
  return;
}
//************************************************************************  
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

  // ************************************************************************
  public void commAuthFkag(String cde1) throws Exception {
    String[] cde = {"N", "Y", "X"};
    String[] txt = {"錯誤待處理", "審核完畢", "結案不處理"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
  }

  // ************************************************************************
  public void commTransType(String cde1) throws Exception {
    String[] cde = {"P", "R"};
    String[] txt = {"購票", "退票"};
    String columnData = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      for (int inti = 0; inti < cde.length; inti++) {
        String txt1 = cde1.substring(5, cde1.length());
        if (wp.colStr(ii, txt1).equals(cde[inti])) {
          wp.colSet(ii, cde1, txt[inti]);
          break;
        }
      }
    }
    return;
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
  String procDynamicDddwCrtUser1(String s1)  throws Exception
  {
    String ls_sql = "";

    ls_sql = " select "
           + " b.crt_user as db_code, "
           + " max(b.crt_user||' '||a.usr_cname) as db_desc "
           + " from sec_user a,mkt_thsr_uptxn_t b "
           + " where a.usr_id = b.crt_user "
           + " and   b.apr_flag = 'N' "
           + " group by b.crt_user "
           ;

    return ls_sql;
  }


}  // End of class
