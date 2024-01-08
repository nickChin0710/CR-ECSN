/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                               *
* 109-12-30  V1.00.02  Ryan        cca_outgoing的key_table的值改成OPPOSITION    * 
* 110-01-06  V1.00.03  Ryan        送oempay時 actCode = 2                      * 
* 110-12-01  V1.00.04  ryan        update oempay iso          
* 112-08-22  V1.00.05  Ryan        FISC與NCCC的reason_code改為一致             *
******************************************************************************/
package busi.func;
/**CCAS outgoing公用程式
 * 2019-1225   JH    pp_xxx >> p1_xxx
 * 2019-0610:  JH    p_seqno >> acno_p_seqno
 * 2018-0314:	JH		initial
 * 110-01-21 V1.00.00  ryan          del isDebit   
 * 111-03-21 V1.00.01  ryan          oempay insert outgoing 改為 trigger處理   
 * 111-03-28 V1.00.02  ryan          oppoNegId if aFile = 5 ,aFile = 2
 * 111-04-12 V1.00.03  ryan          NCCC格式修正 , ncccReason = fiscReason
 * 111-04-27 V1.00.04  ryan          p4Reason 修正
 * */

public class OutgoingOppo extends OutgoingBase {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  taroko.base.SqlParm outgoSqlParm = new taroko.base.SqlParm();
  taroko.base.SqlParm ibmSqlParm = new taroko.base.SqlParm();
  OutgoingOppoLog oppoLog = null;
  private String pseqno = "";

  // private boolean ib_outgoing=false;
  // private String is_neg_reason;
  // private String is_visa_reason;
  // private String is_mast_reason;
  // private String is_jcb_reason;
  // private String is_neg_cap_code;
  // private boolean ib_debit=false;
  // private String is_send_ibm;
  // private String is_del_date;
  // private String _bit120="";

  // ---
  String cardNo = "";
  String keyValue = "";
  String keyTable = "";
  String actCode = "";
  String bitmap = "";
  String procFlag = "";
  String dataFrom = "1";
  public String fiscReason = "";
  public String vCardRowid = "";
  String twmpReason = "";
  String vCardNo = "";
  public String vCardNos = "";
  public boolean isReOutgoing = false; 
  int sendTimes = 1;
  public boolean isInsertOutgoing = true;
  public boolean iscalltwmp = false;
  public String electronicCode = "";
  
  String startYYmd = "";
  String startTime = "";
  
  public int ccaQ2030Update() {

    return rc;
  }

  public int openNegId(String aCardNo, String aFileCode, String aReasonCode,
      String aDelDate, String aCardType) {
    parmClear();
    p1CardNo = aCardNo;
    p4Reason = aReasonCode;
    p5DelDate = aDelDate;
    p2BinType = aCardType;

    wpCallStatus("neg");

    // tt_outgo.sql2Stmt("");
    outgoSqlParm.setString2("bin_type", p2BinType);
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", p5DelDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", "");
    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));

    // --
    cardNo = p1CardNo;
    keyValue = "FISC";
    keyTable = "CARD_OPEN";
    bitmap = "";
    actCode = aFileCode;

    insertCcaOutgoing();
    return rc;
  }

  public int oppoNegId(String aFile) {
    msgOK();
    startYYmd = commDate.sysDate();
    startTime = commDate.sysTime();
    // _p_card_no, _p_bin_type, _p_reason, _p_del_date, _p_file_code
    if(aFile.equals("5")) {
    	aFile = "2";
    	isInsertOutgoing = false;
    }
    //20220412 add
    p4Reason = fiscReason;
    switch(p2BinType) {
    case "V":
    	if(p4Reason.equals("Q")||p4Reason.equals("R")) {
    		p4Reason = "U";
    	}
    	break;
    case "M":
    	if(p4Reason.equals("Q")||p4Reason.equals("R")) {
    		p4Reason = "U";
    	}
    	break;
    case "J":
    	if(p4Reason.equals("L")) {
    		p4Reason = "41";
    	}
    	if(p4Reason.equals("S")) {
    		p4Reason = "43";
    	}
    	if(p4Reason.equals("C")) {
    		p4Reason = "04";
    	}
    	if(p4Reason.equals("F")) {
    		p4Reason = "07";
    	}
    	if(p4Reason.equals("U")) {
    		p4Reason = "05";
    	}
    	if(p4Reason.equals("R")) {
    		p4Reason = "01";
    	}
    	if(p4Reason.equals("Q")) {
    		p4Reason = "05";
    	}
    	break;
    }
    
    this.ftpNegId(aFile);
    wpCallStatus("neg");
 
    // tt_outgo.sql2Stmt("");
    outgoSqlParm.setString2("bin_type", p2BinType);
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", p5DelDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", "");
    outgoSqlParm.setDouble2("vip_amt", commString.strToNum(p6VipAmt));

    // --
    cardNo = p1CardNo;
    keyValue = "NCCC";
    keyTable = "OPPOSITION";
    bitmap = getBitmap();
    actCode = aFile;
    twmpReason = p4Reason;
    
    insertCcaOutgoing();

    oppoFiscReq(aFile);//FISC
    outElectronic(aFile);//票證
