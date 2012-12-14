package scribedzoos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;


/*****
 * 根据参数，将数据写入到zookeeper,/scribed/conf/mconf|sconf中
 * @author xiaozhen
 *
 */
public class SetScribeConf2Zoo {
	public String confPath;
	public String confMode;
	public void  setFilePath(String path){
		this.confPath = path;	
	}
	
	public void setMode(String mode){
		this.confMode = mode;
	}
	
	/***
	 * 根据配置文件路径，读取内容
	 * @return
	 */
	public String readConfFile(){
		File file = new File(this.confPath);
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String tempString = null;
		    StringBuffer contentBuffer = new StringBuffer();
		    while ((tempString = reader.readLine()) != null) {
		        // 显示行号
		    	contentBuffer.append(tempString+"\n");   
		    }
		    reader.close();
		    return contentBuffer.toString();
		    
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    if (reader != null) {
		        try {
		            reader.close();
		        } catch (IOException e1) {
		        }
		    }
		}
		return null;
	}
	
	public boolean setZooConf(String conf){
		try {
			byte[] midbytes=conf.getBytes("UTF8");
			ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 20000, null);
			if(this.confMode.equalsIgnoreCase("mconf")){
				zk.setData("/scribed/conf/mconf", midbytes, -1);
			
			}else if (this.confMode.equalsIgnoreCase("sconf")){
				zk.setData("/scribed/conf/sconf", midbytes, -1);
			}
			
			zk.close();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static void main(String[] args) {
		System.out.println(args.length);
		if( args.length != 2){
			 System.out.println("请出入ConfMode,和配置文件地址");
		     try {
				throw new Exception("参数错误");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		SetScribeConf2Zoo sc2z = new SetScribeConf2Zoo();
		sc2z.setFilePath(args[0]);
		sc2z.setMode(args[1]);
		sc2z.setZooConf( sc2z.readConfFile() );
	}
}
