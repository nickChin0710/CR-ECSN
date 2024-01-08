/**
 * ISO公用程式 V.2018-1106.jh
   2018-1106   JH    modify
 * 2018-1003:	JH		bug-fix
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format     
* 109-06-05  V1.00.01  Ryan       modify                           *
* 109-12-28  V1.00.02  Justin      zz -> comm
* 110-01-21  V1.00.03  ryan         update tscReq() 
* 110-11-29  V1.00.04  ryan         update oempay() isoField[14]
* 110-12-01  V1.00.05  ryan        update oempay iso        
* 110-12-16  V1.00.06  Ryan         oempayReq 新增  token_requestor_id,account_number_ref 欄位 *     
* 110-12-17  V3.00.07  Ryan         oempayReq account_number_ref --> t_c_identifier *  
* 110-12-29  V3.00.08  Ryan         update visa iso 127*  
* 111/04/06  V1.00.22    Ryan     JCB gate.isoField[120] ==>  不帶空白                           *
* 111/07/05  V1.00.23    Ryan     sysGmtDatetime 調整為24小時制                           *
 * */

package busi.func;

import bank.AuthIntf.AuthGate;
import bank.AuthIntf.FhmFormat;
import bank.AuthIntf.NegFormat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;


public class CcasIsoString {
	boolean ibNewNccc = true;
	taroko.base.CommString commString = new taroko.base.CommString();
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	busi.CommBusi commBusi = new busi.CommBusi();
	String strBankIdN = commBusi.BK_ID_NCCC;
	String strBkIca = commBusi.ccas_BK_ICA;
	AuthGate gate = new AuthGate();
	String masterDate = "";
	String msg = "";
	

	void msgOK() {
		msg = "";
	}

	String fill(char cc, int len) {
		return commString.fill('0', len);
	}

	public String getMsg() {
		return msg;
	}

	public String mesgType() {
		return gate.mesgType;
	}

	public void setMasterDate(String s1) {
		masterDate = commString.nvl(s1);
	}

	public String getIsoString() {
		return gate.isoString.substring(2);
	}

