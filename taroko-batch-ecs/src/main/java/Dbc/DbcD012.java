/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  110/06/18  V1.00.01   Wilson     where條件新增 -> end_ibm_date <> ''          *
*  112/01/06  V1.00.02   Wilson     移除end_ibm_date                           *
*  112/12/11  V1.00.03   Wilson     調整為判斷card_item                          *
*  112/12/14  V1.00.04   Wilson     insert crd_whtrans增加覆核欄位                                            *
******************************************************************************/

package Dbc;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*製卡 sucess 系統自動扣除空白卡樣處理程式*/
public class DbcD012 extends AccessDAO {
	private String progname = "製卡 sucess 系統自動扣除空白卡樣處理程式  112/12/14 V1.00.04";

	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;

	int debug = 0;
	String hCallErrorDesc = "";
	int tmpInt = 0;
	int totalCnt = 0;

	String prgmId = "DbcD012";
	String hCallBatchSeqno = "";

	String hTempUser = "";
	String hBusinessDate = "";
	String hSystemDate = "";
	String hBatchno1 = "";
	String hBatchno2 = "";
	String hWareYear = "";
	int hWareMonth = 0;
	String hRealUnitcode = "";
	String hDcesUnitCode = "";
	String hDcesCardType = "";
	String hDcesGroupCode = "";
	String hDcesEmbossSource = "";
	String hDcesEmbossReason = "";
	String hDcesCardNo = "";
	String hDcesIcFlag = "";
	String unitCardItem  = "";
	String hDcesRowid = "";
	// String h_dces_age_indicator = "";
	String hWhtrWarehouseNo = "";
	String hWhtrCardItem = "";
	// String h_whtr_place = "";
	String hKeyCardtype = "";
	String hKeyUnitcode = "";
	String hKeyIcFlag = "";
	String hkeyCardItem = "";
	// String h_key_age_indicator = "";
	String hWhtrWarehouseDate = "";
	String hWhtrCardType = "";
	String hWhtrGroupCode = "";
	String hWhtrUnitCode = "";
	String hWhtrTnsType = "";
	String hWhtrCreateDate = "";
	String hWhtrTransReason = "";
	String hWhtrCreateId = "";
	String hWhtrModUser = "";
	String hWhtrModPgm = "";
	String hWareWhYear = "";
	String hWareCardItem = "";
	String hWarePlace = "";
	double hWareOutQty01 = 0;
	double hWareOutQty02 = 0;
	double hWareOutQty03 = 0;
	double hWareOutQty04 = 0;
	double hWareOutQty05 = 0;
	double hWareOutQty06 = 0;
	double hWareOutQty07 = 0;
	double hWareOutQty08 = 0;
	double hWareOutQty09 = 0;
	double hWareOutQty10 = 0;
	double hWareOutQty11 = 0;
	double hWareOutQty12 = 0;
	String hWareModUser = "";
	String hWareRowid = "";
	String hWareModPgm = "";
	double hWhtrModSeqno = 0;
	int hWhtrPrevTotal = 0;
	int hWhtrUseTotal = 0;

	double hPreTotalBal = 0;
	double hAllTotalBal = 0;
	String hWhtrYear = "";
	int hWhtrMonth = 0;
	int seqNo = 0;
	String hWareLotNo = "";
	double hWareItemAmt = 0;
	double tmpQty = 0;

	String hKeyGroupcode = "";
	int liErr = 0;
	int hCardQty = 0;
	int insertCnt = 0;
	private String hWhtrPlace = "";
	// ******************************************************

