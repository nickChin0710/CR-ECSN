/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名
 * 2023-1030      JH    bugfix
 ******************************************************************************/
package rskm02;

import ofcapp.BaseAction;

public class Rskp2060 extends BaseAction {

String batchNo = "", riskGroup = "";

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
      if (eqIgno(wp.respHtml, "rskp2060")) {
         wp.optionKey = wp.colStr(0, "ex_crt_user");
         dddwList("dddw_crt_user", "sec_user", "usr_id", "usr_cname", "where 1=1");
      }
   } catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {
   if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("建檔日期起迄：輸入錯誤");
      return;
   }


   String lsWhere =
       " where 1=1 and apr_flag<>'Y' "+sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
           +sqlCol(wp.itemStr("ex_risk_group"), "risk_group")
           +sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
           +sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
           +sqlCol(wp.itemStr("ex_crt_user"), "crt_user", "like%");
   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();

}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = ""+" batch_no ,"+" batch_remark ,"+" risk_group ,"+" action_code ,"
       +" adj_limit_rate ,"+" adj_limit_reason ,"+" msg_flag ,"+" block_reason4 ,"
       +" block_reason5 ,"+" spec_status ,"+" delay_action_day ,"+" delay_msg_flag ,"
       +" loan_flag ,"+" decode(loan_flag,'0','正常','1','加強催理') as tt_loan_flag ,"
       +" crt_date ,"+" crt_user , "
       +" (select wf_desc from ptr_sys_idtab where wf_type ='RSK_ACTION_VERSION' and wf_id = batch_no ) as batch_remark";
   wp.daoTable = "rsk_trial_action";
   if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
   }
   wp.whereOrder = " order by batch_no ";
   logSql();
   pageQuery();

   if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setListCount(1);
   wp.setPageValue();
}


@Override
public void querySelect() throws Exception {
   batchNo = wp.itemStr("data_k1");
   riskGroup = wp.itemStr("data_k2");
   dataRead();


}

@Override
public void dataRead() throws Exception {
   if (empty(batchNo))
      batchNo = wp.itemStr("batch_no");
   if (empty(riskGroup))
      riskGroup = wp.itemStr("risk_group");

   wp.selectSQL = "hex(rowid) as rowid , mod_seqno,"+" batch_no,"+" apr_flag,"
       +" action_code,"+" risk_group,"+" delay_action_day,"+" delay_msg_flag,"
       +" msg_flag,"+" adj_limit_rate,"+" adj_limit_reason,"+" block_reason4,"
       +" block_reason5,"+" spec_status,"+" loan_flag,"+" crt_user,"+" crt_date,"
       +" to_char(mod_time,'yyyymmdd') as mod_date,"+" mod_user";
   wp.daoTable = "rsk_trial_action";
   wp.whereStr =
       " where 1=1 and apr_flag='N'"+sqlCol(batchNo, "batch_no")+sqlCol(riskGroup, "risk_group");
   this.logSql();
   pageSelect();
   if (sqlNotFind()) {
      alertErr("查無資料, key1="+batchNo+" key2"+riskGroup);
      return;
   }
   dataAfter();
}

public void dataAfter() {
   String sql1 = " select "+" wf_desc "+" from ptr_sys_idtab "
       +" where wf_type ='ADJ_REASON_DOWN' "+" and wf_id = ? ";
   sqlSelect(sql1, new Object[]{wp.colStr("adj_limit_reason")});
   if (sqlRowNum > 0) {
      wp.colSet("tt_limit_reason", sqlStr("wf_desc"));
   }
}

@Override
public void saveFunc() throws Exception {

}

@Override
public void procFunc() throws Exception {
   int ilOk = 0;
   int ilErr = 0;

   rskm02.Rskp2060Func func = new rskm02.Rskp2060Func();
   func.setConn(wp);

   String[] lsBatchNo = wp.itemBuff("batch_no");
   String[] lsRiskGroup = wp.itemBuff("risk_group");
   String[] opt = wp.itemBuff("opt");
   if (empty(lsBatchNo[0])) {
      wp.listCount[0] = 0;
      alertErr("無資料可處理");
      return;
   }
   wp.listCount[0] = lsBatchNo.length;
   if (optToIndex(opt[0]) < 0) {
      alertErr("未點選欲處理資料");
      return;
   }

   int rr = -1;
   for (int ii = 0; ii < opt.length; ii++) {
//      rr = (int) this.toNum(opt[ii]) - 1;
//      rr = (int) (this.toNum(opt[ii]) - 1);
      rr = optToIndex(opt[ii]);
      if (rr < 0) {
         continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("batch_no", lsBatchNo[rr]);
      func.varsSet("risk_group", lsRiskGroup[rr]);


      rc = func.dataProc();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
   }
   alertMsg("覆核處理: 成功筆數="+ilOk+"; 失敗筆數="+ilErr);

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
