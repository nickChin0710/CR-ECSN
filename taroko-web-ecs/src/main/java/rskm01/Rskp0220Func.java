package rskm01;
/**扣款撥款整批登錄覆核-主管作業
 * 2020-0903   JH    modify
 * V.2018-0419
 * */

import busi.FuncProc;

public class Rskp0220Func extends FuncProc {
String kk1 = "";
taroko.base.CommDate zzdate = new taroko.base.CommDate();

@Override
public int querySelect() {
   // TODO Auto-generated method stub
   return 0;
}

@Override
public int dataSelect() {
   // TODO Auto-generated method stub
   return 0;
}

void selectRskChgback(String rowid) {
   strSql = "select mod_seqno, fst_disb_apr_date,"
         + " chg_stage, sub_stage,"
         + " fst_gl_date, sec_gl_date"
         + " from rsk_chgback"
         + " where 1=1"
         + " and rowid = ? ";

   daoTid = "A.";
   
   setRowId(1,rowid);
   
   sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料已不存在");
      return;
   }

   if (colNum("A.mod_seqno") != varsNum("mod_seqno")) {
      errmsg(errOtherModify);
      return;
   }
   return;
}

// --覆核
@Override
public void dataCheck() {
   selectRskChgback(kk1);
   if (rc != 1)
      return;

   if (colEmpty("A.fst_disb_apr_date") == false) {
      errmsg("資料已覆核, 不可再執行覆核");
      return;
   }
   if (pos(",1,3", colStr("A.chg_stage")) <= 0) {
      errmsg("扣款狀態: 不是[一,二扣], 不可[撥款]覆核");
      return;
   }

}

@Override
public int dataProc() {
   msgOK();
   kk1 = varsStr("rowid");

   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "update rsk_chgback set"
         + " fst_disb_apr_date =" + commSqlStr.sysYYmd
         + ", fst_disb_apr_user =?"
         + ", gl_proc_flag ='1'"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";
//         + commSqlStr.where_rowid(kk1);
   setString(modUser);
   setRowId(kk1);

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback.approve error; err=" + sqlErrtext);
   }

   return rc;
}

// --解覆核
void cancelCheck()  throws Exception {
   selectRskChgback(kk1);
   if (rc != 1)
      return;

   if (zzdate.sysComp(colStr("A.fst_disb_apr_date")) != 0) {
      errmsg("資料不是當天覆核, 不可取消放行");
      return;
   }

   if (colEq("A.chg_stage", "1") && !colEmpty("A.fst_gl_date")) {
      errmsg("[扣款撥款]已啟帳, 不可取消放行");
      return;
   }
   if (colEq("A.chg_stage", "3") && !colEmpty("A.sec_gl_date")) {
      errmsg("[(二)扣款撥款]已啟帳, 不可取消放行");
      return;
   }
}

public int cancelProc() throws Exception  {
   msgOK();
   kk1 = varsStr("rowid");

   cancelCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "update rsk_chgback set"
         + " fst_disb_apr_date =''"
         + ", fst_disb_apr_user =''"
         + ", gl_proc_flag ='0'"
         + "," + commSqlStr.setModxxx(modUser, modPgm)
         + " where rowid =?";
//         + commSqlStr.where_rowid(kk1);
   setRowId(1,kk1);
   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback.unAppr error; err=" + sqlErrtext);
   }

   return rc;
}

}
