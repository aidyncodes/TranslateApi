package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a slip object returned by the Advice Slip API.
 * A slip contains an identifier (id) and a piece of advice.
 * Must account for nested objects in response.
 */
public class QuoteResult {
    public Slip slip;

    /**
     * Nested class to match provided Advice API.
     */
    public static class Slip {
        @SerializedName("slip_id")
        public String slipId;
        public String advice;
    }
}
