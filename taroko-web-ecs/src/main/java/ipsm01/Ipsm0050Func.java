/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-04-12  V1.00.01  ryan       program initial                            *
* 109-04-21  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ipsm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Ipsm0050Func extends FuncEdit {

  public Ipsm0050Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TOD11111
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {

    if (this.isAdd()) {      

    } else {    	
    	//-other modify-
    	if(wp.itemEmpty("rowid") == false) {    		
    		sqlWhere = " where 1=1 and hex(rowid) = ? " + " and nvl(mod_seqno,0) = ?";
    	    Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};
    	    if (this.isOtherModify("ips_comm_parm", sqlWhere, param)) {
    	    	errmsg("請重新查詢 !");
    	        return;
    	    }
    	}      
    }

  }

  @Override
  public int dbInsert() {

    return rc;
  }


  public int ipsInsert() {

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("ips_comm_data");
    sp.ppstr("parm_type", "REJ_AUTH");
    sp.ppstr("data_type", "01");
    sp.ppstr("data_code", varsStr("aa_data_code"));
    sp.ppstr("apr_flag", "Y");
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time ", ", sysdate");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
    if(wp.itemEmpty("rowid")) {
    	busi.SqlPrepare sp = new SqlPrepare();
        sp.sql2Insert("ips_comm_parm");
        sp.ppstr("parm_type", "REJ_AUTH");
        sp.ppint("seq_no", 1);
        sp.ppstr("mcode_cond", wp.itemStr("mcode_cond"));
        sp.ppstr("payment_rate", wp.itemStr("payment_rate"));    
        sp.ppstr("imp_list_cond", wp.itemStr("imp_list_cond"));
        sp.ppstr("apr_user", wp.itemStr2("approval_user"));
        sp.ppstr("apr_date", getSysDate());
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        
        rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    }	else	{
    	busi.SqlPrepare sp = new SqlPrepare();
        sp.sql2Update("ips_comm_parm");
        sp.ppstr("mcode_cond", wp.itemStr("mcode_cond"));
        sp.ppstr("payment_rate", wp.itemStr("payment_rate"));    
        sp.ppstr("imp_list_cond", wp.itemStr("imp_list_cond"));
        sp.ppstr("apr_user", wp.loginUser);
        sp.ppstr("mod_user", wp.loginUser);
        sp.ppstr("mod_pgm", wp.modPgm());
        sp.addsql(", mod_time = sysdate ", ", apr_date = to_char(sysdate,'YYYYMMDD')");
        sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
        sp.sql2Where(" where hex(rowid)=?", wp.itemStr("rowid"));
        sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
        rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    }        
    if (sqlRowNum < 0) {
      rc = -1;
    }
    return rc;
  }

  @Override
  public int dbDelete() {

    return rc;
  }

  public int ipsDelete() {

    strSql = "delete IPS_COMM_DATA where parm_type ='REJ_AUTH' and data_type = '01' ";
    rc = sqlExec(strSql);
    return rc;
  }

  private String dropComma(String data) {
    String buf = "";
    String[] datas = data.split(",");
    for (String dat : datas) {
      buf = buf + dat;
    }
    return buf;
  }
}
