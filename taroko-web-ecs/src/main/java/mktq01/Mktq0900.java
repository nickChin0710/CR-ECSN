/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION    	AUTHOR                 DESCRIPTION                *
* ---------  --------  	-----------    ------------------------------------ *
* 112/04/21  V1.00.01   	Machao      Initial  
* 112/05/04  V1.00.02   	Machao      稅務活動回饋查詢產檔作業    
* 112/05/15  V1.00.03   	Machao      程式页面微调                           *
* 112/06/16  V1.00.04   	Zuwei Su    繳稅活動代號下拉選項去重               *
* 112/07/25  V1.00.05   	Ryan        下載筆數修正                                *
* 112/08/08  V1.00.05.01	Grace       下載資料以逗號區隔                                *
***************************************************************************/
package mktq01;

import java.io.IOException;
import java.text.SimpleDateFormat;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0900 extends BaseEdit
{
 private final String PROGNAME = "稅務活動回饋查詢產檔作業112/06/16 V1.00.04";
//  busi.DataSet dataSet = new busi.DataSet();
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  String orgTabName = "mkt_tax_fbdata";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String   batchNo     = "";
  int errorCnt=0,recCnt=0,notifyCnt=0,colNum=0;
  int[]  datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String   upGroupType= "0";
  static String sqlStr = "";
  private static final String LINE_SEPARATOR = "\n";
  int fo=0;

// ************************************************************************
 @Override
 public void actionFunction(TarokoCommon wr) throws Exception
 {
  super.wp = wr;
  rc = 1;

  strAction = wp.buttonCode;
  if (eqIgno(wp.buttonCode, "X"))
     {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "Q"))
     {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
     }
  else if (eqIgno(wp.buttonCode, "R"))
     {//-資料讀取-
      strAction = "R";
      dataRead();
     }
  else if (eqIgno(wp.buttonCode, "MEDIAFILE"))
     {/* 產生媒體檔 */
      strAction = "U";
      mediafileProcess();
     }
  else if (eqIgno(wp.buttonCode, "A"))
     {// 新增功能 -/
      strAction = "A";
      insertFunc();
     }
  else if (eqIgno(wp.buttonCode, "U"))
     {/*  更新功能 */
      strAction = "U";
      updateFunc();
     }
  else if (eqIgno(wp.buttonCode, "D"))
     {/* 刪除功能 */
      deleteFunc();
     }
  else if (eqIgno(wp.buttonCode, "M"))
     {/* 瀏覽功能 :skip-page*/
      queryRead();
     }
  else if (eqIgno(wp.buttonCode, "S"))
     {/* 動態查詢 */
      querySelect();
     }
  else if (eqIgno(wp.buttonCode, "L"))
     {/* 清畫面 */
      strAction = "";
      clearFunc();
     }
  else if (eqIgno(wp.buttonCode, "NILL"))
     {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
     }
  else if (eqIgno(wp.buttonCode, "XLS"))
  {/* nothing to do */
	  wp.setDownload(wp.itemStr("file_name"));
  }
  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
	 if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 and feedback_date is not null"
              + sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
              + sqlCol(wp.itemStr("ex_id_no"), "a.id_no")
              + sqlCol(wp.itemStr("ex_feedback_id_type"), "a.feedback_id_type")
              + sqlCol(wp.itemStr("ex_active_type"), "a.active_type")
              ;

  //-page control-
  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();
 }