	String getTraceNo() {
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// random = new Random(new Date().getTime());
			throw new RuntimeException("init SecureRandom failed.", e);
		}
		return commString.numFormat(random.nextDouble() * 1000000, "000000");
	}

	public String sysGmtDatetime() {
		 Date currDate = new Date();
//		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddhhmmss");
		 SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmss");
		 form1.setTimeZone(TimeZone.getTimeZone("GMT") );
		 return form1.format(currDate);
	}
	
	String getGmtTime() {
		return sysGmtDatetime().substring(4);
	}
	
	public String getDaysInYear(String ymd) {
		int year = commString.strToInt(commString.mid(ymd, 0,4));
		int month = commString.strToInt(commString.mid(ymd, 4,2));
		int day = commString.strToInt(commString.mid(ymd, 6,2));
        int totalDays = 0;
        int days28 = 28;
        int days29 = 29;
        int days30 = 30;
        int days31 = 31;
        switch (month) {
            case 12:
                totalDays += days30;
            case 11:
                totalDays += days31;
            case 10:
                totalDays += days30;
            case 9:
                totalDays += days31;
            case 8:
                totalDays += days31;
            case 7:
                totalDays += days30;
            case 6:
                totalDays += days31;
            case 5:
                totalDays += days30;
            case 4:
                totalDays += days31;
            case 3:
                if (((year / 4 == 0) && (year / 100 != 0)) || (year / 400 == 0)) {
                    totalDays += days29;
                } else {
                    totalDays += days28;
                }
            case 2:
                totalDays += days31;
            case 1: 
                totalDays += day;
        }
        return String.format("%03d",totalDays);
    }

	public int ibmNegfile(String aBankAcctno, String aCardType, String aNegReason, String aNegCap,
			String aDelDate, String aFileCode) {
//
//		gate.mesgType = "0300";
//		// IF a_file_code = '3' OR a_file_code = '5' THEN //Inq, Delete
//		// lstr_neg_outgo.bytemap =
//		// '11000010001000000000000000000000000000000000000110000000000100000000000000000000000000000010000000001000000000000000000000000000'
//		// ELSE //Add, Update
//		// lstr_neg_outgo.bytemap =
//		// '11000010001000000000000000000000000000000000000110000000000100000000000000000000000000000010000000001000000000000000000100000000'
//		// END IF
//		gate.isoField[2] = a_bank_acctno; // bit2_acct_no金融卡
//		gate.isoField[7] = getGmtTime(); // bit7_date_time
//		gate.isoField[11] = getTraceNo(); // bit11_trace_no
//		gate.isoField[48] = "000" // bit48_add_data
//				+ strBankIdN + "41" + commString.fill('0', 67);
//		gate.isoField[49] = "901"; // bit49_currence
//		gate.isoField[60] = "90" + commString.mid(strBankIdN, 2, 2); // bit60_pos_info
//		if (ibNewNccc) {
//			gate.isoField[60] += "0000PRO200000000000000YY000000000000000000000000000000";
//		} else {
//			gate.isoField[60] += "0000PRO100000000000000YY000000000000000000000000000000";
//		}
//		if (a_file_code.equals("5")) {
//			gate.isoField[73] = commString.fill('0', 6); // bit73_act_date
//		}
//		gate.isoField[91] = a_file_code; // bit91_file_code
//		gate.isoField[101] = "NF"; // bit101_file_name
//		gate.isoField[120] = commString.fill('0', 111); // bit120_mess_data
//		if (commString.pos(",1,2", a_file_code) > 0) {
//			a_neg_cap = commString.nvl(a_neg_cap, "0");
//			gate.isoField[120] = commString.rpad(a_card_type, 2) // bit120_mess_data
//					+ commString.lpad(a_neg_reason, 2, "0") + a_neg_cap + commDate.sysDate().substring(2)
//					+ commString.mid(a_del_date, 2, 4);
//		}
//
//		FhmFormat bic = new FhmFormat(null, gate, null);
//		if (bic.host2Iso()) {
//			return 1;
//		}
		return -1;
//		// return
//		// "|acctno="+a_bank_acctno+"|card-type="+a_card_type+"|neg="+a_neg_reason+"|cap="+a_neg_cap+"|del-date="+a_del_date+"|file-code="+a_file_code;
	}


	public int negId(String aCardNo, String aBinType, String aNegReason,  String aDelDate,
			String aFileCode , String aVipAmt , String region) {
		String mcc = "";
		isoFieldClear();
		gate.mesgType = "0300";
		gate.isoField[2] = "2";
		gate.isoField[3] = aFileCode;
		gate.isoField[4] = aCardNo;
		gate.isoField[5]  = "";
		gate.isoField[6] = aNegReason;
		if(aBinType.equals("M")){
			mcc = "2";
		}
		gate.isoField[7] = mcc;
		gate.isoField[8] = "";
		gate.isoField[9] = aVipAmt;
		gate.isoField[10] = commString.mid2(aDelDate,2,4);
		gate.isoField[11] = "";
		gate.isoField[12] = "";
		gate.isoField[13] = "";
		gate.isoField[14] = getTraceNo();
		gate.isoField[15] = "";

		NegFormat bic = new NegFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}
		msg = "Neg_id.iso轉換失敗; 請call資訊人員";
		return 1;
		
	}
	
	public int fiscReq(String aCardNo, String aBinType, String aNegReason, String aDelDate,
			String aFileCode, String aVipAmt, String region) {
		String mcc = "";
		isoFieldClear();
		gate.mesgType = "0300";
		gate.isoField[2] = "1";
		gate.isoField[3] = aFileCode;
		gate.isoField[4] = aCardNo;
		gate.isoField[5]  = "";
		gate.isoField[6] = aNegReason;
		if(aBinType.equals("M")){
			mcc = "2";
		}
		gate.isoField[7] = mcc;
		gate.isoField[8] = "";
		gate.isoField[9] = aVipAmt;
		gate.isoField[10] = commString.mid2(aDelDate,2,4);
		gate.isoField[11] = "";
		gate.isoField[12] = "";
		gate.isoField[13] = "";
		gate.isoField[14] = getTraceNo();
		gate.isoField[15] = "";

		NegFormat bic = new NegFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}
		msg = "FISC_req.iso轉換失敗; 請call資訊人員";
		return 1;
		
	}
	
	  
	public int twmpReq(String aCardNo, String aFileCode,String aReasonCode , String aCurrentCode) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[91] = aFileCode;
		gate.isoField[101] = "FTD001";

		// 風險卡：R ,暫停使用：U ,註銷：Q ,強制停卡：C ,偽卡：F ,遺失：L ,遭竊：S
		// +發卡行代號
		String ss = "U";
		switch (aCurrentCode) {
		case "0":
			ss = " ";
			break;
		case "1":
			ss = "Q";
			break;
		case "2":
			if(commString.pos(",L,41,01",aReasonCode)>0)ss = "L";
			if(commString.pos(",S,43,02",aReasonCode)>0)ss = "S";
			break;
		case "3":
			ss = "C";
			break;
		case "4":
			ss = "Q";
			break;
		case "5":
			ss = "F";
			break;
		}
		gate.isoField[120] = ss + "006";
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "TWMP_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}
	
	public int oempayReq(String aCardNo, String aFileCode,String aReasonCode ,String binType,String newEndDate ,String oempayCardNo,String currentCode,String tokenRequestorId,String tCIdentifier) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[12] = commDate.sysTime();
		gate.isoField[13] = commString.right(commDate.sysDate(),4);
		gate.isoField[14] = commString.mid(newEndDate,2,4);
		gate.isoField[15] = commString.right(commDate.sysDate(),4);
		gate.isoField[91] = aFileCode;
		if(binType.equals("V")){
			gate.isoField[2] = oempayCardNo;
			gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
			gate.isoField[58] = "7004"+aReasonCode;
//			gate.isoField[92] = "*A";	
			gate.isoField[101] = "TK";
			try {
				gate.isoField[124] = "0300410110"+toHexString(oempayCardNo)+"030B"+toHexString(commString.rpad(tokenRequestorId,11," "))+"0520"+toHexString(commString.rpad(tCIdentifier, 32," "));
			} catch (Exception e) {
				gate.isoField[124] = "";
			}
			gate.isoField[127] = "";
		}
		if(binType.equals("M")){
			gate.isoField[2] = aCardNo;
			gate.isoField[33] = "003741";
			gate.isoField[101] = "MCC106";

			String statusCode ="S";
			if(currentCode.equals("0")){
				statusCode = "C";
			}
			if(commString.pos(",1,2,3,4,5", currentCode)>0){
				statusCode = "D";
			}
			gate.isoField[120] = "M"+statusCode+"0"+oempayCardNo;
		}
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "OEMPAY_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	public int masterReq(String aCardNo, String aCardType, String aFileCode, String aReasonCode,
			String[] aaRegnDate) {

		String lsCardType3 = "";
		if (aCardType.length() == 1) {
			// MASTER普卡
			lsCardType3 = aCardType + "CC";
		} else {
			lsCardType3 = commString.mid(aCardType, 0, 1) + "C" + commString.mid(aCardType, 2, 1);
		}
		isoFieldClear();
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[33] = "003741";
		gate.isoField[73] = commString.right(masterDate, 6);
		gate.isoField[91] = aFileCode;
		gate.isoField[101] = "MCC103";

		String ss = aCardNo + commString.space(3) + "00" + strBkIca;
		if (commString.eqAny(aFileCode, "1")||commString.eqAny(aFileCode, "2")) {
			String lsSortDate = sortMasterRegn(aaRegnDate, "A");
			ss += lsCardType3 + "04" + aReasonCode + commString.space(24) + lsSortDate;
		}
		gate.isoField[120] = ss;
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Master_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	public int masterReq2(String aCardNo, String aFileCode, String aVipAmt, String aReasonCode) {
		// Function:OutGoing to master[Request] (MCC102)
		// Parm: string a_card_no, string a_date, string a_time, string
		// a_file_code, string a_vip_amt,
		// string a_reason_code);
		// arg: a_tx_code 1->新增,修改 3->刪除, 5->查詢
		// =========================================================================
		isoFieldClear();
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[33] = "003741";
		gate.isoField[73] = commString.right(masterDate, 6);
		gate.isoField[91] = aFileCode;
		gate.isoField[101] = "MCC102";
		String ls120 = "";

		ls120 = aCardNo + commString.space(3);
		if (commString.eqAny(aFileCode, "1")||commString.eqAny(aFileCode, "2")) {
			ls120 += aReasonCode + commString.fill('0', 6) + commString.fill('0', 4) + "00";
			if (commString.eqAny(aReasonCode, "V")) {
				ls120 += commString.numFormat(commString.strToNum(aVipAmt), "000000000000") + "840";
			} else {
				ls120 += commString.fill('0', 12) + commString.space(3);
			}
		}
		gate.isoField[120] = ls120;
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Master_req2.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	public int jcbReq(String aCardNo, String aOppoDate, String aFileCode, String aReasonCode, String aRegion) {
		// arg: a_tx_code 1->BA.新增, 2->BC.更新, 3->BD.刪除, 4->BR.查詢
		// Arg list:card_no, opp_date, file_code, visa_reason, visa_region(7)
		// =========================================================================
		isoFieldClear();
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[32] = "88546000";
		gate.isoField[33] = "88546000";
		//gate.isoField[91] = aFileCode;
		gate.isoField[101] = "6332";
		if (commString.pos(",1,2", aFileCode) > 0) {
			// JCB Except File 1-Add 2-Update (Format 1)
			gate.isoField[120] = aFileCode + aCardNo + aReasonCode
					+ commString.mid(aOppoDate, 0, 6) + aRegion;
		} else {
			// JCB Except File 0-Delete 5-Inquiry (Format 3)
//			gate.isoField[120] = aFileCode + aCardNo + commString.space(13);
			gate.isoField[120] = aFileCode + aCardNo;
		}
		gate.isoField[127] = "356713";

		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Jcb_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	public int visaReq(String aCardNo, String aOppoDate, String aFileCode, String aReason, String aRegion) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		if (aFileCode.equals("1")||aFileCode.equals("2")) {
			gate.isoField[73] = commString.right(aOppoDate , 6);
		}
		gate.isoField[91] = aFileCode;
		gate.isoField[92] = "";
		
		//E2 / TK / PAN 
		gate.isoField[101] = "E2";
		
		if(aFileCode.equals("3")){
			gate.isoField[127] = "";
		}else{
//			gate.isoField[127] = aReason+aRegion+commString.space(8);
			gate.isoField[127] = aReason+aRegion;
		}
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Visa_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}
  
	public int tscReq(String aCardNo,String aFileCode,String newEndDate,String aOppoDate,boolean isDebit) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();

		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[73] = commString.left(aOppoDate, 6);
		gate.isoField[91] = aFileCode;

		gate.isoField[101] = "FSD001";
		String pCode = "";
		if(isDebit) {
			pCode = "891399";
			if(aFileCode.equals("3")){
				pCode = "891899";
			}
		}else {
			pCode = "890399";
			if(aFileCode.equals("3")){
				pCode = "890899";
			}
		}
		if(aFileCode.equals("5")){
			pCode = commString.space(6);
		}
		//掛卡代號 掛卡：890399,取消掛卡:890899,連線拒授權名單:890999,卡片效期:YYMM
		gate.isoField[120] = pCode+ commString.mid2(newEndDate,2,4);
		
		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Tsc_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	public int ipsReq(String aCardNo,  String aFileCode) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();
		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		gate.isoField[91] = aFileCode;
		gate.isoField[101] = "FBD001";

		//掛卡代號 掛卡：910000
		String ss = "910000";
		if(aFileCode.equals("3")||aFileCode.equals("5")){
			ss = commString.space(6);
		}
		gate.isoField[120] = ss;

		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Ips_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}
  
	public int ichReq(String aCardNo, String aFileCode,String newEndDate ) {
		isoFieldClear();
		String Y37 = commString.mid(sysGmtDatetime(), 3,1);
		String DDD37 = getDaysInYear(commString.mid(sysGmtDatetime(), 0,8));
		String hh37=commString.mid(sysGmtDatetime(), 8,2);
		String isoField120 = commString.space(6);
		gate.mesgType = "0302";
		gate.isoField[2] = aCardNo;
		gate.isoField[7] = getGmtTime();
		gate.isoField[11] = getTraceNo();

		gate.isoField[37] = Y37 + DDD37 + hh37 + getTraceNo();
		
		gate.isoField[91] = aFileCode;
		gate.isoField[101] = "FID001";
		if(!aFileCode.equals("5")&&!aFileCode.equals("3"))
			isoField120 = "990176";
		//掛卡代號連線掛失：990176,卡片效期 YYMM
		gate.isoField[120] = isoField120+ commString.mid2(newEndDate,2,4);

		FhmFormat bic = new FhmFormat(null, gate, null);
		if (bic.host2Iso()) {
			return 1;
		}

		msg = "Ich_req.iso轉換失敗; 請call資訊人員";
		return -1;
	}

	
	
//  public int master102(String a_card_no, String a_file_code, double a_amt, String a_reason) {
//    /*
//     * ref. function string f_ftp2master102_string (string a_card_no, string a_date, string a_time,
//     * string a_file_code, string a_vip_amt, string a_reason_code)
//     */
//    if (ibNewNccc) {
//      gate.bicHead = "ISO086000051";
//    } else
//      gate.bicHead = "ISO085000051";
//    gate.mesgType = "0302";
//    // lstr_mst_outgo.bytemap =
//    // '11000010001000000000000000000000000000000000000110000000000100000000000000000000000000000010000000001000000000000000000100000000'
//    gate.isoField[2] = a_card_no; // bit2_acct_no
//    gate.isoField[7] = getGmtTime(); // .bit7_date_time
//    gate.isoField[11] = getTraceNo(); // bit11_trace_no
//    gate.isoField[73] = commString.mid(masterDate, 2, 6); // bit73_act_date
//    gate.isoField[91] = a_file_code; // bit91_file_code, 1-Add 3-Delete 5-Inquire(MCC102)
//    gate.isoField[92] = "";
//    gate.isoField[101] = "MC"; // bit101_file_name
//    String ls_120 = "PATHMCC" + a_file_code + "MCC102" + commString.space(9);
//    ls_120 += commString.fill('0', 5) + a_card_no + commString.space(3);
//    if (a_file_code.equals("1")) {
//      ls_120 += a_reason + commString.fill('0', 6) + commString.fill('0', 4) + "00";
//      if (a_reason.equalsIgnoreCase("V"))
//        ls_120 += commString.numFormat(a_amt, "000000000000") + "840";
//      else
//        ls_120 += commString.fill('0', 12) + commString.space(3);
//    }
//    gate.isoField[120] = ls_120; // bit120_mess_data
//
//    FhmFormat bic = new FhmFormat(null, gate, null);
//    if (bic.host2Iso()) {
//      return 1;
//    }
//
//    msg = "Master_102.iso轉換失敗; 請call資訊人員";
//    return -1;
//    // return "|cardNo="+a_card_no+"|aud=,"+a_file_code+"|amt="+a_amt+"|reason="+a_reason;
//  }

String sortMasterRegn(String[] aRegnDate, String aSort) {
    String lsRtn = "";
    if (aRegnDate == null) {
      return "";
    }
    for (int ii = 0; ii < aRegnDate.length; ii++) {
      if (aRegnDate[ii] == null)
        aRegnDate[ii] = "";
    }

    if (aSort.equalsIgnoreCase("A"))
      Arrays.sort(aRegnDate);
    else
      Arrays.sort(aRegnDate, Collections.reverseOrder());

    for (int ii = 0; ii < aRegnDate.length; ii++) {
      if (commString.empty(aRegnDate[ii]))
        continue;
      lsRtn += aRegnDate[ii];
    }

    return lsRtn;
  }

  void isoFieldClear(){
	  for(int x = 0 ; x<gate.isoField.length;x++){
		  gate.isoField[x] = "";
	  }
  }
  
	public static String toHexString(String inputStr) throws Exception {
		String str = null;
		byte[] byteArray = inputStr.getBytes("Cp1047");
		if (byteArray != null && byteArray.length > 0) {
			StringBuffer stringBuffer = new StringBuffer(byteArray.length);
			for (byte byteChar : byteArray) {
				stringBuffer.append(String.format("%02X", byteChar));
			}
			str = stringBuffer.toString();
		}
		return str;
	}
}
