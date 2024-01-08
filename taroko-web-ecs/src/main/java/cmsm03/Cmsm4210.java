/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE             Version    AUTHOR      DESCRIPTION                               *
* ---------        --------     ----------     ------------------------------------------ *
*  108-11-01                      JH                AJAX insert()
 * 108-10-31                      JH                detl-03
 * 108-09-10                      JH                redefine
 * 109-04-20                      shiyuqi       updated for project coding standard     *
*  109-01-09                      JustinWu     add six new columns and new selection of card_hldr_flag
*  109-10-07                      JustinWu     add new columns and the method getting MccCode
*  109-10-08                      JustinWu     change the way to get MCC code
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
* 111-12-30  V1.00.03   zuwei su       權益代碼選單資料:增讀取2個代碼: 15、16       
* 112-03-07  V1.00.04   machao         版面再調整:當年消費門檻                                                                         *    
* 112-05-29  V1.00.05   zuwei su       權益代碼:選單再增列item_no='12', 帳戶類別”選單再增列90,order by ACCT_TYPE                                                                         *    
******************************************************************************/
package cmsm03;

import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Cmsm4210 extends BaseAction {
String itemNo = "", projCode = "", dataKK3 = "", aprFlag = "";

@Override
public void userAction() throws Exception {	
		strAction = wp.buttonCode;
		switch (wp.buttonCode) {
		case "X":
			/* 轉換顯示畫面 */
			strAction = "new";
			clearFunc();
			// 當年消費門檻:預設要勾選
			wp.colSet("curr_cond", "Y");
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
		case "R2":
			/* 明細頁面查詢 */
			dataReadDetl();
		case "S2":
			/* 明細頁面查詢 */
			dataReadDetl();
			break;
		case "L":
			/* 清畫面 */
			strAction = "";
			clearFunc();
			break;
		case "C":
			// -異動處理-
			procFunc();
			break;
		case "C1":
			// -覆核處理-
			procApprove();
			break;
		
//		 case "A2": 
//		 //-新增明細- 
//		  insertDetl(); 
//		  break;
		 
		case "U2":
			// 明細存檔--
			updateDetl();
			break;
		case "C2":
			// 明細存檔--
			detlUpload();
			break;
		case "AJAX":
			// -AJAX:新增明細--
			ajaxFunc();
			break;
		default:
			break;
		}
}

@Override
public void dddwSelect() {
   try {
      if (eqIgno(wp.respHtml, "cmsm4210")) {
         wp.optionKey = wp.colStr("ex_item_no");
         dddwList("dddw_item_no", "ptr_sys_idtab"
               , "wf_id", "wf_id||'_'||wf_desc", "where wf_type = 'RIGHT_ITEM_NO' and wf_id  in ('08','09','10','11','12','15','16')  order by wf_id");
      }else if (eqIgno(wp.respHtml, "cmsm4210_detl")) {
    	  
         wp.optionKey = wp.colStr("kk_item_no");
         dddwList("dddw_item_no", "ptr_sys_idtab"
               , "wf_id", "wf_id||'_'||wf_desc", "where wf_type = 'RIGHT_ITEM_NO' and wf_id in ('08','09','10','11','12','15','16')  order by wf_id");
         //--
//         wp.optionKey =wp.colStr("air_mcc_group07");
//         dddwList("dddw_mcc_group07", "ptr_sys_idtab", "wf_id","wf_desc"
//               ,"where wf_type='CMS-MCC-GROUP'");
         //--
//         wp.optionKey =wp.colStr("A_mcc_group");
//         dddwList("dddw_a_mcc_group", "ptr_sys_idtab", "wf_id","wf_desc"
//               ,"where wf_type='CMS-MCC-GROUP'");
         
//         wp.optionKey =wp.colStr("D_mcc_group");
//         dddwList("dddw_d_mcc_group", "ptr_sys_idtab", "wf_id","wf_desc"
//               ,"where wf_type='CMS-MCC-GROUP'");
         
      }else if (eqIgno(wp.respHtml, "cmsm4210_detl_01")) {
         wp.optionKey = wp.colStr("ex_acct_type");
//         dddwList("dddw_acct_type", "ptr_acct_type"
//               , "acct_type", "acct_type||'_'||chin_name", "where 1=1 and acct_type='90' order by acct_type ");v
         dddwList("dddw_acct_type", "(select acct_type, chin_name from ptr_acct_type union select acct_type, chin_name from dbp_acct_type where acct_type='90') as t"
                 , "acct_type", "acct_type||'_'||chin_name", "where 1=1 order by acct_type ");
      }else if (eqIgno(wp.respHtml, "cmsm4210_detl_02") ||
                      pos(wp.respHtml,"_detl_03")>0 ) {
    	  
         wp.optionKey = wp.colStr("ex_data_code");
         dddwList("dddw_group_code", "ptr_group_code"
               , "group_code", "group_code||'_'||group_name", "where 1=1 ");

         wp.optionKey = wp.colStr("ex_data_code2");
         dddwList("dddw_card_type", "ptr_card_type"
               , "card_type", "card_type||'_'||name", "where 1=1 ");
         
      }else if ( eqIgno(wp.respHtml, "cmsm4210_detl_04") ||
                  eqIgno(wp.respHtml, "cmsm4210_detl_05")  ) {
      	  
           wp.optionKey = wp.colStr("ex_data_code");
           dddwList("dddw_group_code", "ptr_group_code"
                 , "group_code", "group_code||'_'||group_name", "where 1=1 ");

      }else if ( eqIgno(wp.respHtml, "cmsm4210_detl_06") || 
                  eqIgno(wp.respHtml, "cmsm4210_detl_07") ||
                  eqIgno(wp.respHtml, "cmsm4210_detl_08")) {
    	  
         wp.optionKey = wp.colStr("ex_data_code");
         dddwList("dddw_group_code", "ptr_group_code"
               , "group_code", "group_code||'_'||group_name", "where 1=1 ");
         
         wp.optionKey = wp.colStr("ex_data_code2");
         dddwList("dddw_card_type", "ptr_card_type"
               , "card_type", "card_type||'_'||name", "where 1=1 ");
         
         wp.optionKey = wp.colStr("ex_data_code3");
// 2020-10-08: Justin Wu: change the way to get MCC code
         dddwList("dddw_mcc_code", "ptr_sys_idtab"
                 , "WF_ID", "WF_ID||'_'||WF_DESC", "where 1=1 AND wf_type='CMS-MCC-GROUP' order by WF_ID");
        
//         dddwList("dddw_mcc_code", "cca_mcc_risk"
//               , "mcc_code", "mcc_code||'_'||mcc_remark", "where 1=1 order by mcc_code");
         
         
      }
      
   } catch (Exception ex) {
   }

}

@Override
public void queryFunc() throws Exception {

   String lsWhere = " where 1=1 "
         + sqlCol(wp.itemStr("ex_item_no"), "item_no");

   if (!wp.itemEq("ex_active_status", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_active_status"), "active_status");
   }

   if (!wp.itemEq("ex_apr_flag", "0")) {
      lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();

   wp.selectSQL = " apr_flag , "
         + " item_no , "
         + " proj_code , "
         + " active_status , "
         //			 		 + " item_desc , "
         + " to_char(mod_time,'yyyymmdd') as mod_date , "
         + " mod_user , "
         + " apr_date , "
         + " apr_user , "
         + " decode(active_status,'Y','有效','N','取消') as tt_active_status , "
         + " proj_desc , "
         + " mod_seqno , " +
         " uf_tt_idtab('RIGHT_ITEM_NO',item_no) as tt_item_no"
   //			 		 + " (select wf_id||'_'||wf_desc from ptr_sys_idtab where wf_type = 'RIGHT_ITEM_NO' and wf_id = item_no) as wk_item_no "
   ;

   wp.daoTable = " cms_right_parm ";
   wp.whereOrder = " order by item_no ";

   pageQuery();
   wp.setListCount(0);
   if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   wp.setPageValue();

}

@Override
public void querySelect() throws Exception {
   itemNo = wp.itemStr2("data_k1");
   projCode = wp.itemStr2("data_k2");

   dataRead();
}

@Override
public void dataRead() throws Exception {
   if (empty(itemNo)) {
      itemNo = itemkk("item_no");
   }
   if (empty(projCode)) {
      projCode = itemkk("proj_code");
   }
   wp.selectSQL = " A.*, hex(A.rowid) as rowid"+
         ",uf_tt_idtab('RIGHT_ITEM_NO',A.item_no) as tt_item_no"+
      ", decode(A.debut_group_cond,'0','不檢核','1','指定','2','排除','') as tt_debut_group_cond "+
      ", A.debut_group_cond as wk_debut_group_cond";

   wp.daoTable = " cms_right_parm A ";
   wp.whereStr = " where 1=1 "
         + sqlCol(itemNo, "A.item_no")
         + sqlCol(projCode, "A.proj_code")
   ;
   wp.whereOrder = " order by A.apr_flag "
         + commSqlStr.rownum(1)
   ;
   pageSelect();
   if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
   }

   if (wp.colEq("apr_flag", "N")) {
      alertMsg("資料待覆核");
   }
   if (wp.colEq("curr_cond", "Y")) {
	 if(wp.colNum("last_mm")>0) {
		 wp.colSet("choose_cond", "2");
	 }else {
		 wp.colSet("choose_cond", "1"); 
	 }    
   }

}

void dataReadDetl() throws Exception {
   wp.pageRows = 999;

   itemNo = wp.itemStr2("item_no");
   projCode = wp.itemStr2("proj_code");
   dataKK3 = wp.itemStr2("data_k3");
   aprFlag = wp.itemStr2("apr_flag");
   //-master-
   selectCmsRightParm();

   if (empty(dataKK3)) dataKK3 = wp.itemStr2("data_type");

   wp.daoTable = " cms_right_parm_detl A";
   wp.whereStr = " where A.table_id = 'RIGHT' "
         + sqlCol(itemNo, "A.item_no")
         + sqlCol(projCode, "A.proj_code")
         + sqlCol(dataKK3, "A.data_type")
         + sqlCol(aprFlag, "A.apr_flag")
   ;
   wp.whereOrder = " order by A.data_code ";
   if (eqIgno(dataKK3,"01")) {   
      wp.selectSQL =" A.data_code, uf_tt_acct_type(A.data_code) as tt_data_code";
    
   } else if (eqIgno(dataKK3,"02")) {
      wp.selectSQL =" A.data_code , uf_tt_group_code(A.data_code) as tt_data_code , "
      				 +" A.data_code2 , uf_tt_card_type(A.data_code2) as tt_data_code2 "
      				 ;
   } else if (eqIgno(dataKK3,"03")) {
      wp.selectSQL =" A.data_code , uf_tt_group_code(A.data_code) as tt_data_code , "
         +" A.data_code2 , uf_tt_card_type(A.data_code2) as tt_data_code2 "
      ;
   } else if (eqIgno(dataKK3,"05")) {
      wp.selectSQL =" A.data_code , uf_tt_group_code(A.data_code) as tt_data_code ";
      
   } else if (eqIgno(dataKK3,"06") || eqIgno(dataKK3,"07") || eqIgno(dataKK3,"08")) {
      wp.selectSQL =" A.data_code , uf_tt_group_code(A.data_code) as tt_data_code ,"
    		              +" A.data_code2 , uf_tt_card_type(A.data_code2) as tt_data_code2 ,"
    		              +" A.data_code3 , uf_tt_mcc_code(A.data_code3) as tt_data_code3 ";
   } 

   pageQuery();
   wp.setListCount(0);
   if (sqlNotFind()) {
      selectOK();
      return;
   }
//   dataReadDetl_After();
}

void selectCmsRightParm() {
   String sql1 ="select debut_group_cond from cms_right_parm"+
      " where 1=1"+sqlCol(itemNo,"item_no")+
      sqlCol(projCode,"proj_code")+sqlCol(aprFlag,"apr_flag");
   sqlSelect(sql1);
   if (sqlRowNum >0) {
      wp.colSet("debut_group_cond",sqlStr("debut_group_cond"));
   }
}

//void dataReadDetl_After() {
//
//}

@Override
public void saveFunc() throws Exception {

   if (isDelete() && wp.itemEq("apr_flag", "Y")) {
      if (checkApproveZz() == false) return;
   }

   cmsm03.Cmsm4210Func func = new cmsm03.Cmsm4210Func();
   func.setConn(wp);

   rc = func.dbSave(strAction);
   sqlCommit(rc);

   if (rc != 1) {
      alertErr2(func.getMsg());
   } else saveAfter(true);

}

//void insertDetl() throws Exception {
//   wp.listCount[0] = wp.itemRows("data_code");
//
//   if (wp.itemEq("apr_flag","Y")) {
//      alertErr("資料己覆核, 不可新增");
//      return;
//   }
//
//   Cmsm4210Func func =new Cmsm4210Func();
//   func.setConn(wp);
//   rc =func.insertDetl();
//   sqlCommit(rc);
//   if (rc ==-1) {
//      alertErr(func.getMsg());
//   }
//}

void updateDetl() throws Exception {
   int ilOk = 0, ilErr = 0;

   cmsm03.Cmsm4210Func func = new cmsm03.Cmsm4210Func();
   func.setConn(wp);

   String[] aaOpt = wp.itemBuff("opt");
   wp.listCount[0] = wp.itemRows("data_code");

   //--已覆核不可修改明細
   if (wp.itemEq("apr_flag", "Y")) {
      alertErr2("已覆核不可修改 !");
      return;
   }

   rc =func.updateRightParm();
   if (rc ==-1) {
      sqlCommit(rc);
      alertErr(func.getMsg());
      return;
   }

//   if (opt_2index(aa_opt[0])<0) {
//      alert_err("未點選 [刪除] 資料");
//      return;
//   }
   for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr =optToIndex(aaOpt[ii]);
      if (rr<0) continue;

      optOkflag(rr);
      if (func.deleteDetl(rr) == 1) {
         ilOk++;
         optOkflag(rr,1);
      }
      else {
         ilErr++;
         optOkflag(rr,-1);
      }
   }

   if (ilOk > 0) sqlCommit(1);

   dataReadDetl();

   alertMsg("處理結果: 成功:" + ilOk + " , 失敗:" + ilErr);
}

@Override
public void procFunc() throws Exception {

   cmsm03.Cmsm4210Func func = new cmsm03.Cmsm4210Func();
   func.setConn(wp);

   rc = func.dataProc();
   sqlCommit(rc);
   if (rc != 1) {
      errmsg(func.getMsg());
   } else {
      alertMsg("異動處理完成");
      dataRead();
   }
}

void procApprove() throws Exception {
   int ilOk = 0, ilErr = 0;

   cmsm03.Cmsm4210Func func = new cmsm03.Cmsm4210Func();
   func.setConn(wp);

   String[] lsProjCode = wp.itemBuff("proj_code");
   String[] lsItemNo = wp.itemBuff("item_no");
   String[] lsAprFlag = wp.itemBuff("apr_flag");
   String[] aaOpt = wp.itemBuff("opt");
   this.optNumKeep(lsProjCode.length, aaOpt);
   wp.listCount[0] = wp.itemRows("proj_code");

   int rr = -1;
   rr = optToIndex(aaOpt[0]);

   if (rr < 0) {
      alertErr2("請點選欲覆核資料");
      return;
   }

   if (checkApproveZz() == false) return;

   for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) {
         continue;
      }
      // 若已覆核，則無法再覆核。
      if(lsAprFlag[rr].equals("Y")) {
    	  ilErr++;
          wp.colSet(rr, "ok_flag", "X");
          continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("proj_code", lsProjCode[rr]);
      func.varsSet("item_no", lsItemNo[rr]);

      rc = func.dataApprove();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
   }

   alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);

}

