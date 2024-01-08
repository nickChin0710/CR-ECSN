/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/02/28  V1.00.01   machao      Initial  
* 112/03/10  V1.00.02   machao      程式調整：bug修訂                  *
* 112/05/10  V1.00.03   Zuwei Su    程式調整：bug修訂                  *
* 112/05/11  V1.00.04   Zuwei Su    明細名稱為空                  *
* 112/05/12  V1.00.05   Zuwei Su    明細為空，覆核後存檔button不可用                  *
* 112/05/17  V1.00.06   Ryan        活動新增擇一回饋, 提供參數設定                  
* 112-06-06  V1.00.07   machao      活動群組增 ‘群組月累績最低消費金額’*
* 112-07-12  V1.00.08   Zuwei Su    "放行狀態"='未異動', 活動代碼的button仍可點選查看內容*
***************************************************************************/
package mktm01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

import java.util.ArrayList;
// ************************************************************************
public class Mktm0851 extends BaseEdit {

  private ArrayList<Object> params = new ArrayList<Object>();
  private final String PROGNAME = "行銷通路活動群組設定檔維護112/02/28 V1.00.01";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  mktm01.Mktm0851Func func = null;
  String rowid;
  String activeGroupId;
  String fstAprFlag = "";
  String orgTabName = "MKT_CHANNELGP_PARM";
  String controlTabName = "";
  int qFrom = 0;
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
      wp.itemSet("aud_type", "A");
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U3";
      updateFuncU3R();
    } else if (eqIgno(wp.buttonCode, "I")) {/* 單獨新鄒功能 */
      strAction = "I";
      /*
       * kk1 = item_kk("data_k1"); kk2 = item_kk("data_k2"); kk3 = item_kk("data_k3");
       */
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFuncD3R();
    } else if (eqIgno(wp.buttonCode, "R2")) {// 明細查詢 -/
      strAction = "R2";
      dataReadR2();
    } else if (eqIgno(wp.buttonCode, "U2")) {/* 明細更新 */
      strAction = "U2";
      updateFuncU2();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    }  else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
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
    wp.whereStr = "WHERE 1=1 " 
    		+ sqlCol(wp.itemStr("ex_active_group_id"), "a.active_group_id");

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {
    if (wp.colStr("org_tab_name").length() > 0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;
    if (wp.itemStr("ex_apr_flag").equals("N"))
      controlTabName = orgTabName + "_t";

    wp.pageControl();

    wp.selectSQL = " "
            + "hex(a.rowid) as rowid, "
            + "nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_group_id,"
            + "a.active_group_desc,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date,"
            + "a.feedback_type";

    wp.daoTable = controlTabName + " a ";
    wp.whereOrder = " " + " order by a.active_group_id";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    commFeedbackType("feedback_type");
    wp.setPageValue();
  }


  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    fstAprFlag = wp.itemStr("ex_apr_flag");
    if (wp.itemStr("ex_apr_flag").equals("N")) {
    	controlTabName = orgTabName + "_t";
    }
    rowid = itemKk("data_k1");
    qFrom = 1;
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {
    if (controlTabName.length() == 0) {
      if (wp.colStr("control_tab_name").length() == 0)
        controlTabName = orgTabName;
      else
        controlTabName = wp.colStr("control_tab_name");
    } else {
      if (wp.colStr("control_tab_name").length() != 0)
        controlTabName = wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + "a.active_group_id,"
            + "a.active_group_desc,"
            + "a.sum_amt,"
            + "a.feedback_type,"
            + "a.limit_amt,"
            + "a.crt_user,"
            + "a.crt_date,"
            + "a.apr_user,"
            + "a.apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 ";
    if (qFrom == 0) {
        wp.whereStr = wp.whereStr + sqlCol(activeGroupId, "a.active_group_id");
    } else if (qFrom == 1) {
        wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
    }

    pageSelect();
    if (sqlNotFind()) {
      return;
    }
    if (qFrom == 0) {
      wp.colSet("aud_type", "Y");
      wp.colSet("apr_flag", "Y");
    } else {
        wp.colSet("aud_type",wp.itemStr("ex_apr_flag"));
        wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
    }
    checkButtonOff();
    activeGroupId = wp.colStr("active_group_id");
    commfuncAudType("aud_type");
    dataReadCnt(activeGroupId);
    wp.colSet("control_tab_name", controlTabName);
    dataReadR3R();
  }

  private void dataReadCnt(String activeGroupId) {
	  String lsSql = " select count(*) as active_group_cnt from MKT_CHANNELGP_DATA where active_group_id = ? ";
      sqlSelect(lsSql, new Object[]{activeGroupId});
      int active_group_cnt = (int) sqlNum("active_group_cnt");
      wp.colSet("active_group_cnt", active_group_cnt);
}

  private void dataReadCntAft(String activeGroupId) {
      String lsSql = " select count(*) as active_group_cnt from MKT_CHANNELGP_DATA_T where active_group_id = ? ";
      sqlSelect(lsSql, new Object[]{activeGroupId});
      int active_group_cnt = (int) sqlNum("active_group_cnt");
      wp.colSet("active_group_cnt", active_group_cnt);
}

// ************************************************************************
  public void dataReadR3R() throws Exception {
    wp.colSet("control_tab_name", controlTabName);
    controlTabName = orgTabName + "_t";
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " nvl(a.mod_seqno,0) as mod_seqno, "
            + " a.aud_type as aud_type, "
            + "a.active_group_id as active_group_id,"
            + "a.active_group_desc as active_group_desc,"
            + "a.sum_amt as sum_amt,"
            + "a.feedback_type as feedback_type,"
            + "a.limit_amt as limit_amt,"
            + "a.crt_user as crt_user,"
            + "a.crt_date as crt_date,"
            + "a.apr_user as apr_user,"
            + "a.apr_date as apr_date";

    wp.daoTable = controlTabName + " a ";
    wp.whereStr = "where 1=1 " + sqlCol(activeGroupId, "a.active_group_id");

    pageSelect();
    if (sqlNotFind()) {
      wp.notFound = "";
      return;
    }
    wp.colSet("control_tab_name", controlTabName);
    checkButtonOff();
    commfuncAudType("aud_type");
    activeGroupId = wp.colStr("active_group_id");
    dataReadCntAft(activeGroupId);
  }

  // ************************************************************************
  public void deleteFuncD3R() throws Exception {
    qFrom = 0;
    activeGroupId = wp.itemStr("active_group_id");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      activeGroupId = wp.itemStr("active_group_id");
      strAction = "D";
      deleteFunc();
      if (fstAprFlag.equals("Y")) {
        qFrom = 0;
        controlTabName = orgTabName;
      }
    } else {
      strAction = "A";
      wp.itemSet("aud_type", "D");
      insertFunc();
    }
    dataRead();
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void updateFuncU3R() throws Exception {
    qFrom = 0;
    activeGroupId = wp.itemStr("active_group_id");
    fstAprFlag = wp.itemStr("fst_apr_flag");
    if (!wp.itemStr("aud_type").equals("Y")) {
      strAction = "U";
      updateFunc();
      if (rc == 1)
        dataReadR3R();
    } else {
      activeGroupId = wp.itemStr("active_group_id");
      strAction = "A";
      wp.itemSet("aud_type", "U");
      insertFunc();
      if (rc == 1)
        dataRead();
    }
    wp.colSet("fst_apr_flag", fstAprFlag);
  }

  // ************************************************************************
  public void dataReadR2() throws Exception {
    String bnTable = "";

    if ((wp.itemStr("active_group_id").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
      alertErr2("鍵值為空白或主檔未新增 ");
      return;
    }
    wp.selectCnt = 1;
    this.selectNoLimit();
    if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
      buttonOff("btnUpdate_disable");
      buttonOff("newDetail_disable");
      bnTable = "mkt_channelgp_data a left join mkt_channel_parm b on a.active_code = b.active_code ";
    } else {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("newDetail_disable", "");
      bnTable = "mkt_channelgp_data_t a left join mkt_channel_parm b on a.active_code = b.active_code ";
    }

//    wp.selectSQL = "hex(rowid) as rowid, " + "active_code, " + "feedback_seq ";
    wp.selectSQL = "hex(a.rowid) as rowid, " + "a.active_code, " + "b.active_name ";
    wp.daoTable = bnTable;
    wp.whereStr = "where 1=1" ;
    if (wp.respHtml.equals("mktm0851_aecd")) {
        wp.whereStr += " and  a.active_group_id = :active_group_id ";
        setString("active_group_id", wp.itemStr("active_group_id"));
    }
//    wp.whereStr += " order by feedback_seq ";
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "";

    wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
  }

  // ************************************************************************
  public void updateFuncU2() throws Exception
  {
	  mktm01.Mktm0851Func func = new mktm01.Mktm0851Func(wp);
	    int llOk = 0, llErr = 0;
	  String[] optData = wp.itemBuff("opt");
	  String[] key1Data = wp.itemBuff("active_code");
//	  String[] key2Data = wp.itemBuff("feedback_seq");
	    
	  for(String opt:optData) {
	    	if(!empty(opt)) {
	    		int optt = Integer.parseInt(opt);
	    		String activeCode = key1Data[optt];
	    		func.varsSet("active_code", activeCode);
	    		if (func.dbDeleteD2() < 0){
		          alertErr(func.getMsg());
		          return;
		         }else {
		        	 llOk++; 
		         }
	    	}else {
	    		for(int i = 0; i<key1Data.length;i++) {
	    			if(!empty(key1Data[i])) {
	    				String groupId = wp.itemStr("active_group_id");
	    				int a = SelectMktChannelgpDataT(groupId,key1Data[i]);
	    				if(a<1) {
	    					func.varsSet("active_code", key1Data[i]);
//	    				    func.varsSet("feedback_seq", key2Data[i]);
	    				 if (func.dbInsertI2() == 1)
	    				        llOk++;
	    				 else
	    				        llErr++;
	    				      // 有失敗rollback，無失敗commit
	    				      sqlCommit(llOk > 0 ? 1 : 0);
	    				}
	    			}
	    		}
	    	}
	  }
	    	 // -insert-
	       
	    alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");
	    // SAVE後 SELECT
	    dataReadR2();
  }

  public int SelectMktChannelgpDataT(String groupId,String activeCode) {
	  String sql1 = "select count(*) as bndataCount from MKT_CHANNELGP_DATA_T where active_group_id = ? and active_code =? " ;

	    sqlSelect(sql1, new Object[]{groupId,activeCode});

	    return ((int) sqlNum("bndataCount"));
}

// ************************************************************************
  public int selectBndataCount(String bndataTable, String whereStr) throws Exception {
    String sql1 = "select count(*) as bndataCount" + " from " + bndataTable + " " + whereStr;

    sqlSelect(sql1, params.toArray(new Object[params.size()]));

    return ((int) sqlNum("bndataCount"));
  }
//************************************************************************
 public int selectMktParmT( String groupId) throws Exception {
   String sql1 = "select count(*) as bndataCount  from MKT_CHANNELGP_PARM  where active_group_id = ?";

   sqlSelect(sql1, new Object[]{groupId});

   return ((int) sqlNum("bndataCount"));
 }
  // ************************************************************************
  public void saveFunc() throws Exception {
    mktm01.Mktm0851Func func = new mktm01.Mktm0851Func(wp);

    rc = func.dbSave(strAction);
    if (rc != 1)
      alertErr2(func.getMsg());
    log(func.getMsg());
    this.sqlCommit(rc);
  }

  // ************************************************************************
  @Override
  public void initButton() {
    if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
      wp.colSet("btnUpdate_disable", "");
      wp.colSet("btnDelete_disable", "");
      this.btnModeAud();
    }
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {
	  try {
	  wp.initOption = "--";
      wp.optionKey = "";
      if (wp.colStr("active_code").length() > 0) {
//          wp.optionKey = wp.colStr("active_code");
      }
		this.dddwList("dddw_mkt_channel_m", "mkt_channel_parm", "active_code", "active_name",
		          "where 1=1 order by active_code");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  // ************************************************************************
  public String sqlChkEx(String exCol, String sqCond, String fileExt) {
    return "";
  }

  // ************************************************************************
  void commfuncAudType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"Y", "A", "U", "D"};
    String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_func_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
          break;
        }
    }
  }

  
  // ************************************************************************
  void commFeedbackType(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0)
      return;
    String[] cde = {"1", "2", "3"};
    String[] txt = {"1.擇優回饋", "2.回饋上限總金額", "3.擇一回饋"};

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "comm_" + cde1, "");
      for (int inti = 0; inti < cde.length; inti++)
        if (wp.colStr(ii, cde1).equals(cde[inti])) {
          wp.colSet(ii, "comm_" + cde1, txt[inti]);
          break;
        }
    }
  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
//    if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
//      buttonOff("btnmrcd_disable");
//    } else {
//      wp.colSet("btnmrcd_disable", "");
//    }
      wp.colSet("btnmrcd_disable", "");
   
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    buttonOff("btnmrcd_disable");
    buttonOff("uplmrcd_disable");

    return;
  }
  // ************************************************************************

} // End of class
