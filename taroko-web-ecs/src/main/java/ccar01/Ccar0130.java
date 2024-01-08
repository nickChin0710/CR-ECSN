package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
*/

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.base.CommString;
import taroko.com.TarokoPDF;

public class Ccar0130 extends BaseAction implements InfacePdf {

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
      alertErr2("異動日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 and log_mode ='1' and kind_flag ='A' ";
    if (wp.itemEmpty("ex_date1") == false) {
    	lsWhere += " and :ex_date1 <= log_date ";
    	setString("ex_date1", wp.itemStr("ex_date1"));
	}
    if (wp.itemEmpty("ex_date2") == false) {
    	lsWhere += " and  log_date <= :ex_date2 ";
    	setString("ex_date2", wp.itemStr("ex_date2"));
	}
    if (wp.itemEmpty("ex_user") == false) {
    	lsWhere += " and mod_user = :mod_user ";
    	setString("mod_user", wp.itemStr("ex_user"));
	}
//        + sqlCol(wp.itemStr("ex_date1"), "log_date", ">=")
//        + sqlCol(wp.itemStr("ex_date2"), "log_date", "<=")
//        + sqlCol(wp.itemStr("ex_user"), "mod_user");

    if (wp.itemEq("ex_type", "1")) {
		lsWhere += " and log_type ='A' ";
		if (wp.itemEmpty("ex_class_code") == false) {
			lsWhere += " and class_code_aft = :class_code_aft ";
			setString("class_code_aft", wp.itemStr("ex_class_code"));
		}
//      lsWhere += sqlCol(wp.itemStr("ex_class_code"), "class_code_aft");
    } else {
      lsWhere += " and log_type ='B' ";
    }

    if (!wp.itemEmpty("ex_idno")) {
		lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1  and id_no = :id_no ) ";
		setString("id_no", wp.itemStr("ex_idno"));
//    + sqlCol(wp.itemStr("ex_idno"), "id_no") + ")" ;
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    String lsType = wp.itemStr("ex_type");
    wp.selectSQL =
                " acct_type , " + " decode(:type,'1',class_code_bef,ccas_mcode_bef) as db_code_bef , "
            + " decode(:type,'1',class_code_aft,ccas_mcode_aft) as db_code_aft , "
            + " log_date as mod_date , " + " to_char(mod_time,'hh24miss') as mod_time , "
            + " mod_user , " + " uf_idno_id(id_p_seqno) as id_no , "
            + " uf_acno_key2(card_no,acct_type) as acct_key "
            ;
    setString("type", lsType);
    wp.daoTable = " rsk_acnolog ";
    wp.whereOrder = "  order by acct_type , 4 Desc , 5 Desc ";

    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkdata();
    wp.setListCount(1);
    wp.setPageValue();

  }

  void listWkdata() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "-" + wp.colStr(ii, "acct_key"));
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
    wp.colSet("ex_date2", this.getSysDate());

  }

  @Override
  public void pdfPrint() throws Exception {
    wp.reportId = "Ccar0130";
    String type = "";
    if (wp.itemEq("ex_type", "1")) {
      type = "1.卡戶等級";
    } else if (wp.itemEq("ex_type", "2")) {
      type = "2.Curr Mcode";
    }
    String cond1 = "異動日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2")) + " 類別 :" + type;;
    wp.colSet("cond1", cond1);
    wp.pageRows = 9999;
    queryFunc();

    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "ccar0130.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;

  }

}
