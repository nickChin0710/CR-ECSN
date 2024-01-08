package ccam01;
/** 停用記錄查詢
 * 19-0611:    JH    p_xxx >>acno_p_xxx
 * 109-04-20  V1.00.00 Zhenwu Zhu       updated for project coding standard
 * 110-01-15  V1.00.01 Justin           fix  a query bug, improve performance
 * 110-11-18  V1.00.02 Justin           新增「停掛來源」
 * 111-05-27  V1.00.03 Justin           add mail_branch
 * 111-06-02  V1.00.04 Ryan             ETABS,網銀  mod_user ==> crt_user
 * 111-06-14  V1.00.05 Ryan             ETABS,調整欄位OPPO_USER,LOGIC_DEL_USER,CHG_USER
 * 111-08-10  V1.00.06 Ryan             新增 : 非ETABS時且停掛人員為四碼時，欄位顯示中間要空一格
 * 112-03-13  V1.00.07 Ryan             新增 :where條件 mod_pgm = 停掛來源
* */
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoExcel;

public class Ccaq2020 extends BaseAction implements InfaceExcel {
  String lsWhere1 = "";

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      xlsPrint();
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_oppo_date1"), wp.itemStr("ex_oppo_date2")) == false) {
      alertErr2("停掛日期起迄：輸入錯誤");
      return;
    }
    if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
      if (wp.itemEmpty("ex_oppo_date1") && wp.itemEmpty("ex_oppo_date2")) {
        errmsg("停掛日期: 不可都為空白");
        return;
      }
    }
    getWhereStr();

    wp.setQueryMode();
    queryRead();

  }

  
/**
 *  * 112-03-13  V1.00.07 Ryan     新增 :where條件 mod_pgm = 停掛來源
 * */
  void getWhereStr() {
    sqlParm.clear();
    lsWhere1 = " where 1=1 " 
        + sqlCol(wp.itemStr("ex_oppo_date1"), "A.oppo_date", ">=")
        + sqlCol(wp.itemStr("ex_oppo_date2"), "A.oppo_date", "<=")
        + sqlCol(wp.itemStr("ex_oppo_type"), "A.oppo_type")
        + sqlCol(wp.itemStr("ex_oppo_user"), "A.oppo_user", "like%")
        + sqlCol(wp.itemStr("ex_oppo_status"), "A.oppo_status")
        + sqlCol(wp.itemStr("ex_card_no"), "A.card_no");
    
    // Justin 2021/11/18
    // 其他
    if (ModPgm.OTHERS.engVal.equals(wp.itemStr("ex_mod_pgm"))) {
    	// MOD_PGM<> “Etb0001”、“ECSCDA39”
//    	lsWhere1 += sqlCol(ModPgm.EBANK.engVal, "A.mod_pgm", "<>");
//    	lsWhere1 += sqlCol(ModPgm.ETABS.engVal, "A.mod_pgm", "<>");
    	lsWhere1 += sqlCol(ModPgm.EBANK.engVal, "A.crt_user", "<>");
    	lsWhere1 += sqlCol(ModPgm.ETABS.engVal, "A.crt_user", "<>");
    	lsWhere1 += sqlCol(ModPgm.EBANK.engVal, "A.mod_pgm", "<>");
    	lsWhere1 += sqlCol(ModPgm.ETABS.engVal, "A.mod_pgm", "<>");
	}else {
		// ETABS, 網銀, or 沒勾選
//		lsWhere1 += sqlCol(wp.itemStr("ex_mod_pgm"), "A.mod_pgm");
//		lsWhere1 += sqlCol(wp.itemStr("ex_mod_pgm"), "A.crt_user");	
		if(!wp.itemEmpty("ex_mod_pgm")) {
		StringBuffer bf = new StringBuffer();
		bf.append(" and (");
		bf.append(" A.crt_user = '").append(wp.itemStr("ex_mod_pgm")).append("'");
		bf.append(" or A.mod_pgm = '").append(wp.itemStr("ex_mod_pgm")).append("')");
		lsWhere1 += bf.toString();
		}
	}

    if (!wp.itemEmpty("ex_idno")) {
        lsWhere1 += " and A.card_no in ("
            + " (select card_no from crd_card where 1=1 and major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+ sqlCol(wp.itemStr("ex_idno"), "id_no") + ") )"
            + " union "
            + " (select card_no from crd_card where 1=1 and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+ sqlCol(wp.itemStr("ex_idno"), "id_no") + ")) "
            + " union "
            + " (select card_no from dbc_card where 1=1 and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 "+ sqlCol(wp.itemStr("ex_idno"), "id_no") + "))"
            + " ) ";
      }

    wp.whereStr = lsWhere1;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    getWhereStr();
    
    StringBuilder sb = new StringBuilder();
    sb.append(" A.oppo_type , " );
    sb.append(" A.card_no," );
    sb.append(" A.oppo_date," );
    sb.append(" A.oppo_time,");
    sb.append(" A.oppo_status," );
    sb.append(" A.oppo_user," );
    sb.append(" A.renew_flag," );
    sb.append(" A.logic_del_date,");
    sb.append(" A.logic_del_time," );
    sb.append(" A.logic_del_user," );
    sb.append(" 1 as db_sum,");
    sb.append(" uf_hi_cardno(A.card_no) as wk_card_no, " );
    sb.append(" A.mod_user , ");
    sb.append(" to_char(A.mod_time,'yyyymmdd') as mod_date , ");
    sb.append(" to_char(A.mod_time,'hh24miss') as mod_time , ");
    sb.append(" decode((select opp_remark from cca_opp_type_reason where opp_status = A.oppo_status),'',A.oppo_status,(select opp_remark from cca_opp_type_reason where opp_status = A.oppo_status)) as opp_remark , ");
    sb.append(" A.chg_date , " );
    sb.append(" A.chg_time ,  ");
    sb.append(" A.chg_user ,  ");
    sb.append(" A.mod_pgm , ");
    sb.append(" A.mail_branch , ");
    sb.append(" A.crt_user , ");
