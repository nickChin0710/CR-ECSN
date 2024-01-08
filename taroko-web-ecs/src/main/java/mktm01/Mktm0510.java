/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/12/16  V1.00.01   Machao      Initial                              *
***************************************************************************/
package mktm01;

import java.text.SimpleDateFormat;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0510 extends BaseEdit {
  private final String PROGNAME = "推廣人員維護(匯入)程式111/12/16 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm02.Mktm1050Func func = null;
  String rowid;
  String orgTabName = "CRD_EMPLOYEE_A_T";
  String controlTaName = "";
  int qFrom = 0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
  int llOK = 0, llCnt = 0 ,llErr = 0, errs = 0;
  int[] datachkCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  String[] uploadFileCol = new String[50];
  String[] uploadFileDat = new String[50];
  String[] logMsg = new String[20];
  String[] passStr = {"", "", "", "", "", "", "", "", "", "", ""};

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {/* 匯入檔案 */
      procUploadFile(0);
      checkButtonOff();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
	  if (wp.itemEmpty("ex_corp_no")) {
          alertErr("請選擇金控公司");
          wp.colSet("corp_no_pink", "pink ");
          return;
        }
      wp.whereStr = "WHERE 1=1 "
    	+ sqlCol(wp.itemStr("ex_corp_no"), "a.corp_no");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTaName = wp.colStr("org_tab_name");
    else
      controlTaName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " " + "hex(a.rowid) as rowid, " + "a.employ_no, "
        + "a.chi_name," + "a.unit_no," + "a.subunit_no," + "a.subunit_name," + "a.status_id,"
        + "a.status_name," + "a.subsidiary_no," + "a.apr_flag," + "a.error_code," + "a.error_desc";

    wp.daoTable = controlTaName + " a ";
    wp.whereOrder = " " + " order by a.corp_no desc";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setPageValue();
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {

  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
  }

  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm01.Mktm0510Func func = new mktm01.Mktm0510Func();

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
	    try {
	      if ((wp.respHtml.equals("mktm0510"))) {
	        wp.initOption = "--";
	        wp.optionKey = "";
	        if (wp.colStr("ex_corp_no").length() > 0) {
	          wp.optionKey = wp.colStr("ex_corp_no");
	        }
	        this.dddwList("dddw_corp_no", "mkt_office_m", "corp_no",
	            "office_m_name"," where corp_no is not null "+ " order by corp_no desc");
	      }
	    } catch (Exception ex) {
	    }
  }

  // ************************************************************************
  public void procUploadFile(int loadType) throws Exception {
    if (wp.colStr(0, "ser_num").length() > 0)
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      wp.colSet("zz_file_name_pink", "pink ");
      return;
    }
    if (wp.itemEmpty("ex_corp_no")) {
        alertErr("請選擇金控公司");
        wp.colSet("corp_no_pink", "pink ");
        return;
      }
    String filename = wp.itemStr("zz_file_name");
    int loc = filename.lastIndexOf(".");
	  String ext = "";
	  String fname = "";
	  if (loc >= 0) {
		  fname = filename.substring(0, loc);
		  ext = filename.substring(loc + 1);
	  } else {
		  fname = filename;
	  }
	  if(!ext.equals("txt")) {
		  alertErr("上傳檔案:必需是txt檔");
		  wp.colSet("zz_file_name_pink", "pink ");
		  return;
	  }
	    String[] list = fname.split("_");
	  wp.itemSet("file_name", filename);
	  wp.itemSet("corp_no", list[0]);
	  String strMonth = list[list.length-1];
	  SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMDD");
  	  String tmpMonth = sdf.format(sdf.parse(strMonth));
	  if(list[0].length()!=8 || tmpMonth.equals(strMonth)) {
		  alertErr("檔名不符合規定");
		  wp.colSet("zz_file_name_pink", "pink ");
		  return;
	  }
	  if(!list[0].equals(wp.itemStr("ex_corp_no"))) {
		  alertErr("檔名統編與金控公司統編不符");
		  wp.colSet("zz_file_name_pink", "pink ");
		  return;
	  }
	  if (selectCrdEmployeeAT() > 0) {
	      alertErr2("檔名不可重覆匯入");
	      wp.colSet("zz_file_name_pink", "pink ");
	      return;
	    }
    if (loadType == 0)
      fileDataImp0();
  }

  // ************************************************************************
  int fileUpLoad() {
    TarokoUpload func = new TarokoUpload();
    try {
      func.actionFunction(wp);
      wp.colSet("zz_file_name", func.fileName);
    } catch (Exception ex) {
      wp.log("file_upLoad: error=" + ex.getMessage());
      return -1;
    }

    return func.rc;
  }

  // ************************************************************************
  void fileDataImp0() throws Exception {
	  //txt 文檔解析
	    TarokoFileAccess tf = new TarokoFileAccess(wp);

	    // String inputFile = wp.dataRoot + "/upload/" + wp.col_ss("file_name");
	    String inputFile = wp.itemStr("zz_file_name");
	    // int fi = tf.openInputText(inputFile,"UTF-8");
	    int fi = tf.openInputText(inputFile, "MS950");
	    if (fi == -1) {
	      return;
	    }
	    int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");

	    mktm01.Mktm0510Func func = new mktm01.Mktm0510Func();
	    func.setConn(wp);

	    wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));


	    int lineCnt = 0;
	    while (true) {
	      String file = tf.readTextFile(fi);
	      if (tf.endFile[fi].equals("Y"))
	        break;
	      llCnt++;

	      //換區txt文件中的每一欄數據
	      String[] txt = new String[10];
	      txt = file.split(",");
//	      txt[0] = file;
//	      txt = commString.token(txt, ",");
	      wp.itemSet("error_code", "00");
    	  wp.itemSet("error_desc", "成功");
	      String employ_no = txt[0];
	      if(employ_no.length()==0) {
	    	  wp.itemSet("error_code", "01");
	    	  wp.itemSet("error_desc", "檔案員編空白");
	    	  errs++;
	      }
	      if(selectCrdEmployeeAT(employ_no)>0) {
	    	  wp.itemSet("error_code", "04");
	    	  wp.itemSet("error_desc", "員編重覆");
	    	  errs++;
	      }
	      if(selectCrdEmployeeANo(employ_no)>0) {
	    	  wp.itemSet("error_code", "11");
	    	  wp.itemSet("error_desc", "金控員編重覆");
	    	  errs++;
	      }
	      wp.itemSet("employ_no", employ_no);
	      String chi_name  = txt[1];
	      wp.itemSet("chi_name", chi_name);
	      String id  = txt[2];
	      if(id.length()==0) {
	    	  wp.itemSet("error_code", "02");
	    	  wp.itemSet("error_desc", "檔案ID空白");
	    	  errs++;
	      }
	      if(selectCrdEmployeeAId(id)>0) {
	    	  wp.itemSet("error_code", "12");
	    	  wp.itemSet("error_desc", "金控員工ID重覆");
	    	  errs++;
	      }
	      wp.itemSet("id", id);
	      String acct_no  = txt[3];
	      wp.itemSet("acct_no", acct_no);
	      String unit_no  = txt[4];
	      wp.itemSet("unit_no", unit_no);
	      String unit_name  = txt[5];
	      wp.itemSet("unit_name", unit_name);
	      String subunit_no  = txt[6];
	      wp.itemSet("subunit_no", subunit_no);
	      String subunit_name  = txt[7];
	      wp.itemSet("subunit_name", subunit_name);
	      String position_no  = txt[8];
	      wp.itemSet("position_no", position_no);
	      String position_name  = txt[9];
	      wp.itemSet("position_name", position_name);
	      String status_id  = txt[10];
	      if(!status_id.equals("1")) {
	    	  wp.itemSet("error_code", "03");
	    	  wp.itemSet("error_desc", "狀態不是1.在職");
	    	  errs++;
	      }
	      if(selectCrdEmployeeNo(employ_no)>0) {
	    	  wp.itemSet("error_code", "13");
	    	  wp.itemSet("error_desc", "金控員編已存在行員員編資料");
	    	  errs++;
	      }
	      if(selectCrdEmployeeId(id)>0) {
	    	  wp.itemSet("error_code", "14");
	    	  wp.itemSet("error_desc", "金控員工ID已存在行員資料");
	    	  errs++;
	      }
	      wp.itemSet("status_id", status_id);
	      String subsidiary_no  = txt[11];
	      wp.itemSet("subsidiary_no", subsidiary_no);
	      

	      //執行插入數據命令
	      if(func.insertRighList() == 1) {
		    	llOK++;
		    }else {
		    	llErr++;
		    }
	      lineCnt++;
	    }

	    if (llOK > 0) {
	      sqlCommit(1);
	    } else {
	      sqlCommit(-1);
	    }

	    tf.closeOutputText(fileErr);
	    tf.closeInputText(fi);
	    tf.deleteFile(inputFile);

	    alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOK + ", 失敗筆數=" + llErr);
	    wp.colSet("zz_file_name", "");
	    return;
	  }

  // ************************************************************************
  int selectCrdEmployeeAT(String employ_no) throws Exception {
	    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee_a_t "
	    		+ " where 1 = 1 " + sqlCol(employ_no, "employ_no");
	    this.sqlSelect();

	    if (sqlRowNum > 0)
	      return (1);

	    return (0);
	  }
