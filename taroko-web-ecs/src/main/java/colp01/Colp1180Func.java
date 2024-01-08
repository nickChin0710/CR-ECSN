/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00    Zhanghuheng     updated for project coding standard *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package colp01;

import busi.FuncProc;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Colp1180Func extends FuncProc {
	String idNo, caseLetter;
	String kkIdPSeqno;

	public Colp1180Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	@Override
	public int querySelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dataCheck() {
		idNo = varsStr("id_no");
		caseLetter = varsStr("case_letter");
		kkIdPSeqno = selectCrdIdno(idNo);

		// -other modify-
//		sql_where = "where holder_id = ? "
		sqlWhere = "where holder_id_p_seqno = ? " + "and case_letter = ? ";
//		Object[] param = new Object[] { kk1,kk2, var_modseqno() };  //可能會原本沒資料
//		if (other_modify("col_liad_install", sql_where, param)) {
//			return;
//		}

		return;
	}

	String selectCrdIdno(String asIdNo) {
		String outIdPSeqno = "";
		String lsSql = "select id_p_seqno from crd_idno where id_no = ? ";
		Object[] param = new Object[] { asIdNo };
		sqlSelect(lsSql, param);

		if (sqlRowNum > 0) {
			outIdPSeqno = colStr("id_p_seqno");
		}

		return outIdPSeqno;
	}

	@Override
	public int dataProc() {
		dataCheck();

		rc = deleteFunc();
		if (!varsStr("aud_code").equals("D")) {
			rc = insertFunc(); // only DETL, no insert MAST
		}

		return rc;
	}

	int insertFunc() {
		if (rc != 1)
			return rc;

		try {
			String[] aaInstSeq = wp.itemBuff("inst_seq");
			String[] aaInstDateS = wp.itemBuff("inst_date_s");
			String[] aaInstDateE = wp.itemBuff("inst_date_e");
			String[] aaArPerAmt = wp.itemBuff("ar_per_amt");
			String[] aaActPerAmt = wp.itemBuff("act_per_amt");
			String[] aaPayDate = wp.itemBuff("pay_date");
			String[] aaArTotAmt = wp.itemBuff("ar_tot_amt");
			String[] aaActTotAmt = wp.itemBuff("act_tot_amt");
			String[] aaUnpayAmt = wp.itemBuff("unpay_amt");
			String[] aaPaymentDay = wp.itemBuff("payment_day");
			String[] aaFromType = wp.itemBuff("from_type");

//			wp.listCount[0] = aa_inst_seq.length;	//do not delete this line
			int rowcntaa = 0;
			if (!(aaInstSeq == null) && !empty(aaInstSeq[0]))
				rowcntaa = aaInstSeq.length;
			wp.listCount[0] = rowcntaa;

			// -insert-
			for (int ll = 0; ll < rowcntaa; ll++) {
				busi.SqlPrepare sp = new SqlPrepare();
				sp.sql2Insert("col_liad_install");
				sp.ppstr("holder_id_p_seqno", kkIdPSeqno);
				sp.ppstr("holder_id", idNo);
				sp.ppstr("case_letter", caseLetter);
				//
				sp.ppstr("inst_seq", numToStr(strToNum(aaInstSeq[ll]), "###0"));
				sp.ppstr("inst_date_s", aaInstDateS[ll]);
				sp.ppstr("inst_date_e", aaInstDateE[ll]);
				sp.ppstr("ar_per_amt", numToStr(strToNum(aaArPerAmt[ll]), "###0"));
				sp.ppstr("act_per_amt", numToStr(strToNum(aaActPerAmt[ll]), "###0"));
				sp.ppstr("pay_date", aaPayDate[ll]);
				sp.ppstr("ar_tot_amt", numToStr(strToNum(aaArTotAmt[ll]), "###0"));
				sp.ppstr("act_tot_amt", numToStr(strToNum(aaActTotAmt[ll]), "###0"));
				sp.ppstr("unpay_amt", numToStr(strToNum(aaUnpayAmt[ll]), "###0"));
				sp.ppstr("payment_day", aaPaymentDay[ll]);
				sp.ppstr("from_type", aaFromType[ll]);
				sp.ppstr("mod_user", wp.loginUser);
				sp.addsql(", mod_time ", ", sysdate ");
				sp.ppstr("mod_pgm", wp.itemStr("mod_pgm"));
				rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
				if (sqlRowNum == 0) {
					rc = -1;
					break;
				}
			}
		} catch (Exception ex) {
		}

		return rc;
	}

	int deleteFunc() {
		if (rc != 1)
			return rc;

		strSql = "delete col_liad_install " + sqlWhere;
//		Object[] param = new Object[] { kk1,kk2 };
		Object[] param = new Object[] { kkIdPSeqno, caseLetter };
		rc = sqlExec(strSql, param);

		return rc;
	}

	public int deleteColLiadModTmp() {
		strSql = "delete col_liad_mod_tmp where data_type ='INST-DETL' "
				+ "and data_key like rpad(?, 10, ' ')||rpad(?, 10, ' ')||'%' ";
		Object[] param = new Object[] { varsStr("id_no"), varsStr("case_letter") };
		rc = sqlExec(strSql, param);
		// Detail可能沒資料
//		if (sql_nrow == 0) {
//			rc = 0;
//			return rc;
//		}

		strSql = "delete col_liad_mod_tmp where data_type ='INST-MAST' "
				+ "and data_key = rpad(?, 10, ' ')||rpad(?, 10, ' ') ";
		rc = sqlExec(strSql, param);
		if (sqlRowNum == 0) {
			rc = 0;
		}

		return rc;
	}

}
