package cmsm01;
/** cmsm2210;法人基本資料維護
 * 21-0106     JustinWu updated for XSS
 * 20-0217     JustinWu add 9 new columns
 * 19-1202:    Alex  add column 
 * 19-0613:    JH    p_xxx >>acno_p_xxx
 * 109-04-19    shiyuqi       updated for project coding standard  
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名 
 * 112-03-24  V1.00.04   Zuwei Su      增加檢核邏輯，覆核主管不可同為維護經辦
 * 112-07-15   V1.00.05  Zuwei Su      修正method cmsp2010SetModifyData是否異動的判斷條件。
 * */

import busi.FuncAction;

public class Cmsm2020Corp extends FuncAction {

  String corpPSeqno = "";

  String[] aprCols = new String[] {"chi_name", "abbr_name", "force_flag", "eng_name",
      "emboss_data", "business_code", "setup_date", "capital", "charge_id", "charge_name",
      "charge_tel_zone", "charge_tel_no", "charge_tel_ext", "reg_zip", "reg_addr1", "reg_addr2",
      "reg_addr3", "reg_addr4", "reg_addr5", "corp_tel_zone1", "corp_tel_no1", "corp_tel_ext1",
      "corp_tel_zone2", "corp_tel_no2", "corp_tel_ext2", "chi_name", "organ_id"
      // ,"send_visa_flag"
      , "e_mail_addr", "e_mail_addr2", "e_mail_addr3", "spec_busi_code", "charge_cntry_code",
      "charge_eng_name", "charge_birthday", "reg_country_code", "incorporated_type",
      "issues_bearer_shares_ind", "complex_structure", "shareholder_trust", "ubo_flag",
      "non_ubo_type", "risk_level", "risk_level_date", "asig_rel_flag", "empoly_type", "obu_id",
      "corp_act_type", "comm_zip", "comm_addr1", "comm_addr2", "comm_addr3", "comm_addr4",
      "comm_addr5",
      "reg_zip2", "comm_zip2"};
  String[] chgCols = new String[] {"asig_inq1_cname", "asig_inq1_idno", "asig_inq1_telno",
      "asig_inq1_acct", "asig_inq1_card", "asig_inq1_limit", "asig_inq1_chk_data",
      "asig_inq2_cname", "asig_inq2_idno", "asig_inq2_telno", "asig_inq2_acct", "asig_inq2_card",
      "asig_inq2_limit", "asig_inq2_chk_data", "asig_inq3_cname", "asig_inq3_idno",
      "asig_inq3_telno", "asig_inq3_acct", "asig_inq3_card", "asig_inq3_limit",
      "asig_inq3_chk_data"};

  void setKkValue() {
    corpPSeqno = wp.colStr("corp_p_seqno");
  }