//    outTwmp(aFile);//HCE
    if(!p2BinType.equals("J")) {
    	if(!aFile.equals("5")) {
    		p4Reason = "3701";
//    		outOempay("2");//OEMPAY
    	}
    }

    return rc;
  }
  
  public int oppoNegId(String aCardNo, String aFileCode, String aReasonCode,
	      String aDelDate, String aCardType) {
	    parmClear();
	    p1CardNo = aCardNo;
	    p4Reason = aReasonCode;
	    p5DelDate = aDelDate;
	    p2BinType = aCardType;
	    return oppoNegId(aFileCode);
  }

  public int oppoFiscReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
//	    p4Reason = fiscReason;
	    this.ftpFiscReq(aFile);
	    wpCallStatus("fisc");

	    outgoSqlParm.setString2("bin_type", p2BinType);
	    outgoSqlParm.setString2("reason_code", p4Reason);
	    outgoSqlParm.setString2("del_date", p5DelDate);
	    outgoSqlParm.setString2("bank_acct_no", "");
	    outgoSqlParm.setString2("vmj_regn_data", "");
	    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));

	    // --
	    cardNo = p1CardNo;
	    keyValue = "FISC";
	    keyTable = "OPPOSITION";
	    bitmap = getBitmap();
	    actCode = aFile;

	    insertCcaOutgoing();

	    return rc;
  }
  
  public int oppoTwmpReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
	    if(aFile.equals("3")){
	    	return rc;
	    }

	    // --
  
	    String[]cardNos =  vCardNos.split(",");
	    for(int i = 0;i<cardNos.length;i++){
	    	if(empty(cardNos[i])) 
	    		continue;
	    	vCardNo = cardNos[i];
//	    	this.ftpTwmpReq(aFile,vCardNo);
	 	    wpCallStatus("TWMP");
	 	    bitmap = getBitmap();
	 	    cardNo = p1CardNo;
		    keyValue = "TWMP";
		    keyTable = "OPPOSITION";
		    actCode = aFile;
		    if(!isReOutgoing){
//			    outgoSqlParm.setString2("bin_type", p2BinType);
//			    outgoSqlParm.setString2("reason_code", p4Reason);
//			    outgoSqlParm.setString2("del_date", p5DelDate);
//			    outgoSqlParm.setString2("bank_acct_no", "");
//			    outgoSqlParm.setString2("vmj_regn_data", "");
//			    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
//			    dataFrom = "1";
//		    	updateHceCard();
//		    	insertCcaOutgoing();
		    }else
		    	updateCcaOutgoing();

	    }
	    return rc;
}  
  
  public int oppoOempayReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
	    if(aFile.equals("3")){
	    	return rc;
	    }
	    

	    dataFrom = "1";

	    String[]cardNos =  vCardNos.split(",");
	    for(int i = 0;i<cardNos.length;i++){
	    	if(empty(cardNos[i])) 
	    		continue;
	    	vCardNo = cardNos[i];
	    	this.ftpOempayReq(aFile,vCardNo,p1CardNo);
	 	    wpCallStatus("OEMPAY");
	 	    bitmap = getBitmap();
	 	    cardNo = p1CardNo;
		    keyValue = "OEMPAY";
		    keyTable = "OPPOSITION";
		    actCode = aFile;
		    if(!isReOutgoing){
//			    outgoSqlParm.setString2("bin_type", p2BinType);
//			    outgoSqlParm.setString2("reason_code", p4Reason);
//			    outgoSqlParm.setString2("del_date", p5DelDate);
//			    outgoSqlParm.setString2("bank_acct_no", "");
//			    outgoSqlParm.setString2("vmj_regn_data", "");
//			    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
//		    	updateOempayCard();
//		    	insertCcaOutgoing();
		    }else
		    	updateCcaOutgoing();
	    }
	    return rc;
}  

  public int oppoIbmNegfile(String aFile) {
//    msgOK();
//    // p3_bank_acctno, p2_bin_type, p4_reason, p5_del_date, a_file
//    this.ftpIbmNegfile(a_file);
//
//    cardNo = p1CardNo;
//    bitmap = getBitmap();
//    insertIbmOutgoing(cardNo, p3BankAcctno, a_file);
    return rc;
  }
