package rskm01;
/**
 * 2021-0506.6909    JH    rsk_chgback.fst_send_date=sysDate
 * 2020-0116   JH    update FST_xxx
 */

import busi.FuncProc;

public class Rskp0215Func extends FuncProc {

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

@Override
public void dataCheck() {
   sqlWhere = " where reference_no = ? and reference_seq = ? ";
   Object[] parms = new Object[] {varsStr("reference_no"), varsStr("reference_seq")};
     
   if (this.isOtherModify("rsk_chgback", sqlWhere, parms)) {	   
	   wp.log(sqlWhere, parms);
	   return;
   }   
}

@Override
public int dataProc() {
   dataCheck();
   if (rc != 1) {
      return rc;
   }
//		ddd("ct:"+vars_num("chg_times"));
   updateFST();
   return rc;
}

public int updateFST() {
/*
	  UPDATE RSK_CHGBACK
		 SET	  FST_REVERSE_MARK= 'R',
				  FST_SEND_DATE   = :ls_date,
				  FST_SEND_FLAG   = '1'
       WHERE  RSK_CHGBACK.REFERENCE_NO  = :reference_no AND
		        RSK_CHGBACK.REFERENCE_SEQ = :reference_seq ;
* */
   strSql = "update rsk_chgback set send_flag ='1', send_apr_flag ='N' , fst_reverse_mark ='R' , fst_send_date =? "+
         commSqlStr.modxxxSet(modUser, modPgm) +
         " where reference_no =? and reference_seq =?";
   setString(wp.sysDate);
   setString(varsStr("reference_no"));
   setDouble(varsNum("reference_seq"));

   sqlExec(strSql);
   if (sqlRowNum <= 0) {
      errmsg("update rsk_chgback error; " + this.getMsg());
   }
   return rc;

}
//	public int update_SEC(){
//		strSql="update rsk_chgback set"
//				+"  sub_stage ='30' "
//				+", send_flag ='1' "
//				+", send_apr_flag = 'N' "
//				+", sec_reverse_mark ='R'"
//				+", sec_reverse_date = to_char(sysdate,'yyyymmdd')"
//				+", sec_send_date = to_char(sysdate,'yyyymmdd') "
//				+" , mod_time = sysdate"
//				+", mod_user =:mod_user "
//				+", mod_pgm =:mod_pgm "
//				+", mod_seqno =mod_seqno+1 "
//				+" where reference_no=:reference_no"
//				+" and reference_seq=:reference_seq"
//
//				;
//				setString("mod_user", wp.loginUser);
//				setString("mod_pgm", "rskp0215");
//				var2Parm_ss("reference_no");
//				var2Parm_ss("reference_seq");
//				ddd("sql_where"+sql_where);
//				sqlExec(strSql);
//				ddd("sql_nrow:"+sql_nrow);
//			if (sql_nrow<=0) {
//				errmsg("update rsk_chgback error; "+this.getMsg());
//			}
//
//			return rc;
//		}
}
