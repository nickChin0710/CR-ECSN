package cmsm03;
/**
 * 2019-1127:  Alex  sex->db_sex,insert add proc_flag
 * 2019-0614:  JH    p_xxx >>acno_pxxx
  *109-04-20   shiyuqi       updated for project coding standard     *
  * 109-01-04  V1.00.01   shiyuqi       修改无意义命名
  * 110-01-05  V1.00.02  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  
  * 112-04-23  V1.00.03  Wilson     insert crd_ppcard_apply add group_code、card_type*
  * 112-04-25  V1.00.04  Wilson     新增申請信用卡號欄位  *
  * 112-05-16  V1.00.05  Wilson     insert crd_ppcard_apply的bin_type改抓mkt_ppcard_issue  *
  * 112-06-14  V1.00.06  Wilson     apply_credit_card_no改成card_no  *
 */

import busi.FuncAction;

public class Cmsm3120Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();

  String isApplyNo = "", isCardType = "", isGroupCode = "", isBinType = "";
  String lsAddr = "", isIdPSeqno = "", lsPpCard = "";
  String ppGroupCode = "";

  @Override
  public void dataCheck() {
    if (eqIgno(wp.itemStr("proc_flag"), "Y")) {
      errmsg("已執行 [製卡處理] 不可異動");
      return;
    }

    if (this.ibAdd)
      selectApplyNo();

    if (this.ibDelete)
      return;

    if (empty(wp.itemStr("bin_type"))) {
      errmsg("未指定 貴賓卡別 !");
      return;
    }

    if (empty(wp.itemStr("eng_name"))) {
      errmsg("英文姓名 不可空白");
      return;
    }

    lsAddr = wp.itemStr("mail_addr1") + wp.itemStr("mail_addr2") + wp.itemStr("mail_addr3")
        + wp.itemStr("mail_addr4") + wp.itemStr("mail_addr5");

    if ((eqIgno(wp.itemStr("mail_type"), "1") || eqIgno(wp.itemStr("mail_type"), "2"))
        && (empty(wp.itemStr("zip_code")) || empty(lsAddr))) {
      errmsg("郵寄地址 不可空白");
      return;
    }

    if (pos("|1|2", wp.itemStr("db_sex")) == 0) {
      errmsg("請指定性別 !");
      return;
    }

    if (!eqIgno(wp.itemStr("mail_type"), "4") && !empty(wp.itemStr("mail_branch"))) {
      errmsg("寄件別不是分行, 分行別需為空白");
      return;
    }

    if (eqIgno(wp.itemStr("mail_type"), "4") && empty(wp.itemStr("mail_branch"))) {
      errmsg("寄件別為分行, 分行別不可為空白");
      return;
    }

    if (ibAdd) {
      if (wfInsertCheck() != 1) {
        rc = -1;
        return;
      }
    }

    lsPpCard = wp.itemStr("pp_card_no");
    if (!empty(lsPpCard)) {
      String sql1 = " select count(*) as ll_cnt " + " from crd_emboss_pp where pp_card_no = ? "
          + " and vip_kind = ? " + " and in_main_date = '' ";
      sqlSelect(sql1, new Object[] {lsPpCard, wp.itemStr("kk_vip_kind")});
    } else {
      String sql2 = " select count(*) as ll_cnt " + " from crd_emboss_pp where id_no = ? "
          + " and vip_kind = ? " + " and in_main_date ='' ";
      sqlSelect(sql2, new Object[] {wp.itemStr("id_no"), wp.itemStr("kk_vip_kind")});
    }
    if (colNum("ll_cnt") > 0) {
      errmsg("卡人已申請待處理中, 不可重複申請");
      return;
    }
  }

  int wfInsertCheck() {	  
    System.out.println("######" + wp.itemStr("id_p_seqno"));
    System.out.println("######" + wp.itemStr("kk_vip_kind"));
    String sql1 = " select id_p_seqno from crd_idno " + " where id_no = ? " + commSqlStr.rownum(1);
    sqlSelect(sql1, new Object[] {wp.itemStr("id_no")});
    if (sqlRowNum <= 0) {
      errmsg("身分證ID: 不存在");
      return -1;
    }
    isIdPSeqno = colStr("id_p_seqno");

    String sql2 = " select count(*) as ll_cnt1 "
        + " from crd_card_pp where id_p_seqno = ? and current_code='0' and vip_kind = ? ";
    sqlSelect(sql2, new Object[] {wp.itemStr("id_p_seqno"), wp.itemStr("kk_vip_kind")});
    if (colNum("ll_cnt1") > 0) {
      errmsg("持卡人已有 貴賓卡, 不可再申請");
      return -1;
    }

    String sql3 = " select count(*) as ll_cnt2 "
        + " from crd_ppcard_apply where id_p_seqno = ? and vip_kind = ? "
        + " and nvl(proc_flag,'x')<>'Y' ";
    sqlSelect(sql3, new Object[] {wp.itemStr("id_p_seqno"), wp.itemStr("kk_vip_kind")});
    if (colNum("ll_cnt2") > 0) {
      errmsg("卡人已申請待處理中, 不可重複申請");
      return -1;
    }

    String sql4 = " select count(*) as ll_cnt3 " + " from crd_ppcard_stop where id_p_seqno = ? "
        + " and vip_kind = ? " + " and nvl(proc_flag,'x')<>'Y' " + " and nvl(cancel_flag,'x')<>'Y' "
        + " and nvl(reissue_flag,'x')='Y' "
        + " and substrb(pp_card_no,1,6) in (select ppcard_bin_no from mkt_ppcard_issue) ";
    sqlSelect(sql4, new Object[] {wp.itemStr("id_p_seqno"), wp.itemStr("kk_vip_kind")});
    if (colNum("ll_cnt3") > 0) {
      errmsg("卡人已停掛補發中, 不可重複申請");
      return -1;
    }
    
    String sql5 = " select vip_group_code from mkt_ppcard_apply "
    + " where group_code = ? " + " and card_type = ? " + " and vip_kind = ? ";
    sqlSelect(sql5,new Object[] {wp.itemStr("reg_group_code"), wp.itemStr("reg_card_type"), wp.itemStr("kk_vip_kind") }); 
    if(sqlRowNum <= 0){
   	 errmsg("卡別 未指定可發 貴賓卡"); 
   	 return -1; 	 
    }
    
    ppGroupCode = colStr("vip_group_code");
        
     String sql6 = " select " + " card_type as ls_card_type , " + " group_code as ls_group_code , "
     + " bin_type as ls_bin_type " + " from mkt_ppcard_issue " + " where group_code = ? ";
     sqlSelect(sql6,new Object[] {ppGroupCode}); 
     if(sqlRowNum <= 0){
    	 errmsg("貴賓卡團代不存在"); 
    	 return -1; 	 
     }
     
     isCardType = colStr("ls_card_type");
     isGroupCode = colStr("ls_group_code");
     isBinType = colStr("ls_bin_type");
     
    return 1;
  }

  void selectApplyNo() {
    String sql1 = " select ecs_modseq.nextval as apply_no from dual ";
    sqlSelect(sql1);
    isApplyNo = commString.lpad(colStr("apply_no"), 10, "0");
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = "insert into crd_ppcard_apply (" + " apply_no ," + " apply_date ," + " id_p_seqno ,"
        + " eng_name ," + " bin_type ," + " card_type ," + " group_code ," + " from_type ,"
        + " user_remark ," + " zip_code ," + " mail_addr1 ," + " mail_addr2 ," + " mail_addr3 ,"
        + " mail_addr4 ," + " mail_addr5 ," + " reg_card_type ," + " reg_group_code ,"
        + " mail_type ," + " mail_branch ," + " proc_flag ," + " vip_kind," + " card_no," + " crt_user ,"
        + " crt_date ," + " crt_time ," + " apr_flag ," + " apr_date ," + " apr_user ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
        + " :apply_no ," + " to_char(sysdate,'yyyymmdd') ," + " :id_p_seqno ," + " :eng_name ,"
        + " :bin_type ," + " :card_type ," + " :group_code ," + " :from_type ," + " :user_remark ,"
        + " :zip_code ," + " :mail_addr1 ," + " :mail_addr2 ," + " :mail_addr3 ," + " :mail_addr4 ,"
        + " :mail_addr5 ," + " :reg_card_type ," + " :reg_group_code ," + " :mail_type ,"
        + " :mail_branch ," + " 'N' ," + " :vip_kind ," + " :card_no ," + " :crt_user ,"
        + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ," + " 'Y' ,"
        + " to_char(sysdate,'yyyymmdd') ," + " :apr_user ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " '1' " + " )";
    setString("apply_no", isApplyNo);
    setString("id_p_seqno", isIdPSeqno);
    item2ParmStr("eng_name");
    setString("bin_type", isBinType);
    setString("card_type", isCardType);
    setString("group_code", isGroupCode);
    item2ParmStr("from_type");
    item2ParmStr("user_remark");
    item2ParmStr("zip_code");
    item2ParmStr("mail_addr1");
    item2ParmStr("mail_addr2");
    item2ParmStr("mail_addr3");
    item2ParmStr("mail_addr4");
    item2ParmStr("mail_addr5");
    item2ParmStr("reg_card_type");
    item2ParmStr("reg_group_code");
    item2ParmStr("mail_type");
    item2ParmStr("mail_branch");
    item2ParmStr("card_no");
    setString("vip_kind", wp.itemStr("kk_vip_kind"));
    setString("crt_user", wp.loginUser);
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm3120");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_ppcard_apply error !");
      return rc;
    }

    updateSex();
    if (rc != 1)
      errmsg("update crd_idno sex error !");
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    strSql = " update crd_ppcard_apply set " + " user_remark =:user_remark ,"
        + " zip_code =:zip_code ," + " mail_addr1 =:mail_addr1 ," + " mail_addr2 =:mail_addr2 ,"
        + " mail_addr3 =:mail_addr3 ," + " mail_addr4 =:mail_addr4 ," + " mail_addr5 =:mail_addr5 ,"
        + " mail_type =:mail_type ," + " mail_branch =:mail_branch ," + " vip_kind =:vip_kind ,"
        + " apr_flag ='Y' ," + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm =:mod_pgm ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where apply_no =:apply_no ";
    item2ParmStr("user_remark");
    item2ParmStr("zip_code");
    item2ParmStr("mail_addr1");
    item2ParmStr("mail_addr2");
    item2ParmStr("mail_addr3");
    item2ParmStr("mail_addr4");
    item2ParmStr("mail_addr5");
    item2ParmStr("mail_type");
    item2ParmStr("mail_branch");
    setString("vip_kind", wp.itemStr("kk_vip_kind"));
    setString("apr_user", wp.itemStr("approval_user"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm3120");
    item2ParmStr("apply_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_ppcard_apply error !");
      return rc;
    }

    updateSex();
    if (rc != 1)
      errmsg("update crd_idno sex error !");
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    strSql = " delete crd_ppcard_apply where apply_no =:apply_no ";
    item2ParmStr("apply_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      errmsg("delete crd_ppcard_apply error !");
    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int updateSex() {
    if (checkSex()) {
      rc = 1;
      return rc;
    }

    msgOK();
    strSql = " update crd_idno set " + " sex =:sex , " + " mod_user =:mod_user , "
        + " mod_time =sysdate , " + " mod_pgm =:mod_pgm , " + " mod_seqno =nvl(mod_seqno,0)+1 "
        + " where id_p_seqno =:id_p_seqno ";
    item2ParmStr("sex", "db_sex");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    item2ParmStr("id_p_seqno");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update CRD_IDNO.SEX error");
    }
    return rc;
  }

  boolean checkSex() {
    String isSql = " select sex from crd_idno where id_p_seqno = ? ";
    sqlSelect(isSql, new Object[] {wp.itemStr("id_p_seqno")});
    if (sqlRowNum <= 0)
      return true;
    if (empty(colStr("sex")))
      return false;
    return true;
  }

  public int selectBilBill(String asCardPurchFlag, String asCardType, String asGroupCode) {
    // --pp-card 申請條件
    // --card_purch_flag: 1.正附卡合併, 2.正附卡分開
    // --Parm-IN: is_idno, is_date1, is_date2, is_it_type, im_low_amt,
    // bl,it,id,ca,ao,ot
    // --Parm-out: ii_cnt, im_amt=0;
    // =====================================================================
    String lsIdPseqno = varsStr("id_p_seqno");
    String lsDate1 = varsStr("is_date1");
    String lsDate2 = varsStr("is_date2");

    if (empty(asCardPurchFlag))
      asCardPurchFlag = "1";

    if (empty(lsIdPseqno)) {
      errmsg("身分證ID 不可空白");
      return rc;
    }
    // get_idno_pseqno(ls_idno);
    // if (rc!=1) {
    // return rc;
    // }

    if (empty(asCardType) || empty(asGroupCode)) {
      errmsg("卡種, 團體代號 不可空白");
      return rc;
    }
    if (empty(lsDate1) || empty(lsDate2)) {
      errmsg("讀取起迄期間 不可空白");
      return rc;
    }
    strSql = "SELECT  count(*) as db_cnt," + " sum(" + "  decode(a.acct_code,'IT',"
        + "    decode(cast(:is_it_type as varchar(1)),'2'," // :is_it_type,'2',"
        + "    decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
        + "    decode(b.refund_apr_flag,'Y',0,a.dest_amt)),"
        + "    decode(A.sign_flag,'-',-1,1) * A.dest_amt) ) as db_amt"
        + " from bil_bill A left join bil_contract B "
        + "		on A.contract_no =B.contract_no and A.contract_seq_no =B.contract_seq_no"
        + " where 1=1" + " and A.card_no in ("
        + " select card_no from crd_card where major_id_p_seqno =:ls_id_pseqno"
        + " and decode(cast(:ls_card_flag as varchar(1)),'1',major_id_p_seqno,id_p_seqno) =:ls_id_pseqno"
        + " and card_type =:ls_card_type and group_code =:ls_group_code" + " )"
        // -- 消費資料 六大本金類 --
        + " and A.acct_code in " + condAcctCode()
        // -- 消費資料 最低單筆金額 --
        + " and ( A.sign_flag ='-' or (A.sign_flag='+' and A.dest_amt>=:im_low_amt) )"
        // --消費資料 消費期間 --
        + " and A.acct_month between :is_beg_date and :is_end_date";
    this.setString2("is_it_type", varsStr("is_it_type"));
    setString2("ls_id_pseqno", lsIdPseqno);
    setString2("ls_card_flag", asCardPurchFlag);
    setString2("ls_card_type", asCardType);
    setString2("ls_group_code", asGroupCode);
    setDouble2("im_low_amt", varsNum("low_amt"));
    setString2("is_beg_date", lsDate1);
    setString2("is_end_date", lsDate2);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      varsSet("im_cnt", "0");
      varsSet("im_amt", "0");
      return sqlRowNum;
    }
    varsSet("ii_cnt", "" + colInt("db_cnt"));
    varsSet("im_amt", "" + colNum("db_amt"));
    return 1;
  }

  public int selectMktCardConsume(String asCardFlag, String asCardType,
      String asGroupCode) {
    // --card_purch_flag: 1.正附卡合併, 2.正附卡分開
    // ================================================================
    String lsIdPseqno = varsStr("id_p_seqno");
    if (empty(lsIdPseqno)) {
      errmsg("身分證ID 不可同空白");
      return rc;
    }
    if (empty(asCardType) || empty("as_group_code")) {
      errmsg("卡種, 團體代號 不可空白");
      return rc;
    }
    if (empty(asCardFlag))
      asCardFlag = "N";

    // get_idno_pseqno(ls_idno);
    // if (rc!=1) {
    // return rc;
    // }

    if (empty(varsStr("beg_ym")) || empty(varsStr("end_ym"))) {
      errmsg("帳務年月起迄期間 不可空白");
      return rc;
    }
    if (commString.strComp(varsStr("beg_ym"), varsStr("end_ym")) > 0) {
      errmsg("帳務年月:  起迄錯誤");
      return rc;
    }
    strSql = "select sum(consume_bl_amt) as bl_amt," + " sum(consume_it_amt) as it_amt,"
        + " sum(consume_id_amt) as id_amt," + " sum(consume_ca_amt) as ca_amt,"
        + " sum(consume_ao_amt) as ao_amt," + " sum(consume_ot_amt) as ot_amt,"
        + " sum(consume_bl_cnt) as bl_cnt," + " sum(consume_it_cnt) as it_cnt,"
        + " sum(consume_id_cnt) as id_cnt," + " sum(consume_ca_cnt) as ca_cnt,"
        + " sum(consume_ao_cnt) as ao_cnt," + " sum(consume_ot_cnt) as ot_cnt "
        + " from mkt_card_consume" + " where 1=1" + " and card_no in ( "
        + " select card_no from crd_card" + " where major_id_p_seqno =:ls_id_pseqno";
    if (eqAny(asCardFlag, "1") == false) {
      strSql += " and id_p_seqno =:ls_id_pseqno";
    }
    strSql += " and card_type =:ls_card_type" + " and group_code =:ls_group_code"
        + ") and acct_month between :ls_beg_ym and :ls_end_ym";
    setString2("ls_id_pseqno", lsIdPseqno);
    setString2("ls_card_type", asCardType);
    setString2("ls_group_code", nvl(asGroupCode, "0000"));
    setString2("ls_beg_ym", varsStr("beg_ym"));
    setString2("ls_end_ym", varsStr("end_ym"));
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      varsSet("ii_cnt", "0");
      varsSet("im_amt", "0");
      return rc;
    }

    int liCnt = 0;
    double lmAmt = 0;
    if (eqAny(varsStr("bl"), "Y")) {
      liCnt += colInt("bl_ant");
      lmAmt += colNum("bl_amt");
    }
    if (eqAny(varsStr("it"), "Y")) {
      liCnt += colInt("it_ant");
      lmAmt += colNum("it_amt");
    }
    if (eqAny(varsStr("id"), "Y")) {
      liCnt += colInt("id_ant");
      lmAmt += colNum("id_amt");
    }
    if (eqAny(varsStr("ca"), "Y")) {
      liCnt += colInt("ca_ant");
      lmAmt += colNum("ca_amt");
    }
    if (eqAny(varsStr("ao"), "Y")) {
      liCnt += colInt("ao_ant");
      lmAmt += colNum("ao_amt");
    }
    if (eqAny(varsStr("ot"), "Y")) {
      liCnt += colInt("ot_ant");
      lmAmt += colNum("ot_amt");
    }
    varsSet("ii_cnt", "" + liCnt);
    varsSet("im_amt", "" + lmAmt);
    return rc;
  }

  private String condAcctCode() {
    String code = "(''";
    if (eqIgno(varsStr("bl"), "Y")) {
      code += ",'BL'";
    }
    if (eqIgno(varsStr("it"), "Y"))
      code += ",'IT'";
    if (eqIgno(varsStr("id"), "Y"))
      code += ",'ID'";
    if (eqIgno(varsStr("ca"), "Y"))
      code += ",'CA'";
    if (eqIgno(varsStr("ao"), "Y"))
      code += ",'AO'";
    if (eqIgno(varsStr("ot"), "Y"))
      code += ",'OT'";
    return code + ")";
  }

}
