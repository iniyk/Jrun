package com.jrun;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class JrunNetDetect {
	 public static String getMACAddress() {
		 try {
			 InetAddress ia = InetAddress.getLocalHost();
	         byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
	         
	         StringBuffer sb = new StringBuffer();
	         
	         for(int i=0;i<mac.length;i++){
	             if(i!=0){
	                 sb.append(":");
	             }
	
	             String s = Integer.toHexString(mac[i] & 0xFF);
	             if (i>=2) sb.append(s.length()==1?0+s:s);
	             else sb.append("ff");  //WARNING : while use in anywhere others, this should be fixed!
	         }
	         
	         return sb.toString().toLowerCase();
		 } catch (Exception e) {
			 
			 return null;
		}
     }
	 
	 public static void main(String args[]) {
		 	System.out.println(getMACAddress());
	 }
}
