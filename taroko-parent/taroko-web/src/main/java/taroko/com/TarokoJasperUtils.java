/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 110-09-14  V1.00.01  Jiangyingdong       init                              *
******************************************************************************/
package taroko.com;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import Dxc.Util.SecurityUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * JasperReports utility
 * 
 * @author zsu4
 *
 */
public class TarokoJasperUtils {
	/**
	 * 導出報表
	 * @param wp
	 * @param jasperFileName 模板文件名，不需要拓展名
	 * @param exportProgressName 當前程序名
	 * @param parameters 填充模板的屬性
	 * @param list 填充模板的列表數據
	 * @throws Exception
	 */
	public static void exportPdf(TarokoCommon wp ,String jasperFileName, String exportProgressName, Map<String, Object> parameters, Collection<?> list) throws Exception {
		// jasper模板文件的路徑
		String jasperPath = SecurityUtil.verifyPath(TarokoParm.getInstance().getDataRoot()) + "/JasperTemplate/" + jasperFileName + ".jasper";
		// pdf報表保存的路徑
		String pdfFileName = wp.sysDate.substring(6) + "u" + wp.loginUser + "_" + exportProgressName + "-" + wp.sysTime + ".pdf";
		String exportFilePath = SecurityUtil.verifyPath(TarokoParm.getInstance().getWorkDir()) + pdfFileName;
		
		// 導出 pdf
		if (list != null) {
			exportPdf(jasperPath, exportFilePath, parameters, list);
		} else {
			exportPdf(jasperPath, exportFilePath, parameters, wp.getConn());
		}
		
		wp.exportFile = pdfFileName;
		wp.linkMode = "Y";
        wp.linkURL = wp.getWorkPath(wp.exportFile);
    }
	
	/**
	 * 導出報表
	 * @param jasperPath
	 * @param exportFilePath
	 * @param parameters
	 * @throws Exception
	 */
	public static void exportPdf(String jasperPath, String exportFilePath, Map<String, Object> parameters, Collection<?> list) throws Exception {
		OutputStream outputStream = new FileOutputStream(exportFilePath);
		try {
			JRDataSource dataSource = new JRBeanCollectionDataSource(list);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, dataSource);
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			outputStream.flush();
            outputStream.close();
		}
    }
	
	/**
	 * 導出報表
	 * @param jasperPath
	 * @param exportFilePath
	 * @param parameters
	 * @throws Exception
	 */
	public static void exportPdf(String jasperPath, String exportFilePath, Map<String, Object> parameters, Connection connection) throws Exception {
		OutputStream outputStream = new FileOutputStream(exportFilePath);
		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, connection);
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			outputStream.flush();
            outputStream.close();
		}
    }

}
