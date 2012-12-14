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
	 * ��zk�����������ļ���д��������scribedָ����Ŀ¼
	 * @param confCode
	 */
	public ScribedConfManage(byte[] confCode){
		this.confCode = confCode;
	}
	/**
	 * ���캯��
	 */
	public ScribedConfManage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * ������д�뵽�����ļ�
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
	 * ��ȡscribe���Ϸ�����
	 * @return
	 */
	public String getScribedConfFile(){
		
		return null;
	}
	
	/***
	 * ��ȡ������IP
	 * @return
	 */
	public String getLocalHost(){
		//��ȡ������IP
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
	 * conf�����޸ĺ�, ����scribe������
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
