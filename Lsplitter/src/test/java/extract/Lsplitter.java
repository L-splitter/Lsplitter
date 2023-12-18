package extract;

import util.Utils;

import java.io.File;
import java.io.IOException;

public class Lsplitter {
    public static void main(String[] args) throws IOException, InterruptedException {
        String apiKey = "sk-xxxxxx"; // Your APIKEY
        String apiUrl = "https://api.openai.com/";
        for (int i = 1; i <= 60; i++) {
            String originalFile = "pathToTheOriginalFile" + i + "_original.java";
            File original = new File(originalFile);
            extractMethod extract = new extractMethod(original, apiKey, apiUrl, null);
            extract.getExtractedMethod();
            System.out.println(i + " finished!");
            String gptFile = "pathToSaveGPTResult" + i + ".java";
            String resFile = "pathToSaveLspliiterResult" + i + ".java";
            Utils.writeToFile(extract.gptMethod, new File(gptFile));
            Utils.writeToFile(extract.resMethod, new File(resFile));
        }
    }
}