void detlUpload() throws Exception {
   if (wp.itemEq("apr_flag","Y")) {
      alertErr("已覆核不可匯入資料");
      return;
   }
   if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
   }

   TarokoFileAccess tf = new TarokoFileAccess(wp);

   String inputFile = wp.itemStr("zz_file_name");
   int fi = tf.openInputText(inputFile, "MS950");
   if (fi == -1) {
      return;
   }
//   String ls_file_err =inputFile+"-"+wp.sysTime+".err";
//   int file_err =tf.openOutputText(ls_file_err,"MS950");

   wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));
   String[] tt = new String[]{"",","};
   int llOk = 0, llCnt = 0, llErr=0;
   Cmsm4210Func func =new Cmsm4210Func();
   func.setConn(wp);

   if (func.deleteAllDetl() == -1) {
      alertErr(func.getMsg());
      return;
   }

   int ll=0; //avoid 無窮LOOP
   while (ll<9999) {
      ll++;

      String file = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
         break;
      }
      if (file.length() < 2) {
         continue;
      }
      llCnt++;

      tt[0] =file;
      String lsData1 =commString.token(tt);
      String lsData2 =commString.token(tt);
      wp.itemSet("ex_data_code",lsData1);
      wp.itemSet("ex_data_code2",lsData2);
      int liRc =func.insertDetl();
      if (liRc ==1)
         llOk++;
      else if (liRc ==-1) {
         llErr++;
         alertErr("匯入失敗: "+file+func.getMsg());
         break;
//         String tt2 =ss+";"+func.getMsg();
//         tf.writeTextFile(file_err,tt2+wp.newLine);
      }
   }
   if (llErr>0) {
      sqlCommit(-1);
   }
   else {
      sqlCommit(1);
   }

