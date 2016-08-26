package com.util.nivsoft.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;

/**
 * Tns object representation
 * 
 * @author vinicios.rodrigues
 *
 */
public class TNSInformations {

	// List name of alias
	private List<String> aliasList;

	// TNS Alias informations
	private Integer port;
	private String host;
	private String path;
	private String serviceName;

	public TNSInformations() {
		this.aliasList = FXCollections.observableArrayList();
		this.path = null;
	}

	public List<String> getAliasList() {
		return aliasList;
	}

	public void setAliasList(List<String> aliasList) {
		this.aliasList = aliasList;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Extract all Alias from Tns file
	 * 
	 * @param file
	 * @return this
	 */
	public TNSInformations extract(File file) {
		String absoluteTnsPath = null;
		String pattern = "^([^#()\\W ][a-zA-Z0-9._]*(?:[.][a-zA-Z]*\\s?=)?)";
		Pattern r = Pattern.compile(pattern);
		if (file != null) {
			absoluteTnsPath = file.getAbsolutePath();
			this.path = absoluteTnsPath;

			// Read tnsnames.ora file
			FileReader readFile = null;
			try {
				readFile = new FileReader(file.getAbsolutePath());
				BufferedReader readFileBuffer = new BufferedReader(readFile);
				String line = readFileBuffer.readLine();
				StringBuilder strBuilder = new StringBuilder();
				while (line != null) {
					strBuilder.append(line);
					// Set Matcher into line
					Matcher matcher = r.matcher(line);
					if (matcher.find()) {
						this.aliasList.add(matcher.group().toUpperCase());
					}
					line = readFileBuffer.readLine();
				}
				aliasList.sort((x, y) -> x.compareToIgnoreCase(y));
				readFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Return alias list
		return this;
	}

	/**
	 * TNS information extraction by alias
	 * 
	 * @param tnsFile
	 * @param alias
	 */
	public void getTNSInformationsByAlias(File tnsFile, String alias) {
		String absoluteTnsPath = null;
		boolean haveHost = true;
		boolean havePort = true;
		boolean haveService = true;

		if (tnsFile != null) {
			absoluteTnsPath = tnsFile.getAbsolutePath();
			this.path = absoluteTnsPath;
			FileReader readFile = null;
			try {

				readFile = new FileReader(tnsFile.getAbsolutePath());
				BufferedReader readFileBuffer = new BufferedReader(readFile);
				String line = readFileBuffer.readLine();

				while (line != null) {
					if (line.trim().contains(alias)) {
						while (haveHost || havePort || haveService) {
							if (line.trim().contains("HOST")) {
								host = getHostByLine(line);
								haveHost = false;
							}
							if (line.trim().contains("PORT")) {
								port = getPortByLine(line);
								havePort = false;
							}
							if (line.trim().contains("SERVICE_NAME")) {
								serviceName = getServiceNameByLine(line);
								haveService = false;
							}
							line = readFileBuffer.readLine();
						}
						break;
					}
					line = readFileBuffer.readLine();
				}
				readFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 * Get host by line
	 * 
	 * @param line
	 * @return HOST
	 */
	public String getHostByLine(String line) {
		line = line.substring(line.indexOf("HOST"));
		line = line.substring(0, line.indexOf(")"));
		line = line.replace("HOST", "").replace("=", "").trim();
		return line;
	}

	/**
	 * 
	 * Get port by line
	 * 
	 * @param line
	 * @return PORT
	 */
	public Integer getPortByLine(String line) {
		line = line.substring(line.indexOf("PORT"));
		line = line.substring(0, line.indexOf(")"));
		line = line.replace("PORT", "").replace("=", "").trim();
		return Integer.parseInt(line);
	}

	/**
	 * Get service name by line
	 * 
	 * @param line
	 * @return SERVICE_NAME
	 */
	public String getServiceNameByLine(String line) {
		line = line.substring(line.indexOf("SERVICE_NAME"));
		line = line.substring(0, line.indexOf(")"));
		line = line.replace("SERVICE_NAME", "").replace("=", "").trim();
		return line;
	}

	/*
	 * Main method for test. Remember that the path of tns is what is on your
	 * machine.
	 */
	public static void main(String[] args) {
		TNSInformations tns = new TNSInformations();
		File pathTns = new File("C:\\app\\oracle\\product\\11.2.0\\client_1\\NETWORK\\ADMIN\\tnsnames.ora");
		tns = tns.extract(pathTns);
		for (String alias : tns.getAliasList()) {
			tns.getTNSInformationsByAlias(pathTns, alias);

			System.out.println("_______________________________________");
			System.out.println("Alias: " + alias);
			System.out.println("Host: " + tns.getHost());
			System.out.println("Port: " + tns.getPort());
			System.out.println("Service name: " + tns.getServiceName());
			System.out.println("_______________________________________\n\n");
		}
	}
}