// ************************************************************************
 @Override
 public void queryRead() throws Exception
 {
  if (wp.colStr("org_tab_name").length()>0)
     controlTabName = wp.colStr("org_tab_name");
  else
     controlTabName = orgTabName;

  wp.pageControl();

  wp.selectSQL = " " + "a.active_code, " + "a.active_type," + "a.id_no,"
              + "b.chi_name," + "a.card_no," + "a.gift_type,"
              + "a.purchase_date, " + "a.purchase_amt, " + "a.feedback_date " ;

  wp.daoTable = controlTabName + " a "
          + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
              ;
  wp.whereOrder = " "
                + " order by a.active_code "
                ;

  pageQuery();
//  listWkdataQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

  commfuncActType("active_type");
  commfuncGiftType("gift_type");
  wp.setPageValue();
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
public void dddwSelect()
{
String lsSql ="";
try {
    if ((wp.respHtml.equals("mktq0900")))
      {
       wp.initOption ="--";
       wp.optionKey = "";
       if (wp.colStr("ex_active_code").length()>0)
          {
          wp.optionKey = wp.colStr("ex_active_code");
          }
       this.dddwList("dddw_active_code"
              ,"mkt_tax_fbdata a, mkt_tax_parm b "
              ,"distinct trim(a.active_code)"
              ,"trim(a.active_code)||'_'||trim(active_name)"
              ," where a.active_code = b.active_code");
      }
   } catch(Exception ex){}
}

public int queryCheck() throws Exception
{
 if ((itemKk("ex_active_code").length()==0)&&
     (itemKk("ex_id_no").length()==0))
    {
     alertErr("身份證號與活動代號二者不可同時空白");
     return(1);
    }

 if (wp.itemStr("ex_id_no").length()>0)
    {
     String sql1 = "";

     sql1 = "select id_p_seqno "
          + "from crd_idno "
          + "where  id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"'"
          ;
     sqlSelect(sql1);

     if (sqlRowNum <= 0)
         wp.colSet("ex_id_p_seqno","");
     else
        wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));

     sql1 = "select id_p_seqno as vd_id_p_seqno  "
          + "from dbc_idno "
          + "where  id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"'"
          ;
     sqlSelect(sql1);
     if (sqlRowNum <= 0)
        wp.colSet("ex_vd_id_p_seqno","");
     else
        wp.colSet("ex_vd_id_p_seqno",sqlStr("vd_id_p_seqno"));

     if ((sqlStr("vd_id_p_seqno").length()==0)&&
         (sqlStr("id_p_seqno").length()==0))
        {
         alertErr(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
         return(1);
        }

    }


 return(0);
}
//************************************************************************
void commfuncActType(String cde1) {
	    if (cde1 == null || cde1.trim().length() == 0)
	      return;
	    String[] cde = {"1", "2", "3", "4"};
	    String[] txt = {"綜所稅", "地價稅", "牌照稅", "房屋稅"};

	    for (int ii = 0; ii < wp.selectCnt; ii++) {
	      wp.colSet(ii, "comm_func_" + cde1, "");
	      for (int inti = 0; inti < cde.length; inti++)
	        if (wp.colStr(ii, cde1).equals(cde[inti])) {
	          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
	          break;
	        }
	    }
	  }
//************************************************************************
void commfuncGiftType(String cde1) {
	    if (cde1 == null || cde1.trim().length() == 0)
	      return;
	    String[] cde = {"1"};
	    String[] txt = {"50元電子現金抵用券"};

	    for (int ii = 0; ii < wp.selectCnt; ii++) {
	      wp.colSet(ii, "comm_func_" + cde1, "");
	      for (int inti = 0; inti < cde.length; inti++)
	        if (wp.colStr(ii, cde1).equals(cde[inti])) {
	          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
	          break;
	        }
	    }
	  }
void listWkdataQuery()  throws Exception
{
	 String oriFileName = wp.itemStr("active_type");
	 String fileName="";
		java.util.Date date=new java.util.Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String str=sdf.format(date).substring(0, 8);
		String data1 = str.substring(0, 4);
		String dataM = str.substring(4, 8);
		int dataY = Integer.parseInt(data1)-1911;
		if(oriFileName.equals("1")) {
			fileName = "TWMP_IMCOME_BONUS_OK";
		}else if(oriFileName.equals("2")) {
			fileName ="TWMP_PAN_BONUS_OK";
		}else if(oriFileName.equals("3")) {
			fileName ="TWMP_CAR_BONUS_OK";
		}else if(oriFileName.equals("4")) {
			fileName ="TWMP_HOUSE_BONUS_OK";
		}
		
     wp.itemSet("bb_down_file_name", fileName + dataY + dataM + ".TXT");
     wp.colSet("bb_down_file_name", fileName + dataY + dataM + ".TXT");
  return;

}
//************************************************************************
public void mediafileProcess() throws Exception
{
	 String oriFileName = wp.itemStr("active_type");
	 String FileName="";
		java.util.Date date=new java.util.Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String str=sdf.format(date).substring(0, 8);
		String data1 = str.substring(0, 4);
		String dataM = str.substring(4, 8);
		int dataY = Integer.parseInt(data1)-1911;
		if(oriFileName.equals("1")) {
			FileName = "TWMP_IMCOME_BONUS_OK";
		}else if(oriFileName.equals("2")) {
			FileName ="TWMP_PAN_BONUS_OK";
		}else if(oriFileName.equals("3")) {
			FileName ="TWMP_CAR_BONUS_OK";
		}else if(oriFileName.equals("4")) {
			FileName ="TWMP_HOUSE_BONUS_OK";
		}
	
	 String bbDownFileName = FileName + dataY + dataM + ".TXT";
//			 wp.colStr("bb_down_file_name");
     wp.listCount[0] = wp.itemBuff("ser_num").length;

     if (bbDownFileName.length() == 0) {
         alertErr("尚未產生資料, 無法產生檔案");
         return;
     }

     if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
         return;
     }

     wp.dateTime();
     String fileName;
     int intk = bbDownFileName.lastIndexOf('.');
     if (intk >= 0) {
         fileName = bbDownFileName.substring(0, intk);
     } else {
         fileName = bbDownFileName + "_" + wp.sysDate + wp.sysTime;
     }
     fileName = fileName + ".TXT";
     wp.colSet("zz_media_file", fileName);
     TarokoFileAccess tf = new TarokoFileAccess(wp);
     fo = tf.openOutputText(fileName, "MS950");

     if (fo == -1) {
         return;
     }

     if (wp.colStr("org_tab_name").length() > 0) {
         controlTabName = wp.colStr("org_tab_name");
     } else {
         controlTabName = orgTabName;
     }
	
	String outData="";
	
	setSelectLimit(99999);
	String  sqlStr = "";
	sqlStr = "select  "
			+ "a.active_code, " + "a.active_type," + "a.id_no,"
	        + "b.chi_name," + "a.card_no," + "a.gift_type," + "b.cellar_phone,"
	        + "a.purchase_date, " + "a.purchase_amt, " + "a.feedback_date " 
	        + "from mkt_tax_fbdata a "
	        + "left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
	        + "WHERE 1=1 "
