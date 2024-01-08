package rskm02;
/**
 * 2023-0919   JH    ++delete
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoFileAccess;

public class Rskm2400 extends BaseAction {
String reviewDate = "";

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
      case "A": /* 新增功能 */
         strAction ="A";
         saveFunc();
         break;
      case "U": /* 更新功能 */
         strAction ="U";
         saveFunc();
         break;
      case "D": /* 刪除功能 */
         strAction ="D";
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
   if (wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_crt_date1") && wp.itemEmpty("ex_crt_date2") && wp.itemEmpty("ex_review_date")) {
      alertErr("篩選條件: 不可全部空白");
      return;
   }

   String lsWhere = " where 1=1 "
       +sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
       +sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
       +sqlCol(wp.itemStr("ex_review_date"), "review_date");

   if (wp.itemEq("ex_apr_flag", "0") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
   }

   if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"), "id_no")+")";
   }

   wp.whereStr = lsWhere;
   wp.queryWhere = wp.whereStr;
   wp.setQueryMode();

   queryRead();
}

@Override
public void queryRead() throws Exception {
   wp.pageControl();
   wp.selectSQL = " acct_type , uf_acno_key(acno_p_seqno) as acct_key , block_code , review_date , crt_date , "
       +" apr_date , apr_user "
      +", hex(rowid) rowid, acno_p_seqno "
   ;
   wp.daoTable = "rsk_review_block";
   wp.whereOrder = " order by 2 Asc ";
   pageQuery();
   if (sqlNotFind()) {
      alertErr("此條件查無資料");
      return;
   }
   wp.setListCount(0);
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
   String[] aaRowid = wp.itemBuff("rowid");
//   String[] aaModSeqno = wp.itemBuff("mod_seqno");
   String[] aaAprdate = wp.itemBuff("apr_date");
   String[] aaOpt = wp.itemBuff("opt");
   wp.listCount[0] =aaRowid.length;
   if (optToIndex(aaOpt[0])<0) {
      alertErr("請點選欲刪除資料");
      return;
   }

   Rskm2400Func func=new Rskm2400Func();
   func.setConn(wp);

   int rr = -1;
   int ilOk=0, ilErr=0;
   for (int ii = 0; ii < aaOpt.length; ii++) {
      rr =optToIndex(aaOpt[ii]);
      if (rr < 0) continue;

      wp.colSet(rr, "ok_flag", "-");
      wp.itemSet("rowid_delete", aaRowid[rr]);

      rc = func.dbDelete();
      sqlCommit(rc);
      if (rc == 1) {
         wp.colSet(rr, "ok_flag", "V");
         ilOk++;
         continue;
      }
      ilErr++;
      wp.colSet(rr, "ok_flag", "X");
   }

   alertMsg("刪除處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
}

@Override
public void procFunc() throws Exception {
   if (itemIsempty("zz_file_name")) {
      alertErr2("上傳檔名: 不可空白");
      return;
   }

//		if(wp.itemStr("zz_file_name").indexOf("CARDVICancelCard") <0) {
//			alertErr("上傳檔名有誤 , 檔名為: CARDVICancelCard_yyyymmdd.txt");
//			return ;
//		}
//		
//		if(wp.itemStr("zz_file_name").length() != 29) {
//			alertErr("上傳檔名有誤 , 檔名為: CARDVICancelCard_yyyymmdd.txt");
//			return ;
//		}
//		
//		reviewDate = wp.itemStr("zz_file_name").substring(17,25);
   reviewDate = getSysDate();
   fileDataImp();
}

void fileDataImp() throws Exception {
   TarokoFileAccess tf = new TarokoFileAccess(wp);

   String inputFile = wp.itemStr("zz_file_name");
   int fi = tf.openInputText(inputFile, "MS950");
   if (fi == -1) {
      return;
   }

   String lsErrFile = inputFile+"_err"+"_"+wp.sysTime+".txt";
   TarokoFileAccess oofile = new TarokoFileAccess(wp);
   int liFileNum = -1;

   Rskm2400Func func = new Rskm2400Func();
   func.setConn(wp);

   int llOk = 0, llCnt = 0;
   int llErr = 0;
   boolean lbErrFile = false;
   String newLine = "\r\n";
   while (true) {
      String tmpStr = tf.readTextFile(fi);
      if (tf.endFile[fi].equals("Y")) {
         break;
      }
      if (tmpStr.length() < 2) {
         continue;
      }
      llCnt++;
      String lsAcctType = commString.midBig5(tmpStr, 0, 2);
      String lsIdNo = commString.midBig5(tmpStr, 2, 10);
      String lsBolckCode = commString.midBig5(tmpStr, 12, 2);
      String lsIdPSeqno = getIdpseqno(lsIdNo);
      String lsAcctKey = lsIdNo+"0";
      String lsAcnoPSeqno = getAcnoPSeqno(lsAcctType, lsAcctKey);

      if (empty(lsIdPSeqno) || empty(lsAcnoPSeqno)) {
         if (lbErrFile == false) {
            lbErrFile = true;
            liFileNum = oofile.openOutputText(lsErrFile, "MS950");
         }
         String errorDesc = "";
         errorDesc = lsAcctType+lsIdNo+lsBolckCode+"  錯誤原因:";
         if (empty(lsIdPSeqno))
            errorDesc += "查無卡人資訊"+newLine;
         else if (empty(lsAcnoPSeqno))
            errorDesc += "查無帳戶資訊"+newLine;
         oofile.writeTextFile(liFileNum, errorDesc);
         continue;
      }

      wp.itemSet("acct_type", lsAcctType);
      wp.itemSet("id_p_seqno", lsIdPSeqno);
      wp.itemSet("acno_p_seqno", lsAcnoPSeqno);
      wp.itemSet("block_code", lsBolckCode);
      wp.itemSet("review_date", reviewDate);

      if (func.dbInsert() == 1) {
         llOk++;
         this.sqlCommit(1);
         continue;
      } else {
         llErr++;
         if (lbErrFile == false) {
            lbErrFile = true;
            liFileNum = oofile.openOutputText(lsErrFile, "MS950");
         }
         String errorDesc = "";
         errorDesc = lsAcctType+lsIdNo+lsBolckCode+"  錯誤原因:資料已存在"+newLine;
         oofile.writeTextFile(liFileNum, errorDesc);
         continue;
      }
   }

   if (llErr > 0) {
      oofile.closeOutputText(liFileNum);
      wp.setDownload(lsErrFile);
   }

   tf.closeInputText(fi);
   tf.deleteFile(inputFile);
   alertMsg("資料匯入處理筆數: "+llCnt+", 成功筆數="+llOk+" 錯誤筆數:"+llErr);
   return;
}

String getIdpseqno(String aIdno) {
   String sql1 = " select id_p_seqno from crd_idno where id_no = ? ";
   sqlSelect(sql1, new Object[]{aIdno});

   if (sqlRowNum > 0) {
      return sqlStr("id_p_seqno");
   }

   return "";
}

String getAcnoPSeqno(String aAcctType, String aAcctKey) {
   String sql1 = " select acno_p_seqno from act_acno where acct_type = ? and acct_key = ? ";
   sqlSelect(sql1, new Object[]{aAcctType, aAcctKey});

   if (sqlRowNum > 0) {
      return sqlStr("acno_p_seqno");
   }

   return "";
}

@Override
public void initButton() {
   // TODO Auto-generated method stub

}

@Override
public void initPage() {
   // TODO Auto-generated method stub

}

}
