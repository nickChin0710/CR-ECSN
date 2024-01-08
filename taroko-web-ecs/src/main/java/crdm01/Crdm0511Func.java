/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-06  V1.00.00  yash              program initial                            *
* 107-12-23  V1.00.01  ryan              update  mantis 0001966					 *
* 109-04-16  V1.00.02  Yanghan     修改完成後 將crd_employee的id也修改成最新id         *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.04  Justin           update dbc_idno, dba_acno, crd_idno_seqno, and dbc_chg_id
* 111-04/18  V1.00.05  Justin     修改「修改ID」邏輯                         *
* 111/05/11  V1.00.06  Justin     update dba_acno增加acct_holder_id          *
* 111/05/23  V1.00.07  Justin     調整變更ID更新及新增邏輯順序               *
* 111/09/13  V1.00.08  Wilson     update act_acno增加card_indicator = '1'      * 
* 112/05/15  V1.00.09  Wilson     update idno的where條件刪除mod_seqno            *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0511Func extends FuncEdit {
  String mKkIdPSeqno = "";

  public Crdm0511Func(TarokoCommon wr) {
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

    mKkIdPSeqno = wp.itemStr("id_p_seqno");
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from crd_idno where id_p_seqno = ?";
      Object[] param = new Object[] {mKkIdPSeqno};
      sqlSelect(lsSql, param);

      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where id_p_seqno = ? ";
      Object[] param = new Object[] {mKkIdPSeqno};
      if ("N".equals(wp.itemStr("isDebit"))) {
    	  if (isOtherModify("crd_idno", sqlWhere, param)) {
    	        errmsg("更新失敗，資料已被異動，請重新查詢 !");
    	        return;
    	  }
      }else {
    	  if (isOtherModify("dbc_idno", sqlWhere, param)) {
    		    errmsg("更新失敗，資料已被異動，請重新查詢 !");
    	        return;
    	  }
      }
      
    }
  }

  private boolean doesIdnoExistInCrdChgId() {
    String lsSql =
        "select count(*) as tot_cnt from CRD_CHG_ID where old_id_no = ? and old_id_no_code = ?";
    Object[] param = new Object[] {wp.itemStr("h_id_no"), wp.itemStr("h_id_no_code")};
    sqlSelect(lsSql, param);
    if (colNum("tot_cnt") <= 0) {
      return false;
    }
    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into crd_idno (" + " id_p_seqno " + ", xxx "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?, ? " + ", sysdate,?,?,1"
        + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkIdPSeqno // 1
        , wp.itemStr("xxx"), wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
    String aftIdno = wp.itemStr("id_no");
	String aftIdnoCode = empty(wp.itemStr("id_no_code")) ? "0" : wp.itemStr("id_no_code");
	
	String idPSeqnoByNewIDFromCrdIdno = getIdPSeqnoByNewIDFromCrdIdno(aftIdno);
	String idPSeqnoByNewIDFromDbcIdno = getIdPSeqnoByNewIDFromDbcIdno(aftIdno);
	
	// (1)若新ID已存在crd_idno或dbc_idno則做(2)，否則直接做原本的update邏輯
	if (idPSeqnoByNewIDFromCrdIdno != null || idPSeqnoByNewIDFromDbcIdno != null) {
		// (2)若crd_card或dbc_card有存在新ID的卡片，則不可修改(畫面顯示 -> "此ID已存在且向下有卡片，不可修改")，
		//    否則delete crd_idno、dbc_idno、crd_idno_seqno，再做原本的update
		if (doesNewIDExistInCard(idPSeqnoByNewIDFromCrdIdno, idPSeqnoByNewIDFromDbcIdno)) {
			errmsg("此ID已存在且向下有卡片，不可修改");
			return rc;
		}else {
			deleteIdno(idPSeqnoByNewIDFromCrdIdno, idPSeqnoByNewIDFromDbcIdno);
		}
	}

	/** udpate crd_idno **/
    int updateNumber = updateIdno("crd_idno", aftIdno, aftIdnoCode);
    if (rc <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    } 
    
    /** 2022/05/23 Justin **/
    if (updateNumber > 0) {

        if (doesIdnoExistInCrdChgId() == false) {
    		insertCrdChgId();
    		if (rc <= 0) {
    			errmsg(this.sqlErrtext);
    			return rc;
    		}

    	} else {
    		updateCrdChgId();
    		if (rc <= 0) {
    			errmsg(this.sqlErrtext);
    			return rc;
    		}
    	}
        
        updateActAcno(aftIdno, aftIdnoCode);
    	if (rc <= 0) {
    		errmsg(this.sqlErrtext);
    		return rc;
    	}
    }
    
    
    /** udpate dbc_idno **/
    updateNumber = updateIdno("dbc_idno", aftIdno, aftIdnoCode);
    if (rc <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    } 
    
    /** 2022/05/23 Justin **/
    if (updateNumber > 0) {

    	insertDbcChgId(aftIdno, aftIdnoCode);
    	if (rc <= 0) {
    		errmsg(this.sqlErrtext);
    		return rc;
    	}
    	
    	updateDbaAcno(aftIdno, aftIdnoCode);
    	if (rc <= 0) {
    		errmsg(this.sqlErrtext);
    		return rc;
    	}
    }

	updateCrdIdnoSeqno(aftIdno, wp.itemStr("id_p_seqno"));
	if (rc <= 0) {
		errmsg(this.sqlErrtext);
		return rc;
	}

	/***** update mantis 0001966 start *****/
	updateEcsActAcno(aftIdno, aftIdnoCode);
	if (rc < 0) {
		errmsg(this.sqlErrtext);
		return rc;
	}

	/***** update mantis 0001966 end *****/
	
	// 修改成功之後update crd_employee的id
	updateCrdEmployee();
	if (rc <= 0) {
		errmsg(this.sqlErrtext);
		return rc;
	}
    
    return rc;

  }

private int deleteIdno(String idPSeqnoByNewIDFromCrdIdno, String idPSeqnoByNewIDFromDbcIdno) {
	if (idPSeqnoByNewIDFromCrdIdno != null) {
		strSql = "delete crd_idno where id_p_seqno = ? ";
	    Object[] param = new Object[] {idPSeqnoByNewIDFromCrdIdno};
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum <= 0) {
	      errmsg(this.sqlErrtext);
	      return rc;
	    }
	    
	    strSql = "delete crd_idno_seqno where id_p_seqno = ? AND DEBIT_IDNO_FLAG = ? ";
	    param = new Object[] {idPSeqnoByNewIDFromCrdIdno, "N"};
	    rc = sqlExec(strSql, param);
	    
	}
	
	if (idPSeqnoByNewIDFromDbcIdno != null) {
		strSql = "delete dbc_idno where id_p_seqno = ? ";
	    Object[] param = new Object[] {idPSeqnoByNewIDFromDbcIdno};
	    rc = sqlExec(strSql, param);
	    if (sqlRowNum <= 0) {
	      errmsg(this.sqlErrtext);
	      return rc;
	    }
	    
	    strSql = "delete crd_idno_seqno where id_p_seqno = ? AND DEBIT_IDNO_FLAG = ? ";
	    param = new Object[] {idPSeqnoByNewIDFromCrdIdno, "Y"};
	    rc = sqlExec(strSql, param);

	}
	
	
    return rc;
	
}

