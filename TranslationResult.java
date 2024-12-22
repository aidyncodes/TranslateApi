package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * The objects that will store the data from the second json response from Apyhub.
 */
public class TranslationResult {
    // match the format of json output where data is top branch
    public Data data; // nested obj

    /**
     * Nested loop to reflect format returned by Apyhub response.
     * References the next nested object of class Attributes.
     */
    public static class Data {
        public String type;
        public String id;
        public Attributes attributes; // nested obj
    }

    /**
     * Nested loop to reflect format returned by Apyhub response.
     * References the next nested object of class Result.
     */
    public static class Attributes {
        public String status;
        public String type;
        public Result result; // nested obj
    }

    /**
     * Nested loop to reflect format returned by Apyhub response.
     */
    public static class Result {
        public String content;
        @SerializedName("from_language")
        public String fromLanguage;
        @SerializedName("to_language")
        public String toLanguage;
    }

}
