package rskm01;
/* 調單回單及扣款/預仲到期天數參數維護 V.2018-0118
 * 2018-0118:	JH		modify
 *
 * */

public class Rskm0510Func extends busi.FuncEdit {

String kk1 = "", kk2 = "";

//public Rskm0510_func(Connection con1) {
//	this.conn = con1;
//}

@Override
public int querySelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public int dataSelect() {
   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

@Override
public void dataCheck() {
   kk1 = varsStr("bin_type");
   kk2 = varsStr("trans_type");
//	ddd("kk="+kk1+","+kk2);
   if (isEmpty(kk1) || isEmpty(kk2)) {
      errmsg("[卡別, 交易類別] 不可空白");
      return;
   }

   if (this.isAdd()) {
      return;
   }
   
   sqlWhere = " where bin_type = ? and trans_type = ? and nvl(mod_seqno,0) = ? ";
   Object[] parms = new Object[] {kk1, kk2, varModseqno()};
   
   if (this.isOtherModify("ptr_rskinterval", sqlWhere, parms)) {	   
	   return;
   }      
}

//void dispParm() {
//   String[] col = new String[]{"bin_type","trans_type","return_day","fst_cb_day","represent_day","sec_cb_day","pre_arbit_day","pre_comp_day"};
//   for (String col1 : col) {
//	   wp.d
//      ddd("-JJJ->" + col1 + "=|" + varsStr(col1) + "|");
//   }
//}

@Override
public int dbInsert() {
   msgOK();
   actionInit("A");
   dataCheck();
   if (rc != 1) {
      return rc;
   }

   strSql = "insert into ptr_rskinterval (" +
         "bin_type ," +   //1
         "trans_type ," +
         "acq_type ," +
         "return_day ," +
         "fst_cb_day ," +   //5
         "represent_day ," +
         "sec_cb_day ," +
         "pre_arbit_day ," +
         "pre_comp_day, " + //9
         "warn_day ," +      //10
         "pre_comp_day2 , "
         + " mod_time, mod_user, mod_pgm, mod_seqno "
         + " ) values ("
         + " ?,?,?,?,?,?,?,?,?,0,?"
         + ",sysdate, ?, ?, 1"
         + " )";
   //--
   Object[] param = new Object[]{
         kk1, kk2, varsStr("acq_type"), varsNum("return_day"),varsNum("fst_cb_day"),
         varsNum("represent_day"),varsNum("sec_cb_day"),varsNum("pre_arbit_day"),varsNum("pre_comp_day"),
         varsNum("pre_comp_day2"),varsStr("mod_user"), varsStr("mod_pgm")
   };
   sqlExec(strSql, param);
   if (sqlRowNum == 0) {
      errmsg("insert PTR_RSKINTERVAL error; err=" + sqlErrtext);
   }

   return rc;
}


@Override
public int dbUpdate() {
   msgOK();
   //disp_parm();
   actionInit("U");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "update ptr_rskinterval set "
         + " return_day =?, " +
         "fst_cb_day =?, " +
         "represent_day =?, " +
         "sec_cb_day =?, " +
         "pre_arbit_day =?, " +
         "pre_comp_day =?, " +
         "pre_comp_day2 =?, " +
         " mod_user =?, mod_time=sysdate, mod_pgm =?"
         + ", mod_seqno =nvl(mod_seqno,0)+1"
         + " where bin_type = ? and trans_type = ? and nvl(mod_seqno,0) = ? "
   ;
   Object[] param = new Object[]{
         varsNum("return_day"), varsNum("fst_cb_day"), varsNum("represent_day"), varsNum("sec_cb_day")
         , varsNum("pre_arbit_day"), varsNum("pre_comp_day"), varsNum("pre_comp_day2"), varsStr("mod_user"), varsStr("mod_pgm"),
         kk1,kk2,varModseqno()
   };
   rc = sqlExec(strSql, param);
   if (sqlRowNum <= 0) {
      errmsg("update PTR_RSKINTERVAL error; " + this.sqlErrtext);
   }
   return rc;
}

@Override
public int dbDelete() {
   msgOK();
   actionInit("D");
   dataCheck();
   if (rc != 1)
      return rc;

   strSql = "delete ptr_rskinterval "
		  + " where bin_type = ? and trans_type = ? and nvl(mod_seqno,0) = ? ";
   
   Object[] param = new Object[]{kk1,kk2,varModseqno()};
   
   rc = sqlExec(strSql,param);
   if (sqlRowNum <= 0) {
      errmsg("delete PTR_RSKINTERVAL error; " + this.sqlErrtext);
   }
   return rc;
}

}