private boolean doesNewIDExistInCard(String idPSeqnoByNewIDFromCrdIdno, String idPSeqnoByNewIDFromDbcIdno) {
	if (idPSeqnoByNewIDFromCrdIdno != null) {
		String lsSql = "select count(*) as tot_cnt from crd_card where id_p_seqno = ?";
		Object[] param = new Object[] {idPSeqnoByNewIDFromCrdIdno};
	    sqlSelect(lsSql, param);
		if (colNum("tot_cnt") > 0) {
			return true;
		}
	}
	
	if (idPSeqnoByNewIDFromDbcIdno != null) {
		String lsSql = "select count(*) as tot_cnt from dbc_card where id_p_seqno = ?";
		Object[] param = new Object[] {idPSeqnoByNewIDFromDbcIdno};
	    sqlSelect(lsSql, param);
		if (colNum("tot_cnt") > 0) {
			return true;
		}
	}
    
    return false;
}

private String getIdPSeqnoByNewIDFromCrdIdno(String aftIdno) {
	 String lsSql = "select id_p_seqno as idPSeqnoByNewID from crd_idno where id_no = ?";
     Object[] param = new Object[] {aftIdno};
     sqlSelect(lsSql, param);
     if (sqlRowNum > 0) {
    	 return colStr("idPSeqnoByNewID");
     }
     
     return null;
}

