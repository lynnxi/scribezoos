package scribedzoos;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

abstract class ScribeWatcher implements Watcher {
	final String LEADER = "Leader";
	final String FOLLOWER = "Follower";
	
	/**
	 * 根据节点状态获取新的配置文件
	 * @param stat_code
	 */
	public void restartConf(ZooKeeper zk){
		try {
			String stat_code =  getZooNewStat();
			System.out.println(stat_code);
			if(stat_code.equalsIgnoreCase(LEADER)){	
				byte[] mconf= zk.getData(ScribedZoos.mconfPath, false, new Stat());
				ScribedConfManage scf = new ScribedConfManage(mconf);
				
				System.out.println(new String(mconf));
				scf.setScribedConfFile();
				scf.restartScribed();
				
			}else if(stat_code.equalsIgnoreCase(FOLLOWER)){
				
				byte[] sconf= zk.getData(ScribedZoos.sconfPath, false, new Stat());	
				ScribedConfManage scf = new ScribedConfManage(sconf);
				
				System.out.println(new String(sconf));
				
				scf.setScribedConfFile();
				scf.restartScribed();
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/***
	 * 重新获取节点状态
	 * @return
	 */
	public String getZooNewStat(){
		ScribedZoos szoo = new ScribedZoos();
		return szoo.getZooStat("127.0.0.1", 2181);
	}
	
	public void Log(String msg){
		
		
		
	}
}