  public int selectModdataTmp() {
    setKkValue();

    if (empty(corpPSeqno)) {
      errmsg("統編流水號: 不可空白");
      return -1;
    }
    strSql = "select tmp_moddata,mod_user" + " from ecs_moddata_tmp" + " where tmp_pgm='cmsm2020'"
        + " and tmp_table ='CRD_CORP'" + " and tmp_key =?";
    sqlSelect(strSql, new Object[] {corpPSeqno});

    if (sqlRowNum <= 0)
      return 0;

    // -set-mod-data-
    int liMod = 0;
    String[] ss = new String[2];
    ss[0] = colStr("tmp_moddata");

    for (int ii = 0; ii < aprCols.length; ii++) {
      ss = commString.token(ss, "\t");
      String col = aprCols[ii];
      if (eqIgno(ss[1], wp.colStr(col))) {
        continue;
      }
      log("原:" + wp.colStr(col) + "  修改後:" + ss[1]);
      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, ss[1]);

      liMod = 1;
    }
    return liMod;
  }

  void selectCrdCorp() {
    strSql =
        "select * from crd_corp A " + " left join crd_corp_ext B on A.corp_p_seqno = B.corp_p_seqno"
            + " where A.corp_p_seqno =?";
    sqlSelect(strSql, new Object[] {corpPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("公司統編: 不存在");
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] commZipArr = commString.splitZipCode(colStr("comm_zip"));
    colSet("comm_zip", commZipArr[0]);
    colSet("comm_zip2", commZipArr[1]);
    String[] regZipArr = commString.splitZipCode(colStr("reg_zip"));
    colSet("reg_zip", regZipArr[0]);
    colSet("reg_zip2", regZipArr[1]);
    String[] contactZipArr = commString.splitZipCode(colStr("contact_zip"));
    colSet("contact_zip", contactZipArr[0]);
    colSet("contact_zip2", contactZipArr[1]);
    
  }

  boolean checkModifyApr() {

    for (int ii = 0; ii < aprCols.length; ii++) {
      String col = aprCols[ii];
      if (colEq(col, wp.itemStr(col)) == false) {
        return true;
      }
    }

    return false;
  }

  boolean checkModifyChg() {
    for (int ii = 0; ii < chgCols.length; ii++) {
      String col = chgCols[ii];
      if (colEq(col, wp.itemStr(col)) == false) {
        return true;
      }
    }

    return false;
  }

  void deleteEcsModdata() {
    // delete ecs_moddata_tmp
    strSql = "delete ecs_moddata_tmp" + " where tmp_pgm ='cmsm2020'" + " and tmp_table ='CRD_CORP'"
        + " and tmp_key =?";
    this.sqlExec(strSql, new Object[] {corpPSeqno});
  }

  void insertEcsModdata() {
    String lsData = "";
    for (int ii = 0; ii < aprCols.length; ii++) {
      lsData += wp.itemStr(aprCols[ii]) + "\t";
    }

    // insert ecs_moddata_tmp
    strSql = "insert into ecs_moddata_tmp (" + " tmp_pgm, tmp_table, tmp_key "
        + ", tmp_dspdata, tmp_moddata, tmp_audcode" + ", mod_user, mod_date, mod_time2, mod_seqno"
        + " ) values ( " + "'cmsm2020', 'CRD_CORP', :kk1 " + ", :dsp_data, :mod_data, 'U' "
        + ", :mod_user, to_char(sysdate,'yyyymmdd'), to_char(sysdate,'hh24miss') , 1" + " )";
    setString("kk1", corpPSeqno);
    setString("dsp_data", "");
    setString("mod_data", lsData);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ecs_moddata error; kk=" + corpPSeqno);
    }
  }

  void updateNoApproveCrdCorpExt() {
    // update crd_corp_ext
    strSql = "update crd_corp_ext set " + "  asig_inq1_cname    =:asig_inq1_cname   "
        + ", asig_inq1_idno     =:asig_inq1_idno    " + ", asig_inq1_telno    =:asig_inq1_telno   "
        + ", asig_inq1_acct     =:asig_inq1_acct    " + ", asig_inq1_card     =:asig_inq1_card    "
        + ", asig_inq1_limit    =:asig_inq1_limit   " + ", asig_inq1_chk_data =:asig_inq1_chk_data"
        + ", asig_inq2_cname    =:asig_inq2_cname   " + ", asig_inq2_idno     =:asig_inq2_idno    "
        + ", asig_inq2_telno    =:asig_inq2_telno   " + ", asig_inq2_acct     =:asig_inq2_acct    "
        + ", asig_inq2_card     =:asig_inq2_card    " + ", asig_inq2_limit    =:asig_inq2_limit   "
        + ", asig_inq2_chk_data =:asig_inq2_chk_data" + ", asig_inq3_cname    =:asig_inq3_cname   "
        + ", asig_inq3_idno     =:asig_inq3_idno    " + ", asig_inq3_telno    =:asig_inq3_telno   "
        + ", asig_inq3_acct     =:asig_inq3_acct    " + ", asig_inq3_card     =:asig_inq3_card    "
        + ", asig_inq3_limit    =:asig_inq3_limit   " + ", asig_inq3_chk_data =:asig_inq3_chk_data"
        + " where corp_p_seqno =:kk1";
    for (int ii = 0; ii < chgCols.length; ii++) {
      String col = chgCols[ii];
      item2ParmStr(col);
    }
    setString("kk1", corpPSeqno);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update CRD_CORP error; kk=" + corpPSeqno);
    }
  }

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    setKkValue();

    deleteEcsModdata();

    selectCrdCorp();
    if (checkModifyApr()) {
      insertEcsModdata();
    }

    if (rc == 1 && checkModifyChg()) {
      if (checkCorpExt() == false) {
        insertNoApproveCrdCorpExt();
      } else {
        updateNoApproveCrdCorpExt();
      }
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    setKkValue();

    deleteEcsModdata();
    return rc;
  }

  public void cmsp2020SetModifyData() {
    if (!empty(wp.colStr("mod_e_mail_addr")) || !empty(wp.colStr("mod_e_mail_addr2"))
        || !empty(wp.colStr("mod_e_mail_addr3"))) {
      wp.colSet("e_mail_from_mark", "M");
      wp.colSet("e_mail_chg_date", getSysDate());
    }
  }

  @Override
  public int dataProc() {
    msgOK();

    strSql = "select count(*) as db_cnt" + " from ecs_moddata_tmp" + " where tmp_pgm='cmsm2020'"
        + " and tmp_table ='CRD_CORP'" + " and tmp_key =?";
    sqlSelect(strSql, new Object[] {wp.itemStr("corp_p_seqno")});

    if (colNum("db_cnt") <= 0) {
      errmsg("此筆資料已被修改,請重新讀取");
      rc = -1;
      return rc;
    }

    strSql = " update crd_corp set " + " chi_name =:chi_name , " + " abbr_name =:abbr_name , "
        + " eng_name =:eng_name , " + " reg_zip =:reg_zip , " + " reg_addr1 =:reg_addr1 , "
        + " reg_addr2 =:reg_addr2 , " + " reg_addr3 =:reg_addr3 , " + " reg_addr4 =:reg_addr4 , "
        + " reg_addr5 =:reg_addr5 , " + " corp_tel_zone1 =:corp_tel_zone1 , "
        + " corp_tel_no1 =:corp_tel_no1 , " + " corp_tel_ext1 =:corp_tel_ext1 , "
        + " corp_tel_zone2 =:corp_tel_zone2 , " + " corp_tel_no2 =:corp_tel_no2 , "
        + " corp_tel_ext2 =:corp_tel_ext2 , " + " charge_id =:charge_id , "
        + " charge_name =:charge_name , " + " charge_tel_zone =:charge_tel_zone , "
        + " charge_tel_no =:charge_tel_no , " + " charge_tel_ext =:charge_tel_ext , "
        + " capital =:capital , " + " business_code =:business_code , "
        + " setup_date =:setup_date , " + " force_flag =:force_flag , " + " organ_id =:organ_id , "
        + " send_visa_flag =:send_visa_flag , " + " e_mail_addr =:e_mail_addr , "
        + " e_mail_addr2 =:e_mail_addr2 , " + " e_mail_addr3 =:e_mail_addr3 , "
        + " e_mail_chg_date =:e_mail_chg_date , " + " e_mail_from_mark =:e_mail_from_mark , "
        + " charge_cntry_code =:charge_cntry_code , " + " charge_eng_name =:charge_eng_name , "
        + " charge_birthday =:charge_birthday , " + " reg_country_code =:reg_country_code ,"
        + " issues_bearer_shares_ind =:issues_bearer_shares_ind , "
        + " apr_date = to_char(sysdate,'yyyymmdd') , " + " apr_user =:apr_user , "
        + " mod_user =:mod_user , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1, "
        // 2020-02-14 JustinWu begin
        + " empoly_type =:empoly_type, " + " obu_id =:obu_id, " + " corp_act_type =:corp_act_type, "
        + " comm_zip =:comm_zip, " + " comm_addr1 =:comm_addr1, " + " comm_addr2 =:comm_addr2, "
        + " comm_addr3 =:comm_addr3, " + " comm_addr4 =:comm_addr4, " + " comm_addr5 =:comm_addr5 "
        // 2020-02-14 JustinWu end
        + " where corp_p_seqno =:corp_p_seqno";

    item2ParmStr("chi_name");
    item2ParmStr("abbr_name");
    item2ParmStr("eng_name");
//    item2ParmStr("reg_zip");
    setString("reg_zip", wp.itemStr("reg_zip") + wp.itemStr("reg_zip2"));
    item2ParmStr("reg_addr1");
    item2ParmStr("reg_addr2");
    item2ParmStr("reg_addr3");
    item2ParmStr("reg_addr4");
    item2ParmStr("reg_addr5");
    item2ParmStr("corp_tel_zone1");
    item2ParmStr("corp_tel_no1");
    item2ParmStr("corp_tel_ext1");
    item2ParmStr("corp_tel_zone2");
    item2ParmStr("corp_tel_no2");
    item2ParmStr("corp_tel_ext2");
    item2ParmStr("charge_id");
    item2ParmStr("charge_name");
    item2ParmStr("charge_tel_zone");
    item2ParmStr("charge_tel_no");
    item2ParmStr("charge_tel_ext");
    item2ParmNum("capital");
    item2ParmStr("business_code");
    item2ParmStr("setup_date");
    item2ParmStr("force_flag");
    item2ParmStr("organ_id");
    item2ParmNvl("send_visa_flag", "N");
    item2ParmStr("e_mail_addr");
    item2ParmStr("e_mail_addr2");
    item2ParmStr("e_mail_addr3");
    item2ParmStr("e_mail_from_mark");
    item2ParmStr("e_mail_chg_date");
    item2ParmStr("charge_cntry_code");
    item2ParmStr("charge_eng_name");
    item2ParmStr("charge_birthday");
    item2ParmStr("reg_country_code");
    item2ParmNvl("issues_bearer_shares_ind", "N");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    // 2020-02-14 JustinWu begin
    item2ParmStr("empoly_type");
    item2ParmStr("obu_id");
    item2ParmStr("corp_act_type");
//    item2ParmStr("comm_zip");
    setString("comm_zip", wp.itemStr("comm_zip") + wp.itemStr("comm_zip2"));
    item2ParmStr("comm_addr1");
    item2ParmStr("comm_addr2");
    item2ParmStr("comm_addr3");
    item2ParmStr("comm_addr4");
    item2ParmStr("comm_addr5");
    item2ParmStr("corp_p_seqno");
    // 2020-02-14 JustinWu end
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_corp error !");
      rc = -1;
      return rc;
    }

    if (checkCorpExt() == false) {
      insertCrdCorpExt();
    } else {
      updateCrdCorpExt();
    }

    if (rc != 1)
      return rc;

    strSql = " delete ecs_moddata_tmp where tmp_table = 'CRD_CORP' "
        + " and tmp_key =:corp_p_seqno " + " and tmp_pgm ='cmsm2020' ";
    item2ParmStr("corp_p_seqno");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete tmp error !");
      rc = -1;
      return rc;
    }
    return rc;
  }

  boolean checkCorpExt() {

    String sql1 = " select count(*) as db_cnt from crd_corp_ext where corp_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("corp_p_seqno")});
    if (colNum("db_cnt") <= 0) {
      return false;
    }
    return true;
  }

  void insertNoApproveCrdCorpExt() {
    msgOK();

    strSql = " insert into crd_corp_ext ( " + " corp_p_seqno ," + " asig_inq1_cname ,"
        + " asig_inq1_idno ," + " asig_inq1_telno ," + " asig_inq1_acct ," + " asig_inq1_card ,"
        + " asig_inq1_limit ," + " asig_inq1_chk_data ," + " asig_inq2_cname ,"
        + " asig_inq2_idno ," + " asig_inq2_telno ," + " asig_inq2_acct ," + " asig_inq2_card ,"
        + " asig_inq2_limit ," + " asig_inq2_chk_data ," + " asig_inq3_cname ,"
        + " asig_inq3_idno ," + " asig_inq3_telno ," + " asig_inq3_acct ," + " asig_inq3_card ,"
        + " asig_inq3_limit ," + " asig_inq3_chk_data ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values ( " + " :corp_p_seqno ," + " :asig_inq1_cname ,"
        + " :asig_inq1_idno ," + " :asig_inq1_telno ," + " :asig_inq1_acct ," + " :asig_inq1_card ,"
        + " :asig_inq1_limit ," + " :asig_inq1_chk_data ," + " :asig_inq2_cname ,"
        + " :asig_inq2_idno ," + " :asig_inq2_telno ," + " :asig_inq2_acct ," + " :asig_inq2_card ,"
        + " :asig_inq2_limit ," + " :asig_inq2_chk_data ," + " :asig_inq3_cname ,"
        + " :asig_inq3_idno ," + " :asig_inq3_telno ," + " :asig_inq3_acct ," + " :asig_inq3_card ,"
        + " :asig_inq3_limit ," + " :asig_inq3_chk_data ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";
    item2ParmStr("corp_p_seqno");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmStr("asig_inq1_acct");
    item2ParmStr("asig_inq1_card");
    item2ParmStr("asig_inq1_limit");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmStr("asig_inq2_acct");
    item2ParmStr("asig_inq2_card");
    item2ParmStr("asig_inq2_limit");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmStr("asig_inq3_acct");
    item2ParmStr("asig_inq3_card");
    item2ParmStr("asig_inq3_limit");
    item2ParmStr("asig_inq3_chk_data");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_corp_ext error !");
    }
  }

  void insertCrdCorpExt() {
    msgOK();

    strSql = " insert into crd_corp_ext ( " + " corp_p_seqno ," + " pure_card_member ,"
        + " pure_card_member_date ," + " pure_card_member_time ," + " complex_structure ,"
        + " ubo_flag ," + " non_ubo_type ," + " shareholder_trust ," + " incorporated_type ,"
        + " aml_cft_concern_ind ," + " charge_pep_flag ," + " charge_hit_lists ,"
        + " charge_hit_reason ," + " charge_hit_date ," + " charge_hit_time ," + " negti_news_ind ,"
        + " negti_news_memo ," + " rela_negti_news_ind ," + " rela_negti_news_memo ,"
        + " risk_level ," + " risk_level_date ," + " risky_tx_date ," + " hit_lists ,"
        + " hit_reason ," + " hit_date ," + " hit_time ," + " charge_pure_card ,"
        + " charge_pure_card_date ," + " charge_pure_card_time ," + " panama_flag ,"
        + " spec_busi_code ," + " hi_spec_status ," + " hi_block_reason5 ,"
        + " charge_negti_news_ind ," + " charge_negti_news_memo ," + " bahamas_flag ,"
        + " paradise_papers ," + " asig_inq1_cname ," + " asig_inq1_idno ," + " asig_inq1_telno ,"
        + " asig_inq1_acct ," + " asig_inq1_card ," + " asig_inq1_limit ," + " asig_inq1_chk_data ,"
        + " asig_inq2_cname ," + " asig_inq2_idno ," + " asig_inq2_telno ," + " asig_inq2_acct ,"
        + " asig_inq2_card ," + " asig_inq2_limit ," + " asig_inq2_chk_data ,"
        + " asig_inq3_cname ," + " asig_inq3_idno ," + " asig_inq3_telno ," + " asig_inq3_acct ,"
        + " asig_inq3_card ," + " asig_inq3_limit ," + " asig_inq3_chk_data ," + " apr_date ,"
        + " apr_user ," + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno ,"
        + " asig_rel_flag " + " ) values ( " + " :corp_p_seqno ," + " :pure_card_member ,"
        + " :pure_card_member_date ," + " :pure_card_member_time ," + " :complex_structure ,"
        + " :ubo_flag ," + " :non_ubo_type ," + " :shareholder_trust ," + " :incorporated_type ,"
        + " :aml_cft_concern_ind ," + " :charge_pep_flag ," + " :charge_hit_lists ,"
        + " :charge_hit_reason ," + " :charge_hit_date ," + " :charge_hit_time ,"
        + " :negti_news_ind ," + " :negti_news_memo ," + " :rela_negti_news_ind ,"
        + " :rela_negti_news_memo ," + " :risk_level ," + " :risk_level_date ,"
        + " :risky_tx_date ," + " :hit_lists ," + " :hit_reason ," + " :hit_date ," + " :hit_time ,"
        + " :charge_pure_card ," + " :charge_pure_card_date ," + " :charge_pure_card_time ,"
        + " :panama_flag ," + " :spec_busi_code ," + " :hi_spec_status ," + " :hi_block_reason5 ,"
        + " :charge_negti_news_ind ," + " :charge_negti_news_memo ," + " :bahamas_flag ,"
        + " :paradise_papers ," + " :asig_inq1_cname ," + " :asig_inq1_idno ,"
        + " :asig_inq1_telno ," + " :asig_inq1_acct ," + " :asig_inq1_card ,"
        + " :asig_inq1_limit ," + " :asig_inq1_chk_data ," + " :asig_inq2_cname ,"
        + " :asig_inq2_idno ," + " :asig_inq2_telno ," + " :asig_inq2_acct ," + " :asig_inq2_card ,"
        + " :asig_inq2_limit ," + " :asig_inq2_chk_data ," + " :asig_inq3_cname ,"
        + " :asig_inq3_idno ," + " :asig_inq3_telno ," + " :asig_inq3_acct ," + " :asig_inq3_card ,"
        + " :asig_inq3_limit ," + " :asig_inq3_chk_data ," + " to_char(sysdate,'yyyymmdd') ,"
        + " :apr_user ," + " :mod_user ," + " sysdate ," + " :mod_pgm ," + " 1 ,"
        + " :asig_rel_flag " + " ) ";

    item2ParmStr("corp_p_seqno");
    item2ParmStr("pure_card_member");
    item2ParmStr("pure_card_member_date");
    item2ParmStr("pure_card_member_time");
    item2ParmStr("complex_structure");
    item2ParmStr("ubo_flag");
    item2ParmStr("non_ubo_type");
    item2ParmStr("shareholder_trust");
    item2ParmStr("incorporated_type");
    item2ParmStr("aml_cft_concern_ind");
    item2ParmStr("charge_pep_flag");
    item2ParmStr("charge_hit_lists");
    item2ParmStr("charge_hit_reason");
    item2ParmStr("charge_hit_date");
    item2ParmStr("charge_hit_time");
    item2ParmStr("negti_news_ind");
    item2ParmStr("negti_news_memo");
    item2ParmStr("rela_negti_news_ind");
    item2ParmStr("rela_negti_news_memo");
    item2ParmStr("risk_level");
    item2ParmStr("risk_level_date");
    item2ParmStr("risky_tx_date");
    item2ParmStr("hit_lists");
    item2ParmStr("hit_reason");
    item2ParmStr("hit_date");
    item2ParmStr("hit_time");
    item2ParmStr("charge_pure_card");
    item2ParmStr("charge_pure_card_date");
    item2ParmStr("charge_pure_card_time");
    item2ParmStr("panama_flag");
    item2ParmStr("spec_busi_code");
    item2ParmStr("hi_spec_status");
    item2ParmStr("hi_block_reason5");
    item2ParmStr("charge_negti_news_ind");
    item2ParmStr("charge_negti_news_memo");
    item2ParmStr("bahamas_flag");
    item2ParmStr("paradise_papers");
    item2ParmStr("asig_inq1_cname");
    item2ParmStr("asig_inq1_idno");
    item2ParmStr("asig_inq1_telno");
    item2ParmStr("asig_inq1_acct");
    item2ParmStr("asig_inq1_card");
    item2ParmStr("asig_inq1_limit");
    item2ParmStr("asig_inq1_chk_data");
    item2ParmStr("asig_inq2_cname");
    item2ParmStr("asig_inq2_idno");
    item2ParmStr("asig_inq2_telno");
    item2ParmStr("asig_inq2_acct");
    item2ParmStr("asig_inq2_card");
    item2ParmStr("asig_inq2_limit");
    item2ParmStr("asig_inq2_chk_data");
    item2ParmStr("asig_inq3_cname");
    item2ParmStr("asig_inq3_idno");
    item2ParmStr("asig_inq3_telno");
    item2ParmStr("asig_inq3_acct");
    item2ParmStr("asig_inq3_card");
    item2ParmStr("asig_inq3_limit");
    item2ParmStr("asig_inq3_chk_data");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmStr("asig_rel_flag ");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_corp_ext error !");
    }
  }



  void updateCrdCorpExt() {
    msgOK();
    // --update crd_corp_ext
    strSql = " update crd_corp_ext set " + " spec_busi_code =:spec_busi_code , "
        + " incorporated_type =:incorporated_type , " + " complex_structure =:complex_structure , "
        + " shareholder_trust =:shareholder_trust , " + " ubo_flag =:ubo_flag , "
        + " non_ubo_type =:non_ubo_type ," + " risk_level =:risk_level , "
        + " risk_level_date =:risk_level_date , " + " asig_rel_flag =:asig_rel_flag , "
        + " mod_user =:mod_user , " + " mod_time = sysdate , " + " mod_pgm =:mod_pgm , "
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where corp_p_seqno =:corp_p_seqno ";

    item2ParmStr("spec_busi_code");
    item2ParmStr("incorporated_type");
    item2ParmNvl("complex_structure", "N");
    item2ParmNvl("shareholder_trust", "N");
    item2ParmStr("corp_p_seqno");
    item2ParmStr("ubo_flag");
    item2ParmStr("non_ubo_type");
    item2ParmStr("risk_level");
    item2ParmStr("risk_level_date");
    item2ParmNvl("asig_rel_flag", "N");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_corp_ext error !");
      return;
    }
  }

}
