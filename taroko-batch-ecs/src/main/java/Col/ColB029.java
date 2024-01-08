/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/05/06  V1.00.00    phopho     program initial                          *
*  109/12/13  V1.00.01    shiyuqi       updated for project coding standard   *
*  109/12/30  V1.00.03  yanghan       修改了部分无意义的變量名稱          *
******************************************************************************/

package Col;

import java.util.Arrays;

import com.*;

public class ColB029 extends AccessDAO {
    private String progname = "各類協商資料彙整並標示協商狀態作業  109/12/30 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";
    int hParmCnt = 0;
    String hUserId = "";
    String parmType[];
    String parmValue[];
    String parmIdcode[];
    
    String hCnsdIdPSeqno = "";
    String hCnsdIdNo = "";
    String hCnsdChiName = "";
    String hCnsdNegoType = "";
    String hCnsdNegoStatus = "";
    String hCnsdApplyNegoMcode = "";
    String hCnsdInstallSDate = "";
    String hCnsdNegoMeanDate = "";
    String hCnsdNegoMeanEndReason = "";
    String hCnsdStopNotifyDate = "";
    String hCnsdRecolDate = "";
    String hCnsdNotifyDate = "";
    String hCnsdEndDate = "";
    String hCnsdContractDate = "";
    String hCnsdRecolReason = "";
    String hCnsdEndReason = "";
    String hCnsdCaseDate = "";
    String hCnsdRenewDamageDate = "";
    String hCnsdDeliverDate = "";
    String hCnsdLiacSeqno = "";
    
    String hCnscIdPSeqno = "";
    String hCnscIdNo = "";
    String hCnscChiName = "";
    String hCnscNegoType = "";
    String hCnscNegoStatus = "";
    String hCnscApplyNegoMcode = "";
    String hCnscInstallSDate = "";
    
    String hTempIdNo = "";
    String hTempNotifyDate = "";
    String insertFlag = "";
    int    totalCnt = 0;
    
