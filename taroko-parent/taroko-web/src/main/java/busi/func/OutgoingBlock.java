/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
* 109-12-25  V1.00.02  Justin        zz -> comm
* 111-10-25  V1.00.03  Ryan        送NCCC的原因碼如遇到bin_type=J時需要轉為對應的數字才會報送成功
* 112-08-22  V1.00.04  Ryan        FISC與NCCC的reason_code改為一致             *
******************************************************************************/
package busi.func;
/** CCAS 凍結outgoing公用程式
 * 2019-0610:  JH    p_seqno >>acno_p_seqno
 2018-1220:  JH    _card_no=p1_card_no
 2018-1113:  JH    ccaQ2030
 2018-1106:	JH		modify
 * 2018-1003:	JH		debitCard: 不會outgoing
 * 2018-0314:	JH		initial
 * 110-01-07  V1.00.07  tanwei        修改意義不明確變量                                                                          *
 * 111-11-10  V1.00.08  Alex  修改從crd_card撈取 spec_status 欄位 改至 cca_card_base
 * 111-11-14  V1.00.09  Alex  outgoingDelete 送 Outgoing時帶入刪除日期 , 解除凍結時有特指尚未到刪除日期卡號不報送刪除 Outgoing
 * */

public class OutgoingBlock extends OutgoingBase {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  HhData hh = new HhData();
  // taroko.base.SqlParm tt_outgo=new taroko.base.SqlParm();
  // taroko.base.SqlParm tt_ibm=new taroko.base.SqlParm();

  public boolean isDebit = false;
  public String oriNegReason = "", oriVmjReason = "";
  public String blockReason = "";

  private String kkPseqno = "";
  // String[] aa_block=new String[6];
  boolean isOutgoing = true;
  boolean isInsertOutgoing = true;

  CcaSpecialVisa ooSpec = null;

  public int blockIbmNegfile(String aFileCode) {
    // a_bank_cardno,a_bin_type,a_neg_reason,a_del_date,a_file
    this.ftpIbmNegfile(aFileCode);

    hh.initData();
    initPp2hh();

    hh.bitmap = getBitmap();
    hh.keyTable = "SPECIAL_VISA";
    hh.keyValue = "IBM";

    insertIbmOutgoing(aFileCode);
    return rc;
  }

  //111-10-25  V1.00.03  Ryan 送NCCC的原因碼如遇到bin_type=J時需要轉為對應的數字才會報送成功
  public int blockNegId(String aFileCode) {
    // a_card_no, a_card_type, a_reason_code, a_del_date, a_file_code
	String fiscReasonCode = p4Reason;
	if (p2BinType.equals("V") || p2BinType.equals("M")) {
		if(fiscReasonCode.equals("Q") || fiscReasonCode.equals("R")) {
			p4Reason = "U";
		}
	}
	if (p2BinType.equals("J")) {
		switch (fiscReasonCode) {
		case "L":
			p4Reason = "41";
			break;
		case "S":
			p4Reason = "43";
			break;
		case "C":
			p4Reason = "04";
			break;
		case "F":
			p4Reason = "07";
			break;
		case "U":
			p4Reason = "05";
			break;
		case "R":
			p4Reason = "01";
			break;	
		}
	}
	  
    this.ftpNegId(aFileCode);
    wpCallStatus("NEG");

    hh.initData();
    initPp2hh();

    hh.blockCode = blockReason;
    hh.actCode = aFileCode;
    hh.keyValue = "NCCC";
    hh.keyTable = "CARD_BASE_SPEC";
    hh.bitmap = getBitmap();
    insertCcaOutgoing();
//    p4Reason = fiscReasonCode;
    blockFisc(aFileCode); 
    
    return rc;
  }
  
  public int blockFisc(String aFileCode) {
	    // a_card_no, a_card_type, a_reason_code, a_del_date, a_file_code
	    this.ftpFiscReq(aFileCode);
	    wpCallStatus("FISC");

	    hh.initData();
	    initPp2hh();

	    hh.blockCode = blockReason;
	    hh.actCode = aFileCode;
	    hh.keyValue = "FISC";
	    hh.keyTable = "CARD_BASE_SPEC";
	    hh.bitmap = getBitmap();
	    insertCcaOutgoing();

	    return rc;
  }

  public int blockVmjReq(String aFile) {
    if (eq(p2BinType, "V")) {
      return blockVisaReq(aFile);
    } else if (eq(p2BinType, "M")) {
      return blockMasterReq2(aFile);
    } else if (eq(p2BinType, "J"))
      return blockJcbReq(aFile);

    errmsg("bin_type: 不是VMJ");
    return rc;
  }


