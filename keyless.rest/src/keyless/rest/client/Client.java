package keyless.rest.client;

public class Client {
	
	String NAME;
	String UUID;
	String OTP;
	String SECRET;
	
	public String getName(){
		return NAME;
	}
	
	public String getUuid(){
		return UUID;
	}
	
	public String getOtp(){
		return OTP;
	}
	
	public String getSecret(){
		return SECRET;
	}

	public void setName(String n){
		this.NAME = n;
	}
	
	public void setUuid(String u){
		this.UUID = u;
	}
	
	public void setOtp(String o){
		this.OTP = o;
	}
	
	public void setSecret(String s){
		this.SECRET = s;
	}
	
	@Override
	public String toString() {
		return new StringBuffer(" Name: ").append(this.NAME)
		                .append(" UUID : ").append(this.UUID)
		                .append(" OTP: ").append(this.OTP)
		                .append(" Secret: ").append(this.SECRET).toString();
	}
}
