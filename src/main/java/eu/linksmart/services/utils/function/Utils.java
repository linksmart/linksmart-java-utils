
/*
 *  Copyright [2013] [Fraunhofer-Gesellschaft]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package eu.linksmart.services.utils.function;


import eu.linksmart.services.utils.configuration.Configurator;
import eu.linksmart.services.utils.constants.Const;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ScApi;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

/**
 * Provide a set of commonly needed functions. The idea of this Utility class is to centralized all code is used everyplace but not belongs specially in any place.
 *
 * @author Jose Angel Carvajal Soto
 * @since 1.0.0
 */
public class Utils {
    static private DateFormat dateFormat = getDateFormat();
    static public DateFormat
            isoFormatMSTZ = new SimpleDateFormat(Const.TIME_ISO_FORMAT_MS_TZ),
            isoFormatWMSTZ = new SimpleDateFormat(Const.TIME_ISO_FORMAT_WMS_TZ),
            isoFormatMSWTZ = new SimpleDateFormat(Const.TIME_ISO_FORMAT_MS_WTZ),
            isoFormatWMSWTZ = new SimpleDateFormat(Const.TIME_ISO_FORMAT_WMS_WTZ);

    /**
     * Provides the version of the software. The version is linked to the pom version.
     *
     * @return the pom version
     */
    public static synchronized String getVersion() {
        return getVersion("version");
    }

    public static synchronized String getVersion(String version) {
        final Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("version.properties"));
            return properties.getProperty(version);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Provide a quick method to get a data DateFormat. The DateFormat is created using the default values of the conf file if any,
     * otherwise use the hardcoded in the const.
     *
     * @return a default DateFormat
     */
    static public DateFormat getDateFormat() {
        DateFormat dateFormat;
        TimeZone tz = getTimeZone();
        if (Configurator.getDefaultConfig().getString(Const.TIME_FORMAT_CONF_PATH) == null)

            dateFormat = new SimpleDateFormat(Const.TIME_ISO_FORMAT_MS_TZ);

        else
            dateFormat = new SimpleDateFormat(Configurator.getDefaultConfig().getString(Const.TIME_FORMAT_CONF_PATH), Locale.ROOT);

        dateFormat.setTimeZone(tz);

        return dateFormat;

    }

    public static ScApi getServiceCatalogClient(String uri) {
        if (Utils.isRestAvailable(uri)) {
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(uri);
            return new ScApi(apiClient);
        }
        return null;
    }

    static public String getProtocol(String url) throws Exception {

        String[] aux = url.split("://");

        return aux[0];
    }

    static public Pair<String, Integer> getHostnamePort(String url) throws Exception {

        // JDK-6587184 : Underline Problem in java.net.URI VM 1.6.0_01
        // "fix" issue of '_' in uris
        URI uri = new URI(url.replace("--", "-0-").replace("_", "--"));
        // JDK-6587184 : Underline Problem in java.net.URI VM 1.6.0_01
        // "fix" issue of '_' in uris
        return Pair.of(uri.getHost().replace("--", "_").replace("-0-", "--"), uri.getPort());
    }

