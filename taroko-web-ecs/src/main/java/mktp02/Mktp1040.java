/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/11/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名           
* 111-06-08  V1.00.04   machao      bug处理                                                                                *   
***************************************************************************/
package mktp02;

import mktp02.Mktp1040Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1040 extends BaseProc {
  private String PROGNAME = "高鐵車廂升等卡類覆核處理程式108/11/05 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktp02.Mktp1040Func func = null;
  String rowid;
  String cardMode;
  String fstAprFlag = "";
  String orgTabName = "mkt_thsr_upmode_t";
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
    wp.whereStr = "WHERE 1=1 " + sqlCol(wp.itemStr("ex_crt_user"), "a.crt_user", "like%");

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
        + "a.aud_type," + "a.card_mode," + "a.mode_desc," + "a.card_type_sel," + "a.group_code_sel,"
        + "a.ticket_pnt_cond," + "a.ticket_amt_cond," + "a.ex_ticket_amt," + "a.crt_user,"
        + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.crt_user";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }


    commCardType("comm_card_type_sel");
    commGroupCode("comm_group_code_sel");
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
      if (wp.itemStr("kk_card_mode").length() == 0) {
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
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
//        + " id_p_seqno as id_p_seqno," 
    	+ "a.aud_type," + "a.card_mode as card_mode," + "a.crt_user,"
        + "a.mode_desc," + "a.card_type_sel," + "a.group_code_sel," + "a.max_ticket_cnt,"
        + "a.ticket_pnt_cond," + "a.ticket_pnt_cnt," + "a.ticket_pnt," + "a.ticket_amt_cond,"
        + "a.ticket_amt_cnt," + "a.ticket_amt," + "a.ex_ticket_amt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(cardMode, "a.card_mode");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    commCardType("comm_card_type_sel");
    commGroupCode("comm_group_code_sel");
    checkButtonOff();
    cardMode = wp.colStr("card_mode");
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
    controlTabName = "MKT_THSR_UPMODE";
    wp.selectSQL = "hex(a.rowid) as rowid," + " nvl(a.mod_seqno,0) as mod_seqno, "
        + "a.card_mode as card_mode," + "a.crt_user as bef_crt_user,"
        + "a.mode_desc as bef_mode_desc," + "a.card_type_sel as bef_card_type_sel,"
        + "a.group_code_sel as bef_group_code_sel," + "a.max_ticket_cnt as bef_max_ticket_cnt,"
        + "a.ticket_pnt_cond as bef_ticket_pnt_cond," + "a.ticket_pnt_cnt as bef_ticket_pnt_cnt,"
        + "a.ticket_pnt as bef_ticket_pnt," + "a.ticket_amt_cond as bef_ticket_amt_cond,"
        + "a.ticket_amt_cnt as bef_ticket_amt_cnt," + "a.ticket_amt as bef_ticket_amt,"
        + "a.ex_ticket_amt as bef_ex_ticket_amt";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(cardMode, "a.card_mode");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    commCardType("comm_card_type_sel");
    commGroupCode("comm_group_code_sel");
    checkButtonOff();
    commfuncAudType("aud_type");
    listWkdata();
  }

  // ************************************************************************
  void listWkdataAft() throws Exception {
    wp.colSet("card_type_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_THSR_UPMODE", wp.colStr("card_mode"), "1"));
    wp.colSet("group_code_sel_cnt",
        listMktBnData("mkt_bn_data_t", "MKT_THSR_UPMODE", wp.colStr("card_mode"), "2"));
  }

  // ************************************************************************
  void listWkdata() throws Exception {
    if (!wp.colStr("mode_desc").equals(wp.colStr("bef_mode_desc")))
      wp.colSet("opt_mode_desc", "Y");

    if (!wp.colStr("card_type_sel").equals(wp.colStr("bef_card_type_sel")))
      wp.colSet("opt_card_type_sel", "Y");
    commCardType("comm_card_type_sel");
    commCardType("comm_bef_card_type_sel");

    wp.colSet("bef_card_type_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_THSR_UPMODE", wp.colStr("card_mode"), "1"));
    if (!wp.colStr("card_type_sel_cnt").equals(wp.colStr("bef_card_type_sel_cnt")))
      wp.colSet("opt_card_type_sel_cnt", "Y");

    if (!wp.colStr("group_code_sel").equals(wp.colStr("bef_group_code_sel")))
      wp.colSet("opt_group_code_sel", "Y");
    commGroupCode("comm_group_code_sel");
    commGroupCode("comm_bef_group_code_sel");

    wp.colSet("bef_group_code_sel_cnt",
        listMktBnData("mkt_bn_data", "MKT_THSR_UPMODE", wp.colStr("card_mode"), "2"));
    if (!wp.colStr("group_code_sel_cnt").equals(wp.colStr("bef_group_code_sel_cnt")))
      wp.colSet("opt_group_code_sel_cnt", "Y");

    if (!wp.colStr("max_ticket_cnt").equals(wp.colStr("bef_max_ticket_cnt")))
      wp.colSet("opt_max_ticket_cnt", "Y");

    if (!wp.colStr("ticket_pnt_cond").equals(wp.colStr("bef_ticket_pnt_cond")))
      wp.colSet("opt_ticket_pnt_cond", "Y");

    if (!wp.colStr("ticket_pnt_cnt").equals(wp.colStr("bef_ticket_pnt_cnt")))
      wp.colSet("opt_ticket_pnt_cnt", "Y");

    if (!wp.colStr("ticket_pnt").equals(wp.colStr("bef_ticket_pnt")))
      wp.colSet("opt_ticket_pnt", "Y");

    if (!wp.colStr("ticket_amt_cond").equals(wp.colStr("bef_ticket_amt_cond")))
      wp.colSet("opt_ticket_amt_cond", "Y");

    if (!wp.colStr("ticket_amt_cnt").equals(wp.colStr("bef_ticket_amt_cnt")))
      wp.colSet("opt_ticket_amt_cnt", "Y");

    if (!wp.colStr("ticket_amt").equals(wp.colStr("bef_ticket_amt")))
      wp.colSet("opt_ticket_amt", "Y");

    if (!wp.colStr("ex_ticket_amt").equals(wp.colStr("bef_ex_ticket_amt")))
      wp.colSet("opt_ex_ticket_amt", "Y");

    if (wp.colStr("aud_type").equals("D")) {
      wp.colSet("mode_desc", "");
      wp.colSet("card_type_sel", "");
      wp.colSet("card_type_sel_cnt", "");
      wp.colSet("group_code_sel", "");
      wp.colSet("group_code_sel_cnt", "");
      wp.colSet("max_ticket_cnt", "");
      wp.colSet("ticket_pnt_cond", "");
      wp.colSet("ticket_pnt_cnt", "");
      wp.colSet("ticket_pnt", "");
      wp.colSet("ticket_amt_cond", "");
      wp.colSet("ticket_amt_cnt", "");
      wp.colSet("ticket_amt", "");
      wp.colSet("ex_ticket_amt", "");
    }
  }

  // ************************************************************************
  void listWkdataSpace() throws Exception {
    if (wp.colStr("mode_desc").length() == 0)
      wp.colSet("opt_mode_desc", "Y");

    if (wp.colStr("card_type_sel").length() == 0)
      wp.colSet("opt_card_type_sel", "Y");


    if (wp.colStr("group_code_sel").length() == 0)
      wp.colSet("opt_group_code_sel", "Y");


    if (wp.colStr("max_ticket_cnt").length() == 0)
      wp.colSet("opt_max_ticket_cnt", "Y");

    if (wp.colStr("ticket_pnt_cond").length() == 0)
      wp.colSet("opt_ticket_pnt_cond", "Y");

    if (wp.colStr("ticket_pnt_cnt").length() == 0)
      wp.colSet("opt_ticket_pnt_cnt", "Y");

    if (wp.colStr("ticket_pnt").length() == 0)
      wp.colSet("opt_ticket_pnt", "Y");

    if (wp.colStr("ticket_amt_cond").length() == 0)
      wp.colSet("opt_ticket_amt_cond", "Y");

    if (wp.colStr("ticket_amt_cnt").length() == 0)
      wp.colSet("opt_ticket_amt_cnt", "Y");

    if (wp.colStr("ticket_amt").length() == 0)
      wp.colSet("opt_ticket_amt", "Y");

    if (wp.colStr("ex_ticket_amt").length() == 0)
      wp.colSet("opt_ex_ticket_amt", "Y");

  }

  // ************************************************************************
  @Override
  public void dataProcess() throws Exception {
    int ilOk = 0;
    int ilErr = 0;
    int ilAuth = 0;
    mktp02.Mktp1040Func func = new mktp02.Mktp1040Func(wp);

    String[] lsCardMode = wp.itemBuff("card_mode");
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

      func.varsSet("card_mode", lsCardMode[rr]);
      func.varsSet("aud_type", lsAudType[rr]);
      func.varsSet("rowid", lsRowid[rr]);
      wp.itemSet("wprowid", lsRowid[rr]);
      if (lsAudType[rr].equals("A")) {
        rc = func.dbInsertA4();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      } else if (lsAudType[rr].equals("U")) {
        rc = func.dbUpdateU4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbInsertA4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      } else if (lsAudType[rr].equals("D")) {
        rc = func.dbDeleteD4();
        if (rc == 1)
          rc = func.dbDeleteD4Bndata();
        if (rc == 1)
          rc = func.dbDeleteD4TBndata();
      }

      log(func.getMsg());
      if (rc != 1)
        alertErr2(func.getMsg());
      if (rc == 1) {
        commCardType("comm_card_type_sel");
        commGroupCode("comm_group_code_sel");
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
  public void dddwSelect() {}

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
  public void commCardType(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "指定", "排除"};
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
  public void commGroupCode(String cde1) throws Exception {
    String[] cde = {"0", "1", "2"};
    String[] txt = {"全部", "指定", "排除"};
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
  String listMktBnData(String table, String tableName, String dataKey, String dataType) throws Exception {
    String sql1 = "select " + " count(*) as column_data_cnt " + " from " + table + " "
        + " where 1 = 1 " + " and   table_name = '" + tableName + "'" + " and   data_key   = '" + dataKey + "'"
        + " and   data_type  = '" + dataType + "'";
    sqlSelect(sql1);

    if (sqlRowNum > 0)
      return (sqlStr("column_data_cnt"));

    return ("0");
  }

  // ************************************************************************

}  // End of class
