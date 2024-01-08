/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/24  V1.00.01    JeffKung  program initial                           *
 *                                                                             * 
 ******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*每半年產生淘寶網刷卡消費交易明細媒體檔案*/
public class BilN004 extends AccessDAO {
    private boolean debug = false;
    private String progname = "每半年產生淘寶網刷卡消費交易明細媒體檔案   112/02/24 V1.00.01 ";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CommFTP commFTP = null;
	CommRoutine comr = null;

    String prgmId = "BilN004";
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hBusinessDate = "";
    String hStartDate = "";
    String hEndDate = "";
    String sqlSt = "";
    String finalTotalAmt = "";
    String hprocessDate = "";
    String hPurchaseDate = "";
    String hPostDate = "";
    String hCardNo = "";
    String hId = "";
    String hMchtEngName = "";
    String hBillAmt1 = "";
    String hSourceCurrency = "";
    String hMchtCountry = "";
    String hSourceAmt1 = "";
    double hBillAmtOrigi = 0;

    int totalCnt = 0;
    double totalAmt = 0;

    public int mainProcess(String[] args) {

        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length == 2 && args[0].length() == 8 && args[1].length() == 8) {
                hStartDate = args[0];
                hEndDate = args[1];
            } else if (args.length != 0) {
                showLogMessage("I", "", "Usage   : BilN004 [start_date  end_date]");
                String err1 = "Example : bil_n004                    (找最近半年資料0101~0630或0701~1231)";
                String err2 = "        : bil_n004 20160701 20161231  (指定日期區間)";
                comc.errExit(err1, err2);
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			int runFlag = selectStartEndDate(args);
			if (runFlag==1) {
				showLogMessage("I","","執行日期為1/1及7/1,本日非執行日期=["+hBusinessDate+"]");
				return 0;
			}

            showLogMessage("I","","   營業日期=["+hBusinessDate+"]");
            showLogMessage("I","","處理資料區間=["+hStartDate+"]~["+hEndDate+"]");

            
            //開啟檔案
			String filename1 = String.format("%s/media/bil/006ALIPAY_%8.8s.TXT", comc.getECSHOME(), hBusinessDate);
		    if ( openBinaryOutput(filename1) == false) {
		    	showLogMessage("E", "", String.format("無法開啟回覆檔案[%s]", filename1));
		    	return 1;
		    }

            selectBilFiscdtl();

            //寫出檔尾資料
            buf = String.format("%5.5s%3.3s%08d%s\r\n", "TOTAL", "006", totalCnt, encriptAmt(totalAmt, "%018.0f"));
            writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
			
			closeBinaryOutput2(0);
			
			procFTP(String.format("006ALIPAY_%8.8s.TXT",hBusinessDate));

            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    int selectStartEndDate(String [] args) throws Exception {

        sqlCmd  = "select business_date ";
        sqlCmd += " from ptr_businday fetch first 1 rows only";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        
        hBusinessDate = getValue("business_date");
        int hYear = comc.str2int(comc.getSubString(hBusinessDate,0,4));
        
        //沒有帶入日期區間參數時才跑以下段落
        if (args.length == 0) {
        	if (!"0701".equals(comc.getSubString(hBusinessDate, 4)) 
        		&& !"0101".equals(comc.getSubString(hBusinessDate, 4))) {
        		return 1;
        	}
        	
        	if (comc.getSubString(hBusinessDate, 4).compareTo("0701") >=0 ) {
        		hStartDate = String.format("%d",hYear) + "0101";
        		hEndDate = String.format("%d",hYear) + "0630";
        	} else {
        		hYear = hYear -1;
        		hStartDate = String.format("%d",hYear) + "0701";
        		hEndDate = String.format("%d",hYear) + "1231";
        	}
        }
        
        return 0;
    }
    
    /***********************************************************************/
    void selectBilFiscdtl() throws Exception {
    	
    	String signFlag = "";
    	double hSourceAmt = 0.0;
    	
        sqlCmd =  "SELECT a.ecs_real_card_no,a.purchase_date,a.batch_date,a.mcht_eng_name,a.mcht_country, ";
        sqlCmd += "       a.dest_curr, a.dest_amt, a.source_curr, a.source_amt, a.process_day, ";
        sqlCmd += "       a.ecs_sign_code, a.ecs_tx_code, a.ecs_debit_flag, nvl(b.curr_eng_name,'OTH') as curr_eng_name  ";
        sqlCmd += "FROM bil_fiscdtl a ";
        sqlCmd += "     left join ptr_currcode b on a.source_curr = b.curr_code ";
        sqlCmd += "WHERE a.purchase_date BETWEEN ? AND ? ";
        sqlCmd += "AND ( a.mcht_eng_name like 'ZHI FU BAO%%' ";
        sqlCmd += "   or a.mcht_eng_name like '%%ALIPAY%%' ";
        sqlCmd += "   or a.mcht_eng_name like '%%www.taobao.com%%') ";
        sqlCmd += "AND a.ecs_tx_code IN ('05','06','07','25','26','27') ";

        setString(1, hStartDate);
        setString(2, hEndDate);
        
        openCursor();
        while (fetchTable()) {
        	if ("Y".equals(getValue("ecs_debit_flag"))) {
        		hId = selectDbcCard(getValue("ecs_real_card_no"));
        	} else {
        		hId = selectCrdCard(getValue("ecs_real_card_no"));	
        	}
        	
        	//讀卡檔找不到bypass
        	if ("".equals(hId)) continue;
            
            signFlag = getValue("ecs_sign_code");
            if ("-".equals(signFlag)) {
                hSourceAmt = getValueDouble("source_amt") * -1;
                hBillAmtOrigi = getValueDouble("dest_amt") * -1;
            } else {
                hSourceAmt = getValueDouble("source_amt");
                hBillAmtOrigi = getValueDouble("dest_amt");
            }
            hPurchaseDate = toChinDate(getValue("purchase_date"));
            hPostDate = toChinDate(getValue("batch_date"));
            hCardNo = getValue("ecs_real_card_no");
            hMchtEngName = getValue("mcht_eng_name");
            hprocessDate = julDate(getValue("process_day"));
            if ("".equals(hprocessDate)) {
            	hprocessDate = hPostDate;
            } else {
            	hprocessDate = toChinDate(hprocessDate);
            }

            hSourceCurrency = getValue("curr_eng_name");
            
            hMchtCountry = getValue("mcht_country");
            if ("CHN".equals(hMchtCountry)) {
            	hMchtCountry = "CN";
            }
            
            hBillAmt1 = encriptAmt(hBillAmtOrigi, "%010.0f");
            hSourceAmt1 = encriptAmt(hSourceAmt, "%020.6f");;
            
            buf = "";
            buf += fixLeft("006", 3);
            buf += fixLeft(hCardNo, 16);
            buf += fixLeft(hId, 10);
            buf += fixLeft(hPurchaseDate, 7);
            buf += fixLeft(hPostDate, 7);
            buf += fixLeft(hMchtEngName, 100);
            buf += fixLeft(hBillAmt1, 10);
            buf += fixLeft(hprocessDate, 7);
            buf += fixLeft(hSourceCurrency, 3);
            buf += fixLeft(hSourceAmt1, 20);
            buf += fixLeft(hMchtCountry, 2);
            buf += "\r\n";
            
            writeBinFile2(0, buf.getBytes("MS950"), buf.getBytes("MS950").length);
            
            totalCnt++;
            totalAmt = totalAmt + hBillAmtOrigi;

        }
        closeCursor();
    }
    
    String selectCrdCard(String card_no) throws Exception {

    	String idNo = "";
    	extendField= "crd.";
        sqlCmd  = "select (select id_no from crd_idno ";
        sqlCmd += "        where id_p_seqno = a.id_p_seqno) as id_no ";
        sqlCmd += " from crd_card a ";
        sqlCmd += " where card_no = ? ";
        
        setString(1,card_no);
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            idNo = getValue("crd.id_no");
        }
        
        return idNo;
    }
    
