/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
*  109/07/06  V0.00.00    Zuwei     coding standard, rename field method & format                   *
*  110/01/19  V1.00.01    Justin      set the key word called PDPA
*  110/12/23  V1.00.02    Justin    log4j1 -> log4j2                         *
*****************************************************************************/
package Dxc.Util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.apache.logging.log4j.core.Logger;

public class MaskLogger extends Logger {

	private Logger logger = null;
	private Base64.Encoder encoder = Base64.getEncoder();
	private Base64.Decoder decoder = Base64.getDecoder();
	
	public MaskLogger(Logger logger) {
//		super(logger.getName());
		super(logger.getContext(), logger.getName(), logger.getMessageFactory());
		this.logger = logger;
	}
	
	// Object
	public void debug(Object message) {
		logger.debug(mask(message));
	}
	public void debug(Object message, Throwable t) {
		logger.debug(mask(message), t);
	}
	public void error(Object message) {
		logger.error(mask(message));
	}
	public void error(Object message, Throwable t) {
		logger.error(mask(message), t);
	}
	public void fatal(Object message) {
		logger.fatal(mask(message));
	}
	public void fatal(Object message, Throwable t) {
		logger.fatal(mask(message), t);
	}
	public void info(Object message) {
		logger.info(mask(message));
	}
	public void info(Object message, Throwable t) {
		logger.info(mask(message), t);
	}
	public void warn(Object message) {
		logger.warn(mask(message));
	}
	public void warn(Object message, Throwable t) {
		logger.warn(mask(message), t);
	}
	public void privacy(Object message) {
		logger.info("[PDPA]" + mask(message));
	}
	
	// String
    public void debug(String message) {
		logger.debug(mask(message));
	}
	public void debug(String message, Throwable t) {
		logger.debug(mask(message), t);
	}
	public void error(String message) {
		logger.error(mask(message));
	}
	public void error(String message, Throwable t) {
		logger.error(mask(message), t);
	}
	public void fatal(String message) {
		logger.fatal(mask(message));
	}
	public void fatal(String message, Throwable t) {
		logger.fatal(mask(message), t);
	}
	public void info(String message) {
		logger.info(mask(message));
	}
	public void info(String message, Throwable t) {
		logger.info(mask(message), t);
	}
	public void warn(String message) {
		logger.warn(mask(message));
	}
	public void warn(String message, Throwable t) {
		logger.warn(mask(message), t);
	}
	public void privacy(String message) {
		logger.info("[PDPA]" + mask(message));
	}
	
	
	/**
	 * mask log data
	 * @param message
	 * @return
	 */
	private Object mask(Object message) {
		if (message == null) {
			return message;
		}
		String str = message.toString();
		try {
			str = encoder.encodeToString(str.getBytes("UTF-8"));
			str = str.replaceAll("\n", "");
		} catch (UnsupportedEncodingException e) {
			return message;
		}
		str = ";;;~~~" + str + "~~~;;;";
		return str;
	}

	
}
