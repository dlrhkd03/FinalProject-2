package chatbot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64; 
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
@Service
public class NaverChatbotService implements NaverService {
	@Override
	public String test(String voiceMessage) {
		return test(voiceMessage, "send");
	}
	public String test(String voiceMessage, String event) {
		String apiURL = "https://edebfac940cd47bf8f16a0c8c28cd6bd.apigw.ntruss.com/custom/v1/5584/b915fa331486cad3d49010934caf19fc4c31302a9c175967c94c9309cae30344";
        String secretKey = "WVpoSGNmTUpBUmZsdmtBWmd3enRHTnhBR213Y3lhR1o=";
		String chatbotMessage = "";

        try {
            URL url = new URL(apiURL);
            String message = getReqMessage(voiceMessage, event); 
            System.out.println("##" + message);
            String encodeBase64String = makeSignature(message, secretKey); 
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json;UTF-8");
            con.setRequestProperty("X-NCP-CHATBOT_SIGNATURE", encodeBase64String);

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(message.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) {  
                System.out.println(con.getResponseMessage()); 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                con.getInputStream()));
                String decodedString;
                while ((decodedString = in.readLine()) != null) {
                    chatbotMessage = decodedString;
                }
                in.close();
            } else {  
               chatbotMessage = con.getResponseMessage();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("서비스출력:" + chatbotMessage);
        return chatbotMessage;
    }

    public static String makeSignature(String message, String secretKey) {
        String encodeBase64String = "";
        try {
            byte[] secrete_key_bytes = secretKey.getBytes("UTF-8");

            SecretKeySpec signingKey = new SecretKeySpec(secrete_key_bytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            
            encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
            return encodeBase64String;

        } catch (Exception e){
            System.out.println(e);
        }
        return encodeBase64String;
    }

    public static String getReqMessage(String voiceMessage, String event) {
        String requestBody = "";
        try {
            JSONObject obj = new JSONObject();
            long timestamp = new Date().getTime();
            System.out.println("##"+timestamp); 

            obj.put("version", "v2");
            obj.put("userId", "U47b00b58c90f8e47428af8b7bddc1231heo2");

            obj.put("timestamp", timestamp);

            JSONObject bubbles_obj = new JSONObject();

            bubbles_obj.put("type", "text");

            JSONObject data_obj = new JSONObject();
            data_obj.put("description", voiceMessage); //질문내용 

            bubbles_obj.put("type", "text");
            bubbles_obj.put("data", data_obj);

            JSONArray bubbles_array = new JSONArray();
            bubbles_array.put(bubbles_obj);

            obj.put("bubbles", bubbles_array);
            obj.put("event", event); 
  
            requestBody = obj.toString();

        } catch (Exception e){
            System.out.println("## Exception : " + e);
        }
        return requestBody;
    }
}
