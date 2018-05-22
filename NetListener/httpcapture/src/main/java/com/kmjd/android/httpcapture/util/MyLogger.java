package com.kmjd.android.httpcapture.util;

import android.util.Log;

import java.util.Hashtable;

/**
 * The class for print log
 * 
 * @author kesenhoo
 * 
 */
public class MyLogger {
	private final static boolean logFlag = true;

	private final static String TAG = "kmjd_android";//程序名
	private final static int logLevel = Log.VERBOSE;
	private static Hashtable<String, MyLogger> sLoggerTable = new Hashtable<String, MyLogger>();
	private String mClassName;
	// 不同开发人员的日志使用对象
	private static MyLogger zlog;
	private static MyLogger xlog;
	private static MyLogger hlog;
	private static MyLogger flog;
	private static MyLogger mlog;
	// 开发人员的名字
	private static final String ZYM = "@zym@ ";
	private static final String XS = "@xs@ ";
	private static final String HLN = "@hln@ ";
	private static final String ZYF = "@zyf@ ";
	private static final String MWB = "@mwb@ ";

	private MyLogger(String name) {
		mClassName = name;
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	@SuppressWarnings("unused")
	private static MyLogger getLogger(String className) {
		MyLogger classLogger = (MyLogger) sLoggerTable.get(className);
		if (classLogger == null) {
			classLogger = new MyLogger(className);
			sLoggerTable.put(className, classLogger);
		}
		return classLogger;
	}

	/**
	 * Purpose:Mark user one
	 * 
	 * @return
	 */
	public static MyLogger zLog() {
		if (zlog == null) {
			zlog = new MyLogger(ZYM);
		}
		return zlog;
	}
	
	/**
	 * Purpose:Mark user one
	 * 
	 * @return
	 */
	public static MyLogger xLog() {
		if (xlog == null) {
			xlog = new MyLogger(XS);
		}
		return xlog;
	}

	/**
	 * Purpose:Mark user two
	 * 
	 * @return
	 */
	public static MyLogger hLog() {
		if (hlog == null) {
			hlog = new MyLogger(HLN);
		}
		return hlog;
	}

	/**
	 * Purpose:Mark user two
	 *
	 * @return
	 */
	public static MyLogger fLog() {
		if (flog == null) {
			flog = new MyLogger(ZYF);
		}
		return flog;
	}

	/**
	 * Purpose:Mark user two
	 *
	 * @return
	 */
	public static MyLogger mLog() {
		if (mlog == null) {
			mlog = new MyLogger(MWB);
		}
		return mlog;
	}

	/**
	 * Get The Current Function Name
	 * 
	 * @return
	 */
	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}
			return mClassName + "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() + " ]";
		}
		return null;
	}

	/**
	 * The Log Level:i
	 * 
	 * @param str
	 */
	public void i(Object str) {
		if (logFlag) {
			if (logLevel <= Log.INFO) {
				String name = getFunctionName();
				if (name != null) {
					Log.i(TAG, name + " - " + str);
				} else {
					Log.i(TAG, str.toString());
				}
			}
		}

	}

	/**
	 * The Log Level:d
	 * 
	 * @param str
	 */
	public void d(Object str) {
		if (logFlag) {
			if (logLevel <= Log.DEBUG) {
				String name = getFunctionName();
				if (name != null) {
					Log.d(TAG, name + " - " + str);
				} else {
					Log.d(TAG, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:V
	 * 
	 * @param str
	 */
	public void v(Object str) {
		if (logFlag) {
			if (logLevel <= Log.VERBOSE) {
				String name = getFunctionName();
				if (name != null) {
					Log.v(TAG, name + " - " + str);
				} else {
					Log.v(TAG, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:w
	 * 
	 * @param str
	 */
	public void w(Object str) {
		if (logFlag) {
			if (logLevel <= Log.WARN) {
				String name = getFunctionName();
				if (name != null) {
					Log.w(TAG, name + " - " + str);
				} else {
					Log.w(TAG, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param str
	 */
	public void e(Object str) {
		if (logFlag) {
			if (logLevel <= Log.ERROR) {
				String name = getFunctionName();
				if (name != null) {
					Log.e(TAG, name + " - " + str);
				} else {
					Log.e(TAG, str.toString());
				}
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param ex
	 */
	public void e(Exception ex) {
		if (logFlag) {
			if (logLevel <= Log.ERROR) {
				Log.e(TAG, "error", ex);
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param log
	 * @param tr
	 */
	public void e(String log, Throwable tr) {
		if (logFlag) {
			String line = getFunctionName();
			Log.e(TAG, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] " + log + "\n", tr);
		}
	}
}