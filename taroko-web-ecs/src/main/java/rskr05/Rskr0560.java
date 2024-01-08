package rskr05;
/**
 * 2019-1203   JH    UAT
 * 2019-1121:  Alex  bug fixed
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.03  Tanwei       updated for project coding standard
 */

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr0560 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      pdfPrint();
    }
  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    if (itemallEmpty("ex_crt_date1,ex_crt_date2,ex_idno".split(","))) {
      alertErr2("[匯入日期, 身分證ID/統編] 不可全部空白");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("匯入日期 : 起迄錯誤");
      return;
    }
    String lsWhere = " where kind_flag ='A' " 
    		+ sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
            + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
            ;
    
    if (eqIgno(wp.itemStr("ex_annou_type"), "0") == false) {    	
    	lsWhere += sqlCol(wp.itemStr("ex_annou_type"), "annou_type", "like%");
    }

    if (eqIgno(wp.itemStr("ex_id_corp_type"), "1")) {
    	//--corp_p_seqno = '' 才可以排除統編個繳
    	lsWhere += " and corp_p_seqno = '' ";
    	lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
    			+sqlCol(wp.itemStr("ex_idno"),"id_no")
    			+ " ) "
    			;
    } else if (eqIgno(wp.itemStr("ex_id_corp_type"), "2")) {
    	lsWhere += " and corp_p_seqno <> '' ";
    	lsWhere += " and corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "
    			+sqlCol(wp.itemStr("ex_idno"),"corp_no")
    			+ " ) "
    			;
    } else if (eqIgno(wp.itemStr("ex_id_corp_type"), "0")) {    	
    	if (wp.itemEmpty("ex_idno") == false) {        
    		lsWhere += " and ((id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
    				+sqlCol(wp.itemStr("ex_idno"),"id_no")
    				+ " )) or (corp_p_seqno in (select corp_p_seqno from crd_corp where 1=1 "
    				+sqlCol(wp.itemStr("ex_idno"),"corp_no")
    				+ " ))) "
    				;
    	}
    }

    if (eqIgno(wp.itemStr("ex_card_num"), "1")) {
      lsWhere += " and (sup0_card_num+sup1_card_num+corp_card_num) >0 ";
    } else if (eqIgno(wp.itemStr("ex_card_num"), "2")) {
      lsWhere += " and (sup0_card_num+sup1_card_num+corp_card_num) =0 ";
    }

    if (eqIgno(wp.itemStr2("ex_mcode"), "1")) {
      lsWhere += " and m_code >= 1";
    } else if (eqIgno(wp.itemStr2("ex_mcode"), "2")) {
      lsWhere += " and m_code = 0 ";
    }

//    sqlParm.setSqlParmNoClear(true);
//    selectCnt(lsWhere);
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void selectCnt(String lsWhere) {
    String sql1 = " select " + " distinct  decode(A.id_no,'',A.corp_no,A.id_no) as wk_id_corp "
        + " from rsk_bad_annou A join rsk_bad_annou_log B on A.crt_date =B.crt_date and A.from_type =B.from_type and A.id_p_seqno=B.id_p_seqno and A.corp_p_seqno =B.corp_p_seqno and A.major_id_p_seqno =B.major_id_p_seqno "
        + lsWhere;
    sqlSelect(sql1);
    wp.colSet("db_cnt", "" + sqlRowNum);
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " crt_date , decode(corp_p_seqno,'',id_p_seqno,corp_p_seqno) as order_rule , "
        + " from_type , acct_type , "
        + " block_reason||','||block_reason2||','||block_reason3||','||block_reason4||','||block_reason5 as wk_block_reason , "
        + " m_code , " + " sup0_card_num , " + " sup1_card_num , " + " corp_card_num , "
        + " spec_status "
        + ", decode(from_type,'1','支票拒往','3','他行強停-JCIC','4','人工匯入','5','人工登錄','') as tt_from_type , id_p_seqno , corp_p_seqno "
        + ", decode(annou_type,'1','他行強停','2','支票拒往','6','退票','9','轉催收戶') as tt_annou_type , annou_type , proc_reason , card_no "
        ;    
    
    wp.daoTable = " rsk_bad_annou_log ";
    wp.whereOrder = " order by 1 , 2";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setPageValue();
    wp.setListCount(0);
    queryAfter();
  }
  
  void queryAfter() throws Exception {
	  
	  String sql1 = "select corp_no , chi_name from crd_corp where corp_p_seqno = ? ";
	  String sql2 = "select id_no , chi_name from crd_idno where id_p_seqno = ? ";	  
	  String wkIdCorp = "" , chiName = "";
	  
	  for(int ii=0 ; ii <wp.selectCnt ; ii++) {
		  wkIdCorp = "" ; chiName = "";
		  if(wp.colEmpty(ii,"corp_p_seqno") == false) {
			  sqlSelect(sql1,new Object[] {wp.colStr(ii,"corp_p_seqno")});
			  if(sqlRowNum > 0 ) {
				  chiName = sqlStr("chi_name");
				  wkIdCorp = sqlStr("corp_no");
			  }
		  }
		  
		  if(wp.colEmpty(ii,"id_p_seqno") == false) {
			  sqlSelect(sql2,new Object[] {wp.colStr(ii,"id_p_seqno")});
			  if(sqlRowNum > 0 ) {
				  if(chiName.isEmpty()) {
					  chiName = sqlStr("chi_name");
				  }	else	{
					  chiName += " / " + sqlStr("chi_name"); 
				  }
				  
				  if(wkIdCorp.isEmpty()) {
					  wkIdCorp = sqlStr("id_no");
				  }	else	{
					  wkIdCorp += " / " + sqlStr("id_no"); 
				  }				  
			  }
		  }
		  wp.colSet(ii, "chi_name",chiName);
		  wp.colSet(ii, "wk_id_corp",wkIdCorp);
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
  public void pdfPrint() throws Exception {
    wp.reportId = "rskr0560";
    String tlAnnouType = "", cond1 = "";
    cond1 = "匯入日期: " + commString.strToYmd(wp.itemStr("ex_crt_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_crt_date2"));
    if (eqIgno(wp.itemStr("ex_annou_type"), "0")) {
    	tlAnnouType = "全部";
    } else if (eqIgno(wp.itemStr("ex_annou_type"), "1")) {
    	tlAnnouType = "他行強停";
    } else if (eqIgno(wp.itemStr("ex_annou_type"), "2")) {
    	tlAnnouType = "支票拒往";
    } else if (eqIgno(wp.itemStr("ex_annou_type"), "6")) {
    	tlAnnouType = "退票";
    } else if (eqIgno(wp.itemStr("ex_annou_type"), "9")) {
    	tlAnnouType = "轉催收戶";
    }
    wp.colSet("tl_annou_type", tlAnnouType);
    wp.colSet("cond1", cond1);
    wp.colSet("user_id", wp.loginUser);
    wp.pageRows = 99999;
    queryFunc();
    
    if(sqlNotFind()) {
    	wp.respHtml = "TarokoErrorPDF";
    	return;
    }
    
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr0560.xlsx";
    pdf.pageCount = 33;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
