package com.ImageTypers;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by icebox on 22/05/17.
 */
public class ImageTypersAPI {
    // consts
    private static String CAPTCHA_ENDPOINT = "http://captchatypers.com/Forms/UploadFileAndGetTextNEW.ashx";
    private static String RECAPTCHA_SUBMIT_ENDPOINT = "http://captchatypers.com/captchaapi/UploadRecaptchaV1.ashx";
    private static String RECAPTCHA_RETRIEVE_ENDPOINT = "http://captchatypers.com/captchaapi/GetRecaptchaText.ashx";
    private static String BALANCE_ENDPOINT = "http://captchatypers.com/Forms/RequestBalance.ashx";
    private static String BAD_IMAGE_ENDPOINT = "http://captchatypers.com/Forms/SetBadImage.ashx";
    private static String USER_AGENT = "pythonAPI1.0";      // user agent used in requests

    private String _username;
    private String _password;
    private String _ref_id = "0";

    private Captcha _captcha;
    private Recaptcha _recaptcha;

    private String _error = "";

    public ImageTypersAPI(String username, String password)
    {
        this._username = username;
        this._password = password;
    }
    // Constructor with refid
    public ImageTypersAPI(String username, String password, String ref_id)
    {
        this._username = username;
        this._password = password;
        this._ref_id = ref_id;
    }

    // solve normal captcha
    public String solve_captcha(String captcha_path, boolean case_sensitive) throws Exception {
        File f = new File(captcha_path);
        // check if file exists
        if(!f.exists())
        {
            throw new Exception(String.format("File does not exist: %s", captcha_path));
        }

        String base64 = DatatypeConverter.printBase64Binary(Files.readAllBytes(
                Paths.get(captcha_path)));

        // file exists, create params of request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "UPLOADCAPTCHA");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("chkCase", (case_sensitive) ? 1 : 0);
        params.put("refid", this._ref_id);
        params.put("file", base64);

        // do request
        String response = Utils.post(CAPTCHA_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            this._error = resp_err;         // save last error
            throw new Exception(resp_err);
        }

        // we have a good response here, create captcha obj
        Captcha c = new Captcha(response);
        this._captcha = c;      // save captcha to obj

        return this._captcha.text();
    }

    // Submit recaptcha
    public String submit_recaptcha(String page_url, String sitekey) throws Exception {
        // check vars first
        if(page_url.isEmpty())
        {
            throw new Exception("page_url variable is null or empty");
        }
        if(sitekey.isEmpty())
        {
            throw new Exception("sitekey variable is null or empty");
        }

        // create params with request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "UPLOADCAPTCHA");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("pageurl", page_url);
        params.put("googlekey", sitekey);
        params.put("refid", this._ref_id);

        // do request
        String response = Utils.post(RECAPTCHA_SUBMIT_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            this._error = resp_err;         // save last error
            throw new Exception(resp_err);
        }

        // create recaptcha object with received captcha ID
        Recaptcha rc = new Recaptcha(response);
        this._recaptcha = rc;       // save to obj

        return response;        // return response
    }

    // Submit recaptcha with proxy
    public String submit_recaptcha(String page_url, String sitekey, String proxy, String proxy_type) throws Exception {
        // check vars first
        if(page_url.isEmpty())
        {
            throw new Exception("page_url variable is null or empty");
        }
        if(sitekey.isEmpty())
        {
            throw new Exception("sitekey variable is null or empty");
        }

        // create params with request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "UPLOADCAPTCHA");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("pageurl", page_url);
        params.put("googlekey", sitekey);
        params.put("proxy", proxy);                 // with proxy
        params.put("proxy_type", proxy_type);       // and proxy type
        params.put("refid", this._ref_id);

        // do request
        String response = Utils.post(RECAPTCHA_SUBMIT_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            this._error = resp_err;         // save last error
            throw new Exception(resp_err);
        }

        // create recaptcha object with received captcha ID
        Recaptcha rc = new Recaptcha(response);
        this._recaptcha = rc;       // save to obj

        return response;        // return response
    }

    // Retrieve captcha response using captcha ID
    public String retrieve_captcha(String captcha_id) throws Exception {
        // file exists, create params of request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "GETTEXT");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("captchaid", captcha_id);
        params.put("refid", this._ref_id);

        // do request
        String response = Utils.post(RECAPTCHA_RETRIEVE_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            if(response.indexOf("NOT_DECODED") == -1) {     // save it as obj error, only if it's not, NOT_DECODED
                this._error = resp_err;         // save last error
            }
            throw new Exception(resp_err);
        }

        // we got a good response at this point, save it to recapthca obj and return it
        this._recaptcha = new Recaptcha(captcha_id);
        this._recaptcha.set_response(response);
        return response;
    }

    // Check if recaptcha still in progress of solving
    public boolean in_progress(String captcha_id) throws Exception {
        try
        {
            this.retrieve_captcha(captcha_id);
            return false;       // no error, we're good
        }
        catch(Exception ex)
        {
            if(ex.getMessage().contains("NOT_DECODED"))
            {
                return true;
            }
            // otherwise throw exception (if different error)
            throw ex;
        }
    }

    // Get account balance
    public String account_balance() throws Exception {
        // create params of request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "REQUESTBALANCE");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("submit", "Submit");

        // do request
        String response = Utils.post(BALANCE_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            this._error = resp_err;         // save last error
            throw new Exception(resp_err);
        }

        return String.format("$%s", response);      // return balance
    }

    // Set captcha bad
    public String set_captcha_bad(String captcha_id) throws Exception {
        // create params of request
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("action", "SETBADIMAGE");
        params.put("username", this._username);
        params.put("password", this._password);
        params.put("imageid", captcha_id);
        params.put("submit", "Submissssst");

        // do request
        String response = Utils.post(BAD_IMAGE_ENDPOINT, params, USER_AGENT);

        // check if error
        int i = response.indexOf("ERROR:");
        if(i != -1)     // it's an error
        {
            String resp_err = response.substring(6, response.length()).trim();
            this._error = resp_err;         // save last error
            throw new Exception(resp_err);
        }

        return response;        // return response
    }

    // Get last solved captcha text
    public String captcha_text()
    {
        if(this._captcha != null)
        {
            return this._captcha.text();
        }
        return "";      // return nothing if not initialized yet
    }
    // Get last solved captcha id
    public String captcha_id()
    {
        if(this._captcha != null)
        {
            return this._captcha.captcha_id();
        }
        return "";      // return nothing if not initialized yet
    }

    // Get last solved recaptcha response
    public String recaptcha_response()
    {
        if(this._recaptcha != null)
        {
            return this._recaptcha.response();
        }
        return "";      // return nothing if not initialized yet
    }
    // Get last solved recaptcha id
    public String recaptcha_id()
    {
        if(this._recaptcha != null)
        {
            return this._recaptcha.captcha_id();
        }
        return "";      // return nothing if not initialized yet
    }

    // Return last error
    public String error()
    {
        return this._error;
    }
}