//
  public int oppoIbmNegfile(String aBankAcctno, String aFileCode, String aNegReason,
      String aDelDate, String aCardType) {
//    // is_bank_actno, is_card_type, is_neg_reason,is_neg_cap,is_neg_del_date,"3"
//    parmClear();
//    p3BankAcctno = a_bank_acctno;
//    p4Reason = a_neg_reason;
//    p5DelDate = a_del_date;
//    p2BinType = a_card_type;
//
    return oppoIbmNegfile(aFileCode);
  }

  public int oppoMasterReq(String aFile, String[] aRegnDate) {
    msgOK();
    startYYmd = commDate.sysDate();
    startTime = commDate.sysTime();
    this.ftpMasterReq(aFile, aRegnDate);
    wpCallStatus("Master");

    String lsRegn = commString.aa2Str(aRegnDate, '|');
    outgoSqlParm.setString2("bin_type", "M");
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", masterDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", lsRegn);
    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));

    // --
    cardNo = p1CardNo;
    keyValue = "MASTER";
    keyTable = "OPPOSITION";
    bitmap = getBitmap();
    actCode = aFile;
    p2BinType = "M";
    vCardNo = "";
    insertCcaOutgoing();
    return rc;
  }

  public int oppoMasterReq(String aCardNo, String aFileCode, String aReasonCode,
      String aCardType, String[] aRegnDate) {

    parmClear();
    p1CardNo = aCardNo;
    p4Reason = aReasonCode;
    p2BinType = aCardType;

    return oppoMasterReq(aFileCode, aRegnDate);
  }

  public int oppoMasterReq2(String aFile) {
    msgOK();
    startYYmd = commDate.sysDate();
    startTime = commDate.sysTime();
    // a_card_no, a_file_code, a_vip_amt, a_reason
    this.ftpMasterReq2(aFile);
    wpCallStatus("Master2");

    outgoSqlParm.setString2("bin_type", "M");
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", masterDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", "");
    outgoSqlParm.setDouble2("vip_amt", commString.strToNum(p6VipAmt));

    // --
    cardNo = p1CardNo;
    keyValue = "MASTER2";
    keyTable = "OPPOSITION";
    bitmap = getBitmap();
    actCode = aFile;
    p2BinType = "M";
    vCardNo = "";
    insertCcaOutgoing();
    return rc;
  }

  public int oppoMasterReq2(String aCardNo, String aFileCode, String aReason, String aVipAmt) {
    parmClear();
    p1CardNo = aCardNo;
    p4Reason = aReason;
    p6VipAmt = aVipAmt;
    return oppoMasterReq2(aFileCode);
  }

  public int oppoJcbReq(String aFile) {
    msgOK();
    startYYmd = commDate.sysDate();
    startTime = commDate.sysTime();
    // a_card_no, a_purg_date, a_file_code, a_reason, a_region
    this.ftpJcbReq(aFile);
    wpCallStatus("JCB");

    outgoSqlParm.setString2("bin_type", "J");
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", p5DelDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", p7Region);
    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
    // --
    cardNo = p1CardNo;
    keyValue = "JCB";
    keyTable = "OPPOSITION";
    bitmap = getBitmap();
    actCode = aFile;
    p2BinType = "J";
    vCardNo = "";
    insertCcaOutgoing();
    return rc;

  }

  public int oppoTwmpReq(String vCardNo,String aFileCode,String aReason,String aCurrentCode){
	  p4Reason = aReason;
	  p9CurrentCode = aCurrentCode;
	  return ftpTwmpReq(vCardNo,aFileCode);
  }
  
  public int oppoOempayReq(String aCardNo, String aFileCode,String aReasonCode ,String binType,String newEndDate ,String oempayCardNo,String currentCode){
	  p2BinType = binType;
	  p4Reason = aReasonCode;
	  p8NewEndDate = newEndDate;
	  p9CurrentCode = currentCode;
	  return ftpOempayReq(aFileCode,oempayCardNo,aCardNo);
  }
  
  public int oppoJcbReq(String aCardNo, String aFileCode, String aReason, String aPurgDate,
      String aRegion) {
    parmClear();
    p1CardNo = aCardNo;
    p4Reason = aReason;
    p5DelDate = aPurgDate;
    p7Region = aRegion;
    return oppoJcbReq(aFileCode);
  }

  public int oppoVisaReq(String aFile) {
    msgOK();
    startYYmd = commDate.sysDate();
    startTime = commDate.sysTime();
    // a_card_no, a_purg_date, a_file_code, a_reason, a_area
    this.ftpVisaReq(aFile);
    wpCallStatus("Visa");

    outgoSqlParm.setString2("bin_type", "V");
    outgoSqlParm.setString2("reason_code", p4Reason);
    outgoSqlParm.setString2("del_date", p5DelDate);
    outgoSqlParm.setString2("bank_acct_no", "");
    outgoSqlParm.setString2("vmj_regn_data", p7Region);
    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));

    // --
    cardNo = p1CardNo;
    keyValue = "VISA";
    keyTable = "OPPOSITION";
    bitmap = getBitmap();
    actCode = aFile;
    p2BinType = "V";
    vCardNo = "";
    insertCcaOutgoing();
    return rc;
  }
  
  public int oppoVisaReq(String aCardNo, String aFileCode, String aReason, String aPurgDate,
	    String aArea) {
	    parmClear();
	    p1CardNo = aCardNo;
	    p4Reason = aReason;
	    p5DelDate = aPurgDate;
	    p7Region = aArea;

	    return oppoVisaReq(aFileCode);
  }
  
  public int oppoTscReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
	    // a_card_no, a_purg_date, a_file_code, a_reason, a_area
	    this.ftpTscReq(aFile);
	    wpCallStatus("TSC");

	    outgoSqlParm.setString2("bin_type", p2BinType);
	    outgoSqlParm.setString2("reason_code", "");
	    outgoSqlParm.setString2("del_date", p5DelDate);
	    outgoSqlParm.setString2("bank_acct_no", "");
	    outgoSqlParm.setString2("vmj_regn_data", p7Region);
	    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
	    
	    // --
	    cardNo = p1CardNo;
	    keyValue = "TSCC";
	    keyTable = "OPPOSITION";
	    bitmap = getBitmap();
	    actCode = aFile;
	    insertCcaOutgoing();
        oppoLog  = new OutgoingOppoLog();
        oppoLog.setConn(wp);
        try {
    		oppoLog.insertOppoLog(p1CardNo,aFile);
    	} catch (Exception e) {
    	}
	    return rc;
  }
  
  public int oppoIpsReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
	    // a_card_no, a_purg_date, a_file_code, a_reason, a_area
	    this.ftpIpsReq(aFile);
	    wpCallStatus("IPS");

	    outgoSqlParm.setString2("bin_type", p2BinType);
	    outgoSqlParm.setString2("reason_code", "");
	    outgoSqlParm.setString2("del_date", p5DelDate);
	    outgoSqlParm.setString2("bank_acct_no", "");
	    outgoSqlParm.setString2("vmj_regn_data", p7Region);
	    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
	    
	    // --
	    cardNo = p1CardNo;
	    keyValue = "IPASS";
	    keyTable = "OPPOSITION";
	    bitmap = getBitmap();
	    actCode = aFile;
	    insertCcaOutgoing();
        oppoLog  = new OutgoingOppoLog();
        oppoLog.setConn(wp);
        try {
    		oppoLog.insertOppoLog(p1CardNo,aFile);
    	} catch (Exception e) {
    	}
	    return rc;
}

  public int oppoIchReq(String aFile) {
	    msgOK();
	    startYYmd = commDate.sysDate();
	    startTime = commDate.sysTime();
	    // a_card_no, a_purg_date, a_file_code, a_reason, a_area
	    this.ftpIchReq(aFile);
	    wpCallStatus("ICH");

	    outgoSqlParm.setString2("bin_type", p2BinType);
	    outgoSqlParm.setString2("reason_code", "");
	    outgoSqlParm.setString2("del_date", p5DelDate);
	    outgoSqlParm.setString2("bank_acct_no", "");
	    outgoSqlParm.setString2("vmj_regn_data", p7Region);
	    outgoSqlParm.setDouble2("vip_amt", strToNum(p6VipAmt));
	    
	    // --
	    cardNo = p1CardNo;
	    keyValue = "ICASH";
	    keyTable = "OPPOSITION";
	    bitmap = getBitmap();
	    actCode = aFile;
	    insertCcaOutgoing();
        oppoLog  = new OutgoingOppoLog();
        oppoLog.setConn(wp);
        try {
    		oppoLog.insertOppoLog(p1CardNo,aFile);
    	} catch (Exception e) {
    	}
	    return rc;
}  
  
  void insertIbmOutgoing(String aCardNo, String aBankAcctno, String aFile) {
    String lsCardNo = aCardNo;
    if (empty(lsCardNo)) {
      lsCardNo = p1CardNo;
    }

    busi.SqlPrepare tt = new busi.SqlPrepare();
    tt.sql2Insert("cca_ibm_outgoing");
    tt.addsqlParm(" crt_date", commSqlStr.sysYYmd);
    tt.addsqlTime(", crt_time");
    tt.addsqlParm(", seq_no", "," + commSqlStr.seqIbmOutgoing);
    tt.addsqlParm(",?", ", key_table", "OPPOSITION"); // opposition
    tt.addsqlParm(",?", ", card_no", lsCardNo);
    tt.addsqlParm(",?", ", bank_actno", aBankAcctno);
    tt.addsqlParm(",?", ", source_type", "O"); // B
    tt.addsqlParm(",?", ", act_code", aFile); //
    tt.addsqlParm(",?", ", bitmap", bitmap);
    tt.addsqlParm(",?", ", trans_dtime", "");
    tt.addsqlParm(", proc_flag", ",'1'");
    tt.addsqlParm(", send_times", ", 1");
    tt.addsqlParm(",?", ", crt_user", modUser);
    tt.addsqlYmd(", proc_date");
    tt.addsqlTime(", proc_time");
    tt.addsqlParm(",?", ", proc_user", modUser);
    tt.addsqlParm(", data_from", ",'1'");
    tt.addsqlParm(",?", ", resp_code", respCode);

    sqlExec(tt.sqlStmt(), tt.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_ibm_outgoing err, kk[%s]", aCardNo);
      return;
    }
  }

  void insertCcaOutgoing() {
    log("->bitMap:[%s]", bitmap);

    // -查詢不insert-
    if (!isInsertOutgoing || eqIgno(actCode, "5"))
      return;

    strSql =
        "insert into cca_outgoing (" + "  crt_date, crt_time   " + ", card_no    "
            + ", key_value, key_table  " + ", bitmap     "
            + ", act_code   " // 5
            + ", crt_user   "
            + ", proc_flag  " // 7
            + ", send_times " + ", proc_date  " + ", proc_time  "
            + ", proc_user  " // 10
            + ", data_from, resp_code" // 11
            + ", data_type" + ", bin_type" + ", reason_code" + ", del_date" + ", bank_acct_no"
            + ", vmj_regn_data" + ", vip_amt" 
            + ", electronic_card_no "
            + ", current_code "
            + ", new_end_date "
            + ", oppost_date "
            + ", oppost_reason "
            + ", v_card_no "
            + ", mod_time, mod_pgm " 
            + " ) values ("
            + " :startYYmd "
            + ", :startTime "
            + ", :card_no "
            + ", :key_value, :key_table"
            + ", :bitmap"
            + ", :act_code   " // 5
            + ", :crt_user   "
            + ", '1'  " // proc_flag
            + ", :send_times "
//            + ", "
//            + commSqlStr .sysYYmd
//            + ", "
			+ ", to_char(sysdate,'yyyymmdd') "
			+ ", to_char(sysdate,'hh24miss') "
//            + commSqlStr .sysTime
            + ", :proc_user  " // 10
            + ", :data_from, :resp_code" // data_from, resp_code" //11
            + ", 'OPPO'" // data_type"
            + ", :bin_type"
            + ", :reason_code"
            + ", :del_date"
            + ", :bank_acct_no"
            + ", :vmj_regn_data" + ", :vip_amt"  
            + ", :electronic_card_no "
            + ", :current_code "
            + ", :new_end_date "
            + ", to_char(sysdate,'yyyymmdd') "
            + ", :oppost_reason "
            + ", :v_card_no "
            + ", "
            + commSqlStr.sysdate + ",:mod_pgm" + " )";

    outgoSqlParm.setString2("startYYmd", startYYmd);
    outgoSqlParm.setString2("startTime", startTime);
    outgoSqlParm.setString2("card_no", cardNo);
    outgoSqlParm.setString2("key_value", keyValue);
    outgoSqlParm.setString2("key_table", keyTable);
    outgoSqlParm.setString2("bitmap", bitmap);
    outgoSqlParm.setString2("act_code", actCode);
    outgoSqlParm.setString2("crt_user", modUser);
    // ppp(7,_proc_flag);
    outgoSqlParm.setInt2("send_times", sendTimes);
    outgoSqlParm.setString2("proc_user", modUser);
    outgoSqlParm.setString2("data_from", dataFrom);
    outgoSqlParm.setString2("resp_code", respCode);
    
    outgoSqlParm.setString2("electronic_card_no", p11ElectronicCardno);
    outgoSqlParm.setString2("current_code", p9CurrentCode);
    outgoSqlParm.setString2("new_end_date", p8NewEndDate);
    outgoSqlParm.setString2("oppost_reason", p10OppostReason);
    outgoSqlParm.setString2("v_card_no", vCardNo);
    
    outgoSqlParm.setString2("mod_pgm", modPgm);

    outgoSqlParm.nameSqlParm(strSql);
    sqlExec(outgoSqlParm.getConvSQL(), outgoSqlParm.getConvParm());
    if (sqlRowNum <= 0) {
      errmsg("insert cca_outgoing err, kk[%s]", cardNo);
      return;
    }
 
    cardNo = "";
    keyValue = "";
    keyTable = "";
    actCode = "";
  }
  
  void updateCcaOutgoing(){
	  if(actCode.equals("5"))
		  return;
	  String sqlCmd = " update cca_outgoing set send_times = send_times+1, resp_code = ? ,bitmap = ? ,mod_time = sysdate where hex(rowid) = ? ";
		  setString2(1,respCode);
		  setString2(2,bitmap);
		  setString2(3,vCardRowid);
		  sqlExec(sqlCmd);
		  if (sqlRowNum <= 0) {
			  System.out.println("update cca_outgoing err");
		  }
	  isReOutgoing = false;
	  vCardRowid = "";
  }

  void updateHceCard(){
	  if(actCode.equals("5"))
		  return;
	  String sqlCmd = " update hce_card set status_code = '3' "
	  		+ " ,change_date = to_char(sysdate,'yyyymmdd') "
	  		+ " ,mod_time = sysdate "
	  		+ " ,mod_pgm = ? "
	  		+ " where v_card_no = ? ";
	  setString2(1,modPgm);
	  setString2(2,vCardNo);
	  sqlExec(sqlCmd);
	  if (sqlRowNum <= 0) {
		  System.out.println("update hce_card err");
	  }
	  
  }
  
  void updateOempayCard(){
	  if(actCode.equals("5"))
		  return;
	  String sqlCmd = " update oempay_card set status_code = '3' "
	  		+ " ,change_date = to_char(sysdate,'yyyymmdd') "
	  		+ " ,mod_time = sysdate "
	  		+ " ,mod_pgm = ? "
	  		+ " where v_card_no = ? ";
	  setString2(1,modPgm);
	  setString2(2,vCardNo);
	  sqlExec(sqlCmd);
	  if (sqlRowNum <= 0) {
		  System.out.println("update oempay_card err");
	  }
	  
  }
  
  //
  // void select_cca_card_acct() {
  // for(int ii=0; ii<6; ii++)
  // aa_block[ii]="";
  //
  // if (empty(is_p_seqno)) {
  // errmsg("帳戶流水號: 不可空白");
  // return;
  // }
  //
  // this.is_sql
  // ="select block_reason1, block_reason2, block_reason3, block_reason4, block_reason5,"
  // +" spec_code, spec_del_date"
  // +" from cca_card_acct"
  // +" where p_seqno =?";
  // if (ib_debit)
  // is_sql +=" and debit_flag ='Y'";
  // else is_sql +=" and debit_flag<>'Y'";
  //
  // ppp(1,is_p_seqno);
  // sqlSelect(is_sql);
  // if (sql_nrow <=0) {
  // errmsg("帳戶資料不存在, kk[%s]",is_p_seqno);
  // return;
  // }
  // if ( !empty(is_block))
  // return;
  //
  // if (commDate.sysComp(col_ss("spec_del_date"))>0) {
  // is_block =col_ss("spec_code");
  // is_del_date =col_ss("spec_del_date");
  // }
  //
  // aa_block[1] =col_ss("block_reason1");
  // aa_block[2] =col_ss("block_reason2");
  // aa_block[3] =col_ss("block_reason3");
  // aa_block[4] =col_ss("block_reason4");
  // aa_block[5] =col_ss("block_reason5");
  // for(int ii=5; ii>0; ii--) {
  // if ( !empty(aa_block[ii])) {
  // is_block =aa_block[ii];
  // is_del_date ="";
  // }
  // }
  //
  // return;
  // }

  int selectCrdCard() {
    strSql =
        "select" + " card_no," + " current_code," + " bin_type," + " card_type," + " new_end_date"
            + " from crd_card" + " where acno_p_seqno =?";
    daoTid = "card.";
    setString2(1, pseqno);
    sqlSelect(strSql);
    if (sqlRowNum < 0) {
      errmsg("select crd_card error, kk[%s]", pseqno);
      return -1;
    }

    return sqlRowNum;
  }
  
	void outElectronic(String actCode) {
		if (!p9CurrentCode.equals("2")) {
			return;
		}

		// TSCC_CRD
		if (electronicCode.equals("01")&&!empty(p11ElectronicCardno)) {
			if(actCode.equals("1"))
				oppoTscReq(actCode);
		}

		// IPASS
		if (electronicCode.equals("02")&&!empty(p11ElectronicCardno)) {
			if(actCode.equals("1"))
				oppoIpsReq(actCode);
		}

		// ICASH
		if (electronicCode.equals("03")&&!empty(p11ElectronicCardno)) {
			if(actCode.equals("1"))
				oppoIchReq(actCode);
		}

	}
  
  void outTwmp(String actCode){
	  if(isDebit){
		 return; 
	  }
	  if(!iscalltwmp){//由trigger處理
		  return;
	  }

	  vCardNos = "";
	  sqlSelect = "select v_card_no ";
	  sqlSelect += " from HCE_CARD ";
	  sqlSelect += " where 1=1 ";
	  sqlSelect += " and status_code = '0' ";
	  sqlSelect += " and card_no = ? ";
	  setString2(1,p1CardNo);
	  sqlSelect(sqlSelect);
	  if (sqlRowNum > 0) {
		  for(int x = 0 ; x<sqlRowNum ; x++){
			  vCardNos += colStr("v_card_no")+",";
		  }
	  }else
		  return;
	  p4Reason = twmpReason;
	  oppoTwmpReq(actCode);
  }
  
  void outOempay(String actCode){
	  if(isDebit){
		 return; 
	  }
	  if(!iscalltwmp){//由trigger處理
		  return;
	  }

	  vCardNos = "";
	  sqlSelect = "select v_card_no ";
	  sqlSelect += " from OEMPAY_CARD ";
	  sqlSelect += " where 1=1 ";
	  sqlSelect += " and status_code = '0' ";
	  sqlSelect += " and card_no = ? ";
	  setString2(1,p1CardNo);
	  sqlSelect(sqlSelect);
	  if (sqlRowNum > 0) {
		  for(int x = 0 ; x<sqlRowNum ; x++){
			  vCardNos += colStr("v_card_no")+",";
		  }
	  }else
		  return;
	  oppoOempayReq(actCode);
  }

  // void select_cca_spec_code() {
  // if (empty(is_block))
  // return;
  //
  // is_sql ="select"
  // +" neg_reason, visa_reason, mast_reason, jcb_reason, send_ibm"
  // +" from  cca_spec_code"
  // +" where spec_code =?";
  // ppp(1,is_block);
  // sqlSelect(is_sql);
  // if (sql_nrow <=0) {
  // ib_outgoing =false;
  // return;
  // }
  //
  // is_neg_reason =col_ss("neg_reason");
  // is_visa_reason =col_ss("visa_reason");
  // is_mast_reason =col_ss("mast_reason");
  // is_jcb_reason =col_ss("jcb_reason");
  // is_send_ibm =col_nvl("send_ibm","N");
  // is_neg_cap_code ="0";
  //
  // is_sql ="select sys_data2 as neg_cap_code"
  // +" from cca_sys_parm1"
  // +" where sys_id ='NCCC'"
  // +" and sys_key =?" //neg_reason
  // ;
  // ppp(1,is_neg_reason);
  // sqlSelect(is_sql);
  // if (sql_nrow >0) {
  // is_neg_cap_code =col_nvl("neg_cap_code","0");
  // }
  // }

  void xxSelectCcaSpecCode() {
    /*
     * -->SELECT NVL(SPEC_NEG_REASON,' '),NVL(SPEC_VISA_REASON,' '), NVL(SPEC_MAST_REASON,'
     * '),NVL(SPEC_JCB_REASON,' '), NVL(SPEC_AECD_REASON,' '), NVL(SEND_IBM,'N') INTO
     * :DB_NEG_Op4_reason,:DB_VIS_EXCEP_CODE,:DB_MST_AUTH_CODE,:DB_JCB_EXCP_CODE,:DB_AE_EXCP_CODE,
     * :is_send_ibm FROM SPEC_CODE WHERE SPEC_CODE=:wk_block_reason;
     */
  }

  /*
   * dw_4.accepttext() ls_y = 'Y' lm_spec_mst_vip_amt = 0 ls_spec_status = ' ' ls_spec_remark =
   * '(解超)' ls_outgoing = '0' IF (ls_status = 'Y') THEN ls_spec_remark = '凍結(禁超)' IF
   * trim(ls_status_5) <> "" THEN ls_spec_status = ls_status_5 IF trim(ls_status_4) <> "" THEN
   * ls_spec_status = ls_status_4 IF trim(ls_status_3) <> "" THEN ls_spec_status = ls_status_3 IF
   * trim(ls_status_2) <> "" THEN ls_spec_status = ls_status_2 IF trim(ls_status_1) <> "" THEN
   * ls_spec_status = ls_status_1 END IF //--JH:920825-- IF mid(ls_spec_status,1,1) <= 'P' AND
   * mid(ls_spec_status,2,1) = '1' THEN ls_outgoing = '1' SELECT NVL(SPEC_NEG_REASON,'
   * '),NVL(SPEC_VISA_REASON,' '), NVL(SPEC_MAST_REASON,' '),NVL(SPEC_JCB_REASON,' '),
   * NVL(SPEC_AECD_REASON,' '), NVL(SEND_IBM,'N') INTO
   * :DB_NEG_Op4_reason,:DB_VIS_EXCEP_CODE,:DB_MST_AUTH_CODE,:DB_JCB_EXCP_CODE,:DB_AE_EXCP_CODE,
   * :is_send_ibm FROM SPEC_CODE WHERE SPEC_CODE=:ls_spec_status; IF sqlca.sqlcode <> 0 THEN
   * ls_outgoing = '0' ELSE DB_SYSID = "NCCC" SELECT NVL(SYS_DATA2,'0') INTO :DB_NEG_CAP_CODE FROM
   * SYS_PARM1 WHERE SYS_ID=:DB_SYSID AND SYS_KEY=:DB_NEG_Op4_reason; IF sqlca.sqlcode <> 0 THEN
   * DB_NEG_CAP_CODE = "0" END IF //--JH:920825-- END IF Int li_send_AE=0,li_send_IBM=0 OT_KEY_TABLE
   * = "CARD_BASE_SPEC" OT_SUCCESS_FLAG = "0" OT_SEND_TIMES = 0 FOR ll_row = 1 to dw_4.rowcount()
   * ls_card_no = nvl(dw_4.Object.card_no[ll_row],''); ls_flag =
   * nvl(dw_4.Object.spec_flag[ll_row],''); ls_card_status =
   * nvl(dw_4.Object.card_status[ll_row],''); ls_card_type = nvl(dw_4.Object.card_type[ll_row],'');
   * ls_neg_del_date = string(RelativeDate(Date(String(dw_4.Object.eff_date_end[ll_row],
   * "@@@@/@@/@@")), 1), 'yyyymmdd') //ls_spec_status = dw_4.Object.spec_status[ll_row]; is_cardORG
   * = Upper(Left(ls_card_type,1))
   * 
   * //--- card_status != '0' not insert ccas_onbat_interface IF ls_card_status <> '0' THEN Continue
   * 
   * ls_trans_type = '3' ls_to_which = '1' ls_process_status = 0 ls_varo = 'O' ldt_date =
   * datetime(date(string(ls_date,'@@@@/@@/@@')),time(string(ls_time,'@@:@@:@@')))
   * 
   * 
   * //--Insert into OUTGOING table-- IF ls_outgoing <> '1' OR ls_card_status <> '0' THEN Continue
   * 
   * ls_bank_actno='' t_card_acct_class = dw_4.object.card_rule[ll_row]
   * 
   * IF trim(DB_NEG_Op4_reason)<>'' and is_cardORG<>'A' THEN ls_ISOstring='' IF
   * trim(t_card_acct_class) = 'A4' and trim(is_send_ibm) = 'Y' THEN //- DEBIT CARD OT_KEY_VALUE =
   * "IBM" ls_bank_actno = dw_4.object.bank_actno[ll_row] ls_ISOstring = f_ibmnegfile(ls_bank_actno,
   * ls_card_type, DB_NEG_Op4_reason,DB_NEG_Op4_reason,ls_neg_del_date,"1") li_send_IBM++ ELSE //--
   * NCCC OT_KEY_VALUE = "NCCC" ls_ISOstring = f_ftp2neg_id_string(ls_card_no, ls_date, ls_time,
   * ls_card_type, DB_NEG_Op4_reason,DB_NEG_CAP_CODE,ls_neg_del_date,"1") END IF IF
   * trim(ls_ISOstring) <> '' AND Not IsNull(ls_ISOstring) THEN ls_ISOstring = ls_ISOstring + ">" IF
   * wf_outgoing
   * (ls_card_no,ls_bank_actno,"A",OT_KEY_VALUE,OT_KEY_TABLE,OT_SUCCESS_FLAG,OT_SEND_TIMES
   * ,ls_ISOstring) <> 1 THEN dw_3.SetFocus() RETURN FALSE END IF END IF END IF
   * 
   * CHOOSE CASE is_cardORG CASE 'J' OT_KEY_VALUE = "JCB" ls_OutReason = DB_JCB_EXCP_CODE CASE 'M'
   * OT_KEY_VALUE = "MASTER" ls_OutReason = DB_MST_AUTH_CODE CASE 'V' OT_KEY_VALUE = "VISA"
   * ls_OutReason = DB_VIS_EXCEP_CODE CASE 'A' li_send_AE ++ Continue CASE ELSE //--Non VMJA--
   * Continue END CHOOSE
   * 
   * IF trim(ls_OutReason) = "" THEN Continue
   * 
   * IF t_card_acct_class <> 'A4' THEN //--排除 DEBIT CARD IF mid(ls_card_type,1,1)= "J" THEN //JCB卡
   * ls_outgoing_area = "00000" ls_ISOstring =
   * f_ftp2jcb_string(ls_card_no,ls_date,ls_time,ls_neg_del_date,"1",ls_OutReason,ls_outgoing_area)
   * ELSE IF mid(ls_card_type,1,1)= "M" THEN //Master卡 ls_ISOstring =
   * f_ftp2master102_string(ls_card_no, ls_date, ls_time,"1",'0',ls_OutReason) ELSE ls_outgoing_area
   * = "0        " ls_ISOstring =
   * f_ftp2visa_string(ls_card_no,ls_date,ls_time,ls_neg_del_date,"1",ls_OutReason,ls_outgoing_area)
   * END IF END IF END IF
   * 
   * IF trim(ls_ISOstring) <> '' AND Not IsNull(ls_ISOstring) THEN ls_ISOstring = ls_ISOstring + ">"
   * SELECT OUTGO_NO.NEXTVAL INTO :OT_SEQ_NO FROM DUAL; INSERT INTO OUTGOING
   * (SEQ_NO,CARD_NO,KEY_VALUE,KEY_TABLE,BITMAP,ACT_CODE,
   * SUCCESS_FLAG,SEND_TIMES,CREATE_DATE,CREATE_TIME, CREATE_UID,UPDATE_DATE,UPDATE_TIME,UPDATE_UID)
   * VALUES (:OT_SEQ_NO,:ls_card_no,:OT_KEY_VALUE,:OT_KEY_TABLE,
   * :ls_ISOstring,'A',:OT_SUCCESS_FLAG,:OT_SEND_TIMES,
   * :ls_date,:ls_time,:gs_userid,:ls_date,:ls_time,:gs_userid); IF sqlca.sqlcode <> 0 THEN
   * ls_sqlerrtext = SQLCA.sqlerrtext ROLLBACK ; f_show_msg("無法新增NCCC-OutGoing檔 (" + OT_KEY_VALUE +
   * ") 失敗!" + ls_sqlerrtext) dw_3.SetFocus() RETURN FALSE END IF END IF NEXT
   */

}
