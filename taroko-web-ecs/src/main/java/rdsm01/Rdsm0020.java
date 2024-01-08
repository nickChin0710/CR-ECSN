package rdsm01;
/**
 * 2023-0502  JH    ++rds_pcard
 * 109-04-22  V1.00.00  Tanwei       updated for project coding standard
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名
 * 112-12-04  V1.00.04   Zuwei Su    新增同一ID,可建2卡2車(不同卡號、車號)
 * */

import ofcapp.BaseAction;

public class Rdsm0020 extends BaseAction {
  String lsCardNo = "", lsCarNo = "", lsIdNo = "", lsErrorDesc = "";
  // -- kk1: rowid , kk2 = card_no , kk3 = rm_carno , kk4 = rm_type
  String rowid = "", cardNO = "", rdCarno = "", rmType = "";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      lsCardNo = wp.itemStr("ex_card_no");
      lsCarNo = wp.itemStr("ex_car_no");
      lsIdNo = wp.itemStr("ex_id_no");
      strAction = "new";
      // clearFunc();
      if (selectNewData(lsCardNo, lsCarNo, lsIdNo) == false) {
        alertErr2(lsErrorDesc);
        return;
      }
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
      tabClick();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_id_no")) && empty(wp.itemStr("ex_card_no"))
        && empty(wp.itemStr("ex_car_no"))) {
      alertErr2("身分證號，卡號，車號 不可同時空白 ");
      return;
    }

    if (wp.itemEmpty("ex_card_no") == false) {
      zzVipColor(wp.itemStr2("ex_card_no"));
    }

    String lsWhere = " where 1=1  " 
        + sqlCol(wp.itemStr("ex_card_no"), "card_no")
        + sqlCol(wp.itemStr("ex_car_no"), "rm_carno")
        + sqlCol(wp.itemStr("ex_id_no"), "rm_carmanid");


    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    daoTid = "A1_";
    StringBuilder sb= new StringBuilder();
    sb.append(" card_no ,"); 
    sb.append(" rm_type ,");
    sb.append(" decode(rm_type,'F','免費','E','自費') as tt_rm_type ,"); 
    sb.append(" rm_carno ,");
    sb.append(commSqlStr.ufunc("uf_idno_name(card_no) as chi_name ,"));
    sb.append(" rm_carmanname ,"); 
    sb.append(" rm_status ,");
    sb.append(" decode(rm_status,'1','新增車號','2','變更車號','0','停用','3','取消車號','4','未啟用') as tt_rm_status ,");
    sb.append(" rm_payamt ,"); 
    sb.append(" rm_validdate ,"); 
    sb.append(" rm_moddate ,"); 
    sb.append(" rm_reason, rds_pcard, ");
    sb.append(" decode(rm_reason,'1','到期不續購','2','消費不足暫停服務','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rm_reason ,");
    sb.append(" hex(rowid) as rowid ");
    wp.selectSQL = sb.toString();
    wp.daoTable = "cms_roadmaster";
    pageQuery();
    wp.setListCount(0);
    if (sqlNotFind()) {
      wp.notFound = "N";
    }

    queryRead2();

    if (wp.listCount[0] == 0 && wp.listCount[1] == 0) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  public void queryRead2() throws Exception {
    daoTid = "A2_";
    StringBuilder sb = new StringBuilder();
    sb.append(" rd_moddate ,"); 
    sb.append(" rd_type ,");
    sb.append(" decode(rd_type,'F','免費','E','自費') as tt_rd_type ,"); 
    sb.append(" card_no ,"); 
    sb.append(" rd_carno ,");
    sb.append(commSqlStr.ufunc("uf_idno_name(card_no) as db_cname , "));
    sb.append(" rd_carmanname ,"); 
    sb.append(" rd_status ,");
    sb.append(" decode(rd_status,'1','新增車號','2','變更車號','0','停用','3','取消車號', '4', '未啟用') as tt_rd_status ,");
    sb.append(" new_card_no ,"); 
    sb.append(" rd_newcarno ,"); 
    sb.append(" rd_payamt ,"); 
    sb.append(" rd_validdate ,"); 
    sb.append(" rd_stoprsn ,");
    sb.append(" decode(rd_stoprsn,'1','到期不續購','2','消費不足暫停服務','3','卡片已為無效卡','4','卡友來電要求停用','5','金卡提升為白金卡','6','未達年度續用標準','7','無車號且非自動登錄卡') as tt_rd_stoprsn ,");
    sb.append(" hex(rowid) as rowid ,"); 
    sb.append(" apr_date ,"); 
    sb.append(" rd_modtype ,"); 
    sb.append(" rd_seqno ,"); 
    sb.append(" mod_user ,");
    sb.append(" appl_card_no ");
    wp.selectSQL = sb.toString();
    wp.daoTable = "cms_roaddetail";
    wp.whereStr = "where 1=1" 
        + sqlCol(wp.itemStr("ex_card_no"), "appl_card_no")
        + sqlCol(wp.itemStr("ex_car_no"), "rd_carno")
        + sqlCol(wp.itemStr("ex_id_no"), "rd_carmanid");

    if (eqIgno(wp.itemStr("ex_apr_flag"), "1")) {
      wp.whereStr += " and apr_date ='' ";
    } else if (eqIgno(wp.itemStr("ex_apr_flag"), "2")) {
      wp.whereStr += " and apr_date <>'' ";
    }
    wp.whereOrder = "order by mod_time desc";
    pageQuery();
    wp.setListCount(2);

    if (this.sqlNotFind()) {
      wp.notFound = "N";
    }
  }

  @Override
  public void querySelect() throws Exception {
    rowid = wp.itemStr("data_k1");
    cardNO = wp.itemStr("data_k2");
    rdCarno = wp.itemStr("data_k3");
    rmType = wp.itemStr("data_k4");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
	  StringBuilder sb1 =new StringBuilder();
	  sb1.append(" A.* , ");
	  sb1.append(" A.rd_status as rm_status, ");
	  sb1.append(" (select group_name from ptr_group_code as pgc where 1=1 and A.group_code = pgc.group_code ) as group_name , ");
	  sb1.append(" decode(rd_status,'1','新增車號','2','變更車號','0','停用','3','取消車號','4','未啟用') as tt_rm_status , ");
	  sb1.append(" hex(A.rowid) as rowid ");
    wp.selectSQL = sb1.toString();
    wp.daoTable = "cms_roaddetail A ";
    if (!empty(rowid)) {
      wp.whereStr = " where 1=1 and rowid = ? ";
      setRowId(rowid);
    } else {
      wp.whereStr =
          " where 1=1 " 
      + " and apr_date ='' " 
      + sqlCol(cardNO, "appl_card_no") 
      + sqlCol(rdCarno, "rd_carno");
    }
    logSql();
    pageSelect();

    if (sqlNotFind()) {
      wp.notFound = "N";
      StringBuilder sb3 =new StringBuilder();
  	  sb3.append(" A.* , ");
  	  sb3.append(" A.rd_status as rm_status, ");
  	  sb3.append(" (select group_name from ptr_group_code as pgc where 1=1 and A.group_code = pgc.group_code ) as group_name , ");
  	  sb3.append(" decode(rd_status,'1','新增車號','2','變更車號','0','停用','3','取消車號','4','未啟用') as tt_rm_status , ");
  	  sb3.append(" hex(A.rowid) as rowid ");
      wp.selectSQL = sb3.toString();
      wp.daoTable = "cms_roaddetail A ";
	  wp.whereStr = " where 1=1  " 
                                   + sqlCol(cardNO, "appl_card_no") 
                                   ;
	  wp.whereOrder = "order by mod_time DESC limit 1 ";
	  pageSelect();
		if (sqlNotFind()) {
			alertErr2("此條件查無資料");
	        return;
		}
      
//      StringBuilder sb2 = new StringBuilder();
//      sb2.append(" rm_moddate as rd_moddate , "); 
//      sb2.append(" card_no  , "); 
//      sb2.append(" card_no as appl_card_no , ");
//      sb2.append(" group_code , ");
//      sb2.append(" (select group_name from ptr_group_code as pgc where 1=1 and crm.group_code = pgc.group_code ) as group_name , "); 
//      sb2.append(" rm_type as rd_type , "); 
//      sb2.append(" rm_validdate as rd_validdate , ");
//      sb2.append(" rm_status , ");
//      sb2.append(" decode(rm_status,'1','新增車號','2','變更車號','0','停用','3','取消車號','4','未啟用') as tt_rm_status ,"); 
//      sb2.append(" rm_carno as rd_carno , "); 
//      sb2.append(" rm_carmanid as rd_carmanid , ");
//      sb2.append(" rm_carmanname as rd_carmanname , "); 
//      sb2.append(" rm_payamt , "); 
//      sb2.append(" rm_paydate , ");
//      sb2.append(" outstanding_yn , ");
//      sb2.append(" crt_user , ");
//      sb2.append(" crt_date , ");
//      sb2.append(" mod_seqno as rm_mod_seqno , "); 
//      sb2.append(" hex(rowid) as rm_rowid " );
//      
//      wp.selectSQL = sb2.toString();
//      wp.daoTable = "cms_roadmaster as crm";
//      wp.whereStr = " where 1=1 " ;
//      
//      if (notEmpty(cardNO)) {
//    	  wp.whereStr += " and card_no = :card_no ";
//    	  setString("card_no", cardNO);
//	  }
//      if (notEmpty(rdCarno)) {
//    	  wp.whereStr += " and rm_carno = :rm_carno ";
//    	  setString("rm_carno", rdCarno);
//	  }
//      if (notEmpty(rmType)) {
//    	  wp.whereStr += " and rm_type = :rm_type ";
//    	  setString("rm_type", rmType);
//	  }
//      
//      logSql();
//      pageSelect();
//      if (sqlNotFind()) {
//        alertErr2("此條件查無資料");
//        return;
//      }
      
    }else {
    	btnOnAud(false, false, false);
    }

    dataReadAfter();
  }

  void dataReadAfter() {
    String ls_valiDate=wp.colStr("rd_validdate");
    if (ls_valiDate.length() >6) {
      wp.colSet("rd_validdate", commString.left(ls_valiDate,6));
    }

    if (wp.colEmpty("card_no") == false) {
      String sql1 = "select major_card_no from crd_card where card_no = ? ";
      sqlSelect(sql1, new Object[] {wp.colStr("card_no")});

      if (sqlRowNum > 0) {
        wp.colSet("card_no", sqlStr("major_card_no"));
      } else {
        wp.colSet("card_no", wp.colStr("appl_card_no"));
      }
    }

    if (wp.colEmpty("rm_mod_seqno")) {
      String sql2 =
          "select mod_seqno , hex(rowid) as rm_rowid , rm_status from cms_roadmaster where card_no = ? and rm_carno = ? and rm_type = ? ";

      sqlSelect(sql2, new Object[] {cardNO, rdCarno, rmType});
      if (sqlRowNum > 0) {
        wp.colSet("rm_mod_seqno", sqlStr("mod_seqno"));
        wp.colSet("rm_rowid", sqlStr("rm_rowid"));
        wp.colSet("rm_status", sqlStr("rm_status"));
      }
    }

  }

  @Override
  public void saveFunc() throws Exception {
    rdsm01.Rdsm0020Func func = new rdsm01.Rdsm0020Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    log("A:" + func.isMsg);
    if (rc == 2) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "1");
      wp.colSet("conf_mesg", func.isMsg);
      return;
    } else if (rc == 3) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "2");
      wp.colSet("conf_mesg", func.isMsg);
      return;
    } else if (rc == 4) {
      wp.respMesg = "　";
      wp.colSet("conf_chk", "|| 1==1");
      wp.colSet("conf_chk_code", "3");
      wp.colSet("conf_mesg", func.isMsg);
      return;
    }else if (rc == 5) {
        wp.respMesg = "　";
        wp.colSet("conf_chk", "|| 1==1");
        wp.colSet("conf_chk_code", "4");
        wp.colSet("conf_mesg", func.isMsg);
        return;
    }

    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

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
    tabClick();

  }

  boolean selectNewData(String lsCardNo, String lsCarNo, String lsIdNo) {
	  String idPSeqno = "";
    if (empty(lsCardNo)) {
      lsErrorDesc = "卡號不可空白";
      return false;
    }

    String sql1 = " select  A.id_p_seqno ,  A.id_no ,  A.chi_name , "
        + " A.office_area_code1 ,  A.office_tel_no1 ,  A.office_tel_ext1 , "
        + " A.home_area_code1 ,  A.home_tel_no1 ,  A.home_tel_ext1 , "
        + " A.cellar_phone ,  B.major_card_no ,  B.card_type ,  B.new_end_date , "
        + " B.group_code  from crd_idno A , crd_card B "
        + " where A.id_p_seqno = B.id_p_seqno  and B.card_no = ? ";
    sqlSelect(sql1, new Object[] {lsCardNo});
    
    if (sqlRowNum <= 0) {
      lsErrorDesc = "卡號輸入錯誤";
      return false;
    } else {
      if (empty(lsIdNo)) {
        lsIdNo = sqlStr("id_no");
      } else {
        // 卡號存在CRD_CARD，檢核身分證號是否相同
        if ("".equals(sqlStr("id_no"))) {
          lsErrorDesc = "身分證號不存在";
          return false;
        } else if (!lsIdNo.equals(sqlStr("id_no"))) {
          lsErrorDesc = "身份證號(ID_NO)比對不符，該卡號身份證號碼是" + sqlStr("id_no");
          return false;
        }
      }
      idPSeqno = sqlStr("id_p_seqno");
    }

	if (!empty(lsCardNo) && !empty(lsIdNo) && !empty(idPSeqno)) {
		if (checkMaster(lsCardNo, lsIdNo, idPSeqno) == false) {
			return false;
		}
	}

    wp.colSet("rd_carmanid", sqlStr("id_no"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("rd_carmanname", sqlStr("chi_name"));
    wp.colSet("rd_otelno1", sqlStr("office_area_code1"));
    wp.colSet("rd_otelno2", sqlStr("office_tel_no1"));
    wp.colSet("rd_otelno3", sqlStr("office_tel_ext1"));
    wp.colSet("rd_htelno1", sqlStr("home_area_code1"));
    wp.colSet("rd_htelno2", sqlStr("home_tel_no1"));
    wp.colSet("rd_htelno3", sqlStr("home_tel_ext1"));
    wp.colSet("cellar_phone", sqlStr("cellar_phone"));
    wp.colSet("card_no", sqlStr("major_card_no"));
    wp.colSet("group_code", sqlStr("group_code"));
    wp.colSet("appl_card_no", lsCardNo);
    wp.colSet("rd_validdate", commString.mid(sqlStr("new_end_date"), 0, 6));
    wp.colSet("xx_sys_date", getSysDate());
    //--
    RdsFunc loFunc=new RdsFunc();
    loFunc.setConn(wp.getConn());
    String lsRdsPcard =loFunc.getRdsPcard(lsCardNo);
    wp.colSet("rds_pcard", lsRdsPcard);

    return true;
  }

  boolean checkMaster(String lsCardNo, String lsIdNo, String idPSeqno) {
//    String sql1 = " select  count(*) as db_cnt  from cms_roadmaster " 
//  + " where card_no = ?  and rm_carmanid = ? ";
//    sqlSelect(sql1, new Object[] {lsCardNo, lsIdNo});
//    if (sqlNum("db_cnt") > 0) {
//      lsErrorDesc = "此資料已存在, 請由救援主檔中修改";
//      return false;
//    }
//
//    sql1 = " select  count(*) as db_cnt  from cms_roadmaster where id_p_seqno = ? ";
//    sqlSelect(sql1, new Object[] {idPSeqno});
//	if (sqlNum("db_cnt") > 0) {
//		lsErrorDesc = "持卡人己作過免費道路救援";
//
////		// delete the data automatically: Justin
////	  sql1 = " delete from cms_roadmaster where id_p_seqno = ? ";
////      sqlExec(sql1, new Object[] {idPSeqno});
////      if (sqlRowNum > 0) {
////        sqlCommit(1);
////      }
//		
//		return false;
//	}

    String sql1 = " select card_no from cms_roadmaster where id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {idPSeqno});
    // 同一ID資料已登錄2筆,不可再登錄
    if (sqlRowNum > 1) {
        lsErrorDesc = "同一ID資料已登錄2筆,不可再登錄";
        return false;
    }
    if (sqlRowNum == 1) {
        String oldCardNo = sqlStr("card_no");
        if (lsCardNo.equals(oldCardNo)) {
            lsErrorDesc = "相同卡號不可重覆登錄";
            return false;
        }
        sql1 = "select crd_card.card_no,crd_card.current_code,crd_card.id_p_seqno"
                + ",crd_card.new_end_date,crd_card.acct_type "
                + ",PTR_ACCT_TYPE.CARD_INDICATOR "
                + " from crd_card"
                + " left join PTR_ACCT_TYPE on PTR_ACCT_TYPE.ACCT_TYPE = crd_card.ACCT_TYPE "
                + " where card_no = ? ";
        sqlSelect(sql1, new Object[] {oldCardNo});
        // 【cms_roadmaster】CARD_NO的CARD_INDICATOR欄值
        String oldCardIndicator = sqlStr("CARD_INDICATOR");
        sqlSelect(sql1, new Object[] {lsCardNo});
        // 輸入CARD_NO的CARD_INDICATOR欄值
        String cardIndicator = sqlStr("CARD_INDICATOR");
        if (cardIndicator.length() > 0 && cardIndicator.equals(oldCardIndicator)) {
            if (cardIndicator.equals("1")) {
                lsErrorDesc = "一般卡已登錄,不可重覆登錄!!";
                return false;
            }
            if (cardIndicator.equals("2")) {
                lsErrorDesc = "商務卡已登錄,不可重覆登錄!!";
                return false;
            }
            lsErrorDesc = "未知類型卡已登錄,不可重覆登錄!!";
            return false;
        }
    }

    return true;
  }

  // boolean checkDetail(String ls_card_no , String ls_car_no , String ls_id_no){
  // String sql1 = " select "
  // + " count(*) as db_cnt "
  // + " from cms_roaddetail "
  // + " where 1=1 "
  // + " and appl_card_no = ? "
  // + " and apr_date = '' "
  // + sql_col(ls_car_no,"rd_carno")
  // + sql_col(ls_id_no,"rd_carmanid")
  // ;
  // sqlSelect(sql1,new Object[]{ls_card_no});
  // if(sql_num("db_cnt")>0){
  // is_error_desc = "此卡號之車號目前待覆核, 請由異動記錄修改";
  // return false ;
  // }
  // return true;
  // }
  //
  // boolean checkIdno(String ls_id_no){
  // String sql1 = " select "
  // + " count(*) as db_cnt "
  // + " from crd_idno "
  // + " where 1=1 "
  // + " and id_no = ? "
  // ;
  // sqlSelect(sql1,new Object[]{ls_id_no});
  // if(sql_num("db_cnt")==0){
  // is_error_desc = "身分證號不存在";
  // return false ;
  // }
  // return true;
  // }

  void tabClick() {
    String lsClick = "";
    lsClick = wp.itemStr2("tab_click");
    if (eqIgno(wp.buttonCode, "Q"))
      wp.colSet("a_click_1", "");
    if (eqIgno(lsClick, "1")) {
      wp.colSet("a_click_1", "tab_active");
    } else if (eqIgno(lsClick, "2")) {
      wp.colSet("a_click_2", "tab_active");
    } else {
      wp.colSet("a_click_1", "tab_active");
    }
  }

}
