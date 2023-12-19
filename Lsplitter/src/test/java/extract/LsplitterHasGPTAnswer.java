package extract;

import util.Utils;

import java.io.File;
import java.io.IOException;

public class LsplitterHasGPTAnswer {
    public static void main(String[] args) throws IOException, InterruptedException {
        String apiKey = "sk-xxxxxx"; // // Your APIKEY
        String apiUrl = "https://api.openai.com/";
        for (int i = 1; i <= 60; i++) {
            if (i == 24 || i == 60) continue;
            String originalFile = "pathToTheOriginalFile" + i + "_original.java";
            File original = new File(originalFile);
            String gptFile = "pathToTheGPTResult" + i + ".java";
            File gpt = new File(gptFile);
            extractMethod extract = new extractMethod(original, apiKey, apiUrl, gpt);
            extract.getExtractedMethod();
            System.out.println(i + " finished!");
            String resFile = "pathToSaveLsplitterResult" + i + ".java";
            Utils.writeToFile(extract.resMethod, new File(resFile));
        }
    }
}