    public int mainProcess(String[] args) {
        try {
        	dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            
            if (comm.isAppActive(javaProgram)) {
                comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            
            // 檢查參數
            if (args.length != 0) {
                comc.errExit("Usage : ColB029", "");
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            
            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(0, 0, 0);
            
/******************************************************************************
*           ColB029 各類協商資料彙整並標示協商狀態作業
*           
*           #架構與其他批次一致。
*           #原則：
*              a. 【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】：一個 id_no，可以多筆。但是同一種協商中，每個 id_no 只有一筆。
*              b. 【COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔】：一個 id_no 一筆資料。
*              c. 上方圖示，打Ｖ的欄位，要填值。
*           #清空【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】、【COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔  】兩個TABLE資料。(此批次每日執行，為全系統資料檢查、倒檔、整理。故需要清空TABLE)
*           #先取得參數資料，設定為五個ARRAY，以便重複運用。(避免一直重複撈取一樣的資料)
*              a. 查詢TABLE：PTR_SYS_PARM
*                 查詢條件：wf_parm　= ‘COL_PARM’and wf_key = ‘COL_NEGO_STATUS_PARM’
*                 取得內容：wf_value、wf_value2
*                 解析：取得的兩個欄位中，含有逗號分隔字串，內容為五種協商類別之狀態內容的查詢代碼。
*                   Ex：COL_NEGO_LIAB_STATUS 協商狀態_債務協商類之狀態、
*                       COL_NEGO_LIAC_STATUS 協商狀態_前置協商類之狀態、
*                       COL_NEGO_LIAM_STATUS 協商狀態_前置調解類之狀態 (目前尚無 IDTAB，要待開發時，需要新增)、
*                       COL_NEGO_RENEW_STATUS 協商狀態_更生類之狀態、
*                       LIAD_LIQU_STATUS 清算法院進度。　　　
*              b. 將前項取得的解析後的內容，作為查詢條件之一，查詢取得各類協商的狀態內容。
*                 查詢TABLE：PTR_SYS_IDTAB 系統代碼檔
*                 查詢條件：wf_type　= ‘前項解析後的內容之一’(---> Ex：COL_NEGO_LIAB_STATUS 協商狀態_債務協商類之狀態)
*                 取得內容：取得多筆資料 ----> 此為該種協商的狀態，設定於 array 中。
*                 解析運用：ID_CODE 欄位值若為【Y】，表示這筆資料的代碼【WF_ID】，屬於【有協商】的狀態。
*           #取得各類協商之資料內容
*              以下每一種協商，有資料時，寫入資料：COL_NEGO_STATUS_DATA 協商類別及狀態資料檔。
*              一種協商，一個 id_no 只會寫入一筆。若一個id_no 存在於兩種協商資料中，則有2筆【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】。
*              寫入之欄位，參考上圖，有打V者，寫入TABLE 欄位。
*           1. 債務協商 (COL_LIAB_NEGO)
*              a. TABLE：COL_LIAB_NEGO 債務協商狀態基本資料檔
*                 排序：id_no、mod_time desc
*                 選取資料：同一個id_no只取第一筆。
*              b. TABLE：COL_LIAB_NEGO_HST 債務協商狀態資料歷史檔
*                 查詢條件：只取已經結案 ，且 ID_NO不存在於債協主檔中的資料。
*                     COL_LIAB_NEGO_HST . LIAB_STATUS = ‘3’ and END_DATE <> ‘’ (狀態為3，且 end_date 不為空，表示結案)
*                     且COL_LIAB_NEGO .ID_NO  不存在於 COL_LIAB_NEGO
*              c. Set【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】簡易說明：
*                 【STOP_NOTIFY_DATE】、【RECOL_DATE】、【NOTIFY_DATE】、【END_DATE】，只要有值，就帶入。
*                 【NEGO_MEAN_DATE (處理日期(為整理後的資料))】:依據COL_LIAB_NEGO. LIAB_STATUS　（或COL_LIAB_NEGO_HST），參考上方圖片表格，取得對應的日期帶入。
*                 修正表格錯誤，
*                 如果狀態為3，且end_date 為空，則帶入　notify_date。
*                 如果狀態為3，且end_date 不為空，則帶入end_date。             
*                 補充
*                 【INSTALL_S_DATE　(首期應繳款日期)】，不須帶入任何值。
*           2. 前置協商 (COL_LIAC_NEGO)
*              a. TABLE：COL_LIAC_NEGO 前置協商主檔
*                 排序：id_no、mod_time desc
*                 選取資料：同一個id_no只取第一筆。
*              b. TABLE：COL_LIAC_NEGO_HST 前置協商歷史檔
*                 查詢條件：只取已經結案，且 ID_NO不存在於前協主檔中的資料。
*                     COL_LIAC_NEGO_HST. LIAC_STATUS = ‘4’ or ‘5’
*                     且COL_LIAC_NEGO_HST. ID_NO 不存在於COL_LIAC_NEGO
*              c. Set【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】簡易說明：
*                 【STOP_NOTIFY_DATE】、【NOTIFY_DATE】、【END_DATE】、【CONTRACT_DATE】、【RECOL_REASON】、【END_REASON】，只要有值，就帶入。
*                 【NEGO_MEAN_DATE (處理日期(為整理後的資料))】:依據　COL_LIAC_NEGO.LIAC_STATUS　（或COL_LIAC_NEGO_HST），參考上方圖片表格，取得對應的日期帶入。
*                 【NEGO_MEAN_END_REASON (復催/結案原因)】：
*                 若LIAC_STATUS 為4，表示為【結案/復催】，此欄位帶入【recol_reason (復催原因碼)】。
*                 若LIAC_STATUS 為5，表示為【結案/結清】，此欄位帶入【end_reason (復催原因碼)】。
*                 補充
*                 【INSTALL_S_DATE　(首期應繳款日期)】，來源：【COL_LIAC_CONTRACT 前置協商合約檔.INSTALL_S_DATE】。
*                 查詢方式：COL_LIAC_NEGO. LIAC_SEQNO（或COL_LIAC_NEGO_HST）= COL_LIAC_CONTRACT 前置協商合約檔. LIAC_SEQNO
*                 取得欄位：【COL_LIAC_CONTRACT 前置協商合約檔.INSTALL_S_DATE】。
*           3. 前置調解 (COL_LIAM_NEGO)(待前調開發時，補上)
*           4. 更生 (COL_LIAD_RENEW)
*              a. TABLE：COL_LIAD_RENEW 更生公文資料檔
*                 排序：id_no、mod_time desc
*                 選取資料：同一個id_no只取第一筆。
*              b. Set 【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】簡易說明：
*                 【NOTIFY_DATE】、【CASE_DATE】、【RENEW_DAMAGE_DATE】、【DELIVER_DATE】，只要有值，就帶入。
*                 【NEGO_MEAN_DATE (處理日期(為整理後的資料))】:
*                 RENEW_STATUS　若為1, 2, 3, 5, 6，則帶入【CASE_DATE】。
*                 RENEW_STATUS　若為4，則帶入【DELIVER_DATE】。
*                 補充
*                 【INSTALL_S_DATE　(首期應繳款日期)】，來源：【COL_LIAD_RENEW 更生公文資料檔.renew_first_date更生首期繳款日】。
*           5. 清算 (COL_LIAD_LIQUIDATE)
*              a. TABLE：COL_LIAD_LIQUIDATE 清算公文資料檔
*                 排序：id_no、mod_time desc
*                 選取資料：同一個id_no只取第一筆。
*              b. Set 【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】簡易說明：
*                 【NOTIFY_DATE】、【CASE_DATE】，只要有值，就帶入。
*                 【NEGO_MEAN_DATE (處理日期(為整理後的資料))】: 此欄位帶入【CASE_DATE】。
*                 補充
*                 【INSTALL_S_DATE　(首期應繳款日期)】，不須帶入任何值。
*           #整理【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】資料，寫入【COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔】
*              a. 讀取【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】，並解析。
*              b. 若此筆【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔】的【NEGO_TYPE】、【NEGO_STATUS】所對應的【PTR_SYS_IDTAB. ID_CODE】為Y，則寫入【COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔】。
*                 【COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔】中的【中文姓名】欄位，由 crd_idno取得。
*              c. 若此筆資料，寫入時，已經存在，則比較【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔. NOTIFY_DATE】，
*                 刪除【COL_NEGO_STATUS_DATA 協商類別及狀態資料檔. NOTIFY_DATE】比較舊的，insert 較新的。
*           #Shell sh_2_cs
*              列在 ColD300之後
******************************************************************************/
            
            hUserId = "Batch";
            //delete table
            deleteColNegoStatusCurr();
            deleteColNegoStatusData();
            
            //先取得參數資料，設定為五個ARRAY，以便重複運用。(避免一直重複撈取一樣的資料)
            selectPtrSysParm();

//          取得各類協商之資料內容
//          1.	債協
            selectColLiabNego();
//          2.	前協
            selectColLiacNego();
//          3.	前調(待前調開發時，補上)
            selectColLiamNego();
//          4.	更生
            selectColLiadRenew();
//          5.	清算
            selectColLiadLiquidate();

            //commit!
            commitDataBase();
            showLogMessage("I", "", "COL_NEGO_STATUS_DATA 協商類別及狀態資料檔處理完畢,累計筆數 : [" + totalCnt + "]");
            
            //insert col_nego_status_curr
            selectColNegoStatusData();
            showLogMessage("I", "", "COL_NEGO_STATUS_CURR 個人目前之協商類別及狀態資料檔處理完畢,累計筆數 : [" + totalCnt + "]");

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束.";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
            	comcr.callbatch(1, 0, 0);
            
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
    
    /***********************************************************************/
    void deleteColNegoStatusCurr() throws Exception {
        daoTable = "col_nego_status_curr";
        deleteTable();
    }

    /***********************************************************************/
    void deleteColNegoStatusData() throws Exception {
        daoTable = "col_nego_status_data";
        deleteTable();
    }
    
    /***********************************************************************/
    void selectPtrSysParm() throws Exception {
    	String ssArray = "";

    	sqlCmd = "select wf_value from ptr_sys_parm ";
        sqlCmd += "where wf_parm = 'COL_PARM' and wf_key = 'COL_NEGO_TYPE_PARM' ";

        if (selectTable() > 0) {
        	ssArray = getValue("wf_value");
        }
        if (!ssArray.equals("")) {
        	parmType = ssArray.split(",");
        }
        
//        for (int k = 0; k < parm_type.length; k++) {
//        	showLogMessage("I", "", "parm_type="+k+"; "+parm_type[k]);
//        }
    	
        ssArray = "";
    	sqlCmd = "select wf_value||','||wf_value2 as ss_value from ptr_sys_parm ";
        sqlCmd += "where wf_parm = 'COL_PARM' and wf_key = 'COL_NEGO_STATUS_PARM' ";

//        COL_NEGO_LIAB_STATUS,COL_NEGO_LIAC_STATUS,COL_NEGO_LIAM_STATUS,COL_NEGO_RENEW_STATUS,LIAD_LIQU_STATUS
    	
        if (selectTable() > 0) {
        	ssArray = getValue("ss_value");
        }
        if (!ssArray.equals("")) {
        	parmValue = ssArray.split(",");
        }
        
//        for (int k = 0; k < parm_value.length; k++) {
//        	showLogMessage("I", "", "parm_value="+k+"; "+parm_type[k]+" : "+parm_value[k]);
//        }
        
        ssArray = "";
        for (int k = 0; k < parmValue.length; k++) {
//        	String ls_id = "";
        	String lsStr = "";
        	sqlCmd = "select wf_id from ptr_sys_idtab where wf_type = ? and id_code = 'Y' order by wf_id ";
        	setString(1, parmValue[k]);
        	
        	int recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
//            	ls_id += ","+getValue("wf_id", i);
//            	li_id = comcr.str2int(getValue("wf_id", i));
            	lsStr += ","+String.format("%02d",getValueInt("wf_id", i));  //補0
            }
            if (!lsStr.equals("")) lsStr = comc.getSubString(lsStr,1);
            ssArray = ssArray + "#" + lsStr;
        }

        ssArray = comc.getSubString(ssArray,1);
        if (!ssArray.equals("")) {
        	parmIdcode = ssArray.split("#");
        }
        
//        for (int k = 0; k < parm_idcode.length; k++) {
//        	showLogMessage("I", "", "parm_idcode="+k+"; "+parm_type[k]+" : "+parm_idcode[k]);
//        }
    }
    
    /***********************************************************************/
    void clearParameter() throws Exception {
    	hTempIdNo = "";
    	hCnsdIdPSeqno = "";
        hCnsdIdNo = "";
        hCnsdChiName = "";
        hCnsdNegoType = "";
        hCnsdNegoStatus = "";
        hCnsdApplyNegoMcode = "";
        hCnsdInstallSDate = "";
        hCnsdNegoMeanDate = "";
        hCnsdNegoMeanEndReason = "";
        hCnsdStopNotifyDate = "";
        hCnsdRecolDate = "";
        hCnsdNotifyDate = "";
        hCnsdEndDate = "";
        hCnsdContractDate = "";
        hCnsdRecolReason = "";
        hCnsdEndReason = "";
        hCnsdCaseDate = "";
        hCnsdRenewDamageDate = "";
        hCnsdDeliverDate = "";
    }
    
    /***********************************************************************/
    void selectColLiabNego() throws Exception {
//      1.	債協
//    	select distinct id_p_seqno from (
//    			select '1',id_p_seqno, id_no, mod_time, chi_name, liab_status, stop_notify_date, recol_date, notify_date, end_date
//    			from col_liab_nego
//    			UNION
//    			select '2',id_p_seqno, id_no, mod_time, chi_name, liab_status, stop_notify_date, recol_date, notify_date, end_date
//    			from col_liab_nego_hst where liab_status='3' and end_date<>'' 
//    			and id_no not in (select id_no from col_liab_nego)
//    			order by id_no, mod_time desc)
    	clearParameter();
    	
    	sqlCmd = " Select id_p_seqno, id_no, mod_time, chi_name, liab_status, ";
    	sqlCmd += " stop_notify_date, recol_date, notify_date, end_date ";
    	sqlCmd += " From col_liab_nego ";
    	sqlCmd += " UNION ";
    	sqlCmd += " Select id_p_seqno, id_no, mod_time, chi_name, liab_status, ";
		sqlCmd += " stop_notify_date, recol_date, notify_date, end_date ";
		sqlCmd += " From col_liab_nego_hst ";
		sqlCmd += " Where liab_status = '3' and end_date <> '' ";
		sqlCmd += " and id_no not in (select id_no from col_liab_nego) ";
		sqlCmd += " ORDER BY id_no, mod_time desc ";
    	
		openCursor();
        while (fetchTable()) {
        	hCnsdIdNo = getValue("id_no");
        	if (hCnsdIdNo.equals(hTempIdNo)) {  //選取資料：同一個id_no只取第一筆。
        		continue;
            }
        	
        	hCnsdIdPSeqno = getValue("id_p_seqno");
        	hCnsdChiName = getValue("chi_name");
        	hCnsdNegoType = "A";
            hCnsdNegoStatus = getValue("liab_status");
            hCnsdStopNotifyDate = getValue("stop_notify_date");
            hCnsdRecolDate = getValue("recol_date");
            hCnsdNotifyDate = getValue("notify_date");
            hCnsdEndDate = getValue("end_date");
            hTempIdNo = getValue("id_no");
        	
            totalCnt++;
            if (totalCnt % 10000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
            //計算項目
//            h_cnsd_apply_nego_mcode = "";  //todo
            getNegoMeanDateA();
            
            insertColNegoStatusData();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectColLiacNego() throws Exception {
//      2.	前協
//    	select distinct id_no from (
//    			 select id_p_seqno, id_no, mod_time, chi_name, liac_status, stop_notify_date, notify_date, end_date, contract_date, recol_reason, end_reason
//    			 from col_liac_nego
//    			 UNION
//    			 select id_p_seqno, id_no, mod_time, chi_name, liac_status, stop_notify_date, notify_date, end_date, contract_date, recol_reason, end_reason
//    			 from col_liac_nego_hst where liac_status in ('4','5')
//    			 and id_no not in (select id_no from col_liac_nego)
//    			 order by id_no, mod_time desc)
    	clearParameter();
    	
    	sqlCmd = " Select id_p_seqno, id_no, mod_time, chi_name, liac_status, liac_seqno, ";
    	sqlCmd += " stop_notify_date, notify_date, end_date, contract_date, recol_reason, end_reason ";
    	sqlCmd += " From col_liac_nego ";
    	sqlCmd += " UNION ";
    	sqlCmd += " Select id_p_seqno, id_no, mod_time, chi_name, liac_status, liac_seqno, ";
		sqlCmd += " stop_notify_date, notify_date, end_date, contract_date, recol_reason, end_reason ";
		sqlCmd += " From col_liac_nego_hst ";
		sqlCmd += " Where liac_status in ('4','5') ";
		sqlCmd += " and id_no not in (select id_no from col_liac_nego) ";
		sqlCmd += " ORDER BY id_no, mod_time desc ";
    	
		openCursor();
        while (fetchTable()) {
        	hCnsdIdNo = getValue("id_no");
        	if (hCnsdIdNo.equals(hTempIdNo)) {  //選取資料：同一個id_no只取第一筆。
        		continue;
            }
        	
        	hCnsdIdPSeqno = getValue("id_p_seqno");
        	hCnsdChiName = getValue("chi_name");
        	hCnsdNegoType = "B";
            hCnsdNegoStatus = getValue("liac_status");
            hCnsdStopNotifyDate = getValue("stop_notify_date");
            hCnsdNotifyDate = getValue("notify_date");
            hCnsdEndDate = getValue("end_date");
            hCnsdContractDate = getValue("contract_date");
            hCnsdRecolReason = getValue("recol_reason");
            hCnsdEndReason = getValue("end_reason");
            hCnsdLiacSeqno = getValue("liac_seqno");
            hTempIdNo = getValue("id_no");
        	
            totalCnt++;
            if (totalCnt % 10000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
            //計算項目
//            h_cnsd_apply_nego_mcode = "";  //todo
            getInstallSDateB();  //首期繳款日
            getNegoMeanDateB();
            getNegoMeanEndReasonB();
            
            insertColNegoStatusData();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectColLiamNego() throws Exception {
//      3.	前調(待前調開發時，補上)  //todo
    	
    }
    
    /***********************************************************************/
    void selectColLiadRenew() throws Exception {
//      4.	更生
//    	select id_p_seqno, id_no, mod_time, notify_date, case_date, renew_damage_date, deliver_date from col_liad_renew
//    	order by id_no, mod_time desc
//
//    	select id_p_seqno, id_no, mod_time, notify_date, case_date, renew_damage_date, deliver_date from col_liad_renew a
//    	where mod_time = (select max(mod_time) from col_liad_renew b where a.id_no = b.id_no ) order by id_no
//
//    	select distinct id_no from col_liad_renew order by id_no
    	clearParameter();
    	
    	sqlCmd = " Select id_p_seqno, id_no, mod_time, chi_name, renew_status, ";
    	sqlCmd += " notify_date, case_date, renew_damage_date, deliver_date, renew_first_date ";
    	sqlCmd += " From col_liad_renew a ";
    	sqlCmd += " Where mod_time = (select max(mod_time) from col_liad_renew b ";
    	sqlCmd += " where a.id_no = b.id_no ) order by id_no ";
    	
		openCursor();
        while (fetchTable()) {
        	hCnsdIdNo = getValue("id_no");
        	if (hCnsdIdNo.equals(hTempIdNo)) {  //選取資料：同一個id_no只取第一筆。
        		continue;
            }
        	
        	hCnsdIdPSeqno = getValue("id_p_seqno");
        	hCnsdChiName = getValue("chi_name");
        	hCnsdNegoType = "D";
            hCnsdNegoStatus = getValue("renew_status");
            hCnsdNotifyDate = getValue("notify_date");
            hCnsdCaseDate = getValue("case_date");
            hCnsdRenewDamageDate = getValue("renew_damage_date");
            hCnsdDeliverDate = getValue("deliver_date");
            hCnsdInstallSDate = getValue("renew_first_date");
            hTempIdNo = getValue("id_no");
        	
            totalCnt++;
            if (totalCnt % 10000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
            //計算項目
//            h_cnsd_apply_nego_mcode = "";  //todo
            getNegoMeanDateD();
            
            insertColNegoStatusData();
        }
        closeCursor();
    }
    
    /***********************************************************************/
    void selectColLiadLiquidate() throws Exception {
//      5.	清算
//    	select id_p_seqno, id_no, mod_time, notify_date, case_date from col_liad_liquidate
//    	order by id_no, mod_time desc
//
//    	select id_p_seqno, id_no, mod_time, chi_name, acct_status, notify_date, case_date from col_liad_liquidate a
//    	where mod_time = (select max(mod_time) from col_liad_liquidate b where a.id_no = b.id_no ) order by id_no
//
//    	select distinct id_no from col_liad_liquidate order by id_no
    	clearParameter();
    	
    	sqlCmd = " Select id_p_seqno, id_no, mod_time, chi_name, court_status, ";
    	sqlCmd += " notify_date, case_date ";
    	sqlCmd += " From col_liad_liquidate a ";
    	sqlCmd += " Where mod_time = (select max(mod_time) from col_liad_liquidate b ";
    	sqlCmd += " where a.id_no = b.id_no ) order by id_no ";
    	
		openCursor();
        while (fetchTable()) {
        	hCnsdIdNo = getValue("id_no");
        	if (hCnsdIdNo.equals(hTempIdNo)) {  //選取資料：同一個id_no只取第一筆。
        		continue;
            }
        	
        	hCnsdIdPSeqno = getValue("id_p_seqno");
        	hCnsdChiName = getValue("chi_name");
        	hCnsdNegoType = "E";
            hCnsdNegoStatus = getValue("court_status");
            hCnsdNotifyDate = getValue("notify_date");
            hCnsdCaseDate = getValue("case_date");
            hTempIdNo = getValue("id_no");
        	
            totalCnt++;
            if (totalCnt % 10000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }
            
            //計算項目
//            h_cnsd_apply_nego_mcode = "";  //todo
            getNegoMeanDateE();
            
            insertColNegoStatusData();
        }
        closeCursor();
    }

    /***********************************************************************/
    void getNegoMeanDateA() throws Exception {
    	switch (hCnsdNegoStatus) {
		case "1":
			hCnsdNegoMeanDate = hCnsdStopNotifyDate;
			break;
		case "2":
			hCnsdNegoMeanDate = hCnsdRecolDate;
			break;
		case "3":
			if (hCnsdEndDate.equals("")) {
    			hCnsdNegoMeanDate = hCnsdNotifyDate;
    		} else {
    			hCnsdNegoMeanDate = hCnsdEndDate;
    		}
			break;
		case "4":
			hCnsdNegoMeanDate = hCnsdEndDate;
			break;
		}
    }
    
    /***********************************************************************/
    void getNegoMeanDateB() throws Exception {
    	switch (hCnsdNegoStatus) {
		case "1":
		case "4":
		case "5":
			hCnsdNegoMeanDate = hCnsdNotifyDate;
			break;
		case "2":
			hCnsdNegoMeanDate = hCnsdStopNotifyDate;
			break;
		case "3":
			hCnsdNegoMeanDate = hCnsdContractDate;
			break;
		}
    }
    
    /***********************************************************************/
    void getNegoMeanDateC() throws Exception {
//    	h_cnsd_nego_mean_date = h_cnsd_notify_date;//todo 前調
    }
    
    /***********************************************************************/
    void getNegoMeanDateD() throws Exception {
    	switch (hCnsdNegoStatus) {
		case "1":
		case "2":
		case "3":
		case "5":
		case "6":
			hCnsdNegoMeanDate = hCnsdCaseDate;
			break;
		case "4":
			hCnsdNegoMeanDate = hCnsdDeliverDate;
			break;
		}
    }
    
    /***********************************************************************/
    void getNegoMeanDateE() throws Exception {
    	hCnsdNegoMeanDate = hCnsdCaseDate;
    }
    
    /***********************************************************************/
    void getNegoMeanEndReasonB() throws Exception {
    	if (hCnsdNegoStatus.equals("4")) {
    		hCnsdNegoMeanEndReason = hCnsdRecolReason;
    	}
    	if (hCnsdNegoStatus.equals("5")) {
    		hCnsdNegoMeanEndReason = hCnsdEndReason;
    	}
    }
    
    /***********************************************************************/
    void getNegoMeanEndReasonC() throws Exception {
//    	h_cnsd_nego_mean_end_reason = h_cnsd_recol_reason;//todo 前調
    }
    
    /***********************************************************************/
    void getInstallSDateB() throws Exception {
    	hCnsdInstallSDate = "";
    	
    	sqlCmd = "select install_s_date from col_liac_contract ";
        sqlCmd += "where liac_seqno = ? fetch first 1 row only ";
        setString(1, hCnsdLiacSeqno);
        
        extendField = "col_liac_contract.";
        if (selectTable() > 0) {
        	hCnsdInstallSDate = getValue("col_liac_contract.install_s_date");
        }
    }
    
    /***********************************************************************/
    void getInstallSDateC() throws Exception {
    	hCnsdInstallSDate = "";//todo 前調
    }
    
    /***********************************************************************/
    void selectColNegoStatusData() throws Exception {
    	totalCnt=0;
    	hCnscIdNo = "";

  	    sqlCmd = "Select distinct id_no From col_nego_status_data ";
  	    
		openCursor();
        while (fetchTable()) {
        	hCnscIdNo = getValue("id_no");
        	
            //處理資料 by id_no
        	processCurrData();
        	
        	if (insertFlag.equals("Y")) {
//        		if (h_cnsc_id_p_seqno.equals("")) continue; //todo
        		insertColNegoStatusCurr();
        	}
            
        }
        closeCursor();
    }
    
    /***********************************************************************/
    private void processCurrData() throws Exception {
    	String lsNotifyDate = "";
    	String lsNegoType = "";
    	String lsNegoStatus = "";
    	
    	hCnscIdPSeqno = "";
    	hCnscChiName = "";
    	hCnscNegoType = "";
    	hCnscNegoStatus = "";
    	hCnscApplyNegoMcode = "";
    	hCnscInstallSDate = "";
        insertFlag = "";
        hTempNotifyDate = "";

        sqlCmd = "Select id_p_seqno, id_no, chi_name, notify_date, ";
	    sqlCmd += " nego_type, nego_status, apply_nego_mcode, install_s_date ";
	    sqlCmd += " From col_nego_status_data where id_no = ? ";
        setString(1, hCnscIdNo);
        
        extendField = "col_nego_status_data.";

        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
        	lsNotifyDate = getValue("col_nego_status_data.notify_date", i);
        	lsNegoType = getValue("col_nego_status_data.nego_type", i);
        	lsNegoStatus = getValue("col_nego_status_data.nego_status", i);
        	
        	totalCnt++;
            if (totalCnt % 5000 == 0) {
                showLogMessage("I", "", "    目前處理筆數 =[" + totalCnt + "]");
            }        	
        	
        	if (getIdCode(lsNegoType, lsNegoStatus)) {
        		insertFlag = "Y";
        		if (lsNotifyDate.compareTo(hTempNotifyDate) > 0) {
        			hTempNotifyDate = lsNotifyDate;
        			getChiName();
//        			h_cnsc_id_p_seqno = getValue("col_nego_status_data.id_p_seqno", i);
//        	       	h_cnsc_chi_name = getValue("col_nego_status_data.chi_name", i);
        	       	hCnscNegoType = getValue("col_nego_status_data.nego_type", i);
        	       	hCnscNegoStatus = getValue("col_nego_status_data.nego_status", i);
        	       	hCnscApplyNegoMcode = getValue("col_nego_status_data.apply_nego_mcode", i);
        	       	hCnscInstallSDate = getValue("col_nego_status_data.install_s_date", i);
        		}
        	}
        }
    }
    
    /***********************************************************************/
    private boolean getIdCode(String asNegoType, String asNegoStatus) throws Exception {
    	String lsIdCode = "";
    	String lsStatus = "";
    	lsIdCode = decodeColNegoStatusParm(asNegoType);
    	
    	lsStatus = String.format("%02d",comcr.str2int(asNegoStatus.trim()));  //補0, 不然indexOf會有問題
    	
//    	int ii = ls_id_code.indexOf(as_nego_status.trim());
    	int ii = lsIdCode.indexOf(lsStatus);
    	
    	if (ii >= 0) return true;
    	
    	return false;
    	
//    	String ls_wf_type = "";
//    	ls_wf_type = decode_col_nego_status_parm(as_nego_type);
//
//    	sqlCmd = "select id_code from ptr_sys_idtab where wf_type = ? and wf_id = ? ";
//    	setString(1, ls_wf_type);
//    	setString(2, as_nego_status);
//
//    	extendField = "ptr_sys_idtab.";
//    	if (selectTable() > 0) {
//    		String id_code = getValue("ptr_sys_idtab.id_code");
//    		if (id_code.equals("Y")) {
//                return true;
//            }
//        }
//
//        return false;
    }
    
    /***********************************************************************/
    void getChiName() throws Exception {  //for curr use
    	sqlCmd = "select id_p_seqno, chi_name from crd_idno where id_no = ? ";
    	setString(1, hCnscIdNo);
    	
    	extendField = "crd_idno.";
    	if (selectTable() > 0) {
    		hCnscIdPSeqno = getValue("crd_idno.id_p_seqno");
    		hCnscChiName = getValue("crd_idno.chi_name");
    	}
    }
    
    /***********************************************************************/
    void insertColNegoStatusData() throws Exception {
        dateTime();
        daoTable = "col_nego_status_data";
        extendField = daoTable + ".";
        setValue(extendField+"id_p_seqno", hCnsdIdPSeqno);
        setValue(extendField+"id_no", hCnsdIdNo);
        setValue(extendField+"chi_name", hCnsdChiName);
        setValue(extendField+"nego_type", hCnsdNegoType);
        setValue(extendField+"nego_status", hCnsdNegoStatus);
        setValue(extendField+"apply_nego_mcode", hCnsdApplyNegoMcode);
        setValue(extendField+"install_s_date", hCnsdInstallSDate);
        setValue(extendField+"nego_mean_date", hCnsdNegoMeanDate);
        setValue(extendField+"nego_mean_end_reason", hCnsdNegoMeanEndReason);
        setValue(extendField+"stop_notify_date", hCnsdStopNotifyDate);
        setValue(extendField+"recol_date", hCnsdRecolDate);
        setValue(extendField+"notify_date", hCnsdNotifyDate);
        setValue(extendField+"end_date", hCnsdEndDate);
        setValue(extendField+"contract_date", hCnsdContractDate);
        setValue(extendField+"recol_reason", hCnsdRecolReason);
        setValue(extendField+"end_reason", hCnsdEndReason);
        setValue(extendField+"case_date", hCnsdCaseDate);
        setValue(extendField+"renew_damage_date", hCnsdRenewDamageDate);
        setValue(extendField+"deliver_date", hCnsdDeliverDate);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_nego_status_data duplicate!", "", hCallBatchSeqno);
        }
    }
    
    /***********************************************************************/
    void insertColNegoStatusCurr() throws Exception {
        dateTime();
        daoTable = "col_nego_status_curr";
        extendField = daoTable + ".";
        setValue(extendField+"id_p_seqno", hCnscIdPSeqno);
        setValue(extendField+"id_no", hCnscIdNo);
        setValue(extendField+"chi_name", hCnscChiName);
        setValue(extendField+"nego_type", hCnscNegoType);
        setValue(extendField+"nego_status", hCnscNegoStatus);
        setValue(extendField+"apply_nego_mcode", hCnscApplyNegoMcode);
        setValue(extendField+"install_s_date", hCnscInstallSDate);
        setValue(extendField+"mod_user", hUserId);
        setValue(extendField+"mod_time", sysDate + sysTime);
        setValue(extendField+"mod_pgm", javaProgram);

        insertTable();
        if (dupRecord.equals("Y")) {
            comcr.errRtn("insert_col_nego_status_curr duplicate!", "", hCallBatchSeqno);
        }
    }
    
    private String decodeColNegoStatusParm(String string1) {
    	if (string1 == null || string1.trim().length() == 0)
    		return "";

    	int ii = Arrays.asList(parmType).indexOf(string1.trim());
    	if (ii >= 0 && ii < parmIdcode.length) {
    		return parmIdcode[ii];
    	}

    	return string1;
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        ColB029 proc = new ColB029();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
