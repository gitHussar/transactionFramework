package pl.githussar.tx;

import java.io.Serializable;

public interface Operation extends Serializable{

	enum Status {
		OK,
		ERROR,
	}

	public Status prepareTransaction(String operationId);
	
	public Status commit(String operationId);
	
	public Status rollback(String operationId);
	
	public Status revert(String operationId);
	
}
