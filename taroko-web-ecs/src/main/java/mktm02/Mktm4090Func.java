/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-02  V1.00.01  ryan       program initial                            *
* 109-04-20  v1.00.04  Andy       Update add throws Exception    
* 112-02-16  V1.00.05  Machao     sync from mega & updated for project coding standard     
* 112-06-12  V1.00.06  Machao     當帳戶類別,團體代號,卡種有存在資料資,對應旗標正確寫入 , 調整  * 
******************************************************************************/

package mktm02;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Mktm4090Func extends FuncEdit {
    String kkMchtNo = "",kkRdmSeqno="";
    String RdmBinFalg ="";

    public Mktm4090Func(TarokoCommon wr) {
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
    	kkMchtNo = wp.itemStr2("rdm_mchtno");
		kkRdmSeqno = wp.itemStr2("rdm_seqno");
    	if(this.isAdd()){
    		if(empty(kkRdmSeqno)||kkRdmSeqno.equals("0")){
    			String sqlSelect="select max(rdm_seqno) as l_seqno from ptr_redeem_t where rdm_mchtno = :s_mchtno ";
    			setString("s_mchtno",kkMchtNo);
    			sqlSelect(sqlSelect);
    			kkRdmSeqno = colStr("l_seqno");
    			if(empty(kkRdmSeqno)||kkRdmSeqno.equals("0")){
    				sqlSelect="select max(rdm_seqno) as l_seqno from ptr_redeem where rdm_mchtno = :s_mchtno ";
        			setString("s_mchtno",kkMchtNo);
        			sqlSelect(sqlSelect);
        			kkRdmSeqno = colStr("l_seqno");
        			if(empty(kkRdmSeqno)||kkRdmSeqno.equals("0")){
        				kkRdmSeqno = "0";
        			}
    			}
    			kkRdmSeqno = (Long.parseLong(kkRdmSeqno) + 1)+"";
    		}
    		
    	}else{
    		//-other modify-
    		sqlWhere = " where rdm_mchtno = ?  and rdm_seqno = ? and nvl(mod_seqno,0) = ?";
    		Object[] param = new Object[] { kkMchtNo,kkRdmSeqno,wp.modSeqno() };
    		if(isOtherModify("ptr_redeem_t", sqlWhere, param)){
    			errmsg("請重新查詢");
    			return;
    		}
    	}
    	
// 當帳戶類別,團體代號,卡種有存在資料資,對應旗標正確寫入 , 調整
    	if(this.isAdd() || this.isUpdate()){
    		String sql1="select count(*) as c1 from ptr_redeem_dtl1_t where mcht_no = :mchtno and dtl_kind = 'ACCT-TYPE'";
			setString("mchtno",kkMchtNo);
			sqlSelect(sql1);
			int a = colInt("c1");
			if(a==0) {
				RdmBinFalg += "N";
			}else {
				RdmBinFalg += "Y";
			}
			
			String sql2="select count(*) as c2 from ptr_redeem_dtl1_t where mcht_no = :mchtno and dtl_kind = 'GROUP-CODE'";
			setString("mchtno",kkMchtNo);
			sqlSelect(sql2);
			int b = colInt("c2");
			if(b==0) {
				RdmBinFalg += "N";
			}else {
				RdmBinFalg += "Y";
			}
			
			String sql3="select count(*) as c3 from ptr_redeem_dtl1_t where mcht_no = :mchtno and dtl_kind = 'CARD-TYPE'";
			setString("mchtno",kkMchtNo);
			sqlSelect(sql3);
			int c = colInt("c3");
			if(c==0) {
				RdmBinFalg += "N";
			}else {
				RdmBinFalg += "Y";
			}
    	}
    }

    @Override
    public int dbInsert() {
        actionInit("A");
        dataCheck();
        msgOK();
        if (rc != 1){
            return rc;
        }
        busi.SqlPrepare sp=new SqlPrepare();
        sp.sql2Insert("ptr_redeem_t");
        sp.ppstr("rdm_mchtno",kkMchtNo);
        sp.ppstr("rdm_seqno",kkRdmSeqno);
        sp.ppstr("rdm_strdate",wp.itemStr2("rdm_strdate"));
        sp.ppstr("rdm_enddate",wp.itemStr2("rdm_enddate"));
        sp.ppnum("rdm_discrate",wp.itemNum("rdm_discrate"));
        sp.ppnum("rdm_discamt",wp.itemNum("rdm_discamt"));
        sp.ppnum("rdm_unitpoint",wp.itemNum("rdm_unitpoint"));
        sp.ppnum("rdm_unitamt",wp.itemNum("rdm_unitamt"));
        sp.ppstr("RDM_BINFLAG",RdmBinFalg);
        sp.ppnum("rdm_destamt",wp.itemNum("rdm_destamt"));
        sp.ppstr("rdm_discratefg",wp.itemStr2("rdm_discratefg"));
        sp.ppstr("mod_audcode","A");
        sp.ppstr("crt_user",wp.loginUser);
        sp.ppstr("mod_user",wp.loginUser);
        sp.ppstr("crt_date",wp.sysDate);
        sp.addsql(", mod_time ",", sysdate ");
		sp.ppstr("mod_pgm",wp.modPgm());
		sp.ppnum("mod_seqno",1);
		rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
		if(rc==1)
			detlInsert();
        return rc;
    }

    @Override
    public int dbUpdate() {
        actionInit("U");
        dataCheck();
        if(rc != 1){
            return rc;
        }
        busi.SqlPrepare sp=new SqlPrepare();
        sp.sql2Update("ptr_redeem_t");
        sp.ppstr("rdm_mchtno",kkMchtNo);
        sp.ppstr("rdm_seqno",kkRdmSeqno);
        sp.ppstr("rdm_strdate",wp.itemStr2("rdm_strdate"));
        sp.ppstr("rdm_enddate",wp.itemStr2("rdm_enddate"));
        sp.ppnum("rdm_discrate",wp.itemNum("rdm_discrate"));
        sp.ppnum("rdm_discamt",wp.itemNum("rdm_discamt"));
        sp.ppnum("rdm_unitpoint",wp.itemNum("rdm_unitpoint"));
        sp.ppnum("rdm_unitamt",wp.itemNum("rdm_unitamt"));
        sp.ppstr("RDM_BINFLAG",RdmBinFalg);
        sp.ppnum("rdm_destamt",wp.itemNum("rdm_destamt")); 
        sp.ppstr("rdm_discratefg",wp.itemStr2("rdm_discratefg"));
        sp.ppstr("mod_audcode","U");
        sp.addsql(", mod_time =sysdate","");
        sp.ppstr("mod_user",wp.loginUser);
      	sp.ppstr("mod_pgm",wp.modPgm());
      	sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1","");
        sp.sql2Where("where rdm_mchtno=?", kkMchtNo);
        sp.sql2Where("and rdm_seqno=?", kkRdmSeqno);
        rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
	
        return rc;
        
        
    }


    @Override
    public int dbDelete() {
    	actionInit("D");
        dataCheck();

        if(rc != 1){
            return rc;
        }
        //delete bil_merchant_dtl_t
        strSql = "delete ptr_redeem_t where rdm_mchtno = ? and rdm_seqno = ? and nvl(mod_seqno,0) = ?" ;
        
        Object[] param = new Object[] { kkMchtNo,kkRdmSeqno,wp.modSeqno() };
        rc = sqlExec(strSql, param);
 
        return rc;
    }
    
    //bil_installment_dtl_t帳單自動分期類別暫存檔新增
    public int dbInsert2() throws Exception {
        actionInit("A");
        msgOK();
        strSql = "insert into ptr_redeem_dtl1_t ("
        		+ "  mcht_no"
        		+ ", seq_no"
	            + ", dtl_kind "
	            + ", dtl_value "
	            + " ) values ("
	            + " ?,?,?,? "
	            + " )";
        //-set ?value-
        Object[] param = new Object[] { 
        	   wp.itemStr2("mcht_no")
            , varsStr("aa_seq_no")
            , varsStr("aa_kind")
            , varsStr("aa_value")
        };
    
        sqlExec(strSql, param);

        return rc;   
    }    
 
    //bil_installment_dtl_t帳單自動分期類別暫存檔刪除
    public int dbDelete2(String dtlKind) throws Exception {
        actionInit("D");
        msgOK();
        if(rc != 1){
            return rc;
        }
        strSql = "delete ptr_redeem_dtl1_t where mcht_no =? and dtl_kind=? and seq_no=? ";
        Object[] param = new Object[] {wp.itemStr2("mcht_no"),dtlKind,wp.itemStr2("rdm_seqno") };
        rc = sqlExec(strSql, param);
        return rc;
    }
     
    void detlInsert() {

        strSql = "delete ptr_redeem_dtl1_t where mcht_no =? and seq_no=? "
        		 + " and dtl_kind in ('ACCT-TYPE','GROUP-CODE','CARD-TYPE','MCHT-GROUP') ";
        Object[] param1 = new Object[] {   	  
        		kkMchtNo
                , kkRdmSeqno};
        rc = sqlExec(strSql, param1);

        if(rc == -1){
            return;
        }
        
    	strSql = "insert into ptr_redeem_dtl1_t ("
        		+ "  mcht_no"
        		+ ", seq_no"
	            + ", dtl_kind "
	            + ", dtl_value "
	            + " ) select merchant_no "
	            + " ,seq_no "
	            + " ,dtl_kind "
	            + " ,dtl_value "
	            + " from ptr_redeem_dtl1 "
	            + " where merchant_no = ? "
	            + " and seq_no = ? "
	            + " and dtl_kind in ('ACCT-TYPE','GROUP-CODE','CARD-TYPE','MCHT-GROUP') "
	            ;
        //-set ?value-
        Object[] param2 = new Object[] { 
        	  kkMchtNo
            , kkRdmSeqno
        };
    
        rc = sqlExec(strSql, param2);
        
    	return;
    }

}
