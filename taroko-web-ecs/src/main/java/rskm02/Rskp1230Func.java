package rskm02;
/**
 * 商務卡第二次分群處理
 * 2019-0920   JH    modify
 * 2018-0123:	JH		merge
 * 2017-xxxx:	Alex	initial
 */

import busi.FuncAction;

public class Rskp1230Func extends FuncAction {

String is_batch_no = "";
public String is_risk_group = "";

busi.DataSet dsBank = null;

void selectRsk_trcorp_mast()  {
   strSql = "select group_proc_date1 "
         + ", group_date2 "
         + ", group_proc_date2, data_yymm "
         + " from rsk_trcorp_mast"
         + " where batch_no =?"
   ;
   sqlSelect(strSql, new Object[]{
         is_batch_no
   });
}

@Override
public void dataCheck()  {
   selectRsk_trcorp_mast();
   if (sqlRowNum <= 0) {
      errmsg("查無覆審資料, 批號=" + is_batch_no);
      return;
   }
   if (colEmpty("group_proc_date1")) {
      errmsg("未執行 第一次分群處理");
      return;
   }

   if (colEmpty("group_date2")) {
      errmsg("未設定 第二次分群參數設定");
      return;
   }

   if (checkParm(is_batch_no) == false) {
      errmsg("查無 第二分群設定參數");
      return;
   }

   if (checkList(is_batch_no) == false) {
      errmsg("查無覆審資料");
      return;
   }

   if (checkData_yymm(colStr("data_yymm")) == false) {
      errmsg("查無 分行往來統計資料");
      return;
   }
}

boolean checkParm(String ls_batch_no)  {
   String sql1 = "select count(*) as ll_cnt "
         + " from rsk_trcorp_parm A , rsk_trcorp_mast_group B "
         + " where A.risk_group = B.risk_group "
         + " and B.batch_no =?"
         + " and B.group_type='2'";
   sqlSelect(sql1, new Object[]{
         ls_batch_no
   });
   return colNum("ll_cnt") > 0;
}

boolean checkList(String ls_batch_no)  {
   String sql1 = "select count(*) as ll_cnt "
         + " from rsk_trcorp_list"
         + " where batch_no =?"
         + " and close_date = ''";
   sqlSelect(sql1, new Object[]{
         ls_batch_no
   });
   return colNum("ll_cnt") > 0;
}

boolean checkData_yymm(String ls_data_yymm)  {
   String sql1 = "select count(*) as ll_cnt "
         + " from rsk_trcorp_bank_stat"
         + " where data_yymm =?";

   sqlSelect(sql1, new Object[]{
         ls_data_yymm
   });
   return colNum("ll_cnt") > 0;
}

@Override
public int dbInsert() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbUpdate() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dbDelete() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataProc()  {
   is_batch_no = varsStr("batch_no");
   if (empty(is_batch_no)) {
      errmsg("處理批號: 不可空白");
      return rc;
   }
   dataCheck();
   if (rc != 1)
      return rc;


   return rc;
}

void selectRsk_trcorp_bank_stat()  {
   int llunOK = 0, ll_OK = 0;
   dsBank = new busi.DataSet();

   strSql = "select * from rsk_trcorp_bank_stat"
         + " where data_yymm =?";
   dsBank.colList = this.sqlQuery(strSql, new Object[]{varsStr("data_yymm")});
   wp.log("dsBank=" + dsBank.listRows());

   if (sqlRowNum <= 0) {
      log("data_yymm : " + varsStr("data_yymm"));
      errmsg("kkkk");
      return;
   }
   for (int ii = 0; ii < dsBank.listRows(); ii++) {
      dsBank.listToCol(ii);
      updateList();
   }
}

public int updateList()  {
   strSql = "update rsk_trcorp_list set "
         + " risk_group2 = :ls_risk_group2, "
         + " group_proc_date2 = to_char(sysdate,'yyyymmdd') , "
         + " mod_user =:mod_user, "
         + " mod_time=sysdate,"
         + " mod_pgm = 'rskp1230' ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where batch_no =:batch_no "
         + " and corp_no =:corp_no "
   ;
   var2ParmStr("batch_no");
   setString("mod_user", wp.loginUser);
   setString("corp_no", dsBank.colStr("corp_no"));
   //ls_risk_group2;
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

public int updateMast()  {
   strSql = "update rsk_trcorp_mast set "
         + " group_proc_date2 = to_char(sysdate,'yyyymmdd') , "
         + " mod_time=sysdate,"
         + " mod_pgm = 'rskp1230' ,"
         + " mod_seqno =nvl(mod_seqno,0)+1 "
         + " where batch_no =:batch_no "
   ;
   var2ParmStr("batch_no");
   rc = sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
   }
   return rc;
}

}
