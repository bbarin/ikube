package ikube.toolkit;

import ikube.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This class will scan the classpath and the file system below where the process was started looking for properties files to load matching the file name set in
 * the file name pattern variable.
 * 
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
@SuppressWarnings("serial")
public class PropertyConfigurer extends Properties {

	private static final Logger LOGGER;

	static {
		Logging.configure();
		LOGGER = Logger.getLogger(PropertyConfigurer.class);
	}

	private String fileNamePattern;

	/**
	 * This method will look through the class path for properties file with the name specified in the file name property. As well as this it sill look through
	 * the file system checking for properties files on the file system and any jars that are on the file system below the application will also be checked for
	 * the properties file name pattern to load into the property map.
	 */
	public void initialize() {
		LOGGER.debug("User directory : " + new File(".").getAbsolutePath());
		// Check all the jars on the class path
		checkClasspathJars();
		// Check the file system for jars that have the properties files
		checkJarsOnFileSystemFromDotFolder();
		// Check the file system for properties files. We read these last as these
		// will override the other properties that we might have found in the other locations
		checkPropertiesFilesOnFileSystemFromDotFolder();
		// Load the properties from our own jar
		checkOwnJar();
		// If the system property for the configuration has not been set then set it to the dot directory
		if (System.getProperty(Constants.IKUBE_CONFIGURATION) == null) {
			System.setProperty(Constants.IKUBE_CONFIGURATION, ".");
		}
		// Load the properties for the system
		File systemProperties = FileUtilities.findFileRecursively(new File("."), "system\\.properties");
		if (systemProperties != null) {
			Properties properties = new Properties();
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(systemProperties);
				properties.load(inputStream);
				for (final Map.Entry<Object, Object> mapEntry : properties.entrySet()) {
					if (mapEntry.getValue() != null) {
						System.setProperty((String) mapEntry.getKey(), (String) mapEntry.getValue());
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception loading the system properties : ", e);
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					LOGGER.error("Exception closing the stream to the system properties : ", e);
				}
			}
		}
		// Finally overwrite the properties with the system properties
		this.putAll(System.getProperties());
	}

	private void checkOwnJar() {
		try {
			// We check our own jar
			File thisJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			checkJar(thisJar);
		} catch (URISyntaxException e) {
			LOGGER.error("Aaai karumbi! Where am I?", e);
		}
	}

	private void checkPropertiesFilesOnFileSystemFromDotFolder() {
		List<File> propertyFiles = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), fileNamePattern);
		for (File propertyFile : propertyFiles) {
			try {
				if (propertyFile == null || !propertyFile.canRead() || propertyFile.isDirectory()) {
					continue;
				}
				LOGGER.info("         : Loading properties from : " + propertyFile);
				this.load(new FileInputStream(propertyFile));
			} catch (Exception e) {
				LOGGER.error("Exception reading property file : " + propertyFile, e);
			}
		}
	}

	private void checkJarsOnFileSystemFromDotFolder() {
		// Check all the jars in the path of the server
		List<File> jarFiles = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), ".jar\\Z");
		for (File jarFile : jarFiles) {
			try {
				checkJar(jarFile);
			} catch (Exception e) {
				LOGGER.error("Exception reading jar file : " + jarFile, e);
			}
		}
	}

	private void checkClasspathJars() {
		try {
			// Check the classpath, this could take a while of course
			String classPathString = System.getProperty("java.class.path");
			StringTokenizer tokenizer = new StringTokenizer(classPathString, ";", Boolean.FALSE);
			while (tokenizer.hasMoreTokens()) {
				String jarLocation = tokenizer.nextToken();
				try {
					checkJar(new File(jarLocation));
				} catch (Exception e) {
					LOGGER.error("Exception checking jar : " + jarLocation, e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception scanning the classpath : ", e);
		}
	}

	/**
	 * Checks a single file for properties entry. This file will typically be a jar file.
	 * 
	 * @param file the file to check for properties entries
	 */
	protected void checkJar(File file) {
		if (file == null || !file.isFile() || !file.canRead()) {
			return;
		}
		try {
			checkJar(new JarFile(file));
		} catch (Exception e) {
			LOGGER.error("Exception accessing the properties in jar file : " + file, e);
		}
	}

	/**
	 * Checks a jar file for properties entries that match the pattern for properties names.
	 * 
	 * @param jarFile the jar file to check for properties files
	 */
	protected void checkJar(JarFile jarFile) {
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			try {
				JarEntry jarEntry = jarEntries.nextElement();
				String entryName = jarEntry.getName();
				if (fileNamePattern != null && Pattern.compile(".*(" + fileNamePattern + ").*").matcher(entryName).matches()) {
					LOGGER.info("Jar file : " + jarFile.getName() + ", " + jarEntry);
					InputStream inputStream = jarFile.getInputStream(jarEntry);
					this.load(inputStream);
				}
			} catch (Exception e) {
				LOGGER.error("Exception loading properties file from jar : " + jarFile, e);
			}
		}
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}

}