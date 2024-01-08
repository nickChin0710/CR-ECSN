package cmsr03;
/** 
 * 19-1231:   Alex  fix order by
 * 19-1212:   Alex  queryWhere fix
 * 19-0617:   JH    p_xxx >>acno_p_xxx[xx]
 * 109-05-06  V1.00.04  Tanwei       updated for project coding standard
 * * 109-01-04  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
 * 109-07-13：	jiangyigndong	update sql query sentence
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Cmsr3130 extends BaseAction implements InfacePdf {
  String lsWhere1 = "", lsWhere2 = "";

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

    if (chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("查詢年月: 起迄錯誤");
      return;
    }

    if (wp.itemStr("ex_idno").length() < 8) {
      alertErr2("身分證字號至少8碼");
      return;
    }

//    lsWhere1 = " where 1=1 and A.card_no = B.card_no "
//        + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "B.purchase_date");

    lsWhere2 = " where 1=1 and A.card_no = B.card_no ";
//        + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "B.visit_date");
    if (!wp.itemEmpty("ex_date1")) {
    	lsWhere2 += sqlCol(wp.itemStr("ex_date1"), "B.visit_date", ">=");
    }
    if (!wp.itemEmpty("ex_date2")) {
    	lsWhere2 += sqlCol(wp.itemStr("ex_date2"), "B.visit_date", "<=");
    }
    if (wp.itemEq("ex_sup_flag", "Y")) {
//      lsWhere1 += " and A.major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
//          + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%") + ") ";
      lsWhere2 += " and A.major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%") + ") ";
    } else {
//      lsWhere1 += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
//          + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%") + ") ";
      lsWhere2 += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
          + sqlCol(wp.itemStr("ex_idno"), "id_no", "like%") + ") ";
    }


    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {

    if (wp.itemEmpty("ex_data_type")) {
//      wp.sqlCmd =  " select " + " '機場接送' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt	 ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and B.mcht_no in ('2289988600','2894038300','2331156902') " + " union all "
//          + " select " + " '機場停車' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and (B.mcht_no in ('785065526901','785065526902','785065526911','785065526912'"
//          + ",'785065526913','785065526921','785065526922','785065526923','785065526931','785065526932','785065526933') "
//          + " or B.mcht_no like '7850655269%' ) " + " union all " + " select "
//          + " '摩爾貴賓室' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and B.mcht_no in ('9494495101','9494495102','9494495103','9494495104'"
//          + ",'1324147401','1324147402','1324147403','1324147404'"
//          + ",'9972909301','9972909302','9972909303','9972909304'"
//          + ",'9972909201','9972909202','9972909203','9972909204'"
//          + ",'4261761001','4261761002','4261761003','4261761004') " + " union all "
//          + " select '新貴通' as tt_data_type , " + " B.card_no , " + " A.issue_date , "
//          + " '新貴通卡-機場貴賓室' as mcht_name , " + " B.visit_date as use_date , "
//          + " uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cnt , " + " B.crt_date , "
//          + " '' as mail_zip , " + " '' as mail_addr , " + " B.user_remark "
//          + " from crd_card A , cms_ppcard_visit B " + lsWhere2 + " order by 5 Asc "
		if (!wp.itemEmpty("ex_date1")) {
		  sqlCol(wp.itemStr("ex_date1"), "B.visit_date", ">=");
	    }
	    if (!wp.itemEmpty("ex_date2")) {
		  sqlCol(wp.itemStr("ex_date2"), "B.visit_date", "<=");
	    }
	    if (wp.itemEq("ex_sup_flag", "Y")) {
		  sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
	    } else {
	      sqlCol(wp.itemStr("ex_idno"), "id_no", "like%");
	    }
      wp.sqlCmd = " select '新貴通' as tt_data_type , " + " B.card_no , " + " A.issue_date , "
              + " '新貴通卡-機場貴賓室' as mcht_name , " + " B.visit_date as use_date , "
              + " uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cnt , " + " B.crt_date , "
              + " '' as mail_zip , " + " '' as mail_addr , " + " B.user_remark "
              + " from crd_card A , cms_ppcard_visit B " + lsWhere2 + "and VIP_KIND=1 and ERR_CODE= '00' "
              + " union all "
              + " select '龍騰貴賓室' as tt_data_type, B.card_no, A.issue_date, '龍騰卡-機場貴賓室' as mcht_name, "
              + " B.visit_date as use_date, uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cn, B.crt_date, "
              + " '' as mail_zip, '' as mail_addr, B.user_remark "
              + " from crd_card A, cms_ppcard_visit B " + lsWhere2 + "and VIP_KIND=2 and ERR_CODE= '00' order by 5 Asc ";

//    } else if (wp.itemEq("ex_data_type", "01")) {
//      wp.sqlCmd = " select " + " '機場接送' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt	 ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and B.mcht_no in ('2289988600','2894038300','2331156902') " + " order by 5 Asc ";
//    } else if (wp.itemEq("ex_data_type", "02")) {
//      wp.sqlCmd = " select " + " '機場停車' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and (B.mcht_no in ('785065526901','785065526902','785065526911','785065526912'"
//          + ",'785065526913','785065526921','785065526922','785065526923','785065526931','785065526932','785065526933') "
//          + " or B.mcht_no like '7850655269%' ) " + " order by 5 Asc ";
//    } else if (wp.itemEq("ex_data_type", "03")) {
//      wp.sqlCmd = " select " + " '摩爾貴賓室' as tt_data_type , " + " A.card_no ," + " A.issue_date ,"
//          + " B.product_name as mcht_name ," + " B.purchase_date as use_date ,"
//          + " B.qty as use_cnt ," + " '' as crt_date ," + " B.zip_code as mail_zip ,"
//          + " B.receive_address as mail_addr , " + " '' as user_remark "
//          + " from crd_card A , bil_contract B " + lsWhere1
//          + " and B.mcht_no in ('9494495101','9494495102','9494495103','9494495104'"
//          + ",'1324147401','1324147402','1324147403','1324147404'"
//          + ",'9972909301','9972909302','9972909303','9972909304'"
//          + ",'9972909201','9972909202','9972909203','9972909204'"
//          + ",'4261761001','4261761002','4261761003','4261761004') " + " order by 5 Asc ";
//    } else if (wp.itemEq("ex_data_type", "04")) {
//      wp.sqlCmd = " select " + " '新貴通' as tt_data_type , " + " B.card_no , " + " A.issue_date , "
//          + " '新貴通卡-機場貴賓室' as mcht_name , " + " B.visit_date as use_date , "
//          + " uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cnt , " + " B.crt_date , "
//          + " '' as mail_zip , " + " '' as mail_addr , " + " B.user_remark "
//          + " from crd_card A , cms_ppcard_visit B " + lsWhere2 + " order by 5 Asc ";
    } else if (wp.itemEq("ex_data_type", "11")) {
      wp.sqlCmd = " select " + " '新貴通' as tt_data_type , " + " B.card_no , " + " A.issue_date , "
          + " '新貴通卡-機場貴賓室' as mcht_name , " + " B.visit_date as use_date , "
          + " uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cnt , " + " B.crt_date , "
          + " '' as mail_zip , " + " '' as mail_addr , " + " B.user_remark "
          + " from crd_card A , cms_ppcard_visit B " + lsWhere2 + "and VIP_KIND=1 and ERR_CODE= '00' " + " order by 5 Asc ";
    } else if (wp.itemEq("ex_data_type", "10")) {
      wp.sqlCmd = " select " + " '龍騰貴賓室' as tt_data_type , " + " B.card_no , " + " A.issue_date , "
          + " '龍騰卡-機場貴賓室' as mcht_name , " + " B.visit_date as use_date , "
          + " uf_nvl(b.ch_visits,0)+uf_nvl(b.guests_count,0) as use_cnt , " + " B.crt_date , "
          + " '' as mail_zip , " + " '' as mail_addr , " + " B.user_remark "
          + " from crd_card A , cms_ppcard_visit B " + lsWhere2 + "and VIP_KIND=2 and ERR_CODE= '00' " + " order by 5 Asc ";
    }

    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);

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
    wp.reportId = "cmsr3130";
    wp.pageRows = 9999;
    String cond1 = "";
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "cmsr3130.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
