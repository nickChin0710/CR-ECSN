package rskm05;
/** 流通卡不良記錄通報匯入處理
 * 2023-0828   JH    deleteAll
 * 2019-1212:  Alex  from_type fix
 * 2019-0620:  JH    p_xxx >>acno_p_xxx
   1080315:    JH    bugfix

* */

import busi.FuncAction;

public class Rskp2110Func extends FuncAction {
  String supFlag = "";
  String idPSeqno = "";
  String mIdPSeqno = "";
  String idno = "";
  String corpNo = "";
  String corpPSeqno = "";
  private int ilRow = -1;

  busi.SqlPrepare ttIdin = new busi.SqlPrepare();
  busi.SqlPrepare ttAnno = new busi.SqlPrepare();

  @Override
  public void dataCheck() {
	idPSeqno = "";
	corpPSeqno = "";
	if(idno.isEmpty() == false) {		
		if (checkIdno(idno) == false) {			
		    errmsg("非本行卡友");
		    return;
		}
		selectMajor();
	}	else	{
		if(checkCorp(corpNo) == false) {
			errmsg("非本行公司戶");
			return ;
		}
	}        
  }

  boolean checkIdno(String aIdno) {
    idPSeqno = "";
    String sql1 = "select id_p_seqno " + " from crd_idno  " + " where id_no =? ";
    setString2(1, aIdno);
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      idPSeqno = colStr("id_p_seqno");
      return true;
    }
    return false;
  }
  
  boolean checkCorp(String aCorpNo) {
	  corpPSeqno = "";
	  String sql1 = "select corp_p_seqno from crd_corp where corp_no = ? ";
	  setString(1,aCorpNo);
	  sqlSelect(sql1);
	  if(sqlRowNum > 0 ) {
		  corpPSeqno = colStr("corp_p_seqno");
		  return true ;
	  }
	  return false ;
  }
  
  void selectMajor() {
    supFlag = "N";
    mIdPSeqno = "";

    String sql1 = "select DISTINCT major_id_p_seqno " + " from crd_card "
        + " where id_p_seqno =:id_p_seqno " + " and sup_flag = '1' " + " and current_code = '0' ";
    setString("id_p_seqno", idPSeqno);
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      supFlag = "Y";
      mIdPSeqno = colStr("major_id_p_seqno");
    }
  }

  @Override
  public int dbInsert() {
    msgOK();

    ilRow = varsInt("proc_row");
    idno = wp.colStr(ilRow, "id_no");
    corpNo = wp.colStr(ilRow,"corp_no");
    String lsAnnouType = wp.colStr(ilRow, "annou_type");

    // -ID失效-
    if (eq(lsAnnouType, "A")) {
      return insertIdInvalid(ilRow);
    }

    return insertAnnou(ilRow);
  }

  int insertIdInvalid(int ll) {
    msgOK();
    String lsIdno = wp.colStr(ll, "id_no");
    String lsBlock4 = wp.colStr(ll, "block_reason4");
    idPSeqno = "";

    // -102-01-28-
    strSql = "select count(*) as xx_cnt" + " from rsk_id_invalid" + " where crt_date ="
        + commSqlStr.sysYYmd + " and id_no =?" + " and from_type ='1'";
    setString2(1, lsIdno);
    sqlSelect(strSql);
    if (sqlRowNum > 0 && colInt("xx_cnt") > 0) {
      errmsg("ID重覆");
      return rc;
    }

    strSql = "select id_p_seqno from crd_idno where id_no =?" + commSqlStr.rownum(1);
    setString2(1, lsIdno);
    sqlSelect(strSql);
    if (sqlRowNum > 0) {
      idPSeqno = colStr("id_p_seqno");
    }
    colSet("card_cnt", "");
    colSet("sup_cnt", "");
    if (empty(idPSeqno) == false) {
      strSql = "select sum(decode(sup_flag,'0',1,0)) as card_cnt"
          + ", sum(decode(sup_flag,'1',1,0)) as sup_cnt" + " from crd_card" + " where id_p_seqno =?"
          + " and current_code ='0'";
      setString2(1, idPSeqno);
      sqlSelect(strSql);
      if (sqlRowNum < 0) {
        errmsg("卡檔錯誤");
        return rc;
      }
    }

    ttIdin.sql2Insert("rsk_id_invalid");
    ttIdin.addsqlParm(" crt_date", commSqlStr.sysYYmd);
    ttIdin.addsqlParm(",?", ", id_no", lsIdno);
    ttIdin.addsqlParm(",?", ", id_p_seqno", idPSeqno);
    ttIdin.addsqlParm(",?", ", from_type", "1");
    ttIdin.addsqlParm(", from_seqno", ", lpad(ecs_modseq.nextval,10,'0')");
    ttIdin.addsqlParm(",?", ", card_cnt", colInt("card_cnt"));
    ttIdin.addsqlParm(",?", ", card_sup_cnt", colInt("sup_cnt"));
    ttIdin.addsqlParm(",?", ", block_reason2", lsBlock4);
    ttIdin.modxxx(modUser, modPgm);
    sqlExec(ttIdin.sqlStmt(), ttIdin.sqlParm());
    if (sqlRowNum <= 0) {
      if (sqlDupl) {
        errmsg("ID重覆");
      } else
        errmsg("匯入失敗");
    }

    return rc;
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
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  int insertAnnou(int ll) {
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    ttAnno.sql2Insert("rsk_bad_annou");
    ttAnno.addsqlParm("?", "crt_date", wp.colStr(ll, "crt_date"));
    ttAnno.ppstr2("from_type", wp.colStr(ll, "from_type"));
    ttAnno.ppstr2("id_no", idno);
    ttAnno.ppstr2("corp_no", wp.colStr(ll, "corp_no"));
    ttAnno.ppstr2("chi_name", wp.colStr(ll, "chi_name"));
    ttAnno.ppstr2("imp_file", wp.colStr("imp_file"));
    ttAnno.ppstr2("annou_type", wp.colStr(ll, "annou_type"));
    ttAnno.ppstr2("stop_reason", wp.colStr(ll, "stop_reason"));
    ttAnno.ppstr2("stop_date", wp.colStr("stop_date"));
    ttAnno.ppstr2("bank_no", wp.colStr("bank_no"));
    ttAnno.ppstr2("bank_name", wp.colStr(ll, "bank_name"));
    ttAnno.ppstr2("block_reason4", wp.colStr(ll, "block_reason4"));
    ttAnno.ppstr2("crt_user", wp.colStr(ll, "crt_user"));
    ttAnno.ppint2("id_code_rows", wp.colInt("id_code_rows"));
    ttAnno.ppstr2("id_p_seqno", idPSeqno);
    ttAnno.ppstr2("corp_p_seqno", corpPSeqno);
    ttAnno.ppstr2("has_sup_flag", supFlag);
    ttAnno.modxxx(modUser, modPgm);

    this.sqlExec(ttAnno.sqlStmt(), ttAnno.sqlParm());
    if (sqlRowNum <= 0) {
      if (sqlDupl) {
        errmsg("ID重覆");
      } else {
        errmsg("rsk_bad_annou error");
        log("Insert rsk_bad_annou error, " + sqlErrtext);
      }
    }

    if (this.eqIgno(supFlag, "Y")) {
      insertAnno2(ll);
      if (rc != 1) {
        return rc;
      }
    }

    return rc;
  }

  int insertAnno2(int ll) {
    ttAnno.sql2Insert("rsk_bad_annou");
    ttAnno.addsqlParm("?", "crt_date", wp.colStr(ll, "crt_date"));
    ttAnno.ppstr2("from_type", wp.colStr(ll, "from_type") + "1");
    ttAnno.ppstr2("id_no", idno);
    ttAnno.ppstr2("corp_no", wp.colStr(ll, "corp_no"));
    ttAnno.ppstr2("major_id_p_seqno", mIdPSeqno);
    ttAnno.ppstr2("chi_name", wp.colStr(ll, "chi_name"));
    ttAnno.ppstr2("imp_file", wp.colStr("imp_file"));
    ttAnno.ppstr2("annou_type", wp.colStr(ll, "annou_type"));
    ttAnno.ppstr2("stop_reason", wp.colStr(ll, "stop_reason"));
    ttAnno.ppstr2("stop_date", wp.colStr("stop_reason"));
    ttAnno.ppstr2("bank_no", wp.colStr("bank_no"));
    ttAnno.ppstr2("bank_name", wp.colStr(ll, "bank_name"));
    ttAnno.ppstr2("block_reason4", wp.colStr(ll, "block_reason4"));
    ttAnno.ppstr2("crt_user", wp.colStr(ll, "crt_user"));
    ttAnno.ppint2("id_code_rows", wp.colInt("id_code_rows"));
    ttAnno.ppstr2("id_p_seqno", idPSeqno);
    ttAnno.ppstr2("has_sup_flag", supFlag);
    ttAnno.modxxx(modUser, modPgm);

    this.sqlExec(ttAnno.sqlStmt(), ttAnno.sqlParm());
    if (sqlRowNum <= 0) {
      if (sqlRowNum <= 0) {
        if (sqlDupl) {
          errmsg("ID重覆");
        } else {
          errmsg("rsk_bad_annou(sup) error");
          log("Insert rsk_bad_annou(sup) error, " + sqlErrtext);
        }
      }
    }

    return rc;
  }
  
  public int deleteAll() throws Exception {
	  msgOK();
	  String ls_crtDate =wp.itemStr("ex_crt_date");
	  if (empty(ls_crtDate)) {
	     errmsg("匯入日期: 不可空白");
	     return -1;
     }
	  strSql = "delete rsk_bad_annou where proc_flag <> 'Y' and crt_date = ? ";
	  setString(1, ls_crtDate);
	  sqlExec(strSql);
	  if(rc<0)
		  return -1;
	  else
		  return 1;
  }
  
}
