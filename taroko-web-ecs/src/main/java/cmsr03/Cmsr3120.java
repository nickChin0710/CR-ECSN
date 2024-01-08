/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
 * 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package cmsr03;
/*
 * * 19-1213:   Alex  fix queryRead
 * * 19-0617:   JH    p_xxx >>acno_p_xxx
 * * 20-0115:   Ru    add vip_kind
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr3120 extends BaseAction implements InfacePdf {

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
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("發卡日期: 起迄錯誤");
      return;
    }

    String lsWhere = "";
    lsWhere =
        " where 1=1 and A.current_code ='0' " + sqlCol(wp.itemStr("ex_bin_type"), "A.bin_type")
            + sqlBetween("ex_date1", "ex_date2", "A.issue_date")
            + sqlCol(wp.itemStr("ex_vip_kind"), "A.vip_kind");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();
    wp.selectSQL = "" + " A.bin_type ," + " A.pp_card_no ," + " A.eng_name "
        + ", B.id_no, B.chi_name as db_chi_name" + ", uf_hi_cname(B.chi_name) as hh_name"
        + ", uf_hi_cardno(A.pp_card_no) as hh_card_no" + ", uf_hi_idno(B.id_no) as hh_idno"
        + ", A.vip_kind";
    wp.daoTable = " crd_card_pp A join crd_idno B on B.id_p_seqno=A.id_p_seqno ";
    pageQuery2(null);

    if (sqlRowNum < 0) {
      alertErr2("此條件查無資料");
      return;
    } else {
      for (int i = 0; i < sqlRowNum; i++) {
        // 貴賓卡
        if ("1".equals(wp.colStr(i, "vip_kind"))) {
          wp.colSet(i, "vip_kind", "1_新貴通");
        } else if ("2".equals(wp.colStr("vip_kind"))) {
          wp.colSet(i, "vip_kind", "2_龍騰卡");
        }
      }
    }

    wp.setListCount(0);
    // wp.setPageValue();

  }

  @Override
  public boolean rowIsShow(int ll) throws Exception {

    if (checkCode1(ll) > 0)
      return false;

    if (checkCode2(ll) > 0)
      return false;

    return true;
  }

  int checkCode1(int ii) {
    int llOk = 0;
    String lsId = "";
    lsId = wp.colStr(ii, "id_no");

    String sql1 =
        " select sum(decode(current_code,'0',1,0)) as db_cnt1 , max(oppost_date) as db_max_date "
            + " from crd_card "
            + " where id_p_seqno in (select id_p_seqno from crd_idno where id_no = ?) "
            + " and (card_type,group_code) in (select card_type , group_code from mkt_ppcard_apply where 1=1) ";

    sqlSelect(sql1, new Object[] {lsId});
    if (sqlRowNum < 0) {
      errmsg("select crd_card error");
      return -1;
    }

    llOk = sqlInt("db_cnt1");

    if (llOk == 0) {
      wp.colSet(ii, "db_ab_reason", "1.無有效信用卡");
      wp.colSet(ii, "db_max_oppost_date", sqlStr("db_max_date"));
    }

    return llOk;
  }

  int checkCode2(int ii) {
    int llOk = 0, llCnt = 0;
    String lsId = "";
    lsId = wp.colStr(ii, "id_no");
    String sql1 = " select count(*) as db_cnt2 , sum(decode(current_code,'0',1,0)) as db_cnt3 , "
        + " max(oppost_date) as db_max_date2 " + " from crd_card where card_no in "
        + " (select major_card_no from crd_card where sup_flag = '1' "
        + " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = ?)"
        + " and (card_type,group_code) in (select card_type , group_code from mkt_ppcard_apply where 1=1)) ";

    sqlSelect(sql1, new Object[] {lsId});

    if (sqlRowNum < 0) {
      errmsg("select crd_card error");
      return -1;
    }

    llCnt = sqlInt("db_cnt2");
    llOk = sqlInt("db_cnt3");

    if (llCnt == 0)
      return 0;

    if (llCnt > 0 && llOk == 0) {
      wp.colSet(ii, "db_ab_reason", "2.無有效正卡");
      wp.colSet(ii, "db_max_oppost_date", sqlStr("db_max_date2"));
      return -1;
    }


    return 1;
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
    wp.reportId = "cmsr3120";
    wp.pageRows = 9999;
    String cond1 = "";
    cond1 += "發卡日期:" + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));

    if (wp.itemEmpty("ex_bin_type")) {
      cond1 += " 卡種:全部";
    } else if (wp.itemEq("ex_bin_type", "V")) {
      cond1 += " 卡種:VISA";
    } else if (wp.itemEq("ex_bin_type", "M")) {
      cond1 += " 卡種:MasterCard";
    } else if (wp.itemEq("ex_bin_type", "J")) {
      cond1 += " 卡種:JCB";
    }

    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr3120.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
