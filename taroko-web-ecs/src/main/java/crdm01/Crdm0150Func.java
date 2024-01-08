/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111-09-26  V1.00.01  Ryan       Initial                                  *
* 112-05-30  V1.00.02  Ryan       增加貴賓卡查詢掛號號碼維護、barcode_num維護                        *
***************************************************************************/
package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Crdm0150Func extends FuncEdit {

	String batchno = "";
	String recno = "";
	String tableName = "";

	public Crdm0150Func(TarokoCommon wr) {
		wp = wr;
		this.conn = wp.getConn();
	}

	// ************************************************************************
	@Override
	public int querySelect() {
		// TODO Auto-generated method
		return 0;
	}

	// ************************************************************************
	@Override
	public int dataSelect() {
		// TODO Auto-generated method stub
		return 1;
	}

	// ************************************************************************
	@Override
	public void dataCheck() {

		batchno = wp.itemStr("batchno");
		recno = wp.itemStr("recno");
		tableName = wp.itemStr("table_name");
		
		if(wp.itemLen("mail_no")<6) {
			errmsg("掛號號碼長度須為六碼");
			return;
		}
		// -other modify-
		sqlWhere = " where batchno = ? and recno = ? ";
		Object[] param = new Object[] { batchno, recno };
		if (this.isOtherModify(tableName, sqlWhere, param)) {
			errmsg("製卡檔資料不存在，請重新查詢 !");
			return;
		}
		
	}

	// ************************************************************************
	@Override
	public int dbInsert() {
		actionInit("A");
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbUpdate() {
		actionInit("U");
		dataCheck();
		if (rc != 1)
			return rc;
		
		if(tableName.equals("CRD_EMBOSS")) {
			if(updateCrdEmboss()!=1) {
				errmsg("updateCrdEmboss erroer ");
				return rc;
			}
				
			if(updateCrdCard()!=1) {
				errmsg("updateCrdCard erroer ");
				return rc;
			}	
		}
		
		if(tableName.equals("DBC_EMBOSS")) {
			if(updateDbcEmboss()!=1) {
				errmsg("updateDbcEmboss erroer ");
				return rc;
			}
			if(updateDbcCard()!=1) {
				errmsg("updateDbcCard erroer ");
				return rc;
			}
		}
		
		if(tableName.equals("CRD_EMBOSS_PP")) {
			if(updateCrdEmbossPp()!=1) {
				errmsg("updateCrdEmbossPp erroer ");
				return rc;
			}
				
			if(updateCrdCardPp()!=1) {
				errmsg("updateCrdCardPp erroer ");
				return rc;
			}	
		}
		
		return rc;
	}

	// ************************************************************************
	@Override
	public int dbDelete() {
		actionInit("D");
		return rc;
	}
	// ************************************************************************

	int dbUpdate2() {
		String aaCardType = varsStr("aa_card_type");
		String[] aaCardTypes = aaCardType.split(",");
		int x = 0;

		while (x < aaCardTypes.length) {
			if (aaCardTypes[x].trim().equals("信用卡")) {

				if (updateCrdEmboss() != 1) {
					errmsg("updateCrdEmboss erroer ");
					return rc;
				}
				if (updateCrdCard() != 1) {
					errmsg("updateCrdCard erroer ");
					return rc;
				}
			}

			if (aaCardTypes[x].trim().equals("VD卡")) {
				if (updateDbcEmboss() != 1) {
					errmsg("updateDbcEmboss erroer");
					return rc;
				}
				if (updateDbcCard() != 1) {
					errmsg("updateDbcCard erroer ");
					return rc;
				}
			}

			if (aaCardTypes[x].trim().equals("貴賓卡")) {
				if (updateCrdEmbossPp() != 1) {
					errmsg("updateCrdEmbossPp erroer");
					return rc;
				}
				if (updateCrdCardPp() != 1) {
					errmsg("updateCrdCardPp erroer ");
					return rc;
				}
			}
			x++;
		}
		return rc;
	}
	
	int updateCrdEmboss() {
		Object[] param = null;
		strSql = "update crd_emboss set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";
		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and to_nccc_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and to_nccc_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",batchno, recno };
			rc = sqlExec(strSql + sqlWhere, param);
		}
		return rc;
	}
	
	int updateCrdCard() {
		Object[] param = null;
		strSql = "update crd_card set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";
		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and issue_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and issue_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			sqlWhere = " where CARD_NO = ? and mail_type = '4' ";
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",wp.itemStr("card_no") };
			rc = sqlExec(strSql + sqlWhere, param);
		}
		return rc;
	}
	
	int updateDbcEmboss() {
		Object[] param = null;
		strSql = "update dbc_emboss set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";

		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and to_nccc_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and to_nccc_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",batchno, recno };
			rc = sqlExec(strSql + sqlWhere, param);
		}
	
		return rc;
	}
	
	int updateDbcCard() {
		Object[] param = null;
		strSql = "update dbc_card set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";

		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and issue_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and issue_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			sqlWhere = " where CARD_NO = ? and mail_type = '4' ";
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",wp.itemStr("card_no") };
			rc = sqlExec(strSql + sqlWhere, param);
		}
		return rc;
	}
	
	int updateCrdCardPp() {
		Object[] param = null;
		strSql = "update crd_card_pp set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";
		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and issue_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and issue_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and pp_card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			sqlWhere = " where PP_CARD_NO = ? and mail_type = '4' ";
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",wp.itemStr("card_no") };
			rc = sqlExec(strSql + sqlWhere, param);
		}
		return rc;
	}
	
	int updateCrdEmbossPp() {
		Object[] param = null;
		strSql = "update crd_emboss_pp set " + "MAIL_PROC_DATE = to_char(sysdate,'yyyymmdd'), " + "MAIL_NO = ?, " + "MOD_USER = ?, "
				+ "MOD_TIME = sysdate, " + "MOD_PGM = ?, " + " barcode_num = ? ";
		if (wp.respHtml.indexOf("_add") > 0) {
			sqlWhere = " where mail_type = '4' and mail_branch = ? ";
			int i = 1;
			setString(i++,varsStr("aa_mail_no"));
			setString(i++,wp.loginUser);
			setString(i++,wp.itemStr("mod_pgm"));
			setString(i++,varsStr("aa_mail_no") + "10056158");
			setString(i++,varsStr("aa_mail_branch"));
			if(!empty(varsStr("ex_to_nccc_date1"))) {
				sqlWhere += " and to_vendor_date >= ? ";
				setString(i++,varsStr("ex_to_nccc_date1"));
			}
			if(!empty(varsStr("ex_to_nccc_date2"))) {
				sqlWhere += " and to_vendor_date <= ? ";
				setString(i++,varsStr("ex_to_nccc_date2"));
			}
			if(!empty(varsStr("ex_card_no"))) {
				sqlWhere += " and pp_card_no = ? ";
				setString(i++,varsStr("ex_card_no"));
			}
			if(!empty(varsStr("ex_mail_no"))) {
				sqlWhere += " and mail_no = ? ";
				setString(i++,varsStr("ex_mail_no"));
			}
			rc = sqlExec(strSql + sqlWhere);
		}else{
			param = new Object[] { wp.itemStr("mail_no"), wp.loginUser,wp.itemStr("mod_pgm"),wp.itemStr("mail_no")+"10056158",batchno, recno };
			rc = sqlExec(strSql + sqlWhere, param);
		}
		return rc;
	}
	
	int dbUpdateCrdMailnoRange() {
		Object[] param = null;
		strSql = "UPDATE CRD_MAILNO_RANGE SET USED_MAX_MAIL_NO = ? WHERE MAIL_TYPE = '4' AND INUSE_FLAG = 'Y' ";
		param = new Object[] { varsStr("aa_mail_no") };

		rc = sqlExec(strSql, param);
		return rc;
	}
	
} // End of class
