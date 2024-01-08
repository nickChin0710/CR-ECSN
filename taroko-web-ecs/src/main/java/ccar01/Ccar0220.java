/*****************************************************************************
*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  ---------  --------- ----------- ---------------------------------------- *
*  111-03-18  V1.00.01  Justin      initial                                  *
*  111-06-15  V1.00.02  Justin      select line_source options from DB       *
*  111-06-27  V1.00.03  Justin      display Chinese parameter names          * 
/*****************************************************************************
*/
package ccar01;

import ofcapp.BaseAction;

public class Ccar0220 extends BaseAction {
	final String[] lineMessageCols = {"id","cardName","endDigits","amount","ccy","type","store","accessCode","time"};
	final String defaultOption = "<option value=\"ConsumptionNotification\" >LINE-信用卡消費通知</option> <option value=\"TCB_CARD_BUY\" %s >網銀APP-信用卡/VISA金融卡消費通知</option>"; 

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
    } 

  }

  @Override
  public void queryFunc() throws Exception {
	   if(isEmpty(wp.itemStr("ex_date1")) || isEmpty(wp.itemStr("ex_date2"))) {
		   errmsg("推播日期:起迄不得為空");
		   return;
	   }
	  
	   if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
	      errmsg("推播日期:起迄錯誤");
	      return;
	   }

	   String lsWhere = " where 1=1 "
	         + sqlStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2"), "crt_date")
	         + sqlCol(wp.itemStr("ex_line_source"), "line_source")
	         + sqlCol(wp.itemStr("ex_idno"), "id_no");

	   if (wp.itemEq("ex_result", "1")) {
		  // 成功
	      lsWhere += " and line_gw_flag = '1' ";
	   }
	   else if (wp.itemEq("ex_result", "0")) {
		  // 失敗
	      lsWhere += " and line_gw_flag = '0' AND LINE_GW_MESSAGE <> '' ";
	   }
	   else if (wp.itemEq("ex_result", "N")) {
		  // 未發送
	      lsWhere += " and line_gw_flag = '0' AND LINE_GW_MESSAGE = '' ";
	   }

	   wp.whereStr = lsWhere;
	   wp.queryWhere = wp.whereStr;
	   wp.setQueryMode();

	   queryRead();

  }

  @Override
  public void queryRead() throws Exception {
	  wp.pageControl();
	   wp.selectSQL = 
			   " crt_date , "
	         + " crt_time , "
	         + " id_no , "
	         + " line_source_name , "
	         + " line_message , "
	         + " decode(line_gw_flag,'0', decode( LINE_GW_MESSAGE, '', '未發送', '失敗('||LINE_GW_MESSAGE||')') ,'1','成功') as tt_line_gw_flag "

	   ;
	   wp.daoTable = " mkt_line_message ";
	   wp.whereOrder = " order by crt_date Desc , crt_time Desc ";

	   pageQuery();

	   if (sqlNotFind()) {
	      errmsg("此條件查無資料");
	      return;
	   }else {
		   parseLineMessageByJSON();
	   }

	   wp.setListCount(0);
	   wp.setPageValue();
  }

	private void parseLineMessageByJSON() throws Exception {
		for (int i = 0; i < sqlRowNum; i++) {
			String lineMessge = wp.colStr(i, "line_message");
			try {
				if (isEmpty(lineMessge) == false) {
					wp.decodeJASON(lineMessge);
					for (String col : lineMessageCols) {
						wp.colSet(i, col, wp.itemStr(i, col));
					}
				}
			} catch (Exception e) {
				wp.showLogMessage("E", "", String.format("PARSE JSON錯誤[%s]", lineMessge));
				wp.showLogMessage("E", "", String.format("[%s]", e.getCause()));
			}

		}

		wp.jsonCode = "";
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
	  wp.initFlag = "Y";
	  wp.colSet("ex_date1", wp.sysDate);

  }
  
  @Override
  public void dddwSelect() {
		try {
			wp.initOption = "";
			wp.optionKey = wp.colStr("ex_line_source");
			
			
			dddwList("d_dddw_line_source", "PTR_SYS_IDTAB", "WF_ID", "WF_DESC || '' ", 
					"WHERE WF_TYPE ='LINE_SOURCE' AND ID_CODE = 'ccar0220' ORDER BY decode(ID_CODE2, '', '999', ID_CODE2 ) ");
			
			/** 若未設定參數 **/
			if (sqlRowNum == 0 ) {
				String optionKey = wp.colStr("ex_line_source");
				String tmp = String.format(defaultOption, "TCB_CARD_BUY".equals(optionKey) ? "selected" : "");
				wp.colSet("d_dddw_line_source", tmp);
			}
			
			
		} catch (Exception e) {

		}
  }


}