//    sb.append(String.format(" decode(A.mod_pgm, '%s', '%s', '%s', '%s', A.mod_pgm) as mod_pgm_desc ", ModPgm.ETABS.engVal, ModPgm.ETABS.chineseVal, ModPgm.EBANK.engVal, ModPgm.EBANK.chineseVal));
    sb.append(String.format(" decode(A.crt_user, '%s', '%s', '%s', '%s', A.mod_pgm) as mod_pgm_desc ", ModPgm.ETABS.engVal, ModPgm.ETABS.chineseVal, ModPgm.EBANK.engVal, ModPgm.EBANK.chineseVal));

    wp.selectSQL = sb.toString();

    wp.daoTable = " cca_opposition A ";
    wp.whereOrder = " order by 1 , 2 ";

    pageQuery();
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    wp.setPageValue();
    queryAfter();
  }

  void queryAfter() {

    String sql1 =
        " select uf_idno_id(id_p_seqno) as id_no , uf_idno_id(major_id_p_seqno) as major_idno , sup_flag "
            + " from crd_card where 1=1 and card_no = ? ";

    String sql2 =
        " select uf_idno_id2(id_p_seqno,'Y') as id_no , uf_idno_id2(major_id_p_seqno,'Y') as major_idno , sup_flag "
            + " from dbc_card where 1=1 and card_no = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String str = wp.colStr(ii,"crt_user");
      if(ModPgm.ETABS.engVal.equals(str) || !ModPgm.ETABS.engVal.equals(str)) {
    	  str = wp.colStr(ii,"oppo_user");
    	  if(str.length() == 4) {
    		  wp.colSet(ii,"oppo_user",String.format("%s %s",strMid(str, 0, 2),strMid(str, 2, 2)));
    	  }
    	  str = wp.colStr(ii,"logic_del_user");
    	  if(str.length() == 4) {
    		  wp.colSet(ii,"logic_del_user",String.format("%s %s",strMid(str, 0, 2),strMid(str, 2, 2)));
    	  }
    	  str = wp.colStr(ii,"chg_user");
    	  if(str.length() == 4) {
    		  wp.colSet(ii,"chg_user",String.format("%s %s",strMid(str, 0, 2),strMid(str, 2, 2)));
    	  }
      }
    	
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "id_no", sqlStr("id_no"));
        wp.colSet(ii, "major_idno", sqlStr("major_idno"));
        wp.colSet(ii, "sup_flag", sqlStr("sup_flag"));
        continue;
      }

      sqlSelect(sql2, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "id_no", sqlStr("id_no"));
        wp.colSet(ii, "major_idno", sqlStr("major_idno"));
        wp.colSet(ii, "sup_flag", sqlStr("sup_flag"));
        continue;
      }
    }

  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Ccaq2020")) {
        wp.optionKey = wp.colStr("ex_oppo_type");
        dddwList("ddw_opptype", "cca_sys_parm3", "sys_key", "sys_data1",
            "where sys_id = 'OPPTYPE' and sys_key <> '0'");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "Ccaq2020")) {
        wp.optionKey = wp.colStr("ex_oppo_status");
        dddwList("ddw_opptype_reason", "cca_opp_type_reason", "opp_status",
            "ncc_opp_type||'-'||opp_status||'-'||opp_remark", "where 1=1");
      }

    } catch (Exception ex) {
    }
  }

  @Override
  public void xlsPrint() throws Exception {
    try {
      log("xlsFunction: started--------");
      wp.reportId = "ccaq2020";
      String cond1 = "停掛日期: " + commString.strToYmd(wp.itemStr("ex_oppo_date1")) + " -- "
          + commString.strToYmd(wp.itemStr("ex_oppo_date2"));
      wp.colSet("cond1", cond1);
      TarokoExcel xlsx = new TarokoExcel();
      wp.fileMode = "Y";
      xlsx.excelTemplate = "ccaq2020.xlsx";
      wp.pageRows = 9999;
      queryFunc();

      xlsx.processExcelSheet(wp);
      xlsx.outputExcel();
      xlsx = null;
      log("xlsFunction: ended-------------");
    } catch (Exception ex) {
      wp.expMethod = "xlsPrint";
      wp.expHandle(ex);
    }

  }

  @Override
  public void logOnlineApprove() throws Exception {
    // TODO Auto-generated method stub

  }
  
	enum ModPgm {
		ETABS("Etb0001", "ETABS"), EBANK("ECSCDA39", "網銀"), OTHERS("others", "其他");
		String engVal = null;
		String chineseVal = null;
		private ModPgm(String engVal, String chineseVal) {
			this.engVal = engVal;
			this.chineseVal = chineseVal;
		}
	}

}