  private int blockVisaReq(String aFileCode) {
    msgOK();

    // a_card_no, a_purg_date, a_file_code, a_reason, a_area
    this.ftpVisaReq(aFileCode);
    wpCallStatus("Visa");

    hh.initData();
    initPp2hh();

    hh.keyValue = "VISA";
    hh.keyTable = "CARD_BASE_SPEC";
    hh.actCode = aFileCode;
    hh.binType = "V";
    hh.blockCode = blockReason;
    hh.bitmap = getBitmap();

    insertCcaOutgoing();
    return rc;

  }

  private int blockMasterReq2(String aFileCode) {
    msgOK();
    // a_card_no, a_file_code, a_vip_amt, a_reason
    this.ftpMasterReq2(aFileCode);
    wpCallStatus("Master2");

    hh.initData();
    initPp2hh();

    hh.keyValue = "MASTER2";
    hh.keyTable = "CARD_BASE_SPEC";
    hh.actCode = aFileCode;
    hh.binType = "M";
    hh.blockCode = blockReason;
    hh.bitmap = getBitmap();

    insertCcaOutgoing();

    return rc;
  }

  private int blockJcbReq(String aFileCode) {
    msgOK();

    // a_card_no, a_purg_date, a_file_code, a_reason, a_region
    ftpJcbReq(aFileCode);
    wpCallStatus("JCB");

    hh.initData();
    initPp2hh();

    hh.keyValue = "JCB";
    hh.actCode = aFileCode;
    hh.keyTable = "CARD_BASE_SPEC";
    hh.binType = "J";
    hh.blockCode = blockReason;
    hh.bitmap = getBitmap();

    insertCcaOutgoing();

    return rc;
  }

  public int cardOutgoingUpdate(String vdFlag, String aPSeqno, String aBlockReason) {
    isDebit = eq(vdFlag, "Y"); // debitCard: 不會代行, 不送outgoing
    // -凍結-outgoing: <0.error--
    kkPseqno = nvl(aPSeqno);
    blockReason = aBlockReason;
    this.selectCcaSpecCode(aBlockReason);

    if (isDebit == false) {
      strSql =
          "select card_no, bin_type, new_end_date, bank_actno, block_code" + " from crd_card"
              + " where current_code ='0' and acno_p_seqno =?";
    } else {
      strSql =
          "select card_no, bin_type, new_end_date, bank_actno, block_code" + " from dbc_card"
              + " where current_code ='0' and p_seqno =?";
    }
    setString2(1, kkPseqno);
    daoTid = "card.";
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      sqlErr("crd_card.select, kk=" + kkPseqno);
      return -1;
    }
    if (sqlRowNum == 0)
      return 1;

