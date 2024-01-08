package rskm01;

/**
 * 扣款作業結案維護(rskM0220, rskm0250)
 * 2019-0911   JH    --rsk-ctrl-seqno
 */

public class Rskm0220Func extends busi.FuncAction {

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dataCheck() {
   //-結案處理-
   selectRskChgback();
   if (rc != 1)
      return;

   if (wp.itemEmpty("CB_clo_result")) {
      errmsg("結案結果不可空白");
      return;
   }
   String lsCloseReason = wp.itemStr("wk_close_reason");
   if (colEq("A.bin_type", "J") && lsCloseReason.length() != 3) {
      errmsg("JCB理由碼為3碼");
      return;
   }
   else if (colEq("A.bin_type", "M") && lsCloseReason.length() != 4) {
      errmsg("MASTER理由碼為4碼");
      return;
   }
//   else if (col_eq("A.bin_type", "V") && lsCloseReason.length() != 2) {
//      errmsg("NCCC，VISA理由碼為2碼");
//      return;
//   }
   return;
}

void dataCheckCancel() {
   selectRskChgback();
   if (rc != 1) return;

   if (colEmpty("A.final_close")) {
      errmsg("尚未結案, 不須取消");
   }

}

void selectRskChgback() {
   strSql = "select ctrl_seqno, bin_type,"
         + " reference_no, reference_seq,"
         + " chg_stage, sub_stage,"
         + " final_close,"
         + " mod_seqno"
         + " from rsk_chgback"
         + " where 1=1"
         + " and rowid = ? ";

   daoTid = "A.";
   setRowId(1,wp.itemStr("rowid"));
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料已不存在, kk[%s]", wp.itemStr("CB_ctrl_seqno"));
      return;
   }

   if (colNum("A.mod_seqno") != wp.itemNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }

}

@Override
public int dbInsert() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int dbUpdate() {
   msgOK();
   actionInit("U");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "update rsk_chgback set"
         + " final_close ='C'"
         + ", clo_result =?"
         + ", fst_reason_code =?"
         + ", close_add_date =" + commSqlStr.sysYYmd
         + ", close_add_user =?"
         + ", close_apr_date =" + commSqlStr.sysYYmd
         + ", close_apr_user =?"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         + " and rowid = ? ";
   setString(wp.itemStr("CB_clo_result"));
   setString(wp.itemStr("wk_close_reason"));
   setString(modUser);
   setString(modUser);
   setRowId(wp.itemStr("rowid"));
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback error; kk[%s]", colStr("A.ctrl_seqno"));
      return rc;
   }

   return rc;
}

@Override
public int dbDelete() {
   msgOK();
   actionInit("U");
   //-結案取消-
   dataCheckCancel();
   if (rc != 1) {
      return rc;
   }

   strSql = "update rsk_chgback set"
         + " final_close =''"
         + ", clo_result =''"
         //+", fst_reason_code =?"
         + ", close_add_date =''"
         + ", close_add_user =''"
         + ", close_apr_date =''"
         + ", close_apr_user =''"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where 1=1"
         + " and rowid = ? ";
   
   setRowId(1,wp.itemStr("rowid"));   
   sqlExec(strSql);
   
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback.[取消結案] error; kk[%s]", colStr("A.ctrl_seqno"));
      return rc;
   }

   return rc;
}

@Override
public int dataProc() {
   // TODO Auto-generated method stub
   return 0;
}

}
