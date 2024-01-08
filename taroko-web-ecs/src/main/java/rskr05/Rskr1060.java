package rskr05;
/**
 * 2019-0624:  JH    p_xxx >>acno_p_xxx
 * 109-04-28  V1.00.01  Tanwei       updated for project coding standard
 * 110-01-06  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
 * 111-10-09  V1.00.04  Alex         增加欄位讀取欄位解圈日期 , 更正解圈管道欄位定義 , 沖正解圈僅顯示原始交易 
 * 111-11-16  V1.00.05  Alex         篩選條件增加請款比對系統解圈 , 解圈扣帳的解圈日期改為 cca_auth_txlog.mtch_date
 * */
import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr1060 extends BaseAction implements InfacePdf {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
      strAction = "XLS";
      // xlsPrint();
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
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("交易日期: 起迄錯誤");
      return;
    }
    String lsWhere = "";
    
    //--沖銷交易不顯示故加上 trans_type <> '0420'
    lsWhere = " where 1=1 and trans_type <> '0420' " 
    		+ sqlCol(wp.itemStr("ex_date1"), "A.tx_date", ">=")
    		+ sqlCol(wp.itemStr("ex_date2"), "A.tx_date", "<=")
    		+ sqlCol(wp.itemStr("ex_card_no"),"A.card_no","like%")
    		;
    
    if(wp.itemEq("ex_unlock_flag", "0")) {    	
    	lsWhere += " and A.unlock_flag not in ('N','') ";
    } else if(wp.itemEq("ex_unlock_flag", "E")) {
    	//--到期未請款解圈再細分到期未請款解圈和請款比對系統解圈
    	lsWhere += " and A.unlock_flag = 'E' and B.abstract_code in ('','01') ";
    } else if(wp.itemEq("ex_unlock_flag", "M")) {
    	lsWhere += " and A.unlock_flag = 'M' ";
    } else if(wp.itemEq("ex_unlock_flag", "R")) {
    	lsWhere += " and A.unlock_flag = 'R' ";
    } else if(wp.itemEq("ex_unlock_flag", "Y")) {
    	lsWhere += " and A.unlock_flag = 'Y' ";
    } else if(wp.itemEq("ex_unlock_flag", "E2")) {
    	//--新增篩選條件請款比對系統解圈
    	lsWhere += " and A.unlock_flag = 'E' and B.abstract_code ='02' ";
    }
    
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();


  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    //--2022/10/09:讀取解圈日期
    //--人工解圈日期、沖正交易:cca_auth_txlog.chg_date 批次解圈日期: dba_deduct_txn.deduct_date 解圈扣帳:mtch_date
    wp.selectSQL = "A.tx_date ,A.tx_time ,A.card_no , decode(A.tx_currency,'901','台幣','840','美金','392','日幣',A.tx_currency) as tx_currency ,"
    			 + "A.nt_amt ,A.auth_no ,A.mtch_flag , A.unlock_flag , "
    			 + "decode(A.unlock_flag,'M','人工解圈','E','到期未請款解圈','R','沖正解圈','Y','解圈扣帳',A.unlock_flag) as tt_unlock_flag , "
    			 + "decode(A.unlock_flag,'M',A.chg_date,'R',A.chg_date,'Y',A.mtch_date,(select max(C.deduct_date) from dba_deduct_txn C where C.card_no=B.card_no and C.reference_no = B.reference_no and C.tx_seq = B.tx_seq)) as unlock_date , "
    			 + "B.from_code , B.abstract_code "
    			 ;
    wp.daoTable = "cca_auth_txlog A left join dba_deduct_txn B on A.card_no = B.card_no and A.ref_no = B.reference_no and A.tx_seq = B.tx_seq ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();
    queryAfter();
  }
  
  //--2022/10/09:增加解圈管道欄位定義
  //--dba_deduct_txn.from_code ='1' => 解圈扣帳 , 但是 unlock_flag ='Y' 故不用在此細分
  //--dba_deduct_txn.from_code ='2' 且 dba_deduct_txn.abstract_code = '01' => 到期未請款解圈
  //--dba_deduct_txn.from_code ='2' 且 dba_deduct_txn.abstract_code = '02' => 請款比對系統解圈
  void queryAfter() throws Exception {
	  //--逐筆判斷條件回填解圈管道中文說明
	  int row = wp.selectCnt ;
	  for(int ii=0;ii<row;ii++) {
		  if(wp.colEq(ii,"unlock_flag","E")) {
			  if(wp.colEq(ii, "from_code","2")) {
				  if(wp.colEq(ii, "abstract_code","01")) {
					  wp.colSet(ii,"tt_unlock_flag", "到期未請款解圈");
				  } else if(wp.colEq(ii, "abstract_code","02")) {
					  wp.colSet(ii,"tt_unlock_flag", "請款比對系統解圈");
				  }				  
			  }
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
  public void pdfPrint() throws Exception {
    wp.reportId = "Rskr1060";
    wp.pageRows = 9999;
    String cond1;
    cond1 = "交易日期: " + commString.strToYmd(wp.itemStr("ex_date1")) + " -- "
        + commString.strToYmd(wp.itemStr("ex_date2"));
    wp.colSet("cond1", cond1);
    queryFunc();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.excelTemplate = "rskr1060.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;

  }

}
