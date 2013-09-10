package com.jrun;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;


import com.jrun.*;

public class JrunLinker {
	private Map<String, String> config;
	private String uidFilePath, configFilePath;
	
	final static String uid_model = "[0-9a-zA-Z]+";
	final static String config_session[] = {"Account", "Account", "Client", "Server", "Server", "Account", "Account", "Account", "Account"};
	final static String config_var[] = {"username", "password", "interface", "address", "port", "drop", "type", "n", "mac"};
	
	private JrunError readUidFile() {
		try {
			FileReader uid_reader = new FileReader(uidFilePath);
			BufferedReader uid_buffer_reader = new BufferedReader(uid_reader);
			String uid_tmp;
			
			if (config == null) {
				config = new HashMap<String, String>();
			}
			
			
			while ((uid_tmp = uid_buffer_reader.readLine()) != null) {
				uid_tmp.trim();
//				if (Pattern.matches(uid_model, uid_tmp)) {
					config.put("uid", uid_tmp);
					
					System.out.println(uid_tmp);
					
					uid_buffer_reader.close();
					uid_reader.close();
					
					return JrunError.NORMAL_RETURN; 
//				}
			}
			
			uid_buffer_reader.close();
			uid_reader.close();
		} catch (IOException e) {
		
			return JrunError.ERROR_WHILE_READ_FILE;
		}
		
		return JrunError.ERROR_NO_SIGN_MATCH;
	}
	
	private JrunError writeUidFile() {
		try {
			FileWriter uid_writer = new FileWriter(uidFilePath);
			BufferedWriter uid_buffer_writer = new BufferedWriter(uid_writer);
			
			uid_buffer_writer.write(config.get("uid"));
			
			uid_buffer_writer.close();
			uid_writer.close();
		} catch (IOException e) {
		
			return JrunError.ERROR_WHILE_WRITE_FILE;
		}
		
		return JrunError.NORMAL_RETURN;
	}
	
	private JrunError readConfigFile() {
		try {
//			System.out.println(configFilePath);
			
			if (config == null) {
				config = new HashMap<String, String>();
			}
			
//			System.out.println(config_session.length);
			
//			JrunINIReader inireader = new JrunINIReader();
//			inireader.IniReader(configFilePath);
			
			for (int i=0; i<config_session.length; ++i) {
//				System.out.println(i);
//				System.out.println(config_session[i]);
//				System.out.println(config_var[i]);
//				
//				String tmp = inireader.getValue(config_session[i], config_var[i]); 
				String tmp = JrunINIReader.getProfileString(configFilePath, config_session[i], config_var[i], "");
//				System.out.println(tmp+"-");
				
				if (tmp != "") {
//					System.out.printf("%s %s\n", config_var[i], tmp);
					config.put(config_var[i], tmp);
				}
			}
		} catch (IOException e) {
			return JrunError.ERROR_WHILE_READ_FILE;
		}
		
		return JrunError.ERROR_NO_SIGN_MATCH;
	}
	
	private JrunError setMACAdress() {
		String mac_tmp = JrunNetDetect.getMACAddress();
		
		if (mac_tmp == null) {
			
			return JrunError.ERROR_NO_SIGN_MATCH;
		}
		
		if (config == null) {
			config = new HashMap<String, String>();
		}
		
		config.put("mac", mac_tmp);
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunError init_default() {
		setConfigFile();
		setUIDFile();
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunLinker() {
		setConfigFile();
		setUIDFile();
	}
	
	public JrunError setConfigFile() {
		configFilePath = System.getProperty("user.home") + "\\Jrun\\config.ini";
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunError setConfigFile(String path) {
		uidFilePath = path+"\\config.ini";
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunError setUIDFile() {
		uidFilePath = System.getProperty("user.home") + "\\Jrun\\uidfile.dat";
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunError setUIDFile(String path) {
		uidFilePath = path + "\\uidfile.dat";
		
		return JrunError.NORMAL_RETURN;
	}
	
	public JrunError linkToServer(String state) {
		if (state.compareTo("login") == 0) {
			if (readConfigFile() == JrunError.ERROR_WHILE_READ_FILE) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			if (setMACAdress() != JrunError.NORMAL_RETURN) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			try {
				String url = new String("http://" + config.get("address") + ":" + config.get("port") + "/cgi-bin/do_login");
				
				Map<String, String> post_map = new HashMap<String, String>();
				post_map.clear();
				
				for (int i=0; i<config_session.length; ++i) {
					if (config_session[i] == "Account") {
						post_map.put(config_var[i], config.get(config_var[i]));
						System.out.println(config_var[i]+" "+config.get(config_var[i]));
					}
				}
				
				String uid = JrunHttpConector.doPost(url, post_map, "GBK");
				
				System.out.println(uid);
				
//				if (Pattern.matches(uid_model, uid)) {
					config.put("uid", uid);
//				} else {
//					return JrunError.ERROR_NO_SIGN_MATCH;
//				}
				
				writeUidFile();
			} catch (Exception e) {
				return JrunError.ERROR_WHILE_LINK_URL;
			}
		} else if (state.compareTo("logout") == 0) {
			if (readConfigFile() == JrunError.ERROR_WHILE_READ_FILE) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			if (setMACAdress() != JrunError.NORMAL_RETURN) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			if (readUidFile() == JrunError.ERROR_WHILE_READ_FILE) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			try {
				
				String url = new String("http://" + config.get("address") + ":" + config.get("port") + "/cgi-bin/do_logout");
				
				Map<String, String> post_map = new HashMap<String, String>();
				post_map.clear();
				
				post_map.put("uid", config.get("uid"));
				
				String ret = JrunHttpConector.doPost(url, post_map, "UTF-8");
			} catch (Exception e) {
				return JrunError.ERROR_WHILE_LINK_URL;
			}
		} else if (state.compareTo("info") == 0) {
			if (readConfigFile() == JrunError.ERROR_WHILE_READ_FILE) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			if (setMACAdress() != JrunError.NORMAL_RETURN) {
				return JrunError.ERROR_WHILE_READ_FILE;
			}
			
			try {
				String url = new String("http://" + config.get("address") + ":" + "80" + "/user_info1.php");
				System.out.println(url);
				Map<String, String> post_map = new HashMap<String, String>();
				post_map.clear();
				post_map.put("uid", "164265319208851");
				post_map.put("r", "1345559182");
				
				String ret = JrunHttpConector.doGet(url, post_map, "UTF-8");
				
				System.out.println(ret);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
				return JrunError.ERROR_WHILE_LINK_URL;
			}
		} else {
			return JrunError.ERROR_NO_SIGN_MATCH;
		}
		
		
		return JrunError.NORMAL_RETURN;
	}
	
	
	public static void main(String args[]) throws Exception {
		JrunLinker jrunlinker = new JrunLinker();
//		jrunlinker.readConfigFile();
		Scanner input = new Scanner(System.in);
		while (true) {
			String state = input.nextLine();
			System.out.println(state);
			
			JrunError linkerror = jrunlinker.linkToServer(state);
			
			if (linkerror!=JrunError.NORMAL_RETURN) {
				System.out.println(linkerror.toString());
			}
			
		}
	}
}