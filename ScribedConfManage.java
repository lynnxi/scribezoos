package scribedzoos;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

class ScribedConfManage {
	static String CONFIF_FILE_PATH = "/data1/scribed/conf/central_test.conf";
	byte[] confCode;
	/***
	 * 将zk读到的配置文件回写到本机的scribed指定的目录
	 * @param confCode
	 */
	public ScribedConfManage(byte[] confCode){
		this.confCode = confCode;
	}
	/**
	 * 构造函数
	 */
	public ScribedConfManage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * 将数据写入到配置文件
	 */
	public void setScribedConfFile(){
		try {
			PrintWriter confWriter = new PrintWriter(CONFIF_FILE_PATH);
			confWriter.print(new String(confCode));
			confWriter.flush();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取scribe线上服务器
	 * @return
	 */
	public String getScribedConfFile(){
		
		return null;
	}
	
	/***
	 * 获取服务器IP
	 * @return
	 */
	public String getLocalHost(){
		//获取服务器IP
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip=addr.getHostAddress().toString();
			System.out.println(ip);
			return ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * conf配置修改后, 重启scribe服务器
	 */
	public void restartScribed(){
		try {
			Process p = new ProcessBuilder("/bin/sh", "/data1/scribed/bin/restart_scribed.sh").start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s;
			while ((s = br.readLine()) != null){
				System.out.println(s); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
