package com.awspaass.user.apps.syncwechataddress;



import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogPrinter {
	public Logger getMylog(){
        Logger logger = Logger.getLogger("test1");
        logger.setLevel(Level.ALL);
        for(Handler h: logger.getHandlers()){
            //防止出现多个日志文件
            h.close();
        }
        try{
            FileHandler fileHandler = new FileHandler("C:\\WechatAddress.log",true);
            fileHandler.setFormatter(new myFormat());
            
            logger.addHandler(fileHandler);
        }catch (Exception e){
            e.printStackTrace();
        }
        return logger;
    }


}

class myFormat extends Formatter{
/*文件日志格式*/
@Override
	public String format(LogRecord record){
    	ZonedDateTime zdf =ZonedDateTime.now();
    	String sDate  = zdf.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    	return "["+ sDate + "]: "+record.getMessage()+"\n";
	}
}