	public int mainProcess(String[] args) {
		try {

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================
			if (args.length != 0 && args.length != 1) {
				comc.errExit("Usage : DbcD012 callbatch_seqno", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
			if (comc.getSubString(hCallBatchSeqno, 0, 8).equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
				hCallBatchSeqno = "no-call";
			}
			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

			hTempUser = "";
			if (hCallBatchSeqno.length() == 20) {
				comcr.callbatch(0, 0, 0);
				sqlCmd = "select user_id ";
				sqlCmd += " from ptr_callbatch  ";
				sqlCmd += "where batch_seqno = ? ";
				setString(1, hCallBatchSeqno);
				int recordCnt1 = selectTable();
				if (recordCnt1 > 0) {
					hTempUser = getValue("user_id");
				}
			}
			if (hTempUser.length() == 0) {
				hTempUser = comc.commGetUserID();
			}

			commonRtn();

			showLogMessage("I", "", "本日營業日份 : [" + hWhtrYear + "] [" + hWhtrMonth + "]");

			selectDbcEmboss();

			// ==============================================
			// 固定要做的

			hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]" + insertCnt;
			showLogMessage("I", "", hCallErrorDesc);

			if (hCallBatchSeqno.length() == 20)
				comcr.callbatch(1, 0, 1); // 1: 結束

			finalProcess();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}

	/***********************************************************************/
	void commonRtn() throws Exception {
		sqlCmd = "select online_date ";
		sqlCmd += "     , to_char(sysdate,'yyyymmdd') h_system_date ";
		sqlCmd += "  from ptr_businday ";
		int recordCnt1 = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
		}
		if (recordCnt1 > 0) {
			hBusinessDate = getValue("online_date");
			hSystemDate = getValue("h_system_date");
		}

		sqlCmd = "select to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'00001' h_batchno1,";
		sqlCmd += "to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'99999' h_batchno2 ";
		sqlCmd += " from dual ";
		recordCnt1 = selectTable();
		if (recordCnt1 > 0) {
			hBatchno1 = getValue("h_batchno1");
			hBatchno2 = getValue("h_batchno2");
		}

		sqlCmd = "select to_char(sysdate,'yyyy') h_ware_year,";
		sqlCmd += "to_number(to_char(sysdate,'mm')) h_ware_month ";
		sqlCmd += " from dual ";
		recordCnt1 = selectTable();
		if (recordCnt1 > 0) {
			hWareYear = getValue("h_ware_year");
			hWareMonth = getValueInt("h_ware_month");
			hWhtrYear = hWareYear;
			hWhtrMonth = hWareMonth;
		}

	}

	/***********************************************************************/
	void selectDbcEmboss() throws Exception {

		sqlCmd = "select ";
		sqlCmd += "decode(a.unit_code,'','0000',a.unit_code) h_dces_unit_code,";
		sqlCmd += "a.card_type,";
		sqlCmd += "a.group_code,";
		sqlCmd += "a.emboss_source,";
		sqlCmd += "a.emboss_reason,";
		sqlCmd += "a.card_no,";
		sqlCmd += "decode(a.ic_flag,'','N',a.ic_flag) h_dces_ic_flag,";
		sqlCmd += "b.card_item, ";
		sqlCmd += "a.rowid  as rowid ";
		sqlCmd += " from dbc_emboss a, crd_item_unit b ";
		sqlCmd += "where a.unit_code = b.unit_code  ";
		sqlCmd += "  and ((a.in_main_date  <> '' and a.apply_source <> 'P' and a.in_main_error  = '0' ) ";
		sqlCmd += "     or (a.apply_source = 'P' and a.to_ibm_date <> '')) ";
		sqlCmd += "  and a.online_mark    = '0'  ";
		sqlCmd += "  and a.warehouse_date = ''  ";
		sqlCmd += "order by b.card_item  ";

		int recordCnt = selectTable();
		for (int i = 0; i < recordCnt; i++) {
			hDcesUnitCode = getValue("h_dces_unit_code", i);
			hDcesCardType = getValue("card_type", i);
			hDcesGroupCode = getValue("group_code", i);
			hDcesEmbossSource = getValue("emboss_source", i);
			hDcesEmbossReason = getValue("emboss_reason", i);
			hDcesCardNo = getValue("card_no", i);
			hDcesIcFlag = getValue("h_dces_ic_flag", i);
			unitCardItem = getValue("card_item", i);
			hDcesRowid = getValue("rowid", i);

			totalCnt++;
			if (totalCnt % 1000 == 0 || totalCnt == 1)
				showLogMessage("I", "", String.format("Data Process record=[%d]\n", totalCnt));

			if (debug == 1) {
				showLogMessage("I", "", "Read card_no=[" + hDcesCardNo + "]" + totalCnt);
			}

			liErr = 0;
			hRealUnitcode = hDcesUnitCode;

			if (!hkeyCardItem.equals(unitCardItem)) {
				if (hCardQty > 0) {
					liErr = crdWhtransInsert(1);
					if (liErr == 0)
						liErr = crdWarehouseUpdate(1);

					if (liErr == 0) {
						commitDataBase();
					} else {
						showLogMessage("I", "", "***  ROLLBACK ****");
						rollbackDataBase();
					}
				}
				hKeyUnitcode = hRealUnitcode;
				hKeyCardtype = hDcesCardType;
				hKeyGroupcode = hDcesGroupCode;
				hKeyIcFlag = hDcesIcFlag;
				hkeyCardItem = unitCardItem;
				hCardQty = 0;
			}

			hCardQty++;
			if (debug == 1)
				showLogMessage("I", "", " unit_code=" + hDcesUnitCode + "," + hRealUnitcode);

			if (!hRealUnitcode.equals(hDcesUnitCode)) {
				daoTable = "dbc_emboss";
				updateSQL = " unit_code      = ?,";
				updateSQL += " warehouse_date = ?";
				whereStr = "where rowid     = ? ";
				setString(1, hRealUnitcode);
				setString(2, hSystemDate);
				setRowId(3, hDcesRowid);
				updateTable();
				if (notFound.equals("Y")) {
					comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
				}
			} else {
				daoTable = "dbc_emboss";
				updateSQL = "warehouse_date = ?";
				whereStr = "where rowid    = ? ";
				setString(1, hSystemDate);
				setRowId(2, hDcesRowid);
				updateTable();
				if (notFound.equals("Y")) {
					comcr.errRtn("update_dbc_emboss not found!", "", hCallBatchSeqno);
				}
			}

		}

		if (liErr == 0) {
			liErr = crdWhtransInsert(2);
			if (liErr == 0)
				liErr = crdWarehouseUpdate(2);
			if (liErr == 0) {
				commitDataBase();
			} else {
				showLogMessage("I", "", "***  ROLLBACK ****");
				rollbackDataBase();
			}
		}
	}

	/***********************************************************************/
	int crdWhtransInsert(int idx) throws Exception {

		hWhtrWarehouseNo = "";
		hWhtrCardItem = "";
		hWhtrWarehouseDate = hSystemDate;
		hWhtrCardType = hKeyCardtype;
		hWhtrGroupCode = hKeyGroupcode;
		hWhtrUnitCode = hKeyUnitcode;
		hWhtrTnsType = "2";
		hWhtrPrevTotal = 0;
		hWhtrUseTotal = hCardQty;
		hWhtrCreateDate = hSystemDate;
		hWhtrTransReason = "1";
		hWhtrCreateId = "SYSTEM";
		hWhtrModUser = "SYSTEM";
		hWhtrModPgm = prgmId;
		hWhtrModSeqno = 0;

		sqlCmd = "select to_char(to_number(max(warehouse_no)) + 1) h_whtr_warehouse_no ";
		sqlCmd += " from crd_whtrans  ";
		sqlCmd += "where warehouse_no between ?  and ? ";
		setString(1, hBatchno1);
		setString(2, hBatchno2);
		int recordCnt1 = selectTable();
		if (recordCnt1 > 0) {
			hWhtrWarehouseNo = getValue("h_whtr_warehouse_no");
		}

		if (hWhtrWarehouseNo.length() == 0)
			hWhtrWarehouseNo = hBatchno1;

		hWhtrPlace = "";
		sqlCmd = "select a.card_item, a.new_vendor, a.mku_vendor, ";
		sqlCmd += " a.chg_vendor, b.default_place  ";
		sqlCmd += " from crd_card_item b, crd_item_unit a ";
		sqlCmd += "where a.card_item = b.card_item ";
		sqlCmd += "  and a.card_item = ?  ";
		
		setString(1, hkeyCardItem);
		
		recordCnt1 = selectTable();
		if (debug == 1)
			showLogMessage("I", "", " select item_unit=" + recordCnt1 + "," + hkeyCardItem + ", source=" + hDcesEmbossSource);
					
		if (recordCnt1 > 0) {
			switch (comcr.str2int(hDcesEmbossSource)) {
			case 1:
				hWhtrPlace = getValue("new_vendor");
				break;
			case 3:
			case 4:
				hWhtrPlace = getValue("chg_vendor");
				break;
			case 5:
			case 6:
			case 7:
				hWhtrPlace = getValue("mku_vendor");
				break;
			}
			hWhtrCardItem = getValue("card_item");
		}
		hWhtrPlace = getValue("default_place");

		if (hWhtrCardItem.length() == 0) {
			showLogMessage("I", "",
					String.format("Read card_item[%s][%s]ic[%s] not found", hKeyCardtype, hKeyUnitcode, hKeyIcFlag));
			return (1);
		}

		crdWarehouseSelect(0);

		insertCnt++;

		if (debug == 1)
			showLogMessage("I", "", " insert whtrans=" + hAllTotalBal + "," + hWhtrWarehouseNo + "," + hWhtrCardItem);

		setValue("warehouse_no", hWhtrWarehouseNo);
		setValue("card_item", hWhtrCardItem);
		setValue("warehouse_date", hWhtrWarehouseDate);
		setValue("card_type", hWhtrCardType);
//      setValue("group_code"      , h_whtr_group_code);
		setValue("unit_code", hWhtrUnitCode);
		setValue("tns_type", hWhtrTnsType);
		setValue("place", hWhtrPlace);
		setValueDouble("prev_total", hAllTotalBal);
		setValueInt("use_total", hWhtrUseTotal);
		setValue("crt_date", hWhtrCreateDate);
		setValue("trans_reason", hWhtrTransReason);
		setValue("apr_flag", "Y");
        setValue("apr_user", javaProgram);
        setValue("apr_date", sysDate);
		setValue("crt_user", hWhtrCreateId);
		setValue("mod_user", hWhtrModUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", hWhtrModPgm);
		setValueDouble("mod_seqno", hWhtrModSeqno);
		daoTable = "crd_whtrans";
		insertTable();
		if (dupRecord.equals("Y")) {
			return (1);
		}

		return (0);
	}

	/*************************************************************************/
	public int crdWarehouseSelect(int idx) throws Exception {
		hAllTotalBal = 0;
		selectSQL = "  sum(pre_total   " + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
				+ "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
				+ "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
				+ "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12)) all_total_bal ";
		daoTable = "crd_warehouse ";
		whereStr = "where wh_year    =  ? " + "  and card_item  =  ? " + "  and place      =  ? ";

		setString(1, hWhtrYear);
		setString(2, hWhtrCardItem);
		setString(3, hWhtrPlace);

		tmpInt = selectTable();

		hAllTotalBal = getValueDouble("all_total_bal");

		if (debug == 1)
			showLogMessage("I", "", " select whtr=[" + hWhtrCardItem + "," + hWhtrPlace + "]" + hAllTotalBal);

		return (0);
	}

	/***********************************************************************/
	int crdWarehouseUpdate(int idx) throws Exception {
		if (debug == 1)
			showLogMessage("I", "", "crd_warehouse_update [" + idx + "]" + hCardQty);

		double remainCardQty = hCardQty;

		crdWarehouseInit();
		seqNo = 0;

		/* brain */
		selectSQL = "out_qty01  , out_qty02   , out_qty03   , out_qty04   ,"
				+ "out_qty05  , out_qty06   , out_qty07   , out_qty08   ,"
				+ "out_qty09  , out_qty10   , out_qty11   , out_qty12   ," + "  pre_total   "
				+ "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
				+ "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
				+ "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
				+ "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) h_pre_total_bal,"
				+ "item_amt   , lot_no      , rowid   as rowid    ";
		daoTable = "crd_warehouse ";
		whereStr = "where wh_year    =  ? " + "  and card_item  =  ? " + "  and place      =  ? " + "order by lot_no ";

		setString(1, hWareWhYear);
		setString(2, hWareCardItem);
		setString(3, hWarePlace);
		int tmpInt = selectTable();

		if (notFound.equals("Y")) {
			tmpInt = crdWarehouseInsert();
			return (1);
		}
		if (debug == 1)
			showLogMessage("I", "", " select ware_hou=" + hWhtrCardItem + "," + hWhtrPlace + "," + tmpInt);

		for (int i = 0; i < tmpInt; i++) {
			hWareLotNo = getValue("lot_no", i);
			hWareOutQty01 = getValueDouble("out_qty01", i);
			hWareOutQty02 = getValueDouble("out_qty02", i);
			hWareOutQty03 = getValueDouble("out_qty03", i);
			hWareOutQty04 = getValueDouble("out_qty04", i);
			hWareOutQty05 = getValueDouble("out_qty05", i);
			hWareOutQty06 = getValueDouble("out_qty06", i);
			hWareOutQty07 = getValueDouble("out_qty07", i);
			hWareOutQty08 = getValueDouble("out_qty08", i);
			hWareOutQty09 = getValueDouble("out_qty09", i);
			hWareOutQty10 = getValueDouble("out_qty10", i);
			hWareOutQty11 = getValueDouble("out_qty11", i);
			hWareOutQty12 = getValueDouble("out_qty12", i);
			hWareItemAmt = getValueDouble("item_amt", i);
			hPreTotalBal = getValueDouble("h_pre_total_bal", i);
			hWareRowid = getValue("rowid", i);
			if (debug == 1)
				showLogMessage("I", "",
						"    whtr lot=" + hWareLotNo + "," + i + "," + hPreTotalBal + " remain=" + remainCardQty);
			tmpQty = hPreTotalBal; /* 該批號剩餘庫存 */
			if (i + 1 == tmpInt) /* 最後一筆批號全加 */
			{
				tmpQty = remainCardQty;
			} else {
				if (hPreTotalBal < 1)
					continue; // 出庫
				if (remainCardQty < tmpQty) {
					tmpQty = remainCardQty;
					remainCardQty = 0;
				} else
					remainCardQty = remainCardQty - tmpQty;
			}

			switch (hWareMonth) {
			case 1:
				hWareOutQty01 += tmpQty;
				break;
			case 2:
				hWareOutQty02 += tmpQty;
				break;
			case 3:
				hWareOutQty03 += tmpQty;
				break;
			case 4:
				hWareOutQty04 += tmpQty;
				break;
			case 5:
				hWareOutQty05 += tmpQty;
				break;
			case 6:
				hWareOutQty06 += tmpQty;
				break;
			case 7:
				hWareOutQty07 += tmpQty;
				break;
			case 8:
				hWareOutQty08 += tmpQty;
				break;
			case 9:
				hWareOutQty09 += tmpQty;
				break;
			case 10:
				hWareOutQty10 += tmpQty;
				break;
			case 11:
				hWareOutQty11 += tmpQty;
				break;
			case 12:
				hWareOutQty12 += tmpQty;
				break;
			default:
				return (1);
			}
			if (debug == 1)
				showLogMessage("I", "", " update  crd_warehouse !!!");

			updateSQL = " out_qty01 = ?,";
			updateSQL += " out_qty02 = ?,";
			updateSQL += " out_qty03 = ?,";
			updateSQL += " out_qty04 = ?,";
			updateSQL += " out_qty05 = ?,";
			updateSQL += " out_qty06 = ?,";
			updateSQL += " out_qty07 = ?,";
			updateSQL += " out_qty08 = ?,";
			updateSQL += " out_qty09 = ?,";
			updateSQL += " out_qty10 = ?,";
			updateSQL += " out_qty11 = ?,";
			updateSQL += " out_qty12 = ?,";
			updateSQL += " mod_user  = ?,";
			updateSQL += " mod_time  = sysdate,";
			updateSQL += " mod_pgm   = ?,";
			updateSQL += " mod_seqno = 0 ";
			daoTable = "crd_warehouse ";
			whereStr = "where rowid   = ? ";
			setDouble(1, hWareOutQty01);
			setDouble(2, hWareOutQty02);
			setDouble(3, hWareOutQty03);
			setDouble(4, hWareOutQty04);
			setDouble(5, hWareOutQty05);
			setDouble(6, hWareOutQty06);
			setDouble(7, hWareOutQty07);
			setDouble(8, hWareOutQty08);
			setDouble(9, hWareOutQty09);
			setDouble(10, hWareOutQty10);
			setDouble(11, hWareOutQty11);
			setDouble(12, hWareOutQty12);
			setString(13, hWareModUser);
			setString(14, hWareModPgm);
			setRowId(15, hWareRowid);
			updateTable();
			if (notFound.equals("Y")) {
				showLogMessage("I", "", " not find crd_warehouse !!!");
				return (1);
			}

			insertCrdWhtxDtl(hWareLotNo, tmpQty, hWareItemAmt);

			if (remainCardQty == 0)
				break;

		}

		return (0);
	}

	/***********************************************************************/
	int crdWarehouseInsert() throws Exception {

		crdWarehouseInit();

		setValue("wh_year", hWareWhYear);
		setValue("card_item", hWareCardItem);
		setValue("place", hWarePlace);
		setValue("lot_no", sysDate + "01");
		daoTable = "crd_warehouse";
		insertTable();
		if (dupRecord.equals("Y")) {
			return (1);
		}

		return (0);
	}

	/****************************************************************/
	public void insertCrdWhtxDtl(String iLotNo, double tmpQty, double iItemAmt) throws Exception {
		if (debug == 1)
			showLogMessage("I", "", " insert dtl=" + tmpQty + "," + hWhtrWarehouseNo + "," + hPreTotalBal);

		seqNo++;
		setValue("warehouse_no", hWhtrWarehouseNo);
		setValueInt("seq_no", seqNo);
		setValue("warehouse_date", sysDate);
		setValue("card_item", hWhtrCardItem);
		setValue("lot_no", iLotNo);
		setValueDouble("item_amt", iItemAmt);
//  setValue("card_type"       , h_key_cardtype);
//  setValue("group_code"      , h_key_groupcode);
//  setValue("unit_ocde"       , h_key_unitcode);
		setValue("tns_type", "2");
		setValue("place", hWhtrPlace);
		setValueDouble("prev_total", hPreTotalBal);
		setValueDouble("use_total", tmpQty);

		daoTable = "crd_whtx_dtl ";

		insertTable();
		// TODO Auto-generated method stub

	}

	/*************************************************************************/
	void crdWarehouseInit() {
		hWareWhYear = hWareYear;
		hWareCardItem = hWhtrCardItem;
		hWarePlace = hWhtrPlace;
		hWareOutQty01 = 0;
		hWareOutQty02 = 0;
		hWareOutQty03 = 0;
		hWareOutQty04 = 0;
		hWareOutQty05 = 0;
		hWareOutQty06 = 0;
		hWareOutQty07 = 0;
		hWareOutQty08 = 0;
		hWareOutQty09 = 0;
		hWareOutQty10 = 0;
		hWareOutQty11 = 0;
		hWareOutQty12 = 0;
		hWareModUser = "SYSTEM";
		hWareModPgm = prgmId;
		hAllTotalBal = 0;
		hWareLotNo = "";
		hWareItemAmt = 0;
		tmpQty = 0;
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		DbcD012 proc = new DbcD012();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
	/***********************************************************************/
}
