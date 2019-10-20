package com.roy.football.match.logging;

public enum ErrorType {
	// Base: 1 ~ 999;
	Default(0, "This is the default error message."),
	UnableToParseURL(1, "The input URL [%s] is unabled to be parsed."),
    InvalidRequestMethod(2, "The %s request method is not supported."),
    UrlUnableToConnect(3, "Unable to open the connection to URL: [%s]"),
    HttpRequestInterrupted(4, "The [%s] http request is interrupted for timeout or some other causes."),
    UnableWriteData(5, "Unable to write data into stream."),
    UnableReadData(6, "Unable to read data from stream."),
    UnableRedirectToUrl(7, "Unable to redirect to URL %s"),
    UnableParseXMLToObject(8, "Unable to parse XML to match the class [%s] object."),
    ServiceAbnormal(50, "Service respond abnormal."),
    
    MissingArgumentError(9, "Missing required arguments: "),
    
    NoFormatKeyError(10,"Missing the format key %s."),
    NotSupportedValue(11, "Passed in value is not supported."),

    InvalidCellValue(20, "The cell [row:%d, col:%d, val: %s] value is invalid."),
	EmptyCellValue(21, "The cell [row:%d, col:%d] is empty."),
	InvalidSkuCellValue(22, "The SKU [row:%d, col:%d, val: %s] is not in the preset list."),
	InvalidDateCellValue(23, "The date value [%s] is invalid."),
	InvalidHeaderCellValue(24, "The headers don't match the templet."),
	EmptyListingInExcel(25, "The uploaded file contains no listings."),
	DuplicateItemFound(26, "Duplicate item id was found."),
	UnmatchedCellType(27, "The cell type [%s] is not matching field type at row [%d] col [%d]."),
	UnsupportCellType(28, "The cell type [%s] is not supportted."),
	UnsupportFieldType(29, "The field data type [%s] is not supportted to define field for excel use."),
	
	UnableUploadDealsListing(30, "Unable to upload deals listings, with error message [%s]"),
	UnableSubmitDealsListing(31, "Unable to submit deals listings, with error message [%s]"),
	DateExpiredException(32, "The [%s] date value [%s] is expired."),
	
	UnableToPerseMatchData(100, "Unable to parse match data with detail [%s]"),;
	
	ErrorType(int code, String errorMsg) {
		this.code = code;
		this.errorMsg = errorMsg;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public static ErrorType getErrorTypeByCode (int errorCode) {
		for (ErrorType type : ErrorType.values()) {
			if (errorCode == type.getCode()) {
				return type;
			}
		}
		
		return ErrorType.Default;
	}

	private int code;
	private String errorMsg;
}
