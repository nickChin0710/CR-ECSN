/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/02  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名   
* 110-11-08  V1.00.03  machao     SQL Injection                                                                                   *  
***************************************************************************/
package mktq02;

import mktq02.Mktq1010Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq1010 extends BaseEdit {
  private String PROGNAME = "紅利基點異動明細檔查詢處理程式108/12/02 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktq02.Mktq1010Func func = null;
  String rowid;
  String orgTabName = "mkt_tr_bonus";
  String controlTabName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
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
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_bonus_type"), "a.bonus_type", "like%")
        + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%")
        + sqlChkEx(wp.itemStr("ex_id_no"), "4", "");

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
        + "a.trans_date," + "a.bonus_type," + "a.acct_type," + "a.to_acct_type," + "c.id_no,"
        + "c.chi_name," + "a.bonus_pnt," + "a.fee_amt," + "a.tran_seqno," + "a.method,"
        + "a.crt_date," + "a.crt_user," + "a.apr_date," + "a.apr_user";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " " + " order by trans_date desc,tran_seqno desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commBonusType("comm_bonus_type");
    commAcctType("comm_acct_type");
    commAcctType("comm_to_acct_type");

    commMethod("comm_method");

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
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, " + "a.trans_date,"
        + "c.id_no as id_no," + "c.chi_name as chi_name," + "a.tran_seqno," + "a.acct_type,"
        + "a.p_seqno," + "a.id_p_seqno," + "a.to_acct_type," + "a.to_p_seqno," + "a.bonus_type,"
        + "a.method," + "a.bonus_pnt," + "a.fee_amt," + "a.proc_code," + "a.crt_date,"
        + "a.crt_user," + "a.apr_date," + "a.apr_user";

    wp.daoTable = controlTabName + " a " + "JOIN crd_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr;
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]");
      return;
    }
    commMethod("comm_method");
    comProcCode("comm_proc_code");
    commAcctType("comm_acct_type");
    commAcctType("comm_to_acct_type");
    commBonusType("comm_bonus_type");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktq02.Mktq1010Func func = new mktq02.Mktq1010Func(wp);

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
    String lsSql = "";
    try {
      if ((wp.respHtml.equals("mktq1010"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("ex_bonus_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_bonus_type");
        }
        this.dddwList("dddw_bonus_type_b", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type='BONUS_NAME'");
        wp.initOption = "";
        wp.optionKey = itemKk("ex_acct_type");
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "ptr_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    /*
     * if ((wp.item_ss("ex_id_no").length()!=8)&& (wp.item_ss("ex_id_no").length()!=10)&&
     * (wp.item_ss("ex_id_no").length()!=11)) { err_alert("統編輸入8碼, 身分證號10碼,帳戶查詢碼11碼"); return(1); }
     */

    String sql1 = "";
    if (wp.itemStr("ex_id_no").length() == 8) {
      sql1 = "select p_seqno, " + "       chi_name " + "from   crd_corp a,act_acno b "
          + "where  a.corp_p_seqno = b.corp_p_seqno " + "and    b.id_p_seqno   = '' "
//          + "and    corp_no        =  '" + wp.itemStr("ex_id_no").toUpperCase() + "' ";
      	  + " and corp_no = :ex_id_no";
      	  setString("ex_id_no",wp.itemStr("ex_id_no").toUpperCase());

      if (wp.itemStr("ex_acct_type").length() != 0)
//        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";
        sql1 = sql1 + " and   b.acct_type  = :ex_acct_type ";
        setString("ex_acct_type",wp.itemStr("ex_acct_type").toUpperCase());

      sqlSelect(sql1);
      if (sqlRowNum > 1) {
        alertErr2(" 查有多身分資料, 請輸入帳戶類別");
        return (1);
      }

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此統編[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      wp.colSet("ex_id_p_seqno", "");
      wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      return (0);
    }

    if (wp.itemStr("ex_id_no").length() == 10) {
      sql1 = "select a.id_p_seqno, " + "       a.chi_name " + "from crd_idno a,act_acno b "
//          + "where  id_no  =  '" + wp.itemStr("ex_id_no").toUpperCase() + "'"
          + " where 1 = 1 " + " and id_no = :ex_id_no "
          + "and    id_no_code   = '0' " + "and    a.id_p_seqno = b.id_p_seqno ";
      	  setString("ex_id_no",wp.itemStr("ex_id_no").toUpperCase());
      	  
      if (wp.itemStr("ex_acct_type").length() != 0)
        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      wp.colSet("ex_p_seqno", "");
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      return (0);
    }

    if (wp.itemStr("ex_id_no").length() == 11) {
      sql1 = "select a.p_seqno, " + "       a.id_p_seqno, " + "       a.corp_p_seqno, "
          + "       b.card_indicator " + "from act_acno a,ptr_acct_type b "
//          + "where a.acct_key  = '" + wp.itemStr("ex_id_no").toUpperCase() + "' "
          + "where 1 = 1 " + " and a.acct_key = :ex_id_no "
          + "and   a.acct_type = b.acct_type ";
          setString("ex_id_no",wp.itemStr("ex_id_no").toUpperCase());

      if (wp.itemStr("ex_acct_type").length() != 0)
        sql1 = sql1 + "and   b.acct_type  =  '" + wp.itemStr("ex_acct_type").toUpperCase() + "' ";
        sql1 = sql1 + " and b.acct_type = :ex_acct_type ";
        setString("ex_acct_type",wp.itemStr("ex_acct_type").toUpperCase());

      sqlSelect(sql1);
      if (sqlRowNum > 1) {
        alertErr2(" 查有多身分資料, 請輸入帳戶類別");
        return (1);
      }
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此帳戶查詢碼[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }
      if (sqlStr("card_indicator").equals("2")) {
        sql1 = "select chi_name " + "from   crd_corp " 
//        		+ "where  corp_p_seqno = '" + sqlStr("corp_p_seqno") + "' ";
        		+ "where 1 = 1 " + " and corp_p_seqno = :corp_p_seqno ";
        		setString("corp_p_seqno",sqlStr("corp_p_seqno"));
        sqlSelect(sql1);
        wp.colSet("ex_chi_name", sqlStr("chi_name"));
        wp.colSet("ex_id_p_seqno", "");
        wp.colSet("ex_p_seqno", sqlStr("p_seqno"));
      } else {
        sql1 = "select chi_name " + "from   crd_idno " 
//        		+ "where  id_p_seqno = '" + sqlStr("id_p_seqno") + "' ";
        		+ "where 1 = 1" + " and id_p_seqno = :id_p_seqno ";
        		setString("id_p_seqno",sqlStr("id_p_seqno"));
        sqlSelect(sql1);
        wp.colSet("ex_chi_name", sqlStr("chi_name"));
        wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
        wp.colSet("ex_p_seqno", "");
      }
      return (0);
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("4")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
      if (wp.colStr("ex_id_p_seqno").length() != 0)
        return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
      else
        return " and a.p_seqno ='" + wp.colStr("ex_p_seqno") + "' ";
    }

    return "";
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from ptr_acct_type "
          + " where 1 = 1 " 
//    	  + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
          + " and acct_type = :acct_type ";
          setString("acct_type",wp.colStr(ii, "acct_type"));
      if (wp.colStr(ii, "acct_type").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "bonus_TYPE") + "'"
          + " and wf_id = :bonus_TYPE "
          + " and   wf_type = 'BONUS_NAME' ";
      	  setString("bonus_TYPE",wp.colStr(ii, "bonus_TYPE"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commMethod(String cde1) throws Exception {
    String[] cde = {"0", "1"};
    String[] txt = {"現上", "語音"};
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
  public void comProcCode(String cde1) throws Exception {
    String[] cde = {"00", "01"};
    String[] txt = {"移轉成功", "點數不足"};
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

} // End of class
