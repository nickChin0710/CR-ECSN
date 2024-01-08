package rskm01;
/** 再提示整批登錄維護
 * 2020-1005   JH    modify
 * 2020-0117  V1.00.03     Alex      fix dataRead
 * 2020-0115  V1.00.02     Alex      fix cond
 * 2019-1206  V1.00.01     Alex      add initButton
 * */

public class Rskm0240 extends ofcapp.BaseAction {

String kk1 = "", kk2 = "";

@Override
public void userAction() throws Exception {
   switch (wp.buttonCode) {
      case "X": //轉換顯示畫面
         strAction = "new";
         clearFunc(); break;
      case "Q": //查詢功能
         queryFunc(); break;
      case "R": // -資料讀取-
         dataRead(); break;
      case "A": //新增功能
      case "U": //更新功能
      case "D": //刪除功能
         saveFunc(); break;
      case "M": //瀏覽功能 :skip-page-
         queryRead(); break;
      case "S": //動態查詢--
         querySelect(); break;
      case "L": //清畫面--
         strAction = "";
         clearFunc(); break;
      case "C": // -資料處理-
         procFunc(); break;
//      case "XLS":  //-Excel-
//         is_action = "XLS";
// 			xlsPrint(); break;
//      case "PDF": //-PDF-
//         is_action = "PDF";
//         pdfPrint(); break;
      default:
         alertErr("未指定 actionCode 執行Method, action[%s]",wp.buttonCode);
   }

}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "rskm0240")) {
         wp.optionKey = wp.colStr("ex_curcode");
         dddwList(
               "dddw_dc_curr_code_tw",
               "ptr_sys_idtab",
               "wf_id",
               "wf_desc",
               "where wf_type = 'DC_CURRENCY'");
      }
   }
   catch (Exception ex) {
   }
}

@Override
public void queryFunc() throws Exception {
   String lsWhere = "", ss = "";
   ss = wp.itemStr("ex_ctrl_seqno");
   if (empty(ss) == false) {
      if (ss.length() < 4) {
         alertErr("控制流水號: 至少4碼");
         return;
      }
//      lsWhere += " and (ctrl_seqno like '" + ss + "%" + "' or ctrl_seqno2 like '" + ss + "%') ";
      lsWhere += " and (ctrl_seqno like ? or ctrl_seqno2 like ? ) ";
      setString(ss);
      setString(ss);    		  
   }
   ss = wp.itemStr("ex_card_no4");
   if (!empty(ss)) {
      if (ss.length() < 4) {
         alertErr("卡號: 至少4碼");
         return;
      }
      lsWhere += sqlCol("%" + ss, "card_no", "like");
   }
   if (!condStrend(wp.itemStr("ex_rep_date1"), wp.itemStr("ex_rep_date2"))) {
      alertErr("再提示日期: 起迄輸入錯誤");
      return;
   }
   
//   lsWhere += commSqlStr.strend(wp.itemStr("ex_rep_date1"), wp.itemStr("ex_rep_date2"), "repsent_date");
   lsWhere += sqlCol(wp.itemStr("ex_rep_date1"),"repsent_date",">=")
		   +  sqlCol(wp.itemStr("ex_rep_date2"),"repsent_date","<=")
		   ;
   lsWhere += sqlCol(wp.itemStr("ex_film_no"), "film_no", "like%");
   if (empty(lsWhere)) {
      alertErr("條件[控制流水號,卡號,再提示日期,微縮影編號]: 不可同時空白");
      return;
   }
   wp.whereStr = " where 1=1 and ( (chg_stage='1' and sub_stage='30') or chg_stage='2' )"
         + lsWhere
         + sqlCol(wp.itemStr("ex_curcode"), "uf_nvl(curr_code,'901')")
         + sqlCol(wp.itemStr("ex_bin_type"), "bin_type")
   ;

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = "hex(rowid) as rowid,"
         + " reference_no , "
         + " reference_seq , "
         + " ctrl_seqno , "
         + " bin_type , "
         + " debit_flag , "
         + " card_no,"
         + " chg_stage,"
         + " sub_stage,"
         + " rep_status,"
         + " repsent_date,"
         + " rep_add_date,"
         + " rep_add_user,"
         + " fst_disb_yn,"
         + " fst_disb_apr_date,"
         + " decode(final_close,'','N',final_close) as final_close,"
         + " rep_glmemo3,"
         + " film_no,"
         + " rep_apr_date,"
         + " rep_ac_no,"
         + " dest_amt,"
         + " dc_dest_amt,"
         + " rep_amt_twd, rep_part_flag, "
         + " fst_ac_no,"
         + " fst_glmemo3,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt,"
         + " uf_dc_amt(curr_code,rep_amt_twd,rep_dc_amt) as rep_dc_amt,"
         + " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         //+ " rep_dc_amt - fst_dc_amt as db_diffamt"
   +" '' as xx"
   ;
   wp.daoTable = "rsk_chgback";
   wp.whereOrder = " order by repsent_date Desc , ctrl_seqno ";

   pageQuery();
   // db_diffamt();
   wp.setListCount(1);
   if (sqlRowNum <= 0) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setPageValue();
   queryAfter();
}

