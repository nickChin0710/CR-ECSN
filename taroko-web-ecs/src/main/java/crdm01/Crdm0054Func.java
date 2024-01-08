/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-03-01  V1.00.00  Ryan       program initial                            *
******************************************************************************/

package crdm01;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import busi.FuncEdit;
import busi.ecs.CommBusiCrd;
import busi.ecs.CommFunction;
import taroko.com.TarokoCommon;

public class Crdm0054Func extends FuncEdit {
  CommFunction comm = new CommFunction();
  CommBusiCrd comc = new CommBusiCrd();

  int debug = 1;
  int tempInt = 0;


  String introduceEmpNo = "";
  String introduceId = "";
  String service = "";

  public Crdm0054Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
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
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from crd_emap_tmp where batchno=? and recno = ?";
      Object[] param = new Object[] {wp.itemStr("batchno"), wp.itemStr("recno")};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }

      // 推廣人員ID
      introduceEmpNo = wp.itemStr("introduce_emp_no");
      if (introduceEmpNo.length() == 6) {
        strSql = "select id,employ_no,unit_no from crd_employee where right(acct_no, 6) = :acct_no";
        setString("acct_no", introduceEmpNo);
        sqlSelect(strSql);
        if (sqlRowNum > 0) {
          introduceId = colStr("id");
          introduceEmpNo = "";
          colSet("emap_id",colStr("id"));
          colSet("emap_employ_no",colStr("employ_no"));
          colSet("emap_unit_no",colStr("unit_no"));
        }
      }
    } else {
      // -other modify-
      sqlWhere = " where batchno=? and recno = ?";
      Object[] param = new Object[] {wp.itemStr("batchno"), wp.itemStr("recno")};
      isOtherModify("crd_emap_tmp", sqlWhere, param);

      // 推廣人員ID
      introduceEmpNo = wp.itemStr("introduce_emp_no");
      if (introduceEmpNo.length() == 6) {
    	strSql = "select id,employ_no,unit_no from crd_employee where right(acct_no, 6) = :acct_no";
        setString("acct_no", introduceEmpNo);
        sqlSelect(strSql);
        if (sqlRowNum > 0) {
          introduceId = colStr("id");
          introduceEmpNo = "";
          colSet("emap_id",colStr("id"));
          colSet("emap_employ_no",colStr("employ_no"));
          colSet("emap_unit_no",colStr("unit_no"));
        }
      } else {
        strSql = "select id from crd_employee where id = :id";
        setString("id", introduceEmpNo);
        sqlSelect(strSql);
        if (sqlRowNum > 0) {
          introduceId = colStr("id");
          introduceEmpNo = "";
        }
      }
    }

    if(!this.isDelete()) {
    	String residentAddr3 = wp.itemStr("resident_addr3");
    	String residentAddr4 = wp.itemStr("resident_addr4");
       	String residentAddr5 = wp.itemStr("resident_addr5");
    	boolean resultCheck1 = checkText(residentAddr3 + residentAddr4 + residentAddr5);
    	if(resultCheck1 == false) {
    	    errmsg("戶籍地址只能輸入全形");
    	    return;
    	}
    	String mailAddr3 = wp.itemStr("mail_addr3");
    	String mailAddr4 = wp.itemStr("mail_addr4");
       	String mailAddr5 = wp.itemStr("mail_addr5");
    	boolean resultCheck2 = checkText(mailAddr3 + mailAddr4 + mailAddr5);
    	if(resultCheck2 == false) {
    	    errmsg("居住地址只能輸入全形");
    	    return;
    	}
    	String companyAddr3 = wp.itemStr("company_addr3");
    	String companyAddr4 = wp.itemStr("company_addr4");
       	String companyAddr5 = wp.itemStr("company_addr5");
    	boolean resultCheck3 = checkText(companyAddr3 + companyAddr4 + companyAddr5);
    	if(resultCheck3 == false) {
    	    errmsg("公司地址只能輸入全形");
    	    return;
    	}
  
    }
    
    // 身分證、配偶ID
    Boolean error = false;
    String applyId = wp.itemStr("apply_id");
    String spouseIdNo = wp.itemStr("spouse_id_no");
    error = comc.checkId(applyId);
    if (error) {
      errmsg("輸入的身分證字號邏輯有誤");
      rc = -1;
    }
    if (spouseIdNo.length() > 0) {
      error = comc.checkId(spouseIdNo);
      if (error) {
        errmsg("輸入的配偶ID邏輯有誤");
        rc = -1;
      }
    }

    // 年資
    String serviceYear = wp.itemStr("service_year");
    String serviceMonth = wp.itemStr("service_month");
    if (serviceYear.length() < 2) {
      serviceYear = "0" + serviceYear;
    }
    if (serviceMonth.length() < 2) {
      serviceMonth = "0" + serviceMonth;
    }
    service = serviceYear + serviceMonth;
    
    // 帳單期別 Justin 2020/12/03
