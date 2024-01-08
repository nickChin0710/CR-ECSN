/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/03  V1.00.01   Allen Ho      Initial                              *
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change           
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *  
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 110/11/11  V1.00.04  jiangyingdong       sql injection                   *                                                            *
***************************************************************************/
package mktm02;

import mktm02.Mktm6248Func;
import ofcapp.AppMsg;
import java.util.Arrays;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6248 extends BaseEdit {
  private String PROGNAME = "深呆戶卡片等級維護處理程式108/09/03 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm6248Func func = null;
  String rowid, dataKK2, dataKK3;
  String orgTabName = "mkt_bn_data";
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
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        strAction = "A";
        insertFunc();
        break;
      case "U":
        /* 更新功能 */
        strAction = "U";
        updateFunc();
        break;
      case "D":
        /* 刪除功能 */
        deleteFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxFunc2();
        break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = "WHERE 1=1 " + " and table_name  =  'MKT_FSTP_PARM_DEEP' "
        + " and data_key  =  'DEEP_CLASS_CODE' ";

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
        + "a.data_type," + "a.data_code," + "a.data_code2," + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by data_type,data_code desc,data_code2";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCardNote("comm_data_code");
    commCardType("comm_data_code2");
    commCrtuser("comm_crt_user");
    commDataType("comm_data_type");

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
      if (wp.itemStr("kk_data_type").length() == 0) {
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
        + "a.data_type as data_type," + "a.data_code as data_code," + "a.data_code2 as data_code2,"
        + "a.crt_user," + "a.crt_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
      wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_data_type"), "a.data_type")
          + sqlCol(wp.itemStr("kk_data_code"), "a.data_code")
          + sqlCol(wp.itemStr("kk_data_code2"), "a.data_code2");
    } else if (qFrom == 1) {
      wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr2("查無資料, key= " + "[" + rowid + "]" + "[" + dataKK2 + "]" + "[" + dataKK3 + "]");
      return;
    }
    commCardNote("comm_data_code");
    commCrtuser("comm_crt_user");
    commCardType("comm_data_code2");
    checkButtonOff();
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")))
      return;

    mktm02.Mktm6248Func func = new mktm02.Mktm6248Func(wp);

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
      if ((wp.respHtml.equals("mktm6248_detl"))) {
        wp.initOption = "--";
        wp.optionKey = "";
        if (wp.colStr("kk_data_code").length() > 0) {
          wp.optionKey = wp.colStr("kk_data_code");
        }
        if (wp.colStr("data_code").length() > 0) {
          wp.initOption = "--";
        }
        this.dddwList("dddw_lost_code4", "ptr_sys_idtab", "trim(wf_id)", "trim(wf_desc)",
            " where wf_type= 'CARD_NOTE'");
        wp.initOption = "--";
        wp.optionKey = "";
        lsSql = "";
        lsSql = procDynamicDddwCardType(wp.itemStr("kk_data_code"));

        dddwList("dddw_card_type", lsSql);

      }
    } catch (Exception ex) {
    }
  }

  // ************************************************************************
  public void commCardNote(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " wf_desc as column_wf_desc " + " from ptr_sys_idtab " + " where 1 = 1 "
          + " and   wf_id = ? " + " and   wf_type = 'CARD_NOTE' ";
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_wf_desc");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commCardType(String columnData1) throws Exception {
    String columnData = "";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      columnData = "";
      sql1 = "select " + " name as column_name " + " from ptr_card_type " + " where 1 = 1 "
          + " and   card_type = ? ";
      if (wp.colStr(ii, "data_code2").length() == 0)
        continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"data_code2") });

      if (sqlRowNum > 0) {
        columnData = columnData + sqlStr("column_name");
        wp.colSet(ii, columnData1, columnData);
      }
    }
    return;
  }

  // ************************************************************************
  public void commDataType(String cde1) throws Exception {
    String[] cde = {"1", "2", "3", "4", "5", "6", "7", "8"};
    String[] txt = {"等級1", "等級2", "等級3", "等級4", "等級5", "等級6", "等級7", "等級8"};
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
  public void wfAjaxFunc2() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change
    String ajaxjDataCode2 = "";
    // super.wp = wr; // 20200102 updated for archit. change

    selectAjaxFunc20(wp.itemStr("ax_win_data_code"));

    if (rc != 1) {
      wp.addJSON("ajaxj_data_code2", "");
      wp.addJSON("ajaxj_name", "");
      return;
    }

    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_data_code2", sqlStr(ii, "data_code2"));
    for (int ii = 0; ii < sqlRowNum; ii++)
      wp.addJSON("ajaxj_name", sqlStr(ii, "name"));
  }

  // ************************************************************************
  void selectAjaxFunc20(String cardNote) {
    wp.sqlCmd = " select " + " '--' as data_code2,  " + " '' as name  " + " from  ptr_businday "
        + " union " + " select " + " card_type as data_code2," + " name " + " from  ptr_card_type ";
    if (cardNote.length() > 0)
      wp.sqlCmd = wp.sqlCmd + " where card_note  = ? ";

    this.sqlSelect(new Object[] { cardNote });
    if (sqlRowNum <= 0)
      alertErr2("卡片等級:[" + cardNote + "]查無資料");

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
  String procDynamicDddwCardType(String cardNote) throws Exception {
    String lsSql = "";

    lsSql = " select " + " card_type as db_code, " + " card_type||' '||name as db_desc "
        + " from ptr_card_type ";

    if (cardNote.length() > 0)
      lsSql = lsSql + sqlCol(cardNote, "card_note");
//    lsSql = lsSql + " where card_note = '" + cardNote + "' ";
    lsSql = lsSql + " order by card_type ";

    return lsSql;
  }
  // ************************************************************************
//************************************************************************
public void commCrtuser(String columnData1) throws Exception 
{
 String columnData="";
 String sql1 = "";
  for (int ii = 0; ii < wp.selectCnt; ii++)
     {
      columnData="";
      sql1 = "select "
           + " usr_cname as column_usr_cname "
           + " from sec_user "
           + " where 1 = 1 "
           + " and   usr_id = ? "
           ;
      if (wp.colStr(ii,"crt_user").length()==0) continue;
      sqlSelect(sql1, new Object[] { wp.colStr(ii,"crt_user") });

      if (sqlRowNum>0)
         columnData = columnData + sqlStr("column_usr_cname"); 
      wp.colSet(ii, columnData1, columnData);
     }
  return;
}

} // End of class
