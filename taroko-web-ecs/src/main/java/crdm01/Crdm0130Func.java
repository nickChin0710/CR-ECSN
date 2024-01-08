/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-03  V1.00.01  ryan       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 112-05-10  V1.00.02  Wilson     增加毀損補發要update current_code = '4 '        *
******************************************************************************/
package crdm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdm0130Func extends FuncEdit {
  String gsElectronicCode = "";
  String gsElectronicCodeOld = "";

  public Crdm0130Func(TarokoCommon wr) {
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
      // if(!empty(wp.item_ss("card_type")) && !empty(wp.item_ss("unit_code"))){
      // String ls_sql = "select electronic_code "
      // + "from crd_item_unit "
      // + "where card_type =:crd_type "
      // + "and unit_code =:unit_code ";
      // setString("crd_type",wp.item_ss("card_type"));
      // setString("unit_code",wp.item_ss("unit_code"));
      // sqlSelect(ls_sql);
      // if(sql_nrow > 0){
      // gs_electronic_code = col_ss("electronic_code");
      // gs_electronic_code_old = col_ss("electronic_code");
      // }
      // }
      gsElectronicCode = wp.itemStr("electronic_code");
      gsElectronicCodeOld = wp.itemStr("electronic_code_old");
    }
    if (this.isUpdate()) {
      gsElectronicCode = wp.itemStr("electronic_code");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_emboss_tmp where batchno = ? and recno = ? ";
      Object[] param = new Object[] {varsStr("batchno"), varsStr("recno")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增,請從新查詢");
        return;
      }

    } else {
      // -other modify-
      sqlWhere = " where 1=1 and batchno = ?  and recno=? and nvl(mod_seqno,0) = ?";

      Object[] param =
          new Object[] {wp.itemStr("batchno"), wp.itemStr("recno"), wp.itemStr("mod_seqno")};
      if (this.isOtherModify("crd_emboss_tmp", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
	if ("2".equals(wp.itemStr2("emboss_reason"))) {
		rc = dbUpdateCrdCard("A");
		if (rc != 1)
			return rc;
	}
    
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_emboss_tmp");
    if (!wp.itemStr("emboss_reason").equals("2") && !empty(wp.itemStr("ex_card_no"))) {
      sp.ppstr("card_no", wp.itemStr("ex_card_no"));
    }
    sp.ppstr("reissue_code", wp.itemStr("reissue_code"));
    sp.ppstr("valid_fm", wp.itemStr("valid_fm"));
    sp.ppstr("valid_to", wp.itemStr("valid_to"));

    if (wp.itemStr("emboss_reason").equals("2")) {
      sp.ppstr("emboss_4th_data", varsStr("emboss_4th_data"));
      sp.ppstr("voice_passwd", varsStr("voice_passwd"));
      sp.ppstr("card_no", varsStr("old_card_no"));
    }
    sp.ppstr("mail_type", wp.itemStr("mail_type"));
    sp.ppstr("branch", varsStr("branch"));
    sp.ppstr("mail_branch", wp.itemStr("mail_branch")); // 202190925 add

    sp.ppstr("batchno", varsStr("batchno"));
    sp.ppstr("recno", varsStr("recno"));
    sp.ppstr("emboss_source", varsStr("emboss_source"));
    sp.ppstr("emboss_reason", varsStr("emboss_reason"));
    sp.ppstr("to_nccc_code", varsStr("to_nccc_code"));
    sp.ppstr("nccc_type", varsStr("nccc_type"));
    sp.ppstr("card_type", varsStr("card_type"));
    sp.ppstr("unit_code", varsStr("unit_code"));
    // 20190916 add
    sp.ppstr("electronic_code", gsElectronicCode);
    // sp.ppss("electronic_code_old", gs_electronic_code_old);
    //
    sp.ppstr("old_card_no", varsStr("old_card_no"));
    sp.ppstr("ic_flag", varsStr("ic_flag"));
    sp.ppstr("status_code", varsStr("status_code"));
    sp.ppstr("service_type", wp.itemStr("service_type"));
    if (wp.itemStr("db_reason_code").equals("1")) {
      sp.ppstr("reason_code", varsStr("reason_code"));
    }

    sp.ppstr("apply_id", varsStr("apply_id"));
    sp.ppstr("apply_id_code", varsStr("apply_id_code"));
    sp.ppstr("sup_flag", varsStr("sup_flag"));
    sp.ppstr("pm_id", varsStr("pm_id"));
    sp.ppstr("pm_id_code", varsStr("pm_id_code"));
    if (varsStr("ls_crd_value28").equals("1")) {
      sp.ppstr("major_card_no", varsStr("major_card_no"));
      sp.ppstr("major_valid_fm", varsStr("major_valid_fm"));
      sp.ppstr("major_valid_to", varsStr("major_valid_to"));
    }
    sp.ppstr("group_code", varsStr("group_code"));
    sp.ppstr("source_code", varsStr("source_code"));
    sp.ppstr("corp_no", varsStr("corp_no"));
    sp.ppstr("corp_no_code", varsStr("corp_no_code"));
    sp.ppstr("acct_type", varsStr("acct_type"));
    sp.ppstr("acct_key", varsStr("acct_key"));
    sp.ppstr("chi_name", varsStr("chi_name"));
    sp.ppstr("eng_name", varsStr("eng_name"));
    sp.ppstr("birthday", varsStr("birthday"));
    sp.ppstr("force_flag", varsStr("force_flag"));
    sp.ppnum("credit_lmt", varsNum("credit_lmt"));
    sp.ppstr("old_beg_date", varsStr("old_beg_date"));
    sp.ppstr("old_end_date", varsStr("old_end_date"));
    sp.ppstr("reg_bank_no", varsStr("reg_bank_no"));
    sp.ppstr("risk_bank_no", varsStr("risk_bank_no"));
    sp.ppstr("curr_code", varsStr("curr_code"));
    sp.ppstr("bin_no", wp.itemStr("bin_no"));
    // sp.ppss("remark_20", wp.item_ss("remark_20")); //20190822 add
    sp.ppstr("online_mark", wp.itemStr("online_mark"));

    sp.ppstr("crt_user", wp.loginUser);
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.addsql(", crt_date ", ", to_char(sysdate,'yyyymmdd') ");
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_emboss_tmp");
    if (!wp.itemStr("emboss_reason").equals("2") && !empty(wp.itemStr("ex_card_no"))) {
      sp.ppstr("card_no", wp.itemStr("ex_card_no"));
    }
    sp.ppstr("reissue_code", wp.itemStr("reissue_code"));
    sp.ppstr("valid_fm", wp.itemStr("valid_fm"));
    sp.ppstr("valid_to", wp.itemStr("valid_to"));
    sp.ppstr("mail_type", wp.itemStr("mail_type"));
    sp.ppstr("branch", wp.itemStr("branch"));
    sp.ppstr("mail_branch", wp.itemStr("mail_branch")); // 202190925 add
    sp.ppstr("emboss_reason", wp.itemStr("emboss_reason"));
    sp.ppstr("unit_code", wp.itemStr("unit_code"));
    // 20190916 add
    sp.ppstr("electronic_code", gsElectronicCode);
    // sp.ppss("electronic_code_old", gs_electronic_code_old); //electronic_code_old 不變
    //
    sp.ppstr("old_card_no", wp.itemStr("old_card_no"));
    if (wp.itemStr("emboss_reason").equals("2")) {
      sp.ppstr("emboss_4th_data", wp.itemStr("emboss_4th_data"));
      sp.ppstr("card_no", wp.itemStr("old_card_no"));
    }
    if (wp.itemStr("db_reason_code").equals("1")) {
      sp.ppstr("reason_code", "3");
    } else {
      sp.ppstr("reason_code", "");
    }
    sp.ppstr("eng_name", wp.itemStr("eng_name"));
    // sp.ppss("remark_20", wp.item_ss("remark_20"));
    sp.ppstr("online_mark", wp.itemStr("online_mark"));
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_time = sysdate ", "");
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where(" where batchno=?", wp.itemStr("batchno"));
    sp.sql2Where(" and recno=?", wp.itemStr("recno"));
    sp.sql2Where(" and mod_seqno=?", wp.itemStr("mod_seqno"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());

    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
	if ("2".equals(wp.itemStr2("emboss_reason"))) {
		rc = dbUpdateCrdCard("D");
		if (rc != 1)
			return rc;
	}    

    strSql = "delete crd_emboss_tmp where hex(rowid) = ? and mod_seqno = ? ";
    Object[] param = new Object[] {wp.itemStr("rowid"), wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum == 0) {
      rc = 0;
    }
    return rc;
  }
  
	public int dbUpdateCrdCard(String type) {
		strSql = "update crd_card set ";
		if ("A".equals(type)) {
			strSql += "current_code = '4', " + "oppost_reason = 'S1', " + "oppost_date = to_char(sysdate,'yyyymmdd') ";
		} else {
			strSql += "current_code = '0', " + "oppost_reason = '', " + "oppost_date = '' ";
		}
		strSql += "where 1=1 " + "and card_no = ? ";

		Object[] param = new Object[] { wp.itemStr2("old_card_no") };

		rc = sqlExec(strSql, param);
		if (sqlRowNum <= 0) {
			errmsg(this.sqlErrtext);
		}

		return rc;
	}
}