//************************************************************************
 int selectCrdEmployeeANo(String employ_no) throws Exception {
	    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee_a "
	    		+ " where 1 = 1 " + sqlCol(employ_no, "employ_no");
	    this.sqlSelect();

	    if (sqlRowNum > 0)
	      return (1);

	    return (0);
	  }
//************************************************************************
int selectCrdEmployeeAId(String id) throws Exception {
	    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee_a "
	    		+ " where 1 = 1 " + sqlCol(id, "id");
	    this.sqlSelect();

	    if (sqlRowNum > 0)
	      return (1);

	    return (0);
	  }
//************************************************************************
int selectCrdEmployeeNo(String employ_no) throws Exception {
	    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee "
	    		+ " where 1 = 1 and employ_no = ? " 
	    		+ " and status_id in(1,6) ";
	    setString(employ_no);
	    this.sqlSelect();

	    if (sqlRowNum > 0)
	      return (1);

	    return (0);
	  }
//************************************************************************
int selectCrdEmployeeId(String id) throws Exception {
	    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee "
	    		+ " where 1 = 1 and id = ? " 
	    		+ " and status_id in(1,6) ";
	    setString(id);
	    this.sqlSelect();

	    if (sqlRowNum > 0)
	      return (1);

	    return (0);
	  }
  // ************************************************************************
  int selectCrdEmployeeAT() throws Exception {
    wp.sqlCmd = " select " + " 1 as rowdata " + " from crd_employee_a_t "
    		+ " where 1 = 1" + sqlCol(wp.itemStr("zz_file_name"), "file_name");
    this.sqlSelect();

    if (sqlRowNum > 0)
      return (1);

    return (0);
  }


  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

} // End of class
