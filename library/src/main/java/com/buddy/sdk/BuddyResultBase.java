package com.buddy.sdk;

public class BuddyResultBase {

	protected JsonEnvelopeBase env = new JsonEnvelopeBase();
	
    public BuddyResultBase() {
    }

    public BuddyResultBase(JsonEnvelopeBase env) {
        this.env = env;
    }

    public String getError() {
    	
        return env.error;
    }

    public String getErrorMessage() {
        return env.message;
    }
    
    public int getErrorCode() {

        return env.errorCode;
    }

   
    public boolean getIsSuccess(){
        return getError() == null;
    }

    public String getRequestID(){
        return env.request_id;
    }
   

}