//   tf.writeTextFile(file_err,"資料處理完成"+wp.newLine);
//   tf.closeOutputText(file_err);
   tf.closeInputText(fi);
   tf.deleteFile(inputFile);
   if (llErr>0) return;

//   if (ll_err >0) {
//      wp.setDownload(ls_file_err);
//      //wp.setDownload2(ls_file_err);
//   }
   wp.colSet("zz_file_name","");
   alertMsg("資料匯入處理筆數: " + llCnt + ", 成功筆數=" + llOk+", 失敗筆數="+llErr);
   return;

}
//
//public void wf_insert(TarokoCommon wr) throws Exception {
//   wp =wr;
//   ajax_Func();
//   return;
//}
void ajaxFunc() throws Exception {
   if (wp.itemEq("apr_flag","Y")) {
      alertErr("資料己覆核, 不可新增");
      return;
   }

   Cmsm4210Func func =new Cmsm4210Func();
   func.setConn(wp);
   rc =func.insertDetl();
   sqlCommit(rc);
   if (eqIgno(strAction,"AJAX")) {
      wp.addJSON("ax_rc",""+rc);
      if (rc==1) {
         wp.addJSON("ax_msg","");
      }
      else 
    	  wp.addJSON("ax_msg", func.getMsg());
      return;
   }
   if (rc ==-1) {
      alertErr(func.getMsg());
   }
}

@Override
public void initButton() {
   if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
      if (wp.colEmpty("apr_flag") || wp.colEq("apr_flag", "N")) {
         //wp.col_set("btnProc_disable", "disabled style='background: lightgray;'");
         buttonOff("btnProc_off");
      }
      else if (wp.colEq("apr_flag", "Y")) {
         //wp.col_set("btnUpdate_disable", "disabled style='background: lightgray;'");
         buttonOff("btnUpdate_off");
      }
   }

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
