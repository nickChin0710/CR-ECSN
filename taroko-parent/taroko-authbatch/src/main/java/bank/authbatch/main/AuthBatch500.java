package bank.authbatch.main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;

import java.io.OutputStreamWriter;

import java.sql.ResultSet;


import bank.authbatch.dao.ActAcnoDao;
import bank.authbatch.dao.AdjNoticeDao;
import bank.authbatch.dao.CardAcctDao;
import bank.authbatch.dao.CrdIdnoDao;

public class AuthBatch500 extends BatchProgBase{

	BufferedWriter G_FileWriter=null;
	int nG_TotRecCount=0;

	
	@Override
	public void startProcess(String[] sP_Parameters) {
		// TODO Auto-generated method stub
	
		
		try {
	
			String sL_TargetDataFileName = sG_ProjHome + "/cca/auth01" + sG_CurDate + ".txt";
			genFileWriter(sL_TargetDataFileName);
				


			connDb();
			if (processAdjNotice()) {
				writeDataFileTailer();
				commitDb();
			}
			else {
				rollbackDb();
			}
		} catch (Exception e) {
			// TODO: handle exception
			writeLog("E", "Process Adj Notice failed!");			
		} finally {
            closeDb();
        }
	}

	private void genFileWriter(String sP_FullPathFileName) {
		
		try {
			//G_FileWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sP_FullPathFileName), "utf-8"));			
			G_FileWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sP_FullPathFileName), "big5"));
		} catch (Exception e) {
			// TODO: handle exception
			writeLog("E", "Exception on AuthBatch_050, message is " + e.getMessage() + "===");
			G_FileWriter = null;
		}
		

	}
	private void writeDataFileTailer() {
//	    fprintf(fptr1,"%-15s","TRAILER : END");
//	    fprintf(fptr1,"%8.8s%06d\n",db_wk_sysdate.arr,totRec);

		if(nG_TotRecCount==0)
			return;
		
		String sL_Tailer ="TRAILER : END";
		sL_Tailer = HpeUtil.fillCharOnRight(sL_Tailer, 15, " ");
		
		String sL_RecCount = HpeUtil.fillCharOnLeft(Integer.toString(nG_TotRecCount), 6, "0");
		try {
			G_FileWriter.write(sL_Tailer + "\n");	
			G_FileWriter.write(sG_CurDate + sL_RecCount + "\n");
			
			G_FileWriter.flush();
			G_FileWriter.close();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		

	}
	
	private void writeDataFileHeader() {
//	    fprintf(fptr1,"%-15s","HEADER : START");
//	    fprintf(fptr1,"%8.8s%6.6s\n",db_wk_sysdate.arr,db_wk_systime.arr);
		String sL_Header ="HEADER : START";
		sL_Header = HpeUtil.fillCharOnRight(sL_Header, 15, " ");
		try {
			G_FileWriter.write(sL_Header + "\n");	
			G_FileWriter.write(sG_CurDate + sG_CurTime + "\n");
		} catch (Exception e) {
			// TODO: handle exception
		}
		

	}

	private boolean processAdjNotice() {
		//proc is fetch_data
		boolean bL_Result = true;
		
		
		try {
			ResultSet L_ActAcnoRs = null;
			ResultSet L_AdjNoticeRs = AdjNoticeDao.getAdjNotice();
			String sL_IdPSeqNo="", sL_CrdIdnoChiName="";
			String sL_NoticeText="";
			while(L_AdjNoticeRs.next()) {
				sL_IdPSeqNo = L_AdjNoticeRs.getString("AdjNoticeIdPSeqNo");
				if (nG_TotRecCount==0)
					writeDataFileHeader();
				
				sL_CrdIdnoChiName = CrdIdnoDao.getChineseName(sL_IdPSeqNo);
				L_ActAcnoRs = ActAcnoDao.getActAcno(sL_IdPSeqNo);
				
				sL_NoticeText = genNoticeText(L_AdjNoticeRs, L_ActAcnoRs, sL_CrdIdnoChiName);
				
				if (!"".equals(sL_NoticeText)) {
					bL_Result = updateData(L_AdjNoticeRs, sL_CrdIdnoChiName);
				}
				
				if (bL_Result)
					nG_TotRecCount++;
				else
					break;
			}
			commitDb();
			L_AdjNoticeRs.close();
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}

	private boolean updateData(ResultSet P_AdjNoticeRs, String sP_CrdIdnoChiName) {
		boolean bL_Result = true;
		
		try {
			//down, update AdjNotice
			
			
			String sL_AdjNoticeCrtDate = P_AdjNoticeRs.getString("AdjNoticeCrtDate");
			String sL_AdjNoticeIdPSeqNo = P_AdjNoticeRs.getString("AdjNoticeIdPSeqNo");
			int dL_AdjNoticeAcctIdx = P_AdjNoticeRs.getInt("AdjNoticeAcctIdx");
 
			bL_Result = AdjNoticeDao.updateAdjNotice( sP_CrdIdnoChiName, sG_CurDate, sL_AdjNoticeCrtDate, sL_AdjNoticeIdPSeqNo, dL_AdjNoticeAcctIdx);
			
			//up, update AdjNotice
			
			if (bL_Result) {
				//down, update CardAcct
				int nL_CardAcctIdx = P_AdjNoticeRs.getInt("AdjNoticeAcctIdx");
				bL_Result = CardAcctDao.updateCardAcct(nL_CardAcctIdx, sG_CurDate);
				//up, update CardAcct
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}
		
		return bL_Result;
	}
	private String genNoticeText(ResultSet P_AdjNoticeRs,  ResultSet P_ActAcnoRs, String sP_CrdIdnoChiName) {
		//proc is create_notice_text()
		
		/*
	    fprintf(fptr1,"CL %-5.5s%-80.80s%-12.12s%-12.12s%12.0f%12.0f%-8.8s%-8.8s\n",
	    		h_bill_zip_code.arr,  =>%-5.5s  (sL_ZipCode)
	    		h_bill_address.arr,   => %-80.80s (sL_Address)
	    		h_m_note_major_chi_name[i].arr, =>%-12.12s (sL_NoteMajorChiName)
	    		h_m_note_chi_name[i].arr, =>%-12.12s (sL_NoteChiName)
	    		h_m_note_org_tot_consume[i], =>%12.0f  (sL_OrgTotConsume)
	    		h_m_note_lmt_tot_consume[i], =>%12.0f  (sL_LmtTotConsume)
	    		h_m_note_adj_eff_start_date[i].arr, =>%-8.8s (sL_AdjEffStartDate)
	    		h_m_note_adj_eff_end_date[i].arr); =>%-8.8s (sL_AdjEffEndDate)
	    		
		*/
		String sL_NoticeText="";
		try {
			
			String sL_ZipCode = "", sL_Address="", sL_LmtTotConsume="";
			String sL_NoteChiName = HpeUtil.fillCharOnLeft(sP_CrdIdnoChiName, 12, " ");
			
			/*
			  P_SEQNO as AdjNoticePSeqNo,ID_P_SEQNO as AdjNoticeIdPSeqNo,SUP_FLAG  as AdjNoticeSupFlag, CRT_TIME  as AdjNoticeCrtTime, " 
				+"CARD_ACCT_IDX as AdjNoticeAcctIdx, CHI_NAME  as AdjNoticeChiName, MAJOR_CHI_NAME  as AdjNoticeMajorChiName,"
				+"ORG_TOT_CONSUME  as AdjNoticeOrgTotConsume, LMT_TOT_CONSUME as AdjNoticeLmtTotConsume, ADJ_EFF_START_DATE as AdjNoticeAdjEffStartDate, " 
				+"ADJ_EFF_END_DATE as AdjNoticeAdjEffEndDate, SEND_DATE as AdjNoticeSendDate, ROWID  as AdjNoticeRowId
			 */
			String sL_NoteMajorChiName = P_AdjNoticeRs.getString("AdjNoticeMajorChiName");
			sL_NoteMajorChiName = HpeUtil.fillCharOnLeft(sL_NoteMajorChiName, 12, " ");
			
			String sL_OrgTotConsume = P_AdjNoticeRs.getString("AdjNoticeOrgTotConsume");
			sL_OrgTotConsume = HpeUtil.fillCharOnRight(sL_OrgTotConsume, 12, "0");
			
			String sL_AdjEffStartDate = P_AdjNoticeRs.getString("AdjNoticeAdjEffStartDate");
			sL_AdjEffStartDate = HpeUtil.fillCharOnLeft(sL_AdjEffStartDate, 8, " ");

			String sL_AdjEffEndDate = P_AdjNoticeRs.getString("AdjNoticeAdjEffEndDate");
			sL_AdjEffEndDate = HpeUtil.fillCharOnLeft(sL_AdjEffEndDate, 8, " ");

			
			while(P_ActAcnoRs.next()) {
				
				sL_ZipCode = P_ActAcnoRs.getString("BILL_SENDING_ZIP").trim();
				sL_ZipCode = HpeUtil.fillCharOnLeft(sL_ZipCode, 5, " ");
				
				sL_Address = P_ActAcnoRs.getString("BILL_SENDING_ADDR1").trim() 
							+P_ActAcnoRs.getString("BILL_SENDING_ADDR2").trim() 
							+P_ActAcnoRs.getString("BILL_SENDING_ADDR3").trim() 
							+P_ActAcnoRs.getString("BILL_SENDING_ADDR4").trim() 
							+P_ActAcnoRs.getString("BILL_SENDING_ADDR5").trim();
				sL_Address = HpeUtil.fillCharOnLeft(sL_Address, 80, " ");
				
				sL_LmtTotConsume = P_ActAcnoRs.getString("LINE_OF_CREDIT_AMT").trim();
				sL_LmtTotConsume = HpeUtil.fillCharOnRight(sL_LmtTotConsume, 12, "0");
				
			}
			P_ActAcnoRs.close();

			sL_NoticeText = "CL " + sL_ZipCode + sL_Address + sL_NoteMajorChiName + sL_NoteChiName + sL_OrgTotConsume
									+ sL_LmtTotConsume + sL_AdjEffStartDate + sL_AdjEffEndDate; 
		} catch (Exception e) {
			// TODO: handle exception
			sL_NoticeText="";
		}
		return sL_NoticeText;
	}
	public AuthBatch500()  throws Exception{
		// TODO Auto-generated constructor stub
	}

	
}
