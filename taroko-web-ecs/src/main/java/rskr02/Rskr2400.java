package rskr02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskr2400 extends BaseAction implements InfacePdf {
	taroko.base.CommDate commDate = new taroko.base.CommDate();
	String wkSysDate = "";
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
	      case "PDF":
   	    // -PDF列印-
	    	pdfPrint();
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
		
		if(chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
			alertErr("建檔日期: 起迄錯誤");
			return ;
		}
		
		String lsWhere = " where 1=1 and A.apr_flag ='Y' "
					   + sqlCol(wp.itemStr("ex_crt_date1"),"A.crt_date",">=")
					   + sqlCol(wp.itemStr("ex_crt_date2"),"A.crt_date","<=")
					   + sqlCol(wp.itemStr("ex_review_date"),"A.review_date")
					   ;
		
		if(wp.itemEmpty("ex_idno") == false) {
			lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "
					+ sqlCol(wp.itemStr("ex_idno"),"id_no")
					+ " ) "
					;
		}
		
		wp.whereStr = lsWhere ;
		wp.queryWhere = wp.whereStr;
		wp.setQueryMode();
		queryRead();
	}

	@Override
	public void queryRead() throws Exception {
		wp.pageControl();
		wp.selectSQL = "A.acct_type , B.acct_key , A.block_code , A.review_date , A.crt_date , A.print_flag , A.acno_p_seqno , hex(A.rowid) as rowid , "
					 + "B.bill_sending_zip , B.bill_sending_addr1||B.bill_sending_addr2||B.bill_sending_addr3 as addr1 , "
					 + "B.bill_sending_addr4||B.bill_sending_addr5 as addr2 , "
					 + "(select chi_name from crd_idno where id_p_seqno = A.id_p_seqno) as chi_name , "
					 + "(select wf_desc from ptr_sys_idtab where wf_type ='REFUND' and wf_id = A.block_code) as block_desc "
					 ;
		wp.daoTable = "rsk_review_block A join act_acno B on A.acno_p_seqno = B.acno_p_seqno ";
		wp.whereOrder = " order by A.crt_date , B.acct_key , A.block_code ";
		pageQuery();
		
		if(sqlNotFind()) {
			alertErr("此條件查無資料");
			return ;
		}
		
		wp.setListCount(0);
		wp.setPageValue();
		
		queryAfter();
	}
	
	void queryAfter() throws Exception {
		
		String sql1 = "" , sql2 = "";
		sql1 = " select count(*) as db_card_cnt from crd_card where current_code ='0' and acno_p_seqno = ? ";
		sql2 = " select count(*) as db_cnt2 from col_liac_nego where id_p_seqno = ? ";
		
		int rr = wp.selectCnt ;
		
		for(int ii=0;ii<rr;ii++) {
			sqlSelect(sql1,new Object[] {wp.colStr(ii,"acno_p_seqno")});
			if(sqlRowNum <= 0) {
				wp.colSet(ii,"opt_show", "disabled");
				continue;
			}	else {
				if(sqlNum("db_card_cnt") <=0 ) {
					wp.colSet(ii,"opt_show", "disabled");
					continue;
				}	else
					wp.colSet(ii,"opt_show", "");
			}
			
			sqlSelect(sql2,new Object[] {wp.colStr(ii,"id_p_seqno")});
			if(sqlRowNum <= 0) {
				wp.colSet(ii,"opt_show", "");
				continue;
			}	else {
				if(sqlNum("db_cnt2") <=0 ) {
					wp.colSet(ii,"opt_show", "");
					continue;
				}	else
					wp.colSet(ii,"opt_show", "disabled");
			}
		}
		
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
		// TODO Auto-generated method stub

	}

	@Override
	public void pdfPrint() throws Exception {
		wp.reportId = "rskr0550";
	    dataPrint();
	    if (wp.listCount[0] == 0) {
	      alertErr2("請選擇要列印資料");
	      wp.respHtml = "TarokoErrorPDF";
	      return;
	    }
	    TarokoPDF pdf = new TarokoPDF();
	    pdf.pageVert = false;

	    wp.fileMode = "Y";
	    pdf.excelTemplate = "rskr2400.xlsx";
	    pdf.pageCount = 1;
	    pdf.sheetNo = 0;
	    pdf.pageVert = true;
	    pdf.procesPDFreport(wp);
	    pdf = null;
	    sqlCommit(1);
	    return;
		
	}
	
	void dataPrint() throws Exception {		
		int ii = 0;
		
		getTwDate();

		String[] aaOpt = wp.itemBuff("opt");		
		String[] lsChiName = wp.itemBuff("chi_name");		
		String[] lsBlockDesc = wp.itemBuff("block_desc");
		String[] lsAddrZip = wp.itemBuff("bill_sending_zip");
		String[] lsAddr1 = wp.itemBuff("addr1");
		String[] lsAddr2 = wp.itemBuff("addr2");    		
		String[] lsRowid = wp.itemBuff("rowid");
		    
		wp.listCount[0] = wp.itemRows("rowid");
		int zz = 0;
		for (int rr = 0; rr < aaOpt.length; rr++) {			
			ii = (int) optToIndex(aaOpt[rr]);
		    if (ii < 0) {		    	
		        continue;
		    }
		    wp.colSet(zz, "ex_addr_zip", lsAddrZip[ii]);
		    wp.colSet(zz, "ex_addr1", lsAddr1[ii]);
		    wp.colSet(zz, "ex_addr2", lsAddr2[ii]);
		    wp.colSet(zz, "ex_tx_rsn_1", "　　承蒙您使用本行信用卡，不勝感激。");
		    wp.colSet(zz, "ex_tx_rsn_2", "惟查　台端因【"+lsBlockDesc[ii]+"】");
		    wp.colSet(zz, "ex_tx_rsn_3", "故自即日起暫停　台端信用卡之使用。特此通知，如有不便尚祈鑒諒。");
		    wp.colSet(zz, "ex_chi_name", "　"+lsChiName[ii]+"　先生/小姐");		    
		    wp.colSet(zz, "ex_temp1", "　　　　　　　順　　　　頌");      
		    wp.colSet(zz, "ex_temp2", "　　安　　　　　祺");
		    wp.colSet(zz, "ex_tel1", "電話：0800-033175");      
		    wp.colSet(zz, "ex_tel2", "　　　（02）23319370");
		    wp.colSet(zz, "ex_tel3", "　　　（04）22273131");
		    wp.colSet(zz, "wk_sys_date", wkSysDate);
		    zz++;
		    updateLog(lsRowid[ii]);
		}
		wp.listCount[0] = zz;
	}	
	
	  void getTwDate() {		  
		  String lsDate = "" , lsMonth = "" , lsDay = "" , lsYear = "";
		  lsDate = commDate.sysDate();
		  lsDate = commDate.toTwDate(lsDate);
		  if(lsDate.length() == 7) {
			  lsYear = lsDate.substring(0, 3);
			  lsMonth = lsDate.substring(3, 5);    
			  lsDay = lsDate.substring(5, 7);
		  }	else if(lsDate.length() == 6) {
			  lsYear = lsDate.substring(0, 2);
			  lsMonth = lsDate.substring(2, 4);    
			  lsDay = lsDate.substring(4, 6);
		  }
		  
		  wkSysDate = "中　　華　　民　　國";
		  //--年
		  String temp = "";
		  int tempInt = 0;
		  for(int ii=0;ii<lsYear.length();ii++) {
			  tempInt = commString.strToInt(lsYear.substring(ii,ii+1));
			  temp = convertChinese(tempInt);
			  wkSysDate += "　　"+temp;
		  }
		  
		  wkSysDate += "　　年";
		  
		  tempInt = commString.strToInt(lsMonth);
		  temp = convertChinese(tempInt);
		  wkSysDate += "　　"+temp;
		  wkSysDate += "　　月";
		  
		  tempInt = commString.strToInt(lsDay);
		  temp = convertChinese(tempInt);
		  wkSysDate += "　　"+temp;
		  wkSysDate += "　　日";

//		  wkSysDate = "中　　華　　民　　國" + lsDate.substring(0, 3) + "　 年 　" + commString.strToInt(lsMonth) + "　 月　" + commString.strToInt(lsDay) + "　日";
	  }
	  
	  String convertChinese(int z) {
		  String temp = "";
		  switch (z) {
		  	case 1:
		  		temp = "一";
		  		break;
		  	case 2:	
		  		temp = "二";
		  		break;
		  	case 3:	
		  		temp = "三";
		  		break;
		  	case 4:	
		  		temp = "四";
		  		break;
		  	case 5:	
		  		temp = "五";
		  		break;
		  	case 6:	
		  		temp = "六";
		  		break;
		  	case 7:	
		  		temp = "七";
		  		break;
		  	case 8:	
		  		temp = "八";
		  		break;
		  	case 9:	
		  		temp = "九";
		  		break;
		  	case 10:	
		  		temp = "十";
		  		break;
		  	case 11:	
		  		temp = "十　　一";
		  		break;
		  	case 12:	
		  		temp = "十　　二";
		  		break;
		  	case 13:	
		  		temp = "十　　三";
		  		break;
		  	case 14:	
		  		temp = "十　　四";
		  		break;
		  	case 15:	
		  		temp = "十　　五";
		  		break;
		  	case 16:	
		  		temp = "十　　六";
		  		break;
		  	case 17:	
		  		temp = "十　　七";
		  		break;
		  	case 18:	
		  		temp = "十　　八";
		  		break;
		  	case 19:	
		  		temp = "十　　九";
		  		break;
		  	case 20:	
		  		temp = "二　　十";
		  		break;
		  	case 21:	
		  		temp = "二　　十　　一";
		  		break;
		  	case 22:	
		  		temp = "二　　十　　二";
		  		break;
		  	case 23:	
		  		temp = "二　　十　　三";
		  		break;
		  	case 24:	
		  		temp = "二　　十　　四";
		  		break;
		  	case 25:	
		  		temp = "二　　十　　五";
		  		break;
		  	case 26:	
		  		temp = "二　　十　　六";
		  		break;
		  	case 27:	
		  		temp = "二　　十　　七";
		  		break;
		  	case 28:	
		  		temp = "二　　十　　八";
		  		break;
		  	case 29:	
		  		temp = "二　　十　　九";
		  		break;
		  	case 30:	
		  		temp = "三　　十";
		  		break;
		  	case 31:	
		  		temp = "三　　十　　一";
		  		break;
		  }
		  return temp;
	  }
	  
	  void updateLog(String aRowid) throws Exception {	  
		  String sql1 = "";

		  sql1 = " update rsk_review_block set print_flag ='Y' ";	  
		  sql1 += " where 1=1 and rowid = ? ";	  
		  setRowid(1,aRowid);
		  sqlExec(sql1);
	  }
	  
}
