package scribedzoos;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.hadoop.fs.shell.Count;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException;

public class ScribedZoos {
	static String mconfPath = "/scribed/conf/mconf";
	static String sconfPath = "/scribed/conf/sconf";
	private  CountDownLatch connectedSignal_alive;
	private  CountDownLatch connectedSignal_master;
	private  CountDownLatch connectedSignal_slave;
	
	/**
	 * Main函数
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("HelloWorld!");
		try{
			Thread.sleep(1000);
			//创建一个Zookeeper实例，第一个参数为目标服务器地址和端口，第二个参数为Session超时时间，第三个为节点变化时的回调方法
			final CountDownLatch zooConnLock = new CountDownLatch(1);
		    final ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 20000, new Watcher(){
				public void process(WatchedEvent event){
					if(event.getState() == KeeperState.SyncConnected ){
						zooConnLock.countDown();
					}
				}
			});
			zooConnLock.await();
			
			final ScribedZoos zoo = new ScribedZoos();
			final String  zkMode = zoo.getZooStat("127.0.0.1", 2181);
			
			//zkMode.equals("follower") zkMode.equals("leader")
			System.out.println(zkMode);
			
			Thread t = new Thread(){
				@Override
				public void run() {
					zoo.doAliveWather(zk,zkMode);
				}
			};
			t.start();
			
			Thread t2 = new Thread(){
				@Override
				public void run() {
					zoo.doModifiedWatherMaster(zk);
				}
			};
			t2.start();
			
			
			Thread t3 = new Thread(){
				@Override
				public void run() {
					zoo.doModifiedWatherSlave(zk);
				}
			};
			t3.start();
			
			
			System.out.println("ssssssssssssssss");
				      
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建并临时节点目录，并注册回调事件
	 */
	public void doAliveWather(final ZooKeeper zk, String mode){
		String localIp = getLocalHost();
		final String allPath = "/scribed/iplist";
		String myPath = "/scribed/iplist/"+mode+"_"+localIp;
		System.out.println(myPath);
		//创建临时节点
		try {
			zk.create(myPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//注册回调事件
		try {
			while(true){	
				zk.getChildren(allPath, new ScribeWatcher(){
					@Override
					public void process(WatchedEvent event) {
						try {
							restartConf(zk);
							List t = zk.getChildren(allPath, false);
							for (Object o : t) {
								System.out.println(o);
								
							}
						} catch (KeeperException e) {	
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("||sss||");
						connectedSignal_alive.countDown();
					}});
				connectedSignal_alive = new CountDownLatch(1);
				connectedSignal_alive.await();
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 *对conf配置目录，注册回调事件
	 */
	public void doModifiedWatherMaster(final ZooKeeper zk){
		System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
		try {
			while(true){
				zk.exists(mconfPath, new ScribeWatcher(){
					@Override
					public void process(WatchedEvent event) {
						try {
							System.out.println("start master");
							restartConf(zk);
						
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						connectedSignal_master.countDown();
						
					}});
				
				connectedSignal_master = new CountDownLatch(1);
				connectedSignal_master.await();
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *对conf配置目录，注册回调事件
	 */
	public void doModifiedWatherSlave(final ZooKeeper zk){
		System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
		try {
			while(true){				
				zk.exists(sconfPath, new ScribeWatcher(){
					@Override
					public void process(WatchedEvent event) {
						try {
							System.out.println("start slaver");
							restartConf(zk);
						} catch (Exception e) {
							e.printStackTrace();
						}
						connectedSignal_slave.countDown();
					}});
				connectedSignal_slave = new CountDownLatch(1);
				connectedSignal_slave.await();
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/***
	 * 读取zk中 scribed 的 master 角色的配置文件
	 * @param zk
	 */
	public void getLeaderScriberConf(ZooKeeper zk){
		try {
			byte[] nodedata = zk.getData(ScribedZoos.mconfPath, false, null);
			System.out.println(new String(nodedata));
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/***
	 * 读取zk中 scribed 的 slaver 角色的配置文件
	 * @param zk
	 */
	public void getSlaverScriberConf(ZooKeeper zk){
		try {
			byte[] nodedata = zk.getData(ScribedZoos.sconfPath, false, null);
			System.out.println(new String(nodedata));
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	//获取zookeeper的角色状态
	public String getZooStat(String host, int port) {
        Socket s = null;
        try {
            byte[] reqBytes = new byte[4];
            ByteBuffer req = ByteBuffer.wrap(reqBytes);
            req.putInt(ByteBuffer.wrap("stat".getBytes()).getInt());
            s = new Socket();
            s.setSoLinger(false, 10);
            s.setSoTimeout(20000);
            s.connect(new InetSocketAddress(host, port));

            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            os.write(reqBytes);

            byte[] resBytes = new byte[1024];

            int rc = is.read(resBytes);
            String retv = new String(resBytes);
			String[] arrStatus = retv.split("Mode: ");
			String[] mode = arrStatus[1].split("\n");
            return mode[0];
        } catch (IOException e) {
        	 e.printStackTrace();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
	
	/**
	 * 获取服务器执行IP
	 * @return
	 */
	public String getLocalHost(){
		//获取服务器IP
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip =  addr.getHostAddress().toString();
			System.out.println(ip);
			return ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
}
