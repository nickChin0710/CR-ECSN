/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard  
* 110-11-12  V1.00.01  machao        20211109:bug处理   * 
******************************************************************************/
package rdsm01;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rdsr0030 extends BaseAction implements InfacePdf {

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("ex_groupcode");
      dddwList("d_dddw_groupcode", "ptr_group_code", "group_code", "group_name",
          "where group_code<>'0000' ");
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("異動日期: 起迄錯誤");
      return;
    }

    String lsWhere = "";
    lsWhere = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_type"), "rm_type")
        + sqlCol(wp.itemStr("ex_groupcode"), "cms_roadmaster.group_code")
        + sqlCol(wp.itemStr("ex_status"), "rm_status")
        + sqlCol(wp.itemStr("ex_date1"), "cms_roadmaster.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "cms_roadmaster.crt_date", "<=");
    

    sum(lsWhere);

    wp.whereStr = " where 1=1 " 
            + sqlCol(wp.itemStr("ex_type"), "rm_type")
            + sqlCol(wp.itemStr("ex_groupcode"), "cms_roadmaster.group_code")
            + sqlCol(wp.itemStr("ex_status"), "rm_status")
            + sqlCol(wp.itemStr("ex_date1"), "cms_roadmaster.crt_date", ">=")
            + sqlCol(wp.itemStr("ex_date2"), "cms_roadmaster.crt_date", "<=");
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void sum(String lsWhere) {
    String sql1 = " select " 
        + " count(*) as tl_cnt , " 
        + " sum(decode(rm_status,'1',1,0)) as tl_status1 , "
        + " sum(decode(rm_status,'2',1,0)) as tl_status2 , "
        + " sum(decode(rm_status,'0',1,0)) as tl_status0 " 
        + " from cms_roadmaster " 
        + lsWhere;
    sqlSelect(sql1);
    wp.colSet("tl_cnt", "" + sqlNum("tl_cnt"));
    wp.colSet("tl_status1", "" + sqlNum("tl_status1"));
    wp.colSet("tl_status2", "" + sqlNum("tl_status2"));
    wp.colSet("tl_status0", "" + sqlNum("tl_status0"));
    
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" 
        + " cms_roadmaster.card_no ," 
        + " crd_idno.chi_name ," 
        + " crd_idno.id_no ,"
        + " rm_type ," 
        + " rm_carno ," 
        + " cms_roadmaster.group_code ," 
        + " rm_carmanname ,"
        + " rm_carmanid ," 
        + " rm_status ," 
        + " rm_validdate ," 
        + " cms_roadmaster.crt_user ,"
        + " cms_roadmaster.mod_user ,"
        + " varchar_format(cms_roadmaster.mod_time, 'yyyy/mm/dd') as mod_time, "
        + " lpad(' ',20,' ') db_cname ," 
        + " lpad(' ',20,' ') db_idno ,"
        + " decode(rm_status,'1',1,0) as db_status1 , "
        + " decode(rm_status,'2',1,0) as db_status2 , "
        + " decode(rm_status,'0',1,0) as db_status0 ";
    wp.daoTable = "cms_roadmaster left join crd_card on cms_roadmaster.card_no = crd_card.card_no "
        + "left join crd_idno on crd_idno.id_p_seqno = crd_card.id_p_seqno ";
    wp.whereOrder = " order by cms_roadmaster.card_no ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    queryAfter();
    wp.setListCount(0);
    wp.setPageValue();

  }

  void queryAfter() {

    wp.logSql = false;

    for (int ii = 0; ii < wp.selectCnt; ii++) {

      if (wp.colEq(ii, "rm_status", "1")) {
        wp.colSet(ii, "tt_rm_status", "新增車號");
      } else if (wp.colEq(ii, "rm_status", "2")) {
        wp.colSet(ii, "tt_rm_status", "變更車號");
      } else if (wp.colEq(ii, "rm_status", "0")) {
        wp.colSet(ii, "tt_rm_status", "停用");
      } else if (wp.colEq(ii, "rm_status", "3")) {
        wp.colSet(ii, "tt_rm_status", "取消車號");
      } else if (wp.colEq(ii, "rm_status", "4")) {
        wp.colSet(ii, "tt_rm_status", "未啟用");
      }

      if (sqlRowNum <= 0)
        continue;
    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "rdsr0030";
    wp.pageRows = 9999;
    String cond1 = "";
    cond1 = "類別:";
    if (wp.itemEq("ex_type", "F")) {
      cond1 += "免費";
    } else if (wp.itemEq("ex_type", "E")) {
      cond1 += "自費";
    } else {
      cond1 += "全部";
    }
    cond1 += "  狀況:";
    if (wp.itemEmpty("ex_status")) {
      cond1 += "全部";
    } else if (wp.itemEq("ex_status", "1")) {
      cond1 += "新增車號";
    } else if (wp.itemEq("ex_status", "2")) {
      cond1 += "變更車號";
    } else if (wp.itemEq("ex_status", "0")) {
      cond1 += "停用";
    } else if (wp.itemEq("ex_status", "3")) {
      cond1 += "取消車號";
    } else if (wp.itemEq("ex_status", "4")) {
      cond1 += "未啟用";
    }
    cond1 += " 異動日期 : " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rdsr0030.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
