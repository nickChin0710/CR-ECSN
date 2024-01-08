/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-11  V1.00.00  yash       program initial                            *
* 108-07-01  V1.00.01  Amber      Update                                     *
* 108-08-26  V1.00.02  Amber      Update  _add               			     *
* 108-11-29  V1.00.03  Amber	  Update init_button  Authority 			 *
* 109-01-06  V1.00.04  Ru Chebn   Modify AJAX                                *
* 109-04-23  V1.00.05  shiyuqi       updated for project coding standard     * 
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名                                                                                      *  
* 111-12-08  V1.00.06  Ryan       修改為需要線上覆核才可做更動                                                                    *  
******************************************************************************/

package bilm01;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilm0180 extends BaseEdit {
  String mExCardNo = "";
  // String kk_card_no = "";
  String bilAssignInstallmentName = "", lsTmp = "", lsWhere = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
       deleteFunc();
//      dataDelete();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "new";
      clearFunc();
//    } else if (eqIgno(wp.buttonCode, "R2")) {
//      /* card read */
//      strAction = "R2";
//      itemchanged();
//    }
    // else if (eq_igno(wp.buttonCode, "S2")) {
    // /* 存檔 */
    // is_action = "S2";
    // saveFunc2();
    // }
    }else if (eqIgno(wp.buttonCode, "AJAX")) {
      // 20200106 modify AJAX
      itemchanged();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exIdNo = wp.itemStr("ex_id_no");
    String exCradNo = wp.itemStr("ex_crad_no");

    wp.whereStr = " where 1=1 ";

    if (empty(exIdNo) && empty(exCradNo)) {
      alertErr("請輸入至少一項查詢條件!!");
      return false;
    }

    if (empty(wp.itemStr("ex_id_no")) == false) {
      wp.whereStr += " and  i.id_no = :ID_NO ";
      setString("ID_NO", wp.itemStr("ex_id_no"));
    }

    if (empty(wp.itemStr("ex_crad_no")) == false) {
      wp.whereStr += " and  b.card_no = :card_no ";
      setString("card_no", wp.itemStr("ex_crad_no"));
    }

    // if (empty(wp.item_ss("ex_id_no")) && empty(wp.item_ss("ex_crad_no"))) {
    // errmsg("請輸入身分證字號!");
    // return false;
    // }

//    if (wp.itemStr("ex_apr_flag").equals("U")) {
//      lsTmp = " 'U' as db_tmp ";
//      bilAssignInstallmentName = "bil_assign_installment_t ";
//    } else {
//      lsTmp = " 'Y' as db_tmp ";
      bilAssignInstallmentName = "bil_assign_installment ";
//    }

    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    if (!getWhereStr()) {

      return;
    } ;
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    if (getWhereStr() == false)
      return;

    // select columns
    wp.selectSQL = "hex(b.rowid) as rowid, " + "b.card_no," + "i.chi_name," + "i.id_no,"
        + "i.id_p_seqno," + "b.start_date," + "b.end_date," + "b.amt_from," + "b.installment_term,"
        + "decode(b.break_flag,'','N','1','Y','2','Y','3','Y','4','Y','5','Y',b.break_flag) as break_flag ,"
        + "b.break_date," + "b.mod_user," + "uf_2ymd(b.mod_time) as mod_date," + "b.apr_user,"
        + "b.apr_date," + "b.reserve_type," + "hex(b.rowid) as rowid, b.mod_seqno " ;

    wp.daoTable = bilAssignInstallmentName + " b left join crd_card c on b.card_no=c.card_no "
        + "                               left join crd_idno i on c.major_id_p_seqno=i.id_p_seqno";

    wp.whereOrder = "order by  b.start_date";

    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    listWkdata();

  }

  void listWkdata() throws Exception {
    String reserveType = "";

		for (int ii = 0; ii < wp.selectCnt; ii++) {
			// wk_reserve_type 預約類別
			reserveType = wp.colStr(ii, "reserve_type");
			
			if (reserveType.equals("1")) {
		        reserveType = "稅款(排除綜所稅)";
		    } else if (reserveType.equals("2")) {
		        reserveType = "綜所稅";
		    } else if (reserveType.equals("3")) {
		        reserveType = "學雜費";
		    } else if (reserveType.equals("4")) {
		          reserveType = "保費";
		    }

			wp.colSet(ii, "reserve_type", reserveType);
		}
  }

  @Override
  public void querySelect() throws Exception {
    mExCardNo = wp.itemStr("CARD_NO");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    mExCardNo = itemKk("data_k1");
//    if (itemKk("data_k2").equals("U")) {
//      bilAssignInstallmentName = "bil_assign_installment_t";
//    } else {
      bilAssignInstallmentName = "bil_assign_installment";
//    }


    // String lsSql = "select count(*) as tot_cnt from bil_assign_installment_t where card_no = ? ";
    // Object[] param = new Object[] { m_ex_card_no };
    // sqlSelect(lsSql, param);
    // if (sql_num("tot_cnt") > 0) {
    // bil_assign_installment_name = "bil_assign_installment_t";
    // } else {
    // bil_assign_installment_name = "bil_assign_installment";
    // }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno " + ",card_no " + ",start_date" + ",end_date"
        + ",amt_from" + ",installment_term" + ",reserve_type " + ",break_flag as kk_break_flag"
        + ",decode(break_flag,'','N','1','Y','2','Y','3','Y','4','Y','5','Y',break_flag) as break_flag "
        + ",break_date" + ",crt_user" + ",crt_date" + ",apr_user" + ",apr_date" + ",mod_user"
        + ",uf_2ymd(mod_time) as mod_date" + ",mod_pgm";

//    if (bilAssignInstallmentName.equals("bil_assign_installment_t")) {
//      wp.selectSQL += ", 'U.更新待覆核' as apr_flag";
//    } else if (bilAssignInstallmentName.equals("bil_assign_installment")) {
//      wp.selectSQL += ", 'Y.未異動' as apr_flag";
//    }

    wp.daoTable = bilAssignInstallmentName;
    wp.whereStr = " where 1=1 ";
    // wp.whereStr += " and card_no = :CARD_NO ";
    wp.whereStr += " and  hex(rowid) = :rowid ";
    // setString("CARD_NO", m_ex_card_no);
    setString("rowid", itemKk("data_k3"));

    pageSelect();

    if (wp.colStr("break_flag").equals("N")) {
      wp.colSet("break_flag", "");
    }

    // if(wp.col_ss("break_flag").equals("N")){
    // wp.col_set("kk_break_flag", "N");
    // wp.col_set("break_flag", "");
    // }else if(wp.col_ss("break_flag").equals("Y")){
    // wp.col_set("kk_break_flag", "Y");
    // wp.col_set("break_flag", "1");
    // }

    // if(wp.col_ss("kk_break_flag").equals("")){
    // System.out.println("kk_break_flag="+"empty");
    // }else {
    // System.out.println("kk_break_flag="+wp.col_ss("kk_break_flag"));
    // }
    if (sqlNotFind()) {
      alertErr("查無資料, CARD_NO=" + mExCardNo);
    }
    wp.colSet("kk_card_no", mExCardNo);
  }

  @Override
  public void saveFunc() throws Exception {
		// -check approve-
	if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
		return;
	}
    Bilm0180Func func = new Bilm0180Func(wp);
    if (!strAction.equals("D")) {

      if (!wp.itemStr("break_flag").equals("")) {
        wp.colSet("break_flag", "Y");
      }


      if (!wp.itemStr("break_flag").equals("1")) {
        if (wp.itemNum("start_date") < toNum(getSysDate())) {
          alertErr("消費起日不可小於系統日!");

          return;
        }

        if (wp.itemNum("start_date") >= wp.itemNum("end_date")) {
          alertErr("消費迄日不能小於消費起日!");
          return;
        }
      }

      if (empty(wp.itemStr("amt_from")) || wp.itemStr("amt_from").equals("0")) {
        alertErr("金額 不可小於0 ! ");
        return;
      }

      if (empty(wp.itemStr("break_flag"))) {
        if (!empty(wp.itemStr("break_date"))) {
          alertErr("終止旗標為N，終止日期應為空白");
          return;
        }
      }
      if (!empty(wp.itemStr("break_flag"))) {
        if (empty(wp.itemStr("break_date"))) {
          wp.colSet("break_flag", "Y");
          alertErr("終止旗標為Y，請輸入終止日期");
          return;

        }
      }
      if (empty(wp.itemStr("break_date"))) {
        if (!empty(wp.itemStr("break_flag"))) {
          alertErr("終止日期空白，終止旗標應選N");
          return;
        }
      }
      if (!empty(wp.itemStr("break_date"))) {
        if (empty(wp.itemStr("break_flag"))) {
          alertErr("終止日期有值，終止旗標應選Y");
          return;
        }
        if (wp.itemNum("break_date") < commString.strToNum(getSysDate())) {
          wp.colSet("break_flag", "Y");
          alertErr("終止日期 不可小於系統日期");
          return;
        }
      }

      String lsIdPSeqno = "";
      String lsSql = " select id_p_seqno from crd_idno where id_no=:id_no ";
      setString("id_no", wp.itemStr("id_no"));
      sqlSelect(lsSql);
      lsIdPSeqno = sqlStr("id_p_seqno");

      if (chackExist() != 1) {
        alertErr("有效期間 重疊 ");
        return;
      }

      // if (chackTerm(wp.item_ss("installment_term")) <= 0) {
      // alert_err("分期期數不存在!");
      // return;
      // }

      if (chackAmt(wp.itemStr("amt_from")) <= 0) {
        alertErr("與參數金額不符!");
        return;
      }

    }



    // String chackno = chackCardno(wp.item_ss("card_no"), ls_id_p_seqno);
    // String chackno = chackCardno(wp.item_ss("card_no"));

    // switch (chackno) {
    // case "0":
    //
    // alert_err("此卡號與身分證號不符! ");
    // return;
    // case "2":
    //
    // alert_err("無效卡! ");
    // return;
    //
    // case "3":
    //
    // alert_err("此團體代號不可指定! ");
    // return;
    //
    // }
