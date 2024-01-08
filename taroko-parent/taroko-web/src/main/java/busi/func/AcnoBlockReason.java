/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-25   V1.00.02 Justin        zz -> comm 
******************************************************************************/
package busi.func;
/** 帳戶凍結公用程式
 * 2019-1230   JH    busi.func >>ecsfunc
 * 2019-1225   JH    bug-fix
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
   2018-1213:  JH    ccaM2040_XXX
 * 2018-1019:	JH		ccaM2040_XXX
 * 2018-0203:	JH		initial
 * 
 * */

import busi.FuncBase;

public class AcnoBlockReason extends FuncBase {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  public String idPseqno = "", logReason = "", acnoPseqno = "";

  String[] aaBlock = new String[5];
  int iiBlockChg = 0;

  busi.SqlPrepare idsAclg = null;
  busi.SqlPrepare idsOnbat = new busi.SqlPrepare();

  public int ccaM2030Spec(String aCardNo, String aAud) {
    strSql =
        "select acno_p_seqno, acct_type," + " corp_p_seqno, id_p_seqno,"
            + " spec_status, spec_del_date, spec_user, spec_remark , spec_dept_no "
            + " from cca_card_base" + " where card_no =?";
    setString2(1, aCardNo);
    daoTid = "card.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無授權卡片資料, kk[%s]", aCardNo);
      return -1;
    }

    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("rsk_acnolog");

    sp.ppstr2("kind_flag", "C");
    sp.ppstr2("card_no", aCardNo);
    sp.ppstr2("acno_p_seqno", colStr("card.acno_p_seqno"));
    sp.ppstr2("acct_type", colStr("card.acct_type"));
    sp.ppstr2("id_p_seqno", colStr("card.id_p_seqno"));
    sp.ppstr2("corp_p_seqno", colStr("card.corp_p_seqno"));
    sp.ppymd("log_date");
    sp.ppstr2("log_mode", "1");
    sp.ppstr2("log_type", "6");
    sp.ppstr2("log_reason", aAud);
    // sp.ppp("log_not_reason",);
    sp.ppstr2("fit_cond", "Y");
    sp.ppstr2("log_remark", colStr("card.spec_remark"));
    sp.ppstr2("spec_status", colStr("card.spec_status"));
    sp.ppstr2("spec_del_date", colStr("card.spec_del_date"));
    sp.ppstr2("user_dept_no", colStr("card.spec_dept_no"));
    sp.ppstr2("send_ibm_flag", "");
    sp.ppymd("send_ibm_date");
    sp.ppstr2("apr_flag", "Y");
    sp.ppstr2("apr_user", modUser);
    sp.ppymd("apr_date");
    sp.modxxx(modUser, modPgm);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      sqlErr("rsk_acnolog[card].insert");
    }
    return rc;
  }

  public int ccaM2040Update(double aiAcctIdx) {
    strSql =
        "select acno_p_seqno, acct_type," + " corp_p_seqno, id_p_seqno,"
            + " block_reason1, block_reason2, block_reason3," + " block_reason4, block_reason5,"
            + " spec_status, spec_del_date, spec_user, spec_remark," + " block_sms_flag"
            + " from cca_card_acct" + " where card_acct_idx =?";
    setDouble2(1, aiAcctIdx);
    daoTid = "acno.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無授權帳戶資料, kk[%s]", aiAcctIdx);
      return -1;
    }

    idsAclg = new busi.SqlPrepare();
    idsAclg.sql2Insert("rsk_acnolog");
    idsAclg.addsqlParm("kind_flag", "?", "A");
    idsAclg.addsqlParm(", acno_p_seqno", ",?", colStr("acno.acno_p_seqno"));
    idsAclg.addsqlParm(", acct_type", ",?", colStr("acno.acct_type"));
    idsAclg.addsqlParm(", corp_p_seqno", ",?", colStr("acno.corp_p_seqno"));
    idsAclg.addsqlParm(", id_p_seqno", ",?", colStr("acno.id_p_seqno"));
    idsAclg.addsqlYmd(", log_date");
    idsAclg.addsqlParm(", log_mode", ",?", "1");
    idsAclg.addsqlParm(", log_type", ",?", "3");
    idsAclg.addsqlParm(", log_remark", ",?", colStr("acno.spec_remark"));
    idsAclg.addsqlParm(", block_reason", ",?", colStr("acno.block_reason1"));
    idsAclg.addsqlParm(", block_reason2", ",?", colStr("acno.block_reason2"));
    idsAclg.addsqlParm(", block_reason3", ",?", colStr("acno.block_reason3"));
    idsAclg.addsqlParm(", block_reason4", ",?", colStr("acno.block_reason4"));
    idsAclg.addsqlParm(", block_reason5", ",?", colStr("acno.block_reason5"));
    idsAclg.addsqlParm(", spec_status", ",?", colStr("acno.spec_status"));
    idsAclg.addsqlParm(", spec_del_date", ",?", colStr("acno.spec_del_date"));
    idsAclg.addsqlParm(", sms_flag", ",?", colStr("acno.block_sms_flag"));
    idsAclg.addsqlParm(", user_dept_no", ",?", wp.loginDeptNo);
    idsAclg.addsqlParm(", apr_flag", ",?", "Y");
    idsAclg.addsqlYmd(", apr_date");
    idsAclg.addsqlParm(", mod_user", ",?", modUser);
    idsAclg.addsqlDate(", mod_time");
    idsAclg.addsqlParm(", mod_pgm", ",?", modPgm);
    idsAclg.addsqlParm(", mod_seqno", ",?", 1);

    sqlExec(idsAclg.sqlStmt(), idsAclg.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("[戶凍結]insert rsk_acnolog error, kk[%s]", aiAcctIdx);
    }
    return rc;
  }

  public int ccaM2040Delete(double aiAcctIdx) {
    strSql =
        "select acno_p_seqno, acct_type," + " corp_p_seqno, id_p_seqno,"
            + " block_reason1, block_reason2, block_reason3," + " block_reason4, block_reason5,"
            + " spec_status, spec_del_date, spec_user, spec_remark," + " block_sms_flag"
            + " from cca_card_acct" + " where card_acct_idx =?";
    setDouble2(1, aiAcctIdx);
    daoTid = "acno.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無授權帳戶資料, kk[%s]", aiAcctIdx);
      return -1;
    }

    idsAclg = new busi.SqlPrepare();
    idsAclg.sql2Insert("rsk_acnolog");
    idsAclg.addsqlParm("kind_flag", "?", "A");
    idsAclg.addsqlParm(", acno_p_seqno", ",?", colStr("acno.acno_p_seqno"));
    idsAclg.addsqlParm(", acct_type", ",?", colStr("acno.acct_type"));
    idsAclg.addsqlParm(", corp_p_seqno", ",?", colStr("acno.corp_p_seqno"));
    idsAclg.addsqlParm(", id_p_seqno", ",?", colStr("acno.id_p_seqno"));
    idsAclg.addsqlYmd(", log_date");
    idsAclg.addsqlParm(", log_mode", ",?", "1");
    idsAclg.addsqlParm(", log_type", ",?", "4"); // -解凍-
    idsAclg.addsqlParm(", log_remark", ",?", colStr("acno.spec_remark"));
    idsAclg.addsqlParm(", block_reason", ",?", colStr("acno.block_reason1"));
    idsAclg.addsqlParm(", block_reason2", ",?", colStr("acno.block_reason2"));
    idsAclg.addsqlParm(", block_reason3", ",?", colStr("acno.block_reason3"));
    idsAclg.addsqlParm(", block_reason4", ",?", colStr("acno.block_reason4"));
    idsAclg.addsqlParm(", block_reason5", ",?", colStr("acno.block_reason5"));
    idsAclg.addsqlParm(", spec_status", ",?", colStr("acno.spec_status"));
    idsAclg.addsqlParm(", sms_flag", ",?", colStr("acno.block_sms_flag"));
    idsAclg.addsqlParm(", user_dept_no", ",?", wp.loginDeptNo);
    idsAclg.addsqlParm(", apr_flag", ",?", "Y");
    idsAclg.addsqlYmd(", apr_date");
    idsAclg.addsqlParm(", mod_user", ",?", modUser);
    idsAclg.addsqlDate(", mod_time");
    idsAclg.addsqlParm(", mod_pgm", ",?", modPgm);
    idsAclg.addsqlParm(", mod_seqno", ",?", 1);

    sqlExec(idsAclg.sqlStmt(), idsAclg.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("[戶凍結取消]insert rsk_acnolog error, kk[%s]", aiAcctIdx);
    }
    return rc;
  }

  public int trialActionBlock(String aPSeqno, String aBlock4, String aBlock5, String aSpec) {
    if (empty(aBlock4) && empty(aBlock5) && empty(aSpec))
      return 0;

    acnoPseqno = aPSeqno;
    strSql =
        "select B.block_reason1, B.block_reason2, B.block_reason3,"
            + " B.block_reason4, B.block_reason5," + " B.spec_status, B.spec_del_date,"
            + " A.card_indicator as card_indr," + " A.acct_type, A.id_p_seqno, A.corp_p_seqno"
            + " from cca_card_acct B join act_acno A"
            + "   on A.acno_p_seqno =B.acno_p_seqno and B.debit_flag<>'Y'"
            + " where A.acno_p_seqno =?";
    setString2(1, aPSeqno);
    daoTid = "acno.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("查無帳戶資料, kk[%s]", aPSeqno);
      return -1;
    }

    aaBlock[0] = colStr("acno.block_reason1");
    aaBlock[1] = colStr("acno.block_reason2");
    aaBlock[2] = colStr("acno.block_reason3");
    aaBlock[3] = colStr("acno.block_reason4");
    aaBlock[4] = colStr("acno.block_reason5");

    // check block4--
    boolean lbBlock4 = !empty(aBlock4);
    String lsNoBlock = "|05|06|0C|0F|0G|0R|0K|0Q|" + aBlock4;
    if (lbBlock4) {
      if (pos(lsNoBlock, aaBlock[0]) > 0)
        lbBlock4 = false;
      else if (pos(lsNoBlock, aaBlock[1]) > 0)
        lbBlock4 = false;
      else if (pos(lsNoBlock, aaBlock[2]) > 0)
        lbBlock4 = false;
      else if (pos(lsNoBlock, aaBlock[3]) > 0)
        lbBlock4 = false;
      else if (pos(lsNoBlock, aaBlock[4]) > 0)
        lbBlock4 = false;
    }
    // check block5--
    lsNoBlock = "|62|63|64|" + aBlock5;
    boolean lbBlock5 = !empty(aBlock5);
    if (lbBlock5) {
      if (pos(lsNoBlock, aaBlock[0]) > 0)
        lbBlock5 = false;
      else if (pos(lsNoBlock, aaBlock[1]) > 0)
        lbBlock5 = false;
      else if (pos(lsNoBlock, aaBlock[2]) > 0)
        lbBlock5 = false;
      else if (pos(lsNoBlock, aaBlock[3]) > 0)
        lbBlock5 = false;
      else if (pos(lsNoBlock, aaBlock[4]) > 0)
        lbBlock5 = false;
    }
    // check spec_status--
    boolean lbSpec = !empty(aSpec);
    if (lbSpec) {
      if (colEmpty("acno.spec_status") == false && commDate.sysComp(colStr("acno.spec_del_date")) > 0) {
        lbSpec = false;
      }
    }
    if (!lbBlock4 && !lbBlock5 && !lbSpec)
      return 0;

    rskAcnologInit();
    idsAclg.ppstr2("kind_flag", "A");
    idsAclg.ppstr2("card_no", "");
    idsAclg.ppstr2("acno_p_seqno", aPSeqno);
    idsAclg.ppstr2("acct_type", colStr("acno.acct_type"));
    idsAclg.ppstr2("id_p_seqno", colStr("acno.id_p_seqno"));
    idsAclg.ppstr2("corp_p_seqno", colStr("acno.corp_p_seqno"));
    idsAclg.ppstr2("block_reason", aaBlock[0]);
    idsAclg.ppstr2("block_reason2", aaBlock[1]);
    idsAclg.ppstr2("block_reason3", aaBlock[2]);
    idsAclg.ppstr2("log_reason", logReason);
    idsAclg.ppstr2("block_reason4", colStr("acno.block_reason4"));
    idsAclg.ppstr2("block_reason5", colStr("acno.block_reason5"));
    idsAclg.ppstr2("spec_status", colStr("acno.spec_status"));
    idsAclg
        .ppstr2("block_cdoe_old", aaBlock[0] + aaBlock[1] + aaBlock[2] + aaBlock[3] + aaBlock[4]);

    busi.SqlPrepare spAcno = new busi.SqlPrepare();
    spAcno.sql2Update("cca_card_acct");
    if (lbBlock4) {
      spAcno.ppstr2("block_reason4", aBlock4);
      idsAclg.ppstr2("block_reason4", aBlock4);
      aaBlock[3] = aBlock4;
    }
    if (lbBlock5) {
      spAcno.ppstr2("block_reason5", aBlock5);
      idsAclg.ppstr2("block_reason5", aBlock5);
      aaBlock[4] = aBlock5;
    }

    if (lbBlock4 || lbBlock5) {
      spAcno.ppymd("block_date");
      spAcno.ppstr2("block_status", "Y");
    }
    if (lbSpec) {
      spAcno.ppstr2("spec_status", aSpec);
      spAcno.ppstr2("spec_del_date", "");
      spAcno.ppymd("spec_date");
      spAcno.ppstr2("spec_user", modUser);
      idsAclg.ppstr2("spec_status", aSpec);
    }
    spAcno.modxxx(modUser, modPgm);
    spAcno.sql2Where(" where acno_p_seqno =? and debit_flag<>'Y'", aPSeqno);
    sqlExec(spAcno.sqlStmt(), spAcno.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("update act_acno error, kk[%s]", aPSeqno);
      return rc;
    }

    // String sql1 =ids_aclg.sql_stmt();
    // Object[] parms =ids_aclg.sql_parm();
    // wp.ddd_sql(sql1,parms);
    sqlExec(idsAclg.sqlStmt(), idsAclg.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_acnolog.Block error; kk[%s]", aPSeqno);
      return rc;
    }

    // -onbat_2ccas-
    onbatToCcasInit();
    idsOnbat.ppstr2("card_indr", colStr("acno.card_indr"));
    idsOnbat.ppstr2("acct_type", colStr("acno.acct_type"));
    // ids_onbat.ppp("card_hldr_id","");
    idsOnbat.ppstr2("acno_p_seqno", aPSeqno);
    idsOnbat.ppstr2("card_no", "");
    idsOnbat.ppstr2("block_code_1", aaBlock[0]);
    idsOnbat.ppstr2("block_code_2", aaBlock[1]);
    idsOnbat.ppstr2("block_code_3", aaBlock[2]);
    idsOnbat.ppstr2("block_code_4", aaBlock[3]);
    idsOnbat.ppstr2("block_code_5", aaBlock[4]);
    if (lbBlock4) {
      iiBlockChg = 4;
    } else
      iiBlockChg = 5;
    idsOnbat.ppint2("match_flag", iiBlockChg);
    sqlExec(idsOnbat.sqlStmt(), idsOnbat.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert onbat_2ccas.Block error, kk[%s]", aPSeqno);
      return rc;
    }

    if (lbBlock4 || lbBlock5) {
      updateCrdCard();
    }

    return rc;
  }

  void updateCrdCard() {
    strSql =
        "select card_no, block_code," + " acct_type, id_p_seqno, corp_p_seqno" + " from crd_card"
            + " where acno_p_seqno =? and current_code='0'";
    busi.DataSet dsCard = new busi.DataSet();
    setString2(1, acnoPseqno);
    dsCard.colList = sqlQuery(strSql);
    while (dsCard.listNext()) {
      strSql =
          "update crd_card set" + " block_code =?," + " block_date =" + commSqlStr.sysYYmd
              + " where card_no =?";
      setString2(1, aaBlock[0] + aaBlock[1] + aaBlock[2] + aaBlock[3] + aaBlock[4]);
      setString(dsCard.colStr("card_no"));
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg("update crd_card.Block error, kk[%s]", dsCard.colStr("card_no"));
        return;
      }
      // --
      rskAcnologInit();
      idsAclg.ppstr2("kind_flag", "C");
      idsAclg.ppstr2("card_no", dsCard.colStr("card_no"));
      idsAclg.ppstr2("acno_p_seqno", acnoPseqno);
      idsAclg.ppstr2("acct_type", dsCard.colStr("acct_type"));
      idsAclg.ppstr2("id_p_seqno", dsCard.colStr("id_p_seqno"));
      idsAclg.ppstr2("corp_p_seqno", dsCard.colStr("corp_p_seqno"));
      idsAclg.ppstr2("log_reason", logReason);
      idsAclg.ppstr2("block_reason", aaBlock[0]);
      idsAclg.ppstr2("block_reason2", aaBlock[1]);
      idsAclg.ppstr2("block_reason3", aaBlock[2]);
      idsAclg.ppstr2("block_reason4", aaBlock[3]);
      idsAclg.ppstr2("block_reason5", aaBlock[4]);
      idsAclg.ppstr2("spec_status", "");
      // ids_aclg.ppp("block_code_old",ds_card.col_ss("block_code"));
      sqlExec(idsAclg.sqlStmt(), idsAclg.sqlParm());
      if (sqlRowNum <= 0) {
        errmsg("insert rsk_acnolog.card.Block error, kk[%s]", dsCard.colStr("card_no"));
        return;
      }
      // --
      onbatToCcasInit();
      idsOnbat.ppstr2("card_indr", colStr("acno.card_indr"));
      idsOnbat.ppstr2("acct_type", colStr("acno.acct_type"));
      idsOnbat.ppstr2("acno_p_seqno", acnoPseqno);
      idsOnbat.ppstr2("card_no", dsCard.colStr("card_no"));
      idsOnbat.ppstr2("block_code_1", aaBlock[0]);
      idsOnbat.ppstr2("block_code_2", aaBlock[1]);
      idsOnbat.ppstr2("block_code_3", aaBlock[2]);
      idsOnbat.ppstr2("block_code_4", aaBlock[3]);
      idsOnbat.ppstr2("block_code_5", aaBlock[4]);
      idsOnbat.ppint2("match_flag", iiBlockChg);
      sqlExec(idsOnbat.sqlStmt(), idsOnbat.sqlParm());
      if (sqlRowNum <= 0) {
        errmsg("insert onbat_2ccas.card.Block error, kk[%s]", dsCard.colStr("card_no"));
        return;
      }
    }

  }

  void rskAcnologInit() {
    idsAclg = new busi.SqlPrepare();
    strSql =
        "insert into rsk_acnolog ("
            + " kind_flag, card_no,"
            + " acno_p_seqno, acct_type,"
            + " id_p_seqno, corp_p_seqno,"
            + " log_date, log_mode, log_type,"
            + " log_reason, log_not_reason,"
            + " block_reason, block_reason2, block_reason3, block_reason4, block_reason5,"
            + " spec_status,"
            + " fit_cond,"
            // +" block_code_old,"
            + " apr_flag, apr_user, apr_date," + " mod_user, mod_time, mod_pgm, mod_seqno"
            + " ) values (" + " :kind_flag, :card_no," + " :acno_p_seqno, :acct_type,"
            + " :id_p_seqno, :corp_p_seqno," + " " + commSqlStr.sysYYmd + ", '1', '3',"
            + " :log_reason, '',"
            + " :block_reason, :block_reason2, :block_reason3, :block_reason4, :block_reason5,"
            + " :spec_status," + " 'ECS',"
            // +" :block_code_old,"
            + " 'Y', :apr_user, " + commSqlStr.sysYYmd + "," + " :mod_user," + commSqlStr.sysdate
            + ", :mod_pgm, 1" + ")";
    idsAclg.sql2Stmt(strSql);
    idsAclg.ppstr2("kind_flag", "");
    idsAclg.ppstr2("card_no", "");
    idsAclg.ppstr2("acno_p_seqno", "");
    idsAclg.ppstr2("acct_type", "");
    idsAclg.ppstr2("id_p_seqno", "");
    idsAclg.ppstr2("corp_p_seqno", "");
    idsAclg.ppstr2("log_reason", "");
    idsAclg.ppstr2("block_reason", "");
    idsAclg.ppstr2("block_reason2", "");
    idsAclg.ppstr2("block_reason3", "");
    idsAclg.ppstr2("block_reason4", "");
    idsAclg.ppstr2("block_reason5", "");
    idsAclg.ppstr2("spec_status", "");
    // ids_aclg.ppp("block_code_old","");
    idsAclg.ppstr2("apr_user", modUser);
    idsAclg.ppstr2("mod_user", modUser);
    idsAclg.ppstr2("mod_pgm", modPgm);
  }

  void onbatToCcasInit() {
    String sql1 =
        "insert into onbat_2ccas (" + " trans_type," + " to_which," + " dog," + " proc_mode,"
            + " proc_status," + " card_catalog," + " acct_type," + " card_hldr_id,"
            + " acno_p_seqno," + " card_no," + " block_code_1," + " block_code_2,"
            + " block_code_3," + " block_code_4," + " block_code_5," + " match_flag,"
            + " match_date " + " ) values (" + " '2'," + " 2," + " " + commSqlStr.sysdate + ","
            + " 'B'," + " 0," + " :card_indr," + " :acct_type," + " :card_hldr_id,"
            + " :acno_p_seqno," + " :card_no," + " :block_code_1," + " :block_code_2,"
            + " :block_code_3," + " :block_code_4," + " :block_code_5," + " :match_flag," + " "
            + commSqlStr.sysYYmd // :match_date "
            + " )";
    idsOnbat.sql2Stmt(sql1);
    idsOnbat.ppstr2("card_indr", "");
    idsOnbat.ppstr2("acct_type", "");
    idsOnbat.ppstr2("card_hldr_id", "");
    idsOnbat.ppstr2("acno_p_seqno", "");
    idsOnbat.ppstr2("card_no", "");
    idsOnbat.ppstr2("block_code_1", "");
    idsOnbat.ppstr2("block_code_2", "");
    idsOnbat.ppstr2("block_code_3", "");
    idsOnbat.ppstr2("block_code_4", "");
    idsOnbat.ppstr2("block_code_5", "");
    idsOnbat.ppint2("match_flag", 0);
  }

}
