package ccar01;
/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20  V1.00.01  Zhenwu Zhu      updated for project coding standard                           *
*  110-01-14  V1.00.02  Justin                    fix parameterize sql bugs
 *  2023-1205     JH    ++crt_user
*/

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Ccar0210 extends BaseAction implements InfacePdf {
  String hhIdPseqno = "", hhIdPseqno2 = "", lsPSeqno = "";
  String lsCardAcctIdx = "";

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
    } else if (eqIgno(wp.buttonCode, "C")) {
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
    if (empty(wp.itemStr("ex_date1")) && empty(wp.itemStr("ex_date2"))) {
      alertErr2("交易日期不可空白");
      return;
    }
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("交易日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " ;
    
	if (!empty(wp.itemStr("ex_idno"))) {
		if (selectIdPseqno() == false) {
			errmsg("身分證字號輸入錯誤 !");
			return;
		}
//		lsWhere += sqlCol(lsCardAcctIdx, "card_acct_idx");
		
		if(!empty(hhIdPseqno) && !empty(hhIdPseqno2)) {
			lsWhere += " and card_no in (select card_no from cca_card_base where (debit_flag <> 'Y' "
					+ sqlCol(hhIdPseqno,"id_p_seqno")
					+ " ) or (debit_flag ='Y' "
					+ sqlCol(hhIdPseqno2,"id_p_seqno")
					+ " )) ";
		}	else if(!empty(hhIdPseqno)) {
			lsWhere += " and card_no in (select card_no from cca_card_base where debit_flag <> 'Y' "
					+ sqlCol(hhIdPseqno,"id_p_seqno")
					+ " ) ";
		}	else if(!empty(hhIdPseqno2)) {
			lsWhere += " and card_no in (select card_no from cca_card_base where debit_flag = 'Y' "
					+ sqlCol(hhIdPseqno2,"id_p_seqno")
					+ " ) ";
		}
		
	}
    
       lsWhere +=
            sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=")
        + sqlCol(wp.itemStr("ex_card_no"), "card_no")
        + sqlCol(wp.itemStr("ex_msg_type"), "msg_type")
        + sqlCol(wp.itemStr("ex_cellar_phone"),"cellar_phone")
        ;

    // --msg_type , AUTO:刷卡簡訊 , EC:網路非3D交易累積簡訊 ,OPEN

    sqlParm.setSqlParmNoClear(true);
    sum(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  boolean selectIdPseqno() {
    String sql1 = "select " + " uf_idno_pseqno(:id_no) as hh_id_p_seqno ,"
        + " uf_vd_idno_pseqno(:id_no) as hh_id_p_seqno2 " + " from dual ";
    setString("id_no", wp.itemStr("ex_idno"));
    sqlSelect(sql1);
    hhIdPseqno = sqlStr("hh_id_p_seqno");
    hhIdPseqno2 = sqlStr("hh_id_p_seqno2");

    if (empty(hhIdPseqno) && empty(hhIdPseqno2)) {
      return false;
    }

//    String sql2 = " select " + " card_acct_idx " + " from cca_card_acct " + " where 1=1 ";
//
//    if (empty(hhIdPseqno)) {
//      sql2 += " and id_p_seqno = ?";
//      sqlSelect(sql2, new Object[] {hhIdPseqno2});
//    } else if (empty(hhIdPseqno2)) {
//      sql2 += " and id_p_seqno = ? ";
//      sqlSelect(sql2, new Object[] {hhIdPseqno});
//    } else {
//      sql2 += " and id_p_seqno in (?,?)";
//      sqlSelect(sql2, new Object[] {hhIdPseqno, hhIdPseqno2});
//    }
//
//    if (sqlRowNum <= 0)
//      return false;
//
//    lsCardAcctIdx = sqlStr("card_acct_idx");

    return true;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = "" + " tx_date , " + " tx_time , " + " card_no , " + " auth_no , "
        + " trans_type , " + " uf_idno_id2(card_no,'') as id_no , " + " mcht_no , " + " msg_type , "
        + " decode(msg_type,'AUTO','刷卡簡訊','SPEC1','特殊消費簡訊一','SPEC2','特殊消費簡訊二') as tt_mag_type , "
        + " msg_id , " + " entry_mode , " + " risk_type , "
        + " uf_tt_risk_type(risk_type) as tt_risk_type , " + " trans_amt , " + " decode(msg_type,'AUTO2','Y','') as tt_msg_type , "
        + " '' as birthday , " + " chi_name , " + " cellar_phone , " + " iso_resp_code , "
        + " send_date , " + " proc_code , " + " '' as acct_type , " + " '' as vip_code , "
        + " card_acct_idx , " + " uf_acno_key2(card_no,'') as acct_key , appr_pwd , msg_resp_date , msg_resp_time , sms_content "
    +", crt_user"
    ;
    wp.daoTable = "cca_msg_log";
    wp.whereOrder = " order by 1 Desc , 2 Desc ";

    logSql();
    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setPageValue();
  }



  void queryAfter() {

    String sql2 = " select " + " uf_idno_id(major_id_p_seqno) as acct_key " + " from crd_card "
        + " where card_no = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
//      if(commString.strIn(wp.colStr(ii,"proc_code"), "|0|1|2|4")) {
//    	  wp.colSet(ii,"proc_code", "簡訊已送達三竹");
//      }	else {
//    	  wp.colSet(ii,"proc_code", "送簡訊失敗");
//      }
    	
      wp.colSet(ii,"tt_proc_code", smsStatus(wp.colStr(ii,"proc_code")));
    	
//      String sql1 = "select debit_flag , acno_p_seqno " + " from cca_card_acct " + " where 1=1 "
//          + " and CARD_ACCT_IDX = ? ";
//      sqlSelect(sql1, new Object[] {wp.colNum(ii, "card_acct_idx")});      
//      if (sqlRowNum > 0) {
//        lsPSeqno = sqlStr("acno_p_seqno");
//        if (this.eqAny(sqlStr("debit_flag"), "Y")) {
//          selectDbaAcno(lsPSeqno, ii);
//        } else {
//          selectActAcno(lsPSeqno, ii);
//        }
//      }
//
//      sqlSelect(sql2, new Object[] {wp.colStr(ii, "card_no")});
//      if (sqlRowNum > 0) {
//    	if(sqlStr("acct_key").length() >= 11) {
//    		wp.colSet(ii, "acct_key", sqlStr("acct_key"));
//    	} else {
//    		wp.colSet(ii, "acct_key", sqlStr("acct_key")+"0");
//    	}
//      }

    }
    wp.notFound = "N";
  }
  
  String smsStatus(String code) {
	  String tempString = "";
	  
	  switch (code) {
	  	case "0":
	  		tempString = "預約傳送中";
	  		break;
	  	case "1":
	  		tempString = "已送達業者";
	  		break;
	  	case "2":
	  		tempString = "已送達業者";
	  		break;
	  	case "4":
	  		tempString = "已送達手機";
	  		break;
	  	case "5":
	  		tempString = "內容有錯誤";
	  		break;
	  	case "6":
	  		tempString = "門號有錯誤";
	  		break;
	  	case "7":
	  		tempString = "簡訊已停用";
	  		break;
	  	case "8":
	  		tempString = "逾時無送達";
	  		break;
	  	case "9":
	  		tempString = "預約已取消";
	  		break;
	  	default:
	  		tempString = "";
	  		break;
	  }
		
	  
	  return tempString;
  }
  
  void sum(String lsWhere) {

    wp.selectSQL = " sum(decode(proc_code,'0',1,'1',1,'2',1,'4',1,0)) as db_1 , count(*) as db_cnt ";
    wp.daoTable = "cca_msg_log";
    wp.whereStr = lsWhere;
    pageSelect();
    if (sqlRowNum < 0) {
      wp.colSet("db_cnt", "0");
      wp.colSet("db_1", "0");
      wp.colSet("db_2", "0");
      return ;
    }
    wp.colSet("db_2", wp.colNum("db_cnt") - wp.colNum("db_1"));
  }

  void selectDbaAcno(String pSeqno, int ii) {
    String sql1 = "select A.acct_type,  B.birthday, B.chi_name , A.vip_code "
        + " from dba_acno A , dbc_idno B " + " where A.id_p_seqno =B.id_p_seqno "
        + " and A.p_seqno = ? ";
    sqlSelect(sql1, new Object[] {pSeqno});
    if (sqlRowNum <= 0)
      return;
    wp.colSet(ii, "acct_type", sqlStr("acct_type"));
    wp.colSet(ii, "birthday", sqlStr("birthday"));
    wp.colSet(ii, "chi_name", sqlStr("chi_name"));
    wp.colSet(ii, "vip_code", sqlStr("vip_code"));
  }

  void selectActAcno(String pSeqno, int ii) {
    String sql1 = "select A.acct_type,  B.birthday, B.chi_name , A.vip_code "
        + " from act_acno A, crd_idno B " + " where A.id_p_seqno =B.id_p_seqno "
        + " and A.acno_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {pSeqno});
    if (sqlRowNum <= 0)
      return;
    wp.colSet(ii, "acct_type", sqlStr("acct_type"));
    wp.colSet(ii, "birthday", sqlStr("birthday"));
    wp.colSet(ii, "chi_name", sqlStr("chi_name"));
    wp.colSet(ii, "vip_code", sqlStr("vip_code"));
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
	wp.reportId = "Ccar0210";

	String cond1 = "";
	wp.colSet("cond1", cond1);
	wp.colSet("user_id", wp.loginUser);
	wp.pageRows = 9999;
	queryFunc();

	TarokoPDF pdf = new TarokoPDF();
	wp.fileMode = "Y";
	pdf.excelTemplate = "ccar0210.xlsx";
	pdf.pageCount = 30;
	pdf.sheetNo = 0;
	pdf.procesPDFreport(wp);
	pdf = null;	
}

}
