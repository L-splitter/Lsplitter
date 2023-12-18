package chatGPT;

import com.plexpt.chatgpt.ChatGPT;

public class queryGPT {
    public static String query(String apiKey, String apiUrl, String question) throws InterruptedException {
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey(apiKey)
                .apiHost(apiUrl)
                .build()
                .init();
        try {
            return chatGPT.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(1000);
            return query(apiKey, apiUrl, question);
        }
    }
}
