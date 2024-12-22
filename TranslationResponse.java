package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Stores first json response from Apyhub in objects status_url and job_id.
 */
public class TranslationResponse {
    @SerializedName("status_url")
    public String statusUrl;
    @SerializedName("job_id")
    public String jobId;
}
