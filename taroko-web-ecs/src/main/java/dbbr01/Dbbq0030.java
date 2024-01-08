/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-07-16  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  yanghan  修改了變量名稱和方法名稱*      							
* 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  		
* 111-04-06  V1.00.03   ryan       增加查詢條件                                                                                              *  	
* 111-10-11  V1.00.04   Alex      增加畫面欄位顯示入帳日期:dbb_bill.post_date 授權碼:dbb_bill.auth_code , 增加篩選條件授權碼:ex_auth_code *
* 112-08-30  V1.00.05   JeffKung  增加入帳日期條件                                                        *
******************************************************************************/
package dbbr01;

import busi.SqlPrepare;
import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.base.CommString;
import taroko.com.TarokoCommon;


public class Dbbq0030 extends BaseProc {

  int rr = -1;
  String msg = "", msgok = "";
  String referenceNo = "";
  int isOk = 0;
  int errorNum = 0;
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void initPage() {

  }

  @Override
  public void dddwSelect() {

  }

  // for query use only
  private int getWhereStr() throws Exception {
    if (empty(wp.itemStr("ex_yymm")) && empty(wp.itemStr("ex_bill_type"))
        && empty(wp.itemStr("ex_batch_no")) && wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
      alertErr("帳戶帳號、卡號、請款單位、請款批號、關帳年月,至少輸入一個查詢條件");
      return -1;
    }

    wp.whereStr = " where 1=1 ";

    if (empty(wp.itemStr("ex_yymm")) == false) {
      wp.whereStr += " and B.acct_month like :ex_yymm ";
      setString("ex_yymm", wp.itemStr("ex_yymm") + "%");
    }

    if (empty(wp.itemStr("ex_bill_type")) == false) {
      wp.whereStr += " and B.bill_type like :ex_bill_type ";
      setString("ex_bill_type", wp.itemStr("ex_bill_type") + "%");
    }
    if (empty(wp.itemStr("ex_batch_no")) == false) {
      wp.whereStr += " and B.batch_no like :ex_batch_no ";
      setString("ex_batch_no", wp.itemStr("ex_batch_no") + "%");
    }
    
    /* TCB -- VD對帳單不會異動billed_date
    if (wp.itemStr("ex_flag").equals("1")) {
      wp.whereStr += " and B.billed_date = '' ";
      setString("ex_flag", wp.itemStr("ex_flag"));
    }
    if (wp.itemStr("ex_flag").equals("2")) {
      wp.whereStr += " and B.billed_date <> '' ";
      setString("ex_flag", wp.itemStr("ex_flag"));
    }
    */
    
    switch (wp.itemStr("ex_okflag")) {
      case "1":
        wp.whereStr += " and B.rsk_type = '' ";
        break;
      case "2":
        wp.whereStr += " and B.rsk_type <> '' ";
        break;
    }
    
    if (!wp.itemEmpty("ex_acct_key")) {
    	String exAcctKey = wp.itemStr("ex_acct_key");
        wp.whereStr += " and A.acct_key = :ex_acct_key ";
        setString("ex_acct_key", (exAcctKey.length() == 10)?exAcctKey+"0":exAcctKey);
    }
    
    if (!wp.itemEmpty("ex_card_no")) {
        wp.whereStr += " and B.card_no = :ex_card_no ";
        setString("ex_card_no", wp.itemStr("ex_card_no"));
    }
    
    if (!wp.itemEmpty("ex_purchase_date1")) {
    	wp.whereStr += " and B.purchase_date >= :ex_purchase_date1 ";
        setString("ex_purchase_date1", wp.itemStr("ex_purchase_date1"));
      }

    if (!wp.itemEmpty("ex_purchase_date2")) {
    	wp.whereStr += " and B.purchase_date <= :ex_purchase_date2 ";
        setString("ex_purchase_date2", wp.itemStr("ex_purchase_date2"));
    }
    
    //--V1.00.04 增加授權碼篩選條件 , 使用授權碼篩選條件時 , 帳戶帳號或卡號必須輸入其中一個才可以使用授權碼查詢
    if(!wp.itemEmpty("ex_auth_code")) {
    	if(wp.itemEmpty("ex_acct_key") && wp.itemEmpty("ex_card_no")) {
    		alertErr("輸入授權碼查詢時 , 帳戶帳號或卡號必須輸入其中一個才可進行查詢");
    		return -1;
    	}
    	wp.whereStr += " and B.auth_code =:ex_auth_code ";
    	setString("ex_auth_code",wp.itemStr("ex_auth_code"));
    }
    
    if (!wp.itemEmpty("ex_post_date1")) {
    	wp.whereStr += " and B.post_date >= :ex_post_date1 ";
        setString("ex_post_date1", wp.itemStr("ex_post_date1"));
      }

    if (!wp.itemEmpty("ex_post_date2")) {
    	wp.whereStr += " and B.post_date <= :ex_post_date2 ";
        setString("ex_post_date2", wp.itemStr("ex_post_date2"));
    }
    
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL =
            " card_no," + " B.reference_no, " + " B.purchase_date, " + " B.source_amt, " + " decode(B.mcht_chi_name,'',B.mcht_eng_name,B.mcht_chi_name) db_mcht_name , "
                + " B.mcht_city, " + " B.mcht_country, " + " B.dest_amt, " + " B.bill_type, " + " B.rsk_type, "
                + " B.acct_type, " + " B.acct_month, " + " B.batch_no, " + " B.txn_code, " + " B.acct_code , "
                + " B.post_date, " + " B.auth_code "; //--V1.00.04 增加顯示欄位入帳日期、授權碼
    wp.daoTable = " dbb_bill B left join dba_acno A on B.p_seqno = A.p_seqno";
    wp.whereOrder = "  ";
    if (getWhereStr() != 1)
      return;
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();
  }


  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    referenceNo = wp.itemStr("data_k1");

