/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-01-24  V1.00.01  ryan       program initial                            *
* 111-10-26  V1.00.03  Machao     sync from mega & updated for project coding standard*
* 112-03-23  V1.00.04  Simon      取消帳務週期檢核                           *
******************************************************************************/

package cycm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Cycm0015Func extends FuncEdit {
    String kk1 = "";  

    public Cycm0015Func(TarokoCommon wr) {
        wp = wr;
        this.conn = wp.getConn();
    }

    @Override
    public int querySelect() {
        // TODO Auto-generated method 
        return 0;
    }

    @Override
    public int dataSelect() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void dataCheck() {
        kk1 = wp.itemStr2("rowid");
        if (this.isAdd()||this.isUpdate())
        {
        //if(wp.itemEmpty("cycle_type")) {
        //  errmsg("帳務週期CYCLE別 必需選擇");
        // 	return;
        //}
        }

        if(!this.isAdd()){
            //-other modify-
            sqlWhere = " where msg_month = ? and acct_type = ? and msg_code = ? and apr_flag='N' and mod_seqno = ?";
            Object[] param = new Object[] { wp.itemStr2("msg_month"),wp.itemStr2("acct_type"),wp.itemStr2("msg_code") ,wp.modSeqno() };
            if(isOtherModify("ptr_billmsg", sqlWhere, param)){
            	 errmsg("請重新查詢");
            	 return;
            }
        }
    }
    
    public void dataCheck2() throws Exception {
		String lsSql = "select count(*) as tot_cnt " 
				+ " from ptr_msgset " 
				+ " where mesg_month = ? "
				+ " and acct_type=? " 
				+ " and mesg_type=? " 
				+ " and set_data=? " 
				+ " and apr_flag=? ";
		Object[] param = new Object[] { 
				wp.itemStr2("kk1_msg_month"), 
				wp.itemStr2("kk2_acct_type"),
				varsStr("aa_mesg_type"),
				varsStr("aa_set_data"), 
				"N" 
				};
		sqlSelect(lsSql, param);

		if (colNum("tot_cnt") > 0) {
			errmsg("與其他帳單簡訊重複");
			wp.alertMesg = "<script language='javascript'> alert('與其他帳單簡訊重複')</script>";
		}else{
			rc=1;
		}
    }
    
    @Override
    public int dbInsert() {
        actionInit("A");
        dataCheck();
        if (rc != 1){
            return rc;
        }
        String kkMsgMonth = wp.itemStr2("msg_month");
        if(empty(kkMsgMonth)){
        	kkMsgMonth = wp.itemStr2("kk_msg_month");
        }
        String kkMsgCode = wp.itemStr2("msg_code");
        if(empty(kkMsgCode)){
        	kkMsgCode = wp.itemStr2("kk_msg_code");
        }
        String kkAcctType = wp.itemStr2("acct_type");
        if(empty(kkAcctType)){
        	kkAcctType = wp.itemStr2("kk_acct_type");
        }
        String kkMsgType = wp.itemStr2("msg_type");
        if(empty(kkMsgType)){
        	kkMsgType = wp.itemStr2("kk_msg_type");
        }
        SqlPrepare sp=new SqlPrepare();
        sp.sql2Insert("ptr_billmsg");  
        sp.ppstr("msg_month",kkMsgMonth);
        sp.ppstr("msg_code",kkMsgCode);
        sp.ppstr("acct_type",kkAcctType);
        sp.ppstr("msg_type",kkMsgType);
        sp.ppstr("cycle_type",wp.itemStr2("cycle_type"));
        sp.ppstr("param1",wp.itemStr2("param1"));
        sp.ppstr("param2",wp.itemStr2("param2"));
        sp.ppstr("param3",wp.itemStr2("param3"));
        sp.ppstr("param4",wp.itemStr2("param4"));
        sp.ppstr("param5",wp.itemStr2("param5"));
        sp.ppstr("apr_flag","N");
        sp.ppstr("stmt_cycle_parm",varsStr("stmt_cycle_parm"));
        sp.ppstr("crt_user",wp.loginUser);
        sp.addsql(", crt_date ",", to_char(sysdate,'yyyymmdd') ");
        sp.addsql(", mod_time ",", sysdate ");
        sp.ppstr("mod_user",wp.loginUser);
        sp.ppstr("mod_pgm",wp.modPgm());
        sp.ppnum("mod_seqno",1);
        sqlExec(sp.sqlStmt(), sp.sqlParm());
        if (sqlRowNum <= 0) {
            errmsg(sqlErrtext);
        }

        return rc;
    }
    
    
    public int dbInsertCard() throws Exception {
    	dataCheck2();
    	 if (rc != 1){
             return rc;
         }
        SqlPrepare sp=new SqlPrepare();
        sp.sql2Insert("ptr_msgset");
        sp.ppstr("set_data",varsStr("aa_set_data"));
        sp.ppstr("mesg_type","2");
        sp.ppstr("mesg_month",wp.itemStr2("kk1_msg_month"));
        sp.ppstr("acct_type",wp.itemStr2("kk2_acct_type"));
        sp.ppstr("mesg_code",wp.itemStr2("kk3_msg_code"));
        sp.ppstr("apr_flag","N");
        sp.addsql(", mod_time ",", sysdate ");
        sp.ppstr("mod_user",wp.loginUser);
        sp.ppstr("mod_pgm",wp.modPgm());
        sp.ppnum("mod_seqno",1);
        sqlExec(sp.sqlStmt(), sp.sqlParm());
        if (sqlRowNum <= 0) {
            errmsg(sqlErrtext);
        }
        return rc;
    }
    public int dbInsertGroup() throws Exception {
    	dataCheck2();
    	if (rc != 1){
            return rc;
        }
    	 SqlPrepare sp=new SqlPrepare();
         sp.sql2Insert("ptr_msgset");
         sp.ppstr("set_data",varsStr("aa_set_data"));
         sp.ppstr("mesg_type","3");
         sp.ppstr("mesg_month",wp.itemStr2("kk1_msg_month"));
         sp.ppstr("acct_type",wp.itemStr2("kk2_acct_type"));
         sp.ppstr("mesg_code",wp.itemStr2("kk3_msg_code"));
         sp.ppstr("apr_flag","N");
         sp.addsql(", mod_time ",", sysdate ");
         sp.ppstr("mod_user",wp.loginUser);
         sp.ppstr("mod_pgm",wp.modPgm());
         sp.ppnum("mod_seqno",1);
         sqlExec(sp.sqlStmt(), sp.sqlParm());
         if (sqlRowNum <= 0) {
             errmsg(sqlErrtext);
         }
         return rc;
     
    }

    public int dbInsertZip() throws Exception {
    	dataCheck2();
   	 	if (rc != 1){
            return rc;
        }
   	 SqlPrepare sp=new SqlPrepare();
        sp.sql2Insert("ptr_msgset");
        sp.ppstr("set_data",varsStr("aa_set_data"));
        sp.ppstr("mesg_type","4");
        sp.ppstr("mesg_month",wp.itemStr2("kk1_msg_month"));
        sp.ppstr("acct_type",wp.itemStr2("kk2_acct_type"));
        sp.ppstr("mesg_code",wp.itemStr2("kk3_msg_code"));
        sp.ppstr("apr_flag","N");
        sp.addsql(", mod_time ",", sysdate ");
        sp.ppstr("mod_user",wp.loginUser);
        sp.ppstr("mod_pgm",wp.modPgm());
        sp.ppnum("mod_seqno",1);
        sqlExec(sp.sqlStmt(), sp.sqlParm());
        if (sqlRowNum <= 0) {
            errmsg(sqlErrtext);
        }
        return rc;
    
   }
    @Override
    public int dbUpdate() {
        actionInit("U");
        dataCheck();
        if(rc != 1){
            return rc;
        }

        SqlPrepare sp=new SqlPrepare();
        sp.sql2Update("ptr_billmsg");
        sp.ppstr("cycle_type",wp.itemStr2("cycle_type"));
        sp.ppstr("param1",wp.itemStr2("param1"));
        sp.ppstr("param2",wp.itemStr2("param2"));
        sp.ppstr("param3",wp.itemStr2("param3"));
        sp.ppstr("param4",wp.itemStr2("param4"));
        sp.ppstr("param5",wp.itemStr2("param5"));
        sp.ppstr("stmt_cycle_parm",varsStr("stmt_cycle_parm"));
        sp.addsql(", mod_time =sysdate","");
        sp.ppstr("mod_user",wp.loginUser);
        sp.ppstr("mod_pgm",wp.modPgm());
        sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1","");
        sp.sql2Where("where msg_month =?", wp.itemStr2("msg_month"));
        sp.sql2Where("and acct_type=?", wp.itemStr2("acct_type"));
        sp.sql2Where("and msg_code=?", wp.itemStr2("msg_code"));
        sp.sql2Where("and apr_flag=?", "N");
        sp.sql2Where("and mod_seqno=?",  wp.modSeqno());
        sqlExec(sp.sqlStmt(), sp.sqlParm());
        if (sqlRowNum <= 0) {
            errmsg(this.sqlErrtext);
        }
        return rc;

    }

    @Override
    public int dbDelete() {
        actionInit("D");
        dataCheck();
        if(rc != 1){
            return rc;
       }
        strSql = "delete ptr_billmsg " + sqlWhere;
        Object[] param = new Object[] { wp.itemStr2("msg_month"),wp.itemStr2("acct_type"),wp.itemStr2("msg_code") ,wp.modSeqno() };
        rc = sqlExec(strSql, param);
        if (sqlRowNum <= 0) {
	        errmsg(this.sqlErrtext);
        }
        dbDelete3();
        return rc;
    }
    
    public int dbDelete2() throws Exception {

        sqlWhere = " where msg_month = ? and acct_type = ? and msg_code = ? and apr_flag='N' ";
        strSql = "delete ptr_billmsg " + sqlWhere;
        Object[] param = new Object[] { wp.itemStr2("kk1_msg_month"),wp.itemStr2("kk2_acct_type"),wp.itemStr2("kk3_msg_code") };
        rc = sqlExec(strSql, param);
        if (sqlRowNum < 0) {
	        errmsg(this.sqlErrtext);
        }
      
        return rc;
    }
    
    public int dbDelete3() {

        sqlWhere = " where mesg_month = ? and acct_type = ? and mesg_type = ? and mesg_code = ? and apr_flag='N' ";
        strSql = "delete ptr_msgset " + sqlWhere;
        Object[] param = new Object[] { wp.itemStr2("msg_month"),wp.itemStr2("acct_type"),wp.itemStr2("msg_type"),wp.itemStr2("msg_code")};
        rc = sqlExec(strSql, param);
        if (sqlRowNum < 0) {
	        errmsg(this.sqlErrtext);
        }
      
        return rc;
    }
    
    public int dbDeleteCard() throws Exception {

        strSql = "delete ptr_msgset  where hex(rowid) = ? " ;
        Object[] param = new Object[] { varsStr("aa_rowid1")};
   
        rc = sqlExec(strSql, param);
      
        if (sqlRowNum < 0) {
	        errmsg(this.sqlErrtext);
        }
        return rc;
    }
    public int dbDeleteGroup() throws Exception {
    	   strSql = "delete ptr_msgset  where hex(rowid) = ? " ;
           Object[] param = new Object[] { varsStr("aa_rowid2")};
      
           rc = sqlExec(strSql, param);
         
           if (sqlRowNum < 0) {
   	        errmsg(this.sqlErrtext);
           }
           return rc;
    }
    
    public int dbDeleteZip() throws Exception {
 	   strSql = "delete ptr_msgset  where hex(rowid) = ? " ;
        Object[] param = new Object[] { varsStr("aa_rowid3")};
   
        rc = sqlExec(strSql, param);
      
        if (sqlRowNum < 0) {
	        errmsg(this.sqlErrtext);
        }
        return rc;
 }

}