    static public Date formISO8601(String str) throws IOException {
        try {
            return getDateFormat().parse(str);
        } catch (Exception e) {
            // nothing
        }
        try {
            // doesn't uses T
            if (str.contains(" "))
                str.replace(" ", "T");
            // has Timezone
            if (!str.contains("Z")) {
                // uses ':' in timezone
                if (str.substring(str.length() - 6).contains(":")) {
                    str = str.substring(0, str.length() - 6) + str.substring(str.length() - 6).replace(":", "");
                }
                if (str.contains("."))
                    return isoFormatMSTZ.parse(str);
                else
                    return isoFormatWMSTZ.parse(str);
            } else {
                if (str.contains("."))
                    return isoFormatMSWTZ.parse(str);
                else
                    return isoFormatWMSWTZ.parse(str);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Provide a quick method to get a data TimeZone. The TimeZone is created using the default values of the conf file if any,
     * otherwise use UTC timezone.
     *
     * @return a default TimeZone
     */
    static public TimeZone getTimeZone() {
        String tzs = Configurator.getDefaultConfig().getString(Const.TIME_TIMEZONE_CONF_PATH);
        if (tzs == null || tzs.equals(""))
            tzs = "UTC";

        return TimeZone.getTimeZone(tzs);
    }

    /**
     * Provide a quick method to transform a Date as String. The String is constructed using the DateFormat obtained by getDateFormat()
     *
     * @param date to transform into a string
     * @return a Date as String
     */
    static public String getTimestamp(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Provide a quick method to transform a Date into timestamp as String. The String is constructed using a DateFormat based on the standard iso 8601
     *
     * @param date to transform into a string
     * @return a Date as String timestamp base in iso 8601
     */
    static public String getIsoTimestamp(Date date) {
        return isoFormatMSTZ.format(date);
    }

    /**
     * Provide a quick method to get a current time as String. The String is constructed using getTimestamp()
     *
     * @return current Date as a String timestamp
     */
    static public String getDateNowString() {
        return getDateFormat().format(new Date());
    }

    static boolean isLoggingConfLoaded = false;

    /**
     * Provide a quick method to get the hash value of a string as a string. The function is using SH-256 to hash
     *
     * @param string to hash
     * @return the hexadecimal value as string
     */
    public static String hashIt(String string) {
        if (string == null)
            return "";
        MessageDigest SHA256 = null;
        try {
            SHA256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
        return new BigInteger(1, SHA256.digest((string).getBytes())).toString(16);
    }

    // TODO: please remove in version 1.3.0+
    /*   *//*
     * Provide a default method and unique method to get the logging service regardless of the implementation. Additionally, for the reloading of the logging  configuration
     * @param lass is the class which want to load the logging service
     * @return the logging service
     * *//*
    public static Logger initLoggingConf(Class lass){
        Logger loggerService = null;
        try {
            Properties p = new Properties();
            String message=null;

            if(!isLoggingConfLoaded) {

                if (isFile(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile))) {
                    //loading from file system
                    final FileInputStream configStream = new FileInputStream(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile));
                    p.load(configStream);
                    PropertyConfigurator.configure(p);
                    configStream.close();
                    System.setProperty("log4j.configuration", Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile));
                    message = "Loading from configuration from given file";
                } else if (isResource(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile), lass)) {
                    //loading from class resource file
                    InputStream in = lass.getClassLoader().getResourceAsStream(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile));
                    p.load(in);
                    PropertyConfigurator.configure(p);
                    in.close();
                    message = "Loading from configuration from jar default file";
                } else if (Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile)!=null&&isResource(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile))) {
                    //loading from Utils class resource file
                    InputStream in = Utils.class.getClassLoader().getResourceAsStream(Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile));
                    p.load(in);
                    System.setProperty("log4j.configuration", Configurator.getDefaultConfig(lass).getString(Const.LoggingDefaultLoggingFile));
                    PropertyConfigurator.configure(p);
                    in.close();
                    message = "Loading from configuration from Utils jar default file (last resort!)";
                } else //not loading any configuration file
                    message="No logging configuration file found!";

                loggerService = LoggerFactory.getLogger(lass);
                loggerService.info(message);
                isLoggingConfLoaded =true;
            }else {
                loggerService = LoggerFactory.getLogger(lass);
                loggerService.debug("Ignoring reloading of logging configuration file because has bean already loaded");
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;

        }

        loggerService.debug("Logging configuration file had been initialized");
        return loggerService;

    }*/
    // TODO: No Unit test
    /*
     * Provide a quick method to force the reloading of the logging service infrastructure using initLoggingConf(Utils.class)
     *
    public static void initLoggingConf(){
        initLoggingConf(Utils.class);

    }*/

    // TODO: No Unit test

    /**
     * Provide a quick method to construct a SSLSocketFactory which is a TCP socket using TLS/SSL
     *
     * @param trustStore         location of the trust store
     * @param keyStore           location of the key store
     * @param trustStorePassword password to access the trust store
     * @param keyStorePassword   password to access the key store
     * @return the SSLSocketFactory to create secure sockets with the provided certificates infrastructure
     * @throws java.lang.Exception in case of something wrong happens
     */
    static public SSLSocketFactory getSocketFactory(final String trustStore, final String keyStore, final String trustStorePassword, final String keyStorePassword) throws Exception {

        // todo check if the CA needs or can use the password
        final FileInputStream trustStoreStream = new FileInputStream(trustStore);
        final FileInputStream keyStoreStream = new FileInputStream(keyStore);
        // CA certificate is used to authenticate server
        final KeyStore caKs = KeyStore.getInstance("JKS");
        caKs.load(trustStoreStream, trustStorePassword.toCharArray());
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(caKs);

        trustStoreStream.close();

        // client key and certificates are sent to server so it can authenticate us
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(keyStoreStream, keyStorePassword.toCharArray());
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(ks, keyStorePassword.toCharArray());

        keyStoreStream.close();

        // finally, create SSL socket factory
        final SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    /**
     * Provide a quick method to construct a SSLSocketFactory which is a TCP socket using TLS/SSL
     *
     * @param caPem
     * @param clientCertPem
     * @param clientKeyPem
     * @param clientCertPass
     * @param clientKeyPass
     * @return the SSLSocketFactory to create secure sockets with the provided certificates infrastructure
     * @throws java.lang.Exception in case of something wrong happens
     */
    static public SSLSocketFactory getSocketFactory(final String caPem, final String clientCertPem, final String clientKeyPem, String clientCertPass, String clientKeyPass) throws Exception {
        String pass = UUID.randomUUID().toString();
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(getTrustStore(caPem, pass));

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        tmf.init(getKeyStore(clientCertPem, clientKeyPem, caPem, pass));


        // finally, create SSL socket factory
        final SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    /**
     * Returns a SSL Factory instance that accepts all server certificates.
     * <pre>SSLSocket sock =
     *     (SSLSocket) getSocketFactory.createSocket ; </pre>
     *
     * @return An SSL-specific socket factory.
     **/
    public static final SSLSocketFactory getSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLSocketFactory sslSocketFactory;

        TrustManager[] tm = new TrustManager[]{new NaiveTrustManager()};
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(new KeyManager[0], tm, new SecureRandom());

        sslSocketFactory = context.getSocketFactory();


        return sslSocketFactory;
    }

    /**
     * Provide a quick method to find out if file exists (in the filesystem or as JAR resource)
     *
     * @param filename the name of the file to check
     * @return true if the file exists
     */
    public static boolean fileExists(String filename) {

        return isFile(filename) || isResource(filename);
    }

    /**
     * Provide a quick method to find out if file exists in the file system
     *
     * @param filename the name of the file to check
     * @return true if the file exists
     */
    public static boolean isFile(String filename) {
        if (filename == null)
            return false;
        File f = new File(filename);
        return (f.exists() && !f.isDirectory());
    }

    /**
     * Provide a quick method to find out if file exists in the current JAR
     *
     * @param filename the name of the file to check
     * @return true if the file exists
     */
    public static boolean isResource(String filename) {

        return Thread.currentThread().getContextClassLoader().getResource(filename) != null;
    }

    /**
     * Provide a quick method to find out if file exists in the JAR of the class loader of the Class clazz
     *
     * @param filename the name of the file to check
     * @param clazz    JAR where the file should be located
     * @return true if the file exists
     */
    public static boolean isResource(String filename, Class clazz) {
        return !(filename == null || clazz == null) && clazz.getClassLoader().getResource(filename) != null;
    }

    /**
     * Provide a quick method to construct a property object out of a file name
     *
     * @param source the name and location of the property file
     * @return the property object if the file exist
     * @throws IOException if the source file do not exist
     */
    public static Properties createPropertyFiles(String source) throws IOException {
        Properties properties = new Properties();
        if (isFile(source))
            properties.load(new FileInputStream(source));
        else if (isResource(source))
            properties.load(Utils.class.getClassLoader().getResourceAsStream(source));

        return properties;
    }

    public static String runGetLastOutput(String[] cmd, String moduleName, Logger loggerService) throws IOException {
        Process proc = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String readLine = stdInput.readLine();

        new Thread(() -> {
            String s = null;
            try {
                while ((s = stdInput.readLine()) != null) {
                    loggerService.info(moduleName + ": {}", s);
                }
                // errors
                while ((s = stdError.readLine()) != null) {
                    loggerService.error(moduleName + ": {}", s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return readLine;
    }

    /**
     * Test if given endpoint exists
     *
     * @param url of service to test
     * @return true if exists, false otherwise
     */
    public static boolean isRestAvailable(String url) {

        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            if (code == 200) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static KeyStore getKeyStore(String clientCertPem, String privateKeyPem, String caPem, String password) throws IOException {
        try {
            Certificate clientCertificate = loadCertificate(clientCertPem);
            PrivateKey privateKey = loadPrivateKey(privateKeyPem);
            Certificate caCertificate = loadCertificate(caPem);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca-cert", caCertificate);
            keyStore.setCertificateEntry("client-cert", clientCertificate);
            keyStore.setKeyEntry("client-key", privateKey, password.toCharArray(), new Certificate[]{clientCertificate});
            return keyStore;
        } catch (GeneralSecurityException | IOException e) {
            throw new IOException("Cannot build keystore", e);
        }
    }

    private static KeyStore getTrustStore(String caPem, String password) throws IOException {
        try {
            Certificate caCertificate = loadCertificate(caPem);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca-cert", caCertificate);
            return keyStore;
        } catch (GeneralSecurityException | IOException e) {
            throw new IOException("Cannot build keystore", e);
        }
    }

    private static Certificate loadCertificate(String certificatePem) throws IOException, GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        final byte[] content = readPemContent(certificatePem);
        return certificateFactory.generateCertificate(new ByteArrayInputStream(content));
    }

    private static PrivateKey loadPrivateKey(String privateKeyPem) throws IOException, GeneralSecurityException {
        return pemLoadPrivateKeyPkcs1OrPkcs8Encoded(privateKeyPem);
    }

    private static byte[] readPemContent(String pem) throws IOException {
        final byte[] content;
        try (PemReader pemReader = new PemReader(new StringReader(pem))) {
            final PemObject pemObject = pemReader.readPemObject();
            content = pemObject.getContent();
        }
        return content;
    }

    private static PrivateKey pemLoadPrivateKeyPkcs1OrPkcs8Encoded(
            String privateKeyPem) throws GeneralSecurityException, IOException {
        // PKCS#8 format
        final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";
        final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

        // PKCS#1 format
        final String PEM_RSA_PRIVATE_START = "-----BEGIN RSA PRIVATE KEY-----";
        final String PEM_RSA_PRIVATE_END = "-----END RSA PRIVATE KEY-----";

        if (privateKeyPem.contains(PEM_PRIVATE_START)) { // PKCS#8 format
            privateKeyPem = privateKeyPem.replace(PEM_PRIVATE_START, "").replace(PEM_PRIVATE_END, "");
            privateKeyPem = privateKeyPem.replaceAll("\\s", "");

            byte[] pkcs8EncodedKey = Base64.getDecoder().decode(privateKeyPem);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKey));

        } else if (privateKeyPem.contains(PEM_RSA_PRIVATE_START)) {  // PKCS#1 format

            privateKeyPem = privateKeyPem.replace(PEM_RSA_PRIVATE_START, "").replace(PEM_RSA_PRIVATE_END, "");
            privateKeyPem = privateKeyPem.replaceAll("\\s", "");

            ASN1InputStream asn1Reader = new ASN1InputStream(Base64.getDecoder().decode(privateKeyPem));
            ASN1Primitive der = asn1Reader.readObject();

            RSAPrivateKey privateKey = RSAPrivateKey.getInstance(der);
            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(privateKey.getModulus(), privateKey.getPublicExponent(), privateKey.getPrivateExponent(), privateKey.getPrime1(), privateKey.getPrime2(), privateKey.getExponent1(), privateKey.getExponent2(), privateKey.getCoefficient());

            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey returnKey = factory.generatePrivate(keySpec);
            return returnKey;
        }

        throw new GeneralSecurityException("Not supported format of a private key");
    }
    public static <T> T copyObject(T objSource) {
        T objDest = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objSource);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            try {
                objDest = (T) new ObjectInputStream(bais).readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objDest;

    }
}