//			+ sqlCol(wp.itemStr("ex_active_code"), "a.active_code")
	        + " and a.active_code = :ex_active_code "
	        + " order by a.active_code " ;
	

//	busi.FuncBase fB = new busi.FuncBase();
//	fB.setConn(wp);;
	
//	dataSet.colList = fB.sqlQuery(sqlStr,new Object[]{wp.itemStr("ex_active_code")});
	setString("ex_active_code",wp.itemStr("ex_active_code"));
	sqlSelect(sqlStr);
	String activeType = wp.getValue("active_type");
	String giftType = wp.getValue("gift_type");
	if(activeType.equals("1")) {
		 wp.colSet("commfunc_active_type", "綜所稅");
	}else if(activeType.equals("2")) {
		wp.colSet("commfunc_active_type", "地價稅");
	}else if(activeType.equals("3")) {
		wp.colSet("commfunc_active_type", "牌照稅");
	}else if(activeType.equals("4")) {
		wp.colSet("commfunc_active_type", "房屋稅");
	}
	if(giftType.equals("1")) {
		wp.colSet("commfunc_gift_type", "50元電子現金抵用券");
	}
//	sqlParm.clear();
	
//	for (int inti=0; inti<dataSet.listRows(); inti++)
	for (int inti=0; inti<sqlRowNum; inti++)
	{
//		dataSet.listFetch(inti);
	
	  if (inti==0)
	     {
	      outData = "";
	//      outData = outData + "活動代號,";
	//      outData = outData + "繳稅類別,";
	      outData = outData + "卡號,";
	//      outData = outData + "回饋類別,";
	      outData = outData + "消費日期,";
	      outData = outData + "身分證號,";
	      outData = outData + "姓名,";
	      outData = outData + "手機號碼";
	//      outData = outData + "消費金額,";
	//      outData = outData + "回饋日期";
	      tf.writeTextFile(fo, outData + LINE_SEPARATOR);
	     }
//	  outData = "";
	  outData = "";
	//  outData = outData + checkColumn("active_code");
	//  outData = outData + checkColumn("active_type");
	  outData = outData + checkColumn(inti,"card_no") + ",";
	//  outData = outData + checkColumn("gift_type");
	  outData = outData + checkColumn(inti,"purchase_date") + ",";
	  outData = outData + checkColumn(inti,"id_no") + ",";
	  outData = outData + checkColumn(inti,"chi_name") + ",";
	  outData = outData + checkColumn(inti,"cellar_phone") + ",";
	//  outData = outData + checkColumn("purchase_amt");
	//  outData = outData + checkColumn("feedback_date");
	  tf.writeTextFile(fo, outData + LINE_SEPARATOR);
	
	}
	tf.closeOutputText(fo);
	alertMsg("檔案 ["+fileName+"] 已經產生,累計下載 "+ sqlRowNum + " 筆!");
	
//	 wp.colSet("zz_full_media_file", " download="
//             + fileName
//             + " href=./WebData/work/"
//             + fileName
//             + "?response-content-type=application/octet-stream");
     wp.colSet("img_display", " src=images/downLoad.gif ");
     wp.colSet("file_name", fileName);
	return;
}

//************************************************************************
public String checkColumn(int inti,String s1) throws Exception
{
return sqlStr(inti,s1);
}
//************************************************************************
@Override
public void initButton()
{
if (wp.respHtml.indexOf("_detl") > 0)
{
 this.btnModeAud();
}
}
//************************************************************************
}  // End of class
