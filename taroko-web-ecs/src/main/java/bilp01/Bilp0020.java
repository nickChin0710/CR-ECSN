/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-01  V1.00.00  Andy       program initial                            *
* 106-12-14  V1.00.01  Andy		  update : ucStr==>zzStr                     *
* 107-03-30  V1.00.02  Andy		  update : UI POPUP ,bug process             *
* 107-05-16  V1.00.03  Andy		  update : UI                                * 
* 107-08-06  V1.00.04  Andy		  update : Debug                             * 
*109-04-23   V1.00.05  shiyuqi       updated for project coding standard     * 
*111-02-21   V1.00.06  Ryan       修正 dbDelete                                * 
* 111-05-31  V1.00.03  Ryan       增加異動人員與登入人員相同時不能覆核                                                    * 
******************************************************************************/

package bilp01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Bilp0020 extends BaseEdit {
  String mExMchtNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      /* TEST */
      strAction = "AJAX";
      processAjaxOption();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    String exMchtNo = wp.itemStr("ex_merchant");
    String exUniformNo = wp.itemStr("ex_uniform_no");
    String exCrtUser = wp.itemStr("ex_crt_user");

    wp.whereStr = " where 1=1  ";

    if (empty(exMchtNo) == false) {
//      wp.whereStr += " and  mcht_no like '" + exMchtNo + "%' ";
    	wp.whereStr += sqlCol(exMchtNo,"mcht_no","like%");
    	
    }

    if (empty(exUniformNo) == false) {
//      wp.whereStr += " and  uniform_no like '" + exUniformNo + "%' ";
    	wp.whereStr += sqlCol(exUniformNo,"uniform_no","like%");
    }
    wp.whereStr += sqlCol(exCrtUser, "crt_user");
    if(empty(wp.queryWhere))	wp.queryWhere = wp.whereStr;
    return true;
  }

  @Override
  public void queryFunc() throws Exception {
//    getWhereStr();
    // -page control-
    
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + "mcht_no, " + "uniform_no, " + "sign_date, " + "forced_flag, "
        + "mcc_code, " + "broken_date, " + "mcht_status, " + "mcht_eng_name, " + "mcht_chi_name, "
        + "mcht_address, " + "mcht_zip, " + "owner_name, " + "owner_id, " + "mcht_tel1, "
        + "mcht_tel1_1, " + "mcht_tel1_2, " + "mcht_tel2, " + "mcht_tel2_1, " + "mcht_tel2_2, "
        + "mcht_fax1, " + "mcht_fax1_1, " + "mcht_fax2, " + "mcht_fax2_1, " + "e_mail, "
        + "contract_name, " + "assign_acct, " + "bank_name, " + "oth_bank_id, " + "oth_bank_acct, "
        + "oth_bank_name, " + "clr_bank_id, " + "mcht_acct_name, " + "mcht_city, "
        + "mcht_country, " + "mcht_state, " + "confirm_flag, " + "contract_head, "
        + "contract_curr_no, " + "mcht_type, " + "loan_flag, " + "mp_rate, " + "mcht_board_name, "
        + "mcht_open_addr, " + "mcht_capital, " + "mcht_setup_date, " + "tx_type, "
        + "advance_flag, " + "chain_type, " + "mcht_property, " + "card_type_name, " + "pos_flag, "
        + "video_flag, " + "rsecind_kind, " + "rsecind_flag, " + "tx_post, " + "post_jcic, "
        + "trans_flag, " + "gift_file_name, " + "gift_file_passwd, " + "gift_file_dir, "
        + "stmt_inst_flag, " + "installment_delay, " + "crt_user, " + "crt_date, " + "apr_user, "
        + "apr_date, " + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, " + "chk_online ";
    wp.daoTable = "bil_merchant_t";
    wp.whereOrder = " order by mcht_no";
    getWhereStr();

    // System.out.println("select " + wp.selectSQL + " from " + wp.daoTable
    // + wp.whereStr + wp.whereOrder);

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    apprDisabled("mod_user");
    listWkdata();
  }

  void listWkdata() throws Exception {

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      // mcht_type中文
      String[] cde = new String[] {"0", "1", "2", "3"};
      String[] txt = new String[] {"自行", "郵購", "NCCC", "財金"};
      wp.colSet(ii, "db_mcht_type", commString.decode(wp.colStr(ii, "mcht_type"), cde, txt));

      // mcht_status中文
      String[] cde1 = new String[] {"1", "2", "3"};
      String[] txt1 = new String[] {"1.正常", "2.解約", "3.移轉"};
      wp.colSet(ii, "db_mcht_status", commString.decode(wp.colStr(ii, "mcht_status"), cde1, txt1));
    }
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mExMchtNo)) {
      mExMchtNo = itemKk("data_k1");
    }

    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "mcht_no, " + "uniform_no, "
        + "forced_flag, " + "mcht_eng_name, " + "mcht_chi_name, " + "mcht_address, " + "mcht_zip, "
        + "owner_name, " + "owner_id, " + "mcht_tel2, " + "mcht_fax1, " + "mcht_fax2, " + "e_mail, "
        + "contract_name, " + "assign_acct, " + "bank_name, " + "oth_bank_id, " + "oth_bank_acct, "
        + "mcht_acct_name, " + "mcht_city, " + "mcht_country, " + "mcht_state, " + "confirm_flag, "
        + "mod_user, " + "mod_time, " + "mod_pgm, " + "mod_seqno, " + "clr_bank_id, "
        + "sign_date, " + "contract_head, " + "contract_curr_no, " + "mcht_tel1, " + "mcht_tel1_1, "
        + "mcht_tel1_2, " + "mcht_tel2_1, " + "mcht_tel2_2, " + "mcht_fax1_1, " + "mcht_fax2_1, "
        + "oth_bank_name, " + "'   ' db_max, " + "broken_date, " + "mcht_status, " + "mcc_code, "
        + "loan_flag, " + "mp_rate, " + "mcht_type, " + "mcht_board_name, " + "mcht_open_addr, "
        + "mcht_capital, " + "mcht_setup_date, " + "tx_type, " + "advance_flag, " + "chain_type, "
        + "mcht_property, " + "card_type_name, " + "pos_flag, " + "video_flag, " + "rsecind_flag, "
        + "crt_user, " + "crt_date, " + "apr_user, " + "apr_date, " + "tx_post, " + "post_jcic, "
        + "rsecind_kind, " + "trans_flag, " + "gift_file_name, " + "gift_file_passwd, "
        + "gift_file_dir, " + "stmt_inst_flag, " + "installment_delay, " + "chk_online ";
    wp.daoTable = "bil_merchant_t ";
    wp.whereStr = " where 1=1 ";
    wp.whereStr += sqlCol(mExMchtNo, "mcht_no");

    // System.out.println("select1 " + wp.selectSQL + " from "
    // +wp.daoTable+wp.whereStr+wp.whereOrder);
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, mcht_no =" + mExMchtNo);
    }
    
  }

  @Override
  public void saveFunc() throws Exception {
    String[] opt = wp.itemBuff("opt");
    String[] aaMchtNo = wp.itemBuff("mcht_no");
    String[] aaUniformNo = wp.itemBuff("uniform_no");
    String[] aaSignDate = wp.itemBuff("sign_date");
    String[] aaForcedFlag = wp.itemBuff("forced_flag");
    String[] aaMccCode = wp.itemBuff("mcc_code");
    String[] aaBrokenDate = wp.itemBuff("broken_date");
    String[] aaMchtStatus = wp.itemBuff("mcht_status");
    String[] aaMchtEngName = wp.itemBuff("mcht_eng_name");
    String[] aaMchtChiName = wp.itemBuff("mcht_chi_name");
    String[] aaMchtAddress = wp.itemBuff("mcht_address");
    String[] aaMchtZip = wp.itemBuff("mcht_zip");
    String[] aaOwnerName = wp.itemBuff("owner_name");
    String[] aaOwnerId = wp.itemBuff("owner_id");
    String[] aaMchtTel1 = wp.itemBuff("mcht_tel1");
    String[] aaMchtTel11 = wp.itemBuff("mcht_tel1_1");
    String[] aaMchtTel12 = wp.itemBuff("mcht_tel1_2");
    String[] aaMchtTel2 = wp.itemBuff("mcht_tel2");
    String[] aaMchtTel21 = wp.itemBuff("mcht_tel2_1");
    String[] aaMchtTel22 = wp.itemBuff("mcht_tel2_2");
    String[] aaMchtFax1 = wp.itemBuff("mcht_fax1");
    String[] aaMchtFax11 = wp.itemBuff("mcht_fax1_1");
    String[] aaMchtFax2 = wp.itemBuff("mcht_fax2");
    String[] aaMchtFax21 = wp.itemBuff("mcht_fax2_1");
    String[] aaEMail = wp.itemBuff("e_mail");
    String[] aaContractName = wp.itemBuff("contract_name");
    String[] aaAssignAcct = wp.itemBuff("assign_acct");
    String[] aaBankName = wp.itemBuff("bank_name");
    String[] aaOthBankId = wp.itemBuff("oth_bank_id");
    String[] aaOthBankAcct = wp.itemBuff("oth_bank_acct");
    String[] aaOthBankName = wp.itemBuff("oth_bank_name");
    String[] aaClrBankId = wp.itemBuff("clr_bank_id");
    String[] aaMchtAcctName = wp.itemBuff("mcht_acct_name");
    String[] aaMchtCity = wp.itemBuff("mcht_city");
    String[] aaMchtCountry = wp.itemBuff("mcht_country");
    String[] aaMchtState = wp.itemBuff("mcht_state");
    String[] aaContractHead = wp.itemBuff("contract_head");
    String[] aaContractCurrNo = wp.itemBuff("contract_curr_no");
    String[] aaMchtType = wp.itemBuff("mcht_type");
    String[] aaLoanFlag = wp.itemBuff("loan_flag");
    String[] aaMpRate = wp.itemBuff("mp_rate");
    String[] aaMchtBoardName = wp.itemBuff("mcht_board_name");
    String[] aaMchtOpenAddr = wp.itemBuff("mcht_open_addr");
    String[] aaMchtCapital = wp.itemBuff("mcht_capital");
    String[] aaMchtSetupDate = wp.itemBuff("mcht_setup_date");
    String[] aaTxType = wp.itemBuff("tx_type");
    String[] aaAdvanceFlag = wp.itemBuff("advance_flag");
    String[] aaChainType = wp.itemBuff("chain_type");
    String[] aaMchtProperty = wp.itemBuff("mcht_property");
    String[] aaCardTypeName = wp.itemBuff("card_type_name");
    String[] aaPosFlag = wp.itemBuff("pos_flag");
    String[] aaVideoFlag = wp.itemBuff("video_flag");
    String[] aaRsecindKind = wp.itemBuff("rsecind_kind");
    String[] aaRsecindFlag = wp.itemBuff("rsecind_flag");
    String[] aaTxPost = wp.itemBuff("tx_post");
    String[] aaPostJcic = wp.itemBuff("post_jcic");
    String[] aaTransFlag = wp.itemBuff("trans_flag");
    String[] aaGiftFileName = wp.itemBuff("gift_file_name");
    String[] aaGiftFilePasswd = wp.itemBuff("gift_file_passwd");
    String[] aaGiftFileDir = wp.itemBuff("gift_file_dir");
    String[] aaInstallmentDelay = wp.itemBuff("installment_delay");
    String[] aaStmtInstFlag = wp.itemBuff("stmt_inst_flag");
    String[] aaChkOnline = wp.itemBuff("chk_online"); // 20180927 add
                                                      // Andy
    String[] aaCrtUser = wp.itemBuff("crt_user");
    String[] aaCrtDate = wp.itemBuff("crt_date");

    wp.listCount[0] = aaMchtNo.length;

    // check
    int rr = -1;
    int llOK = 0, llErr = 0;
    for (int ii = 0; ii < opt.length; ii++) {
      rr = (int) this.toNum(opt[ii]) - 1;
      if (rr < 0) {
        continue;
      }

    }

    // save

    if (llErr == 0) {
      // -update-
      rr = -1;
      for (int ii = 0; ii < opt.length; ii++) {
        rr = (int) this.toNum(opt[ii]) - 1;
        if (rr < 0) {
          continue;
        }
        mExMchtNo = aaMchtNo[rr];

        String mRowid = "", mModSeqno = "";
        String lsSql =
            "select hex(rowid) as rowid, mod_seqno " + "from bil_merchant " + "where 1=1 ";
        lsSql += sqlCol(mExMchtNo, "mcht_no");
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
          mRowid = sqlStr("rowid");
          mModSeqno = sqlStr("mod_seqno");
          // update bil_merchant
          String usSql = " update bil_merchant set "
              // + "mcht_no =:mcht_no , "
              + "uniform_no =:uniform_no, " // 1
              + "sign_date =:sign_date, " + "forced_flag =:forced_flag, " + "mcc_code =:mcc_code, "
              + "broken_date =:broken_date, " + "mcht_status =:mcht_status, "
              + "mcht_eng_name =:mcht_eng_name, " + "mcht_chi_name =:mcht_chi_name, "
              + "mcht_address =:mcht_address, " + "mcht_zip =:mcht_zip, "

              + "owner_name =:owner_name, " // 11
              + "owner_id =:owner_id, " + "mcht_tel1 =:mcht_tel1, " + "mcht_tel1_1 =:mcht_tel1_1, "
              + "mcht_tel1_2 =:mcht_tel1_2, " + "mcht_tel2 =:mcht_tel2, "
              + "mcht_tel2_1 =:mcht_tel2_1, " + "mcht_tel2_2 =:mcht_tel2_2, "
              + "mcht_fax1 =:mcht_fax1, " + "mcht_fax1_1 =:mcht_fax1_1, "

              + "mcht_fax2 =:mcht_fax2, " // 21
              + "mcht_fax2_1 =:mcht_fax2_1, " + "e_mail =:e_mail, "
              + "contract_name =:contract_name, " + "assign_acct =:assign_acct, "
              + "bank_name =:bank_name, " + "oth_bank_id =:oth_bank_id, "
              + "oth_bank_acct =:oth_bank_acct, " + "oth_bank_name =:oth_bank_name, "
              + "clr_bank_id =:clr_bank_id, "

              + "mcht_acct_name =:mcht_acct_name, " // 31
              + "mcht_city =:mcht_city, " + "mcht_country =:mcht_country, "
              + "mcht_state =:mcht_state, " + "confirm_flag =:confirm_flag, "
              + "contract_head =:contract_head, " + "contract_curr_no =:contract_curr_no, "
              + "mcht_type =:mcht_type, " + "loan_flag =:loan_flag, " + "mp_rate =:mp_rate, "

              + "mcht_board_name =:mcht_board_name, " // 41
              + "mcht_open_addr =:mcht_open_addr, " + "mcht_capital =:mcht_capital, "
              + "mcht_setup_date =:mcht_setup_date, " + "tx_type =:tx_type, "
              + "advance_flag =:advance_flag, " + "chain_type =:chain_type, "
              + "mcht_property =:mcht_property, " + "card_type_name =:card_type_name, "
              + "pos_flag =:pos_flag, "

              + "video_flag =:video_flag, " // 51
              + "rsecind_kind =:rsecind_kind, " + "rsecind_flag =:rsecind_flag, "
              + "tx_post =:tx_post, " + "post_jcic =:post_jcic, " + "trans_flag =:trans_flag, "
              + "gift_file_name =:gift_file_name, " + "gift_file_passwd =:gift_file_passwd, "
              + "gift_file_dir =:gift_file_dir, " + "installment_delay =:installment_delay, "

              + "stmt_inst_flag =:stmt_inst_flag," // 61
              + "chk_online =:chk_online, " + "crt_user =:crt_user, " + "crt_date =:crt_date, "
              + "apr_user =:apr_user, " + "apr_date =:apr_date, " + "mod_user =:mod_user, "
              + "mod_time = sysdate, " + "mod_pgm = 'bilp0020', "
              + "mod_seqno = nvl(mod_seqno,0)+1 "
              + "where  hex(rowid) = :m_rowid  and mod_seqno = :m_mod_seqno ";
          // + "where mcht_no = :mcht_no ";
          setString("uniform_no", aaUniformNo[rr]);
          setString("sign_date", aaSignDate[rr]);
          setString("forced_flag", aaForcedFlag[rr]);
          setString("mcc_code", aaMccCode[rr]);
          setString("broken_date", aaBrokenDate[rr]);
          setString("mcht_status", aaMchtStatus[rr]);
          setString("mcht_eng_name", aaMchtEngName[rr]);
          setString("mcht_chi_name", aaMchtChiName[rr]);
          setString("mcht_address", aaMchtAddress[rr]);
          setString("mcht_zip", aaMchtZip[rr]);

          setString("owner_name", aaOwnerName[rr]);
          setString("owner_id", aaOwnerId[rr]);
          setString("mcht_tel1", aaMchtTel1[rr]);
          setString("mcht_tel1_1", aaMchtTel11[rr]);
          setString("mcht_tel1_2", aaMchtTel12[rr]);
          setString("mcht_tel2", aaMchtTel2[rr]);
          setString("mcht_tel2_1", aaMchtTel21[rr]);
          setString("mcht_tel2_2", aaMchtTel22[rr]);
          setString("mcht_fax1", aaMchtFax1[rr]);
          setString("mcht_fax1_1", aaMchtFax11[rr]);

          setString("mcht_fax2", aaMchtFax2[rr]);
          setString("mcht_fax2_1", aaMchtFax21[rr]);
          setString("e_mail", aaEMail[rr]);
          setString("contract_name", aaContractName[rr]);
          setString("assign_acct", aaAssignAcct[rr]);
          setString("bank_name", aaBankName[rr]);
          setString("oth_bank_id", aaOthBankId[rr]);
          setString("oth_bank_acct", aaOthBankAcct[rr]);
          setString("oth_bank_name", aaOthBankName[rr]);
          setString("clr_bank_id", aaClrBankId[rr]);

          setString("mcht_acct_name", aaMchtAcctName[rr]);
          setString("mcht_city", aaMchtCity[rr]);
          setString("mcht_country", aaMchtCountry[rr]);
          setString("mcht_state", aaMchtState[rr]);
          setString("confirm_flag", "Y");
          setString("contract_head", aaContractHead[rr]);
          setString("contract_curr_no", aaContractCurrNo[rr]);
          setString("mcht_type", aaMchtType[rr]);
          setString("loan_flag", aaLoanFlag[rr]);
          setString("mp_rate", aaMpRate[rr]);

          setString("mcht_board_name", aaMchtBoardName[rr]);
          setString("mcht_open_addr", aaMchtOpenAddr[rr]);
          setString("mcht_capital", aaMchtCapital[rr]);
          setString("mcht_setup_date", aaMchtSetupDate[rr]);
          setString("tx_type", aaTxType[rr]);
          setString("advance_flag", aaAdvanceFlag[rr]);
          setString("chain_type", aaChainType[rr]);
          setString("mcht_property", aaMchtProperty[rr]);
          setString("card_type_name", aaCardTypeName[rr]);
          setString("pos_flag", aaPosFlag[rr]);

          setString("video_flag", aaVideoFlag[rr]);
          setString("rsecind_kind", aaRsecindKind[rr]);
          setString("rsecind_flag", aaRsecindFlag[rr]);
          setString("tx_post", aaTxPost[rr]);
          setString("post_jcic", aaPostJcic[rr]);
          setString("trans_flag", aaTransFlag[rr]);
          setString("gift_file_name", aaGiftFileName[rr]);
          setString("gift_file_passwd", aaGiftFilePasswd[rr]);
          setString("gift_file_dir", aaGiftFileDir[rr]);
          setString("installment_delay", aaInstallmentDelay[rr]);

          setString("stmt_inst_flag", aaStmtInstFlag[rr]);
          setString("chk_online", aaChkOnline[rr]);
          setString("crt_user", aaCrtUser[rr]);
          setString("crt_date", aaCrtDate[rr]);
          setString("apr_user", wp.loginUser);
          setString("apr_date", getSysDate());
          setString("mod_user", wp.loginUser);

          setString("m_rowid", mRowid);
          setString("m_mod_seqno", mModSeqno);
          // setString("mcht_no",m_ex_mcht_no);

          sqlExec(usSql);
          if (sqlRowNum <= 0) {
            wp.colSet(rr, "ok_flag", "!資料更新失敗");
            llErr++;
            sqlCommit(0);
            return;
          } else {
            wp.colSet(rr, "ok_flag", "V");
            llOK++;
          }
        } else {
          // insert bil_merchant
          String liSql = "insert into bil_merchant ( "
              + "mcht_no,			uniform_no,		 	sign_date,			forced_flag,		mcc_code, "
              + "broken_date,	 	mcht_status,		mcht_eng_name,		mcht_chi_name,		mcht_address, "
              + "mcht_zip,		owner_name, 		owner_id, 			mcht_tel1,			mcht_tel1_1, "
              + "mcht_tel1_2,		mcht_tel2, 			mcht_tel2_1, 		mcht_tel2_2, 		mcht_fax1, "
              + "mcht_fax1_1,		mcht_fax2, 			mcht_fax2_1, 		e_mail, 			contract_name, "
              + "assign_acct,		bank_name, 			oth_bank_id, 		oth_bank_acct, 		oth_bank_name, "
              + "clr_bank_id,		mcht_acct_name, 	mcht_city, 			mcht_country, 		mcht_state, "
              + "confirm_flag,	contract_head, 		contract_curr_no, 	mcht_type, 			loan_flag, "
              + "mp_rate,			mcht_board_name, 	mcht_open_addr, 	mcht_capital, 		mcht_setup_date, "
              + "tx_type,			advance_flag, 		chain_type, 		mcht_property, 		card_type_name, "
              + "pos_flag,		video_flag, 		rsecind_kind, 		rsecind_flag, 		tx_post, "
              + "post_jcic,		trans_flag, 		gift_file_name, 	gift_file_passwd, 	gift_file_dir, "
              + "installment_delay,stmt_inst_flag,	crt_user, 			crt_date, 			apr_user, "
              + "apr_date,		mod_user,	 		mod_time, 			mod_pgm, 			mod_seqno, "
              + "chk_online " + ") values ( "
              + ":mcht_no, 		:uniform_no, 		:sign_date, 		:forced_flag, 		:mcc_code, "
              + ":broken_date, 	:mcht_status, 		:mcht_eng_name, 	:mcht_chi_name, 	:mcht_address, "
              + ":mcht_zip, 		:owner_name, 		:owner_id, 			:mcht_tel1,			:mcht_tel1_1, "
              + ":mcht_tel1_2, 	:mcht_tel2, 		:mcht_tel2_1, 		:mcht_tel2_2, 		:mcht_fax1, "
              + ":mcht_fax1_1,	:mcht_fax2, 		:mcht_fax2_1, 		:e_mail, 			:contract_name, "
              + ":assign_acct, 	:bank_name, 		:oth_bank_id, 		:oth_bank_acct, 	:oth_bank_name, "
              + ":clr_bank_id, 	:mcht_acct_name, 	:mcht_city, 		:mcht_country, 		:mcht_state, "
              + ":confirm_flag, 	:contract_head, 	:contract_curr_no, 	:mcht_type, 		:loan_flag, "
              + ":mp_rate, 		:mcht_board_name, 	:mcht_open_addr, 	:mcht_capital, 		:mcht_setup_date, "
              + ":tx_type, 		:advance_flag,		:chain_type, 		:mcht_property, 	:card_type_name, "
              + ":pos_flag, 		:video_flag, 		:rsecind_kind, 		:rsecind_flag,		:tx_post, "
              + ":post_jcic, 		:trans_flag, 		:gift_file_name, 	:gift_file_passwd, 	:gift_file_dir, "
              + ":installment_delay, :stmt_inst_flag,	:crt_user, 			:crt_date,			:apr_user, "
              + ":apr_date,		:mod_user, 			sysdate, 			'bilp0020', 		1, "
              + ":chk_online " + ")";

          setString("mcht_no", aaMchtNo[rr]);
          setString("uniform_no", aaUniformNo[rr]);
          setString("sign_date", aaSignDate[rr]);
          setString("forced_flag", aaForcedFlag[rr]);
          setString("mcc_code", aaMccCode[rr]);

          setString("broken_date", aaBrokenDate[rr]);
          setString("mcht_status", aaMchtStatus[rr]);
          setString("mcht_eng_name", aaMchtEngName[rr]);
          setString("mcht_chi_name", aaMchtChiName[rr]);
          setString("mcht_address", aaMchtAddress[rr]);

          setString("mcht_zip", aaMchtZip[rr]);
          setString("owner_name", aaOwnerName[rr]);
          setString("owner_id", aaOwnerId[rr]);
          setString("mcht_tel1", aaMchtTel1[rr]);
          setString("mcht_tel1_1", aaMchtTel11[rr]);

          setString("mcht_tel1_2", aaMchtTel12[rr]);
          setString("mcht_tel2", aaMchtTel2[rr]);
          setString("mcht_tel2_1", aaMchtTel21[rr]);
          setString("mcht_tel2_2", aaMchtTel22[rr]);
          setString("mcht_fax1", aaMchtFax1[rr]);

          setString("mcht_fax1_1", aaMchtFax11[rr]);
          setString("mcht_fax2", aaMchtFax2[rr]);
          setString("mcht_fax2_1", aaMchtFax21[rr]);
          setString("e_mail", aaEMail[rr]);
          setString("contract_name", aaContractName[rr]);

          setString("assign_acct", aaAssignAcct[rr]);
          setString("bank_name", aaBankName[rr]);
          setString("oth_bank_id", aaOthBankId[rr]);
          setString("oth_bank_acct", aaOthBankAcct[rr]);
          setString("oth_bank_name", aaOthBankName[rr]);

          setString("clr_bank_id", aaClrBankId[rr]);
          setString("mcht_acct_name", aaMchtAcctName[rr]);
          setString("mcht_city", aaMchtCity[rr]);
          setString("mcht_country", aaMchtCountry[rr]);
          setString("mcht_state", aaMchtState[rr]);

          setString("confirm_flag", "Y");
          setString("contract_head", aaContractHead[rr]);
          setString("contract_curr_no", aaContractCurrNo[rr]);
          setString("mcht_type", aaMchtType[rr]);
          setString("loan_flag", aaLoanFlag[rr]);

          setString("mp_rate", aaMpRate[rr]);
          setString("mcht_board_name", aaMchtBoardName[rr]);
          setString("mcht_open_addr", aaMchtOpenAddr[rr]);
          setString("mcht_capital", aaMchtCapital[rr]);
          setString("mcht_setup_date", aaMchtSetupDate[rr]);

          setString("tx_type", aaTxType[rr]);
          setString("advance_flag", aaAdvanceFlag[rr]);
          setString("chain_type", aaChainType[rr]);
          setString("mcht_property", aaMchtProperty[rr]);
          setString("card_type_name", aaCardTypeName[rr]);

          setString("pos_flag", aaPosFlag[rr]);
          setString("video_flag", aaVideoFlag[rr]);
          setString("rsecind_kind", aaRsecindKind[rr]);
          setString("rsecind_flag", aaRsecindFlag[rr]);
          setString("tx_post", aaTxPost[rr]);

          setString("post_jcic", aaPostJcic[rr]);
          setString("trans_flag", aaTransFlag[rr]);
          setString("gift_file_name", aaGiftFileName[rr]);
          setString("gift_file_passwd", aaGiftFilePasswd[rr]);
          setString("gift_file_dir", aaGiftFileDir[rr]);

          setString("installment_delay", aaInstallmentDelay[rr]);
          setString("stmt_inst_flag", aaStmtInstFlag[rr]);
          setString("crt_user", wp.loginUser);
          setString("crt_date", getSysDate());
          setString("apr_user", wp.loginUser);

          setString("apr_date", getSysDate());
          setString("mod_user", wp.loginUser);
          setString("chk_online", aaChkOnline[rr]);

          sqlExec(liSql);

          if (sqlRowNum <= 0) {
            wp.colSet(rr, "ok_flag", "!資料新增失敗");
            llErr++;
            sqlCommit(0);
            return;
          } else {
            wp.colSet(rr, "ok_flag", "V");
            llOK++;
          }

        }
		if (dbDelete(mExMchtNo) != 1) {
			wp.colSet(rr, "ok_flag", "!刪除暫存檔失敗");
			llErr++;
			sqlCommit(0);
			return;
		}
      }
//      if (dbDelete(mExMchtNo) != 1) {
//        wp.colSet(rr, "ok_flag", "!刪除暫存檔失敗");
//        llErr++;
//        sqlCommit(0);
//        return;
//      }
      if (llErr == 0) {
        sqlCommit(1);
      }
      alertMsg("放行處理: 成功筆數=" + llOK);
    }
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      // wp.initOption = "--";
      // wp.optionKey = wp.item_ss("ex_mcht_no");
      // this.dddw_list("dddw_mcht_no", "bil_merchant", "mcht_no",
      // "mcht_chi_name", "where 1=1 and mcht_status = '1' group by
      // mcht_no,mcht_chi_name order by mcht_no");
    } catch (Exception ex) {
    }
  }

  public int dbDelete(String mchtNo) throws Exception {
    String lsSql = " delete bil_merchant_t where 1=1 ";
    if (!empty(mchtNo)) {
      lsSql += sqlCol(mchtNo, "mcht_no");
    } else {
      return -1;
    }

    sqlExec(lsSql);
    if (sqlRowNum <= 0) {
      alertErr(sqlErrtext);
      return -1;
    }
    return 1;
  }

  public void processAjaxOption() throws Exception {
    wp.varRows = 1000;
    setSelectLimit(0);
    // 原來寫法會受主頁面的top.pageRows = "??"及pageQuery() 影響;
    // wp.selectSQL = "mcht_no,mcht_chi_name";
    // wp.daoTable = "bil_merchant";
    // wp.whereStr = "where mcht_status = '1' and mcht_no like :mcht_no ";
    // wp.orderField = "mcht_no";
    //
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // setString("mcht_no",wp.getValue("mcht_no",0)+"%");
    // }else{
    // setString("mcht_no",wp.getValue("ex_merchant",0)+"%");
    // }
    // pageQuery();
    //
    // //for ( int i=0; i <wp.selectCnt; i++ )
    // for ( int i=0; i < 1000; i++ )
    // {
    // wp.addJSON("OPTION_TEXT",wp.col_ss(i,"mcht_no")+"_"+wp.col_ss(i,"mcht_chi_name"));
    // wp.addJSON("OPTION_VALUE",wp.col_ss(i,"mcht_no"));
    // }

    // 改由自行下SQL
    String lsSql = "select mcht_no,mcht_chi_name " + " ,mcht_no||'_'||mcht_chi_name as inter_desc "
        + " from bil_merchant " + " where mcht_status = '1' and mcht_no like :mcht_no "
        + " order by mcht_no ";
    if (wp.respHtml.indexOf("_detl") > 0) {
      setString("mcht_no", wp.getValue("mcht_no", 0) + "%");
    } else {
      setString("mcht_no", wp.getValue("ex_merchant", 0) + "%");
    }
    sqlSelect(lsSql);

    for (int i = 0; i < sqlRowNum; i++) {
      wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
      wp.addJSON("OPTION_VALUE", sqlStr(i, "mcht_no"));
    }

    return;
  }
  
//	void apprDisabled(String col) throws Exception {
//		for (int ll = 0; ll < wp.listCount[0]; ll++) {
//			if (!wp.colStr(ll, col).equals(wp.loginUser)) {
//				wp.colSet(ll, "opt_disabled", "");
//			} else
//				wp.colSet(ll, "opt_disabled", "disabled");
//		}
//	}

}
