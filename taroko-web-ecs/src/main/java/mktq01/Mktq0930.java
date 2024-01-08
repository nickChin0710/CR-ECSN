/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-12-05  V1.00.00  Zuwei Su   Initial                                                                  *
******************************************************************************/
package mktq01;

import java.io.IOException;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;


public class Mktq0930 extends BaseProc {
  private final static String FILE_NAME = "MKTQ0930_YYYYMMDD.csv";
  private final static String COL_SEPERATOR = ",";
  private final static String LINE_SEPERATOR = System.lineSeparator();
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        dataProcess();
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
  	case "XLS":
		// -CSV-
		csvPrint();
		break;
      default:
        break;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
  }

  private int getWhereStr() throws Exception {
    wp.whereStr = " WHERE 1=1 ";
    String exAcctMonth = wp.itemStr("ex_acct_month");
    String exIdNo = wp.itemStr("ex_id_no");
    String exCardNo = wp.itemStr("ex_card_no");
    if (!empty(exAcctMonth)) {
      wp.whereStr += " and acct_month = :acct_month ";
      setString("acct_month", exAcctMonth);
    }
    if (!empty(exIdNo)) {
        wp.whereStr += " and ID_NO = :id_no ";
        setString("id_no", exIdNo);
      }
    if (!empty(exCardNo)) {
      wp.whereStr += " AND card_no = :card_no ";
      setString("card_no", exCardNo);
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    getWhereStr();
    wp.selectSQL = " acct_month,id_no,card_no,sign_flag,to_char(tx_bonus, '99999999.00') as tx_bonus ";
    wp.daoTable = "mkt_openpoint_data685 ";
    wp.whereOrder = "order by id_no";
  
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      return;
    }

    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }
  
	void csvPrint() throws Exception {
		
		if (!checkApprove(wp.itemStr("approval_user"),wp.itemStr("approval_passwd"))) {		
			wp.respHtml = "TarokoError";
			return;
		}
		String fileName = FILE_NAME.replace("YYYYMMDD", wp.sysDate);
		listWkdata(fileName);
	}

	void listWkdata(String fileName) throws Exception {
		String[] activeCode = wp.itemBuff("active_code");
		String[] activeName = wp.itemBuff("active_name");
		String[] activeType = wp.itemBuff("active_type");
		String[] activeTypeName = wp.itemBuff("active_type_name");
		String[] idNo = wp.itemBuff("id_no");
		String[] chiName = wp.itemBuff("chi_name");
		String[] feedbackDate = wp.itemBuff("feedback_date");
		wp.listCount[0] = idNo.length;

		StringBuffer sb = new StringBuffer();
		sb.append("活動代號");
		sb.append(COL_SEPERATOR);
		sb.append("活動說明");
		sb.append(COL_SEPERATOR);
		sb.append("活動別");
		sb.append(COL_SEPERATOR);
		sb.append("身分證號");
		sb.append(COL_SEPERATOR);
		sb.append("姓名");
		sb.append(COL_SEPERATOR);
		sb.append("回饋日期");
		sb.append(LINE_SEPERATOR);

		for (int i = 0; i < idNo.length; i++) {
			sb.append(activeCode[i]);
			sb.append(COL_SEPERATOR);
			sb.append(activeName[i]);
			sb.append(COL_SEPERATOR);
			sb.append(activeType[i]);
			sb.append(activeTypeName[i]);
			sb.append(COL_SEPERATOR);
			sb.append(idNo[i]);
			sb.append(COL_SEPERATOR);
			sb.append(chiName[i]);
			sb.append(COL_SEPERATOR);
			sb.append(feedbackDate[i]);
			sb.append(LINE_SEPERATOR);
		}
		downLoadFile(sb.toString(), fileName);
	}

	void downLoadFile(String textData, String fileName) throws Exception {

		TarokoFileAccess tf = new TarokoFileAccess(wp);
		int file = tf.openOutputText(fileName, "MS950");
		try {
			 tf.writeTextFile(file, textData + wp.newLine);
			// 檔案下載
			try {
				wp.setDownload(fileName);
			} catch (Exception e) {
			}
		} catch (IOException e) {
			alertErr("文字檔產生失敗");

		} finally {
			 tf.closeOutputText(file);
		}
	}
	

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }

  @Override
  public void dataProcess() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
}
