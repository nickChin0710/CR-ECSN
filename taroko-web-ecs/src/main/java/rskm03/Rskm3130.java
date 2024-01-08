/*
 * 2019-1225:  V1.00.01  Alex  fix order by
 *
 */
package rskm03;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Rskm3130 extends BaseEdit {
Rskm3130Func func;
String kk1 = "";
int ll_cnt = 0;

@Override
public void actionFunction(TarokoCommon wr) throws Exception {
   super.wp = wr;
   rc = 1;

   strAction = wp.buttonCode;
   //ddd("action=" + strAction + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" + wp.respHtml);
   if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      //clearFunc();
      //read_ctfg_mast();
   }
   else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
   }
   else if (eqIgno(wp.buttonCode, "R")) {
      //-資料讀取-
      strAction = "R";
      dataRead();

   }
   else if (eqIgno(wp.buttonCode, "R2")) {
      //-資料讀取-
      strAction = "R2";
      dataRead();
   }
   else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
   }
   else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
   }
   else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page*/
      queryRead();
   }
   else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
   }
   else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
   }

   dddwSelect();
   initButton();

}

@Override
public void dddwSelect() {
   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("rejt_reason");
         dddwList("d_dddw_rejt_reason", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_REJT_REASON'");
      }
   }
   catch (Exception ex) {
   }

   try {
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("contr_result");
         dddwList("d_dddw_contr_result", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_CONTR_RESULT'");
      }
   }
   catch (Exception ex) {
   }

   try {
   	
   	if (eqIgno(wp.respHtml,"rskm3130")) {         
         wp.optionKey = wp.colStr("ex_proc_status");
         dddwList("d_dddw_ex_proc_status", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_STATUS'");
      }
   	
      if (wp.respHtml.indexOf("_detl") > 0) {
         wp.initOption = "--";
         wp.optionKey = wp.colStr("proc_status");
         dddwList("d_dddw_proc_status", "ptr_sys_idtab"
               , "wf_desc", "wf_desc", "where wf_type='CTFG_PROC_STATUS'");
      }
   }
   catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_proc_date1"), wp.itemStr("ex_proc_date2")) == false) {
      alertErr("處理日期起迄：輸入錯誤");
      return;
   }
   if (this.chkStrend(wp.itemStr("ex_view_date1"), wp.itemStr("ex_view_date2")) == false) {
      alertErr("檢視日期起迄：輸入錯誤");
      return;
   }

   if (wp.itemEmpty("ex_card_no")) {
      if (wp.itemEmpty("ex_proc_date1") &&
            wp.itemEmpty("ex_proc_date2") &&
            wp.itemEmpty("ex_view_date1") &&
            wp.itemEmpty("ex_view_date2")) {
         alertErr("處理日期/檢視日期，不可全部空白");
         return;
      }
   }

   wp.whereStr = " where 1=1 and proc_type='主動服務'"
         + sqlCol(wp.itemStr("ex_card_no"), "card_no", "like%")
         + sqlCol(wp.itemStr("ex_proc_date1"), "proc_date", ">=")
         + sqlCol(wp.itemStr("ex_proc_date2"), "proc_date", "<=")
         + sqlCol(wp.itemStr("ex_view_date1"), "view_date", ">=")
         + sqlCol(wp.itemStr("ex_view_date2"), "view_date", "<=")
         + sqlCol(wp.itemStr("ex_proc_user"), "proc_user", "like%")
         + sqlCol(wp.itemStr("ex_proc_status"),"proc_status")
         ;
   if (wp.itemEq("ex_close_flag", "1")) {
      wp.whereStr += " and uf_nvl(close_flag,'0')='1'";
   }
   else if (wp.itemEq("ex_close_flag", "2")) {
      wp.whereStr += " and uf_nvl(close_flag,'0')<>'1'";
   }

   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();
   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = "card_no,"
         + "proc_date, "
         + "ctfg_seqno,"
         + "rejt_reason,"
         + "rejt_amt,"
         + "otb_amt,"
         + "contr_result,"
         + "ok_cnt,"
         + "ok_amt,"
         + "proc_status,"
         + "view_date,"
         + "send_sms_flag,"
         + "decode(send_sms_flag,'','','0','','1','Y') as tt_send_sms_flag ,"
         + "close_flag,"
         + "decode(close_flag,'','','0','','1','Y') as tt_close_flag ,"
         + "proc_user,"
         + "mod_user,"
         + "mod_time,"
         + "mod_pgm,"
         + "mod_seqno,"
         + "hex(rowid) as rowid,"
         + "proc_time"
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereOrder = " order by proc_date Asc , proc_time Asc ";
   pageQuery();


   if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
   }
   //list_wkdata();
   wp.setListCount(1);
   wp.setPageValue();
}

@Override
public void querySelect() throws Exception {
   kk1 = wp.itemStr("data_k1");
   ll_cnt = (int) (wp.itemNum("data_k2") % 20);
   String[] ls_rowid = wp.itemBuff("rowid");
   int li_cnt = 0, aa = 1;
   li_cnt = ls_rowid.length;

   for (int ii = ll_cnt; ii < li_cnt; ii++) {
      wp.colSet("ls_rowid" + aa, ls_rowid[ii]);
      aa++;
   }

   dataRead();
}

@Override
public void dataRead() throws Exception {
   int aa = 0;
   if (empty(kk1)) {
      if (eqIgno(strAction, "R")) {
         kk1 = wp.itemStr("rowid");
      }
      else {
         aa = (int) wp.itemNum("aa");
         aa++;
         kk1 = wp.itemStr("ls_rowid" + aa);
         if (empty(kk1)) {
            alertErr("已是最後一筆 !");
            return;
         }
         wp.colSet("aa", aa);
      }
   }
   else {
      wp.colSet("aa", aa);
   }

   wp.selectSQL = "hex(rowid) as rowid, mod_seqno, "
         + "card_no,   "
         + "proc_date, "
         + "proc_time, "
         + "ctfg_seqno, "
         + "rejt_reason,"
         + "rejt_amt,"
         + "otb_amt,"
         + "contr_result,"
         + "ok_cnt,"
         + "ok_amt,"
         + "proc_status,"
         + "view_date,"
         + "send_sms_flag,"
         + "close_flag,"
         + "proc_user,"
         + "mod_user,"
         + "to_char(mod_time,'yyyymmdd') as mod_date,"
         + "mod_pgm,"
         + "mod_seqno"
   ;
   wp.daoTable = "rsk_ctfg_proc";
   wp.whereStr = " where 1=1 "
         + " and rowid = :rowid "
   ;

   this.setRowid("rowid", kk1);
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key=" + kk1);
      return;
   }
}


@Override
public void saveFunc() throws Exception {
   func = new Rskm3130Func(wp);
   rc = func.dbSave(strAction);
   this.sqlCommit(rc);
   if (rc != 1) {
      alertErr(func.getMsg());
   }

}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
   }
}

}
