
package taroko.com;
/** LISTENER FOR WAS STARTUP CHECK DR(合庫台中) DB IP
 * 2020-0520   KEVIN PRIMANY_DB2 IP is PROD(127.0.0.1) OR DR(134.251.80.228) Site
 * 正式環境 DR DB2 NAME:crdb 機電保養對應到 IP:10.0.38.58 平常時對應到 F5 IP:10.0.38.59
 * 測試環境 DR DB2 NAME:crdb 機電保養對應到 IP:10.0.12.19 平常時對應到 F5 IP:10.0.12.20
 * 確認系統非在機電保養狀態，user連接於哪一個環境
 * 2021-0713   Justin initial system parameters
 * */
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Life cycle Listener implementation class OnStartupListener
 *
 */
@WebListener
public class OnStartupListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public OnStartupListener() {
        
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce)  { 
         
    }

	/**
	 * initial system parameters
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
	public void contextInitialized(ServletContextEvent sce)  { 

        try {
        	/* 設定 DbSwitch2Dr */
    		setDbSwitch2Dr(sce); 
    		System.out.println("DB SWITCH TO DR="+ sce.getServletContext().getAttribute(TarokoParm.DB_SWITCH_TO_DR));
    		
        	/* 設定系統參數資訊*/
            TarokoParm stParm = TarokoParm.getInstance();
            stParm.setSystemParm(sce.getServletContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
  	}

	private void setDbSwitch2Dr(ServletContextEvent sce) {
		try {
       		//--addrDr:正式環境 DR DB2 NAME:crdb 機電保養對應到 IP:10.0.38.58 平常時對應到 F5 IP:10.0.38.59
           	InetAddress addrDr = InetAddress.getByName("10.0.38.58");         //設定PROD DR DB2 IP
           	System.out.println(addrDr.getHostName()+" ;PROD DB Ip address : "+ addrDr.getHostAddress());
           	if ("crdb".equals(addrDr.getHostName())) {                        //確認PROD AP是否連接到DR DB2(CRDB) IP:10.0.35.58 
    			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"Y");      //確認系統在機電保養狀態
	           	System.out.println(addrDr.getHostName()+" Switch to PROD DR Ip address : "+ addrDr.getHostAddress());
           	}
           	else {
           		//--addrDrTest:測試環境 DR DB2 NAME:crdb 機電保養對應到 IP:10.0.12.19 平常時對應到 F5 IP:10.0.12.20
           		InetAddress addrDrTest = InetAddress.getByName("10.0.12.19"); //設定TEST DR DB2 IP
               	System.out.println(addrDrTest.getHostName()+" ;TEST DB Ip address : "+ addrDrTest.getHostAddress());
           		if ("crdb".equals(addrDrTest.getHostName())) {                //確認TEST AP是否連接到DR DB2(CRDB) IP:10.0.12.19
        			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"Y");  //確認系統在機電保養狀態
    	           	System.out.println(addrDr.getHostName()+" Switch to TEST DR Ip address : "+ addrDr.getHostAddress());
               	}	
           		else {
               		InetAddress addrDrLocal = InetAddress.getLocalHost(); 
               		String hostName = addrDrLocal.getHostName();
               		System.out.println("local hostname ="+hostName+"===");
               		if ("TW-PF20R50H".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"X"); //測試用，確認系統非在機電保養狀態，並且user連接於TW-PF20R50H的環境
               		}
               		else if ("cr1D".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"D"); //確認系統非在機電保養狀態，並且user連接於開發cr1D的環境
               		}
               		else if ("crap1P".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"1"); //確認系統非在機電保養狀態，並且user連接於正式crap1P的環境
               		}
               		else if ("crap2P".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"2"); //確認系統非在機電保養狀態，並且user連接於正式crap2P的環境
               		}
               		else if ("crap1R".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"3"); //確認系統非在機電保養狀態，並且user連接於正式crap3P的環境
               		}
               		else if ("crap1T".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"4"); //確認系統非在機電保養狀態，並且user連接於測試crap1T的環境
               		}
               		else if ("crap2T".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"5"); //確認系統非在機電保養狀態，並且user連接於測試crap2T的環境
               		}
               		else if ("crap3T".equals(hostName)) {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR,"6"); //確認系統非在機電保養狀態，並且user連接於測試crap3T的環境
               		}
               		else {
               			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR," "); //確認系統非在機電保養狀態，並且user連接於不確定的環境

               		}
               	}           		
           	}
		}
    	catch(UnknownHostException uhe) {
			sce.getServletContext().setAttribute(TarokoParm.DB_SWITCH_TO_DR," "); //系統非在機電保養狀態
        	System.err.println("Unable to find DR DB - PROD ip: 10.0.38.58 or TEST ip: 10.0.12.19"); //設定DR DB2 IP
    	}
	}
 }
    
