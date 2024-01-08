/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-05-31  V1.00.00     Ryan                                               *
* 112-07-27  V1.00.01     Ryan     增加三個欄位                                                                                             *
* 112-11-27  V1.00.02     Ryan     調整匯入功能                                                                                             *
* 112-12-11  V1.00.03     Ryan     調整匯入功能 ,查無此統一編號 , 查無此ID 需寫入  CRD_CORRELATE *                                                                                        *
******************************************************************************/
package cycm01;

import busi.SqlPrepare;
import ofcapp.BaseAction;
import taroko.base.CommString;
import taroko.com.TarokoFileAccess;

public class Cycm0070 extends BaseAction {
  String lsMsg = "";
  CommString commStr = new CommString();
  int listCnt = 0;
  String pSeqno = "";
  String corpPSeqno = "";
  String acctType = "";
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
    } else if (eqIgno(wp.buttonCode, "B1")) {
    	strAction = "new";
	  clearFunc();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
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
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_correlate_id"), "CORRELATE_ID");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " CRT_DATE , " 
    	+ " CORRELATE_ID , " 
    	+ " decode(CORRELATE_ID_CODE,'','0',CORRELATE_ID_CODE) as CORRELATE_ID_CODE , " 
    	+ " DECODE(BK_FLAG,'','N',BK_FLAG) as BK_FLAG, "
        + " DECODE(FH_FLAG,'','N',FH_FLAG) as FH_FLAG , "
        + " NON_ASSET_BALANCE , "
        + " P_SEQNO , "
        + " NON_CREDIT_AMT , "
        + " ACCT_TYPE , "
        + " CORP_P_SEQNO , "
        + " RELATE_STATUS , "
        + " MOD_USER , "
        + " MOD_TIME , "
        + " MOD_PGM ";
    wp.daoTable = "CRD_CORRELATE";
    wp.whereOrder = " order by CORRELATE_ID ";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();

    for(int i = 0 ; i < wp.selectCnt ; i++) {
    	String[] colm = {"1","2","3","4","5"};
    	String[] text = {"1.本行員工","2.本行負責人","3.本行主要股東","4.與本行負責人或辦理授信職員有厲害關係者","5.本行持有實收資本額百分之三以上企業"};
    	wp.colSet("TT_RELATE_STATUS", commStr.decode(wp.colStr("RELATE_STATUS"),colm, text));
    }
    
  }

  @Override
  public void querySelect() throws Exception {

    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
	String crtDate = wp.itemStr("data_k1");
	String correlateId = wp.itemStr("data_k2");
	String correlateIdCode = wp.itemStr("data_k3");
	if(empty(crtDate)) {
		crtDate = wp.itemStr("kk_crt_date");
	}
	if(empty(correlateId)) {
		correlateId = wp.itemStr("kk_correlate_id");
	}
	if(empty(correlateIdCode)) {
		correlateIdCode = "0";
	}

    wp.selectSQL = " CRT_DATE , " 
        	+ " CORRELATE_ID , " 
        	+ " decode(CORRELATE_ID_CODE,'','0',CORRELATE_ID_CODE) as CORRELATE_ID_CODE , " 
        	+ " DECODE(BK_FLAG,'','N',BK_FLAG) as BK_FLAG , "
            + " DECODE(FH_FLAG,'','N',FH_FLAG) as FH_FLAG , "
            + " NON_ASSET_BALANCE , "
            + " P_SEQNO , "
            + " NON_CREDIT_AMT , "
            + " ACCT_TYPE , "
            + " CORP_P_SEQNO , "
            + " RELATE_STATUS , "
            + " MOD_USER , "
            + " MOD_PGM, "
        	+ " to_char(mod_time,'yyyymmdd') as mod_date , " 
            + " hex(rowid) as rowid ";
    wp.daoTable = "CRD_CORRELATE";
    wp.whereStr = "where 1=1 " ;
    wp.whereStr += sqlCol(crtDate, "crt_date");
    wp.whereStr += sqlCol(correlateId, "correlate_id");
    wp.whereStr += sqlCol(correlateIdCode, "decode(correlate_id_code,'','0',correlate_id_code)");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2("查無資料");
      return;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    cycm01.Cycm0070Func func = new cycm01.Cycm0070Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub
		if (itemIsempty("zz_file_name")) {
			alertErr2("上傳檔名: 不可空白");
			return;
		}
		fileDataImp();
  }
  
  void fileDataImp() throws Exception {
		TarokoFileAccess tf = new TarokoFileAccess(wp);
		String inputFile = wp.itemStr("zz_file_name");

		int fi = tf.openInputText(inputFile, "MS950");
		if (fi == -1) return;

		int llOk = 0, llCnt = 0, errCnt = 0;
		deleteCrdCorrelate();
		while (true) {
			String line = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) break;

			llCnt++;

			String batchCorrelateId = "";
//			if (line.length() < 8) {
//				setProcessResult(line, "資料長度不符");
//				errCnt++;
//				continue;
//			}

			batchCorrelateId = commStr.left(line.trim(), 10);
//			textData1112 = commStr.mid(line, 10, 2);

			if(getPSeqno(batchCorrelateId) == -1){
				setProcessResult(line, lsMsg);
//				errCnt++;
//				continue;
			}
			if (inserrtCrdCorrelate(batchCorrelateId) == -1) {
				setProcessResult(line, lsMsg);
				errCnt++;
				continue;
			}
			
			if(llCnt % 1000 == 0) {
				this.sqlCommit(1);
			}
			
//			setProcessResult(llCnt, line, "");
			llOk++;
			
		}
		
		wp.selectCnt = listCnt;
		wp.setListCount(1);
		
		String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ", llCnt, llOk, errCnt);
		wp.showLogMessage("I", "", finalResult);
		wp.alertMesg(finalResult);
		tf.closeInputText(fi);
		wp.colSet("zz_file_name", "");

	}
  
	private void setProcessResult(String batchCorrelateId, String batchErrorMsg) {
		wp.colSet(listCnt, "batch_ser_num", listCnt+1);
		wp.colSet(listCnt, "batch_correlate_id", batchCorrelateId);
		wp.colSet(listCnt, "batch_error_msg", batchErrorMsg);
		listCnt++;
	}
	
	private int getPSeqno(String batchCorrelateId) {
		pSeqno = "";corpPSeqno = "";acctType = "";
		acctType = "03";
    	if(batchCorrelateId.length() == 8) {
        	String sqlSelect = "SELECT CORP_P_SEQNO FROM CRD_CORP WHERE CORP_NO = :CORP_NO ";
        	setString("CORP_NO",batchCorrelateId);
        	sqlSelect(sqlSelect);
        	if(sqlRowNum > 0) {
        		corpPSeqno = sqlStr("acno_p_seqno");
            }else {
            	lsMsg = "查無此統一編號 ";
    			return -1;
            }
    	}
    	if(batchCorrelateId.length() != 8) {
        	acctType = "01";
    		String sqlSelect = "SELECT p_seqno FROM ACT_ACNO WHERE acct_type = '01' AND acct_key = :acct_key and corp_p_seqno = '' ";
            setString("acct_key", batchCorrelateId.length() == 10 ? batchCorrelateId + "0" : batchCorrelateId);
            sqlSelect(sqlSelect);
            if(sqlRowNum > 0) {
            	pSeqno = sqlStr("p_seqno");
            }else {
            	lsMsg = "查無此ID";
    			return -1;
            }
    	}
    	return 1;
	}
	
	private int inserrtCrdCorrelate(String batchBarcodeNum) {
//		String sqlSelect = "select count(*) as crd_cnt from CRD_CORRELATE where CORRELATE_ID = ? and correlate_id_code = 'X' ";
//		setString(1,batchBarcodeNum);
//		sqlSelect(sqlSelect);
//		int crdReturnCnt = sqlInt("crd_cnt");
//		if(crdReturnCnt > 0) {
//			busi.SqlPrepare upsp = new SqlPrepare();
//			upsp.sql2Update("CRD_CORRELATE");
//			upsp.ppstr("CRT_DATE", wp.sysDate);
//			upsp.ppstr("CORRELATE_ID", batchBarcodeNum);
//			upsp.ppstr("BK_FLAG", "");
//			upsp.ppstr("FH_FLAG", "Y");
//			upsp.ppstr("NON_ASSET_BALANCE", "0");
//			upsp.ppstr("P_SEQNO", pSeqno);
//			upsp.ppstr("NON_CREDIT_AMT", "0");
//			upsp.ppstr("CORP_P_SEQNO", corpPSeqno);
//			upsp.ppstr("ACCT_TYPE", batchBarcodeNum.trim().length()==10?"01":"03");
//			upsp.addsql(" , mod_time = sysdate ");
//			upsp.ppstr("mod_user", wp.loginUser);
//			upsp.ppstr("mod_pgm", wp.modPgm());
//			upsp.sql2Where(" where CORRELATE_ID = ? and correlate_id_code = 'X' ", batchBarcodeNum);
//			sqlExec(upsp.sqlStmt(), upsp.sqlParm());
//		}else {
			busi.SqlPrepare sp = new SqlPrepare();
			sp.sql2Insert("CRD_CORRELATE");
			sp.ppstr("CRT_DATE", wp.sysDate);
			sp.ppstr("CORRELATE_ID", batchBarcodeNum);
			sp.ppstr("CORRELATE_ID_CODE", "X");
			sp.ppstr("BK_FLAG", "");
			sp.ppstr("FH_FLAG", "Y");
			sp.ppstr("NON_ASSET_BALANCE", "0");
			sp.ppstr("P_SEQNO", pSeqno);
			sp.ppstr("NON_CREDIT_AMT", "0");
			sp.ppstr("CORP_P_SEQNO", corpPSeqno);
			sp.ppstr("ACCT_TYPE", batchBarcodeNum.trim().length()==10?"01":"03");
			sp.addsql(", mod_time ", ", sysdate ");
			sp.ppstr("mod_user", wp.loginUser);
			sp.ppstr("mod_pgm", wp.modPgm());
			sqlExec(sp.sqlStmt(), sp.sqlParm());
//		}
		if (sqlRowNum <= 0) {
			lsMsg = "匯入失敗";
			return -1;
		}
		return 1;
	}
	
	void deleteCrdCorrelate() {
		String sqlCmd = " delete CRD_CORRELATE where correlate_id_code = 'X' ";
		sqlExec(sqlCmd);
	}

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "cycm0070_detl")) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
