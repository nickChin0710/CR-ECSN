package ccam02;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Ccam5510 extends BaseAction {
	String cellarPhone = "";
	@Override
	public void userAction() throws Exception {
		switch (wp.buttonCode) {
	      case "X":
	        /* 轉換顯示畫面 */
	        strAction = "new";
	        clearFunc();
	        break;
	      case "Q":
	        /* 查詢功能 */
	        strAction = "Q";
	        queryFunc();
	        break;
	      case "R":
	        // -資料讀取-
	        strAction = "R";
	        dataRead();
	        break;
	      case "A":
	        /* 新增功能 */
	        saveFunc();
	        break;
	      case "U":
	        /* 更新功能 */
	        saveFunc();
	        break;
	      case "D":
	        /* 刪除功能 */
	        saveFunc();
	        break;
	      case "M":
	        /* 瀏覽功能 :skip-page */
	        queryRead();
	        break;
	      case "S":
	        /* 動態查詢 */
	        querySelect();
	        break;
	      case "L":
	        /* 清畫面 */
	        strAction = "";
	        clearFunc();
	        break;
	      case "C":
	        // -資料處理-
	        procFunc();
	        break;	      
	      default:
	        break;
	    }

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {

		if(chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
			alertErr("建檔日期: 起迄錯誤");
			return ;
		}
		
		String lsWhere = " where 1=1 "
				+ sqlCol(wp.itemStr("ex_cellar_phone"),"cellar_phone","like%")
				+ sqlCol(wp.itemStr("ex_date1"),"crt_date",">=")
				+ sqlCol(wp.itemStr("ex_date2"),"crt_date","<=")
				;
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr ;
		wp.setQueryMode();
		
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = " cellar_phone , remark , crt_user , crt_date ";
		wp.daoTable = " cca_mobile_black_list ";
		pageQuery();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		wp.setListCount(0);
		wp.setPageValue();
	}

	@Override
	public void querySelect() throws Exception {
		cellarPhone = wp.itemStr("data_k1");
		dataRead();
	}

	@Override
	public void dataRead() throws Exception {
		if(empty(cellarPhone)) {
			cellarPhone = itemkk("cellar_phone");		
		}
		
		if(empty(cellarPhone)) {
			alertErr("手機號碼: 不可空白");
			return ;
		}
		
		wp.selectSQL = "hex(rowid) as rowid , cellar_phone , remark , crt_date , crt_user , mod_user , mod_time , mod_pgm , mod_seqno ";
		wp.daoTable = " cca_mobile_black_list ";
		wp.whereStr = " where 1=1 "
				+sqlCol(cellarPhone,"cellar_phone")
				;
		pageSelect();
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
	}

	@Override
	public void saveFunc() throws Exception {
		
		ccam02.Ccam5510Func func = new Ccam5510Func();
		func.setConn(wp);
		
		func.dbSave(strAction);
		sqlCommit(rc);
		if(rc !=1) {
			alertErr(func.getMsg());
		}	else saveAfter(false);

	}

	@Override
	public void procFunc() throws Exception {
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
		if (fi == -1) {			
		    return;
		}
		
		String lsErrFile = inputFile + "_err" + "_" + wp.sysTime + ".txt";
		TarokoFileAccess oofile = new TarokoFileAccess(wp);
		int liFileNum = -1;				
		
		Ccam5510Func func = new Ccam5510Func();
		func.setConn(wp);

		int llOk = 0, llCnt = 0;
		int llErr = 0;			
		boolean lbErrFile = false;
		String newLine = "\r\n";
		while (true) {			
		    String tmpStr = tf.readTextFile(fi);
		    if (tf.endFile[fi].equals("Y")) {
		      break;
		    }
		    if (tmpStr.length() < 2) {
		      continue;
		    }		    
		    llCnt++;
		    
		    String[] tt = new String[2];
		    tt[0] = tmpStr;
		    tt = commString.token(tt, ",");
		    String lsCellarPhone = tt[1];
		    tt = commString.token(tt, ",");
		    String lsRemark = tt[1];
		    
		    if(lsCellarPhone.isEmpty())
		    	continue ;
		    
		    if(lsCellarPhone.length() > 10) {
		    	if(lbErrFile == false) {
		    		lbErrFile = true; 
		    		liFileNum = oofile.openOutputText(lsErrFile, "MS950");
		    	}
		    	String errorDesc = "";
		    	errorDesc = lsCellarPhone+","+lsRemark+"  錯誤原因:手機長度錯誤"+newLine;
		    	oofile.writeTextFile(liFileNum, errorDesc);
		    	llErr++;
		    	continue;
		    }
		    
		    if(lsRemark.length() > 100)
		    	lsRemark = commString.mid(lsRemark, 0,100);
		    
		    wp.itemSet("cellar_phone", lsCellarPhone);
		    wp.itemSet("remark", lsRemark);
		    
		    if(func.dataProc() == 1) {
		    	llOk ++;
		    	sqlCommit(1);
		    	continue;
		    }	else	{
		    	llErr++;
		    	if(lbErrFile == false) {
		    		lbErrFile = true; 
		    		liFileNum = oofile.openOutputText(lsErrFile, "MS950");
		    	}		    	
		    	String errorDesc = "";
		    	errorDesc = lsCellarPhone+","+lsRemark+"  錯誤原因:"+func.getMsg()+newLine;		    					
				oofile.writeTextFile(liFileNum, errorDesc);				
				continue;
		    }		    		    
		}
		
		if(llErr > 0) {
			oofile.closeOutputText(liFileNum);
			wp.setDownload(lsErrFile);
		}
				
		tf.closeInputText(fi);
		tf.deleteFile(inputFile);
		alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk+" 錯誤筆數:"+ llErr);		
		return;
	}
	
	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0) {		
		    this.btnModeAud();
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
