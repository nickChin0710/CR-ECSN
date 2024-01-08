package ccam01;

/** 預製卡啟用
 * 2021/11/08 V1.00.00   ryan   program initial 
 * 2021/11/15 V1.00.01   ryan   增加欄位 
 * 2022/03/01 V1.00.02   Justin add a function to process data in the file
 * 2022/04/06 V1.00.03   Justin add birthday and cellar_phone in the activation page
 * 2022/04/07 V1.00.04   Justin select birthday and cellar_phone
 * 2022/04/11 V1.00.05   Justin 批次啟用預製卡新增生日及手機欄位
 * */

import ofcapp.BaseAction;
import taroko.base.CommDate;
import taroko.com.TarokoFileAccess;

public class Ccam2081 extends BaseAction {
	String kkBatchno = "", kkRecno = "" , kkCardNo = "";

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
		} else if (eqIgno(wp.buttonCode, "UPLOAD")) {
			procFunc();
		} else if (eqIgno(wp.buttonCode, "BB")) {
			/* 清畫面 */
			strAction = "";
			clearFunc();
		}

	}

	@Override
	public void dddwSelect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryFunc() throws Exception {

	    // -page control-
	    wp.queryWhere = wp.whereStr;
	    wp.setQueryMode();
	    queryRead();

	}

	void getWhere() {
		wp.whereStr = " where 1=1 and apply_source = 'P' ";

		if (empty(wp.itemStr("ex_card_no")) == false) {
			wp.whereStr += " and  card_no = :ex_card_no ";
			setString("ex_card_no", wp.itemStr("ex_card_no"));
		}
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();

		getWhere();

		wp.selectSQL = "batchno" + ", recno" + ", card_no" + ", act_no" + ", group_code" + ", card_type"
				+ ", valid_to" + ", in_main_date" + ", decode(prefab_cancel_flag,'','N',prefab_cancel_flag) as prefab_cancel_flag " + ", sup_flag"
				+ ", unit_code" + ", bin_no" + ", acct_type" + ", valid_fm" + ", class_code" 
				+ ", source_code" + ", card_ref_num" + ", apply_id";

		wp.daoTable = "dbc_emboss";
		wp.whereOrder = " ";
		pageQuery();

		if (sqlNotFind()) {
			alertErr("此條件查無資料");
			return;
		}
		wp.setListCount(1);
		listWkdata(wp.selectCnt);
		wp.setPageValue();
	}

	void listWkdata(int llRow) {

	}

	@Override
	public void querySelect() throws Exception {
		kkBatchno = wp.itemStr("data_k1");
		kkRecno = wp.itemStr("data_k2");
		kkCardNo = wp.itemStr("data_k3");
		dataRead();

	}

	@Override
	public void dataRead() throws Exception {

		wp.selectSQL = "batchno" + ", recno" + ", card_no" + ", act_no" + ", group_code" + ", card_type"
				+ ", valid_to" + ", in_main_date" + ", decode(prefab_cancel_flag,'','N',prefab_cancel_flag) as prefab_cancel_flag " + ", sup_flag"
				+ ", unit_code" + ", bin_no" + ", acct_type" + ", valid_fm" + ", class_code" 
				+ ", source_code" + ", card_ref_num" + ", apply_id, BIRTHDAY, CELLAR_PHONE ";

		wp.daoTable = "dbc_emboss";
		wp.whereStr = "where 1=1" 
		+ sqlCol(kkBatchno, "batchno") 
		+ sqlCol(kkRecno, "recno");

		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, key=" + kkCardNo);
			return;
		}

	}

	@Override
	public void saveFunc() throws Exception {

	    // -check approve-
	    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
	      return;
	    }
		
		ccam01.Ccam2081Func func = new ccam01.Ccam2081Func();
		func.setConn(wp);

		rc = func.dbSave(strAction);
		sqlCommit(rc);
		if (rc != 1) {
			errmsg(func.getMsg());
			return;
		}

		this.saveAfter(false);
		wp.respMesg = "啟用成功";
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
		if (fi == -1) return;
				
		ccam01.Ccam2081Func func = new ccam01.Ccam2081Func();
		func.setConn(wp);

		CommDate commDate = new CommDate();
		int llOk = 0, llCnt = 0, errCnt = 0;
		while (true) {
			String line = tf.readTextFile(fi);
			if (tf.endFile[fi].equals("Y")) break;

			llCnt ++;
			
			// split columns
			boolean isValidFormat = false;
			if (line.indexOf(",") != -1) {
				func.resetMsg();
				String inputCardNo = "";
				String inputIdNo = "";
				String inputActNo = "";
				String inputBirthday = "";
				String inputCellarPhone = "";
				String[] strArr = line.split(",");
				if (strArr.length == 5) {
					inputCardNo = strArr[0].trim();
					inputIdNo = strArr[1].trim();
					inputActNo = strArr[2].trim();
					inputBirthday = strArr[3].trim();
					inputCellarPhone = strArr[4].trim();
					
					isValidFormat = true;
					
					// 2022/04/11 Justin: check required inputs
					if (inputCardNo == null || inputCardNo.trim().isEmpty()) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入卡號不可為空");
						errCnt ++;
						continue;
					}
					if (inputIdNo == null || inputIdNo.trim().isEmpty()) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入身分證號碼不可為空");
						errCnt ++;
						continue;
					}
					if (inputActNo == null || inputActNo.trim().isEmpty()) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入金融帳號不可為空");
						errCnt ++;
						continue;
					}
					if (inputBirthday == null || inputBirthday.trim().isEmpty()) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入生日不可為空");
						errCnt ++;
						continue;
					}
					if (commDate.isDate(inputBirthday) == false) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入生日格式錯誤");
						errCnt ++;
						continue;
					}
					if (inputCellarPhone.length() > 15) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "輸入手機長度不可超過15");
						errCnt ++;
						continue;
					}
					
					// check others
					if (isActNoInDbaAcno(inputActNo)) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "金融帳號已存在帳戶資料檔");
						errCnt ++;
						continue;
					}
					
					int selectCnt = selectDbcEmboss(inputCardNo, inputIdNo, inputActNo);
					if (selectCnt <= 0) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "selectDbcEmboss找不到資料");
						errCnt ++;
						continue;
					}
					
					boolean isUpdateAny = false;
					for (int i = 0; i < selectCnt; i++) {
						/** Required parameters in the web page **/
						wp.itemSet("card_no", inputCardNo); // inputCardNo
						wp.itemSet("act_no", inputActNo);  // inputActNo
						wp.itemSet("apply_id", inputIdNo); // inputIdNo
						wp.itemSet("birthday", inputBirthday); // inputBirthday
						wp.itemSet("cellar_phone", inputCellarPhone); // inputCellarPhone
						
						/** Other parameters in the web page **/
						wp.itemSet("batchno", wp.colStr(i, "batchno"));
						wp.itemSet("recno", wp.colStr(i, "recno"));
						wp.itemSet("group_code", wp.colStr(i, "group_code"));
						wp.itemSet("card_type", wp.colStr(i, "card_type"));
						wp.itemSet("valid_to", wp.colStr(i, "valid_to"));
						wp.itemSet("in_main_date", wp.colStr(i, "in_main_date"));
						wp.itemSet("prefab_cancel_flag", wp.colStr(i, "prefab_cancel_flag"));
						wp.itemSet("sup_flag", wp.colStr(i, "sup_flag"));
						wp.itemSet("unit_code", wp.colStr(i, "unit_code"));
						wp.itemSet("bin_no", wp.colStr(i, "bin_no"));
						wp.itemSet("acct_type", wp.colStr(i, "acct_type"));
						wp.itemSet("valid_fm", wp.colStr(i, "valid_fm"));
						wp.itemSet("class_code", wp.colStr(i, "class_code"));
						wp.itemSet("source_code", wp.colStr(i, "source_code"));
						wp.itemSet("card_ref_num", wp.colStr(i, "card_ref_num"));

						rc = func.dbSave("U");
						sqlCommit(rc);
						if (rc == 1) {
							isUpdateAny = true;
						}
					}
					
					if (isUpdateAny == false) {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, func.getMsg());
						errCnt ++;
						continue;
					}else {
						setProcessResult(llCnt, inputCardNo, inputIdNo, inputActNo, inputBirthday, inputCellarPhone, "");
						llOk++;
					}
					
				}	
			}
			
			
			if (isValidFormat == false) {
				setProcessResult(llCnt, "", "", "", "", "", String.format("資料格式不符[%s]", line));
				errCnt ++;
				continue;	
			}

		}
		
		wp.selectCnt = llCnt;
		wp.setListCount(1);
		
		String finalResult = String.format("資料匯入處理筆數[%d], 成功筆數[%d], 錯誤筆數[%d] ", llCnt, llOk, errCnt);
		wp.showLogMessage("I", "", finalResult);
		wp.colSet("finalResult", finalResult);
		tf.closeInputText(fi);
		
		wp.colSet("zz_file_name", "");
		
		wp.alertMesg = "";
		
	}

	private void setProcessResult(int llCnt, String inputCardNo, String inputIdNo, String inputActNo, 
			String inputBirthday, String inputCellarPhone, String errorReason) {
		wp.colSet(llCnt-1, "rs_SER_NUM", llCnt);
		wp.colSet(llCnt-1, "rs_card_no", inputCardNo);
		wp.colSet(llCnt-1, "rs_apply_id", inputIdNo);
		wp.colSet(llCnt-1, "rs_act_no", inputActNo);
		wp.colSet(llCnt-1, "rs_birthday", inputBirthday);
		wp.colSet(llCnt-1, "rs_cellar_phone", inputCellarPhone);
		wp.colSet(llCnt-1, "rs_error_reason", errorReason);
		wp.colSet(llCnt-1, "rs_ok_flag", errorReason.trim().isEmpty() ? "V" : "X");
	}
	
	private boolean isActNoInDbaAcno(String actNo) {
		String sql = " select count(*) cnt from dba_acno where acct_no = ? ";
		setString(1, actNo);
		sqlSelect(sql);
		if (wp.colInt("cnt") > 0) {
			return true;
		}
		return false;
	}

	private int selectDbcEmboss(String cardNo, String idNo, String actNo) {
		wp.selectSQL = 
		new StringBuilder("batchno, recno, card_no, act_no, group_code, card_type, valid_to ")
		.append(", in_main_date, decode(prefab_cancel_flag,'','N',prefab_cancel_flag) as prefab_cancel_flag , sup_flag")
		.append(", unit_code, bin_no, acct_type, valid_fm, class_code, source_code, card_ref_num, apply_id").toString();
		wp.daoTable = "dbc_emboss";
		wp.whereStr = "where apply_source = 'P' AND in_main_date = '' AND prefab_cancel_flag != 'Y' AND VALID_TO >= to_char(sysdate, 'yyyymmdd')  " 
		            + sqlCol(cardNo, "card_no");
		pageSelect();
		return sqlRowNum;
	}

	@Override
	public void initButton() {
		if (eqIgno(wp.respHtml, "ccam2081_detl")) {
			btnModeAud("XX");
		}

	}

	@Override
	public void initPage() {
		// TODO Auto-generated method stub

	}

}
