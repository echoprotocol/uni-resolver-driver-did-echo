package uniresolver.driver.did.echo;

import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import did.DIDDocument;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DidEchoDriver implements Driver {

    public String mainnetKey = "rpcUrlMainnet";

    public String testnetKey = "rpcUrlTestnet";

    public String devnetKey = "rpcUrlDevnet";

    public String defaultMainnet = "http://localhost:8090/rpc";

    public String defaultTestnet = "http://localhost:8090/rpc";

    public String defaultDevnet = "http://localhost:8090/rpc";

    public static final Pattern DID_ECHO_PATTERN_METHOD = Pattern.compile("^did:echo:(255|0|1|2).(\\d+\\.\\d+\\.\\d+)$");

    private static Logger log = LoggerFactory.getLogger(DidEchoDriver.class);

    private Map<String, Object> properties = new HashMap<String, Object> ();

    public DidEchoDriver() {
        try {
            String envRpcUrlMainnet = System.getenv("uniresolver_driver_did_echo_mainnet_rpc_url");
            String envRpcUrlTestnet = System.getenv("uniresolver_driver_did_echo_testnet_rpc_url");
            String envRpcUrlDevnet = System.getenv("uniresolver_driver_did_echo_devnet_rpc_url");

			if (envRpcUrlMainnet != null) {
                properties.put(mainnetKey, envRpcUrlMainnet);
            } else {
                properties.put(mainnetKey, defaultMainnet);
            }

			if (envRpcUrlTestnet != null) {
                properties.put(testnetKey, envRpcUrlTestnet);
            } else {
                properties.put(testnetKey, defaultTestnet);
            }

			if (envRpcUrlDevnet != null) {
                properties.put(devnetKey, envRpcUrlDevnet);
            } else {
                properties.put(devnetKey, defaultDevnet);
            }

            log.info("Loading from environment:\tmainnet = " + properties.get(mainnetKey) +
                    "\ttestnet = " + properties.get(testnetKey) + "\tdevnet = " + properties.get(devnetKey));
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
    }

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
        Matcher matcher = DID_ECHO_PATTERN_METHOD.matcher(identifier);
		if (!matcher.matches()) {
            throw new ResolutionException("DID method doesn't match pattern!");
        }

        String typeOfNetwork = matcher.group(1);
        String objectId = matcher.group(2);
        String getObjectString = "{\"jsonrpc\": \"2.0\", \"params\": [\"did\", \"get_did_object\", [\"" +
                                 typeOfNetwork + "." + objectId + "\"]], \"method\": \"call\", \"id\": 1}";
        log.info("JSON Request: " + getObjectString);

        ResolveResult resolveResult = null;

        try {
            URL url;
            /// 255 - is undefined network
            if (typeOfNetwork.equals("0") || typeOfNetwork.equals("255")) {
                url = new URL(String.valueOf(properties.get(mainnetKey)));
            } else if (typeOfNetwork.equals("1")) {
                url = new URL(String.valueOf(properties.get(testnetKey)));
            } else if (typeOfNetwork.equals("2")) {
                url = new URL(String.valueOf(properties.get(devnetKey)));
            } else {
                throw new ResolutionException("Bad network type:" + typeOfNetwork + "!");
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(getObjectString.getBytes());
            os.flush();

            String response = "";
            BufferedReader br = null;
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                response += strCurrentLine;
            }

            JsonParser parser = new JsonParser();
            JsonObject responseJson = (JsonObject) parser.parse(new StringReader(response));
            String didInJson = responseJson.get("result").getAsString();

            resolveResult = ResolveResult.build(DIDDocument.fromJson(didInJson));

        } catch (java.net.MalformedURLException e) {
            throw new ResolutionException("Bad url of node! " + e.getMessage());
        } catch (java.net.ConnectException e) {
            throw new ResolutionException("Bad connection! " + e.getMessage());
        } catch (java.net.ProtocolException e) {
            throw new ResolutionException("Protocol exception! " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new ResolutionException("IO exception! Can be invalid json response! " + e.getMessage());
        }
        
        return resolveResult;
    }

	@Override
	public Map<String, Object> properties() {
		return properties;
	}
}