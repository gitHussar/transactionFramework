package pl.githussar.tx;

public interface Operation {

	enum Status {
		OK,
		ERROR,
	}

	public Status prepareTransaction(String operationId);
	
	public Status commit(String operationId);
	
	public Status rollback(String operationId);
	
	public Status invert(String operationId);
	
}