private String getIdPSeqnoByNewIDFromDbcIdno(String aftIdno) {
	String lsSql = "select id_p_seqno as idPSeqnoByNewID from dbc_idno where id_no = ?";
    Object[] param = new Object[] {aftIdno};
    sqlSelect(lsSql, param);
    if (sqlRowNum > 0) {
   	 return colStr("idPSeqnoByNewID");
    }
    
    return null;
}

private void updateCrdEmployee() {
	String usSql = " update crd_employee set   id=?  where id =?";
      Object[] param1 = new Object[] {wp.itemStr("id_no"), wp.itemStr("h_id_no")};
      rc = sqlExec(usSql, param1);
}

private void updateCrdChgId() {
	StringBuffer sb = new StringBuffer();
	sb.append(" update crd_chg_id set  id_no=:id_no  , id_no_code = :id_no_code ")
		.append(", id_p_seqno = :id_p_seqno , old_id_p_seqno = :old_id_p_seqno ")
		.append(" ,chi_name  = :chi_name  ,chg_date  = :chg_date  ,apr_user=:apr_user")
		.append(" ,apr_date=:apr_date, src_from = '2'  ,mod_time=sysdate")
		.append(" ,mod_user=:mod_user ,mod_pgm=:mod_pgm ,mod_seqno =nvl(mod_seqno,0)+1")
		.append(" where old_id_no = :old_id_no  and old_id_no_code = :old_id_no_code ");

	setString("id_no", wp.itemStr("id_no"));
	setString("id_no_code", empty(wp.itemStr("id_no_code")) ? "0" : wp.itemStr("id_no_code"));
	setString("apr_user", wp.itemStr("approval_user"));
	setString("id_p_seqno", wp.itemStr("id_p_seqno"));
	setString("old_id_p_seqno", wp.itemStr("old_id_p_seqno"));
	setString("chi_name", wp.itemStr("chi_name"));
	setString("chg_date", getSysDate());
	setString("apr_date", getSysDate());
	setString("mod_user", wp.loginUser);
	setString("mod_pgm", wp.itemStr("mod_pgm"));
	setString("old_id_no", wp.itemStr("h_id_no"));
	setString("old_id_no_code", empty(wp.itemStr("h_id_no_code")) ? "0" : wp.itemStr("h_id_no_code"));
	
	rc = sqlExec(sb.toString());
}

private void insertCrdChgId() {
	StringBuffer bf = new StringBuffer();
	bf.append("insert into crd_chg_id (" + "  old_id_no " + ", old_id_no_code " + ", id_p_seqno ")
		.append(", old_id_p_seqno " + ", id_no " + ", id_no_code " + ", post_jcic_flag ")
		.append(" ,chi_name " + " ,chg_date " + ", crt_user " + ", crt_date " + ", apr_user ")
		.append(", apr_date " + ", src_from " + ", mod_time, mod_user, mod_pgm, mod_seqno")
		.append(" ) values (" + "  ?,?,?,?,?,?,?,?,?,?,?,?,?,?" + ", sysdate,?,?,1" + " )");
	
	Object[] param1 = new Object[] {
		wp.itemStr("h_id_no"),
	    empty(wp.itemStr("h_id_no_code")) ? 0 : wp.itemStr("h_id_no_code"),
	    wp.itemStr("id_p_seqno"), 
	    wp.itemStr("h_id_p_seqno"), 
	    wp.itemStr("id_no"),
	    empty(wp.itemStr("id_no_code")) ? 0 : wp.itemStr("id_no_code"), 
	    "Y",
	    wp.itemStr("chi_name"), 
	    getSysDate(), 
	    wp.loginUser, 
	    getSysDate(),
	    wp.itemStr("approval_user"), 
	    getSysDate(), 
	    "2", 
	    wp.loginUser, 
	    wp.itemStr("mod_pgm")
	    };
	
	rc = sqlExec(bf.toString(), param1);
}

private void insertDbcChgId(String aftIdno, String aftIdnoCode) {

	StringBuffer sb = new StringBuffer();
	sb.append("insert into dbc_chg_id ")
		.append(" (  id , id_code , corp_flag , aft_id , aft_id_code , crt_date , process_flag , ")
		.append(" mod_user  ,mod_time , mod_pgm )")
		.append(" values ( :id , :id_code, :corp_flag, :aft_id, :aft_id_code, :crt_date, :process_flag, ")
		.append(" :mod_user, sysdate, :mod_pgm  ) ");
	setString("id", wp.itemStr("h_id_no"));
	setString("id_code", empty(wp.itemStr("h_id_no_code")) ? "0" : wp.itemStr("h_id_no_code"));
	setString("corp_flag", "N");
	setString("aft_id", aftIdno);
	setString("aft_id_code", aftIdnoCode);
	setString("crt_date", getSysDate());
	setString("process_flag", "Y");
	setString("mod_user", wp.loginUser);
	setString("mod_pgm", wp.itemStr("mod_pgm"));
	
	rc = sqlExec(sb.toString());
	
}