//
//    if (strAction.equals("U")) {
//
//      if (wp.itemStr("apr_flag").equals("Y.未異動")) {
//        strAction = "A";
//      }
//    }

    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  // public void saveFunc2() throws Exception {
  //
  // Bilm0180Func func = new Bilm0180Func(wp);
  // String chackno = "";
  // int rowcntaa = 0;
  // // if(empty(wp.item_ss("ex_id_no"))){
  // //
  // // alert_msg("身分證不可空白 !");
  // // return;
  // // }
  //
  // String[] aa_card_no = wp.item_buff("card_no");
  // String[] aa_start_date = wp.item_buff("start_date");
  // String[] aa_end_date = wp.item_buff("end_date");
  // String[] aa_amt = wp.item_buff("amt_from");
  // String[] aa_term = wp.item_buff("installment_term");
  // String[] aa_break_flag = wp.item_buff("break_flag");
  // String[] aa_break_date = wp.item_buff("break_date");
  // String[] aa_reserve_type = wp.item_buff("reserve_type");
  // String[] aa_rowid = wp.item_buff("rowid");
  // String[] aa_mod_seqno = wp.item_buff("mod_seqno");
  // String[] aa_opt = wp.item_buff("opt");
  // String[] aa_id_p_seqno = wp.item_buff("id_p_seqno");
  // String[] aa_db_tmp = wp.item_buff("db_tmp");
  //
  // String[] aa_h_break_date = wp.item_buff("h_break_date");
  //
  // if (!(aa_card_no == null) && !empty(aa_card_no[0]))
  // rowcntaa = aa_card_no.length;
  // wp.listCount[0] = rowcntaa;
  //
  // // save
  // int rr = -1;
  // int ll_ok = 0, ll_err = 0;
  // for (int ii = 0; ii < aa_opt.length; ii++) {
  // rr = (int) this.to_Num(aa_opt[ii]) - 1;
  // if (rr < 0) {
  // return;
  // }
  // System.out.println("term1 : " + aa_term[ii]);
  // if (chackTerm(aa_term[ii]) <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "分期期數不存在");
  // ll_err++;
  // continue;
  // }
  // if (!empty(aa_h_break_date[rr]) && aa_db_tmp[rr].equals("N")) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "此筆資料已終止 不可再修改 !");
  // ll_err++;
  // continue;
  // }
  //
  // if (aa_db_tmp[rr].equals("N") && aa_break_flag[rr].equals("Y")) {
  //
  // String ls_del1 = "DELETE FROM bil_assign_installment_t WHERE card_no = :card_no "
  // + " and reserve_type = :reserve_type"
  // + " and start_date = :start_date";
  // setString("card_no", aa_card_no[rr]);
  // setString("reserve_type", aa_reserve_type[rr]);
  // setString("start_date", aa_start_date[rr]);
  // sqlExec(ls_del1);
  //
  // String is_sql = "insert into bil_assign_installment_t ("
  // + " card_no "
  // + ", reserve_type "
  // + ", start_date "
  // + ", end_date "
  // + ", amt_from "
  // + ", installment_term "
  // + ", break_flag"
  // + ", break_date"
  // + ", crt_date, crt_user "
  // + ", mod_time, mod_user, mod_pgm, mod_seqno"
  // + " ) values ("
  // + " ?,?,?,?,?,?,?,? "
  // + ", to_char(sysdate,'yyyymmdd'), ?"
  // + ", sysdate,?,?,1"
  // + " )";
  //
  // Object[] param = new Object[] {
  // aa_card_no[rr], aa_reserve_type[rr], aa_start_date[rr], aa_end_date[rr], aa_amt[rr],
  // aa_term[rr], aa_break_flag[rr], empty(aa_break_date[rr]) ? get_sysDate() : aa_break_date[rr],
  // wp.loginUser, wp.loginUser, wp.item_ss("mod_pgm")
  // };
  //
  // sqlExec(is_sql, param);
  //
  // if (sql_nrow <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "insert bil_assign_installment_t err !");
  // ll_err++;
  // sql_commit(0);
  // continue;
  // } else {
  // wp.col_set(rr, "ok_flag", "V");
  // ll_ok++;
  // sql_commit(1);
  // continue;
  // }
  // }
  //
  // if (empty(aa_card_no[rr])) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "卡號不可空白!");
  // ll_err++;
  // continue;
  // }
  //
  // if (empty(aa_start_date[rr]) || empty(aa_end_date[rr])) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "消費期間不可空白!");
  // ll_err++;
  // continue;
  // }
  //
  // if (to_Int(aa_start_date[rr]) > to_Int(aa_end_date[rr])) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "消費起日 不可大於消費迄日!");
  // ll_err++;
  // continue;
  // }
  //
  // if (to_Int(aa_end_date[rr]) < to_Int(get_sysDate())) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "消費迄日不可小於系統日期!");
  // ll_err++;
  // return;
  // }
  //
  // if (to_Int(aa_end_date[rr]) < to_Int(aa_break_date[rr])) {
  // wp.col_set(rr, "ok_flag", "X");
  // alert_msg("終止日期 不可大於消費迄日!");
  // ll_err++;
  // continue;
  // }
  //
  // if (!empty(aa_break_date[rr]) && to_Int(aa_break_date[rr]) < to_Int(get_sysDate())) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "終止日期 不可小於系統日期!");
  // ll_err++;
  // continue;
  // }
  //
  // if (chackExist(aa_card_no[rr], aa_amt[rr], aa_start_date[rr], aa_reserve_type[rr]) <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "有效期間 重疊");
  // ll_err++;
  // continue;
  // }
  //
  // if (empty(aa_amt[rr]) || to_Num(aa_amt[rr]) < 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "金額 不可小於0 !");
  // ll_err++;
  // continue;
  // }
  //
  // if (!aa_break_flag[rr].equals("Y")) {
  // if (to_Int(aa_start_date[rr]) < to_Int(get_sysDate())) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "消費起日不可小於系統日期!");
  // ll_err++;
  // continue;
  // }
  //
  //// chackno = chackCardno(aa_card_no[rr], aa_id_p_seqno[rr]);
  //// chackno = chackCardno(aa_card_no[rr]);
  //// switch (chackno) {
  //// case "0":
  ////
  //// wp.col_set(rr, "ok_flag", "X");
  //// wp.col_set(rr, "ls_errmsg", "此卡號與身分證號不符! ");
  //// ll_err++;
  //// continue;
  //// case "2":
  ////
  //// wp.col_set(rr, "ok_flag", "X");
  //// wp.col_set(rr, "ls_errmsg", "無效卡!");
  //// ll_err++;
  //// continue;
  ////
  //// case "3":
  ////
  //// wp.col_set(rr, "ok_flag", "X");
  //// wp.col_set(rr, "ls_errmsg", "此團體代號不可指定!");
  //// ll_err++;
  //// continue;
  ////
  //// }
  //
  // if (chackAmt(aa_amt[rr]) <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "與參數金額不符!");
  // ll_err++;
  // continue;
  // }
  //
  // if (chackTerm(aa_term[rr]) <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "分期期數不存在!");
  // ll_err++;
  // continue;
  // }
  // }
  //
  // String ls_break_date = "";
  // if (aa_break_flag[rr].equals("Y") && empty(aa_break_date[rr])) {
  // ls_break_date = get_sysDate();
  // } else {
  // ls_break_date = aa_break_date[rr];
  // }
  //
  // func.vars_set("aa_card_no", aa_card_no[rr]);
  // func.vars_set("aa_start_date", aa_start_date[rr]);
  // func.vars_set("aa_end_date", aa_end_date[rr]);
  // func.vars_set("aa_amt", aa_amt[rr]);
  // func.vars_set("aa_term", aa_term[rr]);
  // func.vars_set("aa_break_flag", aa_break_flag[rr]);
  // func.vars_set("aa_break_date", ls_break_date);
  // func.vars_set("aa_rowid", aa_rowid[rr]);
  // func.vars_set("aa_mod_seqno", aa_mod_seqno[rr]);
  // func.vars_set("aa_reserve_type", aa_reserve_type[rr]);
  //
  // if (aa_db_tmp[rr].equals("N")) {
  // // 主檔
  // String ls_del = "DELETE FROM bil_assign_installment_t WHERE card_no = :card_no "
  // + " and reserve_type = :reserve_type"
  // + " and start_date = :start_date";
  // setString("card_no", aa_card_no[rr]);
  // setString("reserve_type", aa_reserve_type[rr]);
  // setString("start_date", aa_start_date[rr]);
  // sqlExec(ls_del);
  //
  // String is_sql = "insert into bil_assign_installment_t ("
  // + " card_no "
  // + ", reserve_type "
  // + ", start_date "
  // + ", end_date "
  // + ", amt_from "
  // + ", installment_term "
  // + ", crt_date, crt_user "
  // + ", mod_time, mod_user, mod_pgm, mod_seqno"
  // + " ) values ("
  // + " ?,?,?,?,?,? "
  // + ", to_char(sysdate,'yyyymmdd'), ?"
  // + ", sysdate,?,?,1"
  // + " )";
  //
  // Object[] param = new Object[] {
  // aa_card_no[rr], aa_reserve_type[rr], aa_start_date[rr], aa_end_date[rr], aa_amt[rr],
  // aa_term[rr], wp.loginUser, wp.loginUser, wp.item_ss("mod_pgm")
  // };
  //
  // sqlExec(is_sql, param);
  //
  // if (sql_nrow <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "insert bil_assign_installment_t err !");
  // ll_err++;
  // sql_commit(0);
  // continue;
  // } else {
  // wp.col_set(rr, "ok_flag", "V");
  // ll_ok++;
  // sql_commit(1);
  // }
  //
  // } else {
  // // TMP
  // if (func.dbUpdate() <= 0) {
  // wp.col_set(rr, "ok_flag", "X");
  // wp.col_set(rr, "ls_errmsg", "update bil_assign_installment_t err !");
  // ll_err++;
  // sql_commit(0);
  // continue;
  // } else {
  // wp.col_set(rr, "ok_flag", "V");
  // ll_ok++;
  // sql_commit(1);
  // }
  //
  // }
  // }
  //
  // alert_msg("執行處理: 成功筆數=" + ll_ok + "; 失敗筆數=" + ll_err);
  //
  // }

  public int itemchanged() throws Exception {
    //super.wp = wr;

    String dddwWhere = "";
    String ajaxName = "";
    String option = "";
    ajaxName = wp.itemStr("ajaxName");

    switch (ajaxName) {

    case "reserveType":
        String lsReserveType = wp.itemStr("reserveType");// 抓前端輸入的ReserveType
        
        wp.initOption = "--";
        wp.optionKey = wp.colStr("installment_term");
        setString("reserve_type", lsReserveType);
        this.dddwList("dddw_installment_term", "bil_prod", "tot_term",
            "where 1=1 and confirm_flag = 'Y' and mcht_no in (select mcht_no from ptr_assign_installment where to_char(sysdate,'yyyymmdd') between start_date and end_date and reserve_type = :reserve_type) "
                + "group by tot_term");

        String str = wp.colStr("dddw_installment_term");
        wp.addJSON("dddw_tot_term_option", str);
        wp.addJSON("ajaxName", "reserveType");
        break;
      case "idName":
        String lsId = wp.itemStr("id");// 抓前端輸入的ID
        dddwWhere = " and a.id_no = :id ";
        String lsSql2 = "select a.id_p_seqno,b.card_no " + " from crd_idno a "
            + " left join crd_card b on a.id_p_seqno = b.id_p_seqno " + " where 1=1 " + dddwWhere
            + " and b.current_code = '0' " + " order by b.card_no ";
        setString("id", lsId);
        sqlSelect(lsSql2);
        if (sqlRowNum <= 0) {
          wp.addJSON("valid_cardno", "無有效卡號");
        } else if (sqlRowNum == 1) {
          wp.addJSON("card_no", sqlStr("card_no"));
          wp.addJSON("kk_card_no", sqlStr("card_no"));

          for (int ii = 0; ii < sqlRowNum; ii++) {
            option += "<option value='" + sqlStr(ii, "card_no") + "' ${kk_card_no-"
                + sqlStr(ii, "card_no") + "} >" + sqlStr(ii, "card_no") + "</option>";
          }
          wp.addJSON("dddw_card_no", option);
          wp.addJSON("valid_cardno", "");
        } else {
          // String aa = "";
          // String bb = "";
          // for (int i = 0; i < sql_nrow; i++) {
          // aa += sql_ss(i,"card_no")+",";
          // bb = ss_mid(aa,0,aa.length() - 1);
          // }
          wp.addJSON("valid_cardno", "");
          option += "<option value=''>--</option>";
          for (int ii = 0; ii < sqlRowNum; ii++) {
            option += "<option value='" + sqlStr(ii, "card_no") + "' ${kk_card_no-"
                + sqlStr(ii, "card_no") + "} >" + sqlStr(ii, "card_no") + "</option>";
          }
          wp.addJSON("dddw_card_no", option);


        }
        wp.addJSON("ajaxName", "idName");
        break;
      case "breakFlag":
        wp.addJSON("ajaxName", "breakFlag");
        String lsBreakFlag = wp.itemStr("flag");// 抓前端輸入的break_flag
        System.out.print("ls_break_flag=" + lsBreakFlag);
        if (empty(lsBreakFlag)) {
          wp.addJSON("break_date", "");
          break;
        } else {
          // Calendar cal = Calendar.getInstance();
          // cal.setTime(cal.getTime());
          // //取得明天的日期
          // cal.add(Calendar.DATE, 1);
          // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
          // String sysdate1 = format.format(cal.getTime());
          Date date = new Date();
          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

          String sysdate1 = format.format(date);
          wp.addJSON("break_date", sysdate1);
        }

        break;
      // case "breakDate":
      // wp.addJSON("ajaxName","breakDate");
      // String ls_break_date = wp.item_ss("date");//抓前端輸入的break_date
      // System.out.print("ls_break_date="+ls_break_date);
      // if(empty(ls_break_date)) {
      // wp.addJSON("break_falg","");
      // }else {
      // wp.addJSON("break_falg","1");
      // }
      // break;
    }

    return 1;
  }

