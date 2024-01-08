/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION    	AUTHOR                 DESCRIPTION                *
* ---------  --------  	-----------    ------------------------------------ *
* 112/08/25  V1.00.01   	Machao      Initial  
***************************************************************************/
package mktq01;

import java.io.IOException;
import java.text.SimpleDateFormat;

import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq0935 extends BaseEdit
{
 private final String PROGNAME = "數存戶自動扣繳成功回饋查詢作業112/08/25  V1.00.01";
  busi.DataSet ds1 =new busi.DataSet(); 
  String orgTabName = "mkt_cashback_dtl";
  String controlTabName = "";
  static String sqlStr = "";
  String  newLine="\n";
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
//  else if (eqIgno(wp.buttonCode, "XLS"))
//  {/* nothing to do */
//	  wp.setDownload(wp.itemStr("file_name"));
//  }
  dddwSelect();
  initButton();
 }
// ************************************************************************
 @Override
 public void queryFunc() throws Exception
 {
	 if (queryCheck()!=0) return;
  wp.whereStr = "WHERE 1=1 and a.ID_P_SEQNO=b.id_p_seqno "
  			  + " AND a.FUND_CODE='0076000001' "
  			  + " AND a.MOD_DESC='數存戶自行扣' "
  			  + " AND a.mod_pgm='MktC930' "
              + sqlCol(wp.itemStr("ex_acct_month"), "a.acct_month")
              + sqlCol(wp.itemStr("ex_id_no"), "b.id_no")
              ;

  //-page control-
  wp.queryWhere = wp.whereStr;
  wp.setQueryMode();

  queryRead();
 }
private int queryCheck() {
	if(!empty(wp.itemStr("ex_acct_month"))) {
		if (wp.itemStr("ex_acct_month").length()!=6) {
			alertErr("帳務年月只能輸入6碼!");
		     return(1);
		}
	}
	
	return(0);
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

  wp.selectSQL = " " + "a.acct_month, " + "a.p_seqno," + "b.id_no,"
              + "b.chi_name," + "a.tran_date " ;

  wp.daoTable = controlTabName + " a , crd_idno b" ;
//  wp.whereOrder = " "
//                + " order by a.acct_month "
//                ;

  pageQuery();
  listWkdataQuery();
  wp.setListCount(1);
  if (sqlNotFind())
     {
      alertErr(appMsg.errCondNodata);
      return;
     }

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
}

//************************************************************************
void listWkdataQuery()  throws Exception
{
	 wp.itemSet("bb_down_file_name" ,"Mktq0935" +"_"+ wp.itemStr("ex_acct_month"));
	 wp.colSet("bb_down_file_name" , "Mktq0935" +"_"+ wp.itemStr("ex_acct_month"));

  return;

}
//************************************************************************
public void mediafileProcess() throws Exception
{
	 wp.listCount[0] = wp.itemBuff("ser_num").length;
	  
	  if (wp.itemStr("bb_down_file_name").length()==0)
	     {
	      alertErr("尚未產生資料, 無法產生檔案");
	      return;
	     }
	  
	  if (!checkApprove(wp.itemStr("approval_user"),wp.itemStr("approval_passwd"))) return;
	  
	  wp.dateTime();
	  String oriFileName = wp.itemStr("bb_down_file_name");
	  String fileName="";
	  int intk = oriFileName.lastIndexOf('.');
	  if (intk >= 0)
	      fileName = oriFileName.substring(0,intk)+ "_"+wp.sysDate+wp.sysTime+ oriFileName.substring(intk);
	  else
	      fileName = wp.itemStr("bb_down_file_name")+"_"+wp.sysDate+wp.sysTime;
	  fileName = fileName + ".csv";
	  wp.colSet("zz_media_file",fileName);
	  TarokoFileAccess tf = new TarokoFileAccess(wp);
	  fo = tf.openOutputText(fileName,"BIG5");

	  if (fo == -1) return;

	  String outData="";

	  setSelectLimit(99999);
	  String  sqlStr = "";
	  sqlStr = "select  "
	          + "a.acct_month, "
	          + "a.p_seqno,"
	          + "b.id_no,"
	          + "b.chi_name,"
	          + "a.tran_date "
	          + "from MKT_CASHBACK_DTL a, CRD_IDNO b "
	         + "WHERE a.ID_P_SEQNO=b.id_p_seqno "
	         + " AND a.FUND_CODE='0076000001' "
	         + " AND a.MOD_DESC='數存戶自行扣' "
	         + " AND a.mod_pgm='MktC930' "
	         ;
	  if(!empty(wp.itemStr("ex_acct_month")) && !empty(wp.itemStr("ex_id_no"))) {
		  sqlStr += " and a.acct_month = :ex_acct_month and b.id_no = :ex_id_no ";
		  setString("ex_acct_month",wp.itemStr("ex_acct_month"));
		  setString("ex_id_no",wp.itemStr("ex_id_no"));
	  }
	  
	  if(!empty(wp.itemStr("ex_acct_month"))) {
		  sqlStr += " and a.acct_month = :ex_acct_month  ";
		  setString("ex_acct_month",wp.itemStr("ex_acct_month"));
	  }
	  
	  if(!empty(wp.itemStr("ex_id_no"))) {
		  sqlStr += " and a.id_no = :ex_id_no  ";
		  setString("ex_id_no",wp.itemStr("ex_id_no"));
	  }

	 
	  sqlSelect(sqlStr);

	  for (int inti=0; inti<sqlRowNum; inti++)
		{
		  if (inti==0)
	        {
	         outData = "";
	         outData = outData + "帳務年月,";
	         outData = outData + "帳戶號,";
	         outData = outData + "身分證號,";
	         outData = outData + "姓名,";
	         outData = outData + "回饋日期";
	         tf.writeTextFile(fo, outData + newLine);
	        }
	     outData = "";
	     outData = "";
	     outData = outData + checkColumn(inti,"acct_month") + ",";
	     outData = outData + checkColumn(inti,"p_seqno") + ",";
	     outData = outData + checkColumn(inti,"id_no") + ",";
	     outData = outData + checkColumn(inti,"chi_name") + ",";
	     outData = outData + checkColumn(inti,"tran_date") + ",";
	     tf.writeTextFile(fo, outData + newLine);
	   }
	  tf.closeOutputText(fo);
	  alertMsg("檔案 ["+fileName+"] 已經產生,累計下載 "+ sqlRowNum + " 筆!");

	  wp.colSet("zz_full_media_file","href=./WebData/work/"+fileName+"");
	  wp.colSet("img_display"," height=\"30\" src=images/downLoad.gif ");
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
