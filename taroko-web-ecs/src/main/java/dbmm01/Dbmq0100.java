/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/05  V1.00.01   Allen Ho      Initial                              *
*  109-04-23  V1.00.02  yanghan  修改了變量名稱和方法名稱*
* * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *                                               *
* 110-10-29  V1.00.04  Yangbo       joint sql replace to parameters way    *
* 111-10-14  V1.00.05  Ryan          modify     *
***************************************************************************/
package dbmm01;

import dbmm01.Dbmq0100Func;
import ofcapp.AppMsg;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmq0100 extends BaseEdit
{
  private final String PROGNAME = "VD帳戶紅利明細檔查詢作業處理程式111/10/14 V1.00.05";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  dbmm01.Dbmq0100Func func = null;
  String rowid/* , kk2, kk3, kk4, kk5 */;
  String orgTabName = "dbm_bonus_dtl";
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
    } else if (eqIgno(wp.buttonCode, "T")) {/* 動態查詢 */
      querySelect1();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    } else if (eqIgno(wp.buttonCode, "T2")) {/* 動態查詢 */
    	strAction = "T2";
        querySelect1();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    if (queryCheck() != 0)
      return;
    wp.whereStr = "WHERE 1=1 " + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
        + sqlCol(wp.itemStr("ex_acct_type"), "a.acct_type", "like%");
    if(wp.itemEq("ex_select_type", "2")) {
		wp.whereStr += sqlCol(wp.itemStr("ex_tran_date1"), "a.tran_date", ">=")
				+ sqlCol(wp.itemStr("ex_tran_date2"), "a.tran_date", "<=") + " and a.apr_flag = 'Y'";
    }
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    
    if(wp.itemEq("ex_select_type", "1")) {
    	queryRead();
    }else {
    	queryRead2();
    }
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "a.acct_type, " + "a.id_p_seqno, " + "a.bonus_type, "
        + "max(ACCT_TYPE) as acct_type," + "max('') as id_no,"
        + "sum(END_TRAN_BP) as TOT_END_TRAN_BP,"
        + "sum(decode(sign(to_char(add_months(sysdate,-3),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail3_BP,"
        + "sum(decode(sign(to_char(add_months(sysdate,-6),'yyyymmdd') - decode(effect_e_date,'','99999999',effect_e_date)),1,end_tran_bp,0)) as TOT_fail6_BP,"
        + "max(tran_date) as tran_date," + "count(*) as data_cnt,"
        + "max(id_p_seqno) as id_p_seqno";
    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " group by a.acct_type,a.id_p_seqno,a.bonus_type"
        + " order by a.acct_type,a.id_p_seqno,a.bonus_type,tran_date,bonus_type";
 
    wp.pageCountSql =
        "select count(*) from ( " + " select distinct a.acct_type,a.id_p_seqno,a.bonus_type"
            + " from " + wp.daoTable + " " + wp.queryWhere + " )";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commAcctType("comm_acct_type");
    commIdNo("comm_id_no");
    commBonusType("comm_bonus_type");

    // list_wkdata();
    wp.setPageValue();
  }

  public void queryRead2() throws Exception {
		if (wp.colStr("org_tab_name").length() > 0)
			controlTabName = wp.colStr("org_tab_name");
		else
			controlTabName = orgTabName;

		wp.pageControl();
		wp.selectSQL = "hex(a.rowid) as rowid ,a.acct_type, a.tran_date, a.tran_code, a.beg_tran_bp ,trim(mod_memo) as mod_memo , trim(mod_desc) as mod_desc, a.tran_time ";
		wp.daoTable = controlTabName + " a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
		wp.whereOrder = " order by a.tran_date desc , a.tran_time desc";
		pageQuery();
		wp.setListCount(2);
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
			return;
		}
		commTranCode("comm_tran_code");
		commModMemo("mod_memo", "mod_desc");

		wp.setPageValue();
  }
  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

    wp.colSet("bb_acct_type", itemKk("data_k2"));
    wp.colSet("bb_id_p_seqno", itemKk("data_k3"));
    wp.colSet("bb_bonus_type", itemKk("data_k4"));
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  public void querySelect1() throws Exception {
    controlTabName = orgTabName;

    rowid = itemKk("data_k1");
    qFrom = 2;
    dataRead();
    if(strAction.equals("T2")) {
    	wp.colSet("ex_select_type", "2");
    }
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + " ROW_NUMBER()OVER() as ser_num, " + "a.tran_seqno," + "a.bonus_type," + "a.active_code,"
        + "a.active_name," + "c.id_no as id_no," + "c.chi_name as chi_name," + "a.acct_type,"
        + "a.tran_date," + "a.tran_time," + "a.tran_code," + "a.tran_pgm," + "a.beg_tran_bp,"
        + "a.end_tran_bp," + "a.tax_flag," + "a.effect_e_date," + "a.mod_reason," + "a.mod_desc,"
        + "a.mod_memo," + "a.id_p_seqno," + "a.crt_user," + "a.crt_date," + "a.mod_user,"
        + "to_char(a.mod_time,'yyyymmdd') as mod_time," + "a.apr_user," + "a.apr_date";

    wp.daoTable = controlTabName + " a " + "JOIN dbc_idno c " + "ON a.id_p_seqno = c.id_p_seqno ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(itemKk("data_k1"), "a.bonus_type")
          + sqlCol(itemKk("data_k2"), "a.acct_type");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlCol(wp.colStr("bb_acct_type"), "a.acct_type")
          + sqlCol(wp.colStr("bb_id_p_seqno"), "a.id_p_seqno")
          + sqlCol(wp.colStr("bb_bonus_type"), "a.bonus_type")
          + " order by tran_date desc,tran_time desc,tran_seqno desc fetch first 500 rows only";
    } else {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    wp.setListCount(1);
    commBonusType("comm_bonus_type");
    commAcctType("comm_acct_type");
    commTranCode("comm_tran_code");
    commModReason("comm_mod_reason");
    wp.colSet("", itemKk("data_kN"));

    if (qFrom != 0) {
      commBonusType("comm_bonus_type");
      commAcctType("comm_acct_type");
      commTranCode("comm_tran_code");
      commTaxFlag("comm_tax_flag");
      commModReason("comm_mod_reason");
    }
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    dbmm01.Dbmq0100Func func = new dbmm01.Dbmq0100Func(wp);

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
      if ((wp.respHtml.equals("dbmq0100"))) {
        wp.initOption = "";
        wp.optionKey = itemKk("ex_acct_type");
        if (wp.colStr("ex_acct_type").length() > 0) {
          wp.optionKey = wp.colStr("ex_acct_type");
        }
        this.dddwList("dddw_acct_type1", "dbp_acct_type", "trim(acct_type)", "trim(chin_name)",
            " where 1 = 1 ");
      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public int queryCheck() throws Exception {
    if (itemKk("ex_id_no").length() == 0) {
      alertErr2("身份證號不可空白");
      return (1);
    }

    if (wp.itemStr("ex_query_table").equals("2"))
      orgTabName = "dbm_bonus_dtl_hst";
    else
      orgTabName = "dbm_bonus_dtl";

    controlTabName = orgTabName.toUpperCase();
    wp.colSet("control_tab_name", controlTabName);


    if (wp.itemStr("ex_id_no").length() > 0) {
      String idNo = wp.itemStr("ex_id_no");
      String idNoCode = "0";
      if (wp.itemStr("ex_id_no").length() > 10) {
        idNoCode = wp.itemStr("ex_id_no").substring(10, wp.itemStr("ex_id_no").length());
        idNo = wp.itemStr("ex_id_no").substring(0, 10);
      }
      String sql1 = "select id_p_seqno,chi_name " + "from   dbc_idno "
//          + "where  id_no      = '"
//          + idNo.toUpperCase() + "' " + "and    id_no_code = '" + idNoCode + "' ";
          + "where 1 = 1 "
          + sqlCol(idNo.toUpperCase(), "id_no")
          + sqlCol(idNoCode, "id_no_code");

      sqlSelect(sql1);
      if (sqlRowNum <= 0) {
        alertErr2(" 查無此身分證號[ " + wp.itemStr("ex_id_no").toUpperCase() + "] 資料");
        return (1);
      }

      wp.colSet("ex_id_p_seqno", sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name", sqlStr("chi_name"));
      
    }
    
    if(wp.itemEq("ex_select_type", "2")) {
    	if (wp.itemEmpty("ex_tran_date1") || wp.itemEmpty("ex_tran_date2")) {
			alertErr2("查詢日期區間起迄不可空白");
			return (1);
		}
		if (chkStrend(wp.itemStr("ex_tran_date1"), wp.itemStr("ex_tran_date2")) == false) {
			alertErr2("查詢日期區間起迄大小值錯誤");
			return (1);
		}
		if(comm.datePeriod(wp.itemStr("ex_tran_date1"),wp.itemStr("ex_tran_date2")) > 365) {
			alertErr2("查詢日期區間限制1年內");
			return (1);
		}
    }

    return (0);
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    if (sqCond.equals("1")) {
      if (empty(wp.itemStr("ex_id_no")))
        return "";
//      return " and a.id_p_seqno ='" + wp.colStr("ex_id_p_seqno") + "' ";
        return sqlCol(wp.colStr("ex_id_p_seqno"), "a.id_p_seqno");
    }


    return "";
  }

  // ************************************************************************
  public void commBonusType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "bonus_TYPE") + "'"
//          + sqlCol(wp.colStr(ii, "bonus_TYPE"), "wf_id")
		  + " and wf_id = ? "
          + " and   wf_type = 'BONUS_NAME' ";
      setString(1,wp.colStr(ii, "bonus_type"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commAcctType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " chin_name as column_chin_name " + " from dbp_acct_type "
          + " where 1 = 1 "
//              + " and   acct_type = '" + wp.colStr(ii, "acct_type") + "'";
          + sqlCol(wp.colStr(ii, "acct_type"), "acct_type");
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
  public void commModReason(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
//          + " and   wf_id = '" + wp.colStr(ii, "mod_reason") + "'"
//          + sqlCol(wp.colStr(ii, "mod_reason"), "wf_id")
			+ " and wf_id = ? "
          + " and   wf_type = 'ADJMOD_REASON' ";
      setString(1,wp.colStr(ii, "mod_reason"));
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commIdNo(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " id_no as column_id_no " + " from dbc_idno " + " where 1 = 1 "
//          + " and   id_p_seqno = '" + wp.colStr(ii, "id_p_seqno") + "'";
            + sqlCol(wp.colStr(ii, "id_p_seqno"), "id_p_seqno");
      if (wp.colStr(ii, "id_p_seqno").length() == 0)
        continue;
      sqlSelect(sql1);

      if (sqlRowNum > 0)
        columnData = columnData + sqlStr("column_id_no");
      wp.colSet(ii, columnData1, columnData);
    }
    return;
  }

  // ************************************************************************
  public void commTranCode(String cde1) throws Exception {
    String[] cde = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] txt = {"移轉", "新增", "贈與", "調整", "使用", "匯入", "移除", "扣回"};
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
  public void commTaxFlag(String cde1) throws Exception {
    String[] cde = {"Y", "N"};
    String[] txt = {"應稅", "免稅"};
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
  
  //************************************************************************
  public void commModMemo(String columnData1,String columnData2) throws UnsupportedEncodingException {
	  String modMemo = "";
	  for (int ii = 0; ii < wp.selectCnt; ii++) {
		  modMemo = wp.colStr(ii,columnData1);
		  if(empty(modMemo)) {
			  modMemo = wp.colStr(ii,columnData2);
			  if(commString.left(modMemo,2).equals("轉置")) {
				  modMemo = "";
			  }
		  }
		  modMemo = subString(modMemo,40);
		  wp.colSet(ii,columnData1,modMemo);
	  }
  }
  
  //************************************************************************
  public String subString(String columnData, int length) {
	  int len = 0;
	  String str = "";
		for(int i=0;i<columnData.length();i++){
			int acsii = columnData.charAt(i); 
			len += (acsii<0 || acsii > 128)?2:1;
			if(len > length) {
				break;
			}
			str += columnData.charAt(i); 
		}
	  return str;
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