    int liCard = sqlRowNum;
    for (int ll = 0; ll < liCard; ll++) {
      parmClear();

      String lsNewEndDate = colStr(ll, "card.new_end_date");
      p1CardNo = colStr(ll, "card.card_no");
      p3BankAcctno = colStr(ll, "card.bank_actno");
      p2BinType = colStr(ll, "card.bin_type");
      p5DelDate = commDate.dateAdd(lsNewEndDate, 0, 0, 1);
      p4Reason = strNegReason;

      String lsAction = "1", lsAud = "A";
      String lsCardBlock = colStr(ll, "card.block_code");
      if (notEmpty(lsCardBlock) && !eq(lsCardBlock, aBlockReason)) {
        lsAction = "2";
        lsAud = "U";
      }

      if (empty(strNegReason) == false) {
        if (isDebit) {
          if (eqIgno(strSendIbm, "Y")) {
//            rc = blockIbmNegfile(lsAction);
        	  rc = 1;
          }
        } else {
          rc = blockNegId(lsAction);
        }
        if (rc != 1)
          return -1;
      }
      if (isDebit)
        continue;

      if (eqIgno(p2BinType, "J")) {
        p4Reason = strJcbReason;
        p7Region = "00000";
      } else if (eqIgno(hh.binType, "M")) {
        p4Reason = strMastReason;
        if (empty(p4Reason))
          continue;
        p6VipAmt = "0";
      } else if (eqIgno(hh.binType, "V")) {
        p4Reason = strVisaReason;
        p7Region = commString.rpad("0", 9);
      } else
        continue;
      if (empty(p4Reason))
        continue;

      rc = blockVmjReq(lsAction);
      if (rc != 1) {
        return -1;
      }

      ccaSpecialVisaUpdate();
    }
    return rc;
  }

  public int ccaQ2030Update(String aBlock) {

    this.selectCcaSpecCode(aBlock);
    String lsActCode = "2";

    blockReason = aBlock;
    p5DelDate = wp.colStr("aa.neg_del_date");

    if (empty(strNegReason) == false) {
      p4Reason = strNegReason;
      if (empty(oriNegReason)) {
        lsActCode = "1";
      }
      rc = blockNegId(lsActCode);
    }
    if (isDebit)
      return rc;

    // -VMJ.outgoing------------------------------
    lsActCode = "2";
    if (empty(oriVmjReason))
      lsActCode = "1";

    if (eqIgno(p2BinType, "J")) {
      p4Reason = strJcbReason;
      p7Region = "00000";
    } else if (eqIgno(p2BinType, "M")) {
      p4Reason = strMastReason;
      p6VipAmt = "0";
    } else if (eqIgno(p2BinType, "V")) {
      p4Reason = strVisaReason;
      p7Region = commString.rpad("0", 9);
    }
    if (empty(p4Reason))
      return rc;
    rc = blockVmjReq(lsActCode);

    return rc;
  }

  public int ccaQ2030Delete(String aBlock) {
    // -outgoing重送: 刪除-
    this.selectCcaSpecCode(aBlock);
    blockReason = aBlock;

    if (empty(strNegReason) == false) {
      p4Reason = strNegReason;
      rc = blockNegId("3");
      if (rc != 1)
        return -1;
    }
    if (isDebit)
      return rc;

    String negDelDate = "";
    String lsActCode = "";
    if (eqIgno(p2BinType, "J")) {
      p4Reason = strJcbReason;
      p5DelDate = negDelDate;
      p7Region = "00000";
      lsActCode = "0";
    } else if (eqIgno(p2BinType, "M")) {
      p4Reason = strMastReason;
      p6VipAmt = "0";
      lsActCode = "3";
    } else if (eqIgno(p2BinType, "V")) {
      p4Reason = strVisaReason;
      p5DelDate = negDelDate;
      p7Region = commString.rpad("0", 9);
      lsActCode = "3";
    }
    if (empty(p4Reason))
      return rc;
    rc = blockVmjReq(lsActCode);

    return rc;
  }

  public int cardOutgoingDelete(String vdFlag, String aPSeqno) {
    isDebit = eq(vdFlag, "Y"); // debitCard: 不會代行, 不送outgoing
    // -凍結-outgoing: <0.error--
    kkPseqno = nvl(aPSeqno);

    if (isDebit) {
      strSql =
          "select card_no, bin_type, new_end_date, bank_actno, block_code"
              + ", uf_spec_status(spec_status, spec_del_date) as spec_status" + " from dbc_card"
              + " where current_code ='0' and p_seqno =?";
    } else {
      strSql =
          "select A.card_no, A.bin_type, A.new_end_date, A.bank_actno, A.block_code"
              + ", uf_spec_status(B.spec_status, B.spec_del_date) as spec_status" 
              + " from crd_card A join cca_card_base B on A.card_no = B.card_no "
              + " where A.current_code ='0' and A.acno_p_seqno =?";
    }
    setString2(1, kkPseqno);

    daoTid = "card.";
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      sqlErr("crd_card.select, kk=" + kkPseqno);
      return -1;
    }
    if (sqlRowNum == 0)
      return 1;

    int liCard = sqlRowNum;
    for (int ll = 0; ll < liCard; ll++) {
      parmClear();
      blockReason = "";
      if (empty(blockReason))
        blockReason = colStr(ll, "card.spec_status");
      //--解除凍結時若該卡有卡特指不可報送 Outgoing 刪除
      if (empty(blockReason) == false)
        continue;
      
      //--取上一次送Outgoing的block_code
      strSql = "select block_code from cca_outgoing where card_no = ? and act_code ='1' order by crt_date Desc , crt_time Desc fetch first 1 rows only ";
      setString2(1,colStr(ll,"card.card_no"));
      daoTid = "outgoing.";
      sqlSelect(strSql);
      
      if(sqlRowNum <0) {
    	  sqlErr("outgoing.select, kk="+colStr(ll,"card.card_no"));
    	  return -1;
      }
      
      //--查無送Outgoing記錄不送刪除
      if(sqlRowNum ==0) {
    	  continue;
      }
      
      blockReason = colStr("outgoing.block_code");
      
      this.selectCcaSpecCode(blockReason);

      p1CardNo = colStr(ll, "card.card_no");
      p2BinType = colStr(ll, "card.bin_type");
      p3BankAcctno = colStr(ll, "card.bank_cardno");
      p5DelDate = colStr(ll, "card.new_end_date");
//      blockReason = strMid(colStr(ll, "card.block_code"), 0, 2);

      if (empty(strNegReason) == false) {
        p4Reason = strNegReason;
        if (isDebit && eqIgno(strSendIbm, "Y")) {
        	//--TCB 沒有說要通知主機 2022/03/8
//          rc = blockIbmNegfile("3");
        	rc = 1 ;
        } else {
          rc = blockNegId("3");
        }
        if (rc != 1)
          return -1;
      }
      if (isDebit)
        continue;

      String lsFile = "";
      if (eqIgno(p2BinType, "J")) {
        p4Reason = strJcbReason;
        p5DelDate = colStr(ll, "card.new_end_date");
        p7Region = "00000";
        lsFile = "0";
      } else if (eqIgno(p2BinType, "M")) {
        p4Reason = strMastReason;
        p5DelDate = colStr(ll, "card.new_end_date");
        p6VipAmt = "0";
        lsFile = "3";
      } else if (eqIgno(p2BinType, "V")) {
        p4Reason = strVisaReason;
        p5DelDate = colStr(ll, "card.new_end_date");
        p7Region = commString.rpad("0", 9);
        lsFile = "3";
      } else
        continue;

      rc = blockVmjReq(lsFile);
      if (rc != 1)
        return -1;

      // --
      ccaSpecialVisaDelete();
    }
    return rc;
  }

  void ccaSpecialVisaDelete() {
    if (ooSpec == null) {
      ooSpec = new CcaSpecialVisa(wp);
    }

    ooSpec.cardNo = p1CardNo;
    ooSpec.vmjRespCode = vmjRespCode;
    ooSpec.negRespCode = negRespCode;
    if (ooSpec.dbDelete() == -1) {
      errmsg(ooSpec.getMsg());
      return;
    }
  }

  void ccaSpecialVisaUpdate() {
    if (ooSpec == null) {
      ooSpec = new CcaSpecialVisa(wp);
    }

    ooSpec.cardNo = p1CardNo;
    ooSpec.blockReason = blockReason;
    ooSpec.negDelDate = p5DelDate;
    ooSpec.mastVipAmt = wp.itemNum("spec_mst_vip_amt");
    ooSpec.vmjReason = p4Reason;
    ooSpec.negReason = strNegReason;
    ooSpec.vmjRespCode = vmjRespCode;
    ooSpec.negRespCode = negRespCode;
    ooSpec.specRemark = wp.itemStr2("spec_remark");

    if (ooSpec.dbUpdate() == -1) {
      errmsg(ooSpec.getMsg());
      return;
    }
  }

  private void insertIbmOutgoing(String aFile) {
    String lsTransDtime = "";
    if (!isInsertOutgoing)
      return;
    hh.bitmap = getBitmap();
    wp.log("ibm-bitmap=[%s]", hh.bitmap);

    busi.SqlPrepare prepare = new busi.SqlPrepare();
    prepare.sql2Insert("cca_ibm_outgoing");
    prepare.addsqlYmd("crt_date");
    prepare.addsqlTime(", crt_time");
    prepare.addsqlParm(", seq_no", ", " + commSqlStr.seqIbmOutgoing);
    prepare.addsqlParm(",?", ", key_table", hh.keyTable);
    prepare.addsqlParm(",?", ", card_no", hh.cardNo);
    prepare.addsqlParm(",?", ", bank_actno", hh.bankAcctNo);
    prepare.addsqlParm(",?", ", acct_no", "");
    prepare.addsqlParm(",?", ", source_type", "B");
    prepare.addsqlParm(",?", ", act_code", aFile);
    prepare.addsqlParm(",?", ", bitmap", hh.bitmap);
    prepare.addsqlParm(",?", ", trans_dtime", "");
    prepare.addsqlParm(",?", ", proc_flag", "0");
    prepare.addsqlParm(",?", ", send_times", 1);
    prepare.addsqlParm(",?", ", crt_user", modUser);
    // tt.aa_ymd(", proc_date");
    // tt.aa_time(", proc_time");
    // tt.aaa(",?",", proc_user",mod_user);
    prepare.addsqlParm(",?", ", data_from", "1");
    // tt.aaa(",?",", resp_code",resp_code);
    prepare.addsqlDate(", mod_time");
    prepare.addsqlParm(",?", ", mod_pgm", modPgm);

    sqlExec(prepare.sqlStmt(), prepare.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_ibm_outgoing err, kk[%s]", hh.cardNo);
      return;
    }
  }

  void insertCcaOutgoing() {
    log("->bitMap:[%s]", hh.bitmap);
    if (!isInsertOutgoing)
      return;
    busi.SqlPrepare prepare = new busi.SqlPrepare();
    prepare.sql2Insert("cca_outgoing");
    prepare.addsqlYmd("crt_date");
    prepare.addsqlTime(", crt_time");
    prepare.addsqlParm(",?", ", card_no", hh.cardNo);
    prepare.addsqlParm(",?", ", key_value", hh.keyValue);
    prepare.addsqlParm(",?", ", key_table", hh.keyTable);
    prepare.addsqlParm(",?", ", bitmap", hh.bitmap);
    prepare.addsqlParm(",?", ", act_code", hh.actCode);
    prepare.addsqlParm(",?", ", crt_user", modUser);
    prepare.addsqlParm(",?", ", proc_flag", "1");
    prepare.addsqlParm(",?", ", send_times", 1);
    prepare.addsqlYmd(", proc_date");
    prepare.addsqlTime(", proc_time");
    prepare.addsqlParm(",?", ", proc_user", modUser);
    prepare.addsqlParm(",?", ", data_from", "1");
    prepare.addsqlParm(",?", ", resp_code", respCode);
    prepare.addsqlParm(",?", ", data_type", "BLOCK");
    prepare.addsqlParm(",?", ", bin_type", hh.binType);
    prepare.addsqlParm(",?", ", reason_code", p4Reason);
    prepare.addsqlParm(",?", ", del_date", p5DelDate);
    prepare.addsqlParm(",?", ", block_code", hh.blockCode);
    prepare.addsqlDate(", mod_time");
    prepare.addsqlParm(",?", ", mod_pgm", modPgm);

    sqlExec(prepare.sqlStmt(), prepare.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_outgoing err, kk[%s]", hh.cardNo);
      return;
    }
  }

  int selectCrdCard() {
    strSql =
        "select" + " card_no," + " current_code," + " bin_type," + " card_type," + " new_end_date"
            + " from crd_card" + " where acno_p_seqno =?";
    daoTid = "card.";
    setString2(1, kkPseqno);
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      errmsg("select crd_card error, kk[%s]", kkPseqno);
      return -1;
    }

    return sqlRowNum;
  }

  private void initPp2hh() {
    hh.cardNo = p1CardNo;
    hh.binType = p2BinType;
    hh.bankAcctNo = p3BankAcctno;
    hh.reasonCode = p4Reason;
    hh.delDate = p5DelDate;
    hh.vipAmt = strToNum(p6VipAmt);
    hh.vmjRegnData = p7Region;
  }

  void xxSelectCcaSpecCode() {
    /*
     * -->SELECT NVL(SPEC_NEG_REASON,' '),NVL(SPEC_VISA_REASON,' '), NVL(SPEC_MAST_REASON,'
     * '),NVL(SPEC_JCB_REASON,' '), NVL(SPEC_AECD_REASON,' '), NVL(SEND_IBM,'N') INTO
     * :DB_NEG_Op4_reason,:DB_VIS_EXCEP_CODE,:DB_MST_AUTH_CODE,:DB_JCB_EXCP_CODE,:DB_AE_EXCP_CODE,
     * :is_send_ibm FROM SPEC_CODE WHERE SPEC_CODE=:wk_block_reason;
     */
  }

  class HhData {
    String cardNo = "";
    String keyValue = "";
    String keyTable = "";
    String bitmap = "";
    String procFlag = "";
    int sendTimes = 0;
    String actCode = "";
    String crtUser = "";
    String procDate = "";
    String procTime = "";
    String procUser = "";
    String dataFrom = "";
    String respCode = "";
    String dataType = "";
    String binType = "";
    String reasonCode = "";
    String delDate = "";
    String bankAcctNo = "";
    String vmjRegnData = "";
    double vipAmt = 0;
    String blockCode = "";
    String specStatus = "";

    void initData() {
      cardNo = "";
      keyValue = "";
      keyTable = "";
      bitmap = "";
      procFlag = "";
      sendTimes = 0;
      actCode = "";
      crtUser = "";
      procDate = "";
      procTime = "";
      procUser = "";
      dataFrom = "";
      respCode = "";
      dataType = "";
      binType = "";
      reasonCode = "";
      delDate = "";
      bankAcctNo = "";
      vmjRegnData = "";
      vipAmt = 0;
      blockCode = "";
      specStatus = "";
    }

  }

}
