package model;

public class MessageStatus {

	private String status;
	private Integer code;
	private Integer api_code;
	private String message;
	private String more_info;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Integer getApi_code() {
		return api_code;
	}

	public void setApi_code(Integer api_code) {
		this.api_code = api_code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMore_info() {
		return more_info;
	}

	public void setMore_info(String more_info) {
		this.more_info = more_info;
	}

	public MessageStatus(String status, Integer code, Integer api_code, String message, String more_info) {
		this.status = status;
		this.code = code;
		this.api_code = api_code;
		this.message = message;
		this.more_info = more_info;
	}

}