void queryAfter() throws Exception {
   int llNrow =wp.listCount[0];
   for(int ll=0; ll<llNrow; ll++) {
      double lm_diff =wp.colNum(ll,"rep_dc_amt") - wp.colNum(ll,"fst_dc_amt");
      wp.colSet(ll,"db_diffamt",lm_diff);
   }
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   kk2 = wp.itemStr("data_k2");
   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(kk1))
      kk1 = wp.itemStr("reference_no");
   if (empty(kk2))
      kk2 = wp.itemStr("reference_seq");

   wp.selectSQL = "hex(rowid) as rowid,mod_seqno,"
         + " reference_no , "
         + " reference_seq , "
         + " ctrl_seqno , "
         + " bin_type , "
         + " debit_flag , "
         + " card_no,"
         + " chg_stage,"
         + " sub_stage,"
         + " rep_status,"
         + " repsent_date,"
         + " rep_add_date,"
         + " rep_add_user,"
         + " fst_disb_yn,"
         + " fst_disb_apr_date,"
         + " final_close,"
         + " rep_glmemo3,"
         + " film_no,"
         + " rep_apr_date,"
         + " rep_ac_no,"
         + " dest_amt,"
         + " uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt,"
         + " rep_amt_twd,"
         + " fst_ac_no,"
         + " fst_glmemo3,"
         + " uf_dc_curr(curr_code) as curr_code,"
         + " uf_dc_amt(curr_code,fst_disb_amt,fst_disb_dc_amt) as fst_disb_dc_amt,"
         + " uf_dc_amt(curr_code,rep_amt_twd,rep_dc_amt) as rep_dc_amt,"
         + " uf_dc_amt(curr_code,fst_twd_amt,fst_dc_amt) as fst_dc_amt,"
         + " 0 as wk_diffamt";
   wp.daoTable = "Vrsk_chgback";
   wp.whereStr = " where 1=1"
         + sqlCol(kk1, "reference_no")
         + sqlCol(kk2, "reference_seq");

   pageSelect();
   // db_diffamt();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }

   //+ " rep_dc_amt - fst_dc_amt as wk_diffamt";
   double lmAmt = wp.colNum(0, "rep_dc_amt") - wp.colNum(0, "fst_dc_amt");
   wp.colSet("wk_diff_amt", lmAmt);
   if (wp.colEq("chg_stage", "1")) {
      wp.colSet("rep_ac_no", wp.colStr("fst_ac_no"));
      wp.colSet("rep_glmemo3", wp.colStr("fst_glmemo3"));
   }
}


@Override
public void saveFunc() throws Exception {
   rskm01.Rskm0240Func func = new rskm01.Rskm0240Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);
   this.sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }
   //if (is_action)
   this.saveAfter(true);
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") < 0) {
      return;
   }
   btnModeAud();

}

@Override
public void procFunc() throws Exception {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