private void updateEcsActAcno(String idno, String idnoCode) {
	StringBuffer bf = new StringBuffer();
	bf.append(" update ecs_act_acno set   acct_key=:acct_key  ,apr_user=:apr_user")
		.append(" ,apr_date=:apr_date ,mod_time=sysdate ,mod_user=:mod_user")
		.append(" ,mod_pgm=:mod_pgm ,mod_seqno =nvl(mod_seqno,0)+1")
		.append(" where id_p_seqno = :id_p_seqno   ");

      setString("acct_key", idno + idnoCode);
      setString("apr_user", wp.itemStr("approval_user"));
      setString("apr_date", getSysDate());
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.itemStr("mod_pgm"));
      setString("id_p_seqno", wp.itemStr("id_p_seqno"));
      rc = sqlExec(bf.toString());
}

private void updateCrdIdnoSeqno(String idno, String idPSeqno) {
	StringBuffer sb = new StringBuffer();
	sb.append("update crd_idno_seqno set")
		.append(" id_no = :id_no ")
		.append(" where id_p_seqno = :id_p_seqno   ");

      setString("id_no", idno);
      setString("id_p_seqno", idPSeqno);
      rc = sqlExec(sb.toString());
}

private void updateDbaAcno(String idno, String idnoCode) {
	StringBuffer sb = new StringBuffer();
	sb.append("update dba_acno ")
		.append(" set acct_key=:acct_key ")
		.append(" ,mod_time=sysdate ,mod_user=:mod_user")
		.append(" ,mod_pgm=:mod_pgm ,mod_seqno =nvl(mod_seqno,0)+1")
		.append(" ,acct_holder_id=:acct_holder_id")
		.append(" where id_p_seqno = :id_p_seqno   ");

      setString("acct_key", idno + idnoCode);
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.itemStr("mod_pgm"));
      setString("acct_holder_id", idno);
      setString("id_p_seqno", wp.itemStr("id_p_seqno"));
      rc = sqlExec(sb.toString());
}
/**
* @ClassName: Crdm0511Func
* @Description: updateActAcno 異動帳戶資料檔的帳戶查詢碼時增加綁定限一般卡(商務卡不需異動)
* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
* @Company: DXC Team.
* @author Wilson
* @version V1.00.08, Sep 13, 2022
*/
private void updateActAcno(String idno, String idnoCode) {
	StringBuffer sb = new StringBuffer();
	sb.append("update act_acno ")
		.append(" set   acct_key=:acct_key  ,apr_user=:apr_user ")
		.append(" ,apr_date=:apr_date ,mod_time=sysdate ,mod_user=:mod_user")
		.append(" ,mod_pgm=:mod_pgm ,mod_seqno =nvl(mod_seqno,0)+1")
		.append(" where id_p_seqno = :id_p_seqno   ")
	    .append(" and card_indicator = '1'   ");

      setString("acct_key", idno + idnoCode);
      setString("apr_user", wp.itemStr("approval_user"));
      setString("apr_date", getSysDate());
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.itemStr("mod_pgm"));
      setString("id_p_seqno", wp.itemStr("id_p_seqno"));
      rc = sqlExec(sb.toString());
}

private int updateIdno(String tableName, String idno, String idnoCode) {
	StringBuffer sb= new StringBuffer();
	sb.append("update ").append(tableName)
		.append(" set  id_no =?  ,id_no_code =? ")
		.append(" , mod_user =?, mod_time=sysdate, mod_pgm =?  , mod_seqno =nvl(mod_seqno,0)+1 ")
		.append(sqlWhere);
	strSql = sb.toString();
    Object[] param = new Object[] {
    	idno,
        idnoCode,
        wp.loginUser,
        wp.itemStr("mod_pgm"), 
        mKkIdPSeqno};
    
    rc = sqlExec(strSql, param);
    return sqlRowNum;
}

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_idno " + sqlWhere;
    Object[] param = new Object[] {mKkIdPSeqno, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
