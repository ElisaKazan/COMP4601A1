package edu.carleton.comp4601.exceptions;

public class DocumentNotFoundException extends Exception {
	private static final long serialVersionUID = 9032862252061375995L;
	
	private boolean pluralize;
	
	public DocumentNotFoundException(boolean pluralize) {
		this.pluralize = pluralize;
	}

	@Override
	public String getMessage() {
		return "Document" + (pluralize ? "s" : "") + " not found";
	}
}
