/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 112-05-05  V1.00.00  ryan       program initial                            *
* 112-05-09  V1.00.01  ryan       增加覆核功能檢核與產生媒體檔                                                                  *
* 112-06-25  V1.00.02  Zuwei Su   活動代號dropdown做distinct處理                                                                  *
******************************************************************************/
package mktq01;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;


public class Mktq0925 extends BaseProc {
  private final static String FILE_NAME = "MKTQ0925_YYYYMMDD.csv";
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
    try {
      wp.initOption = "--";
      wp.optionKey = wp.itemStr("ex_active_code");
      dddwList("dddw_active_code", " MKT_GOLDBILL_LIST A,MKT_GOLDBILL_PARM B", "distinct A.ACTIVE_CODE||'_'||B.ACTIVE_NAME", "",
          " WHERE 1=1 AND A.ACTIVE_CODE=B.ACTIVE_CODE AND A.ACTIVE_TYPE=B.ACTIVE_TYPE order by A.ACTIVE_CODE||'_'||B.ACTIVE_NAME");
    } catch (Exception ex) {
    }

  }

  private int getWhereStr() throws Exception {
    wp.whereStr = " WHERE 1=1 ";
    String[] splitStr = wp.itemStr("ex_active_code").split("_");
    String exActiveCode = "";
    String exActiveName = "";
    if(splitStr.length > 1) {
    	exActiveCode = splitStr[0];
    	exActiveName = splitStr[1];
    }
    
    if (!empty(exActiveCode)) {
      wp.whereStr += " and A.ACTIVE_CODE = :ex_active_code ";
      setString("ex_active_code", exActiveCode);
    }
    
    if (!empty(exActiveName)) {
        wp.whereStr += " and B.ACTIVE_NAME = :ex_active_name ";
        setString("ex_active_name", exActiveName);
      }
    if (!empty(wp.itemStr("ex_id_no"))) {
      wp.whereStr += " AND A.ID_NO = :ex_id_no ";
      setString("ex_id_no", wp.itemStr("ex_id_no"));

    }
    if (!empty(wp.itemStr("ex_active_type"))) {
      wp.whereStr += " AND A.ACTIVE_TYPE = :ex_active_type ";
      setString("ex_active_type", wp.itemStr("ex_active_type"));
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
    wp.selectSQL = " A.ACTIVE_CODE,B.ACTIVE_NAME,A.ACTIVE_TYPE,A.ID_NO,C.CHI_NAME,A.FEEDBACK_DATE ";
    wp.selectSQL += " ,DECODE(A.ACTIVE_TYPE,'1','.新申辦電子帳單','2','.全新戶-自動扣繳','') AS ACTIVE_TYPE_NAME ";
    wp.daoTable = "MKT_GOLDBILL_LIST A LEFT JOIN MKT_GOLDBILL_PARM B ON A.ACTIVE_CODE=B.ACTIVE_CODE AND A.ACTIVE_TYPE = B.ACTIVE_TYPE ";
    wp.daoTable += " LEFT JOIN CRD_IDNO C ON A.ID_P_SEQNO = C.ID_P_SEQNO  ";
    wp.whereOrder = "";
  
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