    wp.selectSQL = " card_no," + " reference_no, " + " purchase_date, " + " source_amt, "
        + " mcht_chi_name, " + " mcht_city, " + " mcht_country, " + " dest_amt, " + " bill_type, "
        + " rsk_type, " + " acct_type, " + " acct_month, " + " batch_no, " + " txn_code, "
        + " acct_code, " + " mcht_no, " + " mcht_eng_name, " + " mcht_state, " + " mcht_zip, "
        + " mcht_category," + " dest_curr," + " film_no, " + " auth_code," + " post_date,"
        + " source_curr," + " exchange_rate, " + " process_date, " + " acq_member_id, "
        + " pos_entry_mode, " + " settl_amt ";
    wp.daoTable = " dbb_bill ";
    wp.whereOrder = "  ";
    wp.whereStr = "where 1=1 and reference_no = :reference_no ";
    setString("reference_no", referenceNo);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + referenceNo);
      return;
    }
    // list_wkdata();
  }

  @Override
  public void dataProcess() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  void listWkdata() throws Exception {
    String acctCode = "";
    double amt = 0, tolDestAmt = 0, amt2 = 0, tolDestAmt2 = 0;
    String[] txn_code = {"06", "17", "29", "25", "27", "28"};
    for (int i = 0; i < wp.selectCnt; i++) {
      acctCode = wp.colStr(i, "txn_code");
      amt = wp.colNum(i, "dest_amt");
      for (int r = 0; r < txn_code.length; r++) {
        if (acctCode.equals(txn_code[r])) {
          amt = 0 - amt;
          break;
        }
      }
      tolDestAmt += amt;

      acctCode = strMid(wp.colStr(i, "acct_code"), 0, 2);
      amt2 = wp.colNum(i, "dest_amt");
      if (!acctCode.equals("ID")) {
        amt2 = 0;
      }
      tolDestAmt2 += amt2;

      acctCode = strMid(wp.colStr(i, "bill_type"), 0, 2);
      wp.colSet(i, "bill_type", acctCode);

    }
    wp.colSet("tol_dest_amt", tolDestAmt);
    wp.colSet("tol_dest_amt2", tolDestAmt2);

  }
}