    String selectDbcCard(String card_no) throws Exception {

    	String idNo = "";
    	extendField= "dbc.";
        sqlCmd  = "select (select id_no from dbc_idno ";
        sqlCmd += "        where id_p_seqno = a.id_p_seqno) as id_no ";
        sqlCmd += " from dbc_card a ";
        sqlCmd += " where card_no = ? ";
        
        setString(1,card_no);
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            idNo = getValue("dbc.id_no");
        }
        
        return idNo;
    }
    
    String selectPtrCurrCode(String currCode) throws Exception {

    	String currEngName = "OTH";
    	extendField= "cur.";
        sqlCmd  = "select curr_eng_name ";
        sqlCmd += " from ptr_currcode a ";
        sqlCmd += " where curr_code = ? ";
        
        setString(1,currCode);
        
        int recordCnt = selectTable();
        if (recordCnt > 0) {
        	currEngName = getValue("cur.curr_eng_name");
        }
        
        return currEngName;
    }
    
    //************************************************************************
    private String toChinDate(String date) {
      return String.format("%3d", Integer.valueOf(date.substring(0, 4)) - 1911)
          + date.substring(4, 6)
          + date.substring(6, 8);
    }
    
    private String julDate(String juliYddd) throws Exception  {
        String hJuliYddd;
        String hWestDate;
        String westDate = "";

        hJuliYddd = String.format("%4.4s", juliYddd);
        sqlCmd = "select case when  to_date(?,'yddd') > sysdate"
                + " then  to_char(add_months(to_date(?,'yddd'),-120),'yyyymmdd')"
                + " else  to_char(to_date(?,'yddd'),'yyyymmdd') end as h_west_date FROM   DUAL";
        setString(1, hJuliYddd);
        setString(2, hJuliYddd);
        setString(3, hJuliYddd);
        try {
        	selectTable();
            hWestDate = getValue("h_west_date");
            westDate = String.format("%8.8s", hWestDate);
        } catch (Exception e){
        	
        	sqlCmd="";
        	showLogMessage("E", "", "Conver julDate Error: ["+ juliYddd +"]");
        	westDate = "";
        }

        return westDate;
    }
    /***********************************************************************/
    private String encriptAmt(double amt, String fmt) {
        String replaceStr = "";
        String rtn = String.format(fmt, Math.abs(amt));
        int pos = rtn.indexOf(".");
        if (pos == -1) pos = rtn.length();
        replaceStr = rtn.substring(pos - 1, pos);
        if (amt < 0) {
            switch (replaceStr) {
            case "0" :
                replaceStr = "}";
                break;
            case "1" :
                replaceStr = "J";
                break;
            case "2" :
                replaceStr = "K";
                break;
            case "3" :
                replaceStr = "L";
                break;
            case "4" :
                replaceStr = "M";
                break;
            case "5" :
                replaceStr = "N";
                break;
            case "6" :
                replaceStr = "O";
                break;
            case "7" :
                replaceStr = "P";
                break;
            case "8" :
                replaceStr = "Q";
                break;
            case "9" :
                replaceStr = "R";
                break;
            default :
                return String.format(fmt, 0.0);
                    
            }
        } else {
            switch (replaceStr) {
            case "0" :
                replaceStr = "{";
                break;
            case "1" :
                replaceStr = "A";
                break;
            case "2" :
                replaceStr = "B";
                break;
            case "3" :
                replaceStr = "C";
                break;
            case "4" :
                replaceStr = "D";
                break;
            case "5" :
                replaceStr = "E";
                break;
            case "6" :
                replaceStr = "F";
                break;
            case "7" :
                replaceStr = "G";
                break;
            case "8" :
                replaceStr = "H";
                break;
            case "9" :
                replaceStr = "I";
                break;
            default :
                return String.format(fmt, 0.0);
            }
        }
        return rtn.substring(0, pos - 1) + replaceStr + rtn.substring(pos);
    }
    
    /***********************************************************************/
    String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)
            spc += " ";
        if (str == null)
            str = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }
    
    void procFTP(String isFileName) throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/bil", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		showLogMessage("I", "", "put " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "put " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
		} else {
			comc.fileRename2(String.format("%s/media/bil/", comc.getECSHOME()) + isFileName,
					String.format("%s/media/bil/backup/", comc.getECSHOME()) + isFileName);
		}
	}
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilN004 proc = new BilN004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