//  public void itemchanged() throws Exception {
//    if (!empty(wp.itemStr("kk_card_no"))) {
//      wp.colSet("card_no", wp.itemStr("kk_card_no"));
//      wp.colSet("valid_cardno", wp.itemStr("valid_cardno"));
//    }
//    // String ls_break_date = "";
//    // ls_break_date = wp.item_ss("break_date");
//    // String kk_break_flag = "";
//    // String ls_break_flag = "";
//    // ls_break_flag = wp.item_ss("break_flag");
//    //
//    // if (!empty(ls_break_date)) {
//    // kk_break_flag = "Y";
//    // wp.col_set("break_flag", "1");
//    // wp.col_set("kk_break_flag", kk_break_flag);
//    //
//    // }else {
//    // kk_break_flag = "N";
//    // wp.col_set("break_flag", "");
//    // wp.col_set("kk_break_flag", kk_break_flag);
//    // }
//    //
//    return;
//  }

  // public int chackTerm(String term) {
  // String is_sql = " select count(*) as tot_cnt from bil_prod ";
  // is_sql += " where tot_term = ? ";
  // is_sql += " and confirm_flag = 'Y' ";
  // is_sql += " and mcht_no in (select mcht_no from ptr_assign_installment where
  // to_char(sysdate,'yyyymmdd') between start_date and end_date) ";
  // Object[] param1 = new Object[] { term };
  // sqlSelect(is_sql, param1);
  // if (sql_num("tot_cnt") <= 0) {
  // return -1;
  // }
  //
  // return 1;
  //
  // }

  // public String chackCardno(String cardno, String id_p_seqno) {
  //
  // String is_sql = " select current_code as ls_current_code, "
  // + "decode(assign_installment,'Y',decode(ptr_group_code.auto_installment,'Y','N','Y'),'N')as
  // ls_assign_installment "
  // + "from crd_card, ptr_group_code "
  // + "where decode(crd_card.group_code,'','0000',crd_card.group_code) = ptr_group_code.group_code
  // "
  // + "and card_no =? "
  // + "and major_id_p_seqno =? ";
  // Object[] param1 = new Object[] { cardno, id_p_seqno };
  // sqlSelect(is_sql, param1);
  // if (sql_nrow <= 0) {
  // // 此卡號與身分證號不符
  // return "0";
  // }
  //
  // if (!sql_ss("ls_current_code").equals("0")) {
  // // 無效卡
  // return "2";
  // }
  // if (!sql_ss("ls_assign_installment").equals("Y")) {
  // // 此團體代號不可指定
  // return "3";
  // }
  //
  // return "1";
  // }

  // 2019/06/26取消判斷身分證字號
  // public String chackCardno(String cardno) {
  //
  // String is_sql = " select current_code as ls_current_code, "
  // + "decode(assign_installment,'Y',decode(ptr_group_code.auto_installment,'Y','N','Y'),'N')as
  // ls_assign_installment "
  // + "from crd_card, ptr_group_code "
  // + "where decode(crd_card.group_code,'','0000',crd_card.group_code) = ptr_group_code.group_code
  // "
  // + "and card_no =? ";
  // Object[] param1 = new Object[] { cardno};
  // sqlSelect(is_sql, param1);
  //
  // if (!sql_ss("ls_current_code").equals("0")) {
  // // 無效卡
  // return "2";
  // }
  // if (!sql_ss("ls_assign_installment").equals("Y")) {
  // // 此團體代號不可指定
  // return "3";
  // }
  //
  // return "1";
  // }

  public int chackAmt(String amtFrom) {
    String isSql = "  select count(*) as li_count from ptr_assign_installment  ";
    isSql += " where  to_char(sysdate,'yyyymmdd') between start_date and end_date  ";
    isSql += "  and  ?  between amt_from and amt_to ";

    setString2(1, amtFrom);

    sqlSelect(isSql);

    if (sqlRowNum <= 0) {
      return -1;
    }

    return 1;
  }

  // public int chackExist(String card_no, String amt, String aa_start_date, String reserve_type) {
  //
  // String is_sql2 = " select count(*) as li_count from bil_assign_installment ";
  // is_sql2 += " where card_no = ? ";
  // is_sql2 += " and amt_from = ? ";
  // is_sql2 += " and reserve_type = ? ";
  // is_sql2 += " and ? between start_date and end_date ";
  //
  // ppp(1, card_no);
  // ppp(2, amt);
  // ppp(3, reserve_type);
  // ppp(4, aa_start_date);
  //
  // sqlSelect(is_sql2);
  //
  // if (sql_num("li_count") > 0) {
  // // err
  // return -1;
  // } else {
  // return 1;
  // }
  //
  // }

  public int chackExist() {

    String lsVal1 = "", lsVal2 = "", lsVal3 = "";
    String lsValScr1 = "", lsValScr2 = "", lsValScr3 = "";
    double lAmtFrom = 0, lAmtFromScr = 0;

    lsVal1 = wp.itemStr("card_no");
    lsVal2 = wp.itemStr("start_date");
   // String aa = wp.colStr("rowid");

    // 終止為空(N),ls_val3=消費迄日-1
    if (empty(wp.itemStr("break_flag"))) {
      lsVal3 = numToStr(toNum(wp.itemStr("end_date")) - 1, "###");
    } else {
      // 終止有值(Y),判斷是否有終止日期,有→終止日-1 ; 無→系統日-1
      if (empty(wp.itemStr("break_date"))) {
        lsVal3 = numToStr(toNum(getSysDate()) - 1, "###");
      } else {
        lsVal3 = numToStr(toNum(wp.itemStr("break_date")) - 1, "###");
      }
    }

    lAmtFrom = wp.itemNum("amt_from");

    // 消費日大於ls_val3
    if (lsVal2.compareTo(lsVal3) > 0) {
      return 1;

    }


    String lsSql = "";
    lsSql =
        "select card_no,start_date,break_flag,break_date,end_date,amt_from from bil_assign_installment_t "
            + "where card_no = :card_no and hex(rowid) = :rowid ";
    setString("card_no", lsVal1);
    setString("rowid", wp.colStr("rowid"));
    sqlSelect(lsSql);

    if (sqlRowNum > 0) {
      for (int i = 0; i < sqlRowNum; i++) {
        lsValScr1 = sqlStr(i, "card_no");
        lsValScr2 = sqlStr(i, "start_date");

        if (empty(sqlStr(i, "break_flag"))) {
          lsValScr3 = numToStr(toNum(sqlStr(i, "end_date")) - 1, "###");
        } else {
          if (empty(sqlStr(i, "break_date"))) {
            lsValScr3 = numToStr(toNum(getSysDate()) - 1, "###");
          } else {
            lsValScr3 = numToStr(toNum(sqlStr(i, "break_date")) - 1, "###");
          }
        }

        lAmtFromScr = sqlNum(i, "amt_from");

        if (lsValScr2.compareTo(lsValScr3) > 0) {
          continue;
        }

        if (lsValScr1.equals(lsVal1) && lAmtFromScr == lAmtFrom) {

          if (lsValScr2.compareTo(lsVal2) <= 0 && lsValScr3.compareTo(lsVal2) >= 0) {
            return 0;
          }

          if (lsValScr2.compareTo(lsVal3) <= 0 && lsValScr3.compareTo(lsVal3) >= 0) {
            return 0;
          }
        }
      }
    }

    return 1;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
      
//      //若已覆核，則不能刪除
//      if (wp.colStr("apr_date").equals("")==false) {
//    	  btnDeleteOn(false);
//	  }
      
    }

    if (wp.respHtml.indexOf("_add") > 0) {
      this.btnModeAud();
    } else {
      btnAddOn(wp.autUpdate());
    }

  }

  @Override
  public void dddwSelect() {

    try {
      wp.colSet("valid_cardno", wp.itemStr("valid_cardno"));
      String lsKkCardNo = wp.itemStr("kk_card_no");
      wp.optionKey = lsKkCardNo;

      StringBuffer dddwWhere = new StringBuffer();
      dddwWhere.append(" where 1=1 and b.current_code = '0' ");
      dddwWhere.append(" and a.id_no = '");
      dddwWhere.append(wp.itemStr("id_no"));
      dddwWhere.append("' order by b.card_no ");

      this.dddwList("dddw_card_no",
          "crd_idno a left join crd_card b on a.id_p_seqno = b.id_p_seqno ", " card_no ", "",
          dddwWhere.toString());

      wp.initOption = "--";
      wp.optionKey = wp.colStr("installment_term");
      this.dddwList("dddw_installment_term", "bil_prod", "tot_term",
          "where 1=1 and confirm_flag = 'Y' and mcht_no in (select mcht_no from ptr_assign_installment where to_char(sysdate,'yyyymmdd') between start_date and end_date) "
              + "group by tot_term");

    } catch (Exception ex) {
    }
  }

//  public void dataDelete() {
//
//	String isSql2 = "";
//    String rowid = wp.colStr("rowid");
////    if (wp.itemStr("apr_flag").equals("U.更新待覆核")) {
////    	isSql2 += "  delete bil_assign_installment_t   ";
////	} else {
//		isSql2 += "  delete bil_assign_installment   ";
////	}
//    
//    isSql2 += " where hex(rowid) = :rowid  ";
//    setString("rowid", rowid);
//
//    sqlExec(isSql2);
//
//    if (sqlRowNum <= 0) {
//      alertErr("資料刪除失敗!!");
//    }
//  }

  @Override
  public void initPage() {
    // 設定初始搜尋條件值
    if (wp.respHtml.indexOf("_add") > 0) {
//      if (strAction.equals("new")) {
//        wp.colSet("apr_flag", "U.更新待覆核");
//      }
    }

  }

}
