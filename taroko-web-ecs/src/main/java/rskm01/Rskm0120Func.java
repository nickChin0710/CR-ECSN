/*rskm0120	調單作業結案維護 V.2018-1005.jh
 *
 */
package rskm01;

public class Rskm0120Func extends busi.FuncProc {

String kk1, kk2;

@Override
public int querySelect() {
   return 0;
}

@Override
public int dataSelect() {
   return 0;
}

@Override
public void dataCheck() {
   selectRskReceipt();
   if (rc != 1)
      return;

   if (!colEmpty("A.close_apr_date") || eq("A.rept_status", "80")) {
      errmsg("主管已覆核, 不可異動");
   }
}

void selectRskReceipt() {
   daoTid = "A.";
   strSql = "select ctrl_seqno, bin_type, reference_no, rept_status, close_apr_date "
         + " from rsk_receipt "
         + " where rowid =? and mod_seqno =?"
   ;
//		+ " " + "where ctrl_seqno=:ctrl_seqno and bin_type=:bin_type and mod_seqno=:mod_seqno ";
//	var2Parm_ss("ctrl_seqno");
//	var2Parm_ss("bin_type");
//	var2Parm_num("mod_seqno");
   setRowId(varsStr("rowid"));
   setDouble(varsNum("mod_seqno"));
   this.sqlSelect(strSql);
   if (sqlRowNum <= 0) {
      errmsg("資料不存在OR已被修改");
      return;
   }
}

@Override
public int dataProc() {
   dataCheck();
   if (rc != 1)
      return rc;

   // oorept.wp.item_set("rowid",varsStr("rwoid"));
   if (varEmpty("proc_result")) {
      strSql = "update rsk_receipt set"
            + " rept_status ='30' "
            + ", proc_result =:proc_result "
            + ", recv_date ='' "
            + ", fees_flag ='' "
            + ", fees_amt ='0' "
            + ", close_add_user ='' "
            + ", close_add_date ='' "
            + ", " + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";

      setRowId("rowid", varsStr("rowid"));
      setString("proc_result", varsStr("proc_result"));

      sqlExec(strSql);
//		ddd("rc_rskm0120UP:" + rc + "sqlRowNum:::" + sqlRowNum);
//		ddd("rowiD" + varsStr("rowid"));
      if (sqlRowNum <= 0) {
         errmsg("update rsk_receipt error");
         return rc;
      }
   }
   else {
//      +" rept_status ='80' "
//            +", close_apr_user =:mod_user "
//            +", close_apr_date =to_char(sysdate,'yyyymmdd') "
      strSql = "update rsk_receipt set"
            + " rept_status =:rept_status "
            + ", proc_result =:proc_result "
            + ", recv_date =:recv_date "
            + ", fees_flag =:fees_flag "
            + ", fees_amt =:fees_amt "
            + ", close_add_user =:close_add_user "
            + ", close_add_date =:close_add_date "
            + ", close_apr_user =:close_apr_user"
            + ", close_apr_date =:close_apr_date"
            + "," + commSqlStr.setModxxx(modUser, modPgm)
            + " where rowid =:rowid ";

//		if (eq(varsStr("fees_flag"),"Y")) {
      setString("rept_status", "60");
      setString("close_apr_date", "");
      setString("close_apr_user", "");
//      }
//      else {
//         ppp("rept_status","80");
//         ppp("close_apr_date",wp.sysDate);
//         ppp("close_apr_user",mod_user);
//      }
      var2ParmStr("proc_result");
      var2ParmStr("recv_date");
      var2ParmStr("fees_flag");
      var2ParmNum("fees_amt");
      setString("close_add_date", wp.sysDate);
      setString("close_add_user", modUser);
      setRowId("rowid", varsStr("rowid"));

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
         return rc;
      }
   }

//	// --不收費直接結案
//	if (!eq_igno(varsStr("fees_flag"), "Y")) {
//      rskm01.Rsk_receipt oorept = new rskm01.Rsk_receipt();
//      oorept.setConn(wp);
//      oorept.is_rowid = varsStr("rowid");
//      oorept.is_ctrl_seqno =col_ss("A.ctrl_seqno");
//      oorept.is_refer_no =col_ss("A.reference_no");
//		rc = oorept.rskp0120_Update();
//		if (rc != 1) {
//			errmsg(oorept.getMsg());
//			return rc;
//		}
//	}

   return rc;
}
}
