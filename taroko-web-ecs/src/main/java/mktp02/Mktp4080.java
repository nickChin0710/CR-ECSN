/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 109-06-22  V1.00.02 shiyuqi      讀取table : ptr_branch 改為gen_brn           *
******************************************************************************/
package mktp02;

import mktm02.Mktm4080Func;
import ofcapp.BaseAction;

public class Mktp4080 extends BaseAction {

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
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "mktp4080")) {
        wp.optionKey = wp.colStr("ex_acct_type");
        dddwList("dddw_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
        wp.optionKey = wp.colStr("ex_bank_no");
        dddwList("dddw_bank_no", "gen_brn", "branch", "brief_chi_name", "where 1=1");
      }

    } catch (Exception ex) {
    }

  }

  @Override
	public void queryFunc() throws Exception {
		// 判斷前端表單傳入的數據是否爲空
		if (wp.itemEmpty("ex_date1")) {
			alertErr2("篩選年費年月:不可空白!");
			return;
		}

		wp.whereStr = " where 1=1 ";

		if (!empty(wp.itemStr("ex_date1"))) {
			wp.whereStr += "and A.card_fee_date = :ex_date1 ";
			setString("ex_date1", wp.itemStr("ex_date1"));
		}

		if (!empty(wp.itemStr("ex_reg_bank_no"))) {
			wp.whereStr += "and A.reg_bank_no = :reg_bank_no ";
			setString("reg_bank_no", wp.itemStr("ex_reg_bank_no"));
		}

		if (!empty(wp.itemStr("ex_group_code"))) {
			wp.whereStr += "and A.group_code = :ex_group_code ";
			setString("ex_group_code", wp.itemStr("ex_group_code"));
		}

		if (!empty(wp.itemStr("ex_card_type"))) {
			wp.whereStr += "and A.card_type = :ex_card_type ";
			setString("ex_card_type", wp.itemStr("ex_card_type"));
		}

		if (!empty(wp.itemStr("ex_id_no"))) {
			wp.whereStr += "and A.id_p_seqno = (select id_p_seqno from crd_idno where id_no = :ex_id_no ) ";
			setString("ex_id_no", wp.itemStr("ex_id_no"));
		}

		if (!empty(wp.itemStr("ex_card_no"))) {
			wp.whereStr += "and A.card_no = :ex_card_no ";
			setString("ex_card_no", wp.itemStr("ex_card_no"));
		}

		wp.whereStr += "and A.apr_date = '' ";
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();

		queryRead();

	}

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    
    wp.selectSQL = "" 
    		+ "A.fee_date, "
    		+ "A.card_fee_date, "
			+ "A.reg_bank_no, "
			+ "A.issue_date, "
			+ "A.org_annual_fee, A.rcv_annual_fee, "
			+ "A.group_code, "
			+ "A.card_no, "
			+ "I.id_no, "
			+ "A.card_type, "
			+ "I.chi_name, "
			+ "A.reason_code, "
			+ "A.purch_review_month_beg||'~'||A.purch_review_month_end as acct_month_between, "
			+ "A.card_pur_cnt, "
			+ "A.card_pur_amt, "
			+ "A.sum_pur_cnt, "
			+ "A.sum_pur_amt, "
			+ "CONCAT(CONCAT(I.home_area_code1, I.home_tel_no1), I.home_tel_ext1) home_phone, "
			+ "CONCAT(CONCAT(I.office_area_code1, I.office_tel_no1), I.office_tel_ext1) office_phone, "
			+ "I.cellar_phone ";
    wp.daoTable = " cyc_afee A left join crd_idno I ON A.id_p_seqno = I.id_p_seqno";
    
    wp.whereOrder = "";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();

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
    int ilOk = 0, ilErr = 0;

    mktp02.Mktp4080Func func = new mktp02.Mktp4080Func();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt");
    String[] aaCardNo = wp.itemBuff("card_no");
    String[] aaFeeDate = wp.itemBuff("fee_date");
    String[] aaReasonCode = wp.itemBuff("reason_code");

    wp.listCount[0] = wp.itemRows("card_no");

    int rr = -1;
    rr = optToIndex(aaOpt[0]);

    if (rr < 0) {
      alertErr2("請點選欲覆核資料");
      return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("card_no", aaCardNo[rr]);
      func.varsSet("fee_date", aaFeeDate[rr]);
      func.varsSet("reason_code", aaReasonCode[rr]);
      

      rc = func.dataProc();

      if (rc == 1) {
        sqlCommit(rc);
        wp.colSet(rr, "ok_flag", "V");
        ilOk++;
        continue;
      }
      dbRollback();
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
    }

    alertMsg("覆核完成: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