//    String stmtCycle = getStmtCycle(wp.itemStr("group_code"), wp.itemStr("card_type"), wp.itemStr("corp_no"));
//    wp.itemSet("stmt_cycle", stmtCycle);
//    wp.colSet("stmt_cycle", stmtCycle);
    
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
    CrdEmap2 crdEmap = getCrdEmap(wp.itemStr("unit_code"), wp.itemStr("group_code"), wp.itemStr("revolve_int_rate_year"));
    if (rc != 1 || crdEmap == null) return rc;
    
    StringBuffer sb = new StringBuffer();
    
    sb.append("insert into crd_emap_tmp (")
    .append("  pm_id ")
    .append(", batchno ")
    .append(", recno")
    .append(", apply_no")
    .append(", group_code")
    .append(", card_type")
    .append(", unit_code")
    .append(", online_mark")
    .append(", stmt_cycle")
    .append(", apply_id")
    .append(", sex")
    .append(", birthday")
    .append(", chi_name")
    .append(", eng_name")
    .append(", nation")
    .append(", other_cntry_code")
    .append(", resident_no_expire_date")
    .append(", passport_no")
    .append(", passport_date")
    .append(", ur_flag")
    .append(", marriage")
    .append(", education")
    .append(", business_code")
    .append(", graduation_elementarty")
    .append(", cellar_phone")
    .append(", e_mail_addr")
    .append(", home_area_code1")
    .append(", home_tel_no1")
    .append(", home_tel_ext1")
    .append(", office_area_code1")
    .append(", office_tel_no1")
    .append(", office_tel_ext1")
    .append(", resident_zip")
    .append(", resident_addr1")
    .append(", resident_addr2")
    .append(", resident_addr3")
    .append(", resident_addr4")
    .append(", resident_addr5")
    .append(", mail_zip")
    .append(", mail_addr1")
    .append(", mail_addr2")
    .append(", mail_addr3")
    .append(", mail_addr4")
    .append(", mail_addr5")
    .append(", company_zip")
    .append(", company_addr1")
    .append(", company_addr2")
    .append(", company_addr3")
    .append(", company_addr4")
    .append(", company_addr5")
    .append(", bill_apply_flag")
    .append(", branch")
    .append(", mail_type")
    .append(", company_name")
    .append(", job_position")
    .append(", service_year")
    .append(", salary")
    .append(", spouse_id_no")
    .append(", spouse_birthday")
    .append(", spouse_name")
    .append(", act_no_l")
    .append(", act_no_l_ind")
    .append(", autopay_acct_bank")
    .append(", credit_lmt")
    .append(", revolve_int_rate_year")
    .append(", revolve_int_rate")
    .append(", special_card_rate")
    .append(", sms_amt")
    .append(", send_pwd_flag")
    .append(", son_card_flag")
    .append(", indiv_crd_lmt")
    .append(", fee_code_i")
    .append(", inst_flag")
    .append(", roadside_assist_apply")
    .append(", market_agree_base")
    .append(", e_news")
    .append(", introduce_emp_no")
    .append(", introduce_id")
    .append(", reg_bank_no")
    .append(", emboss_4th_data")
    .append(", promote_emp_no")
    .append(", promote_dept")
    .append(", credit_level_new")
    .append(", jcic_score")
    .append(", source")
    .append(", sup_flag")
    .append(", valid_fm")
    .append(", valid_to")
    .append(", combo_indicator")
    .append(", stat_send_internet")
    .append(", nccc_type")
    .append(", crt_date, crt_user ")
    .append(", apr_date, apr_user ")
    .append(", mod_time, mod_user, mod_pgm, mod_seqno")
    .append(", oth_chk_code ")
    .append(", clerk_id ")
    .append(" ) values (")
    .append("  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    .append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    .append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    .append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    .append(", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    .append(", ?, '1', '0', to_char(sysdate, 'yyyymm')||'01',?, ?, ?")
    .append(", '1', to_char(sysdate,'yyyymmdd'), ?")
    .append(", '', '', null, '', ?, 1 ,'0' ,?)");
    
    strSql = sb.toString();
    
    // -set ?value-
    Object[] param = new Object[] {wp.itemStr("apply_id").toUpperCase(), wp.itemStr("batchno"),
        wp.itemStr("recno"), wp.itemStr("apply_no").toUpperCase(), wp.itemStr("group_code"),
        wp.itemStr("card_type"), wp.itemStr("unit_code"),
        wp.itemStr("online_mark").equals("") ? "N" : "Y",
         wp.itemStr("stmt_cycle"), wp.itemStr("apply_id").toUpperCase(),
        wp.itemStr("sex"), wp.itemStr("birthday"), wp.itemStr("chi_name"),
        wp.itemStr("eng_name").toUpperCase(), wp.itemStr("nation"),
        wp.itemStr("other_cntry_code").toUpperCase(), wp.itemStr("resident_no_expire_date"),
        wp.itemStr("passport_no"), wp.itemStr("passport_date"), wp.itemStr("ur_flag"),
        wp.itemStr("marriage"), wp.itemStr("education"), wp.itemStr("business_code"),
        wp.itemStr("graduation_elementarty"), wp.itemStr("cellar_phone"), wp.itemStr("e_mail_addr"),
        wp.itemStr("home_area_code1"), wp.itemStr("home_tel_no1"), wp.itemStr("home_tel_ext1"),
        wp.itemStr("office_area_code1"), wp.itemStr("office_tel_no1"),
        wp.itemStr("office_tel_ext1"), wp.itemStr("resident_zip2"), wp.itemStr("resident_addr1"),
        wp.itemStr("resident_addr2"), wp.itemStr("resident_addr3"), wp.itemStr("resident_addr4"),
        wp.itemStr("resident_addr5"), wp.itemStr("mail_zip2"), wp.itemStr("mail_addr1"),
        wp.itemStr("mail_addr2"), wp.itemStr("mail_addr3"), wp.itemStr("mail_addr4"),
        wp.itemStr("mail_addr5"), wp.itemStr("company_zip2"), wp.itemStr("company_addr1"),
        wp.itemStr("company_addr2"), wp.itemStr("company_addr3"), wp.itemStr("company_addr4"),
        wp.itemStr("company_addr5"), wp.itemStr("bill_apply_flag"), wp.itemStr("branch"),
        wp.itemStr("mail_type"), wp.itemStr("company_name"), wp.itemStr("job_position"), service,
        empty(wp.itemStr("salary")) ? 0 : wp.itemStr("salary"),
        wp.itemStr("spouse_id_no").toUpperCase(), wp.itemStr("spouse_birthday"),
        wp.itemStr("spouse_name"), wp.itemStr("act_no_l"), wp.itemStr("act_no_l_ind"),
        wp.itemStr("autopay_acct_bank"),
        empty(wp.itemStr("credit_lmt")) ? 0 : wp.itemStr("credit_lmt"),
        wp.itemStr("revolve_int_rate_year"), 
        crdEmap.revolveIntRate.doubleValue(), 
        crdEmap.specialCardRate.doubleValue(),
        empty(wp.itemStr("sms_amt")) ? 0 : wp.itemStr("sms_amt"), wp.itemStr("send_pwd_flag"),
        wp.itemStr("son_card_flag"),
        empty(wp.itemStr("indiv_crd_lmt")) ? 0 : wp.itemStr("indiv_crd_lmt"),
        wp.itemStr("fee_code_i"), wp.itemStr("inst_flag"),
        wp.itemStr("roadside_assist_apply").toUpperCase(), wp.itemStr("market_agree_base"),
        wp.itemStr("e_news"), colEmpty("emap_employ_no")?introduceEmpNo.toUpperCase():colStr("emap_employ_no"), introduceId, wp.itemStr("reg_bank_no"),
        wp.itemStr("emboss_4th_data").toUpperCase(), wp.itemStr("promote_emp_no").toUpperCase(),
        colEmpty("emap_unit_no")?wp.itemStr("promote_dept").toUpperCase():colStr("emap_unit_no"), wp.itemStr("credit_level_new"),
        empty(wp.itemStr("jcic_score")) ? 0 : wp.itemStr("jcic_score"),
        crdEmap.validTo, crdEmap.comboIndicator,
        wp.itemStr("stat_send_internet"), wp.itemStr("crt_user"), wp.itemStr("mod_pgm")
       	,colStr("emap_id")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(String.format("新增crd_emap_tmp錯誤。Sql Error Text: %s", this.sqlErrtext));
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    
    CrdEmap2 crdEmap =  getCrdEmap(wp.itemStr("unit_code"), wp.itemStr("group_code"), wp.itemStr("revolve_int_rate_year"));
    if (rc != 1 || crdEmap == null) return rc;
    
    StringBuffer sb = new StringBuffer();
    
    sb.append("update crd_emap_tmp set")
    .append("  pm_id=?")
    .append(", batchno=?")
    .append(", recno=?")
    .append(", apply_no=?")
    .append(", group_code=?")
    .append(", card_type=?")
    .append(", unit_code=?")
    .append(", online_mark=?")
    .append(", stmt_cycle=?")
    .append(", apply_id=?")
    .append(", sex=?")
    .append(", birthday=?")
    .append(", chi_name=?")
    .append(", eng_name=?")
    .append(", nation=?")
    .append(", other_cntry_code=?")
    .append(", resident_no_expire_date=?")
    .append(", passport_no=?")
    .append(", passport_date=?")
    .append(", ur_flag=?")
    .append(", marriage=?")
    .append(", education=?")
    .append(", business_code=?")
    .append(", graduation_elementarty=?")
    .append(", cellar_phone=?")
    .append(", e_mail_addr=?")
    .append(", home_area_code1=?")
    .append(", home_tel_no1=?")
    .append(", home_tel_ext1=?")
    .append(", office_area_code1=?")
    .append(", office_tel_no1=?")
    .append(", office_tel_ext1=?")
    .append(", resident_zip=?")
    .append(", resident_addr1=?")
    .append(", resident_addr2=?")
    .append(", resident_addr3=?")
    .append(", resident_addr4=?")
    .append(", resident_addr5=?")
    .append(", mail_zip=?")
    .append(", mail_addr1=?")
    .append(", mail_addr2=?")
    .append(", mail_addr3=?")
    .append(", mail_addr4=?")
    .append(", mail_addr5=?")
    .append(", company_zip=?")
    .append(", company_addr1=?")
    .append(", company_addr2=?")
    .append(", company_addr3=?")
    .append(", company_addr4=?")
    .append(", company_addr5=?")
    .append(", bill_apply_flag=?")
    .append(", branch=?")
    .append(", mail_type=?")
    .append(", company_name=?")
    .append(", job_position=?")
    .append(", service_year=?")
    .append(", salary=?")
    .append(", spouse_id_no=?")
    .append(", spouse_birthday=?")
    .append(", spouse_name=?")
    .append(", act_no_l=?")
    .append(", act_no_l_ind=?")
    .append(", autopay_acct_bank=?")
    .append(", credit_lmt=?")
    .append(", revolve_int_rate_year=?")
    .append(", revolve_int_rate=?")
    .append(", special_card_rate=?")
    .append(", sms_amt=?")
    .append(", send_pwd_flag=?")
    .append(", son_card_flag=?")
    .append(", indiv_crd_lmt=?")
    .append(", fee_code_i=?")
    .append(", inst_flag=?")
    .append(", roadside_assist_apply=?")
    .append(", market_agree_base=?")
    .append(", e_news=?")
    .append(", introduce_emp_no=?")
    .append(", introduce_id=?")
    .append(", reg_bank_no=?")
    .append(", emboss_4th_data=?")
    .append(", promote_emp_no=?")
    .append(", promote_dept=?")
    .append(", credit_level_new=?")
    .append(", jcic_score=?")
    .append(", valid_fm=to_char(sysdate, 'yyyymm')||'01'")
    .append(", valid_to=?")
    .append(", combo_indicator=?")
    .append(", stat_send_internet=?")
    .append(", mod_time=sysdate")
    .append(", mod_user=?")
    .append(", mod_pgm=?")
    .append(", mod_seqno=nvl(mod_seqno,0)+1")
    .append(", oth_chk_code = '0' ")
    .append(", clerk_id = ? ")
    .append(sqlWhere);

    strSql = sb.toString();
    
    Object[] param = new Object[] {wp.itemStr("apply_id").toUpperCase(), wp.itemStr("batchno"),
        wp.itemStr("recno"), wp.itemStr("apply_no").toUpperCase(), wp.itemStr("group_code"),
        wp.itemStr("card_type"), wp.itemStr("unit_code"),
        wp.itemStr("online_mark").equals("") ? "N" : "Y", 
        wp.itemStr("stmt_cycle"), wp.itemStr("apply_id").toUpperCase(),
        wp.itemStr("sex"), wp.itemStr("birthday"), wp.itemStr("chi_name"),
        wp.itemStr("eng_name").toUpperCase(), wp.itemStr("nation"),
        wp.itemStr("other_cntry_code").toUpperCase(), wp.itemStr("resident_no_expire_date"),
        wp.itemStr("passport_no"), wp.itemStr("passport_date"), wp.itemStr("ur_flag"),
        wp.itemStr("marriage"), wp.itemStr("education"), wp.itemStr("business_code"),
        wp.itemStr("graduation_elementarty"), wp.itemStr("cellar_phone"), wp.itemStr("e_mail_addr"),
        wp.itemStr("home_area_code1"), wp.itemStr("home_tel_no1"), wp.itemStr("home_tel_ext1"),
        wp.itemStr("office_area_code1"), wp.itemStr("office_tel_no1"),
        wp.itemStr("office_tel_ext1"), wp.itemStr("resident_zip2"), wp.itemStr("resident_addr1"),
        wp.itemStr("resident_addr2"), wp.itemStr("resident_addr3"), wp.itemStr("resident_addr4"),
        wp.itemStr("resident_addr5"), wp.itemStr("mail_zip2"), wp.itemStr("mail_addr1"),
        wp.itemStr("mail_addr2"), wp.itemStr("mail_addr3"), wp.itemStr("mail_addr4"),
        wp.itemStr("mail_addr5"), wp.itemStr("company_zip2"), wp.itemStr("company_addr1"),
        wp.itemStr("company_addr2"), wp.itemStr("company_addr3"), wp.itemStr("company_addr4"),
        wp.itemStr("company_addr5"), wp.itemStr("bill_apply_flag"), wp.itemStr("branch"),
        wp.itemStr("mail_type"), wp.itemStr("company_name"), wp.itemStr("job_position"), service,
        empty(wp.itemStr("salary")) ? 0 : wp.itemStr("salary"),
        wp.itemStr("spouse_id_no").toUpperCase(), wp.itemStr("spouse_birthday"),
        wp.itemStr("spouse_name"), wp.itemStr("act_no_l"), wp.itemStr("act_no_l_ind"),
        wp.itemStr("autopay_acct_bank"),
        empty(wp.itemStr("credit_lmt")) ? 0 : wp.itemStr("credit_lmt"),
        wp.itemStr("revolve_int_rate_year"), 
        crdEmap.revolveIntRate.doubleValue(),
        crdEmap.specialCardRate.doubleValue(),
        empty(wp.itemStr("sms_amt")) ? 0 : wp.itemStr("sms_amt"), wp.itemStr("send_pwd_flag"),
        wp.itemStr("son_card_flag"),
        empty(wp.itemStr("indiv_crd_lmt")) ? 0 : wp.itemStr("indiv_crd_lmt"),
        wp.itemStr("fee_code_i"), wp.itemStr("inst_flag"),
        wp.itemStr("roadside_assist_apply").toUpperCase(), wp.itemStr("market_agree_base"),
        wp.itemStr("e_news"), colEmpty("emap_employ_no")?introduceEmpNo.toUpperCase():colStr("emap_employ_no"), introduceId, wp.itemStr("reg_bank_no"),
        wp.itemStr("emboss_4th_data").toUpperCase(), wp.itemStr("promote_emp_no").toUpperCase(),
        colEmpty("emap_unit_no")?wp.itemStr("promote_dept").toUpperCase():colStr("emap_unit_no"), wp.itemStr("credit_level_new"),
        empty(wp.itemStr("jcic_score")) ? 0 : wp.itemStr("jcic_score"), 
        crdEmap.validTo, crdEmap.comboIndicator,
        wp.itemStr("stat_send_internet"), wp.itemStr("mod_user"), wp.itemStr("mod_pgm"),
        colStr("emap_id"),wp.itemStr("batchno"), wp.itemStr("recno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(String.format("修改 crd_emap_tmp 錯誤。Sql error code: %s", this.sqlErrtext));
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_emap_tmp " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("batchno"), wp.itemStr("recno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(String.format("刪除crd_emap_tmp錯誤。Sql Error Text: %s", this.sqlErrtext));
    }
    return rc;
  }
  
  /**
   * get acct_type by group_code and card_type
   * @param groupCode
   * @param cardType
   * @return return acct_type when getting acct_type else return null
   */
  private String getAcctType(String groupCode, String cardType){
	  StringBuffer sb = new StringBuffer();
	  
	  sb.append(" SELECT  a.acct_type ")
	  .append(" FROM ptr_acct_type a, ptr_prod_type b ")
	  .append(" WHERE b.group_code  = :groupCode1 AND  b.card_type = :cardType1  AND  a.acct_type = b.acct_type ")
	  .append(" UNION ")
	  .append(" SELECT  a.acct_type ")
	  .append(" FROM ptr_acct_type a, ptr_prod_type b ")
	  .append(" WHERE b.group_code  = :groupCode2 AND  a.acct_type   = b.acct_type ")
	  .append(" UNION ")
	  .append(" SELECT a.acct_type ")
	  .append(" FROM ptr_acct_type a, ptr_prod_type b ")
	  .append(" WHERE b.card_type   = :cardType2   AND  a.acct_type   = b.acct_type ")
	  .append(" FETCH FIRST 1 ROW ONLY ");
	  
	  String sql = sb.toString();
	  
	  setString("groupCode1", groupCode);
	  setString("cardType1", cardType);
	  setString("groupCode2", groupCode);
	  setString("cardType2", cardType);
	  
	  sqlSelect(sql);
	  
	  if (sqlRowNum <= 0) {
		errmsg(String.format("無法取得acct_type。group_code[%s], card_type[%s]", groupCode, cardType));
		return null;
	  }
	  
	  return colStr("acct_type");
  }
  
  /**
   * get corp_p_seqno by corp_no
   * @param corpNo
   * @return return corp_p_seqno when getting corp_p_seqno else return null
   */
  private String getCorpPSeqno(String corpNo){
		String sql = " SELECT  corp_p_seqno  FROM crd_corp  WHERE corp_no  = :corpNo ";

		setString("corpNo", corpNo);

		sqlSelect(sql);

		if (sqlRowNum <= 0) {
			errmsg(String.format("無法取得corp_p_seqno。corp_no[%s]", corpNo));
			return null;
		}

		return colStr("corp_p_seqno");
  }
  
  /**
   * get stmt_cycle by group_code, card_type, and corp_no
   * @param groupCode
   * @param cardType
   * @param corpNo
   * @return return stmt_cycle if getting it else return null if error occurs
   */
	private String getStmtCycle(String groupCode, String cardType, String corpNo) {
		String acctType = getAcctType(groupCode, cardType);
		if (acctType == null) return null;
		
		String corpPSeqno = getCorpPSeqno(corpNo);
		if (corpPSeqno == null) return null;
		
		String sql = "SELECT STMT_CYCLE  FROM ACT_ACNO aa "
				     + " WHERE ACNO_FLAG = '2'  AND CORP_P_SEQNO = :corpPSeqno "
				     + " AND ACCT_TYPE  = :acctType";
		
		setString("corpPSeqno", corpPSeqno);
		setString("acctType", acctType);
		
		sqlSelect(sql);

		if (sqlRowNum <= 0) {
			errmsg(String.format("無法取得STMT_CYCLE。corp_p_seqno[%s], acct_type[%s]", corpPSeqno, acctType));
			return null;
		}

		return colStr("STMT_CYCLE");
	}

	/**
	 * 	get valid_to, combo_indicator, special_card_rate, and revolve_int_rate
	 * @param unitCode
	 * @param groupCode
	 * @param revolveIntRateYearStr
	 * @return CrdEmap, or return null if any error occurs 
	 */
	private CrdEmap2 getCrdEmap(String unitCode, String groupCode, String revolveIntRateYearStr) {
		CrdEmap2 crdEmap = new CrdEmap2();
		
		BigDecimal specialCardRate = BigDecimal.ZERO, 
				         revolveIntRate = BigDecimal.ZERO,
				         revolveIntRateYearFromPGC = null, 
				         revolveIntRateYear = null;
		
		// 取VALID_TO
		crdEmap.validTo = getValidTo(unitCode);
		if (rc != 1) return null;

	    // 取COMBO_INDICATOR
	    selectPtrGroupCode(groupCode);
	    if (rc != 1) return null;
	    crdEmap.comboIndicator = colStr("combo_indicator");

	    // 將4碼字串轉為小數(整數兩位及小數兩位) 
	    revolveIntRateYearFromPGC = 
	    		BigDecimal.valueOf(strToNum(colStr("revolve_int_rate_year")));

	    // 畫面上的年利率
	    revolveIntRateYear = 
	    		BigDecimal.valueOf(strToNum(revolveIntRateYearStr)).
	    		divide(BigDecimal.valueOf(100));
	    wp.itemSet("revolve_int_rate_year", revolveIntRateYear.toPlainString());

	    // 年利率
	    if ("Y".equals(colStr("special_card_rate_flag"))) {
	      if (revolveIntRateYearFromPGC.doubleValue() == revolveIntRateYear.doubleValue()) {
	    	  specialCardRate = revolveIntRateYearFromPGC.
	    			  divide(BigDecimal.valueOf(10000)).
	    			  divide(BigDecimal.valueOf(365)).
	    			  multiply(BigDecimal.valueOf(1000000)).
	    			  setScale(3, BigDecimal.ROUND_HALF_UP);
	      } else {
	        errmsg("此團代為特殊利率，年利率與Ptrm0190設定的不同");
	        return null;
	      }
	    } else {
	      strSql = "select rcrate_day from ptr_rcrate where rcrate_year = :revolveIntRate";
	      setDouble("revolveIntRate", revolveIntRateYear.doubleValue());
	      sqlSelect(strSql);
	      if (sqlRowNum <= 0) {
	    	  errmsg("年利率不在Ptrm0010中");
	    	  return null;
	      }else {
	    	  revolveIntRate = BigDecimal.valueOf(strToNum(colStr("rcrate_day")));
	      }
	    }
		
	    crdEmap.specialCardRate = specialCardRate;
	    crdEmap.revolveIntRate = revolveIntRate;
		
		return crdEmap;
	}

	/**
	 * select from ptr_group_code
	 * @param groupCode
	 */
	private void selectPtrGroupCode(String groupCode) {
		strSql = "select combo_indicator, special_card_rate_flag, revolve_int_rate_year "
	        + "from ptr_group_code where group_code = :group_code";
	    setString("group_code", groupCode);
	    sqlSelect(strSql);
	    if (sqlRowNum <= 0) {
	    	  errmsg(String.format("查無資料。TABLE: combo_indicator。group_code[%s]", groupCode));
	    	  return;
	    }
	    
	}


	/**
	 * get validTo
	 * @param unitCode
	 * @return valid_to
	 */
	private String getValidTo(String unitCode) {
		strSql = "select new_extn_mm from crd_item_unit where unit_code = :unit_code";
	    setString("unit_code", unitCode);
	    sqlSelect(strSql);
	    if (sqlRowNum <= 0) {
	    	  errmsg(String.format("查無資料。TABLE: crd_item_unit。unit_code[%s]", unitCode));
	    	  return null;
	    }
	    
	    int newExtnMM = Integer.parseInt(nvl(colStr("new_extn_mm"), "0"));

	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	    Calendar c = Calendar.getInstance();
	    c.set(Calendar.YEAR, Integer.parseInt(getSysDate().substring(0, 4)) + newExtnMM);
	    c.set(Calendar.MONTH, Integer.parseInt(getSysDate().substring(4, 6)) - 1);
	    c.set(Calendar.DATE, c.getActualMaximum(Calendar.DAY_OF_MONTH));
	    return sdf.format(c.getTime());

	}
	
	public boolean checkText(String str) {
		boolean checkStr = true;
		for(int i=0;i<str.length();i++){
			int acsii = str.charAt(i); 
			checkStr = (acsii<0 || acsii > 128)?true:false;
			if(checkStr == false)
				return false;
		}
		return true;
	}
	
}

class CrdEmap2{
	String validTo; // 有效年月止
	String comboIndicator; // combol卡指示碼
	BigDecimal specialCardRate = BigDecimal.ZERO; // 特殊卡片利率-新戶
	BigDecimal revolveIntRate = BigDecimal.ZERO;  //帳戶循環信用利率加減利率
}
